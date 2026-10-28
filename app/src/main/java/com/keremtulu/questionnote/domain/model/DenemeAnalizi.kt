package com.keremtulu.questionnote.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun Long.yerelEpochGun(): Int =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay().toInt()

fun Int.yerelTarih(): LocalDate = LocalDate.ofEpochDay(toLong())

fun List<DenemeKaydi>.kronolojik(): List<DenemeKaydi> =
    sortedWith(compareBy<DenemeKaydi> { it.gun }.thenBy { it.olusturma })

fun List<DenemeKaydi>.grafikNoktalari(limit: Int = 12): List<DenemeKaydi> =
    kronolojik().takeLast(limit)

fun List<DenemeKaydi>.karisikKural(): Boolean =
    map { it.kural }.distinct().size > 1

/** A total net is comparable only when the scoring rule and every lesson's question count match.
 * Zero-question rows do not change the scope. Invalid/ambiguous legacy rows never define a group.
 * This does not claim the exams have the same difficulty or official exam format.
 */
private fun DenemeKaydi.kapsamAnahtari(): Map<String, Int>? {
    if (satirlar.any { !it.gecerli() } || satirlar.map { it.dersId }.distinct().size != satirlar.size) return null
    return satirlar.filter { it.soruSayisi > 0 }
        .associate { it.dersId to it.soruSayisi }
        .takeIf { it.isNotEmpty() }
}

fun DenemeKaydi.ayniKapsamVeKural(diger: DenemeKaydi): Boolean {
    if (kural != diger.kural) return false
    val kapsam = kapsamAnahtari() ?: return false
    return kapsam == diger.kapsamAnahtari()
}

private fun DenemeKaydi.onceMi(diger: DenemeKaydi): Boolean =
    gun < diger.gun || (gun == diger.gun && olusturma < diger.olusturma)

/** Keep the selected record visible even if a legacy record has an unusable scope. */
fun List<DenemeKaydi>.karsilastirilabilirler(son: DenemeKaydi): List<DenemeKaydi> =
    filter { it.id == son.id || it.ayniKapsamVeKural(son) }.kronolojik()

/** Latest record for every distinct comparable scope, newest groups first. */
fun List<DenemeKaydi>.kapsamTemsilcileri(): List<DenemeKaydi> {
    val gorulen = mutableSetOf<Pair<NetKurali, Map<String, Int>>>()
    return kronolojik().asReversed().filter { deneme ->
        val kapsam = deneme.kapsamAnahtari()
        kapsam == null || gorulen.add(deneme.kural to kapsam)
    }
}

fun List<DenemeKaydi>.oncekiKarsilastirilabilir(son: DenemeKaydi): DenemeKaydi? =
    filter { it.id != son.id && it.onceMi(son) && it.ayniKapsamVeKural(son) }
        .kronolojik().lastOrNull()

data class DersNetKarsilastirma(
    val dersId: String,
    val sonNet: Double,
    val oncekiOrtalama: Double?,
    val dersAdi: String? = null,
    val oncekiDenemeSayisi: Int = 0,
)

fun List<DenemeKaydi>.dersBarlari(son: DenemeKaydi): List<DersNetKarsilastirma> {
    // A record opened from history must never use a later exam as its "previous" average.
    val onceki = filter { it.id != son.id && it.onceMi(son) && it.ayniKapsamVeKural(son) }
    return son.satirlar.filter { it.soruSayisi > 0 }.map { satir ->
        val oncekiNetler = onceki.mapNotNull { it.satir(satir.dersId) }
            .map { son.kural.net(it.dogru, it.yanlis) }
        DersNetKarsilastirma(
            dersId = satir.dersId,
            sonNet = son.kural.net(satir.dogru, satir.yanlis),
            oncekiOrtalama = oncekiNetler.takeIf { it.isNotEmpty() }?.average(),
            dersAdi = satir.dersAdi,
            oncekiDenemeSayisi = oncekiNetler.size,
        )
    }
}

enum class DenemeAdiDurumu { GECERLI, BOS, COK_UZUN, AYNI_AD_VAR, ESKI_AD_PAYLASILMIS }

/** Sources are currently linked by normalized name, not by exam ID. Do not create ambiguous links. */
fun List<DenemeKaydi>.denemeAdiDogrula(ad: String, mevcutId: String?): DenemeAdiDurumu {
    val temiz = ad.trim()
    if (temiz.isEmpty()) return DenemeAdiDurumu.BOS
    val mevcut = firstOrNull { it.id == mevcutId }
    val eskiAnahtar = mevcut?.ad?.let(::normalizeSoruSecenegiAdi)
    val yeniAnahtar = normalizeSoruSecenegiAdi(temiz)
    val adDegisti = eskiAnahtar != yeniAnahtar
    // Existing long names may still be edited without being forced to rename old data.
    if (temiz.length > SORU_SECENEGI_ADI_UST_SINIR && (mevcut == null || temiz != mevcut.ad.trim())) {
        return DenemeAdiDurumu.COK_UZUN
    }
    if (adDegisti && any { it.id != mevcutId && normalizeSoruSecenegiAdi(it.ad) == yeniAnahtar }) {
        return DenemeAdiDurumu.AYNI_AD_VAR
    }
    if (adDegisti && eskiAnahtar != null && any { it.id != mevcutId && normalizeSoruSecenegiAdi(it.ad) == eskiAnahtar }) {
        return DenemeAdiDurumu.ESKI_AD_PAYLASILMIS
    }
    return DenemeAdiDurumu.GECERLI
}

fun denemeKaynakEslesir(bir: String?, iki: String?): Boolean {
    if (bir.isNullOrBlank() || iki.isNullOrBlank()) return false
    return normalizeSoruSecenegiAdi(bir) == normalizeSoruSecenegiAdi(iki)
}

fun List<DenemeKaydi>.kaynakla(ad: String?): DenemeKaydi? {
    if (ad.isNullOrBlank()) return null
    return firstOrNull { denemeKaynakEslesir(it.ad, ad) }
}

fun List<DenemeKaydi>.oncekiAyniKural(son: DenemeKaydi): DenemeKaydi? =
    kronolojik()
        .filter { it.kural == son.kural && it.id != son.id }
        .lastOrNull { it.gun < son.gun || (it.gun == son.gun && it.olusturma < son.olusturma) }

fun DenemeKaydi.enZayif(): DenemeDersSatiri? =
    satirlar.minByOrNull { kural.net(it.dogru, it.yanlis) }

fun denemeHatirlatmaIndeksi(soruId: String): Int =
    (soruId.hashCode() and Int.MAX_VALUE) % 4

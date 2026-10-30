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

data class DersNetKarsilastirma(
    val dersId: String,
    val sonNet: Double,
    val oncekiOrtalama: Double?,
    val dersAdi: String? = null,
)

fun List<DenemeKaydi>.dersBarlari(son: DenemeKaydi): List<DersNetKarsilastirma> {
    val oncekiDenemeler = filter { it.kural == son.kural && it.id != son.id && it.sondanOnce(son) }
    return son.satirlar.map { satir ->
        // A 10-question lesson and a 40-question lesson do not have comparable raw nets.
        val oncekiler = oncekiDenemeler.mapNotNull { it.satir(satir.dersId) }
            .filter { it.soruSayisi == satir.soruSayisi && it.soruSayisi > 0 }
            .map { son.kural.net(it.dogru, it.yanlis) }
        DersNetKarsilastirma(
            dersId = satir.dersId,
            sonNet = son.kural.net(satir.dogru, satir.yanlis),
            oncekiOrtalama = oncekiler.takeIf { it.isNotEmpty() }?.average(),
            dersAdi = satir.dersAdi,
        )
    }
}

fun denemeKaynakEslesir(bir: String?, iki: String?): Boolean {
    if (bir.isNullOrBlank() || iki.isNullOrBlank()) return false
    return normalizeSoruSecenegiAdi(bir) == normalizeSoruSecenegiAdi(iki)
}

fun List<DenemeKaydi>.kaynakla(ad: String?): DenemeKaydi? {
    if (ad.isNullOrBlank()) return null
    // Source names are the only link in existing data; duplicates cannot be attributed safely.
    return filter { denemeKaynakEslesir(it.ad, ad) }.singleOrNull()?.takeUnless { it.kaynakBelirsiz }
}

private fun DenemeKaydi.sondanOnce(son: DenemeKaydi): Boolean =
    gun < son.gun || (gun == son.gun && olusturma < son.olusturma)

/** Absolute net differences are meaningful only for the same rule and lesson/question makeup. */
fun DenemeKaydi.karsilastirilabilir(diger: DenemeKaydi): Boolean =
    kural == diger.kural && satirlar.isNotEmpty() &&
        satirlar.all { it.soruSayisi > 0 } && diger.satirlar.all { it.soruSayisi > 0 } &&
        satirlar.associate { it.dersId to it.soruSayisi } ==
            diger.satirlar.associate { it.dersId to it.soruSayisi }

fun List<DenemeKaydi>.oncekiAyniKural(son: DenemeKaydi): DenemeKaydi? =
    kronolojik().lastOrNull { it.id != son.id && it.sondanOnce(son) && it.karsilastirilabilir(son) }

/** This is a per-exam observation, not a claim about a student's overall ability. */
fun DenemeKaydi.enZayif(): DenemeDersSatiri? =
    satirlar.filter { it.gecerli() && it.soruSayisi > 0 && it.yuklenecek > 0 }
        .maxByOrNull { it.yuklenecek.toDouble() / it.soruSayisi }

data class DenemeNedenSayisi(val neden: String, val adet: Int)

data class DenemeDefterAnalizi(
    val soruSayisi: Int,
    val nedenler: List<DenemeNedenSayisi>,
    val nedeniBelirtilmeyen: Int,
    val dersSayilari: Map<String, Int>,
)

/**
 * Only currently linked notebook questions are analysed. The source is a name, not a
 * stable exam ID: an ambiguous name yields null instead of invented associations.
 */
fun DenemeKaydi.defterAnalizi(kayitlar: List<DenemeKaydi>, sorular: List<Soru>): DenemeDefterAnalizi? {
    if (kaynakBelirsiz || kayitlar.count { denemeKaynakEslesir(it.ad, ad) } != 1) return null
    val eslesen = sorular.filter { denemeKaynakEslesir(it.kaynak, ad) }.distinctBy { it.id }
    val nedenli = eslesen.mapNotNull { it.yanlisNedeni?.let(::canonicalSoruSecenegiAdi)?.takeIf(String::isNotEmpty) }
    val nedenler = nedenli.groupBy(::normalizeSoruSecenegiAdi).values
        .map { DenemeNedenSayisi(it.first(), it.size) }
        .sortedWith(compareByDescending<DenemeNedenSayisi> { it.adet }.thenBy { normalizeSoruSecenegiAdi(it.neden) })
    return DenemeDefterAnalizi(
        soruSayisi = eslesen.size,
        nedenler = nedenler,
        nedeniBelirtilmeyen = eslesen.size - nedenli.size,
        dersSayilari = eslesen.groupingBy { it.dersId }.eachCount(),
    )
}

fun denemeHatirlatmaIndeksi(soruId: String): Int =
    (soruId.hashCode() and Int.MAX_VALUE) % 4

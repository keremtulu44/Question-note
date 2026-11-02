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
    val sonNetOrani: Double,
    val oncekiOrtalamaOrani: Double?,
    val dersAdi: String? = null,
) {
    val oranFarki: Double? get() = oncekiOrtalamaOrani?.let { sonNetOrani - it }
}

fun List<DenemeKaydi>.dersBarlari(son: DenemeKaydi): List<DersNetKarsilastirma> {
    val oncekiAyniKural = kronolojik().filter {
        it.kural == son.kural && it.id != son.id &&
            (it.gun < son.gun || (it.gun == son.gun && it.olusturma < son.olusturma))
    }
    return son.satirlar.map { satir ->
        val oncekiSatirlar = oncekiAyniKural.mapNotNull { it.satir(satir.dersId) }
        val oncekiNetler = oncekiSatirlar.map { son.kural.net(it.dogru, it.yanlis) }
        val oncekiSoruToplami = oncekiSatirlar.sumOf { it.soruSayisi }
        DersNetKarsilastirma(
            dersId = satir.dersId,
            sonNet = son.kural.net(satir.dogru, satir.yanlis),
            oncekiOrtalama = oncekiNetler.takeIf { it.isNotEmpty() }?.average(),
            sonNetOrani = satir.netOrani(son.kural) ?: 0.0,
            oncekiOrtalamaOrani = oncekiSoruToplami.takeIf { it > 0 }
                ?.let { oncekiNetler.sum() / it * 100.0 },
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

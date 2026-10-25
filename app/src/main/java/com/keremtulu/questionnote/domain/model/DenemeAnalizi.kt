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
)

fun List<DenemeKaydi>.dersBarlari(son: DenemeKaydi): List<DersNetKarsilastirma> {
    val ayni = filter { it.kural == son.kural && it.id != son.id }
    return son.satirlar.map { satir ->
        val oncekiler = ayni.mapNotNull { it.satir(satir.dersId) }
            .map { son.kural.net(it.dogru, it.yanlis) }
        DersNetKarsilastirma(
            dersId = satir.dersId,
            sonNet = son.kural.net(satir.dogru, satir.yanlis),
            oncekiOrtalama = oncekiler.takeIf { it.isNotEmpty() }?.average(),
        )
    }
}

fun List<DenemeKaydi>.kaynakla(ad: String?): DenemeKaydi? {
    val temiz = ad?.trim().orEmpty()
    if (temiz.isEmpty()) return null
    return firstOrNull { it.ad == temiz }
}

fun List<DenemeKaydi>.oncekiAyniKural(son: DenemeKaydi): DenemeKaydi? =
    kronolojik().filter { it.kural == son.kural && it.id != son.id }.lastOrNull { it.gun < son.gun || (it.gun == son.gun && it.olusturma < son.olusturma) }
        ?: kronolojik().filter { it.kural == son.kural && it.id != son.id }.lastOrNull()

fun DenemeKaydi.enZayif(): DenemeDersSatiri? =
    satirlar.minByOrNull { kural.net(it.dogru, it.yanlis) }

fun denemeHatirlatmaIndeksi(soruId: String): Int =
    (soruId.hashCode() and Int.MAX_VALUE) % 4

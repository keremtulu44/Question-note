package com.keremtulu.questionnote.domain.repeat

import com.keremtulu.questionnote.domain.model.OgrenmeDurumu
import com.keremtulu.questionnote.domain.model.TekrarKaydi
import com.keremtulu.questionnote.domain.model.TekrarPlani
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import java.time.LocalDate

/**
 * Adaptive v2. State is reconstructed from immutable review history, avoiding a duplicate-state
 * migration. Old v1 intervals remain intact; ease adjustments start with v2 records only.
 * Same engine is used by previews and the transactional write path.
 */
object AkilliTekrarKurali {
    const val ALGORITMA_SURUMU = 2
    const val EN_AZ_KOLAYLIK = 130
    const val EN_COK_KOLAYLIK = 280
    const val ZOR_SORU_ESIGI = 4

    data class Hafiza(val aralik: Int, val kolaylikYuz: Int, val unutma: Int) {
        val zorSoru: Boolean get() = unutma >= ZOR_SORU_ESIGI
    }

    fun hafiza(gecmis: List<TekrarKaydi>): Hafiza {
        val sirali = gecmis.filter { it.algoritmaSurumu in setOf(1, 2, 3) }.sortedWith(compareBy<TekrarKaydi> { it.gun }.thenBy { it.id })
        var kolaylik = 250
        var unutma = 0
        sirali.forEach { kayit ->
            if (!kayit.cozumAcildi && kayit.sonuc == TekrarSonucu.COZEMEDIM) unutma++
            if (kayit.algoritmaSurumu in setOf(2, 3) && !kayit.cozumAcildi) {
                kolaylik = (kolaylik + when (kayit.sonuc) {
                    TekrarSonucu.RAHAT -> 5
                    TekrarSonucu.ZORLANDIM -> -15
                    TekrarSonucu.COZEMEDIM -> -20
                }).coerceIn(EN_AZ_KOLAYLIK, EN_COK_KOLAYLIK)
            }
        }
        return Hafiza(
            aralik = (sirali.lastOrNull { it.sonrakiAralik != null }?.sonrakiAralik ?: 1).coerceIn(1, 180),
            kolaylikYuz = kolaylik,
            unutma = unutma,
        )
    }

    fun hesapla(
        gecmis: List<TekrarKaydi>,
        sonuc: TekrarSonucu,
        cozumAcildi: Boolean,
        bugun: LocalDate,
        planlananGun: Int?,
    ): TekrarPlani {
        val hafiza = hafiza(gecmis)
        val onceki = hafiza.aralik
        var yeni = when {
            cozumAcildi || sonuc == TekrarSonucu.COZEMEDIM -> 1
            sonuc == TekrarSonucu.ZORLANDIM -> maxOf(onceki + 1, yuzde(onceki, 120))
            else -> yuzde(onceki, hafiza.kolaylikYuz)
        }
        val gecikme = planlananGun?.let { (bugun.toEpochDay() - it).coerceAtLeast(0) } ?: 0
        if (!cozumAcildi && sonuc != TekrarSonucu.COZEMEDIM && gecikme > 2L * onceki) {
            yeni = yuzde(yeni, 80)
        }
        yeni = yeni.coerceIn(1, 180)
        return TekrarPlani(
            oncekiAralik = onceki,
            sonrakiAralik = yeni,
            sonrakiGun = bugun.plusDays(yeni.toLong()).toEpochDay().toInt(),
            ogrenmeDurumu = when {
                yeni <= 3 -> OgrenmeDurumu.OGRENIYOR
                yeni <= 21 -> OgrenmeDurumu.PEKISIYOR
                else -> OgrenmeDurumu.SAGLAM
            },
        )
    }

    private fun yuzde(deger: Int, carpan: Int): Int = (deger * carpan + 50) / 100
}

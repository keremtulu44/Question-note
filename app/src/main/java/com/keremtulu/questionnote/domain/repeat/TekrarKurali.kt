package com.keremtulu.questionnote.domain.repeat

import com.keremtulu.questionnote.domain.model.OgrenmeDurumu
import com.keremtulu.questionnote.domain.model.TekrarPlani
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import java.time.LocalDate

/** Version 1 of the local-calendar-day review interval rule. */
object TekrarKurali {
    const val ALGORITMA_SURUMU = 1
    val araliklar: List<Int> = listOf(1, 3, 7, 21, 45, 90, 180)

    fun ilkTekrarGunu(bugun: LocalDate): Int = bugun.plusDays(1).toEpochDay().toInt()

    fun hesapla(
        oncekiAralik: Int?,
        sonuc: TekrarSonucu,
        cozumAcildi: Boolean,
        bugun: LocalDate,
    ): TekrarPlani {
        val onceki = oncekiAralik?.coerceIn(1, araliklar.last()) ?: 1
        val sonraki = when {
            cozumAcildi || sonuc == TekrarSonucu.COZEMEDIM -> 1
            sonuc == TekrarSonucu.ZORLANDIM -> onceki
            else -> {
                val siradakiIndex = araliklar.indexOfFirst { it >= onceki }
                araliklar[(siradakiIndex + 1).coerceAtMost(araliklar.lastIndex)]
            }
        }
        val ogrenmeDurumu = when {
            sonuc == TekrarSonucu.COZEMEDIM || sonraki <= 3 -> OgrenmeDurumu.OGRENIYOR
            sonraki <= 21 -> OgrenmeDurumu.PEKISIYOR
            else -> OgrenmeDurumu.SAGLAM
        }
        return TekrarPlani(
            oncekiAralik = onceki,
            sonrakiAralik = sonraki,
            sonrakiGun = bugun.plusDays(sonraki.toLong()).toEpochDay().toInt(),
            ogrenmeDurumu = ogrenmeDurumu,
        )
    }
}

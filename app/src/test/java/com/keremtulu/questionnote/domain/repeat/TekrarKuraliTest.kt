package com.keremtulu.questionnote.domain.repeat

import com.keremtulu.questionnote.domain.model.OgrenmeDurumu
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TekrarKuraliTest {
    private val today = LocalDate.of(2026, 10, 16)

    @Test
    fun intervalsGrowOnIndependentEasyReviewsAndCapAt180Days() {
        var interval = 1
        val expected = listOf(3, 7, 21, 45, 90, 180, 180)
        expected.forEach { next ->
            val plan = TekrarKurali.hesapla(interval, TekrarSonucu.RAHAT, false, today)
            assertEquals(next, plan.sonrakiAralik)
            interval = plan.sonrakiAralik
        }
    }

    @Test
    fun difficultAndFailedReviewsStayNearAndSolutionRevealAlwaysSchedulesTomorrow() {
        assertEquals(
            7,
            TekrarKurali.hesapla(7, TekrarSonucu.ZORLANDIM, false, today).sonrakiAralik,
        )
        val opened = TekrarKurali.hesapla(90, TekrarSonucu.RAHAT, true, today)
        assertEquals(1, opened.sonrakiAralik)
        assertEquals(today.plusDays(1).toEpochDay().toInt(), opened.sonrakiGun)
        assertEquals(OgrenmeDurumu.OGRENIYOR, opened.ogrenmeDurumu)
        assertEquals(
            1,
            TekrarKurali.hesapla(21, TekrarSonucu.COZEMEDIM, false, today).sonrakiAralik,
        )
    }

    @Test
    fun localCalendarDayIsUsedAndAlgorithmVersionIsExplicit() {
        val plan = TekrarKurali.hesapla(1, TekrarSonucu.RAHAT, false, today)
        assertEquals(today.plusDays(3).toEpochDay().toInt(), plan.sonrakiGun)
        assertEquals(1, TekrarKurali.ALGORITMA_SURUMU)
        assertEquals(listOf(1, 3, 7, 21, 45, 90, 180), TekrarKurali.araliklar)
    }
}

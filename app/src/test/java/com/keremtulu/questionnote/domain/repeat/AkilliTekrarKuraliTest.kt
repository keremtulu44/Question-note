package com.keremtulu.questionnote.domain.repeat

import com.keremtulu.questionnote.domain.model.TekrarKaydi
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class AkilliTekrarKuraliTest {
    private val bugun = LocalDate.of(2026, 10, 17)
    private val gun = bugun.toEpochDay().toInt()
    private fun kayit(i: Int = 0, aralik: Int = 1, sonuc: TekrarSonucu = TekrarSonucu.RAHAT, cozum: Boolean = false, surum: Int = 1) =
        TekrarKaydi("r$i", gun - 100 + i, null, sonuc, cozum, 1, aralik, false, surum)

    @Test fun firstEasyIsThreeDaysButStruggledNowMakesProgressToTwo() {
        assertEquals(3, AkilliTekrarKurali.hesapla(emptyList(), TekrarSonucu.RAHAT, false, bugun, gun).sonrakiAralik)
        assertEquals(2, AkilliTekrarKurali.hesapla(emptyList(), TekrarSonucu.ZORLANDIM, false, bugun, gun).sonrakiAralik)
        assertEquals(1, AkilliTekrarKurali.hesapla(emptyList(), TekrarSonucu.COZEMEDIM, false, bugun, gun).sonrakiAralik)
    }

    @Test fun solutionAlwaysSchedulesTomorrowAndDoesNotAffectEase() {
        TekrarSonucu.entries.forEach { sonuc ->
            val history = listOf(kayit(aralik = 90, sonuc = sonuc, cozum = true, surum = 2))
            assertEquals(250, AkilliTekrarKurali.hafiza(history).kolaylikYuz)
            assertEquals(0, AkilliTekrarKurali.hafiza(history).unutma)
            val plan = AkilliTekrarKurali.hesapla(history, sonuc, true, bugun, gun - 1000)
            assertEquals(1, plan.sonrakiAralik)
            assertEquals(gun + 1, plan.sonrakiGun)
        }
    }

    @Test fun overdueRoundingMatchesDocumentedExample() {
        val history = listOf(kayit(aralik = 21, surum = 2)) // ease 255
        assertEquals(43, AkilliTekrarKurali.hesapla(history, TekrarSonucu.RAHAT, false, bugun, gun - 50).sonrakiAralik)
        assertEquals(54, AkilliTekrarKurali.hesapla(history, TekrarSonucu.RAHAT, false, bugun, gun - 42).sonrakiAralik)
    }

    @Test fun legacyIntervalsStayIntactAndLegacyAnswersDoNotRetroactivelyChangeEase() {
        val history = (0..5).map { kayit(i = it, aralik = 7, sonuc = TekrarSonucu.COZEMEDIM) }
        val memory = AkilliTekrarKurali.hafiza(history)
        assertEquals(250, memory.kolaylikYuz)
        assertEquals(7, memory.aralik)
        assertEquals(6, memory.unutma)
        assertTrue(memory.zorSoru)
        assertEquals(18, AkilliTekrarKurali.hesapla(history, TekrarSonucu.RAHAT, false, bugun, gun).sonrakiAralik)
    }

    @Test fun reconstructedEaseClampsAndUsesChronologicalHistory() {
        val hard = (0..30).map { kayit(i = it, aralik = 1, sonuc = TekrarSonucu.COZEMEDIM, surum = 2) }
        val easy = (0..30).map { kayit(i = it, aralik = 180, surum = 2) }
        assertEquals(130, AkilliTekrarKurali.hafiza(hard.reversed()).kolaylikYuz)
        assertEquals(280, AkilliTekrarKurali.hafiza(easy).kolaylikYuz)
        assertEquals(AkilliTekrarKurali.hafiza(hard), AkilliTekrarKurali.hafiza(hard.reversed()))
    }

    @Test fun allIntervalsAreBoundedAndAlwaysScheduleAFutureLocalDay() {
        for (interval in 1..180) for (result in TekrarSonucu.entries) for (late in listOf(-50, 0, 5, 500)) {
            val plan = AkilliTekrarKurali.hesapla(listOf(kayit(aralik = interval)), result, false, bugun, gun - late)
            assertTrue(plan.sonrakiAralik in 1..180)
            assertEquals(gun + plan.sonrakiAralik, plan.sonrakiGun)
        }
    }

    @Test fun fourUnassistedLapsesFlagExtraSupportButNeverChangePlanStatus() {
        val history = (0..3).map { kayit(i = it, sonuc = TekrarSonucu.COZEMEDIM) }
        assertFalse(AkilliTekrarKurali.hafiza(history.take(3)).zorSoru)
        assertTrue(AkilliTekrarKurali.hafiza(history).zorSoru)
        assertEquals(2, AkilliTekrarKurali.ALGORITMA_SURUMU)
        assertEquals(1, TekrarKurali.ALGORITMA_SURUMU)
    }
}

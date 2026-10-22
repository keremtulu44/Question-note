package com.keremtulu.questionnote.domain.calisma

import com.keremtulu.questionnote.domain.model.TekrarSonucu
import org.junit.Assert.*
import org.junit.Test

class AktifSureOlcerTest {
    private class Saat(var ms: Long = 10_000) : MonotonikSaat {
        override fun simdiMs() = ms
    }
    private fun yeni(saat: Saat, id: String = "q") = AktifSureOlcer(saat, OlcumDevamNoktasi(id, "attempt-$id"))

    @Test fun elapsedSegmentsAreMeasuredWithoutUiTicks() {
        val saat = Saat(); val o = yeni(saat)
        o.aktifligiAyarla(true); saat.ms += 12_345
        assertEquals(12_345L, o.olcum().denemeAktifMs)
        assertEquals(0L, o.olcum().incelemeAktifMs)
    }

    @Test fun loadingPauseBackgroundAndSavingAddNoTime() {
        val saat = Saat(); val o = yeni(saat)
        saat.ms += 30_000 // initial loading is not active
        assertEquals(0L, o.olcum().denemeAktifMs)
        repeat(4) {
            o.aktifligiAyarla(true); saat.ms += 1_250; o.aktifligiAyarla(false)
            saat.ms += 60_000
        }
        assertEquals(5_000L, o.olcum().denemeAktifMs)
    }

    @Test fun firstSolutionRevealPermanentlySwitchesToAssistedPhase() {
        val saat = Saat(); val o = yeni(saat)
        o.aktifligiAyarla(true); saat.ms += 5_000; o.cozumuAc()
        saat.ms += 3_000; o.cozumuAc() // hiding/reopening never returns to independent time
        saat.ms += 2_000
        assertEquals(5_000L, o.olcum().denemeAktifMs)
        assertEquals(5_000L, o.olcum().incelemeAktifMs)
        assertTrue(o.cozumAcildi)
    }

    @Test fun assessmentFreezesTheAttemptAndRetryRetainsIt() {
        val saat = Saat(); val o = yeni(saat)
        o.aktifligiAyarla(true); saat.ms += 2_500; o.dondur(TekrarSonucu.RAHAT)
        val once = o.devamNoktasi()
        saat.ms += 90_000; o.aktifligiAyarla(true)
        val sonra = o.devamNoktasi()
        assertEquals(once.olcum, sonra.olcum)
        assertEquals(once.denemeId, sonra.denemeId)
        assertEquals(TekrarSonucu.RAHAT, sonra.secim)
    }

    @Test fun returningToStudyStartsANewSegmentOfTheSameAttempt() {
        val saat = Saat(); val o = yeni(saat)
        o.aktifligiAyarla(true); saat.ms += 2_000; o.dondur()
        saat.ms += 50_000; o.calismayaDon(); o.aktifligiAyarla(true); saat.ms += 3_000
        assertEquals(5_000L, o.olcum().denemeAktifMs)
        assertEquals("attempt-q", o.devamNoktasi().denemeId)
    }

    @Test fun fullScreenIsExcludedAndMakesTheMeasurementPartial() {
        val saat = Saat(); val o = yeni(saat)
        o.aktifligiAyarla(true); saat.ms += 1_000; o.tamEkranaGir()
        saat.ms += 90_000; o.aktifligiAyarla(true); saat.ms += 1_000
        assertEquals(2_000L, o.olcum().denemeAktifMs)
        assertEquals(OlcumKalitesi.KISMI, o.olcum().kalite)
        assertTrue(OlcumNedeni.TAM_EKRAN in o.olcum().nedenler)
    }

    @Test fun deferringAndReturningPreservesOnlyThatQuestionsTime() {
        val saat = Saat(); val a = yeni(saat, "a")
        a.aktifligiAyarla(true); saat.ms += 3_000; a.aktifligiAyarla(false)
        val kayit = a.devamNoktasi()
        val b = yeni(saat, "b"); b.aktifligiAyarla(true); saat.ms += 7_000; b.aktifligiAyarla(false)
        val tekrarA = AktifSureOlcer(saat, kayit)
        tekrarA.aktifligiAyarla(true); saat.ms += 2_000
        assertEquals(5_000L, tekrarA.olcum().denemeAktifMs)
        assertEquals(7_000L, b.olcum().denemeAktifMs)
    }

    @Test fun processAndRebootRecoveryNeverUsesTheOldMonotonicOrigin() {
        val saat = Saat(); val o = yeni(saat)
        o.aktifligiAyarla(true); saat.ms += 4_000
        val kayit = o.devamNoktasi().surectenKurtar()
        val yenidenSaat = Saat(100)
        val kurtarilan = AktifSureOlcer(yenidenSaat, kayit)
        yenidenSaat.ms += 100_000 // closed time is not study
        assertEquals(4_000L, kurtarilan.olcum().denemeAktifMs)
        kurtarilan.aktifligiAyarla(true); yenidenSaat.ms += 1_000
        assertEquals(5_000L, kurtarilan.olcum().denemeAktifMs)
        assertEquals(OlcumKalitesi.KISMI, kurtarilan.olcum().kalite)
    }

    @Test fun safeInProcessRecreationRetainsCoverageAndNeverDoubleCounts() {
        val saat = Saat(); val o = yeni(saat)
        o.aktifligiAyarla(true); saat.ms += 1_234; o.aktifligiAyarla(false)
        val yeni = AktifSureOlcer(saat, o.devamNoktasi())
        saat.ms += 5_000; yeni.aktifligiAyarla(true); saat.ms += 1_000
        assertEquals(2_234L, yeni.olcum().denemeAktifMs)
        assertEquals(OlcumKalitesi.TAM, yeni.olcum().kalite)
    }

    @Test fun backwardMonotonicClockInvalidatesInsteadOfSubtractingTime() {
        val saat = Saat(); val o = yeni(saat)
        o.aktifligiAyarla(true); saat.ms = 9_000
        assertEquals(OlcumKalitesi.GECERSIZ, o.olcum().kalite)
        assertTrue(OlcumNedeni.SAAT_TUTARSIZ in o.olcum().nedenler)
        assertEquals(0L, o.olcum().denemeAktifMs)
    }

    @Test fun overflowOrExcessiveSegmentsAreInvalidNotWrappedOrClampedAsValid() {
        val saat = Saat(0); val o = yeni(saat)
        o.aktifligiAyarla(true); saat.ms = Long.MAX_VALUE
        assertEquals(OlcumKalitesi.GECERSIZ, o.olcum().kalite)
        assertTrue(OlcumNedeni.SURE_SINIRI in o.olcum().nedenler)
        assertEquals(0L, o.olcum().denemeAktifMs)
    }

    @Test fun absentIsNullAndInvalidInputDoesNotBecomeAZeroSecondSuccess() {
        assertNull(SureOlcumu().toplamMs)
        assertTrue(SureOlcumu().gecerliAlanlar())
        val hatali = SureOlcumu(-1, Long.MAX_VALUE, OlcumKalitesi.TAM).guvenli()
        assertEquals(OlcumKalitesi.GECERSIZ, hatali.kalite)
        assertNull(hatali.denemeAktifMs)
        assertNull(hatali.toplamMs)
    }

    @Test fun deviceZoneChangesAreObservedByCalendarClockButNeverChangeElapsedStudyTime() {
        val onceki = java.util.TimeZone.getDefault()
        val saat = Saat(); val o = yeni(saat)
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"))
            val utc = YerelTakvimSaati.zone
            o.aktifligiAyarla(true); saat.ms += 1_000
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Europe/Istanbul"))
            assertNotEquals(utc, YerelTakvimSaati.zone)
            assertEquals("Europe/Istanbul", YerelTakvimSaati.zone.id)
            assertEquals(1_000L, o.olcum().denemeAktifMs)
        } finally { java.util.TimeZone.setDefault(onceki) }
    }

}

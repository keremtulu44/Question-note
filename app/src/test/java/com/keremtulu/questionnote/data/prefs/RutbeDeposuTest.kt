package com.keremtulu.questionnote.data.prefs

import android.content.Context
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.domain.seviye.Rutbe
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RutbeDeposuTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()
    @Before fun temizle() {
        listOf("rutbe_kutlamalari", "karne_rozetleri").forEach { context.getSharedPreferences(it, 0).edit().clear().commit() }
    }
    @Test fun firstInstallOrMigrationBaselinesTheCurrentRankWithoutCelebratingOldHistory() {
        val depo = RutbeDeposu(context)
        depo.guncelle(Rutbe.BILGE, 10, true)
        assertNull(depo.bekleyenIzle.value)
        depo.guncelle(Rutbe.BILGE, 10, true)
        assertNull(RutbeDeposu(context).bekleyenIzle.value)
    }
    @Test fun multipleThresholdsAreConsolidatedIntoOneDurableCelebration() {
        val depo = RutbeDeposu(context)
        depo.guncelle(Rutbe.BASLANGIC, 10, false)
        depo.guncelle(Rutbe.DUZENLI, 10, true)
        depo.guncelle(Rutbe.ODAKLI, 10, true)
        val p = requireNotNull(RutbeDeposu(context).bekleyenIzle.value)
        assertEquals(Rutbe.BASLANGIC, p.onceki); assertEquals(Rutbe.ODAKLI, p.son)
        assertEquals(Rutbe.ODAKLI.ordinal, p.gecilenEsik)
    }
    @Test fun restartPreservesPendingButAcknowledgedRankNeverReplays() {
        val depo = RutbeDeposu(context)
        depo.guncelle(Rutbe.BASLANGIC, 10, false); depo.guncelle(Rutbe.MERAKLI, 10, true)
        val restored = RutbeDeposu(context)
        restored.guncelle(Rutbe.MERAKLI, 10, false)
        restored.kutlamayiTamamla(requireNotNull(restored.bekleyenIzle.value))
        val next = RutbeDeposu(context)
        next.guncelle(Rutbe.MERAKLI, 10, true)
        assertNull(next.bekleyenIzle.value)
    }
    @Test fun staleDismissCannotConsumeANewerThreshold() {
        val depo = RutbeDeposu(context)
        depo.guncelle(Rutbe.BASLANGIC, 10, false); depo.guncelle(Rutbe.MERAKLI, 10, true)
        val old = requireNotNull(depo.bekleyenIzle.value)
        depo.guncelle(Rutbe.ODAKLI, 10, true)
        depo.kutlamayiTamamla(old)
        val p = requireNotNull(depo.bekleyenIzle.value)
        assertEquals(Rutbe.MERAKLI, p.onceki); assertEquals(Rutbe.ODAKLI, p.son)
        assertEquals(p, RutbeDeposu(context).bekleyenIzle.value)
    }
    @Test fun goalRecomputationAndXpDropsDoNotCreateReearnedRankStorms() {
        val depo = RutbeDeposu(context)
        depo.guncelle(Rutbe.BASLANGIC, 10, false)
        depo.guncelle(Rutbe.KARARLI, 5, true)
        assertNull(depo.bekleyenIzle.value)
        depo.guncelle(Rutbe.MERAKLI, 5, true); depo.guncelle(Rutbe.KARARLI, 5, true)
        assertNull(depo.bekleyenIzle.value)
        depo.guncelle(Rutbe.DISIPLINLI, 5, true)
        assertEquals(Rutbe.DISIPLINLI, depo.bekleyenIzle.value?.son)
    }
    @Test fun restoreGateSurvivesProcessRecreationAndSilentlyRebaselinesHigherRank() {
        val depo = RutbeDeposu(context)
        depo.guncelle(Rutbe.BASLANGIC, 10, false); depo.guncelle(Rutbe.MERAKLI, 10, true)
        depo.geriYuklemeSonrasi()
        val next = RutbeDeposu(context)
        assertNull(next.bekleyenIzle.value)
        next.guncelle(Rutbe.KOZMIK, 10, true)
        assertNull(next.bekleyenIzle.value)
        next.guncelle(Rutbe.SONSUZ, 10, true)
        assertEquals(Rutbe.SONSUZ, next.bekleyenIzle.value?.son)
    }
    @Test fun retroactiveBadgeOwnershipIsPreservedWithoutEnqueuingAStorm() {
        val depo = RozetDeposu(context)
        depo.kaydet(listOf(Rozet.ILK_SORU), kutla = false)
        assertEquals(setOf(Rozet.ILK_SORU), depo.sahipOlanlar())
        assertTrue(depo.bekleyenIzle.value.isEmpty())
        depo.geriYuklemeSonrasi()
        val restored = RozetDeposu(context)
        restored.kaydet(Rozet.entries.take(8))
        assertEquals(8, restored.sahipOlanlar().size)
        assertTrue(restored.bekleyenIzle.value.isEmpty())
        restored.kaydet(listOf(Rozet.entries[8]))
        assertEquals(listOf(Rozet.entries[8]), restored.bekleyenIzle.value)
    }
    @Test fun emptyRestoreBaselineDoesNotSuppressTheNextRealBadgeAward() {
        val depo = RozetDeposu(context)
        depo.geriYuklemeSonrasi(); depo.kaydet(emptyList())
        depo.kaydet(listOf(Rozet.ILK_SORU))
        assertEquals(listOf(Rozet.ILK_SORU), depo.bekleyenIzle.value)
    }
    @Test fun corruptPreferenceFailsSafeToBaselineRatherThanCrashingOrReplaying() {
        context.getSharedPreferences("rutbe_kutlamalari", 0).edit().putString("durum", "broken").commit()
        val depo = RutbeDeposu(context)
        depo.guncelle(Rutbe.ODAKLI, 10, true)
        assertNull(depo.bekleyenIzle.value)
    }
}

package com.keremtulu.questionnote.data.prefs

import android.content.Context
import com.keremtulu.questionnote.domain.calisma.OdakOturumu
import com.keremtulu.questionnote.domain.karne.OturumSonucu
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.domain.model.SinavProfili
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class IlerlemeTercihleriTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()
    @Before fun temizle() {
        listOf("karne_rozetleri", "odak_oturumu", "sinav_profili").forEach {
            context.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().commit()
        }
    }

    @Test fun badgesAndPendingCelebrationsSurviveARecreatedStore() {
        val depo = RozetDeposu(context)
        assertEquals(listOf(Rozet.ILK_SORU), depo.kaydet(listOf(Rozet.ILK_SORU, Rozet.ILK_SORU)))
        assertTrue(depo.kaydet(listOf(Rozet.ILK_SORU)).isEmpty())
        val restored = RozetDeposu(context)
        assertEquals(setOf(Rozet.ILK_SORU), restored.sahipOlanlar())
        assertEquals(listOf(Rozet.ILK_SORU), restored.bekleyenIzle.value)
        assertNotNull(restored.kazanilmaZamani(Rozet.ILK_SORU))
        restored.kutlamayiTamamla(Rozet.ILK_SORU)
        assertTrue(RozetDeposu(context).bekleyenIzle.value.isEmpty())
        assertEquals(setOf(Rozet.ILK_SORU), RozetDeposu(context).sahipOlanlar())
    }

    @Test fun existingVersionElevenBadgesAreNotReplayedOnUpgrade() {
        context.getSharedPreferences("karne_rozetleri", Context.MODE_PRIVATE).edit()
            .putStringSet("kazanilanlar", setOf("ilk_karne", "kusursuz_tur", "unknown_future")).commit()
        val depo = RozetDeposu(context)
        assertEquals(2, depo.sahipOlanlar().size)
        assertTrue(depo.bekleyenIzle.value.isEmpty())
        assertTrue(depo.kaydet(listOf(Rozet.ILK_KARNE)).isEmpty())
    }

    @Test fun concurrentEvaluationsDoNotLoseBadgesOrDuplicateQueue() {
        val depo = RozetDeposu(context)
        val jobs = Rozet.entries.map { rozet -> Thread { repeat(3) { depo.kaydet(listOf(rozet)) } } }
        jobs.forEach(Thread::start)
        jobs.forEach(Thread::join)
        assertEquals(30, depo.sahipOlanlar().size)
        assertEquals(30, depo.bekleyenIzle.value.size)
        assertEquals(30, RozetDeposu(context).sahipOlanlar().size)
    }

    @Test fun sessionCheckpointRoundTripsOnlySmallIdsAndAnswers() {
        val oturum = OdakOturumu("session", listOf("a", "b"), 10L, bugunden = true)
            .cozumuAc().cevapla(OturumSonucu("a", TekrarSonucu.ZORLANDIM, true))
        OdakOturumuDeposu(context).kaydet(oturum)
        val depo = OdakOturumuDeposu(context)
        assertEquals(oturum, depo.oku())
        depo.temizle()
        assertNull(depo.oku())
    }

    @Test fun corruptCheckpointIsIgnoredWithoutCrashingStartup() {
        context.getSharedPreferences("odak_oturumu", Context.MODE_PRIVATE).edit().putString("oturum", "broken").commit()
        assertNull(OdakOturumuDeposu(context).oku())
    }

    @Test fun profileDefaultsToAllLessonsAndPersistsAnExplicitChoice() {
        val depo = SinavProfiliDeposu(context)
        assertEquals(SinavProfili.YKS, depo.secili())
        depo.kaydet(SinavProfili.LGS, setOf("math-id", "science-id"))
        assertEquals(SinavProfili.LGS, SinavProfiliDeposu(context).secili())
        assertEquals(setOf("math-id", "science-id"), SinavProfiliDeposu(context).secimIzle.value.dersIdleri)
    }
}

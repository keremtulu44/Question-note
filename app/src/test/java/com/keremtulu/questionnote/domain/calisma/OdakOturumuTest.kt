package com.keremtulu.questionnote.domain.calisma

import com.keremtulu.questionnote.domain.karne.OturumSonucu
import com.keremtulu.questionnote.domain.model.TekrarKaydi
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import org.junit.Assert.*
import org.junit.Test

class OdakOturumuTest {
    private fun yeni() = OdakOturumu("session", listOf("a", "b", "c"), 1000L)
    private fun sonuc(id: String) = OturumSonucu(id, TekrarSonucu.RAHAT, false)

    @Test fun onlyCurrentQuestionCanAdvanceAndDuplicateCallbackIsIgnored() {
        val once = yeni().cevapla(sonuc("a"))
        assertEquals(1, once.sira)
        assertEquals(once, once.cevapla(sonuc("a")))
        assertEquals(once, once.cevapla(sonuc("c")))
    }

    @Test fun deferralNeverRecordsAReviewAndPreservesRevealedSolution() {
        val deferred = yeni().cozumuAc().sonaBirak()
        assertEquals(listOf("b", "c", "a"), deferred.soruIdleri)
        assertTrue(deferred.sonuclar.isEmpty())
        assertEquals(setOf("a"), deferred.cozumAcilanlar)
        val answered = deferred.atla().atla().cevapla(sonuc("a"))
        assertTrue(answered.sonuclar.single().cozumAcildi)
        assertTrue(answered.bitti)
    }

    @Test fun cannotDeferLastQuestionIntoAnEndlessLoop() {
        val last = yeni().atla().atla()
        assertEquals(last, last.sonaBirak())
    }

    @Test fun committedReviewAfterCheckpointIsRecoveredWithoutAwardingTwice() {
        val kayit = TekrarKaydi("r", 10, 10, TekrarSonucu.ZORLANDIM, false, 1, 2, false, 2, zaman = 1100L)
        val recovered = yeni().kurtar(setOf("a", "b", "c"), mapOf("a" to kayit))
        assertEquals(1, recovered.sira)
        assertEquals("b", recovered.simdikiId)
        assertEquals(TekrarSonucu.ZORLANDIM, recovered.sonuclar.single().sonuc)
        assertEquals(recovered, recovered.kurtar(setOf("a", "b", "c"), mapOf("a" to kayit)))
    }

    @Test fun olderReviewsAreNotMisattributedToTheCurrentSession() {
        val kayit = TekrarKaydi("r", 1, 1, TekrarSonucu.RAHAT, false, 1, 3, false, zaman = 900L)
        assertEquals(yeni(), yeni().kurtar(setOf("a", "b", "c"), mapOf("a" to kayit)))
    }

    @Test fun deletedQuestionsAreRemovedAndAllMissingEndsCleanly() {
        val recovered = yeni().kurtar(setOf("b", "c"), emptyMap())
        assertEquals(listOf("b", "c"), recovered.soruIdleri)
        assertEquals("b", recovered.simdikiId)
        assertTrue(yeni().kurtar(emptySet(), emptyMap()).bitti)
    }

    @Test fun sessionIdentityRecoversACommitEvenBeforeFirstAttemptCheckpointAndAfterWallClockRollback() {
        val kayit = TekrarKaydi("r", 10, 10, TekrarSonucu.RAHAT, false, 1, 3, false, 2,
            zaman = 500L, denemeId = "attempt-not-yet-checkpointed", oturumId = "session")
        val kurtarilan = yeni().kurtar(setOf("a", "b", "c"), mapOf("a" to kayit))
        assertEquals(1, kurtarilan.sira)
        assertEquals("b", kurtarilan.simdikiId)
        assertEquals(1, kurtarilan.sonuclar.size)
    }

    @Test fun aReviewOwnedByAnotherSessionCannotBeRecoveredJustBecauseItsTimestampIsNewer() {
        val kayit = TekrarKaydi("r", 10, 10, TekrarSonucu.RAHAT, false, 1, 3, false, 2,
            zaman = 9_999L, denemeId = "other-attempt", oturumId = "other-session")
        assertEquals(yeni(), yeni().kurtar(setOf("a", "b", "c"), mapOf("a" to kayit)))
    }

}

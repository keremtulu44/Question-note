package com.keremtulu.questionnote.data.prefs

import android.content.Context
import com.keremtulu.questionnote.domain.calisma.OdakOturumu
import com.keremtulu.questionnote.domain.calisma.OlcumDevamNoktasi
import com.keremtulu.questionnote.domain.calisma.OlcumKalitesi
import com.keremtulu.questionnote.domain.calisma.OlcumNedeni
import com.keremtulu.questionnote.domain.calisma.SureOlcumu
import com.keremtulu.questionnote.domain.karne.OturumSonucu
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OlcumDevamNoktasiTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()
    private val oturum = OdakOturumu("session", listOf("a", "b"), 1_000)
    private fun nokta(rev: Long = 1, ms: Long = 6_000) = OlcumDevamNoktasi("a", "attempt-a",
        SureOlcumu(ms, 0, OlcumKalitesi.TAM), revizyon = rev)
    @Before fun temizle() { context.getSharedPreferences("odak_oturumu", Context.MODE_PRIVATE).edit().clear().commit() }

    @Test fun processReloadKeepsAmountsIdentityAndSelectionButMarksCoveragePartial() = runBlocking {
        val depo = OdakOturumuDeposu(context); depo.kaydet(oturum)
        depo.kalicilastir(depo.olcumuHazirla(oturum.id, nokta().copy(donduruldu = true, secim = TekrarSonucu.RAHAT)))
        assertEquals(OlcumKalitesi.TAM, depo.oku()?.olcumler?.get("a")?.olcum?.kalite)
        val gelen = requireNotNull(OdakOturumuDeposu(context).oku()?.olcumler?.get("a"))
        assertEquals("attempt-a", gelen.denemeId)
        assertEquals(6_000L, gelen.olcum.denemeAktifMs)
        assertEquals(TekrarSonucu.RAHAT, gelen.secim)
        assertTrue(gelen.donduruldu)
        assertTrue(OlcumNedeni.SUREC_KURTARMA in gelen.olcum.nedenler)
    }

    @Test fun sessionNavigationCannotOverwriteANewerQuestionCheckpoint() = runBlocking {
        val depo = OdakOturumuDeposu(context); depo.kaydet(oturum)
        depo.kalicilastir(depo.olcumuHazirla(oturum.id, nokta(5).copy(cozumAcildi = true)))
        depo.kalicilastir(depo.hazirla(oturum.sonaBirak())) // old UI snapshot has no measurement map
        val yeni = requireNotNull(depo.oku())
        assertEquals(listOf("b", "a"), yeni.soruIdleri)
        assertEquals(6_000L, yeni.olcumler["a"]?.olcum?.denemeAktifMs)
        assertTrue("a" in yeni.cozumAcilanlar)
        assertNull(depo.olcumuHazirla(oturum.id, nokta(4, 1_000)))
    }

    @Test fun lateWritesCannotReviveACommittedRestoreOrAClosedSession() = runBlocking {
        val depo = OdakOturumuDeposu(context); depo.kaydet(oturum)
        val bekleyen = depo.olcumuHazirla(oturum.id, nokta())
        depo.temizle()
        depo.kalicilastir(bekleyen)
        assertNull(depo.oku())
        assertNull(depo.olcumuHazirla(oturum.id, nokta(2)))
        assertNull(OdakOturumuDeposu(context).oku())
    }

    @Test fun discardingOnlyClearsTheConfirmedOwnerAndInvalidatesQueuedWrites() = runBlocking {
        val depo = OdakOturumuDeposu(context)
        depo.kaydet(oturum)
        val bekleyen = depo.olcumuHazirla(oturum.id, nokta())
        assertFalse(depo.belirliOturumuTemizle("another-session"))
        assertEquals(oturum.id, depo.oku()?.id)
        assertTrue(depo.belirliOturumuTemizle(oturum.id))
        depo.kalicilastir(bekleyen)
        assertNull(OdakOturumuDeposu(context).oku())

        val yeni = oturum.copy(id = "new-session")
        OdakOturumuDeposu(context).kaydet(yeni)
        assertFalse(depo.belirliOturumuTemizle(oturum.id))
        assertEquals(yeni.id, depo.oku()?.id)
        assertEquals(yeni.id, OdakOturumuDeposu(context).oku()?.id)
    }

    @Test fun anotherStoreClearingPreferencesAlsoInvalidatesOldQueuedIo() = runBlocking {
        val depo = OdakOturumuDeposu(context); depo.kaydet(oturum)
        val eski = depo.olcumuHazirla(oturum.id, nokta())
        OdakOturumuDeposu(context).temizle()
        depo.kalicilastir(eski)
        assertNull(depo.oku())
        assertNull(OdakOturumuDeposu(context).oku())
    }

    @Test fun anOlderSessionWriteCannotRewindTheCompletedQuestionPosition() = runBlocking {
        val depo = OdakOturumuDeposu(context)
        val eski = depo.hazirla(oturum)
        val yeni = depo.hazirla(oturum.cevapla(OturumSonucu("a", TekrarSonucu.RAHAT, false)))
        depo.kalicilastir(yeni); depo.kalicilastir(eski)
        assertEquals(1, OdakOturumuDeposu(context).oku()?.sira)
        assertNull(depo.olcumuHazirla(oturum.id, nokta()))
    }

    @Test fun oldCheckpointRetainsTheSessionWithoutFabricatingAnElapsedOrigin() {
        val json = JSONObject().put("id", "old").put("baslama", 1_000).put("sira", 0).put("bugunden", false)
            .put("sorular", JSONArray(listOf("a"))).put("cozumler", JSONArray(listOf("a"))).put("sonuclar", JSONArray())
        context.getSharedPreferences("odak_oturumu", Context.MODE_PRIVATE).edit().putString("oturum", json.toString()).commit()
        val gelen = requireNotNull(OdakOturumuDeposu(context).oku())
        assertFalse(gelen.olcumDestekli)
        assertTrue(gelen.olcumler.isEmpty())
        assertTrue("a" in gelen.cozumAcilanlar)
    }

    @Test fun unsupportedCheckpointVersionDoesNotCrashOrGuessDurations() {
        context.getSharedPreferences("odak_oturumu", Context.MODE_PRIVATE).edit().putString("oturum", "{\"version\":99}").commit()
        assertNull(OdakOturumuDeposu(context).oku())
    }

    @Test fun solutionRevealBarrierFlushesTheLatestRevisionAndChecksItsOwner() = runBlocking {
        val depo = OdakOturumuDeposu(context); depo.kaydet(oturum)
        assertFalse(depo.cozumIsaretiniKalicilastir(oturum.id, "a"))
        val eski = depo.olcumuHazirla(oturum.id, nokta(1).copy(cozumAcildi = true))
        depo.olcumuHazirla(oturum.id, nokta(2, 7_000).copy(cozumAcildi = true))
        depo.kalicilastir(eski) // obsolete queued write cannot serve as permission to reveal
        assertTrue(depo.cozumIsaretiniKalicilastir(oturum.id, "a"))
        val gelen = requireNotNull(OdakOturumuDeposu(context).oku())
        assertTrue("a" in gelen.cozumAcilanlar)
        assertEquals(7_000L, gelen.olcumler["a"]?.olcum?.denemeAktifMs)
        assertFalse(depo.cozumIsaretiniKalicilastir("other-session", "a"))
        depo.temizle()
        assertFalse(depo.cozumIsaretiniKalicilastir(oturum.id, "a"))
    }

}

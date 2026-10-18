package com.keremtulu.questionnote.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.room.Room
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.prefs.OdakOturumuDeposu
import com.keremtulu.questionnote.data.repository.RoomDersRepository
import com.keremtulu.questionnote.data.repository.RoomSoruRepository
import com.keremtulu.questionnote.domain.calisma.MonotonikSaat
import com.keremtulu.questionnote.domain.calisma.OdakOturumu
import com.keremtulu.questionnote.domain.calisma.OlcumKalitesi
import com.keremtulu.questionnote.domain.calisma.SureOlcumu
import com.keremtulu.questionnote.domain.gelisim.DersSureProfili
import com.keremtulu.questionnote.domain.gelisim.KisiselProfilHesabi
import com.keremtulu.questionnote.domain.gelisim.ProfilBaglami
import com.keremtulu.questionnote.domain.gelisim.SureKapsami
import com.keremtulu.questionnote.domain.gelisim.SureOrnegi
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import com.keremtulu.questionnote.ui.calisma.OdakOturumuEkrani
import com.keremtulu.questionnote.ui.seviye.SureProfiliKarti
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
class OlcumVeProfilArayuzTest {
    @get:Rule val compose = createComposeRule()
    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Test fun oldUnmeasuredHistoryShowsNoInventedMedian() {
        compose.setContent { YanlisDefteriTema { SureProfiliKarti(SureKapsami(41, 100, 12, 0, 0, 12, 0, emptyList())) } }
        compose.onNodeWithTag("progress-time-profile").assertExists()
        compose.onNodeWithText(context.getString(R.string.profile_coverage, 12, 0, 0, 12, 0)).assertExists()
        compose.onNodeWithText(context.getString(R.string.profile_collecting_note)).assertExists()
        compose.onAllNodesWithText("medyan", substring = true).assertCountEquals(0)
    }

    @Test fun sparseProfileStatesTheMissingSamplesInsteadOfASpeedLabel() {
        val baglam = ProfilBaglami("math")
        val p = KisiselProfilHesabi.hesapla(emptyList(), baglam, 100)
        compose.setContent { YanlisDefteriTema { SureProfiliKarti(SureKapsami(41, 100, 0, 0, 0, 0, 0, listOf(DersSureProfili("math", "Matematik", p)))) } }
        compose.onAllNodesWithText(context.getString(R.string.profile_collecting)).assertCountEquals(2)
        compose.onAllNodesWithText("medyan", substring = true).assertCountEquals(0)
    }

    @Test fun twoHundredPercentDarkProfileKeepsScopeAndSampleCountsReadable() {
        val baglam = ProfilBaglami("math")
        val samples = (0 until 20).map { i -> SureOrnegi("r$i", "a$i", "q${i % 10}", 100 - i / 4, i.toLong(),
            baglam, TekrarSonucu.RAHAT, false, SureOlcumu(50_000, 0, OlcumKalitesi.TAM)) }
        val p = KisiselProfilHesabi.hesapla(samples, baglam, 100)
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
                YanlisDefteriTema(koyuTema = true, hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        SureProfiliKarti(SureKapsami(41, 100, 20, 20, 0, 0, 0, listOf(DersSureProfili("math", "Matematik", p))))
                    }
                }
            }
        }
        compose.onNodeWithText(context.getString(R.string.profile_policy_note)).performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText(context.getString(R.string.profile_median, "50")).assertCountEquals(2)
        compose.onAllNodesWithText(context.getString(R.string.profile_sample, 20, 20, 10, 10, 5, 5)).assertCountEquals(2)
    }

    @Test fun focusFreezesAtAssessmentAndSavesThatMeasurementWithTheSelectedResult() {
        val db = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle()).allowMainThreadQueries().build()
        val depo = OdakOturumuDeposu(context)
        depo.temizle(); depo.kaydet(OdakOturumu("ui-session", listOf("q"), 1_000))
        val images = LocalSoruGorselDeposu(context)
        val repo = RoomSoruRepository(db, images)
        val (ders, soru) = runBlocking {
            val d = RoomDersRepository(db).dersleriIzle(false).first().first()
            db.soruDao().ekle(SoruEntity("q", d.id, soruNotu = "Örnek soru metni", cozumNotu = "Örnek çözüm", olusturma = 1, guncelleme = 1))
            d to requireNotNull(repo.soruGetir("q"))
        }
        var now = 10_000L
        val tamam = AtomicBoolean(false)
        try {
            compose.setContent {
                YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                    OdakOturumuEkrani(soru, ders, repo, images, "ui-session", depo,
                        sira = 1, toplam = 1, sonuclar = emptyList(), cozumOnceAcildi = false,
                        onCozumAcildi = {}, onGeri = {}, onSonuc = { sonuc, cozum ->
                            assertEquals(TekrarSonucu.RAHAT, sonuc); assertFalse(cozum); tamam.set(true)
                        }, onSonaBirak = {}, onAtla = {}, monotonikSaat = MonotonikSaat { now })
                }
            }
            val degerlendir = context.getString(R.string.focus_assess)
            compose.waitUntil(5_000) { compose.onAllNodes(hasText(degerlendir) and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
            compose.waitForIdle()
            compose.runOnIdle { now += 5_000 }
            compose.onNodeWithText(degerlendir).performClick()
            compose.mainClock.advanceTimeBy(150) // allow the finite assessment scroll to lay out its items
            val rahat = context.getString(R.string.review_result_easy)
            compose.waitUntil(5_000) { compose.onAllNodes(hasText(rahat) and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText(rahat).performClick()
            compose.waitForIdle()
            val dondurulanRevizyon = requireNotNull(depo.oku()?.olcumler?.get("q")).revizyon
            compose.mainClock.advanceTimeBy(6_000)
            assertEquals(dondurulanRevizyon, depo.oku()?.olcumler?.get("q")?.revizyon)
            compose.runOnIdle { now += 30_000 } // reflection / choosing / saving never counts as study
            compose.onNodeWithText(context.getString(R.string.focus_save_last)).performClick()
            compose.waitUntil(5_000) { tamam.get() }
            val kayit = runBlocking { db.tekrarOlcumDao().tumunuGetir().single() }
            assertEquals(5_000L, kayit.denemeAktifMs)
            assertEquals(0L, kayit.incelemeAktifMs)
            assertEquals("TAM", kayit.kalite)
            assertEquals(1, runBlocking { db.tekrarDao().tumunuGetir().size })
        } finally { db.close() }
    }
}

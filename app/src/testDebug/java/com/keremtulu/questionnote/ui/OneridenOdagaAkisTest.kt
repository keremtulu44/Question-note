package com.keremtulu.questionnote.ui

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.export.YerelYedeklemeHizmeti
import com.keremtulu.questionnote.data.gelisim.MotorTaniHizmeti
import com.keremtulu.questionnote.data.images.*
import com.keremtulu.questionnote.data.ilerleme.*
import com.keremtulu.questionnote.data.local.*
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.prefs.*
import com.keremtulu.questionnote.data.repository.*
import com.keremtulu.questionnote.data.seviye.SeviyeHizmeti
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.domain.model.SinavProfili
import com.keremtulu.questionnote.ui.tema.*
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Real app shell + real Room repositories. This is a simulated UI regression, not physical-device acceptance. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
class OneridenOdagaAkisTest {
    @get:Rule val compose = createComposeRule()
    @Test fun recommendationFilterFocusReportAndProgressKeepEligibilityAndShadowDateContract() {
        val context: Context = RuntimeEnvironment.getApplication()
        listOf("karne_rozetleri", "rutbe_kutlamalari", "odak_oturumu", "sinav_profili", "tekrar_politikasi", "ilerleme_ayarlari")
            .forEach { context.getSharedPreferences(it, 0).edit().clear().commit() }
        val db = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle()).allowMainThreadQueries().build()
        val images = LocalSoruGorselDeposu(context)
        val profile = SinavProfiliDeposu(context)
        val motor = TekrarPolitikasiDeposu(context)
        val sessions = OdakOturumuDeposu(context)
        val badges = RozetDeposu(context)
        // The reward host has its own concurrency regression; use already-owned badges to isolate this navigation test.
        badges.kaydet(Rozet.entries, kutla = false)
        val ranks = RutbeDeposu(context)
        val goals = TercihGunlukHedefDeposu(context)
        val awards = RozetHizmeti(db, badges, goals, rutbeDeposu = ranks)
        val repo = RoomSoruRepository(db, images, motor = motor, sinav = profile)
        val lessons = RoomDersRepository(db, profile)
        val backup = YerelYedeklemeHizmeti(context, db, images) { sessions.temizle(); badges.geriYuklemeSonrasi(); ranks.geriYuklemeSonrasi() }
        val day = LocalDate.now().toEpochDay().toInt()
        runBlocking {
            val ds = db.dersDao().tumunuGetir()
            val math = ds.first { it.ad == "Matematik" }.id
            val physics = ds.first { it.ad == "Fizik" }.id
            profile.kaydet(SinavProfili.LGS, setOf(math))
            db.soruDao().ekle(listOf(
                SoruEntity("eligible", math, soruNotu = "Akış testi için soru", sonrakiTekrarGun = day, olusturma = 1, guncelleme = 1),
                SoruEntity("suspended", math, soruNotu = "Askıda", planDurumu = "ASKIDA", sonrakiTekrarGun = day, olusturma = 1, guncelleme = 1),
                SoruEntity("outside-profile", physics, soruNotu = "Profil dışında", sonrakiTekrarGun = day, olusturma = 1, guncelleme = 1),
            ))
        }
        try {
            compose.setContent {
                YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                    YanlisDefteriUygulamasi(
                        dersRepository = lessons, konuRepository = RoomKonuRepository(db), soruRepository = repo,
                        gorselDeposu = images, gorselTutarliligi = GorselTutarliligiHizmeti(db, images), disaAktarmaHizmeti = backup,
                        ilerlemeHizmeti = IlerlemeHizmeti(db), seviyeHizmeti = SeviyeHizmeti(db, badges, goals, awards, sinav = profile),
                        rozetDeposu = badges, rutbeDeposu = ranks, rozetHizmeti = awards, profilDeposu = profile,
                        tekrarPolitikasi = motor, motorTanisi = MotorTaniHizmeti(db), oturumDeposu = sessions,
                        gunlukHedefDeposu = goals, tercihHedefDeposu = goals,
                    )
                }
            }
            compose.waitUntil(10_000) { compose.onAllNodes(hasText("İlerleme") and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("İlerleme").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("progress-list").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("progress-list").performScrollToNode(hasTestTag("analysis-action"))
            compose.onNodeWithTag("analysis-action").performClick()
            compose.onNodeWithText(context.getString(R.string.analysis_filter_due)).assertExists()
            val start = context.getString(R.string.filtered_practice_start)
            compose.onNodeWithTag("practice-filter-list").performScrollToNode(hasText(start))
            compose.waitUntil(10_000) { compose.onAllNodes(hasText(start) and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText(start).performClick()
            val assess = context.getString(R.string.focus_assess)
            compose.waitUntil(10_000) { compose.onAllNodes(hasText(assess) and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Akış testi için soru").assertExists()
            compose.onNodeWithText(assess).performClick()
            compose.mainClock.advanceTimeBy(150)
            val easy = context.getString(R.string.review_result_easy)
            compose.waitUntil(10_000) { compose.onAllNodes(hasText(easy) and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText(easy).performClick()
            compose.onNodeWithText(context.getString(R.string.focus_save_last)).performClick()
            compose.waitUntil(10_000) { runBlocking { db.tekrarDao().tumunuGetir().size == 1 } }
            val record = runBlocking { db.tekrarDao().tumunuGetir().single() }
            assertEquals("eligible", record.soruId); assertEquals(2, record.algoritmaSurumu)
            val snapshot = org.json.JSONObject(runBlocking { db.tekrarOlcumDao().tumunuGetir().single().kararJson })
            assertEquals("GOLGE", snapshot.getString("mode"))
            assertEquals(snapshot.getInt("base_interval"), record.sonrakiAralik)
            val back = context.getString(R.string.karne_to_filters)
            compose.waitUntil(10_000) { compose.onAllNodesWithText(back).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText(back).performScrollTo().performClick()
            compose.waitUntil(10_000) { compose.onAllNodes(hasText("İlerleme") and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("İlerleme").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("progress-list").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("progress-list").performScrollToNode(hasText(context.getString(R.string.analysis_no_action)))
            compose.onNodeWithText(context.getString(R.string.analysis_no_action)).assertExists()
            assertEquals(1, runBlocking { db.tekrarDao().tumunuGetir().size })
        } finally { db.close() }
    }
}

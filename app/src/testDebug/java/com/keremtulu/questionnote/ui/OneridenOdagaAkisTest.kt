package com.keremtulu.questionnote.ui

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
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
        // Match AppContainer: stable service instances, never recreate dependencies during recomposition.
        val topics = RoomKonuRepository(db)
        val imageIntegrity = GorselTutarliligiHizmeti(db, images)
        val progress = IlerlemeHizmeti(db)
        val levels = SeviyeHizmeti(db, badges, goals, awards, sinav = profile)
        val diagnostics = MotorTaniHizmeti(db)
        val visible = mutableStateOf(true)
        val progressTab = hasText(context.getString(R.string.nav_progress)) and
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
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
                if (visible.value) YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                    YanlisDefteriUygulamasi(
                        dersRepository = lessons, konuRepository = topics, soruRepository = repo,
                        gorselDeposu = images, gorselTutarliligi = imageIntegrity, disaAktarmaHizmeti = backup,
                        ilerlemeHizmeti = progress, seviyeHizmeti = levels,
                        rozetDeposu = badges, rutbeDeposu = ranks, rozetHizmeti = awards, profilDeposu = profile,
                        tekrarPolitikasi = motor, motorTanisi = diagnostics, oturumDeposu = sessions,
                        gunlukHedefDeposu = goals, tercihHedefDeposu = goals,
                    )
                }
            }
            compose.waitUntil(10_000) { compose.onAllNodes(progressTab and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
            compose.onNode(progressTab).tikla()
            ilerlemeyiBekle()
            compose.onNodeWithTag("progress-list").performScrollToNode(hasTestTag("analysis-action"))
            compose.onNodeWithTag("analysis-action").tikla()
            compose.onNodeWithText(context.getString(R.string.analysis_filter_due)).assertExists()
            val start = context.getString(R.string.filtered_practice_start)
            compose.onNodeWithTag("practice-filter-list").performScrollToNode(hasText(start))
            compose.waitUntil(10_000) { compose.onAllNodes(hasText(start) and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText(start).tikla()
            val assess = context.getString(R.string.focus_assess)
            compose.waitUntil(10_000) { compose.onAllNodes(hasText(assess) and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Akış testi için soru").assertExists()
            compose.onNodeWithText(assess).tikla()
            compose.mainClock.advanceTimeBy(150)
            val easy = context.getString(R.string.review_result_easy)
            compose.waitUntil(10_000) { compose.onAllNodes(hasText(easy) and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText(easy).performScrollTo().tikla()
            val save = context.getString(R.string.focus_save_last)
            compose.waitUntil(10_000) { compose.onAllNodes(hasText(save) and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
            // Save is in the fixed Scaffold bottom bar, not the scrollable assessment list.
            compose.onNodeWithText(save).tikla()
            compose.waitUntil(10_000) { runBlocking { db.tekrarDao().tumunuGetir().size == 1 } }
            val record = runBlocking { db.tekrarDao().tumunuGetir().single() }
            assertEquals("eligible", record.soruId); assertEquals(2, record.algoritmaSurumu)
            val snapshot = org.json.JSONObject(runBlocking { db.tekrarOlcumDao().tumunuGetir().single().kararJson })
            assertEquals("GOLGE", snapshot.getString("mode"))
            assertEquals(snapshot.getInt("base_interval"), record.sonrakiAralik)
            // The report is a lazy list: scroll the stable viewport to its asynchronously loaded progress section.
            val report = compose.onNodeWithTag("karne-report-list")
            compose.waitUntil(10_000) {
                report.fetchSemanticsNodes().any { node ->
                    node.config.contains(SemanticsProperties.StateDescription) &&
                        node.config[SemanticsProperties.StateDescription] == context.getString(R.string.karne_kicker)
                }
            }
            report.performScrollToNode(hasText(context.getString(R.string.karne_progress_title)))
            val back = context.getString(R.string.karne_to_filters)
            report.performScrollToNode(hasText(back))
            compose.onNodeWithText(back).tikla()
            // The report also has a non-clickable "İlerleme" heading: wait for the actual Filter screen/tab.
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("practice-filter-list").fetchSemanticsNodes().isNotEmpty() }
            compose.waitUntil(10_000) { compose.onAllNodes(progressTab and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
            compose.onNode(progressTab).tikla()
            ilerlemeyiBekle()
            compose.onNodeWithTag("progress-list").performScrollToNode(hasText(context.getString(R.string.analysis_no_action)))
            compose.onNodeWithText(context.getString(R.string.analysis_no_action)).assertExists()
            assertEquals(1, runBlocking { db.tekrarDao().tumunuGetir().size })
        } finally {
            compose.runOnIdle { visible.value = false }
            compose.waitForIdle()
            db.close()
        }
    }
    // This test verifies routing/state through real UI actions, not Robolectric's asynchronous pixel hit testing.
    // Touch size/layout are covered separately; a report heading must never impersonate a tab click.
    private fun SemanticsNodeInteraction.tikla() = assertIsDisplayed().assertIsEnabled().assertHasClickAction()
        .performSemanticsAction(SemanticsActions.OnClick) { it() }
    private fun ilerlemeyiBekle() {
        try {
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("progress-list").fetchSemanticsNodes().isNotEmpty() }
        } catch (e: ComposeTimeoutException) {
            throw AssertionError("Progress did not load. UI state:\n" + compose.onRoot().printToString(), e)
        }
    }
}

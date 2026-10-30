package com.keremtulu.questionnote.ui

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.export.YerelYedeklemeHizmeti
import com.keremtulu.questionnote.data.gelisim.MotorTaniHizmeti
import com.keremtulu.questionnote.data.images.GorselTutarliligiHizmeti
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.ilerleme.IlerlemeHizmeti
import com.keremtulu.questionnote.data.ilerleme.RozetHizmeti
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.prefs.*
import com.keremtulu.questionnote.data.repository.*
import com.keremtulu.questionnote.data.seviye.SeviyeHizmeti
import com.keremtulu.questionnote.domain.calisma.OdakOturumu
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.domain.model.SinavProfili
import com.keremtulu.questionnote.domain.model.FiltreliCalismaEkraniAyar
import com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Real shell/Room regression; physical-device lifecycle and touch acceptance is separate. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
class OturumDevamArayuzTest {
    @get:Rule val compose = createComposeRule()
    private val context: Context get() = RuntimeEnvironment.getApplication()
    private val gorunur = mutableStateOf(true)
    private lateinit var db: YanlisDefteriDatabase
    private lateinit var sessions: OdakOturumuDeposu
    private lateinit var lessons: RoomDersRepository
    private lateinit var questions: RoomSoruRepository
    private lateinit var topics: RoomKonuRepository
    private lateinit var images: LocalSoruGorselDeposu
    private lateinit var profile: SinavProfiliDeposu
    private lateinit var motor: TekrarPolitikasiDeposu
    private lateinit var badges: RozetDeposu
    private lateinit var ranks: RutbeDeposu
    private lateinit var goals: TercihGunlukHedefDeposu
    private lateinit var awards: RozetHizmeti
    private lateinit var backup: YerelYedeklemeHizmeti
    private lateinit var progress: IlerlemeHizmeti
    private lateinit var levels: SeviyeHizmeti
    private lateinit var diagnostics: MotorTaniHizmeti
    private lateinit var integrity: GorselTutarliligiHizmeti
    private lateinit var trials: DenemeDeposu

    @Before fun hazirla() {
        listOf("karne_rozetleri", "rutbe_kutlamalari", "odak_oturumu", "sinav_profili", "tekrar_politikasi",
            "ilerleme_ayarlari", "denemeler", "calisma_tercihleri").forEach {
            context.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().commit()
        }
        db = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle()).allowMainThreadQueries().build()
        images = LocalSoruGorselDeposu(context)
        profile = SinavProfiliDeposu(context)
        motor = TekrarPolitikasiDeposu(context)
        sessions = OdakOturumuDeposu(context)
        badges = RozetDeposu(context).also { it.kaydet(Rozet.entries, kutla = false) }
        ranks = RutbeDeposu(context)
        goals = TercihGunlukHedefDeposu(context)
        awards = RozetHizmeti(db, badges, goals, rutbeDeposu = ranks)
        lessons = RoomDersRepository(db, profile)
        questions = RoomSoruRepository(db, images, motor = motor, sinav = profile)
        topics = RoomKonuRepository(db)
        integrity = GorselTutarliligiHizmeti(db, images)
        progress = IlerlemeHizmeti(db)
        levels = SeviyeHizmeti(db, badges, goals, awards, sinav = profile)
        diagnostics = MotorTaniHizmeti(db)
        trials = DenemeDeposu(context)
        backup = YerelYedeklemeHizmeti(context, db, images) {
            sessions.temizle(); badges.geriYuklemeSonrasi(); ranks.geriYuklemeSonrasi()
        }
        runBlocking {
            val math = db.dersDao().tumunuGetir().first { it.ad == "Matematik" }.id
            profile.kaydet(SinavProfili.LGS, setOf(math))
            val gun = LocalDate.now().toEpochDay().toInt()
            db.soruDao().ekle(listOf(
                SoruEntity("q1", math, soruNotu = "Birinci test sorusu", cozumNotu = "Kaydedilen çözüm açıklaması",
                    sonrakiTekrarGun = gun, olusturma = 1, guncelleme = 1),
                SoruEntity("q2", math, soruNotu = "İkinci test sorusu", sonrakiTekrarGun = gun,
                    olusturma = 2, guncelleme = 2),
            ))
        }
        sessions.kaydet(OdakOturumu("bekleyen", listOf("q1", "q2"), System.currentTimeMillis(), bugunden = true))
    }

    @After fun kapat() {
        compose.runOnIdle { gorunur.value = false }
        compose.waitForIdle()
        db.close()
    }

    private fun uygulamayiAc() {
        compose.setContent {
            if (gorunur.value) YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                YanlisDefteriUygulamasi(
                    dersRepository = lessons, konuRepository = topics, soruRepository = questions,
                    gorselDeposu = images, gorselTutarliligi = integrity, disaAktarmaHizmeti = backup,
                    ilerlemeHizmeti = progress, seviyeHizmeti = levels, rozetDeposu = badges,
                    rutbeDeposu = ranks, rozetHizmeti = awards, profilDeposu = profile,
                    tekrarPolitikasi = motor, motorTanisi = diagnostics, oturumDeposu = sessions,
                    gunlukHedefDeposu = goals, tercihHedefDeposu = goals, denemeDeposu = trials,
                )
            }
        }
        val devam = context.getString(R.string.focus_continue_session)
        compose.waitUntil(10_000) { compose.onAllNodes(hasText(devam)).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun startupOffersResumeAndNewSessionCannotSilentlyOverwriteIt() {
        uygulamayiAc()
        val assess = context.getString(R.string.focus_assess)
        compose.onAllNodesWithText(assess).assertCountEquals(0)
        assertEquals("bekleyen", sessions.oku()?.id)

        val start = context.getString(R.string.focus_start_today)
        // The resume card can appear before today's Room query completes.
        compose.waitUntil(10_000) {
            try {
                compose.onNodeWithTag("today-list").performScrollToNode(hasText(start))
                compose.onAllNodes(hasText(start) and isEnabled()).fetchSemanticsNodes().isNotEmpty()
            } catch (_: AssertionError) { false }
        }
        compose.onNodeWithText(start).tikla()
        compose.onNodeWithText(context.getString(R.string.focus_replace_title)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.topic_cancel)).tikla()
        assertEquals("bekleyen", sessions.oku()?.id)

        val devam = context.getString(R.string.focus_continue_session)
        compose.onNodeWithTag("today-list").performScrollToNode(hasText(devam))
        compose.onNodeWithText(devam).tikla()
        compose.waitUntil(10_000) { compose.onAllNodes(hasText(assess) and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Birinci test sorusu").assertExists()
        val reveal = context.getString(R.string.focus_reveal)
        compose.onNodeWithTag("focus-list").performScrollToNode(hasText(reveal))
        compose.onNodeWithText(reveal).tikla()
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasText(context.getString(R.string.question_hide_solution))).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("focus-list").performScrollToNode(hasText("Kaydedilen çözüm açıklaması"))
        compose.onNodeWithText("Kaydedilen çözüm açıklaması").assertExists()
        assertTrue("q1" in requireNotNull(sessions.oku()).cozumAcilanlar)
    }

    @Test fun removingPendingSessionRequiresConfirmationAndKeepsQuestions() {
        uygulamayiAc()
        compose.onNodeWithText(context.getString(R.string.focus_remove_action)).tikla()
        compose.onNodeWithText(context.getString(R.string.focus_remove_title)).assertIsDisplayed()
        assertNotNull(sessions.oku())
        compose.onNodeWithText(context.getString(R.string.focus_remove_confirm)).tikla()
        compose.waitUntil(10_000) { sessions.oku() == null }
        assertNotNull(runBlocking { questions.soruGetir("q1") })
        assertNotNull(runBlocking { questions.soruGetir("q2") })
        compose.onAllNodesWithText(context.getString(R.string.focus_continue_session)).assertCountEquals(0)
    }

    @Test fun practiceFiltersSurviveTabNavigationAndRecreatingTheAppShell() {
        val math = runBlocking { db.dersDao().tumunuGetir().first { it.ad == "Matematik" }.id }
        val preferences = CalismaTercihleriDeposu(context)
        preferences.filtreliKaydet(SinavProfili.LGS,
            FiltreliCalismaEkraniAyar(filtre = SoruCalismaFiltresi(dersId = math), soruSayisi = "5"))
        val practiceTab = hasText(context.getString(R.string.nav_practice)) and
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
        val todayTab = hasText(context.getString(R.string.nav_today)) and
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
        val oldest = context.getString(R.string.focus_oldest)
        fun filtreyiAc() {
            compose.onNode(practiceTab).tikla()
            compose.waitUntil(10_000) {
                try {
                    compose.onNodeWithTag("practice-filter-list").performScrollToNode(hasText(oldest))
                    true
                } catch (_: AssertionError) { false }
            }
        }
        uygulamayiAc()
        filtreyiAc()
        compose.onNodeWithText(oldest).tikla()
        assertEquals(math, preferences.filtreliOku(SinavProfili.LGS).filtre.dersId)
        assertEquals("5", preferences.filtreliOku(SinavProfili.LGS).soruSayisi)
        assertFalse(preferences.filtreliOku(SinavProfili.LGS).akilliSira)
        compose.onNode(todayTab).tikla()
        filtreyiAc()
        compose.onNodeWithText(oldest).assertIsSelected()

        // Dispose and recreate the shell to exercise the on-disk preferences, not just remember().
        compose.runOnIdle { gorunur.value = false }
        compose.waitForIdle()
        compose.runOnIdle { gorunur.value = true }
        val devam = context.getString(R.string.focus_continue_session)
        compose.waitUntil(10_000) { compose.onAllNodes(hasText(devam)).fetchSemanticsNodes().isNotEmpty() }
        filtreyiAc()
        compose.onNodeWithText(oldest).assertIsSelected()
        assertEquals(math, CalismaTercihleriDeposu(context).filtreliOku(SinavProfili.LGS).filtre.dersId)
    }

    @Test fun lessonSearchSurvivesOpeningAndReturningFromAQuestion() {
        uygulamayiAc()
        val lessonsTab = hasText(context.getString(R.string.nav_lessons)) and
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
        compose.onNode(lessonsTab).tikla()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("lessons-grid").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("lessons-grid").performScrollToNode(hasText("Matematik"))
        compose.onNodeWithText("Matematik").tikla()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("lesson-question-list").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("lesson-question-list").performScrollToNode(hasTestTag("lesson-search"))
        compose.onNodeWithTag("lesson-search").performTextReplacement("Birinci")
        compose.onNodeWithTag("lesson-search").performImeAction()
        compose.waitUntil(10_000) {
            try {
                compose.onNodeWithTag("lesson-question-list").performScrollToNode(hasText("Birinci test sorusu"))
                true
            } catch (_: AssertionError) { false }
        }
        compose.onNodeWithText("Birinci test sorusu").tikla()
        val back = context.getString(R.string.question_back)
        compose.waitUntil(10_000) { compose.onAllNodes(hasText(back) and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(back).tikla()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("lesson-question-list").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("lesson-question-list").performScrollToNode(hasTestTag("lesson-search"))
        compose.onNodeWithTag("lesson-search").assertTextContains("Birinci")
        val math = runBlocking { db.dersDao().tumunuGetir().first { it.ad == "Matematik" }.id }
        assertEquals("Birinci", CalismaTercihleriDeposu(context).dersOku(SinavProfili.LGS, math).arama)
    }

    private fun SemanticsNodeInteraction.tikla() = assertIsDisplayed().assertIsEnabled().assertHasClickAction()
        .performSemanticsAction(SemanticsActions.OnClick) { it() }
}

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
import com.keremtulu.questionnote.data.images.GorselTutarliligiHizmeti
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.ilerleme.IlerlemeHizmeti
import com.keremtulu.questionnote.data.ilerleme.RozetHizmeti
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.prefs.*
import com.keremtulu.questionnote.data.repository.RoomDersRepository
import com.keremtulu.questionnote.data.repository.RoomKonuRepository
import com.keremtulu.questionnote.data.repository.RoomSoruRepository
import com.keremtulu.questionnote.data.seviye.SeviyeHizmeti
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Exercises the actual app shell's state holder when a student enters and leaves a screen. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
class OgrenciGezinmeDurumuTest {
    @get:Rule val compose = createComposeRule()
    private val visible = mutableStateOf(true)
    private val context: Context = RuntimeEnvironment.getApplication()
    private val preferences = listOf(
        "karne_rozetleri", "rutbe_kutlamalari", "odak_oturumu", "sinav_profili", "tekrar_politikasi",
        "ilerleme_ayarlari", "denemeler", "calisma_tercihleri",
    )
    private lateinit var db: YanlisDefteriDatabase
    private lateinit var math: Ders
    private lateinit var physics: Ders
    private lateinit var images: LocalSoruGorselDeposu
    private lateinit var profile: SinavProfiliDeposu
    private lateinit var motor: TekrarPolitikasiDeposu
    private lateinit var sessions: OdakOturumuDeposu
    private lateinit var badges: RozetDeposu
    private lateinit var ranks: RutbeDeposu
    private lateinit var goals: TercihGunlukHedefDeposu
    private lateinit var lessons: RoomDersRepository
    private lateinit var topics: RoomKonuRepository
    private lateinit var questions: RoomSoruRepository
    private lateinit var progress: IlerlemeHizmeti
    private lateinit var awards: RozetHizmeti
    private lateinit var levels: SeviyeHizmeti
    private lateinit var diagnostics: MotorTaniHizmeti
    private lateinit var imageIntegrity: GorselTutarliligiHizmeti
    private lateinit var backup: YerelYedeklemeHizmeti
    private lateinit var exams: DenemeDeposu

    @Before fun setup() {
        preferences.forEach { context.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().commit() }
        db = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle()).allowMainThreadQueries().build()
        images = LocalSoruGorselDeposu(context)
        profile = SinavProfiliDeposu(context)
        motor = TekrarPolitikasiDeposu(context)
        sessions = OdakOturumuDeposu(context)
        badges = RozetDeposu(context).also { it.kaydet(Rozet.entries, kutla = false) }
        ranks = RutbeDeposu(context)
        goals = TercihGunlukHedefDeposu(context)
        lessons = RoomDersRepository(db, profile)
        topics = RoomKonuRepository(db)
        questions = RoomSoruRepository(db, images, motor = motor, sinav = profile)
        progress = IlerlemeHizmeti(db)
        awards = RozetHizmeti(db, badges, goals, rutbeDeposu = ranks)
        levels = SeviyeHizmeti(db, badges, goals, awards, sinav = profile)
        diagnostics = MotorTaniHizmeti(db)
        imageIntegrity = GorselTutarliligiHizmeti(db, images)
        backup = YerelYedeklemeHizmeti(context, db, images) {
            sessions.temizle(); badges.geriYuklemeSonrasi(); ranks.geriYuklemeSonrasi()
        }
        exams = DenemeDeposu(context)
        runBlocking {
            val all = lessons.kataloguHazirla()
            math = all.first { it.ad == "Matematik" }
            physics = all.first { it.ad == "Fizik" }
            lessons.dersSeciminiKaydet(setOf(math.id, physics.id))
            db.soruDao().ekle(listOf(
                SoruEntity("ask-math", math.id, soruNotu = "Matematik sorusu",
                    planDurumu = "OGRETMENE_SORULACAK", olusturma = 1L, guncelleme = 1L),
                SoruEntity("ask-physics", physics.id, soruNotu = "Fizik sorusu",
                    planDurumu = "OGRETMENE_SORULACAK", olusturma = 1L, guncelleme = 1L),
            ))
        }
    }

    private fun app() {
        compose.setContent {
            if (visible.value) YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                YanlisDefteriUygulamasi(
                    dersRepository = lessons, konuRepository = topics, soruRepository = questions,
                    gorselDeposu = images, gorselTutarliligi = imageIntegrity, disaAktarmaHizmeti = backup,
                    ilerlemeHizmeti = progress, seviyeHizmeti = levels, rozetDeposu = badges,
                    rutbeDeposu = ranks, rozetHizmeti = awards, profilDeposu = profile,
                    tekrarPolitikasi = motor, motorTanisi = diagnostics, oturumDeposu = sessions,
                    gunlukHedefDeposu = goals, tercihHedefDeposu = goals, denemeDeposu = exams,
                )
            }
        }
    }

    @After fun teardown() {
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        db.close()
        preferences.forEach { context.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().commit() }
    }

    private fun SemanticsNodeInteraction.tikla() = assertIsDisplayed().assertIsEnabled()
        .performSemanticsAction(SemanticsActions.OnClick) { it() }

    @Test fun sorulacaklarDersFiltresiSoruDetayindanDonunceKorunur() {
        app()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("today-list").fetchSemanticsNodes().isNotEmpty() }
        // The shortcut sits below the empty state and its Room-backed data is loaded asynchronously.
        compose.waitUntil(10_000) {
            runCatching {
                compose.onNodeWithTag("today-list").performScrollToNode(hasTestTag("bugun_sorulacaklar_karti"))
            }.isSuccess
        }
        compose.onNodeWithTag("bugun_sorulacaklar_karti").tikla()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Matematik (1)").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Matematik (1)").tikla()
        compose.onNodeWithText("Matematik (1)").assertIsSelected()
        compose.onNodeWithTag("sorulacaklar_liste").performScrollToNode(hasTestTag("sorulacak_kart_ask-math"))
        compose.onNodeWithTag("sorulacak_kart_ask-math").tikla()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(context.getString(R.string.question_back)).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(context.getString(R.string.question_back)).tikla()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithTag("sorulacaklar_liste").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("sorulacaklar_liste").performScrollToNode(hasText("Matematik (1)"))
        compose.onNodeWithText("Matematik (1)").assertIsSelected()
    }

    @Test fun konuAramasiVeArsivFiltresiIleriGeriGezinmedeKorunur() {
        app()
        val settingsTab = hasText(context.getString(R.string.nav_settings)) and
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
        compose.onNode(settingsTab).tikla()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("settings-list").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("settings-list").performScrollToNode(hasText(context.getString(R.string.settings_topics)))
        compose.onNodeWithText(context.getString(R.string.settings_topics)).tikla()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("topics-search-field").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("topics-search-field").performTextReplacement("Fizik")
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("topics-lesson-${physics.id}").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("topics-lesson-${physics.id}").tikla()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(context.getString(R.string.topic_filter_archived)).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(context.getString(R.string.topic_filter_archived)).tikla()
        compose.onNodeWithText(context.getString(R.string.topic_back_to_center)).tikla()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("topics-search-field").fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(hasTestTag("topics-search-field") and hasText("Fizik")).assertExists()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("topics-lesson-${physics.id}").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("topics-lesson-${physics.id}").tikla()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(context.getString(R.string.topic_filter_archived)).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(context.getString(R.string.topic_filter_archived)).assertIsSelected()
    }
}

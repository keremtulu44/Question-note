package com.keremtulu.questionnote.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.room.Room
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.gelisim.MotorTaniHizmeti
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.prefs.SinavProfiliDeposu
import com.keremtulu.questionnote.data.prefs.TekrarPolitikasiDeposu
import com.keremtulu.questionnote.data.prefs.TercihGunlukHedefDeposu
import com.keremtulu.questionnote.data.repository.RoomDersRepository
import com.keremtulu.questionnote.data.repository.RoomKonuRepository
import com.keremtulu.questionnote.data.repository.RoomSoruRepository
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.SoruSecenegi
import com.keremtulu.questionnote.domain.model.SoruSecenekTuru
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository
import com.keremtulu.questionnote.ui.ayarlar.AyarlarEkrani
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Simulated UI and Room regressions; these do not replace a physical-device test. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
class OgrenciAyrintiArayuzTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private lateinit var context: Context
    private lateinit var db: YanlisDefteriDatabase
    private lateinit var images: LocalSoruGorselDeposu
    private lateinit var profile: SinavProfiliDeposu
    private lateinit var lessons: RoomDersRepository
    private lateinit var topics: RoomKonuRepository
    private lateinit var questions: RoomSoruRepository
    private lateinit var math: Ders
    private lateinit var physics: Ders

    @Before fun setup() {
        context = RuntimeEnvironment.getApplication()
        listOf("sinav_profili", "ilerleme_ayarlari", "tekrar_politikasi").forEach {
            context.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().commit()
        }
        db = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle()).allowMainThreadQueries().build()
        images = LocalSoruGorselDeposu(context)
        profile = SinavProfiliDeposu(context)
        lessons = RoomDersRepository(db, profile)
        topics = RoomKonuRepository(db)
        questions = RoomSoruRepository(db, images, sinav = profile)
        val all = runBlocking { lessons.kataloguHazirla() }
        math = all.first { it.ad == "Matematik" }
        physics = all.first { it.ad == "Fizik" }
        runBlocking { lessons.dersSeciminiKaydet(setOf(math.id)) }
    }

    @After fun teardown() {
        db.close()
        listOf("sinav_profili", "ilerleme_ayarlari", "tekrar_politikasi").forEach {
            context.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().commit()
        }
    }

    private fun soruEkle(id: String, solution: String?) = runBlocking {
        db.soruDao().ekle(
            SoruEntity(
                id = id, dersId = math.id, soruNotu = "Detay testi", cozumNotu = solution,
                sonrakiTekrarGun = LocalDate.now().toEpochDay().toInt(), olusturma = 1L, guncelleme = 1L,
            ),
        )
    }

    @Test fun bosCozumDugmeGibiSunulmazVeBagimsizCozumDiyeKaydedilir() {
        soruEkle("bos", "   ")
        val callback = AtomicReference<Boolean?>(null)
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                SoruDetayEkrani(
                    soruId = "bos", ders = math, dersArsivli = false, soruRepository = questions,
                    konuRepository = topics, gorselDeposu = images, onGeri = {}, onDuzenle = {},
                    onTekrarTamamlandi = { _, acildi -> callback.set(acildi) }, tekrarOturumunda = true,
                )
            }
        }
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("soru-detay-list").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("soru-detay-list").performScrollToNode(hasText(context.getString(R.string.focus_no_solution)))
        compose.onNodeWithText(context.getString(R.string.focus_no_solution)).assertIsDisplayed()
        compose.onAllNodesWithText(context.getString(R.string.question_show_solution)).assertCountEquals(0)
        compose.onNodeWithTag("soru-detay-list").performScrollToNode(hasText(context.getString(R.string.review_result_easy)))
        compose.onNodeWithText(context.getString(R.string.review_result_easy)).performClick()
        compose.waitUntil(10_000) { callback.get() != null }
        assertFalse(callback.get()!!)
        assertFalse(runBlocking { db.tekrarDao().tumunuGetir().single().cozumAcildi })
    }

    @Test fun acilmisCozumDurumKurtarmadanSonraDaAcildiOlarakKaydedilir() {
        soruEkle("cozumlu", "Gerçek çözüm")
        val callback = AtomicReference<Boolean?>(null)
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                SoruDetayEkrani(
                    soruId = "cozumlu", ders = math, dersArsivli = false, soruRepository = questions,
                    konuRepository = topics, gorselDeposu = images, onGeri = {}, onDuzenle = {},
                    onTekrarTamamlandi = { _, acildi -> callback.set(acildi) }, tekrarOturumunda = true,
                )
            }
        }
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("soru-detay-list").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("soru-detay-list").performScrollToNode(hasText(context.getString(R.string.question_show_solution)))
        compose.onNodeWithText(context.getString(R.string.question_show_solution)).performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("soru-detay-list").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("soru-detay-list").performScrollToNode(hasText(context.getString(R.string.question_hide_solution)))
        compose.onNodeWithText(context.getString(R.string.question_hide_solution)).assertIsDisplayed()
        compose.onNodeWithTag("soru-detay-list").performScrollToNode(hasText(context.getString(R.string.review_result_easy)))
        compose.onNodeWithText(context.getString(R.string.review_result_easy)).performClick()
        compose.waitUntil(10_000) { callback.get() != null }
        assertTrue(callback.get()!!)
        assertTrue(runBlocking { db.tekrarDao().tumunuGetir().single().cozumAcildi })
    }

    private fun ayarlariGoster(repository: DersRepository, buyukYazi: Boolean = false) {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = if (buyukYazi) 2f else 1f)) {
                YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                    AyarlarEkrani(
                        gunlukHedefDeposu = TercihGunlukHedefDeposu(context), profilDeposu = profile,
                        tekrarPolitikasi = TekrarPolitikasiDeposu(context), motorTanisi = MotorTaniHizmeti(db),
                        dersRepository = repository, sonYedek = null, yedeklemeIslemde = false,
                        onYedekOlustur = {}, onYedekGeriYukle = {}, onGorselBakimi = {},
                        onKatalog = {}, onKonular = {},
                    )
                }
            }
        }
    }

    @Test fun sonDersKapatilamiyorsaNedeniBuyukYazidaDaAciklanir() {
        val twoLessons = object : DersRepository by lessons {
            override fun dersleriIzle(arsivli: Boolean): Flow<List<Ders>> =
                flowOf(if (arsivli) emptyList() else listOf(math, physics))
        }
        ayarlariGoster(twoLessons, buyukYazi = true)
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Matematik").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Matematik").performScrollTo().performClick()
        compose.onNodeWithText(context.getString(R.string.settings_profile_at_least_one)).performScrollTo().assertIsDisplayed()
        assertEquals(setOf(math.id), profile.secimIzle.value.dersIdleri)
        compose.onNodeWithText("Fizik").performScrollTo().performClick()
        compose.waitUntil(5_000) { physics.id in profile.secimIzle.value.dersIdleri }
        compose.onNodeWithText(context.getString(R.string.settings_profile_at_least_one)).assertDoesNotExist()
    }

    @Test fun ayarlardaDersAkisiHatasiSonrasiYenidenDenenebilir() {
        val fail = AtomicBoolean(true)
        val flaky = object : DersRepository by lessons {
            override fun dersleriIzle(arsivli: Boolean): Flow<List<Ders>> = flow {
                if (fail.getAndSet(false)) error("geçici ders hatası")
                emit(listOf(math, physics))
            }
        }
        ayarlariGoster(flaky)
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText(context.getString(R.string.settings_profile_load_error)).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(context.getString(R.string.topic_retry)).performScrollTo().performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Matematik").fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun katalogHatasiAciklanirVeYenidenDenenir() {
        val fail = AtomicBoolean(true)
        val flaky = object : SoruRepository by questions {
            override fun secenekleriIzle(tur: SoruSecenekTuru): Flow<List<SoruSecenegi>> = flow {
                if (tur == SoruSecenekTuru.KAYNAK && fail.getAndSet(false)) error("geçici katalog hatası")
                emitAll(questions.secenekleriIzle(tur))
            }
        }
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                KatalogYonetimiEkrani(soruRepository = flaky, onGeri = {})
            }
        }
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText(context.getString(R.string.catalog_load_error)).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(context.getString(R.string.topic_retry)).performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText(context.getString(R.string.catalog_source_heading)).fetchSemanticsNodes().isNotEmpty()
        }
    }
}

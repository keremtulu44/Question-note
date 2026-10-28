package com.keremtulu.questionnote.ui

import android.content.ComponentName
import android.content.Context
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.room.Room
import com.keremtulu.questionnote.MainActivity
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.prefs.DenemeDeposu
import com.keremtulu.questionnote.data.prefs.SinavProfiliDeposu
import com.keremtulu.questionnote.data.repository.RoomDersRepository
import com.keremtulu.questionnote.data.repository.RoomSoruRepository
import com.keremtulu.questionnote.domain.model.DenemeDersSatiri
import com.keremtulu.questionnote.domain.model.DenemeKaydi
import com.keremtulu.questionnote.domain.model.NetKurali
import com.keremtulu.questionnote.domain.model.SoruSecenekTuru
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.repository.SoruRepository
import com.keremtulu.questionnote.ui.deneme.DenemeDetayEkrani
import com.keremtulu.questionnote.ui.deneme.DenemeListesiEkrani
import com.keremtulu.questionnote.ui.deneme.denemeTarihYazi
import com.keremtulu.questionnote.ui.kurulum.DersKurulumEkrani
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
class DenemeVeKurulumArayuzTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private lateinit var database: YanlisDefteriDatabase
    private lateinit var profilDeposu: SinavProfiliDeposu
    private lateinit var denemeDeposu: DenemeDeposu
    private lateinit var dersRepository: RoomDersRepository
    private lateinit var soruRepository: RoomSoruRepository

    @Before
    fun hazirla() {
        val context = RuntimeEnvironment.getApplication()
        listOf("sinav_profili", "denemeler", "ilerleme_ayarlari").forEach {
            context.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().commit()
        }
        database = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
            .allowMainThreadQueries()
            .addCallback(VarsayilanDersleriEkle())
            .build()
        database.openHelper.writableDatabase
        profilDeposu = SinavProfiliDeposu(context)
        denemeDeposu = DenemeDeposu(context)
        dersRepository = RoomDersRepository(database, profilDeposu)
        soruRepository = RoomSoruRepository(database, LocalSoruGorselDeposu(context), sinav = profilDeposu)
    }

    @After
    fun temizle() {
        val context = RuntimeEnvironment.getApplication()
        database.close()
        listOf("sinav_profili", "denemeler", "ilerleme_ayarlari").forEach {
            context.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().commit()
        }
    }

    @Test
    fun dersKurulumSecimiDurumRestorasyonunuKorurVeKaydeder() {
        val restoration = StateRestorationTester(compose)
        var tamamlandi = false

        restoration.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                DersKurulumEkrani(
                    dersRepository = dersRepository,
                    onTamam = { tamamlandi = true },
                )
            }
        }
        compose.waitForIdle()

        compose.onNodeWithTag("onboarding-select-all").performClick()
        compose.waitForIdle()
        compose.onNodeWithText(compose.activity.getString(R.string.onboarding_selected_count, 24))
            .assertIsDisplayed()

        restoration.emulateSavedInstanceStateRestore()
        compose.waitForIdle()

        compose.onNodeWithText(compose.activity.getString(R.string.onboarding_selected_count, 24))
            .assertIsDisplayed()
        compose.onNodeWithText(compose.activity.getString(R.string.onboarding_continue)).performClick()
        compose.waitUntil(5_000) { tamamlandi }

        assertTrue(tamamlandi)
        assertTrue(!profilDeposu.kurulumGerekli())
    }

    @Test
    fun denemeListesiBosDurumuVeDoluGrafikKartlariniGosterir() {
        runBlocking { dersRepository.kataloguHazirla() }

        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                DenemeListesiEkrani(
                    denemeDeposu = denemeDeposu,
                    dersRepository = dersRepository,
                    soruRepository = soruRepository,
                    onGeri = {},
                    onYeni = {},
                    onAc = {},
                )
            }
        }
        compose.waitForIdle()
        compose.onNodeWithTag("deneme-empty-state").assertIsDisplayed()

        val matId = runBlocking { database.dersDao().tumunuGetir().first { it.ad == "Matematik" }.id }
        denemeDeposu.kaydet(
            DenemeKaydi("d1", "Miray 1", NetKurali.DORTTE_BIR, 10L, 20_000, listOf(DenemeDersSatiri(matId, 40, 20, 8, "Matematik"))),
        )
        denemeDeposu.kaydet(
            DenemeKaydi("d2", "Miray 2", NetKurali.DORTTE_BIR, 20L, 20_005, listOf(DenemeDersSatiri(matId, 40, 28, 4, "Matematik"))),
        )
        compose.waitForIdle()

        compose.onNodeWithTag("deneme-net-chart").assertIsDisplayed()
        val history = compose.activity.getString(R.string.deneme_history_header, 2)
        compose.onNodeWithTag("deneme-list").performScrollToNode(hasText(history))
        compose.onNodeWithText(history).assertIsDisplayed()
        compose.onNodeWithTag("deneme-list").performScrollToNode(hasTestTag("deneme-card-d2"))
        compose.onNodeWithTag("deneme-card-d2").assertIsDisplayed()
        val delta = compose.activity.getString(R.string.deneme_chart_change, "+9")
        compose.onNodeWithContentDescription(delta, useUnmergedTree = true).assertExists()
    }

    @Test
    fun denemeGrafigiKuralVeKapsamSecimindeYalnizBenzerKayitlariGosterir() {
        runBlocking { dersRepository.kataloguHazirla() }
        val mat = runBlocking { database.dersDao().tumunuGetir().first { it.ad == "Matematik" }.id }
        listOf(
            DenemeKaydi("a", "TYT 1", NetKurali.DORTTE_BIR, 1, 20_000, listOf(DenemeDersSatiri(mat, 40, 20, 8, "Matematik"))),
            DenemeKaydi("b", "TYT 2", NetKurali.DORTTE_BIR, 2, 20_001, listOf(DenemeDersSatiri(mat, 40, 28, 4, "Matematik"))),
            DenemeKaydi("c", "Farklı kapsam", NetKurali.DORTTE_BIR, 3, 20_002, listOf(DenemeDersSatiri(mat, 20, 10, 4, "Matematik"))),
            DenemeKaydi("d", "Üç yanlış", NetKurali.UCTE_BIR, 4, 20_003, listOf(DenemeDersSatiri(mat, 40, 20, 3, "Matematik"))),
        ).forEach(denemeDeposu::kaydet)
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                DenemeListesiEkrani(denemeDeposu, dersRepository, soruRepository, onGeri = {}, onYeni = {}, onAc = {})
            }
        }
        compose.waitForIdle()
        compose.onNodeWithTag("deneme-rule-DORTTE_BIR").performClick()
        compose.onNodeWithTag("deneme-list").performScrollToNode(hasTestTag("deneme-scope-selector"))
        compose.onNodeWithTag("deneme-scope-selector").performClick()
        compose.onNodeWithTag("deneme-scope-option-b").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("deneme-list").performScrollToNode(hasTestTag("deneme-net-chart"))
        val grafik = compose.onNodeWithTag("deneme-net-chart").fetchSemanticsNode()
            .config[SemanticsProperties.ContentDescription].joinToString()
        assertTrue(grafik.contains(denemeTarihYazi(20_000)))
        assertTrue(grafik.contains(denemeTarihYazi(20_001)))
        assertTrue(!grafik.contains(denemeTarihYazi(20_002)))
        assertTrue(!grafik.contains(denemeTarihYazi(20_003)))

        compose.onNodeWithTag("deneme-list").performScrollToNode(hasTestTag("deneme-distribution-chart"))
        val dagilim = compose.onNodeWithTag("deneme-distribution-chart").fetchSemanticsNode()
            .config[SemanticsProperties.ContentDescription].joinToString()
        assertTrue(dagilim.contains("28 doğru, 4 yanlış, 8 boş"))
        compose.onNodeWithTag("deneme-list").performScrollToNode(hasTestTag("deneme-lessons-card"))
        compose.onNodeWithTag("deneme-lessons-card").assertIsDisplayed()
    }

    @Test
    fun soruSayilariOkunamazsaSifirGibiGosterilmez() {
        runBlocking { dersRepository.kataloguHazirla() }
        val mat = runBlocking { database.dersDao().tumunuGetir().first { it.ad == "Matematik" }.id }
        denemeDeposu.kaydet(DenemeKaydi("d1", "Deneme", NetKurali.DORTTE_BIR, 1, 20_000,
            listOf(DenemeDersSatiri(mat, 40, 20, 8, "Matematik"))))
        val bozukKaynak = object : SoruRepository by soruRepository {
            override fun sorulariIzle(dersId: String, konuId: String?, konusuz: Boolean): Flow<List<Soru>> =
                flow { throw IllegalStateException("Test okuma hatası") }
        }
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                DenemeListesiEkrani(denemeDeposu, dersRepository, bozukKaynak, onGeri = {}, onYeni = {}, onAc = {})
            }
        }
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("deneme-question-counts-error").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("deneme-question-counts-error").assertIsDisplayed()
        compose.onNodeWithTag("deneme-list").performScrollToNode(hasTestTag("deneme-card-d1"))
        compose.onNodeWithText(compose.activity.getString(R.string.deneme_upload_progress, 0, 20)).assertDoesNotExist()
    }

    @Test
    fun denemeGrafigiSonAltiVeSonOnIkiAraliginiDegistirir() {
        runBlocking { dersRepository.kataloguHazirla() }
        val mat = runBlocking { database.dersDao().tumunuGetir().first { it.ad == "Matematik" }.id }
        repeat(13) { index ->
            denemeDeposu.kaydet(DenemeKaydi("d$index", "Deneme $index", NetKurali.DORTTE_BIR,
                index.toLong(), 20_000 + index, listOf(DenemeDersSatiri(mat, 40, 10 + index, 4, "Matematik"))))
        }
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                DenemeListesiEkrani(denemeDeposu, dersRepository, soruRepository, onGeri = {}, onYeni = {}, onAc = {})
            }
        }
        compose.waitForIdle()
        compose.onNodeWithTag("deneme-range-6").performClick()
        compose.onNodeWithTag("deneme-list").performScrollToNode(hasTestTag("deneme-net-chart"))
        val grafik = compose.onNodeWithTag("deneme-net-chart").fetchSemanticsNode()
            .config[SemanticsProperties.ContentDescription].joinToString()
        assertTrue(grafik.contains(denemeTarihYazi(20_007)))
        assertTrue(grafik.contains(denemeTarihYazi(20_012)))
        assertTrue(!grafik.contains(denemeTarihYazi(20_006)))
    }

    @Test
    fun denemeDetayKaydederKatalogSenkronlarGizliDersiKorurVeSilmeOnayiIster() {
        val tumDersler = runBlocking { dersRepository.kataloguHazirla() }
        val mat = tumDersler.first { it.ad == "Matematik" }
        val almanca = tumDersler.first { it.ad == "Almanca" }
        runBlocking { dersRepository.dersSeciminiKaydet(setOf(mat.id)) }

        denemeDeposu.kaydet(
            DenemeKaydi(
                id = "d-1",
                ad = "Miray TG 1",
                kural = NetKurali.DORTTE_BIR,
                olusturma = 100L,
                gun = 20_100,
                satirlar = listOf(
                    DenemeDersSatiri(mat.id, 40, 30, 4, "Matematik"),
                    DenemeDersSatiri(almanca.id, 10, 8, 1, "Almanca"),
                ),
            ),
        )

        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                DenemeDetayEkrani(
                    denemeId = "d-1",
                    denemeDeposu = denemeDeposu,
                    dersRepository = dersRepository,
                    soruRepository = soruRepository,
                    onGeri = {},
                    onKaydedildi = {},
                    onSoruEkle = { _, _, _ -> },
                )
            }
        }
        compose.waitForIdle()

        compose.onNodeWithTag("deneme-name-input").performTextReplacement("Miray TG 2")
        compose.onNodeWithTag("deneme-detail-list").performScrollToNode(hasTestTag("deneme-lesson-${almanca.id}"))
        compose.onNodeWithTag("deneme-lesson-${almanca.id}").assertIsDisplayed()
        compose.onNodeWithTag("deneme-detail-list").performScrollToNode(hasTestTag("deneme-save-button"))
        compose.onNodeWithTag("deneme-save-button").performClick()
        // Kaydetme askıya alınabilir bir Room yazımından geçer: waitForIdle tek başına yetmez.
        compose.waitUntil(5_000) { denemeDeposu.getir("d-1")?.ad == "Miray TG 2" }

        val guncel = denemeDeposu.getir("d-1")
        assertNotNull(guncel)
        assertEquals("Miray TG 2", guncel!!.ad)
        assertEquals(2, guncel.satirlar.size)
        val katalogKaynaklari = runBlocking {
            soruRepository.secenekleriIzle(SoruSecenekTuru.KAYNAK).first().map { it.ad }
        }
        assertTrue("Miray TG 2" in katalogKaynaklari)

        compose.onNodeWithTag("deneme-delete-button").performClick()
        compose.waitForIdle()
        assertNotNull(denemeDeposu.getir("d-1"))

        compose.onNodeWithText(compose.activity.getString(R.string.deneme_delete_confirm_action)).performClick()
        compose.waitForIdle()
        assertNull(denemeDeposu.getir("d-1"))
    }

    @Test
    fun klavyeAcikDarAlandaDenemeFormAlanlariPanelAltindaKaybolmazVeManifestAdjustResizeKullanir() {
        val context = RuntimeEnvironment.getApplication()
        val activityInfo = context.packageManager.getActivityInfo(
            ComponentName(context, MainActivity::class.java),
            0,
        )
        assertEquals(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE,
            activityInfo.softInputMode and WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST,
        )

        val tumDersler = runBlocking { dersRepository.kataloguHazirla() }
        val mat = tumDersler.first { it.ad == "Matematik" }
        runBlocking { dersRepository.dersSeciminiKaydet(setOf(mat.id)) }

        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                // Klavye ~360dp açıldığında kalan görünür pencere yüksekliğini simüle eder.
                Box(modifier = Modifier.fillMaxWidth().height(340.dp)) {
                    DenemeDetayEkrani(
                        denemeId = null,
                        denemeDeposu = denemeDeposu,
                        dersRepository = dersRepository,
                        soruRepository = soruRepository,
                        onGeri = {},
                        onKaydedildi = {},
                        onSoruEkle = { _, _, _ -> },
                    )
                }
            }
        }
        compose.waitForIdle()

        val listeYuksekligi = compose.onNodeWithTag("deneme-detail-list")
            .getUnclippedBoundsInRoot().height
        assertTrue("Liste yüksekliği klavye açıkken 0'a çökmemeli: $listeYuksekligi", listeYuksekligi >= 220.dp)

        compose.onNodeWithTag("deneme-name-input").assertIsDisplayed().performTextReplacement("Özdebir TG 4")
        compose.onNodeWithTag("deneme-detail-list")
            .performScrollToNode(hasTestTag("deneme-correct-${mat.id}"))
        compose.onNodeWithTag("deneme-correct-${mat.id}").assertIsDisplayed().performTextReplacement("32")
    }
}

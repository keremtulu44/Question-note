package com.keremtulu.questionnote.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.room.Room
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
import com.keremtulu.questionnote.ui.deneme.DenemeDetayEkrani
import com.keremtulu.questionnote.ui.deneme.DenemeListesiEkrani
import com.keremtulu.questionnote.ui.kurulum.DersKurulumEkrani
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema
import kotlinx.coroutines.flow.first
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
        compose.onNodeWithTag("onboarding-continue-button").performClick()
        compose.waitForIdle()

        assertTrue(tamamlandi)
        assertTrue(profilDeposu.kurulumTamam())
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
        compose.onNodeWithTag("deneme-list").performScrollToNode(hasTestTag("deneme-card-d2"))
        compose.onNodeWithTag("deneme-card-d2").assertIsDisplayed()
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
        compose.waitForIdle()

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
}

package com.keremtulu.questionnote.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.room.Room
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.ilerleme.IlerlemeHizmeti
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.prefs.OgretmenDeposu
import com.keremtulu.questionnote.data.prefs.SinavProfiliDeposu
import com.keremtulu.questionnote.data.prefs.TercihGunlukHedefDeposu
import com.keremtulu.questionnote.data.repository.RoomDersRepository
import com.keremtulu.questionnote.data.repository.RoomSoruRepository
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.KullaniciRolu
import com.keremtulu.questionnote.domain.model.OgretmenZorluk
import com.keremtulu.questionnote.domain.model.SinavProfili
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import com.keremtulu.questionnote.domain.model.SoruKaydetmeIstegi
import com.keremtulu.questionnote.domain.model.SoruPlanDurumu
import com.keremtulu.questionnote.ui.bilesenler.GorselKirpmaDialog
import com.keremtulu.questionnote.ui.bilesenler.GorselSecimBolumu
import com.keremtulu.questionnote.ui.kurulum.RolSecimEkrani
import com.keremtulu.questionnote.ui.ogretmen.OgretmenAnaEkrani
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema
import java.io.File
import kotlinx.coroutines.flow.first
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
class KirpmaSorulacakVeOgretmenArayuzTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private lateinit var context: Context
    private lateinit var database: YanlisDefteriDatabase
    private lateinit var gorselDeposu: LocalSoruGorselDeposu
    private lateinit var ogretmenDeposu: OgretmenDeposu
    private lateinit var profilDeposu: SinavProfiliDeposu
    private lateinit var dersRepository: RoomDersRepository
    private lateinit var soruRepository: RoomSoruRepository

    @Before
    fun kur() {
        context = RuntimeEnvironment.getApplication()
        listOf("sinav_profili", "denemeler", "ilerleme_ayarlari").forEach {
            context.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().commit()
        }
        File(context.filesDir, "ogretmen_calisma_alani.json").delete()
        File(context.filesDir, "ogretmen-img").deleteRecursively()
        database = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle())
            .allowMainThreadQueries()
            .build()
        gorselDeposu = LocalSoruGorselDeposu(context)
        ogretmenDeposu = OgretmenDeposu(context, gorselDeposu, database)
        profilDeposu = SinavProfiliDeposu(context)
        dersRepository = RoomDersRepository(database, profilDeposu)
        soruRepository = RoomSoruRepository(database, gorselDeposu, sinav = profilDeposu)
    }

    @After
    fun kapat() {
        database.close()
    }

    private fun testGorseli(): File {
        val file = File(context.cacheDir, "ui-img-${System.nanoTime()}.png")
        val bmp = Bitmap.createBitmap(220, 160, Bitmap.Config.ARGB_8888)
        Canvas(bmp).apply { drawColor(Color.rgb(232, 240, 250)) }
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bmp.recycle()
        return file
    }

    @Test
    fun merkezdekiKirpButonuAcilirVeGorselDegistigindeCokmeOlmaz() {
        val kaynak = testGorseli()
        try {
            val ilk = runBlocking { gorselDeposu.dosyadanHazirla(kaynak, GorselTuru.SORU, 0) }
            val ikinci = runBlocking { gorselDeposu.kirp(ilk, 0.15f, 0.15f, 0.85f, 0.85f) }
            var liste by mutableStateOf(listOf(ilk))
            var kirpmaAcik by mutableStateOf<SoruGorselGirdisi?>(null)

            compose.setContent {
                YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                    GorselSecimBolumu(
                        images = liste,
                        imageStore = gorselDeposu,
                        enabled = true,
                        onCamera = {},
                        onPicker = {},
                        onCrop = { kirpmaAcik = it },
                        onImagesChanged = { liste = it },
                    )
                    kirpmaAcik?.let { hedef ->
                        val f = gorselDeposu.dosyaGoruntuIcin(hedef)
                        if (f != null) {
                            GorselKirpmaDialog(
                                file = f,
                                isSaving = false,
                                onDismiss = { kirpmaAcik = null },
                                onApply = {
                                    liste = listOf(ikinci)
                                    kirpmaAcik = null
                                },
                            )
                        }
                    }
                }
            }

            compose.onNodeWithTag("gorsel_kirp_merkez_0").assertIsDisplayed().performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithText(context.getString(R.string.question_form_crop_apply))
                    .fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText(context.getString(R.string.question_form_crop_apply)).performClick()
            compose.waitForIdle()
            assertEquals(ikinci.id, liste.single().id)
        } finally {
            kaynak.delete()
        }
    }

    @Test
    fun bugunSorulacaklarHatirlatmasiVeDersDersCozdurdumAkisiCalisir() {
        val kaynak1 = testGorseli()
        val kaynak2 = testGorseli()
        try {
            val mat = runBlocking { database.dersDao().tumunuGetir().first { it.ad == "Matematik" } }
            val fiz = runBlocking { database.dersDao().tumunuGetir().first { it.ad == "Fizik" } }
            profilDeposu.kaydet(SinavProfili.OZEL, setOf(mat.id, fiz.id))
            runBlocking {
                val g1 = gorselDeposu.dosyadanHazirla(kaynak1, GorselTuru.SORU, 0)
                val g2 = gorselDeposu.dosyadanHazirla(kaynak2, GorselTuru.SORU, 0)
                soruRepository.soruKaydet(
                    SoruKaydetmeIstegi(
                        dersId = mat.id,
                        planDurumu = SoruPlanDurumu.OGRETMENE_SORULACAK,
                        gorseller = listOf(g1),
                    ),
                )
                soruRepository.soruKaydet(
                    SoruKaydetmeIstegi(
                        dersId = fiz.id,
                        planDurumu = SoruPlanDurumu.OGRETMENE_SORULACAK,
                        gorseller = listOf(g2),
                    ),
                )
            }

            val ilerlemeHizmeti = IlerlemeHizmeti(database)
            val gunlukHedefDeposu = TercihGunlukHedefDeposu(context)
            var sorulacakEkraniAcik by mutableStateOf(false)
            compose.setContent {
                YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                    if (!sorulacakEkraniAcik) {
                        BugunEkrani(
                            soruRepository = soruRepository,
                            dersRepository = dersRepository,
                            ilerlemeHizmeti = ilerlemeHizmeti,
                            gunlukHedefDeposu = gunlukHedefDeposu,
                            onSoruEkle = {},
                            onOturumBaslat = { _, _ -> },
                            onSorulacaklar = { sorulacakEkraniAcik = true },
                        )
                    } else {
                        SorulacaklarEkrani(
                            soruRepository = soruRepository,
                            dersRepository = dersRepository,
                            gorselDeposu = gorselDeposu,
                            onGeri = { sorulacakEkraniAcik = false },
                            onSoruDetayAc = { _, _ -> },
                        )
                    }
                }
            }

            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("bugun_sorulacaklar_karti").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText(context.getString(R.string.today_ask_teacher_title)).assertIsDisplayed()
            compose.onNodeWithTag("bugun_sorulacaklar_karti").performClick()

            compose.waitUntil(5_000) {
                compose.onAllNodesWithText("Fizik (1)").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("Fizik (1)").assertIsDisplayed()

            val matSoruId = runBlocking {
                soruRepository.ogretmeneSorulacaklariIzle().first().first { it.dersId == mat.id }.id
            }
            compose.onNodeWithTag("sorulacaklar_liste")
                .performScrollToNode(hasTestTag("sorulacak_cozdurdum_$matSoruId"))
            compose.onNodeWithText("Matematik (1)").assertIsDisplayed()
            compose.onNodeWithTag("sorulacak_cozdurdum_$matSoruId").performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("cozdurdum_fotografsiz_onay").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("cozdurdum_fotografsiz_onay").performClick()

            compose.waitUntil(5_000) {
                runBlocking { soruRepository.ogretmeneSorulacaklariIzle().first().size == 1 }
            }
        } finally {
            kaynak1.delete()
            kaynak2.delete()
        }
    }

    @Test
    fun rolSecimiVeOgretmenCalismaAlanindaGizliCevapVeGrupKopyalamaCalisir() {
        val kaynak = testGorseli()
        try {
            val g6B = runBlocking { requireNotNull(ogretmenDeposu.grupEkle("6B")) }
            val g7C = runBlocking { requireNotNull(ogretmenDeposu.grupEkle("7C")) }
            val hazir = runBlocking { gorselDeposu.dosyadanHazirla(kaynak, GorselTuru.SORU, 0) }
            val soru = runBlocking {
                requireNotNull(
                    ogretmenDeposu.soruKaydet(
                        grupId = g6B.id,
                        baslik = "Oran Orantı",
                        zorluk = OgretmenZorluk.ORTA,
                        cevap = "48",
                        gorseller = listOf(hazir),
                    ),
                )
            }

            var secilenRol by mutableStateOf<KullaniciRolu?>(null)
            compose.setContent {
                YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                    if (secilenRol == null) {
                        RolSecimEkrani(onRolSecildi = { secilenRol = it })
                    } else {
                        OgretmenAnaEkrani(
                            ogretmenDeposu = ogretmenDeposu,
                            gorselDeposu = gorselDeposu,
                            sonYedek = null,
                            yedeklemeIslemde = false,
                            onOgrenciModunaGec = { secilenRol = null },
                            onYedekOlustur = {},
                            onYedekGeriYukle = {},
                        )
                    }
                }
            }

            compose.onNodeWithTag("rol_secim_ogretmen").assertIsDisplayed().performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("ogretmen_grup_${g6B.id}").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("ogretmen_grup_${g6B.id}").performClick()

            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("ogretmen_soru_kart_${soru.id}").fetchSemanticsNodes().isNotEmpty()
            }
            // Cevap liste kartında açıkça görünmemeli; tam ekranda "Cevabı göster" ile açılmalı.
            assertTrue(compose.onAllNodesWithText("Cevap: 48").fetchSemanticsNodes().isEmpty())
            compose.onNodeWithTag("ogretmen_soru_kart_${soru.id}").performClick()

            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("ogretmen_cevabi_goster").fetchSemanticsNodes().isNotEmpty()
            }
            assertTrue(compose.onAllNodesWithTag("ogretmen_cevap_kutusu").fetchSemanticsNodes().isEmpty())
            compose.onNodeWithTag("ogretmen_cevabi_goster").performClick()
            compose.onNodeWithTag("ogretmen_cevap_kutusu").assertIsDisplayed()
            compose.onNodeWithText(context.getString(R.string.common_close)).performClick()

            // Başka gruba kopyala -> 7C'de Çözmediklerim olarak başlamalı; 6B'de Çözdüm işaretlenebilmeli.
            compose.onNodeWithTag("ogretmen_cozuldu_degistir_${soru.id}").performClick()
            compose.waitUntil(5_000) { ogretmenDeposu.anlikVeri().sorular.single().cozuldu }

            compose.onNodeWithTag("sekme_cozduklerim").performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("ogretmen_baska_gruba_ekle_${soru.id}").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("ogretmen_baska_gruba_ekle_${soru.id}").performClick()
            // A dialog opens in its own window; give it a frame before querying its rows.
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("hedef_grup_${g7C.id}").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("hedef_grup_${g7C.id}").performClick()
            compose.onNodeWithTag("hedef_gruplara_kopyala_onay").performClick()

            compose.waitUntil(5_000) { ogretmenDeposu.anlikVeri().sorular.size == 2 }
            val kopya7C = ogretmenDeposu.anlikVeri().sorular.single { it.grupId == g7C.id }
            assertFalse(kopya7C.cozuldu)
        } finally {
            kaynak.delete()
        }
    }

    @Test
    fun ogretmenPremiumAnaEkranHazirGrupOnerisiVeAltMenuSekmeGecisiCalisir() {
        val context = RuntimeEnvironment.getApplication()
        runBlocking {
            ogretmenDeposu.grupEkle("6B", "Salı 15:00")
            ogretmenDeposu.grupEkle("7C", "Çarşamba 16:00")
            ogretmenDeposu.grupEkle("Özel Ders (Ahmet)", "Cumartesi")
        }

        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                OgretmenAnaEkrani(
                    ogretmenDeposu = ogretmenDeposu,
                    gorselDeposu = gorselDeposu,
                    sonYedek = null,
                    yedeklemeIslemde = false,
                    onOgrenciModunaGec = {},
                    onYedekOlustur = {},
                    onYedekGeriYukle = {},
                )
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText(context.getString(R.string.teacher_stat_groups)).assertIsDisplayed()
        compose.onNodeWithTag("ogretmen_grup_ekle_fab").performClick()
        compose.onNodeWithText("8A").performClick()
        compose.onNodeWithTag("ogretmen_grup_kaydet_buton").performClick()
        compose.waitUntil(5_000) { ogretmenDeposu.anlikVeri().gruplar.any { it.ad == "8A" } }

        // Özel alt menü üzerinden Ayarlar sekmesine ve tekrar Gruplarım sekmesine geçiş
        compose.onNodeWithText(context.getString(R.string.teacher_nav_settings)).performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("ogrenci_moduna_gec_buton").assertIsDisplayed()

        compose.onNodeWithText(context.getString(R.string.teacher_nav_groups)).performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("ogretmen_grup_ekle_fab").assertIsDisplayed()
    }
}

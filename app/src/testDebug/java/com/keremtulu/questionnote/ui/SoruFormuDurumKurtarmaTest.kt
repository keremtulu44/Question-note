package com.keremtulu.questionnote.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.room.Room
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.prefs.SinavProfiliDeposu
import com.keremtulu.questionnote.data.repository.RoomDersRepository
import com.keremtulu.questionnote.data.repository.RoomKonuRepository
import com.keremtulu.questionnote.data.repository.RoomSoruRepository
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import com.keremtulu.questionnote.domain.model.SoruIslemSonucu
import com.keremtulu.questionnote.domain.model.SoruKaydetmeIstegi
import com.keremtulu.questionnote.domain.model.SoruPlanDurumu
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.PremiumYanlisDefteriTema
import java.io.File
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Süreç ölümü regresyonları: kamera/galeri açıkken Android uygulamayı bellekten
 * atarsa soru formundaki alanlar, hazırlanmış görseller ve gezinme durumu
 * kaybolmamalı; düzenleme formunda kurtarılan değerler veritabanındaki eski
 * kayıtla ezilmemeli.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-mdpi")
class SoruFormuDurumKurtarmaTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private lateinit var context: Context
    private lateinit var database: YanlisDefteriDatabase
    private lateinit var gorselDeposu: LocalSoruGorselDeposu
    private lateinit var profilDeposu: SinavProfiliDeposu
    private lateinit var dersRepository: RoomDersRepository
    private lateinit var konuRepository: RoomKonuRepository
    private lateinit var soruRepository: RoomSoruRepository

    @Before
    fun kur() {
        context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("sinav_profili", Context.MODE_PRIVATE).edit().clear().commit()
        database = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle())
            .allowMainThreadQueries()
            .build()
        gorselDeposu = LocalSoruGorselDeposu(context)
        profilDeposu = SinavProfiliDeposu(context)
        dersRepository = RoomDersRepository(database, profilDeposu)
        konuRepository = RoomKonuRepository(database)
        soruRepository = RoomSoruRepository(database, gorselDeposu, sinav = profilDeposu)
    }

    @After
    fun kapat() {
        database.close()
    }

    private object TestSaverScope : SaverScope {
        override fun canBeSaved(value: Any): Boolean = true
    }

    @Test
    fun gorselGirdileriSaverBosVeDoluListeyiKorur() {
        val girdiler = listOf(
            SoruGorselGirdisi(
                id = "girdi-1", tur = GorselTuru.SORU, sira = 0, dosyaAdi = "girdi-1.jpg",
                gecici = true, mimeType = "image/jpeg", genislik = 1200, yukseklik = 900,
                boyut = 123_456L, sha256 = "a".repeat(64),
            ),
            SoruGorselGirdisi(
                id = "girdi-2", tur = GorselTuru.COZUM, sira = 1, dosyaAdi = "girdi-2.jpg",
                gecici = false, mimeType = "image/jpeg", genislik = 800, yukseklik = 600,
                boyut = 65_535L, sha256 = "b".repeat(64),
            ),
        )

        val kaydedilen = with(SoruGorselGirdileriSaver) { TestSaverScope.save(girdiler) }
        assertEquals(girdiler, SoruGorselGirdileriSaver.restore(kaydedilen!!))

        val bosKaydedilen = with(SoruGorselGirdileriSaver) {
            TestSaverScope.save(emptyList())
        }
        assertEquals(
            emptyList<SoruGorselGirdisi>(),
            SoruGorselGirdileriSaver.restore(bosKaydedilen!!),
        )
    }

    @Test
    fun enumSaverlariDegerleriKorurVeBozukAddaVarsayilanaDoner() {
        val planKaydedilen = with(SoruPlanDurumuSaver) {
            TestSaverScope.save(SoruPlanDurumu.OGRETMENE_SORULACAK)
        }
        assertEquals(
            SoruPlanDurumu.OGRETMENE_SORULACAK,
            SoruPlanDurumuSaver.restore(planKaydedilen!!),
        )
        assertEquals(SoruPlanDurumu.AKTIF, SoruPlanDurumuSaver.restore("bilinmeyen"))

        val turKaydedilen = with(GorselTuruSaver) { TestSaverScope.save(GorselTuru.COZUM) }
        assertEquals(GorselTuru.COZUM, GorselTuruSaver.restore(turKaydedilen!!))
        assertEquals(GorselTuru.SORU, GorselTuruSaver.restore("bilinmeyen"))
    }

    @Test
    fun uygulamaEkraniSaverIcIceGeriZinciriniKorur() {
        val ders = Ders(id = "ders-1", ad = "Matematik", sira = 1, soruSayisi = 7)
        val ekran = UygulamaEkrani.SoruFormu(
            ders = ders,
            soruId = "soru-9",
            geri = UygulamaEkrani.SoruAyrintisi(
                ders = ders,
                soruId = "soru-9",
                geri = UygulamaEkrani.DersAlani(ders = ders, geri = UygulamaEkrani.Bugun),
                tekrarOturumunda = true,
            ),
            gizliKaynak = "TYT Deneme 4",
        )

        val kaydedilen = with(UygulamaEkraniSaver) { TestSaverScope.save(ekran) }
        assertEquals(ekran, UygulamaEkraniSaver.restore(kaydedilen!!))

        val detayKaydedilen = with(UygulamaEkraniSaver) {
            TestSaverScope.save(UygulamaEkrani.DenemeDetay(denemeId = null))
        }
        assertEquals(
            UygulamaEkrani.DenemeDetay(denemeId = null),
            UygulamaEkraniSaver.restore(detayKaydedilen!!),
        )
    }

    @Test
    fun uygulamaEkraniSaverBozukKayittaVeOturumEkranindaBuguneDoner() {
        assertEquals(UygulamaEkrani.Bugun, UygulamaEkraniSaver.restore("bozuk {"))
        assertEquals(UygulamaEkrani.Bugun, UygulamaEkraniSaver.restore(""))

        val tamamlandi = UygulamaEkrani.FiltreliCalismaTamamlandi(
            sorular = emptyList(),
            dersler = emptyMap(),
            sonuclar = emptyList(),
        )
        val kaydedilen = with(UygulamaEkraniSaver) { TestSaverScope.save(tamamlandi) }
        assertEquals(UygulamaEkrani.Bugun, UygulamaEkraniSaver.restore(kaydedilen!!))
    }

    @Test
    fun yeniSoruFormuAlanlariSurecOlumunuAtlatir() {
        val ders = runBlocking { dersRepository.dersleriIzle(false).first().first() }
        val restoration = StateRestorationTester(compose)

        restoration.setContent {
            PremiumYanlisDefteriTema(
                koyuTema = false,
                hareketPolitikasi = HareketPolitikasi.Azaltilmis,
            ) {
                SoruFormuEkrani(
                    ders = ders,
                    soruId = null,
                    soruRepository = soruRepository,
                    konuRepository = konuRepository,
                    gorselDeposu = gorselDeposu,
                    onGeri = {},
                    gizliKaynak = "TYT Deneme 4",
                )
            }
        }

        compose.waitUntil(5_000) {
            compose.onAllNodes(hasTestTag("soru_formu_liste")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("soru_formu_liste")
            .performScrollToNode(hasTestTag("soru_formu_kaynak_konumu"))
        compose.onNodeWithTag("soru_formu_kaynak_konumu").performTextReplacement("Soru 14")
        compose.waitForIdle()

        restoration.emulateSavedInstanceStateRestore()
        compose.waitForIdle()

        compose.waitUntil(5_000) {
            compose.onAllNodes(hasTestTag("soru_formu_kaynak_konumu") and hasText("Soru 14"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("soru_formu_liste")
            .performScrollToNode(hasTestTag("soru_formu_kaynak_konumu"))
        compose.onNode(hasTestTag("soru_formu_kaynak_konumu") and hasText("Soru 14"))
            .assertExists()
    }

    @Test
    fun duzenlemeFormuKurtarilanDegeriVeritabaniDegeriyleEzmez() {
        val ders = runBlocking { dersRepository.dersleriIzle(false).first().first() }
        val kaynakDosya = File(context.cacheDir, "form-kurtarma-${UUID.randomUUID()}.jpg")
        ornekJpegOlustur(kaynakDosya)
        try {
            val soruId = runBlocking {
                val girdi = gorselDeposu.dosyadanHazirla(kaynakDosya, GorselTuru.SORU, 0)
                val sonuc = soruRepository.soruKaydet(
                    SoruKaydetmeIstegi(
                        dersId = ders.id,
                        kaynakKonumu = "sayfa 3",
                        gorseller = listOf(girdi),
                    ),
                )
                assertEquals(SoruIslemSonucu.Basarili, sonuc)
                database.soruDao().tumunuGetir().single().id
            }

            val restoration = StateRestorationTester(compose)
            restoration.setContent {
                PremiumYanlisDefteriTema(
                    koyuTema = false,
                    hareketPolitikasi = HareketPolitikasi.Azaltilmis,
                ) {
                    SoruFormuEkrani(
                        ders = ders,
                        soruId = soruId,
                        soruRepository = soruRepository,
                        konuRepository = konuRepository,
                        gorselDeposu = gorselDeposu,
                        onGeri = {},
                    )
                }
            }

            // Konum alanı LazyColumn'da ekran altında olabilir; önce kaydır,
            // kaydı Room'dan yüklenen değerle görünmesini sonra bekle.
            compose.waitUntil(5_000) {
                compose.onAllNodes(hasTestTag("soru_formu_liste"))
                    .fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("soru_formu_liste")
                .performScrollToNode(hasTestTag("soru_formu_kaynak_konumu"))
            compose.waitUntil(5_000) {
                compose.onAllNodes(
                    hasTestTag("soru_formu_kaynak_konumu") and hasText("sayfa 3"),
                ).fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("soru_formu_kaynak_konumu").performTextReplacement("sayfa 9")
            compose.waitForIdle()

            restoration.emulateSavedInstanceStateRestore()
            compose.waitForIdle()

            compose.onNodeWithTag("soru_formu_liste")
                .performScrollToNode(hasTestTag("soru_formu_kaynak_konumu"))
            compose.waitUntil(5_000) {
                compose.onAllNodes(
                    hasTestTag("soru_formu_kaynak_konumu") and hasText("sayfa 9"),
                ).fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNode(hasTestTag("soru_formu_kaynak_konumu") and hasText("sayfa 9"))
                .assertExists()
        } finally {
            kaynakDosya.delete()
        }
    }

    private fun ornekJpegOlustur(dosya: File) {
        dosya.parentFile?.mkdirs()
        val bmp = Bitmap.createBitmap(320, 240, Bitmap.Config.ARGB_8888)
        Canvas(bmp).apply { drawColor(Color.rgb(225, 236, 248)) }
        dosya.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        bmp.recycle()
    }
}

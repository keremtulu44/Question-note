package com.keremtulu.questionnote.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.Density
import androidx.room.Room
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.export.YerelYedeklemeHizmeti
import com.keremtulu.questionnote.data.images.GorselTutarliligiHizmeti
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.prefs.DenemeDeposu
import com.keremtulu.questionnote.data.prefs.OdakOturumuDeposu
import com.keremtulu.questionnote.data.prefs.OgretmenDeposu
import com.keremtulu.questionnote.data.prefs.SinavProfiliDeposu
import com.keremtulu.questionnote.data.repository.RoomDersRepository
import com.keremtulu.questionnote.data.repository.RoomKonuRepository
import com.keremtulu.questionnote.data.repository.RoomSoruRepository
import com.keremtulu.questionnote.domain.calisma.OdakOturumu
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.OgretmenZorluk
import com.keremtulu.questionnote.ui.bilesenler.SoruGorselTamEkran
import com.keremtulu.questionnote.ui.calisma.OdakOturumuEkrani
import com.keremtulu.questionnote.ui.ogretmen.OgretmenAnaEkrani
import com.keremtulu.questionnote.ui.premium.PremiumYanlisDefteriTema
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-mdpi")
class GorselButunlukVeYerlesimArayuzTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private lateinit var context: Context
    private lateinit var database: YanlisDefteriDatabase
    private lateinit var gorselDeposu: LocalSoruGorselDeposu
    private lateinit var ogretmenDeposu: OgretmenDeposu
    private lateinit var denemeDeposu: DenemeDeposu
    private lateinit var profilDeposu: SinavProfiliDeposu
    private lateinit var dersRepository: RoomDersRepository
    private lateinit var konuRepository: RoomKonuRepository
    private lateinit var soruRepository: RoomSoruRepository

    @Before
    fun kur() {
        context = RuntimeEnvironment.getApplication()
        listOf("sinav_profili", "denemeler", "ilerleme_ayarlari", "tekrar_politikasi").forEach {
            context.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().commit()
        }
        File(context.filesDir, "ogretmen_calisma_alani.json").delete()
        File(context.filesDir, "ogretmen-img").deleteRecursively()
        File(context.filesDir, "img").deleteRecursively()
        database = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle())
            .allowMainThreadQueries()
            .build()
        gorselDeposu = LocalSoruGorselDeposu(context)
        ogretmenDeposu = OgretmenDeposu(context, gorselDeposu)
        denemeDeposu = DenemeDeposu(context)
        profilDeposu = SinavProfiliDeposu(context)
        dersRepository = RoomDersRepository(database, profilDeposu)
        konuRepository = RoomKonuRepository(database)
        soruRepository = RoomSoruRepository(database, gorselDeposu, sinav = profilDeposu)
    }

    @After
    fun kapat() {
        database.close()
    }

    private fun ornekJpegOlustur(dosya: File) {
        dosya.parentFile?.mkdirs()
        val bmp = Bitmap.createBitmap(320, 240, Bitmap.Config.ARGB_8888)
        Canvas(bmp).apply { drawColor(Color.rgb(225, 236, 248)) }
        dosya.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        bmp.recycle()
    }

    @Test
    fun tamEkranSoruGoruntuleyiciSifirPikselYuksekligeCokmez() {
        val gorselDosya = File(context.cacheDir, "tam-ekran-${UUID.randomUUID()}.jpg")
        ornekJpegOlustur(gorselDosya)
        try {
            compose.setContent {
                PremiumYanlisDefteriTema(
                    koyuTema = false,
                    hareketPolitikasi = HareketPolitikasi.Azaltilmis,
                ) {
                    SoruGorselTamEkran(
                        file = gorselDosya,
                        contentDescription = context.getString(R.string.question_image_description),
                        onDismiss = {},
                    )
                }
            }

            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("tam_ekran_soru_gorseli", useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            val sinirlar = compose
                .onNodeWithTag("tam_ekran_soru_gorseli", useUnmergedTree = true)
                .fetchSemanticsNode()
                .boundsInRoot
            assertTrue("Tam ekran soru görseli genişliği > 200px olmalı: $sinirlar", sinirlar.width > 200f)
            assertTrue("Tam ekran soru görseli yüksekliği > 200px olmalı (0px çökmesi yok): $sinirlar", sinirlar.height > 200f)
        } finally {
            gorselDosya.delete()
        }
    }

    @Test
    fun odakOturumuCozumsuzVeSonaBirakMetinleriUstUsteBinmez() {
        val oturumDeposu = OdakOturumuDeposu(context)
        oturumDeposu.temizle()
        oturumDeposu.kaydet(OdakOturumu("geometri-test", listOf("q1", "q2"), 1_000L))

        val (ders, soru) = runBlocking {
            val seciliDers = dersRepository.dersleriIzle(false).first().first()
            database.soruDao().ekle(
                SoruEntity(
                    id = "q1",
                    dersId = seciliDers.id,
                    soruNotu = "Çözümsüz odak sorusu",
                    cozumNotu = null,
                    olusturma = 1L,
                    guncelleme = 1L,
                ),
            )
            seciliDers to requireNotNull(soruRepository.soruGetir("q1"))
        }

        compose.setContent {
            PremiumYanlisDefteriTema(
                koyuTema = true,
                hareketPolitikasi = HareketPolitikasi.Azaltilmis,
            ) {
                OdakOturumuEkrani(
                    soru = soru,
                    ders = ders,
                    soruRepository = soruRepository,
                    gorselDeposu = gorselDeposu,
                    oturumId = "geometri-test",
                    oturumDeposu = oturumDeposu,
                    sira = 1,
                    toplam = 2,
                    sonuclar = emptyList(),
                    cozumOnceAcildi = false,
                    onCozumAcildi = {},
                    onGeri = {},
                    onSonuc = { _, _ -> },
                    onSonaBirak = {},
                    onAtla = {},
                )
            }
        }

        val cozumGosterMetni = context.getString(R.string.focus_reveal)
        val cozumYokMetni = context.getString(R.string.focus_no_solution)
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(cozumYokMetni))
        compose.onNodeWithText(cozumGosterMetni).assertIsDisplayed()
        compose.onNodeWithText(cozumYokMetni).assertIsDisplayed()

        val cozumButonSinir = compose.onNodeWithText(cozumGosterMetni).fetchSemanticsNode().boundsInRoot
        val cozumYokSinir = compose.onNodeWithText(cozumYokMetni).fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Çözüm kaydı yok metni butonun üstüne binmemeli: buton=$cozumButonSinir, metin=$cozumYokSinir",
            cozumYokSinir.top >= cozumButonSinir.bottom - 1f,
        )

        val sonaBirakMetni = context.getString(R.string.focus_defer)
        val sonaBirakNotMetni = context.getString(R.string.focus_defer_note)
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(sonaBirakNotMetni))
        compose.onNodeWithText(sonaBirakNotMetni).assertIsDisplayed()
        val sonaBirakButonSinir = compose.onNodeWithText(sonaBirakMetni).fetchSemanticsNode().boundsInRoot
        val sonaBirakNotSinir = compose.onNodeWithText(sonaBirakNotMetni).fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Sona bırak açıklaması butonun üstüne binmemeli: buton=$sonaBirakButonSinir, not=$sonaBirakNotSinir",
            sonaBirakNotSinir.top >= sonaBirakButonSinir.bottom - 1f,
        )
    }

    @Test
    fun gorselBakimYetimDosyaBaslikVeUyariUstUsteBinmez() {
        val yetimDosya = File(File(context.filesDir, "img"), "${UUID.randomUUID()}.jpg")
        ornekJpegOlustur(yetimDosya)
        yetimDosya.setLastModified(1_000L)
        val hizmet = GorselTutarliligiHizmeti(database, gorselDeposu)

        try {
            compose.setContent {
                PremiumYanlisDefteriTema(
                    koyuTema = false,
                    hareketPolitikasi = HareketPolitikasi.Azaltilmis,
                ) {
                    GorselBakimEkrani(
                        service = hizmet,
                        onGeri = {},
                    )
                }
            }

            compose.onNodeWithText(context.getString(R.string.image_audit_action)).performClick()
            val baslikMetni = context.getString(R.string.image_orphan_files)
            val uyariMetni = context.getString(R.string.image_orphan_warning)
            compose.waitUntil(5_000) {
                compose.onAllNodesWithText(baslikMetni).fetchSemanticsNodes().isNotEmpty()
            }

            val baslikSinir = compose.onNodeWithText(baslikMetni).fetchSemanticsNode().boundsInRoot
            val uyariSinir = compose.onNodeWithText(uyariMetni).fetchSemanticsNode().boundsInRoot
            assertTrue(
                "Yetim dosya uyarısı başlıkla üst üste binmemeli: baslik=$baslikSinir, uyari=$uyariSinir",
                uyariSinir.top >= baslikSinir.bottom - 1f,
            )
        } finally {
            yetimDosya.delete()
        }
    }

    @Test
    fun denemeKaynagiIleSoruFormundaKonumAlaniGorunurVeDuzenlenebilir() {
        val ders = runBlocking { dersRepository.dersleriIzle(false).first().first() }

        compose.setContent {
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
            compose.onAllNodesWithTag("soru_formu_liste").fetchSemanticsNodes().isNotEmpty()
        }

        compose.onNodeWithTag("soru_formu_liste")
            .performScrollToNode(hasTestTag("soru_formu_kaynak_konumu"))
        compose.onNodeWithTag("soru_formu_deneme_kaynak_rozeti").assertIsDisplayed()
        compose.onNodeWithTag("soru_formu_kaynak_konumu").assertIsDisplayed()
            .performTextReplacement("Soru 14")

        val rozetSinir = compose.onNodeWithTag("soru_formu_deneme_kaynak_rozeti").fetchSemanticsNode().boundsInRoot
        val konumSinir = compose.onNodeWithTag("soru_formu_kaynak_konumu").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Konum alanı Deneme rozetinin altında yer almalı: rozet=$rozetSinir, konum=$konumSinir",
            konumSinir.top >= rozetSinir.bottom - 1f,
        )
    }

    @Test
    fun ogretmenModuPaylasilanSnackbarVeYedekOnizlemeSayilariniGosterir() {
        val kaynakGorsel = File(context.cacheDir, "ogretmen-src-${UUID.randomUUID()}.jpg")
        ornekJpegOlustur(kaynakGorsel)
        try {
            runBlocking {
                val grup = requireNotNull(ogretmenDeposu.grupEkle("8D", "Fen sınıfı"))
                val gorsel = gorselDeposu.ogretmenDosyadanHazirla(kaynakGorsel, 0)
                requireNotNull(
                    ogretmenDeposu.soruKaydet(
                        grupId = grup.id,
                        baslik = "Basınç sorusu",
                        zorluk = OgretmenZorluk.ORTA,
                        cevap = "D",
                        gorseller = listOf(gorsel),
                    ),
                )
            }

            val yedekHizmeti = YerelYedeklemeHizmeti(
                context = context,
                database = database,
                gorselDeposu = gorselDeposu,
                denemeDeposu = denemeDeposu,
                ogretmenDeposu = ogretmenDeposu,
            )
            val arsiv = runBlocking { yedekHizmeti.zipOlustur() }
            val onizleme = runBlocking { yedekHizmeti.dosyadanOnizle(arsiv) }
            try {
                assertEquals(1, onizleme.yedekOgretmenGrupSayisi)
                assertEquals(1, onizleme.yedekOgretmenSoruSayisi)
                assertEquals(1, onizleme.mevcutOgretmenGrupSayisi)
                assertEquals(1, onizleme.mevcutOgretmenSoruSayisi)
            } finally {
                runBlocking { yedekHizmeti.hazirlananYedegiIptalEt(onizleme.id) }
                arsiv.delete()
            }

            val paylasilanSnackbar = SnackbarHostState()
            lateinit var uiScope: CoroutineScope
            compose.setContent {
                uiScope = rememberCoroutineScope()
                PremiumYanlisDefteriTema(
                    koyuTema = true,
                    hareketPolitikasi = HareketPolitikasi.Azaltilmis,
                ) {
                    OgretmenAnaEkrani(
                        ogretmenDeposu = ogretmenDeposu,
                        gorselDeposu = gorselDeposu,
                        sonYedek = null,
                        yedeklemeIslemde = false,
                        snackbarHostState = paylasilanSnackbar,
                        onOgrenciModunaGec = {},
                        onYedekOlustur = {},
                        onYedekGeriYukle = {},
                    )
                }
            }

            compose.runOnIdle {
                uiScope.launch { paylasilanSnackbar.showSnackbar("Yedek hazır: ogretmen.zip") }
            }
            compose.waitUntil(5_000) {
                compose.onAllNodesWithText("Yedek hazır: ogretmen.zip").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("Yedek hazır: ogretmen.zip").assertIsDisplayed()
        } finally {
            kaynakGorsel.delete()
        }
    }

    @Test
    fun buyukYaziOlcegindeDerslerEkraniTasmazVeManropeTablosuTutarlidir() {
        runBlocking { dersRepository.kataloguHazirla() }

        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1.6f)) {
                PremiumYanlisDefteriTema(
                    koyuTema = false,
                    hareketPolitikasi = HareketPolitikasi.Azaltilmis,
                ) {
                    DerslerEkrani(
                        repository = dersRepository,
                        soruEkleModu = false,
                        profilAdi = "YKS",
                        onDersSecildi = {},
                    )
                }
            }
        }

        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("Türkçe").fetchSemanticsNodes().isNotEmpty()
        }
        val turkceSinir = compose.onNodeWithText("Türkçe").fetchSemanticsNode().boundsInRoot
        assertTrue("1.6x yazı ölçeğinde ders kartı görünür olmalı: $turkceSinir", turkceSinir.height > 18f)

        val ttfBaytlari = runCatching {
            context.resources.openRawResource(R.font.manrope_regular).use { it.readBytes() }
        }.getOrElse {
            listOf(
                File("src/main/res/font/manrope_regular.ttf"),
                File("app/src/main/res/font/manrope_regular.ttf"),
            ).first { it.isFile }.readBytes()
        }
        val manropeKodNoktalari = manropeCmapOku(ttfBaytlari)
        assertTrue("Manrope Türkçe karakterleri desteklemeli", 'ş'.code in manropeKodNoktalari && 'İ'.code in manropeKodNoktalari)
        assertTrue("Manrope ok ve eşkenar sembollerini desteklemeli", '→'.code in manropeKodNoktalari && '◊'.code in manropeKodNoktalari)
    }

    private fun manropeCmapOku(veri: ByteArray): Set<Int> {
        val buf = ByteBuffer.wrap(veri).order(ByteOrder.BIG_ENDIAN)
        val tabloSayisi = buf.getShort(4).toInt() and 0xFFFF
        var cmapBaslangic = -1
        for (i in 0 until tabloSayisi) {
            val ofset = 12 + i * 16
            val etiket = String(veri, ofset, 4, Charsets.US_ASCII)
            if (etiket == "cmap") {
                cmapBaslangic = buf.getInt(ofset + 8)
                break
            }
        }
        check(cmapBaslangic >= 0) { "TTF içinde cmap tablosu bulunamadı." }
        val altTabloSayisi = buf.getShort(cmapBaslangic + 2).toInt() and 0xFFFF
        val kodlar = HashSet<Int>()
        for (i in 0 until altTabloSayisi) {
            val altOfset = cmapBaslangic + buf.getInt(cmapBaslangic + 8 + i * 8)
            when (buf.getShort(altOfset).toInt() and 0xFFFF) {
                4 -> {
                    val segSayisi = (buf.getShort(altOfset + 6).toInt() and 0xFFFF) / 2
                    for (j in 0 until segSayisi) {
                        val bitis = buf.getShort(altOfset + 14 + j * 2).toInt() and 0xFFFF
                        val baslangic = buf.getShort(altOfset + 16 + segSayisi * 2 + j * 2).toInt() and 0xFFFF
                        if (baslangic != 0xFFFF) {
                            for (cp in baslangic..bitis) kodlar.add(cp)
                        }
                    }
                }
                12 -> {
                    val grupSayisi = buf.getInt(altOfset + 12)
                    for (j in 0 until grupSayisi) {
                        val baslangic = buf.getInt(altOfset + 16 + j * 12)
                        val bitis = buf.getInt(altOfset + 20 + j * 12)
                        for (cp in baslangic..bitis) kodlar.add(cp)
                    }
                }
            }
        }
        return kodlar
    }
}

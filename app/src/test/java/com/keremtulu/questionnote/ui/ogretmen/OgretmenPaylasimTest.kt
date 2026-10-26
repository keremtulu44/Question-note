package com.keremtulu.questionnote.ui.ogretmen

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.room.Room
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.prefs.OgretmenDeposu
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.OgretmenSorusu
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.xmlpull.v1.XmlPullParser

/**
 * T3 paylaşımı: yerel `ACTION_SEND`, fotoğraflar içerik URI'siyle (cihazda FileProvider), "kim
 * sordu" asla dışarıda değil. Ağ yok, hesap yok — bu yüzden Play veri beyanı değişmez.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OgretmenPaylasimTest {
    private lateinit var context: Context
    private lateinit var database: YanlisDefteriDatabase
    private lateinit var gorselDeposu: LocalSoruGorselDeposu
    private lateinit var ogretmenDeposu: OgretmenDeposu

    @Before
    fun kur() {
        context = RuntimeEnvironment.getApplication()
        File(context.filesDir, "ogretmen_calisma_alani.json").delete()
        File(context.filesDir, "ogretmen-img").deleteRecursively()
        File(context.filesDir, "img").deleteRecursively()
        database = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle())
            .allowMainThreadQueries()
            .build()
        gorselDeposu = LocalSoruGorselDeposu(context)
        ogretmenDeposu = OgretmenDeposu(context, gorselDeposu, database)
    }

    @After
    fun temizle() {
        database.close()
    }

    private fun testGorseli(ad: String): File {
        val file = File(context.cacheDir, "$ad-${System.nanoTime()}.png")
        val bmp = Bitmap.createBitmap(180, 140, Bitmap.Config.ARGB_8888)
        Canvas(bmp).apply { drawColor(Color.rgb(230, 240, 252)) }
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bmp.recycle()
        return file
    }

    private fun soru(
        grupId: String,
        baslik: String,
        kimSordu: String,
        kaynak: File,
        dersId: String? = null,
    ): OgretmenSorusu = runBlocking {
        requireNotNull(
            ogretmenDeposu.soruKaydet(
                grupId = grupId,
                baslik = baslik,
                zorluk = null,
                cevap = "gizli cevap",
                gorseller = listOf(gorselDeposu.dosyadanHazirla(kaynak, GorselTuru.SORU, 0)),
                dersId = dersId,
                kaynak = "Miray 3",
                kimSordu = kimSordu,
            ),
        )
    }

    private fun seciciIci(intent: Intent): Intent =
        requireNotNull(intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT))

    /**
     * FileProvider önbelleği süreç genelinde yetkiye göre tutulur; Robolectric her test metoduna
     * yeni bir dosya dizini verdiği için sağlayıcı aynı JVM'de ikinci kez çalıştırılamaz. Bu yüzden
     * URI üretimi enjekte edilir; sağlayıcı yapılandırması ayrı testte beyan üzerinden doğrulanır.
     */
    private val sahteUriUretici: (File) -> Uri = { dosya ->
        Uri.parse("content://com.keremtulu.questionnote.fileprovider/${dosya.name}")
    }

    @Test
    fun tekSoruFotografiUriOlarakPaylasilirVeSoranKisiGizlenir() {
        val kaynak = testGorseli("paylasim")
        try {
            val grup = runBlocking { requireNotNull(ogretmenDeposu.grupEkle("8A")) }
            // Var olmayan ders kimliği kaydı düşürmemeli; yalnız o etiket atılır.
            val soru = soru(grup.id, "Oran Orantı", "Elif Y.", kaynak, dersId = "yok")
            assertNull(soru.dersId)

            val dosya = requireNotNull(gorselDeposu.dosyaGoruntuIcin(soru.gorseller.single().dosyaAdi))
            assertTrue("Soru fotoğrafı diskte olmalı: ${dosya.absolutePath}", dosya.isFile)

            val uriler = OgretmenPaylasim.gorselUrisi(
                context,
                listOf(soru),
                gorselDeposu,
                uriUret = sahteUriUretici,
            )
            assertEquals(1, uriler.size)
            assertTrue(
                "Fotoğraf content:// URI olarak verilmeli: ${uriler.first()}",
                uriler.first().toString().startsWith("content://"),
            )

            val paylasim = requireNotNull(
                OgretmenPaylasim.intent(context, listOf(soru), gorselDeposu, uriUret = sahteUriUretici),
            )
            assertEquals(Intent.ACTION_CHOOSER, paylasim.action)
            val gonder = seciciIci(paylasim)
            assertEquals(Intent.ACTION_SEND, gonder.action)
            assertEquals("image/jpeg", gonder.type)
            assertTrue(
                gonder.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0,
            )
            val akis: Uri = requireNotNull(gonder.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
            assertEquals(uriler.first(), akis)

            val metin = requireNotNull(gonder.getStringExtra(Intent.EXTRA_TEXT))
            assertTrue(metin.contains("Oran Orantı"))
            assertTrue(metin.contains("Miray 3"))
            // Kişisel veri kuralı: öğrenci adı ve cevap paylaşıma girmez.
            assertFalse("Paylaşımda öğrenci adı olmamalı: $metin", metin.contains("Elif"))
            assertFalse("Paylaşımda cevap olmamalı: $metin", metin.contains("gizli cevap"))
        } finally {
            kaynak.delete()
        }
    }

    @Test
    fun cokluSoruTekIntentteVeGorselSinirindaKalir() {
        val kaynaklar = (1..3).map { testGorseli("coklu-$it") }
        try {
            val grup = runBlocking { requireNotNull(ogretmenDeposu.grupEkle("7B")) }
            val sorular = kaynaklar.mapIndexed { i, dosya -> soru(grup.id, "Soru ${i + 1}", "Ahmet", dosya) }
            val dersAdlari = mapOf("m" to "Matematik")

            val paylasim = requireNotNull(
                OgretmenPaylasim.intent(
                    context = context,
                    sorular = sorular,
                    gorselDeposu = gorselDeposu,
                    dersAdlari = dersAdlari,
                    uriUret = sahteUriUretici,
                ),
            )
            val dosyalar = sorular.flatMap { s ->
                s.gorseller.map { g -> g.dosyaAdi to gorselDeposu.dosyaGoruntuIcin(g.dosyaAdi) }
            }
            assertTrue(
                "Üç fotoğraf da diskte olmalı: ${dosyalar.map { it.first to (it.second?.isFile == true) }}",
                dosyalar.size == 3 && dosyalar.all { it.second?.isFile == true },
            )
            assertEquals(
                "Chooser içeriği: ${OgretmenPaylasim.gorselUrisi(context, sorular, gorselDeposu, uriUret = sahteUriUretici)}",
                Intent.ACTION_SEND_MULTIPLE,
                seciciIci(paylasim).action,
            )
            val gonder = seciciIci(paylasim)
            val uriler = requireNotNull(gonder.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM))
            assertEquals(3, uriler.size)
            val metin = requireNotNull(gonder.getStringExtra(Intent.EXTRA_TEXT))
            assertTrue(metin.contains("1) Soru 1"))
            assertTrue(metin.contains("3) Soru 3"))
            assertFalse(metin.contains("Ahmet"))
            assertTrue(OgretmenPaylasim.MAKS_GORSEL in 1..20)

            // 10 soru 10 fotoğraf olsa da gönderilen dosya sayısı sınırı aşmaz.
            val cok = (1..10).flatMap { sorular }
            val sinirli = OgretmenPaylasim.gorselUrisi(
                context,
                cok,
                gorselDeposu,
                uriUret = sahteUriUretici,
            )
            assertEquals(OgretmenPaylasim.MAKS_GORSEL, sinirli.size)
        } finally {
            kaynaklar.forEach { it.delete() }
        }
    }

    @Test
    fun gorseliOlmayanListeMetinOlarakPaylasilir() {
        val grup = runBlocking { requireNotNull(ogretmenDeposu.grupEkle("6C")) }
        val kaynak = testGorseli("metin")
        try {
            val soru = soru(grup.id, "Kesirler", "Zeynep", kaynak)
            // Dosya diskten düşerse paylaşım ölmez; yalnızca metin gider.
            runBlocking { gorselDeposu.depolananDosyayiSil(soru.gorseller.single().dosyaAdi) }
            assertTrue(
                OgretmenPaylasim.gorselUrisi(context, listOf(soru), gorselDeposu, uriUret = sahteUriUretici)
                    .isEmpty(),
            )

            val paylasim = OgretmenPaylasim.intent(
                context,
                listOf(soru),
                gorselDeposu,
                uriUret = sahteUriUretici,
            )
            assertNotNull(paylasim)
            val gonder = seciciIci(requireNotNull(paylasim))
            assertEquals(Intent.ACTION_SEND, gonder.action)
            assertEquals("text/plain", gonder.type)
            assertFalse(requireNotNull(gonder.getStringExtra(Intent.EXTRA_TEXT)).contains("Zeynep"))
        } finally {
            kaynak.delete()
        }
    }

    @Test
    fun filePathsVeSaglayiciPaylasimIcinYapilandirilmis() {
        // Fotoğraflar `filesDir` altındaki iki kökten gelir; sağlayıcı dışa açık değildir ve izin
        // yalnızca URI başına verilir. Bu, paylaşım niyetinin cihazda çalışmasının ön koşulu.
        val parser = context.resources.getXml(R.xml.file_paths)
        val kokler = mutableListOf<Pair<String, String?>>()
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG) {
                kokler += parser.name to parser.getAttributeValue(null, "path")
            }
        }
        assertTrue("Kökler: $kokler", ("files-path" to "ogretmen-img/") in kokler)
        assertTrue("Kökler: $kokler", ("files-path" to "img/") in kokler)

        val saglayici = context.packageManager.resolveContentProvider(
            "${context.packageName}.fileprovider",
            PackageManager.GET_META_DATA,
        )
        val bilgi = requireNotNull(saglayici) { "FileProvider manifestte bildirilmeli" }
        assertFalse("Paylaşım sağlayıcısı dışa açık olmamalı", bilgi.exported)
        assertTrue("URI izni ACTION_SEND sırasında verilir", bilgi.grantUriPermissions)
    }

    @Test
    fun bosListeIcinIntentUretilmez() {
        assertTrue(OgretmenPaylasim.intent(context, emptyList(), gorselDeposu) == null)
        assertEquals("", OgretmenPaylasim.metin(context, emptyList()))
    }
}

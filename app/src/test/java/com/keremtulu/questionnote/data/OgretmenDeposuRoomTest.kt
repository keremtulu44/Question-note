package com.keremtulu.questionnote.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.room.Room
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.prefs.OgretmenAlanDurumu
import com.keremtulu.questionnote.data.prefs.OgretmenDeposu
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.OgretmenDurum
import com.keremtulu.questionnote.domain.model.OgretmenZorluk
import java.io.File
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
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

/**
 * Store-level coverage for the Room v5 rewrite: the one-time JSON import, bulk status changes,
 * moving a question between classes and the delete undo that keeps a photo alive for a few seconds.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OgretmenDeposuRoomTest {
    private lateinit var context: Context
    private lateinit var database: YanlisDefteriDatabase
    private lateinit var gorselDeposu: LocalSoruGorselDeposu
    private lateinit var ogretmenDeposu: OgretmenDeposu

    private val eskiJson = "ogretmen_calisma_alani.json"

    @Before
    fun kur() {
        context = RuntimeEnvironment.getApplication()
        File(context.filesDir, eskiJson).delete()
        File(context.filesDir, "ogretmen_calisma_alani.ice-aktarildi.json").delete()
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

    private fun testGorseli(ad: String = "room-test"): File {
        val file = File(context.cacheDir, "$ad-${System.nanoTime()}.png")
        val bmp = Bitmap.createBitmap(200, 150, Bitmap.Config.ARGB_8888)
        Canvas(bmp).apply { drawColor(Color.rgb(235, 240, 245)) }
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bmp.recycle()
        return file
    }

    @Test
    fun eskiJsonBirKezIceAktarilirVeKayitlarKorunur() = runBlocking {
        val kaynak = testGorseli("eski")
        val kaliciAd: String
        try {
            val hazir = gorselDeposu.dosyadanHazirla(kaynak, GorselTuru.SORU, 0)
            kaliciAd = requireNotNull(gorselDeposu.ogretmenKaliciyaKopyala(hazir.dosyaAdi))
            gorselDeposu.geciciDosyayiSil(hazir.dosyaAdi)
        } finally {
            kaynak.delete()
        }

        val json = JSONObject()
            .put("schema_version", 1)
            .put(
                "groups",
                JSONArray().put(
                    JSONObject()
                        .put("id", "g1")
                        .put("name", "6B")
                        .put("description", "Salı")
                        .put("archived", false)
                        .put("order", 0)
                        .put("created_at", 1_000L),
                ),
            )
            .put(
                "questions",
                JSONArray()
                    .put(
                        JSONObject()
                            .put("id", "q1")
                            .put("group_id", "g1")
                            .put("title", "Kesirler")
                            .put("source", "Kitap")
                            .put("asked_by", "Elif")
                            .put("difficulty", "ORTA")
                            .put("answer", "12")
                            .put("solved", false)
                            .put("created_at", 1_000L)
                            .put("updated_at", 1_000L)
                            .put(
                                "images",
                                JSONArray().put(
                                    JSONObject()
                                        .put("id", "i1")
                                        .put("order", 0)
                                        .put("kind", "SORU")
                                        .put("file_name", kaliciAd)
                                        .put("mime_type", "image/jpeg")
                                        .put("width", 200)
                                        .put("height", 150)
                                        .put("size_bytes", 10)
                                        .put("sha256", "x"),
                                ),
                            ),
                    )
                    // Broken record: no images. It must be counted, not crash the import.
                    .put(
                        JSONObject()
                            .put("id", "q2")
                            .put("group_id", "g1")
                            .put("title", "Görselsiz")
                            .put("images", JSONArray()),
                    ),
            )
        File(context.filesDir, eskiJson).writeText(json.toString())

        ogretmenDeposu.baslat()

        val veri = ogretmenDeposu.anlikVeri()
        assertEquals(1, veri.gruplar.size)
        assertEquals("6B", veri.gruplar.single().ad)
        assertEquals(1, veri.sorular.size)
        val soru = veri.sorular.single()
        assertEquals("Kesirler", soru.baslik)
        assertEquals(OgretmenZorluk.ORTA, soru.zorluk)
        assertEquals(kaliciAd, soru.soruGorselleri().single().dosyaAdi)
        assertFalse(soru.cozuldu)
        assertTrue(ogretmenDeposu.anlikDurum() is OgretmenAlanDurumu.IceAktarildi)

        // The legacy file is archived, never left as the source of truth.
        assertFalse(File(context.filesDir, eskiJson).exists())
        assertTrue(File(context.filesDir, "ogretmen_calisma_alani.ice-aktarildi.json").isFile)
        assertEquals(1, database.ogretmenDao().soruSayisi())
    }

    @Test
    fun topluDurumTasimaVeKopyalamaTekTekDogrulanir() = runBlocking {
        val g1 = requireNotNull(ogretmenDeposu.grupEkle("6B"))
        val g2 = requireNotNull(ogretmenDeposu.grupEkle("7C"))
        val kaynak = testGorseli("akis")
        val soruId: String
        try {
            val hazir = gorselDeposu.dosyadanHazirla(kaynak, GorselTuru.SORU, 0)
            val soru = requireNotNull(
                ogretmenDeposu.soruKaydet(
                    grupId = g1.id,
                    baslik = "Oran",
                    zorluk = OgretmenZorluk.KOLAY,
                    cevap = "48",
                    gorseller = listOf(hazir),
                ),
            )
            soruId = soru.id
            assertTrue(soru.dosyaAdiKaliciMi())

            // Bulk status: one statement, one changed row, the rest stays untouched.
            assertEquals(1, ogretmenDeposu.topluDurumDegistir(setOf(soruId), OgretmenDurum.COZULDU))
            assertEquals(OgretmenDurum.COZULDU, ogretmenDeposu.anlikVeri().sorular.single().durum)
            assertEquals(0, ogretmenDeposu.topluDurumDegistir(setOf("yok"), OgretmenDurum.ARSIV))

            // Move keeps the same row and the same file.
            assertTrue(ogretmenDeposu.soruTasi(soruId, g2.id))
            val tasinan = ogretmenDeposu.anlikVeri().sorular.single()
            assertEquals(g2.id, tasinan.grupId)
            assertEquals(soruId, tasinan.id)
            assertNotNull(gorselDeposu.dosyaGoruntuIcin(tasinan.gorseller.single().dosyaAdi))

            // Copy creates a new id per target and its own image row, and starts unsolved.
            val g3 = requireNotNull(ogretmenDeposu.grupEkle("8A"))
            assertEquals(1, ogretmenDeposu.baskaGruplaraKopyala(soruId, setOf(g3.id)))
            val kopya = ogretmenDeposu.anlikVeri().sorular.single { it.grupId == g3.id }
            assertTrue(kopya.id != soruId)
            assertEquals(OgretmenDurum.BEKLIYOR, kopya.durum)
            assertEquals(tasinan.gorseller.single().dosyaAdi, kopya.gorseller.single().dosyaAdi)
        } finally {
            kaynak.delete()
        }
    }

    @Test
    fun silmeGeriAlinabilirVeDosyaGeriYuklemedeKorunur() = runBlocking {
        val grup = requireNotNull(ogretmenDeposu.grupEkle("6B"))
        val kaynak = testGorseli("sil")
        try {
            val hazir = gorselDeposu.dosyadanHazirla(kaynak, GorselTuru.SORU, 0)
            val soru = requireNotNull(
                ogretmenDeposu.soruKaydet(
                    grupId = grup.id,
                    baslik = "Silinecek",
                    zorluk = null,
                    cevap = null,
                    gorseller = listOf(hazir),
                ),
            )
            val dosyaAdi = soru.gorseller.single().dosyaAdi

            // UI path: keep the file so the snackbar undo can restore the exact row.
            assertEquals(1, ogretmenDeposu.topluSil(setOf(soru.id), dosyalariKoru = true))
            assertTrue(ogretmenDeposu.anlikVeri().sorular.isEmpty())
            assertNotNull(gorselDeposu.dosyaGoruntuIcin(dosyaAdi))

            assertEquals(1, ogretmenDeposu.silmeyiGeriAl(listOf(soru)))
            val geri = ogretmenDeposu.anlikVeri().sorular.single()
            assertEquals(soru.id, geri.id)
            assertEquals("Silinecek", geri.baslik)

            // Explicit delete without undo still collects the now-unreferenced file.
            assertTrue(ogretmenDeposu.soruSil(geri.id))
            assertNull(gorselDeposu.dosyaGoruntuIcin(dosyaAdi))
        } finally {
            kaynak.delete()
        }
    }
}

/** Test helper: a saved question always points at a copied permanent file, never at a temp one. */
private fun com.keremtulu.questionnote.domain.model.OgretmenSorusu.dosyaAdiKaliciMi(): Boolean =
    gorseller.isNotEmpty() && gorseller.all { it.dosyaAdi.startsWith("ogretmen-img/") || it.dosyaAdi.startsWith("img/") }

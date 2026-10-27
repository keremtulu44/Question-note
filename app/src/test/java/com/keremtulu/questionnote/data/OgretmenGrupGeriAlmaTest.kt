package com.keremtulu.questionnote.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.room.Room
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.prefs.OgretmenDeposu
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.OgretmenDurum
import com.keremtulu.questionnote.domain.model.OgretmenVerisi
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
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
 * T3 kapatma döngüsü: grup silme 10 saniyelik gerçek bir geri alma penceresi sunar ve arşivlenen
 * sorular kuyruğa dönebilir. Dosyalar silme sırasında korunur, yoksa geri alma yarım bir grup
 * bırakırdı ([OgretmenVerisi.tutarliMi] ihlali).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OgretmenGrupGeriAlmaTest {
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
        val bmp = Bitmap.createBitmap(160, 120, Bitmap.Config.ARGB_8888)
        Canvas(bmp).apply { drawColor(Color.rgb(232, 240, 250)) }
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bmp.recycle()
        return file
    }

    @Test
    fun grupSilmeGeriAlinabilirVeSorularAyniKimlikleDoner() = runBlocking {
        val grup = requireNotNull(ogretmenDeposu.grupEkle("6B", "Salı 15:00"))
        val kaynak = testGorseli("grup-geri-al")
        try {
            val bekleyen = requireNotNull(
                ogretmenDeposu.soruKaydet(
                    grupId = grup.id,
                    baslik = "Oran Orantı",
                    zorluk = null,
                    cevap = null,
                    gorseller = listOf(gorselDeposu.dosyadanHazirla(kaynak, GorselTuru.SORU, 0)),
                    kimSordu = "Elif Y.",
                ),
            )
            val cozulen = requireNotNull(
                ogretmenDeposu.soruKaydet(
                    grupId = grup.id,
                    baslik = "Basınç",
                    zorluk = null,
                    cevap = null,
                    gorseller = listOf(gorselDeposu.dosyadanHazirla(kaynak, GorselTuru.SORU, 0)),
                ),
            )
            ogretmenDeposu.cozulduDurumuDegistir(cozulen.id, true)
            val silinecekSorular = ogretmenDeposu.anlikVeri().grupSorulari(grup.id)
            val dosyaAdi = bekleyen.gorseller.single().dosyaAdi

            // UI yolu: dosyalar korunur, böylece geri alma tam kaydı geri getirebilir.
            assertTrue(ogretmenDeposu.grupSil(grup.id, dosyalariKoru = true))
            assertTrue(ogretmenDeposu.anlikVeri().gruplar.isEmpty())
            assertTrue(ogretmenDeposu.anlikVeri().sorular.isEmpty())
            assertNotNull(gorselDeposu.dosyaGoruntuIcin(dosyaAdi))

            val geri = requireNotNull(ogretmenDeposu.grupGeriAl(grup, silinecekSorular))
            assertEquals(2, geri)
            val veri = ogretmenDeposu.anlikVeri()
            assertEquals(listOf(grup.id), veri.gruplar.map { it.id })
            assertEquals("Salı 15:00", veri.gruplar.single().aciklama)
            assertEquals(setOf(bekleyen.id, cozulen.id), veri.sorular.mapTo(mutableSetOf()) { it.id })
            assertEquals(OgretmenDurum.BEKLIYOR, veri.sorular.first { it.id == bekleyen.id }.durum)
            assertEquals(OgretmenDurum.COZULDU, veri.sorular.first { it.id == cozulen.id }.durum)
            assertEquals("Elif Y.", veri.sorular.first { it.id == bekleyen.id }.kimSordu)
            // Geri yükleme tutarlılık kapısından geçmeli: grup var, her sorunun fotoğrafı var.
            assertTrue(veri.tutarliMi())
        } finally {
            kaynak.delete()
        }
    }

    @Test
    fun geriAlmaTekrarCagrilirsaSessizceReddeder() = runBlocking {
        val grup = requireNotNull(ogretmenDeposu.grupEkle("7C"))
        val kaynak = testGorseli("geri-al-tekrar")
        try {
            val soru = requireNotNull(
                ogretmenDeposu.soruKaydet(
                    grupId = grup.id,
                    baslik = "Üçgenler",
                    zorluk = null,
                    cevap = null,
                    gorseller = listOf(gorselDeposu.dosyadanHazirla(kaynak, GorselTuru.SORU, 0)),
                ),
            )
            val kopya = ogretmenDeposu.anlikVeri().grupSorulari(grup.id)
            assertTrue(ogretmenDeposu.grupSil(grup.id, dosyalariKoru = true))
            assertEquals(1, requireNotNull(ogretmenDeposu.grupGeriAl(grup, kopya)))
            // İkinci çağrı aynı satırları iki kez yazmamalı.
            assertNull(ogretmenDeposu.grupGeriAl(grup, kopya))
            assertEquals(listOf(soru.id), ogretmenDeposu.anlikVeri().sorular.map { it.id })
        } finally {
            kaynak.delete()
        }
    }

    @Test
    fun arsivlenenSoruKuyrugaGeriDonerVeDosyaKorunur() = runBlocking {
        val grup = requireNotNull(ogretmenDeposu.grupEkle("8A"))
        val kaynak = testGorseli("arsiv-geri")
        try {
            val soru = requireNotNull(
                ogretmenDeposu.soruKaydet(
                    grupId = grup.id,
                    baslik = "Kesirler",
                    zorluk = null,
                    cevap = null,
                    gorseller = listOf(gorselDeposu.dosyadanHazirla(kaynak, GorselTuru.SORU, 0)),
                ),
            )
            val dosyaAdi = soru.gorseller.single().dosyaAdi
            assertEquals(1, ogretmenDeposu.topluDurumDegistir(setOf(soru.id), OgretmenDurum.ARSIV))
            assertEquals(OgretmenDurum.ARSIV, ogretmenDeposu.anlikVeri().sorular.single().durum)

            assertEquals(1, ogretmenDeposu.topluDurumDegistir(setOf(soru.id), OgretmenDurum.BEKLIYOR))
            val geri = ogretmenDeposu.anlikVeri().sorular.single()
            assertEquals(OgretmenDurum.BEKLIYOR, geri.durum)
            assertNull(geri.cozulme)
            assertNotNull(gorselDeposu.dosyaGoruntuIcin(dosyaAdi))
        } finally {
            kaynak.delete()
        }
    }
}

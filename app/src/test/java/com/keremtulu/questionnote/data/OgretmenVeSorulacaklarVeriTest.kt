package com.keremtulu.questionnote.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.room.Room
import com.keremtulu.questionnote.data.export.YerelYedeklemeHizmeti
import com.keremtulu.questionnote.data.images.GorselTutarliligiHizmeti
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.prefs.OgretmenDeposu
import com.keremtulu.questionnote.data.prefs.SinavProfiliDeposu
import com.keremtulu.questionnote.data.repository.RoomSoruRepository
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.KullaniciRolu
import com.keremtulu.questionnote.domain.model.OgretmenVerisi
import com.keremtulu.questionnote.domain.model.OgretmenZorluk
import com.keremtulu.questionnote.domain.model.SoruIslemSonucu
import com.keremtulu.questionnote.domain.model.SoruKaydetmeIstegi
import com.keremtulu.questionnote.domain.model.SoruPlanDurumu
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OgretmenVeSorulacaklarVeriTest {
    private lateinit var context: Context
    private lateinit var database: YanlisDefteriDatabase
    private lateinit var gorselDeposu: LocalSoruGorselDeposu
    private lateinit var ogretmenDeposu: OgretmenDeposu
    private lateinit var soruRepository: RoomSoruRepository
    private val saat = Clock.fixed(Instant.parse("2026-10-25T10:00:00Z"), ZoneId.of("Europe/Istanbul"))

    @Before
    fun kur() {
        context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("sinav_profili", Context.MODE_PRIVATE).edit().clear().commit()
        File(context.filesDir, "ogretmen_calisma_alani.json").delete()
        File(context.filesDir, "ogretmen-img").deleteRecursively()
        File(context.filesDir, "img").deleteRecursively()
        database = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle())
            .allowMainThreadQueries()
            .build()
        gorselDeposu = LocalSoruGorselDeposu(context)
        ogretmenDeposu = OgretmenDeposu(context, gorselDeposu)
        soruRepository = RoomSoruRepository(database, gorselDeposu, saat = saat)
    }

    @After
    fun temizle() {
        database.close()
    }

    private fun testGorseliOlustur(genislik: Int = 240, yukseklik: Int = 180): File {
        val file = File(context.cacheDir, "test-img-${System.nanoTime()}.png")
        val bmp = Bitmap.createBitmap(genislik, yukseklik, Bitmap.Config.ARGB_8888)
        Canvas(bmp).apply { drawColor(Color.rgb(240, 244, 248)) }
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bmp.recycle()
        return file
    }

    @Test
    fun sorulacakOlarakKaydedilenSoruGunlukTekraraGirmezVeCozdurulunceYarinaPlanlanir() = runBlocking {
        val bugun = LocalDate.now(saat).toEpochDay().toInt()
        val matId = database.dersDao().tumunuGetir().first { it.ad == "Matematik" }.id
        val kaynakDosya = testGorseliOlustur()
        val cozumKaynak = testGorseliOlustur(200, 200)
        try {
            val soruGorsel = gorselDeposu.dosyadanHazirla(kaynakDosya, GorselTuru.SORU, 0)
            val sonuc = soruRepository.soruKaydet(
                SoruKaydetmeIstegi(
                    dersId = matId,
                    kaynak = "3D TYT",
                    planDurumu = SoruPlanDurumu.OGRETMENE_SORULACAK,
                    gorseller = listOf(soruGorsel),
                ),
            )
            assertEquals(SoruIslemSonucu.Basarili, sonuc)

            val bekleyenler = soruRepository.ogretmeneSorulacaklariIzle().first()
            assertEquals(1, bekleyenler.size)
            val kaydedilen = bekleyenler.single()
            assertEquals(SoruPlanDurumu.OGRETMENE_SORULACAK, kaydedilen.planDurumu)
            assertNull(kaydedilen.sonrakiTekrarGun)
            assertTrue(soruRepository.bugunTekrarlanacaklariIzle(bugun + 10).first().isEmpty())

            val hamCozum = gorselDeposu.dosyadanHazirla(cozumKaynak, GorselTuru.COZUM, 0)
            val kirpilmisCozum = gorselDeposu.kirp(hamCozum, 0.1f, 0.1f, 0.9f, 0.9f)
            gorselDeposu.geciciDosyayiSil(hamCozum.dosyaAdi)

            val cozduruldu = soruRepository.ogretmeneCozduruldu(
                soruId = kaydedilen.id,
                yeniCozumGorselleri = listOf(kirpilmisCozum),
                cozumNotuEki = "Hoca benzerlik oranından çözdü.",
            )
            assertEquals(SoruIslemSonucu.Basarili, cozduruldu)
            assertTrue(soruRepository.ogretmeneSorulacaklariIzle().first().isEmpty())

            val guncel = requireNotNull(soruRepository.soruGetir(kaydedilen.id))
            assertEquals(SoruPlanDurumu.AKTIF, guncel.planDurumu)
            assertEquals(bugun + 1, guncel.sonrakiTekrarGun)
            assertEquals("Hoca benzerlik oranından çözdü.", guncel.cozumNotu)
            assertEquals(1, guncel.gorseller.count { it.tur == GorselTuru.COZUM })
            assertEquals(1, soruRepository.bugunTekrarlanacaklariIzle(bugun + 1).first().size)
        } finally {
            kaynakDosya.delete()
            cozumKaynak.delete()
        }
    }

    @Test
    fun ogretmenDeposuGruplarArasiBagimsizCozulduDurumuVeOrtakGorselTemizligi() = runBlocking {
        val profil = SinavProfiliDeposu(context)
        assertFalse(profil.rolSecildi())
        profil.rolKaydet(KullaniciRolu.OGRETMEN)
        assertTrue(profil.rolSecildi())
        assertEquals(KullaniciRolu.OGRETMEN, profil.aktifRol())

        val grup6B = requireNotNull(ogretmenDeposu.grupEkle("6B", "Salı"))
        val grupAhmet = requireNotNull(ogretmenDeposu.grupEkle("Özel Ders (Ahmet)"))
        assertNull(ogretmenDeposu.grupEkle("6b"))

        val kaynak = testGorseliOlustur(240, 200)
        try {
            val hazir = gorselDeposu.dosyadanHazirla(kaynak, GorselTuru.SORU, 0)
            val kirpilmis = gorselDeposu.kirp(hazir, 0.1f, 0.1f, 0.85f, 0.85f)
            gorselDeposu.geciciDosyayiSil(hazir.dosyaAdi)

            val soru6B = requireNotNull(
                ogretmenDeposu.soruKaydet(
                    grupId = grup6B.id,
                    baslik = "Kesirler Yeni Nesil",
                    zorluk = OgretmenZorluk.ZOR,
                    cevap = "D",
                    gorseller = listOf(kirpilmis),
                ),
            )
            val kaliciDosyaAdi = soru6B.gorseller.single().dosyaAdi
            assertNotNull(gorselDeposu.dosyaGoruntuIcin(kaliciDosyaAdi))

            // Öğretmen görseli, öğrenci görsel bakımında yetim olarak görünmemeli.
            val bakim = GorselTutarliligiHizmeti(database, gorselDeposu)
                .denetle(System.currentTimeMillis() + 48L * 3600_000L)
            assertTrue(bakim.onayBekleyenYetimler.isEmpty())

            assertTrue(ogretmenDeposu.cozulduDurumuDegistir(soru6B.id, true))
            assertEquals(1, ogretmenDeposu.baskaGruplaraKopyala(soru6B.id, setOf(grupAhmet.id)))

            val anlik = ogretmenDeposu.anlikVeri()
            val guncel6B = anlik.sorular.single { it.grupId == grup6B.id }
            val kopyaAhmet = anlik.sorular.single { it.grupId == grupAhmet.id }
            assertTrue(guncel6B.cozuldu)
            assertFalse(kopyaAhmet.cozuldu)
            assertEquals("D", kopyaAhmet.cevap)
            assertEquals(OgretmenZorluk.ZOR, kopyaAhmet.zorluk)

            // 6B'den silindiğinde Ahmet grubundaki kopya hâlâ görseli kullandığı için dosya silinmez.
            assertTrue(ogretmenDeposu.soruSil(guncel6B.id))
            assertNotNull(gorselDeposu.dosyaGoruntuIcin(kaliciDosyaAdi))

            // Son kopya da silinince kalıcı öğretmen görseli temizlenir.
            assertTrue(ogretmenDeposu.soruSil(kopyaAhmet.id))
            assertNull(gorselDeposu.dosyaGoruntuIcin(kaliciDosyaAdi))
        } finally {
            kaynak.delete()
        }
    }

    @Test
    fun zipYedeklemeOgretmenCalismaAlaniniVeSorulacakDurumunuKorur() = runBlocking {
        val matId = database.dersDao().tumunuGetir().first { it.ad == "Matematik" }.id
        val ogrenciKaynak = testGorseliOlustur(180, 140)
        val ogretmenKaynak = testGorseliOlustur(200, 160)
        var arsiv: File? = null
        try {
            val ogrenciGorsel = gorselDeposu.dosyadanHazirla(ogrenciKaynak, GorselTuru.SORU, 0)
            assertEquals(
                SoruIslemSonucu.Basarili,
                soruRepository.soruKaydet(
                    SoruKaydetmeIstegi(
                        dersId = matId,
                        planDurumu = SoruPlanDurumu.OGRETMENE_SORULACAK,
                        gorseller = listOf(ogrenciGorsel),
                    ),
                ),
            )
            val grup7C = requireNotNull(ogretmenDeposu.grupEkle("7C", "Cuma"))
            val ogretmenGorsel = gorselDeposu.dosyadanHazirla(ogretmenKaynak, GorselTuru.SORU, 0)
            val kaydedilenOgretmenSorusu = requireNotNull(
                ogretmenDeposu.soruKaydet(
                    grupId = grup7C.id,
                    baslik = "Denklemler",
                    zorluk = OgretmenZorluk.ORTA,
                    cevap = "12",
                    gorseller = listOf(ogretmenGorsel),
                ),
            )
            ogretmenDeposu.cozulduDurumuDegistir(kaydedilenOgretmenSorusu.id, true)

            val yedekleme = YerelYedeklemeHizmeti(
                context = context,
                database = database,
                gorselDeposu = gorselDeposu,
                ogretmenDeposu = ogretmenDeposu,
            )
            arsiv = yedekleme.zipOlustur()
            assertTrue(arsiv.isFile && arsiv.length() > 0L)

            // Bellekteki öğretmen verisini sıfırla ve ZIP'ten geri yükle.
            ogretmenDeposu.geriYukle(OgretmenVerisi())
            assertTrue(ogretmenDeposu.anlikVeri().gruplar.isEmpty())

            val onizleme = yedekleme.uriIleOnizle(Uri.fromFile(arsiv))
            yedekleme.geriYukle(onizleme)

            val geriYuklenenOgretmen = ogretmenDeposu.anlikVeri()
            assertEquals(1, geriYuklenenOgretmen.gruplar.size)
            assertEquals("7C", geriYuklenenOgretmen.gruplar.single().ad)
            val geriSoru = geriYuklenenOgretmen.sorular.single()
            assertEquals("Denklemler", geriSoru.baslik)
            assertEquals("12", geriSoru.cevap)
            assertEquals(OgretmenZorluk.ORTA, geriSoru.zorluk)
            assertTrue(geriSoru.cozuldu)
            assertNotNull(gorselDeposu.dosyaGoruntuIcin(geriSoru.gorseller.single().dosyaAdi))

            val geriSorulacaklar = soruRepository.ogretmeneSorulacaklariIzle().first()
            assertEquals(1, geriSorulacaklar.size)
            assertEquals(SoruPlanDurumu.OGRETMENE_SORULACAK, geriSorulacaklar.single().planDurumu)
        } finally {
            ogrenciKaynak.delete()
            ogretmenKaynak.delete()
            arsiv?.delete()
        }
    }
}

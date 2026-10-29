package com.keremtulu.questionnote.ui.rehber

import com.keremtulu.questionnote.domain.model.NetKurali
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import com.keremtulu.questionnote.domain.repeat.TekrarKurali
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Rehberin sayısal sözleşmesi.
 *
 * Rehber kendi kuralını uydurmaz: tekrar önizlemesi `TekrarKurali`'dan, deneme özeti `ozet()`'ten
 * gelir. Bu testler rehber metinlerindeki sayıların uygulamanın gerçek motoruyla aynı kaldığını
 * doğrular; model değişip rehber geride kalırsa burada kırılır.
 */
class RehberModeliTest {

    @Test
    fun bolumlerYediTanidirVeAnahtarlariTekildir() {
        val bolumler = RehberBolumu.entries
        assertEquals(7, bolumler.size)
        assertEquals(
            listOf("tanitim", "kayit", "bugun", "deneme", "cozum", "yedek", "kapanis"),
            bolumler.map { it.anahtar },
        )
        assertEquals(bolumler.size, bolumler.map { it.anahtar }.distinct().size)
        bolumler.forEach { assertEquals(it, RehberBolumu.anahtarla(it.anahtar)) }
    }

    @Test
    fun bilinmeyenVeyaBosAnahtarBolumDondurmez() {
        assertNull(RehberBolumu.anahtarla("olmayan-bolum"))
        assertNull(RehberBolumu.anahtarla(null))
        assertNull(RehberBolumu.anahtarla(""))
    }

    @Test
    fun tekrarOnizlemesiGercekMotorlaAyniSonucuVerir() {
        val bugun = LocalDate.of(2026, 3, 14)
        TekrarSonucu.entries.forEach { sonuc ->
            val beklenen = TekrarKurali.hesapla(
                oncekiAralik = null,
                sonuc = sonuc,
                cozumAcildi = false,
                bugun = bugun,
            )
            assertEquals(beklenen, tekrarOnizlemesi(sonuc, bugun))
        }
    }

    @Test
    fun sonrakiTekrarTarihiBugundenIleriVeTutarlidir() {
        val bugun = LocalDate.of(2026, 1, 31)
        TekrarSonucu.entries.forEach { sonuc ->
            val plan = tekrarOnizlemesi(sonuc, bugun)
            assertTrue("aralık en az bir gün olmalı", plan.sonrakiAralik >= 1)
            assertEquals(
                bugun.plusDays(plan.sonrakiAralik.toLong()).toEpochDay().toInt(),
                plan.sonrakiGun,
            )
        }
    }

    @Test
    fun cozemedimEnKisaRahatEnUzunAraligiVerir() {
        val bugun = LocalDate.of(2026, 5, 1)
        // Aynı geçmiş (3 günlük aralık) verilir: fark yalnız sonucun kendisinden gelmeli.
        val cozemedim = tekrarOnizlemesi(TekrarSonucu.COZEMEDIM, bugun, oncekiAralik = 3).sonrakiAralik
        val zorlandim = tekrarOnizlemesi(TekrarSonucu.ZORLANDIM, bugun, oncekiAralik = 3).sonrakiAralik
        val rahat = tekrarOnizlemesi(TekrarSonucu.RAHAT, bugun, oncekiAralik = 3).sonrakiAralik
        // "Rahat çözdüm" seyrekleşir, "çözemedim" yarına döner: rehberdeki metin bunu anlatır.
        assertTrue(cozemedim < zorlandim)
        assertTrue(zorlandim < rahat)
    }

    @Test
    fun kuyrukTurBoyuDaimaBesIleOtuzArasinda() {
        listOf(-5, 0, 1, 5, 10, 20, 45, 120).forEach { hedef ->
            val kuyruk = demoKuyrugu(hedef = hedef)
            assertTrue("tur boyu alt sınırı", kuyruk.turBoyu >= 1)
            assertTrue("tur boyu üst sınırı", kuyruk.turBoyu <= 30)
            assertTrue("kalan negatif olamaz", kuyruk.siradaKalan >= 0)
            assertEquals(kuyruk.geciken, demoSorular().count { it.durum == DemoSoruDurumu.GECIKTI })
        }
        listOf(5, 12, 40).forEach { hedef ->
            val kuyruk = demoKuyrugu(hedef = hedef, yapilan = 0)
            assertTrue(kuyruk.turBoyu in 5..30)
        }
    }

    @Test
    fun kuyrukOnizlemesiGecikenSorulariOneAlir() {
        val kuyruk = demoKuyrugu()
        assertEquals(3, kuyruk.onizleme.size)
        assertTrue(kuyruk.onizleme.first().durum == DemoSoruDurumu.GECIKTI)
        assertEquals(
            kuyruk.onizleme.map { it.gecikmeGun }.sortedDescending(),
            kuyruk.onizleme.map { it.gecikmeGun },
        )
    }

    @Test
    fun dersSkoruKurallariVeSinirlari() {
        val skor = DemoDersSkoru(dersAdi = "Matematik", soruSayisi = 20, dogru = 12, yanlis = 4)
        assertEquals(4, skor.bos)
        assertEquals(11.0, skor.net(NetKurali.DORTTE_BIR), 0.0001)
        assertEquals(10.6666, skor.net(NetKurali.UCTE_BIR), 0.001)
        assertEquals(12.0, skor.net(NetKurali.YOK), 0.0001)

        // Doğru + yanlış soru sayısını aşamaz: + düğmesi sessizce durur, eksi sıfırda durur.
        val dolu = DemoDersSkoru(dersAdi = "Matematik", soruSayisi = 20, dogru = 16, yanlis = 4)
        assertEquals(dolu, dolu.dogruArtir())
        assertEquals(dolu, dolu.yanlisArtir())
        val sifir = DemoDersSkoru(dersAdi = "Matematik", soruSayisi = 20, dogru = 0, yanlis = 0)
        assertEquals(sifir, sifir.dogruAzalt())
        assertEquals(sifir, sifir.yanlisAzalt())
        // Hiç işaretlenmemiş satır tamamen boştur; üç sayının toplamı her zaman soru sayısıdır.
        assertEquals(20, sifir.bos)
        assertEquals(20, sifir.dogru + sifir.yanlis + sifir.bos)
    }

    @Test
    fun dersSkoruArtirmaVeAzaltmaSimetrik() {
        val skor = DemoDersSkoru(dersAdi = "Matematik")
        assertEquals(skor.dogru + 1, skor.dogruArtir().dogru)
        assertEquals(skor.dogru - 1, skor.dogruAzalt().dogru)
        assertEquals(skor.yanlis + 1, skor.yanlisArtir().yanlis)
        assertEquals(skor.yanlis - 1, skor.yanlisAzalt().yanlis)
        assertEquals(skor, skor.dogruArtir().dogruAzalt())
        assertEquals(skor, skor.yanlisArtir().yanlisAzalt())
    }

    @Test
    fun denemeOzetiGercekOzettenGelir() {
        val ozet = demoDenemeOzeti()
        assertEquals(2, ozet.kayitSayisi)
        // Örnek geçmişin tamamı aynı kuralda: ortalama/en iyi net karşılaştırılabilir olmalı.
        assertTrue(ozet.karsilastirilabilir)
        assertEquals(7.5, demoDenemeler().first().toplamNet(), 0.0001)
        assertEquals(13.25, demoDenemeler().last().toplamNet(), 0.0001)
        assertEquals(10.375, ozet.ortalamaNet ?: -1.0, 0.0001)
        assertEquals(13.25, ozet.enIyiNet ?: -1.0, 0.0001)
    }

    @Test
    fun yedekListesiGercekKapsamiAnlatir() {
        val icerik = yedekIcerigi()
        assertTrue(icerik.isNotEmpty())
        assertEquals(icerik.size, icerik.map { it.adAnahtari }.distinct().size)
        assertTrue(icerik.filter { it.dahil }.any { it.adAnahtari == "rehber_backup_item_questions" })
        assertTrue(icerik.filter { it.dahil }.any { it.adAnahtari == "rehber_backup_item_images" })
        assertTrue(icerik.filter { it.dahil }.any { it.adAnahtari == "rehber_backup_item_denemeler" })
        // Rozetler ve rehber ilerlemesi cihaza özgüdür: yedeğe girmez.
        assertTrue(icerik.none { it.adAnahtari == "rehber_backup_item_badges" && it.dahil })
        assertTrue(icerik.none { it.adAnahtari == "rehber_backup_item_guide" && it.dahil })
    }
}

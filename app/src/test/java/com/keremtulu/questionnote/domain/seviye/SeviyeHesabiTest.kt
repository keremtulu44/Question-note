package com.keremtulu.questionnote.domain.seviye

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeviyeHesabiTest {
    @Test
    fun `ilk seviye sifir xp ile baslar`() {
        val durum = SeviyeHesabi.durum(0)
        assertEquals(1, durum.seviye)
        assertEquals(0, durum.toplamXp)
        assertEquals(100, durum.sonrakiSeviyeXp)
        assertEquals(0f, durum.oran)
    }

    @Test
    fun `seviye araliklari her seviyede artar`() {
        assertEquals(100, SeviyeHesabi.seviyeAraligi(1))
        assertEquals(250, SeviyeHesabi.seviyeAraligi(2))
        assertEquals(450, SeviyeHesabi.seviyeAraligi(3))
        assertEquals(700, SeviyeHesabi.seviyeAraligi(4))
        assertEquals(1000, SeviyeHesabi.seviyeAraligi(5))
    }

    @Test
    fun `esikler kumulatif toplami verir`() {
        assertEquals(0, SeviyeHesabi.seviyeEsigi(1))
        assertEquals(100, SeviyeHesabi.seviyeEsigi(2))
        assertEquals(350, SeviyeHesabi.seviyeEsigi(3))
        assertEquals(800, SeviyeHesabi.seviyeEsigi(4))
    }

    @Test
    fun `tam esikte seviye atlanir`() {
        val tam = SeviyeHesabi.durum(100)
        assertEquals(2, tam.seviye)
        assertEquals(0, tam.buSeviyedeXp)
        assertEquals(250, tam.kalanXp)
    }

    @Test
    fun `esikten bir eksik onceki seviyede kalir`() {
        val durum = SeviyeHesabi.durum(99)
        assertEquals(1, durum.seviye)
        assertEquals(1, durum.kalanXp)
    }

    @Test
    fun `negatif xp sifir sayilir`() {
        assertEquals(1, SeviyeHesabi.durum(-50).seviye)
        assertEquals(0, SeviyeHesabi.durum(-50).toplamXp)
    }

    @Test
    fun `rutbeler seviyeye gore secilir`() {
        assertEquals(Rutbe.BASLANGIC, Rutbe.seviyeIcin(1))
        assertEquals(Rutbe.MERAKLI, Rutbe.seviyeIcin(2))
        assertEquals(Rutbe.DUZENLI, Rutbe.seviyeIcin(3))
        assertEquals(Rutbe.KARARLI, Rutbe.seviyeIcin(5))
        assertEquals(Rutbe.DISIPLINLI, Rutbe.seviyeIcin(6))
        assertEquals(Rutbe.AZIMLI, Rutbe.seviyeIcin(9))
        assertEquals(Rutbe.ODAKLI, Rutbe.seviyeIcin(11))
        assertEquals(Rutbe.USTA, Rutbe.seviyeIcin(13))
        assertEquals(Rutbe.SAMPION, Rutbe.seviyeIcin(15))
        assertEquals(Rutbe.SAMPION, Rutbe.seviyeIcin(40))
    }

    @Test
    fun `rutbe adlari tekrar etmez`() {
        assertEquals(Rutbe.entries.size, Rutbe.entries.map { it.ad }.toSet().size)
        assertEquals(Rutbe.entries.size, Rutbe.entries.map { it.anahtar }.toSet().size)
    }
}

class XpHesabiTest {
    @Test
    fun `kirilim agirliklari dogru uygular`() {
        val kirilim = XpHesabi.kirilim(
            soruSayisi = 10,
            tekrarSayisi = 20,
            bagimsizTekrarSayisi = 12,
            hedefTamamGunSayisi = 4,
            seriBonusu = 100,
            rozetSayisi = 3,
        )
        assertEquals(100, kirilim.ekleme)
        assertEquals(100, kirilim.tekrar)
        assertEquals(60, kirilim.bagimsiz)
        assertEquals(100, kirilim.hedef)
        assertEquals(100, kirilim.seri)
        assertEquals(150, kirilim.rozet)
        assertEquals(610, kirilim.toplam)
    }

    @Test
    fun `seri bonusu her kilometre tasinda yeniden verilir`() {
        // 3 gün + 7 gün (4-10) => 30 + 70
        val gunler = (0 until 10).map { 100 + it }
        assertEquals(100, XpHesabi.seriBonusu(gunler))
    }

    @Test
    fun `kopuk seriler ayri ayri sayilir`() {
        // 0,1,2 => 3 gün; 10,11,12 => 3 gün
        val gunler = listOf(0, 1, 2, 10, 11, 12)
        assertEquals(60, XpHesabi.seriBonusu(gunler))
    }

    @Test
    fun `en uzun seri hesaplanir`() {
        assertEquals(4, XpHesabi.enUzunSeri(listOf(1, 2, 3, 4, 9, 10)))
        assertEquals(0, XpHesabi.enUzunSeri(emptyList()))
        assertEquals(1, XpHesabi.enUzunSeri(listOf(7)))
    }

    @Test
    fun `tekrarli gunler seriyi bozmaz`() {
        assertEquals(3, XpHesabi.enUzunSeri(listOf(1, 1, 2, 3)))
        assertEquals(30, XpHesabi.seriBonusu((0 until 30).map { it }))
    }

    @Test
    fun `xp arttikca seviye asla dusmez`() {
        var onceki = 0
        var xp = 0
        while (xp < 5000) {
            val seviye = SeviyeHesabi.durum(xp).seviye
            assertTrue(seviye >= onceki)
            onceki = seviye
            xp += 37
        }
        assertNotEquals(1, onceki)
    }
}

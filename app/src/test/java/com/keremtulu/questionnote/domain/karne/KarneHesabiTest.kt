package com.keremtulu.questionnote.domain.karne

import com.keremtulu.questionnote.domain.model.TekrarSonucu
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KarneHesabiTest {
    private fun sonuc(
        sonuc: TekrarSonucu,
        cozumAcildi: Boolean = false,
        soruId: String = "soru",
    ) = OturumSonucu(soruId = soruId, sonuc = sonuc, cozumAcildi = cozumAcildi)

    @Test
    fun `rahat bagimsiz sayilir`() {
        assertEquals(KarneKovasi.BAGIMSIZ, KarneHesabi.kova(sonuc(TekrarSonucu.RAHAT)))
    }

    @Test
    fun `zorlandim yarim sayilir`() {
        assertEquals(KarneKovasi.YARIM, KarneHesabi.kova(sonuc(TekrarSonucu.ZORLANDIM)))
    }

    @Test
    fun `cozemedim cozemedi sayilir`() {
        assertEquals(KarneKovasi.COZEMEDI, KarneHesabi.kova(sonuc(TekrarSonucu.COZEMEDIM)))
    }

    @Test
    fun `cozum acildiysa rahat bile bagimsiz sayilmaz`() {
        assertEquals(
            KarneKovasi.COZUMLU,
            KarneHesabi.kova(sonuc(TekrarSonucu.RAHAT, cozumAcildi = true)),
        )
    }

    @Test
    fun `sayilar kovalara dogru dagilir`() {
        val sayilar = KarneHesabi.hesapla(
            listOf(
                sonuc(TekrarSonucu.RAHAT, soruId = "a"),
                sonuc(TekrarSonucu.RAHAT, soruId = "b"),
                sonuc(TekrarSonucu.ZORLANDIM, soruId = "c"),
                sonuc(TekrarSonucu.COZEMEDIM, soruId = "d"),
                sonuc(TekrarSonucu.RAHAT, cozumAcildi = true, soruId = "e"),
            ),
        )
        assertEquals(5, sayilar.toplam)
        assertEquals(2, sayilar.bagimsiz)
        assertEquals(1, sayilar.yarim)
        assertEquals(1, sayilar.cozemedi)
        assertEquals(1, sayilar.cozumle)
    }

    @Test
    fun `oran yukari yuvarlanir`() {
        val sayilar = KarneHesabi.hesapla(
            listOf(
                sonuc(TekrarSonucu.RAHAT, soruId = "a"),
                sonuc(TekrarSonucu.RAHAT, soruId = "b"),
                sonuc(TekrarSonucu.COZEMEDIM, soruId = "c"),
            ),
        )
        assertEquals(67, sayilar.oran)
    }

    @Test
    fun `bos oturumda oran sifirdir`() {
        val sayilar = KarneHesabi.hesapla(emptyList())
        assertEquals(0, sayilar.toplam)
        assertEquals(0, sayilar.oran)
        assertTrue(sayilar.kisaOturum)
    }

    @Test
    fun `bes sorunun altindaki oturum kisadir`() {
        val dort = KarneHesabi.hesapla(List(4) { sonuc(TekrarSonucu.RAHAT, soruId = "$it") })
        val bes = KarneHesabi.hesapla(List(5) { sonuc(TekrarSonucu.RAHAT, soruId = "$it") })
        assertTrue(dort.kisaOturum)
        assertFalse(bes.kisaOturum)
        assertEquals(TebrikSeviyesi.KISA_OTURUM, KarneHesabi.tebrikSeviyesi(dort))
    }

    @Test
    fun `tebrik seviyesi esikleri`() {
        fun oranIle(bagimsiz: Int, toplam: Int): TebrikSeviyesi {
            val sonuclar = (0 until toplam).map {
                sonuc(if (it < bagimsiz) TekrarSonucu.RAHAT else TekrarSonucu.COZEMEDIM, soruId = "$it")
            }
            return KarneHesabi.tebrikSeviyesi(KarneHesabi.hesapla(sonuclar))
        }

        assertEquals(TebrikSeviyesi.HARIKA, oranIle(10, 10))
        assertEquals(TebrikSeviyesi.HARIKA, oranIle(9, 10))
        assertEquals(TebrikSeviyesi.IYI, oranIle(7, 10))
        assertEquals(TebrikSeviyesi.ORTA, oranIle(5, 10))
        assertEquals(TebrikSeviyesi.DUSUK, oranIle(4, 10))
    }
}

class SeriHesabiTest {
    @Test
    fun `bugun calisildiysa seri bugunden sayilir`() {
        assertEquals(3, SeriHesabi.seri(listOf(100, 99, 98, 95), 100))
    }

    @Test
    fun `bugun bos ise dunku seri korunur`() {
        assertEquals(2, SeriHesabi.seri(listOf(99, 98), 100))
    }

    @Test
    fun `iki gun bosluk seriyi sifirlar`() {
        assertEquals(0, SeriHesabi.seri(listOf(98, 97), 100))
    }

    @Test
    fun `bos gecmis sifir dondurur`() {
        assertEquals(0, SeriHesabi.seri(emptyList(), 100))
    }

    @Test
    fun `tekrarlanan gunler seriyi bozmaz`() {
        assertEquals(2, SeriHesabi.seri(listOf(100, 100, 99), 100))
    }

    @Test
    fun `hafta pazartesi baslar`() {
        // 2026-10-14 Çarşamba
        val bugun = LocalDate.of(2026, 10, 14)
        val buHafta = HaftaPenceresi.buHafta(bugun)
        assertEquals(LocalDate.of(2026, 10, 12).toEpochDay().toInt(), buHafta.ilkGun)
        assertEquals(bugun.toEpochDay().toInt(), buHafta.sonGun)
    }

    @Test
    fun `gecen hafta tam yedi gun`() {
        val bugun = LocalDate.of(2026, 10, 14)
        val gecen = HaftaPenceresi.gecenHafta(bugun)
        assertEquals(6, gecen.sonGun - gecen.ilkGun)
        assertEquals(LocalDate.of(2026, 10, 5).toEpochDay().toInt(), gecen.ilkGun)
    }

    @Test
    fun `pazar gunu bu hafta pazartesiden baslar`() {
        val pazar = LocalDate.of(2026, 10, 18)
        val buHafta = HaftaPenceresi.buHafta(pazar)
        assertEquals(LocalDate.of(2026, 10, 12).toEpochDay().toInt(), buHafta.ilkGun)
    }

    @Test
    fun `son gunler eskiye dogru siralanir`() {
        val gunler = HaftaPenceresi.sonGunler(LocalDate.of(2026, 10, 14), 7)
        assertEquals(7, gunler.size)
        assertEquals(gunler.sorted(), gunler)
        assertEquals(LocalDate.of(2026, 10, 14).toEpochDay().toInt(), gunler.last())
    }
}

class HaftaDengesiTest {
    @Test
    fun `fark gecen haftaya gore hesaplanir`() {
        val denge = com.keremtulu.questionnote.domain.ilerleme.HaftaDengesi(
            buHaftaToplam = 20,
            buHaftaBagimsiz = 17,
            gecenHaftaToplam = 25,
            gecenHaftaBagimsiz = 19,
        )
        assertEquals(85, denge.buHaftaOran)
        assertEquals(76, denge.gecenHaftaOran)
        assertEquals(9, denge.fark)
        assertTrue(denge.karsilastirilabilir)
    }

    @Test
    fun `gecen hafta bes tekrardan az ise karsilastirma yapilmaz`() {
        val denge = com.keremtulu.questionnote.domain.ilerleme.HaftaDengesi(
            buHaftaToplam = 10,
            buHaftaBagimsiz = 8,
            gecenHaftaToplam = 4,
            gecenHaftaBagimsiz = 3,
        )
        assertEquals(80, denge.buHaftaOran)
        assertFalse(denge.gecenHaftaYeterli)
        assertNull(denge.fark)
        assertFalse(denge.karsilastirilabilir)
    }

    @Test
    fun `bu hafta tekrar yoksa fark hesaplanmaz`() {
        val denge = com.keremtulu.questionnote.domain.ilerleme.HaftaDengesi(
            buHaftaToplam = 0,
            buHaftaBagimsiz = 0,
            gecenHaftaToplam = 20,
            gecenHaftaBagimsiz = 15,
        )
        assertNull(denge.buHaftaOran)
        assertNull(denge.fark)
    }
}

class RozetKurallariTest {
    private fun sayilar(toplam: Int, bagimsiz: Int, cozumle: Int = 0): KarneSayilari =
        KarneSayilari(
            toplam = toplam,
            bagimsiz = bagimsiz,
            yarim = 0,
            cozemedi = toplam - bagimsiz - cozumle,
            cozumle = cozumle,
        )

    private fun girdi(
        oturum: KarneSayilari = sayilar(10, 10),
        seri: Int = 0,
        toplamBagimsiz: Int = 0,
        sonBes: List<GunDenge> = emptyList(),
    ) = RozetGirdisi(oturum, seri, toplamBagimsiz, sonBes)

    @Test
    fun `ilk karne her oturumda kazanilir`() {
        assertTrue(Rozet.ILK_KARNE in RozetKurallari.kazanilanlar(girdi(), emptySet()))
    }

    @Test
    fun `kusursuz tur bes soru ister`() {
        val kucuk = girdi(oturum = sayilar(4, 4))
        val buyuk = girdi(oturum = sayilar(5, 5))
        assertFalse(Rozet.KUSURSUZ_TUR in RozetKurallari.kazanilanlar(kucuk, emptySet()))
        assertTrue(Rozet.KUSURSUZ_TUR in RozetKurallari.kazanilanlar(buyuk, emptySet()))
    }

    @Test
    fun `kendi basina rozeti cozum acilmadiginda gelir`() {
        assertTrue(Rozet.COZUMSUZ in RozetKurallari.kazanilanlar(girdi(sayilar(6, 4)), emptySet()))
        assertFalse(
            Rozet.COZUMSUZ in RozetKurallari.kazanilanlar(girdi(sayilar(6, 4, cozumle = 1)), emptySet()),
        )
    }

    @Test
    fun `zaten sahip olunan rozet tekrar kazanilmaz`() {
        val yeni = RozetKurallari.kazanilanlar(girdi(), setOf(Rozet.ILK_KARNE))
        assertFalse(Rozet.ILK_KARNE in yeni)
    }

    @Test
    fun `istikrar bes gun ust uste yuzde yetmis ister`() {
        val iyi = (0 until 5).map { GunDenge(100 + it, toplam = 10, bagimsiz = 7) }
        val bosluk = listOf(
            GunDenge(100, 10, 10),
            GunDenge(101, 0, 0),
            GunDenge(102, 10, 10),
            GunDenge(103, 10, 10),
            GunDenge(104, 10, 10),
        )
        assertTrue(Rozet.ISTIKRAR in RozetKurallari.kazanilanlar(girdi(sonBes = iyi), emptySet()))
        assertFalse(Rozet.ISTIKRAR in RozetKurallari.kazanilanlar(girdi(sonBes = bosluk), emptySet()))
    }

    @Test
    fun `seri rozetleri esiklerde acilir`() {
        val uc = RozetKurallari.kazanilanlar(girdi(seri = 3), emptySet())
        assertTrue(Rozet.SERI_3 in uc)
        assertFalse(Rozet.SERI_7 in uc)
        assertTrue(Rozet.SERI_30 in RozetKurallari.kazanilanlar(girdi(seri = 30), emptySet()))
    }

    @Test
    fun `toplam bagimsiz esikleri`() {
        assertTrue(Rozet.YUZ_BAGIMSIZ in RozetKurallari.kazanilanlar(girdi(toplamBagimsiz = 100), emptySet()))
        assertFalse(Rozet.BES_YUZ in RozetKurallari.kazanilanlar(girdi(toplamBagimsiz = 499), emptySet()))
        assertTrue(Rozet.BES_YUZ in RozetKurallari.kazanilanlar(girdi(toplamBagimsiz = 500), emptySet()))
    }
}

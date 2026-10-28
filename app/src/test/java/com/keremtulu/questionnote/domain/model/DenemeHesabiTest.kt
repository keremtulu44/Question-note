package com.keremtulu.questionnote.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DenemeHesabiTest {
    @Test
    fun bosVeNetKurallari() {
        val satir = DenemeDersSatiri("m", soruSayisi = 40, dogru = 30, yanlis = 8)
        assertEquals(2, satir.bos)
        assertEquals(10, satir.yuklenecek)
        assertEquals(28.0, NetKurali.DORTTE_BIR.net(30, 8), 0.001)
        assertEquals(30.0 - 8.0 / 3.0, NetKurali.UCTE_BIR.net(30, 8), 0.001)
        assertEquals(30.0, NetKurali.YOK.net(30, 8), 0.001)
        assertTrue(satir.gecerli())
        assertFalse(DenemeDersSatiri("m", 10, 8, 4).gecerli())
    }

    @Test
    fun grafikVeDamga() {
        val a = DenemeKaydi("1", "Miray 1", NetKurali.DORTTE_BIR, 1, 10, listOf(DenemeDersSatiri("m", 40, 20, 8)))
        val b = DenemeKaydi("2", "Miray 2", NetKurali.DORTTE_BIR, 2, 20, listOf(DenemeDersSatiri("m", 40, 28, 4)))
        val liste = listOf(b, a)
        assertEquals(listOf(a, b), liste.grafikNoktalari())
        assertEquals(a, liste.kaynakla("Miray 1"))
        assertEquals(a, liste.oncekiAyniKural(b))
        val bar = liste.dersBarlari(b).single()
        assertEquals(NetKurali.DORTTE_BIR.net(28, 4), bar.sonNet, 0.001)
        assertEquals(NetKurali.DORTTE_BIR.net(20, 8), bar.oncekiOrtalama!!, 0.001)
        assertEquals(0, denemeHatirlatmaIndeksi("x") % 4)
    }

    @Test
    fun oncekiAyniKuralEnEskiDenemeIcinGelecektekiDenemeyiSecmez() {
        val enEski = DenemeKaydi("1", "Deneme 1", NetKurali.DORTTE_BIR, 1L, 100, listOf(DenemeDersSatiri("m", 40, 20, 4)))
        val dahaYeni = DenemeKaydi("2", "Deneme 2", NetKurali.DORTTE_BIR, 2L, 105, listOf(DenemeDersSatiri("m", 40, 30, 4)))
        val liste = listOf(enEski, dahaYeni)
        assertNull(liste.oncekiAyniKural(enEski))
        assertEquals(enEski, liste.oncekiAyniKural(dahaYeni))
    }

    @Test
    fun benzerDenemelerKuralVeTumDerslerinSoruSayisiniEsitIster() {
        val baz = DenemeKaydi("1", "Birinci", NetKurali.DORTTE_BIR, 1, 10, listOf(
            DenemeDersSatiri("mat", 40, 20, 4), DenemeDersSatiri("tur", 40, 25, 4),
        ))
        val sirasiFarkli = baz.copy(id = "2", gun = 11, satirlar = baz.satirlar.reversed())
        val bosSatirli = baz.copy(id = "3", gun = 12, satirlar = baz.satirlar + DenemeDersSatiri("fen", 0, 0, 0))
        val kuralFarkli = baz.copy(id = "4", kural = NetKurali.UCTE_BIR)
        val soruSayisiFarkli = baz.copy(id = "5", satirlar = listOf(
            DenemeDersSatiri("mat", 20, 15, 2), DenemeDersSatiri("tur", 40, 25, 4),
        ))
        val dersFarkli = baz.copy(id = "6", satirlar = listOf(
            DenemeDersSatiri("mat", 40, 20, 4), DenemeDersSatiri("fen", 40, 25, 4),
        ))
        val gecersiz = baz.copy(id = "7", satirlar = listOf(DenemeDersSatiri("mat", 40, 35, 10)))
        val tekrarliDers = baz.copy(id = "8", satirlar = baz.satirlar + baz.satirlar.first())
        val tumu = listOf(soruSayisiFarkli, baz, dersFarkli, kuralFarkli, sirasiFarkli, bosSatirli, gecersiz, tekrarliDers)

        assertEquals(listOf(baz, sirasiFarkli, bosSatirli), tumu.karsilastirilabilirler(bosSatirli))
        val temsilciler = tumu.kapsamTemsilcileri()
        assertTrue(bosSatirli in temsilciler)
        assertFalse(baz in temsilciler)
        assertTrue(soruSayisiFarkli in temsilciler)
        assertTrue(dersFarkli in temsilciler)
        assertEquals(sirasiFarkli, tumu.oncekiKarsilastirilabilir(bosSatirli))
        assertFalse(baz.ayniKapsamVeKural(kuralFarkli))
        assertFalse(baz.ayniKapsamVeKural(soruSayisiFarkli))
        assertFalse(baz.ayniKapsamVeKural(dersFarkli))
        assertFalse(baz.ayniKapsamVeKural(gecersiz))
        assertFalse(baz.ayniKapsamVeKural(tekrarliDers))
    }

    @Test
    fun dersOrtalamasiYalnizBenzerVeGercektenOncekiKayitlariKullanir() {
        val eski = DenemeKaydi("a", "Eski", NetKurali.DORTTE_BIR, 1, 100, listOf(DenemeDersSatiri("m", 40, 20, 4)))
        val son = eski.copy(id = "b", ad = "Seçilen", olusturma = 2, gun = 105,
            satirlar = listOf(DenemeDersSatiri("m", 40, 30, 4)))
        val gelecek = son.copy(id = "c", gun = 110, satirlar = listOf(DenemeDersSatiri("m", 40, 40, 0)))
        val ayniGunOnce = son.copy(id = "d", olusturma = 1, satirlar = listOf(DenemeDersSatiri("m", 40, 24, 4)))
        val ayniGunSonra = son.copy(id = "e", olusturma = 3, satirlar = listOf(DenemeDersSatiri("m", 40, 40, 0)))
        val uyumsuz = son.copy(id = "f", gun = 103, satirlar = listOf(DenemeDersSatiri("m", 20, 10, 4)))
        val tumu = listOf(gelecek, uyumsuz, son, ayniGunSonra, eski, ayniGunOnce)

        val bar = tumu.dersBarlari(son).single()
        assertEquals(21.0, bar.oncekiOrtalama!!, 0.001) // (19 + 23) / 2
        assertEquals(2, bar.oncekiDenemeSayisi)
        assertEquals(ayniGunOnce, tumu.oncekiKarsilastirilabilir(son))
        assertNull(tumu.oncekiKarsilastirilabilir(eski))
        assertNull(tumu.dersBarlari(eski).single().oncekiOrtalama)
    }

    @Test
    fun denemeAdlariKaynakBaglantisiniKaristirmadanDogrulanir() {
        val a = DenemeKaydi("a", "Miray TYT 1", NetKurali.DORTTE_BIR, 1, 1, listOf(DenemeDersSatiri("m", 40, 20, 1)))
        val b = a.copy(id = "b", ad = "İkinci")
        val liste = listOf(a, b)
        assertEquals(DenemeAdiDurumu.BOS, liste.denemeAdiDogrula("  ", null))
        assertEquals(DenemeAdiDurumu.AYNI_AD_VAR, liste.denemeAdiDogrula("  miray   tyt 1 ", null))
        assertEquals(DenemeAdiDurumu.AYNI_AD_VAR, liste.denemeAdiDogrula("miray tyt 1", "b"))
        assertEquals(DenemeAdiDurumu.GECERLI, liste.denemeAdiDogrula("Miray TYT 1", "a"))
        assertEquals(DenemeAdiDurumu.COK_UZUN, liste.denemeAdiDogrula("a".repeat(121), null))

        val eskiUzun = a.copy(ad = "a".repeat(121))
        assertEquals(DenemeAdiDurumu.GECERLI, listOf(eskiUzun).denemeAdiDogrula(eskiUzun.ad, eskiUzun.id))
        val yinelenen = listOf(a, a.copy(id = "c", ad = "miray  tyt 1"))
        assertEquals(DenemeAdiDurumu.GECERLI, yinelenen.denemeAdiDogrula(a.ad, a.id))
        assertEquals(DenemeAdiDurumu.ESKI_AD_PAYLASILMIS, yinelenen.denemeAdiDogrula("Yeni isim", a.id))
    }

    @Test
    fun tasmaIleGecersizSatirGecerliGorunmez() {
        val satir = DenemeDersSatiri("m", Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
        assertFalse(satir.gecerli())
        assertEquals(0, satir.bos)
        assertEquals(Int.MAX_VALUE, satir.yuklenecek)
    }

    @Test
    fun derslerArasiToplamYuklenecekTamsayiSiniriniAsabilir() {
        val deneme = DenemeKaydi(
            "buyuk", "Eski kayıt", NetKurali.YOK, 1, 1,
            listOf(
                DenemeDersSatiri("a", Int.MAX_VALUE, 0, 0),
                DenemeDersSatiri("b", Int.MAX_VALUE, 0, 0),
            ),
        )
        assertEquals(2L * Int.MAX_VALUE, deneme.toplamYuklenecek)
    }

    @Test
    fun kaynaklaNormalizeEdilmisDenemeAdiniEslerVeNegatifNetBarlariniKorur() {
        val deneme = DenemeKaydi(
            id = "d1",
            ad = "Miray TYT 1",
            kural = NetKurali.DORTTE_BIR,
            olusturma = 10L,
            gun = 120,
            satirlar = listOf(
                DenemeDersSatiri("mat", soruSayisi = 40, dogru = 0, yanlis = 8, dersAdi = "Matematik"),
            ),
        )
        val bulunan = listOf(deneme).kaynakla("  miray   tyt 1 ")
        assertNotNull(bulunan)
        assertEquals("d1", bulunan!!.id)

        val barlar = listOf(deneme).dersBarlari(deneme)
        assertEquals(1, barlar.size)
        assertEquals(-2.0, barlar.single().sonNet, 0.001)
        assertEquals("Matematik", barlar.single().dersAdi)
    }
}

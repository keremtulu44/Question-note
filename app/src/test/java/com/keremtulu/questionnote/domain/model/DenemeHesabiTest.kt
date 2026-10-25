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

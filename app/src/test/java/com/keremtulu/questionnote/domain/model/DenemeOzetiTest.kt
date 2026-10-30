package com.keremtulu.questionnote.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DenemeOzetiTest {
    private fun kayit(
        id: String,
        net: Pair<Int, Int>,
        gun: Int,
        kural: NetKurali = NetKurali.DORTTE_BIR,
        dersId: String = "m",
        dersAdi: String = "Matematik",
    ) = DenemeKaydi(
        id = id,
        ad = "Deneme $id",
        kural = kural,
        olusturma = gun.toLong(),
        gun = gun,
        satirlar = listOf(DenemeDersSatiri(dersId, 40, net.first, net.second, dersAdi)),
    )

    @Test
    fun bosListeUydurmaSayiUretmez() {
        val ozet = emptyList<DenemeKaydi>().ozet()
        assertEquals(0, ozet.kayitSayisi)
        assertNull(ozet.ortalamaNet)
        assertNull(ozet.enIyiNet)
        assertFalse(ozet.karsilastirilabilir)
    }

    @Test
    fun ayniKuraldaOrtalamaEnIyiVeFark() {
        val liste = listOf(
            kayit("a", 20 to 8, 10),
            kayit("b", 24 to 8, 20),
            kayit("c", 28 to 4, 30),
        )
        val ozet = liste.ozet()
        assertTrue(ozet.karsilastirilabilir)
        assertEquals(3, ozet.kayitSayisi)
        // 18, 22, 27
        assertEquals(22.333, ozet.ortalamaNet!!, 0.01)
        assertEquals(27.0, ozet.enIyiNet!!, 0.001)
        assertEquals(27.0, ozet.sonNet!!, 0.001)
        assertEquals(5.0, ozet.oncekiIleFark!!, 0.001)
    }

    @Test
    fun girisSirasiDegilTarihEsasAlinir() {
        val eski = kayit("a", 20 to 8, 10)
        val yeni = kayit("b", 28 to 4, 30)
        val ozet = listOf(yeni, eski).ozet()
        assertEquals(27.0, ozet.sonNet!!, 0.001)
        assertEquals(9.0, ozet.oncekiIleFark!!, 0.001)
    }

    @Test
    fun karisikKuraldaOrtalamaVeEnIyiNetYok() {
        val liste = listOf(
            kayit("a", 20 to 8, 10),
            kayit("b", 24 to 8, 20, kural = NetKurali.UCTE_BIR),
        )
        val ozet = liste.ozet()
        assertFalse(ozet.karsilastirilabilir)
        assertNull(ozet.ortalamaNet)
        assertNull(ozet.enIyiNet)
        assertNull(ozet.oncekiIleFark)
        assertEquals(2, ozet.kayitSayisi)
        assertEquals(24.0 - 8.0 / 3.0, ozet.sonNet!!, 0.001)
    }

    @Test
    fun enZayifDersTekDersliDenemedeUretilmez() {
        val tekDers = kayit("a", 20 to 8, 10)
        assertNull(tekDers.enZayifDers(mapOf("m" to "Matematik")))

        val cokDers = DenemeKaydi(
            id = "b",
            ad = "Miray 3",
            kural = NetKurali.DORTTE_BIR,
            olusturma = 30,
            gun = 30,
            satirlar = listOf(
                DenemeDersSatiri("m", 40, 30, 4, "Matematik"),
                DenemeDersSatiri("f", 20, 8, 8, "Fen Bilimleri"),
            ),
        )
        val zayif = cokDers.enZayifDers(mapOf("m" to "Matematik", "f" to "Fen Bilimleri"))
        assertEquals("Fen Bilimleri", zayif!!.first)
        assertEquals(6.0, zayif.second, 0.001)
    }

    @Test
    fun dersAdiYoksaKayitliAdKullanilir() {
        val cokDers = DenemeKaydi(
            id = "c",
            ad = "Miray 4",
            kural = NetKurali.YOK,
            olusturma = 40,
            gun = 40,
            satirlar = listOf(
                DenemeDersSatiri("m", 40, 30, 4, "Matematik"),
                DenemeDersSatiri("x", 20, 10, 0, "Seçmeli"),
            ),
        )
        val zayif = cokDers.enZayifDers(emptyMap())
        assertEquals("Seçmeli", zayif!!.first)
        assertEquals(10.0, zayif.second, 0.001)
    }
}

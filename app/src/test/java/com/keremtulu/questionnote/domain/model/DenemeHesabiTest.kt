package com.keremtulu.questionnote.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
}

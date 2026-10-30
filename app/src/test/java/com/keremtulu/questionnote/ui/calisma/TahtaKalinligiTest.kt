package com.keremtulu.questionnote.ui.calisma

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tahta kalınlığı sözleşmesi: 1–10 dp aralığı, 1'er adım.
 * Arayüz kaydırıcısı, kırpma ve yuvarlama bu tek fonksiyondan geçer; böylece farklı yerlerde
 * farklı sınır kullanılması (ör. 0 dp → görünmez çizgi) engellenir.
 */
class TahtaKalinligiTest {
    @Test
    fun sinirlarKirpilir() {
        assertEquals(1f, tahtaKalinligiSinirla(-40f), 0.001f)
        assertEquals(1f, tahtaKalinligiSinirla(0f), 0.001f)
        assertEquals(10f, tahtaKalinligiSinirla(10.4f), 0.001f)
        assertEquals(10f, tahtaKalinligiSinirla(999f), 0.001f)
    }

    @Test
    fun araDegerlerEnYakinAdimaYuvarlanir() {
        assertEquals(3f, tahtaKalinligiSinirla(2.5f), 0.001f)
        assertEquals(4f, tahtaKalinligiSinirla(3.6f), 0.001f)
        assertEquals(7f, tahtaKalinligiSinirla(6.9f), 0.001f)
    }

    @Test
    fun sonluOlmayanDegerVarsayilanaDoner() {
        // roundToInt() NaN'da istisna fırlatır; kaydırıcı hiç NaN üretmese de bu kapı kapalı kalmalı.
        assertEquals(VARSAYILAN_KALINLIK_DP, tahtaKalinligiSinirla(Float.NaN), 0.001f)
        assertEquals(VARSAYILAN_KALINLIK_DP, tahtaKalinligiSinirla(Float.POSITIVE_INFINITY), 0.001f)
        assertEquals(VARSAYILAN_KALINLIK_DP, tahtaKalinligiSinirla(Float.NEGATIVE_INFINITY), 0.001f)
    }

    @Test
    fun varsayilanDegerAraliginIcindedir() {
        assertEquals(VARSAYILAN_KALINLIK_DP, tahtaKalinligiSinirla(VARSAYILAN_KALINLIK_DP), 0.001f)
        assertEquals(1f, TAHTA_MIN_KALINLIK_DP, 0.001f)
        assertEquals(10f, TAHTA_MAKS_KALINLIK_DP, 0.001f)
    }
}

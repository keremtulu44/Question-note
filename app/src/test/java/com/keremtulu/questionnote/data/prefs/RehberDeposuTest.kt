package com.keremtulu.questionnote.data.prefs

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Rehber durumu cihazda kalır: tercih dosyası ayrıdır, ZIP yedeğine girmez ve geri yükleme
 * akışından etkilenmez. Yeni cihazda rehber yeniden açılır; bu test o sözleşmeyi korur.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RehberDeposuTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Before
    fun temizle() {
        context.getSharedPreferences("rehber_durumu", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun ilkAcilistaGorulmediVeIlkBolum() {
        val depo = RehberDeposu(context)
        assertFalse(depo.gorulduMu())
        assertEquals(0, depo.sonBolum())
    }

    @Test
    fun kalinanBolumYenidenOlusturulanDepodaKorunur() {
        RehberDeposu(context).bolumuKaydet(4)
        val yeni = RehberDeposu(context)
        assertEquals(4, yeni.sonBolum())
        // Bölüm kaydetmek "görüldü" demek değildir: yarıda bırakan kullanıcı devam eder.
        assertFalse(yeni.gorulduMu())
    }

    @Test
    fun negatifBolumSifiraKirpilir() {
        RehberDeposu(context).bolumuKaydet(-3)
        assertEquals(0, RehberDeposu(context).sonBolum())
    }

    @Test
    fun tamamlaGorulduyuIsaretlerVeBasiDoner() {
        val depo = RehberDeposu(context)
        depo.bolumuKaydet(5)
        depo.tamamla()
        assertTrue(depo.gorulduMu())
        assertEquals(0, depo.sonBolum())
        // Yeni süreç/cihaz yeniden açılışında da otomatik açılış tekrarlanmaz.
        assertTrue(RehberDeposu(context).gorulduMu())
    }

    @Test
    fun bastanBaslatTekrarAcildigindaOtomatikAcilisiEngeller() {
        val depo = RehberDeposu(context)
        depo.bastanBaslat()
        assertTrue(depo.gorulduMu())
        assertEquals(0, depo.sonBolum())
    }

    @Test
    fun tamamlamaSonrasiBolumKaydiDevamEder() {
        val depo = RehberDeposu(context)
        depo.tamamla()
        depo.bolumuKaydet(2)
        assertTrue(depo.gorulduMu())
        assertEquals(2, depo.sonBolum())
    }

    @Test
    fun sifirlaHerSeyiGeriAlir() {
        val depo = RehberDeposu(context)
        depo.bolumuKaydet(3)
        depo.tamamla()
        depo.sifirla()
        assertFalse(depo.gorulduMu())
        assertEquals(0, depo.sonBolum())
    }
}

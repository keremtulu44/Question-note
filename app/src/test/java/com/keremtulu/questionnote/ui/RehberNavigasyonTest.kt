package com.keremtulu.questionnote.ui

import androidx.compose.runtime.saveable.SaverScope
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Rehber, gezinme yığınının bir parçasıdır: süreç ölürse kaldığı bölümden geri gelmeli,
 * döneceği ekranı da unutmamalı. Kayıt biçimi bozulursa sessizce Bugün'e düşmelidir.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RehberNavigasyonTest {

    private val kapsam = object : SaverScope {
        override fun canBeSaved(value: Any): Boolean = true
    }

    private fun kaydetVeGeriOku(ekran: UygulamaEkrani): UygulamaEkrani {
        val metin = with(UygulamaEkraniSaver) { kapsam.save(ekran) }
            ?: error("ekran kaydedilemedi: $ekran")
        return with(UygulamaEkraniSaver) { restore(metin) } ?: error("ekran geri okunamadı")
    }

    @Test
    fun rehberBolumuVeDonusHedefiKorunur() {
        val ekran = UygulamaEkrani.Rehber(baslangic = 4, geri = UygulamaEkrani.Ayarlar)
        assertEquals(ekran, kaydetVeGeriOku(ekran))
    }

    @Test
    fun rehberIlkKurulumdaBuguneDoner() {
        val ekran = UygulamaEkrani.Rehber()
        assertEquals(UygulamaEkrani.Bugun, ekran.geri)
        assertEquals(ekran, kaydetVeGeriOku(ekran))
    }

    @Test
    fun rehberDerinGeriZinciriniTasir() {
        val ekran = UygulamaEkrani.Rehber(
            baslangic = 2,
            geri = UygulamaEkrani.DenemeDetay("deneme-7"),
        )
        assertEquals(ekran, kaydetVeGeriOku(ekran))
    }

    @Test
    fun bilinmeyenKayitBuguneDuser() {
        val metin = JSONObject().put("t", "taninmayan-ekran").toString()
        val ekran = with(UygulamaEkraniSaver) { restore(metin) }
        assertEquals(UygulamaEkrani.Bugun, ekran)
    }

    @Test
    fun bozukKayitBuguneDuser() {
        val ekran = with(UygulamaEkraniSaver) { restore("{bozuk") }
        assertEquals(UygulamaEkrani.Bugun, ekran)
    }
}

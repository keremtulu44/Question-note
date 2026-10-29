package com.keremtulu.questionnote.data.prefs

import android.content.Context

/**
 * Rehberin cihazdaki durumu. Bu bir *tercih*tir, veri değildir: ZIP yedeğine girmez ve
 * geri yükleme akışından etkilenmez. Yeni cihazda rehber yeniden açılır (istenen davranış).
 */
class RehberDeposu(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(DOSYA, Context.MODE_PRIVATE)

    /** Rehber en az bir kez sonuna kadar (veya "Atla" ile) kapatıldı mı? */
    fun gorulduMu(): Boolean = prefs.getBoolean(GORULDU, false)

    /** Kaldığı bölüm. Aralık dışı/eski bir değer gelirse 0 döner. */
    fun sonBolum(): Int = prefs.getInt(SON_BOLUM, 0).coerceAtLeast(0)

    /** Bölüm değiştikçe çağrılır; süreç ölürse kaldığı yerden devam eder. */
    fun bolumuKaydet(indeks: Int) {
        prefs.edit().putInt(SON_BOLUM, indeks.coerceAtLeast(0)).apply()
    }

    /** "Atla" da tamamlanmış sayılır: otomatik açılış bir daha olmaz. */
    fun tamamla() {
        prefs.edit().putBoolean(GORULDU, true).putInt(SON_BOLUM, 0).apply()
    }

    /** Ayarlar'dan yeniden açınca baştan başlar; otomatik açılış yine tetiklenmez. */
    fun bastanBaslat() {
        prefs.edit().putBoolean(GORULDU, true).putInt(SON_BOLUM, 0).apply()
    }

    /** Test ve "ilk kurulum" senaryosu için: her şeyi sıfırlar. */
    internal fun sifirla() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val DOSYA = "rehber_durumu"
        const val GORULDU = "goruldu"
        const val SON_BOLUM = "son_bolum"
    }
}

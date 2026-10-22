package com.keremtulu.questionnote.data.prefs

import android.content.Context

/** Short feedback sounds (review saved, daily target, celebration). User toggle; default on. */
class SesEfektiDeposu(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(DOSYA, Context.MODE_PRIVATE)

    fun acikMi(): Boolean = prefs.getBoolean(ANAHTAR, true)

    fun ayarla(acik: Boolean) {
        prefs.edit().putBoolean(ANAHTAR, acik).apply()
    }

    companion object {
        private const val DOSYA = "ses_efektleri"
        private const val ANAHTAR = "acik"
    }
}

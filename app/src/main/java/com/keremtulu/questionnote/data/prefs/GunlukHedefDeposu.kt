package com.keremtulu.questionnote.data.prefs

import android.content.Context

/**
 * Daily review target. Fixed for now (design decision K27/S6): onboarding selection and a settings
 * screen arrive in a later release. Keeping the read behind this interface means only the
 * implementation changes later.
 */
interface GunlukHedefDeposu {
    fun hedef(): Int
}

class SabitGunlukHedefDeposu(
    private val deger: Int = VARSAYILAN_HEDEF,
) : GunlukHedefDeposu {
    override fun hedef(): Int = deger

    companion object {
        const val VARSAYILAN_HEDEF = 10
    }
}

/** User-selected target, stored locally. Missing or corrupt values fall back to [SabitGunlukHedefDeposu.VARSAYILAN_HEDEF]. */
class TercihGunlukHedefDeposu(context: Context) : GunlukHedefDeposu {
    private val prefs = context.applicationContext.getSharedPreferences(DOSYA, Context.MODE_PRIVATE)

    override fun hedef(): Int =
        prefs.getInt(ANAHTAR, SabitGunlukHedefDeposu.VARSAYILAN_HEDEF)
            .takeIf { it in SECENEKLER } ?: SabitGunlukHedefDeposu.VARSAYILAN_HEDEF

    fun hedefiAyarla(deger: Int) {
        if (deger !in SECENEKLER) return
        prefs.edit().putInt(ANAHTAR, deger).apply()
    }

    companion object {
        private const val DOSYA = "ilerleme_ayarlari"
        private const val ANAHTAR = "gunluk_hedef"
        val SECENEKLER = listOf(5, 10, 15, 20, 30)
    }
}

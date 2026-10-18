package com.keremtulu.questionnote.data.prefs

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

/** Daily review target. The settings-backed implementation supports 5/10/15/20/30. */
interface GunlukHedefDeposu {
    fun hedef(): Int
    fun izle(): Flow<Int> = flowOf(hedef())
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

    private val guncelHedef = MutableStateFlow(hedef())
    override fun izle(): Flow<Int> = guncelHedef.asStateFlow()

    override fun hedef(): Int =
        prefs.getInt(ANAHTAR, SabitGunlukHedefDeposu.VARSAYILAN_HEDEF)
            .takeIf { it in SECENEKLER } ?: SabitGunlukHedefDeposu.VARSAYILAN_HEDEF

    fun hedefiAyarla(deger: Int) {
        if (deger !in SECENEKLER) return
        prefs.edit().putInt(ANAHTAR, deger).apply()
        guncelHedef.value = deger
    }

    companion object {
        private const val DOSYA = "ilerleme_ayarlari"
        private const val ANAHTAR = "gunluk_hedef"
        val SECENEKLER = listOf(5, 10, 15, 20, 30)
    }
}

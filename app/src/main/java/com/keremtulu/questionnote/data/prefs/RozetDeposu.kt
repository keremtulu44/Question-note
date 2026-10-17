package com.keremtulu.questionnote.data.prefs

import android.content.Context
import com.keremtulu.questionnote.domain.karne.Rozet

/**
 * Device-local badge storage. Badges are intentionally excluded from the ZIP backup: they can be
 * re-earned and must not change the backup format.
 */
class RozetDeposu(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(DOSYA, Context.MODE_PRIVATE)

    fun sahipOlanlar(): Set<Rozet> =
        prefs.getStringSet(ANAHTAR, emptySet())
            .orEmpty()
            .mapNotNull { Rozet.anahtarla(it) }
            .toSet()

    /** Adds badges; already-owned ones are ignored so awarding stays idempotent. */
    fun kaydet(rozetler: Collection<Rozet>) {
        if (rozetler.isEmpty()) return
        val guncel = sahipOlanlar() + rozetler
        prefs.edit().putStringSet(ANAHTAR, guncel.map { it.anahtar }.toSet()).apply()
    }

    private companion object {
        const val DOSYA = "karne_rozetleri"
        const val ANAHTAR = "kazanilanlar"
    }
}

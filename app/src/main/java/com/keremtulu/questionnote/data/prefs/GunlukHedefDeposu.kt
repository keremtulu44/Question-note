package com.keremtulu.questionnote.data.prefs

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

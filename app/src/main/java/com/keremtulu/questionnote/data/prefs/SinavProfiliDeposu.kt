package com.keremtulu.questionnote.data.prefs

import android.content.Context
import com.keremtulu.questionnote.domain.model.SinavProfili
import com.keremtulu.questionnote.domain.model.SinavSecimi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Stored separately from the question backup. Selection is published only after seeding succeeds. */
class SinavProfiliDeposu(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("sinav_profili", Context.MODE_PRIVATE)
    private val secim = MutableStateFlow(
        SinavSecimi(
            profil = SinavProfili.anahtarla(prefs.getString("secili", null)),
            dersIdleri = prefs.getStringSet("dersler", emptySet()).orEmpty().toSet(),
        ),
    )
    val secimIzle: StateFlow<SinavSecimi> = secim.asStateFlow()
    fun secili(): SinavProfili = secim.value.profil

    fun kaydet(profil: SinavProfili, dersIdleri: Set<String>) {
        check(prefs.edit().putString("secili", profil.name).putStringSet("dersler", dersIdleri).commit())
        secim.value = SinavSecimi(profil, dersIdleri.toSet())
    }
}

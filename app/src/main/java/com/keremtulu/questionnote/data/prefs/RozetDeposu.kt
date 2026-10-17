package com.keremtulu.questionnote.data.prefs

import android.content.Context
import com.keremtulu.questionnote.domain.karne.Rozet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Device-local ownership AND durable celebration queue. Neither is included in ZIP backups. */
class RozetDeposu(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(DOSYA, Context.MODE_PRIVATE)
    private val sahip = MutableStateFlow(oku(ANAHTAR))
    private val bekleyen = MutableStateFlow(sirala(oku(BEKLEYEN)))
    val sahipIzle: StateFlow<Set<Rozet>> = sahip.asStateFlow()
    val bekleyenIzle: StateFlow<List<Rozet>> = bekleyen.asStateFlow()

    private fun oku(anahtar: String): Set<Rozet> = prefs.getStringSet(anahtar, emptySet())
        .orEmpty().mapNotNull(Rozet::anahtarla).toSet()
    private fun sirala(rozetler: Set<Rozet>): List<Rozet> = Rozet.entries.filter { it in rozetler }
    fun sahipOlanlar(): Set<Rozet> = sahip.value
    fun kazanilmaZamani(rozet: Rozet): Long? = prefs.getLong("zaman_${rozet.anahtar}", 0L).takeIf { it > 0 }

    /** Atomic read-modify-write: parallel aggregate/session checks cannot award twice. */
    @Synchronized
    fun kaydet(rozetler: Collection<Rozet>): List<Rozet> {
        val yeni = rozetler.distinct().filterNot { it in sahip.value }
        if (yeni.isEmpty()) return emptyList()
        val guncel = sahip.value + yeni
        val kuyruk = sirala(bekleyen.value.toSet() + yeni)
        val editor = prefs.edit()
            .putStringSet(ANAHTAR, guncel.map { it.anahtar }.toSet())
            .putStringSet(BEKLEYEN, kuyruk.map { it.anahtar }.toSet())
        val now = System.currentTimeMillis()
        yeni.forEach { editor.putLong("zaman_${it.anahtar}", now) }
        check(editor.commit()) { "Rozet kaydedilemedi" }
        sahip.value = guncel
        bekleyen.value = kuyruk
        return yeni
    }

    /** Called only when the user dismisses the fullscreen reward, not on recomposition. */
    @Synchronized
    fun kutlamayiTamamla(rozet: Rozet) {
        val kalan = bekleyen.value.filterNot { it == rozet }
        check(prefs.edit().putStringSet(BEKLEYEN, kalan.map { it.anahtar }.toSet()).commit())
        bekleyen.value = kalan
    }

    private companion object {
        const val DOSYA = "karne_rozetleri"
        const val ANAHTAR = "kazanilanlar"
        const val BEKLEYEN = "kutlama_bekleyenler"
    }
}

package com.keremtulu.questionnote.data.prefs

import android.content.Context
import com.keremtulu.questionnote.domain.seviye.Rutbe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

data class RutbeKutlamasi(val onceki: Rutbe, val son: Rutbe) {
    val gecilenEsik: Int get() = son.ordinal - onceki.ordinal
}

/** Cosmetic, device-local seen/high-water/one-item queue. It cannot award XP or modify history. */
class RutbeDeposu(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("rutbe_kutlamalari", Context.MODE_PRIVATE)
    private data class Durum(val ust: Int, val gorulen: Int, val hedef: Int, val esitle: Boolean, val bekleyen: RutbeKutlamasi?)
    private var durum: Durum? = runCatching {
        val j = JSONObject(prefs.getString("durum", null) ?: return@runCatching null)
        require(j.getInt("version") == 1)
        val ust = j.getInt("high"); val seen = j.getInt("seen")
        require(ust in Rutbe.entries.indices && seen in 0..ust)
        val p = if (j.isNull("pending_to")) null else {
            val from = j.getInt("pending_from"); val to = j.getInt("pending_to")
            require(from in Rutbe.entries.indices && to in (from + 1)..ust)
            RutbeKutlamasi(Rutbe.entries[from], Rutbe.entries[to])
        }
        Durum(ust, seen, j.getInt("goal"), j.getBoolean("baseline_required"), p)
    }.getOrNull()
    private val bekleyen = MutableStateFlow(durum?.bekleyen)
    val bekleyenIzle = bekleyen.asStateFlow()

    @Synchronized
    fun guncelle(rutbe: Rutbe, hedef: Int, kutla: Boolean) {
        val eski = durum
        val n = rutbe.ordinal
        val yeni = when {
            eski == null -> Durum(n, n, hedef, false, null) // First install/migration is a baseline, not a win.
            eski.esitle || eski.hedef != hedef -> Durum(maxOf(eski.ust, n), maxOf(eski.gorulen, n), hedef, false, null)
            !kutla -> eski.copy(ust = maxOf(eski.ust, n), gorulen = maxOf(eski.gorulen, n),
                bekleyen = eski.bekleyen?.takeIf { it.son.ordinal <= n })
            n > eski.ust -> eski.copy(ust = n, bekleyen = RutbeKutlamasi(eski.bekleyen?.onceki ?: Rutbe.entries[eski.ust], rutbe))
            else -> eski.copy(bekleyen = eski.bekleyen?.takeIf { it.son.ordinal <= n })
        }
        kaydet(yeni)
    }

    @Synchronized
    fun kutlamayiTamamla(gosterilen: RutbeKutlamasi) {
        val eski = durum ?: return
        val simdi = eski.bekleyen ?: return
        // A stale dismiss callback must not consume a newer, higher queued threshold.
        if (gosterilen.son.ordinal < simdi.onceki.ordinal) return
        val gorulen = maxOf(eski.gorulen, minOf(gosterilen.son.ordinal, simdi.son.ordinal))
        val kalan = if (gosterilen.son.ordinal >= simdi.son.ordinal) null else RutbeKutlamasi(gosterilen.son, simdi.son)
        kaydet(eski.copy(gorulen = gorulen, bekleyen = kalan))
    }

    /** Called after a verified restore commit, before its recovery marker is removed; idempotent. */
    @Synchronized
    fun geriYuklemeSonrasi() {
        val eski = durum ?: Durum(0, 0, 10, true, null)
        kaydet(eski.copy(esitle = true, bekleyen = null))
    }

    private fun kaydet(yeni: Durum) {
        if (yeni == durum) return
        val j = JSONObject().put("version", 1).put("high", yeni.ust).put("seen", yeni.gorulen).put("goal", yeni.hedef)
            .put("baseline_required", yeni.esitle).put("pending_from", yeni.bekleyen?.onceki?.ordinal ?: JSONObject.NULL)
            .put("pending_to", yeni.bekleyen?.son?.ordinal ?: JSONObject.NULL)
        check(prefs.edit().putString("durum", j.toString()).commit())
        durum = yeni
        bekleyen.value = yeni.bekleyen
    }
}

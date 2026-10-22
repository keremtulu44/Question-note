package com.keremtulu.questionnote.domain.calisma

import com.keremtulu.questionnote.domain.karne.OturumSonucu
import com.keremtulu.questionnote.domain.model.TekrarKaydi

/** Compact checkpoint: only IDs and answers, never photos/notes in saved state. */
data class OdakOturumu(
    val id: String,
    val soruIdleri: List<String>,
    val baslamaZamani: Long,
    val bugunden: Boolean = false,
    val sira: Int = 0,
    val sonuclar: List<OturumSonucu> = emptyList(),
    val cozumAcilanlar: Set<String> = emptySet(),
    val olcumler: Map<String, OlcumDevamNoktasi> = emptyMap(),
    val olcumDestekli: Boolean = true,
) {
    val bitti: Boolean get() = sira >= soruIdleri.size
    val simdikiId: String? get() = soruIdleri.getOrNull(sira)

    fun cevapla(sonuc: OturumSonucu): OdakOturumu {
        if (sonuc.soruId != simdikiId || sonuclar.any { it.soruId == sonuc.soruId }) return this
        return copy(sira = sira + 1, sonuclar = sonuclar + sonuc.copy(
            cozumAcildi = sonuc.cozumAcildi || sonuc.soruId in cozumAcilanlar,
        ))
    }

    fun atla(): OdakOturumu = if (bitti) this else copy(sira = sira + 1)

    fun sonaBirak(): OdakOturumu {
        val id = simdikiId ?: return this
        if (sira == soruIdleri.lastIndex) return this
        val yeni = soruIdleri.toMutableList().apply { removeAt(sira); add(id) }
        return copy(soruIdleri = yeni)
    }

    fun cozumuAc(): OdakOturumu = simdikiId?.let { copy(cozumAcilanlar = cozumAcilanlar + it) } ?: this

    /** Reconcile a process death between the Room commit and the preference checkpoint. */
    fun kurtar(mevcutIdler: Set<String>, kayitlar: Map<String, TekrarKaydi>): OdakOturumu {
        val verilenler = sonuclar.associateBy { it.soruId }.toMutableMap()
        soruIdleri.drop(sira).forEach { id ->
            kayitlar[id]?.takeIf { kayit ->
                val deneme = olcumler[id]?.denemeId
                when {
                    deneme != null && kayit.denemeId != null -> kayit.denemeId == deneme && (kayit.oturumId == null || kayit.oturumId == this.id)
                    kayit.oturumId != null -> kayit.oturumId == this.id
                    else -> kayit.zaman >= baslamaZamani
                }
            }?.let {
                verilenler.putIfAbsent(id, OturumSonucu(id, it.sonuc, it.cozumAcildi))
            }
        }
        val biten = soruIdleri.filterIndexed { index, id -> id in mevcutIdler && (index < sira || id in verilenler) }
        val kalan = soruIdleri.filter { it in mevcutIdler && it !in biten }
        return copy(
            soruIdleri = biten + kalan,
            sira = biten.size,
            sonuclar = soruIdleri.mapNotNull { verilenler[it] },
            cozumAcilanlar = cozumAcilanlar.intersect(mevcutIdler),
            olcumler = olcumler.filterKeys { it in kalan },
        )
    }
}

package com.keremtulu.questionnote.domain.ilerleme

import com.keremtulu.questionnote.domain.karne.GunDenge

/** Independent-solve ratio for the current and the previous local week (Monday..Sunday). */
data class HaftaDengesi(
    val buHaftaToplam: Int,
    val buHaftaBagimsiz: Int,
    val gecenHaftaToplam: Int,
    val gecenHaftaBagimsiz: Int,
) {
    val buHaftaOran: Int? get() = oran(buHaftaBagimsiz, buHaftaToplam)
    val gecenHaftaOran: Int? get() = oran(gecenHaftaBagimsiz, gecenHaftaToplam)

    /** Decision S8: fewer reviews than this in the previous week hides the comparison. */
    val gecenHaftaYeterli: Boolean get() = gecenHaftaToplam >= GECEN_HAFTA_MIN_TEKRAR

    /** Positive means improvement. Null when either week lacks comparable records. */
    val fark: Int?
        get() {
            if (!gecenHaftaYeterli) return null
            val onceki = gecenHaftaOran ?: return null
            val simdi = buHaftaOran ?: return null
            return simdi - onceki
        }

    val karsilastirilabilir: Boolean get() = fark != null

    private fun oran(bagimsiz: Int, toplam: Int): Int? =
        if (toplam == 0) null else (bagimsiz * 100 + toplam / 2) / toplam

    companion object {
        const val GECEN_HAFTA_MIN_TEKRAR = 5
    }
}

data class GunDilimi(val gun: Int, val toplam: Int, val bagimsiz: Int)

data class IlerlemeOzeti(
    val seri: Int,
    val bugunTekrar: Int,
    val toplamBagimsiz: Int,
    /** Last seven local days, oldest first; days without reviews are present with zeros. */
    val sonYediGun: List<GunDilimi>,
    val hafta: HaftaDengesi,
    val yarinPlanli: Int,
) {
    val sonBesGun: List<GunDenge>
        get() = sonYediGun.takeLast(5).map { GunDenge(it.gun, it.toplam, it.bagimsiz) }
}

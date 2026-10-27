package com.keremtulu.questionnote.domain.repeat

import com.keremtulu.questionnote.domain.model.Soru

/** Presentation-only batching: never postpone overdue work or change stored due dates. */
data class GunlukCalismaPlani(
    val sirali: List<Soru>,
    val tur: List<Soru>,
    val onizleme: List<Soru>,
    val geciken: Int,
) {
    val kalan: Int get() = sirali.size - tur.size

    companion object {
        const val ONIZLEME_SINIRI = 3

        fun olustur(sorular: List<Soru>, bugun: Int, hedef: Int, yapilan: Int): GunlukCalismaPlani {
            val sirali = CalismaSorulariniSirala.akilliOncelik(sorular, bugun)
            val tur = sirali.take((hedef.coerceAtLeast(1) - yapilan.coerceAtLeast(0)).coerceIn(5, 30))
            return GunlukCalismaPlani(
                sirali, tur, tur.take(ONIZLEME_SINIRI),
                sirali.count { it.sonrakiTekrarGun?.let { gun -> gun < bugun } == true },
            )
        }
    }
}

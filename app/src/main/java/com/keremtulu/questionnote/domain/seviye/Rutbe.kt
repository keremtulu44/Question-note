package com.keremtulu.questionnote.domain.seviye

import com.keremtulu.questionnote.R

/**
 * Ranks are the named levels of the progression system. Each rank is a span of levels, so the
 * system stays endless while still having recognisable badges.
 *
 * Artwork lives in the drawable folders as WebP rank artwork (generated, white background removed, 96dp).
 */
enum class Rutbe(
    val anahtar: String,
    val ad: String,
    val baslangicSeviye: Int,
    val gorsel: Int,
) {
    BASLANGIC("baslangic", "Başlangıç", 1, R.drawable.rank_baslangic),
    MERAKLI("merakli", "Meraklı", 2, R.drawable.rank_merakli),
    DUZENLI("duzenli", "Düzenli", 3, R.drawable.rank_duzenli),
    KARARLI("kararli", "Kararlı", 4, R.drawable.rank_kararli),
    DISIPLINLI("disiplinli", "Disiplinli", 6, R.drawable.rank_disiplinli),
    AZIMLI("azimli", "Azimli", 8, R.drawable.rank_azimli),
    ODAKLI("odakli", "Odaklı", 10, R.drawable.rank_odakli),
    USTA("usta", "Usta", 12, R.drawable.rank_usta),
    SAMPION("sampiyon", "Şampiyon", 15, R.drawable.rank_sampiyon),
    KASIF("kasif", "Kâşif", 18, R.drawable.rank_kasif),
    STRATEJIST("stratejist", "Stratejist", 22, R.drawable.rank_stratejist),
    BILGE("bilge", "Bilge", 28, R.drawable.rank_bilge),
    EFSANE("efsane", "Efsane", 35, R.drawable.rank_efsane),
    KOZMIK("kozmik", "Kozmik", 50, R.drawable.rank_kozmik),
    SONSUZ("sonsuz", "Sonsuz", 75, R.drawable.rank_sonsuz),
    ;

    /** After the last named rank the level keeps growing and shows the infinity emblem. */
    val sonMu: Boolean get() = this == SONSUZ

    companion object {
        fun seviyeIcin(seviye: Int): Rutbe =
            entries.lastOrNull { seviye >= it.baslangicSeviye } ?: BASLANGIC

        fun anahtarla(deger: String): Rutbe? = entries.firstOrNull { it.anahtar == deger }

        /** Banner shown when a rank is first reached. */
        fun icinGorsel(rutbe: Rutbe): Int = rutbe.gorsel
    }
}

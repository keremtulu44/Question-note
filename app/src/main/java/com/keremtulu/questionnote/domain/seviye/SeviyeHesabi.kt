package com.keremtulu.questionnote.domain.seviye

/**
 * XP amounts and level maths for the progress system.
 *
 * Design decisions (v1.1.0):
 * - XP is derived from real work: adding a question, reviewing it, solving it independently.
 * - Bonuses: completed daily goal, streak milestones and badges.
 * - Levels are endless; each level needs more XP than the previous one.
 */
object SeviyeHesabi {
    const val SORU_EKLEME_XP = 10
    const val TEKRAR_XP = 5
    const val BAGIMSIZ_BONUS_XP = 5
    const val HEDEF_BONUS_XP = 25
    const val SERI_3_XP = 30
    const val SERI_7_XP = 70
    const val SERI_30_XP = 300
    const val ROZET_BONUS_XP = 50

    /** Level 1 starts at 0 XP; thresholds are open-ended. */
    const val ILK_SEVIYE = 1

    fun ad(seviye: Int): String = Rutbe.seviyeIcin(seviye).ad

    fun rutbe(seviye: Int): Rutbe = Rutbe.seviyeIcin(seviye)

    /**
     * XP needed to go from [seviye] to the next one.
     * 100, 250, 450, 700, 1000… (the increment grows by 50 each level).
     */
    fun seviyeAraligi(seviye: Int): Int {
        val n = (seviye.toLong() - ILK_SEVIYE).coerceIn(0, 10_000)
        return (100 + 100 * n + 25 * n * (n + 1)).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    /** Saturates only at the storage type's limit, not at a product-level cap. */
    fun seviyeEsigi(seviye: Int): Int {
        var toplam = 0L
        for (mevcut in 1 until seviye.coerceAtMost(10_000)) {
            toplam += seviyeAraligi(mevcut)
            if (toplam >= Int.MAX_VALUE) return Int.MAX_VALUE
        }
        return toplam.toInt()
    }

    fun durum(xp: Int): SeviyeDurumu {
        val guvenliXp = xp.coerceAtLeast(0)
        var seviye = ILK_SEVIYE
        var esik = 0L
        while (guvenliXp.toLong() >= esik + seviyeAraligi(seviye).toLong()) {
            esik += seviyeAraligi(seviye)
            seviye++
        }
        val aralik = seviyeAraligi(seviye)
        val buSeviyede = (guvenliXp.toLong() - esik).toInt()
        return SeviyeDurumu(
            seviye = seviye, ad = ad(seviye), rutbe = rutbe(seviye),
            toplamXp = guvenliXp, seviyeBaslangicXp = esik.toInt(),
            sonrakiSeviyeXp = aralik, buSeviyedeXp = buSeviyede,
            kalanXp = (aralik - buSeviyede).coerceAtLeast(0),
            oran = (buSeviyede.toFloat() / aralik).coerceIn(0f, 1f),
        )
    }
}

data class SeviyeDurumu(
    val seviye: Int,
    val ad: String,
    val rutbe: Rutbe,
    val toplamXp: Int,
    val seviyeBaslangicXp: Int,
    val sonrakiSeviyeXp: Int,
    val buSeviyedeXp: Int,
    val kalanXp: Int,
    val oran: Float,
)

data class XpKirilimi(
    val ekleme: Int,
    val tekrar: Int,
    val bagimsiz: Int,
    val hedef: Int,
    val seri: Int,
    val rozet: Int,
) {
    val toplam: Int get() = (ekleme.toLong() + tekrar + bagimsiz + hedef + seri + rozet)
        .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
}

object XpHesabi {
    fun kirilim(
        soruSayisi: Int,
        tekrarSayisi: Int,
        bagimsizTekrarSayisi: Int,
        hedefTamamGunSayisi: Int,
        seriBonusu: Int,
        rozetSayisi: Int,
    ): XpKirilimi = XpKirilimi(
        ekleme = (soruSayisi.toLong().coerceAtLeast(0) * SeviyeHesabi.SORU_EKLEME_XP).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
        tekrar = (tekrarSayisi.toLong().coerceAtLeast(0) * SeviyeHesabi.TEKRAR_XP).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
        bagimsiz = (bagimsizTekrarSayisi.toLong().coerceAtLeast(0) * SeviyeHesabi.BAGIMSIZ_BONUS_XP).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
        hedef = (hedefTamamGunSayisi.toLong().coerceAtLeast(0) * SeviyeHesabi.HEDEF_BONUS_XP).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
        seri = seriBonusu.coerceAtLeast(0),
        rozet = (rozetSayisi.toLong().coerceAtLeast(0) * SeviyeHesabi.ROZET_BONUS_XP).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
    )

    /** Every time the running streak reaches 3, 7 or 30 days the milestone bonus is added again. */
    fun seriBonusu(gunler: Collection<Int>): Int {
        val sirali = gunler.toSortedSet()
        if (sirali.isEmpty()) return 0
        var toplam = 0
        var seri = 0
        var onceki: Int? = null
        sirali.forEach { gun ->
            seri = if (onceki != null && gun == onceki + 1) seri + 1 else 1
            when (seri) {
                3 -> toplam += SeviyeHesabi.SERI_3_XP
                7 -> toplam += SeviyeHesabi.SERI_7_XP
                30 -> toplam += SeviyeHesabi.SERI_30_XP
            }
            onceki = gun
        }
        return toplam
    }

    fun enUzunSeri(gunler: Collection<Int>): Int {
        val sirali = gunler.toSortedSet()
        if (sirali.isEmpty()) return 0
        var enIyi = 1
        var seri = 1
        var onceki: Int? = null
        sirali.forEach { gun ->
            if (onceki != null) {
                seri = if (gun == onceki + 1) seri + 1 else 1
                if (seri > enIyi) enIyi = seri
            }
            onceki = gun
        }
        return enIyi
    }
}

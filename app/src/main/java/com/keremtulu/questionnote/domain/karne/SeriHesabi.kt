package com.keremtulu.questionnote.domain.karne

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Streak of consecutive days with at least one review; today not being worked does not break it yet. */
object SeriHesabi {
    fun seri(gunler: Collection<Int>, bugun: Int): Int {
        if (gunler.isEmpty()) return 0
        val kume = gunler.toHashSet()
        var gun = when {
            bugun in kume -> bugun
            bugun - 1 in kume -> bugun - 1
            else -> return 0
        }
        var sayi = 0
        while (gun in kume) {
            sayi++
            gun--
        }
        return sayi
    }
}

data class HaftaAraligi(val ilkGun: Int, val sonGun: Int)

/** Local-calendar week windows: Monday..Sunday. Epoch-day values match `tekrarlar.gun`. */
object HaftaPenceresi {
    fun buHafta(bugun: LocalDate): HaftaAraligi {
        val pazartesi = pazartesi(bugun)
        return HaftaAraligi(
            ilkGun = pazartesi.toEpochDay().toInt(),
            sonGun = bugun.toEpochDay().toInt(),
        )
    }

    fun gecenHafta(bugun: LocalDate): HaftaAraligi {
        val pazartesi = pazartesi(bugun).minusWeeks(1)
        return HaftaAraligi(
            ilkGun = pazartesi.toEpochDay().toInt(),
            sonGun = pazartesi.plusDays(6).toEpochDay().toInt(),
        )
    }

    /** Oldest first, including today. */
    fun sonGunler(bugun: LocalDate, adet: Int): List<Int> =
        (adet - 1 downTo 0).map { bugun.minusDays(it.toLong()).toEpochDay().toInt() }

    private fun pazartesi(bugun: LocalDate): LocalDate =
        bugun.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}

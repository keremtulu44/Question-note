package com.keremtulu.questionnote.domain.repeat

import com.keremtulu.questionnote.domain.model.OgrenmeDurumu
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.SoruPlanDurumu
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class CalismaSorulariniSiralaTest {
    private val zone = ZoneId.of("Europe/Istanbul")

    @Test
    fun neverReviewedQuestionsUseCreationDayAndOlderStudyActivityComesFirst() {
        val oldestNeverReviewed = soru("never-old", sonTekrar = null, olusturuldu = "2026-10-01")
        val reviewed = soru("reviewed", sonTekrar = "2026-10-02", olusturuldu = "2026-09-01")
        val recentNeverReviewed = soru("never-new", sonTekrar = null, olusturuldu = "2026-10-03")

        val ordered = CalismaSorulariniSirala.enUzunSureCalisilmayanlar(
            listOf(recentNeverReviewed, reviewed, oldestNeverReviewed),
            zone,
        )

        assertEquals(listOf("never-old", "reviewed", "never-new"), ordered.map { it.id })
        assertEquals(listOf("never-old", "reviewed"), ordered.take(2).map { it.id })
    }

    @Test
    fun sameLastActivityUsesNextReviewThenCreationAndIdForStableOrdering() {
        val laterDue = soru("b", sonTekrar = "2026-10-01", olusturuldu = "2026-09-01")
            .copy(sonrakiTekrarGun = day("2026-10-10"))
        val earlierDue = soru("c", sonTekrar = "2026-10-01", olusturuldu = "2026-09-02")
            .copy(sonrakiTekrarGun = day("2026-10-09"))
        val sameDueEarlierCreated = soru("a", sonTekrar = "2026-10-01", olusturuldu = "2026-08-31")
            .copy(sonrakiTekrarGun = day("2026-10-09"))

        val ordered = CalismaSorulariniSirala.enUzunSureCalisilmayanlar(
            listOf(laterDue, earlierDue, sameDueEarlierCreated),
            zone,
        )

        assertEquals(listOf("a", "c", "b"), ordered.map { it.id })
    }

    @Test
    fun smartOrderingPrioritizesDueWorkThenWeakerQuestionsAtSameDate() {
        val day = day("2026-10-17")
        val strong = soru("strong", null, "2026-10-01").copy(sonrakiTekrarGun = day, ogrenmeDurumu = OgrenmeDurumu.SAGLAM)
        val weak = strong.copy(id = "weak", ogrenmeDurumu = OgrenmeDurumu.OGRENIYOR)
        val future = weak.copy(id = "future", sonrakiTekrarGun = day + 1)
        val overdue = strong.copy(id = "overdue", sonrakiTekrarGun = day - 4)
        assertEquals(listOf("overdue", "weak", "strong", "future"),
            CalismaSorulariniSirala.akilliOncelik(listOf(future, strong, weak, overdue), day).map { it.id })
    }

    @Test
    fun interleavedOrderingSpreadsLessonsWithoutChangingTheirRelativePriority() {
        val gun = day("2026-10-17")
        val matematik1 = soru("mat-1", null, "2026-10-01").copy(sonrakiTekrarGun = gun)
        val matematik2 = matematik1.copy(
            id = "mat-2",
            olusturma = LocalDate.parse("2026-10-02").atStartOfDay(zone).toInstant().toEpochMilli(),
        )
        val biyoloji1 = matematik1.copy(
            id = "bio-1",
            dersId = "biyoloji",
            olusturma = LocalDate.parse("2026-10-03").atStartOfDay(zone).toInstant().toEpochMilli(),
        )
        val biyoloji2 = biyoloji1.copy(
            id = "bio-2",
            olusturma = LocalDate.parse("2026-10-04").atStartOfDay(zone).toInstant().toEpochMilli(),
        )

        val ordered = CalismaSorulariniSirala.karmaAkilliOncelik(
            listOf(biyoloji2, matematik2, biyoloji1, matematik1),
            gun,
        )

        assertEquals(listOf("mat-1", "bio-1", "mat-2", "bio-2"), ordered.map { it.id })
    }

    @Test
    fun interleavedOrderingSpreadsTopicsWhenOnlyOneLessonRemains() {
        val gun = day("2026-10-17")
        val konuA1 = soru("a-1", null, "2026-10-01").copy(sonrakiTekrarGun = gun, konuId = "a")
        val konuA2 = konuA1.copy(
            id = "a-2",
            olusturma = LocalDate.parse("2026-10-02").atStartOfDay(zone).toInstant().toEpochMilli(),
        )
        val konuB1 = konuA1.copy(
            id = "b-1",
            konuId = "b",
            olusturma = LocalDate.parse("2026-10-03").atStartOfDay(zone).toInstant().toEpochMilli(),
        )
        val konuB2 = konuB1.copy(
            id = "b-2",
            olusturma = LocalDate.parse("2026-10-04").atStartOfDay(zone).toInstant().toEpochMilli(),
        )

        val ordered = CalismaSorulariniSirala.karmaAkilliOncelik(
            listOf(konuB2, konuA2, konuB1, konuA1),
            gun,
        )

        assertEquals(listOf("a-1", "b-1", "a-2", "b-2"), ordered.map { it.id })
    }

    @Test
    fun interleavingDoesNotLetDueTodayQuestionsJumpTheOverdueBand() {
        val gun = day("2026-10-17")
        val gecikenMat1 = soru("mat-1", null, "2026-10-01")
            .copy(sonrakiTekrarGun = gun - 5)
        val gecikenMat2 = gecikenMat1.copy(id = "mat-2", sonrakiTekrarGun = gun - 4)
        val gecikenBiyo = gecikenMat1.copy(
            id = "bio-overdue",
            dersId = "biyoloji",
            sonrakiTekrarGun = gun - 1,
        )
        val bugunBiyo = gecikenBiyo.copy(id = "bio-today", sonrakiTekrarGun = gun)

        val ordered = CalismaSorulariniSirala.karmaAkilliOncelik(
            listOf(bugunBiyo, gecikenMat2, gecikenBiyo, gecikenMat1),
            gun,
        )

        assertEquals(
            listOf("mat-1", "bio-overdue", "mat-2", "bio-today"),
            ordered.map { it.id },
        )
    }

    @Test
    fun interleavingAlsoAvoidsSameLessonAcrossUrgencyBandBoundary() {
        val gun = day("2026-10-17")
        val gecikenMat = soru("overdue-mat", null, "2026-10-01")
            .copy(sonrakiTekrarGun = gun - 1)
        val bugunMat1 = gecikenMat.copy(id = "today-mat-1", sonrakiTekrarGun = gun)
        val bugunMat2 = bugunMat1.copy(
            id = "today-mat-2",
            olusturma = LocalDate.parse("2026-10-02").atStartOfDay(zone).toInstant().toEpochMilli(),
        )
        val bugunBiyo = bugunMat1.copy(
            id = "today-bio",
            dersId = "biyoloji",
            olusturma = LocalDate.parse("2026-10-03").atStartOfDay(zone).toInstant().toEpochMilli(),
        )

        val ordered = CalismaSorulariniSirala.karmaAkilliOncelik(
            listOf(bugunBiyo, bugunMat2, gecikenMat, bugunMat1),
            gun,
        )

        assertEquals(
            listOf("overdue-mat", "today-bio", "today-mat-1", "today-mat-2"),
            ordered.map { it.id },
        )
    }

    private fun soru(id: String, sonTekrar: String?, olusturuldu: String): Soru = Soru(
        id = id,
        dersId = "lesson",
        konuId = null,
        soruNotu = null,
        cozumNotu = null,
        kaynak = null,
        kaynakKonumu = null,
        yanlisNedeni = null,
        favori = false,
        planDurumu = SoruPlanDurumu.AKTIF,
        ogrenmeDurumu = OgrenmeDurumu.YENI,
        sonTekrarGun = sonTekrar?.let(::day),
        sonrakiTekrarGun = null,
        olusturma = LocalDate.parse(olusturuldu).atStartOfDay(zone).toInstant().toEpochMilli(),
        guncelleme = 0,
    )

    private fun day(value: String): Int = LocalDate.parse(value).toEpochDay().toInt()
}

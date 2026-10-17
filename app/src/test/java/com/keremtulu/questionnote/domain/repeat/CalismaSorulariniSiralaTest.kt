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

package com.keremtulu.questionnote.domain.gelisim

import com.keremtulu.questionnote.domain.calisma.*
import com.keremtulu.questionnote.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class GelisimAnaliziTest {
    private val gun = 20_744
    private val dersler = listOf(Ders("m", "Matematik", 0, 0), Ders("f", "Fizik", 1, 0), Ders("archive", "Arşiv", 2, 0, true))
    private val b = TarihselBaglam("m", "Matematik", "topic", "Sayılar", "ışık test", "IŞIK TEST")
    private val secim = SinavSecimi.Tumu
    private fun q(id: String = "q", ders: String = "m", vade: Int? = gun + 3) =
        CalismaSorusu(id, ders, "topic", "  IŞIK   TEST  ", SoruPlanDurumu.AKTIF, null, vade)
    private fun r(id: String, q: String = "q", g: Int = gun - 1, s: TekrarSonucu = TekrarSonucu.RAHAT,
        cozum: Boolean = false, baglam: TarihselBaglam? = b, o: SureOlcumu? = SureOlcumu(60_000, 0, OlcumKalitesi.TAM)) =
        AnalizTekrari(id, q, g, g.toLong() * 1_000, s, cozum, baglam, o)
    private fun analiz(qs: List<CalismaSorusu> = listOf(q()), rs: List<AnalizTekrari> = emptyList(), profil: SinavSecimi = secim) =
        GelisimAnaliziHesabi.hesapla(qs, dersler, mapOf("topic" to "Bugünkü konu adı"), rs, profil, gun)

    @Test fun outcomeDenominatorIncludesInvalidMissingAndAssistedMeasurements() {
        val a = analiz(rs = listOf(r("a"), r("b", o = null), r("c", o = SureOlcumu(-1, 0, OlcumKalitesi.TAM)),
            r("d", cozum = true), r("e", s = TekrarSonucu.COZEMEDIM)))
        val s = a.kaynaklar.single().sayac
        assertEquals(1, s.soru); assertEquals(5, s.tekrar); assertEquals(3, s.bagimsiz)
        assertEquals(3, s.guvenilirSure); assertEquals(60, s.kapsamYuzde); assertEquals(1, s.gecersizSure)
    }
    @Test fun unknownLegacyContextIsSeparateFromKnownMissingLabelsAndNeverInventedFromCurrentQuestion() {
        val a = analiz(rs = listOf(r("legacy", baglam = null), r("missing", baglam = b.copy(konuId = null, konuAdi = null, kaynakAnahtari = null, kaynakEtiketi = null))))
        assertEquals(2, a.kaynaklar.size); assertEquals(2, a.konular.size)
        val eski = a.kaynaklar.single { !it.anahtar.bilinen }
        assertNull(eski.filtre); assertNull(eski.dersAdi); assertEquals(0, eski.uygunSoru)
        val bos = a.konular.single { it.anahtar.bilinen }
        assertTrue(requireNotNull(bos.filtre).konusuz)
        assertEquals(1, a.toplam.baglamsiz)
    }
    @Test fun renamedCurrentSourceDoesNotRewriteHistoricalLabelsOrRouteToTheNewBook() {
        val a = analiz(qs = listOf(q().copy(kaynak = "Başka kitap")), rs = listOf(r("old")))
        val k = a.kaynaklar.single()
        assertEquals(listOf("IŞIK TEST"), k.etiketler)
        assertEquals("ışık test", k.filtre?.kaynakAnahtari)
        assertEquals(0, k.uygunSoru)
    }
    @Test fun sameNormalizedBookStaysSeparatedByLessonAndPreservesCapturedVariants() {
        val a = analiz(qs = listOf(q(), q("f-q", "f")), rs = listOf(r("a"),
            r("b", baglam = b.copy(kaynakEtiketi = "ışık test")), r("c", q = "f-q", baglam = b.copy(dersId = "f", dersAdi = "Fizik"))))
        assertEquals(2, a.kaynaklar.size)
        assertEquals(2, a.kaynaklar.single { it.anahtar.dersId == "m" }.etiketler.size)
        assertEquals(1, a.kaynaklar.single { it.anahtar.dersId == "f" }.sayac.tekrar)
    }
    @Test fun rhythmUsesRealDaysMarksTodayIncompleteAndCountsQuestionsSeparately() {
        val a = analiz(rs = listOf(r("a", g = gun), r("b", g = gun), r("c", g = gun - 7)))
        assertEquals(7, a.ritim.size)
        assertEquals(2, a.ritim.last().tekrar); assertEquals(1, a.ritim.last().soru)
        assertTrue(a.ritim.last().bugunTamamlanmadi)
        assertTrue(a.ritim.dropLast(1).none { it.bugunTamamlanmadi })
        assertEquals(2, a.ritim.sumOf { it.tekrar })
    }
    @Test fun completedSevenDayPeriodsExcludeTodayAndDoNotInferGainFromDifferentQuestions() {
        val a = analiz(rs = (0..19).map { r("old$it", "old$it", gun - 8, TekrarSonucu.COZEMEDIM) } +
            (0..19).map { r("new$it", "new$it", gun - 1) } + r("today", g = gun))
        assertEquals(20, a.donem.onceki.tekrar); assertEquals(20, a.donem.son.tekrar)
        assertEquals(0, a.donem.eslesen.soru); assertFalse(a.donem.eslesen.yeterli)
    }
    @Test fun sameQuestionComparisonNeedsTenMatchingQuestionsAndThreeDaysInEachPeriod() {
        val rs = (0..9).flatMap { i -> listOf(r("old$i", "$i", gun - 8 - i % 3, TekrarSonucu.COZEMEDIM, o = null),
            r("new$i", "$i", gun - 1 - i % 3, o = SureOlcumu(-1, 0, OlcumKalitesi.TAM))) }
        val e = analiz(rs = rs).donem.eslesen
        assertEquals(3, e.onceGun); assertEquals(3, e.sonraGun)
        assertTrue(e.yeterli); assertEquals(10, e.soru); assertEquals(0, e.onceBagimsiz); assertEquals(10, e.sonraBagimsiz)
        assertFalse(analiz(rs = rs.filter { it.soruId != "9" }).donem.eslesen.yeterli)
        val sameDay = analiz(rs = rs.map { it.copy(gun = if (it.id.startsWith("old")) gun - 8 else gun - 1) }).donem.eslesen
        assertFalse(sameDay.yeterli); assertEquals(1, sameDay.onceGun); assertEquals(1, sameDay.sonraGun)
    }
    @Test fun changedOrUnknownContextDoesNotEnterMatchedComparison() {
        val rs = listOf(r("a", g = gun - 8), r("b", g = gun - 1, baglam = b.copy(kaynakAnahtari = "other")),
            r("c", "legacy", gun - 9, baglam = null), r("d", "legacy", gun - 2, baglam = null))
        val e = analiz(rs = rs).donem.eslesen
        assertEquals(0, e.soru); assertEquals(2, e.baglamDegisen)
    }
    @Test fun recommendationOrderIsDueThenRecentDifficultyThenSmallOptionalRound() {
        val zor = listOf(r("a", g = gun - 2, s = TekrarSonucu.ZORLANDIM), r("b", g = gun - 1, cozum = true))
        assertEquals(OneriTuru.VADESI_GELEN, analiz(listOf(q(vade = gun)), zor).oneri?.tur)
        val o = requireNotNull(analiz(rs = zor).oneri)
        assertEquals(OneriTuru.YAKIN_GUCLUK, o.tur); assertEquals("m", o.filtre.dersId); assertEquals("topic", o.filtre.konuId)
        assertEquals(OneriTuru.KUCUK_TUR, analiz().oneri?.tur)
        assertEquals(1, o.turBoyutu)
    }
    @Test fun twoIndependentDaysClearRecommendationAndDuplicatesDoNotCreateRepeatedDifficulty() {
        val rs = listOf(r("a", g = gun - 4, s = TekrarSonucu.COZEMEDIM), r("b", g = gun - 3, s = TekrarSonucu.COZEMEDIM),
            r("c", g = gun - 2), r("d", g = gun - 1))
        val a = analiz(rs = rs)
        assertEquals(OneriTuru.KUCUK_TUR, a.oneri?.tur)
        assertEquals(0, a.konular.single().yakinGuclukSoru); assertEquals(1, a.konular.single().sonIkiBagimsizSoru)
        assertEquals(OneriTuru.KUCUK_TUR, analiz(rs = rs.take(2).map { it.copy(gun = gun - 1) }).oneri?.tur)
    }
    @Test fun historicalDifficultyFromAnOldSourceIsNotAttributedToANewSourceOrUnknownContext() {
        val rs = listOf(r("a", g = gun - 2, s = TekrarSonucu.COZEMEDIM), r("b", g = gun - 1, s = TekrarSonucu.COZEMEDIM))
        assertEquals(OneriTuru.KUCUK_TUR, analiz(listOf(q().copy(kaynak = "new", konuId = "new")), rs).oneri?.tur)
        assertEquals(OneriTuru.KUCUK_TUR, analiz(rs = rs.map { it.copy(baglam = null) }).oneri?.tur)
    }
    @Test fun suspendedArchivedOutOfProfileAndActuallyReviewedTodayAreIneligibleEvenWithStaleSummary() {
        val qs = listOf(q("suspended", vade = gun).copy(planDurumu = SoruPlanDurumu.ASKIDA), q("archived", "archive", gun),
            q("physics", "f", gun), q("today", vade = gun), q("ok", vade = gun))
        val a = analiz(qs, listOf(r("today-row", "today", gun)), SinavSecimi(SinavProfili.LGS))
        assertEquals(1, a.oneri?.uygunSoru); assertEquals(1, a.plan.first().adet)
        assertEquals(1, a.toplam.tekrar) // Archive analytics are not hidden by the selected exam profile.
    }
    @Test fun noEligibleQuestionsMeansNoInventedRecommendationAndNoPlannedCount() {
        val a = analiz(qs = listOf(q().copy(planDurumu = SoruPlanDurumu.TAMAMLANDI)))
        assertNull(a.oneri); assertEquals(0, a.plan.sumOf { it.adet })
    }
    @Test fun currentDaysReviewCannotBeRecommendedButItsRealTomorrowPlanRemainsVisible() {
        val a = analiz(listOf(q(vade = gun + 1).copy(sonTekrarGun = gun)), listOf(r("today", g = gun)))
        assertNull(a.oneri); assertEquals(0, a.plan.first().adet); assertEquals(1, a.plan[1].adet)
    }
    @Test fun plannedCountsAreRealEvenWithNoReferenceAndOverdueIsExplicitlyInTodayBucket() {
        val a = analiz(listOf(q("past", vade = gun - 5), q("later", vade = gun + 6), q("outside", vade = gun + 7)))
        assertEquals(1, a.plan.first().adet); assertEquals(1, a.plan.last().adet)
        assertEquals(0, a.plan.sumOf { it.kapsanan }); assertTrue(a.plan.all { it.toplamAltMs == null })
    }
    @Test fun workloadUsesAllOutcomesNotJustEasyAndExtrapolatesOnlyAtEightyPercent() {
        val refs = (0..19).map { i -> r("ref$i", "ref${i % 10}", gun - 1 - i / 4,
            TekrarSonucu.COZEMEDIM, true, o = SureOlcumu(60_000, 60_000, OlcumKalitesi.TAM)) }
        val old = (0..9).map { q("ref$it", vade = null).copy(planDurumu = SoruPlanDurumu.TAMAMLANDI) }
        val targets = (0..3).map { q("target$it", vade = gun) } + q("unknown", "f", gun)
        val p = analiz(targets + old, refs).plan.first()
        assertEquals(5, p.adet); assertEquals(4, p.kapsanan); assertEquals(1, p.bilinmeyen)
        assertEquals(480_000.0, p.altMs, 0.0); assertEquals(600_000.0, requireNotNull(p.toplamAltMs), 0.0)
        val az = analiz(targets.drop(1) + old, refs).plan.first()
        assertEquals(75, az.kapsamYuzde); assertNull(az.toplamAltMs); assertEquals(360_000.0, az.altMs, 0.0)
    }
    @Test fun normalizedSourceAndLessonFiltersMatchOnlyTheIntendedCurrentQuestions() {
        val f = SoruCalismaFiltresi(dersId = "m", kaynakAnahtari = "ışık test", vadesiGelen = true)
        assertTrue(CalismaUygunlugu.filtreUyar(q(vade = gun), f, gun))
        assertFalse(CalismaUygunlugu.filtreUyar(q(ders = "f", vade = gun), f, gun))
        assertFalse(CalismaUygunlugu.filtreUyar(q(vade = gun + 1), f, gun))
        assertTrue(CalismaUygunlugu.filtreUyar(q().copy(kaynak = "\u00a0"), SoruCalismaFiltresi(kaynakBelirtilmemis = true), gun))
    }
    @Test fun recommendationsAndSummariesDoNotDependOnInputOrdering() {
        val qs = listOf(q("a"), q("b"))
        val rs = listOf(r("a1", "a", gun - 2, TekrarSonucu.COZEMEDIM), r("a2", "a", gun - 1, TekrarSonucu.COZEMEDIM),
            r("b1", "b", gun - 2, TekrarSonucu.COZEMEDIM), r("b2", "b", gun - 1, TekrarSonucu.COZEMEDIM))
        assertEquals(analiz(qs, rs), analiz(qs.reversed(), rs.reversed()))
    }
}

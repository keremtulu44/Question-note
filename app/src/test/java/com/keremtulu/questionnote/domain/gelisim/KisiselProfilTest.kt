package com.keremtulu.questionnote.domain.gelisim

import com.keremtulu.questionnote.domain.calisma.OlcumKalitesi
import com.keremtulu.questionnote.domain.calisma.OlcumNedeni
import com.keremtulu.questionnote.domain.calisma.SureOlcumu
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import org.junit.Assert.*
import org.junit.Test

class KisiselProfilTest {
    private val baglam = ProfilBaglami("math", "topic", "kitap")
    private fun veriler(n: Int = 20, soru: Int = 10): List<SureOrnegi> = (0 until n).map { i ->
        val gun = 100 - i / 4
        SureOrnegi("r$i", "a$i", "q${i % soru}", gun, gun * 86_400_000L + i,
            baglam, TekrarSonucu.RAHAT, false, SureOlcumu(50_000, 0, OlcumKalitesi.TAM))
    }
    private fun hesapla(v: List<SureOrnegi>, duzey: ProfilDuzeyi = ProfilDuzeyi.DERS) =
        KisiselProfilHesabi.hesapla(v, baglam, 100, duzey = duzey)

    @Test fun lessonNeedsMeasurementsDistinctQuestionsAndDistinctDaysTogether() {
        val tam = hesapla(veriler())
        assertEquals(VeriDayanagi.YETERLI, tam.yardimsiz.istenen.dayanak)
        assertEquals(20, tam.yardimsiz.istenen.olcumSayisi)
        assertEquals(10, tam.yardimsiz.istenen.farkliSoru)
        assertEquals(5, tam.yardimsiz.istenen.farkliGun)
        assertNull(hesapla(veriler(19)).yardimsiz.referans)
        assertNull(hesapla(veriler(20, 9)).yardimsiz.referans)
        assertNull(hesapla(veriler(20, 20).map { it.copy(gun = 100) }).yardimsiz.referans)
    }

    @Test fun currentAttemptIsExcludedBeforeCheckingEligibilityOrStatistics() {
        val sonuc = KisiselProfilHesabi.hesapla(veriler(), baglam, 100, mevcutDenemeId = "a19")
        assertEquals(19, sonuc.yardimsiz.istenen.olcumSayisi)
        assertNull(sonuc.yardimsiz.referans)
    }

    @Test fun oldAndFutureRecordsAreExcludedFromTheSixtyDayWindow() {
        val tumu = veriler() + veriler(2).mapIndexed { i, s -> s.copy(tekrarId = "outside$i", denemeId = "outside$i",
            soruId = "outside$i", gun = if (i == 0) 40 else 101) }
        val p = hesapla(tumu).yardimsiz.istenen
        assertEquals(20, p.olcumSayisi)
        assertEquals(41, p.ilkGun)
        assertEquals(100, p.sonGun)
    }

    @Test fun missingPartialInvalidAndZeroMeasurementsAreNotReferences() {
        val v = veriler().toMutableList()
        v[0] = v[0].copy(olcum = SureOlcumu())
        v[1] = v[1].copy(olcum = SureOlcumu(1_000, 0, OlcumKalitesi.KISMI, setOf(OlcumNedeni.TAM_EKRAN)))
        v[2] = v[2].copy(olcum = SureOlcumu(-1, 0, OlcumKalitesi.TAM))
        v[3] = v[3].copy(olcum = SureOlcumu(0, 0, OlcumKalitesi.TAM))
        val p = hesapla(v)
        assertEquals(16, p.yardimsiz.istenen.olcumSayisi)
        assertEquals(20, p.yardimsiz.istenen.kapsamKaydi)
        assertNull(p.yardimsiz.referans)
        assertEquals(-1L, v[2].olcum.denemeAktifMs) // raw input is not modified
    }

    @Test fun assistedAndFailedAttemptsBelongToWorkloadButNotIndependentReference() {
        val v = veriler().mapIndexed { i, s -> if (i % 2 == 0)
            s.copy(cozumAcildi = true, olcum = SureOlcumu(4_000, 6_000, OlcumKalitesi.TAM))
            else s.copy(sonuc = TekrarSonucu.COZEMEDIM, olcum = SureOlcumu(10_000, 0, OlcumKalitesi.TAM)) }
        val p = hesapla(v)
        assertEquals(0, p.yardimsiz.istenen.olcumSayisi)
        assertNull(p.yardimsiz.referans)
        assertEquals(10_000.0, requireNotNull(p.isYuku.referans?.dagilim).medyanMs, 0.0)
    }

    @Test fun quartilesAndMadUseQuestionMediansNotAnUnweightedAttemptMean() {
        val p = hesapla(veriler().mapIndexed { i, s -> s.copy(olcum = SureOlcumu((i % 10 + 1) * 10_000L, 0, OlcumKalitesi.TAM)) })
        val d = requireNotNull(p.yardimsiz.referans?.dagilim)
        assertEquals(55_000.0, d.medyanMs, 0.0)
        assertEquals(32_500.0, d.altCeyrekMs, 0.0)
        assertEquals(77_500.0, d.ustCeyrekMs, 0.0)
        assertEquals(25_000.0, d.madMs, 0.0)
    }

    @Test fun oneRepeatedEasyQuestionCannotTakeOverTheReference() {
        val cokTekrar = (0..58).map { i -> veriler(1).first().copy(tekrarId = "extra$i", denemeId = "extra$i",
            gun = 100 - i, olcum = SureOlcumu(1_000, 0, OlcumKalitesi.TAM)) }
        val p = hesapla(veriler() + cokTekrar).yardimsiz.istenen
        assertEquals(10, p.farkliSoru)
        assertTrue(p.olcumSayisi > 20)
        assertEquals(50_000.0, requireNotNull(p.dagilim).medyanMs, 0.0)
    }

    @Test fun aValidLongOutlierIsRetainedButCannotDefineTheMedian() {
        val v = veriler().map { if (it.soruId == "q0") it.copy(olcum = SureOlcumu(80_000_000, 0, OlcumKalitesi.TAM)) else it }
        val d = requireNotNull(hesapla(v).yardimsiz.referans?.dagilim)
        assertEquals(50_000.0, d.medyanMs, 0.0)
        assertEquals(0.0, d.madMs, 0.0)
        assertEquals(0.0, d.iqrMs, 0.0)
        assertEquals(80_000_000L, v[0].olcum.denemeAktifMs)
    }

    @Test fun sparseTopicsAndSourcesFallBackToTheirOwnLesson() {
        listOf(ProfilDuzeyi.KONU, ProfilDuzeyi.KAYNAK).forEach { scope ->
            val p = hesapla(veriler(), scope)
            assertEquals(scope, p.yardimsiz.istenen.duzey)
            assertTrue(p.yardimsiz.derseDonuldu)
            assertEquals(ProfilDuzeyi.DERS, p.yardimsiz.referans?.duzey)
        }
    }

    @Test fun aThirtySampleFifteenQuestionSevenDaySubgroupUsesItsOwnReference() {
        val p = hesapla(veriler(30, 15), ProfilDuzeyi.KAYNAK)
        assertEquals(ProfilDuzeyi.KAYNAK, p.yardimsiz.referans?.duzey)
        assertFalse(p.yardimsiz.derseDonuldu)
    }

    @Test fun differentLessonsAreNeverPooledEvenWhenBookNamesMatch() {
        val diger = veriler(40, 15).map { it.copy(tekrarId = "other-${it.tekrarId}", denemeId = "other-${it.denemeId}",
            baglam = baglam.copy(dersId = "other"), olcum = SureOlcumu(1_000, 0, OlcumKalitesi.TAM)) }
        val p = hesapla(veriler() + diger)
        assertEquals(20, p.yardimsiz.istenen.olcumSayisi)
        assertEquals(50_000.0, requireNotNull(p.yardimsiz.referans?.dagilim).medyanMs, 0.0)
    }

    @Test fun sparseLessonNeverInventsADistributionOrAConfidencePercentage() {
        assertNull(hesapla(veriler(8)).yardimsiz.referans)
        assertNull(hesapla(veriler(8)).yardimsiz.istenen.dagilim)
        assertEquals(VeriDayanagi.YETERSIZ, hesapla(emptyList()).yardimsiz.istenen.dayanak)
    }

    @Test fun sourceNormalizationIsTurkishNfcAndWhitespaceAwareNotFuzzy() {
        assertEquals("ışık test", kaynakAnahtari("  IŞIK   TEST  "))
        assertEquals(kaynakAnahtari("İŞIK"), kaynakAnahtari("I\u0307ŞIK"))
        assertNotEquals(kaynakAnahtari("I"), kaynakAnahtari("İ"))
        assertNotEquals(kaynakAnahtari("Kitap-1"), kaynakAnahtari("Kitap 1"))
        assertNull(kaynakAnahtari("  "))
    }
}

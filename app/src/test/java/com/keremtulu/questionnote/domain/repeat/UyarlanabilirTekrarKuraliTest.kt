package com.keremtulu.questionnote.domain.repeat

import com.keremtulu.questionnote.domain.calisma.*
import com.keremtulu.questionnote.domain.gelisim.*
import com.keremtulu.questionnote.domain.model.*
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class UyarlanabilirTekrarKuraliTest {
    private val gun = 20_744
    private val profil = SureProfili(ProfilDuzeyi.DERS, VeriDayanagi.YETERLI, 20, 10, 5, 20,
        ProfilEsigi(20, 10, 5), gun - 59, gun, DayanikliDagilim(1_000.0, 900.0, 1_100.0, 100.0))
    private val zor = YakinGecmis(listOf(YakinTekrar(gun - 1, TekrarSonucu.ZORLANDIM, false),
        YakinTekrar(gun - 2, TekrarSonucu.COZEMEDIM, false)))
    private val deneme = TekrarDenemesi("a", "q", TekrarSonucu.RAHAT, false, SureOlcumu(3_000, 0, OlcumKalitesi.TAM))
    private fun karar(aralik: Int = 100, d: TekrarDenemesi = deneme, p: SureProfili? = profil,
        yakin: YakinGecmis = zor, mod: TekrarMotorModu = TekrarMotorModu.GOLGE,
        gecikme: Boolean = false, bilinmeyen: Boolean = false) = UyarlanabilirTekrarKurali.karar(
        TekrarPlani(50, aralik, gun + aralik, if (aralik <= 3) OgrenmeDurumu.OGRENIYOR else if (aralik <= 21) OgrenmeDurumu.PEKISIYOR else OgrenmeDurumu.SAGLAM),
        d, p, gun, mod, mod, yakin, bilinmeyen, gecikme)
    private fun kayit(id: String, g: Int, sonuc: TekrarSonucu = TekrarSonucu.COZEMEDIM, zaman: Long = g.toLong(), surum: Int = 2) =
        TekrarKaydi(id, g, null, sonuc, false, 10, 20, false, surum, zaman)

    @Test fun shadowRecordsDifferentCandidateWithoutChangingAppliedV2Date() {
        val k = karar()
        assertEquals(90, k.aday.sonrakiAralik)
        assertEquals(k.temel, k.uygulanan)
        assertEquals(2, k.algoritmaSurumu)
        assertTrue(KararNedeni.BIRLIKTE_SINYAL in k.nedenler)
    }
    @Test fun activeIsAppliedOnlyThroughBothExplicitRolloutGates() {
        for (cihaz in listOf(false, true)) for (golge in listOf(false, true)) {
            val a = MotorAyari(TekrarMotorModu.ETKIN, MotorYayinKapisi(cihaz, golge))
            assertEquals(if (cihaz && golge) TekrarMotorModu.ETKIN else TekrarMotorModu.GOLGE, a.uygulanan)
        }
        val k = karar(mod = TekrarMotorModu.ETKIN)
        assertEquals(3, k.algoritmaSurumu)
        assertEquals(k.aday, k.uygulanan)
    }
    @Test fun everyIntegerIntervalRespectsActualTenPercentLimitIncludingRounding() {
        for (i in 1..180) {
            val k = karar(i, mod = TekrarMotorModu.ETKIN)
            assertTrue(k.uygulanan.sonrakiAralik in 1..180)
            assertEquals(i / 10, i - k.uygulanan.sonrakiAralik)
            assertTrue((i - k.uygulanan.sonrakiAralik) * 10 <= i)
            assertEquals(gun + k.uygulanan.sonrakiAralik, k.uygulanan.sonrakiGun)
        }
    }
    @Test fun speedAloneNeverExtendsTheIntervalAndSlowAloneNeverShortensIt() {
        assertEquals(100, karar(d = deneme.copy(olcum = deneme.olcum.copy(denemeAktifMs = 1))).aday.sonrakiAralik)
        assertEquals(100, karar(yakin = YakinGecmis(emptyList())).aday.sonrakiAralik)
        assertEquals(100, karar(yakin = YakinGecmis(zor.gunler.take(1))).aday.sonrakiAralik)
    }
    @Test fun durationMustExceedBothMedianRatioAndRobustSpreadStrictly() {
        assertEquals(100, karar(d = deneme.copy(olcum = deneme.olcum.copy(denemeAktifMs = 1_500))).aday.sonrakiAralik)
        val genis = profil.copy(dagilim = DayanikliDagilim(1_000.0, 500.0, 2_000.0, 900.0))
        assertEquals(100, karar(p = genis).aday.sonrakiAralik)
        assertEquals(90, karar(d = deneme.copy(olcum = deneme.olcum.copy(denemeAktifMs = 1_501))).aday.sonrakiAralik)
    }
    @Test fun missingPartialZeroInvalidAndUnsupportedMeasurementsFallBackToV2() {
        val olcumler = listOf(SureOlcumu(), SureOlcumu(0, 0, OlcumKalitesi.TAM),
            SureOlcumu(3_000, 0, OlcumKalitesi.KISMI, setOf(OlcumNedeni.SUREC_KURTARMA)),
            SureOlcumu(-1, 0, OlcumKalitesi.TAM), deneme.olcum.copy(surum = 99), deneme.olcum.copy(incelemeAktifMs = 1))
        olcumler.forEach { assertEquals(100, karar(d = deneme.copy(olcum = it)).aday.sonrakiAralik) }
    }
    @Test fun insufficientInvalidOrNonfiniteDistributionsFailSafe() {
        listOf(null, profil.copy(dayanak = VeriDayanagi.SINIRLI), profil.copy(olcumSayisi = 19),
            profil.copy(dagilim = null), profil.copy(dagilim = DayanikliDagilim(Double.NaN, 1.0, 2.0, 0.0)),
            profil.copy(dagilim = DayanikliDagilim(1_000.0, 1_100.0, 900.0, 0.0)))
            .forEach { assertEquals(100, karar(p = it).aday.sonrakiAralik) }
    }
    @Test fun twoMostRecentIndependentDaysClearWarningAndCandidateReduction() {
        val yakin = YakinGecmis(listOf(YakinTekrar(gun, TekrarSonucu.RAHAT, false),
            YakinTekrar(gun - 1, TekrarSonucu.RAHAT, false)) + zor.gunler.map { it.copy(gun = it.gun - 2) })
        assertTrue(yakin.toparlandi)
        assertFalse(yakin.tekrarGucluk)
        assertEquals(100, karar(yakin = yakin).aday.sonrakiAralik)
        assertTrue(KararNedeni.TOPARLANMA in karar(yakin = yakin).nedenler)
    }
    @Test fun solutionOpenedSuccessNeverCountsAsIndependentRecovery() {
        val yakin = YakinGecmis(listOf(YakinTekrar(gun, TekrarSonucu.RAHAT, true), YakinTekrar(gun - 1, TekrarSonucu.RAHAT, true)))
        assertFalse(yakin.toparlandi)
        assertTrue(yakin.tekrarGucluk)
    }
    @Test fun onlyFiveDistinctDaysWithinNinetyDaysAreIncludedAndLatestDuplicateWins() {
        val liste = (1..10).map { kayit("$it", gun - it) } + listOf(kayit("old", gun - 90), kayit("future", gun + 1),
            kayit("latest", gun - 1, TekrarSonucu.RAHAT, 999_999), kayit("edge", gun - 89))
        val sonuc = YakinGecmisHesabi.hesapla(liste.shuffled(), gun)
        assertEquals(listOf(gun - 1, gun - 2, gun - 3, gun - 4, gun - 5), sonuc.gunler.map { it.gun })
        assertTrue(sonuc.gunler.first().bagimsiz)
        assertEquals(sonuc, YakinGecmisHesabi.hesapla(liste.reversed(), gun))
        assertEquals(1, YakinGecmisHesabi.hesapla(listOf(kayit("edge", gun - 89), kayit("old", gun - 90)), gun).gunler.size)
    }
    @Test fun v2OverdueAndHardRulesAreNotPenalizedTwice() {
        assertEquals(100, karar(gecikme = true).aday.sonrakiAralik)
        assertTrue(KararNedeni.IKINCI_INDIRIM_YOK in karar(gecikme = true).nedenler)
        assertEquals(100, karar(d = deneme.copy(sonuc = TekrarSonucu.ZORLANDIM)).aday.sonrakiAralik)
    }
    @Test fun solutionOrFailureAlwaysRetainsOneDayInEveryMode() {
        val p = KisiselProfil(1, ProfilSecimi(profil, profil), ProfilSecimi(profil, profil))
        TekrarMotorModu.entries.forEach { mod ->
            listOf(deneme.copy(cozumAcildi = true), deneme.copy(sonuc = TekrarSonucu.COZEMEDIM)).forEach { d ->
                val k = UyarlanabilirTekrarKurali.hesapla(listOf(kayit("x", gun - 2)), d, p,
                    LocalDate.ofEpochDay(gun.toLong()), gun, MotorAyari(mod, MotorYayinKapisi(true, true)))
                assertEquals(1, k.aday.sonrakiAralik); assertEquals(1, k.uygulanan.sonrakiAralik)
            }
        }
    }
    @Test fun supportedV3HistoryUsesSameEaseDuringV2RollbackAndUnknownVersionsAreNotV2() {
        val v2 = listOf(kayit("a", gun - 2, TekrarSonucu.RAHAT))
        val v3 = v2.map { it.copy(algoritmaSurumu = 3) }
        assertEquals(AkilliTekrarKurali.hafiza(v2), AkilliTekrarKurali.hafiza(v3))
        val bilinmeyen = kayit("unknown", gun - 1, TekrarSonucu.COZEMEDIM, surum = 99).copy(sonrakiAralik = 180)
        assertEquals(AkilliTekrarKurali.hafiza(v2), AkilliTekrarKurali.hafiza(v2 + bilinmeyen))
        assertEquals(100, karar(bilinmeyen = true).aday.sonrakiAralik)
    }
    @Test fun disabledModeKeepsV2EvenWithAllCandidateSignalsPresent() {
        val k = karar(mod = TekrarMotorModu.KAPALI)
        assertEquals(k.temel, k.aday); assertEquals(k.temel, k.uygulanan)
        assertEquals(setOf(KararNedeni.KATMAN_KAPALI), k.nedenler)
    }
}

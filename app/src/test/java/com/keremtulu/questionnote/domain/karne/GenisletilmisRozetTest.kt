package com.keremtulu.questionnote.domain.karne

import com.keremtulu.questionnote.domain.model.TekrarSonucu
import org.junit.Assert.*
import org.junit.Test

class GenisletilmisRozetTest {
    private fun bos() = RozetGirdisi(KarneSayilari(0, 0, 0, 0, 0), 0, 0, emptyList())

    @Test fun catalogueHasThirtyStableUniqueKeysIncludingFiveSecrets() {
        assertEquals(30, Rozet.entries.size)
        assertEquals(5, Rozet.entries.count { it.gizli })
        assertEquals(30, Rozet.entries.map { it.anahtar }.distinct().size)
        assertEquals(Rozet.ILK_KARNE, Rozet.anahtarla("ilk_karne"))
        assertNull(Rozet.anahtarla("future-unknown-key"))
    }

    @Test fun noAchievementIsAwardedForAnEmptyHistory() {
        assertTrue(RozetKurallari.kazanilanlar(bos(), emptySet()).isEmpty())
    }

    @Test fun questionMilestonesUseExactBoundaries() {
        listOf(Rozet.ILK_SORU to 1, Rozet.ARSIVCI to 25, Rozet.KUTUPHANE to 100, Rozet.KOLEKSIYONCU to 250).forEach { (r, n) ->
            assertFalse(RozetKurallari.ilerleme(r, bos().copy(soruSayisi = n - 1)).tamam)
            assertTrue(RozetKurallari.ilerleme(r, bos().copy(soruSayisi = n)).tamam)
        }
    }

    @Test fun lifetimeReviewsAndGoalsDoNotRequireAnActiveSession() {
        val all = RozetKurallari.kazanilanlar(bos().copy(tekrarSayisi = 1000, hedefGunSayisi = 30), emptySet())
        assertTrue(all.containsAll(listOf(Rozet.ELLI_TEKRAR, Rozet.IKI_YUZ_TEKRAR, Rozet.BIN_TEKRAR, Rozet.HEDEF_1, Rozet.HEDEF_7, Rozet.HEDEF_30)))
        assertFalse(Rozet.ILK_KARNE in all)
    }

    @Test fun newStreakMilestonesHaveTheirOwnBoundaries() {
        listOf(Rozet.SERI_14 to 14, Rozet.SERI_60 to 60, Rozet.SERI_100 to 100).forEach { (r, n) ->
            assertFalse(RozetKurallari.ilerleme(r, bos().copy(seri = n - 1)).tamam)
            assertTrue(RozetKurallari.ilerleme(r, bos().copy(seri = n)).tamam)
        }
    }

    @Test fun secretsAreEarnableButNotAutomaticallyAwarded() {
        val girdi = bos().copy(birGundeDersSayisi = 3, toplamDersSayisi = 5, donusumVar = true)
        val yeni = RozetKurallari.kazanilanlar(girdi, emptySet())
        assertTrue(yeni.containsAll(listOf(Rozet.UC_DERS, Rozet.BES_DERS, Rozet.DONUSUM)))
        assertFalse(Rozet.KUSURSUZ_20 in yeni)
        assertFalse(Rozet.ZINCIR_10 in yeni)
    }

    @Test fun solutionRevealAndStruggledBreakTheIndependentChain() {
        fun kolay(i: Int) = OturumSonucu("$i", TekrarSonucu.RAHAT, false)
        val on = (1..10).map(::kolay)
        assertEquals(10, RozetKurallari.enUzunBagimsizZincir(on))
        val ara = on.toMutableList().apply { this[5] = this[5].copy(cozumAcildi = true) }
        assertEquals(5, RozetKurallari.enUzunBagimsizZincir(ara))
        ara[5] = ara[5].copy(cozumAcildi = false, sonuc = TekrarSonucu.ZORLANDIM)
        assertEquals(5, RozetKurallari.enUzunBagimsizZincir(ara))
    }

    @Test fun allConditionsCanEarnAllThirtyAndSecondEvaluationIsEmpty() {
        val girdi = bos().copy(
            oturum = KarneSayilari(20, 20, 0, 0, 0), seri = 100, toplamBagimsiz = 1000,
            sonBesGun = (1..5).map { GunDenge(it, 10, 10) }, soruSayisi = 250,
            tekrarSayisi = 1000, hedefGunSayisi = 30, birGundeDersSayisi = 3, toplamDersSayisi = 5,
            donusumVar = true, bagimsizZincir = 20,
        )
        val yeni = RozetKurallari.kazanilanlar(girdi, emptySet())
        assertEquals(30, yeni.size)
        assertTrue(RozetKurallari.kazanilanlar(girdi, yeni.toSet()).isEmpty())
    }
}

package com.keremtulu.questionnote.domain.repeat

import com.keremtulu.questionnote.domain.model.OgrenmeDurumu
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.SoruPlanDurumu
import org.junit.Assert.*
import org.junit.Test

class GunlukCalismaPlaniTest {
    private fun soru(i: Int, gun: Int = 90) = Soru(
        id = "s-$i", dersId = "ders", konuId = null, soruNotu = null, cozumNotu = null,
        kaynak = null, yanlisNedeni = null, favori = false, planDurumu = SoruPlanDurumu.AKTIF,
        ogrenmeDurumu = OgrenmeDurumu.YENI, sonTekrarGun = null, sonrakiTekrarGun = gun,
        olusturma = i.toLong(), guncelleme = 0,
    )

    @Test fun hundredOverdueQuestionsShowOnlyThreePreviewsWithoutLosingAnyWork() {
        val sorular = (1..100).map { soru(it) }
        val plan = GunlukCalismaPlani.olustur(sorular, 100, 10, 0)
        assertEquals(100, plan.geciken)
        assertEquals(100, plan.sirali.size)
        assertEquals(10, plan.tur.size)
        assertEquals(3, plan.onizleme.size)
        assertEquals(90, plan.kalan)
        assertEquals(sorular, plan.sirali)
        assertTrue(sorular.all { it.sonrakiTekrarGun == 90 })
    }

    @Test fun previewAndSessionFollowTheSameOldestFirstOrder() {
        val plan = GunlukCalismaPlani.olustur(listOf(soru(1, 100), soru(2, 95), soru(3, 80)), 100, 10, 0)
        assertEquals(listOf("s-3", "s-2", "s-1"), plan.onizleme.map { it.id })
        assertEquals(plan.tur, plan.onizleme)
        assertEquals(2, plan.geciken)
        assertEquals(0, plan.kalan)
    }

    @Test fun completedGoalOffersSmallOptionalRoundAndEmptyQueueStaysEmpty() {
        val plan = GunlukCalismaPlani.olustur((1..50).map { soru(it) }, 100, 10, 14)
        assertEquals(5, plan.tur.size)
        assertEquals(45, plan.kalan)
        val empty = GunlukCalismaPlani.olustur(emptyList(), 100, 10, 0)
        assertTrue(empty.tur.isEmpty())
        assertTrue(empty.onizleme.isEmpty())
        assertEquals(0, empty.kalan)
    }

    @Test fun largeTargetIsCappedAndRemainingGoalIsRespected() {
        val sorular = (1..100).map { soru(it) }
        assertEquals(30, GunlukCalismaPlani.olustur(sorular, 100, 100, 0).tur.size)
        assertEquals(8, GunlukCalismaPlani.olustur(sorular, 100, 10, 2).tur.size)
    }
}

package com.keremtulu.questionnote.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class OgretmenIsOzetiTest {
    private fun soru(id: String, gun: Long?, durum: OgretmenDurum = OgretmenDurum.BEKLIYOR) =
        OgretmenSorusu(id = id, grupId = "grup", planGun = gun, durum = durum,
            gorseller = emptyList(), olusturma = 0, guncelleme = 0)

    @Test fun dateBucketsAreDisjointAndSolvedAndArchivedQuestionsAreExcluded() {
        val ozet = OgretmenIsOzeti.hesapla(listOf(
            soru("geciken", 99), soru("bugun", 100), soru("yarin", 101), soru("plansiz", null),
            soru("cozuldu", 90, OgretmenDurum.COZULDU), soru("arsiv", null, OgretmenDurum.ARSIV),
        ), 100)
        assertEquals(OgretmenIsOzeti(1, 1, 1, 1), ozet)
        assertEquals(4, ozet.toplam)
    }

    @Test fun dateChangeMovesTodayIntoOverdueWithoutChangingQuestions() {
        val soru = soru("soru", 100)
        assertEquals(OgretmenIsOzeti(0, 1, 0, 0), OgretmenIsOzeti.hesapla(listOf(soru), 100))
        assertEquals(OgretmenIsOzeti(1, 0, 0, 0), OgretmenIsOzeti.hesapla(listOf(soru), 101))
        assertEquals(100L, soru.planGun)
    }

    @Test fun emptyGroupDoesNotInventWork() {
        assertEquals(0, OgretmenIsOzeti.hesapla(emptyList(), 100).toplam)
    }
}

package com.keremtulu.questionnote.data.local

import androidx.room.Room
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.local.entity.TekrarEntity
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlinx.coroutines.runBlocking

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class IlerlemeSayimiTest {
    private lateinit var db: YanlisDefteriDatabase
    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle()).allowMainThreadQueries().build()
    }
    @After fun close() { db.close() }
    private suspend fun soru(id: String = "q", ders: Int = 0) {
        db.soruDao().ekle(SoruEntity(id, db.dersDao().tumunuGetir()[ders].id, olusturma = 1, guncelleme = 1))
    }
    private suspend fun tekrar(id: String, soru: String = "q", gun: Int, sonuc: String, cozum: Boolean = false) {
        db.tekrarDao().ekle(TekrarEntity(id, soru, gun.toLong(), gun, sonuc = sonuc, cozumAcildi = cozum))
    }

    @Test fun allAggregatesUseTheSameStrictIndependentDefinitionAsTheReport() = runBlocking {
        soru()
        tekrar("easy", gun = 1, sonuc = "RAHAT")
        tekrar("struggle", gun = 2, sonuc = "ZORLANDIM")
        tekrar("solution", gun = 3, sonuc = "RAHAT", cozum = true)
        tekrar("fail", gun = 4, sonuc = "COZEMEDIM")
        assertEquals(1, db.ilerlemeDao().bagimsizTekrarSayisi())
        assertEquals(1, db.tekrarDao().toplamBagimsiz())
        assertEquals(1L, db.tekrarDao().gunDengeleri(1, 4).sumOf { it.bagimsiz })
        assertEquals(1L, db.ilerlemeDao().gunleriBagimsiz().sumOf { it.adet })
        assertEquals(4, db.ilerlemeDao().tekrarSayisi())
    }

    @Test fun comebackRequiresThreeEarlierUnassistedFailuresAndALaterIndependentAnswer() = runBlocking {
        soru()
        tekrar("f1", gun = 1, sonuc = "COZEMEDIM")
        tekrar("f2", gun = 2, sonuc = "COZEMEDIM")
        tekrar("f3", gun = 3, sonuc = "COZEMEDIM")
        tekrar("s", gun = 4, sonuc = "ZORLANDIM")
        assertFalse(db.ilerlemeDao().donusumVar())
        tekrar("r", gun = 5, sonuc = "RAHAT")
        assertTrue(db.ilerlemeDao().donusumVar())
    }

    @Test fun lessonDiversityCountsDistinctLessonsNotRepeatedQuestions() = runBlocking {
        soru("a", 0); soru("b", 1); soru("c", 2)
        tekrar("a1", "a", 1, "RAHAT"); tekrar("a2", "a", 2, "RAHAT")
        tekrar("b1", "b", 1, "RAHAT"); tekrar("c1", "c", 1, "RAHAT")
        assertEquals(3, db.ilerlemeDao().gunDersSayisi(1))
        assertEquals(1, db.ilerlemeDao().gunDersSayisi(2))
        assertEquals(3, db.ilerlemeDao().calisilanDersSayisi())
        assertEquals(1, db.ilerlemeDao().hedefTamamlananGunSayisi(3))
    }
}

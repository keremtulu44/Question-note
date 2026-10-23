package com.keremtulu.questionnote.data.repository

import android.content.Context
import androidx.room.Room
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.prefs.SinavProfiliDeposu
import com.keremtulu.questionnote.domain.model.SinavProfili
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SinavProfiliRepositoryTest {
    private lateinit var db: YanlisDefteriDatabase
    private lateinit var repo: RoomDersRepository
    @Before fun setUp() {
        val context: Context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("sinav_profili", Context.MODE_PRIVATE).edit().clear().commit()
        db = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle()).allowMainThreadQueries().build()
        repo = RoomDersRepository(db, SinavProfiliDeposu(context))
    }
    @After fun close() { db.close() }

    @Test fun lgsAddsTheRightSixLessonsReusingMathAndPreservingExistingData() = runBlocking {
        val once = repo.dersleriIzle(false).first()
        val math = once.first { it.ad == "Matematik" }
        val old = once.first { it.ad == "Edebiyat" }
        db.soruDao().ekle(SoruEntity("old-question", old.id, olusturma = 10, guncelleme = 10))
        val topics = RoomKonuRepository(db)
        topics.konuEkle(old.id, "Eski konu")
        repo.sinavProfiliniUygula(SinavProfili.LGS)
        val gorunen = repo.aktifDersleriIzle().first()
        assertEquals(SinavProfili.LGS.dersAdlari.toSet(), gorunen.map { it.ad }.toSet())
        assertEquals(math.id, gorunen.first { it.ad == "Matematik" }.id)
        assertNotNull(db.soruDao().idIleGetir("old-question"))
        assertEquals("Eski konu", topics.konulariIzle(old.id, false).first().single().ad)
        assertEquals(once.size + 5, repo.dersleriIzle(false).first().size)
        assertFalse(requireNotNull(db.dersDao().idIleGetir(old.id)).arsivli)
    }

    @Test fun repeatedSwitchesAreIdempotentAndAllModeRestoresVisibility() = runBlocking {
        repo.sinavProfiliniUygula(SinavProfili.LGS)
        val first = repo.dersleriIzle(false).first().map { it.id }.toSet()
        repo.sinavProfiliniUygula(SinavProfili.LGS)
        assertEquals(first, repo.dersleriIzle(false).first().map { it.id }.toSet())
        repo.sinavProfiliniUygula(SinavProfili.YKS)
        assertEquals(SinavProfili.YKS.dersAdlari.toSet(), repo.aktifDersleriIzle().first().map { it.ad }.toSet())
        repo.sinavProfiliniUygula(SinavProfili.SINIF_9_10)
        assertEquals(SinavProfili.SINIF_9_10.dersAdlari.toSet(), repo.aktifDersleriIzle().first().map { it.ad }.toSet())
    }

    @Test fun archivedLessonsStayArchivedAndRenamedSelectedLessonKeepsItsMembership() = runBlocking {
        val math = repo.dersleriIzle(false).first().first { it.ad == "Matematik" }
        repo.dersArsivDurumunuDegistir(math.id, true)
        repo.sinavProfiliniUygula(SinavProfili.LGS)
        assertEquals(5, repo.aktifDersleriIzle().first().size)
        assertEquals(math.id, repo.dersleriIzle(true).first().single().id)
        val english = repo.aktifDersleriIzle().first().first { it.ad == "İngilizce" }
        repo.dersYenidenAdlandir(english.id, "Yabancı dil")
        assertTrue(repo.aktifDersleriIzle().first().any { it.id == english.id })
    }
}

package com.keremtulu.questionnote.data.repository

import androidx.room.Room
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.domain.model.DersIslemSonucu
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomDersRepositoryTest {
    private lateinit var database: YanlisDefteriDatabase
    private lateinit var repository: RoomDersRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            YanlisDefteriDatabase::class.java,
        )
            .addCallback(VarsayilanDersleriEkle())
            .allowMainThreadQueries()
            .build()
        repository = RoomDersRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addAndRenameTrimNamesAndRejectTurkishCaseInsensitiveDuplicates() = runBlocking {
        assertEquals(DersIslemSonucu.Basarili, repository.dersEkle("  İstatistik  "))
        assertEquals(DersIslemSonucu.AyniAdVar, repository.dersEkle("istatistik"))
        val added = repository.dersleriIzle(arsivli = false).first().last()
        assertEquals("İstatistik", added.ad)

        assertEquals(DersIslemSonucu.BosAd, repository.dersYenidenAdlandir(added.id, "  "))
        assertEquals(
            DersIslemSonucu.AyniAdVar,
            repository.dersYenidenAdlandir(added.id, " MATEMATİK "),
        )
        assertEquals(
            DersIslemSonucu.Basarili,
            repository.dersYenidenAdlandir(added.id, "  İstatistik II  "),
        )
        assertEquals("İstatistik II", repository.dersleriIzle(false).first().last().ad)
    }

    @Test
    fun archiveAndRestoreKeepsQuestionAndOrderCanBeChanged() = runBlocking {
        val dersler = repository.dersleriIzle(arsivli = false).first()
        val edebiyat = dersler.first()
        val now = System.currentTimeMillis()
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO sorular (id, ders_id, olusturma, guncelleme) VALUES (?, ?, ?, ?)",
            arrayOf<Any?>("lesson-archive-question", edebiyat.id, now, now),
        )

        assertEquals(DersIslemSonucu.Basarili, repository.dersArsivDurumunuDegistir(edebiyat.id, true))
        assertEquals(0, repository.dersleriIzle(false).first().count { it.id == edebiyat.id })
        val archived = repository.dersleriIzle(true).first().single()
        assertEquals(edebiyat.id, archived.id)
        assertEquals(1, archived.soruSayisi)
        assertEquals(1, database.openHelper.readableDatabase.query(
            "SELECT COUNT(*) FROM sorular WHERE id = 'lesson-archive-question'",
        ).use { it.moveToFirst(); it.getInt(0) })

        assertEquals(DersIslemSonucu.Basarili, repository.dersArsivDurumunuDegistir(edebiyat.id, false))
        val restored = repository.dersleriIzle(false).first()
        assertEquals(edebiyat.id, restored.first().id)
        assertEquals(1, restored.first().soruSayisi)
        assertEquals(DersIslemSonucu.Basarili, repository.dersSirasiniDegistir(restored.first().id, 1))
        assertEquals("Matematik", repository.dersleriIzle(false).first().first().ad)
        assertTrue(repository.dersleriIzle(true).first().isEmpty())
    }

    @Test
    fun archivedNamesRemainReservedAcrossTheWholeCatalog() = runBlocking {
        val fizik = repository.dersleriIzle(false).first().first { it.ad == "Fizik" }
        assertEquals(DersIslemSonucu.Basarili, repository.dersArsivDurumunuDegistir(fizik.id, true))
        assertEquals(DersIslemSonucu.AyniAdVar, repository.dersEkle(" fizik "))
    }
}

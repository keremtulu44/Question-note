package com.keremtulu.questionnote.data.repository

import androidx.room.Room
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.domain.model.KonuIslemSonucu
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
class RoomKonuRepositoryTest {
    private lateinit var database: YanlisDefteriDatabase
    private lateinit var repository: RoomKonuRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            YanlisDefteriDatabase::class.java,
        )
            .addCallback(VarsayilanDersleriEkle())
            .allowMainThreadQueries()
            .build()
        repository = RoomKonuRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addingTrimsNamesAndRejectsTurkishCaseInsensitiveDuplicates() = runBlocking {
        val dersId = database.dersDao().tumunuGetir().first().id

        assertEquals(
            KonuIslemSonucu.Basarili,
            repository.konuEkle(dersId, "  İNTEGRALLER  "),
        )
        assertEquals(
            KonuIslemSonucu.AyniAdVar,
            repository.konuEkle(dersId, "integraller"),
        )

        val konular = repository.konulariIzle(dersId, arsivli = false).first()
        assertEquals(listOf("İNTEGRALLER"), konular.map { it.ad })
        assertEquals(listOf(0), konular.map { it.sira })
        assertTrue(konular.none { it.arsivli })
    }

    @Test
    fun addingTopicKeepsExistingTopicsInEachLesson() = runBlocking {
        val dersler = database.dersDao().tumunuGetir()
        val eskiKonular = dersler.mapIndexed { index, ders ->
            assertEquals(
                KonuIslemSonucu.Basarili,
                repository.konuEkle(ders.id, "Eski konu $index"),
            )
            ders.id to repository.konulariIzle(ders.id, arsivli = false).first().single()
        }.toMap()

        assertEquals(
            KonuIslemSonucu.Basarili,
            repository.konuEkle(dersler.first().id, "Yeni konu"),
        )

        dersler.forEachIndexed { index, ders ->
            val konular = repository.konulariIzle(ders.id, arsivli = false).first()
            val expectedNames = if (index == 0) {
                listOf("Eski konu 0", "Yeni konu")
            } else {
                listOf("Eski konu $index")
            }
            assertEquals(expectedNames, konular.map { it.ad })
            assertEquals(eskiKonular.getValue(ders.id).id, konular.first().id)
        }
    }

    @Test
    fun renamingRejectsBlankAndDuplicateNamesAndKeepsOrder() = runBlocking {
        val dersId = database.dersDao().tumunuGetir().first().id
        assertEquals(KonuIslemSonucu.Basarili, repository.konuEkle(dersId, "Fonksiyonlar"))
        assertEquals(KonuIslemSonucu.Basarili, repository.konuEkle(dersId, "Denklemler"))
        val konular = repository.konulariIzle(dersId, arsivli = false).first()

        assertEquals(KonuIslemSonucu.BosAd, repository.konuYenidenAdlandir(konular[0].id, "   "))
        assertEquals(
            KonuIslemSonucu.AyniAdVar,
            repository.konuYenidenAdlandir(konular[0].id, " denklemler "),
        )
        assertEquals(
            KonuIslemSonucu.Basarili,
            repository.konuYenidenAdlandir(konular[0].id, "  Fonksiyonlar 2  "),
        )

        val renamed = repository.konulariIzle(dersId, arsivli = false).first()
        assertEquals(listOf("Fonksiyonlar 2", "Denklemler"), renamed.map { it.ad })
        assertEquals(listOf(0, 1), renamed.map { it.sira })
    }

    @Test
    fun archivingAndRestoringKeepsQuestionTopicRelationship() = runBlocking {
        val dersId = database.dersDao().tumunuGetir().first().id
        assertEquals(KonuIslemSonucu.Basarili, repository.konuEkle(dersId, "Fonksiyonlar"))
        val konu = repository.konulariIzle(dersId, arsivli = false).first().single()
        val now = System.currentTimeMillis()
        val soruId = "test-soru-topic-link"
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO sorular (id, ders_id, konu_id, olusturma, guncelleme) VALUES (?, ?, ?, ?, ?)",
            arrayOf<Any?>(soruId, dersId, konu.id, now, now),
        )

        assertEquals(KonuIslemSonucu.Basarili, repository.konuArsivle(konu.id))
        assertTrue(repository.konulariIzle(dersId, arsivli = false).first().isEmpty())
        assertEquals(listOf(konu.id), repository.konulariIzle(dersId, arsivli = true).first().map { it.id })
        assertEquals(KonuIslemSonucu.AyniAdVar, repository.konuEkle(dersId, "Fonksiyonlar"))
        assertEquals(konu.id, soruKonuId(soruId))

        assertEquals(KonuIslemSonucu.Basarili, repository.konuYenidenEtkinlestir(konu.id))
        val aktifKonular = repository.konulariIzle(dersId, arsivli = false).first()
        assertEquals(listOf(konu.id), aktifKonular.map { it.id })
        assertTrue(repository.konulariIzle(dersId, arsivli = true).first().isEmpty())
        assertEquals(konu.id, soruKonuId(soruId))
    }

    private fun soruKonuId(soruId: String): String {
        val cursor = database.openHelper.readableDatabase.query(
            "SELECT konu_id FROM sorular WHERE id = ?",
            arrayOf(soruId),
        )
        return cursor.use {
            assertTrue(it.moveToFirst())
            it.getString(0)
        }
    }
}

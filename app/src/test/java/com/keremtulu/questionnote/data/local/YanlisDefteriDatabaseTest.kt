package com.keremtulu.questionnote.data.local

import androidx.room.Room
import com.keremtulu.questionnote.data.repository.RoomDersRepository
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
class YanlisDefteriDatabaseTest {
    private lateinit var database: YanlisDefteriDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            YanlisDefteriDatabase::class.java,
        )
            .addCallback(VarsayilanDersleriEkle())
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun freshDatabaseSeedsTheSevenSubjectsInStableOrder() {
        val dersler = database.dersDao().tumunuGetir()

        assertEquals(VarsayilanDersAdlari, dersler.map { it.ad })
        assertEquals((0..6).toList(), dersler.map { it.sira })
        assertTrue(dersler.none { it.arsivli })
        assertTrue(dersler.all { it.id.isNotBlank() && it.olusturma > 0L })
    }

    @Test
    fun repositoryStreamsActiveSubjectsAndTheirQuestionCountsInStableOrder() = runBlocking {
        val sqlite = database.openHelper.writableDatabase
        val firstDers = database.dersDao().tumunuGetir().first()
        sqlite.execSQL("UPDATE dersler SET arsivli = 1 WHERE sira = 1")
        sqlite.execSQL(
            "INSERT INTO sorular (id, ders_id, olusturma, guncelleme) VALUES (?, ?, ?, ?)",
            arrayOf<Any?>("test-soru-1", firstDers.id, 1L, 1L),
        )

        val dersler = RoomDersRepository(database.dersDao())
            .aktifDersleriIzle()
            .first()

        assertEquals(
            VarsayilanDersAdlari.filterIndexed { index, _ -> index != 1 },
            dersler.map { it.ad },
        )
        assertEquals(
            (0..6).filter { it != 1 },
            dersler.map { it.sira },
        )
        assertEquals(1, dersler.first().soruSayisi)
        assertTrue(dersler.drop(1).all { it.soruSayisi == 0 })
    }

    @Test
    fun diskDatabaseSeedsOnceAndKeepsSubjectsAfterReopen() {
        val context = RuntimeEnvironment.getApplication()
        val databaseName = "room-seed-test-${System.nanoTime()}.db"

        fun openDiskDatabase() = Room.databaseBuilder(
            context,
            YanlisDefteriDatabase::class.java,
            databaseName,
        )
            .addCallback(VarsayilanDersleriEkle())
            .allowMainThreadQueries()
            .build()

        try {
            val firstOpen = openDiskDatabase()
            val firstRows = try {
                firstOpen.dersDao().tumunuGetir()
            } finally {
                firstOpen.close()
            }

            val secondOpen = openDiskDatabase()
            val secondRows = try {
                secondOpen.dersDao().tumunuGetir()
            } finally {
                secondOpen.close()
            }

            assertEquals(VarsayilanDersAdlari, firstRows.map { it.ad })
            assertEquals(firstRows, secondRows)
            assertEquals(7, secondRows.size)
        } finally {
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun schemaCreatesAllPlannedTablesAtVersionOne() {
        val sqlite = database.openHelper.readableDatabase
        val cursor = sqlite.query("PRAGMA user_version")
        val version = cursor.use {
            assertTrue(it.moveToFirst())
            it.getInt(0)
        }
        assertEquals(1, version)

        val namesCursor = sqlite.query("SELECT name FROM sqlite_master WHERE type = 'table'")
        val tableNames = namesCursor.use { rows ->
            buildSet {
                while (rows.moveToNext()) add(rows.getString(0))
            }
        }
        assertTrue(tableNames.containsAll(setOf("dersler", "konular", "sorular", "gorseller", "tekrarlar")))
    }

    @Test
    fun questionAndFileHistoryForeignKeysAreExplicit() {
        val sqlite = database.openHelper.readableDatabase
        assertEquals(setOf("dersler", "konular"), foreignKeyParents(sqlite, "sorular"))
        assertEquals(setOf("sorular"), foreignKeyParents(sqlite, "gorseller"))
        assertEquals(setOf("sorular"), foreignKeyParents(sqlite, "tekrarlar"))
        assertEquals(setOf("CASCADE"), foreignKeyDeleteActions(sqlite, "gorseller"))
        assertEquals(setOf("CASCADE"), foreignKeyDeleteActions(sqlite, "tekrarlar"))
    }

    private fun foreignKeyParents(database: androidx.sqlite.db.SupportSQLiteDatabase, table: String): Set<String> {
        val cursor = database.query("PRAGMA foreign_key_list(`$table`)")
        return cursor.use { rows ->
            buildSet {
                val tableColumn = rows.getColumnIndexOrThrow("table")
                while (rows.moveToNext()) add(rows.getString(tableColumn))
            }
        }
    }

    private fun foreignKeyDeleteActions(database: androidx.sqlite.db.SupportSQLiteDatabase, table: String): Set<String> {
        val cursor = database.query("PRAGMA foreign_key_list(`$table`)")
        return cursor.use { rows ->
            buildSet {
                val actionColumn = rows.getColumnIndexOrThrow("on_delete")
                while (rows.moveToNext()) add(rows.getString(actionColumn))
            }
        }
    }
}

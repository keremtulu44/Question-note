package com.keremtulu.questionnote.data.local

import androidx.room.Room
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

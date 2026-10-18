package com.keremtulu.questionnote.data.local

import androidx.room.Room
import java.io.File
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OlcumMigrationTest {
    @Test fun realVersionThreeSchemaMigratesWithoutRewritingReviewsOrInventingMeasurements() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val name = "measurement-migration-${System.nanoTime()}.db"
        val file = context.getDatabasePath(name).apply { parentFile?.mkdirs() }
        val schemaFile = listOf(File("schemas/com.keremtulu.questionnote.data.local.YanlisDefteriDatabase/3.json"),
            File("app/schemas/com.keremtulu.questionnote.data.local.YanlisDefteriDatabase/3.json")).first { it.isFile }
        val schema = JSONObject(schemaFile.readText()).getJSONObject("database")
        val old = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(file, null)
        try {
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                val table = entity.getString("tableName")
                old.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                val indices = entity.getJSONArray("indices")
                for (j in 0 until indices.length()) old.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", table))
            }
            old.execSQL("CREATE TABLE room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
            old.execSQL("INSERT INTO room_master_table VALUES (42, ?)", arrayOf(schema.getString("identityHash")))
            old.execSQL("PRAGMA user_version = 3")
            old.execSQL("INSERT INTO dersler (id, ad, sira, olusturma, guncelleme) VALUES ('lesson', 'Eski ders', 0, 1, 2)")
            old.execSQL("INSERT INTO sorular (id, ders_id, kaynak, kaynak_konumu, son_tekrar_gun, sonraki_tekrar_gun, olusturma, guncelleme) VALUES ('q', 'lesson', 'Eski kitap', 's. 3', 200, 218, 1, 2)")
            old.execSQL("INSERT INTO tekrarlar (id, soru_id, zaman, gun, sonuc, onceki_aralik, sonraki_aralik, algoritma_surumu) VALUES ('r', 'q', 12345, 200, 'RAHAT', 7, 18, 2)")
        } finally { old.close() }
        val db = Room.databaseBuilder(context, YanlisDefteriDatabase::class.java, name)
            .addMigrations(YanlisDefteriMigrations.MIGRATION_3_4).allowMainThreadQueries().build()
        try {
            assertEquals(4, db.openHelper.readableDatabase.version)
            val tekrar = db.tekrarDao().tumunuGetir().single()
            assertEquals(12345L, tekrar.zaman)
            assertEquals(18, tekrar.sonrakiAralik)
            assertEquals(2, tekrar.algoritmaSurumu)
            assertEquals(218, db.soruDao().idIleGetir("q")?.sonrakiTekrarGun)
            assertEquals("s. 3", db.soruDao().idIleGetir("q")?.kaynakKonumu)
            assertTrue(db.tekrarOlcumDao().tumunuGetir().isEmpty())
            assertEquals(1, db.dersDao().tumunuGetir().size)
            val indices = db.openHelper.readableDatabase.query("SELECT name FROM sqlite_master WHERE type='index'").use { cursor ->
                buildSet { while (cursor.moveToNext()) add(cursor.getString(0)) }
            }
            assertTrue("index_tekrarlar_gun" in indices)
            assertTrue("index_tekrar_olcumleri_deneme_id" in indices)
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun freshSchemaProvidesIndexedContextAndDayQueries() {
        val db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), YanlisDefteriDatabase::class.java)
            .allowMainThreadQueries().build()
        try {
            val plan = db.openHelper.readableDatabase.query("EXPLAIN QUERY PLAN SELECT * FROM tekrar_olcumleri WHERE ders_id='lesson' AND konu_id='topic'")
                .use { rows -> buildList { while (rows.moveToNext()) add(rows.getString(3)) }.joinToString() }
            assertTrue(plan.contains("index_tekrar_olcumleri_ders_id_konu_id"))
            val day = db.openHelper.readableDatabase.query("EXPLAIN QUERY PLAN SELECT * FROM tekrarlar WHERE gun BETWEEN 1 AND 60")
                .use { rows -> buildList { while (rows.moveToNext()) add(rows.getString(3)) }.joinToString() }
            assertTrue(day.contains("index_tekrarlar_gun"))
        } finally { db.close() }
    }
}

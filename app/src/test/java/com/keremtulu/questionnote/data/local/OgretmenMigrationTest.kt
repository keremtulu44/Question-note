package com.keremtulu.questionnote.data.local

import androidx.room.Room
import com.keremtulu.questionnote.data.local.entity.OgretmenGrupEntity
import com.keremtulu.questionnote.data.local.entity.OgretmenSoruEntity
import com.keremtulu.questionnote.data.local.entity.OgretmenSoruGorselEntity
import java.io.File
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Room v4 -> v5 covers the teacher workspace. The migration only adds tables and indices, so the
 * test also proves the student rows survive untouched and that the new foreign keys really cascade.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OgretmenMigrationTest {

    private fun schemaDosyasi(): File = listOf(
        File("schemas/com.keremtulu.questionnote.data.local.YanlisDefteriDatabase/4.json"),
        File("app/schemas/com.keremtulu.questionnote.data.local.YanlisDefteriDatabase/4.json"),
    ).first { it.isFile }

    @Test
    fun versionFourAddsTeacherTablesWithoutTouchingStudentRows() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val name = "ogretmen-migration-${System.nanoTime()}.db"
        val file = context.getDatabasePath(name).apply { parentFile?.mkdirs() }
        val schema = JSONObject(schemaDosyasi().readText()).getJSONObject("database")
        val old = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(file, null)
        try {
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                val table = entity.getString("tableName")
                old.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                val indices = entity.optJSONArray("indices") ?: JSONArray()
                for (j in 0 until indices.length()) {
                    old.execSQL(
                        indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", table),
                    )
                }
            }
            old.execSQL("CREATE TABLE room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
            old.execSQL(
                "INSERT INTO room_master_table VALUES (42, ?)",
                arrayOf(schema.getString("identityHash")),
            )
            old.execSQL("PRAGMA user_version = 4")
            old.execSQL(
                "INSERT INTO dersler (id, ad, sira, olusturma, guncelleme) " +
                    "VALUES ('mat', 'Matematik', 0, 1, 2)",
            )
        } finally {
            old.close()
        }

        val db = Room.databaseBuilder(context, YanlisDefteriDatabase::class.java, name)
            .addMigrations(
                YanlisDefteriMigrations.MIGRATION_1_2,
                YanlisDefteriMigrations.MIGRATION_2_3,
                YanlisDefteriMigrations.MIGRATION_3_4,
                YanlisDefteriMigrations.MIGRATION_4_5,
            )
            .allowMainThreadQueries()
            .build()
        try {
            assertEquals(5, db.openHelper.readableDatabase.version)
            // Student data survives.
            assertEquals("Matematik", db.dersDao().tumunuGetir().single().ad)
            // Teacher tables start empty.
            assertEquals(0, db.ogretmenDao().grupSayisi())
            assertEquals(0, db.ogretmenDao().soruSayisi())

            // Every new index is present, otherwise the queue query would fall back to a scan.
            val indices = db.openHelper.readableDatabase
                .query("SELECT name FROM sqlite_master WHERE type='index'")
                .use { cursor -> buildSet { while (cursor.moveToNext()) add(cursor.getString(0)) } }
            listOf(
                "index_ogretmen_gruplari_sira",
                "index_ogretmen_gruplari_ders_id",
                "index_ogretmen_sorulari_grup_id",
                "index_ogretmen_sorulari_ders_id",
                "index_ogretmen_sorulari_konu_id",
                "index_ogretmen_sorulari_durum",
                "index_ogretmen_sorulari_plan_gun",
                "index_ogretmen_soru_gorselleri_soru_id",
            ).forEach { beklenen ->
                assertTrue("Eksik indeks: $beklenen", beklenen in indices)
            }

            // Writes and ON DELETE CASCADE behave as the store expects.
            db.ogretmenDao().grupEkle(
                OgretmenGrupEntity(id = "g1", ad = "8A", dersId = "mat", sira = 0, olusturma = 1),
            )
            db.ogretmenDao().soruEkle(
                OgretmenSoruEntity(id = "q1", grupId = "g1", planGun = 200, olusturma = 2, guncelleme = 2),
            )
            db.ogretmenDao().gorselEkle(
                listOf(
                    OgretmenSoruGorselEntity(
                        id = "img1",
                        soruId = "q1",
                        sira = 0,
                        dosyaAdi = "ogretmen-img/a.jpg",
                        genislik = 10,
                        yukseklik = 10,
                        boyut = 100,
                        sha256 = "x",
                    ),
                ),
            )
            assertEquals(1, db.ogretmenDao().soruSayisi())
            db.ogretmenDao().grupSil("g1")
            assertEquals(0, db.ogretmenDao().soruSayisi())
            assertTrue(db.ogretmenDao().gorselleriGetir().isEmpty())

            // Deleting a catalog lesson must null the tag, never delete the teacher row.
            db.ogretmenDao().grupEkle(
                OgretmenGrupEntity(id = "g2", ad = "7B", dersId = "mat", sira = 1, olusturma = 3),
            )
            // DersDao has no delete-by-id; the catalog restore path issues the same statement.
            db.openHelper.writableDatabase.execSQL("DELETE FROM dersler WHERE id = 'mat'")
            assertEquals("7B", db.ogretmenDao().gruplariGetir().single { it.id == "g2" }.ad)
            assertEquals(null, db.ogretmenDao().gruplariGetir().single { it.id == "g2" }.dersId)
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun freshSchemaUsesTeacherIndicesForQueueAndGroupQueries() {
        val db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            YanlisDefteriDatabase::class.java,
        ).allowMainThreadQueries().build()
        try {
            val durumPlani = db.openHelper.readableDatabase
                .query("EXPLAIN QUERY PLAN SELECT * FROM ogretmen_sorulari WHERE durum = 'BEKLIYOR'")
                .use { rows -> buildList { while (rows.moveToNext()) add(rows.getString(3)) }.joinToString() }
            assertTrue(durumPlani, durumPlani.contains("index_ogretmen_sorulari_durum"))

            val grupPlani = db.openHelper.readableDatabase
                .query("EXPLAIN QUERY PLAN SELECT * FROM ogretmen_sorulari WHERE grup_id = 'g1'")
                .use { rows -> buildList { while (rows.moveToNext()) add(rows.getString(3)) }.joinToString() }
            assertTrue(grupPlani, grupPlani.contains("index_ogretmen_sorulari_grup_id"))
        } finally {
            db.close()
        }
    }
}

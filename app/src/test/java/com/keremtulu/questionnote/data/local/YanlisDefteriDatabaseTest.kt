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

        val dersler = RoomDersRepository(database)
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
    fun schemaCreatesAllPlannedTablesAtVersionFour() = runBlocking {
        val sqlite = database.openHelper.readableDatabase
        val cursor = sqlite.query("PRAGMA user_version")
        val version = cursor.use {
            assertTrue(it.moveToFirst())
            it.getInt(0)
        }
        assertEquals(4, version)

        val namesCursor = sqlite.query("SELECT name FROM sqlite_master WHERE type = 'table'")
        val tableNames = namesCursor.use { rows ->
            buildSet {
                while (rows.moveToNext()) add(rows.getString(0))
            }
        }
        assertTrue(tableNames.containsAll(setOf(
            "dersler", "konular", "sorular", "gorseller", "tekrarlar", "geri_yukleme_isaretleri", "soru_secenekleri", "tekrar_olcumleri",
        )))
        assertEquals(VarsayilanSoruNedenleri, database.soruSecenegiDao().tumunuGetir()
            .filter { it.varsayilan }
            .map { it.ad })
    }

    @Test
    fun migrationOneToFourAddsOptionsAndRecoveryMarkerWithoutLosingExistingRows() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val databaseName = "migration-preserve-${System.nanoTime()}.db"
        val file = context.getDatabasePath(databaseName)
        file.parentFile?.mkdirs()
        val legacy = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(file, null)
        try {
            LEGACY_V1_SCHEMA.forEach { legacy.execSQL(it) }
            legacy.execSQL("CREATE TABLE room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
            legacy.execSQL(
                "INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES(42, ?)",
                arrayOf("db016c68a6f4baddd5a20169d44768ba"),
            )
            legacy.execSQL("PRAGMA user_version = 1")
            legacy.execSQL(
                "INSERT INTO dersler (id, ad, sira, arsivli, olusturma, guncelleme) VALUES (?, ?, ?, 0, ?, ?)",
                arrayOf<Any?>("migration-kept-lesson", "Kaybolmayan ders", 0, 1L, 1L),
            )
        } finally {
            legacy.close()
        }

        val migrated = Room.databaseBuilder(
            context,
            YanlisDefteriDatabase::class.java,
            databaseName,
        )
            .addMigrations(YanlisDefteriMigrations.MIGRATION_1_2, YanlisDefteriMigrations.MIGRATION_2_3, YanlisDefteriMigrations.MIGRATION_3_4)
            .allowMainThreadQueries()
            .build()
        try {
            assertEquals("Kaybolmayan ders", migrated.dersDao().idIleGetir("migration-kept-lesson")?.ad)
            assertEquals(null, migrated.geriYuklemeIsaretiDao().idGetir())
            migrated.geriYuklemeIsaretiDao().ekle(
                com.keremtulu.questionnote.data.local.entity.GeriYuklemeIsaretiEntity("migration-kept-marker"),
            )
            assertEquals("migration-kept-marker", migrated.geriYuklemeIsaretiDao().idGetir())
            val version = migrated.openHelper.readableDatabase.query("PRAGMA user_version").use {
                it.moveToFirst()
                it.getInt(0)
            }
            assertEquals(4, version)
            assertEquals(VarsayilanSoruNedenleri, migrated.soruSecenegiDao().tumunuGetir()
                .filter { it.varsayilan }
                .map { it.ad })
        } finally {
            migrated.close()
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun migrationTwoToFourKeepsQuestionTextAndBackfillsGlobalChoices() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val databaseName = "migration-v2-v3-${System.nanoTime()}.db"
        val file = context.getDatabasePath(databaseName)
        file.parentFile?.mkdirs()
        val legacy = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(file, null)
        try {
            LEGACY_V1_SCHEMA.forEach { legacy.execSQL(it) }
            legacy.execSQL(
                "CREATE TABLE IF NOT EXISTS `geri_yukleme_isaretleri` (`id` TEXT NOT NULL, PRIMARY KEY(`id`))",
            )
            legacy.execSQL("CREATE TABLE room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
            legacy.execSQL(
                "INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES(42, ?)",
                arrayOf("f351145897fa5e2e5c9197ee5f7a52b0"),
            )
            legacy.execSQL("PRAGMA user_version = 2")
            legacy.execSQL(
                "INSERT INTO dersler (id, ad, sira, arsivli, olusturma, guncelleme) VALUES (?, ?, ?, 0, ?, ?)",
                arrayOf<Any?>("legacy-option-lesson", "Eski ders", 0, 1L, 1L),
            )
            legacy.execSQL(
                "INSERT INTO sorular (id, ders_id, soru_notu, kaynak, yanlis_nedeni, olusturma, guncelleme) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any?>("legacy-option-question-1", "legacy-option-lesson", "Eski not", "  Test kitabı A  ", "Kendime özgü neden", 2L, 3L),
            )
            legacy.execSQL(
                "INSERT INTO sorular (id, ders_id, kaynak, yanlis_nedeni, olusturma, guncelleme) " +
                    "VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf<Any?>("legacy-option-question-2", "legacy-option-lesson", "test kitabı a", "Yanlış okuma", 4L, 5L),
            )
        } finally {
            legacy.close()
        }

        val migrated = Room.databaseBuilder(
            context,
            YanlisDefteriDatabase::class.java,
            databaseName,
        )
            .addMigrations(YanlisDefteriMigrations.MIGRATION_1_2, YanlisDefteriMigrations.MIGRATION_2_3, YanlisDefteriMigrations.MIGRATION_3_4)
            .allowMainThreadQueries()
            .build()
        try {
            val firstQuestion = migrated.soruDao().idIleGetir("legacy-option-question-1")!!
            assertEquals("Eski not", firstQuestion.soruNotu)
            assertEquals("  Test kitabı A  ", firstQuestion.kaynak)
            assertEquals("Kendime özgü neden", firstQuestion.yanlisNedeni)
            assertEquals(null, firstQuestion.kaynakKonumu)

            val options = migrated.soruSecenegiDao().tumunuGetir()
            val sourceOptions = options.filter { it.tur == "KAYNAK" }
            assertEquals(1, sourceOptions.size)
            assertTrue(sourceOptions.single().ad in setOf("  Test kitabı A  ", "test kitabı a"))
            assertEquals(
                "test kitabı a",
                com.keremtulu.questionnote.domain.model.normalizeSoruSecenegiAdi(sourceOptions.single().ad),
            )
            assertTrue(options.any { it.tur == "YANLIS_NEDENI" && it.ad == "Kendime özgü neden" && !it.varsayilan })
            assertEquals(VarsayilanSoruNedenleri, options.filter { it.varsayilan }.map { it.ad })
            val version = migrated.openHelper.readableDatabase.query("PRAGMA user_version").use {
                it.moveToFirst()
                it.getInt(0)
            }
            assertEquals(4, version)
        } finally {
            migrated.close()
            context.deleteDatabase(databaseName)
        }
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

    private companion object {
        val LEGACY_V1_SCHEMA = listOf(
            """CREATE TABLE IF NOT EXISTS `dersler` (`id` TEXT NOT NULL, `ad` TEXT NOT NULL, `sira` INTEGER NOT NULL, `arsivli` INTEGER NOT NULL DEFAULT 0, `olusturma` INTEGER NOT NULL, `guncelleme` INTEGER NOT NULL, PRIMARY KEY(`id`))""",
            """CREATE INDEX IF NOT EXISTS `index_dersler_sira` ON `dersler` (`sira`)""",
            """CREATE TABLE IF NOT EXISTS `konular` (`id` TEXT NOT NULL, `ders_id` TEXT NOT NULL, `ad` TEXT NOT NULL, `sira` INTEGER NOT NULL, `arsivli` INTEGER NOT NULL DEFAULT 0, `olusturma` INTEGER NOT NULL, `guncelleme` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`ders_id`) REFERENCES `dersler`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )""",
            """CREATE INDEX IF NOT EXISTS `index_konular_ders_id_sira` ON `konular` (`ders_id`, `sira`)""",
            """CREATE TABLE IF NOT EXISTS `sorular` (`id` TEXT NOT NULL, `ders_id` TEXT NOT NULL, `konu_id` TEXT, `soru_notu` TEXT, `cozum_notu` TEXT, `kaynak` TEXT, `yanlis_nedeni` TEXT, `favori` INTEGER NOT NULL DEFAULT 0, `plan_durumu` TEXT NOT NULL DEFAULT 'AKTIF', `ogrenme_durumu` TEXT NOT NULL DEFAULT 'YENI', `son_tekrar_gun` INTEGER, `sonraki_tekrar_gun` INTEGER, `olusturma` INTEGER NOT NULL, `guncelleme` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`ders_id`) REFERENCES `dersler`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`konu_id`) REFERENCES `konular`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )""",
            """CREATE INDEX IF NOT EXISTS `index_sorular_ders_id` ON `sorular` (`ders_id`)""",
            """CREATE INDEX IF NOT EXISTS `index_sorular_konu_id` ON `sorular` (`konu_id`)""",
            """CREATE INDEX IF NOT EXISTS `index_sorular_sonraki_tekrar_gun` ON `sorular` (`sonraki_tekrar_gun`)""",
            """CREATE INDEX IF NOT EXISTS `index_sorular_plan_durumu` ON `sorular` (`plan_durumu`)""",
            """CREATE INDEX IF NOT EXISTS `index_sorular_guncelleme` ON `sorular` (`guncelleme`)""",
            """CREATE TABLE IF NOT EXISTS `gorseller` (`id` TEXT NOT NULL, `soru_id` TEXT NOT NULL, `tur` TEXT NOT NULL, `sira` INTEGER NOT NULL, `dosya_adi` TEXT NOT NULL, `mime_type` TEXT NOT NULL, `genislik` INTEGER NOT NULL, `yukseklik` INTEGER NOT NULL, `boyut` INTEGER NOT NULL, `sha256` TEXT NOT NULL, `olusturma` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`soru_id`) REFERENCES `sorular`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""",
            """CREATE UNIQUE INDEX IF NOT EXISTS `index_gorseller_soru_id_tur_sira` ON `gorseller` (`soru_id`, `tur`, `sira`)""",
            """CREATE TABLE IF NOT EXISTS `tekrarlar` (`id` TEXT NOT NULL, `soru_id` TEXT NOT NULL, `zaman` INTEGER NOT NULL, `gun` INTEGER NOT NULL, `planlanan_gun` INTEGER, `sonuc` TEXT NOT NULL, `cozum_acildi` INTEGER NOT NULL DEFAULT 0, `onceki_aralik` INTEGER, `sonraki_aralik` INTEGER, `algoritma_surumu` INTEGER NOT NULL DEFAULT 1, `elle_atandi` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`), FOREIGN KEY(`soru_id`) REFERENCES `sorular`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""",
            """CREATE INDEX IF NOT EXISTS `index_tekrarlar_soru_id_gun` ON `tekrarlar` (`soru_id`, `gun`)""",
        )
    }
}

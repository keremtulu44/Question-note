package com.keremtulu.questionnote.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.keremtulu.questionnote.domain.model.SoruSecenekTuru
import com.keremtulu.questionnote.domain.model.SORU_SECENEGI_ADI_UST_SINIR
import com.keremtulu.questionnote.domain.model.normalizeSoruSecenegiAdi
import com.keremtulu.questionnote.domain.model.stableSoruSecenegiId

object YanlisDefteriMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `geri_yukleme_isaretleri` (
                    `id` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `sorular` ADD COLUMN `kaynak_konumu` TEXT")
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `soru_secenekleri` (
                    `id` TEXT NOT NULL,
                    `tur` TEXT NOT NULL,
                    `ad` TEXT NOT NULL,
                    `normalize_ad` TEXT NOT NULL,
                    `varsayilan` INTEGER NOT NULL DEFAULT 0,
                    `sira` INTEGER NOT NULL DEFAULT 0,
                    `olusturma` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            database.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_soru_secenekleri_tur_normalize_ad` " +
                    "ON `soru_secenekleri` (`tur`, `normalize_ad`)",
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_soru_secenekleri_tur_varsayilan_sira` " +
                    "ON `soru_secenekleri` (`tur`, `varsayilan`, `sira`)",
            )

            val now = System.currentTimeMillis()
            val insertSql = """
                INSERT OR IGNORE INTO soru_secenekleri
                    (id, tur, ad, normalize_ad, varsayilan, sira, olusturma)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
            fun insertOption(
                type: SoruSecenekTuru,
                name: String,
                isDefault: Boolean = false,
                order: Int = 0,
                id: String = stableSoruSecenegiId(type, normalizeSoruSecenegiAdi(name)),
            ) {
                val normalized = normalizeSoruSecenegiAdi(name)
                if (normalized.isBlank()) return
                database.execSQL(
                    insertSql,
                    arrayOf<Any?>(id, type.name, name, normalized, if (isDefault) 1 else 0, order, now),
                )
            }

            varsayilanSoruSecenekleri(now).forEach { option ->
                insertOption(
                    type = SoruSecenekTuru.YANLIS_NEDENI,
                    name = option.ad,
                    isDefault = true,
                    order = option.sira,
                    id = option.id,
                )
            }

            fun importLegacyColumn(column: String, type: SoruSecenekTuru) {
                val cursor = database.query(
                    "SELECT DISTINCT `$column` FROM `sorular` " +
                        "WHERE `$column` IS NOT NULL AND length(trim(`$column`)) > 0 " +
                        "ORDER BY `$column` COLLATE NOCASE ASC",
                )
                cursor.use { rows ->
                    while (rows.moveToNext()) {
                        val name = rows.getString(0)
                        if (name.length <= SORU_SECENEGI_ADI_UST_SINIR) {
                            insertOption(type = type, name = name)
                        }
                    }
                }
            }

            // Existing question labels become reusable catalog choices without changing their saved values.
            importLegacyColumn("kaynak", SoruSecenekTuru.KAYNAK)
            importLegacyColumn("yanlis_nedeni", SoruSecenekTuru.YANLIS_NEDENI)
        }
    }

    /** Additive only: no historical review, duration, result or scheduled day is rewritten. */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `tekrar_olcumleri` (
                    `tekrar_id` TEXT NOT NULL, `deneme_id` TEXT NOT NULL, `oturum_id` TEXT,
                    `deneme_aktif_ms` INTEGER, `inceleme_aktif_ms` INTEGER,
                    `kalite` TEXT NOT NULL, `nedenler_json` TEXT NOT NULL, `olcum_surumu` INTEGER NOT NULL,
                    `ders_id` TEXT NOT NULL, `ders_adi` TEXT NOT NULL, `konu_id` TEXT, `konu_adi` TEXT,
                    `kaynak_anahtari` TEXT, `kaynak_etiketi` TEXT, `karar_json` TEXT NOT NULL,
                    PRIMARY KEY(`tekrar_id`),
                    FOREIGN KEY(`tekrar_id`) REFERENCES `tekrarlar`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_tekrar_olcumleri_deneme_id` ON `tekrar_olcumleri` (`deneme_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tekrar_olcumleri_ders_id_konu_id` ON `tekrar_olcumleri` (`ders_id`, `konu_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tekrar_olcumleri_ders_id_kaynak_anahtari` ON `tekrar_olcumleri` (`ders_id`, `kaynak_anahtari`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tekrarlar_gun` ON `tekrarlar` (`gun`)")
        }
    }

    /**
     * Additive only: the teacher workspace moves from one JSON file into indexed tables. Lessons and
     * topics are shared with the student catalog (no copy), and an existing 1.5.x JSON file is imported
     * once at runtime by `OgretmenDeposu` — migration itself never touches user files.
     */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `ogretmen_gruplari` (
                    `id` TEXT NOT NULL,
                    `ad` TEXT NOT NULL,
                    `aciklama` TEXT,
                    `ders_id` TEXT,
                    `arsivli` INTEGER NOT NULL,
                    `sira` INTEGER NOT NULL,
                    `olusturma` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`ders_id`) REFERENCES `dersler`(`id`)
                        ON UPDATE NO ACTION ON DELETE SET NULL
                )
                """.trimIndent(),
            )
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ogretmen_gruplari_sira` ON `ogretmen_gruplari` (`sira`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ogretmen_gruplari_ders_id` ON `ogretmen_gruplari` (`ders_id`)")

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `ogretmen_sorulari` (
                    `id` TEXT NOT NULL,
                    `grup_id` TEXT NOT NULL,
                    `baslik` TEXT,
                    `ders_id` TEXT,
                    `konu_id` TEXT,
                    `kaynak` TEXT,
                    `kim_sordu` TEXT,
                    `plan_gun` INTEGER,
                    `zorluk` TEXT,
                    `cevap` TEXT,
                    `durum` TEXT NOT NULL,
                    `cozulme` INTEGER,
                    `olusturma` INTEGER NOT NULL,
                    `guncelleme` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`grup_id`) REFERENCES `ogretmen_gruplari`(`id`)
                        ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`ders_id`) REFERENCES `dersler`(`id`)
                        ON UPDATE NO ACTION ON DELETE SET NULL,
                    FOREIGN KEY(`konu_id`) REFERENCES `konular`(`id`)
                        ON UPDATE NO ACTION ON DELETE SET NULL
                )
                """.trimIndent(),
            )
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ogretmen_sorulari_grup_id` ON `ogretmen_sorulari` (`grup_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ogretmen_sorulari_ders_id` ON `ogretmen_sorulari` (`ders_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ogretmen_sorulari_konu_id` ON `ogretmen_sorulari` (`konu_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ogretmen_sorulari_durum` ON `ogretmen_sorulari` (`durum`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ogretmen_sorulari_plan_gun` ON `ogretmen_sorulari` (`plan_gun`)")

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `ogretmen_soru_gorselleri` (
                    `id` TEXT NOT NULL,
                    `soru_id` TEXT NOT NULL,
                    `sira` INTEGER NOT NULL,
                    `tur` TEXT NOT NULL,
                    `dosya_adi` TEXT NOT NULL,
                    `mime_type` TEXT NOT NULL,
                    `genislik` INTEGER NOT NULL,
                    `yukseklik` INTEGER NOT NULL,
                    `boyut` INTEGER NOT NULL,
                    `sha256` TEXT NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`soru_id`) REFERENCES `ogretmen_sorulari`(`id`)
                        ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_ogretmen_soru_gorselleri_soru_id` " +
                    "ON `ogretmen_soru_gorselleri` (`soru_id`)",
            )
        }
    }
}

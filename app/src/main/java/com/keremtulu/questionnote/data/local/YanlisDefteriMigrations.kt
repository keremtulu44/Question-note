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
}

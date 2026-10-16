package com.keremtulu.questionnote.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.keremtulu.questionnote.data.local.dao.DersDao
import com.keremtulu.questionnote.data.local.entity.DersEntity
import com.keremtulu.questionnote.data.local.entity.GorselEntity
import com.keremtulu.questionnote.data.local.entity.KonuEntity
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.local.entity.TekrarEntity

@Database(
    entities = [
        DersEntity::class,
        KonuEntity::class,
        SoruEntity::class,
        GorselEntity::class,
        TekrarEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class YanlisDefteriDatabase : RoomDatabase() {
    abstract fun dersDao(): DersDao

    companion object {
        const val DATABASE_NAME = "yanlis_defteri.db"
    }
}

internal class VarsayilanDersleriEkle : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        val now = System.currentTimeMillis()
        val insertSql = """
            INSERT INTO dersler (id, ad, sira, arsivli, olusturma, guncelleme)
            VALUES (?, ?, ?, 0, ?, ?)
        """.trimIndent()

        VarsayilanDersAdlari.forEachIndexed { index, ad ->
            val stableId = "00000000-0000-4000-8000-${(index + 1).toString().padStart(12, '0')}"
            db.execSQL(insertSql, arrayOf<Any?>(stableId, ad, index, now, now))
        }
    }
}

internal val VarsayilanDersAdlari = listOf(
    "Edebiyat",
    "Matematik",
    "Biyoloji",
    "Kimya",
    "Fizik",
    "Coğrafya",
    "Tarih",
)

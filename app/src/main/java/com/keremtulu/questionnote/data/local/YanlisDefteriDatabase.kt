package com.keremtulu.questionnote.data.local

import com.keremtulu.questionnote.data.local.dao.TekrarOlcumDao
import com.keremtulu.questionnote.data.local.entity.TekrarOlcumEntity
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.keremtulu.questionnote.data.local.dao.DersDao
import com.keremtulu.questionnote.data.local.dao.KonuDao
import com.keremtulu.questionnote.data.local.dao.GorselDao
import com.keremtulu.questionnote.data.local.dao.IlerlemeDao
import com.keremtulu.questionnote.data.local.dao.GeriYuklemeIsaretiDao
import com.keremtulu.questionnote.data.local.dao.SoruDao
import com.keremtulu.questionnote.data.local.dao.SoruSecenegiDao
import com.keremtulu.questionnote.data.local.dao.TekrarDao
import com.keremtulu.questionnote.data.local.entity.DersEntity
import com.keremtulu.questionnote.data.local.entity.GorselEntity
import com.keremtulu.questionnote.data.local.entity.GeriYuklemeIsaretiEntity
import com.keremtulu.questionnote.data.local.entity.KonuEntity
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.local.entity.SoruSecenegiEntity
import com.keremtulu.questionnote.data.local.entity.TekrarEntity

@Database(
    entities = [
        DersEntity::class,
        KonuEntity::class,
        SoruEntity::class,
        GorselEntity::class,
        TekrarEntity::class,
        TekrarOlcumEntity::class,
        GeriYuklemeIsaretiEntity::class,
        SoruSecenegiEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class YanlisDefteriDatabase : RoomDatabase() {
    abstract fun dersDao(): DersDao
    abstract fun konuDao(): KonuDao
    abstract fun soruDao(): SoruDao
    abstract fun soruSecenegiDao(): SoruSecenegiDao
    abstract fun gorselDao(): GorselDao
    abstract fun tekrarDao(): TekrarDao
    abstract fun tekrarOlcumDao(): TekrarOlcumDao
    abstract fun ilerlemeDao(): IlerlemeDao
    abstract fun geriYuklemeIsaretiDao(): GeriYuklemeIsaretiDao

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

        val insertOptionSql = """
            INSERT INTO soru_secenekleri (id, tur, ad, normalize_ad, varsayilan, sira, olusturma)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        varsayilanSoruSecenekleri(now).forEach { option ->
            db.execSQL(
                insertOptionSql,
                arrayOf<Any?>(
                    option.id,
                    option.tur,
                    option.ad,
                    option.normalizeAd,
                    if (option.varsayilan) 1 else 0,
                    option.sira,
                    option.olusturma,
                ),
            )
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

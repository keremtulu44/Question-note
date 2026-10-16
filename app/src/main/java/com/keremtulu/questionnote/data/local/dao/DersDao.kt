package com.keremtulu.questionnote.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.keremtulu.questionnote.data.local.entity.DersEntity
import kotlinx.coroutines.flow.Flow

data class DersOzetEntity(
    @Embedded val ders: DersEntity,
    @ColumnInfo(name = "soru_sayisi") val soruSayisi: Int,
)

@Dao
interface DersDao {
    @Query("SELECT * FROM dersler ORDER BY sira ASC, ad COLLATE NOCASE ASC")
    fun tumunuGetir(): List<DersEntity>

    @Query("SELECT * FROM dersler WHERE id = :id LIMIT 1")
    suspend fun idIleGetir(id: String): DersEntity?

    @Query(
        """
        SELECT dersler.*, COUNT(sorular.id) AS soru_sayisi
        FROM dersler
        LEFT JOIN sorular ON sorular.ders_id = dersler.id
        WHERE dersler.arsivli = :arsivli
        GROUP BY dersler.id
        ORDER BY dersler.sira ASC, dersler.ad COLLATE NOCASE ASC
        """,
    )
    fun dersleriIzle(arsivli: Boolean): Flow<List<DersOzetEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun ekle(ders: DersEntity)

    @Update
    suspend fun guncelle(ders: DersEntity): Int

    @Update
    suspend fun guncelle(dersler: List<DersEntity>)
}

package com.keremtulu.questionnote.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Query
import androidx.room.Update
import com.keremtulu.questionnote.data.local.entity.GorselEntity
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import kotlinx.coroutines.flow.Flow

data class SoruVeGorsellerEntity(
    @Embedded val soru: SoruEntity,
    @Relation(parentColumn = "id", entityColumn = "soru_id")
    val gorseller: List<GorselEntity>,
)

@Dao
interface SoruDao {
    @Query(
        """
        SELECT * FROM sorular
        WHERE ders_id = :dersId
          AND (:konuId IS NULL OR konu_id = :konuId)
          AND (:konusuz = 0 OR konu_id IS NULL)
        ORDER BY guncelleme DESC, olusturma DESC, id ASC
        """,
    )
    @Transaction
    fun sorulariIzle(dersId: String, konuId: String?, konusuz: Boolean): Flow<List<SoruVeGorsellerEntity>>

    @Query("SELECT * FROM sorular WHERE id = :id LIMIT 1")
    suspend fun idIleGetir(id: String): SoruEntity?

    @Query("SELECT * FROM sorular ORDER BY olusturma ASC, id ASC")
    suspend fun tumunuGetir(): List<SoruEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun ekle(soru: SoruEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun ekle(sorular: List<SoruEntity>)

    @Query("DELETE FROM sorular")
    suspend fun tumunuSil()

    @Update
    suspend fun guncelle(soru: SoruEntity): Int

    @Delete
    suspend fun sil(soru: SoruEntity): Int
}

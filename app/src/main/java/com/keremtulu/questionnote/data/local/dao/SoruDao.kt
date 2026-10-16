package com.keremtulu.questionnote.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import kotlinx.coroutines.flow.Flow

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
    fun sorulariIzle(dersId: String, konuId: String?, konusuz: Boolean): Flow<List<SoruEntity>>

    @Query("SELECT * FROM sorular WHERE id = :id LIMIT 1")
    suspend fun idIleGetir(id: String): SoruEntity?

    @Query("SELECT * FROM sorular ORDER BY olusturma ASC, id ASC")
    suspend fun tumunuGetir(): List<SoruEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun ekle(soru: SoruEntity)

    @Update
    suspend fun guncelle(soru: SoruEntity): Int

    @Delete
    suspend fun sil(soru: SoruEntity): Int
}

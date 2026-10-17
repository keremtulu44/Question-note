package com.keremtulu.questionnote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.keremtulu.questionnote.data.local.entity.KonuEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KonuDao {
    @Query(
        """
        SELECT * FROM konular
        WHERE ders_id = :dersId AND arsivli = :arsivli
        ORDER BY sira ASC, ad COLLATE NOCASE ASC
        """,
    )
    fun konulariIzle(dersId: String, arsivli: Boolean): Flow<List<KonuEntity>>

    @Query("SELECT * FROM konular ORDER BY ders_id ASC, sira ASC, ad COLLATE NOCASE ASC, id ASC")
    fun tumunuIzle(): Flow<List<KonuEntity>>

    @Query("SELECT * FROM konular WHERE ders_id = :dersId")
    suspend fun dersinKonulariniGetir(dersId: String): List<KonuEntity>

    @Query("SELECT * FROM konular ORDER BY ders_id ASC, sira ASC, id ASC")
    suspend fun tumunuGetir(): List<KonuEntity>

    @Query("SELECT * FROM konular WHERE id = :id LIMIT 1")
    suspend fun idIleGetir(id: String): KonuEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun ekle(konu: KonuEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun ekle(konular: List<KonuEntity>)

    @Query("DELETE FROM konular")
    suspend fun tumunuSil()

    @Query("DELETE FROM konular WHERE id = :id")
    suspend fun sil(id: String): Int

    @Update
    suspend fun guncelle(konu: KonuEntity): Int
}

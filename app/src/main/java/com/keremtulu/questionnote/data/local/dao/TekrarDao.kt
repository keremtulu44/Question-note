package com.keremtulu.questionnote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.keremtulu.questionnote.data.local.entity.TekrarEntity

@Dao
interface TekrarDao {
    @Query("SELECT * FROM tekrarlar ORDER BY zaman ASC, id ASC")
    suspend fun tumunuGetir(): List<TekrarEntity>

    @Query("SELECT * FROM tekrarlar WHERE soru_id = :soruId ORDER BY gun DESC, zaman DESC")
    suspend fun sorununTekrarlariniGetir(soruId: String): List<TekrarEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM tekrarlar WHERE soru_id = :soruId AND gun = :gun)")
    suspend fun ayniGunTekrarVar(soruId: String, gun: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun ekle(tekrar: TekrarEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun ekle(tekrarlar: List<TekrarEntity>)

    @Query("DELETE FROM tekrarlar")
    suspend fun tumunuSil()
}

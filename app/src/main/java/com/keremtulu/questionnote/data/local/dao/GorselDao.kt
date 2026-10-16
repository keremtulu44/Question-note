package com.keremtulu.questionnote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.keremtulu.questionnote.data.local.entity.GorselEntity

@Dao
interface GorselDao {
    @Query(
        """
        SELECT * FROM gorseller WHERE soru_id = :soruId
        ORDER BY CASE tur WHEN 'SORU' THEN 0 ELSE 1 END, sira ASC
        """,
    )
    suspend fun sorununGorselleriniGetir(soruId: String): List<GorselEntity>

    @Query("SELECT * FROM gorseller ORDER BY dosya_adi ASC")
    suspend fun tumunuGetir(): List<GorselEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun ekle(gorseller: List<GorselEntity>)

    @Query("DELETE FROM gorseller WHERE soru_id = :soruId")
    suspend fun sorununGorselleriniSil(soruId: String)
}

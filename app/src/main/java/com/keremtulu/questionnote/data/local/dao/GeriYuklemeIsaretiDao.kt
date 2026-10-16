package com.keremtulu.questionnote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.keremtulu.questionnote.data.local.entity.GeriYuklemeIsaretiEntity

@Dao
interface GeriYuklemeIsaretiDao {
    @Query("SELECT id FROM geri_yukleme_isaretleri LIMIT 1")
    suspend fun idGetir(): String?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun ekle(isaret: GeriYuklemeIsaretiEntity)

    @Query("DELETE FROM geri_yukleme_isaretleri")
    suspend fun tumunuSil()
}

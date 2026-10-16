package com.keremtulu.questionnote.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.keremtulu.questionnote.data.local.entity.DersEntity

@Dao
interface DersDao {
    @Query("SELECT * FROM dersler ORDER BY sira ASC, ad COLLATE NOCASE ASC")
    fun tumunuGetir(): List<DersEntity>
}

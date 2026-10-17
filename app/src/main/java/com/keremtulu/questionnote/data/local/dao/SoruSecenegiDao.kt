package com.keremtulu.questionnote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.keremtulu.questionnote.data.local.entity.SoruSecenegiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SoruSecenegiDao {
    @Query(
        """
        SELECT * FROM soru_secenekleri
        WHERE tur = :tur
        ORDER BY varsayilan DESC, sira ASC, ad COLLATE NOCASE ASC, id ASC
        """,
    )
    fun secenekleriIzle(tur: String): Flow<List<SoruSecenegiEntity>>

    @Query("SELECT * FROM soru_secenekleri WHERE tur = :tur AND normalize_ad = :normalizeAd LIMIT 1")
    suspend fun adaGoreGetir(tur: String, normalizeAd: String): SoruSecenegiEntity?

    @Query("SELECT * FROM soru_secenekleri ORDER BY tur ASC, varsayilan DESC, sira ASC, ad COLLATE NOCASE ASC, id ASC")
    suspend fun tumunuGetir(): List<SoruSecenegiEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun ekle(secenek: SoruSecenegiEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun topluEkle(secenekler: List<SoruSecenegiEntity>)

    @Query("DELETE FROM soru_secenekleri")
    suspend fun tumunuSil()
}

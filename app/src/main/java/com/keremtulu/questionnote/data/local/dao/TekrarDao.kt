package com.keremtulu.questionnote.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.keremtulu.questionnote.data.local.entity.TekrarEntity
import kotlinx.coroutines.flow.Flow

/** Aggregate row for a single local day. Counts use the same independence rule as the result card. */
data class GunDengeEntity(
    @ColumnInfo(name = "gun") val gun: Int,
    @ColumnInfo(name = "toplam") val toplam: Long,
    @ColumnInfo(name = "bagimsiz") val bagimsiz: Long,
)

@Dao
interface TekrarDao {
    @Query("SELECT * FROM tekrarlar ORDER BY zaman ASC, id ASC")
    suspend fun tumunuGetir(): List<TekrarEntity>

    @Query("SELECT * FROM tekrarlar WHERE soru_id = :soruId ORDER BY gun DESC, zaman DESC")
    suspend fun sorununTekrarlariniGetir(soruId: String): List<TekrarEntity>

    @Query("SELECT * FROM tekrarlar WHERE soru_id = :soruId ORDER BY gun DESC, zaman DESC, id ASC")
    fun sorununTekrarGecmisiniIzle(soruId: String): Flow<List<TekrarEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM tekrarlar WHERE soru_id = :soruId AND gun = :gun)")
    suspend fun ayniGunTekrarVar(soruId: String, gun: Int): Boolean

    @Query("SELECT COUNT(*) FROM tekrarlar WHERE gun = :gun")
    fun gunSayisiIzle(gun: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM tekrarlar WHERE gun = :gun")
    suspend fun gunSayisi(gun: Int): Int

    @Query("SELECT DISTINCT gun FROM tekrarlar ORDER BY gun DESC")
    fun gunlerIzle(): Flow<List<Int>>

    @Query("SELECT DISTINCT gun FROM tekrarlar ORDER BY gun DESC")
    suspend fun gunler(): List<Int>

    @Query("SELECT COUNT(*) FROM tekrarlar WHERE sonuc = 'RAHAT' AND cozum_acildi = 0")
    suspend fun toplamBagimsiz(): Int

    @Query(
        """
        SELECT gun,
               COUNT(*) AS toplam,
               SUM(CASE WHEN sonuc = 'RAHAT' AND cozum_acildi = 0 THEN 1 ELSE 0 END) AS bagimsiz
        FROM tekrarlar
        WHERE gun BETWEEN :ilkGun AND :sonGun
        GROUP BY gun
        ORDER BY gun ASC
        """,
    )
    suspend fun gunDengeleri(ilkGun: Int, sonGun: Int): List<GunDengeEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun ekle(tekrar: TekrarEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun ekle(tekrarlar: List<TekrarEntity>)

    @Query("DELETE FROM tekrarlar")
    suspend fun tumunuSil()
}

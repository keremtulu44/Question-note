package com.keremtulu.questionnote.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.keremtulu.questionnote.data.local.entity.TekrarEntity
import com.keremtulu.questionnote.data.local.entity.TekrarOlcumEntity
import kotlinx.coroutines.flow.Flow

data class OlcumluTekrar(
    @Embedded val tekrar: TekrarEntity,
    @Relation(parentColumn = "id", entityColumn = "tekrar_id") val olcum: TekrarOlcumEntity?,
)

data class ProfilSatiri(
    @Embedded val olcum: TekrarOlcumEntity,
    @ColumnInfo(name = "soru_id") val soruId: String,
    @ColumnInfo(name = "gun") val gun: Int,
    @ColumnInfo(name = "zaman") val zaman: Long,
    @ColumnInfo(name = "sonuc") val sonuc: String,
    @ColumnInfo(name = "cozum_acildi") val cozumAcildi: Boolean,
)

@Dao
interface TekrarOlcumDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun ekle(olcum: TekrarOlcumEntity)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun ekle(olcumler: List<TekrarOlcumEntity>)
    @Query("SELECT * FROM tekrar_olcumleri ORDER BY tekrar_id")
    suspend fun tumunuGetir(): List<TekrarOlcumEntity>
    @Query("SELECT * FROM tekrar_olcumleri WHERE deneme_id = :denemeId LIMIT 1")
    suspend fun denemeIleGetir(denemeId: String): TekrarOlcumEntity?
    @Query("SELECT * FROM tekrarlar WHERE id = :id")
    suspend fun tekrarGetir(id: String): TekrarEntity?
    @Transaction
    @Query("SELECT * FROM tekrarlar WHERE soru_id = :soruId ORDER BY gun DESC, zaman DESC, id ASC")
    fun gecmisiIzle(soruId: String): Flow<List<OlcumluTekrar>>
    @Query("""SELECT m.*, t.soru_id, t.gun, t.zaman, t.sonuc, t.cozum_acildi
        FROM tekrar_olcumleri m JOIN tekrarlar t ON t.id = m.tekrar_id
        WHERE t.gun BETWEEN :ilkGun AND :sonGun AND m.ders_id = :dersId
        ORDER BY t.gun, t.zaman, t.id""")
    suspend fun dersVerileri(dersId: String, ilkGun: Int, sonGun: Int): List<ProfilSatiri>
    @Query("""SELECT m.*, t.soru_id, t.gun, t.zaman, t.sonuc, t.cozum_acildi
        FROM tekrarlar t JOIN tekrar_olcumleri m ON t.id = m.tekrar_id
        WHERE t.gun BETWEEN :ilkGun AND :sonGun ORDER BY t.gun, t.zaman, t.id""")
    suspend fun pencereVerileri(ilkGun: Int, sonGun: Int): List<ProfilSatiri>
    @Query("SELECT COUNT(*) FROM tekrarlar WHERE gun BETWEEN :ilkGun AND :sonGun")
    suspend fun pencereSayisi(ilkGun: Int, sonGun: Int): Int
}

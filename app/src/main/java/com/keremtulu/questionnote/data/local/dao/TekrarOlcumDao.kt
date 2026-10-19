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

/** Lightweight measurement/context projection. Never read karar_json for statistical aggregation. */
data class ProfilOlcumSatiri(
    @ColumnInfo(name = "tekrar_id") val tekrarId: String,
    @ColumnInfo(name = "deneme_id") val denemeId: String,
    @ColumnInfo(name = "deneme_aktif_ms") val denemeAktifMs: Long?,
    @ColumnInfo(name = "inceleme_aktif_ms") val incelemeAktifMs: Long?,
    @ColumnInfo(name = "kalite") val kalite: String,
    @ColumnInfo(name = "nedenler_json") val nedenlerJson: String,
    @ColumnInfo(name = "olcum_surumu") val olcumSurumu: Int,
    @ColumnInfo(name = "ders_id") val dersId: String,
    @ColumnInfo(name = "ders_adi") val dersAdi: String,
    @ColumnInfo(name = "konu_id") val konuId: String?,
    @ColumnInfo(name = "konu_adi") val konuAdi: String?,
    @ColumnInfo(name = "kaynak_anahtari") val kaynakAnahtari: String?,
    @ColumnInfo(name = "kaynak_etiketi") val kaynakEtiketi: String?,
)
data class TekrarAnalizSatiri(@Embedded val tekrar: TekrarEntity, @Embedded val olcum: ProfilOlcumSatiri?)

data class ProfilSatiri(
    @Embedded val olcum: ProfilOlcumSatiri,
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

    @Query("DELETE FROM tekrar_olcumleri WHERE tekrar_id = :tekrarId")
    suspend fun tekrarIdIleSil(tekrarId: String)
    @Query("SELECT * FROM tekrarlar WHERE id = :id")
    suspend fun tekrarGetir(id: String): TekrarEntity?
    @Transaction
    @Query("SELECT * FROM tekrarlar WHERE soru_id = :soruId ORDER BY gun DESC, zaman DESC, id ASC")
    fun gecmisiIzle(soruId: String): Flow<List<OlcumluTekrar>>
    @Query("""SELECT m.tekrar_id, m.deneme_id, m.deneme_aktif_ms, m.inceleme_aktif_ms, m.kalite, m.nedenler_json, m.olcum_surumu, m.ders_id, m.ders_adi, m.konu_id, m.konu_adi, m.kaynak_anahtari, m.kaynak_etiketi, t.soru_id, t.gun, t.zaman, t.sonuc, t.cozum_acildi
        FROM tekrar_olcumleri m JOIN tekrarlar t ON t.id = m.tekrar_id
        WHERE t.gun BETWEEN :ilkGun AND :sonGun AND m.ders_id = :dersId
        ORDER BY t.gun, t.zaman, t.id""")
    suspend fun dersVerileri(dersId: String, ilkGun: Int, sonGun: Int): List<ProfilSatiri>
    @Query("""SELECT m.tekrar_id, m.deneme_id, m.deneme_aktif_ms, m.inceleme_aktif_ms, m.kalite, m.nedenler_json, m.olcum_surumu, m.ders_id, m.ders_adi, m.konu_id, m.konu_adi, m.kaynak_anahtari, m.kaynak_etiketi, t.soru_id, t.gun, t.zaman, t.sonuc, t.cozum_acildi
        FROM tekrarlar t JOIN tekrar_olcumleri m ON t.id = m.tekrar_id
        WHERE t.gun BETWEEN :ilkGun AND :sonGun ORDER BY t.gun, t.zaman, t.id""")
    suspend fun pencereVerileri(ilkGun: Int, sonGun: Int): List<ProfilSatiri>
    @Query("""SELECT t.*, m.tekrar_id, m.deneme_id, m.deneme_aktif_ms, m.inceleme_aktif_ms, m.kalite, m.nedenler_json, m.olcum_surumu, m.ders_id, m.ders_adi, m.konu_id, m.konu_adi, m.kaynak_anahtari, m.kaynak_etiketi
        FROM tekrarlar t LEFT JOIN tekrar_olcumleri m ON m.tekrar_id = t.id
        ORDER BY t.gun, t.zaman, t.id""")
    suspend fun analizVerileri(): List<TekrarAnalizSatiri>
    @Transaction
    @Query("""SELECT t.* FROM tekrarlar t JOIN tekrar_olcumleri m ON m.tekrar_id = t.id
        ORDER BY t.zaman DESC, t.id ASC LIMIT :sinir""")
    suspend fun sonKararlar(sinir: Int): List<OlcumluTekrar>
    @Query("SELECT COUNT(*) FROM tekrar_olcumleri")
    suspend fun toplamKararSayisi(): Int
    @Query("SELECT COUNT(*) FROM tekrarlar WHERE gun BETWEEN :ilkGun AND :sonGun")
    suspend fun pencereSayisi(ilkGun: Int, sonGun: Int): Int
}

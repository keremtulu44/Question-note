package com.keremtulu.questionnote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.keremtulu.questionnote.data.local.entity.OgretmenGrupEntity
import com.keremtulu.questionnote.data.local.entity.OgretmenSoruEntity
import com.keremtulu.questionnote.data.local.entity.OgretmenSoruGorselEntity
import kotlinx.coroutines.flow.Flow

/**
 * Teacher workspace tables. Small, targeted statements replace the previous whole-file rewrite;
 * list reads stay ordered so the UI needs no extra sorting.
 */
@Dao
interface OgretmenDao {
    @Query("SELECT * FROM ogretmen_gruplari ORDER BY sira ASC, ad COLLATE NOCASE ASC")
    fun gruplariIzle(): Flow<List<OgretmenGrupEntity>>

    @Query("SELECT * FROM ogretmen_sorulari ORDER BY olusturma DESC")
    fun sorulariIzle(): Flow<List<OgretmenSoruEntity>>

    @Query("SELECT * FROM ogretmen_soru_gorselleri ORDER BY soru_id ASC, sira ASC")
    fun gorselleriIzle(): Flow<List<OgretmenSoruGorselEntity>>

    @Query("SELECT * FROM ogretmen_gruplari ORDER BY sira ASC, ad COLLATE NOCASE ASC")
    suspend fun gruplariGetir(): List<OgretmenGrupEntity>

    @Query("SELECT * FROM ogretmen_sorulari ORDER BY olusturma DESC")
    suspend fun sorulariGetir(): List<OgretmenSoruEntity>

    @Query("SELECT * FROM ogretmen_soru_gorselleri ORDER BY soru_id ASC, sira ASC")
    suspend fun gorselleriGetir(): List<OgretmenSoruGorselEntity>

    @Query("SELECT COUNT(*) FROM ogretmen_sorulari")
    suspend fun soruSayisi(): Int

    @Query("SELECT COUNT(*) FROM ogretmen_gruplari")
    suspend fun grupSayisi(): Int

    @Query("SELECT * FROM ogretmen_sorulari WHERE id = :id LIMIT 1")
    suspend fun soruGetir(id: String): OgretmenSoruEntity?

    @Query("SELECT * FROM ogretmen_soru_gorselleri WHERE soru_id = :soruId ORDER BY sira ASC")
    suspend fun soruGorselleri(soruId: String): List<OgretmenSoruGorselEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun grupEkle(grup: OgretmenGrupEntity)

    @Update
    suspend fun grupGuncelle(grup: OgretmenGrupEntity): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun soruEkle(soru: OgretmenSoruEntity)

    @Update
    suspend fun soruGuncelle(soru: OgretmenSoruEntity): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun gorselEkle(gorseller: List<OgretmenSoruGorselEntity>)

    @Query("DELETE FROM ogretmen_soru_gorselleri WHERE soru_id = :soruId")
    suspend fun soruGorselleriniSil(soruId: String)

    @Query("DELETE FROM ogretmen_gruplari WHERE id = :grupId")
    suspend fun grupSil(grupId: String): Int

    @Query("DELETE FROM ogretmen_sorulari WHERE id = :soruId")
    suspend fun soruSil(soruId: String): Int

    @Query("DELETE FROM ogretmen_sorulari WHERE id IN (:soruIdleri)")
    suspend fun sorulariSil(soruIdleri: List<String>): Int

    @Query(
        "UPDATE ogretmen_sorulari SET durum = :durum, cozulme = :cozulme, guncelleme = :guncelleme " +
            "WHERE id = :soruId",
    )
    suspend fun durumGuncelle(soruId: String, durum: String, cozulme: Long?, guncelleme: Long): Int

    @Query(
        "UPDATE ogretmen_sorulari SET durum = :durum, cozulme = :cozulme, guncelleme = :guncelleme " +
            "WHERE id IN (:soruIdleri)",
    )
    suspend fun topluDurumGuncelle(
        soruIdleri: List<String>,
        durum: String,
        cozulme: Long?,
        guncelleme: Long,
    ): Int

    @Query("UPDATE ogretmen_sorulari SET plan_gun = :gun, guncelleme = :zaman " +
        "WHERE id = :soruId AND durum = 'BEKLIYOR'")
    suspend fun planGuncelle(soruId: String, gun: Long?, zaman: Long): Int

    @Query("UPDATE ogretmen_sorulari SET grup_id = :grupId, guncelleme = :guncelleme WHERE id = :soruId")
    suspend fun soruTasi(soruId: String, grupId: String, guncelleme: Long): Int

    @Query("DELETE FROM ogretmen_gruplari")
    suspend fun tumGruplariSil()

    @Query("DELETE FROM ogretmen_sorulari")
    suspend fun tumSorulariSil()

    @Query("DELETE FROM ogretmen_soru_gorselleri")
    suspend fun tumGorselleriSil()
}

package com.keremtulu.questionnote.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Row used to rebuild the progression state from the review history. */
data class GunSayimEntity(
    @ColumnInfo(name = "gun") val gun: Int,
    @ColumnInfo(name = "adet") val adet: Long,
)

/** Per-lesson distribution of the learning state, used for the consolidation rings. */
data class DersDagilimEntity(
    @ColumnInfo(name = "ders_id") val dersId: String,
    @ColumnInfo(name = "ders_ad") val dersAd: String,
    @ColumnInfo(name = "durum") val durum: String,
    @ColumnInfo(name = "adet") val adet: Long,
)

/** Planlanan tekrar yükü: gün -> soru sayısı (7 günlük tahmin). */
data class PlanGunEntity(
    @ColumnInfo(name = "gun") val gun: Int,
    @ColumnInfo(name = "adet") val adet: Long,
)

/** One row of "how many questions this day were solved independently". */
data class BagimsizGunEntity(
    @ColumnInfo(name = "gun") val gun: Int,
    @ColumnInfo(name = "adet") val adet: Long,
)

@Dao
interface IlerlemeDao {
    @Query("SELECT COUNT(DISTINCT s.ders_id) FROM tekrarlar t JOIN sorular s ON s.id = t.soru_id WHERE t.gun = :gun")
    suspend fun gunDersSayisi(gun: Int): Int

    @Query("SELECT COUNT(DISTINCT s.ders_id) FROM tekrarlar t JOIN sorular s ON s.id = t.soru_id")
    suspend fun calisilanDersSayisi(): Int

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM tekrarlar basari
            WHERE basari.sonuc = 'RAHAT' AND basari.cozum_acildi = 0
              AND (SELECT COUNT(*) FROM tekrarlar eski
                   WHERE eski.soru_id = basari.soru_id AND eski.gun < basari.gun
                     AND eski.sonuc = 'COZEMEDIM' AND eski.cozum_acildi = 0) >= 3
        )
        """,
    )
    suspend fun donusumVar(): Boolean

    @Query("SELECT COUNT(*) FROM sorular")
    suspend fun soruSayisi(): Int

    @Query("SELECT COUNT(*) FROM tekrarlar")
    suspend fun tekrarSayisi(): Int

    @Query("SELECT COUNT(*) FROM tekrarlar WHERE sonuc = 'RAHAT' AND cozum_acildi = 0")
    suspend fun bagimsizTekrarSayisi(): Int

    @Query(
        """
        SELECT gun, SUM(CASE WHEN sonuc = 'RAHAT' AND cozum_acildi = 0 THEN 1 ELSE 0 END) AS adet
        FROM tekrarlar
        GROUP BY gun
        ORDER BY gun ASC
        """,
    )
    suspend fun gunleriBagimsiz(): List<BagimsizGunEntity>

    @Query("SELECT gun, COUNT(*) AS adet FROM tekrarlar GROUP BY gun ORDER BY gun ASC")
    suspend fun gunleriSay(): List<GunSayimEntity>

    /** Daily-goal completions derived from history: each day that reached [hedef]. */
    @Query(
        """
        SELECT COUNT(*) FROM (
            SELECT gun FROM tekrarlar GROUP BY gun HAVING COUNT(*) >= :hedef
        )
        """,
    )
    suspend fun hedefTamamlananGunSayisi(hedef: Int): Int

    @Query("SELECT COUNT(DISTINCT soru_id) FROM tekrarlar WHERE sonuc = 'COZEMEDIM'")
    suspend fun cozemediSoruSayisi(): Int

    @Query(
        """
        SELECT dersler.id AS ders_id, dersler.ad AS ders_ad,
               sorular.ogrenme_durumu AS durum, COUNT(*) AS adet
        FROM sorular
        INNER JOIN dersler ON dersler.id = sorular.ders_id
        WHERE dersler.arsivli = 0
        GROUP BY dersler.id, sorular.ogrenme_durumu
        ORDER BY dersler.sira ASC
        """,
    )
    suspend fun dersDagilimi(): List<DersDagilimEntity>

    @Query(
        """
        SELECT sonraki_tekrar_gun AS gun, COUNT(*) AS adet
        FROM sorular
        WHERE plan_durumu = 'AKTIF'
          AND sonraki_tekrar_gun BETWEEN :ilkGun AND :sonGun
        GROUP BY sonraki_tekrar_gun
        ORDER BY sonraki_tekrar_gun ASC
        """,
    )
    suspend fun planYuku(ilkGun: Int, sonGun: Int): List<PlanGunEntity>

    // --- Seviye / XP akışları (Bugün ekranındaki küçük göstergeler için) ---

    @Query("SELECT COUNT(*) FROM sorular")
    fun soruSayisiIzle(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tekrarlar")
    fun tekrarSayisiIzle(): Flow<Int>
}

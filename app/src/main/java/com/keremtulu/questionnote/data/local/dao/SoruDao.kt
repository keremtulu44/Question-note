package com.keremtulu.questionnote.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Query
import androidx.room.Update
import com.keremtulu.questionnote.data.local.entity.GorselEntity
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import kotlinx.coroutines.flow.Flow

data class SoruVeGorsellerEntity(
    @Embedded val soru: SoruEntity,
    @Relation(parentColumn = "id", entityColumn = "soru_id")
    val gorseller: List<GorselEntity>,
)

data class AnalizSoruSatiri(
    val id: String,
    @ColumnInfo(name = "ders_id") val dersId: String,
    @ColumnInfo(name = "konu_id") val konuId: String?,
    val kaynak: String?,
    @ColumnInfo(name = "plan_durumu") val planDurumu: String,
    @ColumnInfo(name = "son_tekrar_gun") val sonTekrarGun: Int?,
    @ColumnInfo(name = "sonraki_tekrar_gun") val sonrakiTekrarGun: Int?,
)

@Dao
interface SoruDao {
    @Query("SELECT id, ders_id, konu_id, kaynak, plan_durumu, son_tekrar_gun, sonraki_tekrar_gun FROM sorular ORDER BY id")
    suspend fun analizSorulari(): List<AnalizSoruSatiri>

    @Query(
        """
        SELECT * FROM sorular
        WHERE ders_id = :dersId
          AND (:konuId IS NULL OR konu_id = :konuId)
          AND (:konusuz = 0 OR konu_id IS NULL)
        ORDER BY guncelleme DESC, olusturma DESC, id ASC
        """,
    )
    @Transaction
    fun sorulariIzle(dersId: String, konuId: String?, konusuz: Boolean): Flow<List<SoruVeGorsellerEntity>>

    @Query(
        """
        SELECT sorular.* FROM sorular
        INNER JOIN dersler ON dersler.id = sorular.ders_id
        WHERE dersler.arsivli = 0
          AND sorular.plan_durumu = 'AKTIF'
          AND NOT EXISTS (SELECT 1 FROM tekrarlar t WHERE t.soru_id = sorular.id AND t.gun = :bugun)
          AND (sorular.son_tekrar_gun IS NULL OR sorular.son_tekrar_gun < :bugun)
          AND sorular.sonraki_tekrar_gun IS NOT NULL
          AND sorular.sonraki_tekrar_gun <= :bugun
        ORDER BY sorular.sonraki_tekrar_gun ASC, sorular.guncelleme DESC, sorular.id ASC
        """,
    )
    @Transaction
    fun bugunTekrarlanacaklariIzle(bugun: Int): Flow<List<SoruVeGorsellerEntity>>

    @Query(
        """
        SELECT sorular.* FROM sorular
        INNER JOIN dersler ON dersler.id = sorular.ders_id
        WHERE dersler.arsivli = 0
          AND sorular.plan_durumu = 'AKTIF'
          AND NOT EXISTS (SELECT 1 FROM tekrarlar t WHERE t.soru_id = sorular.id AND t.gun = :bugun)
          AND (sorular.son_tekrar_gun IS NULL OR sorular.son_tekrar_gun < :bugun)
          AND (:dersId IS NULL OR sorular.ders_id = :dersId)
          AND (:vadesiGelen = 0 OR (sorular.sonraki_tekrar_gun IS NOT NULL AND sorular.sonraki_tekrar_gun <= :bugun))
          AND (:konuId IS NULL OR sorular.konu_id = :konuId)
          AND (:konusuz = 0 OR sorular.konu_id IS NULL)
          AND (:kaynak IS NULL OR sorular.kaynak = :kaynak)
          AND (:yanlisNedeni IS NULL OR sorular.yanlis_nedeni = :yanlisNedeni)
          AND (:yanlisNedeniBelirtilmemis = 0 OR COALESCE(TRIM(sorular.yanlis_nedeni), '') = '')
        ORDER BY sorular.son_tekrar_gun ASC, sorular.olusturma ASC, sorular.id ASC
        """,
    )
    @Transaction
    fun filtreliCalismaSorulariniIzle(
        bugun: Int,
        konuId: String?,
        konusuz: Boolean,
        kaynak: String?,
        yanlisNedeni: String?,
        yanlisNedeniBelirtilmemis: Boolean,
        dersId: String? = null,
        vadesiGelen: Boolean = false,
    ): Flow<List<SoruVeGorsellerEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM sorular
        INNER JOIN dersler ON dersler.id = sorular.ders_id
        WHERE dersler.arsivli = 0
          AND sorular.plan_durumu = 'AKTIF'
          AND sorular.sonraki_tekrar_gun = :gun
        """,
    )
    suspend fun planliSoruSayisi(gun: Int): Int

    @Query("SELECT COUNT(*) FROM sorular WHERE konu_id = :konuId")
    suspend fun konudakiSoruSayisi(konuId: String): Int

    @Query("SELECT COUNT(*) FROM sorular WHERE kaynak = :ad")
    suspend fun kaynakKullanimSayisi(ad: String): Int

    @Query("SELECT COUNT(*) FROM sorular WHERE yanlis_nedeni = :ad")
    suspend fun nedenKullanimSayisi(ad: String): Int

    @Query("SELECT * FROM sorular WHERE id = :id LIMIT 1")
    suspend fun idIleGetir(id: String): SoruEntity?

    @Query("SELECT * FROM sorular ORDER BY olusturma ASC, id ASC")
    suspend fun tumunuGetir(): List<SoruEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun ekle(soru: SoruEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun ekle(sorular: List<SoruEntity>)

    @Query("DELETE FROM sorular")
    suspend fun tumunuSil()

    @Update
    suspend fun guncelle(soru: SoruEntity): Int

    @Delete
    suspend fun sil(soru: SoruEntity): Int
}

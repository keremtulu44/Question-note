package com.keremtulu.questionnote.data.local.dao

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

@Dao
interface SoruDao {
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
          AND (sorular.son_tekrar_gun IS NULL OR sorular.son_tekrar_gun < :bugun)
          AND (:konuId IS NULL OR sorular.konu_id = :konuId)
          AND (:konusuz = 0 OR sorular.konu_id IS NULL)
          AND (:kaynak IS NULL OR sorular.kaynak = :kaynak)
          AND (:kaynakBelirtilmemis = 0 OR COALESCE(TRIM(sorular.kaynak), '') = '')
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
        kaynakBelirtilmemis: Boolean,
        yanlisNedeni: String?,
        yanlisNedeniBelirtilmemis: Boolean,
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

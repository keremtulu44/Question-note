package com.keremtulu.questionnote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Additive 0..1 measurement/context per review. Historical labels intentionally have no catalogue FK. */
@Entity(tableName = "tekrar_olcumleri",
    foreignKeys = [ForeignKey(entity = TekrarEntity::class, parentColumns = ["id"], childColumns = ["tekrar_id"],
        onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.NO_ACTION)],
    indices = [Index(value = ["deneme_id"], unique = true), Index(value = ["ders_id", "konu_id"]),
        Index(value = ["ders_id", "kaynak_anahtari"])])
data class TekrarOlcumEntity(
    @PrimaryKey @ColumnInfo(name = "tekrar_id") val tekrarId: String,
    @ColumnInfo(name = "deneme_id") val denemeId: String,
    @ColumnInfo(name = "oturum_id") val oturumId: String?,
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
    @ColumnInfo(name = "karar_json") val kararJson: String,
)

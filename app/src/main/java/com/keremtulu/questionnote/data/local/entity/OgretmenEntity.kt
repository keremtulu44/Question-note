package com.keremtulu.questionnote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Teacher workspace rows (schema v5). Lessons and topics are NOT duplicated: `ders_id` and `konu_id`
 * point at the shared student catalog, so a new topic created for the student side is immediately
 * selectable by the teacher and vice versa. `SET NULL` keeps a tag usable after a catalog restore.
 */
@Entity(
    tableName = "ogretmen_gruplari",
    foreignKeys = [
        ForeignKey(
            entity = DersEntity::class,
            parentColumns = ["id"],
            childColumns = ["ders_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["sira"]), Index(value = ["ders_id"])],
)
data class OgretmenGrupEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "ad")
    val ad: String,
    @ColumnInfo(name = "aciklama")
    val aciklama: String? = null,
    @ColumnInfo(name = "ders_id")
    val dersId: String? = null,
    @ColumnInfo(name = "arsivli")
    val arsivli: Boolean = false,
    @ColumnInfo(name = "sira")
    val sira: Int,
    @ColumnInfo(name = "olusturma")
    val olusturma: Long,
)

@Entity(
    tableName = "ogretmen_sorulari",
    foreignKeys = [
        ForeignKey(
            entity = OgretmenGrupEntity::class,
            parentColumns = ["id"],
            childColumns = ["grup_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
        ForeignKey(
            entity = DersEntity::class,
            parentColumns = ["id"],
            childColumns = ["ders_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.NO_ACTION,
        ),
        ForeignKey(
            entity = KonuEntity::class,
            parentColumns = ["id"],
            childColumns = ["konu_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["grup_id"]),
        Index(value = ["ders_id"]),
        Index(value = ["konu_id"]),
        Index(value = ["durum"]),
        Index(value = ["plan_gun"]),
    ],
)
data class OgretmenSoruEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "grup_id")
    val grupId: String,
    @ColumnInfo(name = "baslik")
    val baslik: String? = null,
    @ColumnInfo(name = "ders_id")
    val dersId: String? = null,
    @ColumnInfo(name = "konu_id")
    val konuId: String? = null,
    @ColumnInfo(name = "kaynak")
    val kaynak: String? = null,
    @ColumnInfo(name = "kim_sordu")
    val kimSordu: String? = null,
    @ColumnInfo(name = "plan_gun")
    val planGun: Long? = null,
    @ColumnInfo(name = "zorluk")
    val zorluk: String? = null,
    @ColumnInfo(name = "cevap")
    val cevap: String? = null,
    @ColumnInfo(name = "durum")
    val durum: String = "BEKLIYOR",
    @ColumnInfo(name = "cozulme")
    val cozulme: Long? = null,
    @ColumnInfo(name = "olusturma")
    val olusturma: Long,
    @ColumnInfo(name = "guncelleme")
    val guncelleme: Long,
)

@Entity(
    tableName = "ogretmen_soru_gorselleri",
    foreignKeys = [
        ForeignKey(
            entity = OgretmenSoruEntity::class,
            parentColumns = ["id"],
            childColumns = ["soru_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["soru_id"])],
)
data class OgretmenSoruGorselEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "soru_id")
    val soruId: String,
    @ColumnInfo(name = "sira")
    val sira: Int,
    /** SORU or COZUM; a question needs at least one SORU image, solutions are optional. */
    @ColumnInfo(name = "tur")
    val tur: String = "SORU",
    @ColumnInfo(name = "dosya_adi")
    val dosyaAdi: String,
    @ColumnInfo(name = "mime_type")
    val mimeType: String = "image/jpeg",
    @ColumnInfo(name = "genislik")
    val genislik: Int,
    @ColumnInfo(name = "yukseklik")
    val yukseklik: Int,
    @ColumnInfo(name = "boyut")
    val boyut: Long,
    @ColumnInfo(name = "sha256")
    val sha256: String,
)

package com.keremtulu.questionnote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tekrarlar",
    foreignKeys = [
        ForeignKey(
            entity = SoruEntity::class,
            parentColumns = ["id"],
            childColumns = ["soru_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["soru_id", "gun"]), Index(value = ["gun"])],
)
data class TekrarEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "soru_id")
    val soruId: String,
    @ColumnInfo(name = "zaman")
    val zaman: Long,
    @ColumnInfo(name = "gun")
    val gun: Int,
    @ColumnInfo(name = "planlanan_gun")
    val planlananGun: Int? = null,
    @ColumnInfo(name = "sonuc")
    val sonuc: String,
    @ColumnInfo(name = "cozum_acildi", defaultValue = "0")
    val cozumAcildi: Boolean = false,
    @ColumnInfo(name = "onceki_aralik")
    val oncekiAralik: Int? = null,
    @ColumnInfo(name = "sonraki_aralik")
    val sonrakiAralik: Int? = null,
    @ColumnInfo(name = "algoritma_surumu", defaultValue = "1")
    val algoritmaSurumu: Int = 1,
    @ColumnInfo(name = "elle_atandi", defaultValue = "0")
    val elleAtandi: Boolean = false,
    /** 1 when the solution was opened only to verify a result locked in before the reveal. */
    @ColumnInfo(name = "kontrol_acilisi", defaultValue = "0")
    val kontrolAcilisi: Boolean = false,
    /** Locked pre-reveal result name; non-null only for verification openings. */
    @ColumnInfo(name = "kilitli_sonuc")
    val kilitliSonuc: String? = null,
) {
    /** Assisted attempt: opened as help, not as post-lock verification. Not a stored column. */
    @get:androidx.room.Ignore
    val destekli: Boolean get() = cozumAcildi && !kontrolAcilisi
}

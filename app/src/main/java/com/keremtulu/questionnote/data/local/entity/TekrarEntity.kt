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
    indices = [Index(value = ["soru_id", "gun"])],
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
)

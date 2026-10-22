package com.keremtulu.questionnote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gorseller",
    foreignKeys = [
        ForeignKey(
            entity = SoruEntity::class,
            parentColumns = ["id"],
            childColumns = ["soru_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["soru_id", "tur", "sira"], unique = true)],
)
data class GorselEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "soru_id")
    val soruId: String,
    @ColumnInfo(name = "tur")
    val tur: String,
    @ColumnInfo(name = "sira")
    val sira: Int,
    @ColumnInfo(name = "dosya_adi")
    val dosyaAdi: String,
    @ColumnInfo(name = "mime_type")
    val mimeType: String,
    @ColumnInfo(name = "genislik")
    val genislik: Int,
    @ColumnInfo(name = "yukseklik")
    val yukseklik: Int,
    @ColumnInfo(name = "boyut")
    val boyut: Long,
    @ColumnInfo(name = "sha256")
    val sha256: String,
    @ColumnInfo(name = "olusturma")
    val olusturma: Long,
)

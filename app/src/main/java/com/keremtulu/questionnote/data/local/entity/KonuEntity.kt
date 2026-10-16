package com.keremtulu.questionnote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "konular",
    foreignKeys = [
        ForeignKey(
            entity = DersEntity::class,
            parentColumns = ["id"],
            childColumns = ["ders_id"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["ders_id", "sira"])],
)
data class KonuEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "ders_id")
    val dersId: String,
    @ColumnInfo(name = "ad")
    val ad: String,
    @ColumnInfo(name = "sira")
    val sira: Int,
    @ColumnInfo(name = "arsivli", defaultValue = "0")
    val arsivli: Boolean = false,
    @ColumnInfo(name = "olusturma")
    val olusturma: Long,
    @ColumnInfo(name = "guncelleme")
    val guncelleme: Long,
)

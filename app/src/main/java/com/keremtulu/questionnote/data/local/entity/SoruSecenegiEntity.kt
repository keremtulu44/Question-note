package com.keremtulu.questionnote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "soru_secenekleri",
    indices = [
        Index(value = ["tur", "normalize_ad"], unique = true),
        Index(value = ["tur", "varsayilan", "sira"]),
    ],
)
data class SoruSecenegiEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "tur")
    val tur: String,
    @ColumnInfo(name = "ad")
    val ad: String,
    @ColumnInfo(name = "normalize_ad")
    val normalizeAd: String,
    @ColumnInfo(name = "varsayilan", defaultValue = "0")
    val varsayilan: Boolean = false,
    @ColumnInfo(name = "sira", defaultValue = "0")
    val sira: Int = 0,
    @ColumnInfo(name = "olusturma")
    val olusturma: Long,
)

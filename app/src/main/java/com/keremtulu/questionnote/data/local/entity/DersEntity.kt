package com.keremtulu.questionnote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dersler",
    indices = [Index(value = ["sira"])],
)
data class DersEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
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

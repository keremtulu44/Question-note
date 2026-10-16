package com.keremtulu.questionnote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** A durable commit marker used to finish or roll back an interrupted full restore. */
@Entity(tableName = "geri_yukleme_isaretleri")
data class GeriYuklemeIsaretiEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
)

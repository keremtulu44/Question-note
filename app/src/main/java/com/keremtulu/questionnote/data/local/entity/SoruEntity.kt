package com.keremtulu.questionnote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sorular",
    foreignKeys = [
        ForeignKey(
            entity = DersEntity::class,
            parentColumns = ["id"],
            childColumns = ["ders_id"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION,
        ),
        ForeignKey(
            entity = KonuEntity::class,
            parentColumns = ["id"],
            childColumns = ["konu_id"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["ders_id"]),
        Index(value = ["konu_id"]),
        Index(value = ["sonraki_tekrar_gun"]),
        Index(value = ["plan_durumu"]),
        Index(value = ["guncelleme"]),
    ],
)
data class SoruEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "ders_id")
    val dersId: String,
    @ColumnInfo(name = "konu_id")
    val konuId: String? = null,
    @ColumnInfo(name = "soru_notu")
    val soruNotu: String? = null,
    @ColumnInfo(name = "cozum_notu")
    val cozumNotu: String? = null,
    @ColumnInfo(name = "kaynak")
    val kaynak: String? = null,
    @ColumnInfo(name = "kaynak_konumu")
    val kaynakKonumu: String? = null,
    @ColumnInfo(name = "yanlis_nedeni")
    val yanlisNedeni: String? = null,
    @ColumnInfo(name = "favori", defaultValue = "0")
    val favori: Boolean = false,
    @ColumnInfo(name = "plan_durumu", defaultValue = "'AKTIF'")
    val planDurumu: String = "AKTIF",
    @ColumnInfo(name = "ogrenme_durumu", defaultValue = "'YENI'")
    val ogrenmeDurumu: String = "YENI",
    @ColumnInfo(name = "son_tekrar_gun")
    val sonTekrarGun: Int? = null,
    @ColumnInfo(name = "sonraki_tekrar_gun")
    val sonrakiTekrarGun: Int? = null,
    @ColumnInfo(name = "olusturma")
    val olusturma: Long,
    @ColumnInfo(name = "guncelleme")
    val guncelleme: Long,
)

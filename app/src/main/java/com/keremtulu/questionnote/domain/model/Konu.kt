package com.keremtulu.questionnote.domain.model

data class Konu(
    val id: String,
    val dersId: String,
    val ad: String,
    val sira: Int,
    val arsivli: Boolean,
)

sealed interface KonuIslemSonucu {
    data object Basarili : KonuIslemSonucu
    data object BosAd : KonuIslemSonucu
    data object AyniAdVar : KonuIslemSonucu
    data object Bulunamadi : KonuIslemSonucu
}

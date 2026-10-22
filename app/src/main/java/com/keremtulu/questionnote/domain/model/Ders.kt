package com.keremtulu.questionnote.domain.model

data class Ders(
    val id: String,
    val ad: String,
    val sira: Int,
    val soruSayisi: Int,
    val arsivli: Boolean = false,
)

sealed interface DersIslemSonucu {
    data object Basarili : DersIslemSonucu
    data object BosAd : DersIslemSonucu
    data object AyniAdVar : DersIslemSonucu
    data object Bulunamadi : DersIslemSonucu
}

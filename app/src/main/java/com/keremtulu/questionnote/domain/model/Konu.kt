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

    /** Aynı ad, arşivlenmiş bir konuya ait; kullanıcı önce onu etkinleştirmeli. */
    data object ArsivliAyniAdVar : KonuIslemSonucu

    data object Bulunamadi : KonuIslemSonucu
}

/** Outcome of deleting a topic. Topics still used by questions cannot be deleted. */
sealed interface KonuSilmeSonucu {
    data object Basarili : KonuSilmeSonucu
    data object Bulunamadi : KonuSilmeSonucu
    data class Kullanimda(val soruSayisi: Int) : KonuSilmeSonucu
}

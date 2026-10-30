package com.keremtulu.questionnote.domain.model

/** Local view preferences; these do not change question, review or backup data. */
data class FiltreliCalismaEkraniAyar(
    val filtre: SoruCalismaFiltresi = SoruCalismaFiltresi(),
    val soruSayisi: String = "10",
    val akilliSira: Boolean = true,
)

data class DersCalismaAyar(
    val konuId: String? = null,
    val konusuz: Boolean = false,
    val planDurumu: SoruPlanDurumu? = null,
    val sadeceFavori: Boolean = false,
    val arama: String = "",
)

package com.keremtulu.questionnote.domain.model

/** Optional filters for a focused study session. Selected dimensions are combined with AND. */
data class SoruCalismaFiltresi(
    val konuId: String? = null,
    val konusuz: Boolean = false,
    val kaynak: String? = null,
    val kaynakBelirtilmemis: Boolean = false,
    val yanlisNedeni: String? = null,
    val yanlisNedeniBelirtilmemis: Boolean = false,
)

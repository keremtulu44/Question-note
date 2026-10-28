package com.keremtulu.questionnote.domain.model

enum class NetKurali {
    DORTTE_BIR,
    UCTE_BIR,
    YOK,
    ;

    fun net(dogru: Int, yanlis: Int): Double = when (this) {
        DORTTE_BIR -> dogru - yanlis / 4.0
        UCTE_BIR -> dogru - yanlis / 3.0
        YOK -> dogru.toDouble()
    }
}

data class DenemeDersSatiri(
    val dersId: String,
    val soruSayisi: Int,
    val dogru: Int,
    val yanlis: Int,
    val dersAdi: String? = null,
) {
    // Long arithmetic also keeps an invalid imported record from overflowing before validation.
    val bos: Int get() = (soruSayisi.toLong() - dogru - yanlis).coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
    val yuklenecek: Int get() = (yanlis.toLong() + bos).coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
    fun gecerli(): Boolean =
        soruSayisi >= 0 && dogru >= 0 && yanlis >= 0 && dogru.toLong() + yanlis <= soruSayisi.toLong()
}

data class DenemeKaydi(
    val id: String,
    val ad: String,
    val kural: NetKurali,
    val olusturma: Long,
    val gun: Int,
    val satirlar: List<DenemeDersSatiri>,
) {
    val toplamYuklenecek: Int get() = satirlar.sumOf { it.yuklenecek }
    fun toplamNet(): Double = satirlar.sumOf { kural.net(it.dogru, it.yanlis) }
    fun satir(dersId: String): DenemeDersSatiri? = satirlar.firstOrNull { it.dersId == dersId }
}

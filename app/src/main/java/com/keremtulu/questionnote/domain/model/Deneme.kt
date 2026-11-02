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
    val bos: Int get() = (soruSayisi - dogru - yanlis).coerceAtLeast(0)
    val yuklenecek: Int get() = yanlis + bos
    fun netOrani(kural: NetKurali): Double? =
        soruSayisi.takeIf { it > 0 }?.let { kural.net(dogru, yanlis) / it * 100.0 }
    fun gecerli(): Boolean =
        soruSayisi >= 0 && dogru >= 0 && yanlis >= 0 && dogru + yanlis <= soruSayisi
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
    val toplamSoru: Int get() = satirlar.sumOf { it.soruSayisi }
    fun toplamNet(): Double = satirlar.sumOf { kural.net(it.dogru, it.yanlis) }
    fun netOrani(): Double? = toplamSoru.takeIf { it > 0 }?.let { toplamNet() / it * 100.0 }
    fun satir(dersId: String): DenemeDersSatiri? = satirlar.firstOrNull { it.dersId == dersId }
}

package com.keremtulu.questionnote.domain.model

enum class KullaniciRolu {
    OGRENCI,
    OGRETMEN,
    ;

    companion object {
        fun anahtarla(deger: String?): KullaniciRolu =
            entries.firstOrNull { it.name == deger } ?: OGRENCI
    }
}

enum class OgretmenZorluk {
    KOLAY,
    ORTA,
    ZOR,
}

data class OgretmenGrubu(
    val id: String,
    val ad: String,
    val aciklama: String? = null,
    val sira: Int = 0,
    val olusturma: Long = 0L,
)

data class OgretmenGorseli(
    val id: String,
    val sira: Int,
    val dosyaAdi: String,
    val mimeType: String = "image/jpeg",
    val genislik: Int,
    val yukseklik: Int,
    val boyut: Long,
    val sha256: String,
) {
    fun toGirdi(): SoruGorselGirdisi = SoruGorselGirdisi(
        id = id,
        tur = GorselTuru.SORU,
        sira = sira,
        dosyaAdi = dosyaAdi,
        gecici = false,
        mimeType = mimeType,
        genislik = genislik,
        yukseklik = yukseklik,
        boyut = boyut,
        sha256 = sha256,
    )
}

data class OgretmenSorusu(
    val id: String,
    val grupId: String,
    val baslik: String? = null,
    val zorluk: OgretmenZorluk? = null,
    val cevap: String? = null,
    val cozuldu: Boolean = false,
    val gorseller: List<OgretmenGorseli>,
    val olusturma: Long,
    val guncelleme: Long,
)

data class OgretmenVerisi(
    val gruplar: List<OgretmenGrubu> = emptyList(),
    val sorular: List<OgretmenSorusu> = emptyList(),
) {
    fun bosDegil(): Boolean = gruplar.isNotEmpty() || sorular.isNotEmpty()

    fun kaliciDosyaAdlari(): Set<String> =
        sorular.flatMap { soru -> soru.gorseller.map { it.dosyaAdi } }.toSet()
}

package com.keremtulu.questionnote.domain.model

enum class SoruPlanDurumu {
    AKTIF,
    ASKIDA,
    TAMAMLANDI,
}

enum class OgrenmeDurumu {
    YENI,
    OGRENIYOR,
    PEKISIYOR,
    SAGLAM,
}

enum class GorselTuru {
    SORU,
    COZUM,
}

data class SoruGorseli(
    val id: String,
    val soruId: String,
    val tur: GorselTuru,
    val sira: Int,
    val dosyaAdi: String,
    val mimeType: String,
    val genislik: Int,
    val yukseklik: Int,
    val boyut: Long,
    val sha256: String,
)

/** A selected image is either already in durable storage or in the temporary staging area. */
data class SoruGorselGirdisi(
    val id: String,
    val tur: GorselTuru,
    val sira: Int,
    val dosyaAdi: String,
    val gecici: Boolean,
    val mimeType: String,
    val genislik: Int,
    val yukseklik: Int,
    val boyut: Long,
    val sha256: String,
)

data class Soru(
    val id: String,
    val dersId: String,
    val konuId: String?,
    val soruNotu: String?,
    val cozumNotu: String?,
    val kaynak: String?,
    val kaynakKonumu: String? = null,
    val yanlisNedeni: String?,
    val favori: Boolean,
    val planDurumu: SoruPlanDurumu,
    val ogrenmeDurumu: OgrenmeDurumu,
    val sonTekrarGun: Int?,
    val sonrakiTekrarGun: Int?,
    val olusturma: Long,
    val guncelleme: Long,
    val gorseller: List<SoruGorseli> = emptyList(),
)

data class SoruKaydetmeIstegi(
    val id: String? = null,
    val dersId: String,
    val konuId: String? = null,
    val soruNotu: String = "",
    val cozumNotu: String = "",
    val kaynak: String = "",
    val kaynakKonumu: String = "",
    val yanlisNedeni: String = "",
    val favori: Boolean = false,
    val planDurumu: SoruPlanDurumu = SoruPlanDurumu.AKTIF,
    val gorseller: List<SoruGorselGirdisi>,
)

sealed interface SoruIslemSonucu {
    data object Basarili : SoruIslemSonucu
    data object Bulunamadi : SoruIslemSonucu
    data object GecersizDers : SoruIslemSonucu
    data object GecersizKonu : SoruIslemSonucu
    data object SoruGorseliGerekli : SoruIslemSonucu
    data object GorselGecersiz : SoruIslemSonucu
    data object AyniGunTekrarVar : SoruIslemSonucu
    data object PlanDurumuKapali : SoruIslemSonucu
}

enum class TekrarSonucu {
    COZEMEDIM,
    ZORLANDIM,
    RAHAT,
}

data class TekrarKaydi(
    val id: String,
    val gun: Int,
    val planlananGun: Int?,
    val sonuc: TekrarSonucu,
    val cozumAcildi: Boolean,
    val oncekiAralik: Int?,
    val sonrakiAralik: Int?,
    val elleAtandi: Boolean,
)

data class TekrarPlani(
    val oncekiAralik: Int,
    val sonrakiAralik: Int,
    val sonrakiGun: Int,
    val ogrenmeDurumu: OgrenmeDurumu,
)

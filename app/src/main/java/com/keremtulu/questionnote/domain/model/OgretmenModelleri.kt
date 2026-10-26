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

/**
 * Single source of truth for a teacher question. `solved` (v1) is mapped on read:
 * true -> COZULDU, false -> BEKLIYOR. ARSIV keeps the row and every file, it only leaves the queue.
 */
enum class OgretmenDurum {
    BEKLIYOR,
    COZULDU,
    ARSIV,
    ;

    companion object {
        fun anahtarla(deger: String?): OgretmenDurum =
            entries.firstOrNull { it.name == deger } ?: BEKLIYOR
    }
}

data class OgretmenGrubu(
    val id: String,
    val ad: String,
    val aciklama: String? = null,
    /** Optional link to the shared student lesson catalog; never required. */
    val dersId: String? = null,
    val arsivli: Boolean = false,
    val sira: Int = 0,
    val olusturma: Long = 0L,
)

data class OgretmenGorseli(
    val id: String,
    val sira: Int,
    /** Relative name: `img/<uuid>.jpg` (shared with the student archive) or `ogretmen-img/<uuid>.jpg`. */
    val dosyaAdi: String,
    val tur: GorselTuru = GorselTuru.SORU,
    val mimeType: String = "image/jpeg",
    val genislik: Int,
    val yukseklik: Int,
    val boyut: Long,
    val sha256: String,
) {
    fun toGirdi(): SoruGorselGirdisi = SoruGorselGirdisi(
        id = id,
        tur = tur,
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
    val dersId: String? = null,
    val konuId: String? = null,
    val kaynak: String? = null,
    val kimSordu: String? = null,
    /** Optional due day as epoch day; used only for ordering, never for deletion. */
    val planGun: Long? = null,
    val zorluk: OgretmenZorluk? = null,
    val cevap: String? = null,
    val durum: OgretmenDurum = OgretmenDurum.BEKLIYOR,
    val cozulme: Long? = null,
    val gorseller: List<OgretmenGorseli>,
    val olusturma: Long,
    val guncelleme: Long,
) {
    /** Kept for existing call sites: archived rows still count as solved. */
    val cozuldu: Boolean get() = durum != OgretmenDurum.BEKLIYOR

    val kuyrukta: Boolean get() = durum == OgretmenDurum.BEKLIYOR

    fun soruGorselleri(): List<OgretmenGorseli> =
        gorseller.filter { it.tur == GorselTuru.SORU }.sortedBy { it.sira }

    fun cozumGorselleri(): List<OgretmenGorseli> =
        gorseller.filter { it.tur == GorselTuru.COZUM }.sortedBy { it.sira }
}

data class OgretmenVerisi(
    val gruplar: List<OgretmenGrubu> = emptyList(),
    val sorular: List<OgretmenSorusu> = emptyList(),
) {
    fun bosDegil(): Boolean = gruplar.isNotEmpty() || sorular.isNotEmpty()

    fun kaliciDosyaAdlari(): Set<String> =
        sorular.flatMap { soru -> soru.gorseller.map { it.dosyaAdi } }.toSet()

    fun grupSorulari(grupId: String): List<OgretmenSorusu> =
        sorular.filter { it.grupId == grupId }

    /** Queue across every class: unsolved, oldest first, optional group/lesson narrowing. */
    fun kuyruk(
        grupId: String? = null,
        dersId: String? = null,
        yalnizGeciken: Boolean = false,
        bugun: Long,
    ): List<OgretmenSorusu> = sorular
        .asSequence()
        .filter { it.kuyrukta }
        .filter { grupId == null || it.grupId == grupId }
        .filter { dersId == null || it.dersId == dersId }
        .filter { !yalnizGeciken || (it.planGun != null && it.planGun < bugun) }
        .sortedWith(compareBy<OgretmenSorusu> { it.planGun ?: Long.MAX_VALUE }.thenBy { it.olusturma })
        .toList()

    /** Guards restore and import: a question must point at an existing group and keep at least one question image. */
    fun tutarliMi(): Boolean {
        val grupIdleri = gruplar.mapTo(mutableSetOf()) { it.id }
        if (grupIdleri.size != gruplar.size) return false
        if (sorular.mapTo(mutableSetOf()) { it.id }.size != sorular.size) return false
        return sorular.all { soru ->
            soru.grupId in grupIdleri && soru.soruGorselleri().isNotEmpty()
        }
    }
}

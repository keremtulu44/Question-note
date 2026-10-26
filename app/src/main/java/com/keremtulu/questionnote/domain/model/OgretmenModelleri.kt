package com.keremtulu.questionnote.domain.model

import java.util.Locale

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

/**
 * Day filter of the queue: "what must I solve today" is a different question than "what is open",
 * so the three cases are explicit instead of a single overdue checkbox.
 */
enum class KuyrukGunFiltresi {
    HEPSI,
    BUGUN,
    GECIKEN,
    PLANLANMAMIS,
    ;
    companion object {
        fun anahtarla(deger: String?): KuyrukGunFiltresi =
            entries.firstOrNull { it.name == deger } ?: HEPSI
    }
}

/** Turkish-aware search key. Shared by the search box and the "who asked" chips. */
fun aramaAnahtari(metin: String?): String =
    metin?.trim()?.lowercase(Locale.forLanguageTag("tr-TR")).orEmpty()

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

    /** Queue across every class: unsolved, oldest first, optional group/lesson/day narrowing. */
    fun kuyruk(
        grupId: String? = null,
        dersId: String? = null,
        gunFiltresi: KuyrukGunFiltresi = KuyrukGunFiltresi.HEPSI,
        bugun: Long,
    ): List<OgretmenSorusu> = sorular
        .asSequence()
        .filter { it.kuyrukta }
        .filter { grupId == null || it.grupId == grupId }
        .filter { dersId == null || it.dersId == dersId }
        .filter { soru ->
            when (gunFiltresi) {
                KuyrukGunFiltresi.HEPSI -> true
                KuyrukGunFiltresi.BUGUN -> soru.planGun == bugun
                KuyrukGunFiltresi.GECIKEN -> soru.planGun != null && soru.planGun < bugun
                KuyrukGunFiltresi.PLANLANMAMIS -> soru.planGun == null
            }
        }
        .sortedWith(compareBy<OgretmenSorusu> { it.planGun ?: Long.MAX_VALUE }.thenBy { it.olusturma })
        .toList()

    /**
     * Search + lesson/topic/"who asked" narrowing over an already chosen list (queue or one tab).
     * Every word must appear somewhere in the same question; the answer text is never searched so a
     * search cannot leak the solution into a results list.
     */
    fun filtrele(
        liste: List<OgretmenSorusu> = sorular,
        arama: String = "",
        dersId: String? = null,
        konuId: String? = null,
        kimSordu: String? = null,
        dersAdlari: Map<String, String> = emptyMap(),
        konuAdlari: Map<String, String> = emptyMap(),
    ): List<OgretmenSorusu> {
        val anahtar = aramaAnahtari(arama)
        val kelimeler = anahtar.split(' ').filter { it.isNotEmpty() }
        val soranAnahtari = aramaAnahtari(kimSordu)
        if (kelimeler.isEmpty() && dersId == null && konuId == null && soranAnahtari.isEmpty()) {
            return liste
        }
        return liste.filter { soru ->
            val havuz = soru.aramaHavuzu(
                dersAdi = soru.dersId?.let { dersAdlari[it] },
                konuAdi = soru.konuId?.let { konuAdlari[it] },
            )
            (dersId == null || soru.dersId == dersId) &&
                (konuId == null || soru.konuId == konuId) &&
                (soranAnahtari.isEmpty() || aramaAnahtari(soru.kimSordu) == soranAnahtari) &&
                kelimeler.all { kelime -> kelime in havuz }
        }
    }

    /** Distinct "who asked" names, most frequent first; the first spelling wins. */
    fun kimSorduIsimleri(liste: List<OgretmenSorusu> = sorular): List<String> {
        val sayim = LinkedHashMap<String, Pair<String, Int>>()
        liste.forEach { soru ->
            val ham = soru.kimSordu?.trim()?.takeIf { it.isNotEmpty() } ?: return@forEach
            val anahtar = aramaAnahtari(ham)
            val mevcut = sayim[anahtar]
            sayim[anahtar] = if (mevcut == null) ham to 1 else mevcut.first to (mevcut.second + 1)
        }
        return sayim.values.sortedByDescending { it.second }.map { it.first }
    }

    /**
     * Duplicate-photo guard: the same file (sha256) is already attached to another question, which
     * in practice means the same question was added twice.
     */
    fun ayniGorselliSoru(sha256ler: Set<String>, haricSoruId: String? = null): OgretmenSorusu? {
        val aranan = sha256ler.filter { it.isNotEmpty() }.toSet()
        if (aranan.isEmpty()) return null
        return sorular.firstOrNull { soru ->
            soru.id != haricSoruId && soru.gorseller.any { it.sha256 in aranan }
        }
    }

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

/**
 * Lower-cased haystack of one question: title, who asked, source and the resolved lesson/topic names.
 * Answer text stays out on purpose — the teacher's search results are shown on a projected screen.
 */
fun OgretmenSorusu.aramaHavuzu(dersAdi: String?, konuAdi: String?): String =
    listOfNotNull(baslik, kimSordu, kaynak, dersAdi, konuAdi)
        .joinToString(separator = " ") { aramaAnahtari(it) }

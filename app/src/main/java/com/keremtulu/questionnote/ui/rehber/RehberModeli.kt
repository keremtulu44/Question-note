package com.keremtulu.questionnote.ui.rehber

import com.keremtulu.questionnote.domain.model.DenemeDersSatiri
import com.keremtulu.questionnote.domain.model.DenemeKaydi
import com.keremtulu.questionnote.domain.model.NetKurali
import com.keremtulu.questionnote.domain.model.TekrarPlani
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import com.keremtulu.questionnote.domain.model.ozet
import com.keremtulu.questionnote.domain.repeat.TekrarKurali
import java.time.LocalDate

/**
 * Rehberin bölüm kimlikleri.
 *
 * Sıra kullanıcıya gösterilen sıradır; `anahtar` yalnız kalıcılık ve test için vardır.
 * Yeni bölüm sona eklenir ki kayıtlı `sonBolum` değeri başka bölüme kaymasın.
 */
internal enum class RehberBolumu(val anahtar: String) {
    TANITIM("tanitim"),
    KAYIT("kayit"),
    BUGUN("bugun"),
    DENEME("deneme"),
    COZUM("cozum"),
    YEDEK("yedek"),
    KAPANIS("kapanis"),
    ;

    companion object {
        fun anahtarla(anahtar: String?): RehberBolumu? = entries.firstOrNull { it.anahtar == anahtar }
    }
}

internal enum class DemoSoruDurumu {
    GECIKTI,
    BUGUN,
    YAKINDA,
}

/** Rehberdeki örnek soru: hiçbiri veritabanına yazılmaz, sadece anlatım içindir. */
internal data class DemoSoru(
    val ders: String,
    val konu: String,
    val gecikmeGun: Int,
    val durum: DemoSoruDurumu,
)

internal fun demoSorular(): List<DemoSoru> = listOf(
    DemoSoru("Matematik", "Türev · zincir kuralı", gecikmeGun = 2, durum = DemoSoruDurumu.GECIKTI),
    DemoSoru("Fizik", "Newton yasaları · sürtünme", gecikmeGun = 3, durum = DemoSoruDurumu.GECIKTI),
    DemoSoru("Matematik", "Limit · belirsizlik", gecikmeGun = 0, durum = DemoSoruDurumu.BUGUN),
    DemoSoru("Türkçe", "Paragraf · ana düşünce", gecikmeGun = 0, durum = DemoSoruDurumu.BUGUN),
    DemoSoru("Kimya", "Mol hesabı", gecikmeGun = -1, durum = DemoSoruDurumu.YAKINDA),
)

internal data class DemoKuyrukOzeti(
    val turBoyu: Int,
    val siradaKalan: Int,
    val geciken: Int,
    val onizleme: List<DemoSoru>,
)

/**
 * Bugün bölümündeki örnek kuyruk. Tur boyu uygulamanın gerçek kuralıyla aynı mantıkta
 * türetilir: kalan hedef 5–30 aralığına sıkıştırılır, önizleme en fazla üç satırdır.
 */
internal fun demoKuyrugu(hedef: Int = 10, yapilan: Int = 3): DemoKuyrukOzeti {
    val hepsi = demoSorular()
    val kalanHedef = (hedef - yapilan).coerceAtLeast(0)
    val turBoyu = kalanHedef.coerceIn(5, 30).coerceAtMost(hepsi.size).coerceAtLeast(1)
    return DemoKuyrukOzeti(
        turBoyu = turBoyu,
        siradaKalan = (hepsi.size - turBoyu).coerceAtLeast(0),
        geciken = hepsi.count { it.durum == DemoSoruDurumu.GECIKTI },
        onizleme = hepsi.sortedByDescending { it.gecikmeGun }.take(3),
    )
}

/**
 * Kayıt bölümündeki "sonraki tekrar ne zaman?" önizlemesi.
 *
 * Gerçek motor (`TekrarKurali`) kullanılır; rehber kendi kuralını uydurmaz.
 * `cozumAcildi` bilinçli olarak `false`: rehberde çözüm açma adımı yok.
 */
internal fun tekrarOnizlemesi(
    sonuc: TekrarSonucu,
    bugun: LocalDate,
    oncekiAralik: Int? = null,
): TekrarPlani = TekrarKurali.hesapla(
    oncekiAralik = oncekiAralik,
    sonuc = sonuc,
    cozumAcildi = false,
    bugun = bugun,
)

/** Deneme bölümündeki etkileşimli ders satırı. */
internal data class DemoDersSkoru(
    val dersAdi: String,
    val soruSayisi: Int = 20,
    val dogru: Int = 12,
    val yanlis: Int = 4,
) {
    val bos: Int get() = (soruSayisi - dogru - yanlis).coerceAtLeast(0)

    fun net(kural: NetKurali): Double = kural.net(dogru, yanlis)

    /** Doğru + yanlış soru sayısını aşmasın: artırma düğmeleri bu sınırı kullanır. */
    fun dogruArtir(): DemoDersSkoru = if (dogru + yanlis < soruSayisi) copy(dogru = dogru + 1) else this
    fun dogruAzalt(): DemoDersSkoru = if (dogru > 0) copy(dogru = dogru - 1) else this
    fun yanlisArtir(): DemoDersSkoru = if (dogru + yanlis < soruSayisi) copy(yanlis = yanlis + 1) else this
    fun yanlisAzalt(): DemoDersSkoru = if (yanlis > 0) copy(yanlis = yanlis - 1) else this
}

/** Deneme bölümünün örnek geçmişi: özet gerçek modelle hesaplanır. */
internal fun demoDenemeler(): List<DenemeKaydi> = listOf(
    DenemeKaydi(
        id = "rehber-1",
        ad = "Örnek deneme 1",
        kural = NetKurali.DORTTE_BIR,
        olusturma = 10,
        gun = 19_990,
        satirlar = listOf(DenemeDersSatiri("mat", 20, 9, 6, "Matematik")),
    ),
    DenemeKaydi(
        id = "rehber-2",
        ad = "Örnek deneme 2",
        kural = NetKurali.DORTTE_BIR,
        olusturma = 20,
        gun = 20_004,
        satirlar = listOf(DenemeDersSatiri("mat", 20, 14, 3, "Matematik")),
    ),
)

/**
 * Deneme bölümünde gösterilen özet gerçek `ozet()` fonksiyonundan geçer: böylece rehberde
 * gösterilen "ortalama / en iyi net" uygulamanın başka yerindeki kuralla aynı olur
 * (karışık puanlama kuralında ortalama üretilmez).
 */
internal fun demoDenemeOzeti() = demoDenemeler().ozet()

/** Yedek bölümü: neyin taşındığı, neyin taşınmadığı. */
internal data class YedekOgesi(val adAnahtari: String, val dahil: Boolean)

internal fun yedekIcerigi(): List<YedekOgesi> = listOf(
    YedekOgesi("rehber_backup_item_questions", true),
    YedekOgesi("rehber_backup_item_images", true),
    YedekOgesi("rehber_backup_item_solutions", true),
    YedekOgesi("rehber_backup_item_reviews", true),
    YedekOgesi("rehber_backup_item_denemeler", true),
    YedekOgesi("rehber_backup_item_catalog", true),
    YedekOgesi("rehber_backup_item_badges", false),
    YedekOgesi("rehber_backup_item_guide", false),
)

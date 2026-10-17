package com.keremtulu.questionnote.domain.karne

/** Stable keys preserve previously earned badges across upgrades. Hidden metadata stays out of UI. */
enum class Rozet(val anahtar: String, val gizli: Boolean = false) {
    ILK_KARNE("ilk_karne"),
    KUSURSUZ_TUR("kusursuz_tur"),
    ON_BAGIMSIZ("on_bagimsiz"),
    YIRMI_SORU("yirmi_soru"),
    COZUMSUZ("cozumsuz"),
    ISTIKRAR("istikrar"),
    SERI_3("seri_3"),
    SERI_7("seri_7"),
    SERI_30("seri_30"),
    YUZ_BAGIMSIZ("yuz_bagimsiz"),
    BES_YUZ("bes_yuz"),
    ILK_SORU("ilk_soru"),
    ARSIVCI("arsivci"),
    KUTUPHANE("kutuphane"),
    KOLEKSIYONCU("koleksiyoncu"),
    ELLI_TEKRAR("elli_tekrar"),
    IKI_YUZ_TEKRAR("iki_yuz_tekrar"),
    BIN_TEKRAR("bin_tekrar"),
    BIN_BAGIMSIZ("bin_bagimsiz"),
    SERI_14("seri_14"),
    SERI_60("seri_60"),
    SERI_100("seri_100"),
    HEDEF_1("hedef_1"),
    HEDEF_7("hedef_7"),
    HEDEF_30("hedef_30"),
    UC_DERS("uc_ders", gizli = true),
    BES_DERS("bes_ders", gizli = true),
    DONUSUM("donusum", gizli = true),
    ZINCIR_10("zincir_10", gizli = true),
    KUSURSUZ_20("kusursuz_20", gizli = true),
    ;

    companion object {
        fun anahtarla(deger: String): Rozet? = entries.firstOrNull { it.anahtar == deger }
    }
}

data class GunDenge(val gun: Int, val toplam: Int, val bagimsiz: Int) {
    val oran: Int get() = if (toplam == 0) 0 else (bagimsiz.toLong() * 100 / toplam).toInt()
}

data class RozetGirdisi(
    val oturum: KarneSayilari,
    val seri: Int,
    val toplamBagimsiz: Int,
    /** Exactly the last five local days, oldest first; days without reviews have zero counts. */
    val sonBesGun: List<GunDenge>,
    val soruSayisi: Int = 0,
    val tekrarSayisi: Int = 0,
    val hedefGunSayisi: Int = 0,
    val birGundeDersSayisi: Int = 0,
    val toplamDersSayisi: Int = 0,
    val donusumVar: Boolean = false,
    val bagimsizZincir: Int = 0,
)

data class RozetIlerlemesi(val mevcut: Int, val hedef: Int) {
    val tamam: Boolean get() = mevcut >= hedef
    val oran: Float get() = (mevcut.toFloat() / hedef.coerceAtLeast(1)).coerceIn(0f, 1f)
}

object RozetKurallari {
    private const val ISTIKRAR_ORANI = 70

    /** Visible requirements and award predicates use the SAME calculation. */
    fun ilerleme(rozet: Rozet, girdi: RozetGirdisi): RozetIlerlemesi {
        val o = girdi.oturum
        val kusursuz = if (o.bagimsiz == o.toplam) o.toplam else 0
        val istikrar = girdi.sonBesGun.takeLast(5).asReversed().takeWhile {
            it.toplam >= KarneSayilari.KISA_OTURUM_ESIGI && it.oran >= ISTIKRAR_ORANI
        }.size
        val (mevcut, hedef) = when (rozet) {
            Rozet.ILK_KARNE -> o.toplam to 1
            Rozet.KUSURSUZ_TUR -> kusursuz to 5
            Rozet.ON_BAGIMSIZ -> o.bagimsiz to 10
            Rozet.YIRMI_SORU -> o.toplam to 20
            Rozet.COZUMSUZ -> (if (o.cozumle == 0) o.toplam else 0) to 5
            Rozet.ISTIKRAR -> istikrar to 5
            Rozet.SERI_3 -> girdi.seri to 3
            Rozet.SERI_7 -> girdi.seri to 7
            Rozet.SERI_14 -> girdi.seri to 14
            Rozet.SERI_30 -> girdi.seri to 30
            Rozet.SERI_60 -> girdi.seri to 60
            Rozet.SERI_100 -> girdi.seri to 100
            Rozet.YUZ_BAGIMSIZ -> girdi.toplamBagimsiz to 100
            Rozet.BES_YUZ -> girdi.toplamBagimsiz to 500
            Rozet.BIN_BAGIMSIZ -> girdi.toplamBagimsiz to 1000
            Rozet.ILK_SORU -> girdi.soruSayisi to 1
            Rozet.ARSIVCI -> girdi.soruSayisi to 25
            Rozet.KUTUPHANE -> girdi.soruSayisi to 100
            Rozet.KOLEKSIYONCU -> girdi.soruSayisi to 250
            Rozet.ELLI_TEKRAR -> girdi.tekrarSayisi to 50
            Rozet.IKI_YUZ_TEKRAR -> girdi.tekrarSayisi to 200
            Rozet.BIN_TEKRAR -> girdi.tekrarSayisi to 1000
            Rozet.HEDEF_1 -> girdi.hedefGunSayisi to 1
            Rozet.HEDEF_7 -> girdi.hedefGunSayisi to 7
            Rozet.HEDEF_30 -> girdi.hedefGunSayisi to 30
            Rozet.UC_DERS -> girdi.birGundeDersSayisi to 3
            Rozet.BES_DERS -> girdi.toplamDersSayisi to 5
            Rozet.DONUSUM -> (if (girdi.donusumVar) 1 else 0) to 1
            Rozet.ZINCIR_10 -> girdi.bagimsizZincir to 10
            Rozet.KUSURSUZ_20 -> kusursuz to 20
        }
        return RozetIlerlemesi(mevcut.coerceAtLeast(0), hedef)
    }

    /** Idempotent; caller persists ownership before showing celebrations. */
    fun kazanilanlar(girdi: RozetGirdisi, sahipOlanlar: Set<Rozet>): List<Rozet> =
        Rozet.entries.filter { it !in sahipOlanlar && ilerleme(it, girdi).tamam }

    fun enUzunBagimsizZincir(sonuclar: List<OturumSonucu>): Int {
        var zincir = 0
        var enUzun = 0
        sonuclar.forEach {
            zincir = if (KarneHesabi.kova(it) == KarneKovasi.BAGIMSIZ) zincir + 1 else 0
            enUzun = maxOf(enUzun, zincir)
        }
        return enUzun
    }
}

package com.keremtulu.questionnote.domain.karne

/** Device-local achievements. Stored in SharedPreferences, deliberately not part of the ZIP backup. */
enum class Rozet(val anahtar: String) {
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
    ;

    companion object {
        fun anahtarla(deger: String): Rozet? = values().firstOrNull { it.anahtar == deger }
    }
}

data class GunDenge(val gun: Int, val toplam: Int, val bagimsiz: Int) {
    val oran: Int get() = if (toplam == 0) 0 else bagimsiz * 100 / toplam
}

data class RozetGirdisi(
    val oturum: KarneSayilari,
    val seri: Int,
    val toplamBagimsiz: Int,
    /** Exactly the last five local days, oldest first; days without reviews are present with zeros. */
    val sonBesGun: List<GunDenge>,
)

object RozetKurallari {
    private const val ISTIKRAR_ORANI = 70

    /** Returns only badges the user does not own yet, so awarding is idempotent. */
    fun kazanilanlar(girdi: RozetGirdisi, sahipOlanlar: Set<Rozet>): List<Rozet> = buildList {
        fun ekle(rozet: Rozet, kosul: Boolean) {
            if (kosul && rozet !in sahipOlanlar && rozet !in this) add(rozet)
        }

        val oturum = girdi.oturum
        ekle(Rozet.ILK_KARNE, oturum.toplam > 0)
        ekle(
            Rozet.KUSURSUZ_TUR,
            oturum.toplam >= KarneSayilari.KISA_OTURUM_ESIGI && oturum.bagimsiz == oturum.toplam,
        )
        ekle(Rozet.ON_BAGIMSIZ, oturum.bagimsiz >= 10)
        ekle(Rozet.YIRMI_SORU, oturum.toplam >= 20)
        ekle(
            Rozet.COZUMSUZ,
            oturum.toplam >= KarneSayilari.KISA_OTURUM_ESIGI && oturum.cozumle == 0,
        )
        ekle(
            Rozet.ISTIKRAR,
            girdi.sonBesGun.size == 5 && girdi.sonBesGun.all {
                it.toplam >= KarneSayilari.KISA_OTURUM_ESIGI && it.oran >= ISTIKRAR_ORANI
            },
        )
        ekle(Rozet.SERI_3, girdi.seri >= 3)
        ekle(Rozet.SERI_7, girdi.seri >= 7)
        ekle(Rozet.SERI_30, girdi.seri >= 30)
        ekle(Rozet.YUZ_BAGIMSIZ, girdi.toplamBagimsiz >= 100)
        ekle(Rozet.BES_YUZ, girdi.toplamBagimsiz >= 500)
    }
}

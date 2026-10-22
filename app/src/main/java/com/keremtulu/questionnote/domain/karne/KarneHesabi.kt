package com.keremtulu.questionnote.domain.karne

import com.keremtulu.questionnote.domain.model.TekrarSonucu

/** One answer given during a study session; the card is computed from these, not from the database. */
data class OturumSonucu(
    val soruId: String,
    val sonuc: TekrarSonucu,
    val cozumAcildi: Boolean,
)

/** Mutually exclusive buckets. A question opened with the solution always lands in [COZUMLU]. */
enum class KarneKovasi {
    BAGIMSIZ,
    YARIM,
    COZEMEDI,
    COZUMLU,
}

data class KarneSayilari(
    val toplam: Int,
    val bagimsiz: Int,
    val yarim: Int,
    val cozemedi: Int,
    val cozumle: Int,
) {
    /** Independent-solve percentage, rounded half-up. */
    val oran: Int
        get() = if (toplam == 0) 0 else ((bagimsiz * 100) + toplam / 2) / toplam

    val kisaOturum: Boolean
        get() = toplam < KISA_OTURUM_ESIGI

    companion object {
        /**
         * Sessions below this size show a simplified card. Kept as a single constant so it can become
         * a preference later without touching call sites (design decision S10).
         */
        const val KISA_OTURUM_ESIGI = 5
    }
}

enum class TebrikSeviyesi {
    KISA_OTURUM,
    DUSUK,
    ORTA,
    IYI,
    HARIKA,
}

object KarneHesabi {
    fun kova(sonuc: OturumSonucu): KarneKovasi = when {
        sonuc.cozumAcildi -> KarneKovasi.COZUMLU
        sonuc.sonuc == TekrarSonucu.RAHAT -> KarneKovasi.BAGIMSIZ
        sonuc.sonuc == TekrarSonucu.ZORLANDIM -> KarneKovasi.YARIM
        else -> KarneKovasi.COZEMEDI
    }

    fun hesapla(sonuclar: List<OturumSonucu>): KarneSayilari {
        var bagimsiz = 0
        var yarim = 0
        var cozemedi = 0
        var cozumle = 0
        sonuclar.forEach { sonuc ->
            when (kova(sonuc)) {
                KarneKovasi.BAGIMSIZ -> bagimsiz++
                KarneKovasi.YARIM -> yarim++
                KarneKovasi.COZEMEDI -> cozemedi++
                KarneKovasi.COZUMLU -> cozumle++
            }
        }
        return KarneSayilari(
            toplam = sonuclar.size,
            bagimsiz = bagimsiz,
            yarim = yarim,
            cozemedi = cozemedi,
            cozumle = cozumle,
        )
    }

    fun tebrikSeviyesi(sayilar: KarneSayilari): TebrikSeviyesi = when {
        sayilar.kisaOturum -> TebrikSeviyesi.KISA_OTURUM
        sayilar.oran >= 90 -> TebrikSeviyesi.HARIKA
        sayilar.oran >= 70 -> TebrikSeviyesi.IYI
        sayilar.oran >= 50 -> TebrikSeviyesi.ORTA
        else -> TebrikSeviyesi.DUSUK
    }
}

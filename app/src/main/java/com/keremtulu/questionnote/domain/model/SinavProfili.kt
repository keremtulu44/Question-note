package com.keremtulu.questionnote.domain.model

import java.text.Normalizer
import java.util.Locale

/** Profiles are a view of the lesson catalogue, never an instruction to delete/archive data. */
enum class SinavProfili(val ad: String, val aciklama: String, val dersAdlari: List<String>) {
    OZEL(
        "Derslerim",
        "Kurulumda seçtiğin dersler",
        emptyList(),
    ),
    LGS(
        "LGS", "Ortaokul · liseye hazırlık",
        listOf("Türkçe", "Matematik", "Fen Bilimleri", "T.C. İnkılap Tarihi ve Atatürkçülük", "Din Kültürü ve Ahlak Bilgisi", "İngilizce"),
    ),
    YKS(
        "AYT / TYT", "YKS · temel ve alan yeterlilik",
        listOf(
            "Türkçe", "Edebiyat", "Matematik", "Geometri",
            "Fizik", "Kimya", "Biyoloji",
            "Tarih", "Coğrafya", "Felsefe", "Felsefe Grubu",
            "Din Kültürü ve Ahlak Bilgisi",
        ),
    ),
    SINIF_9_10(
        "9. / 10. sınıf", "Lise 9. ve 10. sınıf dersleri",
        listOf(
            "Türkçe", "Matematik", "Geometri",
            "Fizik", "Kimya", "Biyoloji",
            "Tarih", "Coğrafya", "İngilizce",
            "Din Kültürü ve Ahlak Bilgisi",
        ),
    ),
    ;

    fun dersAdiEslesir(ad: String): Boolean = dersAdlari.any { it.dersAnahtari() == ad.dersAnahtari() }

    companion object {
        fun anahtarla(anahtar: String?): SinavProfili = when (anahtar) {
            OZEL.name -> OZEL
            LGS.name -> LGS
            SINIF_9_10.name -> SINIF_9_10
            YKS.name, "TYT", "AYT", "SERBEST", "KPSS" -> YKS
            else -> YKS
        }
    }
}

internal fun String.dersAnahtari(): String =
    Normalizer.normalize(trim(), Normalizer.Form.NFC).lowercase(Locale.forLanguageTag("tr-TR"))

data class SinavSecimi(
    val profil: SinavProfili,
    val dersIdleri: Set<String> = emptySet(),
    val herDersiGoster: Boolean = false,
) {
    fun gorunur(ders: Ders): Boolean =
        herDersiGoster || ders.id in dersIdleri || profil.dersAdiEslesir(ders.ad)

    companion object {
        val Tumu = SinavSecimi(SinavProfili.YKS, herDersiGoster = true)
    }
}

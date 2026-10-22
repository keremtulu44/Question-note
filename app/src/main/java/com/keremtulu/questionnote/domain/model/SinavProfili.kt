package com.keremtulu.questionnote.domain.model

import java.text.Normalizer
import java.util.Locale

/** Profiles are a view of the lesson catalogue, never an instruction to delete/archive data. */
enum class SinavProfili(val ad: String, val aciklama: String, val dersAdlari: List<String>) {
    SERBEST("Tümü / özel", "Kendi programın. Mevcut bütün aktif dersler görünür.", emptyList()),
    LGS(
        "LGS", "Ortaokul · liseye hazırlık",
        listOf("Türkçe", "Matematik", "Fen Bilimleri", "T.C. İnkılap Tarihi ve Atatürkçülük", "Din Kültürü ve Ahlak Bilgisi", "İngilizce"),
    ),
    TYT(
        "TYT", "YKS · temel yeterlilik",
        listOf("Türkçe", "Matematik", "Geometri", "Fizik", "Kimya", "Biyoloji", "Tarih", "Coğrafya", "Felsefe", "Din Kültürü ve Ahlak Bilgisi"),
    ),
    AYT(
        "AYT", "YKS · alan yeterlilik",
        listOf("Edebiyat", "Matematik", "Geometri", "Fizik", "Kimya", "Biyoloji", "Tarih", "Coğrafya", "Felsefe Grubu", "Din Kültürü ve Ahlak Bilgisi"),
    ),
    KPSS(
        "KPSS", "Genel Yetenek · Genel Kültür",
        listOf("Türkçe", "Matematik", "Geometri", "Tarih", "Coğrafya", "Vatandaşlık", "Güncel Bilgiler"),
    ),
    ;

    fun dersAdiEslesir(ad: String): Boolean = dersAdlari.any { it.dersAnahtari() == ad.dersAnahtari() }

    companion object {
        fun anahtarla(anahtar: String?): SinavProfili = entries.firstOrNull { it.name == anahtar } ?: SERBEST
    }
}

internal fun String.dersAnahtari(): String =
    Normalizer.normalize(trim(), Normalizer.Form.NFC).lowercase(Locale.forLanguageTag("tr-TR"))

data class SinavSecimi(val profil: SinavProfili, val dersIdleri: Set<String> = emptySet()) {
    fun gorunur(ders: Ders): Boolean =
        profil == SinavProfili.SERBEST || ders.id in dersIdleri || profil.dersAdiEslesir(ders.ad)
}

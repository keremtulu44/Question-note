package com.keremtulu.questionnote.domain.model

import java.text.Normalizer
import java.util.Locale
import java.util.UUID

enum class SoruSecenekTuru {
    KAYNAK,
    YANLIS_NEDENI,
}

data class SoruSecenegi(
    val id: String,
    val tur: SoruSecenekTuru,
    val ad: String,
    val varsayilan: Boolean,
    val sira: Int,
)

sealed interface SoruSecenegiEklemeSonucu {
    data class Basarili(val secenek: SoruSecenegi) : SoruSecenegiEklemeSonucu
    data class ZatenVar(val secenek: SoruSecenegi) : SoruSecenegiEklemeSonucu
    data object BosAd : SoruSecenegiEklemeSonucu
    data object CokUzunAd : SoruSecenegiEklemeSonucu
}

/** Outcome of deleting a source / wrong-reason entry from the catalog. */
sealed interface SoruSecenegiSilmeSonucu {
    data object Basarili : SoruSecenegiSilmeSonucu
    data object Bulunamadi : SoruSecenegiSilmeSonucu
    /** Built-in entries ship with the app and must stay selectable. */
    data object VarsayilanSilinemez : SoruSecenegiSilmeSonucu
    data class Kullanimda(val soruSayisi: Int) : SoruSecenegiSilmeSonucu
}

const val SORU_SECENEGI_ADI_UST_SINIR = 120

private val SECENEK_BOSLUKLARI = Regex("\\s+")
private val TURKCE_LOCALE = Locale.forLanguageTag("tr-TR")

/** Stable, locale-independent key used to prevent duplicate catalog entries. */
fun canonicalSoruSecenegiAdi(ad: String): String =
    Normalizer.normalize(ad.trim().replace(SECENEK_BOSLUKLARI, " "), Normalizer.Form.NFC)

fun normalizeSoruSecenegiAdi(ad: String): String =
    canonicalSoruSecenegiAdi(ad).lowercase(TURKCE_LOCALE)

/** Deterministic IDs let old database rows and v2 backups rebuild the same catalog entries. */
fun stableSoruSecenegiId(tur: SoruSecenekTuru, normalizedName: String): String =
    UUID.nameUUIDFromBytes("yanlis-defteri:${tur.name}:$normalizedName".toByteArray(Charsets.UTF_8)).toString()

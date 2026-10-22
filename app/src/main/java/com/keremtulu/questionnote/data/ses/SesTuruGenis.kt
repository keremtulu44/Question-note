package com.keremtulu.questionnote.data.ses

import com.keremtulu.questionnote.R

/**
 * Extended sound types for ultra-premium audio experience.
 * Each sound is designed for a specific UI moment with distinct character.
 */
enum class SesTuruGenis(
    val kaynakId: Int,
    val kategori: SesKategori,
    val oncelik: Int = 1, // 1=high, 2=normal, 3=low (for ducking)
    val looping: Boolean = false,
) {
    // Core feedback sounds (existing)
    TEKRAR(R.raw.ses_tekrar, SesKategori.GERI_BILDIRIM, 1),
    HEDEF(R.raw.ses_hedef, SesKategori.BASARI, 1),
    KUTLAMA(R.raw.ses_rozet, SesKategori.KUTLAMA, 1),

    // Navigation & Interaction
    TIKLAMA(R.raw.ses_tiklama, SesKategori.ETKILESIM, 3),
    GERI(R.raw.ses_geri, SesKategori.ETKILESIM, 3),
    SAYFA_GECIS(R.raw.ses_sayfa_gecis, SesKategori.ETKILESIM, 2),
    KART_ACILIS(R.raw.ses_kart_acilis, SesKategori.ETKILESIM, 2),
    KART_KAPANIS(R.raw.ses_kart_kapanis, SesKategori.ETKILESIM, 2),

    // Success & Achievement
    BASARI_KUCUK(R.raw.ses_basari_kucuk, SesKategori.BASARI, 2),
    BASARI_ORTA(R.raw.ses_basari_orta, SesKategori.BASARI, 1),
    BASARI_BUYUK(R.raw.ses_basari_buyuk, SesKategori.BASARI, 1),
    ROZET_KAZANDI(R.raw.ses_rozet_kazandi, SesKategori.KUTLAMA, 1),
    RUTBE_ATLADI(R.raw.ses_rutbe_atladi, SesKategori.KUTLAMA, 1),
    SERI_ARTTI(R.raw.ses_seri_artti, SesKategori.BASARI, 2),

    // Focus Session
    ODAK_BASLADI(R.raw.ses_odak_basladi, SesKategori.ETKILESIM, 1),
    ODAK_DURDU(R.raw.ses_odak_durdu, SesKategori.GERI_BILDIRIM, 2),
    ODAK_TAMAMLANDI(R.raw.ses_odak_tamamlandi, SesKategori.KUTLAMA, 1),
    COZUM_ACILDI(R.raw.ses_cozum_acildi, SesKategori.ETKILESIM, 2),
    DEGERLENDIRME_ACILDI(R.raw.ses_degerlendirme_acildi, SesKategori.ETKILESIM, 2),

    // Settings & Toggles
    TOGGLE_AC(R.raw.ses_toggle_ac, SesKategori.ETKILESIM, 3),
    TOGGLE_KAPA(R.raw.ses_toggle_kapa, SesKategori.ETKILESIM, 3),
    SLIDER_DEGISIKLIK(R.raw.ses_slider_degisiklik, SesKategori.ETKILESIM, 3),

    // Error & Warning
    HATA(R.raw.ses_hata, SesKategori.HATA, 1),
    UYARI(R.raw.ses_uyari, SesKategori.HATA, 2),
    BASARISIZ(R.raw.ses_basarisiz, SesKategori.HATA, 1),

    // Ambient / Atmospheric (looped, low volume)
    AMBIYANS_ODAK(R.raw.ambiyans_odak, SesKategori.AMBIYANS, 3, true),
    AMBIYANS_CALISMA(R.raw.ambiyans_calisma, SesKategori.AMBIYANS, 3, true),

    // Swipe & Gesture
    SWIPE(R.raw.ses_swipe, SesKategori.ETKILESIM, 3),
    PULL_REFRESH(R.raw.ses_pull_refresh, SesKategori.ETKILESIM, 2),
    RELEASE_REFRESH(R.raw.ses_release_refresh, SesKategori.BASARI, 2),

    // Card & List interactions
    KART_SECILDI(R.raw.ses_kart_secildi, SesKategori.ETKILESIM, 2),
    LISTE_YUKLENDI(R.raw.ses_liste_yuklendi, SesKategori.GERI_BILDIRIM, 2),
    YUKLEME_TAMAM(R.raw.ses_yukleme_tamam, SesKategori.BASARI, 2),

    // Special moments
    ILK_SORU(R.raw.ses_ilk_soru, SesKategori.KUTLAMA, 1),
    MILESTONE(R.raw.ses_milestone, SesKategori.KUTLAMA, 1),
    GUN_SONU(R.raw.ses_gun_sonu, SesKategori.KUTLAMA, 1),

    // Legacy mapping for backward compatibility
    ;

    companion object {
        /** Map legacy SesTuru to new extended types for gradual migration */
        fun legacyDen(eski: SesTuru): SesTuruGenis = when (eski) {
            SesTuru.TEKRAR -> TEKRAR
            SesTuru.HEDEF -> HEDEF
            SesTuru.KUTLAMA -> KUTLAMA
        }
    }
}

enum class SesKategori {
    ETKILESIM,      // Button clicks, navigation, gestures
    GERI_BILDIRIM,  // Confirmations, saves, loads
    BASARI,         // Achievements, completions, goals
    KUTLAMA,        // Big celebrations, badges, ranks
    HATA,           // Errors, warnings, failures
    AMBIYANS,       // Background atmospheres (looped)
}
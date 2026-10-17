package com.keremtulu.questionnote.ui.karne

import androidx.annotation.StringRes
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.karne.Rozet

@StringRes
fun rozetAdi(rozet: Rozet): Int = when (rozet) {
    Rozet.ILK_KARNE -> R.string.badge_ilk_karne
    Rozet.KUSURSUZ_TUR -> R.string.badge_kusursuz_tur
    Rozet.ON_BAGIMSIZ -> R.string.badge_on_bagimsiz
    Rozet.YIRMI_SORU -> R.string.badge_yirmi_soru
    Rozet.COZUMSUZ -> R.string.badge_cozumsuz
    Rozet.ISTIKRAR -> R.string.badge_istikrar
    Rozet.SERI_3 -> R.string.badge_seri_3
    Rozet.SERI_7 -> R.string.badge_seri_7
    Rozet.SERI_30 -> R.string.badge_seri_30
    Rozet.YUZ_BAGIMSIZ -> R.string.badge_yuz_bagimsiz
    Rozet.BES_YUZ -> R.string.badge_bes_yuz
    Rozet.ILK_SORU -> R.string.badge_ilk_soru
    Rozet.ARSIVCI -> R.string.badge_arsivci
    Rozet.KUTUPHANE -> R.string.badge_kutuphane
    Rozet.KOLEKSIYONCU -> R.string.badge_koleksiyoncu
    Rozet.ELLI_TEKRAR -> R.string.badge_elli_tekrar
    Rozet.IKI_YUZ_TEKRAR -> R.string.badge_iki_yuz_tekrar
    Rozet.BIN_TEKRAR -> R.string.badge_bin_tekrar
    Rozet.BIN_BAGIMSIZ -> R.string.badge_bin_bagimsiz
    Rozet.SERI_14 -> R.string.badge_seri_14
    Rozet.SERI_60 -> R.string.badge_seri_60
    Rozet.SERI_100 -> R.string.badge_seri_100
    Rozet.HEDEF_1 -> R.string.badge_hedef_1
    Rozet.HEDEF_7 -> R.string.badge_hedef_7
    Rozet.HEDEF_30 -> R.string.badge_hedef_30
    Rozet.UC_DERS -> R.string.badge_uc_ders
    Rozet.BES_DERS -> R.string.badge_bes_ders
    Rozet.DONUSUM -> R.string.badge_donusum
    Rozet.ZINCIR_10 -> R.string.badge_zincir_10
    Rozet.KUSURSUZ_20 -> R.string.badge_kusursuz_20
}

@StringRes
fun rozetAciklamasi(rozet: Rozet): Int = when (rozet) {
    Rozet.ILK_KARNE -> R.string.badge_desc_ilk_karne
    Rozet.KUSURSUZ_TUR -> R.string.badge_desc_kusursuz_tur
    Rozet.ON_BAGIMSIZ -> R.string.badge_desc_on_bagimsiz
    Rozet.YIRMI_SORU -> R.string.badge_desc_yirmi_soru
    Rozet.COZUMSUZ -> R.string.badge_desc_cozumsuz
    Rozet.ISTIKRAR -> R.string.badge_desc_istikrar
    Rozet.SERI_3 -> R.string.badge_desc_seri_3
    Rozet.SERI_7 -> R.string.badge_desc_seri_7
    Rozet.SERI_30 -> R.string.badge_desc_seri_30
    Rozet.YUZ_BAGIMSIZ -> R.string.badge_desc_yuz_bagimsiz
    Rozet.BES_YUZ -> R.string.badge_desc_bes_yuz
    Rozet.ILK_SORU -> R.string.badge_desc_ilk_soru
    Rozet.ARSIVCI -> R.string.badge_desc_arsivci
    Rozet.KUTUPHANE -> R.string.badge_desc_kutuphane
    Rozet.KOLEKSIYONCU -> R.string.badge_desc_koleksiyoncu
    Rozet.ELLI_TEKRAR -> R.string.badge_desc_elli_tekrar
    Rozet.IKI_YUZ_TEKRAR -> R.string.badge_desc_iki_yuz_tekrar
    Rozet.BIN_TEKRAR -> R.string.badge_desc_bin_tekrar
    Rozet.BIN_BAGIMSIZ -> R.string.badge_desc_bin_bagimsiz
    Rozet.SERI_14 -> R.string.badge_desc_seri_14
    Rozet.SERI_60 -> R.string.badge_desc_seri_60
    Rozet.SERI_100 -> R.string.badge_desc_seri_100
    Rozet.HEDEF_1 -> R.string.badge_desc_hedef_1
    Rozet.HEDEF_7 -> R.string.badge_desc_hedef_7
    Rozet.HEDEF_30 -> R.string.badge_desc_hedef_30
    Rozet.UC_DERS -> R.string.badge_desc_uc_ders
    Rozet.BES_DERS -> R.string.badge_desc_bes_ders
    Rozet.DONUSUM -> R.string.badge_desc_donusum
    Rozet.ZINCIR_10 -> R.string.badge_desc_zincir_10
    Rozet.KUSURSUZ_20 -> R.string.badge_desc_kusursuz_20
}

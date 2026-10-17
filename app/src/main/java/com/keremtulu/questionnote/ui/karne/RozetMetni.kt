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
}

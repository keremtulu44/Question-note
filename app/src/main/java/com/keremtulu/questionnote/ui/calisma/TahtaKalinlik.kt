package com.keremtulu.questionnote.ui.calisma

import kotlin.math.roundToInt

/**
 * Tahta kalem kalınlığı sözleşmesi (dp).
 *
 * Arayüz kaydırıcısı, değer kırpma ve yuvarlama tek yerden geçer. Compose/Android bağımlılığı
 * yoktur; böylece saf JVM testiyle sınırlar doğrulanabilir (0 dp = görünmez çizgi hatası
 * burada engellenir).
 */
internal const val TAHTA_MIN_KALINLIK_DP = 1f
internal const val TAHTA_MAKS_KALINLIK_DP = 10f
internal const val VARSAYILAN_KALINLIK_DP = 4f

internal fun tahtaKalinligiSinirla(deger: Float): Float {
    // NaN/Infinity kaydırıcıdan veya bir hesap hatasından gelebilir; roundToInt() NaN'da
    // istisna fırlatır, bu yüzden önce sonlu değilse varsayılana dönülür.
    if (!deger.isFinite()) return VARSAYILAN_KALINLIK_DP
    return deger.coerceIn(TAHTA_MIN_KALINLIK_DP, TAHTA_MAKS_KALINLIK_DP).roundToInt().toFloat()
}

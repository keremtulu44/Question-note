package com.keremtulu.questionnote.ui.tema

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/** Motion is decoration, never a prerequisite for accessing content or completing an action. */
data class HareketPolitikasi(val azaltildi: Boolean) {
    val hareketAcik: Boolean get() = !azaltildi
    fun sure(tur: HareketTuru): Int = if (azaltildi) 0 else tur.milisaniye

    companion object {
        val Normal = HareketPolitikasi(azaltildi = false)
        val Azaltilmis = HareketPolitikasi(azaltildi = true)
        fun olcekten(olcek: Float): HareketPolitikasi =
            HareketPolitikasi(azaltildi = !olcek.isFinite() || olcek <= 0f)
    }
}

enum class HareketTuru(val milisaniye: Int) {
    ETKILESIM(160), GIRIS(240), GRAFIK(400), DEGER(500), KUTLAMA(1_200),
}

val LocalHareketPolitikasi = staticCompositionLocalOf { HareketPolitikasi.Azaltilmis }

/** Observe changes while the screen is open; no stale remembered accessibility setting. */
@Composable
internal fun sistemHareketPolitikasi(): HareketPolitikasi {
    val resolver = LocalContext.current.applicationContext.contentResolver
    fun oku() = runCatching {
        HareketPolitikasi.olcekten(Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f))
    }.getOrDefault(HareketPolitikasi.Azaltilmis)
    var politika by remember(resolver) { mutableStateOf(oku()) }
    DisposableEffect(resolver) {
        val gozlemci = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { politika = oku() }
        }
        val kayitli = runCatching {
            resolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, gozlemci)
        }.isSuccess
        if (!kayitli) politika = HareketPolitikasi.Azaltilmis
        else politika = oku()
        onDispose { if (kayitli) resolver.unregisterContentObserver(gozlemci) }
    }
    return politika
}

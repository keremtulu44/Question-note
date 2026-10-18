package com.keremtulu.questionnote.ui.bilesenler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.keremtulu.questionnote.domain.calisma.YerelTakvimSaati
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import kotlinx.coroutines.delay

/** Midnight + resume + wall-clock/zone broadcasts. No per-second analytics polling or persisted day cache. */
@Composable
fun hatirlaYerelGun(saat: Clock = YerelTakvimSaati): LocalDate {
    val context = LocalContext.current
    val activity = remember(context) { context.activity() }
    var yenileme by remember { mutableIntStateOf(0) }
    DisposableEffect(context, activity) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) { yenileme++ }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED); addAction(Intent.ACTION_TIME_CHANGED); addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) yenileme++ }
        activity?.lifecycle?.addObserver(observer)
        onDispose {
            context.unregisterReceiver(receiver)
            activity?.lifecycle?.removeObserver(observer)
        }
    }
    val gun by produceState(LocalDate.now(saat), saat, yenileme) {
        while (true) {
            val an = saat.instant().atZone(saat.zone)
            value = an.toLocalDate()
            val gece = an.toLocalDate().plusDays(1).atStartOfDay(an.zone)
            delay(Duration.between(an, gece).toMillis().coerceAtLeast(1_000L))
        }
    }
    return gun
}
private tailrec fun Context.activity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}

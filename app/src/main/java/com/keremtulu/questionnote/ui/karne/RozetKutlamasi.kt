package com.keremtulu.questionnote.ui.karne

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.ui.tema.LocalHareketPolitikasi
import com.keremtulu.questionnote.ui.tema.HareketTuru
import com.keremtulu.questionnote.data.prefs.RozetDeposu
import com.keremtulu.questionnote.domain.karne.Rozet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RozetKutlamaHost(depo: RozetDeposu, goster: Boolean) {
    val bekleyen by depo.bekleyenIzle.collectAsState()
    val rozet = bekleyen.firstOrNull()
    if (goster && rozet != null) key(rozet) {
        val scope = rememberCoroutineScope()
        var kapanis by remember { mutableStateOf(false) }
        var hata by remember { mutableStateOf(false) }
        RozetKutlamasi(rozet, bekleyen.size, kapanis, hata) {
            if (!kapanis) {
                kapanis = true
                scope.launch {
                    try {
                        withContext(Dispatchers.IO) { depo.kutlamayiTamamla(rozet) }
                    } catch (iptal: CancellationException) {
                        throw iptal
                    } catch (_: Exception) {
                        hata = true
                    } finally {
                        kapanis = false
                    }
                }
            }
        }
    }
}

/** Fullscreen, dismissible, no time limit, finite animation and respects disabled system motion. */
@Composable
private fun RozetKutlamasi(rozet: Rozet, kalan: Int, kapanis: Boolean, hata: Boolean, onKapat: () -> Unit) {
    val politika = LocalHareketPolitikasi.current
    val hareket = politika.hareketAcik
    val acilis = remember { Animatable(if (hareket) 0f else 1f) }
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(rozet) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    LaunchedEffect(rozet, hareket) {
        if (hareket) acilis.animateTo(1f, tween(politika.sure(HareketTuru.KUTLAMA)))
        else acilis.snapTo(1f)
    }
    val altin = Color(0xFFF5D38E)
    Dialog(
        onDismissRequest = onKapat,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false, dismissOnClickOutside = false),
    ) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(listOf(Color(0xFF304257), Color(0xFF101D2C))),
            ),
        ) {
            if (hareket) Canvas(Modifier.fillMaxSize()) {
                val t = acilis.value
                repeat(42) { i ->
                    val x = ((i * 47 % 101) / 100f) * size.width
                    val y = ((i * 19 % 59) / 100f + t * 0.42f) * size.height
                    drawCircle(
                        color = (if (i % 2 == 0) altin else Color(0xFF91DACF)).copy(alpha = (1f - t * 0.8f).coerceIn(0f, 1f)),
                        radius = (2 + i % 3).dp.toPx(), center = Offset(x, y),
                    )
                }
            }
            Column(
                Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
            ) {
                Text(stringResource(if (rozet.gizli) R.string.badge_celebrate_secret else R.string.badge_celebrate_kicker),
                    color = altin, letterSpacing = 3.sp, style = MaterialTheme.typography.labelLarge)
                RozetGorseli(rozet, true, Modifier.size(176.dp).graphicsLayer {
                    scaleX = 0.85f + acilis.value * 0.15f
                    scaleY = scaleX
                })
                Text(stringResource(R.string.badge_celebrate_title), color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.titleMedium)
                Text(stringResource(rozetAdi(rozet)), color = Color.White, textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(stringResource(rozetAciklamasi(rozet)), color = Color.White.copy(alpha = 0.86f),
                    textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.badge_celebrate_xp), color = altin, style = MaterialTheme.typography.titleSmall)
                if (kalan > 1) Text(stringResource(R.string.karne_badge_more, kalan - 1), color = Color.White.copy(alpha = 0.65f))
                if (hata) Text(stringResource(R.string.badge_celebrate_error), color = altin)
                Button(onClick = onKapat, enabled = !kapanis, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = altin, contentColor = Color(0xFF152536))) {
                    Text(stringResource(R.string.badge_celebrate_continue), modifier = Modifier.padding(vertical = 6.dp))
                }
            }
        }
    }
}

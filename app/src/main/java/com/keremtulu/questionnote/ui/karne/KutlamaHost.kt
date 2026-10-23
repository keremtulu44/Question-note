package com.keremtulu.questionnote.ui.karne

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.prefs.RozetDeposu
import com.keremtulu.questionnote.data.prefs.RutbeDeposu
import com.keremtulu.questionnote.data.prefs.RutbeKutlamasi
import com.keremtulu.questionnote.data.ses.HapticTipi
import com.keremtulu.questionnote.data.ses.SesTuruGenis
import com.keremtulu.questionnote.data.ses.SesYoneticisi
import com.keremtulu.questionnote.ui.tema.HareketTuru
import com.keremtulu.questionnote.ui.tema.LocalHareketPolitikasi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.keremtulu.questionnote.ui.premium.PremiumButton

/** One owner chooses ONE fullscreen dialog. Badges have priority; a pending rank stays durable. */
@Composable
fun KutlamaHost(rozetler: RozetDeposu, rutbeler: RutbeDeposu, goster: Boolean) {
    val rozet by rozetler.bekleyenIzle.collectAsState()
    val rutbe by rutbeler.bekleyenIzle.collectAsState()
    if (!goster) return
    if (rozet.isNotEmpty()) RozetKutlamaHost(rozetler, true)
    else rutbe?.let { r -> key(r.son) {
        val scope = rememberCoroutineScope()
        var mesgul by remember { mutableStateOf(false) }
        var hata by remember { mutableStateOf(false) }
        RutbeKutlamasiIcerigi(r, mesgul, hata) {
            if (!mesgul) {
                mesgul = true
                scope.launch {
                    try {
                        withContext(Dispatchers.IO) { rutbeler.kutlamayiTamamla(r) }
                    } catch (iptal: CancellationException) {
                        throw iptal
                    } catch (_: Exception) {
                        hata = true
                    } finally {
                        mesgul = false
                    }
                }
            }
        }
    } }
}

@Composable
fun RutbeKutlamasiIcerigi(r: RutbeKutlamasi, mesgul: Boolean = false, hata: Boolean = false, onKapat: () -> Unit) {
    val p = LocalHareketPolitikasi.current
    val context = LocalContext.current
    val giris = remember(r.son) { Animatable(if (p.azaltildi) 1f else 0f) }
    LaunchedEffect(r.son, p) {
        SesYoneticisi.uygulama(context).calPremium(SesTuruGenis.RUTBE_ATLADI, HapticTipi.RUTBE)
        if (p.azaltildi) giris.snapTo(1f) else giris.animateTo(1f, tween(p.sure(HareketTuru.KUTLAMA)))
    }
    Dialog(onDismissRequest = { if (!mesgul) onKapat() }, properties = DialogProperties(
        usePlatformDefaultWidth = false, decorFitsSystemWindows = false, dismissOnClickOutside = false)) {
        Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF153544), Color(0xFF0C202C))))
            .safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp).testTag("rank-celebration"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)) {
            Text(stringResource(R.string.rank_celebration_kicker), color = Color(0xFFEFC18A), style = MaterialTheme.typography.labelLarge)
            Image(painterResource(r.son.gorsel), contentDescription = null, modifier = Modifier.size(160.dp).graphicsLayer {
                val v = if (p.azaltildi) 1f else giris.value
                scaleX = 0.9f + v * 0.1f; scaleY = scaleX; alpha = v
            })
            Text(r.son.ad, color = Color.White, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
            Text(stringResource(R.string.rank_celebration_summary, r.gecilenEsik, r.onceki.ad, r.son.ad),
                color = Color.White, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.rank_celebration_note), color = Color(0xFFE8DCCB), textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium)
            if (hata) Text(stringResource(R.string.rank_celebration_error), color = Color(0xFFFFDAD6))
            PremiumButton(onClick = onKapat, enabled = !mesgul, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFC18A), contentColor = Color(0xFF0C202C))) {
                Text(stringResource(R.string.rank_celebration_continue))
            }
        }
    }
}

package com.keremtulu.questionnote.ui.seviye

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.keremtulu.questionnote.data.prefs.RozetDeposu
import com.keremtulu.questionnote.data.seviye.SeviyeHizmeti
import com.keremtulu.questionnote.data.seviye.SeviyeOzeti
import com.keremtulu.questionnote.domain.karne.Rozet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import com.keremtulu.questionnote.ui.panel.ekranPencereInsetsi

/** Explicit presentation states. Preview fixtures live in src/debug, never in the release data path. */
sealed interface SeviyeDurumuUi {
    data object Yukleniyor : SeviyeDurumuUi
    data class YetersizVeri(val ozet: SeviyeOzeti) : SeviyeDurumuUi
    data class Hazir(val ozet: SeviyeOzeti) : SeviyeDurumuUi
    data object Hata : SeviyeDurumuUi

    companion object {
        fun ozetten(ozet: SeviyeOzeti): SeviyeDurumuUi =
            if (ozet.tekrarSayisi == 0) YetersizVeri(ozet) else Hazir(ozet)
    }
}

@Composable
fun SeviyeEkrani(
    seviyeHizmeti: SeviyeHizmeti,
    rozetDeposu: RozetDeposu,
    onBugun: () -> Unit,
    onSoruEkle: () -> Unit,
    onCalis: (com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi, Int) -> Unit,
) {
    var yenileme by remember { mutableIntStateOf(0) }
    val bugun = com.keremtulu.questionnote.ui.bilesenler.hatirlaYerelGun()
    val context = LocalContext.current
    val activity = remember(context) { context.seviyeActivity() }
    // Refresh date-dependent forecasts when returning after a night/background pause.
    DisposableEffect(activity) {
        var ilk = true
        val gozlemci = LifecycleEventObserver { _, olay ->
            if (olay == Lifecycle.Event.ON_RESUME) {
                if (!ilk) yenileme++
                ilk = false
            }
        }
        activity?.lifecycle?.addObserver(gozlemci)
        onDispose { activity?.lifecycle?.removeObserver(gozlemci) }
    }
    val durum by produceState<SeviyeDurumuUi>(SeviyeDurumuUi.Yukleniyor, seviyeHizmeti, yenileme, bugun) {
        value = SeviyeDurumuUi.Yukleniyor
        seviyeHizmeti.ozetIzle().catch { hata ->
            if (hata is CancellationException) throw hata
            value = SeviyeDurumuUi.Hata
        }.collect { value = SeviyeDurumuUi.ozetten(it) }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = ekranPencereInsetsi(),
    ) { insets ->
        SeviyeSayfasi(
            durum = durum, insets = insets,
            kazanilmaZamani = rozetDeposu::kazanilmaZamani,
            onYenile = { yenileme++ }, onBugun = onBugun, onSoruEkle = onSoruEkle, onCalis = onCalis,
        )
    }
}

/** Stateless entry point shared by production, Studio previews and UI regression tests. */
@Composable
fun SeviyeSayfasi(
    durum: SeviyeDurumuUi,
    insets: PaddingValues = PaddingValues(),
    kazanilmaZamani: (Rozet) -> Long? = { null },
    onYenile: () -> Unit = {},
    onBugun: () -> Unit = {},
    onSoruEkle: () -> Unit = {},
    onCalis: (com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi, Int) -> Unit = { _, _ -> },
) {
    // A date/resume refresh may briefly replace content with Loading. Keep only UI navigation/scroll state,
    // never a second copy of the summary, so returning to the app does not silently send a detail back home.
    val gorunumDurumu = rememberSaveableStateHolder()
    val ozet = when (durum) {
        is SeviyeDurumuUi.YetersizVeri -> durum.ozet
        is SeviyeDurumuUi.Hazir -> durum.ozet
        else -> null
    }
    if (ozet != null) gorunumDurumu.SaveableStateProvider("progress-content") {
        IlerlemeIcerigi(ozet, durum is SeviyeDurumuUi.YetersizVeri, insets, kazanilmaZamani, onBugun, onSoruEkle, onCalis)
    } else IlerlemeDurumPaneli(hata = durum is SeviyeDurumuUi.Hata, insets = insets, onYenile = onYenile)
}

private tailrec fun Context.seviyeActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.seviyeActivity()
    else -> null
}

package com.keremtulu.questionnote.ui.calisma

import android.content.Context
import android.content.ContextWrapper
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.keremtulu.questionnote.data.prefs.OdakOturumuDeposu
import com.keremtulu.questionnote.domain.calisma.AktifSureOlcer
import com.keremtulu.questionnote.domain.calisma.MonotonikSaat
import com.keremtulu.questionnote.domain.calisma.OlcumDevamNoktasi
import com.keremtulu.questionnote.domain.calisma.OlcumNedeni
import com.keremtulu.questionnote.domain.calisma.TekrarDenemesi
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Compose adapter, not a timer: elapsed values are always read from the pure monotonic accumulator. */
internal class OdakOlcumDurumu(
    private val olcer: AktifSureOlcer,
    private val depo: OdakOturumuDeposu,
    private val oturumId: String,
    private val scope: CoroutineScope,
) {
    var nokta by mutableStateOf(olcer.devamNoktasi())
        private set
    var kayitHatasi by mutableStateOf(false)
        private set
    var yenidenOnPlanda by mutableStateOf(0)
        private set

    fun aktiflik(aktif: Boolean) { olcer.aktifligiAyarla(aktif); kaydet() }
    fun cozumuAc() { olcer.cozumuAc(); kaydet() }
    fun dondur(secim: TekrarSonucu? = nokta.secim) { olcer.dondur(secim); kaydet() }
    fun calismayaDon() { olcer.calismayaDon(); kaydet() }
    fun tamEkran() { olcer.tamEkranaGir(); kaydet() }
    fun kismi(neden: OlcumNedeni) { olcer.kismiIsaretle(neden); kaydet() }
    fun onPlanda() { yenidenOnPlanda++ }
    fun gosterimiYenile() { nokta = nokta.copy(olcum = olcer.olcum()) }
    fun deneme(sonuc: TekrarSonucu) = TekrarDenemesi(nokta.denemeId, nokta.soruId, sonuc, nokta.cozumAcildi, nokta.olcum, oturumId)

    fun kaydet() {
        nokta = olcer.devamNoktasi()
        val surum = depo.olcumuHazirla(oturumId, nokta)
        // The short disk commit may finish during disposal; revision checks reject obsolete/cleared owners.
        scope.launch(NonCancellable) {
            val basarili = runCatching { depo.kalicilastir(surum) }.isSuccess
            scope.launch { kayitHatasi = !basarili }
        }
    }
}

@Composable
internal fun rememberOdakOlcum(
    oturumId: String, soruId: String, cozumOnceAcildi: Boolean, depo: OdakOturumuDeposu,
    saat: MonotonikSaat = MonotonikSaat { SystemClock.elapsedRealtime() },
): OdakOlcumDurumu {
    val scope = rememberCoroutineScope()
    return remember(oturumId, soruId, depo) {
        val oturum = depo.oku()
        val once = oturum?.olcumler?.get(soruId)
        val olcer = AktifSureOlcer(saat, once ?: OlcumDevamNoktasi(soruId, UUID.randomUUID().toString()))
        if (oturum?.olcumDestekli == false || cozumOnceAcildi && once?.cozumAcildi != true) olcer.kismiIsaretle(OlcumNedeni.ESKI_OTURUM)
        if (cozumOnceAcildi) olcer.cozumuAc()
        OdakOlcumDurumu(olcer, depo, oturumId, scope)
    }
}

@Composable
internal fun OdakOlcumIzleyicisi(durum: OdakOlcumDurumu, aktifIzin: Boolean) {
    val context = LocalContext.current
    val activity = remember(context) { context.activity() }
    val izin by rememberUpdatedState(aktifIzin)
    var onPlanda by remember { mutableStateOf(activity?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true) }
    DisposableEffect(activity, durum) {
        val observer = LifecycleEventObserver { _, event ->
            onPlanda = activity?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true
            // Stop at the lifecycle event itself, not at a later recomposition/tick.
            durum.aktiflik(onPlanda && izin)
            if (event == Lifecycle.Event.ON_RESUME) durum.onPlanda()
        }
        activity?.lifecycle?.addObserver(observer)
        onDispose {
            durum.aktiflik(false)
            activity?.lifecycle?.removeObserver(observer)
        }
    }
    LaunchedEffect(aktifIzin, onPlanda, durum) { durum.aktiflik(aktifIzin && onPlanda) }
    LaunchedEffect(durum) {
        var tik = 0
        while (true) {
            delay(1_000)
            durum.gosterimiYenile()
            if (++tik % 5 == 0) durum.kaydet()
        }
    }
}

private tailrec fun Context.activity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}

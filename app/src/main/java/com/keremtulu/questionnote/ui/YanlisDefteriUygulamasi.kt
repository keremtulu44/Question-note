package com.keremtulu.questionnote.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.GorselTutarliligiHizmeti
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.export.YerelDisaAktarmaHizmeti
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.KonuRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private sealed interface UygulamaEkrani {
    data object Dersler : UygulamaEkrani
    data class DersAlani(val ders: Ders, val arsivli: Boolean) : UygulamaEkrani
    data class Konular(val ders: Ders, val arsivli: Boolean) : UygulamaEkrani
    data class SoruAyrintisi(val ders: Ders, val arsivli: Boolean, val soruId: String) : UygulamaEkrani
    data class SoruFormu(val ders: Ders, val soruId: String?, val ayrintidanDon: Boolean) : UygulamaEkrani
    data object GorselBakimi : UygulamaEkrani
}

@Composable
fun YanlisDefteriUygulamasi(
    dersRepository: DersRepository,
    konuRepository: KonuRepository,
    soruRepository: SoruRepository,
    gorselDeposu: LocalSoruGorselDeposu,
    gorselTutarliligi: GorselTutarliligiHizmeti,
    disaAktarmaHizmeti: YerelDisaAktarmaHizmeti,
) {
    var ekran by remember { mutableStateOf<UygulamaEkrani>(UygulamaEkrani.Dersler) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    disaAktarmaHizmeti.disariAktar(uri)
                    snackbarHostState.showSnackbar(context.getString(R.string.export_success))
                } catch (cancel: CancellationException) {
                    throw cancel
                } catch (_: Exception) {
                    snackbarHostState.showSnackbar(context.getString(R.string.export_error))
                }
            }
        }
    }

    LaunchedEffect(gorselDeposu) {
        runCatching { gorselDeposu.eskiGecicileriTemizle() }
    }

    BackHandler(enabled = ekran != UygulamaEkrani.Dersler) {
        ekran = when (val current = ekran) {
            UygulamaEkrani.Dersler -> UygulamaEkrani.Dersler
            is UygulamaEkrani.DersAlani -> UygulamaEkrani.Dersler
            is UygulamaEkrani.Konular -> UygulamaEkrani.DersAlani(current.ders, current.arsivli)
            is UygulamaEkrani.SoruAyrintisi -> UygulamaEkrani.DersAlani(current.ders, current.arsivli)
            is UygulamaEkrani.SoruFormu -> if (current.ayrintidanDon && current.soruId != null) {
                UygulamaEkrani.SoruAyrintisi(current.ders, arsivli = false, soruId = current.soruId)
            } else {
                UygulamaEkrani.DersAlani(current.ders, arsivli = false)
            }
            UygulamaEkrani.GorselBakimi -> UygulamaEkrani.Dersler
        }
    }

    Box(Modifier.fillMaxSize()) {
        when (val current = ekran) {
                UygulamaEkrani.Dersler -> DerslerEkrani(
                    repository = dersRepository,
                    onDersSecildi = { ders, archived -> ekran = UygulamaEkrani.DersAlani(ders, archived) },
                    onDisariAktar = {
                        exportLauncher.launch("yanlis-defteri-disari-aktarim.zip")
                    },
                    onBakim = { ekran = UygulamaEkrani.GorselBakimi },
                )

                is UygulamaEkrani.DersAlani -> DersCalismaEkrani(
                    ders = current.ders,
                    dersArsivli = current.arsivli,
                    konuRepository = konuRepository,
                    soruRepository = soruRepository,
                    onGeri = { ekran = UygulamaEkrani.Dersler },
                    onKonulariYonet = { ekran = UygulamaEkrani.Konular(current.ders, current.arsivli) },
                    onSoruEkle = { ekran = UygulamaEkrani.SoruFormu(current.ders, null, ayrintidanDon = false) },
                    onSoruAc = { id -> ekran = UygulamaEkrani.SoruAyrintisi(current.ders, current.arsivli, id) },
                )

                is UygulamaEkrani.Konular -> KonuYonetimiEkrani(
                    ders = current.ders,
                    repository = konuRepository,
                    onGeri = { ekran = UygulamaEkrani.DersAlani(current.ders, current.arsivli) },
                )

                is UygulamaEkrani.SoruAyrintisi -> SoruDetayEkrani(
                    soruId = current.soruId,
                    ders = current.ders,
                    dersArsivli = current.arsivli,
                    soruRepository = soruRepository,
                    konuRepository = konuRepository,
                    gorselDeposu = gorselDeposu,
                    onGeri = { ekran = UygulamaEkrani.DersAlani(current.ders, current.arsivli) },
                    onDuzenle = { id -> ekran = UygulamaEkrani.SoruFormu(current.ders, id, ayrintidanDon = true) },
                )

                is UygulamaEkrani.SoruFormu -> SoruFormuEkrani(
                    ders = current.ders,
                    soruId = current.soruId,
                    soruRepository = soruRepository,
                    konuRepository = konuRepository,
                    gorselDeposu = gorselDeposu,
                    onGeri = {
                        ekran = if (current.ayrintidanDon && current.soruId != null) {
                            UygulamaEkrani.SoruAyrintisi(current.ders, arsivli = false, soruId = current.soruId)
                        } else {
                            UygulamaEkrani.DersAlani(current.ders, arsivli = false)
                        }
                    },
                )

                UygulamaEkrani.GorselBakimi -> GorselBakimEkrani(
                    service = gorselTutarliligi,
                    onGeri = { ekran = UygulamaEkrani.Dersler },
                )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
        )
    }
}

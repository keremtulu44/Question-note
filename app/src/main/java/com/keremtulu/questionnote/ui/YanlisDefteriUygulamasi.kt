package com.keremtulu.questionnote.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.export.GeriYuklemeOnizlemesi
import com.keremtulu.questionnote.data.export.YerelYedeklemeHizmeti
import com.keremtulu.questionnote.data.images.GorselTutarliligiHizmeti
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
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

private enum class KurtarmaDurumu { YUKLENIYOR, HAZIR, HATA }

@Composable
fun YanlisDefteriUygulamasi(
    dersRepository: DersRepository,
    konuRepository: KonuRepository,
    soruRepository: SoruRepository,
    gorselDeposu: LocalSoruGorselDeposu,
    gorselTutarliligi: GorselTutarliligiHizmeti,
    disaAktarmaHizmeti: YerelYedeklemeHizmeti,
) {
    var ekran by remember { mutableStateOf<UygulamaEkrani>(UygulamaEkrani.Dersler) }
    var kurtarma by remember { mutableStateOf(KurtarmaDurumu.YUKLENIYOR) }
    var yedeklemeIslemde by remember { mutableStateOf(false) }
    var geriYuklemeOnizlemesi by remember { mutableStateOf<GeriYuklemeOnizlemesi?>(null) }
    var sonYedek by remember { mutableStateOf<com.keremtulu.questionnote.data.export.SonYedekBilgisi?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    suspend fun kurtarmayiCalistir() {
        kurtarma = KurtarmaDurumu.YUKLENIYOR
        try {
            disaAktarmaHizmeti.kesintiliGeriYuklemeyiTamamla()
            runCatching { gorselDeposu.eskiGecicileriTemizle() }
            sonYedek = disaAktarmaHizmeti.sonYedekBilgisiGetir()
            kurtarma = KurtarmaDurumu.HAZIR
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            kurtarma = KurtarmaDurumu.HATA
        }
    }

    LaunchedEffect(disaAktarmaHizmeti) {
        kurtarmayiCalistir()
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                yedeklemeIslemde = true
                try {
                    sonYedek = disaAktarmaHizmeti.disariAktar(uri)
                    snackbarHostState.showSnackbar(context.getString(R.string.export_success))
                } catch (cancel: CancellationException) {
                    throw cancel
                } catch (_: Exception) {
                    snackbarHostState.showSnackbar(context.getString(R.string.export_error))
                } finally {
                    yedeklemeIslemde = false
                }
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                yedeklemeIslemde = true
                try {
                    geriYuklemeOnizlemesi = disaAktarmaHizmeti.uriIleOnizle(uri)
                } catch (cancel: CancellationException) {
                    throw cancel
                } catch (_: Exception) {
                    snackbarHostState.showSnackbar(context.getString(R.string.backup_restore_error))
                } finally {
                    yedeklemeIslemde = false
                }
            }
        }
    }

    BackHandler(enabled = kurtarma == KurtarmaDurumu.HAZIR && ekran != UygulamaEkrani.Dersler) {
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
        when (kurtarma) {
            KurtarmaDurumu.YUKLENIYOR -> UygulamaDurumEkrani(
                metin = stringResource(R.string.backup_restore_working),
                yukleniyor = true,
            )
            KurtarmaDurumu.HATA -> UygulamaDurumEkrani(
                metin = stringResource(R.string.backup_restore_recovery_error),
                yukleniyor = false,
                tekrarDene = { scope.launch { kurtarmayiCalistir() } },
            )
            KurtarmaDurumu.HAZIR -> {
                when (val current = ekran) {
                    UygulamaEkrani.Dersler -> DerslerEkrani(
                        repository = dersRepository,
                        onDersSecildi = { ders, archived -> ekran = UygulamaEkrani.DersAlani(ders, archived) },
                        onDisariAktar = { exportLauncher.launch("yanlis-defteri-yedek.zip") },
                        onIceriAktar = {
                            restoreLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                        },
                        sonYedek = sonYedek,
                        yedeklemeIslemde = yedeklemeIslemde,
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
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    geriYuklemeOnizlemesi?.let { preview ->
        AlertDialog(
            onDismissRequest = {
                if (!yedeklemeIslemde) {
                    geriYuklemeOnizlemesi = null
                    scope.launch { disaAktarmaHizmeti.hazirlananYedegiIptalEt(preview.id) }
                }
            },
            title = { Text(stringResource(R.string.backup_restore_title)) },
            text = {
                if (yedeklemeIslemde) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator()
                        Text(stringResource(R.string.backup_restore_working))
                    }
                } else {
                    Text(
                        context.getString(
                            R.string.backup_restore_confirm,
                            preview.yedek.dosyaAdi,
                            preview.yedek.sayilar.soruSayisi,
                            preview.yedek.sayilar.gorselSayisi,
                            preview.mevcut.soruSayisi,
                            preview.mevcut.gorselSayisi,
                        ),
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !yedeklemeIslemde,
                    onClick = {
                        scope.launch {
                            yedeklemeIslemde = true
                            try {
                                val result = disaAktarmaHizmeti.geriYukle(preview)
                                geriYuklemeOnizlemesi = null
                                ekran = UygulamaEkrani.Dersler
                                sonYedek = disaAktarmaHizmeti.sonYedekBilgisiGetir()
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.backup_restore_success, result.guvenlikYedegiAdi),
                                )
                            } catch (cancel: CancellationException) {
                                throw cancel
                            } catch (_: Exception) {
                                geriYuklemeOnizlemesi = null
                                val recoverySucceeded = runCatching {
                                    disaAktarmaHizmeti.kesintiliGeriYuklemeyiTamamla()
                                }.isSuccess
                                if (!recoverySucceeded) kurtarma = KurtarmaDurumu.HATA
                                runCatching { disaAktarmaHizmeti.hazirlananYedegiIptalEt(preview.id) }
                                snackbarHostState.showSnackbar(context.getString(R.string.backup_restore_error))
                            } finally {
                                yedeklemeIslemde = false
                            }
                        }
                    },
                ) { Text(stringResource(if (yedeklemeIslemde) R.string.topic_saving else R.string.backup_restore_action)) }
            },
            dismissButton = {
                TextButton(enabled = !yedeklemeIslemde, onClick = {
                    geriYuklemeOnizlemesi = null
                    scope.launch { disaAktarmaHizmeti.hazirlananYedegiIptalEt(preview.id) }
                }) { Text(stringResource(R.string.topic_cancel)) }
            },
        )
    }
}

@Composable
private fun UygulamaDurumEkrani(
    metin: String,
    yukleniyor: Boolean,
    tekrarDene: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (yukleniyor) CircularProgressIndicator()
        Text(metin, modifier = Modifier.padding(top = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        tekrarDene?.let { retry ->
            TextButton(onClick = retry) { Text(stringResource(R.string.backup_restore_retry)) }
        }
    }
}

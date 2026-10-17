package com.keremtulu.questionnote.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.keremtulu.questionnote.data.export.SonYedekBilgisi
import com.keremtulu.questionnote.data.export.YerelYedeklemeHizmeti
import com.keremtulu.questionnote.data.images.GorselTutarliligiHizmeti
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.ilerleme.IlerlemeHizmeti
import com.keremtulu.questionnote.data.prefs.GunlukHedefDeposu
import com.keremtulu.questionnote.data.prefs.RozetDeposu
import com.keremtulu.questionnote.data.prefs.TercihGunlukHedefDeposu
import com.keremtulu.questionnote.data.seviye.SeviyeHizmeti
import com.keremtulu.questionnote.domain.karne.OturumSonucu
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.KonuRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository
import com.keremtulu.questionnote.ui.ayarlar.AyarlarEkrani
import com.keremtulu.questionnote.ui.karne.KarneEkrani
import com.keremtulu.questionnote.ui.seviye.SeviyeEkrani
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private sealed interface UygulamaEkrani {
    data object Bugun : UygulamaEkrani
    data class DersSecimi(val soruEkleModu: Boolean) : UygulamaEkrani
    data class DersAlani(val ders: Ders, val geri: UygulamaEkrani) : UygulamaEkrani
    data class Konular(val ders: Ders, val geri: UygulamaEkrani) : UygulamaEkrani
    data class SoruAyrintisi(
        val ders: Ders,
        val soruId: String,
        val geri: UygulamaEkrani,
        val tekrarOturumunda: Boolean = false,
    ) : UygulamaEkrani
    data class SoruFormu(val ders: Ders, val soruId: String?, val geri: UygulamaEkrani) : UygulamaEkrani
    data object FiltreliCalisma : UygulamaEkrani
    data class FiltreliOturum(
        val sorular: List<Soru>,
        val dersler: Map<String, Ders>,
        val sira: Int,
        val sonuclar: List<OturumSonucu> = emptyList(),
    ) : UygulamaEkrani
    data class FiltreliCalismaTamamlandi(
        val sorular: List<Soru>,
        val dersler: Map<String, Ders>,
        val sonuclar: List<OturumSonucu>,
    ) : UygulamaEkrani
    data object GorselBakimi : UygulamaEkrani
    data object Katalog : UygulamaEkrani
    data object Seviye : UygulamaEkrani
    data object Ayarlar : UygulamaEkrani
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
    ilerlemeHizmeti: IlerlemeHizmeti,
    seviyeHizmeti: SeviyeHizmeti,
    rozetDeposu: RozetDeposu,
    gunlukHedefDeposu: GunlukHedefDeposu,
    tercihHedefDeposu: TercihGunlukHedefDeposu,
) {
    var ekran by remember { mutableStateOf<UygulamaEkrani>(UygulamaEkrani.Bugun) }
    var filtreliCalismaAyar by remember { mutableStateOf(FiltreliCalismaEkraniAyar()) }
    // Filtreli oturum sürerken başka bir ekrana geçilirse önce onay istenir.
    var oturumCikisHedefi by remember { mutableStateOf<UygulamaEkrani?>(null) }
    var kurtarma by remember { mutableStateOf(KurtarmaDurumu.YUKLENIYOR) }
    var yedeklemeIslemde by remember { mutableStateOf(false) }
    var geriYuklemeOnizlemesi by remember { mutableStateOf<GeriYuklemeOnizlemesi?>(null) }
    var sonYedek by remember { mutableStateOf<SonYedekBilgisi?>(null) }
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

    LaunchedEffect(disaAktarmaHizmeti) { kurtarmayiCalistir() }

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

    fun ekranSec(hedef: UygulamaEkrani) {
        if (ekran is UygulamaEkrani.FiltreliOturum) {
            oturumCikisHedefi = hedef
        } else {
            ekran = hedef
        }
    }

    fun sekmeSec(sekme: AltSekme) {
        val hedef = when (sekme) {
            AltSekme.BUGUN -> UygulamaEkrani.Bugun
            AltSekme.DERSLER -> UygulamaEkrani.DersSecimi(soruEkleModu = false)
            AltSekme.FILTRELI -> UygulamaEkrani.FiltreliCalisma
            AltSekme.SEVIYE -> UygulamaEkrani.Seviye
            AltSekme.AYARLAR -> UygulamaEkrani.Ayarlar
        }
        ekranSec(hedef)
    }

    BackHandler(
        enabled = kurtarma == KurtarmaDurumu.HAZIR && ekran != UygulamaEkrani.Bugun,
    ) {
        ekranSec(ekran.geriHedefi())
    }

    val altMenuGorunur = ekran.altMenuGorunur()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (altMenuGorunur) {
                AltMenu(secili = ekran.aktifSekme(), onSec = ::sekmeSec)
            }
        },
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(bottom = insets.calculateBottomPadding())) {
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
                KurtarmaDurumu.HAZIR -> when (val current = ekran) {
                    UygulamaEkrani.Bugun -> BugunEkrani(
                        soruRepository = soruRepository,
                        dersRepository = dersRepository,
                        ilerlemeHizmeti = ilerlemeHizmeti,
                        gunlukHedefDeposu = gunlukHedefDeposu,
                        onMenu = { ekranSec(UygulamaEkrani.Ayarlar) },
                        onSoruEkle = { ekran = UygulamaEkrani.DersSecimi(soruEkleModu = true) },
                        onSoruAc = { ders, soruId ->
                            ekran = UygulamaEkrani.SoruAyrintisi(ders, soruId, UygulamaEkrani.Bugun)
                        },
                    )
                    is UygulamaEkrani.DersSecimi -> DerslerEkrani(
                        repository = dersRepository,
                        soruEkleModu = current.soruEkleModu,
                        onMenu = { ekranSec(UygulamaEkrani.Ayarlar) },
                        onDersSecildi = { ders ->
                            ekran = if (current.soruEkleModu) {
                                UygulamaEkrani.SoruFormu(ders, null, current)
                            } else {
                                UygulamaEkrani.DersAlani(ders, current)
                            }
                        },
                    )
                    is UygulamaEkrani.DersAlani -> DersCalismaEkrani(
                        ders = current.ders,
                        dersArsivli = false,
                        konuRepository = konuRepository,
                        soruRepository = soruRepository,
                        onGeri = { ekran = current.geri },
                        onKonulariYonet = { ekran = UygulamaEkrani.Konular(current.ders, current) },
                        onSoruEkle = {
                            ekran = UygulamaEkrani.SoruFormu(current.ders, null, current)
                        },
                        onSoruAc = { id ->
                            ekran = UygulamaEkrani.SoruAyrintisi(current.ders, id, current)
                        },
                    )
                    is UygulamaEkrani.Konular -> KonuYonetimiEkrani(
                        ders = current.ders,
                        repository = konuRepository,
                        onGeri = { ekran = current.geri },
                    )
                    is UygulamaEkrani.SoruAyrintisi -> SoruDetayEkrani(
                        soruId = current.soruId,
                        ders = current.ders,
                        dersArsivli = false,
                        soruRepository = soruRepository,
                        konuRepository = konuRepository,
                        gorselDeposu = gorselDeposu,
                        onGeri = { ekran = current.geri },
                        onDuzenle = { id -> ekran = UygulamaEkrani.SoruFormu(current.ders, id, current) },
                        onTekrarTamamlandi = { _, _ ->
                            ekran = current.geri
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.review_saved))
                            }
                        },
                        tekrarOturumunda = current.tekrarOturumunda,
                    )
                    UygulamaEkrani.FiltreliCalisma -> FiltreliCalismaEkrani(
                        dersRepository = dersRepository,
                        konuRepository = konuRepository,
                        soruRepository = soruRepository,
                        ayar = filtreliCalismaAyar,
                        onAyarDegisti = { filtreliCalismaAyar = it },
                        onGeri = { ekran = UygulamaEkrani.Bugun },
                        onSoruAc = { ders, soruId ->
                            ekran = UygulamaEkrani.SoruAyrintisi(
                                ders = ders,
                                soruId = soruId,
                                geri = UygulamaEkrani.FiltreliCalisma,
                                tekrarOturumunda = true,
                            )
                        },
                        onOturumBaslat = { questions, lessons ->
                            ekran = UygulamaEkrani.FiltreliOturum(questions, lessons, sira = 0)
                        },
                    )
                    is UygulamaEkrani.FiltreliOturum -> {
                        val question = current.sorular.getOrNull(current.sira)
                        val lesson = question?.let { current.dersler[it.dersId] }
                        if (question == null || lesson == null) {
                            UygulamaDurumEkrani(
                                metin = stringResource(R.string.filtered_practice_question_unavailable),
                                yukleniyor = false,
                                tekrarDene = { ekran = UygulamaEkrani.FiltreliCalisma },
                            )
                        } else {
                            SoruDetayEkrani(
                                soruId = question.id,
                                ders = lesson,
                                dersArsivli = false,
                                soruRepository = soruRepository,
                                konuRepository = konuRepository,
                                gorselDeposu = gorselDeposu,
                                onGeri = { ekranSec(UygulamaEkrani.FiltreliCalisma) },
                                onDuzenle = { id -> ekran = UygulamaEkrani.SoruFormu(lesson, id, current) },
                                onTekrarTamamlandi = { sonuc, cozumAcildi ->
                                    val nextIndex = current.sira + 1
                                    val guncelSonuclar = current.sonuclar +
                                        OturumSonucu(question.id, sonuc, cozumAcildi)
                                    ekran = if (nextIndex < current.sorular.size) {
                                        current.copy(sira = nextIndex, sonuclar = guncelSonuclar)
                                    } else {
                                        UygulamaEkrani.FiltreliCalismaTamamlandi(
                                            sorular = current.sorular,
                                            dersler = current.dersler,
                                            sonuclar = guncelSonuclar,
                                        )
                                    }
                                    scope.launch {
                                        snackbarHostState.showSnackbar(context.getString(R.string.review_saved))
                                    }
                                },
                                tekrarOturumunda = true,
                                oturumSira = current.sira + 1,
                                oturumToplam = current.sorular.size,
                            )
                        }
                    }
                    is UygulamaEkrani.FiltreliCalismaTamamlandi -> KarneEkrani(
                        sorular = current.sorular,
                        dersler = current.dersler,
                        konuRepository = konuRepository,
                        sonuclar = current.sonuclar,
                        ilerlemeHizmeti = ilerlemeHizmeti,
                        rozetDeposu = rozetDeposu,
                        gunlukHedefDeposu = gunlukHedefDeposu,
                        onKonudanCalis = { konuId ->
                            filtreliCalismaAyar = FiltreliCalismaEkraniAyar(
                                filtre = SoruCalismaFiltresi(konuId = konuId),
                            )
                            ekran = UygulamaEkrani.FiltreliCalisma
                        },
                        onFiltrelereDon = { ekran = UygulamaEkrani.FiltreliCalisma },
                        onBugun = { ekran = UygulamaEkrani.Bugun },
                    )
                    is UygulamaEkrani.SoruFormu -> SoruFormuEkrani(
                        ders = current.ders,
                        soruId = current.soruId,
                        soruRepository = soruRepository,
                        konuRepository = konuRepository,
                        gorselDeposu = gorselDeposu,
                        onGeri = { ekran = current.geri },
                    )
                    UygulamaEkrani.GorselBakimi -> GorselBakimEkrani(
                        service = gorselTutarliligi,
                        onGeri = { ekran = UygulamaEkrani.Ayarlar },
                    )
                    UygulamaEkrani.Katalog -> KatalogYonetimiEkrani(
                        soruRepository = soruRepository,
                        onGeri = { ekran = UygulamaEkrani.Ayarlar },
                    )
                    UygulamaEkrani.Seviye -> SeviyeEkrani(
                        seviyeHizmeti = seviyeHizmeti,
                        rozetDeposu = rozetDeposu,
                    )
                    UygulamaEkrani.Ayarlar -> AyarlarEkrani(
                        gunlukHedefDeposu = tercihHedefDeposu,
                        sonYedek = sonYedek,
                        yedeklemeIslemde = yedeklemeIslemde,
                        onYedekOlustur = {
                            if (!yedeklemeIslemde) exportLauncher.launch("yanlis-defteri-yedek.zip")
                        },
                        onYedekGeriYukle = {
                            if (!yedeklemeIslemde) {
                                restoreLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                            }
                        },
                        onGorselBakimi = { ekranSec(UygulamaEkrani.GorselBakimi) },
                        onKatalog = { ekranSec(UygulamaEkrani.Katalog) },
                    )
                }
            }
        }
    }

    oturumCikisHedefi?.let { hedef ->
        val oturum = ekran as? UygulamaEkrani.FiltreliOturum
        val kalanSoru = oturum?.let { (it.sorular.size - it.sira).coerceAtLeast(0) } ?: 0
        AlertDialog(
            onDismissRequest = { oturumCikisHedefi = null },
            title = { Text(stringResource(R.string.filtered_practice_exit_title)) },
            text = { Text(stringResource(R.string.filtered_practice_exit_message, kalanSoru)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        oturumCikisHedefi = null
                        ekran = hedef
                    },
                ) { Text(stringResource(R.string.filtered_practice_exit_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { oturumCikisHedefi = null }) {
                    Text(stringResource(R.string.filtered_practice_exit_cancel))
                }
            },
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
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
                                ekran = UygulamaEkrani.Bugun
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

/** Sub-screens open from a tab keep that tab highlighted. */
private fun UygulamaEkrani.aktifSekme(): AltSekme = when (this) {
    UygulamaEkrani.Bugun -> AltSekme.BUGUN
    is UygulamaEkrani.DersSecimi, is UygulamaEkrani.DersAlani, is UygulamaEkrani.Konular -> AltSekme.DERSLER
    UygulamaEkrani.FiltreliCalisma,
    is UygulamaEkrani.FiltreliOturum,
    is UygulamaEkrani.FiltreliCalismaTamamlandi,
    -> AltSekme.FILTRELI
    UygulamaEkrani.Seviye -> AltSekme.SEVIYE
    UygulamaEkrani.Ayarlar, UygulamaEkrani.Katalog, UygulamaEkrani.GorselBakimi -> AltSekme.AYARLAR
    is UygulamaEkrani.SoruAyrintisi -> if (tekrarOturumunda) AltSekme.FILTRELI else AltSekme.DERSLER
    is UygulamaEkrani.SoruFormu -> if (geri is UygulamaEkrani.FiltreliOturum) {
        AltSekme.FILTRELI
    } else {
        AltSekme.DERSLER
    }
}

/** Detail/form screens take the whole screen; the bar returns on tab roots. */
private fun UygulamaEkrani.altMenuGorunur(): Boolean = when (this) {
    UygulamaEkrani.Bugun,
    UygulamaEkrani.FiltreliCalisma,
    UygulamaEkrani.Seviye,
    UygulamaEkrani.Ayarlar,
    -> true
    is UygulamaEkrani.DersSecimi -> !soruEkleModu
    else -> false
}

private fun UygulamaEkrani.geriHedefi(): UygulamaEkrani = when (this) {
    UygulamaEkrani.Bugun -> UygulamaEkrani.Bugun
    is UygulamaEkrani.DersSecimi -> UygulamaEkrani.Bugun
    is UygulamaEkrani.DersAlani -> geri
    is UygulamaEkrani.Konular -> geri
    is UygulamaEkrani.SoruAyrintisi -> geri
    is UygulamaEkrani.SoruFormu -> geri
    UygulamaEkrani.FiltreliCalisma -> UygulamaEkrani.Bugun
    is UygulamaEkrani.FiltreliOturum -> UygulamaEkrani.FiltreliCalisma
    is UygulamaEkrani.FiltreliCalismaTamamlandi -> UygulamaEkrani.FiltreliCalisma
    UygulamaEkrani.GorselBakimi -> UygulamaEkrani.Ayarlar
    UygulamaEkrani.Katalog -> UygulamaEkrani.Ayarlar
    UygulamaEkrani.Seviye -> UygulamaEkrani.Bugun
    UygulamaEkrani.Ayarlar -> UygulamaEkrani.Bugun
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

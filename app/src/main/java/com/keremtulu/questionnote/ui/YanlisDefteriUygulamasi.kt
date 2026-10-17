package com.keremtulu.questionnote.ui

import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.export.GeriYuklemeOnizlemesi
import com.keremtulu.questionnote.data.export.SonYedekBilgisi
import com.keremtulu.questionnote.data.export.YerelYedeklemeHizmeti
import com.keremtulu.questionnote.data.images.GorselTutarliligiHizmeti
import com.keremtulu.questionnote.data.ilerleme.IlerlemeHizmeti
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.prefs.GunlukHedefDeposu
import com.keremtulu.questionnote.data.prefs.RozetDeposu
import com.keremtulu.questionnote.domain.karne.OturumSonucu
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.KonuRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository
import com.keremtulu.questionnote.ui.karne.KarneEkrani
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
}

private enum class KurtarmaDurumu { YUKLENIYOR, HAZIR, HATA }
private enum class MenuEylemi { BUGUN, DERSLER, FILTRELI_CALISMA, YEDEK_OLUSTUR, YEDEK_GERI_YUKLE, GORSEL_BAKIMI }

@Composable
fun YanlisDefteriUygulamasi(
    dersRepository: DersRepository,
    konuRepository: KonuRepository,
    soruRepository: SoruRepository,
    gorselDeposu: LocalSoruGorselDeposu,
    gorselTutarliligi: GorselTutarliligiHizmeti,
    disaAktarmaHizmeti: YerelYedeklemeHizmeti,
    ilerlemeHizmeti: IlerlemeHizmeti,
    rozetDeposu: RozetDeposu,
    gunlukHedefDeposu: GunlukHedefDeposu,
) {
    var ekran by remember { mutableStateOf<UygulamaEkrani>(UygulamaEkrani.Bugun) }
    var filtreliCalismaAyar by remember { mutableStateOf(FiltreliCalismaEkraniAyar()) }
    // Filtreli oturum sürerken başka bir ekrana geçilirse önce onay istenir.
    var oturumCikisHedefi by remember { mutableStateOf<UygulamaEkrani?>(null) }
    var kurtarma by remember { mutableStateOf(KurtarmaDurumu.YUKLENIYOR) }
    var yedeklemeIslemde by remember { mutableStateOf(false) }
    var geriYuklemeOnizlemesi by remember { mutableStateOf<GeriYuklemeOnizlemesi?>(null) }
    var sonYedek by remember { mutableStateOf<com.keremtulu.questionnote.data.export.SonYedekBilgisi?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
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

    fun menuEyleminiCalistir(eylem: MenuEylemi) {
        scope.launch {
            drawerState.close()
            when (eylem) {
                MenuEylemi.BUGUN -> ekranSec(UygulamaEkrani.Bugun)
                MenuEylemi.DERSLER -> ekranSec(UygulamaEkrani.DersSecimi(soruEkleModu = false))
                MenuEylemi.FILTRELI_CALISMA -> ekranSec(UygulamaEkrani.FiltreliCalisma)
                MenuEylemi.YEDEK_OLUSTUR -> if (!yedeklemeIslemde) {
                    exportLauncher.launch("yanlis-defteri-yedek.zip")
                }
                MenuEylemi.YEDEK_GERI_YUKLE -> if (!yedeklemeIslemde) {
                    restoreLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                }
                MenuEylemi.GORSEL_BAKIMI -> ekranSec(UygulamaEkrani.GorselBakimi)
            }
        }
    }

    BackHandler(
        enabled = kurtarma == KurtarmaDurumu.HAZIR &&
            drawerState.currentValue == androidx.compose.material3.DrawerValue.Closed &&
            ekran != UygulamaEkrani.Bugun,
    ) {
        ekranSec(ekran.geriHedefi())
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = kurtarma == KurtarmaDurumu.HAZIR,
        drawerContent = {
            ModalDrawerSheet {
                YanMenuIcerigi(
                    ekran = ekran,
                    yedeklemeIslemde = yedeklemeIslemde,
                    sonYedek = sonYedek,
                    onEylem = ::menuEyleminiCalistir,
                )
            }
        },
    ) {
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
                KurtarmaDurumu.HAZIR -> when (val current = ekran) {
                    UygulamaEkrani.Bugun -> BugunEkrani(
                        soruRepository = soruRepository,
                        dersRepository = dersRepository,
                        ilerlemeHizmeti = ilerlemeHizmeti,
                        gunlukHedefDeposu = gunlukHedefDeposu,
                        onMenu = { scope.launch { drawerState.open() } },
                        onSoruEkle = { ekran = UygulamaEkrani.DersSecimi(soruEkleModu = true) },
                        onSoruAc = { ders, soruId ->
                            ekran = UygulamaEkrani.SoruAyrintisi(ders, soruId, UygulamaEkrani.Bugun)
                        },
                    )
                    is UygulamaEkrani.DersSecimi -> DerslerEkrani(
                        repository = dersRepository,
                        soruEkleModu = current.soruEkleModu,
                        onMenu = { scope.launch { drawerState.open() } },
                        onDersSecildi = { ders ->
                            ekran = if (current.soruEkleModu) {
                                UygulamaEkrani.SoruFormu(ders, null, UygulamaEkrani.Bugun)
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
                            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.review_saved)) }
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
                        onGeri = { ekran = UygulamaEkrani.Bugun },
                    )
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
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

@Composable
private fun YanMenuIcerigi(
    ekran: UygulamaEkrani,
    yedeklemeIslemde: Boolean,
    sonYedek: SonYedekBilgisi?,
    onEylem: (MenuEylemi) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val context = androidx.compose.ui.platform.LocalContext.current
    val sonYedekMetni = sonYedek?.let { backup ->
        val date = SimpleDateFormat("d MMM yyyy HH:mm", Locale("tr", "TR")).format(Date(backup.olusturmaZamani))
        val size = Formatter.formatShortFileSize(context, backup.boyutBayt)
        context.getString(
            R.string.backup_last_info,
            backup.dosyaAdi,
            date,
            size,
            context.getString(if (backup.guvenlikKopyasi) R.string.backup_safety_copy else R.string.backup_external_copy),
        )
    } ?: stringResource(R.string.backup_last_none)
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            shape = RoundedCornerShape(24.dp),
            color = colors.primaryContainer,
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("✦", color = colors.primary, style = MaterialTheme.typography.headlineSmall)
                Text(
                    stringResource(R.string.today_menu_title),
                    modifier = Modifier.padding(top = 6.dp),
                    color = colors.onPrimaryContainer,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(R.string.today_menu_subtitle),
                    modifier = Modifier.padding(top = 2.dp),
                    color = colors.onPrimaryContainer.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.menu_today)) },
            selected = ekran == UygulamaEkrani.Bugun,
            onClick = { onEylem(MenuEylemi.BUGUN) },
            icon = { Text("◷", fontSize = 20.sp) },
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.menu_lessons)) },
            selected = ekran is UygulamaEkrani.DersSecimi || ekran is UygulamaEkrani.DersAlani || ekran is UygulamaEkrani.Konular,
            onClick = { onEylem(MenuEylemi.DERSLER) },
            icon = { Text("▦", fontSize = 20.sp) },
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.menu_filtered_practice)) },
            selected = ekran == UygulamaEkrani.FiltreliCalisma ||
                ekran is UygulamaEkrani.FiltreliOturum ||
                ekran is UygulamaEkrani.FiltreliCalismaTamamlandi ||
                (ekran is UygulamaEkrani.SoruAyrintisi && ekran.geri == UygulamaEkrani.FiltreliCalisma) ||
                (ekran is UygulamaEkrani.SoruFormu &&
                    (ekran.geri == UygulamaEkrani.FiltreliCalisma || ekran.geri is UygulamaEkrani.FiltreliOturum)),
            onClick = { onEylem(MenuEylemi.FILTRELI_CALISMA) },
            icon = { Text("⌕", fontSize = 20.sp) },
        )
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text(
            text = stringResource(R.string.menu_tools_heading),
            modifier = Modifier.padding(start = 16.dp, bottom = 2.dp),
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.sp,
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.menu_create_backup)) },
            selected = false,
            onClick = { if (!yedeklemeIslemde) onEylem(MenuEylemi.YEDEK_OLUSTUR) },
            icon = { Text("↓", fontSize = 20.sp) },
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.menu_restore_backup)) },
            selected = false,
            onClick = { if (!yedeklemeIslemde) onEylem(MenuEylemi.YEDEK_GERI_YUKLE) },
            icon = { Text("↑", fontSize = 20.sp) },
        )
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(18.dp),
            color = colors.surfaceVariant,
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(
                    stringResource(R.string.menu_last_backup),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    sonYedekMetni,
                    modifier = Modifier.padding(top = 4.dp),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.menu_image_maintenance)) },
            selected = ekran == UygulamaEkrani.GorselBakimi,
            onClick = { onEylem(MenuEylemi.GORSEL_BAKIMI) },
            icon = { Text("◇", fontSize = 20.sp) },
        )
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            shape = RoundedCornerShape(20.dp),
            color = colors.surfaceVariant,
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.menu_education_title),
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    stringResource(R.string.menu_education_note),
                    modifier = Modifier.padding(top = 5.dp),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
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
    UygulamaEkrani.GorselBakimi -> UygulamaEkrani.Bugun
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

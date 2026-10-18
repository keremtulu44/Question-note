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
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import com.keremtulu.questionnote.data.ilerleme.RozetHizmeti
import com.keremtulu.questionnote.data.ilerleme.dayanikliIzle
import com.keremtulu.questionnote.data.ilerleme.IzlemeDurumu
import com.keremtulu.questionnote.data.prefs.SinavProfiliDeposu
import com.keremtulu.questionnote.data.prefs.OdakOturumuDeposu
import com.keremtulu.questionnote.domain.calisma.OdakOturumu
import com.keremtulu.questionnote.ui.ayarlar.KonuMerkeziEkrani
import com.keremtulu.questionnote.ui.calisma.OdakOturumuEkrani
import com.keremtulu.questionnote.ui.karne.RozetKutlamaHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import java.util.UUID
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
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
        val oturum: OdakOturumu,
    ) : UygulamaEkrani {
        val sira: Int get() = oturum.sira
        val sonuclar: List<OturumSonucu> get() = oturum.sonuclar
    }
    data class FiltreliCalismaTamamlandi(
        val sorular: List<Soru>,
        val dersler: Map<String, Ders>,
        val sonuclar: List<OturumSonucu>,
    ) : UygulamaEkrani
    data object GorselBakimi : UygulamaEkrani
    data object Katalog : UygulamaEkrani
    data object KonuMerkezi : UygulamaEkrani
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
    rozetHizmeti: RozetHizmeti,
    profilDeposu: SinavProfiliDeposu,
    oturumDeposu: OdakOturumuDeposu,
    gunlukHedefDeposu: GunlukHedefDeposu,
    tercihHedefDeposu: TercihGunlukHedefDeposu,
) {
    var ekran by remember { mutableStateOf<UygulamaEkrani>(UygulamaEkrani.Bugun) }
    val profilSecimi by profilDeposu.secimIzle.collectAsState()
    var bekleyenOturum by remember { mutableStateOf<UygulamaEkrani.FiltreliOturum?>(null) }
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
    var rozetGozlemYenileme by remember { mutableIntStateOf(0) }
    var rozetGozlemDurdu by remember { mutableStateOf(false) }

    fun oturumGoster(sorular: List<Soru>, dersler: Map<String, Ders>, oturum: OdakOturumu) {
        // Reviews are already committed in Room; a checkpoint failure must not undo that fact.
        runCatching { oturumDeposu.kaydet(oturum) }.onFailure {
            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.focus_checkpoint_error)) }
        }
        bekleyenOturum = null
        ekran = if (oturum.bitti) {
            UygulamaEkrani.FiltreliCalismaTamamlandi(sorular, dersler, oturum.sonuclar)
        } else {
            UygulamaEkrani.FiltreliOturum(sorular, dersler, oturum)
        }
    }

    fun oturumBaslat(sorular: List<Soru>, dersler: Map<String, Ders>, bugunden: Boolean = false) {
        if (sorular.isEmpty()) return
        val benzersiz = sorular.distinctBy { it.id }
        oturumGoster(benzersiz, dersler, OdakOturumu(UUID.randomUUID().toString(), benzersiz.map { it.id },
            System.currentTimeMillis(), bugunden))
    }

    fun karneyiKapat(hedef: UygulamaEkrani) {
        runCatching { oturumDeposu.temizle() }
        ekran = hedef
    }

    suspend fun kurtarmayiCalistir() {
        kurtarma = KurtarmaDurumu.YUKLENIYOR
        try {
            disaAktarmaHizmeti.kesintiliGeriYuklemeyiTamamla()
            runCatching { gorselDeposu.eskiGecicileriTemizle() }
            sonYedek = disaAktarmaHizmeti.sonYedekBilgisiGetir()
            val kayit = withContext(Dispatchers.IO) { oturumDeposu.oku() }
            if (kayit != null) {
                val dersler = dersRepository.dersleriIzle(false).first().associateBy { it.id }
                val sorular = kayit.soruIdleri.mapNotNull { soruRepository.soruGetir(it) }
                    .filter { it.dersId in dersler }
                val gecmis = sorular.mapNotNull { soru ->
                    soruRepository.tekrarGecmisiniIzle(soru.id).first().firstOrNull()?.let { soru.id to it }
                }.toMap()
                val kurtarilan = kayit.kurtar(sorular.map { it.id }.toSet(), gecmis)
                if (kurtarilan.soruIdleri.isNotEmpty()) oturumGoster(sorular, dersler, kurtarilan)
                else withContext(Dispatchers.IO) { oturumDeposu.temizle() }
            }
            kurtarma = KurtarmaDurumu.HAZIR
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            kurtarma = KurtarmaDurumu.HATA
        }
    }

    LaunchedEffect(disaAktarmaHizmeti) { kurtarmayiCalistir() }
    LaunchedEffect(profilSecimi.profil) { filtreliCalismaAyar = FiltreliCalismaEkraniAyar() }
    LaunchedEffect(ekran, kurtarma, yedeklemeIslemde) {
        if (kurtarma == KurtarmaDurumu.HAZIR && !yedeklemeIslemde &&
            (ekran == UygulamaEkrani.Seviye || ekran == UygulamaEkrani.Bugun)) {
            if (rozetGozlemDurdu) rozetGozlemYenileme++
            try {
                rozetHizmeti.degerlendir()
            } catch (iptal: CancellationException) {
                throw iptal
            } catch (_: Exception) {
                snackbarHostState.showSnackbar(context.getString(R.string.level_error))
            }
        }
    }
    LaunchedEffect(kurtarma, yedeklemeIslemde, rozetHizmeti, rozetGozlemYenileme) {
        if (kurtarma == KurtarmaDurumu.HAZIR && !yedeklemeIslemde) {
            rozetGozlemDurdu = false
            dayanikliIzle { rozetHizmeti.degisiklikleriIzle() }.collect { durum ->
                when (durum) {
                    is IzlemeDurumu.Hazir -> rozetGozlemDurdu = false
                    is IzlemeDurumu.Hata -> if (!durum.yenidenDeneniyor) {
                        rozetGozlemDurdu = true
                        val yanit = snackbarHostState.showSnackbar(
                            context.getString(R.string.progress_observer_error),
                            actionLabel = context.getString(R.string.level_retry),
                            duration = SnackbarDuration.Long,
                        )
                        if (yanit == SnackbarResult.ActionPerformed) rozetGozlemYenileme++
                    }
                }
            }
        }
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

    fun ekranSec(hedef: UygulamaEkrani) {
        if (yedeklemeIslemde || kurtarma != KurtarmaDurumu.HAZIR) return
        if (ekran is UygulamaEkrani.FiltreliOturum) {
            oturumCikisHedefi = hedef
        } else if (ekran is UygulamaEkrani.FiltreliCalismaTamamlandi) {
            karneyiKapat(hedef)
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (altMenuGorunur) {
                AltMenu(secili = ekran.aktifSekme(), onSec = ::sekmeSec)
            }
        },
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets)) {
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
                        onSoruEkle = { ekran = UygulamaEkrani.DersSecimi(soruEkleModu = true) },
                        onOturumBaslat = { sorular, dersler -> oturumBaslat(sorular, dersler, bugunden = true) },
                        onDevam = bekleyenOturum?.let { onceki -> { ekran = onceki; bekleyenOturum = null } },
                    )
                    is UygulamaEkrani.DersSecimi -> DerslerEkrani(
                        repository = dersRepository,
                        soruEkleModu = current.soruEkleModu,
                        profilAdi = profilSecimi.profil.ad,
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
                        onOturumBaslat = { questions, lessons -> oturumBaslat(questions, lessons) },
                        onDevam = bekleyenOturum?.let { onceki -> { ekran = onceki; bekleyenOturum = null } },
                    )
                    is UygulamaEkrani.FiltreliOturum -> {
                        val question = current.sorular.firstOrNull { it.id == current.oturum.simdikiId }
                        val lesson = question?.let { current.dersler[it.dersId] }
                        if (question == null || lesson == null) {
                            UygulamaDurumEkrani(
                                metin = stringResource(R.string.filtered_practice_question_unavailable), yukleniyor = false,
                                tekrarDene = { oturumGoster(current.sorular, current.dersler, current.oturum.atla()) },
                            )
                        } else key(current.oturum.id, question.id) {
                            OdakOturumuEkrani(
                                soru = question, ders = lesson, repository = soruRepository, gorselDeposu = gorselDeposu,
                                sira = current.sira + 1, toplam = current.oturum.soruIdleri.size, sonuclar = current.sonuclar,
                                cozumOnceAcildi = question.id in current.oturum.cozumAcilanlar,
                                onCozumAcildi = { oturumGoster(current.sorular, current.dersler, current.oturum.cozumuAc()) },
                                onGeri = { ekranSec(current.geriHedefi()) },
                                onSonuc = { sonuc, cozum ->
                                    val yeni = current.oturum.cevapla(OturumSonucu(question.id, sonuc, cozum))
                                    oturumGoster(current.sorular, current.dersler, yeni)
                                },
                                onSonaBirak = { oturumGoster(current.sorular, current.dersler, current.oturum.sonaBirak()) },
                                onAtla = { oturumGoster(current.sorular, current.dersler, current.oturum.atla()) },
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
                        rozetHizmeti = rozetHizmeti,
                        gunlukHedefDeposu = gunlukHedefDeposu,
                        onKonudanCalis = { konuId ->
                            filtreliCalismaAyar = FiltreliCalismaEkraniAyar(
                                filtre = SoruCalismaFiltresi(konuId = konuId),
                            )
                            karneyiKapat(UygulamaEkrani.FiltreliCalisma)
                        },
                        onFiltrelereDon = { karneyiKapat(UygulamaEkrani.FiltreliCalisma) },
                        onBugun = { karneyiKapat(UygulamaEkrani.Bugun) },
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
                    UygulamaEkrani.KonuMerkezi -> KonuMerkeziEkrani(
                        dersRepository = dersRepository, konuRepository = konuRepository,
                        onDers = { ekran = UygulamaEkrani.Konular(it, UygulamaEkrani.KonuMerkezi) },
                        onGeri = { ekran = UygulamaEkrani.Ayarlar },
                    )
                    UygulamaEkrani.Katalog -> KatalogYonetimiEkrani(
                        soruRepository = soruRepository,
                        onGeri = { ekran = UygulamaEkrani.Ayarlar },
                    )
                    UygulamaEkrani.Seviye -> SeviyeEkrani(
                        seviyeHizmeti = seviyeHizmeti,
                        rozetDeposu = rozetDeposu,
                        onBugun = { ekranSec(UygulamaEkrani.Bugun) },
                        onSoruEkle = { ekranSec(UygulamaEkrani.DersSecimi(soruEkleModu = true)) },
                    )
                    UygulamaEkrani.Ayarlar -> AyarlarEkrani(
                        gunlukHedefDeposu = tercihHedefDeposu,
                        profilDeposu = profilDeposu, dersRepository = dersRepository,
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
                        onKonular = { ekranSec(UygulamaEkrani.KonuMerkezi) },
                    )
                }
            }
        }
    }

    RozetKutlamaHost(rozetDeposu,
        goster = kurtarma == KurtarmaDurumu.HAZIR && !yedeklemeIslemde &&
            ekran !is UygulamaEkrani.FiltreliOturum && ekran !is UygulamaEkrani.SoruFormu &&
            ekran !is UygulamaEkrani.SoruAyrintisi && oturumCikisHedefi == null && geriYuklemeOnizlemesi == null,
    )

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
                        bekleyenOturum = oturum
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
                                // The service invalidates persisted resume state only after a proven commit.
                                val result = disaAktarmaHizmeti.geriYukle(preview)
                                bekleyenOturum = null
                                val profilHazir = try {
                                    dersRepository.sinavProfiliniUygula(profilDeposu.secili())
                                    true
                                } catch (iptal: CancellationException) {
                                    throw iptal
                                } catch (_: Exception) {
                                    false
                                }
                                geriYuklemeOnizlemesi = null
                                ekran = UygulamaEkrani.Bugun
                                sonYedek = disaAktarmaHizmeti.sonYedekBilgisiGetir()
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.backup_restore_success, result.guvenlikYedegiAdi) +
                                        if (profilHazir) "" else "\n" + context.getString(R.string.settings_profile_error),
                                )
                            } catch (cancel: CancellationException) {
                                throw cancel
                            } catch (_: Exception) {
                                geriYuklemeOnizlemesi = null
                                try {
                                    disaAktarmaHizmeti.kesintiliGeriYuklemeyiTamamla()
                                    val kayit = withContext(Dispatchers.IO) { oturumDeposu.oku() }
                                    if (kayit?.id != bekleyenOturum?.oturum?.id) bekleyenOturum = null
                                } catch (iptal: CancellationException) {
                                    throw iptal
                                } catch (_: Exception) {
                                    bekleyenOturum = null
                                    kurtarma = KurtarmaDurumu.HATA
                                }
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
    UygulamaEkrani.Ayarlar, UygulamaEkrani.Katalog, UygulamaEkrani.GorselBakimi, UygulamaEkrani.KonuMerkezi -> AltSekme.AYARLAR
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
    is UygulamaEkrani.FiltreliOturum -> if (oturum.bugunden) UygulamaEkrani.Bugun else UygulamaEkrani.FiltreliCalisma
    is UygulamaEkrani.FiltreliCalismaTamamlandi -> UygulamaEkrani.FiltreliCalisma
    UygulamaEkrani.GorselBakimi -> UygulamaEkrani.Ayarlar
    UygulamaEkrani.KonuMerkezi -> UygulamaEkrani.Ayarlar
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

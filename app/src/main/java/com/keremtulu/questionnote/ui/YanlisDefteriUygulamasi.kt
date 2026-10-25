package com.keremtulu.questionnote.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import com.keremtulu.questionnote.data.ilerleme.RozetHizmeti
import com.keremtulu.questionnote.data.ilerleme.dayanikliIzle
import com.keremtulu.questionnote.data.ilerleme.IzlemeDurumu
import com.keremtulu.questionnote.data.prefs.SinavProfiliDeposu
import com.keremtulu.questionnote.data.prefs.OdakOturumuDeposu
import com.keremtulu.questionnote.domain.calisma.OdakOturumu
import com.keremtulu.questionnote.domain.calisma.YerelTakvimSaati
import com.keremtulu.questionnote.domain.model.SoruIslemSonucu
import com.keremtulu.questionnote.data.ses.HapticTipi
import com.keremtulu.questionnote.data.ses.SesTuruGenis
import com.keremtulu.questionnote.data.ses.SesYoneticisi
import com.keremtulu.questionnote.ui.ayarlar.KonuMerkeziEkrani
import com.keremtulu.questionnote.ui.calisma.OdakOturumuEkrani
import com.keremtulu.questionnote.ui.calisma.TahtaOturumCizimBellegi
import com.keremtulu.questionnote.ui.karne.RozetKutlamaHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
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
import androidx.compose.ui.text.font.FontWeight
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
import com.keremtulu.questionnote.ui.tema.HareketTuru
import com.keremtulu.questionnote.ui.tema.LocalHareketPolitikasi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import com.keremtulu.questionnote.ui.premium.PremiumButton

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
    data class SoruFormu(
        val ders: Ders,
        val soruId: String?,
        val geri: UygulamaEkrani,
        val gizliKaynak: String? = null,
    ) : UygulamaEkrani
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
    data object Denemeler : UygulamaEkrani
    data class DenemeDetay(val denemeId: String?) : UygulamaEkrani
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
    rutbeDeposu: com.keremtulu.questionnote.data.prefs.RutbeDeposu,
    rozetHizmeti: RozetHizmeti,
    profilDeposu: SinavProfiliDeposu,
    tekrarPolitikasi: com.keremtulu.questionnote.data.prefs.TekrarPolitikasiDeposu,
    motorTanisi: com.keremtulu.questionnote.data.gelisim.MotorTaniHizmeti,
    oturumDeposu: OdakOturumuDeposu,
    gunlukHedefDeposu: GunlukHedefDeposu,
    tercihHedefDeposu: TercihGunlukHedefDeposu,
    denemeDeposu: com.keremtulu.questionnote.data.prefs.DenemeDeposu,
) {
    var ekran by remember { mutableStateOf<UygulamaEkrani>(UygulamaEkrani.Bugun) }
    var kurulumGerekli by remember { mutableStateOf(profilDeposu.kurulumGerekli()) }
    val tahtaOturumId = (ekran as? UygulamaEkrani.FiltreliOturum)?.oturum?.id
    val tahtaOturumCizimBellegi = remember(tahtaOturumId) { TahtaOturumCizimBellegi() }
    val profilSecimi by profilDeposu.secimIzle.collectAsState()
    val denemeler by denemeDeposu.kayitlariIzle.collectAsState()
    var bekleyenOturum by remember { mutableStateOf<UygulamaEkrani.FiltreliOturum?>(null) }
    var filtreliCalismaAyar by remember { mutableStateOf(FiltreliCalismaEkraniAyar()) }
    // Filtreli oturum sürerken başka bir ekrana geçilirse önce onay istenir.
    var oturumCikisHedefi by remember { mutableStateOf<UygulamaEkrani?>(null) }
    var kurtarma by remember { mutableStateOf(KurtarmaDurumu.YUKLENIYOR) }
    var yedeklemeIslemde by remember { mutableStateOf(false) }
    var oturumBaslatiliyor by remember { mutableStateOf(false) }
    var geriYuklemeOnizlemesi by remember { mutableStateOf<GeriYuklemeOnizlemesi?>(null) }
    var sonYedek by remember { mutableStateOf<SonYedekBilgisi?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var rozetGozlemYenileme by remember { mutableIntStateOf(0) }
    var rozetGozlemDurdu by remember { mutableStateOf(false) }
    /** Kaydedilen tekrarın kısa kutucuğu: sıradaki tarih + "Geri al" (az önceki kaydı siler). */
    fun geriAlKutucuguGoster(soruId: String, sonrakiGun: Int?) {
        SesYoneticisi.uygulama(context).calPremium(SesTuruGenis.TEKRAR, HapticTipi.BASARI)
        scope.launch {
            val tarih = sonrakiGun?.let {
                LocalDate.ofEpochDay(it.toLong())
                    .format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("tr", "TR")))
            }
            val mesaj = if (tarih != null) {
                context.getString(R.string.review_saved_on, tarih)
            } else {
                context.getString(R.string.review_saved)
            }
            val secim = snackbarHostState.showSnackbar(
                message = mesaj,
                actionLabel = context.getString(R.string.review_undo),
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            )
            if (secim == SnackbarResult.ActionPerformed) {
                when (soruRepository.tekrarGeriAl(soruId)) {
                    SoruIslemSonucu.Basarili ->
                        snackbarHostState.showSnackbar(context.getString(R.string.review_undone))
                    else ->
                        snackbarHostState.showSnackbar(context.getString(R.string.review_undo_error))
                }
            }
        }
    }

    // Günlük hedef tamamlandığında tek seferlik kısa ses; kaynak sayaç geçişi (ekranlar arası).
    val hedefGun = remember { LocalDate.now(YerelTakvimSaati.zone).toEpochDay().toInt() }
    var sonHedefSayisi: Int? by remember { mutableStateOf(null) }
    LaunchedEffect(ilerlemeHizmeti, hedefGun) {
        ilerlemeHizmeti.bugunSayisiIzle(hedefGun)
            .catch { }
            .collect { yeni ->
                val once = sonHedefSayisi
                val hedef = gunlukHedefDeposu.hedef()
                if (once != null && once < hedef && yeni >= hedef) {
                    delay(300)
                    SesYoneticisi.uygulama(context).calPremium(SesTuruGenis.HEDEF, HapticTipi.BASARI_GUCLU)
                }
                sonHedefSayisi = yeni
            }
    }

    fun oturumGoster(sorular: List<Soru>, dersler: Map<String, Ders>, oturum: OdakOturumu) {
        val surum = oturumDeposu.hazirla(oturum)
        val guncel = oturumDeposu.oku() ?: oturum
        scope.launch(NonCancellable) {
            try { oturumDeposu.kalicilastir(surum) }
            catch (_: Exception) { scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.focus_checkpoint_error)) } }
        }
        bekleyenOturum = null
        ekran = if (guncel.bitti) {
            UygulamaEkrani.FiltreliCalismaTamamlandi(sorular, dersler, guncel.sonuclar)
        } else {
            UygulamaEkrani.FiltreliOturum(sorular, dersler, guncel)
        }
    }

    fun oturumBaslat(sorular: List<Soru>, dersler: Map<String, Ders>, bugunden: Boolean = false) {
        if (sorular.isEmpty() || oturumBaslatiliyor || yedeklemeIslemde || kurtarma != KurtarmaDurumu.HAZIR) return
        oturumBaslatiliyor = true
        scope.launch {
            try {
                val filtre = if (bugunden) SoruCalismaFiltresi(vadesiGelen = true) else filtreliCalismaAyar.filtre
                val uygun = soruRepository.filtreliCalismaSorulariniIzle(filtre).first().associateBy { it.id }
                val guncelDersler = dersRepository.aktifDersleriIzle().first().associateBy { it.id }
                val benzersiz = sorular.distinctBy { it.id }.mapNotNull { uygun[it.id] }.filter { it.dersId in guncelDersler }
                if (benzersiz.isNotEmpty()) oturumGoster(benzersiz, guncelDersler,
                    OdakOturumu(UUID.randomUUID().toString(), benzersiz.map { it.id }, System.currentTimeMillis(), bugunden))
                else snackbarHostState.showSnackbar(context.getString(R.string.analysis_no_eligible_group))
            } catch (iptal: CancellationException) {
                throw iptal
            } catch (_: Exception) {
                snackbarHostState.showSnackbar(context.getString(R.string.focus_load_error))
            } finally {
                oturumBaslatiliyor = false
            }
        }
    }

    fun karneyiKapat(hedef: UygulamaEkrani) {
        val surum = oturumDeposu.sifirlamayiHazirla()
        scope.launch(NonCancellable) {
            try { oturumDeposu.kalicilastir(surum) }
            catch (_: Exception) { scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.focus_checkpoint_error)) } }
        }
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
            rozetHizmeti.degerlendir(kutla = false)
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
        if (yedeklemeIslemde || oturumBaslatiliyor || kurtarma != KurtarmaDurumu.HAZIR) return
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

    if (kurulumGerekli) {
        com.keremtulu.questionnote.ui.kurulum.DersKurulumEkrani(
            dersRepository = dersRepository,
            onTamam = { kurulumGerekli = false },
        )
        return
    }
    val altMenuGorunur = ekran.altMenuGorunur()
    val hareketPolitikasi = LocalHareketPolitikasi.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Surface(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    shadowElevation = 6.dp,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text(
                            data.visuals.message,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        data.visuals.actionLabel?.let { etiket ->
                            PremiumTextButton(
                                onClick = { data.performAction() },
                                interactionTur = com.keremtulu.questionnote.ui.premium.EtkilesimTuru.GERI,
                            ) {
                                Text(
                                    etiket,
                                    color = MaterialTheme.colorScheme.inversePrimary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (altMenuGorunur) {
                AltMenu(secili = ekran.aktifSekme(), onSec = ::sekmeSec)
            }
        },
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets)) {
            AnimatedContent(
                targetState = ekran,
                transitionSpec = {
                    if (hareketPolitikasi.hareketAcik) {
                        val duration = hareketPolitikasi.sure(HareketTuru.GIRIS)
                        (fadeIn(tween(duration)) + slideInHorizontally(
                            animationSpec = tween(duration),
                            initialOffsetX = { width -> width / 18 },
                        )) togetherWith (fadeOut(tween(duration)) + slideOutHorizontally(
                            animationSpec = tween(duration),
                            targetOffsetX = { width -> -width / 24 },
                        ))
                    } else {
                        EnterTransition.None togetherWith ExitTransition.None
                    }
                },
                label = "uygulama-ekran-gecisi",
            ) { displayedScreen ->
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
                KurtarmaDurumu.HAZIR -> when (val current = displayedScreen) {
                    UygulamaEkrani.Bugun -> BugunEkrani(
                        soruRepository = soruRepository,
                        dersRepository = dersRepository,
                        ilerlemeHizmeti = ilerlemeHizmeti,
                        gunlukHedefDeposu = gunlukHedefDeposu,
                        onSoruEkle = { ekran = UygulamaEkrani.DersSecimi(soruEkleModu = true) },
                        onDeneme = { ekran = UygulamaEkrani.Denemeler },
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
                        denemeler = denemeler,
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
                        onTekrarTamamlandi = { _, _ -> ekran = current.geri },
                        onKaydedildi = ::geriAlKutucuguGoster,
                        tekrarOturumunda = current.tekrarOturumunda,
                        denemeler = denemeler,
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
                                oturumId = current.oturum.id,
                                oturumDeposu = oturumDeposu,
                                oturumCizimBellegi = tahtaOturumCizimBellegi,
                                denemeler = denemeler,
                                disDuraklatildi = oturumCikisHedefi != null,
                                cozumOnceAcildi = question.id in current.oturum.cozumAcilanlar,
                                onCozumAcildi = { oturumGoster(current.sorular, current.dersler, current.oturum.cozumuAc()) },
                                onGeri = { ekranSec(current.geriHedefi()) },
                                onSonuc = { sonuc, cozum ->
                                    val yeni = current.oturum.cevapla(OturumSonucu(question.id, sonuc, cozum))
                                    oturumGoster(current.sorular, current.dersler, yeni)
                                },
                                onKaydedildi = ::geriAlKutucuguGoster,
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
                        gizliKaynak = current.gizliKaynak,
                    )
                    UygulamaEkrani.Denemeler -> com.keremtulu.questionnote.ui.deneme.DenemeListesiEkrani(
                        denemeDeposu = denemeDeposu,
                        dersRepository = dersRepository,
                        onGeri = { ekran = UygulamaEkrani.Bugun },
                        onYeni = { ekran = UygulamaEkrani.DenemeDetay(null) },
                        onAc = { ekran = UygulamaEkrani.DenemeDetay(it) },
                    )
                    is UygulamaEkrani.DenemeDetay -> com.keremtulu.questionnote.ui.deneme.DenemeDetayEkrani(
                        denemeId = current.denemeId,
                        denemeDeposu = denemeDeposu,
                        dersRepository = dersRepository,
                        soruRepository = soruRepository,
                        onGeri = { ekran = UygulamaEkrani.Denemeler },
                        onKaydedildi = { id ->
                            if (current.denemeId != id) ekran = UygulamaEkrani.DenemeDetay(id)
                        },
                        onSoruEkle = { ders, kaynak, id ->
                            ekran = UygulamaEkrani.SoruFormu(ders, null, UygulamaEkrani.DenemeDetay(id), kaynak)
                        },
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
                        onCalis = { filtre, adet ->
                            filtreliCalismaAyar = FiltreliCalismaEkraniAyar(filtre, adet.coerceIn(1, 5).toString())
                            ekranSec(UygulamaEkrani.FiltreliCalisma)
                        },
                    )
                    UygulamaEkrani.Ayarlar -> AyarlarEkrani(
                        gunlukHedefDeposu = tercihHedefDeposu,
                        profilDeposu = profilDeposu, dersRepository = dersRepository,
                        tekrarPolitikasi = tekrarPolitikasi,
                        motorTanisi = motorTanisi,
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
                        onPremiumDemo = {
                            context.startActivity(
                                android.content.Intent(
                                    context,
                                    com.keremtulu.questionnote.ui.premium.PremiumDemoActivity::class.java,
                                ),
                            )
                        },
                    )
                }
            }
            }
        }
    }

    com.keremtulu.questionnote.ui.karne.KutlamaHost(rozetDeposu, rutbeDeposu,
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
                PremiumTextButton(
                    onClick = {
                        bekleyenOturum = oturum
                        oturumCikisHedefi = null
                        ekran = hedef
                    },
                ) { Text(stringResource(R.string.filtered_practice_exit_confirm)) }
            },
            dismissButton = {
                PremiumTextButton(onClick = { oturumCikisHedefi = null }) {
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
                        ) + "\n\n" + context.getString(R.string.backup_measurement_count, preview.yedek.sayilar.olcumSayisi),
                    )
                }
            },
            confirmButton = {
                PremiumButton(
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
                PremiumTextButton(enabled = !yedeklemeIslemde, onClick = {
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
    UygulamaEkrani.Denemeler, is UygulamaEkrani.DenemeDetay -> AltSekme.BUGUN
    is UygulamaEkrani.SoruAyrintisi -> if (tekrarOturumunda) AltSekme.FILTRELI else AltSekme.DERSLER
    is UygulamaEkrani.SoruFormu -> if (geri is UygulamaEkrani.FiltreliOturum) {
        AltSekme.FILTRELI
    } else if (geri is UygulamaEkrani.DenemeDetay) {
        AltSekme.BUGUN
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
    UygulamaEkrani.Denemeler -> UygulamaEkrani.Bugun
    is UygulamaEkrani.DenemeDetay -> UygulamaEkrani.Denemeler
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
            PremiumTextButton(onClick = retry) { Text(stringResource(R.string.backup_restore_retry)) }
        }
    }
}

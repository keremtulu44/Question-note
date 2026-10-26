package com.keremtulu.questionnote.ui.ogretmen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.export.SonYedekBilgisi
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.prefs.OgretmenAlanDurumu
import com.keremtulu.questionnote.data.prefs.OgretmenDeposu
import com.keremtulu.questionnote.data.ses.HapticTipi
import com.keremtulu.questionnote.data.ses.SesTuruGenis
import com.keremtulu.questionnote.data.ses.SesYoneticisi
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.Konu
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.KonuRepository
import com.keremtulu.questionnote.ui.animasyon.SureToken
import com.keremtulu.questionnote.ui.animasyon.animateFloatPremium
import com.keremtulu.questionnote.ui.animasyon.horizontalSwipeNavigation
import com.keremtulu.questionnote.ui.animasyon.tweenPremium
import com.keremtulu.questionnote.ui.premium.EtkilesimTuru
import com.keremtulu.questionnote.ui.premium.PremiumButton
import com.keremtulu.questionnote.ui.premium.PremiumCard
import com.keremtulu.questionnote.ui.premium.PremiumGradyanButon
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import com.keremtulu.questionnote.ui.tema.HareketTuru
import com.keremtulu.questionnote.ui.tema.LocalHareketPolitikasi
import kotlinx.coroutines.launch

/**
 * Teacher workspace host. Three destinations: the cross-class queue, the class list and settings.
 * The class list stays the landing tab so an existing user keeps the familiar first screen; the
 * queue is surfaced as a pending-count card that answers "what do I solve today".
 */
@Composable
fun OgretmenAnaEkrani(
    ogretmenDeposu: OgretmenDeposu,
    gorselDeposu: LocalSoruGorselDeposu,
    sonYedek: SonYedekBilgisi?,
    yedeklemeIslemde: Boolean,
    onOgrenciModunaGec: () -> Unit,
    onYedekOlustur: () -> Unit,
    onYedekGeriYukle: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    dersRepository: DersRepository? = null,
    konuRepository: KonuRepository? = null,
) {
    val veri by ogretmenDeposu.veriIzle.collectAsState()
    val alanDurumu by ogretmenDeposu.durumIzle.collectAsState()
    val dersler by (dersRepository?.dersleriIzle(arsivli = true)?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList<Ders>()) })
    val konular by (konuRepository?.tumunuIzle()?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList<Konu>()) })

    var altEkran by remember { mutableStateOf<OgretmenEkranDurumu>(OgretmenEkranDurumu.Gruplar) }
    var baslatildi by remember { mutableStateOf(ogretmenDeposu.anlikDurum() != OgretmenAlanDurumu.Yukleniyor) }
    var iceAktarmaBandi by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        ogretmenDeposu.baslat()
        baslatildi = true
    }

    val hareketPolitikasi = LocalHareketPolitikasi.current
    val klavyeAcik = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val bugun = remember { bugunEpochGun() }
    val dersAdlari = remember(dersler) { dersler.associate { it.id to it.ad } }
    val konuAdlari = remember(konular) { konular.associate { it.id to it.ad } }

    BackHandler(enabled = altEkran != OgretmenEkranDurumu.Gruplar) {
        altEkran = when (val mevcut = altEkran) {
            OgretmenEkranDurumu.Kuyruk, OgretmenEkranDurumu.Ayarlar -> OgretmenEkranDurumu.Gruplar
            is OgretmenEkranDurumu.GrupDetay -> OgretmenEkranDurumu.Gruplar
            is OgretmenEkranDurumu.SoruFormu -> OgretmenEkranDurumu.GrupDetay(mevcut.grupId)
            OgretmenEkranDurumu.Gruplar -> OgretmenEkranDurumu.Gruplar
        }
    }

    val altMenuGorunur = (altEkran == OgretmenEkranDurumu.Gruplar ||
        altEkran == OgretmenEkranDurumu.Kuyruk ||
        altEkran == OgretmenEkranDurumu.Ayarlar) && !klavyeAcik
    val aktifSekme = when (altEkran) {
        OgretmenEkranDurumu.Kuyruk -> OgretmenAltSekme.KUYRUK
        OgretmenEkranDurumu.Ayarlar -> OgretmenAltSekme.AYARLAR
        else -> OgretmenAltSekme.GRUPLAR
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = data.visuals.message,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        data.visuals.actionLabel?.let { etiket ->
                            PremiumTextButton(
                                onClick = { data.performAction() },
                                interactionTur = EtkilesimTuru.GERI,
                            ) {
                                Text(
                                    text = etiket,
                                    color = MaterialTheme.colorScheme.inversePrimary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (altMenuGorunur) {
                OgretmenAltMenu(
                    secili = aktifSekme,
                    onSec = { hedef ->
                        altEkran = when (hedef) {
                            OgretmenAltSekme.KUYRUK -> OgretmenEkranDurumu.Kuyruk
                            OgretmenAltSekme.GRUPLAR -> OgretmenEkranDurumu.Gruplar
                            OgretmenAltSekme.AYARLAR -> OgretmenEkranDurumu.Ayarlar
                        }
                    },
                )
            }
        },
    ) { insets ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .consumeWindowInsets(insets),
        ) {
            if (!baslatildi) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Box
            }
            Column(Modifier.fillMaxSize()) {
                AlanDurumuBandi(
                    durum = alanDurumu,
                    gorunur = iceAktarmaBandi,
                    onKapat = { iceAktarmaBandi = false },
                )
                AnimatedContent(
                    targetState = altEkran,
                    modifier = Modifier.weight(1f),
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
                    label = "ogretmen-ekran-gecisi",
                ) { mevcut ->
                    when (mevcut) {
                        OgretmenEkranDurumu.Kuyruk -> OgretmenKuyrukEkrani(
                            veri = veri,
                            ogretmenDeposu = ogretmenDeposu,
                            gorselDeposu = gorselDeposu,
                            dersAdlari = dersAdlari,
                            konuAdlari = konuAdlari,
                            bugun = bugun,
                            snackbarHostState = snackbarHostState,
                            onGrupAc = { grupId ->
                                altEkran = OgretmenEkranDurumu.GrupDetay(grupId)
                            },
                        )
                        OgretmenEkranDurumu.Gruplar -> OgretmenGruplarEkrani(
                            veri = veri,
                            ogretmenDeposu = ogretmenDeposu,
                            gorselDeposu = gorselDeposu,
                            dersAdlari = dersAdlari,
                            bugun = bugun,
                            onGrupAc = { grupId -> altEkran = OgretmenEkranDurumu.GrupDetay(grupId) },
                            onKuyrukAc = { altEkran = OgretmenEkranDurumu.Kuyruk },
                        )
                        OgretmenEkranDurumu.Ayarlar -> OgretmenAyarlarEkrani(
                            veri = veri,
                            ogretmenDeposu = ogretmenDeposu,
                            sonYedek = sonYedek,
                            yedeklemeIslemde = yedeklemeIslemde,
                            onOgrenciModunaGec = onOgrenciModunaGec,
                            onYedekOlustur = onYedekOlustur,
                            onYedekGeriYukle = onYedekGeriYukle,
                            snackbarHostState = snackbarHostState,
                        )
                        is OgretmenEkranDurumu.GrupDetay -> {
                            val grup = veri.gruplar.firstOrNull { it.id == mevcut.grupId }
                            if (grup == null) {
                                altEkran = OgretmenEkranDurumu.Gruplar
                            } else {
                                OgretmenGrupDetayEkrani(
                                    grup = grup,
                                    veri = veri,
                                    ogretmenDeposu = ogretmenDeposu,
                                    gorselDeposu = gorselDeposu,
                                    dersAdlari = dersAdlari,
                                    konuAdlari = konuAdlari,
                                    bugun = bugun,
                                    sonYedek = sonYedek,
                                    snackbarHostState = snackbarHostState,
                                    onGeri = { altEkran = OgretmenEkranDurumu.Gruplar },
                                    onSoruEkle = { altEkran = OgretmenEkranDurumu.SoruFormu(grup.id, null) },
                                    onSoruDuzenle = { soruId ->
                                        altEkran = OgretmenEkranDurumu.SoruFormu(grup.id, soruId)
                                    },
                                )
                            }
                        }
                        is OgretmenEkranDurumu.SoruFormu -> {
                            val grup = veri.gruplar.firstOrNull { it.id == mevcut.grupId }
                            if (grup == null) {
                                altEkran = OgretmenEkranDurumu.Gruplar
                            } else {
                                OgretmenSoruFormuEkrani(
                                    grup = grup,
                                    mevcutSoru = mevcut.soruId?.let { aranan ->
                                        veri.sorular.firstOrNull { it.id == aranan }
                                    },
                                    ogretmenDeposu = ogretmenDeposu,
                                    gorselDeposu = gorselDeposu,
                                    dersler = dersler,
                                    konular = konular,
                                    onGeri = { altEkran = OgretmenEkranDurumu.GrupDetay(grup.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Import notice only: Room is writable even when the legacy file could not be read. */
@Composable
private fun AlanDurumuBandi(
    durum: OgretmenAlanDurumu,
    gorunur: Boolean,
    onKapat: () -> Unit,
) {
    if (!gorunur) return
    val (metin, renk, onay) = when (durum) {
        is OgretmenAlanDurumu.IceAktarildi -> Triple(
            if (durum.atlananKayit > 0) {
                stringResource(R.string.teacher_import_banner) + " " +
                    stringResource(R.string.teacher_import_skipped, durum.atlananKayit)
            } else {
                stringResource(R.string.teacher_import_banner)
            },
            PremiumMarkaRenkler.Deniz,
            true,
        )
        is OgretmenAlanDurumu.Aktarilamadi -> Triple(
            stringResource(R.string.teacher_import_failed),
            MaterialTheme.colorScheme.error,
            true,
        )
        else -> Triple("", PremiumMarkaRenkler.Deniz, false)
    }
    if (!onay) return
    Surface(
        color = renk.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, renk.copy(alpha = 0.38f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = metin,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            PremiumTextButton(onClick = onKapat) {
                Text(stringResource(R.string.teacher_import_dismiss), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
internal fun OgretmenAltMenu(
    secili: OgretmenAltSekme,
    onSec: (OgretmenAltSekme) -> Unit,
    systemInsets: WindowInsets = WindowInsets.navigationBars.union(WindowInsets.displayCutout),
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val feedbackScope = rememberCoroutineScope()
    val buyukYazi = LocalDensity.current.fontScale >= 1.5f

    fun sekmeyeGec(hedef: OgretmenAltSekme) {
        if (hedef == secili) return
        val pan = if (hedef.ordinal < secili.ordinal) -0.18f else 0.18f
        SesYoneticisi.uygulama(context).calPremium(
            SesTuruGenis.SAYFA_GECIS,
            HapticTipi.SWIPE,
            feedbackScope,
            pan,
        )
        onSec(hedef)
    }

    Surface(color = colors.surface, tonalElevation = 4.dp, shadowElevation = 10.dp) {
        Column(
            Modifier.windowInsetsPadding(
                systemInsets.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
            ),
        ) {
            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectableGroup()
                    .horizontalSwipeNavigation(
                        thresholdDp = 56f,
                        onSwipeLeft = { OgretmenAltSekme.entries.getOrNull(secili.ordinal + 1)?.let(::sekmeyeGec) },
                        onSwipeRight = { OgretmenAltSekme.entries.getOrNull(secili.ordinal - 1)?.let(::sekmeyeGec) },
                    )
                    .padding(horizontal = 8.dp),
            ) {
                OgretmenAltSekme.entries.forEach { sekme ->
                    val aktif = sekme == secili
                    val etiket = stringResource(
                        when (sekme) {
                            OgretmenAltSekme.KUYRUK -> R.string.teacher_nav_queue
                            OgretmenAltSekme.GRUPLAR -> R.string.teacher_nav_groups
                            OgretmenAltSekme.AYARLAR -> R.string.teacher_nav_settings
                        },
                    )
                    val gosterge = animateFloatPremium(
                        target = if (aktif) 1f else 0f,
                        animationSpec = tweenPremium(SureToken.NORMAL),
                        label = "ogretmen-alt-menu",
                    )
                    Column(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 64.dp)
                            .semantics { contentDescription = etiket }
                            .selectable(selected = aktif, role = Role.Tab, onClick = { sekmeyeGec(sekme) })
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Box(
                            Modifier
                                .size(52.dp, 30.dp)
                                .drawBehind {
                                    drawRoundRect(
                                        color = colors.primary.copy(alpha = 0.16f * gosterge.value),
                                        cornerRadius = CornerRadius(15.dp.toPx()),
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            OgretmenMenuSimgesi(sekme, if (aktif) colors.primary else colors.onSurfaceVariant)
                        }
                        Text(
                            text = etiket,
                            fontSize = if (buyukYazi) 10.sp else 11.sp,
                            lineHeight = 13.sp,
                            textAlign = TextAlign.Center,
                            color = if (aktif) colors.primary else colors.onSurfaceVariant,
                            fontWeight = if (aktif) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

/** Shared header used by the queue and class list so both screens state the same numbers. */
@Composable
internal fun OgretmenHeroKarti(
    kicker: String,
    baslik: String,
    altBaslik: String,
    bekleyen: Int,
    cozulen: Int,
    grupSayisi: Int,
    birinciEtiket: String = stringResource(R.string.teacher_stat_groups),
    ikinciEtiket: String = stringResource(R.string.teacher_stat_unsolved),
    ucuncuEtiket: String = stringResource(R.string.teacher_stat_solved),
) {
    com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart(
        modifier = Modifier.fillMaxWidth(),
        borderAnimation = false,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = kicker,
                color = PremiumMarkaRenkler.BakirIsik,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = baslik,
                color = PremiumMarkaRenkler.Kagit,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = altBaslik,
                color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HeroSayacKutusu(
                    deger = grupSayisi,
                    etiket = birinciEtiket,
                    vurgu = PremiumMarkaRenkler.Kagit,
                    modifier = Modifier.weight(1f),
                )
                HeroSayacKutusu(
                    deger = bekleyen,
                    etiket = ikinciEtiket,
                    vurgu = PremiumMarkaRenkler.BakirIsik,
                    modifier = Modifier.weight(1f),
                )
                HeroSayacKutusu(
                    deger = cozulen,
                    etiket = ucuncuEtiket,
                    vurgu = PremiumMarkaRenkler.YesilAcik,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
internal fun HeroSayacKutusu(
    deger: Int,
    etiket: String,
    vurgu: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.11f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            PremiumMarkaRenkler.Kagit.copy(alpha = 0.16f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = deger.toString(),
                color = vurgu,
                style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = etiket,
                color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.78f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )
        }
    }
}

/** Small helper shared by the queue and detail screens for the empty state button. */
@Composable
internal fun KuyrukButonu(onClick: () -> Unit, modifier: Modifier = Modifier) {
    PremiumButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 46.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(stringResource(R.string.teacher_queue_open), fontWeight = FontWeight.SemiBold)
    }
}

internal val OgretmenIcerikPadding = PaddingValues(start = 18.dp, top = 10.dp, end = 18.dp, bottom = 104.dp)

internal val OgretmenGuvenliInsets = WindowInsets.safeDrawing

/** Scaffold-level helper: keeps the FAB hidden while the keyboard is open. */
@Composable
internal fun klavyeAcikMi(): Boolean = WindowInsets.ime.getBottom(LocalDensity.current) > 0

/** Reports a short message through the shared snackbar host. */
internal fun SnackbarHostState.bildir(scope: kotlinx.coroutines.CoroutineScope, metin: String) {
    scope.launch { showSnackbar(metin) }
}

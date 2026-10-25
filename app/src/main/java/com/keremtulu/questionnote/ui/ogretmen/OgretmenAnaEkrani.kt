package com.keremtulu.questionnote.ui.ogretmen

import android.Manifest
import android.content.pm.PackageManager
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.keremtulu.questionnote.BuildConfig
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.export.SonYedekBilgisi
import com.keremtulu.questionnote.data.images.KameraCekimi
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.prefs.OgretmenDeposu
import com.keremtulu.questionnote.data.ses.HapticTipi
import com.keremtulu.questionnote.data.ses.SesTuruGenis
import com.keremtulu.questionnote.data.ses.SesYoneticisi
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.OgretmenGrubu
import com.keremtulu.questionnote.domain.model.OgretmenSorusu
import com.keremtulu.questionnote.domain.model.OgretmenVerisi
import com.keremtulu.questionnote.domain.model.OgretmenZorluk
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import com.keremtulu.questionnote.ui.LocalUygulamaSesleri
import com.keremtulu.questionnote.ui.animasyon.SureToken
import com.keremtulu.questionnote.ui.animasyon.animateFloatPremium
import com.keremtulu.questionnote.ui.animasyon.horizontalSwipeNavigation
import com.keremtulu.questionnote.ui.animasyon.tweenPremium
import com.keremtulu.questionnote.ui.bilesenler.GorselKirpmaAlani
import com.keremtulu.questionnote.ui.bilesenler.GorselKirpmaDialog
import com.keremtulu.questionnote.ui.bilesenler.GorselSecimBolumu
import com.keremtulu.questionnote.ui.bilesenler.SoruGorselResmi
import com.keremtulu.questionnote.ui.bilesenler.premium.CamKarti
import com.keremtulu.questionnote.ui.bilesenler.premium.CamYukseklik
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.bilesenler.premium.PremiumBosDurum
import com.keremtulu.questionnote.ui.premium.EtkilesimTuru
import com.keremtulu.questionnote.ui.premium.PremiumButton
import com.keremtulu.questionnote.ui.premium.PremiumCard
import com.keremtulu.questionnote.ui.premium.PremiumExtendedFloatingActionButton
import com.keremtulu.questionnote.ui.premium.PremiumGradyanButon
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumSembolAilesi
import com.keremtulu.questionnote.ui.premium.PremiumSwitch
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import com.keremtulu.questionnote.ui.tema.HareketTuru
import com.keremtulu.questionnote.ui.tema.LocalHareketPolitikasi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private sealed interface OgretmenEkranDurumu {
    data object Gruplar : OgretmenEkranDurumu
    data object Ayarlar : OgretmenEkranDurumu
    data class GrupDetay(val grupId: String) : OgretmenEkranDurumu
    data class SoruFormu(val grupId: String, val soruId: String?) : OgretmenEkranDurumu
}

private enum class OgretmenAltSekme { GRUPLAR, AYARLAR }

@Composable
fun OgretmenAnaEkrani(
    ogretmenDeposu: OgretmenDeposu,
    gorselDeposu: LocalSoruGorselDeposu,
    sonYedek: SonYedekBilgisi?,
    yedeklemeIslemde: Boolean,
    onOgrenciModunaGec: () -> Unit,
    onYedekOlustur: () -> Unit,
    onYedekGeriYukle: () -> Unit,
) {
    val veri by ogretmenDeposu.veriIzle.collectAsState()
    var altEkran by remember { mutableStateOf<OgretmenEkranDurumu>(OgretmenEkranDurumu.Gruplar) }
    val snackbarHostState = remember { SnackbarHostState() }
    val hareketPolitikasi = LocalHareketPolitikasi.current
    val klavyeAcik = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    BackHandler(enabled = altEkran != OgretmenEkranDurumu.Gruplar) {
        altEkran = when (val mevcut = altEkran) {
            OgretmenEkranDurumu.Gruplar -> OgretmenEkranDurumu.Gruplar
            OgretmenEkranDurumu.Ayarlar -> OgretmenEkranDurumu.Gruplar
            is OgretmenEkranDurumu.GrupDetay -> OgretmenEkranDurumu.Gruplar
            is OgretmenEkranDurumu.SoruFormu -> OgretmenEkranDurumu.GrupDetay(mevcut.grupId)
        }
    }

    val altMenuGorunur = (altEkran == OgretmenEkranDurumu.Gruplar || altEkran == OgretmenEkranDurumu.Ayarlar) &&
        !klavyeAcik
    val aktifSekme = if (altEkran == OgretmenEkranDurumu.Ayarlar) {
        OgretmenAltSekme.AYARLAR
    } else {
        OgretmenAltSekme.GRUPLAR
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
            AnimatedContent(
                targetState = altEkran,
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
                    OgretmenEkranDurumu.Gruplar -> OgretmenGruplarEkrani(
                        veri = veri,
                        ogretmenDeposu = ogretmenDeposu,
                        onGrupAc = { grupId -> altEkran = OgretmenEkranDurumu.GrupDetay(grupId) },
                    )
                    OgretmenEkranDurumu.Ayarlar -> OgretmenAyarlarEkrani(
                        veri = veri,
                        sonYedek = sonYedek,
                        yedeklemeIslemde = yedeklemeIslemde,
                        onOgrenciModunaGec = onOgrenciModunaGec,
                        onYedekOlustur = onYedekOlustur,
                        onYedekGeriYukle = onYedekGeriYukle,
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
                                snackbarHostState = snackbarHostState,
                                onGeri = { altEkran = OgretmenEkranDurumu.Gruplar },
                                onSoruEkle = { altEkran = OgretmenEkranDurumu.SoruFormu(grup.id, null) },
                                onSoruDuzenle = { soruId -> altEkran = OgretmenEkranDurumu.SoruFormu(grup.id, soruId) },
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
                                onGeri = { altEkran = OgretmenEkranDurumu.GrupDetay(grup.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OgretmenAltMenu(
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
                    .padding(horizontal = 12.dp),
            ) {
                OgretmenAltSekme.entries.forEach { sekme ->
                    val aktif = sekme == secili
                    val etiket = stringResource(
                        when (sekme) {
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
                            .weight(if (buyukYazi && aktif) 1.5f else 1f)
                            .heightIn(min = 72.dp)
                            .semantics { if (buyukYazi && !aktif) contentDescription = etiket }
                            .selectable(selected = aktif, role = Role.Tab, onClick = { sekmeyeGec(sekme) })
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(56.dp, 32.dp)
                                .drawBehind {
                                    drawRoundRect(
                                        color = colors.primary.copy(alpha = 0.16f * gosterge.value),
                                        cornerRadius = CornerRadius(16.dp.toPx()),
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            OgretmenMenuSimgesi(sekme, if (aktif) colors.primary else colors.onSurfaceVariant)
                        }
                        if (!buyukYazi || aktif) {
                            Text(
                                text = etiket,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
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
}

@Composable
private fun OgretmenMenuSimgesi(sekme: OgretmenAltSekme, renk: Color) {
    Canvas(Modifier.size(22.dp)) {
        val kalinlik = 1.9.dp.toPx()
        val firca = Stroke(width = kalinlik, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = size.width
        val h = size.height
        when (sekme) {
            OgretmenAltSekme.GRUPLAR -> {
                drawRoundRect(
                    color = renk.copy(alpha = 0.45f),
                    topLeft = Offset(w * 0.24f, h * 0.14f),
                    size = Size(w * 0.62f, h * 0.50f),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                    style = firca,
                )
                drawRoundRect(
                    color = renk,
                    topLeft = Offset(w * 0.12f, h * 0.30f),
                    size = Size(w * 0.66f, h * 0.56f),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                    style = firca,
                )
                drawLine(
                    color = renk,
                    start = Offset(w * 0.28f, h * 0.55f),
                    end = Offset(w * 0.62f, h * 0.55f),
                    strokeWidth = kalinlik,
                    cap = StrokeCap.Round,
                )
            }
            OgretmenAltSekme.AYARLAR -> {
                val m = Offset(w / 2f, h / 2f)
                drawCircle(renk, radius = w * 0.25f, center = m, style = firca)
                drawCircle(renk, radius = 1.8.dp.toPx(), center = m)
                for (i in 0 until 6) {
                    val aci = Math.toRadians((i * 60.0) - 90.0)
                    val ic = Offset(
                        (m.x + cos(aci) * w * 0.28f).toFloat(),
                        (m.y + sin(aci) * h * 0.28f).toFloat(),
                    )
                    val dis = Offset(
                        (m.x + cos(aci) * w * 0.41f).toFloat(),
                        (m.y + sin(aci) * h * 0.41f).toFloat(),
                    )
                    drawLine(renk, ic, dis, strokeWidth = kalinlik, cap = StrokeCap.Round)
                }
            }
        }
    }
}

@Composable
private fun OgretmenGruplarEkrani(
    veri: OgretmenVerisi,
    ogretmenDeposu: OgretmenDeposu,
    onGrupAc: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val klavyeAcik = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val koyuTema = isSystemInDarkTheme()
    var grupDialogAcik by remember { mutableStateOf(false) }
    var grupArama by remember { mutableStateOf("") }

    val toplamCozulmemis = remember(veri.sorular) { veri.sorular.count { !it.cozuldu } }
    val toplamCozulmus = remember(veri.sorular) { veri.sorular.count { it.cozuldu } }
    val toplamSoru = toplamCozulmemis + toplamCozulmus

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            if (!klavyeAcik) {
                PremiumExtendedFloatingActionButton(
                    onClick = { grupDialogAcik = true },
                    modifier = Modifier.testTag("ogretmen_grup_ekle_fab"),
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text("+", fontSize = 24.sp, fontWeight = FontWeight.Light)
                    Text(
                        text = stringResource(R.string.teacher_add_group),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
    ) { insets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .consumeWindowInsets(insets),
            contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                GradyanKart(modifier = Modifier.fillMaxWidth(), borderAnimation = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.teacher_kicker),
                            color = PremiumMarkaRenkler.BakirIsik,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.teacher_home_title),
                            color = PremiumMarkaRenkler.Kagit,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.teacher_home_subtitle),
                            color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (veri.gruplar.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                HeroSayacKutusu(
                                    deger = veri.gruplar.size,
                                    etiket = stringResource(R.string.teacher_stat_groups),
                                    vurgu = PremiumMarkaRenkler.Kagit,
                                    modifier = Modifier.weight(1f),
                                )
                                HeroSayacKutusu(
                                    deger = toplamCozulmemis,
                                    etiket = stringResource(R.string.teacher_stat_unsolved),
                                    vurgu = PremiumMarkaRenkler.BakirIsik,
                                    modifier = Modifier.weight(1f),
                                )
                                HeroSayacKutusu(
                                    deger = toplamCozulmus,
                                    etiket = stringResource(R.string.teacher_stat_solved),
                                    vurgu = PremiumMarkaRenkler.YesilAcik,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (toplamSoru > 0) {
                                val oran = animateFloatPremium(
                                    target = toplamCozulmus.toFloat() / toplamSoru.toFloat(),
                                    animationSpec = tweenPremium(SureToken.MEDIUM),
                                    label = "ogretmen-toplam-ilerleme",
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(PremiumMarkaRenkler.Kagit.copy(alpha = 0.16f)),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(oran.value.coerceIn(0f, 1f))
                                            .height(6.dp)
                                            .clip(CircleShape)
                                            .background(PremiumMarkaRenkler.YesilAcik),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (veri.gruplar.size >= 4) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = grupArama,
                            onValueChange = { grupArama = it.take(60) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text(stringResource(R.string.teacher_group_search_placeholder)) },
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        )
                        if (klavyeAcik) {
                            PremiumTextButton(onClick = { focusManager.clearFocus() }) {
                                Text(stringResource(R.string.common_hide_keyboard), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            if (veri.gruplar.isEmpty()) {
                item {
                    PremiumBosDurum(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        baslik = stringResource(R.string.teacher_groups_empty_title),
                        aciklama = stringResource(R.string.teacher_groups_empty_subtitle),
                        butonMetni = stringResource(R.string.teacher_add_group),
                        onButonTikla = { grupDialogAcik = true },
                    )
                }
            } else {
                val aranan = grupArama.trim()
                val siraliGruplar = veri.gruplar
                    .sortedWith(compareBy<OgretmenGrubu> { it.sira }.thenBy { it.ad })
                    .filter { grup ->
                        aranan.isEmpty() ||
                            grup.ad.contains(aranan, ignoreCase = true) ||
                            grup.aciklama.orEmpty().contains(aranan, ignoreCase = true)
                    }
                if (siraliGruplar.isEmpty()) {
                    item {
                        PremiumBosDurum(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            baslik = stringResource(R.string.teacher_search_empty_title),
                            aciklama = stringResource(R.string.teacher_search_empty_subtitle),
                        )
                    }
                } else {
                    items(siraliGruplar, key = { it.id }) { grup ->
                        val grupSorulari = veri.sorular.filter { it.grupId == grup.id }
                        val cozulmemis = grupSorulari.count { !it.cozuldu }
                        val cozulmus = grupSorulari.count { it.cozuldu }
                        OgretmenGrupKarti(
                            grup = grup,
                            cozulmemis = cozulmemis,
                            cozulmus = cozulmus,
                            koyuTema = koyuTema,
                            onClick = { onGrupAc(grup.id) },
                        )
                    }
                }
            }
        }
    }

    if (grupDialogAcik) {
        GrupFormDialog(
            baslik = stringResource(R.string.teacher_add_group),
            baslangicAd = "",
            baslangicAciklama = "",
            hazirOnerileriGoster = true,
            onDismiss = { grupDialogAcik = false },
            onKaydet = { ad, aciklama ->
                ogretmenDeposu.grupEkle(ad, aciklama) != null
            },
        )
    }
}

@Composable
private fun HeroSayacKutusu(
    deger: Int,
    etiket: String,
    vurgu: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.11f),
        border = BorderStroke(1.dp, PremiumMarkaRenkler.Kagit.copy(alpha = 0.16f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
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
                color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.80f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun OgretmenGrupKarti(
    grup: OgretmenGrubu,
    cozulmemis: Int,
    cozulmus: Int,
    koyuTema: Boolean,
    onClick: () -> Unit,
) {
    val vurgu = grupVurguRengi(grup.ad, koyuTema)
    val cozulmusRenk = if (koyuTema) PremiumMarkaRenkler.YesilAcik else PremiumMarkaRenkler.Yesil
    val bekleyenRenk = if (koyuTema) PremiumMarkaRenkler.BakirIsik else PremiumMarkaRenkler.Bakir
    val toplam = cozulmemis + cozulmus

    PremiumCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag("ogretmen_grup_${grup.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (cozulmemis > 0) vurgu.copy(alpha = 0.42f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        interactionTur = EtkilesimTuru.KART_ACILIS,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(vurgu.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = grupMonogram(grup.ad),
                        color = vurgu,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = grup.ad,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    grup.aciklama?.takeIf { it.isNotBlank() }?.let { not ->
                        Text(
                            text = not,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (toplam == 0) {
                            Text(
                                text = stringResource(R.string.teacher_group_empty_badge),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            DurumRozeti(
                                metin = stringResource(R.string.teacher_group_pending_badge, cozulmemis),
                                renk = if (cozulmemis > 0) bekleyenRenk else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            DurumRozeti(
                                metin = stringResource(R.string.teacher_group_solved_badge, cozulmus),
                                renk = cozulmusRenk,
                            )
                        }
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = vurgu.copy(alpha = 0.12f),
                ) {
                    Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "→",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = PremiumSembolAilesi,
                            color = vurgu,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            if (toplam > 0) {
                val oran = cozulmus.toFloat() / toplam.toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(oran.coerceIn(0f, 1f))
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(cozulmusRenk),
                    )
                }
            }
        }
    }
}

@Composable
private fun DurumRozeti(metin: String, renk: Color) {
    Surface(
        shape = CircleShape,
        color = renk.copy(alpha = 0.13f),
    ) {
        Text(
            text = metin,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            color = renk,
            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GrupFormDialog(
    baslik: String,
    baslangicAd: String,
    baslangicAciklama: String,
    hazirOnerileriGoster: Boolean,
    onDismiss: () -> Unit,
    onKaydet: suspend (String, String) -> Boolean,
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var grupAdi by remember { mutableStateOf(baslangicAd) }
    var grupAciklama by remember { mutableStateOf(baslangicAciklama) }
    var grupHatasi by remember { mutableStateOf(false) }
    var kaydediliyor by remember { mutableStateOf(false) }

    val hazirOneriler = remember {
        listOf("6A", "6B", "7A", "7B", "8A", "8B", "9A", "10A", "11A", "12A", "TYT Grup", "Özel Ders")
    }

    fun kaydet() {
        if (kaydediliyor) return
        kaydediliyor = true
        scope.launch {
            try {
                if (onKaydet(grupAdi, grupAciklama)) {
                    onDismiss()
                } else {
                    grupHatasi = true
                }
            } finally {
                kaydediliyor = false
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!kaydediliyor) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        text = baslik,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (hazirOnerileriGoster) {
                    item {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            hazirOneriler.forEach { oneri ->
                                FilterChip(
                                    selected = grupAdi == oneri,
                                    onClick = {
                                        grupAdi = oneri
                                        grupHatasi = false
                                    },
                                    label = { Text(oneri) },
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = grupAdi,
                        onValueChange = {
                            grupAdi = it
                            grupHatasi = false
                        },
                        modifier = Modifier.fillMaxWidth().testTag("ogretmen_grup_adi_girdi"),
                        singleLine = true,
                        isError = grupHatasi,
                        label = { Text(stringResource(R.string.teacher_group_name_label)) },
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        ),
                    )
                }
                item {
                    OutlinedTextField(
                        value = grupAciklama,
                        onValueChange = { grupAciklama = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stringResource(R.string.teacher_group_desc_label)) },
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                kaydet()
                            },
                        ),
                    )
                }
                if (grupHatasi) {
                    item {
                        Text(
                            text = stringResource(R.string.teacher_group_name_error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PremiumTextButton(
                            enabled = !kaydediliyor,
                            onClick = onDismiss,
                        ) { Text(stringResource(R.string.question_form_cancel)) }
                        PremiumButton(
                            enabled = !kaydediliyor,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .testTag("ogretmen_grup_kaydet_buton"),
                            onClick = ::kaydet,
                        ) { Text(stringResource(R.string.teacher_group_save)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun OgretmenGrupDetayEkrani(
    grup: OgretmenGrubu,
    veri: OgretmenVerisi,
    ogretmenDeposu: OgretmenDeposu,
    gorselDeposu: LocalSoruGorselDeposu,
    snackbarHostState: SnackbarHostState,
    onGeri: () -> Unit,
    onSoruEkle: () -> Unit,
    onSoruDuzenle: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val koyuTema = isSystemInDarkTheme()
    val klavyeAcik = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    var cozulmusSekmesi by remember(grup.id) { mutableStateOf(false) }
    var zorlukFiltresi by remember(grup.id) { mutableStateOf<OgretmenZorluk?>(null) }
    var soruArama by remember(grup.id) { mutableStateOf("") }
    var tamEkranSoruId by remember(grup.id) { mutableStateOf<String?>(null) }
    var kopyaSoru by remember(grup.id) { mutableStateOf<OgretmenSorusu?>(null) }
    var silinecekSoru by remember(grup.id) { mutableStateOf<OgretmenSorusu?>(null) }
    var grupDuzenleAcik by remember(grup.id) { mutableStateOf(false) }
    var grupSilOnayAcik by remember(grup.id) { mutableStateOf(false) }

    val grupSorulari = remember(veri.sorular, grup.id) {
        veri.sorular.filter { it.grupId == grup.id }.sortedByDescending { it.olusturma }
    }
    val cozulmemisler = remember(grupSorulari) { grupSorulari.filter { !it.cozuldu } }
    val cozulmusler = remember(grupSorulari) { grupSorulari.filter { it.cozuldu } }
    val aktifListe = remember(cozulmusSekmesi, cozulmemisler, cozulmusler, zorlukFiltresi, soruArama) {
        val baz = if (cozulmusSekmesi) cozulmusler else cozulmemisler
        val aranan = soruArama.trim()
        baz.filter { soru ->
            (zorlukFiltresi == null || soru.zorluk == zorlukFiltresi) &&
                (aranan.isEmpty() ||
                    soru.baslik.orEmpty().contains(aranan, ignoreCase = true) ||
                    soru.cevap.orEmpty().contains(aranan, ignoreCase = true))
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            if (!klavyeAcik) {
                PremiumExtendedFloatingActionButton(
                    onClick = onSoruEkle,
                    modifier = Modifier.testTag("ogretmen_soru_ekle_fab"),
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text("+", fontSize = 24.sp, fontWeight = FontWeight.Light)
                    Text(
                        text = stringResource(R.string.teacher_add_question),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
    ) { insets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .consumeWindowInsets(insets),
            contentPadding = PaddingValues(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PremiumTextButton(onClick = onGeri, interactionTur = EtkilesimTuru.GERI) {
                        Text("← " + stringResource(R.string.common_back))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (klavyeAcik) {
                            PremiumTextButton(onClick = { focusManager.clearFocus() }) {
                                Text(
                                    stringResource(R.string.common_hide_keyboard),
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        PremiumTextButton(onClick = { grupDuzenleAcik = true }) {
                            Text(stringResource(R.string.teacher_edit_group))
                        }
                        PremiumTextButton(onClick = { grupSilOnayAcik = true }) {
                            Text(
                                stringResource(R.string.teacher_delete_group),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
            item {
                GradyanKart(modifier = Modifier.fillMaxWidth(), borderAnimation = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(PremiumMarkaRenkler.Kagit.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = grupMonogram(grup.ad),
                                    color = PremiumMarkaRenkler.Kagit,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.teacher_kicker),
                                    color = PremiumMarkaRenkler.BakirIsik,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = grup.ad,
                                    color = PremiumMarkaRenkler.Kagit,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        grup.aciklama?.takeIf { it.isNotBlank() }?.let { not ->
                            Text(
                                text = not,
                                color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        if (grupSorulari.isNotEmpty()) {
                            val oran = cozulmusler.size.toFloat() / grupSorulari.size.toFloat()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.teacher_group_counts,
                                        cozulmemisler.size,
                                        cozulmusler.size,
                                    ),
                                    color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.90f),
                                    style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(PremiumMarkaRenkler.Kagit.copy(alpha = 0.18f)),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(oran.coerceIn(0f, 1f))
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(PremiumMarkaRenkler.YesilAcik),
                                )
                            }
                        }
                    }
                }
            }
            item {
                CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PremiumButton(
                            onClick = { cozulmusSekmesi = false },
                            modifier = Modifier.weight(1f).heightIn(min = 46.dp).testTag("sekme_cozmediklerim"),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!cozulmusSekmesi) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Transparent
                                },
                                contentColor = if (!cozulmusSekmesi) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            ),
                        ) {
                            Text(
                                text = stringResource(R.string.teacher_tab_unsolved, cozulmemisler.size),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        PremiumButton(
                            onClick = { cozulmusSekmesi = true },
                            modifier = Modifier.weight(1f).heightIn(min = 46.dp).testTag("sekme_cozduklerim"),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (cozulmusSekmesi) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Transparent
                                },
                                contentColor = if (cozulmusSekmesi) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            ),
                        ) {
                            Text(
                                text = stringResource(R.string.teacher_tab_solved, cozulmusler.size),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = zorlukFiltresi == null,
                        onClick = { zorlukFiltresi = null },
                        label = { Text(stringResource(R.string.teacher_filter_all)) },
                    )
                    OgretmenZorluk.entries.forEach { z ->
                        FilterChip(
                            selected = zorlukFiltresi == z,
                            onClick = { zorlukFiltresi = if (zorlukFiltresi == z) null else z },
                            label = { Text(stringResource(zorlukEtiketi(z))) },
                        )
                    }
                }
            }
            if (grupSorulari.size >= 3) {
                item {
                    OutlinedTextField(
                        value = soruArama,
                        onValueChange = { soruArama = it.take(60) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.teacher_question_search_placeholder)) },
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    )
                }
            }

            if (aktifListe.isEmpty()) {
                item {
                    val filtreAktif = zorlukFiltresi != null || soruArama.isNotBlank()
                    PremiumBosDurum(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        baslik = stringResource(
                            when {
                                filtreAktif -> R.string.teacher_search_empty_title
                                cozulmusSekmesi -> R.string.teacher_solved_empty_title
                                else -> R.string.teacher_unsolved_empty_title
                            },
                        ),
                        aciklama = stringResource(
                            when {
                                filtreAktif -> R.string.teacher_search_empty_subtitle
                                cozulmusSekmesi -> R.string.teacher_solved_empty_subtitle
                                else -> R.string.teacher_unsolved_empty_subtitle
                            },
                        ),
                        butonMetni = if (!filtreAktif && !cozulmusSekmesi) {
                            stringResource(R.string.teacher_add_question)
                        } else {
                            null
                        },
                        onButonTikla = if (!filtreAktif && !cozulmusSekmesi) onSoruEkle else null,
                    )
                }
            } else {
                itemsIndexed(aktifListe, key = { _, s -> s.id }) { idx, soru ->
                    OgretmenSoruKarti(
                        siraNo = idx + 1,
                        soru = soru,
                        koyuTema = koyuTema,
                        gorselDeposu = gorselDeposu,
                        onTamEkran = { tamEkranSoruId = soru.id },
                        onCozulduDegistir = { yeniDurum ->
                            scope.launch { ogretmenDeposu.cozulduDurumuDegistir(soru.id, yeniDurum) }
                        },
                        onBaskaGrubaEkle = { kopyaSoru = soru },
                        onDuzenle = { onSoruDuzenle(soru.id) },
                        onSil = { silinecekSoru = soru },
                    )
                }
            }
        }
    }

    tamEkranSoruId?.let { id ->
        val canliSoru = veri.sorular.firstOrNull { it.id == id }
        if (canliSoru != null) {
            OgretmenTamEkranSoruDialog(
                grupAdi = grup.ad,
                soru = canliSoru,
                gorselDeposu = gorselDeposu,
                onDismiss = { tamEkranSoruId = null },
                onCozulduDegistir = { yeniDurum ->
                    scope.launch { ogretmenDeposu.cozulduDurumuDegistir(canliSoru.id, yeniDurum) }
                },
                onBaskaGrubaEkle = {
                    tamEkranSoruId = null
                    kopyaSoru = canliSoru
                },
            )
        } else {
            tamEkranSoruId = null
        }
    }

    kopyaSoru?.let { kaynak ->
        val digerGruplar = veri.gruplar.filter { it.id != grup.id }
            .sortedWith(compareBy<OgretmenGrubu> { it.sira }.thenBy { it.ad })
        BaskaGrubaEkleDialog(
            digerGruplar = digerGruplar,
            onDismiss = { kopyaSoru = null },
            onOnayla = { secilenler ->
                scope.launch {
                    val adet = ogretmenDeposu.baskaGruplaraKopyala(kaynak.id, secilenler)
                    kopyaSoru = null
                    if (adet > 0) {
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.teacher_copy_success, adet),
                        )
                    }
                }
            },
        )
    }

    silinecekSoru?.let { hedef ->
        AlertDialog(
            onDismissRequest = { silinecekSoru = null },
            title = { Text(stringResource(R.string.teacher_delete_question_title)) },
            text = { Text(stringResource(R.string.teacher_delete_question_message)) },
            confirmButton = {
                PremiumButton(
                    onClick = {
                        scope.launch {
                            ogretmenDeposu.soruSil(hedef.id)
                            silinecekSoru = null
                        }
                    },
                ) { Text(stringResource(R.string.question_delete)) }
            },
            dismissButton = {
                PremiumTextButton(onClick = { silinecekSoru = null }) {
                    Text(stringResource(R.string.question_form_cancel))
                }
            },
        )
    }

    if (grupDuzenleAcik) {
        GrupFormDialog(
            baslik = stringResource(R.string.teacher_edit_group),
            baslangicAd = grup.ad,
            baslangicAciklama = grup.aciklama.orEmpty(),
            hazirOnerileriGoster = false,
            onDismiss = { grupDuzenleAcik = false },
            onKaydet = { yeniAd, yeniAciklama ->
                ogretmenDeposu.grupGuncelle(grup.id, yeniAd, yeniAciklama)
            },
        )
    }

    if (grupSilOnayAcik) {
        AlertDialog(
            onDismissRequest = { grupSilOnayAcik = false },
            title = { Text(stringResource(R.string.teacher_delete_group)) },
            text = { Text(stringResource(R.string.teacher_delete_group_confirm, grup.ad)) },
            confirmButton = {
                PremiumButton(
                    onClick = {
                        scope.launch {
                            grupSilOnayAcik = false
                            ogretmenDeposu.grupSil(grup.id)
                            onGeri()
                        }
                    },
                ) { Text(stringResource(R.string.question_delete)) }
            },
            dismissButton = {
                PremiumTextButton(onClick = { grupSilOnayAcik = false }) {
                    Text(stringResource(R.string.question_form_cancel))
                }
            },
        )
    }
}

@Composable
private fun OgretmenSoruKarti(
    siraNo: Int,
    soru: OgretmenSorusu,
    koyuTema: Boolean,
    gorselDeposu: LocalSoruGorselDeposu,
    onTamEkran: () -> Unit,
    onCozulduDegistir: (Boolean) -> Unit,
    onBaskaGrubaEkle: () -> Unit,
    onDuzenle: () -> Unit,
    onSil: () -> Unit,
) {
    val siraliGorseller = remember(soru.gorseller) { soru.gorseller.sortedBy { it.sira } }
    val ilkDosya = siraliGorseller.firstOrNull()?.let { gorselDeposu.dosyaGoruntuIcin(it.dosyaAdi) }

    PremiumCard(
        onClick = onTamEkran,
        modifier = Modifier.fillMaxWidth().testTag("ogretmen_soru_kart_${soru.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        interactionTur = EtkilesimTuru.KART_ACILIS,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(196.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)),
            ) {
                SoruGorselResmi(
                    file = ilkDosya,
                    contentDescription = stringResource(R.string.question_image_description),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    maxDecodeEdge = 900,
                    cerceve = false,
                    onClick = onTamEkran,
                )
                if (siraliGorseller.size > 1) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
                        shape = CircleShape,
                        color = PremiumMarkaRenkler.MurekkepKoyu.copy(alpha = 0.78f),
                    ) {
                        Text(
                            text = stringResource(R.string.teacher_photo_count_badge, siraliGorseller.size),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = PremiumMarkaRenkler.Kagit,
                            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = soru.baslik ?: stringResource(R.string.teacher_question_default_title, siraNo),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                soru.zorluk?.let { z ->
                    ZorlukRozeti(z, koyuTema = koyuTema)
                }
            }
            if (!soru.cevap.isNullOrBlank()) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                ) {
                    Text(
                        text = stringResource(R.string.teacher_answer_hidden_badge),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PremiumOutlinedButton(
                    onClick = onTamEkran,
                    modifier = Modifier.weight(1f).heightIn(min = 46.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(stringResource(R.string.ask_teacher_open_fullscreen))
                }
                PremiumButton(
                    onClick = { onCozulduDegistir(!soru.cozuldu) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 46.dp)
                        .testTag("ogretmen_cozuldu_degistir_${soru.id}"),
                    shape = RoundedCornerShape(14.dp),
                    interactionTur = if (soru.cozuldu) EtkilesimTuru.TIKLAMA else EtkilesimTuru.BASARI,
                ) {
                    Text(
                        stringResource(
                            if (soru.cozuldu) R.string.teacher_mark_unsolved
                            else R.string.teacher_mark_solved,
                        ),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PremiumTextButton(
                    onClick = onBaskaGrubaEkle,
                    modifier = Modifier.testTag("ogretmen_baska_gruba_ekle_${soru.id}"),
                ) {
                    Text(
                        text = stringResource(R.string.teacher_copy_to_group),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    PremiumTextButton(onClick = onDuzenle) {
                        Text(stringResource(R.string.question_edit))
                    }
                    PremiumTextButton(onClick = onSil) {
                        Text(
                            stringResource(R.string.question_delete),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZorlukRozeti(zorluk: OgretmenZorluk, koyuTema: Boolean = false) {
    val renk = zorlukRengi(zorluk, koyuTema)
    Surface(
        shape = CircleShape,
        color = renk.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, renk.copy(alpha = 0.36f)),
    ) {
        Text(
            text = stringResource(zorlukEtiketi(zorluk)),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = renk,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun OgretmenTamEkranSoruDialog(
    grupAdi: String,
    soru: OgretmenSorusu,
    gorselDeposu: LocalSoruGorselDeposu,
    onDismiss: () -> Unit,
    onCozulduDegistir: (Boolean) -> Unit,
    onBaskaGrubaEkle: () -> Unit,
) {
    val gorseller = remember(soru.gorseller) { soru.gorseller.sortedBy { it.sira } }
    var sayfa by remember(soru.id) { mutableIntStateOf(0) }
    var cevapAcik by remember(soru.id) { mutableStateOf(false) }
    val aktifGorsel = gorseller.getOrNull(sayfa)
    val dosya = aktifGorsel?.let { gorselDeposu.dosyaGoruntuIcin(it.dosyaAdi) }
    var scale by remember(aktifGorsel?.id) { mutableFloatStateOf(1f) }
    var offsetX by remember(aktifGorsel?.id) { mutableFloatStateOf(0f) }
    var offsetY by remember(aktifGorsel?.id) { mutableFloatStateOf(0f) }
    val transformState = rememberTransformableState { zoom, pan, _ ->
        scale = (scale * zoom).coerceIn(1f, 5f)
        if (scale <= 1.02f) {
            offsetX = 0f
            offsetY = 0f
        } else {
            offsetX += pan.x
            offsetY += pan.y
        }
    }

    BackHandler(onBack = onDismiss)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PremiumMarkaRenkler.MurekkepKoyu)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = listOfNotNull(grupAdi, soru.baslik).joinToString(" · "),
                        color = PremiumMarkaRenkler.Kagit,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (gorseller.size > 1) {
                            stringResource(R.string.ask_teacher_page_indicator, sayfa + 1, gorseller.size)
                        } else {
                            stringResource(R.string.teacher_zoom_hint)
                        },
                        color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.68f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (scale > 1.05f) {
                        PremiumTextButton(
                            onClick = {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            },
                        ) {
                            Text(
                                text = stringResource(R.string.teacher_zoom_reset),
                                color = PremiumMarkaRenkler.BakirIsik,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    soru.zorluk?.let { ZorlukRozeti(it, koyuTema = true) }
                    PremiumTextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.common_close), color = PremiumMarkaRenkler.Kagit)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(aktifGorsel?.id) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (scale > 1.05f) {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                } else {
                                    scale = 2.4f
                                }
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                SoruGorselResmi(
                    file = dosya,
                    contentDescription = stringResource(R.string.question_image_description),
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                        }
                        .transformable(transformState),
                    contentScale = ContentScale.Fit,
                    maxDecodeEdge = 2_400,
                    cerceve = false,
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = PremiumMarkaRenkler.Murekkep.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, PremiumMarkaRenkler.Kagit.copy(alpha = 0.14f)),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (gorseller.size > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            PremiumTextButton(
                                onClick = { if (sayfa > 0) sayfa-- },
                                enabled = sayfa > 0,
                            ) {
                                Text(
                                    stringResource(R.string.ask_teacher_prev_page),
                                    color = PremiumMarkaRenkler.Kagit,
                                )
                            }
                            PremiumTextButton(
                                onClick = { if (sayfa < gorseller.lastIndex) sayfa++ },
                                enabled = sayfa < gorseller.lastIndex,
                            ) {
                                Text(
                                    stringResource(R.string.ask_teacher_next_page),
                                    color = PremiumMarkaRenkler.Kagit,
                                )
                            }
                        }
                    }
                    if (!soru.cevap.isNullOrBlank()) {
                        if (cevapAcik) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().testTag("ogretmen_cevap_kutusu"),
                                shape = RoundedCornerShape(14.dp),
                                color = PremiumMarkaRenkler.Bakir.copy(alpha = 0.22f),
                                border = BorderStroke(1.dp, PremiumMarkaRenkler.BakirIsik.copy(alpha = 0.45f)),
                            ) {
                                Text(
                                    text = stringResource(R.string.teacher_answer_prefix, soru.cevap),
                                    modifier = Modifier.padding(14.dp),
                                    color = PremiumMarkaRenkler.Kagit,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        PremiumOutlinedButton(
                            onClick = { cevapAcik = !cevapAcik },
                            modifier = Modifier.fillMaxWidth().testTag("ogretmen_cevabi_goster"),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text(
                                stringResource(
                                    if (cevapAcik) R.string.teacher_hide_answer else R.string.teacher_show_answer,
                                ),
                                color = PremiumMarkaRenkler.Kagit,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PremiumOutlinedButton(
                            onClick = onBaskaGrubaEkle,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text(stringResource(R.string.teacher_copy_to_group), color = PremiumMarkaRenkler.Kagit)
                        }
                        PremiumButton(
                            onClick = { onCozulduDegistir(!soru.cozuldu) },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text(
                                stringResource(
                                    if (soru.cozuldu) R.string.teacher_mark_unsolved
                                    else R.string.teacher_mark_solved,
                                ),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BaskaGrubaEkleDialog(
    digerGruplar: List<OgretmenGrubu>,
    onDismiss: () -> Unit,
    onOnayla: (Set<String>) -> Unit,
) {
    var secilenler by remember { mutableStateOf<Set<String>>(emptySet()) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Text(
                        text = stringResource(R.string.teacher_copy_dialog_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                item {
                    Text(
                        text = stringResource(R.string.teacher_copy_dialog_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (digerGruplar.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.teacher_copy_no_other_groups),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else {
                    items(digerGruplar, key = { it.id }) { hedef ->
                        val isaretli = hedef.id in secilenler
                        Surface(
                            onClick = {
                                secilenler = if (isaretli) secilenler - hedef.id else secilenler + hedef.id
                            },
                            modifier = Modifier.fillMaxWidth().testTag("hedef_grup_${hedef.id}"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isaretli) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isaretli) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                                else Color.Transparent,
                            ),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(checked = isaretli, onCheckedChange = null)
                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                    Text(
                                        text = hedef.ad,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    hedef.aciklama?.takeIf { it.isNotBlank() }?.let { not ->
                                        Text(
                                            text = not,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PremiumTextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.question_form_cancel))
                        }
                        if (digerGruplar.isNotEmpty()) {
                            PremiumButton(
                                enabled = secilenler.isNotEmpty(),
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .testTag("hedef_gruplara_kopyala_onay"),
                                onClick = { onOnayla(secilenler) },
                            ) { Text(stringResource(R.string.teacher_copy_confirm)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OgretmenSoruFormuEkrani(
    grup: OgretmenGrubu,
    mevcutSoru: OgretmenSorusu?,
    ogretmenDeposu: OgretmenDeposu,
    gorselDeposu: LocalSoruGorselDeposu,
    onGeri: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val koyuTema = isSystemInDarkTheme()
    val klavyeAcik = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val snackbarHostState = remember { SnackbarHostState() }
    var gorseller by remember(mevcutSoru?.id) {
        mutableStateOf(mevcutSoru?.gorseller.orEmpty().sortedBy { it.sira }.map { it.toGirdi() })
    }
    var baslik by remember(mevcutSoru?.id) { mutableStateOf(mevcutSoru?.baslik.orEmpty()) }
    var zorluk by remember(mevcutSoru?.id) { mutableStateOf(mevcutSoru?.zorluk) }
    var cevap by remember(mevcutSoru?.id) { mutableStateOf(mevcutSoru?.cevap.orEmpty()) }
    var kaydediliyor by remember(mevcutSoru?.id) { mutableStateOf(false) }
    var formHatasi by remember(mevcutSoru?.id) { mutableStateOf<Int?>(null) }
    var kameraCekimi by remember(mevcutSoru?.id) { mutableStateOf<KameraCekimi?>(null) }
    var kirpmaHedefi by remember(mevcutSoru?.id) { mutableStateOf<SoruGorselGirdisi?>(null) }
    var kirpmaIsleniyor by remember(mevcutSoru?.id) { mutableStateOf(false) }

    fun iptalEtVeDon() {
        val geciciler = gorseller.filter { it.gecici }
        scope.launch {
            geciciler.forEach { runCatching { gorselDeposu.geciciDosyayiSil(it.dosyaAdi) } }
        }
        onGeri()
    }

    fun soruyuKaydet() {
        if (kaydediliyor || kirpmaIsleniyor) return
        if (gorseller.isEmpty()) {
            formHatasi = R.string.question_error_photo_required
            return
        }
        kaydediliyor = true
        scope.launch {
            try {
                val sonuc = ogretmenDeposu.soruKaydet(
                    id = mevcutSoru?.id,
                    grupId = grup.id,
                    baslik = baslik,
                    zorluk = zorluk,
                    cevap = cevap,
                    gorseller = gorseller,
                )
                if (sonuc != null) {
                    onGeri()
                } else {
                    formHatasi = R.string.question_error_generic
                }
            } finally {
                kaydediliyor = false
            }
        }
    }

    BackHandler(enabled = !kaydediliyor && !kirpmaIsleniyor) { iptalEtVeDon() }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { basarili ->
        val aktif = kameraCekimi
        kameraCekimi = null
        if (aktif == null) return@rememberLauncherForActivityResult
        if (!basarili) {
            gorselDeposu.kameraCekiminiSil(aktif.id)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            try {
                if (gorseller.size >= 20) {
                    gorselDeposu.kameraCekiminiSil(aktif.id)
                    snackbarHostState.showSnackbar(context.getString(R.string.question_form_photo_limit))
                    return@launch
                }
                val girdi = gorselDeposu.kameradanHazirla(aktif.id, GorselTuru.SORU, gorseller.size)
                gorseller = gorseller + girdi
                formHatasi = null
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Throwable) {
                snackbarHostState.showSnackbar(context.getString(R.string.question_form_photo_error))
            }
        }
    }

    val kameraIzni = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { verildi ->
        if (verildi) {
            try {
                val cekim = gorselDeposu.kameraCekimiOlustur()
                kameraCekimi = cekim
                takePicture.launch(cekim.uri)
            } catch (_: Throwable) {
                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.question_form_photo_error)) }
            }
        }
    }

    val galeriSecici = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val liste = gorseller.toMutableList()
                for (uri in uris) {
                    if (liste.size >= 20) break
                    liste += gorselDeposu.secicidenHazirla(uri, GorselTuru.SORU, liste.size)
                }
                gorseller = liste
                if (gorseller.isNotEmpty()) formHatasi = null
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Throwable) {
                snackbarHostState.showSnackbar(context.getString(R.string.question_form_photo_error))
            }
        }
    }

    fun kamerayiAc() {
        val izinli = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (izinli) {
            try {
                val cekim = gorselDeposu.kameraCekimiOlustur()
                kameraCekimi = cekim
                takePicture.launch(cekim.uri)
            } catch (_: Throwable) {
                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.question_form_photo_error)) }
            }
        } else {
            kameraIzni.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!klavyeAcik) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 4.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        formHatasi?.let { hataId ->
                            Text(
                                text = stringResource(hataId),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        PremiumGradyanButon(
                            onClick = ::soruyuKaydet,
                            enabled = !kaydediliyor && !kirpmaIsleniyor,
                            modifier = Modifier.fillMaxWidth().testTag("ogretmen_soru_kaydet"),
                        ) {
                            Text(stringResource(R.string.teacher_question_save), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
            contentPadding = PaddingValues(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PremiumTextButton(onClick = ::iptalEtVeDon, enabled = !kaydediliyor) {
                        Text("← " + stringResource(R.string.question_form_cancel))
                    }
                    if (klavyeAcik) {
                        PremiumTextButton(onClick = { focusManager.clearFocus() }) {
                            Text(
                                stringResource(R.string.common_hide_keyboard),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
            item {
                GradyanKart(modifier = Modifier.fillMaxWidth(), borderAnimation = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = grup.ad,
                            color = PremiumMarkaRenkler.BakirIsik,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(
                                if (mevcutSoru == null) R.string.teacher_add_question
                                else R.string.teacher_edit_question,
                            ),
                            color = PremiumMarkaRenkler.Kagit,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.teacher_question_form_hint),
                            color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.84f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            item {
                CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GorselSecimBolumu(
                            title = stringResource(R.string.question_form_question_images),
                            images = gorseller,
                            imageStore = gorselDeposu,
                            enabled = !kaydediliyor && !kirpmaIsleniyor,
                            onCamera = ::kamerayiAc,
                            onPicker = {
                                galeriSecici.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                            onCrop = { kirpmaHedefi = it },
                            onImagesChanged = {
                                gorseller = it
                                if (it.isNotEmpty()) formHatasi = null
                            },
                        )
                    }
                }
            }
            item {
                CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(R.string.teacher_question_difficulty_label),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OgretmenZorluk.entries.forEach { secenek ->
                                val secili = zorluk == secenek
                                val zRenk = zorlukRengi(secenek, koyuTema)
                                Surface(
                                    onClick = { zorluk = if (secili) null else secenek },
                                    modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (secili) zRenk.copy(alpha = 0.18f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = BorderStroke(
                                        width = if (secili) 1.5.dp else 1.dp,
                                        color = if (secili) zRenk
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    ),
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                        Text(
                                            text = stringResource(zorlukEtiketi(secenek)),
                                            color = if (secili) zRenk else MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (secili) FontWeight.Bold else FontWeight.Medium,
                                        )
                                    }
                                }
                            }
                        }
                        OutlinedTextField(
                            value = cevap,
                            onValueChange = { cevap = it },
                            modifier = Modifier.fillMaxWidth().testTag("ogretmen_soru_cevap_girdi"),
                            enabled = !kaydediliyor,
                            minLines = 2,
                            label = { Text(stringResource(R.string.teacher_question_answer_label)) },
                            shape = RoundedCornerShape(14.dp),
                        )
                        OutlinedTextField(
                            value = baslik,
                            onValueChange = { baslik = it },
                            modifier = Modifier.fillMaxWidth().testTag("ogretmen_soru_baslik_girdi"),
                            enabled = !kaydediliyor,
                            singleLine = true,
                            label = { Text(stringResource(R.string.teacher_question_title_label)) },
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        )
                    }
                }
            }
            if (klavyeAcik) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        formHatasi?.let { hataId ->
                            Text(
                                text = stringResource(hataId),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PremiumTextButton(
                                onClick = { focusManager.clearFocus() },
                                modifier = Modifier.heightIn(min = 48.dp),
                            ) {
                                Text(stringResource(R.string.common_hide_keyboard))
                            }
                            PremiumButton(
                                onClick = ::soruyuKaydet,
                                enabled = !kaydediliyor && !kirpmaIsleniyor,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("ogretmen_soru_kaydet"),
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Text(stringResource(R.string.teacher_question_save), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    kirpmaHedefi?.let { target ->
        val file = gorselDeposu.dosyaGoruntuIcin(target)
        if (file != null) {
            GorselKirpmaDialog(
                file = file,
                isSaving = kirpmaIsleniyor,
                onDismiss = { if (!kirpmaIsleniyor) kirpmaHedefi = null },
                onApply = { area: GorselKirpmaAlani ->
                    if (kirpmaIsleniyor) return@GorselKirpmaDialog
                    kirpmaIsleniyor = true
                    scope.launch {
                        var output: SoruGorselGirdisi? = null
                        var replaced = false
                        try {
                            val cropped = gorselDeposu.kirp(target, area.sol, area.ust, area.sag, area.alt)
                            output = cropped
                            val idx = gorseller.indexOfFirst { it.id == target.id }
                            if (idx >= 0) {
                                gorseller = gorseller.toMutableList().apply {
                                    this[idx] = cropped.copy(sira = idx)
                                }
                                replaced = true
                            }
                            kirpmaHedefi = null
                            if (replaced && target.gecici && cropped.id != target.id) {
                                runCatching { gorselDeposu.geciciDosyayiSil(target.dosyaAdi) }
                            }
                        } catch (cancel: CancellationException) {
                            throw cancel
                        } catch (_: Throwable) {
                            snackbarHostState.showSnackbar(context.getString(R.string.question_form_crop_error))
                        } finally {
                            if (!replaced) {
                                output?.takeIf { it.gecici && it.id != target.id }?.let {
                                    runCatching { gorselDeposu.geciciDosyayiSil(it.dosyaAdi) }
                                }
                            }
                            kirpmaIsleniyor = false
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun OgretmenAyarlarEkrani(
    veri: OgretmenVerisi,
    sonYedek: SonYedekBilgisi?,
    yedeklemeIslemde: Boolean,
    onOgrenciModunaGec: () -> Unit,
    onYedekOlustur: () -> Unit,
    onYedekGeriYukle: () -> Unit,
) {
    val context = LocalContext.current
    val sesEfektleri = LocalUygulamaSesleri.current
    var sesAcik by remember { mutableStateOf(sesEfektleri?.acikMi() ?: false) }

    val sonYedekMetni = sonYedek?.let { backup ->
        val tarih = SimpleDateFormat("d MMM yyyy HH:mm", Locale.forLanguageTag("tr-TR"))
            .format(Date(backup.olusturmaZamani))
        val boyut = Formatter.formatShortFileSize(context, backup.boyutBayt)
        context.getString(
            R.string.backup_last_info,
            backup.dosyaAdi,
            tarih,
            boyut,
            context.getString(if (backup.guvenlikKopyasi) R.string.backup_safety_copy else R.string.backup_external_copy),
        )
    } ?: stringResource(R.string.backup_last_none)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { insets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .consumeWindowInsets(insets),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                GradyanKart(modifier = Modifier.fillMaxWidth(), borderAnimation = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.settings_kicker),
                            color = PremiumMarkaRenkler.BakirIsik,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.settings_title),
                            color = PremiumMarkaRenkler.Kagit,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.settings_role_current_teacher),
                            color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.86f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            item {
                CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.settings_role_section_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.settings_role_switch_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        PremiumOutlinedButton(
                            onClick = onOgrenciModunaGec,
                            enabled = !yedeklemeIslemde,
                            modifier = Modifier.fillMaxWidth().testTag("ogrenci_moduna_gec_buton"),
                        ) {
                            Text(stringResource(R.string.settings_switch_to_student))
                        }
                    }
                }
            }
            item {
                CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.settings_backup_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(
                                R.string.teacher_settings_workspace_summary,
                                veri.gruplar.size,
                                veri.sorular.size,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = sonYedekMetni,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        PremiumGradyanButon(
                            onClick = onYedekOlustur,
                            enabled = !yedeklemeIslemde,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.menu_create_backup), fontWeight = FontWeight.Bold)
                        }
                        PremiumOutlinedButton(
                            onClick = onYedekGeriYukle,
                            enabled = !yedeklemeIslemde,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.menu_restore_backup))
                        }
                    }
                }
            }
            item {
                CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.settings_sound_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        PremiumSwitch(
                            checked = sesAcik,
                            onCheckedChange = { yeni ->
                                sesAcik = yeni
                                sesEfektleri?.ayarla(yeni)
                            },
                        )
                    }
                }
            }
            item {
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun grupMonogram(ad: String): String {
    val temiz = ad.trim()
    if (temiz.isEmpty()) return "GR"
    val parcalar = temiz.split(Regex("[\\s()\\-/]+")).filter { it.isNotEmpty() }
    if (parcalar.size == 1 && temiz.length <= 3) {
        return temiz.uppercase(Locale.forLanguageTag("tr-TR"))
    }
    if (parcalar.size >= 2) {
        val ilk = parcalar[0]
        if (ilk.length <= 2 && ilk.any { it.isDigit() }) {
            return ilk.uppercase(Locale.forLanguageTag("tr-TR"))
        }
        return (parcalar[0].take(1) + parcalar[1].take(1)).uppercase(Locale.forLanguageTag("tr-TR"))
    }
    return temiz.take(2).uppercase(Locale.forLanguageTag("tr-TR"))
}

private fun grupVurguRengi(ad: String, koyuTema: Boolean): Color {
    val acikPalet = listOf(
        PremiumMarkaRenkler.Petrol,
        PremiumMarkaRenkler.Bakir,
        PremiumMarkaRenkler.Deniz,
        PremiumMarkaRenkler.Yesil,
        Color(0xFF5C6BC0),
        Color(0xFF7B5EA7),
    )
    val koyuPalet = listOf(
        PremiumMarkaRenkler.DenizAcik,
        PremiumMarkaRenkler.BakirIsik,
        PremiumMarkaRenkler.PetrolParlak,
        PremiumMarkaRenkler.YesilAcik,
        Color(0xFF9FA8DA),
        PremiumMarkaRenkler.MorAcik,
    )
    val palet = if (koyuTema) koyuPalet else acikPalet
    val indeks = abs(ad.trim().lowercase(Locale.forLanguageTag("tr-TR")).hashCode()) % palet.size
    return palet[indeks]
}

private fun zorlukRengi(zorluk: OgretmenZorluk, koyuTema: Boolean): Color = when (zorluk) {
    OgretmenZorluk.KOLAY -> if (koyuTema) PremiumMarkaRenkler.YesilAcik else PremiumMarkaRenkler.Yesil
    OgretmenZorluk.ORTA -> if (koyuTema) PremiumMarkaRenkler.BakirIsik else PremiumMarkaRenkler.Bakir
    OgretmenZorluk.ZOR -> if (koyuTema) PremiumMarkaRenkler.MercanAcik else Color(0xFFC62828)
}

@androidx.annotation.StringRes
private fun zorlukEtiketi(zorluk: OgretmenZorluk): Int = when (zorluk) {
    OgretmenZorluk.KOLAY -> R.string.teacher_difficulty_easy
    OgretmenZorluk.ORTA -> R.string.teacher_difficulty_medium
    OgretmenZorluk.ZOR -> R.string.teacher_difficulty_hard
}

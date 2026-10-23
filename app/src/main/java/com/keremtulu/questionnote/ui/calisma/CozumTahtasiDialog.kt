package com.keremtulu.questionnote.ui.calisma

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.SoruGorseli
import com.keremtulu.questionnote.ui.bilesenler.SoruGorselResmi
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.tema.HareketTuru
import com.keremtulu.questionnote.ui.tema.LocalHareketPolitikasi
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private enum class TahtaAraci { KALEM, CIZGI, DIKDORTGEN, UCGEN, SILGI }

private data class TahtaNoktasi(val x: Float, val y: Float)

private data class TahtaVurusu(
    val araci: TahtaAraci,
    val noktalar: List<TahtaNoktasi>,
    val renk: Color,
    val kalinlikDp: Float,
)

/** Drawing history belongs to one surface: the blank board or one specific question image. */
private data class TahtaYuzeyCizimi(
    val kalemler: List<TahtaVurusu> = emptyList(),
    val geriAlinanlar: List<TahtaVurusu> = emptyList(),
)

private data class TahtaRengi(val ad: Int, val renk: Color)

private val TahtaKalemRenkleri = listOf(
    TahtaRengi(R.string.whiteboard_color_black, Color(0xFF17212A)),
    TahtaRengi(R.string.whiteboard_color_red, Color(0xFFC62828)),
    TahtaRengi(R.string.whiteboard_color_blue, Color(0xFF1565C0)),
    TahtaRengi(R.string.whiteboard_color_green, Color(0xFF2E7D32)),
    TahtaRengi(R.string.whiteboard_color_purple, Color(0xFF6A1B9A)),
    TahtaRengi(R.string.whiteboard_color_orange, Color(0xFFB45309)),
)

/** A temporary, orientation-aware solution surface. It never paints on or replaces the source image. */
@Composable
fun CozumTahtasiDialog(
    soru: Soru,
    gorselDeposu: LocalSoruGorselDeposu,
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    val cizimKatmanlari = remember(soru.id) { mutableStateMapOf<String, TahtaYuzeyCizimi>() }
    val soruGorselleri = remember(soru.id, soru.gorseller) {
        soru.gorseller.filter { it.tur == GorselTuru.SORU }
    }
    var seciliGorselId by remember(soru.id, soru.gorseller) {
        mutableStateOf(soruGorselleri.firstOrNull()?.id)
    }
    val seciliGorsel = soruGorselleri.firstOrNull { it.id == seciliGorselId } ?: soruGorselleri.firstOrNull()
    var etkinVurus by remember(soru.id) { mutableStateOf<TahtaVurusu?>(null) }
    var arac by remember(soru.id) { mutableStateOf(TahtaAraci.KALEM) }
    var renk by remember(soru.id) { mutableStateOf(TahtaKalemRenkleri.first().renk) }
    var kalinlik by remember(soru.id) { mutableStateOf(4f) }
    var temizlemeOnayi by remember(soru.id) { mutableStateOf(false) }
    var cizimSiniriUyarisi by remember(soru.id) { mutableStateOf(false) }
    var soruUzerindeCizim by remember(soru.id) { mutableStateOf(false) }
    var soruPaneliAcik by remember(soru.id) { mutableStateOf(false) }
    if (!visible) return

    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val hareket = LocalHareketPolitikasi.current
    val hareketSuresi = hareket.sure(HareketTuru.GIRIS)
    val panelGirisi = if (hareket.azaltildi) EnterTransition.None else {
        slideInVertically(animationSpec = tween(hareketSuresi)) { it } + fadeIn(tween(hareketSuresi))
    }
    val panelCikisi = if (hareket.azaltildi) ExitTransition.None else {
        slideOutVertically(animationSpec = tween(hareketSuresi)) { it } + fadeOut(tween(hareketSuresi))
    }
    val perdeGirisi = if (hareket.azaltildi) EnterTransition.None else fadeIn(tween(hareketSuresi))
    val perdeCikisi = if (hareket.azaltildi) ExitTransition.None else fadeOut(tween(hareketSuresi))
    val seciliGorselDosyasi = seciliGorsel?.let { gorselDeposu.dosyaGoruntuIcin(it.dosyaAdi) }
    val soruZeminiKullanilabilir = seciliGorsel != null && seciliGorselDosyasi != null
    val etkinKatmanAnahtari = if (soruUzerindeCizim && seciliGorsel != null) {
        "soru:${seciliGorsel.id}"
    } else {
        BOS_TAHTA_KATMANI
    }
    val etkinKatman = cizimKatmanlari[etkinKatmanAnahtari] ?: TahtaYuzeyCizimi()

    fun soruGorseliniSec(gorsel: SoruGorseli) {
        seciliGorselId = gorsel.id
        soruUzerindeCizim = true
        soruPaneliAcik = false
        etkinVurus = null
        cizimSiniriUyarisi = false
    }

    fun cizimZemininiDegistir() {
        if (!soruUzerindeCizim && !soruZeminiKullanilabilir) return
        soruUzerindeCizim = !soruUzerindeCizim
        etkinVurus = null
        cizimSiniriUyarisi = false
    }

    fun vurusBitti(yeniVurus: TahtaVurusu?) {
        etkinVurus = null
        if (yeniVurus == null) return
        val katman = cizimKatmanlari[etkinKatmanAnahtari] ?: TahtaYuzeyCizimi()
        if (katman.kalemler.size >= MAX_TAHTA_VURUSU) {
            cizimSiniriUyarisi = true
            return
        }
        cizimKatmanlari[etkinKatmanAnahtari] = katman.copy(
            kalemler = katman.kalemler + yeniVurus,
            geriAlinanlar = emptyList(),
        )
        cizimSiniriUyarisi = false
    }

    fun geriAl() {
        val sonVurus = etkinKatman.kalemler.lastOrNull() ?: return
        cizimKatmanlari[etkinKatmanAnahtari] = etkinKatman.copy(
            kalemler = etkinKatman.kalemler.dropLast(1),
            geriAlinanlar = etkinKatman.geriAlinanlar + sonVurus,
        )
    }

    fun yinele() {
        val sonVurus = etkinKatman.geriAlinanlar.lastOrNull() ?: return
        cizimKatmanlari[etkinKatmanAnahtari] = etkinKatman.copy(
            kalemler = etkinKatman.kalemler + sonVurus,
            geriAlinanlar = etkinKatman.geriAlinanlar.dropLast(1),
        )
    }

    val temizlenebilir = cizimKatmanlari.values.any {
        it.kalemler.isNotEmpty() || it.geriAlinanlar.isNotEmpty()
    } || etkinVurus != null

    androidx.compose.ui.window.Dialog(
        onDismissRequest = {
            if (soruPaneliAcik) soruPaneliAcik = false else onDismiss()
        },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            color = colors.background,
            shape = RoundedCornerShape(30.dp),
            tonalElevation = 3.dp,
            border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.62f)),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TahtaCalismaBasligi(
                    soruPaneliAcik = soruPaneliAcik,
                    onSoruPaneliTikla = { soruPaneliAcik = !soruPaneliAcik },
                    onKapat = {
                        soruPaneliAcik = false
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                    val landscape = maxWidth > maxHeight && maxWidth >= 480.dp
                    val compactControls = maxWidth >= 760.dp
                    if (landscape) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            SoruOnizlemesi(
                                soru = soru,
                                gorseller = soruGorselleri,
                                seciliGorselId = seciliGorsel?.id,
                                gorselDeposu = gorselDeposu,
                                onGorselSec = ::soruGorseliniSec,
                                modifier = Modifier.weight(0.34f).fillMaxHeight(),
                            )
                            TahtaCalismaAlani(
                                kalemler = etkinKatman.kalemler,
                                geriAlinanlar = etkinKatman.geriAlinanlar,
                                etkinVurus = etkinVurus,
                                arac = arac,
                                renk = renk,
                                kalinlik = kalinlik,
                                compact = true,
                                brushTekSatir = compactControls,
                                gorselDeposu = gorselDeposu,
                                soruGorseli = seciliGorsel,
                                soruUzerindeCizim = soruUzerindeCizim,
                                soruZeminiKullanilabilir = soruZeminiKullanilabilir,
                                cizimSiniriUyarisi = cizimSiniriUyarisi,
                                temizlenebilir = temizlenebilir,
                                onYuzeyDegistir = ::cizimZemininiDegistir,
                                onAracSec = { arac = it },
                                onRenkSec = { renk = it },
                                onKalinlikSec = { kalinlik = it },
                                onVurusDegisti = { etkinVurus = it },
                                onVurusBitti = ::vurusBitti,
                                onGeriAl = ::geriAl,
                                onYinele = ::yinele,
                                onTemizle = { temizlemeOnayi = true },
                                modifier = Modifier.weight(0.66f).fillMaxHeight(),
                            )
                        }
                    } else {
                        TahtaCalismaAlani(
                            kalemler = etkinKatman.kalemler,
                            geriAlinanlar = etkinKatman.geriAlinanlar,
                            etkinVurus = etkinVurus,
                            arac = arac,
                            renk = renk,
                            kalinlik = kalinlik,
                            compact = false,
                            gorselDeposu = gorselDeposu,
                            soruGorseli = seciliGorsel,
                            soruUzerindeCizim = soruUzerindeCizim,
                            soruZeminiKullanilabilir = soruZeminiKullanilabilir,
                            cizimSiniriUyarisi = cizimSiniriUyarisi,
                            temizlenebilir = temizlenebilir,
                            onYuzeyDegistir = ::cizimZemininiDegistir,
                            onAracSec = { arac = it },
                            onRenkSec = { renk = it },
                            onKalinlikSec = { kalinlik = it },
                            onVurusDegisti = { etkinVurus = it },
                            onVurusBitti = ::vurusBitti,
                            onGeriAl = ::geriAl,
                            onYinele = ::yinele,
                            onTemizle = { temizlemeOnayi = true },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    SoruPaneliAnimasyonu(
                        visible = soruPaneliAcik,
                        modifier = Modifier.matchParentSize(),
                        enter = perdeGirisi,
                        exit = perdeCikisi,
                    ) {
                        Box(
                            Modifier.fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.34f))
                                .clickable(role = Role.Button, onClick = { soruPaneliAcik = false })
                                .semantics { contentDescription = context.getString(R.string.common_close) }
                                .testTag("whiteboard-question-scrim"),
                        )
                    }
                    SoruPaneliAnimasyonu(
                        visible = soruPaneliAcik,
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .fillMaxHeight(0.78f),
                        enter = panelGirisi,
                        exit = panelCikisi,
                    ) {
                        SoruAltPaneli(
                            soru = soru,
                            gorseller = soruGorselleri,
                            seciliGorselId = seciliGorsel?.id,
                            gorselDeposu = gorselDeposu,
                            onGorselSec = ::soruGorseliniSec,
                            onKapat = { soruPaneliAcik = false },
                            modifier = Modifier.fillMaxSize().testTag("whiteboard-question-sheet"),
                        )
                    }
                }
            }
        }
    }

    if (temizlemeOnayi) {
        AlertDialog(
            onDismissRequest = { temizlemeOnayi = false },
            title = { Text(stringResource(R.string.whiteboard_clear_title)) },
            text = { Text(stringResource(R.string.whiteboard_clear_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    cizimKatmanlari.clear()
                    etkinVurus = null
                    temizlemeOnayi = false
                    cizimSiniriUyarisi = false
                }) { Text(stringResource(R.string.whiteboard_clear_action)) }
            },
            dismissButton = {
                TextButton(onClick = { temizlemeOnayi = false }) { Text(stringResource(R.string.question_form_cancel)) }
            },
        )
    }
}

@Composable
private fun TahtaCalismaBasligi(
    soruPaneliAcik: Boolean,
    onSoruPaneliTikla: () -> Unit,
    onKapat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)
    val panelButtonLabel = stringResource(
        if (soruPaneliAcik) R.string.common_close else R.string.question_form_question_images,
    )
    val closeLabel = stringResource(R.string.whiteboard_close)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(PremiumMarkaRenkler.MurekkepKoyu, PremiumMarkaRenkler.MurekkepAcik)))
            .border(1.dp, PremiumMarkaRenkler.BakirIsik.copy(alpha = 0.34f), shape),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PremiumMarkaRenkler.DenizAcik.copy(alpha = 0.16f),
            ) {
                Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                    TahtaAraciIkonu(TahtaAraci.KALEM, PremiumMarkaRenkler.DenizAcik, Modifier.size(24.dp))
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    stringResource(R.string.whiteboard_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(R.string.whiteboard_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.76f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(
                onClick = onSoruPaneliTikla,
                modifier = Modifier.size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .testTag("whiteboard-question-preview-toggle")
                    .semantics { contentDescription = panelButtonLabel },
            ) {
                SoruPaneliDugmeIkonu(acik = soruPaneliAcik, color = Color.White)
            }
            IconButton(
                onClick = onKapat,
                modifier = Modifier.size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .testTag("whiteboard-close")
                    .semantics { contentDescription = closeLabel },
            ) {
                TahtaKapatIkonu(Color.White)
            }
        }
    }
}

@Composable
private fun TahtaKapatIkonu(color: Color) {
    Canvas(Modifier.size(20.dp)) {
        val inset = size.minDimension * 0.24f
        val stroke = 2.dp.toPx()
        drawLine(color, Offset(inset, inset), Offset(size.width - inset, size.height - inset), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width - inset, inset), Offset(inset, size.height - inset), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun SoruPaneliAnimasyonu(
    visible: Boolean,
    modifier: Modifier,
    enter: EnterTransition,
    exit: ExitTransition,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit,
    ) {
        content()
    }
}

@Composable
private fun SoruPaneliDugmeIkonu(
    acik: Boolean,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Canvas(Modifier.size(22.dp)) {
        val stroke = 1.8.dp.toPx()
        val center = Offset(size.width * 0.4f, size.height * 0.4f)
        val radius = size.minDimension * 0.23f
        drawCircle(color = color, radius = radius, center = center, style = Stroke(width = stroke))
        drawLine(
            color = color,
            start = Offset(center.x + radius * 0.7f, center.y + radius * 0.7f),
            end = Offset(size.width * 0.9f, size.height * 0.9f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(center.x - radius * 0.45f, center.y),
            end = Offset(center.x + radius * 0.45f, center.y),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        if (!acik) {
            drawLine(
                color = color,
                start = Offset(center.x, center.y - radius * 0.45f),
                end = Offset(center.x, center.y + radius * 0.45f),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun SoruOnizlemesi(
    soru: Soru,
    gorseller: List<SoruGorseli>,
    seciliGorselId: String?,
    gorselDeposu: LocalSoruGorselDeposu,
    onGorselSec: (SoruGorseli) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        color = colors.surface,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.72f)),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                stringResource(R.string.whiteboard_question_section),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            soru.soruNotu?.takeIf(String::isNotBlank)?.let { note ->
                Text(
                    note,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val source = listOfNotNull(soru.kaynak, soru.kaynakKonumu)
                .filter(String::isNotBlank)
                .joinToString(" · ")
            if (source.isNotBlank()) {
                Text(
                    source,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (gorseller.isEmpty()) {
                Text(
                    stringResource(R.string.whiteboard_question_missing),
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.error,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp),
                ) {
                    items(gorseller, key = SoruGorseli::id) { image ->
                        SoruOnizlemeGorseli(
                            image = image,
                            gorselDeposu = gorselDeposu,
                            selected = image.id == seciliGorselId,
                            onSelect = { onGorselSec(image) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SoruAltPaneli(
    soru: Soru,
    gorseller: List<SoruGorseli>,
    seciliGorselId: String?,
    gorselDeposu: LocalSoruGorselDeposu,
    onGorselSec: (SoruGorseli) -> Unit,
    onKapat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = colors.surface,
        tonalElevation = 14.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(Modifier.fillMaxWidth().height(16.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(width = 38.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(colors.outlineVariant),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f).padding(end = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        stringResource(R.string.whiteboard_question_section),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    soru.soruNotu?.takeIf(String::isNotBlank)?.let { note ->
                        Text(
                            note,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    val source = listOfNotNull(soru.kaynak, soru.kaynakKonumu)
                        .filter(String::isNotBlank)
                        .joinToString(" · ")
                    if (source.isNotBlank()) {
                        Text(
                            source,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                TextButton(onClick = onKapat, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.common_close))
                }
            }
            if (gorseller.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(R.string.whiteboard_question_missing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.error,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f).testTag("whiteboard-question-pages"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 12.dp),
                ) {
                    items(gorseller, key = SoruGorseli::id) { image ->
                        SoruOnizlemeGorseli(
                            image = image,
                            gorselDeposu = gorselDeposu,
                            selected = image.id == seciliGorselId,
                            onSelect = { onGorselSec(image) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SoruOnizlemeGorseli(
    image: SoruGorseli,
    gorselDeposu: LocalSoruGorselDeposu,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val actionLabel = stringResource(R.string.whiteboard_surface_switch_to_question)
    val ratio = (image.genislik.toFloat() / image.yukseklik.coerceAtLeast(1)).coerceIn(0.25f, 4f)
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("whiteboard-question-image-${image.id}")
            .clip(shape)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colors.primary else colors.outlineVariant,
                shape = shape,
            )
            .clickable(role = Role.Button, onClick = onSelect)
            .semantics {
                contentDescription = actionLabel
                this.selected = selected
            },
    ) {
        SoruGorselResmi(
            file = gorselDeposu.dosyaGoruntuIcin(image.dosyaAdi),
            contentDescription = actionLabel,
            modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp).aspectRatio(ratio),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            maxDecodeEdge = 1_800,
        )
    }
}

@Composable
private fun TahtaYuzeySecici(
    soruUzerindeCizim: Boolean,
    soruZeminiKullanilabilir: Boolean,
    kompakt: Boolean,
    arac: TahtaAraci,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val title = stringResource(if (soruUzerindeCizim) R.string.whiteboard_surface_question else R.string.whiteboard_surface_blank)
    val action = stringResource(
        if (soruUzerindeCizim) R.string.whiteboard_surface_switch_to_blank else R.string.whiteboard_surface_switch_to_question,
    )
    val enabled = soruUzerindeCizim || soruZeminiKullanilabilir
    Surface(
        modifier = Modifier.fillMaxWidth()
            .testTag("whiteboard-surface-toggle")
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = action
                stateDescription = title
            },
        shape = RoundedCornerShape(if (kompakt) 15.dp else 18.dp),
        color = if (soruUzerindeCizim) colors.primaryContainer else colors.surface,
        border = BorderStroke(
            1.dp,
            if (soruUzerindeCizim) colors.primary.copy(alpha = 0.28f) else colors.outlineVariant.copy(alpha = 0.72f),
        ),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .heightIn(min = if (kompakt) 42.dp else 54.dp)
                .padding(horizontal = 12.dp, vertical = if (kompakt) 6.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier.size(if (kompakt) 28.dp else 34.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                TahtaYuzeyIkonu(soruUzerindeCizim, colors.primary, Modifier.size(18.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(
                    if (kompakt) stringResource(arac.stringResource()) else action,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TahtaYuzeyGecisIkonu(soruUzerindeCizim, colors.primary)
        }
    }
}

@Composable
private fun TahtaYuzeyIkonu(soruUzerinde: Boolean, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val stroke = 1.7.dp.toPx()
        val left = size.width * 0.16f
        val top = size.height * 0.12f
        val width = size.width * 0.68f
        val height = size.height * 0.76f
        drawRoundRect(
            color = color,
            topLeft = Offset(left, top),
            size = Size(width, height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),
            style = Stroke(width = stroke),
        )
        if (soruUzerinde) {
            drawLine(color, Offset(left + width * 0.22f, top + height * 0.28f), Offset(left + width * 0.78f, top + height * 0.28f), strokeWidth = stroke)
            drawLine(color, Offset(left + width * 0.22f, top + height * 0.52f), Offset(left + width * 0.62f, top + height * 0.52f), strokeWidth = stroke)
        } else {
            drawLine(color, Offset(left + width * 0.18f, top + height * 0.70f), Offset(left + width * 0.82f, top + height * 0.70f), strokeWidth = stroke)
        }
    }
}

@Composable
private fun TahtaYuzeyGecisIkonu(soruUzerinde: Boolean, color: Color) {
    Canvas(Modifier.size(width = 22.dp, height = 18.dp)) {
        val stroke = 1.8.dp.toPx()
        val y = size.height / 2f
        if (soruUzerinde) {
            drawLine(color, Offset(size.width * 0.82f, y), Offset(size.width * 0.18f, y), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color, Offset(size.width * 0.18f, y), Offset(size.width * 0.42f, y - size.height * 0.28f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color, Offset(size.width * 0.18f, y), Offset(size.width * 0.42f, y + size.height * 0.28f), strokeWidth = stroke, cap = StrokeCap.Round)
        } else {
            drawLine(color, Offset(size.width * 0.18f, y), Offset(size.width * 0.82f, y), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color, Offset(size.width * 0.82f, y), Offset(size.width * 0.58f, y - size.height * 0.28f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color, Offset(size.width * 0.82f, y), Offset(size.width * 0.58f, y + size.height * 0.28f), strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun TahtaAracPaleti(
    seciliArac: TahtaAraci,
    soruUzerindeCizim: Boolean,
    soruZeminiKullanilabilir: Boolean,
    kompakt: Boolean,
    onYuzeyDegistir: () -> Unit,
    onAracSec: (TahtaAraci) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("whiteboard-tool-palette"),
        shape = RoundedCornerShape(21.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.72f)),
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(if (kompakt) 4.dp else 6.dp),
            horizontalArrangement = Arrangement.spacedBy(if (kompakt) 2.dp else 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (kompakt) {
                TahtaKompaktYuzeyDugmesi(
                    soruUzerindeCizim = soruUzerindeCizim,
                    soruZeminiKullanilabilir = soruZeminiKullanilabilir,
                    onClick = onYuzeyDegistir,
                )
            }
            TahtaAraci.entries.forEach { tool ->
                TahtaAraciDugmesi(
                    arac = tool,
                    secili = tool == seciliArac,
                    kompakt = kompakt,
                    onClick = { onAracSec(tool) },
                )
            }
        }
    }
}

@Composable
private fun TahtaKompaktYuzeyDugmesi(
    soruUzerindeCizim: Boolean,
    soruZeminiKullanilabilir: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val title = stringResource(if (soruUzerindeCizim) R.string.whiteboard_surface_question else R.string.whiteboard_surface_blank)
    val action = stringResource(
        if (soruUzerindeCizim) R.string.whiteboard_surface_switch_to_blank else R.string.whiteboard_surface_switch_to_question,
    )
    val enabled = soruUzerindeCizim || soruZeminiKullanilabilir
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier.size(48.dp)
            .testTag("whiteboard-surface-toggle")
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = action
                stateDescription = title
            },
        shape = shape,
        color = if (soruUzerindeCizim) colors.primaryContainer else colors.surfaceVariant.copy(alpha = 0.56f),
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.65f)),
        tonalElevation = 1.dp,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            TahtaYuzeyIkonu(soruUzerindeCizim, colors.primary, Modifier.size(20.dp))
        }
    }
}

@Composable
private fun TahtaAraciDugmesi(
    arac: TahtaAraci,
    secili: Boolean,
    kompakt: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val label = stringResource(arac.stringResource())
    val shape = RoundedCornerShape(if (kompakt) 14.dp else 16.dp)
    val buttonModifier = Modifier.width(
        when {
            !kompakt -> 60.dp
            secili -> 92.dp
            else -> 44.dp
        },
    ).height(if (kompakt) 44.dp else 62.dp)
        .clip(shape)
        .background(if (secili) colors.primaryContainer else Color.Transparent)
        .border(
            1.dp,
            if (secili) colors.primary.copy(alpha = 0.26f) else colors.outlineVariant.copy(alpha = 0.24f),
            shape,
        )
        .clickable(role = Role.RadioButton, onClick = onClick)
        .semantics {
            contentDescription = label
            selected = secili
            role = Role.RadioButton
        }
        .testTag("whiteboard-tool-${arac.name.lowercase()}")
    if (kompakt && secili) {
        Row(
            modifier = buttonModifier.padding(horizontal = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TahtaAraciIkonu(
                arac = arac,
                color = colors.onPrimaryContainer,
                modifier = Modifier.size(20.dp),
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    } else {
        Column(
            modifier = buttonModifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TahtaAraciIkonu(
                arac = arac,
                color = if (secili) colors.onPrimaryContainer else colors.onSurfaceVariant,
                modifier = Modifier.size(if (kompakt) 20.dp else 22.dp),
            )
            if (!kompakt) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (secili) colors.onPrimaryContainer else colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun TahtaAraciIkonu(arac: TahtaAraci, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val stroke = 2.dp.toPx()
        val left = size.width * 0.18f
        val right = size.width * 0.82f
        val top = size.height * 0.18f
        val bottom = size.height * 0.82f
        when (arac) {
            TahtaAraci.KALEM -> {
                val path = Path().apply {
                    moveTo(left, bottom)
                    lineTo(right * 0.78f, top * 1.18f)
                    lineTo(right, top)
                    lineTo(left * 1.34f, bottom * 0.78f)
                    close()
                }
                drawPath(path, color, style = Stroke(width = stroke, join = StrokeJoin.Round))
                drawLine(color, Offset(left * 1.18f, bottom * 0.84f), Offset(left, bottom), strokeWidth = stroke, cap = StrokeCap.Round)
            }
            TahtaAraci.CIZGI -> drawLine(
                color,
                Offset(left, bottom),
                Offset(right, top),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            TahtaAraci.DIKDORTGEN -> drawRoundRect(
                color,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
                style = Stroke(width = stroke),
            )
            TahtaAraci.UCGEN -> {
                val path = Path().apply {
                    moveTo(size.width * 0.5f, top)
                    lineTo(right, bottom)
                    lineTo(left, bottom)
                    close()
                }
                drawPath(path, color, style = Stroke(width = stroke, join = StrokeJoin.Round))
            }
            TahtaAraci.SILGI -> {
                val path = Path().apply {
                    moveTo(size.width * 0.34f, top)
                    lineTo(right, size.height * 0.50f)
                    lineTo(size.width * 0.66f, bottom)
                    lineTo(left, size.height * 0.50f)
                    close()
                }
                drawPath(path, color, style = Stroke(width = stroke, join = StrokeJoin.Round))
                drawLine(color, Offset(size.width * 0.50f, size.height * 0.27f), Offset(size.width * 0.80f, size.height * 0.62f), strokeWidth = stroke)
            }
        }
    }
}

@Composable
private fun TahtaCizimStili(
    seciliRenk: Color,
    kalinlik: Float,
    tekSatir: Boolean,
    kompakt: Boolean,
    onRenkSec: (Color) -> Unit,
    onKalinlikSec: (Float) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("whiteboard-style-dock"),
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.68f)),
        tonalElevation = 1.dp,
    ) {
        if (tekSatir) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(stringResource(R.string.whiteboard_color_label), style = MaterialTheme.typography.labelSmall)
                TahtaKalemRenkleri.forEach { item ->
                    TahtaRenkSecimi(item, selected = seciliRenk == item.renk, targetSize = 40.dp) {
                        onRenkSec(item.renk)
                    }
                }
                Spacer(Modifier.padding(horizontal = 3.dp).width(1.dp).height(23.dp).background(colors.outlineVariant))
                Text(
                    stringResource(R.string.whiteboard_width_label),
                    modifier = Modifier.width(58.dp),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                kalinlikSecenekleri().forEach { (width, label) ->
                    TahtaKalinlikSecimi(width, label, selected = kalinlik == width, width = 34.dp) {
                        onKalinlikSec(width)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(Modifier.fillMaxWidth().height(38.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.whiteboard_color_label),
                        modifier = Modifier.width(if (kompakt) 62.dp else 52.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                    )
                    TahtaKalemRenkleri.forEach { item ->
                        TahtaRenkSecimi(item, selected = seciliRenk == item.renk, targetSize = if (kompakt) 38.dp else 44.dp) {
                            onRenkSec(item.renk)
                        }
                    }
                }
                Spacer(Modifier.fillMaxWidth().height(1.dp).background(colors.outlineVariant.copy(alpha = 0.45f)))
                Row(Modifier.fillMaxWidth().height(38.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.whiteboard_width_label),
                        modifier = Modifier.width(if (kompakt) 62.dp else 52.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    kalinlikSecenekleri().forEach { (width, label) ->
                        TahtaKalinlikSecimi(width, label, selected = kalinlik == width, width = 48.dp) {
                            onKalinlikSec(width)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TahtaKompaktKontrolCubugu(
    seciliRenk: Color,
    kalinlik: Float,
    geriAlEtkin: Boolean,
    yineleEtkin: Boolean,
    temizleEtkin: Boolean,
    onRenkSec: (Color) -> Unit,
    onKalinlikSec: (Float) -> Unit,
    onGeriAl: () -> Unit,
    onYinele: () -> Unit,
    onTemizle: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("whiteboard-style-dock"),
        shape = RoundedCornerShape(17.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.68f)),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            TahtaKalemRenkleri.forEach { item ->
                TahtaRenkSecimi(item, selected = seciliRenk == item.renk, targetSize = 36.dp) {
                    onRenkSec(item.renk)
                }
            }
            Spacer(
                Modifier.padding(horizontal = 2.dp).width(1.dp).height(22.dp)
                    .background(colors.outlineVariant),
            )
            kalinlikSecenekleri().forEach { (width, label) ->
                TahtaKalinlikSecimi(width, label, selected = kalinlik == width, width = 31.dp) {
                    onKalinlikSec(width)
                }
            }
            Spacer(
                Modifier.padding(horizontal = 2.dp).width(1.dp).height(22.dp)
                    .background(colors.outlineVariant),
            )
            Row(
                modifier = Modifier.testTag("whiteboard-actions"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                TahtaIslemDugmesi(R.string.whiteboard_undo, TahtaIslemi.GERI_AL, geriAlEtkin, true, onGeriAl)
                TahtaIslemDugmesi(R.string.whiteboard_redo, TahtaIslemi.YINELE, yineleEtkin, true, onYinele)
                TahtaIslemDugmesi(R.string.whiteboard_clear, TahtaIslemi.TEMIZLE, temizleEtkin, true, onTemizle)
            }
        }
    }
}

@Composable
private fun TahtaRenkSecimi(
    item: TahtaRengi,
    selected: Boolean,
    targetSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val label = stringResource(item.ad)
    val description = if (selected) stringResource(R.string.whiteboard_selected_color, label) else label
    Box(
        modifier = Modifier.size(targetSize)
            .clip(CircleShape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics {
                contentDescription = description
                this.selected = selected
                role = Role.RadioButton
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(24.dp)) {
            if (selected) drawCircle(colors.primary, style = Stroke(width = 2.dp.toPx()))
            drawCircle(item.renk, radius = size.minDimension * if (selected) 0.31f else 0.35f)
            if (!selected) drawCircle(colors.outline.copy(alpha = 0.55f), style = Stroke(width = 0.75.dp.toPx()))
        }
    }
}

@Composable
private fun TahtaKalinlikSecimi(
    kalinlik: Float,
    etiket: Int,
    selected: Boolean,
    width: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val description = stringResource(etiket)
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier = Modifier.width(width).height(32.dp)
            .clip(shape)
            .background(if (selected) colors.primaryContainer else Color.Transparent)
            .border(1.dp, if (selected) colors.primary.copy(alpha = 0.22f) else Color.Transparent, shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics {
                contentDescription = description
                this.selected = selected
                role = Role.RadioButton
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(width = 22.dp, height = 20.dp)) {
            drawLine(
                if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
                Offset(size.width * 0.12f, size.height * 0.5f),
                Offset(size.width * 0.88f, size.height * 0.5f),
                strokeWidth = kalinlik.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun TahtaIslemPaneli(
    geriAlEtkin: Boolean,
    yineleEtkin: Boolean,
    temizleEtkin: Boolean,
    kompakt: Boolean,
    onGeriAl: () -> Unit,
    onYinele: () -> Unit,
    onTemizle: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("whiteboard-actions"),
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.68f)),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TahtaIslemDugmesi(R.string.whiteboard_undo, TahtaIslemi.GERI_AL, geriAlEtkin, kompakt, onGeriAl)
            TahtaIslemDugmesi(R.string.whiteboard_redo, TahtaIslemi.YINELE, yineleEtkin, kompakt, onYinele)
            Spacer(Modifier.weight(1f))
            TahtaIslemDugmesi(R.string.whiteboard_clear, TahtaIslemi.TEMIZLE, temizleEtkin, kompakt, onTemizle)
        }
    }
}

private enum class TahtaIslemi { GERI_AL, YINELE, TEMIZLE }

@Composable
private fun TahtaIslemDugmesi(
    etiket: Int,
    islem: TahtaIslemi,
    etkin: Boolean,
    kompakt: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val label = stringResource(etiket)
    val shape = RoundedCornerShape(14.dp)
    val bg = when {
        !etkin -> Color.Transparent
        islem == TahtaIslemi.TEMIZLE -> colors.error.copy(alpha = 0.10f)
        else -> colors.surfaceVariant.copy(alpha = 0.66f)
    }
    Surface(
        modifier = Modifier.heightIn(min = 42.dp)
            .clip(shape)
            .clickable(enabled = etkin, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label }
            .testTag(
                when (islem) {
                    TahtaIslemi.GERI_AL -> "whiteboard-undo"
                    TahtaIslemi.YINELE -> "whiteboard-redo"
                    TahtaIslemi.TEMIZLE -> "whiteboard-clear"
                },
            ),
        shape = shape,
        color = bg,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (kompakt) 11.dp else 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            TahtaIslemIkonu(islem, if (etkin) colors.onSurface else colors.onSurfaceVariant.copy(alpha = 0.44f))
            if (!kompakt) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = if (etkin) colors.onSurface else colors.onSurfaceVariant.copy(alpha = 0.44f))
            }
        }
    }
}

@Composable
private fun TahtaIslemIkonu(islem: TahtaIslemi, color: Color) {
    Canvas(Modifier.size(18.dp)) {
        val stroke = 1.8.dp.toPx()
        val box = size.minDimension * 0.56f
        val left = (size.width - box) / 2f
        val top = (size.height - box) / 2f
        when (islem) {
            TahtaIslemi.GERI_AL, TahtaIslemi.YINELE -> {
                val reverse = islem == TahtaIslemi.GERI_AL
                val startX = if (reverse) size.width * 0.78f else size.width * 0.22f
                val endX = if (reverse) size.width * 0.22f else size.width * 0.78f
                drawArc(
                    color = color,
                    startAngle = if (reverse) 205f else -25f,
                    sweepAngle = if (reverse) 235f else -235f,
                    useCenter = false,
                    topLeft = Offset(left, top),
                    size = Size(box, box),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                val tip = Offset(endX, size.height * 0.28f)
                val direction = if (reverse) 1f else -1f
                drawLine(color, tip, Offset(tip.x + direction * size.width * 0.20f, tip.y), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color, tip, Offset(tip.x, tip.y + size.height * 0.18f), strokeWidth = stroke, cap = StrokeCap.Round)
            }
            TahtaIslemi.TEMIZLE -> {
                drawLine(color, Offset(size.width * 0.18f, size.height * 0.30f), Offset(size.width * 0.82f, size.height * 0.30f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.34f, size.height * 0.19f), Offset(size.width * 0.66f, size.height * 0.19f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawRoundRect(
                    color,
                    topLeft = Offset(size.width * 0.27f, size.height * 0.34f),
                    size = Size(size.width * 0.46f, size.height * 0.49f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
                    style = Stroke(width = stroke),
                )
                drawLine(color, Offset(size.width * 0.44f, size.height * 0.45f), Offset(size.width * 0.44f, size.height * 0.72f), strokeWidth = stroke)
                drawLine(color, Offset(size.width * 0.56f, size.height * 0.45f), Offset(size.width * 0.56f, size.height * 0.72f), strokeWidth = stroke)
            }
        }
    }
}

private fun kalinlikSecenekleri() = listOf(
    2f to R.string.whiteboard_width_thin,
    4f to R.string.whiteboard_width_normal,
    8f to R.string.whiteboard_width_thick,
)

@Composable
private fun TahtaCalismaAlani(
    kalemler: List<TahtaVurusu>,
    geriAlinanlar: List<TahtaVurusu>,
    etkinVurus: TahtaVurusu?,
    arac: TahtaAraci,
    renk: Color,
    kalinlik: Float,
    compact: Boolean,
    brushTekSatir: Boolean = compact,
    gorselDeposu: LocalSoruGorselDeposu,
    soruGorseli: SoruGorseli?,
    soruUzerindeCizim: Boolean,
    soruZeminiKullanilabilir: Boolean,
    cizimSiniriUyarisi: Boolean,
    temizlenebilir: Boolean,
    onYuzeyDegistir: () -> Unit,
    onAracSec: (TahtaAraci) -> Unit,
    onRenkSec: (Color) -> Unit,
    onKalinlikSec: (Float) -> Unit,
    onVurusDegisti: (TahtaVurusu?) -> Unit,
    onVurusBitti: (TahtaVurusu?) -> Unit,
    onGeriAl: () -> Unit,
    onYinele: () -> Unit,
    onTemizle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val mergedCompactControls = compact && brushTekSatir
    Column(modifier, verticalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 8.dp)) {
        if (!compact) {
            TahtaYuzeySecici(
                soruUzerindeCizim = soruUzerindeCizim,
                soruZeminiKullanilabilir = soruZeminiKullanilabilir,
                kompakt = false,
                arac = arac,
                onClick = onYuzeyDegistir,
            )
        }
        TahtaAracPaleti(
            seciliArac = arac,
            soruUzerindeCizim = soruUzerindeCizim,
            soruZeminiKullanilabilir = soruZeminiKullanilabilir,
            kompakt = compact,
            onYuzeyDegistir = onYuzeyDegistir,
            onAracSec = onAracSec,
        )
        if (mergedCompactControls) {
            TahtaKompaktKontrolCubugu(
                seciliRenk = renk,
                kalinlik = kalinlik,
                geriAlEtkin = kalemler.isNotEmpty(),
                yineleEtkin = geriAlinanlar.isNotEmpty(),
                temizleEtkin = temizlenebilir,
                onRenkSec = onRenkSec,
                onKalinlikSec = onKalinlikSec,
                onGeriAl = onGeriAl,
                onYinele = onYinele,
                onTemizle = onTemizle,
            )
        } else {
            TahtaCizimStili(
                seciliRenk = renk,
                kalinlik = kalinlik,
                tekSatir = brushTekSatir,
                kompakt = compact,
                onRenkSec = onRenkSec,
                onKalinlikSec = onKalinlikSec,
            )
        }

        val countDescription = if (kalemler.isEmpty()) {
            stringResource(R.string.whiteboard_no_marks)
        } else {
            context.getString(R.string.whiteboard_mark_count, kalemler.size)
        }
        val backgroundFile = if (soruUzerindeCizim) {
            soruGorseli?.let { gorselDeposu.dosyaGoruntuIcin(it.dosyaAdi) }
        } else null
        val imageRatio = if (soruUzerindeCizim) {
            soruGorseli?.let { (it.genislik.toFloat() / it.yukseklik.coerceAtLeast(1)).takeIf { ratio -> ratio.isFinite() } }
                ?.takeIf { it > 0f }
        } else null
        val boardAspectRatio = imageRatio?.coerceIn(0.45f, 2.5f)
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            val boardWidth = boardAspectRatio?.let { minOf(maxWidth, maxHeight * it) } ?: maxWidth
            val boardHeight = boardAspectRatio?.let { (boardWidth / it).coerceAtMost(maxHeight) } ?: maxHeight
            val boardShape = RoundedCornerShape(18.dp)
            Box(
                modifier = Modifier.size(boardWidth, boardHeight)
                    .shadow(8.dp, boardShape)
                    .clip(boardShape)
                    .background(Color(0xFFFFFEFB))
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.78f), boardShape),
            ) {
                if (backgroundFile == null) {
                    Canvas(Modifier.fillMaxSize()) {
                        val step = 28.dp.toPx()
                        val gridColor = colors.primary.copy(alpha = 0.075f)
                        var x = step
                        while (x < size.width) {
                            drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 0.8.dp.toPx())
                            x += step
                        }
                        var y = step
                        while (y < size.height) {
                            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.8.dp.toPx())
                            y += step
                        }
                    }
                }
                if (backgroundFile != null) {
                    SoruGorselResmi(
                        file = backgroundFile,
                        contentDescription = stringResource(R.string.question_image_description),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        maxDecodeEdge = 1_800,
                    )
                }
                Canvas(
                    modifier = Modifier.fillMaxSize()
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        .testTag("solution-whiteboard-canvas")
                        .semantics {
                            contentDescription = context.getString(R.string.whiteboard_canvas_description)
                            stateDescription = countDescription
                        }
                        .pointerInput(arac, renk, kalinlik, imageRatio, soruUzerindeCizim, soruGorseli?.id) {
                            var baslangic: TahtaNoktasi? = null
                            var sonVurus: TahtaVurusu? = null
                            val viewport = fittedImageBounds(Size(size.width.toFloat(), size.height.toFloat()), imageRatio)
                            detectDragGestures(
                                onDragStart = { pos ->
                                    val start = normalizedPoint(pos, viewport, clampToBounds = false)
                                        ?: return@detectDragGestures
                                    baslangic = start
                                    sonVurus = TahtaVurusu(arac, listOf(start), renk, kalinlik)
                                    onVurusDegisti(sonVurus)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val start = baslangic ?: return@detectDragGestures
                                    val current = normalizedPoint(change.position, viewport, clampToBounds = true)
                                        ?: return@detectDragGestures
                                    val previous = sonVurus ?: TahtaVurusu(arac, listOf(start), renk, kalinlik)
                                    val next = when (arac) {
                                        TahtaAraci.KALEM, TahtaAraci.SILGI -> previous.copy(
                                            noktalar = appendPoint(previous.noktalar, current, viewport.width, viewport.height),
                                        )
                                        else -> previous.copy(noktalar = listOf(start, current))
                                    }
                                    sonVurus = next
                                    onVurusDegisti(next)
                                },
                                onDragEnd = {
                                    onVurusBitti(sonVurus)
                                    sonVurus = null
                                    baslangic = null
                                },
                                onDragCancel = {
                                    onVurusDegisti(null)
                                    sonVurus = null
                                    baslangic = null
                                },
                            )
                        },
                ) {
                    val viewport = fittedImageBounds(size, imageRatio)
                    (kalemler + listOfNotNull(etkinVurus)).forEach { drawVurus(it, viewport) }
                }
                if (soruUzerindeCizim && backgroundFile == null) {
                    Text(
                        stringResource(R.string.whiteboard_question_missing),
                        modifier = Modifier.align(Alignment.Center).padding(12.dp),
                        color = colors.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else if (kalemler.isEmpty() && etkinVurus == null) {
                    Surface(
                        modifier = Modifier.align(if (soruUzerindeCizim) Alignment.BottomCenter else Alignment.Center)
                            .padding(14.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.94f),
                        border = BorderStroke(1.dp, PremiumMarkaRenkler.Murekkep.copy(alpha = 0.08f)),
                        shadowElevation = 2.dp,
                    ) {
                        Text(
                            stringResource(R.string.whiteboard_no_marks),
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                            color = PremiumMarkaRenkler.Murekkep,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
        if (cizimSiniriUyarisi) {
            Surface(
                color = colors.errorContainer.copy(alpha = 0.78f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            ) {
                Text(
                    stringResource(R.string.whiteboard_mark_limit),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 7.dp),
                    color = colors.onErrorContainer,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else if (!compact) {
            Text(
                stringResource(R.string.whiteboard_temporary_note),
                modifier = Modifier.fillMaxWidth(),
                color = colors.onSurfaceVariant.copy(alpha = 0.78f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (!mergedCompactControls) {
            TahtaIslemPaneli(
                geriAlEtkin = kalemler.isNotEmpty(),
                yineleEtkin = geriAlinanlar.isNotEmpty(),
                temizleEtkin = temizlenebilir,
                kompakt = compact,
                onGeriAl = onGeriAl,
                onYinele = onYinele,
                onTemizle = onTemizle,
            )
        }
    }
}

private fun TahtaAraci.stringResource(): Int = when (this) {
    TahtaAraci.KALEM -> R.string.whiteboard_tool_pencil
    TahtaAraci.CIZGI -> R.string.whiteboard_tool_line
    TahtaAraci.DIKDORTGEN -> R.string.whiteboard_tool_rectangle
    TahtaAraci.UCGEN -> R.string.whiteboard_tool_triangle
    TahtaAraci.SILGI -> R.string.whiteboard_tool_eraser
}

private fun fittedImageBounds(canvasSize: Size, imageRatio: Float?): Rect {
    val width = canvasSize.width.coerceAtLeast(1f)
    val height = canvasSize.height.coerceAtLeast(1f)
    val ratio = imageRatio?.takeIf { it.isFinite() && it > 0f }
        ?: return Rect(0f, 0f, width, height)
    val contentWidth = min(width, height * ratio)
    val contentHeight = min(height, width / ratio)
    val left = (width - contentWidth) / 2f
    val top = (height - contentHeight) / 2f
    return Rect(left, top, left + contentWidth, top + contentHeight)
}

private fun normalizedPoint(position: Offset, viewport: Rect, clampToBounds: Boolean): TahtaNoktasi? {
    if (!clampToBounds && !viewport.contains(position)) return null
    return TahtaNoktasi(
        x = ((position.x - viewport.left) / viewport.width.coerceAtLeast(1f)).coerceIn(0f, 1f),
        y = ((position.y - viewport.top) / viewport.height.coerceAtLeast(1f)).coerceIn(0f, 1f),
    )
}

private fun appendPoint(
    points: List<TahtaNoktasi>,
    next: TahtaNoktasi,
    width: Float,
    height: Float,
): List<TahtaNoktasi> {
    val previous = points.lastOrNull() ?: return listOf(next)
    val dx = (next.x - previous.x) * width
    val dy = (next.y - previous.y) * height
    if (dx * dx + dy * dy < MIN_POINT_DISTANCE_PX * MIN_POINT_DISTANCE_PX) return points
    return if (points.size >= MAX_POINTS_PER_STROKE) {
        // Keep every other point so long freehand strokes stay bounded without truncating their ends.
        points.filterIndexed { index, _ -> index % 2 == 0 }.plus(next)
    } else {
        points + next
    }
}

private fun DrawScope.drawVurus(vurus: TahtaVurusu, viewport: Rect) {
    val points = vurus.noktalar.map {
        Offset(viewport.left + it.x * viewport.width, viewport.top + it.y * viewport.height)
    }
    if (points.isEmpty()) return
    val isEraser = vurus.araci == TahtaAraci.SILGI
    val color = if (isEraser) Color.Transparent else vurus.renk
    val blendMode = if (isEraser) BlendMode.Clear else BlendMode.SrcOver
    val widthPx = vurus.kalinlikDp.dp.toPx() * (if (isEraser) ERASER_WIDTH_FACTOR else 1f)
    val stroke = Stroke(width = widthPx, cap = StrokeCap.Round, join = StrokeJoin.Round)
    when (vurus.araci) {
        TahtaAraci.KALEM, TahtaAraci.SILGI -> {
            if (points.size == 1) {
                drawCircle(color = color, radius = widthPx / 2f, center = points.first(), blendMode = blendMode)
            } else {
                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(path = path, color = color, style = stroke, blendMode = blendMode)
            }
        }
        TahtaAraci.CIZGI -> if (points.size >= 2) {
            drawLine(color, points.first(), points.last(), strokeWidth = widthPx, cap = StrokeCap.Round)
        }
        TahtaAraci.DIKDORTGEN -> if (points.size >= 2) {
            val start = points.first()
            val end = points.last()
            drawRect(
                color = color,
                topLeft = Offset(min(start.x, end.x), min(start.y, end.y)),
                size = Size(abs(end.x - start.x), abs(end.y - start.y)),
                style = stroke,
            )
        }
        TahtaAraci.UCGEN -> if (points.size >= 2) {
            val start = points.first()
            val end = points.last()
            val left = min(start.x, end.x)
            val right = max(start.x, end.x)
            val top = min(start.y, end.y)
            val bottom = max(start.y, end.y)
            val path = Path().apply {
                moveTo((left + right) / 2f, top)
                lineTo(right, bottom)
                lineTo(left, bottom)
                close()
            }
            drawPath(path = path, color = color, style = stroke)
        }
    }
}

private const val BOS_TAHTA_KATMANI = "tahta:bos"
private const val MAX_TAHTA_VURUSU = 250
private const val MAX_POINTS_PER_STROKE = 2_048
private const val MIN_POINT_DISTANCE_PX = 1.5f
private const val ERASER_WIDTH_FACTOR = 3.4f

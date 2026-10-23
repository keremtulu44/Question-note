package com.keremtulu.questionnote.ui.calisma

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
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
            shape = RoundedCornerShape(26.dp),
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            stringResource(R.string.whiteboard_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            stringResource(R.string.whiteboard_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(
                        onClick = { soruPaneliAcik = !soruPaneliAcik },
                        modifier = Modifier.testTag("whiteboard-question-preview-toggle").semantics {
                            contentDescription = context.getString(
                                if (soruPaneliAcik) R.string.common_close else R.string.question_form_question_images,
                            )
                        },
                    ) {
                        SoruPaneliDugmeIkonu(acik = soruPaneliAcik)
                    }
                    TextButton(
                        onClick = {
                            soruPaneliAcik = false
                            onDismiss()
                        },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(R.string.whiteboard_close))
                    }
                }

                BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                    val landscape = maxWidth > maxHeight && maxWidth >= 480.dp
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
private fun SoruPaneliDugmeIkonu(acik: Boolean) {
    val color = MaterialTheme.colorScheme.onSurface
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
        color = colors.surfaceVariant.copy(alpha = 0.62f),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
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
private fun TahtaCalismaAlani(
    kalemler: List<TahtaVurusu>,
    geriAlinanlar: List<TahtaVurusu>,
    etkinVurus: TahtaVurusu?,
    arac: TahtaAraci,
    renk: Color,
    kalinlik: Float,
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
    Column(modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = soruUzerindeCizim,
                onClick = onYuzeyDegistir,
                enabled = soruUzerindeCizim || soruZeminiKullanilabilir,
                modifier = Modifier.testTag("whiteboard-surface-toggle").semantics {
                    contentDescription = context.getString(
                        if (soruUzerindeCizim) R.string.whiteboard_surface_switch_to_blank
                        else R.string.whiteboard_surface_switch_to_question,
                    )
                    stateDescription = context.getString(
                        if (soruUzerindeCizim) R.string.whiteboard_surface_question
                        else R.string.whiteboard_surface_blank,
                    )
                },
                label = {
                    Text(
                        stringResource(
                            if (soruUzerindeCizim) R.string.whiteboard_surface_question
                            else R.string.whiteboard_surface_blank,
                        ),
                        maxLines = 1,
                    )
                },
            )
            TahtaAraci.entries.forEach { tool ->
                val label = stringResource(tool.stringResource())
                FilterChip(
                    selected = arac == tool,
                    onClick = { onAracSec(tool) },
                    modifier = Modifier.semantics { selected = arac == tool; role = androidx.compose.ui.semantics.Role.RadioButton },
                    label = { Text(label, maxLines = 1) },
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.whiteboard_color_label), style = MaterialTheme.typography.labelSmall)
            TahtaKalemRenkleri.forEach { item ->
                val label = stringResource(item.ad)
                FilterChip(
                    selected = renk == item.renk,
                    onClick = { onRenkSec(item.renk) },
                    modifier = Modifier.semantics {
                        contentDescription = context.getString(R.string.whiteboard_selected_color, label)
                        selected = renk == item.renk
                        role = androidx.compose.ui.semantics.Role.RadioButton
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Surface(
                                modifier = Modifier.size(16.dp).border(1.dp, colors.outline.copy(alpha = 0.6f), CircleShape),
                                shape = CircleShape,
                                color = item.renk,
                            ) {}
                            Text(label, maxLines = 1)
                        }
                    },
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.whiteboard_width_label), style = MaterialTheme.typography.labelSmall)
            listOf(2f to R.string.whiteboard_width_thin, 4f to R.string.whiteboard_width_normal, 8f to R.string.whiteboard_width_thick)
                .forEach { (width, label) ->
                    FilterChip(
                        selected = kalinlik == width,
                        onClick = { onKalinlikSec(width) },
                        modifier = Modifier.semantics {
                            selected = kalinlik == width
                            role = androidx.compose.ui.semantics.Role.RadioButton
                        },
                        label = { Text(stringResource(label)) },
                    )
                }
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
                    .clip(boardShape)
                    .background(Color.White)
                    .border(1.dp, colors.outlineVariant, boardShape),
            ) {
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
                    Text(
                        stringResource(R.string.whiteboard_no_marks),
                        modifier = Modifier.align(if (soruUzerindeCizim) Alignment.BottomCenter else Alignment.Center)
                            .padding(14.dp)
                            .background(Color.White.copy(alpha = 0.86f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        color = Color(0xFF52616B),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        if (cizimSiniriUyarisi) {
            Text(
                stringResource(R.string.whiteboard_mark_limit),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                color = colors.error,
                style = MaterialTheme.typography.labelSmall,
            )
        } else {
            Text(
                stringResource(R.string.whiteboard_temporary_note),
                modifier = Modifier.fillMaxWidth(),
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onGeriAl, enabled = kalemler.isNotEmpty()) {
                Text(stringResource(R.string.whiteboard_undo))
            }
            TextButton(onClick = onYinele, enabled = geriAlinanlar.isNotEmpty()) {
                Text(stringResource(R.string.whiteboard_redo))
            }
            TextButton(onClick = onTemizle, enabled = temizlenebilir) {
                Text(stringResource(R.string.whiteboard_clear))
            }
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

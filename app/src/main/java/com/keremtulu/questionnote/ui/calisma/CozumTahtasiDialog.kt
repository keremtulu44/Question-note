package com.keremtulu.questionnote.ui.calisma

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.SoruGorseli
import com.keremtulu.questionnote.ui.bilesenler.SoruGorselResmi
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
    val kalemler = remember(soru.id) { mutableStateListOf<TahtaVurusu>() }
    val geriAlinanlar = remember(soru.id) { mutableStateListOf<TahtaVurusu>() }
    var etkinVurus by remember(soru.id) { mutableStateOf<TahtaVurusu?>(null) }
    var arac by remember(soru.id) { mutableStateOf(TahtaAraci.KALEM) }
    var renk by remember(soru.id) { mutableStateOf(TahtaKalemRenkleri.first().renk) }
    var kalinlik by remember(soru.id) { mutableStateOf(4f) }
    var temizlemeOnayi by remember(soru.id) { mutableStateOf(false) }
    var cizimSiniriUyarisi by remember(soru.id) { mutableStateOf(false) }
    var soruUzerindeCizim by remember(soru.id) { mutableStateOf(false) }
    val ilkSoruGorseli = remember(soru.id, soru.gorseller) { soru.gorseller.firstOrNull { it.tur == GorselTuru.SORU } }
    if (!visible) return

    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val soruGorseliDosyasi = ilkSoruGorseli?.let { gorselDeposu.dosyaGoruntuIcin(it.dosyaAdi) }
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
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
                    Column(Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            stringResource(R.string.whiteboard_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            stringResource(R.string.whiteboard_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    FilterChip(
                        selected = soruUzerindeCizim,
                        onClick = { soruUzerindeCizim = !soruUzerindeCizim },
                        enabled = ilkSoruGorseli != null && soruGorseliDosyasi != null,
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
                    TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
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
                                gorselDeposu = gorselDeposu,
                                landscape = true,
                                modifier = Modifier.weight(0.38f).fillMaxHeight(),
                            )
                            TahtaCalismaAlani(
                                kalemler = kalemler,
                                geriAlinanlar = geriAlinanlar,
                                etkinVurus = etkinVurus,
                                arac = arac,
                                renk = renk,
                                kalinlik = kalinlik,
                                gorselDeposu = gorselDeposu,
                                arkaPlanGorseli = ilkSoruGorseli,
                                soruUzerindeCizim = soruUzerindeCizim,
                                cizimSiniriUyarisi = cizimSiniriUyarisi,
                                onAracSec = { arac = it },
                                onRenkSec = { renk = it },
                                onKalinlikSec = { kalinlik = it },
                                onVurusDegisti = { etkinVurus = it },
                                onVurusBitti = { yeni ->
                                    etkinVurus = null
                                    if (yeni != null) {
                                        if (kalemler.size >= MAX_TAHTA_VURUSU) {
                                            cizimSiniriUyarisi = true
                                        } else {
                                            kalemler += yeni
                                            geriAlinanlar.clear()
                                            cizimSiniriUyarisi = false
                                        }
                                    }
                                },
                                onGeriAl = {
                                    if (kalemler.isNotEmpty()) geriAlinanlar += kalemler.removeAt(kalemler.lastIndex)
                                },
                                onYinele = {
                                    if (geriAlinanlar.isNotEmpty()) kalemler += geriAlinanlar.removeAt(geriAlinanlar.lastIndex)
                                },
                                onTemizle = { temizlemeOnayi = true },
                                modifier = Modifier.weight(0.62f).fillMaxHeight(),
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            SoruOnizlemesi(
                                soru = soru,
                                gorselDeposu = gorselDeposu,
                                landscape = false,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 126.dp, max = 190.dp),
                            )
                            TahtaCalismaAlani(
                                kalemler = kalemler,
                                geriAlinanlar = geriAlinanlar,
                                etkinVurus = etkinVurus,
                                arac = arac,
                                renk = renk,
                                kalinlik = kalinlik,
                                gorselDeposu = gorselDeposu,
                                arkaPlanGorseli = ilkSoruGorseli,
                                soruUzerindeCizim = soruUzerindeCizim,
                                cizimSiniriUyarisi = cizimSiniriUyarisi,
                                onAracSec = { arac = it },
                                onRenkSec = { renk = it },
                                onKalinlikSec = { kalinlik = it },
                                onVurusDegisti = { etkinVurus = it },
                                onVurusBitti = { yeni ->
                                    etkinVurus = null
                                    if (yeni != null) {
                                        if (kalemler.size >= MAX_TAHTA_VURUSU) {
                                            cizimSiniriUyarisi = true
                                        } else {
                                            kalemler += yeni
                                            geriAlinanlar.clear()
                                            cizimSiniriUyarisi = false
                                        }
                                    }
                                },
                                onGeriAl = {
                                    if (kalemler.isNotEmpty()) geriAlinanlar += kalemler.removeAt(kalemler.lastIndex)
                                },
                                onYinele = {
                                    if (geriAlinanlar.isNotEmpty()) kalemler += geriAlinanlar.removeAt(geriAlinanlar.lastIndex)
                                },
                                onTemizle = { temizlemeOnayi = true },
                                modifier = Modifier.fillMaxWidth().weight(1f),
                            )
                        }
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
                    kalemler.clear()
                    geriAlinanlar.clear()
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
private fun SoruOnizlemesi(
    soru: Soru,
    gorselDeposu: LocalSoruGorselDeposu,
    landscape: Boolean,
    modifier: Modifier = Modifier,
) {
    val images = soru.gorseller.filter { it.tur == GorselTuru.SORU }
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
                    maxLines = if (landscape) 3 else 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val source = listOfNotNull(soru.kaynak, soru.kaynakKonumu).filter(String::isNotBlank).joinToString(" · ")
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
            if (images.isEmpty()) {
                Text(
                    stringResource(R.string.whiteboard_question_missing),
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.error,
                )
            } else if (landscape) {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp),
                ) {
                    items(images, key = SoruGorseli::id) { image ->
                        SoruOnizlemeGorseli(image, gorselDeposu, Modifier.fillMaxWidth())
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(images, key = SoruGorseli::id) { image ->
                        SoruOnizlemeGorseli(image, gorselDeposu, Modifier.fillMaxHeight().width(240.dp))
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
    modifier: Modifier = Modifier,
) {
    val ratio = (image.genislik.toFloat() / image.yukseklik.coerceAtLeast(1)).coerceIn(0.45f, 2.5f)
    SoruGorselResmi(
        file = gorselDeposu.dosyaGoruntuIcin(image.dosyaAdi),
        contentDescription = stringResource(
            if (image.tur == GorselTuru.SORU) R.string.question_image_description else R.string.solution_image_description,
        ),
        modifier = modifier.clip(RoundedCornerShape(12.dp)).heightIn(min = 90.dp).aspectRatio(ratio),
        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
        maxDecodeEdge = 1_200,
    )
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
    arkaPlanGorseli: SoruGorseli?,
    soruUzerindeCizim: Boolean,
    cizimSiniriUyarisi: Boolean,
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
            arkaPlanGorseli?.let { gorselDeposu.dosyaGoruntuIcin(it.dosyaAdi) }
        } else null
        val imageRatio = if (soruUzerindeCizim) {
            arkaPlanGorseli?.let { (it.genislik.toFloat() / it.yukseklik.coerceAtLeast(1)).takeIf { ratio -> ratio.isFinite() } }
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
                        .pointerInput(arac, renk, kalinlik, imageRatio) {
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
                        stringResource(R.string.whiteboard_question_background_missing),
                        modifier = Modifier.align(Alignment.Center).padding(12.dp),
                        color = colors.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else if (kalemler.isEmpty() && etkinVurus == null) {
                    Text(
                        stringResource(
                            if (soruUzerindeCizim) R.string.whiteboard_question_draw_hint
                            else R.string.whiteboard_no_marks,
                        ),
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
            TextButton(onClick = onTemizle, enabled = kalemler.isNotEmpty() || etkinVurus != null) {
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

private const val MAX_TAHTA_VURUSU = 250
private const val MAX_POINTS_PER_STROKE = 2_048
private const val MIN_POINT_DISTANCE_PX = 1.5f
private const val ERASER_WIDTH_FACTOR = 3.4f

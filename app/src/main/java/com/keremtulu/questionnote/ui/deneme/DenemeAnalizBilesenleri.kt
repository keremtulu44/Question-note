package com.keremtulu.questionnote.ui.deneme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.DenemeKaydi
import com.keremtulu.questionnote.domain.model.DersNetKarsilastirma
import com.keremtulu.questionnote.domain.model.NetKurali
import com.keremtulu.questionnote.ui.bilesenler.SadeKart
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/** The selected rule and the latest matching scope are the only series connected on the chart. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DenemeTrendKarti(
    noktalar: List<DenemeKaydi>,
    kurallar: List<NetKurali>,
    seciliKural: NetKurali,
    kapsamlar: List<DenemeKaydi>,
    uyumsuzSayisi: Int,
    toplamEslesen: Int,
    limit: Int,
    oncekiNet: Double?,
    onKuralSec: (NetKurali) -> Unit,
    onKapsamSec: (DenemeKaydi) -> Unit,
    onLimitSec: (Int) -> Unit,
) {
    val son = noktalar.lastOrNull() ?: return
    val colors = MaterialTheme.colorScheme
    SadeKart(modifier = Modifier.testTag("deneme-trend-card")) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.deneme_chart_net),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            if (kurallar.size > 1) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    kurallar.forEach { kural ->
                        FilterChip(
                            selected = seciliKural == kural,
                            onClick = { onKuralSec(kural) },
                            label = { Text(netKuraliEtiketi(kural)) },
                            modifier = Modifier.testTag("deneme-rule-${kural.name}"),
                        )
                    }
                }
            }
            if (kapsamlar.size > 1) {
                var kapsamMenusuAcik by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { kapsamMenusuAcik = true },
                        modifier = Modifier.fillMaxWidth().testTag("deneme-scope-selector"),
                    ) { Text(stringResource(R.string.deneme_scope_select, son.ad)) }
                    DropdownMenu(expanded = kapsamMenusuAcik, onDismissRequest = { kapsamMenusuAcik = false }) {
                        kapsamlar.forEach { temsilci ->
                            DropdownMenuItem(
                                modifier = Modifier.testTag("deneme-scope-option-${temsilci.id}"),
                                text = {
                                    Column {
                                        Text(stringResource(R.string.deneme_scope_item, temsilci.ad, temsilci.satirlar.sumOf { it.soruSayisi.toLong() }))
                                        Text(denemeTarihYazi(temsilci.gun), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                    }
                                },
                                onClick = { kapsamMenusuAcik = false; onKapsamSec(temsilci) },
                            )
                        }
                    }
                }
                Text(stringResource(R.string.deneme_scope_hint), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
            Text(
                text = stringResource(R.string.deneme_chart_scope, noktalar.size),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TrendDegeri(
                    baslik = stringResource(R.string.deneme_chart_latest),
                    deger = netYazi(son.toplamNet()),
                    modifier = Modifier.weight(1f),
                )
                TrendDegeri(
                    baslik = stringResource(R.string.deneme_chart_average),
                    deger = if (noktalar.size >= 2) netYazi(noktalar.map { it.toplamNet() }.average()) else "—",
                    modifier = Modifier.weight(1f),
                )
            }
            if (oncekiNet != null) {
                val fark = son.toplamNet() - oncekiNet
                val farkYazi = if (fark > 0.005) "+${netYazi(fark)}" else netYazi(fark)
                Text(
                    text = stringResource(R.string.deneme_chart_change, farkYazi),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (toplamEslesen > 6) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(6 to R.string.deneme_chart_last_six, 12 to R.string.deneme_chart_last_twelve).forEach { (sayisi, yazi) ->
                        FilterChip(
                            selected = limit == sayisi,
                            onClick = { onLimitSec(sayisi) },
                            label = { Text(stringResource(yazi)) },
                            modifier = Modifier.testTag("deneme-range-$sayisi"),
                        )
                    }
                }
            }
            NetEgilimGrafigi(noktalar)
            if (noktalar.size < 2) {
                Text(stringResource(R.string.deneme_chart_same_scope_needed), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            } else {
                Text(stringResource(R.string.deneme_chart_order), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
            Text(stringResource(R.string.deneme_chart_scope_note), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            if (uyumsuzSayisi > 0) {
                Text(
                    text = stringResource(R.string.deneme_chart_excluded, uyumsuzSayisi),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.testTag("deneme-scope-excluded"),
                )
            }
        }
    }
}

@Composable
private fun TrendDegeri(baslik: String, deger: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(baslik, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(deger, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun NetEgilimGrafigi(noktalar: List<DenemeKaydi>) {
    val colors = MaterialTheme.colorScheme
    val degerler = noktalar.map { it.toplamNet() }
    // Include zero and use identical scaling for positive, negative and flat series.
    val alt = floor(min(0.0, degerler.minOrNull() ?: 0.0) / 5.0) * 5.0
    val ust = max(alt + 5.0, ceil(max(0.0, degerler.maxOrNull() ?: 0.0) / 5.0) * 5.0)
    val orta = (ust + alt) / 2.0
    val aciklama = stringResource(
        R.string.deneme_chart_accessibility,
        noktalar.joinToString("; ") { "${denemeTarihYazi(it.gun)}: ${netYazi(it.toplamNet())} net" },
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth().height(152.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(
                modifier = Modifier.fillMaxHeight().padding(vertical = 2.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
            ) {
                listOf(ust, orta, alt).forEach { deger ->
                    Text(netYazi(deger), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                }
            }
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("deneme-net-chart")
                    .semantics { contentDescription = aciklama },
            ) {
                val ic = 9.dp.toPx()
                val w = (size.width - ic * 2).coerceAtLeast(1f)
                val h = (size.height - ic * 2).coerceAtLeast(1f)
                fun y(deger: Double): Float = ic + h * (1.0 - (deger - alt) / (ust - alt)).toFloat().coerceIn(0f, 1f)
                listOf(ust, orta, alt).forEach { deger ->
                    drawLine(
                        color = colors.outlineVariant.copy(alpha = if (abs(deger) < 0.001) 0.9f else 0.55f),
                        start = Offset(ic, y(deger)),
                        end = Offset(size.width - ic, y(deger)),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = if (abs(deger) < 0.001) null else PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx())),
                    )
                }
                if (alt < 0.0 && ust > 0.0 && abs(orta) > 0.001) {
                    drawLine(colors.outlineVariant, Offset(ic, y(0.0)), Offset(size.width - ic, y(0.0)), 1.dp.toPx())
                }
                val konumlar = degerler.mapIndexed { index, deger ->
                    val x = if (degerler.size == 1) size.width / 2f else ic + w * index / degerler.lastIndex.toFloat()
                    Offset(x, y(deger))
                }
                if (konumlar.size > 1) {
                    val yol = Path().apply {
                        konumlar.forEachIndexed { index, nokta ->
                            if (index == 0) moveTo(nokta.x, nokta.y) else lineTo(nokta.x, nokta.y)
                        }
                    }
                    drawPath(yol, colors.primary, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
                konumlar.forEachIndexed { index, nokta ->
                    if (index == konumlar.lastIndex) drawCircle(colors.primary.copy(alpha = 0.15f), 8.dp.toPx(), nokta)
                    drawCircle(colors.primary, if (index == konumlar.lastIndex) 4.5.dp.toPx() else 3.5.dp.toPx(), nokta)
                    drawCircle(colors.surface, 1.5.dp.toPx(), nokta)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(denemeTarihYazi(noktalar.first().gun), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            if (noktalar.size > 1) Text(denemeTarihYazi(noktalar.last().gun), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
    }
}

/** The stack shows actual question counts, not net points or an estimated success score. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DenemeDagilimKarti(son: DenemeKaydi) {
    val dogru = son.satirlar.sumOf { it.dogru.toLong() }
    val yanlis = son.satirlar.sumOf { it.yanlis.toLong() }
    val bos = son.satirlar.sumOf { it.bos.toLong() }
    val toplam = dogru + yanlis + bos
    if (toplam <= 0L) return
    val colors = MaterialTheme.colorScheme
    val renkler = listOf(colors.primary, colors.secondary, colors.outline)
    val sayilar = listOf(dogru, yanlis, bos)
    val etiketler = listOf(R.string.deneme_correct, R.string.deneme_wrong, R.string.deneme_blank)
    val aciklama = stringResource(R.string.deneme_distribution_accessibility, dogru, yanlis, bos)
    SadeKart(modifier = Modifier.testTag("deneme-distribution-card")) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.deneme_distribution_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.deneme_distribution_subtitle, son.ad, toplam), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Canvas(
                modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(8.dp))
                    .testTag("deneme-distribution-chart")
                    .semantics { contentDescription = aciklama },
            ) {
                var baslangic = 0f
                sayilar.forEachIndexed { index, sayi ->
                    val genislik = size.width * sayi / toplam.toFloat()
                    if (genislik > 0f) drawRect(renkler[index], topLeft = Offset(baslangic, 0f), size = Size(genislik, size.height))
                    baslangic += genislik
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                sayilar.forEachIndexed { index, sayi ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(8.dp).background(renkler[index], CircleShape))
                        Text(
                            stringResource(R.string.deneme_distribution_legend, sayi, stringResource(etiketler[index])),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DenemeDersKarti(son: DenemeKaydi, barlar: List<DersNetKarsilastirma>, dersAdlari: Map<String, String>) {
    if (barlar.isEmpty()) return
    val colors = MaterialTheme.colorScheme
    val degerler = barlar.flatMap { listOfNotNull(it.sonNet, it.oncekiOrtalama) }
    val alt = min(0.0, degerler.minOrNull() ?: 0.0)
    val ust = max(0.0, degerler.maxOrNull() ?: 0.0)
    val aralik = (ust - alt).coerceAtLeast(1.0)
    SadeKart(modifier = Modifier.testTag("deneme-lessons-card")) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.deneme_chart_lessons, son.ad), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                stringResource(if (barlar.any { it.oncekiOrtalama != null }) R.string.deneme_chart_legend else R.string.deneme_chart_no_previous),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            barlar.forEach { bar ->
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = dersAdlari[bar.dersId] ?: bar.dersAdi ?: "—",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(stringResource(R.string.deneme_lesson_net, netYazi(bar.sonNet)), style = MaterialTheme.typography.labelLarge, color = colors.primary, fontWeight = FontWeight.Bold)
                    }
                    bar.oncekiOrtalama?.let { ortalama ->
                        Text(
                            stringResource(R.string.deneme_lesson_previous, netYazi(ortalama), bar.oncekiDenemeSayisi),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    Canvas(Modifier.fillMaxWidth().height(12.dp)) {
                        val sifir = ((-alt / aralik).toFloat() * size.width).coerceIn(0f, size.width)
                        drawRoundRect(colors.surfaceVariant, cornerRadius = CornerRadius(size.height / 2f))
                        fun ciz(deger: Double, renk: Color, yukseklik: Float, y: Float) {
                            if (abs(deger) < 0.00001) return // zero must not look like a small positive bar
                            val hedef = (((deger - alt) / aralik).toFloat() * size.width).coerceIn(0f, size.width)
                            val sol = min(sifir, hedef)
                            val genislik = abs(hedef - sifir)
                            if (genislik > 0f) drawRoundRect(renk, Offset(sol, y), Size(genislik, yukseklik), CornerRadius(yukseklik / 2f))
                        }
                        bar.oncekiOrtalama?.let { ciz(it, colors.primary.copy(alpha = 0.25f), size.height, 0f) }
                        ciz(bar.sonNet, colors.primary, size.height * 0.55f, size.height * 0.225f)
                        if (alt < 0.0) drawLine(colors.outline, Offset(sifir, 0f), Offset(sifir, size.height), 1.dp.toPx())
                    }
                }
            }
        }
    }
}

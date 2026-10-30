package com.keremtulu.questionnote.ui.rehber

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Rehberin çizimleri.
 *
 * Uygulamanın geri kalanıyla aynı dil: Compose Canvas, tek çizgi kalınlığı, yuvarlak uçlar,
 * bitmap varlık yok. Hepsi dekoratiftir; bilgi metin olarak da yazılır (ekran okuyucu çizime
 * bağımlı kalmaz).
 */

/** Kahraman: kâğıt defter, bakır kenar, kırmızı ayraç. */
@Composable
fun RehberDefterCizimi(
    modifier: Modifier = Modifier,
    kagit: Color,
    murekkep: Color,
    bakir: Color,
    vurgu: Color,
) {
    Canvas(modifier) {
        val d = size.minDimension
        val u = d / 100f
        val sol = size.width / 2f - 26f * u
        val ust = size.height / 2f - 34f * u
        val genislik = 52f * u
        val yukseklik = 68f * u
        val yaricap = CornerRadius(6f * u, 6f * u)

        // Sayfa
        drawRoundRect(
            color = kagit,
            topLeft = Offset(sol, ust),
            size = Size(genislik, yukseklik),
            cornerRadius = yaricap,
        )
        // Bakır sırt
        drawRoundRect(
            color = bakir,
            topLeft = Offset(sol, ust),
            size = Size(6f * u, yukseklik),
            cornerRadius = yaricap,
        )
        // Kırmızı ayraç
        drawLine(
            color = vurgu,
            start = Offset(sol + 13f * u, ust + 8f * u),
            end = Offset(sol + 13f * u, ust + yukseklik - 8f * u),
            strokeWidth = 1.6f * u,
            cap = StrokeCap.Round,
        )
        // Satırlar
        repeat(5) { satir ->
            val y = ust + 16f * u + satir * 9f * u
            drawLine(
                color = murekkep.copy(alpha = 0.55f),
                start = Offset(sol + 19f * u, y),
                end = Offset(sol + genislik - 8f * u - (satir % 2) * 8f * u, y),
                strokeWidth = 1.5f * u,
                cap = StrokeCap.Round,
            )
        }
        // Kenar tırnağı: "bu sayfa işaretli"
        drawLine(
            color = bakir,
            start = Offset(sol + genislik, ust + 18f * u),
            end = Offset(sol + genislik + 9f * u, ust + 22f * u),
            strokeWidth = 2f * u,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = bakir,
            start = Offset(sol + genislik + 9f * u, ust + 22f * u),
            end = Offset(sol + genislik, ust + 26f * u),
            strokeWidth = 2f * u,
            cap = StrokeCap.Round,
        )
    }
}

/**
 * Aralıklı tekrar yolculuğu: 1 → 3 → 7 → 21 gün.
 * [vurgulanan] indeksindeki durak dolu çizilir; kalanlar boş halkadır.
 */
@Composable
fun RehberAralikYolu(
    modifier: Modifier = Modifier,
    cizgi: Color,
    dolgu: Color,
    vurgulanan: Int = 0,
    duraklar: List<Int> = listOf(1, 3, 7, 21),
) {
    Canvas(modifier) {
        if (duraklar.isEmpty()) return@Canvas
        val merkezY = size.height * 0.5f
        val sol = 6.dp.toPx()
        val sag = (size.width - 6.dp.toPx()).coerceAtLeast(sol + 1f)
        val adim = if (duraklar.size == 1) 0f else (sag - sol) / (duraklar.size - 1)
        drawLine(
            color = cizgi.copy(alpha = 0.6f),
            start = Offset(sol, merkezY),
            end = Offset(sag, merkezY),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round,
        )
        duraklar.forEachIndexed { indeks, _ ->
            val x = sol + adim * indeks
            val secili = indeks == vurgulanan.coerceIn(0, duraklar.lastIndex)
            drawCircle(
                color = if (secili) dolgu else cizgi.copy(alpha = 0.35f),
                radius = if (secili) 6.dp.toPx() else 3.5.dp.toPx(),
                center = Offset(x, merkezY),
            )
        }
    }
}

/** Yedek: kapalı bir ZIP kutusu, içine inen ok. */
@Composable
fun RehberYedekKutusu(
    modifier: Modifier = Modifier,
    cizgi: Color,
    vurgu: Color,
    dolgu: Color,
) {
    Canvas(modifier) {
        val d = size.minDimension
        val u = d / 100f
        val sol = size.width / 2f - 34f * u
        val ust = size.height / 2f - 20f * u
        val genislik = 68f * u
        val yukseklik = 44f * u
        val kalin = 2f * u

        drawRoundRect(
            color = dolgu,
            topLeft = Offset(sol, ust),
            size = Size(genislik, yukseklik),
            cornerRadius = CornerRadius(8f * u, 8f * u),
        )
        drawRoundRect(
            color = cizgi,
            topLeft = Offset(sol, ust),
            size = Size(genislik, yukseklik),
            cornerRadius = CornerRadius(8f * u, 8f * u),
            style = Stroke(kalin),
        )
        // Fermuar
        drawLine(
            color = cizgi,
            start = Offset(sol + genislik / 2f, ust + 3f * u),
            end = Offset(sol + genislik / 2f, ust + yukseklik - 3f * u),
            strokeWidth = kalin,
            cap = StrokeCap.Round,
        )
        repeat(4) { i ->
            val y = ust + 9f * u + i * 8f * u
            drawLine(
                color = cizgi.copy(alpha = 0.55f),
                start = Offset(sol + genislik / 2f - 4f * u, y),
                end = Offset(sol + genislik / 2f + 4f * u, y),
                strokeWidth = 1.2f * u,
                cap = StrokeCap.Round,
            )
        }
        // İçine inen ok
        val okX = sol + genislik / 2f
        val okUst = ust - 16f * u
        drawLine(
            color = vurgu,
            start = Offset(okX, okUst),
            end = Offset(okX, okUst + 10f * u),
            strokeWidth = kalin,
            cap = StrokeCap.Round,
        )
        drawPath(
            path = Path().apply {
                moveTo(okX - 5f * u, okUst + 6f * u)
                lineTo(okX, okUst + 11f * u)
                lineTo(okX + 5f * u, okUst + 6f * u)
            },
            color = vurgu,
            style = Stroke(width = kalin, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

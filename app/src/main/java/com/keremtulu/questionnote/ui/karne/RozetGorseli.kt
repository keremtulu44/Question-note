package com.keremtulu.questionnote.ui.karne

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.domain.karne.Rozet

/**
 * Badge artwork drawn with Canvas (no image files, scales to any size).
 * Locked badges are drawn in the surface colours; earned ones use the brand gold/indigo pair.
 */
@Composable
fun RozetGorseli(
    rozet: Rozet,
    kazanildi: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val palet = when (rozet) {
        Rozet.ILK_SORU, Rozet.ARSIVCI, Rozet.KUTUPHANE, Rozet.KOLEKSIYONCU -> Color(0xFF186C68) to Color(0xFF7BDDC3)
        Rozet.SERI_3, Rozet.SERI_7, Rozet.SERI_14, Rozet.SERI_30, Rozet.SERI_60, Rozet.SERI_100 -> Color(0xFF934418) to Color(0xFFFFC46D)
        Rozet.HEDEF_1, Rozet.HEDEF_7, Rozet.HEDEF_30 -> Color(0xFF3756A2) to Color(0xFFAAC8FF)
        else -> if (rozet.gizli) Color(0xFF6347A3) to Color(0xFFE4C2FF) else Color(0xFF326174) to Color(0xFFE7BF6C)
    }
    val ana = if (kazanildi) palet.first else colors.outline.copy(alpha = 0.5f)
    val vurgu = if (kazanildi) palet.second else colors.outlineVariant.copy(alpha = 0.5f)
    Box(modifier = modifier) {
        Canvas(Modifier.fillMaxSize().padding(2.dp)) {
            val olcek = size.minDimension
            val kalinlik = (olcek * 0.075f).coerceAtLeast(2f)
            // Every badge sits on the same hexagonal medallion for a consistent family look.
            madalyon(ana, vurgu, kalinlik)
            val m = olcek * 0.5f
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = m * 0.52f
            when (rozet) {
                Rozet.ILK_KARNE -> ilkAdim(c, r, ana, vurgu, kalinlik)
                Rozet.KUSURSUZ_TUR -> kusursuz(c, r, ana, vurgu, kalinlik)
                Rozet.ON_BAGIMSIZ -> onluk(c, r, ana, vurgu, kalinlik)
                Rozet.YIRMI_SORU -> yirmilik(c, r, ana, vurgu, kalinlik)
                Rozet.COZUMSUZ -> kendiBasina(c, r, ana, vurgu, kalinlik)
                Rozet.ISTIKRAR -> istikrar(c, r, ana, vurgu, kalinlik)
                Rozet.SERI_3 -> seri(c, r, 3, ana, vurgu, kalinlik)
                Rozet.SERI_7 -> seri(c, r, 7, ana, vurgu, kalinlik)
                Rozet.SERI_30 -> seri(c, r, 30, ana, vurgu, kalinlik)
                Rozet.YUZ_BAGIMSIZ -> yuz(c, r, ana, vurgu, kalinlik)
                Rozet.BES_YUZ -> besYuz(c, r, ana, vurgu, kalinlik)
                Rozet.ILK_SORU -> kitap(c, r, 1, ana, vurgu, kalinlik)
                Rozet.ARSIVCI -> kitap(c, r, 2, ana, vurgu, kalinlik)
                Rozet.KUTUPHANE -> kitap(c, r, 3, ana, vurgu, kalinlik)
                Rozet.KOLEKSIYONCU -> kitap(c, r, 4, ana, vurgu, kalinlik)
                Rozet.ELLI_TEKRAR -> dongu(c, r, 1, ana, vurgu, kalinlik)
                Rozet.IKI_YUZ_TEKRAR -> dongu(c, r, 2, ana, vurgu, kalinlik)
                Rozet.BIN_TEKRAR -> dongu(c, r, 3, ana, vurgu, kalinlik)
                Rozet.BIN_BAGIMSIZ -> {
                    besYuz(c, r * 0.75f, ana, vurgu, kalinlik)
                    isilti(c, r * 1.1f, 5, ana)
                }
                Rozet.SERI_14, Rozet.SERI_60, Rozet.SERI_100 -> {
                    seri(c, r * 0.8f, 30, ana, vurgu, kalinlik)
                    val derece = when (rozet) { Rozet.SERI_14 -> 2; Rozet.SERI_60 -> 3; else -> 5 }
                    isilti(c, r * 1.15f, derece, ana)
                }
                Rozet.HEDEF_1 -> yuz(c, r, ana, vurgu, kalinlik)
                Rozet.HEDEF_7 -> {
                    yuz(c, r * 0.75f, ana, vurgu, kalinlik)
                    isilti(c, r * 1.15f, 3, ana)
                }
                Rozet.HEDEF_30 -> {
                    yuz(c, r * 0.7f, ana, vurgu, kalinlik)
                    tac(c.copy(y = c.y - r * 0.8f), r * 0.5f, ana, vurgu, kalinlik * 0.65f)
                }
                Rozet.UC_DERS -> {
                    repeat(3) { i ->
                        val a = Math.toRadians((-90.0 + i * 120))
                        val pos = c + Offset(kotlin.math.cos(a).toFloat() * r * 0.55f, kotlin.math.sin(a).toFloat() * r * 0.55f)
                        drawCircle(vurgu, r * 0.42f, pos)
                        drawCircle(ana, r * 0.42f, pos, style = Stroke(kalinlik * 0.6f))
                    }
                }
                Rozet.BES_DERS -> papatya(c, r, ana, vurgu, kalinlik)
                Rozet.DONUSUM -> anka(c, r, ana, vurgu, kalinlik)
                Rozet.ZINCIR_10 -> zincir(c, r, ana, vurgu, kalinlik)
                Rozet.KUSURSUZ_20 -> tac(c, r, ana, vurgu, kalinlik)
            }
        }
    }
}

private fun DrawScope.madalyon(ana: Color, vurgu: Color, kalinlik: Float) {
    val yaricap = size.minDimension * 0.46f
    val merkez = Offset(size.width / 2f, size.height / 2f)
    drawCircle(brush = Brush.radialGradient(listOf(vurgu.copy(alpha = 0.15f), vurgu.copy(alpha = 0.6f)), center = merkez, radius = yaricap), radius = yaricap, center = merkez)
    drawCircle(color = ana, radius = yaricap, center = merkez, style = Stroke(width = kalinlik * 0.55f))
    drawCircle(color = vurgu, radius = yaricap * 0.9f, center = merkez, style = Stroke(width = kalinlik * 0.25f))
}

private fun DrawScope.papatya(c: Offset, r: Float, ana: Color, vurgu: Color, kalinlik: Float) {
    val yaprak = Path()
    repeat(8) { i ->
        val aci = Math.toRadians((i * 45).toDouble())
        val x = c.x + (Math.cos(aci) * r * 0.72).toFloat()
        val y = c.y + (Math.sin(aci) * r * 0.72).toFloat()
        yaprak.addOval(Rect(Offset(x - r * 0.22f, y - r * 0.22f), Size(r * 0.44f, r * 0.44f)))
    }
    drawPath(yaprak, color = vurgu, style = Stroke(width = kalinlik * 0.8f))
    drawCircle(color = ana, radius = r * 0.34f, center = c)
}

private fun DrawScope.ilkAdim(c: Offset, r: Float, ana: Color, vurgu: Color, kalinlik: Float) {
    // A single footprint-like arrow rising: first step.
    val p = Path().apply {
        moveTo(c.x, c.y - r * 0.9f)
        lineTo(c.x + r * 0.7f, c.y - r * 0.1f)
        lineTo(c.x + r * 0.26f, c.y - r * 0.1f)
        lineTo(c.x + r * 0.26f, c.y + r * 0.85f)
        lineTo(c.x - r * 0.26f, c.y + r * 0.85f)
        lineTo(c.x - r * 0.26f, c.y - r * 0.1f)
        lineTo(c.x - r * 0.7f, c.y - r * 0.1f)
        close()
    }
    drawPath(p, color = vurgu)
    drawPath(p, color = ana, style = Stroke(width = kalinlik * 0.7f))
}

private fun DrawScope.kusursuz(c: Offset, r: Float, ana: Color, vurgu: Color, kalinlik: Float) {
    val p = Path().apply {
        moveTo(c.x - r * 0.62f, c.y + r * 0.05f)
        lineTo(c.x - r * 0.16f, c.y + r * 0.55f)
        lineTo(c.x + r * 0.68f, c.y - r * 0.55f)
    }
    drawPath(p, color = ana, style = Stroke(width = kalinlik * 1.6f, cap = StrokeCap.Round))
    drawCircle(color = vurgu, radius = r * 0.16f, center = Offset(c.x - r * 0.62f, c.y + r * 0.05f))
}

private fun DrawScope.onluk(c: Offset, r: Float, ana: Color, vurgu: Color, kalinlik: Float) {
    // Roman ten: an X over a bar.
    val xBoy = r * 0.62f
    drawLine(
        color = ana,
        start = Offset(c.x - xBoy, c.y - xBoy),
        end = Offset(c.x + xBoy, c.y + xBoy),
        strokeWidth = kalinlik * 1.2f,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = ana,
        start = Offset(c.x + xBoy, c.y - xBoy),
        end = Offset(c.x - xBoy, c.y + xBoy),
        strokeWidth = kalinlik * 1.2f,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = vurgu,
        start = Offset(c.x - r * 0.9f, c.y + r * 0.78f),
        end = Offset(c.x + r * 0.9f, c.y + r * 0.78f),
        strokeWidth = kalinlik,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.yirmilik(c: Offset, r: Float, ana: Color, vurgu: Color, kalinlik: Float) {
    // Two stacked bars with a crown notch.
    drawRoundBar(c, r, -0.34f, 0.9f, vurgu)
    drawRoundBar(c, r, 0.06f, 1.2f, ana)
    val tac = Path().apply {
        moveTo(c.x - r * 0.5f, c.y - r * 0.5f)
        lineTo(c.x - r * 0.5f, c.y - r * 0.95f)
        lineTo(c.x, c.y - r * 0.7f)
        lineTo(c.x + r * 0.5f, c.y - r * 0.95f)
        lineTo(c.x + r * 0.5f, c.y - r * 0.5f)
        close()
    }
    drawPath(tac, color = vurgu)
    drawPath(tac, color = ana, style = Stroke(width = kalinlik * 0.6f))
}

private fun DrawScope.drawRoundBar(c: Offset, r: Float, dy: Float, genislik: Float, renk: Color) {
    val y = c.y + r * dy
    drawLine(
        color = renk,
        start = Offset(c.x - r * genislik * 0.5f, y),
        end = Offset(c.x + r * genislik * 0.5f, y),
        strokeWidth = r * 0.34f,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.kendiBasina(c: Offset, r: Float, ana: Color, vurgu: Color, kalinlik: Float) {
    // A key: solve it yourself.
    drawCircle(color = vurgu, radius = r * 0.34f, center = Offset(c.x - r * 0.36f, c.y - r * 0.3f))
    drawCircle(
        color = ana,
        radius = r * 0.34f,
        center = Offset(c.x - r * 0.36f, c.y - r * 0.3f),
        style = Stroke(width = kalinlik * 0.9f),
    )
    val govde = Path().apply {
        moveTo(c.x - r * 0.12f, c.y - r * 0.06f)
        lineTo(c.x + r * 0.78f, c.y + r * 0.6f)
    }
    drawPath(govde, color = ana, style = Stroke(width = kalinlik * 1.1f, cap = StrokeCap.Round))
    drawLine(
        color = ana,
        start = Offset(c.x + r * 0.46f, c.y + r * 0.36f),
        end = Offset(c.x + r * 0.68f, c.y + r * 0.16f),
        strokeWidth = kalinlik,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.istikrar(c: Offset, r: Float, ana: Color, vurgu: Color, kalinlik: Float) {
    // Five equal bars: consistency.
    val adet = 5
    val genis = r * 0.17f
    val bosluk = r * 0.29f
    repeat(adet) { i ->
        val x = c.x + (i - (adet - 1) / 2f) * bosluk
        val yukseklik = r * (0.55f + 0.09f * i)
        drawLine(
            color = if (i == adet - 1) vurgu else ana,
            start = Offset(x, c.y + r * 0.72f),
            end = Offset(x, c.y + r * 0.72f - yukseklik),
            strokeWidth = genis,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.seri(c: Offset, r: Float, gun: Int, ana: Color, vurgu: Color, kalinlik: Float) {
    // Flame whose size grows with the streak length.
    val olcek = when (gun) {
        3 -> 0.72f
        7 -> 0.88f
        else -> 1f
    }
    val p = Path().apply {
        moveTo(c.x, c.y - r * olcek)
        cubicTo(
            c.x + r * olcek * 0.9f, c.y - r * olcek * 0.15f,
            c.x + r * olcek * 0.45f, c.y + r * olcek * 0.9f,
            c.x, c.y + r * olcek * 0.9f,
        )
        cubicTo(
            c.x - r * olcek * 0.45f, c.y + r * olcek * 0.9f,
            c.x - r * olcek * 0.9f, c.y - r * olcek * 0.15f,
            c.x, c.y - r * olcek,
        )
        close()
    }
    drawPath(p, color = vurgu)
    drawPath(p, color = ana, style = Stroke(width = kalinlik * 0.8f))
    val ic = Path().apply {
        moveTo(c.x, c.y + r * olcek * 0.1f)
        cubicTo(
            c.x + r * olcek * 0.34f, c.y + r * olcek * 0.35f,
            c.x + r * olcek * 0.2f, c.y + r * olcek * 0.85f,
            c.x, c.y + r * olcek * 0.85f,
        )
        cubicTo(
            c.x - r * olcek * 0.2f, c.y + r * olcek * 0.85f,
            c.x - r * olcek * 0.34f, c.y + r * olcek * 0.35f,
            c.x, c.y + r * olcek * 0.1f,
        )
        close()
    }
    drawPath(ic, color = ana.copy(alpha = 0.85f))
}

private fun DrawScope.yuz(c: Offset, r: Float, ana: Color, vurgu: Color, kalinlik: Float) {
    // A round target with one arrow: one hundred independent solves.
    drawCircle(color = vurgu.copy(alpha = 0.35f), radius = r * 0.78f, center = c)
    drawCircle(color = ana, radius = r * 0.78f, center = c, style = Stroke(width = kalinlik))
    drawCircle(color = vurgu, radius = r * 0.36f, center = c)
    drawCircle(color = ana, radius = r * 0.36f, center = c, style = Stroke(width = kalinlik * 0.8f))
    drawLine(
        color = ana,
        start = Offset(c.x - r * 0.95f, c.y + r * 0.95f),
        end = Offset(c.x, c.y),
        strokeWidth = kalinlik,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.besYuz(c: Offset, r: Float, ana: Color, vurgu: Color, kalinlik: Float) {
    // Five-pointed star with a small sparkle: five hundred.
    val yildiz = Path()
    val disYari = r * 0.86f
    val icYari = r * 0.38f
    repeat(10) { i ->
        val aci = Math.toRadians((-90 + i * 36).toDouble())
        val yari = if (i % 2 == 0) disYari else icYari
        val x = c.x + (Math.cos(aci) * yari).toFloat()
        val y = c.y + (Math.sin(aci) * yari).toFloat()
        if (i == 0) yildiz.moveTo(x, y) else yildiz.lineTo(x, y)
    }
    yildiz.close()
    drawPath(yildiz, color = vurgu)
    drawPath(yildiz, color = ana, style = Stroke(width = kalinlik * 0.8f))
}

/** A neutral sealed medallion: the actual secret badge silhouette must not leak before earning. */
@Composable
fun GizliRozetGorseli(modifier: Modifier = Modifier) {
    val renk = MaterialTheme.colorScheme.onSurfaceVariant
    val zemin = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier) {
        val r = size.minDimension * 0.45f
        val c = center
        drawCircle(zemin, r, c)
        drawCircle(renk.copy(alpha = 0.4f), r, c, style = Stroke(r * 0.06f))
        drawArc(renk, 180f, 180f, false, c - Offset(r * 0.3f, r * 0.48f), Size(r * 0.6f, r * 0.65f), style = Stroke(r * 0.09f))
        drawRoundRect(renk, c - Offset(r * 0.46f, r * 0.12f), Size(r * 0.92f, r * 0.72f), CornerRadius(r * 0.12f))
        drawCircle(zemin, r * 0.09f, c + Offset(0f, r * 0.17f))
    }
}

private fun DrawScope.kitap(c: Offset, r: Float, derece: Int, ana: Color, vurgu: Color, k: Float) {
    val book = Path().apply {
        moveTo(c.x, c.y - r * 0.55f)
        quadraticTo(c.x - r * 0.48f, c.y - r * 0.9f, c.x - r * 0.82f, c.y - r * 0.65f)
        lineTo(c.x - r * 0.82f, c.y + r * 0.65f)
        quadraticTo(c.x - r * 0.35f, c.y + r * 0.4f, c.x, c.y + r * 0.75f)
        quadraticTo(c.x + r * 0.35f, c.y + r * 0.4f, c.x + r * 0.82f, c.y + r * 0.65f)
        lineTo(c.x + r * 0.82f, c.y - r * 0.65f)
        quadraticTo(c.x + r * 0.48f, c.y - r * 0.9f, c.x, c.y - r * 0.55f)
        close()
    }
    drawPath(book, vurgu)
    drawPath(book, ana, style = Stroke(k * 0.65f))
    drawLine(ana, c - Offset(0f, r * 0.55f), c + Offset(0f, r * 0.7f), k * 0.5f)
    repeat(derece) { i ->
        val y = c.y - r * 0.35f + i * r * 0.22f
        drawLine(ana, Offset(c.x + r * 0.22f, y), Offset(c.x + r * 0.6f, y), k * 0.35f, StrokeCap.Round)
    }
    if (derece == 4) isilti(c, r * 1.1f, 3, ana)
}

private fun DrawScope.isilti(c: Offset, r: Float, adet: Int, renk: Color) {
    repeat(adet) { i ->
        val angle = Math.toRadians(-150.0 + i * (120.0 / (adet - 1).coerceAtLeast(1)))
        val p = c + Offset(kotlin.math.cos(angle).toFloat() * r, kotlin.math.sin(angle).toFloat() * r)
        val d = r * 0.1f
        val star = Path().apply {
            moveTo(p.x, p.y - d); lineTo(p.x + d * 0.4f, p.y)
            lineTo(p.x, p.y + d); lineTo(p.x - d * 0.4f, p.y); close()
        }
        drawPath(star, renk)
    }
}

private fun DrawScope.dongu(c: Offset, r: Float, derece: Int, ana: Color, vurgu: Color, k: Float) {
    drawArc(ana, -30f, 290f, false, c - Offset(r * 0.8f, r * 0.8f), Size(r * 1.6f, r * 1.6f), style = Stroke(k * 0.9f, cap = StrokeCap.Round))
    val arrow = Path().apply {
        moveTo(c.x + r, c.y - r * 0.8f); lineTo(c.x + r * 0.1f, c.y - r * 0.68f)
        lineTo(c.x + r * 0.8f, c.y - r * 0.05f); close()
    }
    drawPath(arrow, vurgu)
    drawPath(arrow, ana, style = Stroke(k * 0.5f))
    repeat(derece) { i ->
        drawCircle(vurgu, r * 0.16f, c + Offset((i - (derece - 1) / 2f) * r * 0.38f, r * 0.15f))
    }
}

private fun DrawScope.tac(c: Offset, r: Float, ana: Color, vurgu: Color, k: Float) {
    val path = Path().apply {
        moveTo(c.x - r * 0.65f, c.y + r * 0.6f)
        lineTo(c.x - r * 0.9f, c.y - r * 0.55f)
        lineTo(c.x - r * 0.35f, c.y - r * 0.1f)
        lineTo(c.x, c.y - r * 0.9f)
        lineTo(c.x + r * 0.35f, c.y - r * 0.1f)
        lineTo(c.x + r * 0.9f, c.y - r * 0.55f)
        lineTo(c.x + r * 0.65f, c.y + r * 0.6f); close()
    }
    drawPath(path, vurgu); drawPath(path, ana, style = Stroke(k * 0.7f))
    drawCircle(ana, r * 0.15f, c + Offset(0f, r * 0.2f))
    drawLine(ana, c + Offset(-r * 0.6f, r * 0.85f), c + Offset(r * 0.6f, r * 0.85f), k * 0.6f, StrokeCap.Round)
}

private fun DrawScope.anka(c: Offset, r: Float, ana: Color, vurgu: Color, k: Float) {
    val path = Path().apply {
        moveTo(c.x, c.y + r * 0.9f)
        lineTo(c.x - r * 0.9f, c.y - r * 0.7f)
        lineTo(c.x - r * 0.15f, c.y - r * 0.1f)
        lineTo(c.x, c.y - r * 0.7f)
        lineTo(c.x + r * 0.15f, c.y - r * 0.1f)
        lineTo(c.x + r * 0.9f, c.y - r * 0.7f); close()
    }
    drawPath(path, vurgu); drawPath(path, ana, style = Stroke(k * 0.6f))
    drawCircle(ana, r * 0.13f, c - Offset(0f, r * 0.58f))
}

private fun DrawScope.zincir(c: Offset, r: Float, ana: Color, vurgu: Color, k: Float) {
    rotate(-35f, c) {
        drawRoundRect(vurgu, c - Offset(r * 0.85f, r * 0.42f), Size(r * 1.1f, r * 0.85f), CornerRadius(r * 0.4f), style = Stroke(k))
        drawRoundRect(ana, c - Offset(r * 0.25f, r * 0.42f), Size(r * 1.1f, r * 0.85f), CornerRadius(r * 0.4f), style = Stroke(k))
    }
}

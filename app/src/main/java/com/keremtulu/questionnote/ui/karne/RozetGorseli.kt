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
    val ana = if (kazanildi) colors.primary else colors.outline.copy(alpha = 0.45f)
    val vurgu = if (kazanildi) colors.tertiary else colors.outlineVariant.copy(alpha = 0.5f)
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
            }
        }
    }
}

private fun DrawScope.madalyon(ana: Color, vurgu: Color, kalinlik: Float) {
    val yaricap = size.minDimension * 0.46f
    val merkez = Offset(size.width / 2f, size.height / 2f)
    drawCircle(color = vurgu.copy(alpha = 0.30f), radius = yaricap, center = merkez)
    drawCircle(color = ana, radius = yaricap, center = merkez, style = Stroke(width = kalinlik))
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

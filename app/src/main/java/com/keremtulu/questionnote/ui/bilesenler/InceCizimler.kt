package com.keremtulu.questionnote.ui.bilesenler

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Küçük, elle çizilmiş işaretler.
 *
 * Ekranlardaki ince "basılı kâğıt" hissi tek bir çizgi kalınlığından gelir: hepsi Compose Canvas
 * ile çizilir, bitmap varlık ya da yazı tipi glifi kullanmaz. Böylece aynı ağırlık ve yuvarlak
 * uçlar her yoğunlukta korunur, sembol ailesi karışması olmaz.
 */

/** Marka mührü: küçük bir defter sayfası, bakır alt kenar. */
@Composable
fun Damga(
    modifier: Modifier = Modifier,
    zemin: Color,
    cizgi: Color,
    vurgu: Color,
) {
    Canvas(modifier) {
        val d = size.minDimension
        val u = d / 24f
        drawCircle(zemin, radius = d / 2f)
        val genislik = 9.4f * u
        val yukseklik = 12.2f * u
        val sol = (d - genislik) / 2f
        val ust = (d - yukseklik) / 2f
        val kalin = 1.05f * u
        drawRoundRect(
            color = cizgi,
            topLeft = Offset(sol, ust),
            size = Size(genislik, yukseklik),
            cornerRadius = CornerRadius(1.6f * u, 1.6f * u),
            style = Stroke(kalin),
        )
        drawLine(
            color = cizgi,
            start = Offset(sol + 3.1f * u, ust + 0.8f * u),
            end = Offset(sol + 3.1f * u, ust + yukseklik - 0.8f * u),
            strokeWidth = kalin,
            cap = StrokeCap.Round,
        )
        repeat(2) { satir ->
            val y = ust + 4.5f * u + satir * 2.5f * u
            drawLine(
                color = cizgi,
                start = Offset(sol + 4.7f * u, y),
                end = Offset(sol + 7.8f * u, y),
                strokeWidth = kalin * 0.85f,
                cap = StrokeCap.Round,
            )
        }
        drawLine(
            color = vurgu,
            start = Offset(sol + 1f * u, ust + yukseklik),
            end = Offset(sol + genislik - 1f * u, ust + yukseklik),
            strokeWidth = 1.5f * u,
            cap = StrokeCap.Round,
        )
    }
}

/** Hedef halkası. [isaretSayisi] verilirse çevresine ince ölçek çentikleri eklenir. */
@Composable
fun HedefHalkasi(
    oran: Float,
    modifier: Modifier = Modifier,
    izRenk: Color,
    dolguRenk: Color,
    kalinlik: Dp = 2.5.dp,
    isaretSayisi: Int = 0,
) {
    Canvas(modifier) {
        val kalin = kalinlik.toPx()
        val cap = kalin / 2f
        val alan = Size((size.width - kalin).coerceAtLeast(0f), (size.height - kalin).coerceAtLeast(0f))
        val oranSinirli = oran.coerceIn(0f, 1f)
        if (isaretSayisi in 4..48) {
            val merkez = Offset(size.width / 2f, size.height / 2f)
            val dis = (size.minDimension / 2f - kalin).coerceAtLeast(0.5f)
            val ic = (dis - 2.6.dp.toPx()).coerceAtLeast(0.5f)
            repeat(isaretSayisi) { i ->
                val aci = (i * 2.0 * PI / isaretSayisi - PI / 2.0).toFloat()
                drawLine(
                    color = izRenk,
                    start = Offset(merkez.x + cos(aci) * ic, merkez.y + sin(aci) * ic),
                    end = Offset(merkez.x + cos(aci) * dis, merkez.y + sin(aci) * dis),
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
        drawArc(
            color = izRenk,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(cap, cap),
            size = alan,
            style = Stroke(kalin, cap = StrokeCap.Round),
        )
        if (oranSinirli > 0f) {
            drawArc(
                color = dolguRenk,
                startAngle = -90f,
                sweepAngle = 360f * oranSinirli,
                useCenter = false,
                topLeft = Offset(cap, cap),
                size = alan,
                style = Stroke(kalin, cap = StrokeCap.Round),
            )
        }
    }
}

/** Bölmeli ince ölçek: dolu kısım yuvarlak uçlu, üstünde hassas çentikler. */
@Composable
fun InceOlcek(
    oran: Float,
    modifier: Modifier = Modifier,
    izRenk: Color,
    dolguRenk: Color,
    bolme: Int = 10,
    yukseklik: Dp = 8.dp,
) {
    Canvas(modifier) {
        val yuksek = yukseklik.toPx().coerceAtMost(size.height)
        val yaricap = CornerRadius(yuksek / 2f, yuksek / 2f)
        drawRoundRect(
            color = izRenk,
            topLeft = Offset.Zero,
            size = Size(size.width, yuksek),
            cornerRadius = yaricap,
        )
        val oranSinirli = oran.coerceIn(0f, 1f)
        val dolu = size.width * oranSinirli
        if (dolu > 0f) {
            val kucukYaricap = CornerRadius((yuksek / 2f).coerceAtMost(dolu / 2f), (yuksek / 2f).coerceAtMost(dolu / 2f))
            drawRoundRect(
                color = dolguRenk,
                topLeft = Offset.Zero,
                size = Size(dolu, yuksek),
                cornerRadius = kucukYaricap,
            )
        }
        if (bolme in 2..24) {
            repeat(bolme - 1) { i ->
                val x = size.width * (i + 1) / bolme
                drawLine(
                    color = izRenk,
                    start = Offset(x, yuksek * 0.16f),
                    end = Offset(x, yuksek * 0.84f),
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

/** Seri işareti: küçük, tek çizgi alev. */
@Composable
fun SeriAlevi(modifier: Modifier = Modifier, renk: Color) {
    Canvas(modifier) {
        val u = size.minDimension / 24f
        val alev = Path().apply {
            moveTo(12.4f * u, 3.2f * u)
            cubicTo(12.6f * u, 7.4f * u, 16.8f * u, 8.6f * u, 17.6f * u, 12.4f * u)
            cubicTo(18.4f * u, 16.4f * u, 15.8f * u, 20.4f * u, 12f * u, 20.4f * u)
            cubicTo(8.2f * u, 20.4f * u, 5.6f * u, 16.6f * u, 6.4f * u, 12.8f * u)
            cubicTo(7f * u, 10.2f * u, 8.8f * u, 8.6f * u, 9.6f * u, 6.2f * u)
            cubicTo(10.2f * u, 8.8f * u, 11.4f * u, 9.8f * u, 12.6f * u, 10.8f * u)
            cubicTo(12.2f * u, 8.2f * u, 11.8f * u, 5.2f * u, 12.4f * u, 3.2f * u)
            close()
        }
        drawPath(alev, renk, style = Stroke(1.55f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** Küçük soru işareti: kıvrım ve tek nokta. */
@Composable
fun SoruIsareti(modifier: Modifier = Modifier, renk: Color) {
    Canvas(modifier) {
        val u = size.minDimension / 24f
        val kivrim = Path().apply {
            moveTo(8.6f * u, 8.8f * u)
            cubicTo(9.2f * u, 5.8f * u, 15.2f * u, 5.4f * u, 15.6f * u, 9.4f * u)
            cubicTo(15.9f * u, 12.2f * u, 12.2f * u, 12.3f * u, 12.2f * u, 15.1f * u)
        }
        drawPath(kivrim, renk, style = Stroke(1.8f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(renk, 1.25f * u, Offset(12.2f * u, 18.6f * u))
    }
}

@Composable
fun InceOk(modifier: Modifier = Modifier, renk: Color, asagi: Boolean = false) {
    Canvas(modifier) {
        val u = size.minDimension / 24f
        if (asagi) {
            drawLine(renk, Offset(6f * u, 9.5f * u), Offset(12f * u, 15.5f * u), 1.8f * u, StrokeCap.Round)
            drawLine(renk, Offset(12f * u, 15.5f * u), Offset(18f * u, 9.5f * u), 1.8f * u, StrokeCap.Round)
        } else {
            drawLine(renk, Offset(9f * u, 5f * u), Offset(16f * u, 12f * u), 1.8f * u, StrokeCap.Round)
            drawLine(renk, Offset(16f * u, 12f * u), Offset(9f * u, 19f * u), 1.8f * u, StrokeCap.Round)
        }
    }
}

@Composable
fun KucukArti(modifier: Modifier = Modifier, renk: Color) {
    Canvas(modifier) {
        val u = size.minDimension / 24f
        drawLine(renk, Offset(12f * u, 5.5f * u), Offset(12f * u, 18.5f * u), 1.9f * u, StrokeCap.Round)
        drawLine(renk, Offset(5.5f * u, 12f * u), Offset(18.5f * u, 12f * u), 1.9f * u, StrokeCap.Round)
    }
}

@Composable
fun KucukOnay(modifier: Modifier = Modifier, renk: Color) {
    Canvas(modifier) {
        val u = size.minDimension / 24f
        drawLine(renk, Offset(5f * u, 12.5f * u), Offset(10f * u, 17.5f * u), 2f * u, StrokeCap.Round)
        drawLine(renk, Offset(10f * u, 17.5f * u), Offset(19f * u, 6.5f * u), 2f * u, StrokeCap.Round)
    }
}

@Composable
fun OynatSimgesi(modifier: Modifier = Modifier, renk: Color) {
    Canvas(modifier) {
        val u = size.minDimension / 24f
        val ucgen = Path().apply {
            moveTo(8.5f * u, 5.5f * u)
            lineTo(18.5f * u, 12f * u)
            lineTo(8.5f * u, 18.5f * u)
            close()
        }
        drawPath(ucgen, renk, style = Stroke(1.8f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** Küçük noktalardan oluşan ince yol: ulaşılan noktalar dolu, yol tek saç teli çizgi. */
@Composable
fun YolNoktalari(
    oran: Float,
    modifier: Modifier = Modifier,
    renk: Color,
    dolguRenk: Color,
    noktaSayisi: Int = 7,
) {
    Canvas(modifier) {
        val adet = noktaSayisi.coerceIn(2, 15)
        val merkezY = size.height / 2f
        val sol = size.height / 2f
        val sag = (size.width - size.height / 2f).coerceAtLeast(sol + 1f)
        val noktaYaricap = size.height * 0.22f
        val sinirli = oran.coerceIn(0f, 1f)
        drawLine(
            color = renk,
            start = Offset(sol, merkezY),
            end = Offset(sag, merkezY),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round,
        )
        repeat(adet) { i ->
            val x = sol + (sag - sol) * i / (adet - 1)
            val dolu = adet == 1 || i.toFloat() / (adet - 1) <= sinirli + 0.0001f
            drawCircle(
                color = if (dolu) dolguRenk else renk,
                radius = if (dolu) noktaYaricap else noktaYaricap * 0.72f,
                center = Offset(x, merkezY),
            )
        }
    }
}

/**
 * Kısa gidişat çizgisi (sparkline): son birkaç netin yönü.
 *
 * Yalnız gerçek ölçüm noktaları çizilir; eksen, ızgara veya "oran" yoktur. İki noktadan az
 * veri varsa hiçbir şey çizilmez (çağıran taraf yedek simgeyi gösterir), böylece tek bir
 * kayıttan trend uydurulmaz.
 */
@Composable
fun MiniCizgi(
    noktalar: List<Float>,
    modifier: Modifier = Modifier,
    renk: Color,
    kalinlik: Dp = 1.72.dp,
) {
    if (noktalar.size < 2) return
    Canvas(modifier) {
        val enAz = noktalar.minOrNull() ?: return@Canvas
        val enCok = noktalar.maxOrNull() ?: enAz
        val aralik = (enCok - enAz).coerceAtLeast(0.001f)
        val yaricapPayi = kalinlik.toPx()
        val kullanilabilirYukseklik = (size.height - yaricapPayi * 2f).coerceAtLeast(1f)
        val adim = size.width / (noktalar.size - 1)
        val yol = Path().apply {
            noktalar.forEachIndexed { indeks, deger ->
                val x = adim * indeks
                val oran = ((deger - enAz) / aralik).coerceIn(0f, 1f)
                val y = yaricapPayi + kullanilabilirYukseklik * (1f - oran)
                if (indeks == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(
            path = yol,
            color = renk,
            style = Stroke(width = kalinlik.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        // Son nokta, çizginin nerede bittiğini gösterir.
        val sonOran = ((noktalar.last() - enAz) / aralik).coerceIn(0f, 1f)
        drawCircle(
            color = renk,
            radius = kalinlik.toPx() * 1.15f,
            center = Offset(
                x = size.width,
                y = yaricapPayi + kullanilabilirYukseklik * (1f - sonOran),
            ),
        )
    }
}

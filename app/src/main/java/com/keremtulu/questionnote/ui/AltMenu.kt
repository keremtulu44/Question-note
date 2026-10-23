package com.keremtulu.questionnote.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.ses.HapticTipi
import com.keremtulu.questionnote.data.ses.SesTuruGenis
import com.keremtulu.questionnote.data.ses.SesYoneticisi
import com.keremtulu.questionnote.ui.animasyon.SureToken
import com.keremtulu.questionnote.ui.animasyon.animateFloatPremium
import com.keremtulu.questionnote.ui.animasyon.horizontalSwipeNavigation
import com.keremtulu.questionnote.ui.animasyon.tweenPremium
import androidx.compose.ui.draw.drawBehind
import kotlin.math.cos
import kotlin.math.sin

enum class AltSekme { BUGUN, DERSLER, FILTRELI, SEVIYE, AYARLAR }

/** System insets belong to the bar, not to a hard-coded screen padding. Works with 3-button nav. */
@Composable
fun AltMenu(
    secili: AltSekme,
    onSec: (AltSekme) -> Unit,
    systemInsets: WindowInsets = WindowInsets.navigationBars.union(WindowInsets.displayCutout),
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val feedbackScope = rememberCoroutineScope()
    // At large font scales keep all five 48dp+ destinations, but give the selected label room
    // rather than splitting words or silently shrinking the user's chosen text size.
    val buyukYazi = LocalDensity.current.fontScale >= 1.5f
    fun sekmeyeGec(hedef: AltSekme) {
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
    Surface(color = colors.surface, tonalElevation = 2.dp) {
        Column(
            Modifier.windowInsetsPadding(
                systemInsets.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
            ),
        ) {
            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f))
            Row(
                Modifier.fillMaxWidth()
                    .selectableGroup()
                    .horizontalSwipeNavigation(
                        thresholdDp = 56f,
                        onSwipeLeft = { AltSekme.entries.getOrNull(secili.ordinal + 1)?.let(::sekmeyeGec) },
                        onSwipeRight = { AltSekme.entries.getOrNull(secili.ordinal - 1)?.let(::sekmeyeGec) },
                    )
                    .padding(horizontal = 4.dp),
            ) {
                AltSekme.entries.forEach { sekme ->
                    val aktif = sekme == secili
                    val etiket = stringResource(when (sekme) {
                        AltSekme.BUGUN -> R.string.nav_today
                        AltSekme.DERSLER -> R.string.nav_lessons
                        AltSekme.FILTRELI -> R.string.nav_practice
                        AltSekme.SEVIYE -> R.string.nav_progress
                        AltSekme.AYARLAR -> R.string.nav_settings
                    })
                    Column(
                        Modifier.weight(if (buyukYazi && aktif) 2f else 1f).heightIn(min = 72.dp)
                            .semantics { if (buyukYazi && !aktif) contentDescription = etiket }
                            .selectable(selected = aktif, role = Role.Tab, onClick = { sekmeyeGec(sekme) })
                            .padding(horizontal = 2.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                    val aktiflik = if (aktif) 1f else 0f
                    val gosterge = animateFloatPremium(
                        target = aktiflik,
                        animationSpec = tweenPremium(SureToken.NORMAL),
                        label = "alt-menu-gosterge",
                    )
                    Box(
                        Modifier.size(52.dp, 30.dp).drawBehind {
                            drawRoundRect(
                                color = colors.primaryContainer.copy(alpha = gosterge.value),
                                cornerRadius = CornerRadius(16.dp.toPx()),
                            )
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        MenuSimgesi(sekme, if (aktif) colors.primary else colors.onSurfaceVariant)
                    }
                        if (!buyukYazi || aktif) Text(
                            etiket, fontSize = 11.sp, lineHeight = 14.sp, textAlign = TextAlign.Center,
                            color = if (aktif) colors.primary else colors.onSurfaceVariant,
                            fontWeight = if (aktif) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuSimgesi(sekme: AltSekme, renk: Color) {
    Canvas(Modifier.size(22.dp)) {
        val u = size.minDimension / 24f
        fun p(x: Float, y: Float) = Offset(x * u, y * u)
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
            drawLine(renk, p(x1, y1), p(x2, y2), 1.8f * u, StrokeCap.Round)
        when (sekme) {
            AltSekme.BUGUN -> {
                drawCircle(renk, 8.5f * u, p(12f, 12f), style = Stroke(1.8f * u))
                line(12f, 6f, 12f, 12f); line(12f, 12f, 16f, 14f)
            }
            AltSekme.DERSLER -> {
                drawRoundRect(renk, p(3f, 4f), Size(18f * u, 16f * u), CornerRadius(2f * u), style = Stroke(1.8f * u))
                line(12f, 4f, 12f, 20f); line(6f, 8f, 9f, 8f); line(15f, 8f, 18f, 8f)
            }
            AltSekme.FILTRELI -> {
                val path = Path().apply {
                    moveTo(8f * u, 4f * u); lineTo(20f * u, 12f * u); lineTo(8f * u, 20f * u); close()
                }
                drawPath(path, renk, style = Stroke(1.8f * u, cap = StrokeCap.Round))
            }
            AltSekme.SEVIYE -> {
                line(5f, 19f, 5f, 14f); line(12f, 19f, 12f, 9f); line(19f, 19f, 19f, 4f)
            }
            AltSekme.AYARLAR -> {
                drawCircle(renk, 6f * u, p(12f, 12f), style = Stroke(1.8f * u))
                drawCircle(renk, 2.3f * u, p(12f, 12f), style = Stroke(1.8f * u))
                repeat(8) {
                    val a = it * Math.PI / 4
                    val start = p(12f + cos(a).toFloat() * 7, 12f + sin(a).toFloat() * 7)
                    val end = p(12f + cos(a).toFloat() * 9, 12f + sin(a).toFloat() * 9)
                    drawLine(renk, start, end, 2.4f * u, StrokeCap.Round)
                }
            }
        }
    }
}

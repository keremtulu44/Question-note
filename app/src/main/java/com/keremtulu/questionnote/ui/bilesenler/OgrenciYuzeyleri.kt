package com.keremtulu.questionnote.ui.bilesenler

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.ui.bilesenler.premium.CamYukseklik
import com.keremtulu.questionnote.ui.premium.EtkilesimTuru
import com.keremtulu.questionnote.ui.premium.PremiumButton

/**
 * Plain, matte surfaces for the active student screens.
 * No brand gradient, no glass highlight: the content carries the hierarchy.
 * Parked teacher components deliberately keep their own existing style.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun SadeKart(
    modifier: Modifier = Modifier,
    elevation: CamYukseklik = CamYukseklik.DUSUK,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
    if (onClick == null) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            contentColor = contentColor,
            border = border,
            content = content,
        )
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            contentColor = contentColor,
            border = border,
            content = content,
        )
    }
}

/** One primary action per screen, kept flat so it never looks like a decorative badge. */
@Composable
fun SadeAnaButon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    interactionTur: EtkilesimTuru = EtkilesimTuru.TIKLAMA,
    content: @Composable RowScope.() -> Unit,
) {
    PremiumButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        contentPadding = contentPadding,
        interactionTur = interactionTur,
        content = content,
    )
}

@Composable
fun SayfaBasligi(baslik: String, alt: String? = null, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            baslik,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() },
        )
        if (!alt.isNullOrBlank()) {
            Text(alt, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Plain page header: title on the surface colour, supporting line muted, optional third fact. */
@Composable
fun DuzBaslik(
    baslik: String,
    modifier: Modifier = Modifier,
    alt: String? = null,
    ek: String? = null,
    vurgu: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Column(
        modifier.fillMaxWidth().padding(top = 4.dp, bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            baslik,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        if (!alt.isNullOrBlank()) {
            Text(alt, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!ek.isNullOrBlank()) {
            Text(ek, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = vurgu)
        }
    }
}

/** A destination is one whole tappable row, not a label followed by a second "Open" button. */
@Composable
fun GezinmeSatiri(
    baslik: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    alt: String? = null,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = 64.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    baslik,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!alt.isNullOrBlank()) {
                    Text(alt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

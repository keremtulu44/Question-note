package com.keremtulu.questionnote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R

/** Top-level destinations of the bottom bar. The old side drawer is gone (v1.1.0). */
enum class AltSekme { BUGUN, DERSLER, FILTRELI, SEVIYE, AYARLAR }

/**
 * Hand-built bottom bar (Surface + Row) instead of NavigationBar: it keeps the same look on every
 * Material3 version and avoids extra dependencies.
 */
@Composable
fun AltMenu(secili: AltSekme, onSec: (AltSekme) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surface, tonalElevation = 0.dp) {
        Column {
            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Sekme(AltSekme.BUGUN, secili, stringResource(R.string.nav_today), "◷", onSec)
                Sekme(AltSekme.DERSLER, secili, stringResource(R.string.nav_lessons), "▦", onSec)
                Sekme(AltSekme.FILTRELI, secili, stringResource(R.string.nav_practice), "⌕", onSec)
                Sekme(AltSekme.SEVIYE, secili, stringResource(R.string.nav_progress), "★", onSec)
                Sekme(AltSekme.AYARLAR, secili, stringResource(R.string.nav_settings), "⚙", onSec)
            }
        }
    }
}

@Composable
private fun Sekme(
    secim: AltSekme,
    secili: AltSekme,
    etiket: String,
    ikon: String,
    onSec: (AltSekme) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val aktif = secim == secili
    Column(
        modifier = Modifier
            .width(64.dp)
            .clickable { onSec(secim) }
            .semantics {
                contentDescription = etiket
                selected = aktif
            }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Column(
            modifier = Modifier
                .size(width = 46.dp, height = 26.dp)
                .background(
                    color = if (aktif) colors.primaryContainer else colors.surface,
                    shape = RoundedCornerShape(13.dp),
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                ikon,
                fontSize = 15.sp,
                color = if (aktif) colors.onPrimaryContainer else colors.onSurfaceVariant,
            )
        }
        Text(
            etiket,
            fontSize = 10.sp,
            maxLines = 1,
            color = if (aktif) colors.primary else colors.onSurfaceVariant,
            fontWeight = if (aktif) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

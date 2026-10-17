package com.keremtulu.questionnote.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R

/** Top-level destinations of the bottom bar. The old side drawer is gone (v1.1.0). */
enum class AltSekme { BUGUN, DERSLER, FILTRELI, SEVIYE, AYARLAR }

@Composable
fun AltMenu(secili: AltSekme, onSec: (AltSekme) -> Unit) {
    val colors = MaterialTheme.colorScheme
    NavigationBar(containerColor = colors.surface, tonalElevation = 0.dp) {
        Sekme(AltSekme.BUGUN, secili, stringResource(R.string.nav_today), "◷", onSec)
        Sekme(AltSekme.DERSLER, secili, stringResource(R.string.nav_lessons), "▦", onSec)
        Sekme(AltSekme.FILTRELI, secili, stringResource(R.string.nav_practice), "⌕", onSec)
        Sekme(AltSekme.SEVIYE, secili, stringResource(R.string.nav_progress), "★", onSec)
        Sekme(AltSekme.AYARLAR, secili, stringResource(R.string.nav_settings), "⚙", onSec)
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
    NavigationBarItem(
        selected = aktif,
        onClick = { onSec(secim) },
        icon = {
            Text(ikon, fontSize = 20.sp)
        },
        label = {
            Text(etiket, fontSize = 11.sp)
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = colors.primaryContainer,
            selectedIconColor = colors.onPrimaryContainer,
            selectedTextColor = colors.onSurface,
            unselectedIconColor = colors.onSurfaceVariant,
            unselectedTextColor = colors.onSurfaceVariant,
        ),
    )
}

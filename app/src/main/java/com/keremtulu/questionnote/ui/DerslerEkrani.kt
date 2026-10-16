package com.keremtulu.questionnote.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.ui.bilesenler.DersKartVerisi
import com.keremtulu.questionnote.ui.bilesenler.DersKarti
import com.keremtulu.questionnote.ui.tema.DersRenkleri

private val dersler = listOf(
    DersKartVerisi(R.string.lesson_edebiyat, R.string.symbol_edebiyat, DersRenkleri.EdebiyatAcik, DersRenkleri.EdebiyatKoyu),
    DersKartVerisi(R.string.lesson_matematik, R.string.symbol_matematik, DersRenkleri.MatematikAcik, DersRenkleri.MatematikKoyu),
    DersKartVerisi(R.string.lesson_biyoloji, R.string.symbol_biyoloji, DersRenkleri.BiyolojiAcik, DersRenkleri.BiyolojiKoyu),
    DersKartVerisi(R.string.lesson_kimya, R.string.symbol_kimya, DersRenkleri.KimyaAcik, DersRenkleri.KimyaKoyu),
    DersKartVerisi(R.string.lesson_fizik, R.string.symbol_fizik, DersRenkleri.FizikAcik, DersRenkleri.FizikKoyu),
    DersKartVerisi(R.string.lesson_cografya, R.string.symbol_cografya, DersRenkleri.CografyaAcik, DersRenkleri.CografyaKoyu),
    DersKartVerisi(R.string.lesson_tarih, R.string.symbol_tarih, DersRenkleri.TarihAcik, DersRenkleri.TarihKoyu),
)

@Composable
fun DerslerEkrani() {
    val koyuTema = isSystemInDarkTheme()
    val renkler = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = renkler.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { icBosluk ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(icBosluk),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, top = 18.dp, end = 22.dp, bottom = 14.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_kicker),
                    color = renkler.primary,
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.8.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.home_title),
                    modifier = Modifier.padding(top = 8.dp),
                    color = renkler.onBackground,
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    text = stringResource(R.string.home_subtitle),
                    modifier = Modifier.padding(top = 4.dp),
                    color = renkler.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Surface(
                    modifier = Modifier.padding(top = 14.dp),
                    shape = CircleShape,
                    color = renkler.primaryContainer,
                ) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.lesson_count,
                            dersler.size,
                            dersler.size,
                        ),
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                        color = renkler.onPrimaryContainer,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 156.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 18.dp, top = 2.dp, end = 18.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(dersler, key = { it.ad }) { ders ->
                    DersKarti(veri = ders, koyuTema = koyuTema)
                }
            }
        }
    }
}

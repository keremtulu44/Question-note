package com.keremtulu.questionnote.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.ui.bilesenler.DersKartVerisi
import com.keremtulu.questionnote.ui.bilesenler.DersKarti
import com.keremtulu.questionnote.ui.tema.DersRenkleri
import java.util.Locale
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

private sealed interface DersSecimDurumu {
    data object Yukleniyor : DersSecimDurumu
    data class Hazir(val dersler: List<Ders>) : DersSecimDurumu
    data object Hata : DersSecimDurumu
}

private data class DersKartStili(
    val isaret: Int,
    val acikRenk: androidx.compose.ui.graphics.Color,
    val koyuRenk: androidx.compose.ui.graphics.Color,
)

private val dersKartStilleri = mapOf(
    "edebiyat" to DersKartStili(R.string.symbol_edebiyat, DersRenkleri.EdebiyatAcik, DersRenkleri.EdebiyatKoyu),
    "matematik" to DersKartStili(R.string.symbol_matematik, DersRenkleri.MatematikAcik, DersRenkleri.MatematikKoyu),
    "biyoloji" to DersKartStili(R.string.symbol_biyoloji, DersRenkleri.BiyolojiAcik, DersRenkleri.BiyolojiKoyu),
    "kimya" to DersKartStili(R.string.symbol_kimya, DersRenkleri.KimyaAcik, DersRenkleri.KimyaKoyu),
    "fizik" to DersKartStili(R.string.symbol_fizik, DersRenkleri.FizikAcik, DersRenkleri.FizikKoyu),
    "coğrafya" to DersKartStili(R.string.symbol_cografya, DersRenkleri.CografyaAcik, DersRenkleri.CografyaKoyu),
    "tarih" to DersKartStili(R.string.symbol_tarih, DersRenkleri.TarihAcik, DersRenkleri.TarihKoyu),
)

private val varsayilanDersKartStili = DersKartStili(
    R.string.symbol_diger,
    DersRenkleri.GenelAcik,
    DersRenkleri.GenelKoyu,
)

@Composable
fun DerslerEkrani(
    repository: DersRepository,
    soruEkleModu: Boolean,
    onMenu: () -> Unit,
    onDersSecildi: (Ders) -> Unit,
) {
    var yenileme by remember { mutableIntStateOf(0) }
    val akis = remember(repository, yenileme) { repository.aktifDersleriIzle() }
    val durum = produceState<DersSecimDurumu>(DersSecimDurumu.Yukleniyor, akis) {
        akis.catch { value = DersSecimDurumu.Hata }
            .collect { value = DersSecimDurumu.Hazir(it) }
    }.value
    val koyuTema = isSystemInDarkTheme()
    val colors = MaterialTheme.colorScheme
    val menuDescription = stringResource(R.string.today_menu_description)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, top = 10.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.lessons_kicker),
                        color = colors.primary,
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(if (soruEkleModu) R.string.lessons_choose_title else R.string.lessons_title),
                        modifier = Modifier.padding(top = 4.dp),
                        color = colors.onBackground,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = colors.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant),
                ) {
                    IconButton(
                        onClick = onMenu,
                        modifier = Modifier.size(48.dp).semantics { contentDescription = menuDescription },
                    ) {
                        Text("☰", color = colors.onSurface, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            Text(
                text = stringResource(if (soruEkleModu) R.string.lessons_choose_subtitle else R.string.lessons_browse_subtitle),
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, top = 5.dp, end = 20.dp),
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            Surface(
                modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = colors.secondaryContainer,
            ) {
                Text(
                    text = stringResource(R.string.lessons_high_school_badge),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    color = colors.onSecondaryContainer,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            when (val current = durum) {
                DersSecimDurumu.Yukleniyor -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
                DersSecimDurumu.Hata -> Column(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(stringResource(R.string.home_error_dersler), color = colors.onSurfaceVariant)
                    TextButton(onClick = { yenileme++ }) { Text(stringResource(R.string.topic_retry)) }
                }
                is DersSecimDurumu.Hazir -> {
                    if (current.dersler.isEmpty()) {
                        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.home_empty_dersler), color = colors.onSurfaceVariant)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 156.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(current.dersler, key = { it.id }) { ders ->
                                val style = dersKartStilleri[ders.ad.trim().lowercase(Locale.ROOT)]
                                    ?: varsayilanDersKartStili
                                DersKarti(
                                    veri = DersKartVerisi(
                                        ad = ders.ad,
                                        isaret = style.isaret,
                                        acikRenk = style.acikRenk,
                                        koyuRenk = style.koyuRenk,
                                        soruSayisi = ders.soruSayisi,
                                    ),
                                    koyuTema = koyuTema,
                                    onClick = { onDersSecildi(ders) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

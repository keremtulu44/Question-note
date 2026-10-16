package com.keremtulu.questionnote.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.ui.bilesenler.DersKartVerisi
import com.keremtulu.questionnote.ui.bilesenler.DersKarti
import com.keremtulu.questionnote.ui.tema.DersRenkleri
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import java.util.Locale

private sealed interface DerslerEkraniDurumu {
    data object Yukleniyor : DerslerEkraniDurumu
    data class Hazir(val dersler: List<Ders>) : DerslerEkraniDurumu
    data object Hata : DerslerEkraniDurumu
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

private val varsayilanDersKartStili =
    DersKartStili(R.string.symbol_diger, DersRenkleri.GenelAcik, DersRenkleri.GenelKoyu)

@Composable
fun DerslerEkrani(
    repository: DersRepository,
    onDersSecildi: (Ders) -> Unit,
) {
    val durum = produceState<DerslerEkraniDurumu>(
        initialValue = DerslerEkraniDurumu.Yukleniyor,
        key1 = repository,
    ) {
        repository.aktifDersleriIzle()
            .catch { value = DerslerEkraniDurumu.Hata }
            .collect { dersler -> value = DerslerEkraniDurumu.Hazir(dersler) }
    }.value
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
                        text = dersSayisiMetni(durum),
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                        color = renkler.onPrimaryContainer,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

            when (val current = durum) {
                DerslerEkraniDurumu.Yukleniyor -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                DerslerEkraniDurumu.Hata -> DerslerDurumMesaji(
                    metin = stringResource(R.string.home_error_dersler),
                    modifier = Modifier.weight(1f),
                )

                is DerslerEkraniDurumu.Hazir -> {
                    if (current.dersler.isEmpty()) {
                        DerslerDurumMesaji(
                            metin = stringResource(R.string.home_empty_dersler),
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 156.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(start = 18.dp, top = 2.dp, end = 18.dp, bottom = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(current.dersler, key = { it.id }) { ders ->
                                DersKarti(
                                    veri = ders.toKartVerisi(),
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

@Composable
private fun dersSayisiMetni(durum: DerslerEkraniDurumu): String =
    when (durum) {
        DerslerEkraniDurumu.Yukleniyor -> stringResource(R.string.home_loading_dersler)
        DerslerEkraniDurumu.Hata -> stringResource(R.string.home_error_dersler)
        is DerslerEkraniDurumu.Hazir -> pluralStringResource(
            R.plurals.lesson_count,
            durum.dersler.size,
            durum.dersler.size,
        )
    }

@Composable
private fun DerslerDurumMesaji(
    metin: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = metin,
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

private fun Ders.toKartVerisi(): DersKartVerisi {
    val stil = dersKartStilleri[ad.trim().lowercase(Locale.ROOT)] ?: varsayilanDersKartStili
    return DersKartVerisi(
        ad = ad,
        isaret = stil.isaret,
        acikRenk = stil.acikRenk,
        koyuRenk = stil.koyuRenk,
        soruSayisi = soruSayisi,
    )
}

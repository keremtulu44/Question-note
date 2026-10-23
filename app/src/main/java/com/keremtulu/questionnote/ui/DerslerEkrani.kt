package com.keremtulu.questionnote.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.ui.bilesenler.DersKartVerisi
import com.keremtulu.questionnote.ui.bilesenler.DersKarti
import com.keremtulu.questionnote.ui.animasyon.animateItemInsertion
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.bilesenler.premium.IskeletKart
import com.keremtulu.questionnote.ui.bilesenler.premium.PremiumBosDurum
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
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
    profilAdi: String,
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

    val loadingLabel = stringResource(R.string.home_loading_dersler)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GradyanKart(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                borderAnimation = false,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.lessons_kicker),
                        color = PremiumMarkaRenkler.BakirIsik,
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(if (soruEkleModu) R.string.lessons_choose_title else R.string.lessons_title),
                        color = PremiumMarkaRenkler.Kagit,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(if (soruEkleModu) R.string.lessons_choose_subtitle else R.string.lessons_browse_subtitle),
                        color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.86f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (profilAdi.isNotBlank()) {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.14f),
                            ) {
                                Text(
                                    text = profilAdi,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    color = PremiumMarkaRenkler.Kagit,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                        if (durum is DersSecimDurumu.Hazir) {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.14f),
                            ) {
                                Text(
                                    text = stringResource(R.string.lessons_count, durum.dersler.size),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    color = PremiumMarkaRenkler.Kagit,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    }
                }
            }
            when (val current = durum) {
                DersSecimDurumu.Yukleniyor -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 156.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                        .semantics {
                            contentDescription = loadingLabel
                            liveRegion = LiveRegionMode.Polite
                        },
                    contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(6) { IskeletKart(height = 132, showImage = true, showSubtitle = false) }
                }
                DersSecimDurumu.Hata -> PremiumBosDurum(
                    modifier = Modifier.fillMaxWidth().weight(1f).semantics {
                        liveRegion = LiveRegionMode.Assertive
                    },
                    baslik = stringResource(R.string.lessons_error_title),
                    aciklama = stringResource(R.string.lessons_error_description),
                    butonMetni = stringResource(R.string.topic_retry),
                    onButonTikla = { yenileme++ },
                )
                is DersSecimDurumu.Hazir -> {
                    if (current.dersler.isEmpty()) {
                        PremiumBosDurum(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            baslik = stringResource(R.string.lessons_empty_title),
                            aciklama = stringResource(R.string.lessons_empty_description),
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 156.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            itemsIndexed(current.dersler, key = { _, ders -> ders.id }) { index, ders ->
                                val style = dersKartStilleri[ders.ad.trim().lowercase(Locale.ROOT)]
                                    ?: varsayilanDersKartStili
                                val enter = animateItemInsertion(index)
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
                                    modifier = Modifier.graphicsLayer {
                                        alpha = enter.value
                                        translationY = (1f - enter.value) * 14.dp.toPx()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

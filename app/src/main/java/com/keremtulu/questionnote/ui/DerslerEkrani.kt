package com.keremtulu.questionnote.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.dersAnahtari
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.ui.bilesenler.DersKartVerisi
import com.keremtulu.questionnote.ui.bilesenler.DersKarti
import com.keremtulu.questionnote.ui.animasyon.animateItemInsertion
import com.keremtulu.questionnote.ui.bilesenler.premium.IskeletKart
import com.keremtulu.questionnote.ui.bilesenler.premium.PremiumBosDurum
import com.keremtulu.questionnote.ui.tema.DersRenkleri
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.panel.ekranPencereInsetsi

private sealed interface DersSecimDurumu {
    data object Yukleniyor : DersSecimDurumu
    data class Hazir(val dersler: List<Ders>) : DersSecimDurumu
    data object Hata : DersSecimDurumu
}

internal data class DersKartStili(
    val isaret: Int,
    val acikRenk: androidx.compose.ui.graphics.Color,
    val koyuRenk: androidx.compose.ui.graphics.Color,
) {
    fun vurgu(koyuTema: Boolean): androidx.compose.ui.graphics.Color =
        if (koyuTema) koyuRenk else acikRenk
}

private val dersKartStilleri = mapOf(
    "türkçe" to DersKartStili(R.string.symbol_turkce, DersRenkleri.TurkceAcik, DersRenkleri.TurkceKoyu),
    "edebiyat" to DersKartStili(R.string.symbol_edebiyat, DersRenkleri.EdebiyatAcik, DersRenkleri.EdebiyatKoyu),
    "paragraf" to DersKartStili(R.string.symbol_paragraf, DersRenkleri.ParagrafAcik, DersRenkleri.ParagrafKoyu),
    "matematik" to DersKartStili(R.string.symbol_matematik, DersRenkleri.MatematikAcik, DersRenkleri.MatematikKoyu),
    "geometri" to DersKartStili(R.string.symbol_geometri, DersRenkleri.GeometriAcik, DersRenkleri.GeometriKoyu),
    "problemler" to DersKartStili(R.string.symbol_problemler, DersRenkleri.ProblemlerAcik, DersRenkleri.ProblemlerKoyu),
    "biyoloji" to DersKartStili(R.string.symbol_biyoloji, DersRenkleri.BiyolojiAcik, DersRenkleri.BiyolojiKoyu),
    "kimya" to DersKartStili(R.string.symbol_kimya, DersRenkleri.KimyaAcik, DersRenkleri.KimyaKoyu),
    "fizik" to DersKartStili(R.string.symbol_fizik, DersRenkleri.FizikAcik, DersRenkleri.FizikKoyu),
    "fen bilimleri" to DersKartStili(R.string.symbol_fen, DersRenkleri.FenAcik, DersRenkleri.FenKoyu),
    "coğrafya" to DersKartStili(R.string.symbol_cografya, DersRenkleri.CografyaAcik, DersRenkleri.CografyaKoyu),
    "tarih" to DersKartStili(R.string.symbol_tarih, DersRenkleri.TarihAcik, DersRenkleri.TarihKoyu),
    "t.c. inkılap tarihi ve atatürkçülük" to DersKartStili(R.string.symbol_inkilap, DersRenkleri.InkilapAcik, DersRenkleri.InkilapKoyu),
    "felsefe" to DersKartStili(R.string.symbol_felsefe, DersRenkleri.FelsefeAcik, DersRenkleri.FelsefeKoyu),
    "felsefe grubu" to DersKartStili(R.string.symbol_felsefe_grubu, DersRenkleri.FelsefeAcik, DersRenkleri.FelsefeKoyu),
    "din kültürü ve ahlak bilgisi" to DersKartStili(R.string.symbol_din, DersRenkleri.DinAcik, DersRenkleri.DinKoyu),
    "ingilizce" to DersKartStili(R.string.symbol_ingilizce, DersRenkleri.DilAcik, DersRenkleri.DilKoyu),
    "almanca" to DersKartStili(R.string.symbol_almanca, DersRenkleri.DilAcik, DersRenkleri.DilKoyu),
    "fransızca" to DersKartStili(R.string.symbol_fransizca, DersRenkleri.DilAcik, DersRenkleri.DilKoyu),
    "vatandaşlık" to DersKartStili(R.string.symbol_vatandaslik, DersRenkleri.TarihAcik, DersRenkleri.TarihKoyu),
    "rehberlik" to DersKartStili(R.string.symbol_rehberlik, DersRenkleri.GenelAcik, DersRenkleri.GenelKoyu),
    "müzik" to DersKartStili(R.string.symbol_muzik, DersRenkleri.SanatSporAcik, DersRenkleri.SanatSporKoyu),
    "görsel sanatlar" to DersKartStili(R.string.symbol_sanat, DersRenkleri.SanatSporAcik, DersRenkleri.SanatSporKoyu),
    "beden eğitimi" to DersKartStili(R.string.symbol_spor, DersRenkleri.SanatSporAcik, DersRenkleri.SanatSporKoyu),
)

private val varsayilanDersKartStili = DersKartStili(
    R.string.symbol_diger,
    DersRenkleri.GenelAcik,
    DersRenkleri.GenelKoyu,
)

internal fun dersKartStiliGetir(dersAdi: String): DersKartStili =
    dersKartStilleri[dersAdi.dersAnahtari()] ?: varsayilanDersKartStili

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
        contentWindowInsets = ekranPencereInsetsi(),
    ) { padding ->
        @Composable
        fun DerslerUstBaslik(modifier: Modifier = Modifier) {
            Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                com.keremtulu.questionnote.ui.bilesenler.SayfaBasligi(
                    baslik = stringResource(if (soruEkleModu) R.string.lessons_choose_title else R.string.lessons_title),
                    alt = if (soruEkleModu) stringResource(R.string.lessons_choose_subtitle) else stringResource(R.string.lessons_browse_subtitle),
                )
                val hazir = durum as? DersSecimDurumu.Hazir
                if (!soruEkleModu && hazir != null && hazir.dersler.isNotEmpty()) {
                    GradyanKart(modifier = Modifier.testTag("lessons-summary"), borderAnimation = false) {
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(stringResource(R.string.lessons_summary_kicker), color = Color(0xFFF0D18E),
                                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.lessons_summary_questions, hazir.dersler.sumOf { it.soruSayisi }),
                                color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.lessons_summary_detail, hazir.dersler.size, profilAdi),
                                color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            when (val current = durum) {
                DersSecimDurumu.Yukleniyor -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 156.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                        .semantics {
                            contentDescription = loadingLabel
                            liveRegion = LiveRegionMode.Polite
                        },
                    contentPadding = PaddingValues(start = 18.dp, top = 20.dp, end = 18.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        DerslerUstBaslik(Modifier.padding(bottom = 4.dp))
                    }
                    items(6) { IskeletKart(height = 132, showImage = true, showSubtitle = false) }
                }
                DersSecimDurumu.Hata -> {
                    DerslerUstBaslik(Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
                    PremiumBosDurum(
                        modifier = Modifier.fillMaxWidth().weight(1f).semantics {
                            liveRegion = LiveRegionMode.Assertive
                        },
                        baslik = stringResource(R.string.lessons_error_title),
                        aciklama = stringResource(R.string.lessons_error_description),
                        butonMetni = stringResource(R.string.topic_retry),
                        onButonTikla = { yenileme++ },
                    )
                }
                is DersSecimDurumu.Hazir -> {
                    if (current.dersler.isEmpty()) {
                        DerslerUstBaslik(Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
                        PremiumBosDurum(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            baslik = stringResource(R.string.lessons_empty_title),
                            aciklama = stringResource(R.string.lessons_empty_description),
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 156.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f).testTag("lessons-grid"),
                            contentPadding = PaddingValues(start = 18.dp, top = 20.dp, end = 18.dp, bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            item(span = { GridItemSpan(maxLineSpan) }, key = "dersler-baslik") {
                                DerslerUstBaslik(Modifier.padding(bottom = 4.dp))
                            }
                            itemsIndexed(current.dersler, key = { _, ders -> ders.id }) { index, ders ->
                                val style = dersKartStiliGetir(ders.ad)
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

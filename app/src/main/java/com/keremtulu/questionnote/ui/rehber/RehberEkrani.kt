package com.keremtulu.questionnote.ui.rehber

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.ui.bilesenler.InceOlcek
import com.keremtulu.questionnote.ui.premium.PremiumButton
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import com.keremtulu.questionnote.ui.tema.LocalHareketPolitikasi
import kotlinx.coroutines.launch

/**
 * Uygulama içi rehber.
 *
 * Yedi bölüm gerçek uygulama bileşenleriyle çizilir: tekrar önizlemesi `TekrarKurali`'dan,
 * deneme özeti `ozet()`'ten gelir. Rehber hiçbir demo veriyi veritabanına yazmaz; bölümler
 * arasındaki etkileşim durumu yalnız bu ekranın belleğinde yaşar.
 *
 * Erişilebilirlik: her bölüm kaydırılabilir, ilerleme hem yazıyla hem çubukla verilir,
 * azaltılmış harekette sayfa geçişi animasyonsuz olur ve sistem geri tuşu bölüm bölüm geri gider.
 */
@Composable
internal fun RehberEkrani(
    onKapat: () -> Unit,
    onBitir: () -> Unit,
    onSoruEkle: () -> Unit,
    onDenemeler: () -> Unit,
    onYedek: () -> Unit,
    onBugun: () -> Unit,
    modifier: Modifier = Modifier,
    baslangicIndex: Int = 0,
    onBolumDegisti: (Int) -> Unit = {},
) {
    val bolumler = RehberBolumu.entries
    val baslangic = baslangicIndex.coerceIn(0, bolumler.lastIndex)
    val pagerState = rememberPagerState(initialPage = baslangic) { bolumler.size }
    val durum = remember { RehberDurumu() }
    val hareket = LocalHareketPolitikasi.current
    val kapsam = rememberCoroutineScope()
    var devamNotu by remember(baslangic) { mutableStateOf(baslangic > 0) }

    val sayfa = pagerState.currentPage.coerceIn(0, bolumler.lastIndex)
    val sonSayfa = sayfa == bolumler.lastIndex

    fun git(hedef: Int) {
        val sinirli = hedef.coerceIn(0, bolumler.lastIndex)
        kapsam.launch {
            if (hareket.hareketAcik) pagerState.animateScrollToPage(sinirli) else pagerState.scrollToPage(sinirli)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        onBolumDegisti(pagerState.currentPage)
        if (pagerState.currentPage != baslangic) devamNotu = false
    }

    // Geri tuşu rehberi bir anda kapatmaz: önce bölüm bölüm geri sarar.
    BackHandler {
        if (pagerState.currentPage > 0) git(pagerState.currentPage - 1) else onKapat()
    }

    val ilerlemeMetni = stringResource(R.string.rehber_progress, sayfa + 1, bolumler.size)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        UstBar(
            ilerlemeMetni = ilerlemeMetni,
            oran = (sayfa + 1f) / bolumler.size,
            onKapat = onKapat,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (devamNotu) {
                Text(
                    text = stringResource(R.string.rehber_resume_hint),
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .testTag("rehber-devam-notu"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("rehber-sayfalar"),
                pageSpacing = 12.dp,
                key = { bolumler[it].anahtar },
            ) { indeks ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    RehberBolumuIcerigi(
                        bolum = bolumler[indeks],
                        durum = durum,
                        onSoruEkle = onSoruEkle,
                        onDenemeler = onDenemeler,
                        onYedek = onYedek,
                        onBugun = onBugun,
                    )
                }
            }
        }

        AltBar(
            sayfa = sayfa,
            toplam = bolumler.size,
            sonSayfa = sonSayfa,
            onGeri = { git(sayfa - 1) },
            onIleri = { git(sayfa + 1) },
            onBitir = onBitir,
        )
    }
}

@Composable
private fun UstBar(ilerlemeMetni: String, oran: Float, onKapat: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = stringResource(R.string.rehber_kicker),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.6.sp,
                    color = colors.primary,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.rehber_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = ilerlemeMetni,
                        modifier = Modifier.testTag("rehber-ilerleme"),
                        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            PremiumTextButton(
                onClick = onKapat,
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .testTag("rehber-kapat"),
            ) {
                Text(stringResource(R.string.rehber_close))
            }
        }
        InceOlcek(
            oran = oran,
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clearAndSetSemantics { stateDescription = ilerlemeMetni },
            izRenk = colors.surfaceVariant,
            dolguRenk = colors.primary,
            bolme = 0,
            yukseklik = 5.dp,
        )
    }
}

@Composable
private fun AltBar(
    sayfa: Int,
    toplam: Int,
    sonSayfa: Boolean,
    onGeri: () -> Unit,
    onIleri: () -> Unit,
    onBitir: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (sonSayfa) {
                PremiumOutlinedButton(
                    onClick = onGeri,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("rehber-geri"),
                ) {
                    Text(stringResource(R.string.rehber_prev))
                }
                PremiumButton(
                    onClick = onBitir,
                    modifier = Modifier
                        .weight(1.4f)
                        .heightIn(min = 48.dp)
                        .testTag("rehber-bitir"),
                ) {
                    Text(stringResource(R.string.rehber_finish), fontWeight = FontWeight.Bold)
                }
            } else {
                PremiumTextButton(
                    onClick = onBitir,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("rehber-atla"),
                ) {
                    Text(stringResource(R.string.rehber_skip))
                }
                Box(Modifier.weight(1f))
                if (sayfa > 0) {
                    PremiumOutlinedButton(
                        onClick = onGeri,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("rehber-geri"),
                    ) {
                        Text(stringResource(R.string.rehber_prev))
                    }
                }
                PremiumButton(
                    onClick = onIleri,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("rehber-ileri"),
                ) {
                    Text(stringResource(R.string.rehber_next), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        // Kayarak ilerlemek isteyen için sessiz ipucu; ekran okuyucu bunu tekrar okumaz.
        Text(
            text = stringResource(R.string.rehber_progress, sayfa + 1, toplam),
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics { },
            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
            color = colors.onSurfaceVariant,
        )
    }
}

package com.keremtulu.questionnote.ui.kurulum

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.dersAnahtari
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.dersKartStiliGetir
import com.keremtulu.questionnote.ui.premium.EtkilesimTuru
import com.keremtulu.questionnote.ui.premium.PremiumCard
import com.keremtulu.questionnote.ui.premium.PremiumGradyanButon
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import kotlinx.coroutines.launch

private val VarsayilanBaslangicAnahtarlari = setOf(
    "turkce",
    "matematik",
    "geometri",
    "fizik",
    "kimya",
    "biyoloji",
    "tarih",
    "cografya",
)

private data class DersGrubu(
    @StringRes val baslikRes: Int,
    val dersler: List<Ders>,
)

private fun dersGrubuAnahtari(ad: String): Int = when (ad.dersAnahtari()) {
    "turkce", "turk dili ve edebiyati", "paragraf" -> R.string.onboarding_group_language
    "matematik", "geometri", "sayisal mantik" -> R.string.onboarding_group_math
    "fen bilimleri", "fizik", "kimya", "biyoloji" -> R.string.onboarding_group_science
    "sosyal bilgiler",
    "tarih",
    "cografya",
    "t.c. inkilap tarihi",
    "felsefe",
    "psikoloji",
    "sosyoloji",
    "mantik",
    "din kulturu",
    "vatandaslik",
    -> R.string.onboarding_group_social
    else -> R.string.onboarding_group_foreign
}

private fun dersleriGrupla(dersler: List<Ders>): List<DersGrubu> {
    val siraliBasliklar = listOf(
        R.string.onboarding_group_language,
        R.string.onboarding_group_math,
        R.string.onboarding_group_science,
        R.string.onboarding_group_social,
        R.string.onboarding_group_foreign,
    )
    val harita = dersler.groupBy { dersGrubuAnahtari(it.ad) }
    return siraliBasliklar.mapNotNull { baslik ->
        val liste = harita[baslik].orEmpty()
        if (liste.isEmpty()) null else DersGrubu(baslik, liste)
    }
}

@Composable
fun DersKurulumEkrani(
    dersRepository: DersRepository,
    onTamam: () -> Unit,
) {
    var dersler by remember { mutableStateOf<List<Ders>>(emptyList()) }
    var yukleniyor by remember { mutableStateOf(true) }
    var baslatildi by rememberSaveable { mutableStateOf(false) }
    var seciliListe by rememberSaveable { mutableStateOf(listOf<String>()) }
    var hata by rememberSaveable { mutableStateOf(false) }
    var kaydediliyor by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val secili = remember(seciliListe) { seciliListe.toSet() }
    val gruplar = remember(dersler) { dersleriGrupla(dersler) }

    LaunchedEffect(dersRepository) {
        yukleniyor = true
        val hazir = runCatching { dersRepository.kataloguHazirla() }.getOrElse {
            hata = true
            emptyList()
        }
        dersler = hazir
        if (!baslatildi && hazir.isNotEmpty()) {
            val cekirdek = hazir
                .filter { it.ad.dersAnahtari() in VarsayilanBaslangicAnahtarlari }
                .map { it.id }
            seciliListe = cekirdek.ifEmpty { hazir.map { it.id } }
            baslatildi = true
        }
        yukleniyor = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .testTag("onboarding-screen"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GradyanKart(modifier = Modifier.fillMaxWidth(), borderAnimation = false) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.onboarding_kicker),
                        color = PremiumMarkaRenkler.BakirIsik,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.onboarding_title),
                        color = PremiumMarkaRenkler.Kagit,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.onboarding_subtitle),
                        color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_selected_count, secili.size),
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("onboarding-selected-count"),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    PremiumTextButton(
                        enabled = dersler.isNotEmpty() && secili.size < dersler.size,
                        interactionTur = EtkilesimTuru.TOGGLE_AC,
                        onClick = { seciliListe = dersler.map { it.id } },
                        modifier = Modifier.testTag("onboarding-select-all"),
                    ) {
                        Text(stringResource(R.string.onboarding_select_all))
                    }
                    PremiumTextButton(
                        enabled = secili.isNotEmpty(),
                        interactionTur = EtkilesimTuru.TOGGLE_KAPA,
                        onClick = { seciliListe = emptyList() },
                        modifier = Modifier.testTag("onboarding-clear-all"),
                    ) {
                        Text(stringResource(R.string.onboarding_clear_all))
                    }
                }
            }

            if (yukleniyor && dersler.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("onboarding-lesson-list"),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    gruplar.forEach { grup ->
                        item(key = "header-${grup.baslikRes}") {
                            Text(
                                text = stringResource(grup.baslikRes),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
                            )
                        }
                        val ciftler = grup.dersler.chunked(2)
                        items(ciftler.size, key = { idx -> "row-${grup.baslikRes}-$idx" }) { idx ->
                            val satir = ciftler[idx]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                satir.forEach { ders ->
                                    val seciliMi = ders.id in secili
                                    KurulumDersKarti(
                                        ders = ders,
                                        secili = seciliMi,
                                        modifier = Modifier.weight(1f),
                                        onTikla = {
                                            seciliListe = if (seciliMi) {
                                                seciliListe.filterNot { it == ders.id }
                                            } else {
                                                seciliListe + ders.id
                                            }
                                        },
                                    )
                                }
                                if (satir.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            if (hata) {
                Text(
                    text = stringResource(R.string.onboarding_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            PremiumGradyanButon(
                enabled = !kaydediliyor && secili.isNotEmpty(),
                onClick = {
                    kaydediliyor = true
                    hata = false
                    scope.launch {
                        try {
                            dersRepository.dersSeciminiKaydet(secili)
                            onTamam()
                        } catch (_: Exception) {
                            hata = true
                            kaydediliyor = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding-continue-button"),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_continue),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun KurulumDersKarti(
    ders: Ders,
    secili: Boolean,
    modifier: Modifier = Modifier,
    onTikla: () -> Unit,
) {
    val koyu = isSystemInDarkTheme()
    val colors = MaterialTheme.colorScheme
    val stil = dersKartStiliGetir(ders.ad)
    val vurgu = if (koyu) stil.koyuRenk else stil.acikRenk
    val sembol = stringResource(stil.sembolRes)

    PremiumCard(
        onClick = onTikla,
        modifier = modifier
            .semantics { selected = secili }
            .testTag("onboarding-lesson-${ders.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (secili) {
                vurgu.copy(alpha = if (koyu) 0.18f else 0.11f)
            } else {
                colors.surface
            },
        ),
        border = BorderStroke(
            width = if (secili) 1.5.dp else 1.dp,
            color = if (secili) vurgu.copy(alpha = 0.72f) else colors.outlineVariant.copy(alpha = 0.55f),
        ),
        interactionTur = if (secili) EtkilesimTuru.TOGGLE_KAPA else EtkilesimTuru.TOGGLE_AC,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(vurgu.copy(alpha = if (secili) 0.24f else 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = sembol,
                    style = MaterialTheme.typography.labelLarge,
                    color = vurgu,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = ders.ad,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (secili) FontWeight.Bold else FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (secili) vurgu else colors.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (secili) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.labelSmall,
                        color = ColorOnAccent(koyu),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorOnAccent(koyu: Boolean) =
    if (koyu) MaterialTheme.colorScheme.inverseOnSurface else PremiumMarkaRenkler.Kagit

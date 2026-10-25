package com.keremtulu.questionnote.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.Konu
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.SoruPlanDurumu
import com.keremtulu.questionnote.domain.repository.KonuRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository
import com.keremtulu.questionnote.ui.animasyon.animateItemInsertion
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.bilesenler.premium.IskeletKart
import com.keremtulu.questionnote.ui.bilesenler.premium.PremiumBosDurum
import com.keremtulu.questionnote.ui.bilesenler.premium.VarsayilanIlustrasyon
import com.keremtulu.questionnote.ui.premium.EtkilesimTuru
import com.keremtulu.questionnote.ui.premium.PremiumButton
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import com.keremtulu.questionnote.ui.premium.PremiumCard
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine

private sealed interface KonuListesiDurumu {
    data object Yukleniyor : KonuListesiDurumu
    data class Hazir(val konular: List<Konu>) : KonuListesiDurumu
    data object Hata : KonuListesiDurumu
}

private sealed interface SoruListesiDurumu {
    data object Yukleniyor : SoruListesiDurumu
    data class Hazir(val sorular: List<Soru>) : SoruListesiDurumu
    data object Hata : SoruListesiDurumu
}

@Composable
fun DersCalismaEkrani(
    ders: Ders,
    dersArsivli: Boolean,
    konuRepository: KonuRepository,
    soruRepository: SoruRepository,
    onGeri: () -> Unit,
    onKonulariYonet: () -> Unit,
    onSoruEkle: () -> Unit,
    onSoruAc: (String) -> Unit,
    denemeler: List<com.keremtulu.questionnote.domain.model.DenemeKaydi> = emptyList(),
) {
    var seciliKonuId by remember(ders.id) { mutableStateOf<String?>(null) }
    var konusuzFiltre by remember(ders.id) { mutableStateOf(false) }
    var planDurumuFiltresi by remember(ders.id) { mutableStateOf<SoruPlanDurumu?>(null) }
    var sadeceFavori by remember(ders.id) { mutableStateOf(false) }
    var arama by remember(ders.id) { mutableStateOf("") }
    var yenileme by remember { mutableIntStateOf(0) }
    val konuAkisi = remember(konuRepository, ders.id, yenileme) {
        combine(
            konuRepository.konulariIzle(ders.id, arsivli = false),
            konuRepository.konulariIzle(ders.id, arsivli = true),
        ) { active, archived -> (active + archived).sortedBy { it.sira } }
    }
    val konular = produceState<KonuListesiDurumu>(KonuListesiDurumu.Yukleniyor, konuAkisi) {
        konuAkisi.catch { value = KonuListesiDurumu.Hata }
            .collect { value = KonuListesiDurumu.Hazir(it) }
    }.value
    val soruAkisi = remember(soruRepository, ders.id, seciliKonuId, konusuzFiltre, yenileme) {
        soruRepository.sorulariIzle(
            dersId = ders.id,
            konuId = seciliKonuId,
            konusuz = konusuzFiltre,
        )
    }
    val sorular = produceState<SoruListesiDurumu>(SoruListesiDurumu.Yukleniyor, soruAkisi) {
        soruAkisi.catch { value = SoruListesiDurumu.Hata }
            .collect { value = SoruListesiDurumu.Hazir(it) }
    }.value
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PremiumTextButton(
                    onClick = onGeri,
                    interactionTur = EtkilesimTuru.GERI,
                ) { Text(stringResource(R.string.topic_back_to_lessons)) }
                GradyanKart(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    borderAnimation = false,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = ders.ad,
                            style = MaterialTheme.typography.headlineSmall,
                            color = PremiumMarkaRenkler.Kagit,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = if (dersArsivli) stringResource(R.string.lesson_archived_read_only)
                            else stringResource(R.string.question_screen_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.86f),
                        )
                    }
                }
            }

            if (!dersArsivli) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PremiumTextButton(
                        onClick = onKonulariYonet,
                        modifier = Modifier.weight(1f),
                        interactionTur = EtkilesimTuru.KART_KAPANIS,
                    ) { Text(stringResource(R.string.topic_screen_title)) }
                    PremiumButton(
                        onClick = onSoruEkle,
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.question_add)) }
                }
            }

            when (val current = konular) {
                KonuListesiDurumu.Yukleniyor -> Text(
                    stringResource(R.string.topic_loading),
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
                KonuListesiDurumu.Hata -> PremiumBosDurum(
                    illustration = { VarsayilanIlustrasyon() },
                    baslik = stringResource(R.string.topic_screen_title),
                    aciklama = stringResource(R.string.topic_load_error),
                    butonMetni = stringResource(R.string.topic_retry),
                    onButonTikla = { yenileme++ },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)
                        .semantics { liveRegion = LiveRegionMode.Assertive },
                )
                is KonuListesiDurumu.Hazir -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                            .padding(horizontal = 18.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = seciliKonuId == null && !konusuzFiltre,
                            onClick = { seciliKonuId = null; konusuzFiltre = false },
                            label = { Text(stringResource(R.string.question_filter_all)) },
                        )
                        FilterChip(
                            selected = konusuzFiltre,
                            onClick = { seciliKonuId = null; konusuzFiltre = true },
                            label = { Text(stringResource(R.string.question_filter_unassigned)) },
                        )
                        current.konular.forEach { konu ->
                            FilterChip(
                                selected = seciliKonuId == konu.id,
                                onClick = { seciliKonuId = konu.id; konusuzFiltre = false },
                                label = {
                                    Text(
                                        if (konu.arsivli) stringResource(R.string.topic_name_archived, konu.ad)
                                        else konu.ad,
                                    )
                                },
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = planDurumuFiltresi == null,
                    onClick = { planDurumuFiltresi = null },
                    label = { Text(stringResource(R.string.question_filter_all_statuses)) },
                )
                SoruPlanDurumu.entries.forEach { status ->
                    FilterChip(
                        selected = planDurumuFiltresi == status,
                        onClick = { planDurumuFiltresi = status },
                        label = { Text(stringResource(statusFilterLabel(status))) },
                    )
                }
                FilterChip(
                    selected = sadeceFavori,
                    onClick = { sadeceFavori = !sadeceFavori },
                    label = { Text(stringResource(R.string.question_filter_favorites), maxLines = 1) },
                )
            }
            OutlinedTextField(
                value = arama,
                onValueChange = { arama = it.take(80) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp).heightIn(min = 56.dp),
                label = { Text(stringResource(R.string.question_search), maxLines = 1) },
                singleLine = true,
            )

            when (val current = sorular) {
                SoruListesiDurumu.Yukleniyor -> {
                    val loadingLabel = stringResource(R.string.question_list_loading_label)
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f).semantics {
                            contentDescription = loadingLabel
                            liveRegion = LiveRegionMode.Polite
                        },
                        contentPadding = PaddingValues(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(4) { IskeletKart(height = 96, showImage = true) }
                    }
                }
                SoruListesiDurumu.Hata -> DurumKarti(
                    stringResource(R.string.question_list_error),
                    modifier = Modifier.weight(1f),
                    onRetry = { yenileme++ },
                )
                is SoruListesiDurumu.Hazir -> {
                    val query = arama.trim()
                    val visibleQuestions = current.sorular.filter { question ->
                        (planDurumuFiltresi == null || question.planDurumu == planDurumuFiltresi) &&
                            (!sadeceFavori || question.favori) &&
                            (query.isEmpty() ||
                                question.soruNotu.orEmpty().contains(query, ignoreCase = true) ||
                                question.kaynak.orEmpty().contains(query, ignoreCase = true) ||
                                question.yanlisNedeni.orEmpty().contains(query, ignoreCase = true))
                    }
                    if (visibleQuestions.isEmpty()) {
                        val emptyMessage = when {
                            current.sorular.isNotEmpty() || planDurumuFiltresi != null || seciliKonuId != null ->
                                R.string.question_empty_filtered
                            konusuzFiltre -> R.string.question_empty_unassigned
                            dersArsivli -> R.string.question_empty_archived_lesson
                            else -> R.string.question_empty
                        }
                        DurumKarti(
                            stringResource(emptyMessage),
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentPadding = PaddingValues(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            itemsIndexed(visibleQuestions, key = { _, question -> question.id }) { index, soru ->
                                val enter = animateItemInsertion(index)
                                SoruListeKarti(
                                    soru = soru,
                                    denemeler = denemeler,
                                    onClick = { onSoruAc(soru.id) },
                                    modifier = Modifier.graphicsLayer {
                                        alpha = enter.value
                                        translationY = (1f - enter.value) * 12.dp.toPx()
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

@androidx.annotation.StringRes
private fun statusFilterLabel(status: SoruPlanDurumu): Int = when (status) {
    SoruPlanDurumu.AKTIF -> R.string.question_status_active
    SoruPlanDurumu.ASKIDA -> R.string.question_status_paused
    SoruPlanDurumu.TAMAMLANDI -> R.string.question_status_done
}

@Composable
private fun SoruListeKarti(
    soru: Soru,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    denemeler: List<com.keremtulu.questionnote.domain.model.DenemeKaydi> = emptyList(),
) {
    PremiumCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        interactionTur = EtkilesimTuru.KART_ACILIS,
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = soru.soruNotu?.takeIf { it.isNotBlank() }
                        ?: soru.kaynak?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.question_untitled),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (soru.favori) {
                    Text("★", color = PremiumMarkaRenkler.Bakir, style = MaterialTheme.typography.titleMedium)
                }
            }
            val deneme = com.keremtulu.questionnote.ui.deneme.run { soru.denemeKaydi(denemeler) }
            if (deneme != null) {
                com.keremtulu.questionnote.ui.deneme.DenemeDamgaYazi(deneme)
            } else {
            soru.kaynak?.takeIf { it.isNotBlank() && it != soru.soruNotu }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            }
            soru.kaynakKonumu?.takeIf { it.isNotBlank() }?.let {
                Text(
                    stringResource(R.string.question_source_location) + ": " + it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = stringResource(
                            when (soru.planDurumu) {
                                SoruPlanDurumu.AKTIF -> R.string.question_status_active
                                SoruPlanDurumu.ASKIDA -> R.string.question_status_paused
                                SoruPlanDurumu.TAMAMLANDI -> R.string.question_status_done
                            },
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Text(
                    text = stringResource(R.string.question_photo_badge),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DurumKarti(
    metin: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    val retryLabel = if (onRetry != null) stringResource(R.string.topic_retry) else null
    PremiumBosDurum(
        illustration = { VarsayilanIlustrasyon() },
        baslik = stringResource(
            if (onRetry != null) R.string.question_list_error_title else R.string.question_list_empty_title,
        ),
        aciklama = metin,
        butonMetni = retryLabel,
        onButonTikla = onRetry,
        modifier = modifier.fillMaxWidth().semantics {
            liveRegion = if (onRetry != null) LiveRegionMode.Assertive else LiveRegionMode.Polite
        },
    )
}

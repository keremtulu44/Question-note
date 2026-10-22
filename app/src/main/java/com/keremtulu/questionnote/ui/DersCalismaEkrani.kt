package com.keremtulu.questionnote.ui

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.Konu
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.SoruPlanDurumu
import com.keremtulu.questionnote.domain.repository.KonuRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository
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
) {
    var seciliKonuId by remember(ders.id) { mutableStateOf<String?>(null) }
    var konusuzFiltre by remember(ders.id) { mutableStateOf(false) }
    var planDurumuFiltresi by remember(ders.id) { mutableStateOf<SoruPlanDurumu?>(null) }
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
            Column(Modifier.fillMaxWidth().padding(start = 22.dp, top = 10.dp, end = 22.dp, bottom = 8.dp)) {
                TextButton(onClick = onGeri) { Text(stringResource(R.string.topic_back_to_lessons)) }
                Text(
                    text = ders.ad,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onBackground,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (dersArsivli) stringResource(R.string.lesson_archived_read_only)
                    else stringResource(R.string.question_screen_subtitle),
                    modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!dersArsivli) {
                    TextButton(onClick = onKonulariYonet) { Text(stringResource(R.string.topic_screen_title)) }
                    Button(onClick = onSoruEkle) { Text(stringResource(R.string.question_add)) }
                }
            }

            when (val current = konular) {
                KonuListesiDurumu.Yukleniyor -> Text(
                    stringResource(R.string.topic_loading),
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
                KonuListesiDurumu.Hata -> TextButton(onClick = { yenileme++ }) {
                    Text(stringResource(R.string.topic_load_error))
                }
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
            }

            when (val current = sorular) {
                SoruListesiDurumu.Yukleniyor -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
                SoruListesiDurumu.Hata -> DurumKarti(
                    stringResource(R.string.question_list_error),
                    modifier = Modifier.weight(1f),
                    onRetry = { yenileme++ },
                )
                is SoruListesiDurumu.Hazir -> {
                    val visibleQuestions = current.sorular.filter { question ->
                        planDurumuFiltresi == null || question.planDurumu == planDurumuFiltresi
                    }
                    if (visibleQuestions.isEmpty()) {
                        val emptyMessage = when {
                            current.sorular.isNotEmpty() -> R.string.question_empty_filtered
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
                            items(visibleQuestions, key = { it.id }) { soru ->
                                SoruListeKarti(soru = soru, onClick = { onSoruAc(soru.id) })
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
private fun SoruListeKarti(soru: Soru, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.question_photo_only),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            soru.kaynak?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            soru.kaynakKonumu?.takeIf { it.isNotBlank() }?.let {
                Text(
                    stringResource(R.string.question_source_location) + ": " + it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(
                        when (soru.planDurumu) {
                            com.keremtulu.questionnote.domain.model.SoruPlanDurumu.AKTIF -> R.string.question_status_active
                            com.keremtulu.questionnote.domain.model.SoruPlanDurumu.ASKIDA -> R.string.question_status_paused
                            com.keremtulu.questionnote.domain.model.SoruPlanDurumu.TAMAMLANDI -> R.string.question_status_done
                        },
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
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
    Box(modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(metin, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            onRetry?.let { TextButton(onClick = it) { Text(stringResource(R.string.topic_retry)) } }
        }
    }
}

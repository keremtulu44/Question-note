package com.keremtulu.questionnote.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine

private data class BugunSoruSatiri(
    val soru: Soru,
    val ders: Ders,
)

private sealed interface BugunDurumu {
    data object Yukleniyor : BugunDurumu
    data class Hazir(val sorular: List<BugunSoruSatiri>) : BugunDurumu
    data object Hata : BugunDurumu
}

@Composable
fun BugunEkrani(
    soruRepository: SoruRepository,
    dersRepository: DersRepository,
    onMenu: () -> Unit,
    onSoruEkle: () -> Unit,
    onSoruAc: (Ders, String) -> Unit,
) {
    val today by produceState(initialValue = LocalDate.now()) {
        while (true) {
            val now = ZonedDateTime.now()
            val midnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
            val waitMillis = Duration.between(now, midnight).toMillis().coerceAtLeast(1_000L)
            delay(waitMillis)
            value = LocalDate.now()
        }
    }
    val menuDescription = stringResource(R.string.today_menu_description)
    val dateText = remember(today) {
        today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("tr", "TR")))
            .replaceFirstChar { it.titlecase(Locale("tr", "TR")) }
    }
    var yenileme by remember { mutableIntStateOf(0) }
    val questionsFlow = remember(soruRepository, today.toEpochDay(), yenileme) {
        soruRepository.bugunTekrarlanacaklariIzle(today.toEpochDay().toInt())
    }
    val lessonsFlow = remember(dersRepository) { dersRepository.aktifDersleriIzle() }
    val homeFlow = remember(questionsFlow, lessonsFlow) {
        combine(questionsFlow, lessonsFlow) { questions, lessons ->
            val byId = lessons.associateBy { it.id }
            questions.mapNotNull { question ->
                byId[question.dersId]?.let { lesson -> BugunSoruSatiri(question, lesson) }
            }
        }
    }
    val state = produceState<BugunDurumu>(BugunDurumu.Yukleniyor, homeFlow) {
        homeFlow.catch { value = BugunDurumu.Hata }
            .collect { value = BugunDurumu.Hazir(it) }
    }.value

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onSoruEkle,
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Light)
                Text(
                    text = stringResource(R.string.today_add_question),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    ) { insets ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(insets),
            contentPadding = PaddingValues(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.today_kicker),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            letterSpacing = 1.4.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.today_title),
                            modifier = Modifier.padding(top = 4.dp),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.displaySmall,
                        )
                        Text(
                            text = dateText,
                            modifier = Modifier.padding(top = 2.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                        ),
                    ) {
                        IconButton(
                            onClick = onMenu,
                            modifier = Modifier.size(48.dp).semantics {
                                contentDescription = menuDescription
                            },
                        ) {
                            Text(
                                text = "☰",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                }
            }
            item {
                BugunOdakKarti(
                    dateText = dateText,
                    state = state,
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.today_section_title),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    (state as? BugunDurumu.Hazir)?.let { ready ->
                        if (ready.sorular.isNotEmpty()) {
                            Text(
                                text = ready.sorular.size.toString(),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
            when (val current = state) {
                BugunDurumu.Yukleniyor -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 42.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                }
                BugunDurumu.Hata -> item {
                    DurumKarti(
                        metin = stringResource(R.string.today_load_error),
                        onRetry = { yenileme++ },
                    )
                }
                is BugunDurumu.Hazir -> {
                    if (current.sorular.isEmpty()) {
                        item { BugunBosDurumu() }
                    } else {
                        items(current.sorular, key = { it.soru.id }) { row ->
                            BugunSoruKarti(
                                row = row,
                                today = today,
                                onClick = { onSoruAc(row.ders, row.soru.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BugunOdakKarti(
    dateText: String,
    state: BugunDurumu,
) {
    val colors = MaterialTheme.colorScheme
    val count = (state as? BugunDurumu.Hazir)?.sorular?.size
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.today_focus_label),
                    color = colors.onPrimaryContainer.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.1.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = count?.let { pluralStringResource(R.plurals.today_question_count, it, it) }
                        ?: stringResource(R.string.today_loading_count),
                    modifier = Modifier.padding(top = 8.dp),
                    color = colors.onPrimaryContainer,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = when {
                        count == null -> dateText
                        count == 0 -> stringResource(R.string.today_focus_empty)
                        else -> stringResource(R.string.today_focus_message)
                    },
                    modifier = Modifier.padding(top = 4.dp),
                    color = colors.onPrimaryContainer.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Surface(
                modifier = Modifier.padding(start = 12.dp),
                shape = CircleShape,
                color = colors.surface.copy(alpha = 0.45f),
            ) {
                Box(
                    modifier = Modifier.padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "↻", color = colors.onPrimaryContainer, fontSize = 30.sp)
                }
            }
        }
    }
}

@Composable
private fun BugunSoruKarti(
    row: BugunSoruSatiri,
    today: LocalDate,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val dueDay = row.soru.sonrakiTekrarGun ?: today.toEpochDay().toInt()
    val lateDays = (today.toEpochDay().toInt() - dueDay).coerceAtLeast(0)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.65f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = row.ders.ad,
                    color = colors.primary,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                )
                Surface(
                    shape = CircleShape,
                    color = if (lateDays == 0) colors.secondaryContainer else colors.errorContainer,
                ) {
                    Text(
                        text = if (lateDays == 0) stringResource(R.string.today_due_today)
                        else pluralStringResource(R.plurals.today_overdue_days, lateDays, lateDays),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = if (lateDays == 0) colors.onSecondaryContainer else colors.onErrorContainer,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Text(
                text = row.soru.soruNotu?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.question_photo_only),
                color = colors.onSurface,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.today_photo_count, row.soru.gorseller.count { it.tur == GorselTuru.SORU }),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text("·", color = colors.onSurfaceVariant)
                Text(
                    text = stringResource(R.string.today_tap_to_review),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun BugunBosDurumu() {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.65f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(shape = CircleShape, color = colors.tertiaryContainer) {
                Text(
                    text = "✓",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = colors.onTertiaryContainer,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = stringResource(R.string.today_empty_title),
                color = colors.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.today_empty_description),
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DurumKarti(
    metin: String,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(metin, color = MaterialTheme.colorScheme.onErrorContainer)
            TextButton(onClick = onRetry) { Text(stringResource(R.string.topic_retry)) }
        }
    }
}

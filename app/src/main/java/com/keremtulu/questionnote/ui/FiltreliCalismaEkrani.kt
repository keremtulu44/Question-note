package com.keremtulu.questionnote.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.Konu
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi
import com.keremtulu.questionnote.domain.model.SoruSecenegi
import com.keremtulu.questionnote.domain.model.SoruSecenekTuru
import com.keremtulu.questionnote.domain.repeat.CalismaSorulariniSirala
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.KonuRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository
import java.time.ZoneId
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine

/** State kept by the app shell so opening a question does not discard the chosen filters. */
data class FiltreliCalismaEkraniAyar(
    val filtre: SoruCalismaFiltresi = SoruCalismaFiltresi(),
    val soruSayisi: String = "10",
)

private sealed interface EkranYukleme<out T> {
    data object Yukleniyor : EkranYukleme<Nothing>
    data class Hazir<T>(val deger: T) : EkranYukleme<T>
    data object Hata : EkranYukleme<Nothing>
}

private data class CalismaFiltreSecenekleri(
    val dersler: List<Ders>,
    val konular: List<Konu>,
    val kaynaklar: List<SoruSecenegi>,
    val yanlisNedenleri: List<SoruSecenegi>,
)

private sealed interface FiltreSecimi {
    data object Hepsi : FiltreSecimi
    data object Belirtilmemis : FiltreSecimi
    data class Deger(val metin: String) : FiltreSecimi
}

private data class FiltreSecenegi(
    val secim: FiltreSecimi,
    val etiket: String,
)

@Composable
fun FiltreliCalismaEkrani(
    dersRepository: DersRepository,
    konuRepository: KonuRepository,
    soruRepository: SoruRepository,
    ayar: FiltreliCalismaEkraniAyar,
    onAyarDegisti: (FiltreliCalismaEkraniAyar) -> Unit,
    onGeri: () -> Unit,
    onSoruAc: (Ders, String) -> Unit,
    onOturumBaslat: (List<Soru>, Map<String, Ders>) -> Unit,
) {
    var yenileme by remember { mutableIntStateOf(0) }

    val dersAkisi = remember(dersRepository, yenileme) { dersRepository.aktifDersleriIzle() }
    val konuAkisi = remember(konuRepository, yenileme) { konuRepository.tumunuIzle() }
    val kaynakAkisi = remember(soruRepository, yenileme) {
        soruRepository.secenekleriIzle(SoruSecenekTuru.KAYNAK)
    }
    val nedenAkisi = remember(soruRepository, yenileme) {
        soruRepository.secenekleriIzle(SoruSecenekTuru.YANLIS_NEDENI)
    }
    val seceneklerAkisi = remember(dersAkisi, konuAkisi, kaynakAkisi, nedenAkisi) {
        combine(dersAkisi, konuAkisi, kaynakAkisi, nedenAkisi) { dersler, konular, kaynaklar, nedenler ->
            val etkinDersIdleri = dersler.mapTo(mutableSetOf()) { it.id }
            CalismaFiltreSecenekleri(
                dersler = dersler,
                konular = konular.filter { it.dersId in etkinDersIdleri },
                kaynaklar = kaynaklar,
                yanlisNedenleri = nedenler,
            )
        }
    }
    val secenekDurumu = produceState<EkranYukleme<CalismaFiltreSecenekleri>>(
        EkranYukleme.Yukleniyor,
        seceneklerAkisi,
    ) {
        value = EkranYukleme.Yukleniyor
        seceneklerAkisi.catch { value = EkranYukleme.Hata }
            .collect { value = EkranYukleme.Hazir(it) }
    }.value

    val soruAkisi = remember(soruRepository, ayar.filtre, yenileme) {
        soruRepository.filtreliCalismaSorulariniIzle(ayar.filtre)
    }
    val soruDurumu = produceState<EkranYukleme<List<Soru>>>(
        EkranYukleme.Yukleniyor,
        soruAkisi,
    ) {
        value = EkranYukleme.Yukleniyor
        soruAkisi.catch { value = EkranYukleme.Hata }
            .collect { value = EkranYukleme.Hazir(it) }
    }.value

    val secenekler: CalismaFiltreSecenekleri? = when (val state = secenekDurumu) {
        is EkranYukleme.Hazir -> state.deger
        else -> null
    }
    val dersHaritasi = secenekler?.dersler.orEmpty().associateBy { it.id }
    val tumuSecimi = FiltreSecimi.Hepsi
    val bosSecimi = FiltreSecimi.Belirtilmemis
    val topicSecimi = when {
        ayar.filtre.konusuz -> FiltreSecimi.Belirtilmemis
        ayar.filtre.konuId != null -> FiltreSecimi.Deger(ayar.filtre.konuId)
        else -> tumuSecimi
    }
    val sourceSecimi = when {
        ayar.filtre.kaynakBelirtilmemis -> bosSecimi
        ayar.filtre.kaynak != null -> FiltreSecimi.Deger(ayar.filtre.kaynak)
        else -> tumuSecimi
    }
    val reasonSecimi = when {
        ayar.filtre.yanlisNedeniBelirtilmemis -> bosSecimi
        ayar.filtre.yanlisNedeni != null -> FiltreSecimi.Deger(ayar.filtre.yanlisNedeni)
        else -> tumuSecimi
    }

    val topicOptions = buildList {
        add(FiltreSecenegi(tumuSecimi, ""))
        add(FiltreSecenegi(bosSecimi, ""))
        secenekler?.konular.orEmpty().forEach { topic ->
            val lessonName = dersHaritasi[topic.dersId]?.ad ?: return@forEach
            val topicName = if (topic.arsivli) {
                "${topic.ad} (arşivli)"
            } else {
                topic.ad
            }
            add(FiltreSecenegi(FiltreSecimi.Deger(topic.id), "$lessonName · $topicName"))
        }
    }
    val sourceOptions = buildList {
        add(FiltreSecenegi(tumuSecimi, ""))
        add(FiltreSecenegi(bosSecimi, ""))
        secenekler?.kaynaklar.orEmpty().forEach { add(FiltreSecenegi(FiltreSecimi.Deger(it.ad), it.ad)) }
    }
    val reasonOptions = buildList {
        add(FiltreSecenegi(tumuSecimi, ""))
        add(FiltreSecenegi(bosSecimi, ""))
        secenekler?.yanlisNedenleri.orEmpty().forEach { add(FiltreSecenegi(FiltreSecimi.Deger(it.ad), it.ad)) }
    }

    val allLabel = androidx.compose.ui.res.stringResource(R.string.filtered_practice_all)
    val noTopicLabel = androidx.compose.ui.res.stringResource(R.string.question_filter_unassigned)
    val missingLabel = androidx.compose.ui.res.stringResource(R.string.filtered_practice_missing)
    val topicDropdownOptions = topicOptions.map {
        it.copy(
            etiket = when (it.secim) {
                FiltreSecimi.Hepsi -> allLabel
                FiltreSecimi.Belirtilmemis -> noTopicLabel
                is FiltreSecimi.Deger -> it.etiket
            },
        )
    }
    val textDropdownOptions = { values: List<FiltreSecenegi> ->
        values.map {
            it.copy(
                etiket = when (it.secim) {
                    FiltreSecimi.Hepsi -> allLabel
                    FiltreSecimi.Belirtilmemis -> missingLabel
                    is FiltreSecimi.Deger -> it.etiket
                },
            )
        }
    }

    val sorular: List<Soru> = when (val state = soruDurumu) {
        is EkranYukleme.Hazir -> state.deger
        else -> emptyList()
    }
    val siraliSorular = remember(sorular) {
        CalismaSorulariniSirala.enUzunSureCalisilmayanlar(sorular, ZoneId.systemDefault())
    }
    LaunchedEffect(soruDurumu, siraliSorular.size) {
        if (soruDurumu !is EkranYukleme.Hazir) return@LaunchedEffect
        val uygunSayi = when {
            siraliSorular.isEmpty() -> ""
            else -> (ayar.soruSayisi.toIntOrNull() ?: minOf(10, siraliSorular.size))
                .coerceIn(1, siraliSorular.size)
                .toString()
        }
        if (uygunSayi != ayar.soruSayisi) onAyarDegisti(ayar.copy(soruSayisi = uygunSayi))
    }
    val secilecekSoruSayisi = ayar.soruSayisi.toIntOrNull()
        ?.takeIf { it in 1..siraliSorular.size }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { insets ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(insets),
            contentPadding = PaddingValues(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column {
                    TextButton(onClick = onGeri) { Text("‹  ${androidx.compose.ui.res.stringResource(R.string.menu_today)}") }
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.filtered_practice_kicker),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.filtered_practice_title),
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.filtered_practice_subtitle),
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        FiltreDropdown(
                            baslik = androidx.compose.ui.res.stringResource(R.string.filtered_practice_filter_topic),
                            secim = topicSecimi,
                            secenekler = topicDropdownOptions,
                            onSecim = { choice ->
                                val topic = choice as? FiltreSecimi.Deger
                                onAyarDegisti(
                                    ayar.copy(
                                        filtre = ayar.filtre.copy(
                                            konuId = topic?.metin,
                                            konusuz = choice == FiltreSecimi.Belirtilmemis,
                                        ),
                                    ),
                                )
                            },
                        )
                        FiltreDropdown(
                            baslik = androidx.compose.ui.res.stringResource(R.string.filtered_practice_filter_source),
                            secim = sourceSecimi,
                            secenekler = textDropdownOptions(sourceOptions),
                            onSecim = { choice ->
                                val source = choice as? FiltreSecimi.Deger
                                onAyarDegisti(
                                    ayar.copy(
                                        filtre = ayar.filtre.copy(
                                            kaynak = source?.metin,
                                            kaynakBelirtilmemis = choice == FiltreSecimi.Belirtilmemis,
                                        ),
                                    ),
                                )
                            },
                        )
                        FiltreDropdown(
                            baslik = androidx.compose.ui.res.stringResource(R.string.filtered_practice_filter_reason),
                            secim = reasonSecimi,
                            secenekler = textDropdownOptions(reasonOptions),
                            onSecim = { choice ->
                                val reason = choice as? FiltreSecimi.Deger
                                onAyarDegisti(
                                    ayar.copy(
                                        filtre = ayar.filtre.copy(
                                            yanlisNedeni = reason?.metin,
                                            yanlisNedeniBelirtilmemis = choice == FiltreSecimi.Belirtilmemis,
                                        ),
                                    ),
                                )
                            },
                        )
                        TextButton(onClick = { onAyarDegisti(ayar.copy(filtre = SoruCalismaFiltresi())) }) {
                            Text(androidx.compose.ui.res.stringResource(R.string.filtered_practice_clear_filters))
                        }
                    }
                }
            }

            when (val current = soruDurumu) {
                EkranYukleme.Yukleniyor -> item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.width(20.dp), strokeWidth = 2.dp)
                        Text(androidx.compose.ui.res.stringResource(R.string.filtered_practice_loading))
                    }
                }
                EkranYukleme.Hata -> item {
                    Column {
                        Text(
                            androidx.compose.ui.res.stringResource(R.string.filtered_practice_error),
                            color = MaterialTheme.colorScheme.error,
                        )
                        TextButton(onClick = { yenileme++ }) {
                            Text(androidx.compose.ui.res.stringResource(R.string.question_review_history_retry))
                        }
                    }
                }
                is EkranYukleme.Hazir -> item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            androidx.compose.ui.res.stringResource(R.string.filtered_practice_matched_count, siraliSorular.size),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            androidx.compose.ui.res.stringResource(R.string.filtered_practice_order_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = ayar.soruSayisi,
                                onValueChange = { input ->
                                    onAyarDegisti(ayar.copy(soruSayisi = input.filter { it in '0'..'9' }.take(4)))
                                },
                                label = {
                                    Text(androidx.compose.ui.res.stringResource(R.string.filtered_practice_count_label))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                            Button(
                                modifier = Modifier.height(56.dp),
                                enabled = secenekler != null && secilecekSoruSayisi != null,
                                onClick = {
                                    val count = secilecekSoruSayisi ?: return@Button
                                    val lessonMap = secenekler?.dersler.orEmpty().associateBy { it.id }
                                    val selected = siraliSorular.take(count)
                                    if (selected.isNotEmpty() && selected.all { it.dersId in lessonMap }) {
                                        onOturumBaslat(selected, lessonMap)
                                    }
                                },
                            ) {
                                Text(androidx.compose.ui.res.stringResource(R.string.filtered_practice_start))
                            }
                        }
                    }
                }
            }

            if (secenekDurumu is EkranYukleme.Hata) {
                item {
                    Column {
                        Text(
                            androidx.compose.ui.res.stringResource(R.string.filtered_practice_options_error),
                            color = MaterialTheme.colorScheme.error,
                        )
                        TextButton(onClick = { yenileme++ }) {
                            Text(androidx.compose.ui.res.stringResource(R.string.question_review_history_retry))
                        }
                    }
                }
            }

            if (soruDurumu is EkranYukleme.Hazir && siraliSorular.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            androidx.compose.ui.res.stringResource(R.string.filtered_practice_no_matches),
                            modifier = Modifier.padding(18.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            items(siraliSorular, key = { it.id }) { question ->
                val lesson = dersHaritasi[question.dersId]
                if (lesson != null) {
                    val topic = secenekler?.konular?.firstOrNull { it.id == question.konuId }
                    CalismaSoruKarti(
                        soru = question,
                        ders = lesson,
                        konu = topic,
                        onClick = { onSoruAc(lesson, question.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FiltreDropdown(
    baslik: String,
    secim: FiltreSecimi,
    secenekler: List<FiltreSecenegi>,
    onSecim: (FiltreSecimi) -> Unit,
) {
    var acik by remember(baslik) { mutableStateOf(false) }
    val selectedLabel = secenekler.firstOrNull { it.secim == secim }?.etiket
        ?: secenekler.firstOrNull()?.etiket.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            baslik,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Box {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { acik = true },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        selectedLabel,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text("⌄", modifier = Modifier.padding(start = 8.dp))
                }
            }
            DropdownMenu(
                expanded = acik,
                onDismissRequest = { acik = false },
                modifier = Modifier.heightIn(max = 360.dp),
            ) {
                secenekler.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.etiket) },
                        onClick = {
                            acik = false
                            onSecim(option.secim)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CalismaSoruKarti(
    soru: Soru,
    ders: Ders,
    konu: Konu?,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(
                    R.string.filtered_practice_course_topic,
                    ders.ad,
                    konu?.let { if (it.arsivli) "${it.ad} (arşivli)" else it.ad }
                        ?: androidx.compose.ui.res.stringResource(R.string.question_filter_unassigned),
                ),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.question_photo_only),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            soru.kaynak?.takeIf { it.isNotBlank() }?.let { source ->
                Text(
                    androidx.compose.ui.res.stringResource(R.string.filtered_practice_source_value, source),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            soru.yanlisNedeni?.takeIf { it.isNotBlank() }?.let { reason ->
                Text(
                    androidx.compose.ui.res.stringResource(R.string.filtered_practice_reason_value, reason),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun FiltreliCalismaTamamlandiEkrani(
    tamamlananSoruSayisi: Int,
    onFiltrelereDon: () -> Unit,
    onBugun: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("✓", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.displayMedium)
            Text(
                androidx.compose.ui.res.stringResource(R.string.filtered_practice_complete_title),
                modifier = Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                androidx.compose.ui.res.stringResource(
                    R.string.filtered_practice_complete_description,
                    tamamlananSoruSayisi,
                ),
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(
                modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
                onClick = onFiltrelereDon,
            ) {
                Text(androidx.compose.ui.res.stringResource(R.string.filtered_practice_return))
            }
            TextButton(onClick = onBugun) {
                Text(androidx.compose.ui.res.stringResource(R.string.menu_today))
            }
        }
    }
}

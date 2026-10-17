package com.keremtulu.questionnote.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.Konu
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.SoruGorseli
import com.keremtulu.questionnote.domain.model.SoruIslemSonucu
import com.keremtulu.questionnote.domain.model.SoruPlanDurumu
import com.keremtulu.questionnote.domain.model.TekrarKaydi
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import com.keremtulu.questionnote.domain.repository.KonuRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository
import com.keremtulu.questionnote.ui.bilesenler.SoruGorselResmi
import com.keremtulu.questionnote.ui.bilesenler.SoruGorselTamEkran
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.CancellationException

private sealed interface SoruDetayDurumu {
    data object Yukleniyor : SoruDetayDurumu
    data class Hazir(val soru: Soru) : SoruDetayDurumu
    data object Bulunamadi : SoruDetayDurumu
    data object Hata : SoruDetayDurumu
}

private sealed interface TekrarGecmisiDurumu {
    data object Yukleniyor : TekrarGecmisiDurumu
    data class Hazir(val kayitlar: List<TekrarKaydi>) : TekrarGecmisiDurumu
    data object Hata : TekrarGecmisiDurumu
}

@Composable
fun SoruDetayEkrani(
    soruId: String,
    ders: Ders,
    dersArsivli: Boolean,
    soruRepository: SoruRepository,
    konuRepository: KonuRepository,
    gorselDeposu: LocalSoruGorselDeposu,
    onGeri: () -> Unit,
    onDuzenle: (String) -> Unit,
    onTekrarTamamlandi: () -> Unit,
    tekrarOturumunda: Boolean = false,
    oturumSira: Int? = null,
    oturumToplam: Int? = null,
) {
    var yenileme by remember(soruId) { mutableIntStateOf(0) }
    var cozumAcik by remember(soruId) { mutableStateOf(false) }
    var cozumAcildi by remember(soruId) { mutableStateOf(false) }
    var tekrarKaydediliyor by remember(soruId) { mutableStateOf(false) }
    var silmeOnayi by remember { mutableStateOf(false) }
    var durumMenusu by remember { mutableStateOf(false) }
    var tamEkranGorsel by remember { mutableStateOf<SoruGorseli?>(null) }
    var tekrarGecmisiYenileme by remember(soruId) { mutableIntStateOf(0) }
    val tekrarGecmisiFlow = remember(soruRepository, soruId, tekrarGecmisiYenileme) {
        soruRepository.tekrarGecmisiniIzle(soruId)
    }
    val tekrarGecmisi = produceState<TekrarGecmisiDurumu>(
        TekrarGecmisiDurumu.Yukleniyor,
        tekrarGecmisiFlow,
    ) {
        tekrarGecmisiFlow.catch { value = TekrarGecmisiDurumu.Hata }
            .collect { value = TekrarGecmisiDurumu.Hazir(it) }
    }.value
    val detail = produceState<SoruDetayDurumu>(SoruDetayDurumu.Yukleniyor, soruId, yenileme) {
        value = try {
            soruRepository.soruGetir(soruId)?.let(SoruDetayDurumu::Hazir) ?: SoruDetayDurumu.Bulunamadi
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            SoruDetayDurumu.Hata
        }
    }.value
    val topicFlow = remember(konuRepository, ders.id) {
        combine(
            konuRepository.konulariIzle(ders.id, arsivli = false),
            konuRepository.konulariIzle(ders.id, arsivli = true),
        ) { active, archived -> (active + archived).associateBy { it.id } }
    }
    val topicMap by produceState<Map<String, Konu>>(emptyMap(), topicFlow) {
        topicFlow.catch { value = emptyMap() }.collect { value = it }
    }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val currentQuestion = (detail as? SoruDetayDurumu.Hazir)?.soru

    fun tekrarSonucunuKaydet(soru: Soru, sonuc: TekrarSonucu) {
        if (tekrarKaydediliyor) return
        tekrarKaydediliyor = true
        scope.launch {
            val result = try {
                soruRepository.tekrarKaydet(soru.id, sonuc, cozumAcildi)
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                null
            }
            tekrarKaydediliyor = false
            if (result == SoruIslemSonucu.Basarili) {
                onTekrarTamamlandi()
            } else {
                snackbarHostState.showSnackbar(context.getString(errorResource(result)))
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing,
    ) { padding ->
        when (val state = detail) {
            SoruDetayDurumu.Yukleniyor -> Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) { CircularProgressIndicator() }

            SoruDetayDurumu.Bulunamadi -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(stringResource(R.string.question_not_found), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onGeri) { Text(stringResource(R.string.topic_back_to_lessons)) }
            }

            SoruDetayDurumu.Hata -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(stringResource(R.string.question_detail_error), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = { yenileme++ }) { Text(stringResource(R.string.topic_retry)) }
            }

            is SoruDetayDurumu.Hazir -> {
                val soru = state.soru
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Column {
                            TextButton(onClick = onGeri) { Text(stringResource(R.string.question_back)) }
                            if (oturumSira != null && oturumToplam != null) {
                                Text(
                                    text = stringResource(R.string.filtered_practice_session_progress, oturumSira, oturumToplam),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Text(
                                text = stringResource(R.string.question_photo_only),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = stringResource(R.string.question_lesson_label, ders.ad),
                                modifier = Modifier.padding(top = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            soru.konuId?.let { id ->
                                topicMap[id]?.let { topic ->
                                    Text(
                                        text = stringResource(
                                            if (topic.arsivli) R.string.topic_name_archived else R.string.question_topic_label,
                                            topic.ad,
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
                    soru.kaynak?.takeIf { it.isNotBlank() }?.let { source ->
                        item { BilgiAlani(R.string.question_source, source) }
                    }
                    soru.kaynakKonumu?.takeIf { it.isNotBlank() }?.let { location ->
                        item { BilgiAlani(R.string.question_source_location, location) }
                    }
                    soru.yanlisNedeni?.takeIf { it.isNotBlank() }?.let { reason ->
                        item { BilgiAlani(R.string.question_reason, reason) }
                    }
                    item {
                        Text(
                            text = stringResource(R.string.question_prompt_images),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    items(soru.gorseller.filter { it.tur == com.keremtulu.questionnote.domain.model.GorselTuru.SORU }, key = { it.id }) { image ->
                        val file = gorselDeposu.dosyaGoruntuIcin(image.dosyaAdi)
                        SoruGorselResmi(
                            file = file,
                            contentDescription = stringResource(R.string.question_image_description),
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                            maxDecodeEdge = 3_000,
                            onClick = { if (file != null) tamEkranGorsel = image },
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(onClick = {
                                if (!cozumAcik && (soru.cozumNotu != null || soru.gorseller.any { it.tur == com.keremtulu.questionnote.domain.model.GorselTuru.COZUM })) {
                                    cozumAcildi = true
                                }
                                cozumAcik = !cozumAcik
                            }) {
                                Text(stringResource(if (cozumAcik) R.string.question_hide_solution else R.string.question_show_solution))
                            }
                            Text(
                                text = stringResource(
                                    when (soru.planDurumu) {
                                        SoruPlanDurumu.AKTIF -> R.string.question_status_active
                                        SoruPlanDurumu.ASKIDA -> R.string.question_status_paused
                                        SoruPlanDurumu.TAMAMLANDI -> R.string.question_status_done
                                    },
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                    if (cozumAcik) {
                        if (soru.cozumNotu != null) item { BilgiAlani(R.string.question_solution_note, soru.cozumNotu) }
                        items(soru.gorseller.filter { it.tur == com.keremtulu.questionnote.domain.model.GorselTuru.COZUM }, key = { it.id }) { image ->
                            val file = gorselDeposu.dosyaGoruntuIcin(image.dosyaAdi)
                            SoruGorselResmi(
                                file = file,
                                contentDescription = stringResource(R.string.solution_image_description),
                                modifier = Modifier.fillMaxWidth().height(250.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                maxDecodeEdge = 3_000,
                                onClick = { if (file != null) tamEkranGorsel = image },
                            )
                        }
                    }
                    item {
                        Text(
                            text = stringResource(
                                R.string.question_next_review,
                                soru.sonrakiTekrarGun?.let(::gunTarihi) ?: stringResource(R.string.question_not_scheduled),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    item {
                        Text(
                            text = stringResource(R.string.question_review_history_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    when (val history = tekrarGecmisi) {
                        TekrarGecmisiDurumu.Yukleniyor -> item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Text(stringResource(R.string.question_review_history_loading))
                            }
                        }
                        TekrarGecmisiDurumu.Hata -> item {
                            Column {
                                Text(
                                    stringResource(R.string.question_review_history_load_error),
                                    color = MaterialTheme.colorScheme.error,
                                )
                                TextButton(onClick = { tekrarGecmisiYenileme++ }) {
                                    Text(stringResource(R.string.question_review_history_retry))
                                }
                            }
                        }
                        is TekrarGecmisiDurumu.Hazir -> {
                            if (history.kayitlar.isEmpty()) {
                                item {
                                    Text(
                                        stringResource(R.string.question_review_history_empty),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            } else {
                                items(history.kayitlar, key = { it.id }) { record ->
                                    TekrarGecmisiKarti(record)
                                }
                            }
                        }
                    }
                    val today = LocalDate.now().toEpochDay().toInt()
                    if (
                        !dersArsivli && soru.planDurumu == SoruPlanDurumu.AKTIF &&
                        (tekrarOturumunda || soru.sonrakiTekrarGun?.let { it <= today } == true)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                                colors = androidx.compose.material3.CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        stringResource(R.string.review_record_title),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        stringResource(
                                            if (tekrarOturumunda) R.string.filtered_practice_result_note
                                            else R.string.review_record_subtitle,
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    OutlinedButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !tekrarKaydediliyor,
                                        onClick = { tekrarSonucunuKaydet(soru, TekrarSonucu.COZEMEDIM) },
                                    ) { Text(stringResource(R.string.review_result_not_solved)) }
                                    OutlinedButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !tekrarKaydediliyor,
                                        onClick = { tekrarSonucunuKaydet(soru, TekrarSonucu.ZORLANDIM) },
                                    ) { Text(stringResource(R.string.review_result_struggled)) }
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !tekrarKaydediliyor,
                                        onClick = { tekrarSonucunuKaydet(soru, TekrarSonucu.RAHAT) },
                                    ) {
                                        Text(
                                            stringResource(
                                                if (tekrarKaydediliyor) R.string.review_saving else R.string.review_result_easy,
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (!dersArsivli) {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { onDuzenle(soru.id) }) { Text(stringResource(R.string.question_edit)) }
                                TextButton(onClick = { durumMenusu = true }) { Text(stringResource(R.string.question_plan_status)) }
                                DropdownMenu(expanded = durumMenusu, onDismissRequest = { durumMenusu = false }) {
                                    SoruPlanDurumu.entries.forEach { newStatus ->
                                        DropdownMenuItem(
                                            text = { Text(stringResource(statusResource(newStatus))) },
                                            onClick = {
                                                durumMenusu = false
                                                scope.launch {
                                                    val result = try {
                                                        soruRepository.planDurumunuAyarla(soru.id, newStatus)
                                                    } catch (cancel: CancellationException) {
                                                        throw cancel
                                                    } catch (_: Exception) {
                                                        null
                                                    }
                                                    if (result == SoruIslemSonucu.Basarili) yenileme++
                                                    else snackbarHostState.showSnackbar(context.getString(errorResource(result)))
                                                }
                                            },
                                        )
                                    }
                                }
                                TextButton(onClick = { silmeOnayi = true }) { Text(stringResource(R.string.question_delete)) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (silmeOnayi && currentQuestion != null) {
        AlertDialog(
            onDismissRequest = { silmeOnayi = false },
            title = { Text(stringResource(R.string.question_delete_title)) },
            text = { Text(stringResource(R.string.question_delete_confirm, currentQuestion.gorseller.size)) },
            confirmButton = {
                TextButton(onClick = {
                    silmeOnayi = false
                    scope.launch {
                        val result = try {
                            soruRepository.soruSil(soruId)
                        } catch (cancel: CancellationException) {
                            throw cancel
                        } catch (_: Exception) {
                            null
                        }
                        if (result == SoruIslemSonucu.Basarili) onGeri()
                        else snackbarHostState.showSnackbar(context.getString(errorResource(result)))
                    }
                }) { Text(stringResource(R.string.question_delete)) }
            },
            dismissButton = { TextButton(onClick = { silmeOnayi = false }) { Text(stringResource(R.string.topic_cancel)) } },
        )
    }

    tamEkranGorsel?.let { image ->
        val file = gorselDeposu.dosyaGoruntuIcin(image.dosyaAdi)
        if (file != null) {
            SoruGorselTamEkran(
                file = file,
                contentDescription = stringResource(R.string.question_image_description),
                onDismiss = { tamEkranGorsel = null },
            )
        } else {
            tamEkranGorsel = null
        }
    }
}

@Composable
private fun TekrarGecmisiKarti(record: TekrarKaydi) {
    val resultLabel = when (record.sonuc) {
        TekrarSonucu.COZEMEDIM -> R.string.review_result_not_solved
        TekrarSonucu.ZORLANDIM -> R.string.review_result_struggled
        TekrarSonucu.RAHAT -> R.string.review_result_easy
    }
    val plannedDay = record.planlananGun?.let(::gunTarihi)
        ?: stringResource(R.string.question_review_history_no_planned_day)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = stringResource(resultLabel),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.review_history_actual_day, gunTarihi(record.gun)),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                stringResource(R.string.review_history_planned_day, plannedDay),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                stringResource(
                    if (record.cozumAcildi) R.string.review_history_solution_opened
                    else R.string.review_history_solution_not_opened,
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            if (record.oncekiAralik != null && record.sonrakiAralik != null) {
                Text(
                    stringResource(
                        R.string.review_history_interval,
                        record.oncekiAralik,
                        record.sonrakiAralik,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (record.elleAtandi) {
                Text(
                    stringResource(R.string.review_history_manual),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun BilgiAlani(label: Int, value: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(stringResource(label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 3.dp))
    }
}

private fun gunTarihi(day: Int): String = runCatching {
    LocalDate.ofEpochDay(day.toLong()).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("tr", "TR")))
}.getOrDefault("—")

@androidx.annotation.StringRes
private fun statusResource(status: SoruPlanDurumu): Int = when (status) {
    SoruPlanDurumu.AKTIF -> R.string.question_status_active
    SoruPlanDurumu.ASKIDA -> R.string.question_status_paused
    SoruPlanDurumu.TAMAMLANDI -> R.string.question_status_done
}

@androidx.annotation.StringRes
private fun errorResource(result: SoruIslemSonucu?): Int = when (result) {
    SoruIslemSonucu.Bulunamadi -> R.string.question_error_not_found
    SoruIslemSonucu.GecersizDers -> R.string.question_error_lesson
    SoruIslemSonucu.GecersizKonu -> R.string.question_error_topic
    SoruIslemSonucu.SoruGorseliGerekli -> R.string.question_error_photo_required
    SoruIslemSonucu.GorselGecersiz -> R.string.question_error_photo_invalid
    SoruIslemSonucu.AyniGunTekrarVar -> R.string.question_error_same_day
    SoruIslemSonucu.PlanDurumuKapali -> R.string.question_error_plan_paused
    else -> R.string.question_error_generic
}

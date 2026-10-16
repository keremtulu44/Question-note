package com.keremtulu.questionnote.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.keremtulu.questionnote.data.images.KameraCekimi
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.Konu
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import com.keremtulu.questionnote.domain.model.SoruIslemSonucu
import com.keremtulu.questionnote.domain.model.SoruKaydetmeIstegi
import com.keremtulu.questionnote.domain.model.SoruPlanDurumu
import com.keremtulu.questionnote.domain.repository.KonuRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository
import com.keremtulu.questionnote.ui.bilesenler.SoruGorselResmi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private sealed interface KonuSecenekleriDurumu {
    data object Yukleniyor : KonuSecenekleriDurumu
    data class Hazir(val topics: List<Konu>) : KonuSecenekleriDurumu
    data object Hata : KonuSecenekleriDurumu
}

@Composable
fun SoruFormuEkrani(
    ders: Ders,
    soruId: String?,
    soruRepository: SoruRepository,
    konuRepository: KonuRepository,
    gorselDeposu: LocalSoruGorselDeposu,
    onGeri: () -> Unit,
) {
    var yukleniyor by remember(soruId) { mutableStateOf(soruId != null) }
    var bulunamadi by remember(soruId) { mutableStateOf(false) }
    var formHatasi by remember { mutableStateOf<Int?>(null) }
    var kaydediliyor by remember { mutableStateOf(false) }
    var soruNotu by remember(soruId) { mutableStateOf("") }
    var cozumNotu by remember(soruId) { mutableStateOf("") }
    var kaynak by remember(soruId) { mutableStateOf("") }
    var yanlisNedeni by remember(soruId) { mutableStateOf("") }
    var konuId by remember(soruId) { mutableStateOf<String?>(null) }
    var planDurumu by remember(soruId) { mutableStateOf(SoruPlanDurumu.AKTIF) }
    var favori by remember(soruId) { mutableStateOf(false) }
    var soruGorselleri by remember(soruId) { mutableStateOf<List<SoruGorselGirdisi>>(emptyList()) }
    var cozumGorselleri by remember(soruId) { mutableStateOf<List<SoruGorselGirdisi>>(emptyList()) }
    var konuMenu by remember { mutableStateOf(false) }
    var kameraCekimi by remember { mutableStateOf<KameraCekimi?>(null) }
    var kameraTuru by remember { mutableStateOf(GorselTuru.SORU) }
    var galeriTuru by remember { mutableStateOf(GorselTuru.SORU) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val topicsFlow = remember(konuRepository, ders.id) {
        combine(
            konuRepository.konulariIzle(ders.id, arsivli = false),
            konuRepository.konulariIzle(ders.id, arsivli = true),
        ) { active, archived -> (active + archived).sortedBy { it.sira } }
    }
    val topicsState = produceState<KonuSecenekleriDurumu>(KonuSecenekleriDurumu.Yukleniyor, topicsFlow) {
        topicsFlow.catch { value = KonuSecenekleriDurumu.Hata }
            .collect { value = KonuSecenekleriDurumu.Hazir(it) }
    }.value
    val allTopics = (topicsState as? KonuSecenekleriDurumu.Hazir)?.topics.orEmpty()

    LaunchedEffect(soruId) {
        if (soruId == null) {
            yukleniyor = false
            return@LaunchedEffect
        }
        try {
            val current = soruRepository.soruGetir(soruId)
            if (current == null) {
                bulunamadi = true
            } else {
                soruNotu = current.soruNotu.orEmpty()
                cozumNotu = current.cozumNotu.orEmpty()
                kaynak = current.kaynak.orEmpty()
                yanlisNedeni = current.yanlisNedeni.orEmpty()
                konuId = current.konuId
                planDurumu = current.planDurumu
                favori = current.favori
                soruGorselleri = current.gorseller.filter { it.tur == GorselTuru.SORU }.map { it.toGirdi() }
                cozumGorselleri = current.gorseller.filter { it.tur == GorselTuru.COZUM }.map { it.toGirdi() }
            }
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            formHatasi = R.string.question_error_generic
        } finally {
            yukleniyor = false
        }
    }

    fun cancelAndReturn() {
        if (kaydediliyor) return
        scope.launch {
            (soruGorselleri + cozumGorselleri).filter { it.gecici }
                .forEach { runCatching { gorselDeposu.geciciDosyayiSil(it.dosyaAdi) } }
            onGeri()
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_FOTO_SAYISI),
    ) { uris: List<Uri> ->
        val tur = galeriTuru
        val currentCount = if (tur == GorselTuru.SORU) soruGorselleri.size else cozumGorselleri.size
        if (currentCount + uris.size > MAX_FOTO_SAYISI) {
            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.question_form_photo_limit)) }
        } else if (uris.isNotEmpty()) {
            scope.launch {
                val staged = mutableListOf<SoruGorselGirdisi>()
                var failed = false
                uris.forEach { uri ->
                    try {
                        staged += gorselDeposu.secicidenHazirla(uri, tur, currentCount + staged.size)
                    } catch (cancel: CancellationException) {
                        throw cancel
                    } catch (_: Exception) {
                        failed = true
                    }
                }
                if (tur == GorselTuru.SORU) soruGorselleri = soruGorselleri + staged
                else cozumGorselleri = cozumGorselleri + staged
                if (failed) snackbarHostState.showSnackbar(context.getString(R.string.question_form_photo_error))
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val capture = kameraCekimi
        if (capture != null) {
            val tur = kameraTuru
            kameraCekimi = null
            scope.launch {
                if (success) {
                    try {
                        val count = if (tur == GorselTuru.SORU) soruGorselleri.size else cozumGorselleri.size
                        val staged = gorselDeposu.kameradanHazirla(capture.id, tur, count)
                        if (tur == GorselTuru.SORU) soruGorselleri = soruGorselleri + staged
                        else cozumGorselleri = cozumGorselleri + staged
                    } catch (cancel: CancellationException) {
                        throw cancel
                    } catch (_: Exception) {
                        snackbarHostState.showSnackbar(context.getString(R.string.question_form_photo_error))
                    }
                } else {
                    gorselDeposu.kameraCekiminiSil(capture.id)
                }
            }
        }
    }

    fun launchCamera(tur: GorselTuru) {
        val count = if (tur == GorselTuru.SORU) soruGorselleri.size else cozumGorselleri.size
        if (count >= MAX_FOTO_SAYISI) {
            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.question_form_photo_limit)) }
            return
        }
        try {
            val capture = gorselDeposu.kameraCekimiOlustur()
            kameraCekimi = capture
            kameraTuru = tur
            cameraLauncher.launch(capture.uri)
        } catch (_: Exception) {
            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.question_form_photo_error)) }
        }
    }

    fun launchPicker(tur: GorselTuru) {
        galeriTuru = tur
        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    BackHandler(enabled = true, onBack = ::cancelAndReturn)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing,
    ) { padding ->
        when {
            yukleniyor -> Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) { CircularProgressIndicator() }
            bulunamadi -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(stringResource(R.string.question_not_found), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onGeri) { Text(stringResource(R.string.question_back)) }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column {
                        TextButton(onClick = ::cancelAndReturn, enabled = !kaydediliyor) {
                            Text(stringResource(R.string.question_form_cancel))
                        }
                        Text(
                            stringResource(if (soruId == null) R.string.question_form_add_title else R.string.question_form_edit_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(ders.ad, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                item {
                    OutlinedTextField(
                        value = soruNotu,
                        onValueChange = { soruNotu = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !kaydediliyor,
                        label = { Text(stringResource(R.string.question_form_note_label)) },
                        minLines = 2,
                    )
                }
                item {
                    Column {
                        Text(stringResource(R.string.question_form_topic_label), style = MaterialTheme.typography.labelLarge)
                        TextButton(onClick = { konuMenu = true }) {
                            Text(
                                allTopics.firstOrNull { it.id == konuId }?.let { topic ->
                                    if (topic.arsivli) context.getString(R.string.topic_name_archived, topic.ad) else topic.ad
                                } ?: stringResource(R.string.question_form_choose_topic),
                            )
                        }
                        DropdownMenu(expanded = konuMenu, onDismissRequest = { konuMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.question_form_no_topic)) },
                                onClick = { konuId = null; konuMenu = false },
                            )
                            allTopics.filter { !it.arsivli || it.id == konuId }.forEach { topic ->
                                DropdownMenuItem(
                                    text = {
                                        Text(if (topic.arsivli) stringResource(R.string.topic_name_archived, topic.ad) else topic.ad)
                                    },
                                    enabled = !topic.arsivli,
                                    onClick = { konuId = topic.id; konuMenu = false },
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = kaynak,
                        onValueChange = { kaynak = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !kaydediliyor,
                        singleLine = true,
                        label = { Text(stringResource(R.string.question_form_source_label)) },
                    )
                }
                item {
                    OutlinedTextField(
                        value = yanlisNedeni,
                        onValueChange = { yanlisNedeni = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !kaydediliyor,
                        minLines = 2,
                        label = { Text(stringResource(R.string.question_form_reason_label)) },
                    )
                }
                item {
                    ImageSection(
                        title = stringResource(R.string.question_form_question_images),
                        images = soruGorselleri,
                        imageStore = gorselDeposu,
                        enabled = !kaydediliyor,
                        onCamera = { launchCamera(GorselTuru.SORU) },
                        onPicker = { launchPicker(GorselTuru.SORU) },
                        onImagesChanged = { soruGorselleri = it },
                    )
                }
                item {
                    OutlinedTextField(
                        value = cozumNotu,
                        onValueChange = { cozumNotu = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !kaydediliyor,
                        minLines = 2,
                        label = { Text(stringResource(R.string.question_form_solution_note_label)) },
                    )
                }
                item {
                    ImageSection(
                        title = stringResource(R.string.question_form_solution_images),
                        images = cozumGorselleri,
                        imageStore = gorselDeposu,
                        enabled = !kaydediliyor,
                        onCamera = { launchCamera(GorselTuru.COZUM) },
                        onPicker = { launchPicker(GorselTuru.COZUM) },
                        onImagesChanged = { cozumGorselleri = it },
                    )
                }
                item {
                    FilterChip(
                        selected = favori,
                        onClick = { favori = !favori },
                        label = { Text(stringResource(R.string.question_form_favorite)) },
                        enabled = !kaydediliyor,
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.question_plan_status), style = MaterialTheme.typography.labelLarge)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            SoruPlanDurumu.entries.forEach { status ->
                                FilterChip(
                                    selected = planDurumu == status,
                                    onClick = { planDurumu = status },
                                    label = { Text(stringResource(statusLabel(status))) },
                                    enabled = !kaydediliyor,
                                )
                            }
                        }
                    }
                }
                formHatasi?.let { id ->
                    item { Text(stringResource(id), color = MaterialTheme.colorScheme.error) }
                }
                item {
                    Button(
                        onClick = {
                            if (kaydediliyor) return@Button
                            if (soruGorselleri.none { it.tur == GorselTuru.SORU }) {
                                formHatasi = R.string.question_error_photo_required
                                return@Button
                            }
                            kaydediliyor = true
                            formHatasi = null
                            scope.launch {
                                val result = try {
                                    soruRepository.soruKaydet(
                                        SoruKaydetmeIstegi(
                                            id = soruId,
                                            dersId = ders.id,
                                            konuId = konuId,
                                            soruNotu = soruNotu,
                                            cozumNotu = cozumNotu,
                                            kaynak = kaynak,
                                            yanlisNedeni = yanlisNedeni,
                                            planDurumu = planDurumu,
                                            favori = favori,
                                            gorseller = (soruGorselleri.mapIndexed { index, image -> image.copy(tur = GorselTuru.SORU, sira = index) } +
                                                cozumGorselleri.mapIndexed { index, image -> image.copy(tur = GorselTuru.COZUM, sira = index) }),
                                        ),
                                    )
                                } catch (cancel: CancellationException) {
                                    throw cancel
                                } catch (_: Exception) {
                                    null
                                }
                                if (result == SoruIslemSonucu.Basarili) {
                                    onGeri()
                                } else {
                                    kaydediliyor = false
                                    formHatasi = errorLabel(result)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !kaydediliyor,
                    ) {
                        Text(stringResource(if (kaydediliyor) R.string.question_form_saving else R.string.question_form_save))
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageSection(
    title: String,
    images: List<SoruGorselGirdisi>,
    imageStore: LocalSoruGorselDeposu,
    enabled: Boolean,
    onCamera: () -> Unit,
    onPicker: () -> Unit,
    onImagesChanged: (List<SoruGorselGirdisi>) -> Unit,
) {
    val scope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onCamera, enabled = enabled) { Text(stringResource(R.string.question_form_take_photo)) }
            TextButton(onClick = onPicker, enabled = enabled) { Text(stringResource(R.string.question_form_add_page)) }
        }
        if (images.isEmpty()) {
            Text(
                stringResource(R.string.question_form_no_photos),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(images, key = { it.id }) { image ->
                    val file = imageStore.dosyaGoruntuIcin(image)
                    Column(Modifier.width(188.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        SoruGorselResmi(
                            file = file,
                            contentDescription = stringResource(R.string.question_image_description),
                            modifier = Modifier.fillMaxWidth().height(148.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            maxDecodeEdge = 480,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            TextButton(
                                enabled = enabled && image.sira > 0,
                                onClick = {
                                    val index = images.indexOfFirst { it.id == image.id }
                                    if (index > 0) onImagesChanged(images.swapped(index, index - 1))
                                },
                            ) { Text("↑", color = MaterialTheme.colorScheme.primary) }
                            TextButton(
                                enabled = enabled && image.sira < images.lastIndex,
                                onClick = {
                                    val index = images.indexOfFirst { it.id == image.id }
                                    if (index in 0 until images.lastIndex) onImagesChanged(images.swapped(index, index + 1))
                                },
                            ) { Text("↓", color = MaterialTheme.colorScheme.primary) }
                            TextButton(
                                enabled = enabled,
                                onClick = {
                                    if (image.gecici) scope.launch { imageStore.geciciDosyayiSil(image.dosyaAdi) }
                                    onImagesChanged(images.filterNot { it.id == image.id }.mapIndexed { order, item -> item.copy(sira = order) })
                                },
                            ) { Text(stringResource(R.string.question_form_remove_image)) }
                        }
                    }
                }
            }
        }
    }
}

private fun List<SoruGorselGirdisi>.swapped(first: Int, second: Int): List<SoruGorselGirdisi> =
    toMutableList().also { items ->
        val current = items[first]
        items[first] = items[second]
        items[second] = current
    }.mapIndexed { index, item -> item.copy(sira = index) }

private fun com.keremtulu.questionnote.domain.model.SoruGorseli.toGirdi() = SoruGorselGirdisi(
    id = id,
    tur = tur,
    sira = sira,
    dosyaAdi = dosyaAdi,
    gecici = false,
    mimeType = mimeType,
    genislik = genislik,
    yukseklik = yukseklik,
    boyut = boyut,
    sha256 = sha256,
)

@androidx.annotation.StringRes
private fun statusLabel(status: SoruPlanDurumu): Int = when (status) {
    SoruPlanDurumu.AKTIF -> R.string.question_status_active
    SoruPlanDurumu.ASKIDA -> R.string.question_status_paused
    SoruPlanDurumu.TAMAMLANDI -> R.string.question_status_done
}

@androidx.annotation.StringRes
private fun errorLabel(result: SoruIslemSonucu?): Int = when (result) {
    SoruIslemSonucu.Bulunamadi -> R.string.question_error_not_found
    SoruIslemSonucu.GecersizDers -> R.string.question_error_lesson
    SoruIslemSonucu.GecersizKonu -> R.string.question_error_topic
    SoruIslemSonucu.SoruGorseliGerekli -> R.string.question_error_photo_required
    SoruIslemSonucu.GorselGecersiz -> R.string.question_error_photo_invalid
    else -> R.string.question_error_generic
}

private const val MAX_FOTO_SAYISI = 20

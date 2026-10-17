package com.keremtulu.questionnote.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.KameraCekimi
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.Konu
import com.keremtulu.questionnote.domain.model.KonuIslemSonucu
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import com.keremtulu.questionnote.domain.model.SoruIslemSonucu
import com.keremtulu.questionnote.domain.model.SoruKaydetmeIstegi
import com.keremtulu.questionnote.domain.model.SoruPlanDurumu
import com.keremtulu.questionnote.domain.model.SoruSecenekTuru
import com.keremtulu.questionnote.domain.model.SoruSecenegi
import com.keremtulu.questionnote.domain.model.SoruSecenegiEklemeSonucu
import com.keremtulu.questionnote.domain.model.normalizeSoruSecenegiAdi
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

private sealed interface SoruKatalogDurumu {
    data object Yukleniyor : SoruKatalogDurumu
    data class Hazir(
        val kaynaklar: List<SoruSecenegi>,
        val nedenler: List<SoruSecenegi>,
    ) : SoruKatalogDurumu
    data object Hata : SoruKatalogDurumu
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
    var yuklemeBasarisiz by remember(soruId) { mutableStateOf(false) }
    var formHatasi by remember { mutableStateOf<Int?>(null) }
    var kaydediliyor by remember { mutableStateOf(false) }
    var cozumNotu by remember(soruId) { mutableStateOf("") }
    var kaynak by remember(soruId) { mutableStateOf("") }
    var kaynakKonumu by remember(soruId) { mutableStateOf("") }
    var yanlisNedeni by remember(soruId) { mutableStateOf("") }
    var katalogDialogTuru by remember { mutableStateOf<SoruSecenekTuru?>(null) }
    var yeniSecenekAdi by remember { mutableStateOf("") }
    var katalogDialogHatasi by remember { mutableStateOf<Int?>(null) }
    var katalogKaydediliyor by remember { mutableStateOf(false) }
    var konuId by remember(soruId) { mutableStateOf<String?>(null) }
    var planDurumu by remember(soruId) { mutableStateOf(SoruPlanDurumu.AKTIF) }
    var favori by remember(soruId) { mutableStateOf(false) }
    var soruGorselleri by remember(soruId) { mutableStateOf<List<SoruGorselGirdisi>>(emptyList()) }
    var cozumGorselleri by remember(soruId) { mutableStateOf<List<SoruGorselGirdisi>>(emptyList()) }
    var konuMenu by remember { mutableStateOf(false) }
    var konuEklemeDialogu by remember { mutableStateOf(false) }
    var yeniKonuAdi by remember { mutableStateOf("") }
    var konuEklemeHatasi by remember { mutableStateOf<Int?>(null) }
    var konuKaydediliyor by remember { mutableStateOf(false) }
    var yeniEklenenKonuAdi by remember { mutableStateOf<String?>(null) }
    var konuAkisiYenileme by remember { mutableIntStateOf(0) }
    var kameraCekimi by remember { mutableStateOf<KameraCekimi?>(null) }
    var kameraTuru by remember { mutableStateOf(GorselTuru.SORU) }
    var galeriTuru by remember { mutableStateOf(GorselTuru.SORU) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val topicsFlow = remember(konuRepository, ders.id, konuAkisiYenileme) {
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

    LaunchedEffect(yeniEklenenKonuAdi, allTopics) {
        val yeniAd = yeniEklenenKonuAdi ?: return@LaunchedEffect
        val yeniKonu = allTopics.firstOrNull {
            it.dersId == ders.id && !it.arsivli && it.ad == yeniAd
        } ?: return@LaunchedEffect
        konuId = yeniKonu.id
        yeniEklenenKonuAdi = null
    }
    val catalogFlow = remember(soruRepository) {
        combine(
            soruRepository.secenekleriIzle(SoruSecenekTuru.KAYNAK),
            soruRepository.secenekleriIzle(SoruSecenekTuru.YANLIS_NEDENI),
        ) { sources, reasons -> SoruKatalogDurumu.Hazir(sources, reasons) }
    }
    val catalogState = produceState<SoruKatalogDurumu>(SoruKatalogDurumu.Yukleniyor, catalogFlow) {
        catalogFlow.catch { value = SoruKatalogDurumu.Hata }
            .collect { value = it }
    }.value
    val catalog = catalogState as? SoruKatalogDurumu.Hazir

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
                cozumNotu = current.cozumNotu.orEmpty()
                kaynak = current.kaynak.orEmpty()
                kaynakKonumu = current.kaynakKonumu.orEmpty()
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
            yuklemeBasarisiz = true
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

    fun addCatalogOption(tur: SoruSecenekTuru) {
        katalogDialogTuru = tur
        yeniSecenekAdi = ""
        katalogDialogHatasi = null
    }

    fun selectCatalogOption(tur: SoruSecenekTuru, ad: String) {
        when (tur) {
            SoruSecenekTuru.KAYNAK -> kaynak = ad
            SoruSecenekTuru.YANLIS_NEDENI -> yanlisNedeni = ad
        }
    }

    fun openTopicDialog() {
        yeniKonuAdi = ""
        konuEklemeHatasi = null
        konuEklemeDialogu = true
    }

    fun saveTopic() {
        if (konuKaydediliyor) return
        konuKaydediliyor = true
        konuEklemeHatasi = null
        val ad = yeniKonuAdi.trim()
        scope.launch {
            val result = try {
                konuRepository.konuEkle(ders.id, ad)
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                null
            }
            when (result) {
                KonuIslemSonucu.Basarili -> {
                    yeniEklenenKonuAdi = ad
                    konuEklemeDialogu = false
                    yeniKonuAdi = ""
                    if (topicsState is KonuSecenekleriDurumu.Hata) konuAkisiYenileme++
                    snackbarHostState.showSnackbar(context.getString(R.string.topic_added))
                }
                KonuIslemSonucu.BosAd -> konuEklemeHatasi = R.string.topic_error_empty_name
                KonuIslemSonucu.AyniAdVar -> konuEklemeHatasi = R.string.topic_error_duplicate_name
                KonuIslemSonucu.Bulunamadi -> konuEklemeHatasi = R.string.topic_not_found
                null -> konuEklemeHatasi = R.string.topic_save_error
            }
            konuKaydediliyor = false
        }
    }

    fun saveQuestion() {
        if (kaydediliyor) return
        if (soruGorselleri.none { it.tur == GorselTuru.SORU }) {
            formHatasi = R.string.question_error_photo_required
            return
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
                        cozumNotu = cozumNotu,
                        kaynak = kaynak,
                        kaynakKonumu = kaynakKonumu,
                        yanlisNedeni = yanlisNedeni,
                        planDurumu = planDurumu,
                        favori = favori,
                        gorseller = soruGorselleri.mapIndexed { index, image ->
                            image.copy(tur = GorselTuru.SORU, sira = index)
                        } + cozumGorselleri.mapIndexed { index, image ->
                            image.copy(tur = GorselTuru.COZUM, sira = index)
                        },
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
    }

    BackHandler(enabled = true, onBack = ::cancelAndReturn)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing,
        bottomBar = {
            if (!yukleniyor && !bulunamadi && !yuklemeBasarisiz) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        formHatasi?.let { errorId ->
                            Text(
                                text = stringResource(errorId),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Button(
                            onClick = ::saveQuestion,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !kaydediliyor,
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(vertical = 14.dp),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (kaydediliyor) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.height(18.dp).width(18.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }
                                Text(
                                    stringResource(
                                        if (kaydediliyor) R.string.question_form_saving
                                        else R.string.question_form_save,
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    }
                }
            }
        },
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
            yuklemeBasarisiz -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(stringResource(R.string.question_error_generic), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onGeri) { Text(stringResource(R.string.question_back)) }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextButton(
                            onClick = ::cancelAndReturn,
                            enabled = !kaydediliyor,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("←", style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(R.string.question_form_cancel))
                            }
                        }
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(22.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    stringResource(R.string.question_form_header_kicker),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.labelMedium,
                                    letterSpacing = 1.2.sp,
                                )
                                Text(
                                    stringResource(
                                        if (soruId == null) R.string.question_form_add_title
                                        else R.string.question_form_edit_title,
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    stringResource(R.string.question_form_header_hint),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.86f),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                                ) {
                                    Text(
                                        ders.ad,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    SoruFormBolumu(
                        adim = "01",
                        baslik = stringResource(R.string.question_form_section_question),
                        aciklama = stringResource(R.string.question_form_section_question_hint),
                    ) {
                        ImageSection(
                            images = soruGorselleri,
                            imageStore = gorselDeposu,
                            enabled = !kaydediliyor,
                            onCamera = { launchCamera(GorselTuru.SORU) },
                            onPicker = { launchPicker(GorselTuru.SORU) },
                            onImagesChanged = {
                                soruGorselleri = it
                                if (it.isNotEmpty() && formHatasi == R.string.question_error_photo_required) {
                                    formHatasi = null
                                }
                            },
                        )
                    }
                }
                item {
                    SoruFormBolumu(
                        adim = "02",
                        baslik = stringResource(R.string.question_form_section_details),
                        aciklama = stringResource(R.string.question_form_section_details_hint),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (topicsState is KonuSecenekleriDurumu.Hata) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        stringResource(R.string.topic_load_error),
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    TextButton(
                                        enabled = !kaydediliyor,
                                        onClick = { konuAkisiYenileme++ },
                                    ) { Text(stringResource(R.string.topic_retry)) }
                                }
                            }
                            FormKonuSecicisi(
                                topics = allTopics,
                                selectedTopicId = konuId,
                                enabled = !kaydediliyor,
                                menuExpanded = konuMenu,
                                onMenuExpandedChange = { konuMenu = it },
                                onTopicSelect = { konuId = it },
                                onAddTopic = ::openTopicDialog,
                            )
                            if (catalogState is SoruKatalogDurumu.Hata) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.errorContainer,
                                ) {
                                    Text(
                                        stringResource(R.string.question_form_catalog_load_error),
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                            SoruSecenegiSecici(
                                baslik = stringResource(R.string.question_form_source_label),
                                secimYok = stringResource(R.string.question_form_source_choose),
                                secimiTemizle = stringResource(R.string.question_form_source_clear),
                                ekleEtiketi = stringResource(R.string.question_form_add_source),
                                bosListeMesaji = stringResource(R.string.question_form_catalog_no_options),
                                secenekler = catalog?.kaynaklar.orEmpty(),
                                secilenAd = kaynak,
                                etkin = !kaydediliyor,
                                onSec = {
                                    kaynak = it
                                    if (it.isBlank()) kaynakKonumu = ""
                                },
                                onEkle = { addCatalogOption(SoruSecenekTuru.KAYNAK) },
                            )
                            OutlinedTextField(
                                value = kaynakKonumu,
                                onValueChange = { kaynakKonumu = it },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !kaydediliyor,
                                singleLine = true,
                                label = { Text(stringResource(R.string.question_form_source_location_label)) },
                                shape = RoundedCornerShape(16.dp),
                            )
                            SoruSecenegiSecici(
                                baslik = stringResource(R.string.question_form_reason_label),
                                secimYok = stringResource(R.string.question_form_reason_choose),
                                secimiTemizle = stringResource(R.string.question_form_reason_clear),
                                ekleEtiketi = stringResource(R.string.question_form_add_reason),
                                bosListeMesaji = stringResource(R.string.question_form_catalog_no_options),
                                secenekler = catalog?.nedenler.orEmpty(),
                                secilenAd = yanlisNedeni,
                                etkin = !kaydediliyor,
                                onSec = { yanlisNedeni = it },
                                onEkle = { addCatalogOption(SoruSecenekTuru.YANLIS_NEDENI) },
                            )
                        }
                    }
                }
                item {
                    SoruFormBolumu(
                        adim = "03",
                        baslik = stringResource(R.string.question_form_section_solution),
                        aciklama = stringResource(R.string.question_form_section_solution_hint),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            OutlinedTextField(
                                value = cozumNotu,
                                onValueChange = { cozumNotu = it },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !kaydediliyor,
                                minLines = 3,
                                label = { Text(stringResource(R.string.question_form_solution_note_label)) },
                                shape = RoundedCornerShape(16.dp),
                            )
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
                    }
                }
                item {
                    SoruFormBolumu(
                        adim = "04",
                        baslik = stringResource(R.string.question_form_section_plan),
                        aciklama = stringResource(R.string.question_form_section_plan_hint),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            FilterChip(
                                selected = favori,
                                onClick = { favori = !favori },
                                label = { Text(stringResource(R.string.question_form_favorite)) },
                                enabled = !kaydediliyor,
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    stringResource(R.string.question_plan_status),
                                    style = MaterialTheme.typography.labelLarge,
                                )
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
                    }
                }
            }
        }
    }

    if (konuEklemeDialogu) {
        AlertDialog(
            onDismissRequest = { if (!konuKaydediliyor) konuEklemeDialogu = false },
            title = { Text(stringResource(R.string.topic_dialog_add_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(R.string.question_form_topic_lesson_hint, ders.ad),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedTextField(
                        value = yeniKonuAdi,
                        onValueChange = {
                            yeniKonuAdi = it
                            konuEklemeHatasi = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !konuKaydediliyor,
                        singleLine = true,
                        label = { Text(stringResource(R.string.topic_name_label)) },
                        isError = konuEklemeHatasi != null,
                        shape = RoundedCornerShape(16.dp),
                    )
                    konuEklemeHatasi?.let { errorId ->
                        Text(
                            stringResource(errorId),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !konuKaydediliyor,
                    onClick = ::saveTopic,
                ) {
                    Text(
                        stringResource(
                            if (konuKaydediliyor) R.string.topic_saving else R.string.topic_save,
                        ),
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !konuKaydediliyor,
                    onClick = { konuEklemeDialogu = false },
                ) { Text(stringResource(R.string.topic_cancel)) }
            },
        )
    }

    katalogDialogTuru?.let { tur ->
        val title = if (tur == SoruSecenekTuru.KAYNAK) {
            stringResource(R.string.question_form_add_source_title)
        } else {
            stringResource(R.string.question_form_add_reason_title)
        }
        AlertDialog(
            onDismissRequest = { if (!katalogKaydediliyor) katalogDialogTuru = null },
            title = { Text(title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = yeniSecenekAdi,
                        onValueChange = { yeniSecenekAdi = it },
                        enabled = !katalogKaydediliyor,
                        singleLine = true,
                        label = { Text(stringResource(R.string.question_form_catalog_name)) },
                    )
                    katalogDialogHatasi?.let { errorId ->
                        Text(stringResource(errorId), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !katalogKaydediliyor,
                    onClick = {
                        if (katalogKaydediliyor) return@TextButton
                        katalogKaydediliyor = true
                        katalogDialogHatasi = null
                        scope.launch {
                            val result = try {
                                soruRepository.secenekEkle(tur, yeniSecenekAdi)
                            } catch (_: Exception) {
                                null
                            }
                            when (result) {
                                is SoruSecenegiEklemeSonucu.Basarili -> {
                                    selectCatalogOption(tur, result.secenek.ad)
                                    katalogDialogTuru = null
                                    yeniSecenekAdi = ""
                                }
                                is SoruSecenegiEklemeSonucu.ZatenVar -> {
                                    selectCatalogOption(tur, result.secenek.ad)
                                    katalogDialogTuru = null
                                    yeniSecenekAdi = ""
                                    snackbarHostState.showSnackbar(context.getString(R.string.question_form_catalog_duplicate))
                                }
                                SoruSecenegiEklemeSonucu.BosAd ->
                                    katalogDialogHatasi = R.string.question_form_catalog_empty
                                SoruSecenegiEklemeSonucu.CokUzunAd ->
                                    katalogDialogHatasi = R.string.question_form_catalog_too_long
                                null -> katalogDialogHatasi = R.string.question_form_catalog_error
                            }
                            katalogKaydediliyor = false
                        }
                    },
                ) { Text(stringResource(R.string.question_form_catalog_save)) }
            },
            dismissButton = {
                TextButton(
                    enabled = !katalogKaydediliyor,
                    onClick = { katalogDialogTuru = null },
                ) { Text(stringResource(R.string.question_form_catalog_cancel)) }
            },
        )
    }
}

@Composable
private fun SoruFormBolumu(
    adim: String,
    baslik: String,
    aciklama: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(shape = CircleShape, color = colors.primaryContainer) {
                    Box(Modifier.width(42.dp).height(42.dp), contentAlignment = Alignment.Center) {
                        Text(
                            adim,
                            color = colors.onPrimaryContainer,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(baslik, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        aciklama,
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun FormKonuSecicisi(
    topics: List<Konu>,
    selectedTopicId: String?,
    enabled: Boolean,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onTopicSelect: (String?) -> Unit,
    onAddTopic: () -> Unit,
) {
    val selectedTopic = topics.firstOrNull { it.id == selectedTopicId }
    val selectedLabel = selectedTopic?.let { topic ->
        if (topic.arsivli) stringResource(R.string.topic_name_archived, topic.ad) else topic.ad
    } ?: stringResource(R.string.question_form_choose_topic)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(R.string.question_form_topic_label), style = MaterialTheme.typography.labelLarge)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { onMenuExpandedChange(true) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 13.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            selectedLabel,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            "⌄",
                            modifier = Modifier.padding(start = 12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { onMenuExpandedChange(false) },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.question_form_no_topic)) },
                        onClick = {
                            onTopicSelect(null)
                            onMenuExpandedChange(false)
                        },
                    )
                    topics.filter { !it.arsivli || it.id == selectedTopicId }.forEach { topic ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (topic.arsivli) stringResource(R.string.topic_name_archived, topic.ad)
                                    else topic.ad,
                                )
                            },
                            enabled = !topic.arsivli,
                            onClick = {
                                onTopicSelect(topic.id)
                                onMenuExpandedChange(false)
                            },
                        )
                    }
                }
            }
            TextButton(onClick = onAddTopic, enabled = enabled) {
                Text(
                    stringResource(R.string.question_form_add_topic),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun SoruSecenegiSecici(
    baslik: String,
    secimYok: String,
    secimiTemizle: String,
    ekleEtiketi: String,
    bosListeMesaji: String,
    secenekler: List<SoruSecenegi>,
    secilenAd: String,
    etkin: Boolean,
    onSec: (String) -> Unit,
    onEkle: () -> Unit,
) {
    var menuAcik by remember(baslik) { mutableStateOf(false) }
    val secilenSecenek = secenekler.firstOrNull {
        normalizeSoruSecenegiAdi(it.ad) == normalizeSoruSecenegiAdi(secilenAd)
    }
    val gorunenAd = secilenSecenek?.ad ?: secilenAd.takeIf { it.isNotBlank() } ?: secimYok

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(baslik, style = MaterialTheme.typography.labelLarge)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { menuAcik = true },
                    enabled = etkin,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 13.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            gorunenAd,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            "⌄",
                            modifier = Modifier.padding(start = 10.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                DropdownMenu(expanded = menuAcik, onDismissRequest = { menuAcik = false }) {
                    if (secilenAd.isNotBlank()) {
                        DropdownMenuItem(
                            text = { Text(secimiTemizle) },
                            onClick = { onSec(""); menuAcik = false },
                        )
                    }
                    if (secenekler.isEmpty() && secilenAd.isBlank()) {
                        DropdownMenuItem(
                            text = { Text(bosListeMesaji) },
                            enabled = false,
                            onClick = { menuAcik = false },
                        )
                    }
                    secenekler.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.ad) },
                            onClick = { onSec(option.ad); menuAcik = false },
                        )
                    }
                }
            }
            TextButton(onClick = onEkle, enabled = etkin) {
                Text(ekleEtiketi, maxLines = 1)
            }
        }
    }
}

@Composable
private fun ImageSection(
    title: String? = null,
    images: List<SoruGorselGirdisi>,
    imageStore: LocalSoruGorselDeposu,
    enabled: Boolean,
    onCamera: () -> Unit,
    onPicker: () -> Unit,
    onImagesChanged: (List<SoruGorselGirdisi>) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val removeImageDescription = stringResource(R.string.question_form_remove_image)
    val moveUpDescription = stringResource(R.string.question_form_image_order_up)
    val moveDownDescription = stringResource(R.string.question_form_image_order_down)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        title?.let { Text(it, style = MaterialTheme.typography.labelLarge) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(
                onClick = onCamera,
                enabled = enabled,
                modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
            ) {
                Text(
                    stringResource(R.string.question_form_take_photo),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OutlinedButton(
                onClick = onPicker,
                enabled = enabled,
                modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
            ) {
                Text(
                    stringResource(R.string.question_form_pick_photos),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (images.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Box(Modifier.width(42.dp).height(42.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "+",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            stringResource(R.string.question_form_no_photos),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            stringResource(R.string.question_form_photo_empty_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(images, key = { it.id }) { image ->
                    val file = imageStore.dosyaGoruntuIcin(image)
                    Card(
                        modifier = Modifier.width(220.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            SoruGorselResmi(
                                file = file,
                                contentDescription = stringResource(R.string.question_image_description),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(148.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                maxDecodeEdge = 480,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(
                                    enabled = enabled && image.sira > 0,
                                    onClick = {
                                        val index = images.indexOfFirst { it.id == image.id }
                                        if (index > 0) onImagesChanged(images.swapped(index, index - 1))
                                    },
                                    modifier = Modifier.semantics { contentDescription = moveUpDescription },
                                ) {
                                    Text("↑", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                                }
                                IconButton(
                                    enabled = enabled && image.sira < images.lastIndex,
                                    onClick = {
                                        val index = images.indexOfFirst { it.id == image.id }
                                        if (index in 0 until images.lastIndex) onImagesChanged(images.swapped(index, index + 1))
                                    },
                                    modifier = Modifier.semantics { contentDescription = moveDownDescription },
                                ) {
                                    Text("↓", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                                }
                                IconButton(
                                    enabled = enabled,
                                    onClick = {
                                        if (image.gecici) scope.launch { imageStore.geciciDosyayiSil(image.dosyaAdi) }
                                        onImagesChanged(
                                            images.filterNot { it.id == image.id }
                                                .mapIndexed { order, item -> item.copy(sira = order) },
                                        )
                                    },
                                    modifier = Modifier.semantics { contentDescription = removeImageDescription },
                                ) {
                                    Text("×", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleLarge)
                                }
                            }
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

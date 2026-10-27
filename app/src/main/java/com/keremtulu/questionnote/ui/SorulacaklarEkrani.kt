package com.keremtulu.questionnote.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.KameraCekimi
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import com.keremtulu.questionnote.domain.model.SoruIslemSonucu
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository
import com.keremtulu.questionnote.ui.bilesenler.GorselKirpmaAlani
import com.keremtulu.questionnote.ui.bilesenler.GorselKirpmaDialog
import com.keremtulu.questionnote.ui.bilesenler.GorselSecimBolumu
import com.keremtulu.questionnote.ui.bilesenler.SoruGorselResmi
import com.keremtulu.questionnote.ui.bilesenler.premium.PremiumBosDurum
import com.keremtulu.questionnote.ui.premium.PremiumButton
import com.keremtulu.questionnote.ui.premium.PremiumCard
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumSembolAilesi
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private data class DersSorulacakGrubu(
    val ders: Ders,
    val sorular: List<Soru>,
)

private sealed interface SorulacaklarDurumu {
    data object Yukleniyor : SorulacaklarDurumu
    data class Hazir(val gruplar: List<DersSorulacakGrubu>) : SorulacaklarDurumu
    data object Hata : SorulacaklarDurumu
}

@Composable
fun SorulacaklarEkrani(
    soruRepository: SoruRepository,
    dersRepository: DersRepository,
    gorselDeposu: LocalSoruGorselDeposu,
    onGeri: () -> Unit,
    onSoruDetayAc: (Ders, String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val koyuTema = isSystemInDarkTheme()
    val snackbarHostState = remember { SnackbarHostState() }
    var yenileme by remember { mutableIntStateOf(0) }
    var seciliDersId by remember { mutableStateOf<String?>(null) }
    var tamEkranHedef by remember { mutableStateOf<Pair<Ders, Soru>?>(null) }
    var cozdurdumHedef by remember { mutableStateOf<Soru?>(null) }

    val gruplarFlow = remember(soruRepository, dersRepository, yenileme) {
        combine(
            soruRepository.ogretmeneSorulacaklariIzle(),
            dersRepository.aktifDersleriIzle(),
        ) { sorular, dersler ->
            val dersMap = dersler.associateBy { it.id }
            sorular.groupBy { it.dersId }
                .mapNotNull { (dersId, liste) ->
                    dersMap[dersId]?.let { ders -> DersSorulacakGrubu(ders, liste) }
                }
                .sortedWith(compareBy<DersSorulacakGrubu> { it.ders.sira }.thenBy { it.ders.ad })
        }
    }
    val durum by produceState<SorulacaklarDurumu>(SorulacaklarDurumu.Yukleniyor, gruplarFlow) {
        gruplarFlow.catch { value = SorulacaklarDurumu.Hata }
            .collect { value = SorulacaklarDurumu.Hazir(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { insets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .consumeWindowInsets(insets)
                .testTag("sorulacaklar_liste"),
            contentPadding = PaddingValues(start = 18.dp, top = 20.dp, end = 18.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                PremiumTextButton(
                    onClick = onGeri,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("←", style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.common_back))
                    }
                }
            }
            item {
                com.keremtulu.questionnote.ui.bilesenler.DuzBaslik(
                    baslik = stringResource(R.string.ask_teacher_screen_title),
                    alt = stringResource(R.string.ask_teacher_screen_subtitle),
                )
            }

            when (val current = durum) {
                SorulacaklarDurumu.Yukleniyor -> item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                SorulacaklarDurumu.Hata -> item {
                    PremiumBosDurum(
                        modifier = Modifier.fillMaxWidth(),
                        baslik = stringResource(R.string.today_load_error),
                        aciklama = "",
                        butonMetni = stringResource(R.string.topic_retry),
                        onButonTikla = { yenileme++ },
                    )
                }
                is SorulacaklarDurumu.Hazir -> {
                    if (current.gruplar.isEmpty()) {
                        item {
                            PremiumBosDurum(
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                baslik = stringResource(R.string.ask_teacher_empty_title),
                                aciklama = stringResource(R.string.ask_teacher_empty_description),
                            )
                        }
                    } else {
                        val gecerliSeciliDers = seciliDersId?.takeIf { id ->
                            current.gruplar.any { it.ders.id == id }
                        }
                        val toplamAdet = current.gruplar.sumOf { it.sorular.size }
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                FilterChip(
                                    selected = gecerliSeciliDers == null,
                                    onClick = { seciliDersId = null },
                                    label = {
                                        Text(stringResource(R.string.ask_teacher_all_lessons, toplamAdet))
                                    },
                                )
                                current.gruplar.forEach { grup ->
                                    FilterChip(
                                        selected = gecerliSeciliDers == grup.ders.id,
                                        onClick = {
                                            seciliDersId = if (gecerliSeciliDers == grup.ders.id) null else grup.ders.id
                                        },
                                        label = {
                                            Text(
                                                stringResource(
                                                    R.string.ask_teacher_lesson_chip,
                                                    grup.ders.ad,
                                                    grup.sorular.size,
                                                ),
                                            )
                                        },
                                    )
                                }
                            }
                        }

                        val gorunenGruplar = if (gecerliSeciliDers == null) {
                            current.gruplar
                        } else {
                            current.gruplar.filter { it.ders.id == gecerliSeciliDers }
                        }

                        gorunenGruplar.forEach { grup ->
                            item(key = "ders_baslik_${grup.ders.id}") {
                                val stil = dersKartStiliGetir(grup.ders.ad)
                                val vurgu = stil.vurgu(koyuTema)
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    color = vurgu.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, vurgu.copy(alpha = 0.28f)),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(vurgu.copy(alpha = 0.20f)),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    text = stringResource(stil.isaret),
                                                    color = vurgu,
                                                    fontFamily = PremiumSembolAilesi,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                            }
                                            Text(
                                                text = grup.ders.ad,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surface,
                                        ) {
                                            Text(
                                                text = "${grup.sorular.size}",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = vurgu,
                                            )
                                        }
                                    }
                                }
                            }

                            items(grup.sorular, key = { it.id }) { soru ->
                                SorulacakSoruKarti(
                                    ders = grup.ders,
                                    soru = soru,
                                    gorselDeposu = gorselDeposu,
                                    onTamEkran = { tamEkranHedef = grup.ders to soru },
                                    onCozdurdum = { cozdurdumHedef = soru },
                                    onDetay = { onSoruDetayAc(grup.ders, soru.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    tamEkranHedef?.let { (ders, soru) ->
        SorulacakTamEkranSunumDialog(
            dersAdi = ders.ad,
            soru = soru,
            gorselDeposu = gorselDeposu,
            onDismiss = { tamEkranHedef = null },
            onCozdurdum = {
                tamEkranHedef = null
                cozdurdumHedef = soru
            },
        )
    }

    cozdurdumHedef?.let { soru ->
        CozdurdumDialog(
            soru = soru,
            soruRepository = soruRepository,
            gorselDeposu = gorselDeposu,
            onDismiss = { cozdurdumHedef = null },
            onBasarili = {
                cozdurdumHedef = null
                scope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.ask_teacher_resolved_snackbar))
                }
            },
            onHata = { mesaj ->
                scope.launch { snackbarHostState.showSnackbar(mesaj) }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SorulacakSoruKarti(
    ders: Ders,
    soru: Soru,
    gorselDeposu: LocalSoruGorselDeposu,
    onTamEkran: () -> Unit,
    onCozdurdum: () -> Unit,
    onDetay: () -> Unit,
) {
    val soruGorselleri = remember(soru.gorseller) {
        soru.gorseller.filter { it.tur == GorselTuru.SORU }.sortedBy { it.sira }
    }
    val ilkDosya = soruGorselleri.firstOrNull()?.let { gorselDeposu.dosyaGoruntuIcin(it.dosyaAdi) }

    PremiumCard(
        onClick = onDetay,
        modifier = Modifier.fillMaxWidth().testTag("sorulacak_kart_${soru.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            ) {
                SoruGorselResmi(
                    file = ilkDosya,
                    contentDescription = stringResource(R.string.question_image_description),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    maxDecodeEdge = 900,
                    cerceve = false,
                    onClick = onTamEkran,
                )
            }
            val baslikMetni = soru.kaynak?.takeIf { it.isNotBlank() }
                ?: soru.soruNotu?.takeIf { it.isNotBlank() }
                ?: ders.ad
            Text(
                text = baslikMetni,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            soru.kaynakKonumu?.takeIf { it.isNotBlank() }?.let { konum ->
                Text(
                    text = konum,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = if (androidx.compose.ui.platform.LocalDensity.current.fontScale >= 1.5f) 1 else 2,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PremiumOutlinedButton(
                    onClick = onTamEkran,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(stringResource(R.string.ask_teacher_open_fullscreen))
                }
                PremiumButton(
                    onClick = onCozdurdum,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("sorulacak_cozdurdum_${soru.id}"),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        stringResource(R.string.ask_teacher_solved_button),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SorulacakTamEkranSunumDialog(
    dersAdi: String,
    soru: Soru,
    gorselDeposu: LocalSoruGorselDeposu,
    onDismiss: () -> Unit,
    onCozdurdum: () -> Unit,
) {
    val gorseller = remember(soru.gorseller) {
        soru.gorseller.filter { it.tur == GorselTuru.SORU }.sortedBy { it.sira }
    }
    var sayfa by remember(soru.id) { mutableIntStateOf(0) }
    val aktifGorsel = gorseller.getOrNull(sayfa)
    val dosya = aktifGorsel?.let { gorselDeposu.dosyaGoruntuIcin(it.dosyaAdi) }
    var scale by remember(aktifGorsel?.id) { mutableFloatStateOf(1f) }
    var offsetX by remember(aktifGorsel?.id) { mutableFloatStateOf(0f) }
    var offsetY by remember(aktifGorsel?.id) { mutableFloatStateOf(0f) }
    val transformState = rememberTransformableState { zoom, pan, _ ->
        scale = (scale * zoom).coerceIn(1f, 5f)
        if (scale <= 1.02f) {
            offsetX = 0f
            offsetY = 0f
        } else {
            offsetX += pan.x
            offsetY += pan.y
        }
    }

    BackHandler(onBack = onDismiss)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0E141B))
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dersAdi,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (gorseller.size > 1) {
                        Text(
                            text = stringResource(R.string.ask_teacher_page_indicator, sayfa + 1, gorseller.size),
                            color = Color.White.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.teacher_zoom_hint),
                            color = Color.White.copy(alpha = 0.68f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (scale > 1.05f) {
                        PremiumTextButton(
                            onClick = {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            },
                        ) {
                            Text(
                                text = stringResource(R.string.teacher_zoom_reset),
                                color = PremiumMarkaRenkler.BakirIsik,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    PremiumTextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.common_close), color = Color.White)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clipToBounds()
                    .pointerInput(aktifGorsel?.id) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (scale > 1.05f) {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                } else {
                                    scale = 2.4f
                                }
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                SoruGorselResmi(
                    file = dosya,
                    contentDescription = stringResource(R.string.question_image_description),
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                        }
                        .transformable(transformState),
                    contentScale = ContentScale.Fit,
                    maxDecodeEdge = 2_400,
                    cerceve = false,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (gorseller.size > 1) {
                    val oncekiAktif = sayfa > 0
                    val sonrakiAktif = sayfa < gorseller.lastIndex
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        PremiumTextButton(
                            onClick = { if (oncekiAktif) sayfa-- },
                            enabled = oncekiAktif,
                        ) {
                            Text(
                                text = stringResource(R.string.ask_teacher_prev_page),
                                color = if (oncekiAktif) Color.White else Color.White.copy(alpha = 0.36f),
                            )
                        }
                        PremiumTextButton(
                            onClick = { if (sonrakiAktif) sayfa++ },
                            enabled = sonrakiAktif,
                        ) {
                            Text(
                                text = stringResource(R.string.ask_teacher_next_page),
                                color = if (sonrakiAktif) Color.White else Color.White.copy(alpha = 0.36f),
                            )
                        }
                    }
                }
                PremiumButton(
                    onClick = onCozdurdum,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        stringResource(R.string.ask_teacher_solved_button),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
internal fun CozdurdumDialog(
    soru: Soru,
    soruRepository: SoruRepository,
    gorselDeposu: LocalSoruGorselDeposu,
    onDismiss: () -> Unit,
    onBasarili: () -> Unit,
    onHata: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cozumGorselleri by remember(soru.id) { mutableStateOf<List<SoruGorselGirdisi>>(emptyList()) }
    var cozumNotu by remember(soru.id) { mutableStateOf("") }
    var kaydediliyor by remember(soru.id) { mutableStateOf(false) }
    var kameraCekimi by remember(soru.id) { mutableStateOf<KameraCekimi?>(null) }
    var kirpmaHedefi by remember(soru.id) { mutableStateOf<SoruGorselGirdisi?>(null) }
    var kirpmaIsleniyor by remember(soru.id) { mutableStateOf(false) }

    fun gecicileriTemizle() {
        val kopya = cozumGorselleri
        cozumGorselleri = emptyList()
        scope.launch {
            kopya.filter { it.gecici }.forEach { runCatching { gorselDeposu.geciciDosyayiSil(it.dosyaAdi) } }
        }
    }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { basarili ->
        val aktif = kameraCekimi
        kameraCekimi = null
        if (aktif == null) return@rememberLauncherForActivityResult
        if (!basarili) {
            gorselDeposu.kameraCekiminiSil(aktif.id)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            try {
                if (cozumGorselleri.size >= 20) {
                    gorselDeposu.kameraCekiminiSil(aktif.id)
                    onHata(context.getString(R.string.question_form_photo_limit))
                    return@launch
                }
                val girdi = gorselDeposu.kameradanHazirla(aktif.id, GorselTuru.COZUM, cozumGorselleri.size)
                cozumGorselleri = cozumGorselleri + girdi
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Throwable) {
                onHata(context.getString(R.string.question_form_photo_error))
            }
        }
    }

    val galeriSecici = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val guncel = cozumGorselleri.toMutableList()
                for (uri in uris) {
                    if (guncel.size >= 20) break
                    guncel += gorselDeposu.secicidenHazirla(uri, GorselTuru.COZUM, guncel.size)
                }
                cozumGorselleri = guncel
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Throwable) {
                onHata(context.getString(R.string.question_form_photo_error))
            }
        }
    }

    /**
     * No runtime permission gate: the manifest declares no CAMERA permission and the capture intent
     * writes through our FileProvider, so the old check could only fail and left the button dead.
     */
    fun kamerayiAc() {
        try {
            val cekim = gorselDeposu.kameraCekimiOlustur()
            kameraCekimi = cekim
            takePicture.launch(cekim.uri)
        } catch (_: Throwable) {
            onHata(context.getString(R.string.question_form_photo_error))
        }
    }

    fun tekraraAl(gorsellerle: Boolean) {
        if (kaydediliyor) return
        kaydediliyor = true
        scope.launch {
            try {
                val gonderilecek = if (gorsellerle) cozumGorselleri else emptyList()
                val sonuc = soruRepository.ogretmeneCozduruldu(
                    soruId = soru.id,
                    yeniCozumGorselleri = gonderilecek,
                    cozumNotuEki = cozumNotu,
                )
                if (sonuc == SoruIslemSonucu.Basarili) {
                    if (!gorsellerle && cozumGorselleri.isNotEmpty()) gecicileriTemizle()
                    onBasarili()
                } else {
                    onHata(context.getString(R.string.question_error_generic))
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Throwable) {
                onHata(context.getString(R.string.question_error_generic))
            } finally {
                kaydediliyor = false
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (!kaydediliyor && !kirpmaIsleniyor) {
                gecicileriTemizle()
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        text = stringResource(R.string.ask_teacher_dialog_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                item {
                    Text(
                        text = stringResource(R.string.ask_teacher_dialog_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item {
                    GorselSecimBolumu(
                        title = stringResource(R.string.question_form_solution_images),
                        images = cozumGorselleri,
                        imageStore = gorselDeposu,
                        enabled = !kaydediliyor && !kirpmaIsleniyor,
                        onCamera = ::kamerayiAc,
                        onPicker = {
                            galeriSecici.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onCrop = { kirpmaHedefi = it },
                        onImagesChanged = { cozumGorselleri = it },
                    )
                }
                item {
                    OutlinedTextField(
                        value = cozumNotu,
                        onValueChange = { cozumNotu = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !kaydediliyor,
                        minLines = 2,
                        label = { Text(stringResource(R.string.ask_teacher_solution_note_label)) },
                        shape = RoundedCornerShape(14.dp),
                    )
                }
                if (cozumGorselleri.isNotEmpty()) {
                    item {
                        PremiumButton(
                            onClick = { tekraraAl(gorsellerle = true) },
                            enabled = !kaydediliyor,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("cozdurdum_fotografli_onay"),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text(stringResource(R.string.ask_teacher_save_with_photo), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item {
                    PremiumOutlinedButton(
                        onClick = { tekraraAl(gorsellerle = false) },
                        enabled = !kaydediliyor,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("cozdurdum_fotografsiz_onay"),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.ask_teacher_save_without_photo), fontWeight = FontWeight.SemiBold)
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        PremiumTextButton(
                            enabled = !kaydediliyor,
                            onClick = {
                                gecicileriTemizle()
                                onDismiss()
                            },
                        ) {
                            Text(stringResource(R.string.question_form_cancel))
                        }
                    }
                }
            }
        }
    }

    kirpmaHedefi?.let { target ->
        val file = gorselDeposu.dosyaGoruntuIcin(target)
        if (file != null) {
            GorselKirpmaDialog(
                file = file,
                isSaving = kirpmaIsleniyor,
                onDismiss = { if (!kirpmaIsleniyor) kirpmaHedefi = null },
                onApply = { area: GorselKirpmaAlani ->
                    if (kirpmaIsleniyor) return@GorselKirpmaDialog
                    kirpmaIsleniyor = true
                    scope.launch {
                        var output: SoruGorselGirdisi? = null
                        var replaced = false
                        try {
                            val cropped = gorselDeposu.kirp(target, area.sol, area.ust, area.sag, area.alt)
                            output = cropped
                            val idx = cozumGorselleri.indexOfFirst { it.id == target.id }
                            if (idx >= 0) {
                                cozumGorselleri = cozumGorselleri.toMutableList().apply {
                                    this[idx] = cropped.copy(sira = idx)
                                }
                                replaced = true
                            }
                            kirpmaHedefi = null
                            if (replaced && target.gecici && cropped.id != target.id) {
                                runCatching { gorselDeposu.geciciDosyayiSil(target.dosyaAdi) }
                            }
                        } catch (cancel: CancellationException) {
                            throw cancel
                        } catch (_: Throwable) {
                            onHata(context.getString(R.string.question_form_crop_error))
                        } finally {
                            if (!replaced) {
                                output?.takeIf { it.gecici && it.id != target.id }?.let {
                                    runCatching { gorselDeposu.geciciDosyayiSil(it.dosyaAdi) }
                                }
                            }
                            kirpmaIsleniyor = false
                        }
                    }
                },
            )
        }
    }
}

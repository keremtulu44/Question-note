package com.keremtulu.questionnote.ui.ogretmen

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.KameraCekimi
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.prefs.OgretmenDeposu
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.Konu
import com.keremtulu.questionnote.domain.model.OgretmenGrubu
import com.keremtulu.questionnote.domain.model.OgretmenSorusu
import com.keremtulu.questionnote.domain.model.OgretmenZorluk
import com.keremtulu.questionnote.domain.model.aramaAnahtari
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import com.keremtulu.questionnote.ui.bilesenler.GorselKirpmaAlani
import com.keremtulu.questionnote.ui.bilesenler.GorselKirpmaDialog
import com.keremtulu.questionnote.ui.bilesenler.GorselSecimBolumu
import com.keremtulu.questionnote.ui.bilesenler.premium.CamKarti
import com.keremtulu.questionnote.ui.bilesenler.premium.CamYukseklik
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.premium.PremiumButton
import com.keremtulu.questionnote.ui.premium.PremiumGradyanButon
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val MAKS_GORSEL = 20

/** Sticky save bar; a form that loses its button on scroll is the fastest way to lose trust. */
@Composable
private fun KaydetCubugu(
    kaydediliyor: Boolean,
    kirpmaIsleniyor: Boolean,
    yeniSoru: Boolean,
    onKaydet: () -> Unit,
    onKaydetVeYeni: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PremiumGradyanButon(
                onClick = onKaydet,
                enabled = !kaydediliyor && !kirpmaIsleniyor,
                modifier = Modifier.fillMaxWidth().testTag("ogretmen_soru_kaydet"),
            ) {
                Text(stringResource(R.string.teacher_question_save), fontWeight = FontWeight.Bold)
            }
            if (yeniSoru) {
                PremiumButton(
                    onClick = onKaydetVeYeni,
                    enabled = !kaydediliyor && !kirpmaIsleniyor,
                    modifier = Modifier.fillMaxWidth().testTag("ogretmen_soru_kaydet_yeni"),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(stringResource(R.string.teacher_form_save_and_new))
                }
            }
        }
    }
}

/**
 * Question form.
 *
 * Two deliberate changes against the previous version:
 * 1. The camera opens without a runtime permission check — `ACTION_IMAGE_CAPTURE` writes through
 *    our own FileProvider, and the manifest does not declare CAMERA, so the old check could only
 *    ever fail and left the button dead.
 * 2. Lesson/topic/source/asked-by/due-day are real fields that write into Room v5; the lesson and
 *    topic ids come from the shared student catalog.
 */
@Composable
internal fun OgretmenSoruFormuEkrani(
    grup: OgretmenGrubu,
    mevcutSoru: OgretmenSorusu?,
    ogretmenDeposu: OgretmenDeposu,
    gorselDeposu: LocalSoruGorselDeposu,
    dersler: List<Ders>,
    konular: List<Konu>,
    onGeri: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val klavyeAcik = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    var formHatasi by remember { mutableStateOf<Int?>(null) }
    var kaydediliyor by remember { mutableStateOf(false) }
    var bilgi by remember { mutableStateOf<String?>(null) }

    var baslik by remember(mevcutSoru?.id) { mutableStateOf(mevcutSoru?.baslik.orEmpty()) }
    var cevap by remember(mevcutSoru?.id) { mutableStateOf(mevcutSoru?.cevap.orEmpty()) }
    var zorluk by remember(mevcutSoru?.id) { mutableStateOf(mevcutSoru?.zorluk) }
    var dersId by remember(mevcutSoru?.id) { mutableStateOf(mevcutSoru?.dersId) }
    var konuId by remember(mevcutSoru?.id) { mutableStateOf(mevcutSoru?.konuId) }
    var kaynak by remember(mevcutSoru?.id) { mutableStateOf(mevcutSoru?.kaynak.orEmpty()) }
    var kimSordu by remember(mevcutSoru?.id) { mutableStateOf(mevcutSoru?.kimSordu.orEmpty()) }
    var planGun by remember(mevcutSoru?.id) { mutableStateOf(mevcutSoru?.planGun) }
    var gorseller by remember(mevcutSoru?.id) {
        mutableStateOf(mevcutSoru?.soruGorselleri().orEmpty().map { it.toGirdi() })
    }
    var cozumGorselleri by remember(mevcutSoru?.id) {
        mutableStateOf(mevcutSoru?.cozumGorselleri().orEmpty().map { it.toGirdi() })
    }
    var kameraCekimi by remember { mutableStateOf<KameraCekimi?>(null) }
    var kirpmaHedefi by remember { mutableStateOf<SoruGorselGirdisi?>(null) }
    var kirpmaCozum by remember { mutableStateOf(false) }
    var kirpmaIsleniyor by remember { mutableStateOf(false) }
    var dersSecimiAcik by remember { mutableStateOf(false) }
    var konuSecimiAcik by remember { mutableStateOf(false) }
    // Duplicate photo guard (same sha256 in another question) — confirmed once per form session.
    var cakisanSoru by remember(mevcutSoru?.id) { mutableStateOf<OgretmenSorusu?>(null) }
    var cakismaOnaylandi by remember(mevcutSoru?.id) { mutableStateOf(false) }
    // Which button opened the duplicate dialog: "kaydet" and "kaydet ve yeni" must both survive.
    var cakismaSonrasiYeniyeDevam by remember(mevcutSoru?.id) { mutableStateOf(false) }
    val hizliIsimler = remember(ogretmenDeposu) {
        ogretmenDeposu.anlikVeri().kimSorduIsimleri().take(6)
    }

    // Lessons that are archived in the catalog are still offered when the question already uses one.
    val dersSecenekleri = remember(dersler) {
        dersler.filter { !it.arsivli || it.id == mevcutSoru?.dersId }
            .sortedBy { it.sira }
            .map { it.id to it.ad }
    }
    val konuSecenekleri = remember(konular, dersId) {
        konular.filter { it.dersId == dersId && (!it.arsivli || it.id == mevcutSoru?.konuId) }
            .sortedBy { it.sira }
            .map { it.id to it.ad }
    }

    fun iptalEtVeDon() {
        val geciciler = (gorseller + cozumGorselleri).filter { it.gecici }
        scope.launch { geciciler.forEach { runCatching { gorselDeposu.geciciDosyayiSil(it.dosyaAdi) } } }
        onGeri()
    }

    fun kaydet(yeniyeDevam: Boolean) {
        if (kaydediliyor || kirpmaIsleniyor) return
        if (gorseller.isEmpty()) {
            formHatasi = R.string.question_error_photo_required
            return
        }
        if (!cakismaOnaylandi) {
            // Same file already attached to another question: almost always the same question
            // photographed twice. Ask once, then let the teacher decide.
            val cakisan = ogretmenDeposu.anlikVeri().ayniGorselliSoru(
                sha256ler = (gorseller + cozumGorselleri).mapTo(mutableSetOf()) { it.sha256 },
                haricSoruId = mevcutSoru?.id,
            )
            if (cakisan != null) {
                cakismaSonrasiYeniyeDevam = yeniyeDevam
                cakisanSoru = cakisan
                return
            }
        }
        kaydediliyor = true
        formHatasi = null
        scope.launch {
            try {
                val sonuc = ogretmenDeposu.soruKaydet(
                    id = mevcutSoru?.id,
                    grupId = grup.id,
                    baslik = baslik,
                    zorluk = zorluk,
                    cevap = cevap,
                    gorseller = gorseller,
                    dersId = dersId,
                    konuId = konuId,
                    kaynak = kaynak,
                    kimSordu = kimSordu,
                    planGun = planGun,
                    cozumGorselleri = cozumGorselleri,
                )
                if (sonuc == null) {
                    formHatasi = R.string.question_error_generic
                } else if (yeniyeDevam) {
                    baslik = ""
                    cevap = ""
                    zorluk = null
                    kaynak = ""
                    kimSordu = ""
                    planGun = null
                    gorseller = emptyList()
                    cozumGorselleri = emptyList()
                    bilgi = context.getString(R.string.teacher_form_saved_new)
                    focusManager.clearFocus()
                } else {
                    onGeri()
                }
            } finally {
                kaydediliyor = false
            }
        }
    }

    BackHandler(enabled = !kaydediliyor && !kirpmaIsleniyor) { iptalEtVeDon() }

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
                if (gorseller.size >= MAKS_GORSEL) {
                    gorselDeposu.kameraCekiminiSil(aktif.id)
                    bilgi = context.getString(R.string.question_form_photo_limit)
                    return@launch
                }
                val girdi = gorselDeposu.kameradanHazirla(aktif.id, GorselTuru.SORU, gorseller.size)
                gorseller = gorseller + girdi
                formHatasi = null
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Throwable) {
                bilgi = context.getString(R.string.question_form_photo_error)
            }
        }
    }

    val cozumKamera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { basarili ->
        val aktif = kameraCekimi
        kameraCekimi = null
        if (aktif == null) return@rememberLauncherForActivityResult
        if (!basarili) {
            gorselDeposu.kameraCekiminiSil(aktif.id)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            try {
                val girdi = gorselDeposu.kameradanHazirla(aktif.id, GorselTuru.COZUM, cozumGorselleri.size)
                cozumGorselleri = cozumGorselleri + girdi
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Throwable) {
                bilgi = context.getString(R.string.question_form_photo_error)
            }
        }
    }

    val galeriSecici = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val liste = gorseller.toMutableList()
                for (uri in uris) {
                    if (liste.size >= MAKS_GORSEL) break
                    liste += gorselDeposu.secicidenHazirla(uri, GorselTuru.SORU, liste.size)
                }
                gorseller = liste
                if (gorseller.isNotEmpty()) formHatasi = null
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Throwable) {
                bilgi = context.getString(R.string.question_form_photo_error)
            }
        }
    }

    val cozumGaleri = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val liste = cozumGorselleri.toMutableList()
                for (uri in uris) {
                    if (liste.size >= 10) break
                    liste += gorselDeposu.secicidenHazirla(uri, GorselTuru.COZUM, liste.size)
                }
                cozumGorselleri = liste
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Throwable) {
                bilgi = context.getString(R.string.question_form_photo_error)
            }
        }
    }

    /**
     * No permission gate: the manifest intentionally has no CAMERA entry, and the intent writes to a
     * FileProvider URI, so requesting a permission here could only ever block the button.
     */
    fun kamerayiAc(hedefCozum: Boolean) {
        try {
            val cekim = gorselDeposu.kameraCekimiOlustur()
            kameraCekimi = cekim
            if (hedefCozum) cozumKamera.launch(cekim.uri) else takePicture.launch(cekim.uri)
        } catch (_: Throwable) {
            bilgi = context.getString(R.string.question_form_photo_error)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            KaydetCubugu(
                kaydediliyor = kaydediliyor,
                kirpmaIsleniyor = kirpmaIsleniyor,
                yeniSoru = mevcutSoru == null,
                onKaydet = { kaydet(false) },
                onKaydetVeYeni = { kaydet(true) },
            )
        },
    ) { insetler ->
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(insetler)
            .consumeWindowInsets(insetler),
        contentPadding = PaddingValues(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PremiumTextButton(onClick = ::iptalEtVeDon, enabled = !kaydediliyor) {
                    Text("← " + stringResource(R.string.question_form_cancel))
                }
                if (klavyeAcik) {
                    PremiumTextButton(onClick = { focusManager.clearFocus() }) {
                        Text(stringResource(R.string.common_hide_keyboard), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        item {
            GradyanKart(modifier = Modifier.fillMaxWidth(), borderAnimation = false) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = grup.ad,
                        color = PremiumMarkaRenkler.BakirIsik,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(
                            if (mevcutSoru == null) R.string.teacher_form_title_new
                            else R.string.teacher_form_title_edit,
                        ),
                        color = PremiumMarkaRenkler.Kagit,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.teacher_question_form_hint),
                        color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.84f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        item {
            CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GorselSecimBolumu(
                        title = stringResource(R.string.question_form_question_images),
                        images = gorseller,
                        imageStore = gorselDeposu,
                        enabled = !kaydediliyor && !kirpmaIsleniyor,
                        onCamera = { kamerayiAc(false) },
                        onPicker = {
                            galeriSecici.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        onCrop = {
                            kirpmaCozum = false
                            kirpmaHedefi = it
                        },
                        onImagesChanged = {
                            gorseller = it
                            if (it.isNotEmpty()) formHatasi = null
                        },
                        // Editing an existing question must not delete permanent files on remove;
                        // the store collects unreferenced files after a successful save.
                        silinirkenGeciciTemizle = mevcutSoru == null,
                    )
                }
            }
        }
        item {
            CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BolumBasligi(stringResource(R.string.teacher_form_solution_section))
                    Text(
                        text = stringResource(R.string.teacher_solution_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GorselSecimBolumu(
                        images = cozumGorselleri,
                        imageStore = gorselDeposu,
                        enabled = !kaydediliyor && !kirpmaIsleniyor,
                        onCamera = { kamerayiAc(true) },
                        onPicker = {
                            cozumGaleri.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        onCrop = {
                            kirpmaCozum = true
                            kirpmaHedefi = it
                        },
                        onImagesChanged = { cozumGorselleri = it },
                        silinirkenGeciciTemizle = mevcutSoru == null,
                    )
                }
            }
        }
        item {
            CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BolumBasligi(stringResource(R.string.teacher_form_labels_section))
                    Text(
                        text = stringResource(R.string.teacher_form_labels_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    SecimSatiriButonu(
                        etiket = stringResource(R.string.teacher_form_lesson_label),
                        deger = dersId?.let { id -> dersSecenekleri.firstOrNull { it.first == id }?.second },
                        bosDeger = stringResource(R.string.teacher_form_lesson_none),
                        modifier = Modifier.testTag("ogretmen_form_ders"),
                        onClick = { dersSecimiAcik = true },
                    )
                    SecimSatiriButonu(
                        etiket = stringResource(R.string.teacher_form_topic_label),
                        deger = konuId?.let { id -> konuSecenekleri.firstOrNull { it.first == id }?.second },
                        bosDeger = stringResource(R.string.teacher_form_topic_none),
                        modifier = Modifier.testTag("ogretmen_form_konu"),
                        onClick = { if (dersId != null) konuSecimiAcik = true },
                        enabled = dersId != null,
                    )
                    OutlinedTextField(
                        value = kaynak,
                        onValueChange = { kaynak = it },
                        modifier = Modifier.fillMaxWidth().testTag("ogretmen_form_kaynak"),
                        singleLine = true,
                        label = { Text(stringResource(R.string.teacher_form_source_label)) },
                        placeholder = { Text(stringResource(R.string.teacher_form_source_hint)) },
                        shape = RoundedCornerShape(14.dp),
                    )
                    OutlinedTextField(
                        value = kimSordu,
                        onValueChange = { kimSordu = it },
                        modifier = Modifier.fillMaxWidth().testTag("ogretmen_form_kim_sordu"),
                        singleLine = true,
                        label = { Text(stringResource(R.string.teacher_form_asked_by_label)) },
                        placeholder = { Text(stringResource(R.string.teacher_form_asked_by_hint)) },
                        shape = RoundedCornerShape(14.dp),
                    )
                    // Typing the same five names every week is the real cost here: one tap fills the
                    // field with a name that is already in the workspace.
                    if (hizliIsimler.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.teacher_quick_names),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(hizliIsimler, key = { it }) { ad ->
                                FiltreCipi(
                                    metin = ad,
                                    secili = aramaAnahtari(kimSordu) == aramaAnahtari(ad),
                                    onClick = { kimSordu = ad },
                                )
                            }
                        }
                    }
                    BolumBasligi(stringResource(R.string.teacher_form_due_label))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val bugun = bugunEpochGun()
                        val secenekler = listOf(
                            null to stringResource(R.string.teacher_form_due_none),
                            bugun to stringResource(R.string.teacher_form_due_today),
                            (bugun + 1) to stringResource(R.string.teacher_form_due_tomorrow),
                            (bugun + 3) to "+3",
                        )
                        secenekler.forEach { (gun, metin) ->
                            val secili = planGun == gun
                            PremiumOutlinedButton(
                                onClick = { planGun = gun },
                                modifier = Modifier.heightIn(min = 42.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (secili) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                                ),
                            ) {
                                Text(
                                    text = metin,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (secili) FontWeight.Bold else FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BolumBasligi(stringResource(R.string.teacher_question_difficulty_label))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OgretmenZorluk.entries.forEach { secenek ->
                            val secili = zorluk == secenek
                            val zRenk = zorlukRengi(secenek, false)
                            Surface(
                                onClick = { zorluk = if (secili) null else secenek },
                                modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = if (secili) zRenk.copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(
                                    width = if (secili) 1.5.dp else 1.dp,
                                    color = if (secili) zRenk
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                ),
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                    Text(
                                        text = stringResource(zorlukEtiketi(secenek)),
                                        color = if (secili) zRenk else MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (secili) FontWeight.Bold else FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = baslik,
                        onValueChange = { baslik = it },
                        modifier = Modifier.fillMaxWidth().testTag("ogretmen_soru_baslik_girdi"),
                        enabled = !kaydediliyor,
                        singleLine = true,
                        label = { Text(stringResource(R.string.teacher_question_title_label)) },
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    )
                    OutlinedTextField(
                        value = cevap,
                        onValueChange = { cevap = it },
                        modifier = Modifier.fillMaxWidth().testTag("ogretmen_soru_cevap_girdi"),
                        enabled = !kaydediliyor,
                        minLines = 2,
                        label = { Text(stringResource(R.string.teacher_question_answer_label)) },
                        shape = RoundedCornerShape(14.dp),
                    )
                    Text(
                        text = stringResource(R.string.teacher_answer_hidden_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            bilgi?.let { mesaj ->
                NotKutusu(metin = mesaj, vurgu = PremiumMarkaRenkler.Deniz)
            }
            formHatasi?.let { hataId ->
                Text(
                    text = stringResource(hataId),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }

    }

    if (dersSecimiAcik) {
        SecimDialogu(
            baslik = stringResource(R.string.teacher_form_lesson_label),
            secenekler = dersSecenekleri,
            seciliId = dersId,
            bosEtiket = stringResource(R.string.teacher_form_lesson_none),
            onSec = {
                dersId = it
                if (konuId != null && konular.none { konu -> konu.id == konuId && konu.dersId == it }) {
                    konuId = null
                }
                dersSecimiAcik = false
            },
            onDismiss = { dersSecimiAcik = false },
            testEtiketi = "ogretmen_ders_secim",
        )
    }

    if (konuSecimiAcik) {
        SecimDialogu(
            baslik = stringResource(R.string.teacher_form_topic_label),
            secenekler = konuSecenekleri,
            seciliId = konuId,
            bosEtiket = stringResource(R.string.teacher_form_topic_none),
            onSec = {
                konuId = it
                konuSecimiAcik = false
            },
            onDismiss = { konuSecimiAcik = false },
            testEtiketi = "ogretmen_konu_secim",
        )
    }

    cakisanSoru?.let { cakisan ->
        val grupAdi = ogretmenDeposu.anlikVeri().gruplar.firstOrNull { it.id == cakisan.grupId }?.ad
        OgretmenDiyalogu(onDismiss = { cakisanSoru = null }, testEtiketi = "ogretmen_ayni_gorsel_dialog") {
            Text(
                text = stringResource(R.string.teacher_duplicate_photo_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                stringResource(
                    R.string.teacher_duplicate_photo_message,
                    cakisan.baslik?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.teacher_untitled_question),
                    grupAdi ?: "",
                ),
            )
            DiyalogDugmeleri {
                TextButton(onClick = { cakisanSoru = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
                TextButton(onClick = {
                    cakisanSoru = null
                    cakismaOnaylandi = true
                    kaydet(yeniyeDevam = cakismaSonrasiYeniyeDevam)
                }) {
                    Text(stringResource(R.string.teacher_duplicate_photo_confirm))
                }
            }
        }
    }

    kirpmaHedefi?.let { hedef ->
        val dosya = gorselDeposu.dosyaGoruntuIcin(hedef)
        if (dosya != null) {
            GorselKirpmaDialog(
                file = dosya,
                isSaving = kirpmaIsleniyor,
                onDismiss = { if (!kirpmaIsleniyor) kirpmaHedefi = null },
                onApply = { alan: GorselKirpmaAlani ->
                    if (kirpmaIsleniyor) return@GorselKirpmaDialog
                    kirpmaIsleniyor = true
                    scope.launch {
                        var yeni: SoruGorselGirdisi? = null
                        try {
                            val kirpilmis = gorselDeposu.kirp(hedef, alan.sol, alan.ust, alan.sag, alan.alt)
                            yeni = kirpilmis
                            if (kirpmaCozum) {
                                val idx = cozumGorselleri.indexOfFirst { it.id == hedef.id }
                                if (idx >= 0) {
                                    cozumGorselleri = cozumGorselleri.toMutableList().apply {
                                        this[idx] = kirpilmis.copy(sira = idx)
                                    }
                                }
                            } else {
                                val idx = gorseller.indexOfFirst { it.id == hedef.id }
                                if (idx >= 0) {
                                    gorseller = gorseller.toMutableList().apply {
                                        this[idx] = kirpilmis.copy(sira = idx)
                                    }
                                }
                            }
                            kirpmaHedefi = null
                            if (hedef.gecici && kirpilmis.id != hedef.id) {
                                runCatching { gorselDeposu.geciciDosyayiSil(hedef.dosyaAdi) }
                            }
                        } catch (cancel: CancellationException) {
                            throw cancel
                        } catch (_: Throwable) {
                            bilgi = context.getString(R.string.question_form_crop_error)
                        } finally {
                            kirpmaIsleniyor = false
                            val kalan = yeni
                            if (kalan != null && kalan.gecici && kalan.id != hedef.id &&
                                gorseller.none { it.id == kalan.id } &&
                                cozumGorselleri.none { it.id == kalan.id }
                            ) {
                                runCatching { gorselDeposu.geciciDosyayiSil(kalan.dosyaAdi) }
                            }
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun SecimSatiriButonu(
    etiket: String,
    deger: String?,
    bosDeger: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    PremiumOutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(
            text = etiket + ": " + (deger ?: bosDeger),
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

package com.keremtulu.questionnote.ui.ogretmen

import android.Manifest
import android.content.pm.PackageManager
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.keremtulu.questionnote.BuildConfig
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.export.SonYedekBilgisi
import com.keremtulu.questionnote.data.images.KameraCekimi
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.prefs.OgretmenDeposu
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.OgretmenGrubu
import com.keremtulu.questionnote.domain.model.OgretmenSorusu
import com.keremtulu.questionnote.domain.model.OgretmenVerisi
import com.keremtulu.questionnote.domain.model.OgretmenZorluk
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import com.keremtulu.questionnote.ui.LocalUygulamaSesleri
import com.keremtulu.questionnote.ui.bilesenler.GorselKirpmaAlani
import com.keremtulu.questionnote.ui.bilesenler.GorselKirpmaDialog
import com.keremtulu.questionnote.ui.bilesenler.GorselSecimBolumu
import com.keremtulu.questionnote.ui.bilesenler.SoruGorselResmi
import com.keremtulu.questionnote.ui.bilesenler.premium.CamKarti
import com.keremtulu.questionnote.ui.bilesenler.premium.CamYukseklik
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.bilesenler.premium.PremiumBosDurum
import com.keremtulu.questionnote.ui.premium.EtkilesimTuru
import com.keremtulu.questionnote.ui.premium.PremiumButton
import com.keremtulu.questionnote.ui.premium.PremiumCard
import com.keremtulu.questionnote.ui.premium.PremiumExtendedFloatingActionButton
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumSwitch
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private sealed interface OgretmenEkranDurumu {
    data object Gruplar : OgretmenEkranDurumu
    data object Ayarlar : OgretmenEkranDurumu
    data class GrupDetay(val grupId: String) : OgretmenEkranDurumu
    data class SoruFormu(val grupId: String, val soruId: String?) : OgretmenEkranDurumu
}

@Composable
fun OgretmenAnaEkrani(
    ogretmenDeposu: OgretmenDeposu,
    gorselDeposu: LocalSoruGorselDeposu,
    sonYedek: SonYedekBilgisi?,
    yedeklemeIslemde: Boolean,
    onOgrenciModunaGec: () -> Unit,
    onYedekOlustur: () -> Unit,
    onYedekGeriYukle: () -> Unit,
) {
    val veri by ogretmenDeposu.veriIzle.collectAsState()
    var altEkran by remember { mutableStateOf<OgretmenEkranDurumu>(OgretmenEkranDurumu.Gruplar) }
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(enabled = altEkran != OgretmenEkranDurumu.Gruplar) {
        altEkran = when (val mevcut = altEkran) {
            OgretmenEkranDurumu.Gruplar -> OgretmenEkranDurumu.Gruplar
            OgretmenEkranDurumu.Ayarlar -> OgretmenEkranDurumu.Gruplar
            is OgretmenEkranDurumu.GrupDetay -> OgretmenEkranDurumu.Gruplar
            is OgretmenEkranDurumu.SoruFormu -> OgretmenEkranDurumu.GrupDetay(mevcut.grupId)
        }
    }

    val ustMenuGorunur = altEkran == OgretmenEkranDurumu.Gruplar || altEkran == OgretmenEkranDurumu.Ayarlar

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (ustMenuGorunur) {
                NavigationBar {
                    NavigationBarItem(
                        selected = altEkran == OgretmenEkranDurumu.Gruplar,
                        onClick = { altEkran = OgretmenEkranDurumu.Gruplar },
                        icon = { Text("◈", fontWeight = FontWeight.Bold) },
                        label = { Text(stringResource(R.string.teacher_nav_groups)) },
                    )
                    NavigationBarItem(
                        selected = altEkran == OgretmenEkranDurumu.Ayarlar,
                        onClick = { altEkran = OgretmenEkranDurumu.Ayarlar },
                        icon = { Text("⚙", fontWeight = FontWeight.Bold) },
                        label = { Text(stringResource(R.string.teacher_nav_settings)) },
                    )
                }
            }
        },
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets)) {
            when (val mevcut = altEkran) {
                OgretmenEkranDurumu.Gruplar -> OgretmenGruplarEkrani(
                    veri = veri,
                    ogretmenDeposu = ogretmenDeposu,
                    onGrupAc = { grupId -> altEkran = OgretmenEkranDurumu.GrupDetay(grupId) },
                )
                OgretmenEkranDurumu.Ayarlar -> OgretmenAyarlarEkrani(
                    sonYedek = sonYedek,
                    yedeklemeIslemde = yedeklemeIslemde,
                    onOgrenciModunaGec = onOgrenciModunaGec,
                    onYedekOlustur = onYedekOlustur,
                    onYedekGeriYukle = onYedekGeriYukle,
                )
                is OgretmenEkranDurumu.GrupDetay -> {
                    val grup = veri.gruplar.firstOrNull { it.id == mevcut.grupId }
                    if (grup == null) {
                        altEkran = OgretmenEkranDurumu.Gruplar
                    } else {
                        OgretmenGrupDetayEkrani(
                            grup = grup,
                            veri = veri,
                            ogretmenDeposu = ogretmenDeposu,
                            gorselDeposu = gorselDeposu,
                            snackbarHostState = snackbarHostState,
                            onGeri = { altEkran = OgretmenEkranDurumu.Gruplar },
                            onSoruEkle = { altEkran = OgretmenEkranDurumu.SoruFormu(grup.id, null) },
                            onSoruDuzenle = { soruId -> altEkran = OgretmenEkranDurumu.SoruFormu(grup.id, soruId) },
                        )
                    }
                }
                is OgretmenEkranDurumu.SoruFormu -> {
                    val grup = veri.gruplar.firstOrNull { it.id == mevcut.grupId }
                    if (grup == null) {
                        altEkran = OgretmenEkranDurumu.Gruplar
                    } else {
                        OgretmenSoruFormuEkrani(
                            grup = grup,
                            mevcutSoru = mevcut.soruId?.let { aranan -> veri.sorular.firstOrNull { it.id == aranan } },
                            ogretmenDeposu = ogretmenDeposu,
                            gorselDeposu = gorselDeposu,
                            onGeri = { altEkran = OgretmenEkranDurumu.GrupDetay(grup.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OgretmenGruplarEkrani(
    veri: OgretmenVerisi,
    ogretmenDeposu: OgretmenDeposu,
    onGrupAc: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var grupDialogAcik by remember { mutableStateOf(false) }
    var grupAdi by remember { mutableStateOf("") }
    var grupAciklama by remember { mutableStateOf("") }
    var grupHatasi by remember { mutableStateOf(false) }
    var kaydediliyor by remember { mutableStateOf(false) }

    val hazirOneriler = listOf("6A", "6B", "7A", "7B", "8A", "8B", "Özel Ders (Ahmet)")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            PremiumExtendedFloatingActionButton(
                onClick = {
                    grupAdi = ""
                    grupAciklama = ""
                    grupHatasi = false
                    grupDialogAcik = true
                },
                modifier = Modifier.testTag("ogretmen_grup_ekle_fab"),
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Light)
                Text(
                    text = stringResource(R.string.teacher_add_group),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    ) { insets ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(insets),
            contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                GradyanKart(modifier = Modifier.fillMaxWidth(), borderAnimation = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.teacher_kicker),
                            color = PremiumMarkaRenkler.BakirIsik,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.teacher_home_title),
                            color = PremiumMarkaRenkler.Kagit,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.teacher_home_subtitle),
                            color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.84f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            if (veri.gruplar.isEmpty()) {
                item {
                    PremiumBosDurum(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        baslik = stringResource(R.string.teacher_groups_empty_title),
                        aciklama = stringResource(R.string.teacher_groups_empty_subtitle),
                        butonMetni = stringResource(R.string.teacher_add_group),
                        onButonTikla = {
                            grupAdi = ""
                            grupAciklama = ""
                            grupHatasi = false
                            grupDialogAcik = true
                        },
                    )
                }
            } else {
                val siraliGruplar = veri.gruplar.sortedWith(compareBy<OgretmenGrubu> { it.sira }.thenBy { it.ad })
                items(siraliGruplar, key = { it.id }) { grup ->
                    val grupSorulari = veri.sorular.filter { it.grupId == grup.id }
                    val cozulmemis = grupSorulari.count { !it.cozuldu }
                    val cozulmus = grupSorulari.count { it.cozuldu }
                    PremiumCard(
                        onClick = { onGrupAc(grup.id) },
                        modifier = Modifier.fillMaxWidth().testTag("ogretmen_grup_${grup.id}"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
                        interactionTur = EtkilesimTuru.KART_ACILIS,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = grup.ad.take(3).uppercase(Locale.forLanguageTag("tr-TR")),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = grup.ad,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                grup.aciklama?.let { not ->
                                    Text(
                                        text = not,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.teacher_group_counts, cozulmemis, cozulmus),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Text(
                                text = "→",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }

    if (grupDialogAcik) {
        AlertDialog(
            onDismissRequest = { if (!kaydediliyor) grupDialogAcik = false },
            title = { Text(stringResource(R.string.teacher_add_group), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        hazirOneriler.forEach { oneri ->
                            FilterChip(
                                selected = grupAdi == oneri,
                                onClick = {
                                    grupAdi = oneri
                                    grupHatasi = false
                                },
                                label = { Text(oneri) },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = grupAdi,
                        onValueChange = {
                            grupAdi = it
                            grupHatasi = false
                        },
                        modifier = Modifier.fillMaxWidth().testTag("ogretmen_grup_adi_girdi"),
                        singleLine = true,
                        isError = grupHatasi,
                        label = { Text(stringResource(R.string.teacher_group_name_label)) },
                        shape = RoundedCornerShape(14.dp),
                    )
                    OutlinedTextField(
                        value = grupAciklama,
                        onValueChange = { grupAciklama = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stringResource(R.string.teacher_group_desc_label)) },
                        shape = RoundedCornerShape(14.dp),
                    )
                    if (grupHatasi) {
                        Text(
                            text = stringResource(R.string.teacher_group_name_error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                PremiumButton(
                    enabled = !kaydediliyor,
                    modifier = Modifier.testTag("ogretmen_grup_kaydet_buton"),
                    onClick = {
                        kaydediliyor = true
                        scope.launch {
                            try {
                                val eklenen = ogretmenDeposu.grupEkle(grupAdi, grupAciklama)
                                if (eklenen != null) {
                                    grupDialogAcik = false
                                } else {
                                    grupHatasi = true
                                }
                            } finally {
                                kaydediliyor = false
                            }
                        }
                    },
                ) { Text(stringResource(R.string.teacher_group_save)) }
            },
            dismissButton = {
                PremiumTextButton(
                    enabled = !kaydediliyor,
                    onClick = { grupDialogAcik = false },
                ) { Text(stringResource(R.string.question_form_cancel)) }
            },
        )
    }
}

@Composable
private fun OgretmenGrupDetayEkrani(
    grup: OgretmenGrubu,
    veri: OgretmenVerisi,
    ogretmenDeposu: OgretmenDeposu,
    gorselDeposu: LocalSoruGorselDeposu,
    snackbarHostState: SnackbarHostState,
    onGeri: () -> Unit,
    onSoruEkle: () -> Unit,
    onSoruDuzenle: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cozulmusSekmesi by remember(grup.id) { mutableStateOf(false) }
    var zorlukFiltresi by remember(grup.id) { mutableStateOf<OgretmenZorluk?>(null) }
    var tamEkranSoruId by remember(grup.id) { mutableStateOf<String?>(null) }
    var kopyaSoru by remember(grup.id) { mutableStateOf<OgretmenSorusu?>(null) }
    var silinecekSoru by remember(grup.id) { mutableStateOf<OgretmenSorusu?>(null) }
    var grupDuzenleAcik by remember(grup.id) { mutableStateOf(false) }
    var grupSilOnayAcik by remember(grup.id) { mutableStateOf(false) }

    val grupSorulari = remember(veri.sorular, grup.id) {
        veri.sorular.filter { it.grupId == grup.id }.sortedByDescending { it.olusturma }
    }
    val cozulmemisler = remember(grupSorulari) { grupSorulari.filter { !it.cozuldu } }
    val cozulmusler = remember(grupSorulari) { grupSorulari.filter { it.cozuldu } }
    val aktifListe = remember(cozulmusSekmesi, cozulmemisler, cozulmusler, zorlukFiltresi) {
        val baz = if (cozulmusSekmesi) cozulmusler else cozulmemisler
        if (zorlukFiltresi == null) baz else baz.filter { it.zorluk == zorlukFiltresi }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            PremiumExtendedFloatingActionButton(
                onClick = onSoruEkle,
                modifier = Modifier.testTag("ogretmen_soru_ekle_fab"),
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Light)
                Text(
                    text = stringResource(R.string.teacher_add_question),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    ) { insets ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(insets),
            contentPadding = PaddingValues(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PremiumTextButton(onClick = onGeri) {
                        Text("← " + stringResource(R.string.common_back))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        PremiumTextButton(onClick = { grupDuzenleAcik = true }) {
                            Text(stringResource(R.string.teacher_edit_group))
                        }
                        PremiumTextButton(onClick = { grupSilOnayAcik = true }) {
                            Text(
                                stringResource(R.string.teacher_delete_group),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
            item {
                GradyanKart(modifier = Modifier.fillMaxWidth(), borderAnimation = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.teacher_kicker),
                            color = PremiumMarkaRenkler.BakirIsik,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = grup.ad,
                            color = PremiumMarkaRenkler.Kagit,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        grup.aciklama?.let { not ->
                            Text(
                                text = not,
                                color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.84f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PremiumButton(
                        onClick = { cozulmusSekmesi = false },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("sekme_cozmediklerim"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!cozulmusSekmesi) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = if (!cozulmusSekmesi) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        ),
                    ) {
                        Text(
                            stringResource(R.string.teacher_tab_unsolved, cozulmemisler.size),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    PremiumButton(
                        onClick = { cozulmusSekmesi = true },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("sekme_cozduklerim"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (cozulmusSekmesi) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = if (cozulmusSekmesi) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        ),
                    ) {
                        Text(
                            stringResource(R.string.teacher_tab_solved, cozulmusler.size),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = zorlukFiltresi == null,
                        onClick = { zorlukFiltresi = null },
                        label = { Text(stringResource(R.string.teacher_filter_all)) },
                    )
                    OgretmenZorluk.entries.forEach { z ->
                        FilterChip(
                            selected = zorlukFiltresi == z,
                            onClick = { zorlukFiltresi = if (zorlukFiltresi == z) null else z },
                            label = { Text(stringResource(zorlukEtiketi(z))) },
                        )
                    }
                }
            }

            if (aktifListe.isEmpty()) {
                item {
                    PremiumBosDurum(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        baslik = stringResource(
                            if (cozulmusSekmesi) R.string.teacher_solved_empty_title
                            else R.string.teacher_unsolved_empty_title,
                        ),
                        aciklama = stringResource(
                            if (cozulmusSekmesi) R.string.teacher_solved_empty_subtitle
                            else R.string.teacher_unsolved_empty_subtitle,
                        ),
                    )
                }
            } else {
                itemsIndexed(aktifListe, key = { _, s -> s.id }) { idx, soru ->
                    OgretmenSoruKarti(
                        siraNo = idx + 1,
                        soru = soru,
                        gorselDeposu = gorselDeposu,
                        onTamEkran = { tamEkranSoruId = soru.id },
                        onCozulduDegistir = { yeniDurum ->
                            scope.launch { ogretmenDeposu.cozulduDurumuDegistir(soru.id, yeniDurum) }
                        },
                        onBaskaGrubaEkle = { kopyaSoru = soru },
                        onDuzenle = { onSoruDuzenle(soru.id) },
                        onSil = { silinecekSoru = soru },
                    )
                }
            }
        }
    }

    tamEkranSoruId?.let { id ->
        val canliSoru = veri.sorular.firstOrNull { it.id == id }
        if (canliSoru != null) {
            OgretmenTamEkranSoruDialog(
                grupAdi = grup.ad,
                soru = canliSoru,
                gorselDeposu = gorselDeposu,
                onDismiss = { tamEkranSoruId = null },
                onCozulduDegistir = { yeniDurum ->
                    scope.launch { ogretmenDeposu.cozulduDurumuDegistir(canliSoru.id, yeniDurum) }
                },
                onBaskaGrubaEkle = {
                    tamEkranSoruId = null
                    kopyaSoru = canliSoru
                },
            )
        } else {
            tamEkranSoruId = null
        }
    }

    kopyaSoru?.let { kaynak ->
        val digerGruplar = veri.gruplar.filter { it.id != grup.id }
            .sortedWith(compareBy<OgretmenGrubu> { it.sira }.thenBy { it.ad })
        BaskaGrubaEkleDialog(
            digerGruplar = digerGruplar,
            onDismiss = { kopyaSoru = null },
            onOnayla = { secilenler ->
                scope.launch {
                    val adet = ogretmenDeposu.baskaGruplaraKopyala(kaynak.id, secilenler)
                    kopyaSoru = null
                    if (adet > 0) {
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.teacher_copy_success, adet),
                        )
                    }
                }
            },
        )
    }

    silinecekSoru?.let { hedef ->
        AlertDialog(
            onDismissRequest = { silinecekSoru = null },
            title = { Text(stringResource(R.string.teacher_delete_question_title)) },
            text = { Text(stringResource(R.string.teacher_delete_question_message)) },
            confirmButton = {
                PremiumButton(
                    onClick = {
                        scope.launch {
                            ogretmenDeposu.soruSil(hedef.id)
                            silinecekSoru = null
                        }
                    },
                ) { Text(stringResource(R.string.question_delete)) }
            },
            dismissButton = {
                PremiumTextButton(onClick = { silinecekSoru = null }) {
                    Text(stringResource(R.string.question_form_cancel))
                }
            },
        )
    }

    if (grupDuzenleAcik) {
        var yeniAd by remember(grup.id) { mutableStateOf(grup.ad) }
        var yeniAciklama by remember(grup.id) { mutableStateOf(grup.aciklama.orEmpty()) }
        var hata by remember(grup.id) { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { grupDuzenleAcik = false },
            title = { Text(stringResource(R.string.teacher_edit_group)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = yeniAd,
                        onValueChange = {
                            yeniAd = it
                            hata = false
                        },
                        singleLine = true,
                        isError = hata,
                        label = { Text(stringResource(R.string.teacher_group_name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = yeniAciklama,
                        onValueChange = { yeniAciklama = it },
                        singleLine = true,
                        label = { Text(stringResource(R.string.teacher_group_desc_label)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (hata) {
                        Text(
                            stringResource(R.string.teacher_group_name_error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                PremiumButton(
                    onClick = {
                        scope.launch {
                            if (ogretmenDeposu.grupGuncelle(grup.id, yeniAd, yeniAciklama)) {
                                grupDuzenleAcik = false
                            } else {
                                hata = true
                            }
                        }
                    },
                ) { Text(stringResource(R.string.teacher_group_save)) }
            },
            dismissButton = {
                PremiumTextButton(onClick = { grupDuzenleAcik = false }) {
                    Text(stringResource(R.string.question_form_cancel))
                }
            },
        )
    }

    if (grupSilOnayAcik) {
        AlertDialog(
            onDismissRequest = { grupSilOnayAcik = false },
            title = { Text(stringResource(R.string.teacher_delete_group)) },
            text = { Text(stringResource(R.string.teacher_delete_group_confirm, grup.ad)) },
            confirmButton = {
                PremiumButton(
                    onClick = {
                        scope.launch {
                            grupSilOnayAcik = false
                            ogretmenDeposu.grupSil(grup.id)
                            onGeri()
                        }
                    },
                ) { Text(stringResource(R.string.question_delete)) }
            },
            dismissButton = {
                PremiumTextButton(onClick = { grupSilOnayAcik = false }) {
                    Text(stringResource(R.string.question_form_cancel))
                }
            },
        )
    }
}

@Composable
private fun OgretmenSoruKarti(
    siraNo: Int,
    soru: OgretmenSorusu,
    gorselDeposu: LocalSoruGorselDeposu,
    onTamEkran: () -> Unit,
    onCozulduDegistir: (Boolean) -> Unit,
    onBaskaGrubaEkle: () -> Unit,
    onDuzenle: () -> Unit,
    onSil: () -> Unit,
) {
    val ilkDosya = soru.gorseller.minByOrNull { it.sira }?.let { gorselDeposu.dosyaGoruntuIcin(it.dosyaAdi) }

    PremiumCard(
        onClick = onTamEkran,
        modifier = Modifier.fillMaxWidth().testTag("ogretmen_soru_kart_${soru.id}"),
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
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = soru.baslik ?: stringResource(R.string.teacher_question_default_title, siraNo),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                soru.zorluk?.let { z ->
                    ZorlukRozeti(z)
                }
            }
            if (!soru.cevap.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.teacher_answer_hidden_badge),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PremiumOutlinedButton(
                    onClick = onTamEkran,
                    modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(stringResource(R.string.ask_teacher_open_fullscreen))
                }
                PremiumButton(
                    onClick = { onCozulduDegistir(!soru.cozuldu) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .testTag("ogretmen_cozuldu_degistir_${soru.id}"),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        stringResource(
                            if (soru.cozuldu) R.string.teacher_mark_unsolved
                            else R.string.teacher_mark_solved,
                        ),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PremiumTextButton(
                    onClick = onBaskaGrubaEkle,
                    modifier = Modifier.testTag("ogretmen_baska_gruba_ekle_${soru.id}"),
                ) {
                    Text(stringResource(R.string.teacher_copy_to_group))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    PremiumTextButton(onClick = onDuzenle) {
                        Text(stringResource(R.string.question_edit))
                    }
                    PremiumTextButton(onClick = onSil) {
                        Text(
                            stringResource(R.string.question_delete),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZorlukRozeti(zorluk: OgretmenZorluk) {
    val renk = when (zorluk) {
        OgretmenZorluk.KOLAY -> Color(0xFF2E7D32)
        OgretmenZorluk.ORTA -> Color(0xFFEF6C00)
        OgretmenZorluk.ZOR -> Color(0xFFC62828)
    }
    Surface(
        shape = CircleShape,
        color = renk.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, renk.copy(alpha = 0.35f)),
    ) {
        Text(
            text = stringResource(zorlukEtiketi(zorluk)),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = renk,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun OgretmenTamEkranSoruDialog(
    grupAdi: String,
    soru: OgretmenSorusu,
    gorselDeposu: LocalSoruGorselDeposu,
    onDismiss: () -> Unit,
    onCozulduDegistir: (Boolean) -> Unit,
    onBaskaGrubaEkle: () -> Unit,
) {
    val gorseller = remember(soru.gorseller) { soru.gorseller.sortedBy { it.sira } }
    var sayfa by remember(soru.id) { mutableIntStateOf(0) }
    var cevapAcik by remember(soru.id) { mutableStateOf(false) }
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
                        text = listOfNotNull(grupAdi, soru.baslik).joinToString(" · "),
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
                    }
                }
                soru.zorluk?.let { ZorlukRozeti(it) }
                PremiumTextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.common_close), color = Color.White)
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        PremiumTextButton(
                            onClick = { if (sayfa > 0) sayfa-- },
                            enabled = sayfa > 0,
                        ) { Text(stringResource(R.string.ask_teacher_prev_page), color = Color.White) }
                        PremiumTextButton(
                            onClick = { if (sayfa < gorseller.lastIndex) sayfa++ },
                            enabled = sayfa < gorseller.lastIndex,
                        ) { Text(stringResource(R.string.ask_teacher_next_page), color = Color.White) }
                    }
                }
                if (!soru.cevap.isNullOrBlank()) {
                    if (cevapAcik) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().testTag("ogretmen_cevap_kutusu"),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1F2B38),
                        ) {
                            Text(
                                text = stringResource(R.string.teacher_answer_prefix, soru.cevap),
                                modifier = Modifier.padding(14.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    PremiumOutlinedButton(
                        onClick = { cevapAcik = !cevapAcik },
                        modifier = Modifier.fillMaxWidth().testTag("ogretmen_cevabi_goster"),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            stringResource(
                                if (cevapAcik) R.string.teacher_hide_answer else R.string.teacher_show_answer,
                            ),
                            color = Color.White,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PremiumOutlinedButton(
                        onClick = onBaskaGrubaEkle,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.teacher_copy_to_group), color = Color.White)
                    }
                    PremiumButton(
                        onClick = { onCozulduDegistir(!soru.cozuldu) },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            stringResource(
                                if (soru.cozuldu) R.string.teacher_mark_unsolved
                                else R.string.teacher_mark_solved,
                            ),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BaskaGrubaEkleDialog(
    digerGruplar: List<OgretmenGrubu>,
    onDismiss: () -> Unit,
    onOnayla: (Set<String>) -> Unit,
) {
    var secilenler by remember { mutableStateOf<Set<String>>(emptySet()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.teacher_copy_dialog_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.teacher_copy_dialog_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (digerGruplar.isEmpty()) {
                    Text(
                        stringResource(R.string.teacher_copy_no_other_groups),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    digerGruplar.forEach { hedef ->
                        val isaretli = hedef.id in secilenler
                        Surface(
                            onClick = {
                                secilenler = if (isaretli) secilenler - hedef.id else secilenler + hedef.id
                            },
                            modifier = Modifier.fillMaxWidth().testTag("hedef_grup_${hedef.id}"),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(checked = isaretli, onCheckedChange = null)
                                Text(
                                    text = hedef.ad,
                                    modifier = Modifier.padding(start = 8.dp),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (digerGruplar.isNotEmpty()) {
                PremiumButton(
                    enabled = secilenler.isNotEmpty(),
                    modifier = Modifier.testTag("hedef_gruplara_kopyala_onay"),
                    onClick = { onOnayla(secilenler) },
                ) { Text(stringResource(R.string.teacher_copy_confirm)) }
            }
        },
        dismissButton = {
            PremiumTextButton(onClick = onDismiss) {
                Text(stringResource(R.string.question_form_cancel))
            }
        },
    )
}

@Composable
private fun OgretmenSoruFormuEkrani(
    grup: OgretmenGrubu,
    mevcutSoru: OgretmenSorusu?,
    ogretmenDeposu: OgretmenDeposu,
    gorselDeposu: LocalSoruGorselDeposu,
    onGeri: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var gorseller by remember(mevcutSoru?.id) {
        mutableStateOf(mevcutSoru?.gorseller.orEmpty().sortedBy { it.sira }.map { it.toGirdi() })
    }
    var baslik by remember(mevcutSoru?.id) { mutableStateOf(mevcutSoru?.baslik.orEmpty()) }
    var zorluk by remember(mevcutSoru?.id) { mutableStateOf(mevcutSoru?.zorluk) }
    var cevap by remember(mevcutSoru?.id) { mutableStateOf(mevcutSoru?.cevap.orEmpty()) }
    var kaydediliyor by remember(mevcutSoru?.id) { mutableStateOf(false) }
    var formHatasi by remember(mevcutSoru?.id) { mutableStateOf<Int?>(null) }
    var kameraCekimi by remember(mevcutSoru?.id) { mutableStateOf<KameraCekimi?>(null) }
    var kirpmaHedefi by remember(mevcutSoru?.id) { mutableStateOf<SoruGorselGirdisi?>(null) }
    var kirpmaIsleniyor by remember(mevcutSoru?.id) { mutableStateOf(false) }

    fun iptalEtVeDon() {
        val geciciler = gorseller.filter { it.gecici }
        scope.launch {
            geciciler.forEach { runCatching { gorselDeposu.geciciDosyayiSil(it.dosyaAdi) } }
        }
        onGeri()
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
                if (gorseller.size >= 20) {
                    gorselDeposu.kameraCekiminiSil(aktif.id)
                    snackbarHostState.showSnackbar(context.getString(R.string.question_form_photo_limit))
                    return@launch
                }
                val girdi = gorselDeposu.kameradanHazirla(aktif.id, GorselTuru.SORU, gorseller.size)
                gorseller = gorseller + girdi
                formHatasi = null
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Throwable) {
                snackbarHostState.showSnackbar(context.getString(R.string.question_form_photo_error))
            }
        }
    }

    val kameraIzni = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { verildi ->
        if (verildi) {
            try {
                val cekim = gorselDeposu.kameraCekimiOlustur()
                kameraCekimi = cekim
                takePicture.launch(cekim.uri)
            } catch (_: Throwable) {
                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.question_form_photo_error)) }
            }
        }
    }

    val galeriSecici = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val liste = gorseller.toMutableList()
                for (uri in uris) {
                    if (liste.size >= 20) break
                    liste += gorselDeposu.secicidenHazirla(uri, GorselTuru.SORU, liste.size)
                }
                gorseller = liste
                if (gorseller.isNotEmpty()) formHatasi = null
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Throwable) {
                snackbarHostState.showSnackbar(context.getString(R.string.question_form_photo_error))
            }
        }
    }

    fun kamerayiAc() {
        val izinli = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (izinli) {
            try {
                val cekim = gorselDeposu.kameraCekimiOlustur()
                kameraCekimi = cekim
                takePicture.launch(cekim.uri)
            } catch (_: Throwable) {
                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.question_form_photo_error)) }
            }
        } else {
            kameraIzni.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(tonalElevation = 3.dp, shadowElevation = 4.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    formHatasi?.let { hataId ->
                        Text(
                            text = stringResource(hataId),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    PremiumButton(
                        onClick = {
                            if (kaydediliyor) return@PremiumButton
                            if (gorseller.isEmpty()) {
                                formHatasi = R.string.question_error_photo_required
                                return@PremiumButton
                            }
                            kaydediliyor = true
                            scope.launch {
                                try {
                                    val sonuc = ogretmenDeposu.soruKaydet(
                                        id = mevcutSoru?.id,
                                        grupId = grup.id,
                                        baslik = baslik,
                                        zorluk = zorluk,
                                        cevap = cevap,
                                        gorseller = gorseller,
                                    )
                                    if (sonuc != null) {
                                        onGeri()
                                    } else {
                                        formHatasi = R.string.question_error_generic
                                    }
                                } finally {
                                    kaydediliyor = false
                                }
                            }
                        },
                        enabled = !kaydediliyor && !kirpmaIsleniyor,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).testTag("ogretmen_soru_kaydet"),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(stringResource(R.string.teacher_question_save), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                PremiumTextButton(onClick = ::iptalEtVeDon, enabled = !kaydediliyor) {
                    Text("← " + stringResource(R.string.question_form_cancel))
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
                                if (mevcutSoru == null) R.string.teacher_add_question
                                else R.string.teacher_edit_question,
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
                            onCamera = ::kamerayiAc,
                            onPicker = {
                                galeriSecici.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                            onCrop = { kirpmaHedefi = it },
                            onImagesChanged = {
                                gorseller = it
                                if (it.isNotEmpty()) formHatasi = null
                            },
                        )
                    }
                }
            }
            item {
                CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(R.string.teacher_question_difficulty_label),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OgretmenZorluk.entries.forEach { secenek ->
                                FilterChip(
                                    selected = zorluk == secenek,
                                    onClick = { zorluk = if (zorluk == secenek) null else secenek },
                                    label = { Text(stringResource(zorlukEtiketi(secenek))) },
                                )
                            }
                        }
                        OutlinedTextField(
                            value = cevap,
                            onValueChange = { cevap = it },
                            modifier = Modifier.fillMaxWidth().testTag("ogretmen_soru_cevap_girdi"),
                            enabled = !kaydediliyor,
                            minLines = 2,
                            label = { Text(stringResource(R.string.teacher_question_answer_label)) },
                            shape = RoundedCornerShape(14.dp),
                        )
                        OutlinedTextField(
                            value = baslik,
                            onValueChange = { baslik = it },
                            modifier = Modifier.fillMaxWidth().testTag("ogretmen_soru_baslik_girdi"),
                            enabled = !kaydediliyor,
                            singleLine = true,
                            label = { Text(stringResource(R.string.teacher_question_title_label)) },
                            shape = RoundedCornerShape(14.dp),
                        )
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
                            val idx = gorseller.indexOfFirst { it.id == target.id }
                            if (idx >= 0) {
                                gorseller = gorseller.toMutableList().apply {
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
                            snackbarHostState.showSnackbar(context.getString(R.string.question_form_crop_error))
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

@Composable
private fun OgretmenAyarlarEkrani(
    sonYedek: SonYedekBilgisi?,
    yedeklemeIslemde: Boolean,
    onOgrenciModunaGec: () -> Unit,
    onYedekOlustur: () -> Unit,
    onYedekGeriYukle: () -> Unit,
) {
    val context = LocalContext.current
    val sesEfektleri = LocalUygulamaSesleri.current
    var sesAcik by remember { mutableStateOf(sesEfektleri?.acikMi() ?: false) }

    val sonYedekMetni = sonYedek?.let { backup ->
        val tarih = SimpleDateFormat("d MMM yyyy HH:mm", Locale.forLanguageTag("tr-TR"))
            .format(Date(backup.olusturmaZamani))
        val boyut = Formatter.formatShortFileSize(context, backup.boyutBayt)
        context.getString(
            R.string.backup_last_info,
            backup.dosyaAdi,
            tarih,
            boyut,
            context.getString(if (backup.guvenlikKopyasi) R.string.backup_safety_copy else R.string.backup_external_copy),
        )
    } ?: stringResource(R.string.backup_last_none)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { insets ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(insets),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                GradyanKart(modifier = Modifier.fillMaxWidth(), borderAnimation = false) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.settings_kicker),
                            color = PremiumMarkaRenkler.BakirIsik,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.settings_title),
                            color = PremiumMarkaRenkler.Kagit,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.settings_role_current_teacher),
                            color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            item {
                CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.settings_role_section_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.settings_role_switch_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        PremiumOutlinedButton(
                            onClick = onOgrenciModunaGec,
                            enabled = !yedeklemeIslemde,
                            modifier = Modifier.fillMaxWidth().testTag("ogrenci_moduna_gec_buton"),
                        ) {
                            Text(stringResource(R.string.settings_switch_to_student))
                        }
                    }
                }
            }
            item {
                CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.settings_backup_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = sonYedekMetni,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        PremiumButton(
                            onClick = onYedekOlustur,
                            enabled = !yedeklemeIslemde,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.menu_create_backup))
                        }
                        PremiumOutlinedButton(
                            onClick = onYedekGeriYukle,
                            enabled = !yedeklemeIslemde,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.menu_restore_backup))
                        }
                    }
                }
            }
            item {
                CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.settings_sound_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        PremiumSwitch(
                            checked = sesAcik,
                            onCheckedChange = { yeni ->
                                sesAcik = yeni
                                sesEfektleri?.ayarla(yeni)
                            },
                        )
                    }
                }
            }
            item {
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@androidx.annotation.StringRes
private fun zorlukEtiketi(zorluk: OgretmenZorluk): Int = when (zorluk) {
    OgretmenZorluk.KOLAY -> R.string.teacher_difficulty_easy
    OgretmenZorluk.ORTA -> R.string.teacher_difficulty_medium
    OgretmenZorluk.ZOR -> R.string.teacher_difficulty_hard
}

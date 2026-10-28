package com.keremtulu.questionnote.ui.deneme

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.prefs.DenemeDeposu
import com.keremtulu.questionnote.domain.model.DenemeAdiDurumu
import com.keremtulu.questionnote.domain.model.DenemeDersSatiri
import com.keremtulu.questionnote.domain.model.DenemeKaydi
import com.keremtulu.questionnote.domain.model.SORU_SECENEGI_ADI_UST_SINIR
import com.keremtulu.questionnote.domain.model.denemeAdiDogrula
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.DersNetKarsilastirma
import com.keremtulu.questionnote.domain.model.NetKurali
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.denemeKaynakEslesir
import com.keremtulu.questionnote.domain.model.dersBarlari
import com.keremtulu.questionnote.domain.model.karsilastirilabilirler
import com.keremtulu.questionnote.domain.model.kapsamTemsilcileri
import com.keremtulu.questionnote.domain.model.kronolojik
import com.keremtulu.questionnote.domain.model.oncekiKarsilastirilabilir
import com.keremtulu.questionnote.domain.model.yerelTarih
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository
import com.keremtulu.questionnote.ui.animasyon.SureToken
import com.keremtulu.questionnote.ui.animasyon.animateFloatPremium
import com.keremtulu.questionnote.ui.animasyon.animateItemInsertion
import com.keremtulu.questionnote.ui.animasyon.tweenPremium
import com.keremtulu.questionnote.ui.bilesenler.hatirlaYerelGun
import com.keremtulu.questionnote.ui.bilesenler.SadeKart
import com.keremtulu.questionnote.ui.bilesenler.premium.CamYukseklik
import com.keremtulu.questionnote.ui.bilesenler.premium.PremiumBosDurum
import com.keremtulu.questionnote.ui.dersKartStiliGetir
import com.keremtulu.questionnote.ui.premium.EtkilesimTuru
import com.keremtulu.questionnote.ui.premium.PremiumButton
import com.keremtulu.questionnote.ui.premium.PremiumCard
import com.keremtulu.questionnote.ui.premium.PremiumExtendedFloatingActionButton
import com.keremtulu.questionnote.ui.bilesenler.SadeAnaButon
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumSembolAilesi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.keremtulu.questionnote.ui.bilesenler.premium.CamKarti
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.premium.PremiumGradyanButon

private val DenemeTurkceLocale: Locale = Locale.forLanguageTag("tr-TR")
private val DenemeTarihBicimi: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", DenemeTurkceLocale)
private val DenemeUzunTarihBicimi: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy, EEEE", DenemeTurkceLocale)

private val MetinHaritasiSaver = listSaver<Map<String, String>, String>(
    save = { harita -> harita.entries.flatMap { listOf(it.key, it.value) } },
    restore = { liste ->
        liste.chunked(2).filter { it.size == 2 }.associate { it[0] to it[1] }
    },
)

fun denemeTarihYazi(gun: Int): String = gun.yerelTarih().format(DenemeTarihBicimi)

fun denemeUzunTarihYazi(gun: Int): String =
    gun.yerelTarih().format(DenemeUzunTarihBicimi)
        .replaceFirstChar { it.titlecase(DenemeTurkceLocale) }

fun netYazi(deger: Double): String {
    val temiz = if (abs(deger) < 0.005) 0.0 else deger
    return if (abs(temiz - temiz.roundToInt()) < 0.005) {
        temiz.roundToInt().toString()
    } else {
        String.format(DenemeTurkceLocale, "%.2f", temiz)
            .trimEnd('0')
            .trimEnd(',')
            .trimEnd('.')
    }
}

@Composable
internal fun netKuraliEtiketi(kural: NetKurali): String = when (kural) {
    NetKurali.DORTTE_BIR -> stringResource(R.string.deneme_rule_4)
    NetKurali.UCTE_BIR -> stringResource(R.string.deneme_rule_3)
    NetKurali.YOK -> stringResource(R.string.deneme_rule_none)
}

private sealed interface DenemeSoruTakibi {
    data object Yukleniyor : DenemeSoruTakibi
    data object Hata : DenemeSoruTakibi
    data class Hazir(val sorular: List<Soru>) : DenemeSoruTakibi
}

@Composable
private fun tumSorulariIzle(
    dersler: List<Ders>,
    soruRepository: SoruRepository?,
): DenemeSoruTakibi {
    val akis = remember(dersler, soruRepository) {
        if (soruRepository == null || dersler.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(dersler.map { ders -> soruRepository.sorulariIzle(ders.id) }) { gruplar ->
                gruplar.flatMap { it }
            }
        }
    }
    val sonuc by produceState<DenemeSoruTakibi>(DenemeSoruTakibi.Yukleniyor, akis) {
        value = DenemeSoruTakibi.Yukleniyor
        akis.catch { value = DenemeSoruTakibi.Hata }
            .collect { value = DenemeSoruTakibi.Hazir(it) }
    }
    return sonuc
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DenemeListesiEkrani(
    denemeDeposu: DenemeDeposu,
    dersRepository: DersRepository,
    soruRepository: SoruRepository? = null,
    onGeri: () -> Unit,
    onYeni: () -> Unit,
    onAc: (String) -> Unit,
    onYedekAyarlari: (() -> Unit)? = null,
) {
    val kayitlar by denemeDeposu.kayitlariIzle.collectAsState()
    val veriBozuk by denemeDeposu.bozukMuIzle.collectAsState()
    val tumDersler by remember(dersRepository) {
        combine(
            dersRepository.dersleriIzle(false),
            dersRepository.dersleriIzle(true),
        ) { acik, arsiv -> acik + arsiv }
    }.collectAsState(initial = emptyList())
    val dersAdlari = remember(tumDersler) { tumDersler.associate { it.id to it.ad } }
    val soruTakibi = tumSorulariIzle(tumDersler, soruRepository)
    val tumSorular = (soruTakibi as? DenemeSoruTakibi.Hazir)?.sorular

    val mevcutKurallar = remember(kayitlar) { kayitlar.map { it.kural }.distinct() }
    var seciliKuralAdi by rememberSaveable { mutableStateOf<String?>(null) }
    var seciliKapsamId by rememberSaveable { mutableStateOf<String?>(null) }
    var grafikLimit by rememberSaveable { mutableIntStateOf(12) }
    val seciliKural = remember(seciliKuralAdi, mevcutKurallar) {
        seciliKuralAdi?.let { ad -> NetKurali.entries.firstOrNull { it.name == ad } }
            ?.takeIf { it in mevcutKurallar } ?: mevcutKurallar.firstOrNull()
    }
    val seciliKuralKayitlari = remember(kayitlar, seciliKural) {
        kayitlar.filter { it.kural == seciliKural }.kronolojik()
    }
    val kapsamlar = remember(seciliKuralKayitlari) { seciliKuralKayitlari.kapsamTemsilcileri() }
    // Keep a selected group even when a newer exam is added to it; fall back safely if it was deleted.
    val kapsamOrnegi = seciliKuralKayitlari.firstOrNull { it.id == seciliKapsamId }
        ?: kapsamlar.firstOrNull()
    val eslesenKayitlar = remember(kayitlar, kapsamOrnegi) {
        kapsamOrnegi?.let { kayitlar.karsilastirilabilirler(it) }.orEmpty()
    }
    val sonDeneme = eslesenKayitlar.lastOrNull()
    val grafikKayitlari = remember(eslesenKayitlar, grafikLimit) { eslesenKayitlar.takeLast(grafikLimit) }
    val uyumsuzSayisi = (seciliKuralKayitlari.size - eslesenKayitlar.size).coerceAtLeast(0)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            if (!veriBozuk && kayitlar.isNotEmpty()) {
                PremiumExtendedFloatingActionButton(
                    onClick = onYeni,
                    modifier = Modifier.testTag("deneme-fab-new"),
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text("+", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = stringResource(R.string.deneme_new),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .consumeWindowInsets(insets),
        ) {
            DenemeUstCubuk(
                baslik = stringResource(R.string.deneme_title),
                onGeri = onGeri,
            )

            if (veriBozuk) {
                DenemeBozukIcerik(denemeDeposu, onYedekAyarlari)
            } else if (kayitlar.isEmpty()) {
                PremiumBosDurum(
                    illustration = { DenemeBosIlustrasyon() },
                    baslik = stringResource(R.string.deneme_empty_title),
                    aciklama = stringResource(R.string.deneme_empty),
                    butonMetni = stringResource(R.string.deneme_new),
                    onButonTikla = onYeni,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("deneme-empty-state"),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("deneme-list"),
                    contentPadding = PaddingValues(start = 20.dp, top = 6.dp, end = 20.dp, bottom = 104.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.deneme_list_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (soruTakibi == DenemeSoruTakibi.Hata && soruRepository != null) {
                        item {
                            Text(stringResource(R.string.deneme_question_counts_error),
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.testTag("deneme-question-counts-error"))
                        }
                    }
                    if (sonDeneme != null && seciliKural != null) {
                        item {
                            DenemeTrendKarti(
                                noktalar = grafikKayitlari,
                                kurallar = mevcutKurallar,
                                seciliKural = seciliKural,
                                kapsamlar = kapsamlar,
                                uyumsuzSayisi = uyumsuzSayisi,
                                toplamEslesen = eslesenKayitlar.size,
                                limit = grafikLimit,
                                oncekiNet = kayitlar.oncekiKarsilastirilabilir(sonDeneme)?.toplamNet(),
                                onKuralSec = { seciliKuralAdi = it.name; seciliKapsamId = null },
                                onKapsamSec = { seciliKapsamId = it.id },
                                onLimitSec = { grafikLimit = it },
                            )
                        }
                        item { DenemeDagilimKarti(sonDeneme) }
                        item {
                            DenemeDersKarti(
                                son = sonDeneme,
                                barlar = kayitlar.dersBarlari(sonDeneme),
                                dersAdlari = dersAdlari,
                            )
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.deneme_history_header, kayitlar.size),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }

                    itemsIndexed(kayitlar, key = { _, deneme -> deneme.id }) { index, deneme ->
                        val giris by animateItemInsertion(index)
                        val onceki = remember(kayitlar, deneme.id) { kayitlar.oncekiKarsilastirilabilir(deneme) }
                        val yuklenen = remember(tumSorular, deneme.ad) {
                            tumSorular?.count { denemeKaynakEslesir(it.kaynak, deneme.ad) }
                        }
                        DenemeListeKarti(
                            deneme = deneme,
                            fark = onceki?.let { deneme.toplamNet() - it.toplamNet() },
                            yuklenenSoruSayisi = if (soruRepository != null) yuklenen else null,
                            onClick = { onAc(deneme.id) },
                            modifier = Modifier.graphicsLayer {
                                alpha = giris
                                translationY = (1f - giris) * 18f
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DenemeBozukIcerik(denemeDeposu: DenemeDeposu, onYedekAyarlari: (() -> Unit)?) {
    val scope = rememberCoroutineScope()
    var onay by rememberSaveable { mutableStateOf(false) }
    var islemde by remember { mutableStateOf(false) }
    var hata by remember { mutableStateOf(false) }

    if (onay) {
        AlertDialog(
            onDismissRequest = { if (!islemde) onay = false },
            title = { Text(stringResource(R.string.deneme_storage_reset_title)) },
            text = {
                Text(
                    text = stringResource(R.string.deneme_storage_reset_body),
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                )
            },
            confirmButton = {
                PremiumButton(
                    enabled = !islemde,
                    modifier = Modifier.testTag("deneme-storage-reset-confirm"),
                    onClick = {
                        scope.launch {
                            islemde = true
                            try {
                                withContext(Dispatchers.IO) { denemeDeposu.bozukKayitlariSifirla() }
                                onay = false
                            } catch (cancel: CancellationException) {
                                throw cancel
                            } catch (_: Exception) {
                                hata = true
                                onay = false
                            } finally {
                                islemde = false
                            }
                        }
                    },
                ) { Text(stringResource(R.string.deneme_storage_reset)) }
            },
            dismissButton = {
                PremiumTextButton(enabled = !islemde, onClick = { onay = false }) {
                    Text(stringResource(R.string.topic_cancel))
                }
            },
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(20.dp).testTag("deneme-storage-error"),
    ) {
        SadeKart {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = stringResource(R.string.deneme_storage_error_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = stringResource(R.string.deneme_storage_error_body),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (onYedekAyarlari != null) {
                    PremiumButton(onClick = onYedekAyarlari, enabled = !islemde) {
                        Text(stringResource(R.string.deneme_storage_backup_settings))
                    }
                }
                PremiumOutlinedButton(
                    onClick = { onay = true },
                    enabled = !islemde,
                    modifier = Modifier.testTag("deneme-storage-reset"),
                ) { Text(stringResource(R.string.deneme_storage_reset)) }
                if (hata) Text(stringResource(R.string.deneme_storage_reset_failed), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun DenemeListeKarti(
    deneme: DenemeKaydi,
    fark: Double?,
    yuklenenSoruSayisi: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val dogruToplam = deneme.satirlar.sumOf { it.dogru.toLong() }
    val yanlisToplam = deneme.satirlar.sumOf { it.yanlis.toLong() }
    val bosToplam = deneme.satirlar.sumOf { it.bos.toLong() }
    val hedefYukleme = deneme.toplamYuklenecek

    PremiumCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("deneme-card-${deneme.id}"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.55f)),
        interactionTur = EtkilesimTuru.KART_ACILIS,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = deneme.ad,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${denemeTarihYazi(deneme.gun)} · ${netKuraliEtiketi(deneme.kural)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
                NetOzetRozeti(net = deneme.toplamNet(), fark = fark)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.deneme_card_counts, dogruToplam, yanlisToplam, bosToplam),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant,
                )
                if (yuklenenSoruSayisi != null) {
                    val (etiket, arkaPlan, metinRengi) = when {
                        hedefYukleme == 0L -> Triple(
                            stringResource(R.string.deneme_upload_clean),
                            colors.secondaryContainer.copy(alpha = 0.6f),
                            colors.onSecondaryContainer,
                        )
                        yuklenenSoruSayisi >= hedefYukleme -> Triple(
                            stringResource(R.string.deneme_upload_complete, yuklenenSoruSayisi),
                            PremiumMarkaRenkler.Yesil.copy(alpha = 0.16f),
                            if (isSystemInDarkTheme()) PremiumMarkaRenkler.YesilAcik else PremiumMarkaRenkler.Yesil,
                        )
                        else -> Triple(
                            stringResource(R.string.deneme_upload_progress, yuklenenSoruSayisi, hedefYukleme),
                            colors.primaryContainer.copy(alpha = 0.7f),
                            colors.onPrimaryContainer,
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = arkaPlan,
                    ) {
                        Text(
                            text = etiket,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = metinRengi,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NetOzetRozeti(
    net: Double,
    fark: Double?,
) {
    val colors = MaterialTheme.colorScheme
    val koyu = isSystemInDarkTheme()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (fark != null && abs(fark) >= 0.05) {
            val artis = fark > 0
            val vurgu = if (artis) {
                if (koyu) PremiumMarkaRenkler.YesilAcik else PremiumMarkaRenkler.Yesil
            } else {
                if (koyu) PremiumMarkaRenkler.BakirIsik else PremiumMarkaRenkler.Bakir
            }
            val farkYazi = if (artis) {
                stringResource(R.string.deneme_delta_positive, netYazi(fark))
            } else {
                netYazi(fark)
            }
            val farkAciklamasi = stringResource(R.string.deneme_chart_change, farkYazi)
            Surface(
                modifier = Modifier.clearAndSetSemantics { contentDescription = farkAciklamasi },
                shape = RoundedCornerShape(999.dp),
                color = vurgu.copy(alpha = 0.15f),
            ) {
                Text(
                    text = farkYazi,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = vurgu,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.primary.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.25f)),
        ) {
            Text(
                text = stringResource(R.string.deneme_net_badge, netYazi(net)),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                style = MaterialTheme.typography.titleSmall,
                color = colors.primary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private data class DenemeFormDersi(
    val ders: Ders,
    val gizli: Boolean,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DenemeDetayEkrani(
    denemeId: String?,
    denemeDeposu: DenemeDeposu,
    dersRepository: DersRepository,
    soruRepository: SoruRepository,
    onGeri: () -> Unit,
    onKaydedildi: (String) -> Unit,
    onSoruEkle: (Ders, String, String) -> Unit,
    onDenemeyiCalis: ((String) -> Unit)? = null,
    onYedekAyarlari: (() -> Unit)? = null,
) {
    val veriBozuk by denemeDeposu.bozukMuIzle.collectAsState()
    if (veriBozuk) {
        Scaffold(containerColor = MaterialTheme.colorScheme.background, contentWindowInsets = WindowInsets.safeDrawing) { insets ->
            Column(Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets)) {
                DenemeUstCubuk(baslik = stringResource(R.string.deneme_title), onGeri = onGeri)
                DenemeBozukIcerik(denemeDeposu, onYedekAyarlari)
            }
        }
        return
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val bugunTarih = hatirlaYerelGun()

    val kayitlar by denemeDeposu.kayitlariIzle.collectAsState()
    val mevcut = remember(kayitlar, denemeId) {
        denemeId?.let { id -> kayitlar.firstOrNull { it.id == id } ?: denemeDeposu.getir(id) }
    }

    val aktifDersler by remember(dersRepository) {
        dersRepository.aktifDersleriIzle()
    }.collectAsState(initial = emptyList())
    val tumDersler by remember(dersRepository) {
        combine(
            dersRepository.dersleriIzle(false),
            dersRepository.dersleriIzle(true),
        ) { acik, arsiv -> acik + arsiv }
    }.collectAsState(initial = emptyList())
    val soruTakibi = tumSorulariIzle(tumDersler, soruRepository)
    val tumSorular = (soruTakibi as? DenemeSoruTakibi.Hazir)?.sorular

    val formDersleri = remember(aktifDersler, tumDersler, mevcut) {
        val aktifIdler = aktifDersler.map { it.id }.toSet()
        val tamHarita = tumDersler.associateBy { it.id }
        val ekKayitliDersler = mevcut?.satirlar.orEmpty()
            .filter { it.dersId !in aktifIdler }
            .map { satir ->
                val gercekDers = tamHarita[satir.dersId] ?: Ders(
                    id = satir.dersId,
                    ad = satir.dersAdi ?: "Ders",
                    sira = Int.MAX_VALUE,
                    soruSayisi = 0,
                    arsivli = true,
                )
                DenemeFormDersi(ders = gercekDers, gizli = true)
            }
        aktifDersler.map { DenemeFormDersi(ders = it, gizli = false) } + ekKayitliDersler
    }

    val kayitAnahtari = mevcut?.id ?: "yeni"
    var ad by rememberSaveable(kayitAnahtari) { mutableStateOf(mevcut?.ad.orEmpty()) }
    var kuralAdi by rememberSaveable(kayitAnahtari) {
        mutableStateOf((mevcut?.kural ?: NetKurali.DORTTE_BIR).name)
    }
    var seciliGun by rememberSaveable(kayitAnahtari) {
        mutableIntStateOf(mevcut?.gun ?: bugunTarih.toEpochDay().toInt())
    }
    var soruMetinleri by rememberSaveable(kayitAnahtari, stateSaver = MetinHaritasiSaver) {
        mutableStateOf(
            mevcut?.satirlar?.associate { it.dersId to it.soruSayisi.toString() }.orEmpty(),
        )
    }
    var dogruMetinleri by rememberSaveable(kayitAnahtari, stateSaver = MetinHaritasiSaver) {
        mutableStateOf(
            mevcut?.satirlar?.associate { it.dersId to it.dogru.toString() }.orEmpty(),
        )
    }
    var yanlisMetinleri by rememberSaveable(kayitAnahtari, stateSaver = MetinHaritasiSaver) {
        mutableStateOf(
            mevcut?.satirlar?.associate { it.dersId to it.yanlis.toString() }.orEmpty(),
        )
    }
    var silmeOnayiAcik by rememberSaveable { mutableStateOf(false) }
    var islemde by remember { mutableStateOf(false) }

    val kural = remember(kuralAdi) {
        NetKurali.entries.firstOrNull { it.name == kuralAdi } ?: NetKurali.DORTTE_BIR
    }

    val yuklenenSayilar = remember(tumSorular, mevcut?.ad, ad) {
        val hedefKaynak = mevcut?.ad ?: ad
        tumSorular?.let { sorular ->
            if (hedefKaynak.isBlank()) emptyMap() else sorular
                .filter { denemeKaynakEslesir(it.kaynak, hedefKaynak) }
                .groupingBy { it.dersId }
                .eachCount()
        }
    }

    val anlikSatirlar = remember(formDersleri, soruMetinleri, dogruMetinleri, yanlisMetinleri) {
        formDersleri.mapNotNull { oge ->
            val id = oge.ders.id
            val soru = soruMetinleri[id]?.toIntOrNull() ?: 0
            val dogru = dogruMetinleri[id]?.toIntOrNull() ?: 0
            val yanlis = yanlisMetinleri[id]?.toIntOrNull() ?: 0
            if (soru == 0 && dogru == 0 && yanlis == 0) {
                null
            } else {
                DenemeDersSatiri(
                    dersId = id,
                    soruSayisi = soru,
                    dogru = dogru,
                    yanlis = yanlis,
                    dersAdi = oge.ders.ad,
                )
            }
        }
    }
    val gecerliSatirlar = remember(anlikSatirlar) { anlikSatirlar.filter { it.gecerli() && it.soruSayisi > 0 } }
    val anlikToplamNet = remember(gecerliSatirlar, kural) {
        gecerliSatirlar.sumOf { kural.net(it.dogru, it.yanlis) }
    }
    val anlikDogru = remember(gecerliSatirlar) { gecerliSatirlar.sumOf { it.dogru.toLong() } }
    val anlikYanlis = remember(gecerliSatirlar) { gecerliSatirlar.sumOf { it.yanlis.toLong() } }
    val anlikBos = remember(gecerliSatirlar) { gecerliSatirlar.sumOf { it.bos.toLong() } }

    suspend fun dogrulaVeKaydet(): DenemeKaydi? {
        if (!withContext(Dispatchers.IO) { denemeDeposu.yazilabilirMi() }) {
            snackbar.showSnackbar(context.getString(R.string.deneme_storage_error_title))
            return null
        }
        val temizAd = ad.trim()
        val adHatasi = when (kayitlar.denemeAdiDogrula(temizAd, mevcut?.id)) {
            DenemeAdiDurumu.GECERLI -> null
            DenemeAdiDurumu.BOS -> context.getString(R.string.deneme_error_name)
            DenemeAdiDurumu.COK_UZUN -> context.getString(R.string.deneme_error_name_too_long, SORU_SECENEGI_ADI_UST_SINIR)
            DenemeAdiDurumu.AYNI_AD_VAR -> context.getString(R.string.deneme_error_duplicate_name)
            DenemeAdiDurumu.ESKI_AD_PAYLASILMIS -> context.getString(R.string.deneme_error_shared_name)
        }
        if (adHatasi != null) {
            snackbar.showSnackbar(adHatasi)
            return null
        }
        val hataliVar = anlikSatirlar.any { !it.gecerli() || it.soruSayisi <= 0 }
        if (hataliVar || gecerliSatirlar.isEmpty()) {
            snackbar.showSnackbar(context.getString(R.string.deneme_error_scores))
            return null
        }
        val simdi = System.currentTimeMillis()
        val kaydedilecek = DenemeKaydi(
            id = mevcut?.id ?: UUID.randomUUID().toString(),
            ad = temizAd,
            kural = kural,
            olusturma = mevcut?.olusturma ?: simdi,
            gun = seciliGun,
            satirlar = gecerliSatirlar,
        )
        return try {
            soruRepository.kaynakAdiDegistir(mevcut?.ad, temizAd)
            withContext(Dispatchers.IO) { denemeDeposu.kaydet(kaydedilecek) }
            kaydedilecek
        } catch (iptal: CancellationException) {
            throw iptal
        } catch (_: Exception) {
            snackbar.showSnackbar(context.getString(R.string.deneme_error_save))
            null
        }
    }

    if (silmeOnayiAcik && mevcut != null) {
        AlertDialog(
            onDismissRequest = { silmeOnayiAcik = false },
            title = { Text(stringResource(R.string.deneme_delete_confirm_title)) },
            text = { Text(stringResource(R.string.deneme_delete_confirm_body, mevcut.ad)) },
            confirmButton = {
                PremiumButton(
                    onClick = {
                        silmeOnayiAcik = false
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) { denemeDeposu.sil(mevcut.id) }
                                onGeri()
                            } catch (cancel: CancellationException) {
                                throw cancel
                            } catch (_: Exception) {
                                snackbar.showSnackbar(context.getString(R.string.deneme_error_save))
                            }
                        }
                    },
                ) {
                    Text(stringResource(R.string.deneme_delete_confirm_action))
                }
            },
            dismissButton = {
                PremiumTextButton(onClick = { silmeOnayiAcik = false }) {
                    Text(stringResource(R.string.topic_cancel))
                }
            },
        )
    }

    val focusManager = LocalFocusManager.current
    val klavyeAcik = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .consumeWindowInsets(insets),
        ) {
            DenemeUstCubuk(
                baslik = stringResource(
                    if (mevcut == null) R.string.deneme_form_new_title else R.string.deneme_form_title,
                ),
                onGeri = onGeri,
                sagAksiyon = if (klavyeAcik || mevcut != null) {
                    {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            if (klavyeAcik) {
                                PremiumTextButton(
                                    onClick = { focusManager.clearFocus() },
                                    modifier = Modifier.testTag("deneme-hide-keyboard-button"),
                                ) {
                                    Text(
                                        text = stringResource(R.string.common_hide_keyboard),
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                            if (mevcut != null) {
                                PremiumTextButton(
                                    onClick = { silmeOnayiAcik = true },
                                    modifier = Modifier.testTag("deneme-delete-button"),
                                ) {
                                    Text(
                                        text = stringResource(R.string.deneme_delete),
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                    }
                } else {
                    null
                },
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("deneme-detail-list"),
                contentPadding = PaddingValues(start = 20.dp, top = 6.dp, end = 20.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    com.keremtulu.questionnote.ui.bilesenler.DuzBaslik(
                    baslik = stringResource(R.string.deneme_net_line, netYazi(anlikToplamNet)),
                    alt = stringResource( R.string.deneme_live_counts, anlikDogru, anlikYanlis, anlikBos, ),
                )
                }

                item {
                    SadeKart(elevation = CamYukseklik.DUSUK) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            OutlinedTextField(
                                value = ad,
                                onValueChange = { ad = it },
                                label = { Text(stringResource(R.string.deneme_name)) },
                                placeholder = { Text(stringResource(R.string.deneme_name_placeholder)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        if (!focusManager.moveFocus(FocusDirection.Next)) {
                                            focusManager.clearFocus()
                                        }
                                    },
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("deneme-name-input"),
                            )

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                ),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.deneme_date_label),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            text = denemeUzunTarihYazi(seciliGun),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                    PremiumTextButton(
                                        onClick = {
                                            val mevcutTarih = LocalDate.ofEpochDay(seciliGun.toLong())
                                            DatePickerDialog(
                                                context,
                                                { _, yil, ay, gunAyin ->
                                                    seciliGun = LocalDate.of(yil, ay + 1, gunAyin)
                                                        .toEpochDay()
                                                        .toInt()
                                                },
                                                mevcutTarih.year,
                                                mevcutTarih.monthValue - 1,
                                                mevcutTarih.dayOfMonth,
                                            ).show()
                                        },
                                        modifier = Modifier.testTag("deneme-date-button"),
                                    ) {
                                        Text(stringResource(R.string.deneme_date_change))
                                    }
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = stringResource(R.string.deneme_rule_label),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    NetKurali.entries.forEach { secenek ->
                                        FilterChip(
                                            selected = kural == secenek,
                                            onClick = { kuralAdi = secenek.name },
                                            label = { Text(netKuraliEtiketi(secenek)) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (soruTakibi == DenemeSoruTakibi.Hata) item {
                    Text(stringResource(R.string.deneme_question_counts_error),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("deneme-question-counts-error"))
                }
                item {
                    Text(
                        text = stringResource(R.string.deneme_lessons_section),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                itemsIndexed(formDersleri, key = { _, oge -> oge.ders.id }) { siraIndex, oge ->
                    val ders = oge.ders
                    val soruAdet = soruMetinleri[ders.id]?.toIntOrNull() ?: 0
                    val dogruAdet = dogruMetinleri[ders.id]?.toIntOrNull() ?: 0
                    val yanlisAdet = yanlisMetinleri[ders.id]?.toIntOrNull() ?: 0
                    val satir = DenemeDersSatiri(
                        dersId = ders.id,
                        soruSayisi = soruAdet,
                        dogru = dogruAdet,
                        yanlis = yanlisAdet,
                        dersAdi = ders.ad,
                    )
                    val yuklenen = yuklenenSayilar?.let { it[ders.id] ?: 0 }
                    DenemeDersSkorKarti(
                        ders = ders,
                        gizli = oge.gizli,
                        sonDersMi = siraIndex == formDersleri.lastIndex,
                        soruMetni = soruMetinleri[ders.id].orEmpty(),
                        dogruMetni = dogruMetinleri[ders.id].orEmpty(),
                        yanlisMetni = yanlisMetinleri[ders.id].orEmpty(),
                        satir = satir,
                        kural = kural,
                        yuklenen = yuklenen,
                        fotografAktif = !oge.gizli && !islemde && satir.gecerli() && satir.soruSayisi > 0 && satir.yuklenecek > 0 && ad.isNotBlank(),
                        onSoruDegisti = { deger -> soruMetinleri = soruMetinleri + (ders.id to deger) },
                        onDogruDegisti = { deger -> dogruMetinleri = dogruMetinleri + (ders.id to deger) },
                        onYanlisDegisti = { deger -> yanlisMetinleri = yanlisMetinleri + (ders.id to deger) },
                        onFotografYukle = {
                            if (!islemde) {
                                scope.launch {
                                    islemde = true
                                    try {
                                        val kaydedilen = dogrulaVeKaydet()
                                        if (kaydedilen != null) {
                                            onKaydedildi(kaydedilen.id)
                                            onSoruEkle(ders, kaydedilen.ad, kaydedilen.id)
                                        }
                                    } finally {
                                        islemde = false
                                    }
                                }
                            }
                        },
                    )
                }

                item {
                    SadeAnaButon(
                        enabled = !islemde,
                        onClick = {
                            scope.launch {
                                islemde = true
                                try {
                                    val kaydedilen = dogrulaVeKaydet()
                                    if (kaydedilen != null) {
                                        onKaydedildi(kaydedilen.id)
                                        onGeri()
                                    }
                                } finally {
                                    islemde = false
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .testTag("deneme-save-button"),
                    ) {
                        Text(
                            text = stringResource(R.string.deneme_save),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DenemeDersSkorKarti(
    ders: Ders,
    gizli: Boolean,
    sonDersMi: Boolean,
    soruMetni: String,
    dogruMetni: String,
    yanlisMetni: String,
    satir: DenemeDersSatiri,
    kural: NetKurali,
    yuklenen: Int?,
    fotografAktif: Boolean,
    onSoruDegisti: (String) -> Unit,
    onDogruDegisti: (String) -> Unit,
    onYanlisDegisti: (String) -> Unit,
    onFotografYukle: () -> Unit,
) {
    val koyu = isSystemInDarkTheme()
    val colors = MaterialTheme.colorScheme
    val stil = dersKartStiliGetir(ders.ad)
    val vurgu = if (koyu) stil.koyuRenk else stil.acikRenk
    val sembol = stringResource(stil.isaret)
    val doluMu = soruMetni.isNotBlank() || dogruMetni.isNotBlank() || yanlisMetni.isNotBlank()
    val gecersiz = doluMu && (!satir.gecerli() || (satir.soruSayisi == 0 && (satir.dogru > 0 || satir.yanlis > 0)))

    PremiumCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("deneme-lesson-${ders.id}"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (gecersiz) {
                colors.error.copy(alpha = 0.7f)
            } else if (doluMu) {
                vurgu.copy(alpha = 0.38f)
            } else {
                colors.outlineVariant.copy(alpha = 0.50f)
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(vurgu.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = sembol,
                            style = MaterialTheme.typography.labelLarge,
                            color = vurgu,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PremiumSembolAilesi,
                        )
                    }
                    Column {
                        Text(
                            text = ders.ad,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (gizli) {
                            Text(
                                text = stringResource(R.string.deneme_hidden_lesson),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                }

                if (satir.soruSayisi > 0 && satir.gecerli()) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = vurgu.copy(alpha = 0.14f),
                    ) {
                        Text(
                            text = stringResource(
                                R.string.deneme_blank_net,
                                satir.bos,
                                netYazi(kural.net(satir.dogru, satir.yanlis)),
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = vurgu,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SayiAlani(
                    etiket = stringResource(R.string.deneme_total),
                    deger = soruMetni,
                    hatali = gecersiz,
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f).testTag("deneme-total-${ders.id}"),
                    onDegisti = onSoruDegisti,
                )
                SayiAlani(
                    etiket = stringResource(R.string.deneme_correct),
                    deger = dogruMetni,
                    hatali = gecersiz,
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f).testTag("deneme-correct-${ders.id}"),
                    onDegisti = onDogruDegisti,
                )
                SayiAlani(
                    etiket = stringResource(R.string.deneme_wrong),
                    deger = yanlisMetni,
                    hatali = gecersiz,
                    imeAction = if (sonDersMi) ImeAction.Done else ImeAction.Next,
                    modifier = Modifier.weight(1f).testTag("deneme-wrong-${ders.id}"),
                    onDegisti = onYanlisDegisti,
                )
            }

            if (gecersiz) {
                Text(
                    text = stringResource(R.string.deneme_row_invalid),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.error,
                )
            } else if (!gizli && satir.soruSayisi > 0 && satir.yuklenecek > 0) {
                val kalan = yuklenen?.let { (satir.yuklenecek - it).coerceAtLeast(0) }
                PremiumOutlinedButton(
                    enabled = fotografAktif,
                    onClick = onFotografYukle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deneme-photo-${ders.id}"),
                ) {
                    Text(
                        text = when {
                            kalan == null -> stringResource(R.string.deneme_add_photo_unknown)
                            kalan > 0 -> stringResource(R.string.deneme_add_photo, kalan)
                            else -> stringResource(R.string.deneme_add_more_photo, yuklenen ?: 0)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SayiAlani(
    etiket: String,
    deger: String,
    hatali: Boolean,
    imeAction: ImeAction = ImeAction.Next,
    modifier: Modifier = Modifier,
    onDegisti: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = deger,
        onValueChange = { girdi -> onDegisti(girdi.filter(Char::isDigit).take(3)) },
        label = { Text(etiket, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        singleLine = true,
        isError = hatali,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                if (!focusManager.moveFocus(FocusDirection.Next)) {
                    focusManager.clearFocus()
                }
            },
            onDone = { focusManager.clearFocus() },
        ),
        modifier = modifier,
    )
}

@Composable
private fun DenemeUstCubuk(
    baslik: String,
    onGeri: () -> Unit,
    sagAksiyon: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PremiumTextButton(
                onClick = onGeri,
                interactionTur = EtkilesimTuru.GERI,
                modifier = Modifier.testTag("deneme-back-button"),
            ) {
                Text("← ${stringResource(R.string.common_back)}")
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = baslik,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        sagAksiyon?.invoke()
    }
}

@Composable
private fun DenemeBosIlustrasyon() {
    val birincil = MaterialTheme.colorScheme.primary
    val vurgu = PremiumMarkaRenkler.Bakir
    Canvas(Modifier.size(72.dp)) {
        val sol = size.width * 0.16f
        val sag = size.width * 0.84f
        val alt = size.height * 0.78f
        val ust = size.height * 0.24f
        drawLine(
            color = birincil.copy(alpha = 0.30f),
            start = Offset(sol, alt),
            end = Offset(sag, alt),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
        val iz = Path().apply {
            moveTo(sol, alt - 8.dp.toPx())
            lineTo(size.width * 0.38f, size.height * 0.56f)
            lineTo(size.width * 0.60f, size.height * 0.62f)
            lineTo(sag, ust)
        }
        drawPath(
            path = iz,
            color = birincil,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        drawCircle(color = vurgu, radius = 5.dp.toPx(), center = Offset(sag, ust))
    }
}

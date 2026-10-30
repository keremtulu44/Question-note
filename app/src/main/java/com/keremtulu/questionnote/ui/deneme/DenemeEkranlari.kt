package com.keremtulu.questionnote.ui.deneme

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.prefs.DenemeDeposu
import com.keremtulu.questionnote.domain.model.DenemeDersSatiri
import com.keremtulu.questionnote.domain.model.DenemeDefterAnalizi
import com.keremtulu.questionnote.domain.model.DenemeKaydi
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.DersNetKarsilastirma
import com.keremtulu.questionnote.domain.model.NetKurali
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.denemeKaynakEslesir
import com.keremtulu.questionnote.domain.model.defterAnalizi
import com.keremtulu.questionnote.domain.model.enZayif
import com.keremtulu.questionnote.domain.model.karsilastirilabilir
import com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi
import com.keremtulu.questionnote.domain.model.SORU_SECENEGI_ADI_UST_SINIR
import com.keremtulu.questionnote.domain.gelisim.kaynakAnahtari
import com.keremtulu.questionnote.domain.model.dersBarlari
import com.keremtulu.questionnote.domain.model.grafikNoktalari
import com.keremtulu.questionnote.domain.model.oncekiAyniKural
import com.keremtulu.questionnote.domain.model.yerelTarih
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository
import com.keremtulu.questionnote.ui.animasyon.SureToken
import com.keremtulu.questionnote.ui.animasyon.animateFloatPremium
import com.keremtulu.questionnote.ui.animasyon.animateItemInsertion
import com.keremtulu.questionnote.ui.animasyon.tweenPremium
import com.keremtulu.questionnote.ui.bilesenler.hatirlaYerelGun
import com.keremtulu.questionnote.ui.bilesenler.KaydedilmemisDegisiklikDialog
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
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
private fun netKuraliEtiketi(kural: NetKurali): String = when (kural) {
    NetKurali.DORTTE_BIR -> stringResource(R.string.deneme_rule_4)
    NetKurali.UCTE_BIR -> stringResource(R.string.deneme_rule_3)
    NetKurali.YOK -> stringResource(R.string.deneme_rule_none)
}

private sealed interface DenemeSoruDurumu {
    data object Yukleniyor : DenemeSoruDurumu
    data class Hazir(val sorular: List<Soru>) : DenemeSoruDurumu
    data object Hata : DenemeSoruDurumu
}

@Composable
private fun tumSorulariIzle(
    dersler: List<Ders>?,
    soruRepository: SoruRepository?,
): DenemeSoruDurumu {
    val akis: Flow<DenemeSoruDurumu> = remember(dersler, soruRepository) {
        when {
            soruRepository == null -> flowOf(DenemeSoruDurumu.Hazir(emptyList()))
            dersler == null -> flowOf(DenemeSoruDurumu.Yukleniyor)
            dersler.isEmpty() -> flowOf(DenemeSoruDurumu.Hazir(emptyList()))
            else -> combine(dersler.map { ders -> soruRepository.sorulariIzle(ders.id) }) { gruplar ->
                gruplar.flatMap { it }
            }.map<List<Soru>, DenemeSoruDurumu> { DenemeSoruDurumu.Hazir(it) }
                .catch { emit(DenemeSoruDurumu.Hata) }
        }
    }
    val sonuc by produceState<DenemeSoruDurumu>(DenemeSoruDurumu.Yukleniyor, akis) {
        value = DenemeSoruDurumu.Yukleniyor
        akis.collect { value = it }
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
) {
    val kayitlar by denemeDeposu.kayitlariIzle.collectAsState()
    val tumDersler by remember(dersRepository) {
        combine(
            dersRepository.dersleriIzle(false),
            dersRepository.dersleriIzle(true),
        ) { acik, arsiv -> acik + arsiv }
    }.collectAsState(initial = null as List<Ders>?)
    val dersAdlari = remember(tumDersler) { tumDersler.orEmpty().associate { it.id to it.ad } }
    val soruDurumu = tumSorulariIzle(tumDersler, soruRepository)
    val tumSorular = (soruDurumu as? DenemeSoruDurumu.Hazir)?.sorular

    val mevcutKurallar = remember(kayitlar) { kayitlar.map { it.kural }.distinct() }
    var seciliKuralAdi by rememberSaveable { mutableStateOf<String?>(null) }
    val seciliKural = remember(seciliKuralAdi, mevcutKurallar) {
        seciliKuralAdi?.let { ad -> NetKurali.entries.firstOrNull { it.name == ad } }
            ?.takeIf { it in mevcutKurallar }
    }
    val grafikKayitlari = remember(kayitlar, seciliKural) {
        val filtreli = if (seciliKural == null) kayitlar else kayitlar.filter { it.kural == seciliKural }
        filtreli.grafikNoktalari()
    }
    val sonDeneme = grafikKayitlari.lastOrNull() ?: kayitlar.firstOrNull()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            if (kayitlar.isNotEmpty()) {
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

            if (kayitlar.isEmpty()) {
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
                        com.keremtulu.questionnote.ui.bilesenler.DuzBaslik(
                    baslik = stringResource(R.string.deneme_title),
                    alt = stringResource(R.string.deneme_list_subtitle),
                )
                    }

                    item {
                        SadeKart(elevation = CamYukseklik.DUSUK) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.deneme_chart_net),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        if (grafikKayitlari.size >= 2) {
                                            val enDusuk = grafikKayitlari.minOf { it.toplamNet() }
                                            val enYuksek = grafikKayitlari.maxOf { it.toplamNet() }
                                            Text(
                                                text = stringResource(
                                                    R.string.deneme_chart_range,
                                                    netYazi(enDusuk),
                                                    netYazi(enYuksek),
                                                ),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    grafikKayitlari.lastOrNull()?.let { enSon ->
                                        val onceki = kayitlar.oncekiAyniKural(enSon)
                                        NetOzetRozeti(
                                            net = enSon.toplamNet(),
                                            fark = onceki?.let { enSon.toplamNet() - it.toplamNet() },
                                        )
                                    }
                                }

                                if (mevcutKurallar.size > 1) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        FilterChip(
                                            selected = seciliKural == null,
                                            onClick = { seciliKuralAdi = null },
                                            label = { Text(stringResource(R.string.deneme_chart_all_rules)) },
                                        )
                                        mevcutKurallar.forEach { kural ->
                                            FilterChip(
                                                selected = seciliKural == kural,
                                                onClick = { seciliKuralAdi = kural.name },
                                                label = { Text(netKuraliEtiketi(kural)) },
                                            )
                                        }
                                    }
                                    if (seciliKural == null) {
                                        Text(
                                            text = stringResource(R.string.deneme_chart_mixed),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                if (grafikKayitlari.size > 1 &&
                                    grafikKayitlari.drop(1).any { !it.karsilastirilabilir(grafikKayitlari.first()) }) {
                                    Text(stringResource(R.string.deneme_chart_format_note),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                NetCizgisi(grafikKayitlari)

                                if (grafikKayitlari.size < 2) {
                                    Text(
                                        text = stringResource(R.string.deneme_chart_need_more),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    if (sonDeneme != null && sonDeneme.satirlar.isNotEmpty()) {
                        item {
                            SadeKart(elevation = CamYukseklik.DUSUK) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.deneme_chart_lessons, sonDeneme.ad),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = stringResource(R.string.deneme_chart_legend),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    DersNetBarlari(
                                        barlar = kayitlar.dersBarlari(sonDeneme),
                                        dersAdlari = dersAdlari,
                                    )
                                }
                            }
                        }
                    }

                    itemsIndexed(kayitlar, key = { _, deneme -> deneme.id }) { index, deneme ->
                        val giris by animateItemInsertion(index)
                        val onceki = remember(kayitlar, deneme.id) { kayitlar.oncekiAyniKural(deneme) }
                        val yuklenen = remember(tumSorular, kayitlar, deneme) {
                            tumSorular?.let { deneme.defterAnalizi(kayitlar, it)?.soruSayisi }
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
private fun DenemeListeKarti(
    deneme: DenemeKaydi,
    fark: Double?,
    yuklenenSoruSayisi: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val dogruToplam = deneme.satirlar.sumOf { it.dogru }
    val yanlisToplam = deneme.satirlar.sumOf { it.yanlis }
    val bosToplam = deneme.satirlar.sumOf { it.bos }
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

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.deneme_card_counts, dogruToplam, yanlisToplam, bosToplam),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant,
                )
                if (yuklenenSoruSayisi != null) {
                    val (etiket, arkaPlan, metinRengi) = when {
                        hedefYukleme == 0 && yuklenenSoruSayisi == 0 -> Triple(
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
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = vurgu.copy(alpha = 0.15f),
            ) {
                Text(
                    text = if (artis) {
                        stringResource(R.string.deneme_delta_positive, netYazi(fark))
                    } else {
                        netYazi(fark)
                    },
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

@Composable
private fun NetCizgisi(noktalar: List<DenemeKaydi>) {
    if (noktalar.isEmpty()) return
    val cizgiRengi = MaterialTheme.colorScheme.primary
    val izRengi = MaterialTheme.colorScheme.outlineVariant
    val yuzeyRengi = MaterialTheme.colorScheme.surface
    val ilerleme by animateFloatPremium(
        target = 1f,
        animationSpec = tweenPremium(SureToken.SLOW),
        label = "deneme-line-progress",
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
            .testTag("deneme-net-chart"),
    ) {
        val yatayIcBosluk = 14.dp.toPx()
        val dikeyIcBosluk = 14.dp.toPx()
        val icGenislik = (size.width - yatayIcBosluk * 2f).coerceAtLeast(1f)
        val icYukseklik = (size.height - dikeyIcBosluk * 2f).coerceAtLeast(1f)

        val degerler = noktalar.map { it.toplamNet().toFloat() }
        val altSinir = min(0f, degerler.minOrNull() ?: 0f)
        val hamUst = degerler.maxOrNull() ?: 1f
        val ustSinir = max(altSinir + 4f, hamUst * 1.08f)
        val aralik = (ustSinir - altSinir).coerceAtLeast(1f)

        fun yKonumu(deger: Float): Float =
            dikeyIcBosluk + icYukseklik * (1f - ((deger - altSinir) / aralik).coerceIn(0f, 1f))

        listOf(0f, 0.5f, 1f).forEach { oran ->
            val y = dikeyIcBosluk + icYukseklik * oran
            drawLine(
                color = izRengi.copy(alpha = 0.45f),
                start = Offset(yatayIcBosluk, y),
                end = Offset(size.width - yatayIcBosluk, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
            )
        }

        if (altSinir < 0f) {
            val sifirY = yKonumu(0f)
            drawLine(
                color = izRengi.copy(alpha = 0.85f),
                start = Offset(yatayIcBosluk, sifirY),
                end = Offset(size.width - yatayIcBosluk, sifirY),
                strokeWidth = 1.2.dp.toPx(),
            )
        }

        val konumlar = degerler.mapIndexed { indeks, deger ->
            val x = if (degerler.size == 1) {
                size.width / 2f
            } else {
                yatayIcBosluk + icGenislik * indeks / (degerler.lastIndex.toFloat())
            }
            Offset(x, yKonumu(deger * ilerleme.coerceIn(0f, 1f)))
        }

        if (konumlar.size >= 2) {
            val tabanY = dikeyIcBosluk + icYukseklik
            val alanYolu = Path().apply {
                moveTo(konumlar.first().x, tabanY)
                konumlar.forEach { nokta -> lineTo(nokta.x, nokta.y) }
                lineTo(konumlar.last().x, tabanY)
                close()
            }
            drawPath(
                path = alanYolu,
                brush = Brush.verticalGradient(
                    colors = listOf(cizgiRengi.copy(alpha = 0.24f), Color.Transparent),
                    startY = dikeyIcBosluk,
                    endY = tabanY,
                ),
            )

            val cizgiYolu = Path().apply {
                konumlar.forEachIndexed { indeks, nokta ->
                    if (indeks == 0) moveTo(nokta.x, nokta.y) else lineTo(nokta.x, nokta.y)
                }
            }
            drawPath(
                path = cizgiYolu,
                color = cizgiRengi,
                style = Stroke(
                    width = 2.8.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }

        konumlar.forEachIndexed { indeks, nokta ->
            val sonMu = indeks == konumlar.lastIndex
            val disCap = if (sonMu) 6.dp.toPx() else 4.5.dp.toPx()
            if (sonMu) {
                drawCircle(
                    color = cizgiRengi.copy(alpha = 0.20f),
                    radius = 10.dp.toPx(),
                    center = nokta,
                )
            }
            drawCircle(color = cizgiRengi, radius = disCap, center = nokta)
            drawCircle(color = yuzeyRengi, radius = (disCap - 2.dp.toPx()).coerceAtLeast(1.5f), center = nokta)
        }
    }
}

@Composable
private fun DersNetBarlari(
    barlar: List<DersNetKarsilastirma>,
    dersAdlari: Map<String, String>,
) {
    if (barlar.isEmpty()) return
    val koyu = isSystemInDarkTheme()
    val colors = MaterialTheme.colorScheme
    val tumDegerler = barlar.flatMap { listOfNotNull(it.sonNet, it.oncekiOrtalama) }
    val enKucuk = min(0.0, tumDegerler.minOrNull() ?: 0.0)
    val enBuyuk = max(1.0, tumDegerler.maxOrNull() ?: 1.0)
    val toplamAralik = (enBuyuk - enKucuk).coerceAtLeast(1.0)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        barlar.forEach { bar ->
            val ad = dersAdlari[bar.dersId] ?: bar.dersAdi ?: "—"
            val stil = dersKartStiliGetir(ad)
            val vurgu = if (koyu) stil.koyuRenk else stil.acikRenk
            val sembol = stringResource(stil.isaret)
            val fark = bar.oncekiOrtalama?.let { bar.sonNet - it }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(vurgu.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = sembol,
                                style = MaterialTheme.typography.labelSmall,
                                color = vurgu,
                                fontWeight = FontWeight.Bold,
                                fontFamily = PremiumSembolAilesi,
                            )
                        }
                        Text(
                            text = ad,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (fark != null && abs(fark) >= 0.05) {
                            Text(
                                text = if (fark > 0) "+${netYazi(fark)}" else netYazi(fark),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = netYazi(bar.sonNet),
                            style = MaterialTheme.typography.labelLarge,
                            color = vurgu,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp),
                ) {
                    val sifirX = ((-enKucuk / toplamAralik).toFloat() * size.width).coerceIn(0f, size.width)
                    val kose = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    drawRoundRect(
                        color = colors.surfaceVariant.copy(alpha = 0.65f),
                        size = size,
                        cornerRadius = kose,
                    )

                    fun cubukCiz(deger: Double, renk: Color, ustBosluk: Float, yukseklik: Float) {
                        val hedefX = (((deger - enKucuk) / toplamAralik).toFloat() * size.width)
                            .coerceIn(0f, size.width)
                        val sol = min(sifirX, hedefX)
                        val genislik = abs(hedefX - sifirX).coerceAtLeast(2.dp.toPx())
                        drawRoundRect(
                            color = renk,
                            topLeft = Offset(sol, ustBosluk),
                            size = Size(genislik.coerceAtMost(size.width - sol), yukseklik),
                            cornerRadius = CornerRadius(yukseklik / 2f, yukseklik / 2f),
                        )
                    }

                    bar.oncekiOrtalama?.let { ort ->
                        cubukCiz(ort, vurgu.copy(alpha = 0.30f), 0f, size.height)
                    }
                    cubukCiz(bar.sonNet, vurgu, 2.dp.toPx(), (size.height - 4.dp.toPx()).coerceAtLeast(4f))

                    if (enKucuk < 0.0) {
                        drawLine(
                            color = colors.outline.copy(alpha = 0.65f),
                            start = Offset(sifirX, 0f),
                            end = Offset(sifirX, size.height),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }
                }
            }
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
) {
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
    }.collectAsState(initial = null as List<Ders>?)
    val soruDurumu = tumSorulariIzle(tumDersler, soruRepository)
    val tumSorular = (soruDurumu as? DenemeSoruDurumu.Hazir)?.sorular
    val kaynakBelirsiz = mevcut != null && (mevcut.kaynakBelirsiz ||
        kayitlar.count { denemeKaynakEslesir(it.ad, mevcut.ad) } > 1)
    val defterAnalizi = remember(mevcut, kayitlar, tumSorular) {
        if (mevcut != null && tumSorular != null) mevcut.defterAnalizi(kayitlar, tumSorular) else null
    }
    val uygunAkis = remember(soruRepository, mevcut?.id, mevcut?.ad, kaynakBelirsiz, bugunTarih) {
        if (mevcut == null || kaynakBelirsiz) flowOf(emptyList())
        else soruRepository.filtreliCalismaSorulariniIzle(SoruCalismaFiltresi(kaynakAnahtari = kaynakAnahtari(mevcut.ad)))
    }
    val uygunSoruSayisi by produceState<Int?>(null, uygunAkis) {
        value = null
        uygunAkis.catch { value = null }.collect { value = it.size }
    }

    val formDersleri = remember(aktifDersler, tumDersler, mevcut) {
        val aktifIdler = aktifDersler.map { it.id }.toSet()
        val tamHarita = tumDersler.orEmpty().associateBy { it.id }
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
    val ilkGun = rememberSaveable(kayitAnahtari) { mevcut?.gun ?: bugunTarih.toEpochDay().toInt() }
    var seciliGun by rememberSaveable(kayitAnahtari) { mutableIntStateOf(ilkGun) }
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
    var cikisOnayi by rememberSaveable(kayitAnahtari) { mutableStateOf(false) }
    var islemde by remember { mutableStateOf(false) }

    val kural = remember(kuralAdi) {
        NetKurali.entries.firstOrNull { it.name == kuralAdi } ?: NetKurali.DORTTE_BIR
    }

    val yuklenenSayilar = remember(tumSorular, mevcut?.ad, ad, kaynakBelirsiz) {
        val hedefKaynak = mevcut?.ad ?: ad
        if (hedefKaynak.isBlank() || kaynakBelirsiz || tumSorular == null) {
            emptyMap()
        } else {
            tumSorular
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
    val anlikDogru = remember(gecerliSatirlar) { gecerliSatirlar.sumOf { it.dogru } }
    val anlikYanlis = remember(gecerliSatirlar) { gecerliSatirlar.sumOf { it.yanlis } }
    val anlikBos = remember(gecerliSatirlar) { gecerliSatirlar.sumOf { it.bos } }

    fun skorlar(satirlar: List<DenemeDersSatiri>) =
        satirlar.associate { it.dersId to Triple(it.soruSayisi, it.dogru, it.yanlis) }
    val kaydedilmemisDegisiklik = if (mevcut != null) {
        ad.trim() != mevcut.ad || kural != mevcut.kural || seciliGun != mevcut.gun ||
            skorlar(anlikSatirlar) != skorlar(mevcut.satirlar)
    } else {
        ad.isNotEmpty() || kural != NetKurali.DORTTE_BIR || seciliGun != ilkGun ||
            soruMetinleri.values.any { it.isNotEmpty() } || dogruMetinleri.values.any { it.isNotEmpty() } ||
            yanlisMetinleri.values.any { it.isNotEmpty() }
    }

    fun cikisIste() {
        if (islemde) return
        if (kaydedilmemisDegisiklik) cikisOnayi = true else onGeri()
    }
    BackHandler(enabled = true, onBack = ::cikisIste)

    suspend fun dogrulaVeKaydet(): DenemeKaydi? {
        val temizAd = ad.trim()
        if (temizAd.isEmpty() || temizAd.length > SORU_SECENEGI_ADI_UST_SINIR) {
            snackbar.showSnackbar(context.getString(R.string.deneme_error_name_length))
            return null
        }
        if (kayitlar.any { it.id != mevcut?.id && denemeKaynakEslesir(it.ad, temizAd) }) {
            snackbar.showSnackbar(context.getString(R.string.deneme_error_duplicate_name))
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
        var kaynakTasindi = false
        try {
            // Room and SharedPreferences cannot share a transaction. Finish both operations
            // even if navigation disposes the screen, and report any partial failure honestly.
            withContext(NonCancellable + Dispatchers.IO) {
                // Older duplicate names cannot safely be attributed to either exam.
                soruRepository.kaynakAdiDegistir(if (kaynakBelirsiz) null else mevcut?.ad, temizAd)
                kaynakTasindi = mevcut != null && !kaynakBelirsiz && !denemeKaynakEslesir(mevcut.ad, temizAd)
                denemeDeposu.kaydet(kaydedilecek)
            }
            return kaydedilecek
        } catch (iptal: kotlinx.coroutines.CancellationException) {
            throw iptal
        } catch (_: Exception) {
            snackbar.showSnackbar(context.getString(
                if (kaynakTasindi) R.string.deneme_save_partial_error else R.string.deneme_save_error,
            ))
            return null
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
                        try {
                            denemeDeposu.sil(mevcut.id)
                            onGeri()
                        } catch (_: Exception) {
                            scope.launch { snackbar.showSnackbar(context.getString(R.string.deneme_delete_error)) }
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

    if (cikisOnayi) {
        KaydedilmemisDegisiklikDialog(
            onCik = { cikisOnayi = false; onGeri() },
            onDevam = { cikisOnayi = false },
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
                onGeri = ::cikisIste,
                geriEtkin = !islemde,
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
                                    enabled = !islemde,
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

                if (mevcut != null) item(key = "deneme-analiz") {
                    DenemeAnalizKarti(
                        deneme = mevcut,
                        dersAdlari = tumDersler.orEmpty().associate { it.id to it.ad },
                        defterAnalizi = defterAnalizi,
                        soruDurumu = soruDurumu,
                        kaynakBelirsiz = kaynakBelirsiz,
                        uygunSoruSayisi = uygunSoruSayisi,
                        kaydedilmemisDegisiklik = kaydedilmemisDegisiklik || islemde,
                        onDenemeyiCalis = onDenemeyiCalis?.let { callback -> { callback(mevcut.ad) } },
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
                                enabled = !islemde,
                                onValueChange = { ad = it.take(SORU_SECENEGI_ADI_UST_SINIR) },
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
                                        enabled = !islemde,
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
                                            enabled = !islemde,
                                            onClick = { kuralAdi = secenek.name },
                                            label = { Text(netKuraliEtiketi(secenek)) },
                                        )
                                    }
                                }
                            }
                        }
                    }
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
                    val yuklenen = if (tumSorular != null && !kaynakBelirsiz) yuklenenSayilar[ders.id] ?: 0 else null
                    DenemeDersSkorKarti(
                        ders = ders,
                        gizli = oge.gizli,
                        sonDersMi = siraIndex == formDersleri.lastIndex,
                        etkin = !islemde,
                        soruMetni = soruMetinleri[ders.id].orEmpty(),
                        dogruMetni = dogruMetinleri[ders.id].orEmpty(),
                        yanlisMetni = yanlisMetinleri[ders.id].orEmpty(),
                        satir = satir,
                        kural = kural,
                        yuklenen = yuklenen,
                        fotografAktif = !oge.gizli && !islemde && !kaynakBelirsiz && satir.gecerli() && satir.soruSayisi > 0 && ad.isNotBlank(),
                        onSoruDegisti = { deger -> soruMetinleri = soruMetinleri + (ders.id to deger) },
                        onDogruDegisti = { deger -> dogruMetinleri = dogruMetinleri + (ders.id to deger) },
                        onYanlisDegisti = { deger -> yanlisMetinleri = yanlisMetinleri + (ders.id to deger) },
                        onFotografYukle = {
                            if (!islemde) {
                                islemde = true
                                scope.launch {
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
                            if (islemde) return@SadeAnaButon
                            islemde = true
                            scope.launch {
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
private fun DenemeAnalizKarti(
    deneme: DenemeKaydi,
    dersAdlari: Map<String, String>,
    defterAnalizi: DenemeDefterAnalizi?,
    soruDurumu: DenemeSoruDurumu,
    kaynakBelirsiz: Boolean,
    uygunSoruSayisi: Int?,
    kaydedilmemisDegisiklik: Boolean,
    onDenemeyiCalis: (() -> Unit)?,
) {
    val colors = MaterialTheme.colorScheme
    SadeKart(modifier = Modifier.testTag("deneme-analysis")) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.deneme_analysis_title), style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.deneme_analysis_saved), style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant)
            deneme.enZayif()?.let { satir ->
                val ad = dersAdlari[satir.dersId] ?: satir.dersAdi ?: stringResource(R.string.deneme_unknown_lesson)
                Text(stringResource(R.string.deneme_analysis_loss, ad, satir.yuklenecek, satir.soruSayisi),
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            if (kaynakBelirsiz) {
                Text(stringResource(R.string.deneme_analysis_ambiguous), color = colors.error,
                    style = MaterialTheme.typography.bodySmall)
            } else when (soruDurumu) {
                DenemeSoruDurumu.Yukleniyor -> Text(stringResource(R.string.deneme_analysis_loading),
                    style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                DenemeSoruDurumu.Hata -> Text(stringResource(R.string.deneme_analysis_load_error),
                    style = MaterialTheme.typography.bodySmall, color = colors.error)
                is DenemeSoruDurumu.Hazir -> if (defterAnalizi != null) {
                    if (defterAnalizi.soruSayisi == 0) {
                        Text(stringResource(R.string.deneme_analysis_empty), style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant)
                    } else {
                        Text(stringResource(R.string.deneme_analysis_notebook_count, defterAnalizi.soruSayisi),
                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        val dersler = defterAnalizi.dersSayilari.entries
                            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                            .take(4).joinToString(" · ") { (id, adet) ->
                                "${dersAdlari[id] ?: deneme.satir(id)?.dersAdi ?: "Ders"}: $adet"
                            }
                        if (dersler.isNotEmpty()) Text(dersler, style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant)
                        if (defterAnalizi.nedenler.isNotEmpty()) {
                            Text(stringResource(R.string.deneme_analysis_reasons),
                                style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            defterAnalizi.nedenler.take(5).forEach { neden ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.Top) {
                                    Text(neden.neden, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                    Text(stringResource(R.string.deneme_analysis_reason_count, neden.adet),
                                        style = MaterialTheme.typography.labelLarge, color = colors.primary,
                                        fontWeight = FontWeight.SemiBold)
                                }
                            }
                            if (defterAnalizi.nedenler.size > 5) Text(
                                stringResource(R.string.deneme_analysis_other_reasons, defterAnalizi.nedenler.size - 5),
                                style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        }
                        if (defterAnalizi.nedeniBelirtilmeyen > 0) Text(
                            stringResource(R.string.deneme_analysis_missing_reason, defterAnalizi.nedeniBelirtilmeyen),
                            style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        Text(stringResource(R.string.deneme_analysis_scope), style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant)
                        if (onDenemeyiCalis != null && uygunSoruSayisi != null && uygunSoruSayisi > 0) {
                            PremiumOutlinedButton(onClick = onDenemeyiCalis, enabled = !kaydedilmemisDegisiklik,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("deneme-study-action")) {
                                Text(stringResource(R.string.deneme_study_action, uygunSoruSayisi))
                            }
                            if (kaydedilmemisDegisiklik) Text(stringResource(R.string.deneme_analysis_save_first),
                                style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        } else if (onDenemeyiCalis != null && uygunSoruSayisi == 0) {
                            Text(stringResource(R.string.deneme_analysis_no_eligible), style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant)
                        }
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
    etkin: Boolean,
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
                    enabled = etkin,
                    deger = soruMetni,
                    hatali = gecersiz,
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f).testTag("deneme-total-${ders.id}"),
                    onDegisti = onSoruDegisti,
                )
                SayiAlani(
                    etiket = stringResource(R.string.deneme_correct),
                    enabled = etkin,
                    deger = dogruMetni,
                    hatali = gecersiz,
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f).testTag("deneme-correct-${ders.id}"),
                    onDegisti = onDogruDegisti,
                )
                SayiAlani(
                    etiket = stringResource(R.string.deneme_wrong),
                    enabled = etkin,
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
            } else if (!gizli && satir.soruSayisi > 0) {
                PremiumOutlinedButton(
                    enabled = fotografAktif,
                    onClick = onFotografYukle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deneme-photo-${ders.id}"),
                ) {
                    Text(
                        text = if (yuklenen != null && yuklenen > 0) {
                            stringResource(R.string.deneme_add_more_photo, yuklenen)
                        } else {
                            stringResource(R.string.deneme_add_question)
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
    enabled: Boolean,
    imeAction: ImeAction = ImeAction.Next,
    modifier: Modifier = Modifier,
    onDegisti: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = deger,
        enabled = enabled,
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
    geriEtkin: Boolean = true,
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
                enabled = geriEtkin,
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

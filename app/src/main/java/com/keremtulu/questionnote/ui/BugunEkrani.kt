package com.keremtulu.questionnote.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.keremtulu.questionnote.domain.repeat.CalismaSorulariniSirala
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.ui.animasyon.SureToken
import com.keremtulu.questionnote.ui.animasyon.animateFloatPremium
import com.keremtulu.questionnote.ui.animasyon.tweenPremium
import com.keremtulu.questionnote.ui.bilesenler.premium.BosDurumSoruYok
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.bilesenler.premium.KutlamaParcaciklari
import com.keremtulu.questionnote.ui.premium.EtkilesimTuru
import com.keremtulu.questionnote.ui.premium.PremiumCard
import com.keremtulu.questionnote.ui.premium.PremiumExtendedFloatingActionButton
import com.keremtulu.questionnote.ui.premium.PremiumGradyanButon
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumSembolAilesi
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import com.keremtulu.questionnote.data.ilerleme.IlerlemeHizmeti
import com.keremtulu.questionnote.data.prefs.GunlukHedefDeposu
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
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

/**
 * Bugün ekranı bilinçli olarak tek bir akış halinde: başlık kartı + gün kartı.
 * Gün kartı; odak özetini, günlük hedef halkasını, sıraya girmiş soruları ve başlatma
 * düğmesini taşır. Liste içeriği ve tıklama sözleşmesi eski çok kartlı düzenle birebir aynıdır.
 */
@Composable
fun BugunEkrani(
    soruRepository: SoruRepository,
    dersRepository: DersRepository,
    ilerlemeHizmeti: IlerlemeHizmeti,
    gunlukHedefDeposu: GunlukHedefDeposu,
    onSoruEkle: () -> Unit,
    onOturumBaslat: (List<Soru>, Map<String, Ders>) -> Unit,
    onDevam: (() -> Unit)? = null,
    onDeneme: () -> Unit = {},
    onSorulacaklar: () -> Unit = {},
    denemeler: List<com.keremtulu.questionnote.domain.model.DenemeKaydi> = emptyList(),
) {
    val today = com.keremtulu.questionnote.ui.bilesenler.hatirlaYerelGun()
    val dateText = remember(today) {
        today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.forLanguageTag("tr-TR")))
            .replaceFirstChar { it.titlecase(Locale.forLanguageTag("tr-TR")) }
    }
    var yenileme by remember { mutableIntStateOf(0) }
    val questionsFlow = remember(soruRepository, today.toEpochDay(), yenileme) {
        soruRepository.bugunTekrarlanacaklariIzle(today.toEpochDay().toInt())
    }
    val lessonsFlow = remember(dersRepository) { dersRepository.aktifDersleriIzle() }
    val sorulacaklarFlow = remember(soruRepository, lessonsFlow, yenileme) {
        combine(soruRepository.ogretmeneSorulacaklariIzle(), lessonsFlow) { questions, lessons ->
            val byId = lessons.associateBy { it.id }
            questions.groupBy { it.dersId }
                .mapNotNull { (dersId, liste) -> byId[dersId]?.let { it to liste.size } }
                .sortedWith(compareBy<Pair<Ders, Int>> { it.first.sira }.thenBy { it.first.ad })
        }
    }
    val sorulacakGruplar by produceState<List<Pair<Ders, Int>>>(emptyList(), sorulacaklarFlow) {
        sorulacaklarFlow.catch { emit(emptyList()) }.collect { value = it }
    }
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
    val bugunGun = today.toEpochDay().toInt()
    val bugunAkisi = remember(ilerlemeHizmeti, bugunGun) {
        ilerlemeHizmeti.bugunSayisiIzle(bugunGun)
    }
    val seriAkisi = remember(ilerlemeHizmeti, bugunGun) { ilerlemeHizmeti.seriIzle(bugunGun) }
    val gunluk by produceState(0, bugunAkisi) {
        bugunAkisi.catch { emit(0) }.collect { value = it }
    }
    val seri by produceState(0, seriAkisi) {
        seriAkisi.catch { emit(0) }.collect { value = it }
    }
    val hedef = remember(gunlukHedefDeposu) { gunlukHedefDeposu.hedef() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            PremiumExtendedFloatingActionButton(
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
            modifier = Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets),
            contentPadding = PaddingValues(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                GradyanKart(modifier = Modifier.fillMaxWidth(), borderAnimation = false) {
                    Column {
                        Text(
                            text = stringResource(R.string.today_kicker),
                            color = PremiumMarkaRenkler.BakirIsik,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                        Text(
                            text = stringResource(R.string.today_title),
                            modifier = Modifier.padding(top = 4.dp),
                            color = PremiumMarkaRenkler.Kagit,
                            style = MaterialTheme.typography.displaySmall,
                        )
                        Text(
                            text = dateText,
                            modifier = Modifier.padding(top = 2.dp),
                            color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.74f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            if (onDevam != null) item {
                PremiumOutlinedButton(onClick = onDevam, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.focus_continue_session))
                }
            }
            item {
                BugunGunKarti(
                    today = today,
                    dateText = dateText,
                    hedef = hedef,
                    yapilan = gunluk,
                    seri = seri,
                    state = state,
                    onOturumBaslat = onOturumBaslat,
                )
            }
            if (sorulacakGruplar.isNotEmpty()) {
                item(key = "bugun_sorulacaklar") {
                    BugunSorulacaklarKarti(
                        gruplar = sorulacakGruplar,
                        onClick = onSorulacaklar,
                    )
                }
            }
            item {
                BugunDenemeKarti(
                    denemeler = denemeler,
                    onClick = onDeneme,
                )
            }
            when (val current = state) {
                BugunDurumu.Yukleniyor -> Unit
                BugunDurumu.Hata -> item {
                    DurumKarti(
                        metin = stringResource(R.string.today_load_error),
                        onRetry = { yenileme++ },
                    )
                }
                is BugunDurumu.Hazir -> if (current.sorular.isEmpty()) {
                    item { BosDurumSoruYok(onSoruEkle) }
                }
            }
        }
    }
}

@Composable
private fun BugunGunKarti(
    today: LocalDate,
    dateText: String,
    hedef: Int,
    yapilan: Int,
    seri: Int,
    state: BugunDurumu,
    onOturumBaslat: (List<Soru>, Map<String, Ders>) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val guvenliHedef = hedef.coerceAtLeast(1)
    val oran = (yapilan.toFloat() / guvenliHedef).coerceIn(0f, 1f)
    val tamamlandi = yapilan >= guvenliHedef
    // Celebrate only a fresh crossing of the goal, never a re-entry while it is already complete.
    var oncekiTamam by remember { mutableStateOf(tamamlandi) }
    var kutlama by remember { mutableStateOf(false) }
    LaunchedEffect(tamamlandi) {
        if (tamamlandi && !oncekiTamam) {
            kutlama = true
            delay(2_200)
            kutlama = false
        }
        oncekiTamam = tamamlandi
    }
    // One calm sweep on entry; later goal changes keep animating from wherever the ring is.
    var halkaGoster by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { halkaGoster = true }
    val halka = animateFloatPremium(
        target = if (halkaGoster) oran else 0f,
        animationSpec = tweenPremium(SureToken.VERY_SLOW),
        label = "bugun-hedef-halkasi",
    )
    val ready = state as? BugunDurumu.Hazir
    val count = ready?.sorular?.size

    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box {
            Column(
                modifier = Modifier.fillMaxWidth().padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.today_focus_label),
                            color = colors.onPrimaryContainer.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.labelMedium,
                            letterSpacing = 1.1.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = count?.let {
                                pluralStringResource(R.plurals.today_question_count, it, it)
                            } ?: stringResource(R.string.today_loading_count),
                            modifier = Modifier.padding(top = 4.dp),
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
                            color = colors.onPrimaryContainer.copy(alpha = 0.78f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = stringResource(R.string.today_goal_title),
                            modifier = Modifier.padding(top = 6.dp),
                            color = colors.onPrimaryContainer.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = if (tamamlandi) {
                                stringResource(R.string.today_goal_done)
                            } else {
                                stringResource(R.string.today_goal_remaining, guvenliHedef - yapilan)
                            },
                            color = colors.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = if (seri > 0) {
                                stringResource(R.string.today_streak, seri)
                            } else {
                                stringResource(R.string.today_streak_start)
                            },
                            color = colors.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Box(Modifier.size(76.dp), contentAlignment = Alignment.Center) {
                        Canvas(Modifier.fillMaxSize()) {
                            val kalinlik = 8.dp.toPx()
                            val inset = kalinlik / 2f
                            val boyut = Size(size.width - kalinlik, size.height - kalinlik)
                            drawArc(
                                color = colors.onPrimaryContainer.copy(alpha = 0.16f),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = Offset(inset, inset),
                                size = boyut,
                                style = Stroke(width = kalinlik),
                            )
                            drawArc(
                                color = if (tamamlandi) colors.primary else colors.secondary,
                                startAngle = -90f,
                                sweepAngle = 360f * halka.value,
                                useCenter = false,
                                topLeft = Offset(inset, inset),
                                size = boyut,
                                style = Stroke(width = kalinlik, cap = StrokeCap.Round),
                            )
                        }
                        Text(
                            text = "$yapilan/$guvenliHedef",
                            color = colors.onPrimaryContainer,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                if (ready != null && count != null && count > 0) {
                    HorizontalDivider(color = colors.onPrimaryContainer.copy(alpha = 0.14f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.today_section_title),
                            color = colors.onPrimaryContainer,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = count.toString(),
                            color = colors.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ready.sorular.forEach { row ->
                            GunSoruSatiri(
                                row = row,
                                today = today,
                                onClick = {
                                    onOturumBaslat(listOf(row.soru), mapOf(row.ders.id to row.ders))
                                },
                            )
                        }
                    }
                    val dersler = ready.sorular.associate { it.ders.id to it.ders }
                    val sorular = CalismaSorulariniSirala.akilliOncelik(
                        ready.sorular.map { it.soru },
                        today.toEpochDay().toInt(),
                    ).take((hedef - yapilan).coerceIn(5, 30))
                    PremiumGradyanButon(
                        onClick = { onOturumBaslat(sorular, dersler) },
                        modifier = Modifier.fillMaxWidth(),
                        interactionTur = EtkilesimTuru.ODAK_BASLADI,
                    ) {
                        Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.focus_start_today), fontWeight = FontWeight.Bold)
                            Text(
                                stringResource(R.string.focus_start_count, sorular.size),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
            KutlamaParcaciklari(
                visible = kutlama,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@Composable
private fun GunSoruSatiri(
    row: BugunSoruSatiri,
    today: LocalDate,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val dueDay = row.soru.sonrakiTekrarGun ?: today.toEpochDay().toInt()
    val lateDays = (today.toEpochDay().toInt() - dueDay).coerceAtLeast(0)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = row.ders.ad,
                color = colors.primary,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = row.soru.soruNotu?.trim()?.takeIf { it.isNotEmpty() }
                    ?: row.soru.kaynak?.trim()?.takeIf { it.isNotEmpty() }
                    ?: stringResource(R.string.question_photo_only),
                color = colors.onPrimaryContainer,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.today_photo_count, row.soru.gorseller.count { it.tur == GorselTuru.SORU }),
                    color = colors.onPrimaryContainer.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text("·", color = colors.onPrimaryContainer.copy(alpha = 0.72f))
                Text(
                    text = stringResource(R.string.today_tap_to_review),
                    modifier = Modifier.weight(1f, fill = false),
                    color = colors.onPrimaryContainer.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
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
}

@Composable
private fun DurumKarti(
    metin: String,
    onRetry: () -> Unit,
) {
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(metin, color = MaterialTheme.colorScheme.onErrorContainer)
            PremiumTextButton(onClick = onRetry) { Text(stringResource(R.string.topic_retry)) }
        }
    }
}

@Composable
private fun BugunDenemeKarti(
    denemeler: List<com.keremtulu.questionnote.domain.model.DenemeKaydi>,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val sonDeneme = denemeler.firstOrNull()
    val altMetin = if (sonDeneme == null) {
        stringResource(R.string.today_deneme_subtitle_empty)
    } else {
        stringResource(
            R.string.today_deneme_subtitle_summary,
            denemeler.size,
            sonDeneme.ad,
            com.keremtulu.questionnote.ui.deneme.netYazi(sonDeneme.toplamNet()),
        )
    }

    PremiumCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today-deneme-card"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.55f)),
        interactionTur = EtkilesimTuru.KART_ACILIS,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val bakirVurgu = if (isSystemInDarkTheme()) PremiumMarkaRenkler.BakirIsik else PremiumMarkaRenkler.Bakir
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bakirVurgu.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "◈",
                    style = MaterialTheme.typography.titleMedium,
                    color = bakirVurgu,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PremiumSembolAilesi,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.deneme_open),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = altMetin,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = "→",
                style = MaterialTheme.typography.titleMedium,
                color = colors.primary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun BugunSorulacaklarKarti(
    gruplar: List<Pair<Ders, Int>>,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val bakirVurgu = if (isSystemInDarkTheme()) PremiumMarkaRenkler.BakirIsik else PremiumMarkaRenkler.Bakir
    val toplamSoru = gruplar.sumOf { it.second }
    val dersOzeti = remember(gruplar) {
        gruplar.joinToString(" · ") { (ders, adet) -> "${ders.ad} ($adet)" }
    }

    PremiumCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag("bugun_sorulacaklar_karti"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.secondaryContainer.copy(alpha = 0.65f)),
        border = BorderStroke(1.2.dp, bakirVurgu.copy(alpha = 0.45f)),
        interactionTur = EtkilesimTuru.KART_ACILIS,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bakirVurgu.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "?",
                        style = MaterialTheme.typography.titleLarge,
                        color = bakirVurgu,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.today_ask_teacher_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.today_ask_teacher_subtitle, gruplar.size, toplamSoru),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = dersOzeti,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Surface(
                    shape = CircleShape,
                    color = colors.primary,
                ) {
                    Text(
                        text = stringResource(R.string.today_ask_teacher_action) + " →",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onPrimary,
                    )
                }
            }
        }
    }
}



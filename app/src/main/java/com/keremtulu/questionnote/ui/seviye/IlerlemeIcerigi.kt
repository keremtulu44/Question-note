package com.keremtulu.questionnote.ui.seviye

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.seviye.DersIlerlemesi
import com.keremtulu.questionnote.data.seviye.SeviyeOzeti
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi
import com.keremtulu.questionnote.domain.seviye.Rutbe
import com.keremtulu.questionnote.domain.seviye.SeviyeHesabi
import com.keremtulu.questionnote.ui.karne.GizliRozetGorseli
import com.keremtulu.questionnote.ui.karne.RozetGorseli
import com.keremtulu.questionnote.ui.tema.*
import java.text.NumberFormat
import java.util.Locale
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import com.keremtulu.questionnote.ui.premium.PremiumButton

// Established petrol/copper hero palette; deliberately independent of the page's light/dark surfaces.
private val Murekkep = Color(0xFF153544)
private val Gece = Color(0xFF0C202C)
private val Kagit = Color(0xFFFFF7E8)
private val BakirIsik = Color(0xFFEFC18A)

/** Navigation is presentation state only. Opening a detail never creates a measurement or changes a plan. */
private enum class IlerlemeBolumu(val baslik: Int) {
    OZET(R.string.progress_headline), AYRINTILAR(R.string.progress_details_title),
    EMEK(R.string.progress_effort_details), RITIM(R.string.progress_rhythm_short),
    PLAN(R.string.progress_plan_short), DONEM(R.string.progress_comparison_short),
    GRUPLAR(R.string.progress_groups_short), DERSLER(R.string.level_lessons_title),
    SURE(R.string.profile_title), ROZETLER(R.string.progress_collection_title), RUTBELER(R.string.rank_gallery_title);

    fun ust(): IlerlemeBolumu = when (this) {
        OZET, AYRINTILAR, ROZETLER -> OZET
        RUTBELER -> ROZETLER
        else -> AYRINTILAR
    }
}

@Composable
internal fun IlerlemeIcerigi(
    ozet: SeviyeOzeti,
    yetersizVeri: Boolean,
    insets: PaddingValues,
    kazanilmaZamani: (Rozet) -> Long?,
    onBugun: () -> Unit,
    onSoruEkle: () -> Unit,
    onCalis: (SoruCalismaFiltresi, Int) -> Unit,
) {
    var sayfa by rememberSaveable { mutableStateOf(IlerlemeBolumu.OZET) }
    val ozetListesi = rememberLazyListState()
    BackHandler(sayfa != IlerlemeBolumu.OZET) { sayfa = sayfa.ust() }
    val padding = PaddingValues(20.dp, insets.calculateTopPadding() + 16.dp, 20.dp, insets.calculateBottomPadding() + 24.dp)

    if (sayfa == IlerlemeBolumu.OZET) {
        // Four visual blocks, one primary action. Technical panels are not composed off-screen underneath this summary.
        LazyColumn(state = ozetListesi, modifier = Modifier.fillMaxSize().testTag("progress-list"),
            contentPadding = padding, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item(key = "baslik") { IlerlemeBasligi() }
            item(key = "rutbe") { YeniRutbeKarti(ozet) }
            item(key = "bugun") {
                IlerlemePaneli(Modifier.testTag(if (yetersizVeri) "progress-empty" else "progress-today")) {
                    if (ozet.soruSayisi == 0) {
                        BolumBasligi(stringResource(R.string.progress_first_step))
                        Text(stringResource(R.string.progress_empty_archive), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        PremiumButton(onClick = onSoruEkle, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                            Text(stringResource(R.string.progress_add_question))
                        }
                    } else {
                        // This is the already-calculated global daily goal, not a new score or a profile-specific count.
                        val bugun = ozet.analiz?.let { a -> a.ritim.firstOrNull { it.gun == a.gun }?.tekrar }
                            ?: ozet.rozetGirdisi.sonBesGun.maxByOrNull { it.gun }?.toplam
                        if (bugun != null) GunlukHedef(bugun, ozet.hedef)
                        if (ozet.analiz != null) SonrakiAdimIcerigi(ozet.analiz, onCalis)
                        else {
                            if (yetersizVeri) Text(stringResource(R.string.progress_no_reviews), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            PremiumButton(onClick = onBugun, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                                Text(stringResource(R.string.progress_go_today))
                            }
                        }
                    }
                }
            }
            item(key = "gecisler") {
                IlerlemePaneli(icerikBosluk = 0) {
                    IlerlemeGecisSatiri(stringResource(R.string.progress_collection_title),
                        stringResource(R.string.progress_badge_count, ozet.sahipRozetler.size, Rozet.entries.size),
                        "progress-collection", onClick = { sayfa = IlerlemeBolumu.ROZETLER },
                        simge = {
                            val rozet = Rozet.entries.firstOrNull { it in ozet.sahipRozetler }
                            if (rozet == null) GizliRozetGorseli(Modifier.size(40.dp))
                            else RozetGorseli(rozet, true, Modifier.size(40.dp))
                        })
                    if (ozet.soruSayisi > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        IlerlemeGecisSatiri(stringResource(R.string.progress_details_title),
                            stringResource(R.string.progress_details_hint), "progress-details",
                            onClick = { sayfa = IlerlemeBolumu.AYRINTILAR })
                    }
                }
            }
        }
    } else key(sayfa) {
        // Each destination has its own short page; the summary's scroll position stays untouched.
        LazyColumn(modifier = Modifier.fillMaxSize().testTag("progress-detail-list"), contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { AyrintiBasligi(stringResource(sayfa.baslik)) { sayfa = sayfa.ust() } }
            when (sayfa) {
                IlerlemeBolumu.AYRINTILAR -> item {
                    IlerlemePaneli(icerikBosluk = 0) {
                        AyrintiGecisi(IlerlemeBolumu.EMEK, "progress-open-effort") { sayfa = it }
                        if (ozet.analiz != null) {
                            if (ozet.tekrarSayisi > 0) {
                                AyrintiGecisi(IlerlemeBolumu.RITIM, "progress-open-rhythm") { sayfa = it }
                                AyrintiGecisi(IlerlemeBolumu.GRUPLAR, "progress-open-groups") { sayfa = it }
                                AyrintiGecisi(IlerlemeBolumu.DONEM, "progress-open-comparison") { sayfa = it }
                            }
                            AyrintiGecisi(IlerlemeBolumu.PLAN, "progress-open-plan") { sayfa = it }
                        }
                        if (ozet.dersler.isNotEmpty()) AyrintiGecisi(IlerlemeBolumu.DERSLER, "progress-open-lessons") { sayfa = it }
                        if (ozet.sureKapsami != null && ozet.tekrarSayisi > 0)
                            AyrintiGecisi(IlerlemeBolumu.SURE, "progress-open-time") { sayfa = it }
                    }
                }
                IlerlemeBolumu.EMEK -> {
                    item { Text(stringResource(R.string.progress_scope), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    item { Istatistikler(ozet) }
                    item { XpDokumu(ozet) }
                }
                IlerlemeBolumu.ROZETLER -> {
                    item { IlerlemeGecisSatiri(stringResource(R.string.rank_gallery_title), null, "progress-open-ranks",
                        onClick = { sayfa = IlerlemeBolumu.RUTBELER }) }
                    item { RozetKoleksiyonu(ozet.sahipRozetler, ozet.rozetGirdisi, kazanilmaZamani) }
                }
                IlerlemeBolumu.RUTBELER -> item { RutbeYolculugu(ozet.durum, baslikGoster = false) }
                IlerlemeBolumu.DERSLER -> item { DersPekismesi(ozet.dersler) }
                IlerlemeBolumu.SURE -> item { ozet.sureKapsami?.let { SureProfiliKarti(it, baslikGoster = false) } }
                IlerlemeBolumu.RITIM, IlerlemeBolumu.PLAN, IlerlemeBolumu.DONEM, IlerlemeBolumu.GRUPLAR -> item {
                    ozet.analiz?.let { a -> GelisimAnaliziKarti(a, when (sayfa) {
                        IlerlemeBolumu.RITIM -> AnalizBolumu.RITIM
                        IlerlemeBolumu.PLAN -> AnalizBolumu.PLAN
                        IlerlemeBolumu.DONEM -> AnalizBolumu.DONEM
                        else -> AnalizBolumu.GRUPLAR
                    }, onCalis) }
                }
                IlerlemeBolumu.OZET -> Unit
            }
        }
    }
}

@Composable
private fun AyrintiGecisi(bolum: IlerlemeBolumu, tag: String, onSec: (IlerlemeBolumu) -> Unit) =
    IlerlemeGecisSatiri(stringResource(bolum.baslik), null, tag, onClick = { onSec(bolum) })

@Composable
internal fun IlerlemeGecisSatiri(baslik: String, alt: String?, tag: String, onClick: () -> Unit,
    simge: (@Composable () -> Unit)? = null) {
    Surface(onClick = onClick, shape = RoundedCornerShape(16.dp), color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag(tag)) {
        Row(Modifier.padding(vertical = 12.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            simge?.invoke()
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(baslik, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (alt != null) Text(alt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val renk = MaterialTheme.colorScheme.onSurfaceVariant
            Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
                drawLine(renk, Offset(size.width * .35f, size.height * .2f), Offset(size.width * .65f, size.height * .5f), 2.dp.toPx(), StrokeCap.Round)
                drawLine(renk, Offset(size.width * .65f, size.height * .5f), Offset(size.width * .35f, size.height * .8f), 2.dp.toPx(), StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun IlerlemeBasligi() {
    Text(stringResource(R.string.progress_headline), style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
}

@Composable
private fun AyrintiBasligi(baslik: String, onGeri: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        PremiumTextButton(onClick = onGeri, contentPadding = PaddingValues(horizontal = 0.dp),
            modifier = Modifier.heightIn(min = 48.dp).testTag("progress-back")) { Text(stringResource(R.string.common_back)) }
        Text(baslik, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
    }
}

@Composable
private fun YeniRutbeKarti(ozet: SeviyeOzeti) {
    val durum = ozet.durum
    val politika = LocalHareketPolitikasi.current
    val xp = if (politika.azaltildi) durum.toplamXp else
        animateIntAsState(durum.toplamXp, tween(politika.sure(HareketTuru.DEGER)), label = "xp-degisim").value
    Surface(shape = RoundedCornerShape(28.dp), color = Gece, border = BorderStroke(1.dp, BakirIsik.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth().testTag("progress-hero")) {
        Box(Modifier.background(Brush.linearGradient(listOf(Murekkep, Gece)))) {
            Canvas(Modifier.matchParentSize()) {
                repeat(3) { i -> drawCircle(BakirIsik.copy(alpha = 0.065f), 62.dp.toPx() + i * 32.dp.toPx(),
                    Offset(size.width * .92f, size.height * .22f), style = Stroke(1.dp.toPx())) }
            }
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (LocalDensity.current.fontScale >= 1.5f) Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    EmekEtiketi(); XpMetni(xp)
                } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) { EmekEtiketi(); XpMetni(xp) }
                if (LocalDensity.current.fontScale >= 1.5f) Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RutbeMadalyonu(durum.rutbe); RutbeBasligi(durum.rutbe, durum.seviye)
                } else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    RutbeMadalyonu(durum.rutbe)
                    Column(Modifier.weight(1f)) { RutbeBasligi(durum.rutbe, durum.seviye) }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    key(durum.seviye) { XpCubugu(durum.oran) }
                    Text(stringResource(R.string.progress_level_remaining, sayi(durum.kalanXp)),
                        color = Kagit.copy(alpha = .8f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable private fun EmekEtiketi() {
    Text(stringResource(R.string.progress_effort_label), color = BakirIsik,
        style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp)
}
@Composable private fun XpMetni(xp: Int) {
    Text(stringResource(R.string.progress_total_xp, sayi(xp)), color = Kagit.copy(alpha = .85f),
        style = MaterialTheme.typography.labelLarge, modifier = Modifier.testTag("progress-xp-total"))
}
@Composable private fun RutbeBasligi(rutbe: Rutbe, seviye: Int) {
    Text(rutbe.ad, color = Kagit, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text(stringResource(R.string.level_number, seviye), color = BakirIsik, style = MaterialTheme.typography.bodyMedium)
}
@Composable private fun RutbeMadalyonu(rutbe: Rutbe) {
    Box(Modifier.size(80.dp).background(Brush.radialGradient(listOf(Color.White.copy(alpha = .16f), Color.White.copy(alpha = .02f))), CircleShape),
        contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) { drawCircle(BakirIsik.copy(alpha = .25f), style = Stroke(1.dp.toPx())) }
        Image(painterResource(rutbe.gorsel), contentDescription = null, modifier = Modifier.size(72.dp))
    }
}
@Composable private fun XpCubugu(oran: Float) {
    val politika = LocalHareketPolitikasi.current
    val hedef = oran.coerceIn(0f, 1f)
    val ilerleme = if (politika.azaltildi) hedef else animateFloatAsState(hedef,
        tween(politika.sure(HareketTuru.DEGER)), label = "xp-ilerleme").value
    Box(Modifier.fillMaxWidth().height(7.dp).clip(CircleShape).background(Color.White.copy(alpha = .12f))
        .testTag("progress-xp-bar").semantics { progressBarRangeInfo = ProgressBarRangeInfo(hedef, 0f..1f) }) {
        Box(Modifier.fillMaxWidth(ilerleme.coerceIn(0f, 1f)).height(7.dp)
            .background(Brush.horizontalGradient(listOf(Color(0xFFCE9566), BakirIsik))))
    }
}

@Composable
private fun GunlukHedef(tekrar: Int, hedef: Int) {
    val oran = if (hedef > 0) (tekrar.toFloat() / hedef).coerceIn(0f, 1f) else 0f
    val renk = MaterialTheme.colorScheme
    val politika = LocalHareketPolitikasi.current
    val cizim = if (politika.azaltildi) oran else animateFloatAsState(oran,
        tween(politika.sure(HareketTuru.DEGER)), label = "gunluk-hedef").value
    Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Canvas(Modifier.size(52.dp).clearAndSetSemantics {}) {
            val kalinlik = 5.dp.toPx()
            val kenar = Offset(kalinlik / 2, kalinlik / 2)
            val alan = androidx.compose.ui.geometry.Size(size.width - kalinlik, size.height - kalinlik)
            drawArc(renk.surfaceVariant, -90f, 360f, false, kenar, alan, style = Stroke(kalinlik, cap = StrokeCap.Round))
            if (cizim > 0f) drawArc(renk.primary, -90f, 360f * cizim, false, kenar, alan,
                style = Stroke(kalinlik, cap = StrokeCap.Round))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(stringResource(R.string.progress_today_scope), style = MaterialTheme.typography.labelMedium,
                color = renk.onSurfaceVariant)
            Text(stringResource(R.string.progress_daily_count, tekrar, hedef), style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold, modifier = Modifier.testTag("progress-daily-count"))
        }
    }
}

@Composable
private fun Istatistikler(ozet: SeviyeOzeti) {
    val veriler = listOf(R.string.level_stat_questions to ozet.soruSayisi, R.string.level_stat_reviews to ozet.tekrarSayisi,
        R.string.level_stat_independent to ozet.bagimsizSayisi, R.string.level_stat_best_streak to ozet.enUzunSeri)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.testTag("progress-lifetime-stats")) {
        val sutun = if (LocalDensity.current.fontScale >= 1.5f) 1 else 2
        veriler.chunked(sutun).forEach { satir ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                satir.forEach { (ad, n) -> IlerlemePaneli(Modifier.weight(1f)) {
                    Text(sayi(n), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold)
                    Text(stringResource(ad), style = MaterialTheme.typography.labelMedium)
                } }
            }
        }
    }
}

@Composable
private fun XpDokumu(ozet: SeviyeOzeti) {
    IlerlemePaneli(Modifier.testTag("progress-xp-breakdown")) {
        BolumBasligi(stringResource(R.string.level_breakdown_title))
        Text(stringResource(R.string.progress_xp_note), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        val k = ozet.kirilim
        XpSatiri(stringResource(R.string.level_xp_questions, ozet.soruSayisi), k.ekleme)
        XpSatiri(stringResource(R.string.level_xp_reviews, ozet.tekrarSayisi), k.tekrar)
        XpSatiri(stringResource(R.string.level_xp_independent, ozet.bagimsizSayisi), k.bagimsiz)
        XpSatiri(stringResource(R.string.level_xp_goal, k.hedef / SeviyeHesabi.HEDEF_BONUS_XP), k.hedef)
        XpSatiri(stringResource(R.string.level_xp_streak), k.seri)
        XpSatiri(stringResource(R.string.level_xp_badges, ozet.sahipRozetler.size, Rozet.entries.size), k.rozet)
    }
}
@Composable private fun XpSatiri(ad: String, deger: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(ad, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.progress_xp_amount, sayi(deger)), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun DersPekismesi(dersler: List<DersIlerlemesi>) {
    IlerlemePaneli {
        Text(stringResource(R.string.progress_learning_note), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        dersler.forEach { ders ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                Text(ders.dersAdi, style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.level_lesson_counts, ders.saglam, ders.pekisiyor, ders.ogreniyor, ders.yeni),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val oran = ders.pekismeOrani / 100f
                LinearProgressIndicator(progress = { oran }, modifier = Modifier.fillMaxWidth().height(5.dp))
            }
        }
    }
}

@Composable
internal fun IlerlemeDurumPaneli(hata: Boolean, insets: PaddingValues, onYenile: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().testTag(if (hata) "progress-error" else "progress-loading"),
        contentPadding = PaddingValues(20.dp, insets.calculateTopPadding() + 16.dp, 20.dp, insets.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { IlerlemeBasligi() }
        item { IlerlemePaneli {
            BolumBasligi(stringResource(if (hata) R.string.level_error else R.string.progress_loading))
            if (hata) {
                Text(
                    stringResource(R.string.progress_error_note),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PremiumButton(onClick = onYenile, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.level_retry))
                }
            } else {
                val loadingLabel = stringResource(R.string.progress_loading)
                Column(
                    modifier = Modifier.semantics {
                        contentDescription = loadingLabel
                        liveRegion = LiveRegionMode.Polite
                    },
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    listOf(1f, .72f, .46f).forEach { fraction ->
                        Box(
                            Modifier.fillMaxWidth(fraction).height(12.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        )
                    }
                }
            }
        } }
    }
}

@Composable
internal fun IlerlemePaneli(modifier: Modifier = Modifier, icerikBosluk: Int = 12, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f))) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(icerikBosluk.dp), content = content)
    }
}
@Composable private fun BolumBasligi(metin: String) {
    Text(metin, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.semantics { heading() })
}
private fun sayi(deger: Int): String = NumberFormat.getIntegerInstance(Locale.forLanguageTag("tr-TR")).format(deger)

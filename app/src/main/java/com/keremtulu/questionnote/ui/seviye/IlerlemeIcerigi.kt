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
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.seviye.DersIlerlemesi
import com.keremtulu.questionnote.data.seviye.SeviyeOzeti
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi
import com.keremtulu.questionnote.domain.seviye.Rutbe
import com.keremtulu.questionnote.ui.karne.GizliRozetGorseli
import com.keremtulu.questionnote.ui.karne.RozetGorseli
import com.keremtulu.questionnote.ui.tema.*
import java.text.NumberFormat
import java.util.Locale
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import com.keremtulu.questionnote.ui.premium.PremiumButton

// Established petrol/copper hero palette; deliberately independent of the page's light/dark surfaces.

/** Navigation is presentation state only. Opening a detail never creates a measurement or changes a plan. */
private enum class IlerlemeBolumu(val baslik: Int) {
    OZET(R.string.progress_headline),
    ROZETLER(R.string.progress_collection_title), RUTBELER(R.string.rank_gallery_title);

    fun ust(): IlerlemeBolumu = if (this == RUTBELER) ROZETLER else OZET

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
        // Study first, evidence second, rewards last. No technical report navigation.
        LazyColumn(state = ozetListesi, modifier = Modifier.fillMaxSize().testTag("progress-list"),
            contentPadding = padding, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item(key = "baslik") { IlerlemeBasligi() }
            // Level first: this is the "how far have I come" screen, the action comes right below it.
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
            ozet.analiz?.let { analiz ->
                item(key = "hafta") { HaftalikCalismaOzeti(analiz) }
            }
            if (ozet.dersler.isNotEmpty()) item(key = "dersler") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BolumBasligi(stringResource(R.string.level_lessons_title))
                    DersPekismesi(ozet.dersler)
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

                }
            }
        }
    } else key(sayfa) {
        // Each destination has its own short page; the summary's scroll position stays untouched.
        LazyColumn(modifier = Modifier.fillMaxSize().testTag("progress-detail-list"), contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { AyrintiBasligi(stringResource(sayfa.baslik)) { sayfa = sayfa.ust() } }
            when (sayfa) {
                IlerlemeBolumu.ROZETLER -> {
                    item { IlerlemeGecisSatiri(stringResource(R.string.rank_gallery_title), null, "progress-open-ranks",
                        onClick = { sayfa = IlerlemeBolumu.RUTBELER }) }
                    item { RozetKoleksiyonu(ozet.sahipRozetler, ozet.rozetGirdisi, kazanilmaZamani) }
                }
                IlerlemeBolumu.RUTBELER -> item { RutbeYolculugu(ozet.durum, baslikGoster = false) }
                IlerlemeBolumu.OZET -> Unit
            }
        }
    }
}

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
private fun HaftalikCalismaOzeti(a: com.keremtulu.questionnote.domain.gelisim.GelisimAnalizi) {
    val toplam = a.ritim.sumOf { it.tekrar }
    val aktifGun = a.ritim.count { it.tekrar > 0 }
    val tarih = remember { java.time.format.DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("tr-TR")) }
    IlerlemePaneli(Modifier.testTag("progress-weekly-summary")) {
        BolumBasligi(stringResource(R.string.progress_weekly_title))
        Text(stringResource(R.string.progress_action_scope, a.kapsamAdi),
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.progress_weekly_summary, aktifGun, toplam),
            style = MaterialTheme.typography.titleMedium)
        val enYuksek = (a.ritim.maxOfOrNull { it.tekrar } ?: 0).coerceAtLeast(1)
        if (LocalDensity.current.fontScale < 1.5f) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom) {
                a.ritim.forEach { gun ->
                    val aciklama = tarih.format(java.time.LocalDate.ofEpochDay(gun.gun.toLong())) + ": " +
                        stringResource(R.string.progress_weekly_reviews, gun.tekrar)
                    Column(Modifier.weight(1f).clearAndSetSemantics { contentDescription = aciklama },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(gun.tekrar.toString(), style = MaterialTheme.typography.labelMedium)
                        Box(Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.BottomCenter) {
                            if (gun.tekrar > 0) Box(Modifier.fillMaxWidth()
                                .height((64f * gun.tekrar / enYuksek).dp)
                                .background(if (gun.gun == a.gun) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.secondary))
                        }
                        Text(java.time.LocalDate.ofEpochDay(gun.gun.toLong()).dayOfMonth.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            a.ritim.forEach { gun ->
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(tarih.format(java.time.LocalDate.ofEpochDay(gun.gun.toLong())), Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (gun.gun == a.gun) FontWeight.SemiBold else FontWeight.Normal)
                        Text(stringResource(R.string.progress_weekly_reviews, gun.tekrar),
                            style = MaterialTheme.typography.bodyMedium)
                    }
                    // Decorative count bars: exact accessible counts remain in text above; no fake percentage.
                    Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant).clearAndSetSemantics {}) {
                        if (gun.tekrar > 0) Box(Modifier.fillMaxWidth(gun.tekrar.toFloat() / enYuksek)
                            .height(4.dp).background(if (gun.gun == a.gun) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.secondary))
                    }
                }
            }
        }
        Text(stringResource(R.string.progress_weekly_note), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    val yuzeyRenkleri = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(24.dp), color = yuzeyRenkleri.surface,
        border = BorderStroke(1.dp, yuzeyRenkleri.outlineVariant.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth().testTag("progress-hero")) {
        Box {
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable private fun EmekEtiketi() {
    Text(stringResource(R.string.progress_effort_label), color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
}
@Composable private fun XpMetni(xp: Int) {
    Text(stringResource(R.string.progress_total_xp, sayi(xp)), color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.labelLarge, modifier = Modifier.testTag("progress-xp-total"))
}
@Composable private fun RutbeBasligi(rutbe: Rutbe, seviye: Int) {
    Text(rutbe.ad, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold)
    Text(stringResource(R.string.level_number, seviye), color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium)
}
@Composable private fun RutbeMadalyonu(rutbe: Rutbe) {
    // Flat medallion: no fake glass highlight over the emblem.
    Box(Modifier.size(80.dp), contentAlignment = Alignment.Center) {
        Image(painterResource(rutbe.gorsel), contentDescription = null, modifier = Modifier.size(72.dp))
    }
}
@Composable private fun XpCubugu(oran: Float) {
    val politika = LocalHareketPolitikasi.current
    val hedef = oran.coerceIn(0f, 1f)
    val ilerleme = if (politika.azaltildi) hedef else animateFloatAsState(hedef,
        tween(politika.sure(HareketTuru.DEGER)), label = "xp-ilerleme").value
    Box(Modifier.fillMaxWidth().height(7.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
        .testTag("progress-xp-bar").semantics { progressBarRangeInfo = ProgressBarRangeInfo(hedef, 0f..1f) }) {
        Box(Modifier.fillMaxWidth(ilerleme.coerceIn(0f, 1f)).height(7.dp)
            .background(MaterialTheme.colorScheme.primary))
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

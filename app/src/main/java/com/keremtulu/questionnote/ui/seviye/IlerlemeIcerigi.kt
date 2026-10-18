package com.keremtulu.questionnote.ui.seviye

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.seviye.DersIlerlemesi
import com.keremtulu.questionnote.data.seviye.GunTahmini
import com.keremtulu.questionnote.data.seviye.SeviyeOzeti
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.domain.seviye.Rutbe
import com.keremtulu.questionnote.domain.seviye.SeviyeHesabi
import com.keremtulu.questionnote.ui.karne.GizliRozetGorseli
import com.keremtulu.questionnote.ui.karne.RozetGorseli
import com.keremtulu.questionnote.ui.tema.HareketTuru
import com.keremtulu.questionnote.ui.tema.LocalHareketPolitikasi
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Murekkep = Color(0xFF153544)
private val Gece = Color(0xFF0C202C)
private val Kagit = Color(0xFFFFF7E8)
private val BakirIsik = Color(0xFFEFC18A)
private val KartSekli = RoundedCornerShape(24.dp)

@Composable
internal fun IlerlemeIcerigi(
    ozet: SeviyeOzeti,
    yetersizVeri: Boolean,
    insets: PaddingValues,
    kazanilmaZamani: (Rozet) -> Long?,
    onBugun: () -> Unit,
    onSoruEkle: () -> Unit,
    onCalis: (com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi, Int) -> Unit,
) {
    val liste = rememberLazyListState()
    LazyColumn(
        state = liste,
        modifier = Modifier.fillMaxSize().testTag("progress-list"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp,
            top = insets.calculateTopPadding() + 20.dp, bottom = insets.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item(key = "baslik") { IlerlemeBasligi() }
        item(key = "rutbe") { YeniRutbeKarti(ozet) }
        ozet.analiz?.let { a -> item(key = "sonraki-adim") { SonrakiAdimKarti(a, onCalis) } }
        if (yetersizVeri) item(key = "ilk-adim") {
            IlerlemePaneli(Modifier.testTag("progress-empty")) {
                BolumBasligi(stringResource(R.string.progress_first_step))
                Text(stringResource(if (ozet.soruSayisi == 0) R.string.progress_empty_archive else R.string.progress_no_reviews),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                Button(onClick = if (ozet.soruSayisi == 0) onSoruEkle else onBugun,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(stringResource(if (ozet.soruSayisi == 0) R.string.progress_add_question else R.string.progress_go_today))
                }
            }
        }
        item(key = "sayilar") { Istatistikler(ozet) }
        item(key = "xp") { XpDokumu(ozet) }
        item(key = "ogrenme-baslik") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BolumBasligi(stringResource(R.string.progress_learning_title))
                Text(stringResource(R.string.progress_learning_note), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (ozet.dersler.isNotEmpty()) item(key = "dersler") { DersPekismesi(ozet.dersler) }
        if (ozet.analiz == null) item(key = "plan") { TekrarTakvimi(ozet.tahmin) }
        ozet.analiz?.let { a -> item(key = "analiz") { GelisimAnaliziKarti(a, onCalis) } }
        ozet.sureKapsami?.let { kapsam -> item(key = "sure-profili") { SureProfiliKarti(kapsam) } }
        if (!yetersizVeri) item(key = "bugun") {
            OutlinedButton(onClick = onBugun, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(stringResource(R.string.progress_go_today))
            }
        }
        item(key = "koleksiyon") { Koleksiyonlar(ozet, kazanilmaZamani) }
        item(key = "kapsam") {
            Text(stringResource(R.string.progress_scope), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun IlerlemeBasligi() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(7.dp).background(MaterialTheme.colorScheme.secondary, CircleShape))
            Text(stringResource(R.string.level_kicker), style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
        }
        Text(stringResource(R.string.progress_headline), style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
        Text(stringResource(R.string.progress_subtitle), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun YeniRutbeKarti(ozet: SeviyeOzeti) {
    val durum = ozet.durum
    val politika = LocalHareketPolitikasi.current
    // Initial values are real values, not fake XP gained from zero on each screen entry.
    val xp = if (politika.azaltildi) durum.toplamXp else
        animateIntAsState(durum.toplamXp, tween(politika.sure(HareketTuru.DEGER)), label = "xp-degisim").value
    Surface(shape = RoundedCornerShape(28.dp), color = Gece,
        border = BorderStroke(1.dp, BakirIsik.copy(alpha = 0.2f)), modifier = Modifier.fillMaxWidth().testTag("progress-hero")) {
        Box(Modifier.background(Brush.linearGradient(listOf(Murekkep, Gece)))) {
            Canvas(Modifier.matchParentSize()) {
                val merkez = Offset(size.width * 0.94f, size.height * 0.12f)
                repeat(3) { i ->
                    drawCircle(BakirIsik.copy(alpha = 0.06f + i * 0.015f), 70.dp.toPx() + i * 34.dp.toPx(), merkez, style = Stroke(1.dp.toPx()))
                }
            }
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(stringResource(R.string.progress_effort_label), color = BakirIsik,
                    style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
                BoxWithConstraints {
                    val dikey = maxWidth < 280.dp || LocalDensity.current.fontScale >= 1.5f
                    if (dikey) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            RutbeMadalyonu(durum.rutbe)
                            RutbeBasligi(durum.rutbe, durum.seviye, xp)
                        }
                    } else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        RutbeMadalyonu(durum.rutbe)
                        Column(Modifier.weight(1f)) { RutbeBasligi(durum.rutbe, durum.seviye, xp) }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // A new level resets this decorative interpolation rather than draining the old level's bar.
                    key(durum.seviye) { XpCubugu(durum.oran) }
                    Text(stringResource(R.string.progress_xp_fraction, sayi(durum.buSeviyedeXp), sayi(durum.sonrakiSeviyeXp)),
                        color = Kagit, style = MaterialTheme.typography.labelLarge)
                    Text(stringResource(R.string.progress_level_remaining, sayi(durum.kalanXp)),
                        color = Kagit.copy(alpha = 0.78f), style = MaterialTheme.typography.bodySmall)
                }
                val sonraki = Rutbe.entries.firstOrNull { it.baslangicSeviye > durum.seviye }
                Surface(color = Color.White.copy(alpha = 0.055f), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (sonraki != null) {
                            Text(stringResource(R.string.progress_next_rank, sonraki.ad), color = BakirIsik,
                                style = MaterialTheme.typography.titleSmall)
                            val kalan = (SeviyeHesabi.seviyeEsigi(sonraki.baslangicSeviye).toLong() - durum.toplamXp).coerceAtLeast(0).toInt()
                            Text(stringResource(R.string.progress_rank_remaining, sonraki.baslangicSeviye, sayi(kalan)),
                                color = Kagit.copy(alpha = 0.82f), style = MaterialTheme.typography.bodySmall)
                        } else Text(stringResource(R.string.level_next_rank_none), color = Kagit, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun RutbeMadalyonu(rutbe: Rutbe) {
    Box(Modifier.size(96.dp).background(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.02f))), CircleShape),
        contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) { drawCircle(BakirIsik.copy(alpha = 0.28f), style = Stroke(1.dp.toPx())) }
        Image(painterResource(rutbe.gorsel), contentDescription = null, modifier = Modifier.size(84.dp))
    }
}

@Composable
private fun RutbeBasligi(rutbe: Rutbe, seviye: Int, xp: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(rutbe.ad, color = Kagit, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.level_number, seviye), color = BakirIsik, style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.progress_total_xp, sayi(xp)), color = Kagit.copy(alpha = 0.82f),
            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag("progress-xp-total"))
    }
}

@Composable
private fun XpCubugu(oran: Float) {
    val politika = LocalHareketPolitikasi.current
    val hedef = oran.coerceIn(0f, 1f)
    val ilerleme = if (politika.azaltildi) hedef else
        animateFloatAsState(hedef, tween(politika.sure(HareketTuru.DEGER)), label = "xp-ilerleme").value
    Box(Modifier.fillMaxWidth().height(9.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f))
        .testTag("progress-xp-bar").semantics { progressBarRangeInfo = ProgressBarRangeInfo(hedef, 0f..1f) }) {
        Box(Modifier.fillMaxWidth(ilerleme.coerceIn(0f, 1f)).height(9.dp)
            .background(Brush.horizontalGradient(listOf(Color(0xFFCE9566), BakirIsik))))
    }
}

@Composable
private fun Istatistikler(ozet: SeviyeOzeti) {
    val veriler = listOf(
        R.string.level_stat_reviews to ozet.tekrarSayisi,
        R.string.level_stat_independent to ozet.bagimsizSayisi,
        R.string.level_stat_questions to ozet.soruSayisi,
        R.string.level_stat_best_streak to ozet.enUzunSeri,
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BolumBasligi(stringResource(R.string.progress_effort_title))
        Text(stringResource(R.string.progress_effort_note), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (LocalDensity.current.fontScale >= 1.6f) {
            veriler.forEach { (ad, sayi) -> IstatistikKutusu(stringResource(ad), sayi, Modifier.fillMaxWidth()) }
        } else veriler.chunked(2).forEach { satir ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                satir.forEach { (ad, sayi) -> IstatistikKutusu(stringResource(ad), sayi, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun IstatistikKutusu(ad: String, deger: Int, modifier: Modifier) {
    Surface(modifier.semantics(mergeDescendants = true) {}, shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(sayi(deger), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(ad, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun XpDokumu(ozet: SeviyeOzeti) {
    var acik by rememberSaveable { mutableStateOf(false) }
    IlerlemePaneli {
        BolumBasligi(stringResource(R.string.level_breakdown_title))
        Text(stringResource(R.string.progress_xp_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = { acik = !acik }, modifier = Modifier.heightIn(min = 48.dp).testTag("progress-xp-toggle")) {
            Text(stringResource(if (acik) R.string.progress_hide_details else R.string.progress_show_details))
        }
        AcilirBolum(acik) {
            val k = ozet.kirilim
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                XpSatiri(stringResource(R.string.level_xp_questions, ozet.soruSayisi), k.ekleme)
                XpSatiri(stringResource(R.string.level_xp_reviews, ozet.tekrarSayisi), k.tekrar)
                XpSatiri(stringResource(R.string.level_xp_independent, ozet.bagimsizSayisi), k.bagimsiz)
                XpSatiri(stringResource(R.string.level_xp_goal, k.hedef / SeviyeHesabi.HEDEF_BONUS_XP), k.hedef)
                XpSatiri(stringResource(R.string.level_xp_streak), k.seri)
                XpSatiri(stringResource(R.string.level_xp_badges, ozet.sahipRozetler.size, Rozet.entries.size), k.rozet)
            }
        }
    }
}

@Composable
private fun XpSatiri(ad: String, deger: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(ad, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.progress_xp_amount, sayi(deger)), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun DersPekismesi(dersler: List<DersIlerlemesi>) {
    var tumu by rememberSaveable { mutableStateOf(false) }
    IlerlemePaneli {
        BolumBasligi(stringResource(R.string.level_lessons_title))
        (if (tumu) dersler else dersler.take(5)).forEach { ders ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                Text(ders.dersAdi, style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.level_lesson_counts, ders.saglam, ders.pekisiyor, ders.ogreniyor, ders.yeni),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val oran = ders.pekismeOrani / 100f
                Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                    .semantics { progressBarRangeInfo = ProgressBarRangeInfo(oran, 0f..1f) }) {
                    Box(Modifier.fillMaxWidth(oran).height(5.dp).background(MaterialTheme.colorScheme.tertiary))
                }
            }
        }
        if (dersler.size > 5) TextButton(onClick = { tumu = !tumu }) {
            Text(stringResource(if (tumu) R.string.progress_hide_details else R.string.progress_all_lessons))
        }
    }
}

@Composable
private fun TekrarTakvimi(tahmin: List<GunTahmini>) {
    val formatter = remember { DateTimeFormatter.ofPattern("EEE", Locale.forLanguageTag("tr-TR")) }
    val tamTarih = remember { DateTimeFormatter.ofPattern("d MMMM EEEE", Locale.forLanguageTag("tr-TR")) }
    val max = (tahmin.maxOfOrNull { it.adet } ?: 0).coerceAtLeast(1)
    IlerlemePaneli(Modifier.testTag("progress-forecast")) {
        BolumBasligi(stringResource(R.string.level_forecast_title))
        Text(stringResource(R.string.progress_forecast_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (tahmin.none { it.adet > 0 }) Text(stringResource(R.string.level_forecast_empty), style = MaterialTheme.typography.bodyMedium)
        else if (LocalDensity.current.fontScale >= 1.5f) {
            tahmin.forEach { gun ->
                Text(stringResource(R.string.progress_day_load, tamTarih.format(LocalDate.ofEpochDay(gun.gun.toLong())), gun.adet),
                    style = MaterialTheme.typography.bodyMedium)
            }
        } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            tahmin.forEachIndexed { index, gun ->
                val tarih = LocalDate.ofEpochDay(gun.gun.toLong())
                val aciklama = stringResource(R.string.progress_day_load, tamTarih.format(tarih), gun.adet)
                Column(Modifier.weight(1f).clearAndSetSemantics { contentDescription = aciklama },
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(gun.adet.toString(), style = MaterialTheme.typography.labelMedium)
                    Box(Modifier.fillMaxWidth().height(84.dp), contentAlignment = Alignment.BottomCenter) {
                        Box(Modifier.fillMaxWidth().height((4 + 80f * gun.adet.coerceAtLeast(0) / max).dp).clip(RoundedCornerShape(7.dp))
                            .background(if (gun.adet == 0) MaterialTheme.colorScheme.surfaceVariant
                                else if (index == 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary))
                    }
                    Text(formatter.format(tarih), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun Koleksiyonlar(ozet: SeviyeOzeti, kazanilmaZamani: (Rozet) -> Long?) {
    var rozetlerAcik by rememberSaveable { mutableStateOf(false) }
    var rutbelerAcik by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        IlerlemePaneli {
            BolumBasligi(stringResource(R.string.progress_collection_title))
            Text(stringResource(R.string.level_badges_title, ozet.sahipRozetler.size, Rozet.entries.size),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val secilenler = (Rozet.entries.filter { it in ozet.sahipRozetler } + Rozet.entries.filter { it !in ozet.sahipRozetler }).take(3)
                secilenler.forEach { rozet ->
                    val kazanildi = rozet in ozet.sahipRozetler
                    if (rozet.gizli && !kazanildi) GizliRozetGorseli(Modifier.size(56.dp))
                    else RozetGorseli(rozet, kazanildi, Modifier.size(56.dp))
                }
            }
            OutlinedButton(onClick = { rozetlerAcik = !rozetlerAcik }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(stringResource(if (rozetlerAcik) R.string.progress_close_badges else R.string.progress_open_badges))
            }
            AcilirBolum(rozetlerAcik) { RozetKoleksiyonu(ozet.sahipRozetler, ozet.rozetGirdisi, kazanilmaZamani) }
        }
        IlerlemePaneli {
            BolumBasligi(stringResource(R.string.rank_gallery_title))
            Text(stringResource(R.string.rank_gallery_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = { rutbelerAcik = !rutbelerAcik }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(stringResource(if (rutbelerAcik) R.string.progress_close_ranks else R.string.progress_open_ranks))
            }
            AcilirBolum(rutbelerAcik) { RutbeYolculugu(ozet.durum) }
        }
    }
}

@Composable
private fun AcilirBolum(acik: Boolean, content: @Composable () -> Unit) {
    val politika = LocalHareketPolitikasi.current
    if (politika.azaltildi) {
        if (acik) content()
    } else AnimatedVisibility(acik,
        enter = expandVertically(tween(politika.sure(HareketTuru.GIRIS))),
        exit = shrinkVertically(tween(politika.sure(HareketTuru.GIRIS)))) { content() }
}

@Composable
internal fun IlerlemeDurumPaneli(hata: Boolean, insets: PaddingValues, onYenile: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().testTag(if (hata) "progress-error" else "progress-loading"),
        contentPadding = PaddingValues(20.dp, insets.calculateTopPadding() + 20.dp, 20.dp, insets.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item { IlerlemeBasligi() }
        item {
            IlerlemePaneli {
                Box(Modifier.size(52.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Text(if (hata) "!" else "…", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                BolumBasligi(stringResource(if (hata) R.string.level_error else R.string.progress_loading))
                Text(stringResource(if (hata) R.string.progress_error_note else R.string.progress_loading_note),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (hata) Button(onClick = onYenile, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.level_retry))
                } else {
                    // Static skeleton: no infinite shimmer, fake metrics or motion-only loading signal.
                    listOf(1f, 0.72f, 0.46f).forEach { genislik ->
                        Box(Modifier.fillMaxWidth(genislik).height(12.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape))
                    }
                }
            }
        }
    }
}

@Composable
private fun IlerlemePaneli(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = KartSekli, color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
private fun BolumBasligi(metin: String) {
    Text(metin, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
}

private fun sayi(deger: Int): String = NumberFormat.getIntegerInstance(Locale.forLanguageTag("tr-TR")).format(deger)

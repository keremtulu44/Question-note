package com.keremtulu.questionnote.ui.seviye

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.TextButton
import kotlinx.coroutines.flow.collect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.prefs.RozetDeposu
import com.keremtulu.questionnote.data.seviye.DersIlerlemesi
import com.keremtulu.questionnote.data.seviye.GunTahmini
import com.keremtulu.questionnote.data.seviye.SeviyeHizmeti
import com.keremtulu.questionnote.data.seviye.SeviyeOzeti
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.domain.seviye.Rutbe
import com.keremtulu.questionnote.domain.seviye.SeviyeHesabi
import com.keremtulu.questionnote.ui.karne.RozetGorseli
import com.keremtulu.questionnote.ui.karne.rozetAdi
import com.keremtulu.questionnote.ui.karne.rozetAciklamasi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch

private sealed interface SeviyeDurumuUi {
    data object Yukleniyor : SeviyeDurumuUi
    data class Hazir(val ozet: SeviyeOzeti, val rozetler: Set<Rozet>) : SeviyeDurumuUi
    data object Hata : SeviyeDurumuUi
}

@Composable
fun SeviyeEkrani(
    seviyeHizmeti: SeviyeHizmeti,
    rozetDeposu: RozetDeposu,
) {
    var yenileme by remember { mutableIntStateOf(0) }
    val durum = produceState<SeviyeDurumuUi>(SeviyeDurumuUi.Yukleniyor, seviyeHizmeti, yenileme) {
        value = SeviyeDurumuUi.Yukleniyor
        seviyeHizmeti.ozetIzle().catch { value = SeviyeDurumuUi.Hata }.collect {
            value = SeviyeDurumuUi.Hazir(it, rozetDeposu.sahipOlanlar())
        }
    }.value

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { insets ->
        when (durum) {
            SeviyeDurumuUi.Yukleniyor -> Box(
                Modifier.fillMaxSize().padding(insets),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
            SeviyeDurumuUi.Hata -> Box(
                Modifier.fillMaxSize().padding(insets),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.level_error), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { yenileme++ }) { Text(stringResource(R.string.level_retry)) }
                }
            }
            is SeviyeDurumuUi.Hazir -> Icerik(durum.ozet, durum.rozetler, rozetDeposu, insets)
        }
    }
}

@Composable
private fun Icerik(ozet: SeviyeOzeti, sahip: Set<Rozet>, depo: RozetDeposu, insets: PaddingValues) {
    val durum = ozet.durum
    val colors = MaterialTheme.colorScheme
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = insets.calculateTopPadding() + 12.dp,
            end = 20.dp,
            bottom = insets.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                stringResource(R.string.level_kicker),
                color = colors.primary,
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.4.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                stringResource(R.string.level_title),
                modifier = Modifier.padding(top = 4.dp),
                color = colors.onBackground,
                style = MaterialTheme.typography.displaySmall,
            )
        }
        item { RutbeKarti(durum.rutbe, durum.seviye, durum.toplamXp, durum.buSeviyedeXp, durum.sonrakiSeviyeXp, durum.kalanXp, durum.oran) }
        item { IstatistikKarti(ozet) }
        item { XpKirilimKarti(ozet) }
        item { RutbeYolculugu(durum) }
        item { RozetKoleksiyonu(sahip, ozet.rozetGirdisi, depo) }
        if (ozet.dersler.isNotEmpty()) {
            item { PekismeKarti(ozet.dersler) }
        }
        item { TahminKarti(ozet.tahmin) }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun RutbeKarti(
    rutbe: Rutbe,
    seviye: Int,
    toplamXp: Int,
    buSeviyede: Int,
    gereken: Int,
    kalan: Int,
    oran: Float,
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = colors.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = colors.surface.copy(alpha = 0.55f),
                ) {
                    Image(
                        painter = painterResource(rutbe.gorsel),
                        contentDescription = rutbe.ad,
                        modifier = Modifier.size(84.dp).padding(8.dp),
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        rutbe.ad,
                        color = colors.onPrimaryContainer,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        stringResource(R.string.level_number, seviye),
                        color = colors.onPrimaryContainer.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        stringResource(R.string.level_total_xp, toplamXp),
                        color = colors.onPrimaryContainer.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(colors.surface.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(oran.coerceIn(0f, 1f))
                        .height(12.dp)
                        .background(colors.primary, RoundedCornerShape(8.dp)),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "$buSeviyede / $gereken XP",
                    color = colors.onPrimaryContainer.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    stringResource(R.string.level_next_remaining, kalan),
                    color = colors.onPrimaryContainer.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            val sonraki = Rutbe.entries.firstOrNull { it.baslangicSeviye > seviye }
            Text(
                text = if (sonraki != null) {
                    stringResource(R.string.level_next_rank, sonraki.ad, sonraki.baslangicSeviye)
                } else {
                    stringResource(R.string.level_next_rank_none)
                },
                modifier = Modifier.padding(top = 10.dp),
                color = colors.onPrimaryContainer.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun IstatistikKarti(ozet: SeviyeOzeti) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.level_stats_title),
                color = colors.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Istatistik(ozet.soruSayisi.toString(), stringResource(R.string.level_stat_questions), Modifier.weight(1f))
                Istatistik(ozet.tekrarSayisi.toString(), stringResource(R.string.level_stat_reviews), Modifier.weight(1f))
                Istatistik(ozet.bagimsizSayisi.toString(), stringResource(R.string.level_stat_independent), Modifier.weight(1f))
                Istatistik(ozet.enUzunSeri.toString(), stringResource(R.string.level_stat_best_streak), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Istatistik(deger: String, etiket: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            deger,
            color = colors.primary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            etiket,
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun XpKirilimKarti(ozet: SeviyeOzeti) {
    val colors = MaterialTheme.colorScheme
    val k = ozet.kirilim
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.level_breakdown_title),
                color = colors.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            XpSatiri(stringResource(R.string.level_xp_questions, ozet.soruSayisi), k.ekleme)
            XpSatiri(stringResource(R.string.level_xp_reviews, ozet.tekrarSayisi), k.tekrar)
            XpSatiri(stringResource(R.string.level_xp_independent, ozet.bagimsizSayisi), k.bagimsiz)
            XpSatiri(
                stringResource(R.string.level_xp_goal, (k.hedef / SeviyeHesabi.HEDEF_BONUS_XP)),
                k.hedef,
            )
            XpSatiri(stringResource(R.string.level_xp_streak), k.seri)
            XpSatiri(stringResource(R.string.level_xp_badges, k.rozet / SeviyeHesabi.ROZET_BONUS_XP), k.rozet)
        }
    }
}

@Composable
private fun XpSatiri(etiket: String, xp: Int) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiket, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text(
            text = if (xp > 0) stringResource(R.string.level_xp_value, xp) else stringResource(R.string.level_xp_zero),
            color = if (xp > 0) colors.primary else colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun PekismeKarti(dersler: List<DersIlerlemesi>) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                stringResource(R.string.level_lessons_title),
                color = colors.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            dersler.take(5).forEach { ders ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PekismeHalkasi(ders.pekismeOrani, Modifier.size(44.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            ders.dersAdi,
                            color = colors.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            stringResource(
                                R.string.level_lesson_counts,
                                ders.saglam,
                                ders.pekisiyor,
                                ders.ogreniyor,
                                ders.yeni,
                            ),
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    Text(
                        "%${ders.pekismeOrani}",
                        color = colors.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun PekismeHalkasi(oran: Int, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Canvas(modifier) {
        val kalinlik = 5.dp.toPx()
        val inset = kalinlik / 2f
        val boyut = Size(size.width - kalinlik, size.height - kalinlik)
        drawArc(
            color = colors.surfaceVariant,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = boyut,
            style = Stroke(width = kalinlik),
        )
        drawArc(
            color = colors.primary,
            startAngle = -90f,
            sweepAngle = 360f * (oran.coerceIn(0, 100) / 100f),
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = boyut,
            style = Stroke(width = kalinlik, cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun TahminKarti(tahmin: List<GunTahmini>) {
    val colors = MaterialTheme.colorScheme
    val enYuksek = tahmin.maxOfOrNull { it.adet } ?: 0
    val formatter = remember { DateTimeFormatter.ofPattern("EEE", Locale.forLanguageTag("tr-TR")) }
    val bugun = remember { LocalDate.now().toEpochDay().toInt() }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.level_forecast_title),
                color = colors.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            if (enYuksek == 0) {
                Text(
                    stringResource(R.string.level_forecast_empty),
                    modifier = Modifier.padding(top = 8.dp),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    tahmin.forEach { gun ->
                        val oran = if (enYuksek == 0) 0f else gun.adet.toFloat() / enYuksek
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = if (gun.adet > 0) gun.adet.toString() else "",
                                color = colors.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                            )
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height((8 + 58 * oran).dp)
                                    .background(
                                        if (gun.adet == 0) colors.surfaceVariant
                                        else if (gun.gun == bugun) colors.tertiary
                                        else colors.primary.copy(alpha = 0.75f),
                                        RoundedCornerShape(6.dp),
                                    ),
                            )
                            Text(
                                formatter.format(LocalDate.ofEpochDay(gun.gun.toLong())),
                                color = colors.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

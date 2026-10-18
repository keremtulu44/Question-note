package com.keremtulu.questionnote.ui.seviye

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.gelisim.*
import com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi
import com.keremtulu.questionnote.ui.tema.HareketTuru
import com.keremtulu.questionnote.ui.tema.LocalHareketPolitikasi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor

enum class AnalizBolumu { RITIM, DONEM, PLAN, GRUPLAR }

/** One requested detail at a time. Immutable DTOs only; no extra data, queries or medians in Compose. */
@Composable
fun GelisimAnaliziKarti(a: GelisimAnalizi, bolum: AnalizBolumu = AnalizBolumu.RITIM, onCalis: (SoruCalismaFiltresi, Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.testTag("analysis-section")) {
        when (bolum) {
            AnalizBolumu.RITIM -> RitimKarti(a)
            AnalizBolumu.DONEM -> DonemKarti(a.donem)
            AnalizBolumu.PLAN -> PlanKarti(a)
            AnalizBolumu.GRUPLAR -> GrupListesi(a, onCalis)
        }
    }
}

/** The same route as before, without the repeated policy paragraphs on the overview. */
@Composable
fun SonrakiAdimKarti(a: GelisimAnalizi, onCalis: (SoruCalismaFiltresi, Int) -> Unit) {
    IlerlemePaneli { SonrakiAdimIcerigi(a, onCalis) }
}

@Composable
internal fun SonrakiAdimIcerigi(a: GelisimAnalizi, onCalis: (SoruCalismaFiltresi, Int) -> Unit) {
    val o = a.oneri
    if (o == null) {
        Text(stringResource(R.string.analysis_no_action), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        Text(stringResource(R.string.progress_action_scope, a.kapsamAdi), style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(when (o.tur) {
            OneriTuru.VADESI_GELEN -> R.string.progress_action_due
            OneriTuru.YAKIN_GUCLUK -> R.string.progress_action_difficulty
            OneriTuru.KUCUK_TUR -> R.string.progress_action_small
        }, o.uygunSoru), style = MaterialTheme.typography.bodyMedium)
        o.etiket?.let { Text(it, style = MaterialTheme.typography.titleSmall) }
        Button(onClick = { onCalis(o.filtre, o.turBoyutu) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            .testTag("analysis-action")) { Text(stringResource(R.string.progress_action_inspect, o.turBoyutu)) }
    }
}

@Composable
private fun RitimKarti(a: GelisimAnalizi) {
    IlerlemePaneli(Modifier.testTag("analysis-rhythm")) {
        Text(stringResource(R.string.progress_rhythm_scope), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (LocalDensity.current.fontScale >= 1.5f) {
            a.ritim.forEach { g ->
                Text(stringResource(R.string.analysis_rhythm_row, tarih(g.gun), g.soru, g.tekrar) +
                    if (g.bugunTamamlanmadi) " · ${stringResource(R.string.analysis_today_incomplete)}" else "",
                    Modifier.fillMaxWidth().padding(vertical = 8.dp), style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            val max = (a.ritim.maxOfOrNull { it.tekrar } ?: 0).coerceAtLeast(1)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                a.ritim.forEach { g ->
                    val aciklama = stringResource(R.string.analysis_rhythm_row, tarih(g.gun), g.soru, g.tekrar) +
                        if (g.bugunTamamlanmadi) " · ${stringResource(R.string.analysis_today_incomplete)}" else ""
                    Column(Modifier.weight(1f).clearAndSetSemantics { contentDescription = aciklama },
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(g.tekrar.toString(), style = MaterialTheme.typography.labelLarge)
                        Box(Modifier.fillMaxWidth().height(104.dp), contentAlignment = Alignment.BottomCenter) {
                            Box(Modifier.fillMaxWidth().height((4f + 100f * g.tekrar / max).dp).clip(RoundedCornerShape(8.dp))
                                .background(if (g.tekrar == 0) MaterialTheme.colorScheme.surfaceVariant
                                    else if (g.bugunTamamlanmadi) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary))
                        }
                        Text(HaftaGunu.format(LocalDate.ofEpochDay(g.gun.toLong())), style = MaterialTheme.typography.labelSmall,
                            color = if (g.bugunTamamlanmadi) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Text(stringResource(R.string.progress_today_partial), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        BilgiAciklamasi(R.string.progress_day_details, "analysis-rhythm-details") {
            a.ritim.forEach { g -> Text(stringResource(R.string.analysis_rhythm_row, tarih(g.gun), g.soru, g.tekrar),
                style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun PlanKarti(a: GelisimAnalizi) {
    var acikGun by rememberSaveable(a.gun, a.kapsamAdi) { mutableStateOf<Int?>(null) }
    IlerlemePaneli(Modifier.testTag("analysis-plan"), icerikBosluk = 4) {
        Text(stringResource(R.string.progress_action_scope, a.kapsamAdi), style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        a.plan.forEachIndexed { i, p ->
            val acik = acikGun == p.gun
            Surface(onClick = { acikGun = if (acik) null else p.gun },
                color = if (acik) MaterialTheme.colorScheme.primaryContainer.copy(alpha = .35f) else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("analysis-plan-day-$i")) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (i == 0) stringResource(R.string.progress_plan_today) else tarih(p.gun),
                        Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.progress_plan_count, p.adet), style = MaterialTheme.typography.titleSmall)
                }
            }
            AcilanIcerik(acik) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (p.adet == 0) Text(stringResource(R.string.progress_plan_empty)) else {
                        Text(stringResource(R.string.analysis_plan_coverage, p.kapsanan, p.adet, p.kapsamYuzde, p.bilinmeyen),
                            style = MaterialTheme.typography.bodySmall)
                        if (p.kapsanan > 0) Text(stringResource(R.string.analysis_partial_time, dakikaAlt(p.altMs), dakikaUst(p.ustMs)),
                            style = MaterialTheme.typography.bodyMedium)
                        if (p.toplamTahminAcik) Text(stringResource(R.string.analysis_total_time,
                            dakikaAlt(requireNotNull(p.toplamAltMs)), dakikaUst(requireNotNull(p.toplamUstMs))), style = MaterialTheme.typography.bodyMedium)
                        else Text(stringResource(R.string.analysis_no_total_time), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        BilgiAciklamasi(R.string.progress_calculation_info, "analysis-plan-method") {
            Text(stringResource(R.string.analysis_workload_note), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DonemKarti(d: DonemKarsilastirmasi) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.testTag("analysis-comparison")) {
        Text(stringResource(R.string.progress_completed_periods), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (LocalDensity.current.fontScale >= 1.5f) {
            DonemOzeti(R.string.progress_period_before, d.oncekiIlkGun, d.sonIlkGun - 1, d.onceki)
            DonemOzeti(R.string.progress_period_after, d.sonIlkGun, d.sonGun, d.son)
        } else Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DonemOzeti(R.string.progress_period_before, d.oncekiIlkGun, d.sonIlkGun - 1, d.onceki, Modifier.weight(1f))
            DonemOzeti(R.string.progress_period_after, d.sonIlkGun, d.sonGun, d.son, Modifier.weight(1f))
        }
        IlerlemePaneli {
            val e = d.eslesen
            Text(stringResource(R.string.progress_matched_sample, e.soru), style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold)
            if (e.yeterli) Text(stringResource(R.string.progress_matched_counts, e.onceBagimsiz, e.sonraBagimsiz),
                style = MaterialTheme.typography.bodyMedium)
            else Text(stringResource(R.string.progress_comparison_pending), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.progress_comparison_limit), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            BilgiAciklamasi(if (e.yeterli) R.string.progress_calculation_info else R.string.progress_why,
                "analysis-comparison-method") {
                Text(stringResource(R.string.analysis_matched_days, e.onceGun, e.sonraGun), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(if (e.yeterli) R.string.analysis_matched else R.string.analysis_insufficient_comparison,
                    e.soru, e.onceBagimsiz, e.sonraBagimsiz), style = MaterialTheme.typography.bodySmall)
                if (e.baglamDegisen > 0) Text(stringResource(R.string.analysis_context_excluded, e.baglamDegisen), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.analysis_comparison_note), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun DonemOzeti(baslik: Int, ilk: Int, son: Int, s: AnalizSayaci, modifier: Modifier = Modifier) {
    IlerlemePaneli(modifier) {
        Text(stringResource(baslik), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text("${tarih(ilk)} – ${tarih(son)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.progress_period_independent, s.bagimsiz, s.tekrar), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.progress_independent_short), style = MaterialTheme.typography.bodySmall)
        Text(stringResource(R.string.analysis_group_counts, s.soru, s.tekrar), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun GrupListesi(a: GelisimAnalizi, onCalis: (SoruCalismaFiltresi, Int) -> Unit) {
    var kaynak by rememberSaveable { mutableStateOf(false) }
    var sayfa by rememberSaveable { mutableIntStateOf(0) }
    var secili by remember(a.kapsamAdi) { mutableStateOf<GrupAnahtari?>(null) }
    val gruplar = if (kaynak) a.kaynaklar else a.konular
    val sonSayfa = ((gruplar.size - 1).coerceAtLeast(0) / 6)
    val guncelSayfa = sayfa.coerceIn(0, sonSayfa)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.testTag("analysis-groups")) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(false to R.string.progress_topics_short, true to R.string.progress_sources_short).forEach { (secim, ad) ->
                FilterChip(selected = kaynak == secim, onClick = { kaynak = secim; sayfa = 0; secili = null },
                    modifier = Modifier.heightIn(min = 48.dp).testTag(if (secim) "analysis-sources" else "analysis-topics"),
                    label = { Text(stringResource(ad)) })
            }
        }
        Text(stringResource(R.string.progress_captured_context), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (gruplar.isEmpty()) Text(stringResource(R.string.analysis_groups_empty))
        val ilk = (guncelSayfa * 6).coerceAtMost(gruplar.size)
        gruplar.subList(ilk, (ilk + 6).coerceAtMost(gruplar.size)).forEach { g ->
            val acik = secili == g.anahtar
            IlerlemePaneli(icerikBosluk = 0) {
                IlerlemeGecisSatiri(grupAdi(g), listOfNotNull(g.dersAdi,
                    stringResource(R.string.analysis_group_counts, g.sayac.soru, g.sayac.tekrar)).joinToString(" · "),
                    "analysis-group-${g.anahtar.tur}-${g.anahtar.dersId}-${g.anahtar.deger}",
                    onClick = { secili = if (acik) null else g.anahtar })
                if (acik) key(g.anahtar) {
                    Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.analysis_independent_fraction, g.sayac.bagimsiz, g.sayac.tekrar),
                            style = MaterialTheme.typography.bodyMedium)
                        if (g.filtre != null && g.uygunSoru > 0) Button(onClick = { onCalis(g.filtre, minOf(5, g.uygunSoru)) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                            Text(stringResource(R.string.analysis_group_action, g.uygunSoru))
                        } else Text(stringResource(if (g.anahtar.bilinen) R.string.analysis_no_eligible_group else R.string.analysis_unknown_no_route),
                            style = MaterialTheme.typography.bodySmall)
                        BilgiAciklamasi(R.string.progress_sample_details, "analysis-group-sample") {
                            if (g.etiketler.size > 1) Text(stringResource(R.string.analysis_captured_names,
                                g.etiketler.take(5).joinToString(" · "), g.etiketler.size), style = MaterialTheme.typography.bodySmall)
                            val s = g.sayac
                            Text(stringResource(R.string.analysis_measurement_fraction, s.guvenilirSure, s.tekrar, s.kapsamYuzde,
                                s.kismiSure, s.gecersizSure, s.baglamsiz), style = MaterialTheme.typography.bodySmall)
                            Text(stringResource(R.string.analysis_recent_group, g.yakinGuclukSoru, g.sonIkiBagimsizSoru), style = MaterialTheme.typography.bodySmall)
                            g.dersReferansi?.dagilim?.let { Text(stringResource(R.string.analysis_lesson_reference, saniye(it.medyanMs)),
                                style = MaterialTheme.typography.bodySmall) }
                            val p = g.yardimsizProfil
                            if (p?.dagilim != null) Text(stringResource(R.string.analysis_group_reference, saniye(p.dagilim.medyanMs),
                                p.olcumSayisi, p.farkliSoru, p.farkliGun), style = MaterialTheme.typography.bodySmall)
                            else Text(stringResource(R.string.analysis_group_reference_missing), style = MaterialTheme.typography.bodySmall)
                        }
                        BilgiAciklamasi(R.string.progress_comparison_short, "analysis-group-comparison") { DonemKarti(g.donem) }
                    }
                }
            }
        }
        if (sonSayfa > 0) {
            Text(stringResource(R.string.analysis_page, guncelSayfa + 1, sonSayfa + 1), style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { sayfa = guncelSayfa - 1; secili = null }, enabled = guncelSayfa > 0,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text(stringResource(R.string.analysis_previous)) }
                OutlinedButton(onClick = { sayfa = guncelSayfa + 1; secili = null }, enabled = guncelSayfa < sonSayfa,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text(stringResource(R.string.analysis_next)) }
            }
        }
        BilgiAciklamasi(R.string.progress_calculation_info, "analysis-history-method") {
            Text(stringResource(R.string.analysis_history_note), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun grupAdi(g: AnalizGrubu): String = if (!g.anahtar.bilinen) stringResource(R.string.analysis_unknown_context)
    else g.etiketler.firstOrNull() ?: stringResource(if (g.anahtar.tur == AnalizTuru.KONU) R.string.analysis_missing_topic else R.string.analysis_missing_source)

@Composable
private fun BilgiAciklamasi(baslik: Int, tag: String, content: @Composable ColumnScope.() -> Unit) {
    var acik by rememberSaveable { mutableStateOf(false) }
    val durum = stringResource(if (acik) R.string.progress_details_open else R.string.progress_details_closed)
    Column {
        TextButton(onClick = { acik = !acik }, modifier = Modifier.heightIn(min = 48.dp).testTag(tag)
            .semantics { stateDescription = durum }) { Text(stringResource(baslik)) }
        AcilanIcerik(acik) { Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content) }
    }
}

@Composable
private fun AcilanIcerik(acik: Boolean, content: @Composable () -> Unit) {
    val p = LocalHareketPolitikasi.current
    if (p.azaltildi) { if (acik) content() }
    else AnimatedVisibility(acik, enter = expandVertically(tween(p.sure(HareketTuru.GIRIS))),
        exit = shrinkVertically(tween(p.sure(HareketTuru.GIRIS)))) { content() }
}
private val TarihBicimi = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("tr-TR"))
private val HaftaGunu = DateTimeFormatter.ofPattern("EEE", Locale.forLanguageTag("tr-TR"))
private fun tarih(gun: Int) = LocalDate.ofEpochDay(gun.toLong()).format(TarihBicimi)
private fun dakikaAlt(ms: Double): Int = floor(ms / 60_000).toInt()
private fun dakikaUst(ms: Double): Int = ceil(ms / 60_000).toInt()
private fun saniye(ms: Double): String = java.text.NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR"))
    .apply { maximumFractionDigits = if (ms < 1_000) 3 else 1 }.format(ms / 1_000)

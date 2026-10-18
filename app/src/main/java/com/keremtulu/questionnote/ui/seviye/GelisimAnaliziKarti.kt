package com.keremtulu.questionnote.ui.seviye

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
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

/** All calculations arrive as immutable DTOs. This surface never queries Room or computes a median. */
@Composable
fun GelisimAnaliziKarti(a: GelisimAnalizi, onCalis: (SoruCalismaFiltresi, Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.testTag("analysis-section")) {
        AnalizPanel(stringResource(R.string.analysis_rhythm_title)) {
            Text(stringResource(R.string.analysis_archive_scope), style = MaterialTheme.typography.bodySmall)
            a.ritim.forEach { g ->
                Text(stringResource(R.string.analysis_rhythm_row, tarih(g.gun), g.soru, g.tekrar) +
                    if (g.bugunTamamlanmadi) " · ${stringResource(R.string.analysis_today_incomplete)}" else "",
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
        AnalizPanel(stringResource(R.string.analysis_comparison_title)) { DonemMetni(a.donem) }
        AnalizPanel(stringResource(R.string.analysis_plan_title)) {
            Text(stringResource(R.string.analysis_current_scope, a.kapsamAdi), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.analysis_workload_note), style = MaterialTheme.typography.bodySmall)
            a.plan.forEachIndexed { i, p ->
                Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(stringResource(R.string.analysis_plan_row, tarih(p.gun) + if (i == 0) " / öncesi" else "", p.adet),
                        fontWeight = FontWeight.SemiBold)
                    if (p.adet > 0) {
                        Text(stringResource(R.string.analysis_plan_coverage, p.kapsanan, p.adet, p.kapsamYuzde, p.bilinmeyen),
                            style = MaterialTheme.typography.bodySmall)
                        if (p.kapsanan > 0) Text(stringResource(R.string.analysis_partial_time, dakikaAlt(p.altMs), dakikaUst(p.ustMs)),
                            style = MaterialTheme.typography.bodySmall)
                        if (p.toplamTahminAcik) Text(stringResource(R.string.analysis_total_time,
                            dakikaAlt(requireNotNull(p.toplamAltMs)), dakikaUst(requireNotNull(p.toplamUstMs))), style = MaterialTheme.typography.bodySmall)
                        else Text(stringResource(R.string.analysis_no_total_time), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        AnalizPanel(stringResource(R.string.analysis_groups_title)) {
            Text(stringResource(R.string.analysis_history_note), style = MaterialTheme.typography.bodySmall)
            SayacMetni(a.toplam)
            var acik by rememberSaveable { mutableStateOf(false) }
            OutlinedButton(onClick = { acik = !acik }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .testTag("analysis-groups-toggle")) {
                Text(stringResource(if (acik) R.string.analysis_hide_details else R.string.analysis_show_details))
            }
            val p = LocalHareketPolitikasi.current
            if (p.azaltildi) {
                if (acik) GrupListesi(a, onCalis)
            } else AnimatedVisibility(acik, enter = expandVertically(tween(p.sure(HareketTuru.GIRIS))),
                exit = shrinkVertically(tween(p.sure(HareketTuru.GIRIS)))) { GrupListesi(a, onCalis) }
        }
    }
}

@Composable
fun SonrakiAdimKarti(a: GelisimAnalizi, onCalis: (SoruCalismaFiltresi, Int) -> Unit) {
        AnalizPanel(stringResource(R.string.analysis_next_title)) {
            Text(stringResource(R.string.analysis_current_scope, a.kapsamAdi), style = MaterialTheme.typography.bodySmall)
            val o = a.oneri
            if (o == null) Text(stringResource(R.string.analysis_no_action)) else {
                Text(stringResource(when (o.tur) {
                    OneriTuru.VADESI_GELEN -> R.string.analysis_due_action
                    OneriTuru.YAKIN_GUCLUK -> R.string.analysis_difficulty_action
                    OneriTuru.KUCUK_TUR -> R.string.analysis_small_action
                }, o.uygunSoru, o.guclukSoru), style = MaterialTheme.typography.bodyMedium)
                o.etiket?.let { Text(it, fontWeight = FontWeight.SemiBold) }
                Button(onClick = { onCalis(o.filtre, o.turBoyutu) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .testTag("analysis-action")) { Text(stringResource(R.string.analysis_action_button, o.turBoyutu)) }
                Text(stringResource(R.string.analysis_action_note), style = MaterialTheme.typography.bodySmall)
            }
        }
    }

@Composable
private fun GrupListesi(a: GelisimAnalizi, onCalis: (SoruCalismaFiltresi, Int) -> Unit) {
    var kaynak by rememberSaveable { mutableStateOf(false) }
    var sayfa by rememberSaveable { mutableIntStateOf(0) }
    var secili by remember { mutableStateOf<GrupAnahtari?>(null) }
    val gruplar = if (kaynak) a.kaynaklar else a.konular
    val sonSayfa = ((gruplar.size - 1).coerceAtLeast(0) / 6)
    val guncelSayfa = sayfa.coerceIn(0, sonSayfa)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = { kaynak = !kaynak; sayfa = 0; secili = null }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(stringResource(if (kaynak) R.string.analysis_to_topics else R.string.analysis_to_sources))
        }
        Text(stringResource(if (kaynak) R.string.analysis_sources else R.string.analysis_topics),
            modifier = Modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium)
        if (gruplar.isEmpty()) Text(stringResource(R.string.analysis_groups_empty))
        val ilk = (guncelSayfa * 6).coerceAtMost(gruplar.size)
        gruplar.subList(ilk, (ilk + 6).coerceAtMost(gruplar.size)).forEach { g ->
            OutlinedButton(onClick = { secili = if (secili == g.anahtar) null else g.anahtar },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Column(Modifier.fillMaxWidth()) {
                    Text(grupAdi(g), fontWeight = FontWeight.SemiBold)
                    g.dersAdi?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    Text(stringResource(R.string.analysis_group_counts, g.sayac.soru, g.sayac.tekrar), style = MaterialTheme.typography.bodySmall)
                }
            }
            if (secili == g.anahtar) Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (g.etiketler.size > 1) Text(stringResource(R.string.analysis_captured_names,
                        g.etiketler.take(5).joinToString(" · "), g.etiketler.size), style = MaterialTheme.typography.bodySmall)
                    SayacMetni(g.sayac)
                    Text(stringResource(R.string.analysis_recent_group, g.yakinGuclukSoru, g.sonIkiBagimsizSoru), style = MaterialTheme.typography.bodySmall)
                    val d = g.dersReferansi?.dagilim
                    val p = g.yardimsizProfil
                    if (d != null) Text(stringResource(R.string.analysis_lesson_reference, saniye(d.medyanMs)), style = MaterialTheme.typography.bodySmall)
                    if (p?.dagilim != null) Text(stringResource(R.string.analysis_group_reference,
                        saniye(p.dagilim.medyanMs), p.olcumSayisi, p.farkliSoru, p.farkliGun), style = MaterialTheme.typography.bodySmall)
                    else Text(stringResource(R.string.analysis_group_reference_missing), style = MaterialTheme.typography.bodySmall)
                    DonemMetni(g.donem)
                    if (g.filtre != null && g.uygunSoru > 0) Button(onClick = { onCalis(g.filtre, minOf(5, g.uygunSoru)) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.analysis_group_action, g.uygunSoru))
                    } else Text(stringResource(if (g.anahtar.bilinen) R.string.analysis_no_eligible_group else R.string.analysis_unknown_no_route),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (sonSayfa > 0) {
            Text(stringResource(R.string.analysis_page, guncelSayfa + 1, sonSayfa + 1))
            OutlinedButton(onClick = { sayfa = guncelSayfa - 1; secili = null }, enabled = guncelSayfa > 0,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(stringResource(R.string.analysis_previous)) }
            OutlinedButton(onClick = { sayfa = guncelSayfa + 1; secili = null }, enabled = guncelSayfa < sonSayfa,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(stringResource(R.string.analysis_next)) }
        }
    }
}

@Composable
private fun grupAdi(g: AnalizGrubu): String = if (!g.anahtar.bilinen) stringResource(R.string.analysis_unknown_context)
    else g.etiketler.firstOrNull() ?: stringResource(if (g.anahtar.tur == AnalizTuru.KONU) R.string.analysis_missing_topic else R.string.analysis_missing_source)

@Composable
private fun SayacMetni(s: AnalizSayaci) {
    Text(stringResource(R.string.analysis_group_counts, s.soru, s.tekrar))
    Text(stringResource(R.string.analysis_independent_fraction, s.bagimsiz, s.tekrar), style = MaterialTheme.typography.bodyMedium)
    Text(stringResource(R.string.analysis_measurement_fraction, s.guvenilirSure, s.tekrar, s.kapsamYuzde, s.kismiSure, s.gecersizSure, s.baglamsiz),
        style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun DonemMetni(d: DonemKarsilastirmasi) {
    Text(stringResource(R.string.analysis_period_row, tarih(d.oncekiIlkGun), tarih(d.sonIlkGun - 1), d.onceki.soru, d.onceki.tekrar,
        d.onceki.bagimsiz), style = MaterialTheme.typography.bodySmall)
    Text(stringResource(R.string.analysis_period_row, tarih(d.sonIlkGun), tarih(d.sonGun), d.son.soru, d.son.tekrar,
        d.son.bagimsiz), style = MaterialTheme.typography.bodySmall)
    val e = d.eslesen
    Text(stringResource(R.string.analysis_matched_days, e.onceGun, e.sonraGun), style = MaterialTheme.typography.bodySmall)
    Text(stringResource(if (e.yeterli) R.string.analysis_matched else R.string.analysis_insufficient_comparison,
        e.soru, e.onceBagimsiz, e.sonraBagimsiz), fontWeight = FontWeight.SemiBold)
    if (e.baglamDegisen > 0) Text(stringResource(R.string.analysis_context_excluded, e.baglamDegisen), style = MaterialTheme.typography.bodySmall)
    Text(stringResource(R.string.analysis_comparison_note), style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun AnalizPanel(baslik: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(baslik, modifier = Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}
private val TarihBicimi = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("tr-TR"))
private fun tarih(gun: Int) = LocalDate.ofEpochDay(gun.toLong()).format(TarihBicimi)
private fun dakikaAlt(ms: Double): Int = floor(ms / 60_000).toInt()
private fun dakikaUst(ms: Double): Int = ceil(ms / 60_000).toInt()

private fun saniye(ms: Double): String = java.text.NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR"))
    .apply { maximumFractionDigits = if (ms < 1_000) 3 else 1 }.format(ms / 1_000)

package com.keremtulu.questionnote.ui.seviye

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.gelisim.SureKapsami
import com.keremtulu.questionnote.domain.gelisim.SureProfili
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.keremtulu.questionnote.ui.premium.PremiumTextButton

/** P3 foundation, not the P5 source/topic dashboard or a general exam-speed score. */
@Composable
internal fun SureProfiliKarti(kapsam: SureKapsami, baslikGoster: Boolean = true) {
    var tumu by rememberSaveable { mutableStateOf(false) }
    val renk = MaterialTheme.colorScheme
    val tarih = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("tr-TR"))
    Surface(Modifier.fillMaxWidth().testTag("progress-time-profile"), shape = RoundedCornerShape(24.dp),
        color = renk.surface, border = BorderStroke(1.dp, renk.outlineVariant)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (baslikGoster) Text(stringResource(R.string.profile_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() })
            Text(stringResource(R.string.profile_scope_note), style = MaterialTheme.typography.bodySmall, color = renk.onSurfaceVariant)
            Text(stringResource(R.string.profile_window, tarih.format(LocalDate.ofEpochDay(kapsam.ilkGun.toLong())),
                tarih.format(LocalDate.ofEpochDay(kapsam.sonGun.toLong()))), style = MaterialTheme.typography.labelSmall)
            Text(stringResource(R.string.profile_coverage, kapsam.toplamTekrar, kapsam.tamOlcum, kapsam.kismiOlcum, kapsam.olcumsuz, kapsam.gecersizOlcum),
                style = MaterialTheme.typography.bodySmall)
            if (kapsam.dersler.isEmpty()) Text(stringResource(R.string.profile_collecting_note), style = MaterialTheme.typography.bodyMedium)
            (if (tumu) kapsam.dersler else kapsam.dersler.take(3)).forEach { ders ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(ders.dersAdi, style = MaterialTheme.typography.titleSmall, color = renk.primary)
                    ProfilSatiri(stringResource(R.string.profile_independent), ders.profil.yardimsiz.istenen)
                    ProfilSatiri(stringResource(R.string.profile_workload), ders.profil.isYuku.istenen)
                }
            }
            if (kapsam.dersler.size > 3) PremiumTextButton(onClick = { tumu = !tumu }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(if (tumu) R.string.progress_hide_details else R.string.progress_all_lessons))
            }
            Text(stringResource(R.string.profile_policy_note), style = MaterialTheme.typography.bodySmall, color = renk.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProfilSatiri(baslik: String, profil: SureProfili) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(baslik, style = MaterialTheme.typography.labelLarge)
        val dagilim = profil.dagilim
        if (dagilim == null) {
            Text(stringResource(R.string.profile_collecting), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text(stringResource(R.string.profile_median, saniye(dagilim.medyanMs)), style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.profile_spread, saniye(dagilim.altCeyrekMs), saniye(dagilim.ustCeyrekMs), saniye(dagilim.madMs)),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(stringResource(R.string.profile_sample, profil.olcumSayisi, profil.esik.olcum,
            profil.farkliSoru, profil.esik.soru, profil.farkliGun, profil.esik.gun), style = MaterialTheme.typography.bodySmall)
    }
}

private fun saniye(ms: Double): String = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR"))
    .apply { maximumFractionDigits = if (ms < 1_000) 3 else 1 }.format(ms / 1_000)

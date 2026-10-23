package com.keremtulu.questionnote.ui.seviye

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.prefs.RozetDeposu
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.domain.karne.RozetGirdisi
import com.keremtulu.questionnote.domain.karne.RozetKurallari
import com.keremtulu.questionnote.ui.karne.GizliRozetGorseli
import com.keremtulu.questionnote.ui.karne.RozetGorseli
import com.keremtulu.questionnote.ui.karne.rozetAciklamasi
import com.keremtulu.questionnote.ui.karne.rozetAdi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import com.keremtulu.questionnote.ui.premium.PremiumCard

private val oturumRozetleri = setOf(Rozet.ILK_KARNE, Rozet.KUSURSUZ_TUR, Rozet.ON_BAGIMSIZ,
    Rozet.YIRMI_SORU, Rozet.COZUMSUZ, Rozet.ZINCIR_10, Rozet.KUSURSUZ_20)

@Composable
fun RozetKoleksiyonu(sahip: Set<Rozet>, girdi: RozetGirdisi, depo: RozetDeposu) =
    RozetKoleksiyonu(sahip, girdi, depo::kazanilmaZamani)

@Composable
fun RozetKoleksiyonu(sahip: Set<Rozet>, girdi: RozetGirdisi, kazanilmaZamani: (Rozet) -> Long?) {
    var filtre by rememberSaveable { mutableStateOf("all") }
    var detay by remember { mutableStateOf<Rozet?>(null) }
    val sutunSayisi = if (LocalDensity.current.fontScale >= 1.5f) 1 else 2
    val liste = Rozet.entries.filter { when (filtre) {
        "earned" -> it in sahip
        "locked" -> it !in sahip
        "secret" -> it.gizli
        else -> true
    } }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.level_badges_title, sahip.size, Rozet.entries.size),
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.badge_gallery_note), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("all" to R.string.badge_filter_all, "earned" to R.string.badge_filter_earned,
                "locked" to R.string.badge_filter_locked, "secret" to R.string.badge_filter_secret).forEach { (key, label) ->
                FilterChip(modifier = Modifier.heightIn(min = 48.dp), selected = filtre == key, onClick = { filtre = key }, label = { Text(stringResource(label)) })
            }
        }
        if (liste.isEmpty()) Text(stringResource(R.string.badge_filter_empty), style = MaterialTheme.typography.bodyMedium)
        liste.chunked(sutunSayisi).forEach { satir ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                satir.forEach { rozet ->
                    val kazanildi = rozet in sahip
                    val gizli = rozet.gizli && !kazanildi
                    PremiumCard(
                        onClick = { detay = rozet }, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (gizli) GizliRozetGorseli(Modifier.size(64.dp))
                            else RozetGorseli(rozet, kazanildi, Modifier.size(64.dp))
                            Text(stringResource(if (gizli) R.string.badge_hidden else rozetAdi(rozet)),
                                style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
                            // Unlock instructions belong in the existing detail dialog, not on every tile.
                            if (kazanildi) Text("✓ " + stringResource(R.string.badge_earned),
                                color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                            else if (!gizli && rozet !in oturumRozetleri) {
                                val ilerleme = RozetKurallari.ilerleme(rozet, girdi)
                                Box(Modifier.fillMaxWidth().height(4.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))) {
                                    Box(Modifier.fillMaxWidth(ilerleme.oran).height(4.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)))
                                }
                                Text(stringResource(R.string.badge_progress, ilerleme.mevcut.coerceAtMost(ilerleme.hedef), ilerleme.hedef),
                                    style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                if (sutunSayisi == 2 && satir.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
    detay?.let { rozet ->
        val kazanildi = rozet in sahip
        val gizli = rozet.gizli && !kazanildi
        AlertDialog(
            onDismissRequest = { detay = null },
            icon = { if (gizli) GizliRozetGorseli(Modifier.size(80.dp)) else RozetGorseli(rozet, kazanildi, Modifier.size(80.dp)) },
            title = { Text(stringResource(if (gizli) R.string.badge_hidden else rozetAdi(rozet))) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!gizli) Text(stringResource(R.string.badge_how), fontWeight = FontWeight.Bold)
                    Text(stringResource(if (gizli) R.string.badge_hidden_desc else rozetAciklamasi(rozet)))
                    if (!gizli) {
                        if (kazanildi) {
                            Text("✓ " + stringResource(R.string.badge_earned))
                            kazanilmaZamani(rozet)?.let {
                                val tarih = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                                Text(stringResource(R.string.badge_earned_date, tarih))
                            }
                        } else if (rozet in oturumRozetleri) {
                            Text(stringResource(R.string.badge_session_progress))
                        } else {
                            val p = RozetKurallari.ilerleme(rozet, girdi)
                            Text(stringResource(R.string.badge_progress, p.mevcut, p.hedef))
                        }
                    }
                }
            },
            confirmButton = { PremiumTextButton(onClick = { detay = null }) { Text(stringResource(R.string.common_back)) } },
        )
    }
}

package com.keremtulu.questionnote.ui.seviye

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.seviye.Rutbe
import com.keremtulu.questionnote.domain.seviye.SeviyeDurumu
import com.keremtulu.questionnote.domain.seviye.SeviyeHesabi
import com.keremtulu.questionnote.ui.bilesenler.HedefHalkasi
import com.keremtulu.questionnote.ui.bilesenler.KucukOnay
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import com.keremtulu.questionnote.ui.premium.PremiumCard

@Composable
fun RutbeYolculugu(durum: SeviyeDurumu, baslikGoster: Boolean = true) {
    val kartGenisligi = if (LocalDensity.current.fontScale >= 1.5f) 200.dp else 132.dp
    var detay by remember { mutableStateOf<Rutbe?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (baslikGoster) Text(stringResource(R.string.rank_gallery_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.rank_gallery_note), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        // Küçük yol: 15 rütbenin neresinde olduğumuz, tek bakışta.
        com.keremtulu.questionnote.ui.bilesenler.YolNoktalari(
            oran = ((durum.seviye - 1).toFloat() / (Rutbe.entries.size - 1).coerceAtLeast(1)).coerceIn(0f, 1f),
            modifier = Modifier.width(232.dp).height(14.dp).clearAndSetSemantics { },
            renk = MaterialTheme.colorScheme.surfaceVariant,
            dolguRenk = MaterialTheme.colorScheme.primary,
            noktaSayisi = Rutbe.entries.size.coerceIn(2, 15),
        )
        LazyRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(Rutbe.entries.toList(), key = { it.anahtar }) { rutbe ->
                val aktif = durum.seviye >= rutbe.baslangicSeviye
                val durumMetni = stringResource(if (aktif) R.string.rank_earned else R.string.rank_locked)
                PremiumCard(onClick = { detay = rutbe },
                    modifier = Modifier.width(kartGenisligi)
                        .semantics { stateDescription = durumMetni },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = if (rutbe == durum.rutbe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Image(painterResource(rutbe.gorsel), contentDescription = null,
                            modifier = Modifier.size(80.dp).alpha(if (aktif) 1f else 0.45f))
                        Text(rutbe.ad, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.level_number, rutbe.baslangicSeviye), style = MaterialTheme.typography.labelSmall)
                        if (aktif) {
                            KucukOnay(Modifier.size(14.dp).clearAndSetSemantics { }, MaterialTheme.colorScheme.primary)
                        } else {
                            HedefHalkasi(
                                oran = 0f,
                                modifier = Modifier.size(12.dp).clearAndSetSemantics { },
                                izRenk = MaterialTheme.colorScheme.outlineVariant,
                                dolguRenk = MaterialTheme.colorScheme.primary,
                                kalinlik = 1.5.dp,
                            )
                        }
                    }
                }
            }
        }
    }
    detay?.let { rutbe ->
        val esik = SeviyeHesabi.seviyeEsigi(rutbe.baslangicSeviye)
        AlertDialog(
            onDismissRequest = { detay = null },
            icon = { Image(painterResource(rutbe.gorsel), null, Modifier.size(128.dp)) },
            title = { Text(rutbe.ad) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.rank_unlock, rutbe.baslangicSeviye, esik))
                    if (durum.toplamXp >= esik) Text(stringResource(R.string.rank_earned))
                    else Text(stringResource(R.string.rank_remaining, esik - durum.toplamXp))
                }
            },
            confirmButton = { PremiumTextButton(onClick = { detay = null }) { Text(stringResource(R.string.common_back)) } },
        )
    }
}

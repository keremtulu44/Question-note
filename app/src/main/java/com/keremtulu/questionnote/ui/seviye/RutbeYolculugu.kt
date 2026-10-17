package com.keremtulu.questionnote.ui.seviye

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.seviye.Rutbe
import com.keremtulu.questionnote.domain.seviye.SeviyeDurumu
import com.keremtulu.questionnote.domain.seviye.SeviyeHesabi

@Composable
fun RutbeYolculugu(durum: SeviyeDurumu) {
    var detay by remember { mutableStateOf<Rutbe?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.rank_gallery_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.rank_gallery_note), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(Rutbe.entries.toList(), key = { it.anahtar }) { rutbe ->
                val aktif = durum.seviye >= rutbe.baslangicSeviye
                Card(onClick = { detay = rutbe }, modifier = Modifier.width(132.dp), shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = if (rutbe == durum.rutbe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Image(painterResource(rutbe.gorsel), contentDescription = null,
                            modifier = Modifier.size(80.dp).alpha(if (aktif) 1f else 0.45f))
                        Text(rutbe.ad, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.level_number, rutbe.baslangicSeviye), style = MaterialTheme.typography.labelSmall)
                        Text(if (aktif) "✓" else "◇", color = MaterialTheme.colorScheme.primary)
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
            confirmButton = { TextButton(onClick = { detay = null }) { Text(stringResource(R.string.common_back)) } },
        )
    }
}

package com.keremtulu.questionnote.ui.seviye

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.testTag
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
import com.keremtulu.questionnote.ui.bilesenler.InceOlcek
import com.keremtulu.questionnote.ui.bilesenler.KucukOnay
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import com.keremtulu.questionnote.ui.premium.PremiumCard

@Composable
fun RutbeYolculugu(durum: SeviyeDurumu, baslikGoster: Boolean = true) {
    val kartGenisligi = if (LocalDensity.current.fontScale >= 1.5f) 200.dp else 132.dp
    val listeDurumu = rememberLazyListState(initialFirstVisibleItemIndex = durum.rutbe.ordinal)
    var detay by remember { mutableStateOf<Rutbe?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (baslikGoster) Text(stringResource(R.string.rank_gallery_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.rank_gallery_note), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        // Küçük yol: 15 rütbenin neresinde olduğumuz, tek bakışta.
        com.keremtulu.questionnote.ui.bilesenler.YolNoktalari(
            oran = (durum.rutbe.ordinal.toFloat() / Rutbe.entries.lastIndex.coerceAtLeast(1)).coerceIn(0f, 1f),
            modifier = Modifier.width(232.dp).height(14.dp).clearAndSetSemantics { },
            renk = MaterialTheme.colorScheme.surfaceVariant,
            dolguRenk = MaterialTheme.colorScheme.primary,
            noktaSayisi = Rutbe.entries.size.coerceIn(2, 15),
        )
        LazyRow(state = listeDurumu, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(Rutbe.entries.toList(), key = { it.anahtar }) { rutbe ->
                val aktif = durum.seviye >= rutbe.baslangicSeviye
                val suAnki = rutbe == durum.rutbe
                val durumMetni = stringResource(when {
                    suAnki -> R.string.rank_current
                    aktif -> R.string.rank_earned
                    else -> R.string.rank_locked
                })
                PremiumCard(onClick = { detay = rutbe },
                    modifier = Modifier.width(kartGenisligi)
                        .semantics { stateDescription = durumMetni },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = if (suAnki) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Image(painterResource(rutbe.gorsel), contentDescription = null,
                            modifier = Modifier.size(80.dp).alpha(if (aktif) 1f else 0.45f))
                        Text(rutbe.ad, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.level_number, rutbe.baslangicSeviye), style = MaterialTheme.typography.labelSmall)
                        if (suAnki) {
                            Text(stringResource(R.string.rank_current), style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        } else if (aktif) {
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
        // Galerin altında kalan boşluk yerine tek, ölçülebilir bir sonraki adım:
        // sıradaki rütbe, kalan XP ve ince ölçek. Yeni oyunlaştırma değil, mevcut XP'nin özeti.
        val sonraki = Rutbe.entries.firstOrNull { it.baslangicSeviye > durum.seviye }
        if (sonraki != null) {
            val esik = SeviyeHesabi.seviyeEsigi(sonraki.baslangicSeviye)
            val mevcutEsik = SeviyeHesabi.seviyeEsigi(durum.rutbe.baslangicSeviye)
            val aralik = (esik - mevcutEsik).coerceAtLeast(1)
            val ilerleme = ((durum.toplamXp - mevcutEsik).toFloat() / aralik).coerceIn(0f, 1f)
            PremiumCard(
                modifier = Modifier.fillMaxWidth().testTag("rank-next-card"),
                shape = RoundedCornerShape(22.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Image(
                            painterResource(sonraki.gorsel),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp).alpha(0.55f),
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(R.string.rank_next_label),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = sonraki.ad,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = stringResource(
                                    R.string.rank_next_remaining,
                                    (esik - durum.toplamXp).coerceAtLeast(0),
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    InceOlcek(
                        oran = ilerleme,
                        modifier = Modifier.fillMaxWidth().height(6.dp).clearAndSetSemantics { },
                        izRenk = MaterialTheme.colorScheme.surfaceVariant,
                        dolguRenk = MaterialTheme.colorScheme.primary,
                        bolme = 8,
                        yukseklik = 6.dp,
                    )
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
                    if (rutbe == durum.rutbe) Text(stringResource(R.string.rank_current))
                    else if (durum.toplamXp >= esik) Text(stringResource(R.string.rank_earned))
                    else Text(stringResource(R.string.rank_remaining, esik - durum.toplamXp))
                }
            },
            confirmButton = { PremiumTextButton(onClick = { detay = null }) { Text(stringResource(R.string.common_close)) } },
        )
    }
}

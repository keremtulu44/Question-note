package com.keremtulu.questionnote.ui.deneme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.DenemeKaydi
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.denemeHatirlatmaIndeksi
import com.keremtulu.questionnote.domain.model.kaynakla

fun Soru.denemeKaydi(denemeler: List<DenemeKaydi>): DenemeKaydi? = denemeler.kaynakla(kaynak)

@Composable
fun DenemeDamgaYazi(deneme: DenemeKaydi) {
    Text(
        stringResource(R.string.deneme_stamp, deneme.ad, denemeTarihYazi(deneme.gun)),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
fun DenemeHatirlatmaYazi(soruId: String, deneme: DenemeKaydi) {
    val id = when (denemeHatirlatmaIndeksi(soruId)) {
        0 -> R.string.deneme_recall_0
        1 -> R.string.deneme_recall_1
        2 -> R.string.deneme_recall_2
        else -> R.string.deneme_recall_3
    }
    Text(
        stringResource(id, deneme.ad, denemeTarihYazi(deneme.gun)),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

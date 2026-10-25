package com.keremtulu.questionnote.ui.deneme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.DenemeKaydi
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.denemeHatirlatmaIndeksi
import com.keremtulu.questionnote.domain.model.kaynakla
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumSembolAilesi

fun Soru.denemeKaydi(denemeler: List<DenemeKaydi>): DenemeKaydi? = denemeler.kaynakla(kaynak)

@Composable
fun DenemeDamgaYazi(
    deneme: DenemeKaydi,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = colors.primary.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.24f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "◈",
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary,
                fontWeight = FontWeight.Bold,
                fontFamily = PremiumSembolAilesi,
            )
            Text(
                text = stringResource(R.string.deneme_stamp, deneme.ad, denemeTarihYazi(deneme.gun)),
                style = MaterialTheme.typography.labelMedium,
                color = colors.primary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun DenemeHatirlatmaYazi(
    soruId: String,
    deneme: DenemeKaydi,
    modifier: Modifier = Modifier,
) {
    val id = when (denemeHatirlatmaIndeksi(soruId)) {
        0 -> R.string.deneme_recall_0
        1 -> R.string.deneme_recall_1
        2 -> R.string.deneme_recall_2
        else -> R.string.deneme_recall_3
    }
    val colors = MaterialTheme.colorScheme
    val vurgu = if (isSystemInDarkTheme()) PremiumMarkaRenkler.BakirIsik else PremiumMarkaRenkler.Bakir
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = colors.secondaryContainer.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, vurgu.copy(alpha = 0.28f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "◈",
                style = MaterialTheme.typography.labelMedium,
                color = vurgu,
                fontWeight = FontWeight.Bold,
                fontFamily = PremiumSembolAilesi,
            )
            Text(
                text = stringResource(id, deneme.ad, denemeTarihYazi(deneme.gun)),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSecondaryContainer,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

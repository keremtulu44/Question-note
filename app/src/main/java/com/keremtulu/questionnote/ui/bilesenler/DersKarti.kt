package com.keremtulu.questionnote.ui.bilesenler

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.ui.premium.PremiumSembolAilesi
import com.keremtulu.questionnote.ui.premium.EtkilesimTuru
import com.keremtulu.questionnote.ui.premium.PremiumCard

internal data class DersKartVerisi(
    val ad: String,
    @StringRes val isaret: Int,
    val acikRenk: Color,
    val koyuRenk: Color,
    val soruSayisi: Int = 0,
)

@Composable
internal fun DersKarti(
    veri: DersKartVerisi,
    koyuTema: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vurguRengi = if (koyuTema) veri.koyuRenk else veri.acikRenk
    val renkler = MaterialTheme.colorScheme

    PremiumCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(132.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = renkler.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 5.dp),
        border = BorderStroke(1.dp, renkler.outlineVariant.copy(alpha = 0.8f)),
        interactionTur = EtkilesimTuru.KART_ACILIS,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(vurguRengi.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(veri.isaret),
                        color = vurguRengi,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PremiumSembolAilesi,
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = renkler.surfaceVariant,
                ) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.question_count,
                            veri.soruSayisi,
                            veri.soruSayisi,
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = renkler.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            Text(
                text = veri.ad,
                color = renkler.onSurface,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

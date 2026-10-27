package com.keremtulu.questionnote.ui.kurulum

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.KullaniciRolu
import com.keremtulu.questionnote.ui.bilesenler.SadeKart
import com.keremtulu.questionnote.ui.bilesenler.premium.CamYukseklik
import com.keremtulu.questionnote.ui.premium.EtkilesimTuru
import com.keremtulu.questionnote.ui.premium.PremiumCard
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumSembolAilesi

@Composable
fun RolSecimEkrani(
    onRolSecildi: (KullaniciRolu) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { insets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .consumeWindowInsets(insets),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                com.keremtulu.questionnote.ui.bilesenler.DuzBaslik(
                    baslik = stringResource(R.string.role_select_title),
                    alt = stringResource(R.string.role_select_subtitle),
                )
            }
            item {
                RolSecenekKarti(
                    vurgu = MaterialTheme.colorScheme.primary,
                    baslik = stringResource(R.string.role_student_title),
                    aciklama = stringResource(R.string.role_student_subtitle),
                    etiketler = listOf(
                        stringResource(R.string.role_student_pill_1),
                        stringResource(R.string.role_student_pill_2),
                        stringResource(R.string.role_student_pill_3),
                    ),
                    tag = "rol_secim_ogrenci",
                    ikon = { renk -> OgrenciRolSimgesi(renk) },
                    onClick = { onRolSecildi(KullaniciRolu.OGRENCI) },
                )
            }
            if (com.keremtulu.questionnote.domain.model.UrunKapsami.OGRETMEN_MODU_GORUNUR) item {
                RolSecenekKarti(
                    vurgu = PremiumMarkaRenkler.Bakir,
                    baslik = stringResource(R.string.role_teacher_title),
                    aciklama = stringResource(R.string.role_teacher_subtitle),
                    etiketler = listOf(
                        stringResource(R.string.role_teacher_pill_1),
                        stringResource(R.string.role_teacher_pill_2),
                        stringResource(R.string.role_teacher_pill_3),
                    ),
                    tag = "rol_secim_ogretmen",
                    ikon = { renk -> OgretmenRolSimgesi(renk) },
                    onClick = { onRolSecildi(KullaniciRolu.OGRETMEN) },
                )
            }
            if (com.keremtulu.questionnote.domain.model.UrunKapsami.OGRETMEN_MODU_GORUNUR) item {
                SadeKart(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CamYukseklik.DUSUK,
                ) {
                    Text(
                        text = stringResource(R.string.role_switch_anytime_hint),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RolSecenekKarti(
    vurgu: Color,
    baslik: String,
    aciklama: String,
    etiketler: List<String>,
    tag: String,
    ikon: @Composable (Color) -> Unit,
    onClick: () -> Unit,
) {
    PremiumCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag(tag),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.3.dp, vurgu.copy(alpha = 0.40f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        interactionTur = EtkilesimTuru.KART_ACILIS,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(vurgu.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    ikon(vurgu)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = baslik,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = aciklama,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = vurgu.copy(alpha = 0.14f),
                ) {
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "→",
                            color = vurgu,
                            fontFamily = PremiumSembolAilesi,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                etiketler.forEach { etiket ->
                    Surface(
                        shape = CircleShape,
                        color = vurgu.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, vurgu.copy(alpha = 0.24f)),
                    ) {
                        Text(
                            text = etiket,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = vurgu,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OgrenciRolSimgesi(renk: Color) {
    Canvas(Modifier.size(28.dp)) {
        val kalinlik = 2.2.dp.toPx()
        val firca = Stroke(width = kalinlik, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(
            color = renk,
            topLeft = Offset(size.width * 0.16f, size.height * 0.12f),
            size = Size(size.width * 0.68f, size.height * 0.76f),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = firca,
        )
        drawLine(
            color = renk,
            start = Offset(size.width * 0.32f, size.height * 0.36f),
            end = Offset(size.width * 0.68f, size.height * 0.36f),
            strokeWidth = kalinlik,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = renk.copy(alpha = 0.75f),
            start = Offset(size.width * 0.32f, size.height * 0.54f),
            end = Offset(size.width * 0.62f, size.height * 0.54f),
            strokeWidth = kalinlik,
            cap = StrokeCap.Round,
        )
        val ayrac = Path().apply {
            moveTo(size.width * 0.58f, size.height * 0.12f)
            lineTo(size.width * 0.58f, size.height * 0.30f)
            lineTo(size.width * 0.65f, size.height * 0.25f)
            lineTo(size.width * 0.72f, size.height * 0.30f)
            lineTo(size.width * 0.72f, size.height * 0.12f)
        }
        drawPath(ayrac, color = renk, style = firca)
    }
}

@Composable
private fun OgretmenRolSimgesi(renk: Color) {
    Canvas(Modifier.size(28.dp)) {
        val kalinlik = 2.2.dp.toPx()
        val firca = Stroke(width = kalinlik, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(
            color = renk.copy(alpha = 0.45f),
            topLeft = Offset(size.width * 0.24f, size.height * 0.12f),
            size = Size(size.width * 0.64f, size.height * 0.54f),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = firca,
        )
        drawRoundRect(
            color = renk,
            topLeft = Offset(size.width * 0.12f, size.height * 0.26f),
            size = Size(size.width * 0.68f, size.height * 0.58f),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = firca,
        )
        drawCircle(
            color = renk,
            radius = 2.6.dp.toPx(),
            center = Offset(size.width * 0.46f, size.height * 0.55f),
        )
    }
}

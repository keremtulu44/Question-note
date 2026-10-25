package com.keremtulu.questionnote.ui.bilesenler

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import com.keremtulu.questionnote.ui.premium.PremiumButton
import com.keremtulu.questionnote.ui.premium.PremiumCard
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumSembolAilesi
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import java.io.File
import kotlinx.coroutines.launch

@Composable
fun GorselSecimBolumu(
    images: List<SoruGorselGirdisi>,
    imageStore: LocalSoruGorselDeposu,
    enabled: Boolean,
    onCamera: () -> Unit,
    onPicker: () -> Unit,
    onCrop: (SoruGorselGirdisi) -> Unit,
    onImagesChanged: (List<SoruGorselGirdisi>) -> Unit,
    title: String? = null,
    silinirkenGeciciTemizle: Boolean = true,
) {
    val scope = rememberCoroutineScope()
    var tamEkranDosya by remember { mutableStateOf<File?>(null) }
    val cropLabel = stringResource(R.string.question_form_crop_image)
    val removeImageDescription = stringResource(R.string.question_form_remove_image)
    val moveUpDescription = stringResource(R.string.question_form_image_order_up)
    val moveDownDescription = stringResource(R.string.question_form_image_order_down)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        title?.let { Text(it, style = MaterialTheme.typography.labelLarge) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PremiumButton(
                onClick = onCamera,
                enabled = enabled,
                modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
            ) {
                Text(
                    stringResource(R.string.question_form_take_photo),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            PremiumOutlinedButton(
                onClick = onPicker,
                enabled = enabled,
                modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
            ) {
                Text(
                    stringResource(R.string.question_form_pick_photos),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (images.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Box(Modifier.width(42.dp).height(42.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "+",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            stringResource(R.string.question_form_no_photos),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            stringResource(R.string.question_form_photo_empty_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        } else if (images.size == 1) {
            val image = images.first()
            val file = imageStore.dosyaGoruntuIcin(image)
            PremiumCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(196.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        SoruGorselResmi(
                            file = file,
                            contentDescription = stringResource(R.string.question_image_description),
                            modifier = Modifier.fillMaxWidth().height(196.dp),
                            contentScale = ContentScale.Fit,
                            maxDecodeEdge = 900,
                            cerceve = false,
                            onClick = file?.let { f -> { tamEkranDosya = f } },
                        )
                    }
                    PremiumButton(
                        onClick = { onCrop(image) },
                        enabled = enabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("gorsel_kirp_merkez_0")
                            .semantics { contentDescription = cropLabel },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("✂", fontFamily = PremiumSembolAilesi, style = MaterialTheme.typography.titleMedium)
                            Text(
                                cropLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Text(
                        stringResource(R.string.question_form_crop_center_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (file != null) {
                            PremiumTextButton(onClick = { tamEkranDosya = file }) {
                                Text(stringResource(R.string.question_form_zoom_image))
                            }
                        } else {
                            Box(Modifier)
                        }
                        PremiumTextButton(
                            enabled = enabled,
                            onClick = {
                                if (silinirkenGeciciTemizle && image.gecici) {
                                    scope.launch { imageStore.geciciDosyayiSil(image.dosyaAdi) }
                                }
                                onImagesChanged(emptyList())
                            },
                            modifier = Modifier.semantics { contentDescription = removeImageDescription },
                        ) {
                            Text(
                                removeImageDescription,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(images, key = { it.id }) { image ->
                    val file = imageStore.dosyaGoruntuIcin(image)
                    PremiumCard(
                        modifier = Modifier.width(256.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(156.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)),
                            ) {
                                SoruGorselResmi(
                                    file = file,
                                    contentDescription = stringResource(R.string.question_image_description),
                                    modifier = Modifier.fillMaxWidth().height(156.dp),
                                    contentScale = ContentScale.Fit,
                                    maxDecodeEdge = 640,
                                    cerceve = false,
                                    onClick = file?.let { f -> { tamEkranDosya = f } },
                                )
                            }
                            PremiumButton(
                                onClick = { onCrop(image) },
                                enabled = enabled,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 44.dp)
                                    .testTag("gorsel_kirp_merkez_${image.sira}")
                                    .semantics { contentDescription = cropLabel },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("✂", fontFamily = PremiumSembolAilesi)
                                    Text(cropLabel, fontWeight = FontWeight.Bold)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val solaAktif = enabled && image.sira > 0
                                val sagaAktif = enabled && image.sira < images.lastIndex
                                IconButton(
                                    enabled = solaAktif,
                                    onClick = {
                                        val index = images.indexOfFirst { it.id == image.id }
                                        if (index > 0) onImagesChanged(images.swapped(index, index - 1))
                                    },
                                    modifier = Modifier.semantics { contentDescription = moveUpDescription },
                                ) {
                                    Text(
                                        text = "←",
                                        color = if (solaAktif) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.34f)
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontFamily = PremiumSembolAilesi,
                                    )
                                }
                                IconButton(
                                    enabled = sagaAktif,
                                    onClick = {
                                        val index = images.indexOfFirst { it.id == image.id }
                                        if (index in 0 until images.lastIndex) onImagesChanged(images.swapped(index, index + 1))
                                    },
                                    modifier = Modifier.semantics { contentDescription = moveDownDescription },
                                ) {
                                    Text(
                                        text = "→",
                                        color = if (sagaAktif) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.34f)
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontFamily = PremiumSembolAilesi,
                                    )
                                }
                                IconButton(
                                    enabled = enabled,
                                    onClick = {
                                        if (silinirkenGeciciTemizle && image.gecici) {
                                            scope.launch { imageStore.geciciDosyayiSil(image.dosyaAdi) }
                                        }
                                        onImagesChanged(
                                            images.filterNot { it.id == image.id }
                                                .mapIndexed { order, item -> item.copy(sira = order) },
                                        )
                                    },
                                    modifier = Modifier.semantics { contentDescription = removeImageDescription },
                                ) {
                                    Text(
                                        text = "×",
                                        color = if (enabled) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.34f)
                                        },
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    tamEkranDosya?.let { file ->
        SoruGorselTamEkran(
            file = file,
            contentDescription = stringResource(R.string.question_image_description),
            onDismiss = { tamEkranDosya = null },
        )
    }
}

private fun List<SoruGorselGirdisi>.swapped(first: Int, second: Int): List<SoruGorselGirdisi> =
    toMutableList().also { items ->
        val current = items[first]
        items[first] = items[second]
        items[second] = current
    }.mapIndexed { index, item -> item.copy(sira = index) }

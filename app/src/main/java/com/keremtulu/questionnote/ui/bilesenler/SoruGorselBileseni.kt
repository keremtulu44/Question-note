package com.keremtulu.questionnote.ui.bilesenler

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.keremtulu.questionnote.R
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import com.keremtulu.questionnote.ui.animasyon.rememberShimmer
import kotlin.math.PI
import kotlin.math.cos

enum class GorselHazirligi { YUKLENIYOR, HAZIR, HATA }

@Composable
fun SoruGorselResmi(
    file: File?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    maxDecodeEdge: Int = 1_800,
    onDurum: ((GorselHazirligi?) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    cerceve: Boolean = true,
) {
    val durumBildir by rememberUpdatedState(onDurum)
    var yukleniyor by remember(file?.absolutePath, file?.length(), file?.lastModified(), maxDecodeEdge) {
        mutableStateOf(true)
    }
    val bitmapState = produceState<Bitmap?>(
        initialValue = null,
        file?.absolutePath,
        file?.length(),
        file?.lastModified(),
        maxDecodeEdge,
    ) {
        yukleniyor = true
        value = if (file == null) null else withContext(Dispatchers.IO) {
            cozumleGuvenli(file, maxDecodeEdge)
        }
        yukleniyor = false
    }
    val bitmap = bitmapState.value
    val imageBitmap = remember(bitmap) {
        if (bitmap != null && !bitmap.isRecycled && bitmap.width > 0 && bitmap.height > 0) {
            runCatching { bitmap.asImageBitmap() }.getOrNull()
        } else {
            null
        }
    }
    LaunchedEffect(imageBitmap, yukleniyor) {
        durumBildir?.invoke(
            if (yukleniyor) GorselHazirligi.YUKLENIYOR
            else if (imageBitmap != null) GorselHazirligi.HAZIR
            else GorselHazirligi.HATA,
        )
    }
    DisposableEffect(file?.absolutePath) { onDispose { durumBildir?.invoke(null) } }
    if (imageBitmap == null) {
        // A quiet breathing placeholder while decoding; the status text stays for TalkBack/tests.
        val shimmer = rememberShimmer()
        val parilti = if (yukleniyor) {
            0.62f + 0.30f * (0.5f + 0.5f * cos(shimmer.value * 2f * PI.toFloat()))
        } else {
            0.62f
        }
        Box(
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = parilti.coerceIn(0.35f, 0.92f))),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(if (yukleniyor) R.string.image_loading else if (file == null || !file.exists()) R.string.image_missing else R.string.image_load_error),
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    } else {
        val clickModifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
        val cerceveModu = if (cerceve) {
            Modifier
                .shadow(1.dp, shape = MaterialTheme.shapes.medium, clip = true)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                    MaterialTheme.shapes.medium,
                )
        } else {
            Modifier
        }
        Box(modifier = modifier.then(cerceveModu)) {
            Image(
                bitmap = imageBitmap,
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize().then(clickModifier),
                contentScale = contentScale,
            )
        }
    }
}

@Composable
fun SoruGorselTamEkran(
    file: File,
    contentDescription: String,
    onDismiss: () -> Unit,
) {
    var scale by remember(file.absolutePath) { mutableFloatStateOf(1f) }
    var offsetX by remember(file.absolutePath) { mutableFloatStateOf(0f) }
    var offsetY by remember(file.absolutePath) { mutableFloatStateOf(0f) }
    val transformState = rememberTransformableState { zoom, pan, _ ->
        scale = (scale * zoom).coerceIn(1f, 5f)
        offsetX += pan.x
        offsetY += pan.y
    }
    BackHandler(onBack = onDismiss)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
        ) {
            SoruGorselResmi(
                file = file,
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                    }
                    .transformable(transformState),
                contentScale = ContentScale.Fit,
                maxDecodeEdge = 3_000,
                cerceve = false,
            )
            PremiumTextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            ) {
                Text(text = stringResource(R.string.common_close), color = Color.White)
            }
        }
    }
}

private fun cozumleGuvenli(file: File, maxEdge: Int): Bitmap? {
    if (!file.isFile) return null
    val guvenliHedef = maxEdge.coerceIn(64, 3_000)
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    try {
        BitmapFactory.decodeFile(file.absolutePath, bounds)
    } catch (_: Throwable) {
        return null
    }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    // Hedef kenarı aşmayacak şekilde küçült; eski eşik hedefin 2 katına kadar izin veriyordu.
    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > guvenliHedef) sample *= 2
    try {
        return BitmapFactory.decodeFile(
            file.absolutePath,
            BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            },
        )
    } catch (_: OutOfMemoryError) {
        return try {
            BitmapFactory.decodeFile(
                file.absolutePath,
                BitmapFactory.Options().apply {
                    inSampleSize = (sample * 2).coerceAtLeast(2)
                    inPreferredConfig = Bitmap.Config.RGB_565
                },
            )
        } catch (_: Throwable) {
            null
        }
    } catch (_: Throwable) {
        return null
    }
}

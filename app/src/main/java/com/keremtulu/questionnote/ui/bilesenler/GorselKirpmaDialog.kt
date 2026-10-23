package com.keremtulu.questionnote.ui.bilesenler

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import com.keremtulu.questionnote.R
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min
import kotlin.math.roundToInt
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumButton

/** Normalized crop bounds; these coordinates correspond to the orientation-corrected preview. */
data class GorselKirpmaAlani(
    val sol: Float,
    val ust: Float,
    val sag: Float,
    val alt: Float,
) {
    val genislik: Float get() = sag - sol
    val yukseklik: Float get() = alt - ust

    companion object {
        val Tamami = GorselKirpmaAlani(0f, 0f, 1f, 1f)
    }
}

@Composable
fun GorselKirpmaDialog(
    file: File,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onApply: (GorselKirpmaAlani) -> Unit,
) {
    var previewError by remember(file.absolutePath) { mutableStateOf(false) }
    val bitmap by produceState<Bitmap?>(null, file.absolutePath, file.length(), file.lastModified()) {
        value = withContext(Dispatchers.IO) { decodePreview(file) }
        previewError = value == null
    }
    var selection by remember(file.absolutePath) { mutableStateOf(GorselKirpmaAlani.Tamami) }
    val fullImageSelected = selection == GorselKirpmaAlani.Tamami
    val selectionTooSmall = bitmap?.let {
        !fullImageSelected && (selection.genislik * it.width < MIN_CROP_EDGE || selection.yukseklik * it.height < MIN_CROP_EDGE)
    } == true
    val cropReady = bitmap != null && !selectionTooSmall
    val selectionDescription = stringResource(
        when {
            fullImageSelected -> R.string.question_form_crop_full_selection
            selectionTooSmall -> R.string.question_form_crop_error
            else -> R.string.question_form_crop_area_selection
        },
    )

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        stringResource(R.string.question_form_crop_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    PremiumTextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text(stringResource(R.string.common_close))
                    }
                }
                Text(
                    stringResource(R.string.question_form_crop_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    val image = bitmap
                    when {
                        image == null -> Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            if (previewError) {
                                Text(
                                    stringResource(R.string.question_form_crop_error),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            } else {
                                CircularProgressIndicator()
                                Text(
                                    stringResource(R.string.question_form_crop_loading),
                                    modifier = Modifier.padding(top = 10.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                        else -> {
                            if (image.isRecycled || image.width <= 0 || image.height <= 0 ||
                                maxWidth <= 0.dp || maxHeight <= 0.dp
                            ) {
                                Text(
                                    stringResource(R.string.question_form_crop_error),
                                    color = MaterialTheme.colorScheme.error,
                                )
                                return@BoxWithConstraints
                            }
                            val availableHeight = minOf(maxHeight, 420.dp).coerceAtLeast(1.dp)
                            val ratio = image.width.toFloat() / image.height.coerceAtLeast(1)
                            val previewWidth = minOf(maxWidth, availableHeight * ratio).coerceAtLeast(1.dp)
                            val previewHeight = (previewWidth / ratio).coerceAtMost(availableHeight).coerceAtLeast(1.dp)
                            val selectionText = stringResource(R.string.question_form_crop_canvas_description)
                            CropSelectionCanvas(
                                bitmap = image,
                                selection = selection,
                                onSelectionChange = { selection = it },
                                modifier = Modifier.size(previewWidth, previewHeight)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF17212A))
                                    .semantics {
                                        contentDescription = selectionText
                                        stateDescription = selectionDescription
                                    },
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (isSaving) stringResource(R.string.question_form_crop_saving) else selectionDescription,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    PremiumTextButton(
                        onClick = { selection = GorselKirpmaAlani.Tamami },
                        enabled = !isSaving && bitmap != null,
                    ) { Text(stringResource(R.string.question_form_crop_reset)) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PremiumOutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f).height(52.dp),
                    ) { Text(stringResource(R.string.question_form_cancel)) }
                    PremiumButton(
                        onClick = { onApply(selection) },
                        enabled = cropReady && !isSaving,
                        modifier = Modifier.weight(1f).height(52.dp),
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                        Text(
                            stringResource(
                                if (isSaving) R.string.question_form_crop_working
                                else R.string.question_form_crop_apply,
                            ),
                            modifier = Modifier.padding(start = if (isSaving) 8.dp else 0.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CropSelectionCanvas(
    bitmap: Bitmap,
    selection: GorselKirpmaAlani,
    onSelectionChange: (GorselKirpmaAlani) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestSelection = rememberUpdatedState(selection)
    val latestOnSelectionChange = rememberUpdatedState(onSelectionChange)

    Canvas(
        modifier = modifier.pointerInput(bitmap.width, bitmap.height) {
            var dragStart: Offset? = null
            var selectionBeforeDrag = latestSelection.value
            var currentSelection = latestSelection.value
            detectDragGestures(
                onDragStart = { start ->
                    selectionBeforeDrag = latestSelection.value
                    dragStart = Offset(
                        (start.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f),
                        (start.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f),
                    )
                    val point = requireNotNull(dragStart)
                    currentSelection = GorselKirpmaAlani(point.x, point.y, point.x, point.y)
                    latestOnSelectionChange.value(currentSelection)
                },
                onDrag = { change, _ ->
                    change.consume()
                    val start = dragStart ?: return@detectDragGestures
                    val end = Offset(
                        (change.position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f),
                        (change.position.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f),
                    )
                    currentSelection = selectionFrom(start, end)
                    latestOnSelectionChange.value(currentSelection)
                },
                onDragEnd = {
                    if (currentSelection.genislik * bitmap.width < MIN_CROP_EDGE ||
                        currentSelection.yukseklik * bitmap.height < MIN_CROP_EDGE
                    ) latestOnSelectionChange.value(selectionBeforeDrag)
                    dragStart = null
                },
                onDragCancel = {
                    latestOnSelectionChange.value(selectionBeforeDrag)
                    dragStart = null
                },
            )
        },
    ) {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return@Canvas
        drawImage(
            image = bitmap.asImageBitmap(),
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(size.width.roundToInt().coerceAtLeast(1), size.height.roundToInt().coerceAtLeast(1)),
        )
        val left = selection.sol.coerceIn(0f, 1f) * size.width
        val top = selection.ust.coerceIn(0f, 1f) * size.height
        val right = selection.sag.coerceIn(0f, 1f) * size.width
        val bottom = selection.alt.coerceIn(0f, 1f) * size.height
        val rectWidth = (right - left).coerceAtLeast(0f)
        val rectHeight = (bottom - top).coerceAtLeast(0f)
        val shade = Color.Black.copy(alpha = 0.58f)
        drawRect(shade, topLeft = Offset.Zero, size = Size(size.width, top.coerceAtLeast(0f)))
        drawRect(shade, topLeft = Offset(0f, bottom), size = Size(size.width, (size.height - bottom).coerceAtLeast(0f)))
        drawRect(shade, topLeft = Offset.Zero.copy(y = top), size = Size(left.coerceAtLeast(0f), rectHeight))
        drawRect(shade, topLeft = Offset(right, top), size = Size((size.width - right).coerceAtLeast(0f), rectHeight))
        if (rectWidth > 0f && rectHeight > 0f) {
            val thirdX = rectWidth / 3f
            val thirdY = rectHeight / 3f
            drawLine(Color.White.copy(alpha = 0.34f), Offset(left + thirdX, top), Offset(left + thirdX, bottom), strokeWidth = 1.dp.toPx())
            drawLine(Color.White.copy(alpha = 0.34f), Offset(left + thirdX * 2f, top), Offset(left + thirdX * 2f, bottom), strokeWidth = 1.dp.toPx())
            drawLine(Color.White.copy(alpha = 0.34f), Offset(left, top + thirdY), Offset(right, top + thirdY), strokeWidth = 1.dp.toPx())
            drawLine(Color.White.copy(alpha = 0.34f), Offset(left, top + thirdY * 2f), Offset(right, top + thirdY * 2f), strokeWidth = 1.dp.toPx())
            drawRect(
                color = Color.White,
                topLeft = Offset(left, top),
                size = Size(rectWidth, rectHeight),
                style = Stroke(width = 2.dp.toPx()),
            )
            listOf(Offset(left, top), Offset(right, top), Offset(left, bottom), Offset(right, bottom)).forEach { corner ->
                drawCircle(Color.White, radius = 5.dp.toPx(), center = corner)
                drawCircle(Color(0xFF173A49), radius = 3.dp.toPx(), center = corner)
            }
        }
    }
}

private fun selectionFrom(start: Offset, end: Offset) = GorselKirpmaAlani(
    sol = min(start.x, end.x),
    ust = min(start.y, end.y),
    sag = maxOf(start.x, end.x),
    alt = maxOf(start.y, end.y),
)

private fun decodePreview(file: File): Bitmap? {
    if (!file.isFile) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_PREVIEW_EDGE) sample *= 2
    return BitmapFactory.decodeFile(
        file.absolutePath,
        BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.ARGB_8888 },
    )
}

private const val MIN_CROP_EDGE = 32
private const val MAX_PREVIEW_EDGE = 1_600

package com.keremtulu.questionnote.data.images

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.exifinterface.media.ExifInterface
import com.keremtulu.questionnote.domain.model.GorselTuru
import java.io.File
import java.io.IOException
import java.time.Duration
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LocalSoruGorselDeposuTest {
    @Test
    fun jpegIsOptimizedStagedHashedPromotedAndDeletedSafely() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val store = LocalSoruGorselDeposu(context)
        val source = createTextImage(context.cacheDir, 3_201, 1_200)
        var staged: com.keremtulu.questionnote.domain.model.SoruGorselGirdisi? = null
        var permanentName: String? = null
        try {
            staged = store.dosyadanHazirla(source, GorselTuru.SORU, 0)
            assertEquals("image/jpeg", staged.mimeType)
            assertEquals(3_000, staged.genislik)
            assertEquals(1_124, staged.yukseklik)
            assertTrue(staged.boyut > 0L)
            assertEquals(64, staged.sha256.length)
            assertTrue(store.geciciDosyayiDogrula(staged))

            permanentName = store.kaliciyaKopyala(staged.dosyaAdi)
            assertEquals("img/${staged.id}.jpg", permanentName)
            val stored = staged.copy(dosyaAdi = permanentName!!, gecici = false)
            assertTrue(store.kaliciDosyayiDogrula(stored))
            assertTrue(store.kaliciDosyalariListele().any { it.dosyaAdi == permanentName })
            assertTrue(store.kaliciDosyayiSil(permanentName!!))
            assertFalse(File(context.filesDir, permanentName!!).exists())
            store.geciciDosyayiSil(staged.dosyaAdi)
        } finally {
            source.delete()
            staged?.let { store.geciciDosyayiSil(it.dosyaAdi) }
            permanentName?.let { store.kaliciDosyayiSil(it) }
        }
    }

    @Test
    fun imageExifOrientationIsAppliedBeforeSizeMetadataIsStored() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val store = LocalSoruGorselDeposu(context)
        val source = createTextImage(context.cacheDir, 900, 1_400, Bitmap.CompressFormat.JPEG)
        var staged: com.keremtulu.questionnote.domain.model.SoruGorselGirdisi? = null
        try {
            ExifInterface(source.absolutePath).apply {
                setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString())
                saveAttributes()
            }
            staged = store.dosyadanHazirla(source, GorselTuru.COZUM, 0)
            assertEquals(1_400, staged.genislik)
            assertEquals(900, staged.yukseklik)
        } finally {
            source.delete()
            staged?.let { store.geciciDosyayiSil(it.dosyaAdi) }
        }
    }

    @Test
    fun nonImageInputIsRejectedAndStaleStagesAreOnlyRemovedAfterOneDay() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val store = LocalSoruGorselDeposu(context)
        val invalid = File(context.cacheDir, "not-an-image-${System.nanoTime()}.txt").apply { writeText("not a picture") }
        try {
            var failed = false
            try {
                store.dosyadanHazirla(invalid, GorselTuru.SORU, 0)
            } catch (_: IOException) {
                failed = true
            }
            assertTrue(failed)

            val oneDay = Duration.ofDays(1).toMillis()
            val now = System.currentTimeMillis()
            val staging = File(context.cacheDir, "yanlis-defteri/staging").apply { mkdirs() }
            val stale = File(staging, "test-stale-${System.nanoTime()}.jpg").apply { writeBytes(byteArrayOf(1)) }
            val fresh = File(staging, "test-fresh-${System.nanoTime()}.jpg").apply { writeBytes(byteArrayOf(1)) }
            assertTrue(stale.setLastModified(now - oneDay - 1))
            assertTrue(fresh.setLastModified(now))
            assertTrue(store.eskiGecicileriTemizle(simdi = now, enAzYas = oneDay) >= 1)
            assertFalse(stale.exists())
            assertTrue(fresh.exists())
            fresh.delete()
        } finally {
            invalid.delete()
        }
    }

    private fun createTextImage(
        directory: File,
        width: Int,
        height: Int,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    ): File {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 56f
            strokeWidth = 4f
        }
        canvas.drawText("x² + 3x = 0", 32f, 100f, paint)
        canvas.drawLine(24f, height / 2f, width - 24f, height / 2f, paint)
        val extension = if (format == Bitmap.CompressFormat.JPEG) "jpg" else "png"
        val file = File(directory, "source-${System.nanoTime()}.$extension")
        file.outputStream().use { bitmap.compress(format, 100, it) }
        bitmap.recycle()
        return file
    }
}

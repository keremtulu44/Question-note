package com.keremtulu.questionnote.ui

import android.graphics.Bitmap
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import com.keremtulu.questionnote.ui.seviye.P6BilesenOnizlemesi
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalTestApi::class)
class P6GorselOnizlemeTest {
    @get:Rule val compose = createComposeRule()
    @Test fun lightAction() = kaydet("p6-action-light")
    @Test fun darkAction() = kaydet("p6-action-dark", koyu = true)
    @Test fun analytics() = kaydet("p6-analysis", analiz = true)
    @Test fun largeTextReducedMotion() = kaydet("p6-large", koyu = true, olcek = 2f)
    @Test fun noData() = kaydet("p6-empty", bos = true)
    private fun kaydet(ad: String, koyu: Boolean = false, analiz: Boolean = false, bos: Boolean = false, olcek: Float = 1f) {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = olcek)) {
                P6BilesenOnizlemesi(koyu, analiz, bos)
            }
        }
        compose.waitForIdle()
        val b = compose.onNodeWithTag("p6-preview").captureToImage().asAndroidBitmap()
        assertTrue(b.width >= 320 && b.height >= 600)
        val out = File("build/outputs/p6-previews/$ad.png").apply { requireNotNull(parentFile).mkdirs() }
        out.outputStream().use { assertTrue(b.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        assertTrue(out.length() in 1..1_500_000)
    }
}

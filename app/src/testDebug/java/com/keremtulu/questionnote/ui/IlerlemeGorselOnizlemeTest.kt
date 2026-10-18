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
import com.keremtulu.questionnote.ui.seviye.P1TasarimOnizlemesi
import com.keremtulu.questionnote.ui.seviye.SeviyeDurumuUi
import com.keremtulu.questionnote.ui.seviye.ornekIlerlemeOzeti
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Real Compose rendering with synthetic, explicitly labelled DEBUG data; NOT physical-device evidence. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalTestApi::class)
class IlerlemeGorselOnizlemeTest {
    @get:Rule val compose = createComposeRule()

    @Test fun light() = kaydet("p1-light", SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()))
    @Test fun dark() = kaydet("p1-dark", SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()), koyu = true)
    @Test fun firstDay() = kaydet("p1-empty", SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti(bos = true)))
    @Test fun error() = kaydet("p1-error", SeviyeDurumuUi.Hata)
    @Test fun largeText() = kaydet("p1-large", SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()), koyu = true, olcek = 2f)

    private fun kaydet(ad: String, durum: SeviyeDurumuUi, koyu: Boolean = false, olcek: Float = 1f) {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = olcek)) {
                P1TasarimOnizlemesi(durum, koyu)
            }
        }
        compose.waitForIdle()
        val bitmap = compose.onNodeWithTag("preview-root").captureToImage().asAndroidBitmap()
        assertTrue(bitmap.width >= 320 && bitmap.height >= 600)
        val klasor = File("build/outputs/p1-previews").apply { mkdirs() }
        File(klasor, "$ad.png").outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
    }
}

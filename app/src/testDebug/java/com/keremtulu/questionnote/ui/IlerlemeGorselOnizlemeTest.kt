package com.keremtulu.questionnote.ui

import android.graphics.Bitmap
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import com.keremtulu.questionnote.ui.seviye.IlerlemeTasarimOnizlemesi
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

/** Actual Compose rendering, explicitly synthetic DEBUG data. Not a device screenshot or a golden test. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalTestApi::class)
class IlerlemeGorselOnizlemeTest {
    @get:Rule val compose = createComposeRule()

    @Test fun light() = kaydet("overview-light", SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()))
    @Test fun dark() = kaydet("overview-dark", SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()), koyu = true)
    @Test fun firstDay() = kaydet("overview-empty", SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti(bos = true)))
    @Test fun error() = kaydet("overview-error", SeviyeDurumuUi.Hata)
    @Test fun largeText() = kaydet("overview-large", SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()), koyu = true, olcek = 2f)
    @Test fun detailMenu() = kaydet("details", SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()), bolum = "menu")
    @Test fun comparison() = kaydet("comparison", SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()), bolum = "comparison")
    @Test fun plan() = kaydet("plan", SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()), bolum = "plan")

    private fun kaydet(ad: String, durum: SeviyeDurumuUi, koyu: Boolean = false, olcek: Float = 1f, bolum: String? = null) {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = olcek)) {
                IlerlemeTasarimOnizlemesi(durum, koyu)
            }
        }
        if (bolum != null) {
            compose.onNodeWithTag("progress-list").performScrollToNode(hasTestTag("progress-details"))
            compose.onNodeWithTag("progress-details").performScrollTo()
                .performSemanticsAction(SemanticsActions.OnClick) { it() }
            if (bolum != "menu") {
                compose.onNodeWithTag("progress-detail-list").performScrollToNode(hasTestTag("progress-open-$bolum"))
                compose.onNodeWithTag("progress-open-$bolum").performScrollTo()
                    .performSemanticsAction(SemanticsActions.OnClick) { it() }
            }
        }
        if (olcek >= 1.5f) {
            compose.onNodeWithTag("progress-list").performScrollToNode(hasTestTag("analysis-action"))
            compose.onNodeWithTag("analysis-action").performScrollTo().assertIsDisplayed()
        }
        compose.waitForIdle()
        val bitmap = compose.onNodeWithTag("preview-root").captureToImage().asAndroidBitmap()
        assertTrue(bitmap.width >= 320 && bitmap.height >= 600)
        val klasor = File("build/outputs/progress-previews").apply { mkdirs() }
        File(klasor, "$ad.png").outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
    }
}

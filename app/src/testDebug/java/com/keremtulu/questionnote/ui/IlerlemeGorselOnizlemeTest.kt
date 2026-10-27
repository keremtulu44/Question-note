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
    @Test fun weeklySummary() = kaydet("weekly", SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()), bolum = "weekly")
    @Test fun collection() = kaydet("collection", SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()), bolum = "collection")
    @Test fun ranks() = kaydet("ranks", SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()), bolum = "ranks")

    private fun kaydet(ad: String, durum: SeviyeDurumuUi, koyu: Boolean = false, olcek: Float = 1f, bolum: String? = null) {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = olcek)) {
                IlerlemeTasarimOnizlemesi(durum, koyu)
            }
        }
        if (bolum == "weekly") {
            compose.onNodeWithTag("progress-list").performScrollToNode(hasTestTag("progress-weekly-summary"))
        } else if (bolum != null) {
            compose.onNodeWithTag("progress-list").performScrollToNode(hasTestTag("progress-collection"))
            compose.onNodeWithTag("progress-collection").performScrollTo()
                .performSemanticsAction(SemanticsActions.OnClick) { it() }
            if (bolum == "ranks") {
                compose.onNodeWithTag("progress-open-ranks").performScrollTo()
                    .performSemanticsAction(SemanticsActions.OnClick) { it() }
            }
        }
        if (olcek >= 1.5f) {
            compose.onNodeWithTag("progress-list").performScrollToNode(hasTestTag("analysis-action"))
            compose.onNodeWithTag("analysis-action").performScrollTo().assertIsDisplayed()
            val button = compose.onNodeWithTag("analysis-action").getUnclippedBoundsInRoot()
            val viewport = compose.onNodeWithTag("progress-list").getUnclippedBoundsInRoot()
            val bar = compose.onNodeWithTag("preview-navigation").getUnclippedBoundsInRoot()
            assertTrue("The entire large-text action must be above the bottom bar", button.top >= viewport.top &&
                button.bottom <= viewport.bottom && button.bottom <= bar.top)
            compose.mainClock.advanceTimeByFrame()
        }
        compose.waitForIdle()
        val bitmap = compose.onNodeWithTag("preview-root").captureToImage().asAndroidBitmap()
        assertTrue(bitmap.width >= 320 && bitmap.height >= 600)
        val klasor = File("build/outputs/progress-previews").apply { mkdirs() }
        File(klasor, "$ad.png").outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
    }
}

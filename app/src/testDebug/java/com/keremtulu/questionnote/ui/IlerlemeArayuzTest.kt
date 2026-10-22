package com.keremtulu.questionnote.ui

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.test.*
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.prefs.RozetDeposu
import com.keremtulu.questionnote.domain.karne.KarneSayilari
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.domain.karne.RozetGirdisi
import com.keremtulu.questionnote.ui.karne.RozetKutlamaHost
import com.keremtulu.questionnote.ui.karne.rozetAciklamasi
import com.keremtulu.questionnote.ui.karne.rozetAdi
import com.keremtulu.questionnote.ui.seviye.RozetKoleksiyonu
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class IlerlemeArayuzTest {
    @get:Rule val compose = createComposeRule()
    private val context: Context get() = RuntimeEnvironment.getApplication()
    @Before fun temizle() {
        context.getSharedPreferences("karne_rozetleri", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun bottomTabsStayAboveSystemNavigationAndInsideHorizontalCutoutsWithLargeText() {
        val navHeightPx = 80
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 1.6f)) {
                YanlisDefteriTema {
                    Box(Modifier.requiredSize(360.dp, 480.dp).testTag("root")) {
                        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                            AltMenu(AltSekme.BUGUN, {}, systemInsets = WindowInsets(14, 0, 14, navHeightPx))
                        }
                    }
                }
            }
        }
        val root = compose.onNodeWithTag("root").fetchSemanticsNode().boundsInRoot
        listOf("Bugün", "Dersler", "Filtreli", "İlerleme", "Ayarlar").forEach { text ->
            val tab = (hasText(text) or hasContentDescription(text)) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
            val node = compose.onNode(tab).fetchSemanticsNode().boundsInRoot
            assertTrue("$text overlaps system navigation", node.bottom <= root.bottom - navHeightPx + 1)
            assertTrue("$text is outside left cutout", node.left >= root.left + 14 - 1)
            assertTrue("$text is outside right cutout", node.right <= root.right - 14 + 1)
            assertTrue("$text touch target is too small", node.width >= 48f && node.height >= 48f)
        }
    }

    @Test fun twoHundredPercentNavigationKeepsEveryDestinationAndTheSelectedLabelOnOneLine() {
        val selected = mutableStateOf(AltSekme.SEVIYE)
        compose.setContent { CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
            YanlisDefteriTema {
                Box(Modifier.requiredSize(320.dp, 180.dp)) { AltMenu(selected.value, { selected.value = it }, WindowInsets(0, 0, 0, 0)) }
            }
        } }
        val labels = mapOf(AltSekme.BUGUN to "Bugün", AltSekme.DERSLER to "Dersler", AltSekme.FILTRELI to "Filtreli",
            AltSekme.SEVIYE to "İlerleme", AltSekme.AYARLAR to "Ayarlar")
        compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).assertCountEquals(5)
        labels.forEach { (destination, label) ->
            val matcher = (hasText(label) or hasContentDescription(label)) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
            val node = compose.onNode(matcher).assertIsDisplayed()
            val bounds = node.fetchSemanticsNode().boundsInRoot
            assertTrue(bounds.width >= 48 && bounds.height >= 48)
            node.performSemanticsAction(SemanticsActions.OnClick) { it() }
            assertEquals(destination, selected.value)
            val layouts = mutableListOf<TextLayoutResult>()
            compose.onNodeWithText(label, useUnmergedTree = true)
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertEquals("$label must not break into fragments", 1, layouts.single().lineCount)
        }
    }

    @Test fun secretGalleryAndDetailDoNotLeakRealNamesOrUnlockConditions() {
        val depo = RozetDeposu(context)
        compose.setContent {
            YanlisDefteriTema {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    RozetKoleksiyonu(emptySet(), RozetGirdisi(KarneSayilari(0, 0, 0, 0, 0), 0, 0, emptyList()), depo)
                }
            }
        }
        compose.onNodeWithText("Gizli").performClick()
        compose.onAllNodesWithText("Gizli rozet").assertCountEquals(5)
        Rozet.entries.filter { it.gizli }.forEach {
            compose.onAllNodesWithText(context.getString(rozetAdi(it))).assertCountEquals(0)
            compose.onAllNodesWithText(context.getString(rozetAciklamasi(it))).assertCountEquals(0)
        }
        compose.onAllNodesWithText("Gizli rozet")[0].performClick()
        Rozet.entries.filter { it.gizli }.forEach {
            compose.onAllNodesWithText(context.getString(rozetAdi(it))).assertCountEquals(0)
        }
    }

    @Test fun earnedSecretIsRevealedAndDismissedOnceWithoutLosingOwnership() {
        val depo = RozetDeposu(context)
        depo.kaydet(listOf(Rozet.DONUSUM))
        compose.setContent { YanlisDefteriTema { RozetKutlamaHost(depo, goster = true) } }
        compose.onNodeWithText(context.getString(R.string.badge_celebrate_secret)).assertExists()
        compose.onNodeWithText(context.getString(rozetAdi(Rozet.DONUSUM))).assertExists()
        compose.onNodeWithText(context.getString(rozetAciklamasi(Rozet.DONUSUM))).assertExists()
        compose.onNodeWithText(context.getString(R.string.badge_celebrate_continue)).performScrollTo().performClick()
        compose.waitUntil(5_000) { depo.bekleyenIzle.value.isEmpty() }
        assertEquals(setOf(Rozet.DONUSUM), depo.sahipOlanlar())
        assertTrue(RozetDeposu(context).bekleyenIzle.value.isEmpty())
    }
}

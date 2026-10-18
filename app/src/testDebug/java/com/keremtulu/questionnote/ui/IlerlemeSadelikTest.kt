package com.keremtulu.questionnote.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi
import com.keremtulu.questionnote.ui.seviye.*
import com.keremtulu.questionnote.ui.tema.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Guard the information hierarchy, not just the presence of another analytics card. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
@OptIn(ExperimentalTestApi::class)
class IlerlemeSadelikTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private fun text(id: Int, vararg args: Any) = compose.activity.getString(id, *args)

    @Test fun overviewHasOnePrimaryActionAndNoTechnicalReportBelowIt() {
        val sample = ornekIlerlemeOzeti()
        compose.setContent { YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
            SeviyeSayfasi(SeviyeDurumuUi.ozetten(sample))
        } }
        compose.onNodeWithTag("analysis-action").assertIsDisplayed()
        compose.onAllNodesWithTag("analysis-action").assertCountEquals(1)
        val textSize = compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text)).fetchSemanticsNodes()
            .sumOf { node -> node.config[SemanticsProperties.Text].sumOf { it.text.length } }
        assertTrue("Overview copy budget exceeded: $textSize characters", textSize < 420)
        compose.onNodeWithTag("progress-list").performScrollToNode(hasTestTag("progress-details"))
        compose.onNodeWithTag("progress-collection").assertExists()
        listOf("analysis-section", "progress-time-profile", "progress-xp-breakdown", "progress-lifetime-stats")
            .forEach { compose.onNodeWithTag(it).assertDoesNotExist() }
        listOf(R.string.analysis_workload_note, R.string.analysis_insufficient_comparison,
            R.string.progress_xp_note, R.string.analysis_history_note).forEach {
            compose.onAllNodesWithText(text(it, 40)).assertCountEquals(0)
        }
    }

    @Test fun overviewActionKeepsTheRealFilterAndNeverStartsATourAutomatically() {
        val sample = ornekIlerlemeOzeti()
        var route: Pair<SoruCalismaFiltresi, Int>? = null
        compose.setContent { YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
            SeviyeSayfasi(SeviyeDurumuUi.ozetten(sample), onCalis = { f, n -> route = f to n })
        } }
        assertNull(route)
        compose.onNodeWithTag("analysis-action").assertIsDisplayed().performSemanticsAction(SemanticsActions.OnClick) { it() }
        val proposal = requireNotNull(sample.analiz?.oneri)
        assertEquals(proposal.filtre, route?.first)
        assertEquals(proposal.turBoyutu, route?.second)
    }

    @Test fun detailNavigationComposesOnlyTheChosenSectionAndBackRestoresOverview() {
        val sample = ornekIlerlemeOzeti()
        compose.setContent { YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
            SeviyeSayfasi(SeviyeDurumuUi.ozetten(sample))
        } }
        bolumAc("progress-details", "progress-list")
        compose.onNodeWithTag("analysis-section").assertDoesNotExist()
        bolumAc("progress-open-plan")
        compose.onNodeWithTag("analysis-plan").assertExists()
        compose.onNodeWithTag("analysis-comparison").assertDoesNotExist()
        compose.onNodeWithTag("analysis-groups").assertDoesNotExist()
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithTag("progress-open-plan").assertExists()
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithTag("progress-list").assertExists()
        compose.onNodeWithTag("progress-details").assertIsDisplayed()
        compose.onNodeWithTag("analysis-section").assertDoesNotExist()
    }

    @Test fun selectedDetailSurvivesSavedStateRestorationWithoutOpeningEverything() {
        val sample = ornekIlerlemeOzeti()
        val restoration = StateRestorationTester(compose)
        restoration.setContent { YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
            SeviyeSayfasi(SeviyeDurumuUi.ozetten(sample))
        } }
        bolumAc("progress-details", "progress-list")
        bolumAc("progress-open-comparison")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("analysis-comparison").assertExists()
        compose.onNodeWithTag("analysis-plan").assertDoesNotExist()
        compose.onNodeWithText(text(R.string.progress_comparison_pending)).assertExists()
    }

    @Test fun loadingRefreshKeepsTheChosenDetailInsteadOfReturningToTheOverview() {
        val sample = ornekIlerlemeOzeti()
        val state = mutableStateOf<SeviyeDurumuUi>(SeviyeDurumuUi.ozetten(sample))
        compose.setContent { YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
            SeviyeSayfasi(state.value)
        } }
        bolumAc("progress-details", "progress-list")
        bolumAc("progress-open-plan")
        compose.runOnIdle { state.value = SeviyeDurumuUi.Yukleniyor }
        compose.onNodeWithTag("progress-loading").assertExists()
        compose.runOnIdle { state.value = SeviyeDurumuUi.ozetten(sample) }
        compose.onNodeWithTag("analysis-plan").assertExists()
        compose.onNodeWithTag("progress-hero").assertDoesNotExist()
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithTag("progress-open-plan").assertExists()
    }

    @Test fun realTodayCountUpdatesWithoutClampingItsLabelOrInventingXp() {
        val sample = mutableStateOf(ornekIlerlemeOzeti())
        compose.setContent { YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
            SeviyeSayfasi(SeviyeDurumuUi.ozetten(sample.value))
        } }
        compose.onNodeWithTag("progress-daily-count").assertTextEquals(text(R.string.progress_daily_count, 0, 10))
        val beforeXp = sample.value.durum.toplamXp
        compose.runOnIdle {
            val a = requireNotNull(sample.value.analiz)
            sample.value = sample.value.copy(analiz = a.copy(ritim = a.ritim.map { if (it.gun == a.gun) it.copy(tekrar = 14) else it }))
        }
        compose.onNodeWithTag("progress-daily-count").assertTextEquals(text(R.string.progress_daily_count, 14, 10))
        assertEquals(beforeXp, sample.value.durum.toplamXp)
        compose.onNodeWithText(text(R.string.analysis_workload_note)).assertDoesNotExist()
    }

    @Test fun unknownTodayCountIsNotRenderedAsARealZero() {
        val sample = ornekIlerlemeOzeti().copy(analiz = null,
            rozetGirdisi = ornekIlerlemeOzeti().rozetGirdisi.copy(sonBesGun = emptyList()))
        compose.setContent { YanlisDefteriTema { SeviyeSayfasi(SeviyeDurumuUi.ozetten(sample)) } }
        compose.onNodeWithTag("progress-daily-count").assertDoesNotExist()
        compose.onNodeWithText(text(R.string.progress_go_today)).assertExists()
    }

    @Test fun largeTextKeepsTheSingleActionWithinBoundsAndAtLeast48dp() {
        val sample = ornekIlerlemeOzeti()
        compose.setContent { CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
            IlerlemeTasarimOnizlemesi(SeviyeDurumuUi.ozetten(sample), koyu = true)
        } }
        compose.onNodeWithTag("progress-list").performScrollToNode(hasTestTag("analysis-action"))
        compose.onNodeWithTag("analysis-action").performScrollTo().assertIsDisplayed()
        val button = compose.onNodeWithTag("analysis-action").getUnclippedBoundsInRoot()
        val root = compose.onNodeWithTag("progress-list").getUnclippedBoundsInRoot()
        assertTrue(button.height >= 48.dp && button.width >= 48.dp)
        assertTrue(button.left >= root.left && button.right <= root.right)
        assertTrue(button.top >= root.top && button.bottom <= root.bottom)
        val bar = compose.onNodeWithTag("preview-navigation").getUnclippedBoundsInRoot()
        assertTrue("The bottom navigation must not cover the CTA", button.bottom <= bar.top)
        compose.onAllNodesWithTag("analysis-action").assertCountEquals(1)
        compose.onNodeWithTag("analysis-section").assertDoesNotExist()
    }

    @Test fun collectionIsASeparateDestinationAndSecretConditionsRemainMasked() {
        val sample = ornekIlerlemeOzeti()
        compose.setContent { YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
            SeviyeSayfasi(SeviyeDurumuUi.ozetten(sample))
        } }
        compose.onAllNodesWithText(text(R.string.badge_hidden)).assertCountEquals(0)
        bolumAc("progress-collection", "progress-list")
        compose.onNodeWithTag("progress-open-ranks").assertExists()
        compose.onNodeWithTag("progress-hero").assertDoesNotExist()
        compose.onAllNodesWithText(text(R.string.badge_desc_donusum)).assertCountEquals(0)
        compose.onNodeWithTag("analysis-section").assertDoesNotExist()
    }

    private fun bolumAc(tag: String, list: String = "progress-detail-list") {
        compose.onNodeWithTag(list).performScrollToNode(hasTestTag(tag))
        compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
            .performSemanticsAction(SemanticsActions.OnClick) { it() }
    }
}

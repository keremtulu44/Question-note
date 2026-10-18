package com.keremtulu.questionnote.ui

import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.prefs.*
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi
import com.keremtulu.questionnote.domain.repeat.TekrarMotorModu
import com.keremtulu.questionnote.domain.seviye.Rutbe
import com.keremtulu.questionnote.ui.ayarlar.MotorAyarlariKarti
import com.keremtulu.questionnote.ui.bilesenler.hatirlaYerelGun
import com.keremtulu.questionnote.ui.karne.KutlamaHost
import com.keremtulu.questionnote.ui.seviye.*
import com.keremtulu.questionnote.ui.tema.*
import java.time.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
class P5P6ArayuzTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context: Context get() = RuntimeEnvironment.getApplication()
    @Before fun temizle() {
        listOf("karne_rozetleri", "rutbe_kutlamalari", "tekrar_politikasi").forEach { context.getSharedPreferences(it, 0).edit().clear().commit() }
    }
    @Test fun recommendationPreservesDueScopeAndSmallRoundAtTwoHundredPercent() {
        val a = p6AnalizOrnegi()
        var route: Pair<SoruCalismaFiltresi, Int>? = null
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
                YanlisDefteriTema(koyuTema = true, hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        SonrakiAdimKarti(a) { f, n -> route = f to n }
                    }
                }
            }
        }
        compose.onNodeWithTag("analysis-action").performScrollTo().assertIsDisplayed().performClick()
        val bounds = compose.onNodeWithTag("analysis-action").fetchSemanticsNode().boundsInRoot
        assertTrue(bounds.width >= 48 && bounds.height >= 48)
        assertTrue(requireNotNull(route).first.vadesiGelen)
        assertEquals(5, route?.second)
    }
    @Test fun noEligibleDataShowsNoInventedAction() {
        compose.setContent { YanlisDefteriTema { SonrakiAdimKarti(p6AnalizOrnegi(true)) { _, _ -> fail("No route allowed") } } }
        compose.onNodeWithTag("analysis-action").assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.analysis_no_action)).assertExists()
    }
    @Test fun progressiveTopicDetailsUseCapturedNameAndCurrentScopedFilter() {
        val a = p6AnalizOrnegi()
        var route: Pair<SoruCalismaFiltresi, Int>? = null
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) { GelisimAnaliziKarti(a) { f, n -> route = f to n } }
            }
        }
        compose.onNodeWithText("Kesirler").assertDoesNotExist()
        compose.onNodeWithTag("analysis-groups-toggle").performScrollTo().performClick()
        compose.onNodeWithText("Kesirler").performScrollTo().performClick()
        val g = a.konular.single { it.anahtar.deger == "kesirler" }
        val label = context.getString(R.string.analysis_group_action, g.uygunSoru)
        compose.onNodeWithText(label).performScrollTo().performClick()
        assertEquals("m", route?.first?.dersId); assertEquals("kesirler", route?.first?.konuId); assertEquals(5, route?.second)
    }
    @Test fun zeroCoveredQuestionsNeverRenderZeroMinutesAsAnEstimate() {
        val a = p6AnalizOrnegi().let { it.copy(plan = it.plan.map { p -> p.copy(kapsanan = 0, bilinmeyen = p.adet, altMs = 0.0, ustMs = 0.0) }) }
        compose.setContent {
            YanlisDefteriTema {
                Column(Modifier.verticalScroll(rememberScrollState())) { GelisimAnaliziKarti(a) { _, _ -> } }
            }
        }
        compose.onAllNodesWithText("dk", substring = true).assertCountEquals(0)
        compose.onAllNodesWithText(context.getString(R.string.analysis_no_total_time)).assertCountEquals(7)
    }
    @Test fun insufficientComparisonShowsActualDaysRatherThanJustTheMinimumThreshold() {
        val a = p6AnalizOrnegi()
        assertEquals(40, a.donem.eslesen.soru)
        assertEquals(2, a.donem.eslesen.onceGun); assertEquals(2, a.donem.eslesen.sonraGun)
        assertFalse(a.donem.eslesen.yeterli)
        compose.setContent {
            YanlisDefteriTema {
                Column(Modifier.verticalScroll(rememberScrollState())) { GelisimAnaliziKarti(a) { _, _ -> } }
            }
        }
        compose.onNodeWithText(context.getString(R.string.analysis_matched_days, 2, 2)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.analysis_insufficient_comparison, 40)).assertExists()
    }
    @Test fun activeModeIsDisabledAndOffChoiceIsPersistedWithoutAnActivationBypass() {
        val depo = TekrarPolitikasiDeposu(context)
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                Column(Modifier.verticalScroll(rememberScrollState())) { MotorAyarlariKarti(depo) }
            }
        }
        compose.onNodeWithText(context.getString(R.string.motor_active)).performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText(context.getString(R.string.motor_off)).performScrollTo().performClick()
        compose.waitUntil(5_000) { depo.oku().uygulanan == TekrarMotorModu.KAPALI }
        assertEquals(TekrarMotorModu.KAPALI, TekrarPolitikasiDeposu(context).oku().uygulanan)
    }
    @Test fun badgeAndRankNeverOverlapAndAcknowledgementIsDurable() {
        val badges = RozetDeposu(context); val ranks = RutbeDeposu(context)
        badges.kaydet(listOf(Rozet.ILK_KARNE))
        ranks.guncelle(Rutbe.BASLANGIC, 10, false); ranks.guncelle(Rutbe.KARARLI, 10, true)
        compose.setContent { YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) { KutlamaHost(badges, ranks, true) } }
        compose.onNodeWithTag("rank-celebration").assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.badge_celebrate_continue)).performScrollTo().performClick()
        compose.waitUntil(5_000) { badges.bekleyenIzle.value.isEmpty() }
        compose.onNodeWithTag("rank-celebration").assertExists()
        compose.onNodeWithText(context.getString(R.string.badge_celebrate_continue)).assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.rank_celebration_continue)).performScrollTo().performClick()
        compose.waitUntil(5_000) { ranks.bekleyenIzle.value == null }
        assertNull(RutbeDeposu(context).bekleyenIzle.value)
        assertEquals(setOf(Rozet.ILK_KARNE), badges.sahipOlanlar())
    }
    @Test fun wallClockAndZoneBroadcastsRefreshTheDisplayedDayWithoutPerSecondPolling() {
        val clock = TestClock(Instant.parse("2026-10-18T00:30:00Z"))
        compose.setContent { Text(hatirlaYerelGun(clock).toString()) }
        compose.onNodeWithText("2026-10-18").assertExists()
        compose.runOnIdle { clock.zoneId = ZoneId.of("America/Los_Angeles"); context.sendBroadcast(Intent(Intent.ACTION_TIMEZONE_CHANGED)) }
        compose.waitUntil(5_000) { compose.onAllNodesWithText("2026-10-17").fetchSemanticsNodes().isNotEmpty() }
        compose.runOnIdle { clock.now = clock.now.plusSeconds(86_400); context.sendBroadcast(Intent(Intent.ACTION_TIME_CHANGED)) }
        compose.waitUntil(5_000) { compose.onAllNodesWithText("2026-10-18").fetchSemanticsNodes().isNotEmpty() }
    }
    @Test fun resumeAfterClockChangeRefreshesTheDayWithoutNeedingABroadcast() {
        val clock = TestClock(Instant.parse("2026-10-18T12:00:00Z"))
        compose.setContent { Text(hatirlaYerelGun(clock).toString()) }
        compose.onNodeWithText("2026-10-18").assertExists()
        compose.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        clock.now = clock.now.plusSeconds(86_400)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        compose.onNodeWithText("2026-10-19").assertExists()
    }
    @Test fun nextMidnightRefreshesTheDayWithoutAResumeOrDateBroadcast() {
        val clock = TestClock(Instant.parse("2026-10-18T23:59:59Z"))
        compose.setContent { Text(hatirlaYerelGun(clock).toString()) }
        compose.onNodeWithText("2026-10-18").assertExists()
        compose.runOnIdle { clock.now = clock.now.plusSeconds(2) }
        compose.mainClock.advanceTimeBy(2_000)
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(2))
        compose.onNodeWithText("2026-10-19").assertExists()
    }
    private class TestClock(var now: Instant, var zoneId: ZoneId = ZoneOffset.UTC) : Clock() {
        override fun getZone(): ZoneId = zoneId
        override fun withZone(zone: ZoneId): Clock = Clock.fixed(now, zone)
        override fun instant(): Instant = now
    }
}

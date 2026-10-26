package com.keremtulu.questionnote.ui

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.prefs.RozetDeposu
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.domain.seviye.SeviyeHesabi
import com.keremtulu.questionnote.ui.karne.KarneRozetBolumu
import com.keremtulu.questionnote.ui.seviye.SeviyeDurumuUi
import com.keremtulu.questionnote.ui.seviye.SeviyeSayfasi
import com.keremtulu.questionnote.ui.seviye.ornekIlerlemeOzeti
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.HareketTuru
import com.keremtulu.questionnote.ui.tema.LocalHareketPolitikasi
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema
import androidx.compose.material3.Text
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.text.NumberFormat
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
class PremiumIlerlemeTest {
    @get:Rule val compose = createComposeRule()
    private val context: Context get() = RuntimeEnvironment.getApplication()
    @Before fun temizle() {
        context.getSharedPreferences("karne_rozetleri", Context.MODE_PRIVATE).edit().clear().commit()
        Settings.Global.putFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    }

    @Test fun emptyStateHasAnActualActionAndNoMadeUpMeasurements() {
        var ekleme = 0
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                SeviyeSayfasi(SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti(bos = true)), onSoruEkle = { ekleme++ })
            }
        }
        val etiket = context.getString(R.string.progress_add_question)
        compose.onNodeWithTag("progress-list").performScrollToNode(hasText(etiket))
        compose.onNodeWithText(etiket).assertIsDisplayed().performClick()
        assertEquals(1, ekleme)
        compose.onAllNodesWithText("%68").assertCountEquals(0)
        compose.onAllNodesWithText("64").assertCountEquals(0)
    }

    @Test fun loadingStateDoesNotPresentZeroAsAnActualMeasurement() {
        compose.setContent { YanlisDefteriTema { SeviyeSayfasi(SeviyeDurumuUi.Yukleniyor) } }
        compose.onNodeWithTag("progress-loading").assertExists()
        compose.onNodeWithTag("progress-xp-total").assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.progress_loading)).assertExists()
    }

    @Test fun errorStateOffersRetryInDarkTheme() {
        var deneme = 0
        compose.setContent {
            YanlisDefteriTema(koyuTema = true, hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                SeviyeSayfasi(SeviyeDurumuUi.Hata, onYenile = { deneme++ })
            }
        }
        compose.onNodeWithTag("progress-error").assertExists()
        compose.onNodeWithText(context.getString(R.string.level_retry)).performClick()
        assertEquals(1, deneme)
    }

    @Test fun openingWithMotionEnabledDoesNotPretendTheExistingXpWasJustEarned() {
        val ozet = ornekIlerlemeOzeti()
        compose.mainClock.autoAdvance = false
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Normal) {
                SeviyeSayfasi(SeviyeDurumuUi.ozetten(ozet))
            }
        }
        compose.onNodeWithTag("progress-list").performScrollToNode(hasTestTag("progress-hero"))
        val etiket = context.getString(R.string.progress_total_xp,
            NumberFormat.getIntegerInstance(Locale.forLanguageTag("tr-TR")).format(ozet.durum.toplamXp))
        compose.onNodeWithTag("progress-xp-total").assertTextEquals(etiket)
        compose.mainClock.autoAdvance = true
    }

    @Test fun initialXpIsRealAndReducedMotionAppliesUpdatesWithoutCountingFromZero() {
        val ozet = mutableStateOf(ornekIlerlemeOzeti())
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                SeviyeSayfasi(SeviyeDurumuUi.ozetten(ozet.value))
            }
        }
        compose.onNodeWithTag("progress-list").performScrollToNode(hasTestTag("progress-hero"))
        fun etiket(xp: Int) = context.getString(R.string.progress_total_xp,
            NumberFormat.getIntegerInstance(Locale.forLanguageTag("tr-TR")).format(xp))
        compose.onNodeWithTag("progress-xp-total").assertTextEquals(etiket(ozet.value.durum.toplamXp))
        val baslangic = compose.mainClock.currentTime
        compose.runOnIdle { ozet.value = ozet.value.copy(durum = SeviyeHesabi.durum(ozet.value.durum.toplamXp + 10)) }
        // Let Compose dispatch snapshot/apply/measure work. A frozen v1 test clock can assert
        // stale semantics even for plain Text; bound virtual time instead of skipping redraws.
        compose.onNodeWithTag("progress-xp-total").assertTextEquals(etiket(ozet.value.durum.toplamXp))
        val ilerleme = compose.onNodeWithTag("progress-xp-bar").fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(ozet.value.durum.oran, ilerleme.current, 0.0001f)
        assertTrue("Reduced motion must not wait for the 500 ms XP animation",
            compose.mainClock.currentTime - baslangic <= 100)
    }

    @Test fun aRealXpChangeUsesOneShortFiniteAnimationWhenMotionIsEnabled() {
        val ozet = mutableStateOf(ornekIlerlemeOzeti())
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Normal) {
                SeviyeSayfasi(SeviyeDurumuUi.ozetten(ozet.value))
            }
        }
        compose.onNodeWithTag("progress-list").performScrollToNode(hasTestTag("progress-hero"))
        compose.waitForIdle()
        val baslangic = compose.mainClock.currentTime
        val hedef = ozet.value.durum.toplamXp + 100
        compose.runOnIdle { ozet.value = ozet.value.copy(durum = SeviyeHesabi.durum(hedef)) }
        compose.onNodeWithTag("progress-xp-total").assertTextEquals(context.getString(R.string.progress_total_xp,
            NumberFormat.getIntegerInstance(Locale.forLanguageTag("tr-TR")).format(hedef)))
        assertTrue(compose.mainClock.currentTime - baslangic in 400..800)
    }

    @Test fun twoHundredPercentTextKeepsTheActionUsableWithoutTechnicalDetails() {
        var bugun = 0
        val ozet = ornekIlerlemeOzeti(bos = true).copy(soruSayisi = 3, analiz = null)
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 2f)) {
                YanlisDefteriTema(koyuTema = true, hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                    Box(Modifier.fillMaxSize()) {
                        SeviyeSayfasi(SeviyeDurumuUi.ozetten(ozet), onBugun = { bugun++ })
                    }
                }
            }
        }
        val etiket = context.getString(R.string.progress_go_today)
        compose.onNodeWithTag("progress-list").performScrollToNode(hasText(etiket))
        compose.onNodeWithText(etiket).assertIsDisplayed().performClick()
        val alan = compose.onNodeWithText(etiket).fetchSemanticsNode().boundsInRoot
        assertTrue(alan.width >= 48 && alan.height >= 48)
        assertEquals(1, bugun)
        compose.onNodeWithText(context.getString(R.string.progress_xp_note)).assertDoesNotExist()
        compose.onNodeWithTag("progress-details").assertDoesNotExist()
    }

    @Test fun collapsedCollectionDoesNotExposeHiddenNamesAndBadgeDenominatorIsThirty() {
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                SeviyeSayfasi(SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()))
            }
        }
        compose.onNodeWithText(context.getString(R.string.progress_xp_note)).assertDoesNotExist()
        compose.onNodeWithTag("progress-list").performScrollToNode(hasTestTag("progress-collection"))
        compose.onNodeWithTag("progress-collection").assertExists()
        compose.onAllNodesWithText("Gizli rozet").assertCountEquals(0)
        compose.onAllNodesWithText(context.getString(R.string.badge_donusum)).assertCountEquals(0)
    }

    private fun detayAc(tag: String, liste: String) {
        compose.onNodeWithTag(liste).performScrollToNode(hasTestTag(tag))
        compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
            .performSemanticsAction(SemanticsActions.OnClick) { it() }
    }

    @Test fun reportOwnershipCountTracksAConcurrentLifetimeAward() {
        val depo = RozetDeposu(context)
        depo.kaydet(listOf(Rozet.ILK_KARNE))
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                KarneRozetBolumu(listOf(Rozet.ILK_KARNE), depo)
            }
        }
        compose.onNodeWithText(context.getString(R.string.karne_badge_total, 1, 30)).assertExists()
        compose.runOnIdle { depo.kaydet(listOf(Rozet.ELLI_TEKRAR)) }
        compose.onNodeWithText(context.getString(R.string.karne_badge_total, 2, 30)).assertExists()
        assertEquals(2, depo.sahipOlanlar().size)
    }

    @Test fun systemMotionSettingIsObservedWhileTheScreenIsOpen() {
        compose.setContent {
            YanlisDefteriTema {
                Text(if (LocalHareketPolitikasi.current.azaltildi) "motion-off" else "motion-on")
            }
        }
        compose.onNodeWithText("motion-on").assertExists()
        compose.runOnIdle {
            Settings.Global.putFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f)
            context.contentResolver.notifyChange(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), null)
        }
        compose.onNodeWithText("motion-off").assertExists()
    }

    @Test fun motionPolicyHasFiniteDurationsAndSafeInvalidScaleFallbacks() {
        HareketTuru.entries.forEach {
            assertEquals(0, HareketPolitikasi.Azaltilmis.sure(it))
            assertTrue(HareketPolitikasi.Normal.sure(it) in 120..1_200)
        }
        listOf(0f, -1f, Float.NaN, Float.POSITIVE_INFINITY).forEach {
            assertTrue(HareketPolitikasi.olcekten(it).azaltildi)
        }
        assertFalse(HareketPolitikasi.olcekten(0.5f).azaltildi)
    }
}

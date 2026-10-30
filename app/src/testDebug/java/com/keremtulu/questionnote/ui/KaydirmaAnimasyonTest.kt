package com.keremtulu.questionnote.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.ui.animasyon.animateItemInsertion
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Newly composed lazy items must not start transparent, even with normal motion enabled. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
class KaydirmaAnimasyonTest {
    @get:Rule val compose = createComposeRule()

    @Test fun recycledItemsAreVisibleOnTheirFirstCompositionWithoutWaitingForAnimation() {
        val frames = mutableListOf<Pair<Int, Float>>()
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Normal) {
                LazyColumn(Modifier.height(300.dp).testTag("scroll-test")) {
                    itemsIndexed((0 until 100).toList()) { index, number ->
                        val alpha = animateItemInsertion(index).value
                        SideEffect { frames.add(index to alpha) }
                        Text("Soru $number", Modifier.height(50.dp).graphicsLayer { this.alpha = alpha })
                    }
                }
            }
        }
        compose.onNodeWithTag("scroll-test").performScrollToIndex(80)
        compose.onNodeWithText("Soru 80").assertIsDisplayed()
        compose.runOnIdle {
            val newFrames = frames.filter { it.first >= 80 }
            assertTrue("Scrolling should compose new items", newFrames.isNotEmpty())
            assertTrue("A new item was hidden until an entrance animation finished", newFrames.all { it.second == 1f })
        }
    }
}

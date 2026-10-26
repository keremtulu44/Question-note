package com.keremtulu.questionnote.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.domain.model.*
import com.keremtulu.questionnote.ui.ogretmen.OgretmenGrupKarti
import com.keremtulu.questionnote.ui.ogretmen.OgretmenSoruKarti
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.PremiumYanlisDefteriTema
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w320dp-h800dp-mdpi")
class OgretmenGorselDuzenTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = RuntimeEnvironment.getApplication()

    @Test fun largeTextQuestionActionsWrapAndKeepTheirTouchTargetsInsideTheViewport() {
        val soru = OgretmenSorusu(id = "buyuk", grupId = "g", baslik = "Uzun başlıklı bir geometri sorusu",
            cevap = "Gizli cevap", kimSordu = "Uzun isimli öğrenci", gorseller = emptyList(),
            olusturma = 0L, guncelleme = 0L)
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
                PremiumYanlisDefteriTema(koyuTema = true, hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                    Column(Modifier.fillMaxSize().testTag("viewport").verticalScroll(rememberScrollState()).padding(16.dp)) {
                        OgretmenSoruKarti(soru, "Uzun isimli çalışma grubu", "Matematik", "Analitik geometri",
                            true, LocalSoruGorselDeposu(context), {}, {}, {}, {}, {})
                    }
                }
            }
        }
        listOf("ogretmen_cozuldu_degistir_buyuk", "ogretmen_baska_gruba_ekle_buyuk", "ogretmen_paylas_buyuk")
            .forEach { tag ->
                compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
                val bounds = compose.onNodeWithTag(tag).getUnclippedBoundsInRoot()
                val viewport = compose.onNodeWithTag("viewport").getUnclippedBoundsInRoot()
                assertTrue("$tag must stay inside the viewport", bounds.left >= viewport.left && bounds.right <= viewport.right)
                assertTrue("$tag must have a 48dp touch target", bounds.bottom - bounds.top >= 48.dp)
            }
    }

    @Test fun groupWorkloadIsVisibleWithoutOpeningAnotherScreen() {
        compose.setContent {
            PremiumYanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                OgretmenGrupKarti(OgretmenGrubu("g", "8A"), 9, 2, false, {},
                    isOzeti = OgretmenIsOzeti(3, 2, 0, 4))
            }
        }
        compose.onNodeWithText(context.getString(R.string.teacher_work_due, 2)).assertExists()
        compose.onNodeWithText(context.getString(R.string.teacher_work_overdue, 3)).assertExists()
        compose.onNodeWithText(context.getString(R.string.teacher_work_unplanned, 4)).assertExists()
    }
}

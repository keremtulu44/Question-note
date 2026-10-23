package com.keremtulu.questionnote.ui.calisma

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.OgrenmeDurumu
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.SoruGorseli
import com.keremtulu.questionnote.domain.model.SoruPlanDurumu
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
class CozumTahtasiKullanimTest {
    @get:Rule val compose = createComposeRule()
    private val context: Context get() = RuntimeEnvironment.getApplication()
    private val temporaryImages = mutableListOf<File>()

    @After
    fun temizle() {
        temporaryImages.forEach { it.delete() }
    }

    @Test
    fun topQuestionIconOpensScrollableQuestionPanelAndReturnsToBoard() {
        val depot = LocalSoruGorselDeposu(context)
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                CozumTahtasiDialog(
                    soru = soru(),
                    gorselDeposu = depot,
                    visible = true,
                    onDismiss = {},
                )
            }
        }

        compose.onNodeWithTag("solution-whiteboard-canvas").assertExists()
        compose.onNodeWithTag("whiteboard-question-preview-toggle").performClick()
        compose.onNodeWithTag("whiteboard-question-sheet").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.whiteboard_question_missing)).assertExists()

        compose.onNodeWithTag("whiteboard-question-preview-toggle").performClick()
        compose.onNodeWithTag("whiteboard-question-sheet").assertDoesNotExist()
        compose.onNodeWithTag("solution-whiteboard-canvas").assertExists()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w852dp-h393dp-mdpi")
    fun landscapeShowsTheQuestionBesideTheWorkingBoard() {
        val depot = LocalSoruGorselDeposu(context)
        val image = questionImage(sira = 0, width = 480, height = 320)
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                CozumTahtasiDialog(
                    soru = soru(listOf(image)),
                    gorselDeposu = depot,
                    visible = true,
                    onDismiss = {},
                )
            }
        }

        compose.onNodeWithTag("whiteboard-question-image-${image.id}").assertIsDisplayed()
        compose.onNodeWithTag("solution-whiteboard-canvas").assertIsDisplayed()
    }

    @Test
    fun choosingAQuestionPageStartsOverlayDrawingAndKeepsEachSurfaceUndoHistorySeparate() {
        val depot = LocalSoruGorselDeposu(context)
        val first = questionImage(sira = 0, width = 320, height = 480)
        val second = questionImage(sira = 1, width = 480, height = 320)
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                CozumTahtasiDialog(
                    soru = soru(listOf(first, second)),
                    gorselDeposu = depot,
                    visible = true,
                    onDismiss = {},
                )
            }
        }

        compose.onNodeWithTag("whiteboard-question-preview-toggle").performClick()
        compose.onNodeWithTag("whiteboard-question-pages")
            .performScrollToNode(hasTestTag("whiteboard-question-image-${second.id}"))
        compose.onNodeWithTag("whiteboard-question-image-${second.id}").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithTag("whiteboard-question-sheet").fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithText(context.getString(R.string.whiteboard_surface_question)).assertExists()

        val canvas = compose.onNodeWithTag("solution-whiteboard-canvas")
        val noMarks = context.getString(R.string.whiteboard_no_marks)
        val oneMark = context.getString(R.string.whiteboard_mark_count, 1)
        assertEquals(noMarks, canvas.fetchSemanticsNode().config[SemanticsProperties.StateDescription])

        compose.onNodeWithTag("whiteboard-surface-toggle").performClick()
        val bounds = canvas.fetchSemanticsNode().boundsInRoot
        canvas.performTouchInput {
            down(Offset(bounds.width * 0.2f, bounds.height * 0.2f))
            moveTo(Offset(bounds.width * 0.75f, bounds.height * 0.75f), durationMillis = 120)
            up()
        }
        compose.waitUntil(5_000) {
            canvas.fetchSemanticsNode().config[SemanticsProperties.StateDescription] == oneMark
        }

        compose.onNodeWithTag("whiteboard-surface-toggle").performClick()
        compose.waitUntil(5_000) {
            canvas.fetchSemanticsNode().config[SemanticsProperties.StateDescription] == noMarks
        }
        compose.onNodeWithTag("whiteboard-surface-toggle").performClick()
        compose.waitUntil(5_000) {
            canvas.fetchSemanticsNode().config[SemanticsProperties.StateDescription] == oneMark
        }
    }

    private fun soru(gorseller: List<SoruGorseli> = emptyList()) = Soru(
        id = "question-1",
        dersId = "lesson-1",
        konuId = null,
        soruNotu = "",
        cozumNotu = null,
        kaynak = null,
        yanlisNedeni = null,
        favori = false,
        planDurumu = SoruPlanDurumu.AKTIF,
        ogrenmeDurumu = OgrenmeDurumu.YENI,
        sonTekrarGun = null,
        sonrakiTekrarGun = null,
        olusturma = 1L,
        guncelleme = 1L,
        gorseller = gorseller,
    )

    private fun questionImage(sira: Int, width: Int, height: Int): SoruGorseli {
        val id = UUID.randomUUID().toString()
        val name = "img/$id.jpg"
        val file = File(context.filesDir, name).apply {
            parentFile?.mkdirs()
            createNewFile()
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try {
            FileOutputStream(file).use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output))
            }
        } finally {
            bitmap.recycle()
        }
        temporaryImages += file
        return SoruGorseli(
            id = id,
            soruId = "question-1",
            tur = GorselTuru.SORU,
            sira = sira,
            dosyaAdi = name,
            mimeType = "image/jpeg",
            genislik = width,
            yukseklik = height,
            boyut = file.length(),
            sha256 = "test-$id",
        )
    }
}

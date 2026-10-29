package com.keremtulu.questionnote.ui.calisma

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
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
        compose.onNodeWithTag("whiteboard-style-dock").assertIsDisplayed()
        compose.onNodeWithTag("whiteboard-actions").assertIsDisplayed()
    }

    @Test
    fun questionSurfaceCanZoomAndShowsTheFiveQuestionSessionPosition() {
        val depot = LocalSoruGorselDeposu(context)
        val image = questionImage(sira = 0, width = 320, height = 480)
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                CozumTahtasiDialog(
                    soru = soru(listOf(image)),
                    gorselDeposu = depot,
                    visible = true,
                    oturumSira = 3,
                    oturumToplam = 5,
                    onDismiss = {},
                )
            }
        }

        compose.onNodeWithText(context.getString(R.string.whiteboard_session_progress, 3, 5)).assertIsDisplayed()
        val surfaceToggle = compose.onNodeWithTag("whiteboard-surface-toggle")
        assertEquals(
            context.getString(R.string.whiteboard_surface_blank),
            surfaceToggle.fetchSemanticsNode().config[SemanticsProperties.StateDescription],
        )
        assertEquals(
            listOf(context.getString(R.string.whiteboard_surface_switch_to_question)),
            surfaceToggle.fetchSemanticsNode().config[SemanticsProperties.ContentDescription],
        )

        surfaceToggle.performClick()
        assertEquals(
            context.getString(R.string.whiteboard_surface_question),
            surfaceToggle.fetchSemanticsNode().config[SemanticsProperties.StateDescription],
        )
        assertEquals(
            listOf(context.getString(R.string.whiteboard_surface_switch_to_blank)),
            surfaceToggle.fetchSemanticsNode().config[SemanticsProperties.ContentDescription],
        )
        compose.onNodeWithTag("whiteboard-zoom-controls").assertIsDisplayed()
        val zoomLevel = compose.onNodeWithTag("whiteboard-zoom-level")
        zoomLevel.assertTextEquals("100%")
        compose.onNodeWithTag("whiteboard-zoom-in").performClick()
        zoomLevel.assertTextEquals("125%")
        compose.onNodeWithTag("whiteboard-zoom-out").performClick()
        zoomLevel.assertTextEquals("100%")
        compose.onNodeWithTag("whiteboard-zoom-in").performClick()
        zoomLevel.assertTextEquals("125%")
        compose.onNodeWithTag("whiteboard-pan-mode").performClick()
        compose.onNodeWithText(context.getString(R.string.whiteboard_draw_mode)).assertExists()
        compose.onNodeWithTag("whiteboard-pan-mode").performClick()
        compose.onNodeWithTag("whiteboard-zoom-reset").performClick()
        zoomLevel.assertTextEquals("100%")
    }

    @Test
    fun sessionWhiteboardKeepsSeparateDrawingsForEachQuestion() {
        val depot = LocalSoruGorselDeposu(context)
        val image = questionImage(sira = 0, width = 320, height = 480)
        val firstQuestion = soru(listOf(image))
        val secondQuestion = firstQuestion.copy(id = "question-2")
        val currentQuestion = mutableStateOf(firstQuestion)
        val sessionMemory = TahtaOturumCizimBellegi()
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                CozumTahtasiDialog(
                    soru = currentQuestion.value,
                    gorselDeposu = depot,
                    visible = true,
                    oturumCizimBellegi = sessionMemory,
                    onDismiss = {},
                )
            }
        }

        val canvas = compose.onNodeWithTag("solution-whiteboard-canvas")
        val noMarks = context.getString(R.string.whiteboard_no_marks)
        val oneMark = context.getString(R.string.whiteboard_mark_count, 1)
        fun drawMark() {
            val bounds = canvas.fetchSemanticsNode().boundsInRoot
            canvas.performTouchInput {
                down(Offset(bounds.width * 0.20f, bounds.height * 0.20f))
                moveTo(Offset(bounds.width * 0.75f, bounds.height * 0.75f), delayMillis = 120)
                up()
            }
        }

        drawMark()
        compose.waitUntil(5_000) {
            canvas.fetchSemanticsNode().config[SemanticsProperties.StateDescription] == oneMark
        }
        compose.runOnIdle { currentQuestion.value = secondQuestion }
        compose.waitUntil(5_000) {
            canvas.fetchSemanticsNode().config[SemanticsProperties.StateDescription] == noMarks
        }
        drawMark()
        compose.waitUntil(5_000) {
            canvas.fetchSemanticsNode().config[SemanticsProperties.StateDescription] == oneMark
        }
        compose.runOnIdle { currentQuestion.value = firstQuestion }
        compose.waitUntil(5_000) {
            canvas.fetchSemanticsNode().config[SemanticsProperties.StateDescription] == oneMark
        }
        compose.runOnIdle { currentQuestion.value = secondQuestion }
        compose.waitUntil(5_000) {
            canvas.fetchSemanticsNode().config[SemanticsProperties.StateDescription] == oneMark
        }
    }

    @Test
    fun tekDokunusNoktaBirakirVeSuruklemeTekVurusUretir() {
        val depot = LocalSoruGorselDeposu(context)
        val memory = TahtaOturumCizimBellegi()
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                CozumTahtasiDialog(
                    soru = soru(),
                    gorselDeposu = depot,
                    visible = true,
                    oturumCizimBellegi = memory,
                    onDismiss = {},
                )
            }
        }
        val canvas = compose.onNodeWithTag("solution-whiteboard-canvas")
        val oneMark = context.getString(R.string.whiteboard_mark_count, 1)
        val ikiMark = context.getString(R.string.whiteboard_mark_count, 2)

        // Yalnız dokunup kaldırma: eskiden hiç iz bırakmıyordu ("nokta koyulmuyor").
        val bounds = canvas.fetchSemanticsNode().boundsInRoot
        canvas.performTouchInput {
            down(Offset(bounds.width * 0.30f, bounds.height * 0.30f))
            up()
        }
        compose.waitUntil(5_000) {
            canvas.fetchSemanticsNode().config[SemanticsProperties.StateDescription] == oneMark
        }

        // Sürükleme tek vuruş olarak eklenir ve ilk vuruş korunur.
        canvas.performTouchInput {
            down(Offset(bounds.width * 0.20f, bounds.height * 0.60f))
            moveTo(Offset(bounds.width * 0.40f, bounds.height * 0.55f), delayMillis = 16)
            moveTo(Offset(bounds.width * 0.60f, bounds.height * 0.50f), delayMillis = 16)
            moveTo(Offset(bounds.width * 0.80f, bounds.height * 0.45f), delayMillis = 16)
            up()
        }
        compose.waitUntil(5_000) {
            canvas.fetchSemanticsNode().config[SemanticsProperties.StateDescription] == ikiMark
        }
    }

    @Test
    fun kalinlikKaydirmalariBirIleOnArasindaAdimAdimAyarlanir() {
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
        val kaydirici = compose.onNodeWithTag("whiteboard-width-slider")
        kaydirici.assertExists()
        // Açılışta varsayılan kalınlık 4 dp.
        compose.onNodeWithTag("whiteboard-width-value")
            .assertTextEquals(context.getString(R.string.whiteboard_width_value, 4))

        kaydirici.performSemanticsAction(SemanticsActions.SetProgress) { it(10f) }
        compose.onNodeWithTag("whiteboard-width-value")
            .assertTextEquals(context.getString(R.string.whiteboard_width_value, 10))

        // Sınır dışı değerler 1–10 aralığına kırpılır.
        kaydirici.performSemanticsAction(SemanticsActions.SetProgress) { it(99f) }
        compose.onNodeWithTag("whiteboard-width-value")
            .assertTextEquals(context.getString(R.string.whiteboard_width_value, 10))
        kaydirici.performSemanticsAction(SemanticsActions.SetProgress) { it(-5f) }
        compose.onNodeWithTag("whiteboard-width-value")
            .assertTextEquals(context.getString(R.string.whiteboard_width_value, 1))
    }

    @Test
    fun drawingDockHasClearToolSelectionAndGroupedStyleControls() {
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

        compose.onNodeWithTag("whiteboard-tool-palette").assertIsDisplayed()
        compose.onNodeWithTag("whiteboard-style-dock").assertIsDisplayed()
        compose.onNodeWithTag("whiteboard-actions").assertIsDisplayed()
        compose.onNodeWithTag("whiteboard-tool-kalem").assertIsSelected()
        compose.onNodeWithTag("whiteboard-tool-silgi").performClick().assertIsSelected()
        compose.onNodeWithTag("whiteboard-tool-kalem").assertIsNotSelected()
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
            moveTo(Offset(bounds.width * 0.75f, bounds.height * 0.75f), delayMillis = 120)
            up()
        }
        compose.waitUntil(5_000) {
            canvas.fetchSemanticsNode().config[SemanticsProperties.StateDescription] == oneMark
        }

        compose.onNodeWithTag("whiteboard-surface-toggle").performClick()
        compose.waitUntil(5_000) {
            canvas.fetchSemanticsNode().config[SemanticsProperties.StateDescription] == noMarks
        }
        compose.onNodeWithTag("whiteboard-zoom-in").performClick()
        val zoomedBounds = canvas.fetchSemanticsNode().boundsInRoot
        canvas.performTouchInput {
            down(Offset(zoomedBounds.width * 0.35f, zoomedBounds.height * 0.40f))
            moveTo(Offset(zoomedBounds.width * 0.62f, zoomedBounds.height * 0.58f), delayMillis = 120)
            up()
        }
        compose.waitUntil(5_000) {
            canvas.fetchSemanticsNode().config[SemanticsProperties.StateDescription] == oneMark
        }
        compose.onNodeWithTag("whiteboard-surface-toggle").performClick()
        compose.waitUntil(5_000) {
            canvas.fetchSemanticsNode().config[SemanticsProperties.StateDescription] == oneMark
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

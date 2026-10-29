package com.keremtulu.questionnote.ui.rehber

import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.fetchSemanticsNode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.PremiumYanlisDefteriTema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Rehberin gerçek kullanıcı akışı: bölüm geçişi, atlama, deneme sayacı ve mini tahta.
 *
 * Hareket azaltılmış politikayla çalışır: sayfa geçişi animasyonsuzdur, bu yüzden test
 * animasyon süresine bağlı kalmaz. Mini tahta dokunuşu da burada doğrulanır; çizim
 * gerçek tahtanın sözleşmesini (tek dokunuş = iz) kullanır.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
class RehberEkraniTest {
    @get:Rule val compose = createComposeRule()
    private val context: Context get() = RuntimeEnvironment.getApplication()

    private var bitirilen = 0
    private var kapatilan = 0

    private fun kur() {
        compose.setContent {
            PremiumYanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                RehberEkrani(
                    onKapat = { kapatilan++ },
                    onBitir = { bitirilen++ },
                    onSoruEkle = {},
                    onDenemeler = {},
                    onYedek = {},
                    onBugun = {},
                )
            }
        }
    }

    private fun ilerle(sayi: Int) {
        repeat(sayi) {
            compose.onNodeWithTag("rehber-ileri").performClick()
            compose.waitForIdle()
        }
    }

    private fun metin(etiket: String): String =
        compose.onNodeWithTag(etiket).fetchSemanticsNode()
            .config[SemanticsProperties.Text].joinToString("") { it.text }

    @Test
    fun ilkBolumIlerlemeyleAcilirVeIleriGeriCalisir() {
        kur()
        assertEquals(context.getString(R.string.rehber_progress, 1, 7), metin("rehber-ilerleme"))
        compose.onNodeWithTag("rehber-bolum-tanitim").assertIsDisplayed()

        ilerle(1)
        assertEquals(context.getString(R.string.rehber_progress, 2, 7), metin("rehber-ilerleme"))
        compose.onNodeWithTag("rehber-bolum-kayit").assertIsDisplayed()

        compose.onNodeWithTag("rehber-geri").performClick()
        compose.waitForIdle()
        assertEquals(context.getString(R.string.rehber_progress, 1, 7), metin("rehber-ilerleme"))
        assertEquals(0, bitirilen)
    }

    @Test
    fun atlaRehberiBitirirAmaKapatmaz() {
        kur()
        compose.onNodeWithTag("rehber-atla").performClick()
        compose.waitForIdle()
        assertEquals(1, bitirilen)
        assertEquals(0, kapatilan)
    }

    @Test
    fun sonBolumdeBitirDugmesiGorunur() {
        kur()
        ilerle(6)
        assertEquals(context.getString(R.string.rehber_progress, 7, 7), metin("rehber-ilerleme"))
        compose.onNodeWithTag("rehber-ileri", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithTag("rehber-bitir").performClick()
        compose.waitForIdle()
        assertEquals(1, bitirilen)
    }

    @Test
    fun denemeSayaciNetiDegistirir() {
        kur()
        ilerle(3)
        compose.onNodeWithTag("rehber-bolum-deneme").assertIsDisplayed()
        val onceki = metin("rehber-net")
        compose.onNodeWithContentDescription(context.getString(R.string.rehber_4_artir, context.getString(R.string.rehber_4_correct)))
            .performClick()
        compose.waitForIdle()
        val sonraki = metin("rehber-net")
        // Doğru sayısını artırmak neti büyütür; rehber canlı hesap yapar (sabit metin değil).
        assertNotEquals(onceki, sonraki)
    }

    @Test
    fun tekrarSecenegiSonrakiTarihiDegistirir() {
        kur()
        ilerle(1)
        val onceki = metin("rehber-sonraki-tekrar")
        compose.onNodeWithTag("rehber-sonuc-rahat").performClick()
        compose.waitForIdle()
        val sonraki = metin("rehber-sonraki-tekrar")
        assertNotEquals(onceki, sonraki)
    }

    @Test
    fun miniTahtadaTekDokunusIzBirakir() {
        kur()
        ilerle(4)
        compose.onNodeWithTag("rehber-bolum-cozum").assertIsDisplayed()
        val bosMetin = context.getString(R.string.rehber_5_empty)
        assertEquals(bosMetin, compose.onNodeWithTag("rehber-mini-tahta").fetchSemanticsNode()
            .config[SemanticsProperties.StateDescription])

        compose.onNodeWithTag("rehber-mini-tahta").performTouchInput { click(center) }
        compose.waitForIdle()
        val dolu = compose.onNodeWithTag("rehber-mini-tahta").fetchSemanticsNode()
            .config[SemanticsProperties.StateDescription]
        assertEquals(context.getString(R.string.rehber_5_mark_count, 1), dolu)
    }

    @Test
    @Config(sdk = [35], qualifiers = "w320dp-h640dp-mdpi")
    fun darEkrandaKontrollerKirpilmaz() {
        kur()
        compose.onNodeWithTag("rehber-ileri").assertIsDisplayed()
        compose.onNodeWithTag("rehber-ilerleme").assertIsDisplayed()
        ilerle(1)
        compose.onNodeWithTag("rehber-geri").assertIsDisplayed()
    }
}

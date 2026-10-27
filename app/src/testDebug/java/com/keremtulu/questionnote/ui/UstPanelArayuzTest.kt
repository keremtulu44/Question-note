package com.keremtulu.questionnote.ui

import android.content.Context
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.ilerleme.IlerlemeHizmeti
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.TekrarEntity
import com.keremtulu.questionnote.data.prefs.SabitGunlukHedefDeposu
import com.keremtulu.questionnote.ui.panel.UstPanel
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Üst panelin sözleşmesi: kalıcı gün/seri/hedef özeti, tek anlamlı geçiş ve oyunlaştırma yok.
 * Bu bir Robolectric arayüz regresyonudur; fiziksel cihaz kabulü değildir.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
@OptIn(ExperimentalTestApi::class)
class UstPanelArayuzTest {
    @get:Rule val compose = createComposeRule()
    private val context: Context = RuntimeEnvironment.getApplication()
    private val db = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
        .addCallback(VarsayilanDersleriEkle()).allowMainThreadQueries().build()


    @Test
    fun hedefSeriVeTarihGorunurTekDokunuslaIlerlemeyeGider() {
        val bugun = LocalDate.now()
        val gun = bugun.toEpochDay().toInt()
        runBlocking {
            db.tekrarDao().ekle(
                TekrarEntity(
                    id = "ust-panel-tekrar",
                    soruId = "ust-panel-soru",
                    zaman = System.currentTimeMillis(),
                    gun = gun,
                    sonuc = "RAHAT",
                ),
            )
        }
        val hedefDeposu = SabitGunlukHedefDeposu(10)
        val ilerleme = IlerlemeHizmeti(db)
        var ilerlemeAcildi = false
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                UstPanel(
                    ilerlemeHizmeti = ilerleme,
                    gunlukHedefDeposu = hedefDeposu,
                    onIlerleme = { ilerlemeAcildi = true },
                )
            }
        }

        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("1 / 10").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("ust-panel").assertIsDisplayed()
        compose.onNodeWithText("1 / 10").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.today_streak, 1)).assertIsDisplayed()
        val tarih = bugun.format(DateTimeFormatter.ofPattern("d MMMM EEEE", Locale.forLanguageTag("tr-TR")))
        compose.onNodeWithText(tarih).assertIsDisplayed()
        // Oyunlaştırma yok: panelde XP/para/enerji dilinde tek bir metin bulunmaz.
        listOf("XP", "para", "enerji", "elmas").forEach { kelime ->
            compose.onAllNodesWithText(kelime, substring = true, ignoreCase = true).assertCountEquals(0)
        }

        val hedefDugmesi = compose.onNodeWithTag("ust-panel-hedef")
        val olcu = hedefDugmesi.getUnclippedBoundsInRoot()
        assertTrue("Hedef düğmesi en az 48dp yüksek olmalı", olcu.bottom - olcu.top >= 48.dp)
        hedefDugmesi.performSemanticsAction(SemanticsActions.OnClick) { it() }
        assertTrue("Hedef düğmesi İlerleme sekmesine gitmeli", ilerlemeAcildi)
    }

    @Test
    fun buyukYazidaHedefDugmesiHalaOkunurVeDokunulabilir() {
        val hedefDeposu = SabitGunlukHedefDeposu(15)
        val ilerleme = IlerlemeHizmeti(db)
        compose.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalDensity provides
                    androidx.compose.ui.unit.Density(1f, fontScale = 2f),
            ) {
                YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                    UstPanel(
                        ilerlemeHizmeti = ilerleme,
                        gunlukHedefDeposu = hedefDeposu,
                        onIlerleme = {},
                    )
                }
            }
        }
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("0 / 15").fetchSemanticsNodes().isNotEmpty()
        }
        val olcu = compose.onNodeWithTag("ust-panel-hedef").getUnclippedBoundsInRoot()
        assertTrue("2× yazıda hedef düğmesi 48dp altına düşmemeli", (olcu.bottom - olcu.top).value >= 48f)
        assertTrue("2× yazıda hedef düğmesi okunabilir genişlikte kalmalı", (olcu.right - olcu.left).value >= 88f)
    }
}

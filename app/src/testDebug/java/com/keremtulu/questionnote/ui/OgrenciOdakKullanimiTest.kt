package com.keremtulu.questionnote.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.room.Room
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.prefs.OdakOturumuDeposu
import com.keremtulu.questionnote.data.repository.RoomDersRepository
import com.keremtulu.questionnote.data.repository.RoomSoruRepository
import com.keremtulu.questionnote.domain.calisma.OdakOturumu
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import com.keremtulu.questionnote.ui.calisma.OdakOturumuEkrani
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
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
@Config(sdk = [35], qualifiers = "w320dp-h720dp-mdpi")
class OgrenciOdakKullanimiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context: Context get() = RuntimeEnvironment.getApplication()
    private lateinit var db: YanlisDefteriDatabase
    private lateinit var depo: OdakOturumuDeposu
    private lateinit var images: LocalSoruGorselDeposu
    private lateinit var repo: RoomSoruRepository
    private lateinit var ders: Ders
    private lateinit var soru: Soru

    @Before fun kur() {
        context.getSharedPreferences("odak_oturumu", Context.MODE_PRIVATE).edit().clear().commit()
        db = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle()).allowMainThreadQueries().build()
        images = LocalSoruGorselDeposu(context)
        repo = RoomSoruRepository(db, images)
        depo = OdakOturumuDeposu(context)
        depo.kaydet(OdakOturumu("focus-ui", listOf("q", "q2", "q3"), 1_000))
        val degerler = runBlocking {
            val lesson = RoomDersRepository(db).dersleriIzle(false).first().first()
            db.soruDao().ekle(SoruEntity("q", lesson.id, soruNotu = "Öncelikli soru metni", olusturma = 1, guncelleme = 1))
            lesson to requireNotNull(repo.soruGetir("q"))
        }
        ders = degerler.first
        soru = degerler.second
    }

    @After fun kapat() {
        db.close()
        depo.temizle()
    }

    @Test fun soruOnceGorunurVeSecilmemisSonucIcinPlanAciklamasiGosterilmez() {
        val bitti = AtomicBoolean(false)
        compose.setContent {
            YanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                OdakOturumuEkrani(
                    soru, ders, repo, images, "focus-ui", depo,
                    sira = 2, toplam = 3, sonuclar = emptyList(), cozumOnceAcildi = false,
                    onCozumAcildi = {}, onGeri = {}, onSonuc = { sonuc, acildi ->
                        assertEquals(TekrarSonucu.ZORLANDIM, sonuc)
                        assertTrue(!acildi)
                        bitti.set(true)
                    }, onSonaBirak = {}, onAtla = {},
                )
            }
        }
        compose.onNodeWithTag("focus-question-note").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.focus_image_hint)).assertDoesNotExist()
        val ilerleme = compose.onNodeWithTag("focus-session-progress").fetchSemanticsNode()
            .config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(0f, ilerleme.current, 0.001f) // skipped questions are not recorded as completed
        val degerlendir = context.getString(R.string.focus_assess)
        compose.waitUntil(5_000) { compose.onAllNodes(androidx.compose.ui.test.hasText(degerlendir) and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(degerlendir).performClick()
        compose.waitUntil(5_000) { compose.onAllNodes(hasTestTag("focus-result-ZORLANDIM") and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(context.getString(R.string.focus_plan_details)).assertDoesNotExist()
        compose.onNodeWithTag("focus-list").performScrollToNode(hasTestTag("focus-result-ZORLANDIM"))
        compose.onNodeWithTag("focus-result-ZORLANDIM").performClick().assertIsSelected()
        compose.onNodeWithText(context.getString(R.string.focus_plan_details)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.focus_save)).performClick()
        compose.waitUntil(5_000) { bitti.get() }
        assertEquals(TekrarSonucu.ZORLANDIM.name, runBlocking { db.tekrarDao().tumunuGetir().single().sonuc })
    }

    @Test fun buyukYaziVeKoyuTemadaSonucSecenekleriErisilebilirKalir() {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
                YanlisDefteriTema(koyuTema = true, hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                    OdakOturumuEkrani(
                        soru, ders, repo, images, "focus-ui", depo,
                        sira = 1, toplam = 3, sonuclar = emptyList(), cozumOnceAcildi = false,
                        onCozumAcildi = {}, onGeri = {}, onSonuc = { _, _ -> }, onSonaBirak = {}, onAtla = {},
                    )
                }
            }
        }
        val degerlendir = context.getString(R.string.focus_assess)
        compose.waitUntil(5_000) { compose.onAllNodes(androidx.compose.ui.test.hasText(degerlendir) and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(degerlendir).performClick()
        compose.waitUntil(5_000) { compose.onAllNodes(hasTestTag("focus-result-ZORLANDIM") and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("focus-list").performScrollToNode(hasTestTag("focus-result-ZORLANDIM"))
        val kart = compose.onNodeWithTag("focus-result-ZORLANDIM")
        kart.performScrollTo().assertIsDisplayed()
        val kartSiniri = kart.getUnclippedBoundsInRoot()
        val yaziSiniri = compose.onNodeWithText(context.getString(R.string.review_result_struggled), useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        assertTrue(yaziSiniri.left >= kartSiniri.left && yaziSiniri.right <= kartSiniri.right)
        assertTrue(yaziSiniri.top >= kartSiniri.top && yaziSiniri.bottom <= kartSiniri.bottom)
    }
}

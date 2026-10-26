package com.keremtulu.questionnote.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.semantics.SemanticsActions
import androidx.room.Room
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.prefs.OgretmenDeposu
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.OgretmenSorusu
import com.keremtulu.questionnote.ui.ogretmen.OgretmenAnaEkrani
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.PremiumYanlisDefteriTema
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * T3 arayüz kanıtı: arama listeyi daraltır, kart paylaşımı yerel bir chooser açar ve grup silme
 * snackbar'daki "Geri al" ile geri döner (10 saniyelik pencere).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
class OgretmenT3ArayuzTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private lateinit var context: Context
    private lateinit var database: YanlisDefteriDatabase
    private lateinit var gorselDeposu: LocalSoruGorselDeposu
    private lateinit var ogretmenDeposu: OgretmenDeposu

    @Before
    fun kur() {
        context = RuntimeEnvironment.getApplication()
        File(context.filesDir, "ogretmen_calisma_alani.json").delete()
        File(context.filesDir, "ogretmen-img").deleteRecursively()
        File(context.filesDir, "img").deleteRecursively()
        database = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle())
            .allowMainThreadQueries()
            .build()
        gorselDeposu = LocalSoruGorselDeposu(context)
        ogretmenDeposu = OgretmenDeposu(context, gorselDeposu, database)
    }

    @After
    fun kapat() {
        database.close()
    }

    private fun testGorseli(): File {
        val file = File(context.cacheDir, "t3-ui-${System.nanoTime()}.png")
        val bmp = Bitmap.createBitmap(200, 150, Bitmap.Config.ARGB_8888)
        Canvas(bmp).apply { drawColor(Color.rgb(232, 240, 250)) }
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bmp.recycle()
        return file
    }

    private fun soruEkle(grupId: String, baslik: String, kimSordu: String?, kaynak: File): OgretmenSorusu =
        runBlocking {
            requireNotNull(
                ogretmenDeposu.soruKaydet(
                    grupId = grupId,
                    baslik = baslik,
                    zorluk = null,
                    cevap = null,
                    gorseller = listOf(gorselDeposu.dosyadanHazirla(kaynak, GorselTuru.SORU, 0)),
                    kimSordu = kimSordu,
                ),
            )
        }

    private fun ekraniAc() {
        compose.setContent {
            PremiumYanlisDefteriTema(hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                OgretmenAnaEkrani(
                    ogretmenDeposu = ogretmenDeposu,
                    gorselDeposu = gorselDeposu,
                    sonYedek = null,
                    yedeklemeIslemde = false,
                    onOgrenciModunaGec = {},
                    onYedekOlustur = {},
                    onYedekGeriYukle = {},
                )
            }
        }
    }

    /** LazyColumn yalnız görünür kartı çizer: hedef düğüme listenin kendisi kaydırılarak ulaşılır. */
    private fun kartaKaydir(etiket: String) {
        compose.onNodeWithTag("ogretmen_soru_listesi").performScrollToNode(hasTestTag(etiket))
    }

    @Test
    fun aramaVeSoranKisiFiltresiGrubunListesiniDaraltir() {
        val kaynak = testGorseli()
        try {
            val grup = runBlocking { requireNotNull(ogretmenDeposu.grupEkle("6B")) }
            val oran = soruEkle(grup.id, "Oran Orantı", "Elif Y.", kaynak)
            val basinc = soruEkle(grup.id, "Basınç", "Ahmet", kaynak)

            ekraniAc()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("ogretmen_grup_${grup.id}").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("ogretmen_grup_${grup.id}").performClick()
            kartaKaydir("ogretmen_soru_kart_${oran.id}")

            // Başlıkla arama: yalnız eşleşen kart kalır.
            kartaKaydir("ogretmen_arama")
            compose.onNodeWithTag("ogretmen_arama").performTextReplacement("basınç")
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("ogretmen_soru_kart_${basinc.id}").fetchSemanticsNodes().isNotEmpty() &&
                    compose.onAllNodesWithTag("ogretmen_soru_kart_${oran.id}").fetchSemanticsNodes().isEmpty()
            }
            // Sayaç, filtrenin ne kadar daralttığını yazıyla söyler.
            compose.onNodeWithTag("ogretmen_sonuc_sayisi")
                .assertTextEquals(context.getString(R.string.teacher_search_count, 1))

            // Temizle: diğer kart geri gelir, ardından "soran kişi" çipi tek soruyu bırakır.
            compose.onNodeWithTag("ogretmen_arama_temizle").performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("ogretmen_soru_kart_${oran.id}").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("Soran kişi: Elif Y.").performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("ogretmen_soru_kart_${basinc.id}").fetchSemanticsNodes().isEmpty()
            }
            kartaKaydir("ogretmen_soru_kart_${oran.id}")
            compose.onNodeWithTag("ogretmen_soru_kart_${oran.id}").assertIsDisplayed()
            // Kart üzerindeki rozet, "kim sordu" alanını görünür kılar.
            assertTrue(
                compose.onAllNodesWithText("Soran: Elif Y.").fetchSemanticsNodes().isNotEmpty(),
            )
        } finally {
            kaynak.delete()
        }
    }

    @Test
    fun karttanPaylasYerelChooserAcar() {
        val kaynak = testGorseli()
        try {
            val grup = runBlocking { requireNotNull(ogretmenDeposu.grupEkle("7C")) }
            val soru = soruEkle(grup.id, "Üçgenler", "Zeynep", kaynak)

            ekraniAc()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("ogretmen_grup_${grup.id}").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("ogretmen_grup_${grup.id}").performClick()
            kartaKaydir("ogretmen_paylas_${soru.id}")
            compose.onNodeWithTag("ogretmen_paylas_${soru.id}")
                .performSemanticsAction(SemanticsActions.OnClick)

            val baslatilan: Intent? = shadowOf(compose.activity).nextStartedActivity
            assertNotNull("Paylaş bir chooser açmalı", baslatilan)
            assertEquals(Intent.ACTION_CHOOSER, baslatilan?.action)
            val ic = baslatilan?.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
            assertNotNull(ic)
            // Fotoğraf eki ve MIME türü birim testinde doğrulanır: FileProvider önbelleği süreç
            // genelinde tutulduğu için Robolectric'te metod başına değişen dosya dizininde tekrar
            // çalışmayabilir. Burada kanıtlanan şey: buton gerçek bir yerel paylaşım başlatıyor.
            assertTrue(
                "Yerel paylaşım niyeti bekleniyordu: ${ic?.action}",
                ic?.action == Intent.ACTION_SEND || ic?.action == Intent.ACTION_SEND_MULTIPLE,
            )
            val metin = ic?.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
            assertTrue(metin.contains("Üçgenler"))
            assertTrue("Öğrenci adı paylaşılmamalı: $metin", !metin.contains("Zeynep"))
        } finally {
            kaynak.delete()
        }
    }

    @Test
    fun grupSilmeSnackbarGeriAlmasiGrubuVeSorulariniKurtarir() {
        val kaynak = testGorseli()
        try {
            val grup = runBlocking { requireNotNull(ogretmenDeposu.grupEkle("8A")) }
            val soru = soruEkle(grup.id, "Kesirler", null, kaynak)

            ekraniAc()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("ogretmen_grup_${grup.id}").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("ogretmen_grup_${grup.id}").performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithText("⋮").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("⋮").performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithText(context.getString(R.string.teacher_group_delete))
                    .fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText(context.getString(R.string.teacher_group_delete)).performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("ogretmen_grup_sil_onay").fetchSemanticsNodes().isNotEmpty()
            }
            // Kart altbilgisinde de "Sil" var; onay düğmesi test etiketiyle hedeflenir.
            compose.onNodeWithTag("ogretmen_grup_sil_onay").performClick()

            // Silme sonrası liste ekranına dönülür ve geri alma fırsatı snackbar'da durur.
            // Snackbar 10 saniye yaşar; bu yüzden önce görünmesini bekle, sonra hemen tıkla.
            compose.waitUntil(5_000) {
                compose.onAllNodesWithText(context.getString(R.string.teacher_undo))
                    .fetchSemanticsNodes().isNotEmpty()
            }
            assertTrue(
                compose.onAllNodesWithText(
                    context.getString(R.string.teacher_group_deleted_message, "8A", 1),
                ).fetchSemanticsNodes().isNotEmpty(),
            )
            assertTrue(ogretmenDeposu.anlikVeri().gruplar.none { it.id == grup.id })
            compose.onNodeWithText(context.getString(R.string.teacher_undo)).performClick()

            compose.waitUntil(5_000) {
                val veri = ogretmenDeposu.anlikVeri()
                veri.gruplar.any { it.id == grup.id } && veri.sorular.any { it.id == soru.id }
            }
            val geri = ogretmenDeposu.anlikVeri()
            assertEquals("8A", geri.gruplar.single { it.id == grup.id }.ad)
            assertNotNull(geri.sorular.single { it.id == soru.id })
            assertTrue(geri.tutarliMi())
            assertNull(geri.sorular.firstOrNull { it.id != soru.id })
        } finally {
            kaynak.delete()
        }
    }
}

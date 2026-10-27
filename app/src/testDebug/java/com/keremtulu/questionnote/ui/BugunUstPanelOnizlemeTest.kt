package com.keremtulu.questionnote.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.ilerleme.IlerlemeHizmeti
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.local.entity.TekrarEntity
import com.keremtulu.questionnote.data.prefs.SabitGunlukHedefDeposu
import com.keremtulu.questionnote.data.repository.RoomDersRepository
import com.keremtulu.questionnote.data.repository.RoomSoruRepository
import com.keremtulu.questionnote.ui.panel.LocalUstPanelVar
import com.keremtulu.questionnote.ui.panel.UstPanel
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Bugün + üst panelin gerçek kabuk içindeki görünümü. Sentetik veri, YEREL Compose çizimi;
 * cihaz ekran görüntüsü ya da golden test değildir.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalTestApi::class)
class BugunUstPanelOnizlemeTest {
    @get:Rule val compose = createComposeRule()
    private val context: Context = RuntimeEnvironment.getApplication()

    private fun kaydet(ad: String, koyu: Boolean) {
        val db = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle()).allowMainThreadQueries().build()
        val gun = LocalDate.now().toEpochDay().toInt()
        runBlocking {
            val dersler = db.dersDao().tumunuGetir()
            val matematik = dersler.first { it.ad == "Matematik" }.id
            val fizik = dersler.first { it.ad == "Fizik" }.id
            db.soruDao().ekle(
                listOf(
                    SoruEntity("p1", matematik, soruNotu = "Türev · zincir kuralı", sonrakiTekrarGun = gun, olusturma = 1, guncelleme = 1),
                    SoruEntity("p2", matematik, soruNotu = "Limit · belirsizlik", sonrakiTekrarGun = gun - 2, olusturma = 1, guncelleme = 1),
                    SoruEntity("p3", fizik, soruNotu = "Kuvvet ve ivme", sonrakiTekrarGun = gun, olusturma = 1, guncelleme = 1),
                    SoruEntity("p4", fizik, soruNotu = "Enerji korunumu", sonrakiTekrarGun = gun - 1, olusturma = 1, guncelleme = 1),
                    SoruEntity("p5", matematik, soruNotu = "Fonksiyon grafiği", sonrakiTekrarGun = gun, olusturma = 1, guncelleme = 1),
                    SoruEntity("p6", fizik, soruNotu = "Basınç · sıvılar", sonrakiTekrarGun = gun, olusturma = 1, guncelleme = 1),
                    // Bugün çözülmüş üç soru: günlük hedefin 3/10 görünmesi için, sırada değiller.
                    SoruEntity("d1", matematik, soruNotu = "Türev · teğet", sonTekrarGun = gun, sonrakiTekrarGun = gun + 3, olusturma = 1, guncelleme = 1),
                    SoruEntity("d2", fizik, soruNotu = "Optik · mercek", sonTekrarGun = gun, sonrakiTekrarGun = gun + 4, olusturma = 1, guncelleme = 1),
                    SoruEntity("d3", matematik, soruNotu = "Dizi · toplam", sonTekrarGun = gun, sonrakiTekrarGun = gun + 5, olusturma = 1, guncelleme = 1),
                ),
            )
            db.tekrarDao().ekle(
                listOf("d1", "d2", "d3").mapIndexed { i, soruId ->
                    TekrarEntity(id = "onizleme-$i", soruId = soruId, zaman = 1_000L + i, gun = gun, sonuc = "RAHAT")
                },
            )
        }
        val images = LocalSoruGorselDeposu(context)
        val sorular = RoomSoruRepository(db, images)
        val dersler = RoomDersRepository(db)
        val ilerleme = IlerlemeHizmeti(db)
        val hedef = SabitGunlukHedefDeposu(10)
        compose.setContent {
            YanlisDefteriTema(koyuTema = koyu, hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
                CompositionLocalProvider(LocalUstPanelVar provides true) {
                    Box(Modifier.fillMaxSize().testTag("today-preview-root")) {
                        // Üretim kabuğuyla aynı sıra: panel üstte, menü altta, boşluk panellerde.
                        Scaffold(
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            topBar = { UstPanel(ilerleme, hedef, onIlerleme = {}) },
                            bottomBar = { AltMenu(AltSekme.BUGUN, {}) },
                        ) { insets ->
                            Box(Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets)) {
                                BugunEkrani(
                                    soruRepository = sorular,
                                    dersRepository = dersler,
                                    ilerlemeHizmeti = ilerleme,
                                    gunlukHedefDeposu = hedef,
                                    onSoruEkle = {},
                                    onOturumBaslat = { _, _ -> },
                                )
                            }
                        }
                    }
                }
            }
        }
        // CTA yalnız veri geldikten sonra çizilir; ekran görüntüsü hazır hâli yakalar.
        val baslat = context.getString(R.string.focus_start_today)
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(baslat).fetchSemanticsNodes().isNotEmpty()
        }
        compose.waitForIdle()
        val bitmap = compose.onNodeWithTag("today-preview-root").captureToImage().asAndroidBitmap()
        assertTrue(bitmap.width >= 320 && bitmap.height >= 600)
        val klasor = File("build/outputs/today-previews").apply { mkdirs() }
        File(klasor, "$ad.png").outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
    }

    @Test fun light() = kaydet("bugun-light", koyu = false)
    @Test fun dark() = kaydet("bugun-dark", koyu = true)
}

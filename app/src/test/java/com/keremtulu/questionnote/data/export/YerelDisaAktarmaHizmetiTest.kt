package com.keremtulu.questionnote.data.export

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.room.Room
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.GorselEntity
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.local.entity.SoruSecenegiEntity
import com.keremtulu.questionnote.data.local.entity.TekrarEntity
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.SoruSecenekTuru
import com.keremtulu.questionnote.domain.model.normalizeSoruSecenegiAdi
import java.io.File
import java.util.zip.ZipFile
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class YerelDisaAktarmaHizmetiTest {
    private lateinit var database: YanlisDefteriDatabase
    private lateinit var imageStore: LocalSoruGorselDeposu

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            YanlisDefteriDatabase::class.java,
        )
            .addCallback(VarsayilanDersleriEkle())
            .allowMainThreadQueries()
            .build()
        imageStore = LocalSoruGorselDeposu(RuntimeEnvironment.getApplication())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun exportContainsEveryRoomTableAndReferencedImageBytes() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val ders = database.dersDao().tumunuGetir().first()
        val now = System.currentTimeMillis()
        database.soruSecenegiDao().topluEkle(
            listOf(
                SoruSecenegiEntity(
                    id = "export-source-option",
                    tur = SoruSecenekTuru.KAYNAK.name,
                    ad = "Test kitabı",
                    normalizeAd = normalizeSoruSecenegiAdi("Test kitabı"),
                    olusturma = now,
                ),
                SoruSecenegiEntity(
                    id = "export-reason-option",
                    tur = SoruSecenekTuru.YANLIS_NEDENI.name,
                    ad = "Dikkatsizlik",
                    normalizeAd = normalizeSoruSecenegiAdi("Dikkatsizlik"),
                    olusturma = now,
                ),
            ),
        )
        val topicResult = com.keremtulu.questionnote.data.repository.RoomKonuRepository(database)
            .konuEkle(ders.id, "Fonksiyonlar")
        assertEquals(com.keremtulu.questionnote.domain.model.KonuIslemSonucu.Basarili, topicResult)
        val konu = database.konuDao().dersinKonulariniGetir(ders.id).single()
        val soruId = "export-question"
        val source = File(context.cacheDir, "export-source-${System.nanoTime()}.png")
        val bitmap = Bitmap.createBitmap(96, 64, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).apply { drawColor(Color.WHITE) }
        source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        var staged: com.keremtulu.questionnote.domain.model.SoruGorselGirdisi? = null
        var storedName: String? = null
        var archive: File? = null
        try {
            staged = imageStore.dosyadanHazirla(source, GorselTuru.SORU, 0)
            storedName = imageStore.kaliciyaKopyala(staged.dosyaAdi)
            assertTrue(storedName != null)
            database.soruDao().ekle(
                SoruEntity(
                    id = soruId,
                    dersId = ders.id,
                    konuId = konu.id,
                    soruNotu = "Dışa aktarma test notu",
                    cozumNotu = "Kısa çözüm",
                    kaynak = "Test kitabı",
                    kaynakKonumu = "s. 10, soru 2",
                    yanlisNedeni = "Dikkatsizlik",
                    favori = true,
                    planDurumu = "AKTIF",
                    ogrenmeDurumu = "YENI",
                    sonTekrarGun = null,
                    sonrakiTekrarGun = 20_000,
                    olusturma = now,
                    guncelleme = now,
                ),
            )
            database.gorselDao().ekle(
                listOf(
                    GorselEntity(
                        id = staged.id,
                        soruId = soruId,
                        tur = GorselTuru.SORU.name,
                        sira = 0,
                        dosyaAdi = storedName!!,
                        mimeType = staged.mimeType,
                        genislik = staged.genislik,
                        yukseklik = staged.yukseklik,
                        boyut = staged.boyut,
                        sha256 = staged.sha256,
                        olusturma = now,
                    ),
                ),
            )
            database.tekrarDao().ekle(
                TekrarEntity(
                    id = "export-review",
                    soruId = soruId,
                    zaman = now,
                    gun = 20_000,
                    planlananGun = 19_999,
                    sonuc = "RAHAT",
                    cozumAcildi = false,
                    oncekiAralik = 1,
                    sonrakiAralik = 3,
                    algoritmaSurumu = 1,
                    elleAtandi = false,
                ),
            )

            val createdArchive = YerelDisaAktarmaHizmeti(context, database, imageStore).zipOlustur()
            archive = createdArchive
            ZipFile(createdArchive).use { zip ->
                val entryNames = zip.entries().asSequence().map { it.name }.toSet()
                assertTrue(entryNames.containsAll(setOf("manifest.json", "data.json", "images/${File(storedName!!).name}")))
                assertTrue(zip.getInputStream(zip.getEntry("images/${File(storedName!!).name}")).readBytes().isNotEmpty())
                val manifest = zip.getInputStream(zip.getEntry("manifest.json")).bufferedReader().use {
                    JSONObject(it.readText())
                }
                assertEquals("yanlis-defteri-backup", manifest.getString("format"))
                assertEquals(5, manifest.getInt("format_version"))
                assertEquals(5, manifest.getInt("room_schema_version"))
                assertTrue(manifest.getJSONArray("files").length() >= 9)
                val inventory = manifest.getJSONArray("files")
                for (index in 0 until inventory.length()) {
                    val item = inventory.getJSONObject(index)
                    val bytes = zip.getInputStream(zip.getEntry(item.getString("path"))).use { it.readBytes() }
                    val digest = java.security.MessageDigest.getInstance("SHA-256").digest(bytes)
                        .joinToString("") { "%02x".format(it.toInt() and 0xff) }
                    assertEquals(item.getLong("size_bytes"), bytes.size.toLong())
                    assertEquals(item.getString("sha256"), digest)
                }
                assertTrue(entryNames.containsAll(setOf(
                    "csv/lessons.csv", "csv/topics.csv", "csv/questions.csv", "csv/catalog_options.csv", "csv/images.csv", "csv/reviews.csv", "csv/measurements.csv",
                )))
                assertEquals(1, manifest.getJSONObject("counts").getInt("questions"))
                assertEquals(1, manifest.getJSONObject("counts").getInt("reviews"))
                assertEquals(10, manifest.getJSONObject("counts").getInt("catalog_options"))
                val data = zip.getInputStream(zip.getEntry("data.json")).bufferedReader().use {
                    JSONObject(it.readText())
                }
                assertEquals("Dışa aktarma test notu", data.getJSONArray("questions").getJSONObject(0).getString("question_note"))
                assertEquals("s. 10, soru 2", data.getJSONArray("questions").getJSONObject(0).getString("source_location"))
                assertEquals(10, data.getJSONArray("catalog_options").length())
                assertEquals(7, data.getJSONArray("lessons").length())
                assertEquals(1, data.getJSONArray("topics").length())
            }
        } finally {
            archive?.delete()
            source.delete()
            staged?.let { imageStore.geciciDosyayiSil(it.dosyaAdi) }
            storedName?.let { imageStore.kaliciDosyayiSil(it) }
        }
    }

    @Test
    fun corruptedOrMissingPhotoAbortsExportInsteadOfReportingSuccess() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val ders = database.dersDao().tumunuGetir().first()
        val now = System.currentTimeMillis()
        database.soruDao().ekle(
            SoruEntity(
                id = "missing-export-image-question",
                dersId = ders.id,
                olusturma = now,
                guncelleme = now,
            ),
        )
        database.gorselDao().ekle(
            listOf(
                GorselEntity(
                    id = "missing-export-image",
                    soruId = "missing-export-image-question",
                    tur = GorselTuru.SORU.name,
                    sira = 0,
                    dosyaAdi = "img/00000000-0000-4000-8000-000000000001.jpg",
                    mimeType = "image/jpeg",
                    genislik = 100,
                    yukseklik = 100,
                    boyut = 1,
                    sha256 = "a".repeat(64),
                    olusturma = now,
                ),
            ),
        )

        var failed = false
        try {
            YerelDisaAktarmaHizmeti(context, database, imageStore).zipOlustur()
        } catch (_: Exception) {
            failed = true
        }
        assertTrue(failed)
    }
}

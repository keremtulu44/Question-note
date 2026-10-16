package com.keremtulu.questionnote.data.images

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.room.Room
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.GorselEntity
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class GorselTutarliligiHizmetiTest {
    private lateinit var database: YanlisDefteriDatabase
    private lateinit var store: LocalSoruGorselDeposu
    private lateinit var service: GorselTutarliligiHizmeti
    private lateinit var source: File
    private val permanentFiles = mutableListOf<String>()
    private val stagedFiles = mutableListOf<String>()

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle())
            .allowMainThreadQueries()
            .build()
        store = LocalSoruGorselDeposu(context)
        service = GorselTutarliligiHizmeti(database, store)
        source = File(context.cacheDir, "integrity-source-${System.nanoTime()}.png")
        val bitmap = Bitmap.createBitmap(48, 32, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).apply { drawColor(Color.WHITE) }
        source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    @After
    fun tearDown() = runBlocking {
        stagedFiles.forEach { store.geciciDosyayiSil(it) }
        permanentFiles.forEach { store.kaliciDosyayiSil(it) }
        source.delete()
        database.close()
    }

    @Test
    fun auditSeparatesMissingCorruptAndOldOrphanImages() = runBlocking {
        val now = System.currentTimeMillis()
        val referenced = makePermanent(GorselTuru.SORU)
        val orphan = makePermanent(GorselTuru.COZUM)
        val storedFile = requireNotNull(store.dosyaGoruntuIcin(referenced.dosyaAdi))
        val lessonId = database.dersDao().tumunuGetir().first().id
        addQuestion("integrity-audit-question", lessonId)
        database.gorselDao().ekle(
            listOf(
                referenced.toEntity("integrity-audit-question", referenced.genislik + 1),
                missingEntity("integrity-audit-missing", "integrity-audit-question"),
            ),
        )
        assertTrue(storedFile.setLastModified(now))
        val orphanFile = requireNotNull(store.dosyaGoruntuIcin(orphan.dosyaAdi))
        assertTrue(orphanFile.setLastModified(now - ORPHAN_AGE_MS - 1))

        val report = service.denetle(simdi = now)
        assertEquals(listOf("img/integrity-audit-missing.jpg"), report.eksikDosyalar)
        assertEquals(listOf(referenced.dosyaAdi), report.bozukDosyalar)
        assertEquals(listOf(orphan.dosyaAdi), report.onayBekleyenYetimler.map { it.dosyaAdi })
    }

    @Test
    fun cleanupDeletesOnlySelectedOldOrphansAndNeverDeletesReferencedFiles() = runBlocking {
        val now = System.currentTimeMillis()
        val orphan = makePermanent(GorselTuru.SORU)
        val referenced = makePermanent(GorselTuru.SORU)
        val lessonId = database.dersDao().tumunuGetir().first().id
        addQuestion("integrity-cleanup-question", lessonId)
        database.gorselDao().ekle(listOf(referenced.toEntity("integrity-cleanup-question")))

        listOf(orphan, referenced).forEach { image ->
            val file = requireNotNull(store.dosyaGoruntuIcin(image.dosyaAdi))
            assertTrue(file.setLastModified(now - ORPHAN_AGE_MS - 1))
        }
        assertEquals(1, service.onaylananYetimleriSil(setOf(orphan.dosyaAdi, referenced.dosyaAdi), simdi = now))
        assertFalse(File(RuntimeEnvironment.getApplication().filesDir, orphan.dosyaAdi).exists())
        assertTrue(requireNotNull(store.dosyaGoruntuIcin(referenced.dosyaAdi)).exists())
        assertEquals(0, service.onaylananYetimleriSil(setOf(referenced.dosyaAdi), simdi = now))
    }

    private suspend fun makePermanent(tur: GorselTuru): SoruGorselGirdisi {
        val staged = store.dosyadanHazirla(source, tur, 0)
        stagedFiles += staged.dosyaAdi
        val name = requireNotNull(store.kaliciyaKopyala(staged.dosyaAdi))
        permanentFiles += name
        return staged.copy(dosyaAdi = name, gecici = false)
    }

    private suspend fun addQuestion(id: String, lessonId: String) {
        val now = System.currentTimeMillis()
        database.soruDao().ekle(SoruEntity(id = id, dersId = lessonId, olusturma = now, guncelleme = now))
    }

    private fun SoruGorselGirdisi.toEntity(questionId: String, width: Int = genislik) = GorselEntity(
        id = UUID.randomUUID().toString(),
        soruId = questionId,
        tur = tur.name,
        sira = 0,
        dosyaAdi = dosyaAdi,
        mimeType = mimeType,
        genislik = width,
        yukseklik = yukseklik,
        boyut = boyut,
        sha256 = sha256,
        olusturma = System.currentTimeMillis(),
    )

    private fun missingEntity(id: String, questionId: String) = GorselEntity(
        id = id,
        soruId = questionId,
        tur = GorselTuru.SORU.name,
        sira = 1,
        dosyaAdi = "img/$id.jpg",
        mimeType = "image/jpeg",
        genislik = 48,
        yukseklik = 32,
        boyut = 123,
        sha256 = "0".repeat(64),
        olusturma = System.currentTimeMillis(),
    )

    private companion object {
        const val ORPHAN_AGE_MS = 24L * 60L * 60L * 1_000L
    }
}

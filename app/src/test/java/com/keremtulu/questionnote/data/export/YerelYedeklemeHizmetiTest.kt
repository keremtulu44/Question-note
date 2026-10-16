package com.keremtulu.questionnote.data.export

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.room.Room
import androidx.room.withTransaction
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.GeriYuklemeIsaretiEntity
import com.keremtulu.questionnote.data.local.entity.GorselEntity
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class YerelYedeklemeHizmetiTest {
    private lateinit var database: YanlisDefteriDatabase
    private lateinit var store: LocalSoruGorselDeposu
    private lateinit var service: YerelYedeklemeHizmeti
    private lateinit var source: File
    private val stagedNames = mutableListOf<String>()
    private val permanentNames = mutableListOf<String>()
    private val temporaryFiles = mutableListOf<File>()

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle())
            .allowMainThreadQueries()
            .build()
        store = LocalSoruGorselDeposu(context)
        service = YerelYedeklemeHizmeti(context, database, store)
        source = createImage(context.cacheDir)
    }

    @After
    fun tearDown() = runBlocking {
        temporaryFiles.forEach { it.delete() }
        stagedNames.forEach { store.geciciDosyayiSil(it) }
        permanentNames.forEach { store.kaliciDosyayiSil(it) }
        source.delete()
        File(RuntimeEnvironment.getApplication().filesDir, "yedekler").listFiles().orEmpty()
            .filter { it.name.startsWith("safety-") }
            .forEach { it.delete() }
        database.close()
    }

    @Test
    fun fullRestoreReplacesRowsRewritesImagePathsAndLeavesSafetyArchive() = runBlocking {
        val lessonId = database.dersDao().tumunuGetir().first().id
        val stored = insertQuestionWithImage("restore-original-question", lessonId, "Yedekten gelen not")
        val archive = service.zipOlustur().also(temporaryFiles::add)
        val preview = service.dosyadanOnizle(archive)

        val before = database.soruDao().idIleGetir("restore-original-question")!!
        database.soruDao().guncelle(before.copy(soruNotu = "Yedekten sonra değiştirildi"))

        val result = service.geriYukle(preview)
        assertEquals(1, result.sayilar.soruSayisi)
        assertEquals(1, result.sayilar.gorselSayisi)
        assertEquals("Yedekten gelen not", database.soruDao().idIleGetir("restore-original-question")?.soruNotu)
        assertEquals(1, database.soruDao().tumunuGetir().size)
        val restoredImage = database.gorselDao().tumunuGetir().single()
        assertEquals(stored.id, restoredImage.id)
        assertNotEquals(stored.dosyaAdi, restoredImage.dosyaAdi)
        assertTrue(store.kaliciDosyayiDogrula(restoredImage.toInput()))
        assertFalse(requireNotNull(store.dosyaGoruntuIcin(stored.dosyaAdi)).exists())
        assertEquals(null, database.geriYuklemeIsaretiDao().idGetir())

        val safety = File(RuntimeEnvironment.getApplication().filesDir, "yedekler/${result.guvenlikYedegiAdi}")
        assertTrue(safety.isFile && safety.length() > 0L)
        val last = service.sonYedekBilgisiGetir()
        assertNotNull(last)
        assertTrue(last!!.guvenlikKopyasi)
        assertEquals(result.guvenlikYedegiAdi, last.dosyaAdi)
    }

    @Test
    fun changedPayloadIsRejectedBeforeDatabaseOrFilesAreTouched() = runBlocking {
        val lessonId = database.dersDao().tumunuGetir().first().id
        insertQuestionWithImage("tamper-check-question", lessonId, "Değişmemeli")
        val valid = service.zipOlustur().also(temporaryFiles::add)
        val tampered = File(valid.parentFile, "tampered-${System.nanoTime()}.zip").also(temporaryFiles::add)
        rewriteArchiveWithBadJson(valid, tampered)

        val existingImages = database.gorselDao().tumunuGetir()
        assertThrows(Exception::class.java) { runBlocking { service.dosyadanOnizle(tampered) } }
        assertEquals("Değişmemeli", database.soruDao().idIleGetir("tamper-check-question")?.soruNotu)
        assertEquals(existingImages, database.gorselDao().tumunuGetir())
        assertFalse(File(RuntimeEnvironment.getApplication().filesDir, "yedekler/geri-yukleme.json").exists())
    }

    @Test
    fun unsupportedFormatVersionIsRejectedBeforeDatabaseChanges() = runBlocking {
        val valid = service.zipOlustur().also(temporaryFiles::add)
        val unsupported = File(valid.parentFile, "unsupported-${System.nanoTime()}.zip").also(temporaryFiles::add)
        rewriteManifest(valid, unsupported) { it.put("format_version", 999) }
        val before = database.dersDao().tumunuGetir()

        assertThrows(Exception::class.java) { runBlocking { service.dosyadanOnizle(unsupported) } }
        assertEquals(before, database.dersDao().tumunuGetir())
    }

    @Test
    fun archivePathTraversalIsRejectedBeforeAnyFileIsExtracted() = runBlocking {
        val malicious = File(RuntimeEnvironment.getApplication().cacheDir, "unsafe-${System.nanoTime()}.zip")
            .also(temporaryFiles::add)
        ZipOutputStream(FileOutputStream(malicious)).use { zip ->
            zip.putNextEntry(ZipEntry("../../outside.txt"))
            zip.write("not allowed".toByteArray())
            zip.closeEntry()
        }
        val before = database.dersDao().tumunuGetir()
        assertThrows(Exception::class.java) { runBlocking { service.dosyadanOnizle(malicious) } }
        assertEquals(before, database.dersDao().tumunuGetir())
        assertFalse(File(RuntimeEnvironment.getApplication().filesDir.parentFile, "outside.txt").exists())
    }

    @Test
    fun interruptedRestoreRecoveryKeepsCommittedFilesAndRollsBackUncommittedFiles() = runBlocking {
        val lessonId = database.dersDao().tumunuGetir().first().id
        val oldImage = insertQuestionWithImage("recovery-question", lessonId, "Mevcut")
        val newStaged = store.dosyadanHazirla(source, GorselTuru.SORU, 0)
        stagedNames += newStaged.dosyaAdi
        val newName = requireNotNull(store.kaliciyaKopyala(newStaged.dosyaAdi))
        permanentNames += newName
        val newImage = newStaged.copy(dosyaAdi = newName, gecici = false)

        // Simulate the pre-commit state: DB still references the old file, so recovery removes only new bytes.
        val restoreId = UUID.randomUUID().toString()
        writeJournal(restoreId, oldImage.dosyaAdi, newName, "PREPARED")
        service.kesintiliGeriYuklemeyiTamamla()
        assertTrue(requireNotNull(store.dosyaGoruntuIcin(oldImage.dosyaAdi)).exists())
        assertFalse(requireNotNull(store.dosyaGoruntuIcin(newName)).exists())
        assertFalse(File(RuntimeEnvironment.getApplication().filesDir, "yedekler/geri-yukleme.json").exists())

        // Simulate the post-commit state: the marker and the new row make recovery finish cleanup, not roll back.
        val nextName = requireNotNull(store.kaliciyaKopyala(newStaged.dosyaAdi))
        permanentNames += nextName
        val committedImage = newStaged.copy(dosyaAdi = nextName, gecici = false)
        database.withTransaction {
            database.gorselDao().tumunuSil()
            database.gorselDao().ekle(listOf(committedImage.toEntity("recovery-question")))
            database.geriYuklemeIsaretiDao().ekle(GeriYuklemeIsaretiEntity(restoreId))
        }
        writeJournal(restoreId, oldImage.dosyaAdi, nextName, "PREPARED")
        service.kesintiliGeriYuklemeyiTamamla()
        assertFalse(requireNotNull(store.dosyaGoruntuIcin(oldImage.dosyaAdi)).exists())
        assertTrue(store.kaliciDosyayiDogrula(committedImage.asInput()))
        assertEquals(null, database.geriYuklemeIsaretiDao().idGetir())
        assertFalse(File(RuntimeEnvironment.getApplication().filesDir, "yedekler/geri-yukleme.json").exists())
    }

    private suspend fun insertQuestionWithImage(id: String, lessonId: String, note: String): SoruGorselGirdisi {
        val staged = store.dosyadanHazirla(source, GorselTuru.SORU, 0)
        stagedNames += staged.dosyaAdi
        val permanentName = requireNotNull(store.kaliciyaKopyala(staged.dosyaAdi))
        permanentNames += permanentName
        val permanent = staged.copy(dosyaAdi = permanentName, gecici = false)
        val now = System.currentTimeMillis()
        database.soruDao().ekle(
            SoruEntity(
                id = id,
                dersId = lessonId,
                soruNotu = note,
                olusturma = now,
                guncelleme = now,
            ),
        )
        database.gorselDao().ekle(listOf(permanent.toEntity(id)))
        return permanent
    }

    private fun createImage(directory: File): File {
        val file = File(directory, "backup-source-${System.nanoTime()}.png")
        val bitmap = Bitmap.createBitmap(96, 64, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).apply { drawColor(Color.WHITE) }
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        return file
    }

    private fun rewriteArchiveWithBadJson(source: File, destination: File) {
        rewriteArchive(source, destination) { name, _, _ ->
            if (name == "data.json") "{\"questions\":[]}".toByteArray(Charsets.UTF_8) else null
        }
    }

    private fun rewriteManifest(source: File, destination: File, transform: (JSONObject) -> JSONObject) {
        rewriteArchive(source, destination) { name, entry, zip ->
            if (name != "manifest.json") return@rewriteArchive null
            val original = zip.getInputStream(entry).use { JSONObject(it.readBytes().toString(Charsets.UTF_8)) }
            transform(original).toString().toByteArray(Charsets.UTF_8)
        }
    }

    /** A null replacement means stream-copy the existing entry. */
    private fun rewriteArchive(
        source: File,
        destination: File,
        replacement: (String, ZipEntry, ZipFile) -> ByteArray?,
    ) {
        ZipFile(source).use { input ->
            ZipOutputStream(FileOutputStream(destination)).use { output ->
                input.entries().asSequence().forEach { entry ->
                    output.putNextEntry(ZipEntry(entry.name))
                    val bytes = replacement(entry.name, entry, input)
                    if (bytes != null) output.write(bytes) else input.getInputStream(entry).use { it.copyTo(output) }
                    output.closeEntry()
                }
            }
        }
    }

    private suspend fun writeJournal(id: String, oldName: String, newName: String, state: String) {
        val directory = File(RuntimeEnvironment.getApplication().filesDir, "yedekler").apply { mkdirs() }
        val safetyName = "safety-$id.zip"
        File(directory, safetyName).writeBytes(byteArrayOf(1))
        val json = JSONObject()
            .put("id", id)
            .put("state", state)
            .put("safety_backup", safetyName)
            .put("prepared_id", id)
            .put("old_files", JSONArray(listOf(oldName)))
            .put("new_files", JSONArray(listOf(newName)))
        File(directory, "geri-yukleme.json").writeText(json.toString())
    }

    private fun SoruGorselGirdisi.toEntity(questionId: String) = GorselEntity(
        id = id,
        soruId = questionId,
        tur = tur.name,
        sira = sira,
        dosyaAdi = dosyaAdi,
        mimeType = mimeType,
        genislik = genislik,
        yukseklik = yukseklik,
        boyut = boyut,
        sha256 = sha256,
        olusturma = System.currentTimeMillis(),
    )

    private fun GorselEntity.toInput() = SoruGorselGirdisi(
        id = id,
        tur = GorselTuru.valueOf(tur),
        sira = sira,
        dosyaAdi = dosyaAdi,
        gecici = false,
        mimeType = mimeType,
        genislik = genislik,
        yukseklik = yukseklik,
        boyut = boyut,
        sha256 = sha256,
    )

    private fun SoruGorselGirdisi.asInput() = copy(gecici = false)
}

package com.keremtulu.questionnote.data.export

import com.keremtulu.questionnote.domain.repeat.*
import com.keremtulu.questionnote.data.gelisim.MotorTaniHizmeti
import com.keremtulu.questionnote.data.prefs.RozetDeposu
import com.keremtulu.questionnote.data.prefs.RutbeDeposu
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.domain.seviye.Rutbe
import com.keremtulu.questionnote.data.repository.RoomSoruRepository
import com.keremtulu.questionnote.data.gelisim.OlcumSerilestirme
import com.keremtulu.questionnote.data.gelisim.ProfilHizmeti
import com.keremtulu.questionnote.domain.calisma.OlcumKalitesi
import com.keremtulu.questionnote.domain.calisma.OlcumNedeni
import com.keremtulu.questionnote.domain.calisma.SureOlcumu
import com.keremtulu.questionnote.domain.calisma.TekrarDenemesi
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import com.keremtulu.questionnote.domain.model.SoruIslemSonucu
import android.graphics.Bitmap
import com.keremtulu.questionnote.data.prefs.OdakOturumuDeposu
import com.keremtulu.questionnote.domain.calisma.OdakOturumu
import java.io.IOException
import android.graphics.Canvas
import android.graphics.Color
import androidx.room.Room
import com.keremtulu.questionnote.data.local.entity.TekrarEntity
import java.time.LocalDate
import androidx.room.withTransaction
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.GeriYuklemeIsaretiEntity
import com.keremtulu.questionnote.data.local.entity.GorselEntity
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.local.entity.SoruSecenegiEntity
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import com.keremtulu.questionnote.domain.model.SoruSecenekTuru
import com.keremtulu.questionnote.domain.model.normalizeSoruSecenegiAdi
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
import org.junit.Assert.assertNull
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
        OdakOturumuDeposu(context).temizle()
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
        val sourceName = "Geometri soru bankası"
        val reasonName = "Soruyu hızlı okudum"
        database.soruSecenegiDao().topluEkle(
            listOf(
                catalogOption("restore-source-option", SoruSecenekTuru.KAYNAK, sourceName),
                catalogOption("restore-reason-option", SoruSecenekTuru.YANLIS_NEDENI, reasonName),
            ),
        )
        val stored = insertQuestionWithImage(
            "restore-original-question",
            lessonId,
            "Yedekten gelen not",
            sourceName,
            "s. 118, test 4, soru 7",
            reasonName,
        )
        val archive = service.zipOlustur().also(temporaryFiles::add)
        val preview = service.dosyadanOnizle(archive)
        database.soruSecenegiDao().ekle(
            catalogOption("not-in-backup-option", SoruSecenekTuru.KAYNAK, "Yedekten sonra eklenen"),
        )

        val before = database.soruDao().idIleGetir("restore-original-question")!!
        database.soruDao().guncelle(before.copy(soruNotu = "Yedekten sonra değiştirildi"))

        val result = service.geriYukle(preview)
        assertEquals(1, result.sayilar.soruSayisi)
        assertEquals(1, result.sayilar.gorselSayisi)
        val restoredQuestion = database.soruDao().idIleGetir("restore-original-question")
        assertEquals("Yedekten gelen not", restoredQuestion?.soruNotu)
        assertEquals(sourceName, restoredQuestion?.kaynak)
        assertEquals("s. 118, test 4, soru 7", restoredQuestion?.kaynakKonumu)
        assertEquals(reasonName, restoredQuestion?.yanlisNedeni)
        assertEquals(1, database.soruDao().tumunuGetir().size)
        val restoredOptions = database.soruSecenegiDao().tumunuGetir()
        assertTrue(restoredOptions.any { it.tur == SoruSecenekTuru.KAYNAK.name && it.ad == sourceName })
        assertTrue(restoredOptions.any { it.tur == SoruSecenekTuru.YANLIS_NEDENI.name && it.ad == reasonName })
        assertFalse(restoredOptions.any { it.id == "not-in-backup-option" })
        val restoredImage = database.gorselDao().tumunuGetir().single()
        assertEquals(stored.id, restoredImage.id)
        assertNotEquals(stored.dosyaAdi, restoredImage.dosyaAdi)
        assertTrue(store.kaliciDosyayiDogrula(restoredImage.toInput()))
        assertFalse(File(RuntimeEnvironment.getApplication().filesDir, stored.dosyaAdi).exists())
        assertEquals(null, database.geriYuklemeIsaretiDao().idGetir())

        val safety = File(RuntimeEnvironment.getApplication().filesDir, "yedekler/${result.guvenlikYedegiAdi}")
        assertTrue(safety.isFile && safety.length() > 0L)
        val last = service.sonYedekBilgisiGetir()
        assertNotNull(last)
        assertTrue(last!!.guvenlikKopyasi)
        assertEquals(result.guvenlikYedegiAdi, last.dosyaAdi)
    }

    @Test
    fun v2AlgorithmHistoryRoundTripsInCurrentBackupWithoutRewritingHistory() = runBlocking {
        val lesson = database.dersDao().tumunuGetir().first().id
        insertQuestionWithImage("adaptive-question", lesson, "Adaptif tekrar notu")
        val gun = LocalDate.of(2026, 10, 17).toEpochDay().toInt()
        val record = TekrarEntity(
            id = "adaptive-review", soruId = "adaptive-question", zaman = System.currentTimeMillis(),
            gun = gun, planlananGun = gun - 1, sonuc = "RAHAT", cozumAcildi = false,
            oncekiAralik = 7, sonrakiAralik = 18, algoritmaSurumu = 2,
        )
        database.tekrarDao().ekle(record)
        val question = requireNotNull(database.soruDao().idIleGetir("adaptive-question"))
        database.soruDao().guncelle(question.copy(sonTekrarGun = gun, sonrakiTekrarGun = gun + 18, ogrenmeDurumu = "PEKISIYOR"))
        val archive = service.zipOlustur().also(temporaryFiles::add)
        val preview = service.dosyadanOnizle(archive)
        service.geriYukle(preview)
        assertEquals(record, database.tekrarDao().tumunuGetir().single())
        assertEquals(gun + 18, database.soruDao().idIleGetir("adaptive-question")?.sonrakiTekrarGun)
    }

    @Test
    fun csvExportNeutralizesSpreadsheetFormulasWithoutChangingJsonData() = runBlocking {
        val lessonId = database.dersDao().tumunuGetir().first().id
        insertQuestionWithImage("formula-question", lessonId, "not", "=2+2")

        val archive = service.zipOlustur().also(temporaryFiles::add)
        val (csv, json) = ZipFile(archive).use { zip ->
            val csvText = zip.getInputStream(zip.getEntry("csv/questions.csv"))
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            val jsonText = zip.getInputStream(zip.getEntry("data.json"))
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            csvText to jsonText
        }

        assertFalse(csv.contains("\"=2+2\""))
        assertTrue(csv.contains("\"'=2+2\""))
        // CSV yalnızca insan içindir; geri yüklemenin okuduğu JSON değeri değişmez.
        assertTrue(json.contains("=2+2"))
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
    fun versionTwoBackupRestoresLegacySourceReasonAndQuestionNote() = runBlocking {
        val lessonId = database.dersDao().tumunuGetir().first().id
        val sourceName = "Eski kaynak kitabı"
        val reasonName = "Kendime özgü yanlış nedeni"
        insertQuestionWithImage(
            "legacy-backup-question",
            lessonId,
            "Yedekten korunacak not",
            sourceName,
            reason = reasonName,
        )
        val currentArchive = service.zipOlustur().also(temporaryFiles::add)
        val oldArchive = File(currentArchive.parentFile, "v2-${System.nanoTime()}.zip").also(temporaryFiles::add)
        convertCurrentArchiveToV2(currentArchive, oldArchive)
        ZipFile(oldArchive).use { zip ->
            val legacyHeader = zip.getInputStream(zip.getEntry("csv/questions.csv")).bufferedReader().use { it.readLine() }
            assertFalse(legacyHeader.contains("source_location"))
            assertFalse(zip.entries().asSequence().any { it.name == "csv/catalog_options.csv" })
        }

        val preview = service.dosyadanOnizle(oldArchive)
        service.geriYukle(preview)

        val restored = database.soruDao().idIleGetir("legacy-backup-question")!!
        assertEquals("Yedekten korunacak not", restored.soruNotu)
        assertEquals(sourceName, restored.kaynak)
        assertEquals(null, restored.kaynakKonumu)
        assertEquals(reasonName, restored.yanlisNedeni)
        val options = database.soruSecenegiDao().tumunuGetir()
        assertTrue(options.any { it.tur == SoruSecenekTuru.KAYNAK.name && it.ad == sourceName })
        assertTrue(options.any { it.tur == SoruSecenekTuru.YANLIS_NEDENI.name && it.ad == reasonName })
        assertEquals(8, options.count { it.varsayilan })
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

        val oturumDeposu = OdakOturumuDeposu(RuntimeEnvironment.getApplication())
        val oturum = OdakOturumu("old-session", listOf("recovery-question"), 1L)
        oturumDeposu.kaydet(oturum)
        // Simulate the pre-commit state: DB still references the old file, so recovery removes only new bytes.
        val restoreId = UUID.randomUUID().toString()
        writeJournal(restoreId, oldImage.dosyaAdi, newName, "PREPARED")
        service.kesintiliGeriYuklemeyiTamamla()
        assertTrue(requireNotNull(store.dosyaGoruntuIcin(oldImage.dosyaAdi)).exists())
        assertFalse(File(RuntimeEnvironment.getApplication().filesDir, newName).exists())
        assertFalse(File(RuntimeEnvironment.getApplication().filesDir, "yedekler/geri-yukleme.json").exists())

        assertEquals(oturum, oturumDeposu.oku())
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
        assertFalse(File(RuntimeEnvironment.getApplication().filesDir, oldImage.dosyaAdi).exists())
        assertTrue(store.kaliciDosyayiDogrula(committedImage.asInput()))
        assertEquals(null, oturumDeposu.oku())
        assertEquals(null, database.geriYuklemeIsaretiDao().idGetir())
        assertFalse(File(RuntimeEnvironment.getApplication().filesDir, "yedekler/geri-yukleme.json").exists())
    }

    @Test
    fun failedRestoreBeforeCommitPreservesResumeEvenWhenIdsMatchBackup() = runBlocking {
        val lesson = database.dersDao().tumunuGetir().first().id
        insertQuestionWithImage("resume-question", lesson, "Original")
        val archive = service.zipOlustur().also(temporaryFiles::add)
        val preview = service.dosyadanOnizle(archive)
        val depo = OdakOturumuDeposu(RuntimeEnvironment.getApplication())
        val oturum = OdakOturumu("resume-session", listOf("resume-question"), 1)
        depo.kaydet(oturum)
        service.hazirlananYedegiIptalEt(preview.id)
        try {
            service.geriYukle(preview)
            org.junit.Assert.fail("Missing prepared archive must fail")
        } catch (_: IOException) { /* No commit. */ }
        service.kesintiliGeriYuklemeyiTamamla()
        assertEquals(oturum, depo.oku())
        assertEquals("Original", database.soruDao().idIleGetir("resume-question")?.soruNotu)
    }

    @Test
    fun successfulRestoreClearsResumeEvenWhenQuestionIdsAreReused() = runBlocking {
        val lesson = database.dersDao().tumunuGetir().first().id
        insertQuestionWithImage("same-id", lesson, "Original")
        val archive = service.zipOlustur().also(temporaryFiles::add)
        val preview = service.dosyadanOnizle(archive)
        val depo = OdakOturumuDeposu(RuntimeEnvironment.getApplication())
        depo.kaydet(OdakOturumu("resume-session", listOf("same-id"), 1))
        service.geriYukle(preview)
        assertNotNull(database.soruDao().idIleGetir("same-id"))
        assertEquals(null, depo.oku())
        assertEquals(null, OdakOturumuDeposu(RuntimeEnvironment.getApplication()).oku())
    }

    @Test
    fun checkpointClearFailureKeepsCommitMarkerUntilRecoveryCanInvalidateOldSession() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val lesson = database.dersDao().tumunuGetir().first().id
        insertQuestionWithImage("same-id", lesson, "Original")
        val archive = service.zipOlustur().also(temporaryFiles::add)
        val preview = service.dosyadanOnizle(archive)
        val depo = OdakOturumuDeposu(context)
        depo.kaydet(OdakOturumu("resume-session", listOf("same-id"), 1))
        val bozuk = YerelYedeklemeHizmeti(context, database, store) { throw IOException("prefs unavailable") }
        try {
            bozuk.geriYukle(preview)
            org.junit.Assert.fail("Cannot finish while stale resume remains")
        } catch (_: IOException) { /* Marker must remain for startup recovery. */ }
        assertEquals(preview.id, database.geriYuklemeIsaretiDao().idGetir())
        assertNotNull(depo.oku())
        assertTrue(service.kesintiliGeriYuklemeyiTamamla())
        assertEquals(null, depo.oku())
        assertEquals(null, database.geriYuklemeIsaretiDao().idGetir())
        assertFalse(service.kesintiliGeriYuklemeyiTamamla())
    }

    @Test
    fun versionFourRoundTripsMeasurementsQualityAndDecisionContextWithoutChangingTheV2Plan() = runBlocking {
        val archive = measuredArchive()
        val before = database.tekrarOlcumDao().tumunuGetir()
        val reviews = database.tekrarDao().tumunuGetir()
        ZipFile(archive).use { zip ->
            val manifest = zip.getInputStream(zip.getEntry("manifest.json")).use { JSONObject(it.readBytes().toString(Charsets.UTF_8)) }
            assertEquals(4, manifest.getInt("format_version"))
            assertEquals(4, manifest.getInt("room_schema_version"))
            assertEquals(4, manifest.getJSONObject("counts").getInt("measurements"))
            val csv = zip.getInputStream(zip.getEntry("csv/measurements.csv")).bufferedReader().use { it.readText() }
            assertTrue(csv.contains("attempt_active_ms"))
            assertTrue(csv.contains("'=SUM")) // labels remain safe in human-readable CSV
        }
        val preview = service.dosyadanOnizle(archive)
        assertEquals(4, preview.yedek.sayilar.olcumSayisi)
        service.geriYukle(preview)
        assertEquals(before, database.tekrarOlcumDao().tumunuGetir())
        assertEquals(reviews, database.tekrarDao().tumunuGetir())
        assertEquals(setOf("TAM", "KISMI", "YOK", "GECERSIZ"), before.map { it.kalite }.toSet())
        assertEquals(null, before.single { it.kalite == "YOK" }.denemeAktifMs)
        assertTrue(reviews.all { it.algoritmaSurumu == 2 })
    }

    @Test
    fun versionThreeReaderKeepsCatalogAndLocatorButDoesNotInventMeasurements() = runBlocking {
        val archive = measuredArchive()
        val old = File(archive.parentFile, "v3-${System.nanoTime()}.zip").also(temporaryFiles::add)
        rewriteMeasuredArchive(archive, old, version = 3)
        val before = database.tekrarDao().tumunuGetir()
        service.geriYukle(service.dosyadanOnizle(old))
        assertEquals(before, database.tekrarDao().tumunuGetir())
        assertTrue(database.tekrarOlcumDao().tumunuGetir().isEmpty())
        assertEquals("s. 10", database.soruDao().idIleGetir("measured-0")?.kaynakKonumu)
        val scope = ProfilHizmeti(database).kapsam(LocalDate.now().toEpochDay().toInt())
        assertEquals(4, scope.olcumsuz)
        assertTrue(scope.dersler.isEmpty())
    }

    @Test
    fun invalidMeasurementNumbersAndQualitiesAreRejectedEvenWithValidChecksums() = runBlocking {
        val archive = measuredArchive()
        val before = database.tekrarOlcumDao().tumunuGetir()
        val changes: List<(JSONObject) -> Unit> = listOf(
            { it.put("attempt_active_ms", -1) },
            { it.put("attempt_active_ms", 1e30) },
            { it.put("attempt_active_ms", "9223372036854775808") },
            { it.put("quality", "UNKNOWN") },
            { it.put("measurement_version", 99) },
            { it.put("source_key", "not-the-normalized-label") },
        )
        changes.forEachIndexed { i, change ->
            val bad = File(archive.parentFile, "bad-measure-$i-${System.nanoTime()}.zip").also(temporaryFiles::add)
            rewriteMeasuredArchive(archive, bad) { data -> change(data.getJSONArray("measurements").getJSONObject(0)) }
            assertThrows(Exception::class.java) { runBlocking { service.dosyadanOnizle(bad) } }
            assertEquals(before, database.tekrarOlcumDao().tumunuGetir())
        }
    }

    @Test
    fun orphanAndDuplicateAttemptMeasurementsAreRejectedBeforeRestore() = runBlocking {
        val archive = measuredArchive()
        val before = database.tekrarDao().tumunuGetir()
        val orphan = File(archive.parentFile, "orphan-${System.nanoTime()}.zip").also(temporaryFiles::add)
        rewriteMeasuredArchive(archive, orphan) { it.getJSONArray("measurements").getJSONObject(0).put("review_id", "missing-review") }
        assertThrows(Exception::class.java) { runBlocking { service.dosyadanOnizle(orphan) } }
        val duplicate = File(archive.parentFile, "duplicate-${System.nanoTime()}.zip").also(temporaryFiles::add)
        rewriteMeasuredArchive(archive, duplicate) {
            val rows = it.getJSONArray("measurements")
            rows.getJSONObject(1).put("attempt_id", rows.getJSONObject(0).getString("attempt_id"))
        }
        assertThrows(Exception::class.java) { runBlocking { service.dosyadanOnizle(duplicate) } }
        assertEquals(before, database.tekrarDao().tumunuGetir())
    }

    @Test
    fun malformedUnsupportedOrOversizedDecisionSnapshotsAreRejected() = runBlocking {
        val archive = measuredArchive()
        val changes: List<(JSONObject) -> Unit> = listOf(
            { it.put("version", 99) },
            { it.put("mode", "SHADOW_V3") },
            { it.put("final_interval", 180) },
            { it.put("padding", "x".repeat(OlcumSerilestirme.KARAR_UST_SINIR)) },
        )
        changes.forEachIndexed { i, change ->
            val bad = File(archive.parentFile, "bad-decision-$i-${System.nanoTime()}.zip").also(temporaryFiles::add)
            rewriteMeasuredArchive(archive, bad) { data -> change(data.getJSONArray("measurements").getJSONObject(0).getJSONObject("decision")) }
            assertThrows(Exception::class.java) { runBlocking { service.dosyadanOnizle(bad) } }
        }
        assertEquals(4, database.tekrarDao().tumunuGetir().size)
    }

    @Test fun shadowDecisionVersionTwoRoundTripsZipFourAndDiagnosticsAreTruthful() = runBlocking<Unit> {
        val archive = measuredArchive(TekrarMotorModu.GOLGE)
        val measurements = database.tekrarOlcumDao().tumunuGetir()
        val reviews = database.tekrarDao().tumunuGetir()
        assertTrue(measurements.all { JSONObject(it.kararJson).getInt("version") == 2 })
        assertTrue(reviews.all { it.algoritmaSurumu == 2 })
        service.geriYukle(service.dosyadanOnizle(archive))
        assertEquals(measurements, database.tekrarOlcumDao().tumunuGetir())
        assertEquals(reviews, database.tekrarDao().tumunuGetir())
        val tani = MotorTaniHizmeti(database).oku()
        assertEquals(4, tani.golge); assertEquals(0, tani.gecersiz); assertEquals(0, tani.farkliAday)
    }

    @Test fun explicitlyGatedActiveRecordsRoundTripThenRollbackWritesOnlyNewV2History() = runBlocking<Unit> {
        val archive = measuredArchive(TekrarMotorModu.ETKIN)
        val old = database.tekrarDao().tumunuGetir().associateBy { it.id }
        service.geriYukle(service.dosyadanOnizle(archive))
        assertTrue(database.tekrarDao().tumunuGetir().all { it.algoritmaSurumu == 3 })
        val tomorrow = LocalDate.now().plusDays(1).atTime(12, 0).atZone(java.time.ZoneId.systemDefault())
        val repo = RoomSoruRepository(database, store, java.time.Clock.fixed(tomorrow.toInstant(), tomorrow.zone))
        assertEquals(SoruIslemSonucu.Basarili, repo.tekrarKaydet("measured-0", TekrarSonucu.RAHAT, false))
        val records = database.tekrarDao().tumunuGetir()
        assertEquals(2, records.single { it.id !in old }.algoritmaSurumu)
        assertTrue(records.filter { it.id in old }.all { it == old[it.id] })
    }

    @Test fun forgedV2CandidateModeOrReasonIsRejectedEvenAfterBackupHashesAreRecomputed() = runBlocking<Unit> {
        val archive = measuredArchive(TekrarMotorModu.GOLGE)
        val before = database.tekrarOlcumDao().tumunuGetir()
        val changes: List<(JSONObject) -> Unit> = listOf(
            { it.put("version", 99) }, { it.put("candidate_interval", 180) },
            { it.put("mode", "ETKIN") }, { it.put("reasons", JSONArray(listOf("SPEED_BONUS"))) },
            { it.put("base_interval", 181) },
        )
        changes.forEachIndexed { i, change ->
            val bad = File(archive.parentFile, "bad-shadow-$i-${System.nanoTime()}.zip").also(temporaryFiles::add)
            rewriteMeasuredArchive(archive, bad) { data -> change(data.getJSONArray("measurements").getJSONObject(0).getJSONObject("decision")) }
            assertThrows(Exception::class.java) { runBlocking { service.dosyadanOnizle(bad) } }
            assertEquals(before, database.tekrarOlcumDao().tumunuGetir())
        }
    }

    @Test fun verifiedRestoreClearsCosmeticQueuesBeforeRecoveryMarkerAndKeepsOwnership() = runBlocking<Unit> {
        listOf("karne_rozetleri", "rutbe_kutlamalari").forEach { context.getSharedPreferences(it, 0).edit().clear().commit() }
        val archive = measuredArchive()
        val badges = RozetDeposu(context); val ranks = RutbeDeposu(context)
        badges.kaydet(listOf(Rozet.ILK_SORU)); ranks.guncelle(Rutbe.BASLANGIC, 10, false); ranks.guncelle(Rutbe.MERAKLI, 10, true)
        assertNotNull(ranks.bekleyenIzle.value)
        assertFalse(badges.bekleyenIzle.value.isEmpty())
        var fail = true
        val guarded = YerelYedeklemeHizmeti(context, database, store) {
            OdakOturumuDeposu(context).temizle(); badges.geriYuklemeSonrasi(); ranks.geriYuklemeSonrasi()
            if (fail) { fail = false; throw IOException("Simulated interruption after durable cosmetic reset") }
        }
        val preview = guarded.dosyadanOnizle(archive)
        assertThrows(IOException::class.java) { runBlocking { guarded.geriYukle(preview) } }
        assertEquals(preview.id, database.geriYuklemeIsaretiDao().idGetir())
        assertTrue(guarded.kesintiliGeriYuklemeyiTamamla())
        assertNull(database.geriYuklemeIsaretiDao().idGetir())
        assertTrue(Rozet.ILK_SORU in RozetDeposu(context).sahipOlanlar())
        assertTrue(RozetDeposu(context).bekleyenIzle.value.isEmpty())
        val restored = RutbeDeposu(context)
        restored.guncelle(Rutbe.KOZMIK, 10, true)
        assertNull(restored.bekleyenIzle.value)
    }

    private suspend fun measuredArchive(mod: TekrarMotorModu = TekrarMotorModu.KAPALI): File {
        val lesson = database.dersDao().tumunuGetir().first().id
        val repo = RoomSoruRepository(database, store, motor = TekrarModuSaglayici { MotorAyari(mod, MotorYayinKapisi(true, true)) })
        val olcumler = listOf(SureOlcumu(6_000, 0, OlcumKalitesi.TAM),
            SureOlcumu(1_000, 2_000, OlcumKalitesi.KISMI, setOf(OlcumNedeni.TAM_EKRAN)),
            SureOlcumu(), SureOlcumu(kalite = OlcumKalitesi.GECERSIZ, nedenler = setOf(OlcumNedeni.SAAT_TUTARSIZ)))
        olcumler.forEachIndexed { i, olcum ->
            val id = "measured-$i"
            insertQuestionWithImage(id, lesson, "Ölçümlü", "=SUM(1,2)", "s. 10")
            val onizleme = requireNotNull(repo.tekrarOnizle(TekrarDenemesi("attempt-$i", id, TekrarSonucu.RAHAT, i == 1, olcum, "session")))
            assertEquals(SoruIslemSonucu.Basarili, repo.tekrarKaydet(onizleme))
        }
        return service.zipOlustur().also(temporaryFiles::add)
    }

    /** Rebuild all inventory sizes/hashes, so rejection tests exercise semantic validation, not just checksums. */
    private fun rewriteMeasuredArchive(source: File, destination: File, version: Int = 4, change: (JSONObject) -> Unit = {}) {
        ZipFile(source).use { zip ->
            val manifest = zip.getInputStream(zip.getEntry("manifest.json")).use { JSONObject(it.readBytes().toString(Charsets.UTF_8)) }
            val payloads = linkedMapOf<String, ByteArray>()
            zip.entries().asSequence().filter { it.name != "manifest.json" }.forEach { entry ->
                payloads[entry.name] = zip.getInputStream(entry).use { it.readBytes() }
            }
            val data = JSONObject(requireNotNull(payloads["data.json"]).toString(Charsets.UTF_8))
            change(data)
            if (version == 3) {
                data.remove("measurements"); payloads.remove("csv/measurements.csv")
                manifest.getJSONObject("counts").remove("measurements")
            }
            payloads["data.json"] = data.toString().toByteArray(Charsets.UTF_8)
            manifest.put("format_version", version).put("room_schema_version", version)
                .put("csv_files", JSONArray(payloads.keys.filter { it.startsWith("csv/") }.sorted()))
            manifest.put("files", JSONArray().apply { payloads.toSortedMap().forEach { (name, bytes) ->
                val hash = java.security.MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 0xff) }
                put(JSONObject().put("path", name).put("size_bytes", bytes.size).put("sha256", hash))
            } })
            ZipOutputStream(FileOutputStream(destination)).use { out ->
                out.putNextEntry(ZipEntry("manifest.json")); out.write(manifest.toString().toByteArray(Charsets.UTF_8)); out.closeEntry()
                payloads.forEach { (name, bytes) -> out.putNextEntry(ZipEntry(name)); out.write(bytes); out.closeEntry() }
            }
        }
    }

    private suspend fun insertQuestionWithImage(
        id: String,
        lessonId: String,
        note: String,
        sourceName: String? = null,
        sourceLocation: String? = null,
        reason: String? = null,
    ): SoruGorselGirdisi {
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
                kaynak = sourceName,
                kaynakKonumu = sourceLocation,
                yanlisNedeni = reason,
                olusturma = now,
                guncelleme = now,
            ),
        )
        database.gorselDao().ekle(listOf(permanent.toEntity(id)))
        return permanent
    }

    private fun catalogOption(id: String, type: SoruSecenekTuru, name: String) = SoruSecenegiEntity(
        id = id,
        tur = type.name,
        ad = name,
        normalizeAd = normalizeSoruSecenegiAdi(name),
        olusturma = System.currentTimeMillis(),
    )

    private fun createImage(directory: File): File {
        val file = File(directory, "backup-source-${System.nanoTime()}.png")
        val bitmap = Bitmap.createBitmap(96, 64, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).apply { drawColor(Color.WHITE) }
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        return file
    }

    private fun convertCurrentArchiveToV2(source: File, destination: File) {
        ZipFile(source).use { zip ->
            val manifest = zip.getInputStream(zip.getEntry("manifest.json")).use {
                JSONObject(it.readBytes().toString(Charsets.UTF_8))
            }
            val sourceQuestions = zip.getInputStream(zip.getEntry("data.json")).use {
                JSONObject(it.readBytes().toString(Charsets.UTF_8)).getJSONArray("questions")
            }
            val payloads = linkedMapOf<String, ByteArray>()
            zip.entries().asSequence().forEach { entry ->
                if (entry.name == "manifest.json" || entry.name == "csv/catalog_options.csv" || entry.name == "csv/measurements.csv") return@forEach
                var bytes = zip.getInputStream(entry).use { it.readBytes() }
                when (entry.name) {
                    "data.json" -> {
                        val data = JSONObject(bytes.toString(Charsets.UTF_8))
                        data.remove("catalog_options")
                        data.remove("measurements")
                        val questions = data.getJSONArray("questions")
                        for (index in 0 until questions.length()) {
                            questions.getJSONObject(index).remove("source_location")
                        }
                        bytes = data.toString().toByteArray(Charsets.UTF_8)
                    }
                    "csv/questions.csv" -> bytes = versionTwoQuestionsCsv(sourceQuestions)
                }
                payloads[entry.name] = bytes
            }

            manifest.put("format_version", 2)
            manifest.put("room_schema_version", 2)
            manifest.put(
                "csv_files",
                JSONArray(listOf(
                    "csv/lessons.csv",
                    "csv/topics.csv",
                    "csv/questions.csv",
                    "csv/images.csv",
                    "csv/reviews.csv",
                )),
            )
            val counts = manifest.getJSONObject("counts")
            counts.remove("catalog_options")
            counts.remove("measurements")
            manifest.put("files", JSONArray().apply {
                payloads.toSortedMap().forEach { (name, bytes) ->
                    val hash = java.security.MessageDigest.getInstance("SHA-256").digest(bytes)
                        .joinToString("") { "%02x".format(it.toInt() and 0xff) }
                    put(JSONObject()
                        .put("path", name)
                        .put("size_bytes", bytes.size)
                        .put("sha256", hash))
                }
            })

            ZipOutputStream(FileOutputStream(destination)).use { output ->
                output.putNextEntry(ZipEntry("manifest.json"))
                output.write(manifest.toString().toByteArray(Charsets.UTF_8))
                output.closeEntry()
                payloads.toSortedMap().forEach { (name, bytes) ->
                    output.putNextEntry(ZipEntry(name))
                    output.write(bytes)
                    output.closeEntry()
                }
            }
        }
    }

    private fun versionTwoQuestionsCsv(questions: JSONArray): ByteArray {
        val columns = listOf(
            "id", "lesson_id", "topic_id", "question_note", "solution_note", "source", "mistake_reason",
            "favorite", "plan_status", "learning_status", "last_review_day", "next_review_day",
            "created_at_epoch_ms", "updated_at_epoch_ms",
        )
        fun csvRow(values: List<Any?>): String = values.joinToString(",") { value ->
            val text = when (value) {
                null -> ""
                is Boolean -> if (value) "true" else "false"
                else -> value.toString()
            }
            "\"${text.replace("\"", "\"\"")}\""
        }
        val rows = mutableListOf(csvRow(columns))
        for (index in 0 until questions.length()) {
            val question = questions.getJSONObject(index)
            rows += csvRow(columns.map { column ->
                if (question.isNull(column)) null else question.get(column)
            })
        }
        return rows.joinToString("\r\n", postfix = "\r\n").toByteArray(Charsets.UTF_8)
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

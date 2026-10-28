package com.keremtulu.questionnote.data.export

import com.keremtulu.questionnote.data.gelisim.OlcumSerilestirme
import com.keremtulu.questionnote.data.local.entity.TekrarOlcumEntity
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.room.withTransaction
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.local.VarsayilanSoruNedenleri
import com.keremtulu.questionnote.data.local.varsayilanSoruSecenekleri
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.DersEntity
import com.keremtulu.questionnote.data.local.entity.GeriYuklemeIsaretiEntity
import com.keremtulu.questionnote.data.local.entity.GorselEntity
import com.keremtulu.questionnote.data.local.entity.KonuEntity
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.local.entity.SoruSecenegiEntity
import com.keremtulu.questionnote.data.local.entity.TekrarEntity
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.OgrenmeDurumu
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import com.keremtulu.questionnote.domain.model.SoruPlanDurumu
import com.keremtulu.questionnote.domain.model.SoruSecenekTuru
import com.keremtulu.questionnote.domain.model.SORU_SECENEGI_ADI_UST_SINIR
import com.keremtulu.questionnote.domain.model.normalizeSoruSecenegiAdi
import com.keremtulu.questionnote.domain.model.stableSoruSecenegiId
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Table counts shown before the user confirms a full replacement restore. */
data class YedekSayilari(
    val dersSayisi: Int,
    val konuSayisi: Int,
    val soruSayisi: Int,
    val gorselSayisi: Int,
    val tekrarSayisi: Int,
    val olcumSayisi: Int = 0,
)

data class YedekOzet(
    val dosyaAdi: String,
    val olusturmaZamani: Long,
    val boyutBayt: Long,
    val sayilar: YedekSayilari,
)

data class GeriYuklemeOnizlemesi internal constructor(
    val id: String,
    val yedek: YedekOzet,
    val mevcut: YedekSayilari,
    val yedekOgretmenGrupSayisi: Int = 0,
    val yedekOgretmenSoruSayisi: Int = 0,
    val mevcutOgretmenGrupSayisi: Int = 0,
    val mevcutOgretmenSoruSayisi: Int = 0,
    val mevcutDenemeBozuk: Boolean = false,
    val yedekteDenemeBozuk: Boolean = false,
)

data class GeriYuklemeSonucu(
    val sayilar: YedekSayilari,
    val guvenlikYedegiAdi: String,
    val bozukDenemeGuvenlikYedeginde: Boolean = false,
    val yedektenGelenDenemeBozuk: Boolean = false,
)

data class SonYedekBilgisi(
    val dosyaAdi: String,
    val olusturmaZamani: Long,
    val boyutBayt: Long,
    val guvenlikKopyasi: Boolean,
)

/**
 * Complete, versioned ZIP backups and full-replacement restore.
 * JSON is the canonical data format; CSV tables are spreadsheet-friendly companion files.
 */
class YerelYedeklemeHizmeti(
    context: Context,
    private val database: YanlisDefteriDatabase,
    private val gorselDeposu: LocalSoruGorselDeposu,
    private val denemeDeposu: com.keremtulu.questionnote.data.prefs.DenemeDeposu? = null,
    private val ogretmenDeposu: com.keremtulu.questionnote.data.prefs.OgretmenDeposu? = null,
    // Clear ONLY a committed restore, before removing its recovery markers. Also used on startup.
    private val oturumTemizle: () -> Unit = {
        com.keremtulu.questionnote.data.prefs.OdakOturumuDeposu(context.applicationContext).temizle()
    },
) {
    private val appContext = context.applicationContext
    private val root = File(appContext.filesDir, BACKUP_DIRECTORY).apply { mkdirs() }
    private val preparedRoot = File(root, PREPARED_DIRECTORY).apply { mkdirs() }
    private val journalFile = File(root, RESTORE_JOURNAL)
    private val lastBackupFile = File(root, LAST_BACKUP_INFO)
    private val restoreMutex = Mutex()

    suspend fun zipOlustur(): File = withContext(Dispatchers.IO) {
        val exportDirectory = File(appContext.cacheDir, EXPORT_DIRECTORY).apply { mkdirs() }
        val destination = File(exportDirectory, "yanlis-defteri-${UUID.randomUUID()}.zip")
        createArchiveAtomically(destination)
    }

    /** Writes through SAF (including a user-selected Drive provider) and records success only after close. */
    suspend fun disariAktar(destination: Uri): SonYedekBilgisi = withContext(Dispatchers.IO) {
        val archive = zipOlustur()
        try {
            val output = appContext.contentResolver.openOutputStream(destination, "w")
                ?: throw IOException("Seçilen konuma yazılamadı.")
            try {
                output.use { out ->
                    archive.inputStream().buffered().use { input ->
                        input.copyTo(out, bufferSize = IO_BUFFER_SIZE)
                    }
                    out.flush()
                }
            } catch (error: Exception) {
                // Hedef açıldıktan sonra yazma yarıda kaldıysa yarım ZIP'i bırakma; geçerli yedek sanılmasın.
                runCatching { appContext.contentResolver.delete(destination, null, null) }
                throw error
            }
            val info = SonYedekBilgisi(
                dosyaAdi = destination.lastPathSegment?.substringAfterLast('/')
                    ?.takeIf { it.isNotBlank() } ?: archive.name,
                olusturmaZamani = System.currentTimeMillis(),
                boyutBayt = archive.length(),
                guvenlikKopyasi = false,
            )
            saveLastBackupInfo(info)
            info
        } finally {
            archive.delete()
        }
    }

    suspend fun sonYedekBilgisiGetir(): SonYedekBilgisi? = withContext(Dispatchers.IO) {
        if (!lastBackupFile.isFile) return@withContext null
        runCatching {
            val json = JSONObject(lastBackupFile.readText(Charsets.UTF_8))
            SonYedekBilgisi(
                dosyaAdi = json.getString("file_name"),
                olusturmaZamani = json.getLong("created_at_epoch_ms"),
                boyutBayt = json.getLong("size_bytes"),
                guvenlikKopyasi = json.getBoolean("safety_copy"),
            )
        }.getOrNull()
    }

    suspend fun uriIleOnizle(source: Uri): GeriYuklemeOnizlemesi = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val directory = preparedDirectory(id).apply { mkdirs() }
        val archive = File(directory, INPUT_ARCHIVE)
        try {
            val input = appContext.contentResolver.openInputStream(source)
                ?: throw IOException("Seçilen yedek okunamadı.")
            input.use { copyBounded(it, archive, MAX_ARCHIVE_BYTES) }
            val displayName = runCatching {
                appContext.contentResolver.query(source, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                    ?.use { cursor ->
                        val column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (cursor.moveToFirst() && column >= 0) cursor.getString(column) else null
                    }
            }.getOrNull()?.takeIf { it.isNotBlank() } ?: source.lastPathSegment ?: archive.name
            previewCopiedArchive(id, archive, directory, displayName)
        } catch (cancel: CancellationException) {
            directory.deleteRecursively()
            throw cancel
        } catch (error: Exception) {
            directory.deleteRecursively()
            throw error
        }
    }

    /** Local-file entry point is also used by repository tests; the UI uses the SAF URI overload. */
    suspend fun dosyadanOnizle(source: File): GeriYuklemeOnizlemesi = withContext(Dispatchers.IO) {
        if (!source.isFile || source.length() !in 1L..MAX_ARCHIVE_BYTES) {
            throw IOException("Yedek dosyası bulunamadı veya boyut sınırını aşıyor.")
        }
        val id = UUID.randomUUID().toString()
        val directory = preparedDirectory(id).apply { mkdirs() }
        val archive = File(directory, INPUT_ARCHIVE)
        try {
            source.inputStream().use { copyBounded(it, archive, MAX_ARCHIVE_BYTES) }
            previewCopiedArchive(id, archive, directory, source.name)
        } catch (cancel: CancellationException) {
            directory.deleteRecursively()
            throw cancel
        } catch (error: Exception) {
            directory.deleteRecursively()
            throw error
        }
    }

    /** Preview the most recent on-device safety copy through the same verification path as SAF. */
    suspend fun sonGuvenlikYedeginiOnizle(): GeriYuklemeOnizlemesi = withContext(Dispatchers.IO) {
        val son = sonYedekBilgisiGetir()
        if (son == null || !son.guvenlikKopyasi || !SAFETY_NAME.matches(son.dosyaAdi)) {
            throw IOException("Cihazda seçilebilir bir güvenlik yedeği bulunamadı.")
        }
        val dosya = File(root, son.dosyaAdi)
        if (!dosya.isFile || dosya.length() != son.boyutBayt) {
            throw IOException("Güvenlik yedeği eksik veya boyutu değişmiş.")
        }
        dosyadanOnizle(dosya)
    }

    suspend fun hazirlananYedegiIptalEt(previewId: String) = withContext(Dispatchers.IO) {
        preparedDirectory(previewId).deleteRecursively()
        Unit
    }

    suspend fun geriYukle(preview: GeriYuklemeOnizlemesi): GeriYuklemeSonucu =
        withContext(Dispatchers.IO) {
            restoreMutex.withLock {
                restorePrepared(preview)
            }
        }

    /** Must finish before the application exposes its normal screens after process restart. */
    suspend fun kesintiliGeriYuklemeyiTamamla(): Boolean = withContext(Dispatchers.IO) {
        restoreMutex.withLock { recoverIfNeeded() }
    }

    private suspend fun previewCopiedArchive(
        id: String,
        archive: File,
        directory: File,
        displayName: String,
    ): GeriYuklemeOnizlemesi {
        if (!archive.isFile || archive.length() !in 1L..MAX_ARCHIVE_BYTES) {
            throw IOException("Yedek dosyası boş veya boyut sınırını aşıyor.")
        }
        val payloadDirectory = File(directory, PAYLOAD_DIRECTORY)
        val verified = verifyArchive(archive, payloadDirectory)
        val current = currentCounts()
        val mevcutOgretmen = ogretmenDeposu?.anlikVeri()
            ?: com.keremtulu.questionnote.domain.model.OgretmenVerisi()
        return GeriYuklemeOnizlemesi(
            id = id,
            yedek = verified.summary.copy(dosyaAdi = displayName.takeIf { it.isNotBlank() } ?: archive.name),
            mevcut = current,
            yedekOgretmenGrupSayisi = verified.data.ogretmenVerisi.gruplar.size,
            yedekOgretmenSoruSayisi = verified.data.ogretmenVerisi.sorular.size,
            mevcutOgretmenGrupSayisi = mevcutOgretmen.gruplar.size,
            mevcutOgretmenSoruSayisi = mevcutOgretmen.sorular.size,
            mevcutDenemeBozuk = denemeDeposu?.yazilabilirMi() == false,
            yedekteDenemeBozuk = verified.data.denemeHam != null,
        )
    }

    private suspend fun restorePrepared(preview: GeriYuklemeOnizlemesi): GeriYuklemeSonucu {
        val directory = preparedDirectory(preview.id)
        val archive = File(directory, INPUT_ARCHIVE)
        if (!archive.isFile) throw IOException("Onizlemesi yapılan yedek artık bulunamıyor.")
        if (journalFile.exists()) throw IOException("Önceki geri yükleme işlemi henüz tamamlanmadı.")
        val markerBefore = database.geriYuklemeIsaretiDao().idGetir()
        if (markerBefore != null) throw IOException("Bir geri yükleme işlemi kurtarılmayı bekliyor.")

        val payloadDirectory = File(directory, PAYLOAD_DIRECTORY)
        payloadDirectory.deleteRecursively()
        val verified = verifyArchive(archive, payloadDirectory)
        if (denemeDeposu == null && (verified.data.denemeler.isNotEmpty() || verified.data.denemeHam != null)) {
            throw IOException("Deneme deposu olmadan deneme kayıtları geri yüklenemez.")
        }
        val current = currentSnapshot(bozukDenemeyeIzinVer = true)
        if (preview.mevcutDenemeBozuk != (current.denemeHam != null) ||
            preview.yedekteDenemeBozuk != (verified.data.denemeHam != null)
        ) throw IOException("Deneme verisi önizlemeden sonra değişti; yedeği yeniden seçin.")
        val safetyName = "safety-${UUID.randomUUID()}.zip"
        val safetyArchive = File(root, safetyName)
        try {
            createArchiveAtomically(safetyArchive, current)
        } catch (cancel: CancellationException) {
            safetyArchive.delete()
            throw cancel
        } catch (error: Exception) {
            safetyArchive.delete()
            throw IOException("Mevcut arşiv için güvenlik yedeği alınamadı; geri yükleme yapılmadı.", error)
        }
        saveLastBackupInfo(
            SonYedekBilgisi(
                dosyaAdi = safetyName,
                olusturmaZamani = System.currentTimeMillis(),
                boyutBayt = safetyArchive.length(),
                guvenlikKopyasi = true,
            ),
        )
        val uniqueTeacherImages = verified.data.ogretmenVerisi.sorular
            .flatMap { it.gorseller }
            .distinctBy { it.dosyaAdi }
        val restoreImageBytes = verified.data.images.sumOf { it.sizeBytes } +
            uniqueTeacherImages.sumOf { it.boyut }
        val rawJournalBytes = verified.data.denemeHam?.toByteArray(Charsets.UTF_8)?.size?.toLong() ?: 0L
        ensureFreeSpace(root, restoreImageBytes + rawJournalBytes + MIN_FREE_SPACE_RESERVE_BYTES)

        val newFileNames = verified.data.images.map { image ->
            image.permanentFileName(preview.id)
        }
        val remappedTeacherWorkspace = verified.data.ogretmenVerisi.copy(
            sorular = verified.data.ogretmenVerisi.sorular.map { soru ->
                soru.copy(
                    gorseller = soru.gorseller.map { img ->
                        img.copy(dosyaAdi = teacherPermanentFileName(preview.id, img.dosyaAdi))
                    },
                )
            },
        )
        val newTeacherFileNames = uniqueTeacherImages.map { img ->
            teacherPermanentFileName(preview.id, img.dosyaAdi)
        }.distinct()
        val journal = RestoreJournal(
            id = preview.id,
            state = STATE_PREPARED,
            safetyBackupName = safetyName,
            preparedId = preview.id,
            oldFileNames = current.gorseller.map { it.dosyaAdi }.distinct(),
            newFileNames = newFileNames,
            mockExams = verified.data.denemeler,
            mockExamsRaw = verified.data.denemeHam,
            oldTeacherFileNames = current.ogretmenVerisi.kaliciDosyaAdlari().toList(),
            newTeacherFileNames = newTeacherFileNames,
            teacherWorkspace = remappedTeacherWorkspace,
        )
        writeJournal(journal)

        return try {
            verified.data.images.forEach { image ->
                val source = File(payloadDirectory, image.zipFileName)
                val destinationName = image.permanentFileName(preview.id)
                val expected = image.asInput(destinationName)
                val existing = gorselDeposu.dosyaGoruntuIcin(destinationName)
                if (existing?.exists() == true) {
                    if (gorselDeposu.kaliciDosyayiDogrula(expected)) return@forEach
                    gorselDeposu.kaliciDosyayiSil(destinationName)
                }
                val copied = gorselDeposu.yedektenKaliciyaKopyala(
                    source = source,
                    beklenen = image.asInput(image.zipFileName),
                    hedefDosyaAdi = destinationName,
                )
                if (copied != destinationName) throw IOException("Yedek görseli kalıcı alana alınamadı: ${image.id}")
                if (!gorselDeposu.kaliciDosyayiDogrula(expected)) {
                    throw IOException("Kalıcılaştırılan yedek görseli doğrulanamadı: ${image.id}")
                }
            }
            uniqueTeacherImages.forEach { img ->
                val zipName = com.keremtulu.questionnote.data.prefs.OgretmenDeposu.zipAdi(img.dosyaAdi)
                val source = File(payloadDirectory, zipName)
                val destinationName = teacherPermanentFileName(preview.id, img.dosyaAdi)
                val expected = img.copy(dosyaAdi = destinationName).toGirdi()
                val existing = gorselDeposu.dosyaGoruntuIcin(destinationName)
                if (existing?.exists() == true) {
                    if (gorselDeposu.ogretmenKaliciDosyayiDogrula(expected)) return@forEach
                    gorselDeposu.ogretmenKaliciDosyayiSil(destinationName)
                }
                val copied = gorselDeposu.ogretmenYedektenKaliciyaKopyala(
                    source = source,
                    beklenen = img.toGirdi(),
                    hedefDosyaAdi = destinationName,
                )
                if (copied != destinationName) {
                    throw IOException("Öğretmen görseli kalıcı alana alınamadı: ${img.id}")
                }
                if (!gorselDeposu.ogretmenKaliciDosyayiDogrula(expected)) {
                    throw IOException("Kalıcılaştırılan öğretmen görseli doğrulanamadı: ${img.id}")
                }
            }

            database.withTransaction {
                if (database.geriYuklemeIsaretiDao().idGetir() != null) {
                    throw IOException("Başka bir geri yükleme işlemi aynı anda başladı.")
                }
                database.tekrarDao().tumunuSil()
                database.gorselDao().tumunuSil()
                database.soruDao().tumunuSil()
                database.soruSecenegiDao().tumunuSil()
                database.konuDao().tumunuSil()
                database.dersDao().tumunuSil()
                database.dersDao().ekle(verified.data.dersler)
                if (verified.data.konular.isNotEmpty()) database.konuDao().ekle(verified.data.konular)
                if (verified.data.sorular.isNotEmpty()) database.soruDao().ekle(verified.data.sorular)
                if (verified.data.secenekler.isNotEmpty()) {
                    database.soruSecenegiDao().topluEkle(verified.data.secenekler)
                }
                if (verified.data.images.isNotEmpty()) {
                    database.gorselDao().ekle(
                        verified.data.images.map { image ->
                            image.toEntity(image.permanentFileName(preview.id))
                        },
                    )
                }
                if (verified.data.tekrarlar.isNotEmpty()) database.tekrarDao().ekle(verified.data.tekrarlar)
                if (verified.data.olcumler.isNotEmpty()) database.tekrarOlcumDao().ekle(verified.data.olcumler)
                database.geriYuklemeIsaretiDao().ekle(GeriYuklemeIsaretiEntity(preview.id))
            }

            val committed = journal.copy(state = STATE_COMMITTED)
            writeJournal(committed)
            finishCommittedRestore(committed)
            directory.deleteRecursively()
            GeriYuklemeSonucu(
                sayilar = verified.summary.sayilar,
                guvenlikYedegiAdi = safetyName,
                bozukDenemeGuvenlikYedeginde = current.denemeHam != null,
                yedektenGelenDenemeBozuk = verified.data.denemeHam != null,
            )
        } catch (cancel: CancellationException) {
            // Keep the durable journal; startup recovery decides commit vs. rollback from the DB marker.
            throw cancel
        } catch (error: Exception) {
            try {
                recoverIfNeeded()
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (recovery: Exception) {
                if (recovery !== error) error.addSuppressed(recovery)
            }
            throw error
        }
    }

    /** Called under restoreMutex. The SQLite marker and file journal form a two-phase recovery protocol. */
    private suspend fun recoverIfNeeded(): Boolean {
        if (!journalFile.isFile) {
            val marker = database.geriYuklemeIsaretiDao().idGetir()
            if (marker != null) throw IOException("Geri yükleme işareti var ancak işlem günlüğü kayıp.")
            return false
        }
        val journal = readJournal()
        val marker = database.geriYuklemeIsaretiDao().idGetir()
        if (marker != null && marker != journal.id) {
            throw IOException("Geri yükleme günlüğü ile veritabanı işareti uyuşmuyor.")
        }
        if (journal.state == STATE_COMMITTED || marker == journal.id) {
            finishCommittedRestore(journal.copy(state = STATE_COMMITTED))
        } else {
            // No marker means the all-or-nothing Room transaction did not commit; retain old rows/files.
            journal.newFileNames.forEach { gorselDeposu.kaliciDosyayiSil(it) }
            journal.newTeacherFileNames.forEach { gorselDeposu.ogretmenKaliciDosyayiSil(it) }
            preparedDirectory(journal.preparedId).deleteRecursively()
            journalFile.delete()
        }
        return true
    }

    private suspend fun finishCommittedRestore(journal: RestoreJournal) {
        val currentRows = database.gorselDao().tumunuGetir()
        val currentNames = currentRows.mapTo(mutableSetOf()) { it.dosyaAdi }
        if (!currentNames.containsAll(journal.newFileNames)) {
            throw IOException("Geri yükleme tamamlandı işaretli ancak veritabanı görselleri eksik.")
        }
        for (row in currentRows) {
            val input = row.toInput()
            if (!gorselDeposu.kaliciDosyayiDogrula(input)) {
                throw IOException("Geri yükleme sonrası görsel doğrulaması başarısız: ${row.dosyaAdi}")
            }
        }
        for (img in journal.teacherWorkspace.sorular.flatMap { it.gorseller }.distinctBy { it.dosyaAdi }) {
            if (!gorselDeposu.ogretmenKaliciDosyayiDogrula(img.toGirdi())) {
                throw IOException("Geri yükleme sonrası öğretmen görsel doğrulaması başarısız: ${img.dosyaAdi}")
            }
        }
        // A failed pre-commit restore preserves the old session. A committed replacement must
        // never resume an old question ID, even when the imported backup reuses that ID.
        // If preference persistence fails, retain journal/DB marker and block recovery safely.
        if (journal.mockExamsRaw != null) {
            val depo = denemeDeposu ?: throw IOException("Ham deneme verisini yüklemek için deneme deposu bulunamadı.")
            depo.hamYedegiGeriYukle(journal.mockExamsRaw)
        } else {
            denemeDeposu?.geriYukleVeUzlastir(journal.mockExams, database.dersDao().tumunuGetir())
        }
        ogretmenDeposu?.geriYukle(journal.teacherWorkspace)
        oturumTemizle()
        journal.oldFileNames.filterNot { it in journal.newFileNames }
            .forEach { gorselDeposu.kaliciDosyayiSil(it) }
        journal.oldTeacherFileNames.filterNot { it in journal.newTeacherFileNames }
            .forEach { gorselDeposu.ogretmenKaliciDosyayiSil(it) }
        database.withTransaction {
            val marker = database.geriYuklemeIsaretiDao().idGetir()
            if (marker == journal.id) database.geriYuklemeIsaretiDao().tumunuSil()
        }
        preparedDirectory(journal.preparedId).deleteRecursively()
        journalFile.delete()
    }

    private suspend fun currentCounts(): YedekSayilari = database.withTransaction {
        YedekSayilari(
            dersSayisi = database.dersDao().tumunuGetir().size,
            konuSayisi = database.konuDao().tumunuGetir().size,
            soruSayisi = database.soruDao().tumunuGetir().size,
            gorselSayisi = database.gorselDao().tumunuGetir().size,
            tekrarSayisi = database.tekrarDao().tumunuGetir().size,
            olcumSayisi = database.tekrarOlcumDao().tumunuGetir().size,
        )
    }

    private suspend fun currentSnapshot(bozukDenemeyeIzinVer: Boolean = false): ExportSnapshot = database.withTransaction {
        val dersler = database.dersDao().tumunuGetir()
        val dersHaritasi = dersler.associateBy { it.id }
        val goruntu = denemeDeposu?.yedekGoruntusu(bozukHamIle = bozukDenemeyeIzinVer)
        val denemeler = goruntu?.kayitlar.orEmpty().map { deneme ->
            val satirlar = deneme.satirlar.map { satir ->
                satir.copy(dersAdi = satir.dersAdi ?: dersHaritasi[satir.dersId]?.ad)
            }
            deneme.copy(satirlar = satirlar)
        }
        ExportSnapshot(
            dersler = dersler,
            konular = database.konuDao().tumunuGetir(),
            sorular = database.soruDao().tumunuGetir(),
            secenekler = database.soruSecenegiDao().tumunuGetir(),
            gorseller = database.gorselDao().tumunuGetir(),
            tekrarlar = database.tekrarDao().tumunuGetir(),
            olcumler = database.tekrarOlcumDao().tumunuGetir(),
            denemeler = denemeler,
            denemeHam = goruntu?.bozukHam,
            ogretmenVerisi = ogretmenDeposu?.anlikVeri() ?: com.keremtulu.questionnote.domain.model.OgretmenVerisi(),
        )
    }

    private suspend fun createArchiveAtomically(destination: File, snapshotOverride: ExportSnapshot? = null): File {
        destination.parentFile?.mkdirs()
        val temp = File(destination.parentFile, "${destination.name}.pending")
        temp.delete()
        return try {
            val snapshot = snapshotOverride ?: currentSnapshot()
            val backupData = snapshot.toBackupData()
            validateBackupData(backupData, null)
            val jsonBytes = dataJson(backupData).toString().toByteArray(Charsets.UTF_8)
            if (jsonBytes.size.toLong() > MAX_JSON_BYTES) throw IOException("Yedek JSON dosyası üst sınırı aşıyor.")
            val csvBytes = csvFiles(backupData)
            val payloads = mutableListOf<ArchivePayload>()
            payloads += payloadFromBytes(DATA_FILE, jsonBytes)
            csvBytes.forEach { (name, bytes) -> payloads += payloadFromBytes(name, bytes) }
            backupData.images.forEach { image ->
                val entity = snapshot.gorseller.first { it.id == image.id }
                val input = entity.toInput()
                if (!gorselDeposu.kaliciDosyayiDogrula(input)) {
                    throw IOException("Eksik veya bozuk görsel bulundu: ${image.id}")
                }
                val file = gorselDeposu.dosyaGoruntuIcin(entity.dosyaAdi)
                    ?: throw IOException("Görsel bulunamadı: ${image.id}")
                if (file.length() != image.sizeBytes) throw IOException("Görsel boyutu değişti: ${image.id}")
                payloads += payloadFromFile(image.zipFileName, file, image.sizeBytes, image.sha256)
            }
            val uniqueTeacherImages = backupData.ogretmenVerisi.sorular
                .flatMap { it.gorseller }
                .distinctBy { it.dosyaAdi }
            uniqueTeacherImages.forEach { img ->
                val input = img.toGirdi()
                if (!gorselDeposu.depolananDosyayiDogrula(input)) {
                    throw IOException("Eksik veya bozuk öğretmen görseli bulundu: ${img.id}")
                }
                val file = gorselDeposu.dosyaGoruntuIcin(img.dosyaAdi)
                    ?: throw IOException("Öğretmen görseli bulunamadı: ${img.id}")
                if (file.length() != img.boyut) throw IOException("Öğretmen görsel boyutu değişti: ${img.id}")
                val zipPath = com.keremtulu.questionnote.data.prefs.OgretmenDeposu.zipAdi(img.dosyaAdi)
                if (payloads.none { it.name == zipPath }) {
                    payloads += payloadFromFile(zipPath, file, img.boyut, img.sha256)
                }
            }
            val payloadSize = payloads.sumOf { it.sizeBytes }
            if (payloads.size + 1 > MAX_ARCHIVE_ENTRIES || payloadSize > MAX_TOTAL_UNCOMPRESSED_BYTES) {
                throw IOException("Yedek, dosya sayısı veya açılmış toplam boyut sınırını aşıyor.")
            }
            ensureFreeSpace(temp.parentFile ?: appContext.cacheDir, estimatedArchiveBytes(payloadSize))
            val createdAt = System.currentTimeMillis()
            val manifestBytes = manifestJson(
                counts = backupData.counts(),
                catalogOptionCount = backupData.secenekler.size,
                teacherImageCount = uniqueTeacherImages.size,
                formatVersion = if (backupData.denemeHam != null) RECOVERY_FORMAT_VERSION else FORMAT_VERSION,
                createdAt = createdAt,
                payloads = payloads,
            ).toString(2).toByteArray(Charsets.UTF_8)
            if (manifestBytes.size > MAX_MANIFEST_BYTES) throw IOException("Yedek manifesti beklenenden büyük.")

            val fileOutput = FileOutputStream(temp)
            val zip = ZipOutputStream(BufferedOutputStream(fileOutput))
            try {
                zip.putBytes(MANIFEST_FILE, manifestBytes)
                payloads.sortedBy { it.name }.forEach { payload ->
                    // A corrupt value may be highly repetitive; keep our own recovery ZIP within
                    // the same compression-ratio limit enforced for all incoming archives.
                    zip.setLevel(if (backupData.denemeHam != null && payload.name == DATA_FILE) Deflater.NO_COMPRESSION else Deflater.DEFAULT_COMPRESSION)
                    writePayload(zip, payload)
                }
                zip.finish()
                zip.flush()
                fileOutput.fd.sync()
            } finally {
                zip.close()
            }
            if (temp.length() > MAX_ARCHIVE_BYTES) throw IOException("Oluşturulan ZIP yedeği boyut sınırını aşıyor.")
            java.nio.file.Files.move(
                temp.toPath(),
                destination.toPath(),
                java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
            )
            destination
        } catch (error: Exception) {
            temp.delete()
            destination.delete()
            throw error
        }
    }

    private fun writePayload(zip: ZipOutputStream, payload: ArchivePayload) {
        zip.putNextEntry(ZipEntry(payload.name))
        val digest = MessageDigest.getInstance("SHA-256")
        var size = 0L
        when {
            payload.bytes != null -> {
                zip.write(payload.bytes)
                digest.update(payload.bytes)
                size = payload.bytes.size.toLong()
            }
            payload.file != null -> {
                BufferedInputStream(FileInputStream(payload.file)).use { input ->
                    val buffer = ByteArray(IO_BUFFER_SIZE)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        size += count
                        if (size > MAX_ENTRY_BYTES) throw IOException("ZIP girdisi boyut sınırını aşıyor: ${payload.name}")
                        digest.update(buffer, 0, count)
                        zip.write(buffer, 0, count)
                    }
                }
            }
        }
        zip.closeEntry()
        if (size != payload.sizeBytes || digest.digest().toHex() != payload.sha256) {
            throw IOException("ZIP hazırlanırken kaynak değişti: ${payload.name}")
        }
    }

    private fun manifestJson(
        counts: YedekSayilari,
        catalogOptionCount: Int,
        teacherImageCount: Int,
        formatVersion: Int,
        createdAt: Long,
        payloads: List<ArchivePayload>,
    ) = JSONObject()
        .put("format", FORMAT_ID)
        .put("format_version", formatVersion)
        .put("room_schema_version", ROOM_SCHEMA_VERSION)
        .put("created_at_epoch_ms", createdAt)
        .put("data_file", DATA_FILE)
        .put("csv_files", JSONArray(CSV_FILES))
        .put("image_directory", IMAGE_DIRECTORY)
        .put("counts", JSONObject()
            .put("lessons", counts.dersSayisi)
            .put("topics", counts.konuSayisi)
            .put("questions", counts.soruSayisi)
            .put("images", counts.gorselSayisi)
            .put("reviews", counts.tekrarSayisi)
            .put("measurements", counts.olcumSayisi)
            .put("catalog_options", catalogOptionCount)
            .apply {
                if (teacherImageCount > 0) put("teacher_images", teacherImageCount)
            })
        .put("files", JSONArray().apply {
            payloads.sortedBy { it.name }.forEach { item ->
                put(JSONObject()
                    .put("path", item.name)
                    .put("size_bytes", item.sizeBytes)
                    .put("sha256", item.sha256))
            }
        })

    private fun dataJson(data: BackupData) = JSONObject()
        .put("lessons", JSONArray().apply { data.dersler.forEach { put(it.toJson()) } })
        .put("topics", JSONArray().apply { data.konular.forEach { put(it.toJson()) } })
        .put("questions", JSONArray().apply { data.sorular.forEach { put(it.toJson()) } })
        .put("catalog_options", JSONArray().apply { data.secenekler.forEach { put(it.toJson()) } })
        .put("images", JSONArray().apply { data.images.forEach { put(it.toJson()) } })
        .put("reviews", JSONArray().apply { data.tekrarlar.forEach { put(it.toJson()) } })
        .put("measurements", JSONArray().apply { data.olcumler.forEach { put(OlcumSerilestirme.yaz(it)) } })
        .apply {
            if (data.denemeHam != null) put("mock_exams_corrupt_raw", JSONObject(data.denemeHam))
            if (data.denemeler.isNotEmpty()) {
                put(
                    "mock_exams",
                    JSONArray().apply {
                        data.denemeler.forEach {
                            put(com.keremtulu.questionnote.data.prefs.DenemeDeposu.jsonYaz(it))
                        }
                    },
                )
            }
            if (data.ogretmenVerisi.bosDegil()) {
                put(
                    "teacher_workspace",
                    com.keremtulu.questionnote.data.prefs.OgretmenDeposu.jsonYaz(data.ogretmenVerisi),
                )
                put(
                    "teacher_workspace_version",
                    com.keremtulu.questionnote.data.prefs.OgretmenDeposu.SURUM,
                )
            }
        }

    private fun csvFiles(data: BackupData): Map<String, ByteArray> = linkedMapOf(
        "csv/lessons.csv" to csv(
            listOf("id", "name", "order", "archived", "created_at_epoch_ms", "updated_at_epoch_ms"),
            data.dersler.map { listOf(it.id, it.ad, it.sira, it.arsivli, it.olusturma, it.guncelleme) },
        ),
        "csv/topics.csv" to csv(
            listOf("id", "lesson_id", "name", "order", "archived", "created_at_epoch_ms", "updated_at_epoch_ms"),
            data.konular.map { listOf(it.id, it.dersId, it.ad, it.sira, it.arsivli, it.olusturma, it.guncelleme) },
        ),
        "csv/questions.csv" to csv(
            listOf("id", "lesson_id", "topic_id", "question_note", "solution_note", "source", "source_location", "mistake_reason", "favorite", "plan_status", "learning_status", "last_review_day", "next_review_day", "created_at_epoch_ms", "updated_at_epoch_ms"),
            data.sorular.map { listOf(it.id, it.dersId, it.konuId, it.soruNotu, it.cozumNotu, it.kaynak, it.kaynakKonumu, it.yanlisNedeni, it.favori, it.planDurumu, it.ogrenmeDurumu, it.sonTekrarGun, it.sonrakiTekrarGun, it.olusturma, it.guncelleme) },
        ),
        "csv/catalog_options.csv" to csv(
            listOf("id", "type", "name", "normalized_name", "is_default", "order", "created_at_epoch_ms"),
            data.secenekler.map { listOf(it.id, it.tur, it.ad, it.normalizeAd, it.varsayilan, it.sira, it.olusturma) },
        ),
        "csv/images.csv" to csv(
            listOf("id", "question_id", "type", "order", "file", "mime_type", "width", "height", "size_bytes", "sha256", "created_at_epoch_ms"),
            data.images.map { listOf(it.id, it.questionId, it.tur.name, it.sira, it.zipFileName, it.mimeType, it.width, it.height, it.sizeBytes, it.sha256, it.createdAt) },
        ),
        "csv/reviews.csv" to csv(
            listOf("id", "question_id", "time_epoch_ms", "day", "scheduled_day", "result", "solution_opened", "previous_interval_days", "next_interval_days", "algorithm_version", "manually_assigned"),
            data.tekrarlar.map { listOf(it.id, it.soruId, it.zaman, it.gun, it.planlananGun, it.sonuc, it.cozumAcildi, it.oncekiAralik, it.sonrakiAralik, it.algoritmaSurumu, it.elleAtandi) },
        ),
        "csv/measurements.csv" to csv(
            listOf("review_id", "attempt_id", "session_id", "attempt_active_ms", "assisted_active_ms", "quality", "reasons", "measurement_version",
                "lesson_id", "lesson_label", "topic_id", "topic_label", "source_key", "source_label", "decision_json"),
            data.olcumler.map { listOf(it.tekrarId, it.denemeId, it.oturumId, it.denemeAktifMs, it.incelemeAktifMs, it.kalite, it.nedenlerJson,
                it.olcumSurumu, it.dersId, it.dersAdi, it.konuId, it.konuAdi, it.kaynakAnahtari, it.kaynakEtiketi, it.kararJson) },
        ),
    ).mapValues { (_, text) -> text.toByteArray(Charsets.UTF_8) }

    private fun csv(header: List<Any?>, rows: List<List<Any?>>): String =
        (listOf(header) + rows).joinToString("\r\n", postfix = "\r\n") { row ->
            row.joinToString(",") { value ->
                val text = when (value) {
                    null -> ""
                    is Boolean -> if (value) "true" else "false"
                    else -> value.toString()
                }
                "\"${text.ciktisizMetin().replace("\"", "\"\"")}\""
            }
        }

    /**
     * CSV yalnızca insan içindir (geri yükleme data.json okur) ama tabloda açıldığında
     * `=`, `+`, `@` gibi önekler formül olarak çalışabilir. Metin hücmesini nötrleştir.
     */
    private fun String.ciktisizMetin(): String {
        if (isEmpty()) return this
        val formulOnegi = this[0] == '=' || this[0] == '+' || this[0] == '@' ||
            this[0] == '\t' || this[0] == '\r' ||
            (this[0] == '-' && toDoubleOrNull() == null)
        return if (formulOnegi) "'$this" else this
    }

    private fun DersEntity.toJson() = JSONObject()
        .put("id", id)
        .put("name", ad)
        .put("order", sira)
        .put("archived", arsivli)
        .put("created_at_epoch_ms", olusturma)
        .put("updated_at_epoch_ms", guncelleme)

    private fun KonuEntity.toJson() = JSONObject()
        .put("id", id)
        .put("lesson_id", dersId)
        .put("name", ad)
        .put("order", sira)
        .put("archived", arsivli)
        .put("created_at_epoch_ms", olusturma)
        .put("updated_at_epoch_ms", guncelleme)

    private fun SoruEntity.toJson() = JSONObject()
        .put("id", id)
        .put("lesson_id", dersId)
        .putNullable("topic_id", konuId)
        .putNullable("question_note", soruNotu)
        .putNullable("solution_note", cozumNotu)
        .putNullable("source", kaynak)
        .putNullable("source_location", kaynakKonumu)
        .putNullable("mistake_reason", yanlisNedeni)
        .put("favorite", favori)
        .put("plan_status", planDurumu)
        .put("learning_status", ogrenmeDurumu)
        .putNullable("last_review_day", sonTekrarGun)
        .putNullable("next_review_day", sonrakiTekrarGun)
        .put("created_at_epoch_ms", olusturma)
        .put("updated_at_epoch_ms", guncelleme)

    private fun SoruSecenegiEntity.toJson() = JSONObject()
        .put("id", id)
        .put("type", tur)
        .put("name", ad)
        .put("normalized_name", normalizeAd)
        .put("is_default", varsayilan)
        .put("order", sira)
        .put("created_at_epoch_ms", olusturma)

    private fun BackupImage.toJson() = JSONObject()
        .put("id", id)
        .put("question_id", questionId)
        .put("type", tur.name)
        .put("order", sira)
        .put("file", zipFileName)
        .put("mime_type", mimeType)
        .put("width", width)
        .put("height", height)
        .put("size_bytes", sizeBytes)
        .put("sha256", sha256)
        .put("created_at_epoch_ms", createdAt)

    private fun TekrarEntity.toJson() = JSONObject()
        .put("id", id)
        .put("question_id", soruId)
        .put("time_epoch_ms", zaman)
        .put("day", gun)
        .putNullable("scheduled_day", planlananGun)
        .put("result", sonuc)
        .put("solution_opened", cozumAcildi)
        .putNullable("previous_interval_days", oncekiAralik)
        .putNullable("next_interval_days", sonrakiAralik)
        .put("algorithm_version", algoritmaSurumu)
        .put("manually_assigned", elleAtandi)

    private fun JSONObject.putNullable(name: String, value: Any?): JSONObject =
        put(name, value ?: JSONObject.NULL)

    private fun ExportSnapshot.toBackupData(): BackupData {
        val imageData = gorseller.map { row ->
            BackupImage(
                id = row.id,
                questionId = row.soruId,
                tur = runCatching { GorselTuru.valueOf(row.tur) }
                    .getOrElse { throw IOException("Bilinmeyen görsel türü: ${row.tur}") },
                sira = row.sira,
                zipFileName = "$IMAGE_DIRECTORY${row.id}.jpg",
                mimeType = row.mimeType,
                width = row.genislik,
                height = row.yukseklik,
                sizeBytes = row.boyut,
                sha256 = row.sha256,
                createdAt = row.olusturma,
            )
        }
        return BackupData(
            dersler = dersler,
            konular = konular,
            sorular = sorular,
            secenekler = secenekler,
            images = imageData,
            tekrarlar = tekrarlar,
            olcumler = olcumler,
            denemeler = denemeler,
            denemeHam = denemeHam,
            ogretmenVerisi = ogretmenVerisi,
        )
    }

    private suspend fun verifyArchive(archive: File, payloadDirectory: File): VerifiedArchive {
        if (archive.length() !in 1L..MAX_ARCHIVE_BYTES) throw IOException("ZIP boyut sınırını aşıyor.")
        payloadDirectory.deleteRecursively()
        payloadDirectory.mkdirs()
        ZipFile(archive).use { zip ->
            val entries = zip.entries().asSequence().toList()
            if (entries.isEmpty() || entries.size > MAX_ARCHIVE_ENTRIES) {
                throw IOException("ZIP içindeki dosya sayısı geçersiz.")
            }
            val entryNames = entries.map { it.name }
            if (entryNames.size != entryNames.distinct().size) throw IOException("ZIP yinelenen dosya adı içeriyor.")
            if (entries.any { it.isDirectory || !isSafeZipPath(it.name) }) {
                throw IOException("ZIP güvenli olmayan yol veya klasör girdisi içeriyor.")
            }
            val manifestEntry = zip.getEntry(MANIFEST_FILE)
                ?: throw IOException("ZIP manifest.json içermiyor.")
            if (manifestEntry.size !in 1L..MAX_MANIFEST_BYTES.toLong()) {
                throw IOException("Manifest boyutu geçersiz.")
            }
            val manifestBytes = readEntryBytes(zip, manifestEntry, MAX_MANIFEST_BYTES)
            if (manifestBytes.size.toLong() != manifestEntry.size) throw IOException("Manifest ZIP girdisi kesik veya bozuk.")
            val manifest = JSONObject(manifestBytes.toString(Charsets.UTF_8))
            val formatVersion = manifest.getInt("format_version")
            if (manifest.getString("format") != FORMAT_ID || formatVersion !in SUPPORTED_FORMAT_VERSIONS) {
                throw IOException("Bu ZIP yedek biçimi desteklenmiyor.")
            }
            val expectedSchemaVersion = when (formatVersion) {
                2 -> 2
                3 -> 3
                4 -> 4
                5, 6 -> 5
                else -> throw IOException("Desteklenmeyen yedek sürümü.")
            }
            if (manifest.getInt("room_schema_version") != expectedSchemaVersion) {
                throw IOException("Yedeğin Room şema sürümü desteklenmiyor.")
            }
            if (manifest.getString("data_file") != DATA_FILE || manifest.getString("image_directory") != IMAGE_DIRECTORY) {
                throw IOException("Manifest dosya yolları geçersiz.")
            }
            val createdAt = manifest.getLong("created_at_epoch_ms")
            if (createdAt <= 0L) throw IOException("Yedek tarihi geçersiz.")
            val manifestCounts = manifest.getJSONObject("counts")
            val counts = manifestCounts.toCounts(formatVersion)
            val catalogOptionCount = if (formatVersion in 3..6) {
                manifestCounts.getInt("catalog_options").also {
                    if (it < 0) throw IOException("Manifest içindeki katalog sayısı negatif.")
                }
            } else {
                null
            }
            val teacherImageCount = manifestCounts.optInt("teacher_images", 0).also {
                if (it < 0) throw IOException("Manifest içindeki öğretmen görsel sayısı negatif.")
            }
            val inventory = parseInventory(manifest.getJSONArray("files"))
            val inventoryPaths = inventory.map { it.path }.toSet()
            if (inventory.size + 1 != entries.size || inventoryPaths + MANIFEST_FILE != entryNames.toSet()) {
                throw IOException("ZIP girdileri manifest ile eşleşmiyor.")
            }
            val declaredCsv = manifest.getJSONArray("csv_files").toStringSet()
            val expectedCsvFiles = when (formatVersion) {
                2 -> LEGACY_REQUIRED_CSV_FILES
                3 -> V3_REQUIRED_CSV_FILES
                4, 5, 6 -> REQUIRED_CSV_FILES
                else -> throw IOException("Desteklenmeyen CSV sürümü.")
            }
            if (declaredCsv != expectedCsvFiles || !inventoryPaths.containsAll(expectedCsvFiles)) {
                throw IOException("Yedek CSV tabloları eksik veya tanımsız.")
            }
            val imageEntries = inventoryPaths.filter { it.startsWith(IMAGE_DIRECTORY) }
            if (inventoryPaths != expectedCsvFiles + DATA_FILE + imageEntries) {
                throw IOException("ZIP envanteri biçim sürümünün tablolarıyla eşleşmiyor.")
            }
            val beklenenGorselUst = counts.gorselSayisi + teacherImageCount
            if (imageEntries.size > beklenenGorselUst || imageEntries.size < counts.gorselSayisi ||
                imageEntries.any { !IMAGE_PATH.matches(it) }
            ) {
                throw IOException("Yedek görsel envanteri geçersiz.")
            }
            if (!inventoryPaths.contains(DATA_FILE)) throw IOException("data.json yedekte eksik.")
            val totalBytes = inventory.sumOf { it.sizeBytes }
            if (totalBytes > MAX_TOTAL_UNCOMPRESSED_BYTES) throw IOException("ZIP açılmış toplam boyutu aşıyor.")
            ensureFreeSpace(payloadDirectory, totalBytes + MIN_FREE_SPACE_RESERVE_BYTES)

            for (item in inventory) {
                if (!isAllowedPayloadPath(item.path)) throw IOException("ZIP desteklenmeyen bir dosya içeriyor: ${item.path}")
                val entry = zip.getEntry(item.path) ?: throw IOException("ZIP dosyası eksik: ${item.path}")
                if (entry.isDirectory || entry.size != item.sizeBytes || entry.size > MAX_ENTRY_BYTES) {
                    throw IOException("ZIP dosya boyutu geçersiz: ${item.path}")
                }
                if (entry.compressedSize < 0L || entry.size > 0L &&
                    (entry.compressedSize == 0L || entry.size > entry.compressedSize * MAX_COMPRESSION_RATIO)
                ) {
                    throw IOException("ZIP sıkıştırma oranı güvenlik sınırını aşıyor: ${item.path}")
                }
                val out = File(payloadDirectory, item.path)
                out.parentFile?.mkdirs()
                val measured = copyZipEntry(zip, entry, out, item.sizeBytes)
                if (measured.first != item.sizeBytes || measured.second != item.sha256) {
                    throw IOException("Yedek checksum doğrulaması başarısız: ${item.path}")
                }
            }
            val dataFile = File(payloadDirectory, DATA_FILE)
            if (dataFile.length() > MAX_JSON_BYTES) throw IOException("data.json üst boyut sınırını aşıyor.")
            val data = parseData(JSONObject(dataFile.readText(Charsets.UTF_8)), formatVersion)
            validateBackupData(data, payloadDirectory)
            if (data.counts() != counts || catalogOptionCount?.let { it != data.secenekler.size } == true) {
                throw IOException("Manifest sayıları içerikle uyuşmuyor.")
            }
            val teacherZipPaths = data.ogretmenVerisi.sorular
                .flatMap { it.gorseller }
                .distinctBy { it.dosyaAdi }
                .map { com.keremtulu.questionnote.data.prefs.OgretmenDeposu.zipAdi(it.dosyaAdi) }
            if (teacherZipPaths.size != teacherImageCount) {
                throw IOException("Öğretmen görsel sayısı manifest ile uyuşmuyor.")
            }
            // A teacher question may reuse a student image file, so the union is compared, not the sum.
            val dataImagePaths = (data.images.map { it.zipFileName } + teacherZipPaths).toSet()
            if (dataImagePaths != imageEntries.toSet()) {
                throw IOException("JSON görsel ilişkileri ZIP girdileriyle uyuşmuyor.")
            }
            return VerifiedArchive(
                data = data,
                summary = YedekOzet(archive.name, createdAt, archive.length(), counts),
            )
        }
    }

    private fun parseInventory(json: JSONArray): List<ArchiveFileInfo> {
        if (json.length() > MAX_ARCHIVE_ENTRIES - 1) throw IOException("Manifest dosya sayısı çok büyük.")
        val items = ArrayList<ArchiveFileInfo>(json.length())
        val paths = mutableSetOf<String>()
        for (index in 0 until json.length()) {
            val item = json.getJSONObject(index)
            val path = item.getString("path")
            val size = item.getLong("size_bytes")
            val hash = item.getString("sha256").lowercase()
            if (!isSafeZipPath(path) || path == MANIFEST_FILE || !paths.add(path)) {
                throw IOException("Manifest dosya yolu yinelenmiş veya güvenli değil.")
            }
            if (size < 0L || size > MAX_ENTRY_BYTES || !SHA256.matches(hash)) {
                throw IOException("Manifest checksum veya boyut geçersiz: $path")
            }
            items += ArchiveFileInfo(path, size, hash)
        }
        return items
    }

    private fun parseData(json: JSONObject, formatVersion: Int): BackupData = try {
        val lessons = json.getJSONArray("lessons").toTypedList(MAX_ROWS_PER_TABLE) { item ->
            DersEntity(
                id = item.getString("id"),
                ad = item.getString("name"),
                sira = item.getInt("order"),
                arsivli = item.getBoolean("archived"),
                olusturma = item.getLong("created_at_epoch_ms"),
                guncelleme = item.getLong("updated_at_epoch_ms"),
            )
        }
        val topics = json.getJSONArray("topics").toTypedList(MAX_ROWS_PER_TABLE) { item ->
            KonuEntity(
                id = item.getString("id"),
                dersId = item.getString("lesson_id"),
                ad = item.getString("name"),
                sira = item.getInt("order"),
                arsivli = item.getBoolean("archived"),
                olusturma = item.getLong("created_at_epoch_ms"),
                guncelleme = item.getLong("updated_at_epoch_ms"),
            )
        }
        val questions = json.getJSONArray("questions").toTypedList(MAX_ROWS_PER_TABLE) { item ->
            val plan = item.getString("plan_status")
            val learning = item.getString("learning_status")
            if (plan !in SoruPlanDurumu.entries.map { it.name } || learning !in OgrenmeDurumu.entries.map { it.name }) {
                throw IOException("Yedekte bilinmeyen soru planı durumu var.")
            }
            SoruEntity(
                id = item.getString("id"),
                dersId = item.getString("lesson_id"),
                konuId = item.nullableString("topic_id"),
                soruNotu = item.nullableString("question_note"),
                cozumNotu = item.nullableString("solution_note"),
                kaynak = item.nullableString("source"),
                kaynakKonumu = item.nullableString("source_location"),
                yanlisNedeni = item.nullableString("mistake_reason"),
                favori = item.getBoolean("favorite"),
                planDurumu = plan,
                ogrenmeDurumu = learning,
                sonTekrarGun = item.nullableInt("last_review_day"),
                sonrakiTekrarGun = item.nullableInt("next_review_day"),
                olusturma = item.getLong("created_at_epoch_ms"),
                guncelleme = item.getLong("updated_at_epoch_ms"),
            )
        }
        val options = when (formatVersion) {
            2 -> legacyCatalogOptions(questions)
            3, 4, 5, 6 -> {
            json.getJSONArray("catalog_options").toTypedList(MAX_ROWS_PER_TABLE) { item ->
                val type = runCatching { SoruSecenekTuru.valueOf(item.getString("type")) }
                    .getOrElse { throw IOException("Yedekte bilinmeyen katalog türü var.") }
                SoruSecenegiEntity(
                    id = item.getString("id"),
                    tur = type.name,
                    ad = item.getString("name"),
                    normalizeAd = item.getString("normalized_name"),
                    varsayilan = item.getBoolean("is_default"),
                    sira = item.getInt("order"),
                    olusturma = item.getLong("created_at_epoch_ms"),
                )
            }
        }
            else -> throw IOException("Desteklenmeyen katalog sürümü.")
        }
        val images = json.getJSONArray("images").toTypedList(MAX_IMAGES) { item ->
            val type = runCatching { GorselTuru.valueOf(item.getString("type")) }
                .getOrElse { throw IOException("Yedekte bilinmeyen görsel türü var.") }
            BackupImage(
                id = item.getString("id"),
                questionId = item.getString("question_id"),
                tur = type,
                sira = item.getInt("order"),
                zipFileName = item.getString("file"),
                mimeType = item.getString("mime_type"),
                width = item.getInt("width"),
                height = item.getInt("height"),
                sizeBytes = item.getLong("size_bytes"),
                sha256 = item.getString("sha256").lowercase(),
                createdAt = item.getLong("created_at_epoch_ms"),
            )
        }
        val reviews = json.getJSONArray("reviews").toTypedList(MAX_ROWS_PER_TABLE) { item ->
            val result = item.getString("result")
            if (result !in TekrarSonucu.entries.map { it.name }) throw IOException("Yedekte bilinmeyen tekrar sonucu var.")
            val algorithmVersion = item.getInt("algorithm_version")
            if (algorithmVersion <= 0) throw IOException("Tekrar algoritması sürümü geçersiz.")
            TekrarEntity(
                id = item.getString("id"),
                soruId = item.getString("question_id"),
                zaman = item.getLong("time_epoch_ms"),
                gun = item.getInt("day"),
                planlananGun = item.nullableInt("scheduled_day"),
                sonuc = result,
                cozumAcildi = item.getBoolean("solution_opened"),
                oncekiAralik = item.nullableInt("previous_interval_days"),
                sonrakiAralik = item.nullableInt("next_interval_days"),
                algoritmaSurumu = algorithmVersion,
                elleAtandi = item.getBoolean("manually_assigned"),
            )
        }
        val measurements = when (formatVersion) {
            2, 3 -> {
                if (json.has("measurements")) throw IOException("Eski biçim ölçüm tablosu içeremez.")
                emptyList()
            }
            4, 5, 6 -> json.getJSONArray("measurements").toTypedList(MAX_ROWS_PER_TABLE, OlcumSerilestirme::oku)
            else -> throw IOException("Desteklenmeyen ölçüm tablosu sürümü.")
        }
        val mockExams = if (json.has("mock_exams") && !json.isNull("mock_exams")) {
            json.getJSONArray("mock_exams").toTypedList(MAX_ROWS_PER_TABLE) { item ->
                com.keremtulu.questionnote.data.prefs.DenemeDeposu.jsonOku(item, strict = true)
            }
        } else {
            emptyList()
        }
        val rawExam = if (formatVersion == RECOVERY_FORMAT_VERSION) {
            if (!json.has("mock_exams_corrupt_raw") || json.isNull("mock_exams_corrupt_raw") || mockExams.isNotEmpty()) {
                throw IOException("Ham deneme verisi güvenlik yedeğinde eksik veya çelişkili.")
            }
            json.getJSONObject("mock_exams_corrupt_raw").toString().also(::validateRawMockExams)
        } else {
            if (json.has("mock_exams_corrupt_raw")) throw IOException("Beklenmeyen ham deneme verisi.")
            null
        }
        val teacherWorkspace = if (json.has("teacher_workspace") && !json.isNull("teacher_workspace")) {
            val bildirilenSurum = if (json.has("teacher_workspace_version")) {
                json.getInt("teacher_workspace_version")
            } else {
                1
            }
            if (bildirilenSurum > com.keremtulu.questionnote.data.prefs.OgretmenDeposu.SURUM) {
                throw IOException("Yedek, bu uygulamanın okuyamadığı yeni bir öğretmen çalışma alanı sürümü içeriyor.")
            }
            com.keremtulu.questionnote.data.prefs.OgretmenDeposu.jsonOku(json.getJSONObject("teacher_workspace"))
        } else {
            com.keremtulu.questionnote.domain.model.OgretmenVerisi()
        }
        BackupData(
            dersler = lessons,
            konular = topics,
            sorular = questions,
            secenekler = options,
            images = images,
            tekrarlar = reviews,
            olcumler = measurements,
            denemeler = mockExams,
            denemeHam = rawExam,
            ogretmenVerisi = teacherWorkspace,
        )
    } catch (error: IOException) {
        throw error
    } catch (error: Exception) {
        throw IOException("data.json okunamadı veya alanları geçersiz.", error)
    }

    /** v2 backups had no catalog; rebuild reusable choices from their saved question labels. */
    private fun legacyCatalogOptions(questions: List<SoruEntity>): List<SoruSecenegiEntity> {
        val now = System.currentTimeMillis()
        val options = linkedMapOf<Pair<String, String>, SoruSecenegiEntity>()
        varsayilanSoruSecenekleri(now).forEach { option ->
            options[option.tur to option.normalizeAd] = option
        }
        fun add(type: SoruSecenekTuru, name: String?) {
            if (name.isNullOrBlank() || name.length > SORU_SECENEGI_ADI_UST_SINIR) return
            val normalized = normalizeSoruSecenegiAdi(name)
            if (normalized.isBlank()) return
            val key = type.name to normalized
            if (key !in options) {
                options[key] = SoruSecenegiEntity(
                    id = stableSoruSecenegiId(type, normalized),
                    tur = type.name,
                    ad = name,
                    normalizeAd = normalized,
                    varsayilan = false,
                    sira = 0,
                    olusturma = now,
                )
            }
        }
        questions.forEach { question ->
            add(SoruSecenekTuru.KAYNAK, question.kaynak)
            add(SoruSecenekTuru.YANLIS_NEDENI, question.yanlisNedeni)
        }
        return options.values.toList()
    }

    private fun validateRawMockExams(raw: String) {
        if (raw.toByteArray(Charsets.UTF_8).size > MAX_CORRUPT_EXAM_BYTES) {
            throw IOException("Bozuk deneme verisi güvenlik yedeği için çok büyük.")
        }
        com.keremtulu.questionnote.data.prefs.DenemeDeposu.hamYedekDogrula(raw)
    }

    private fun validateBackupData(data: BackupData, extractedPayload: File?) {
        data.denemeHam?.let {
            if (data.denemeler.isNotEmpty()) throw IOException("Ham ve geçerli deneme verileri birlikte bulunamaz.")
            validateRawMockExams(it)
        }
        if (data.dersler.isEmpty()) throw IOException("Yedek en az bir ders içermeli.")
        val lessonIds = uniqueIds(data.dersler.map { it.id }, "ders")
        val topicIds = uniqueIds(data.konular.map { it.id }, "konu")
        val questionIds = uniqueIds(data.sorular.map { it.id }, "soru")
        uniqueIds(data.images.map { it.id }, "görsel")
        uniqueIds(data.tekrarlar.map { it.id }, "tekrar")
        uniqueIds(data.secenekler.map { it.id }, "katalog seçeneği")
        if (data.images.size > MAX_IMAGES) throw IOException("Yedekte izin verilenden fazla görsel var.")
        if (data.secenekler.size > MAX_ROWS_PER_TABLE) throw IOException("Yedekte katalog satırı sınırı aşıldı.")
        val normalizedOptionNames = mutableSetOf<Pair<String, String>>()
        data.secenekler.forEach { option ->
            val type = runCatching { SoruSecenekTuru.valueOf(option.tur) }
                .getOrElse { throw IOException("Yedekte bilinmeyen katalog türü var.") }
            val normalized = normalizeSoruSecenegiAdi(option.ad)
            if (option.ad.isBlank() || option.ad.length > SORU_SECENEGI_ADI_UST_SINIR || normalized.isBlank() ||
                option.normalizeAd != normalized || option.sira < 0 || option.olusturma < 0L ||
                (option.varsayilan && type != SoruSecenekTuru.YANLIS_NEDENI) ||
                !normalizedOptionNames.add(type.name to normalized)
            ) {
                throw IOException("Yedekte geçersiz veya yinelenen katalog seçeneği var.")
            }
        }
        val expectedDefaultReasons = VarsayilanSoruNedenleri.map(::normalizeSoruSecenegiAdi).toSet()
        val actualDefaultReasons = data.secenekler
            .filter { it.varsayilan }
            .map { it.normalizeAd }
            .toSet()
        if (actualDefaultReasons != expectedDefaultReasons) {
            throw IOException("Yedekte hazır yanlış nedeni listesi eksik veya geçersiz.")
        }
        val lessons = data.dersler.associateBy { it.id }
        val topics = data.konular.associateBy { it.id }

        data.dersler.forEach { lesson ->
            if (lesson.ad.isBlank() || lesson.ad.length > MAX_TEXT_FIELD || lesson.sira < 0) {
                throw IOException("Yedekte geçersiz ders kaydı var.")
            }
        }
        data.konular.forEach { topic ->
            if (topic.dersId !in lessonIds || topic.ad.isBlank() || topic.ad.length > MAX_TEXT_FIELD || topic.sira < 0) {
                throw IOException("Yedekte ilişki veya alanı geçersiz konu kaydı var.")
            }
        }
        data.sorular.forEach { question ->
            if (question.dersId !in lessonIds || question.konuId?.let { it !in topicIds } == true) {
                throw IOException("Yedekte geçersiz ders/konu ilişkisi olan soru var.")
            }
            if (question.konuId != null && topics[question.konuId]?.dersId != question.dersId) {
                throw IOException("Soru, başka bir derse ait konuya bağlanmış.")
            }
            if (question.olusturma < 0L || question.guncelleme < 0L) throw IOException("Soru tarihi geçersiz.")
        }
        val seenImageOrders = mutableMapOf<Pair<String, GorselTuru>, MutableList<Int>>()
        data.images.forEach { image ->
            if (image.questionId !in questionIds || !IMAGE_ID.matches(image.id) ||
                image.zipFileName != "$IMAGE_DIRECTORY${image.id}.jpg" ||
                image.mimeType != JPEG_MIME || image.width !in 1..MAX_IMAGE_EDGE ||
                image.height !in 1..MAX_IMAGE_EDGE || image.sizeBytes <= 0L ||
                image.sizeBytes > MAX_ENTRY_BYTES || !SHA256.matches(image.sha256) || image.sira < 0
            ) {
                throw IOException("Yedekte görsel kaydı geçersiz: ${image.id}")
            }
            seenImageOrders.getOrPut(image.questionId to image.tur) { mutableListOf() }.add(image.sira)
            if (extractedPayload != null) {
                val file = File(extractedPayload, image.zipFileName)
                if (!file.isFile || file.length() != image.sizeBytes || sha256(file) != image.sha256) {
                    throw IOException("Görsel içeriği JSON metadata ile uyuşmuyor: ${image.id}")
                }
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.absolutePath, bounds)
                if (bounds.outWidth != image.width || bounds.outHeight != image.height) {
                    throw IOException("Görsel çözünürlüğü JSON metadata ile uyuşmuyor: ${image.id}")
                }
            }
        }
        seenImageOrders.forEach { (key, orders) ->
            if (orders.sorted() != orders.indices.toList()) throw IOException("Görsel sırası kesintili veya yinelenmiş: ${key.first}")
        }
        data.sorular.forEach { question ->
            if (data.images.none { it.questionId == question.id && it.tur == GorselTuru.SORU }) {
                throw IOException("Soru görseli olmayan kayıt bulundu: ${question.id}")
            }
        }
        if (data.olcumler.size > MAX_ROWS_PER_TABLE) throw IOException("Ölçüm tablosu sınırı aşıldı.")
        uniqueIds(data.olcumler.map { it.tekrarId }, "ölçüm")
        uniqueIds(data.olcumler.map { it.denemeId }, "deneme")
        if (data.denemeler.size > MAX_ROWS_PER_TABLE) throw IOException("Deneme kaydı sınırı aşıldı.")
        uniqueIds(data.denemeler.map { it.id }, "deneme kaydı")
        data.denemeler.forEach { sinav ->
            if (sinav.ad.isBlank() || sinav.ad.length > MAX_TEXT_FIELD ||
                sinav.olusturma < 0L || sinav.gun < 0 || sinav.satirlar.isEmpty()
            ) {
                throw IOException("Yedekte geçersiz deneme kaydı var.")
            }
            uniqueIds(sinav.satirlar.map { it.dersId }, "deneme ders satırı")
            if (sinav.satirlar.any { !it.gecerli() || (it.dersAdi != null && it.dersAdi.length > MAX_TEXT_FIELD) }) {
                throw IOException("Yedekte geçersiz deneme ders skoru var.")
            }
        }
        if (data.ogretmenVerisi.gruplar.size > MAX_ROWS_PER_TABLE ||
            data.ogretmenVerisi.sorular.size > MAX_ROWS_PER_TABLE
        ) {
            throw IOException("Yedekte öğretmen kaydı sınırı aşıldı.")
        }
        val teacherGroupIds = uniqueIds(data.ogretmenVerisi.gruplar.map { it.id }, "öğretmen grubu")
        data.ogretmenVerisi.gruplar.forEach { g ->
            if (g.ad.isBlank() || g.ad.length > MAX_TEXT_FIELD) {
                throw IOException("Yedekte geçersiz öğretmen grubu var.")
            }
        }
        uniqueIds(data.ogretmenVerisi.sorular.map { it.id }, "öğretmen sorusu")
        data.ogretmenVerisi.sorular.forEach { s ->
            if (s.grupId !in teacherGroupIds || s.gorseller.isEmpty() || s.gorseller.size > 20 ||
                s.soruGorselleri().isEmpty() || s.cozumGorselleri().size + s.soruGorselleri().size != s.gorseller.size
            ) {
                throw IOException("Yedekte geçersiz öğretmen sorusu var: ${s.id}")
            }
            s.gorseller.forEach { img ->
                if (!TEACHER_PERMANENT_IMAGE_PATH.matches(img.dosyaAdi) ||
                    img.mimeType != JPEG_MIME || img.genislik !in 1..MAX_IMAGE_EDGE ||
                    img.yukseklik !in 1..MAX_IMAGE_EDGE || img.boyut !in 1L..MAX_ENTRY_BYTES ||
                    !SHA256.matches(img.sha256)
                ) {
                    throw IOException("Yedekte öğretmen görsel kaydı geçersiz: ${img.id}")
                }
                if (extractedPayload != null) {
                    val zipPath = com.keremtulu.questionnote.data.prefs.OgretmenDeposu.zipAdi(img.dosyaAdi)
                    val file = File(extractedPayload, zipPath)
                    if (!file.isFile || file.length() != img.boyut || sha256(file) != img.sha256) {
                        throw IOException("Öğretmen görsel içeriği JSON metadata ile uyuşmuyor: ${img.id}")
                    }
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.absolutePath, bounds)
                    if (bounds.outWidth != img.genislik || bounds.outHeight != img.yukseklik) {
                        throw IOException("Öğretmen görsel çözünürlüğü uyuşmuyor: ${img.id}")
                    }
                }
            }
        }
        val reviewsById = data.tekrarlar.associateBy { it.id }
        data.olcumler.forEach { olcum ->
            val tekrar = reviewsById[olcum.tekrarId] ?: throw IOException("Yetim ölçüm kaydı.")
            try { OlcumSerilestirme.dogrula(olcum, tekrar) }
            catch (error: Exception) { throw IOException("Ölçüm/bağlam/karar kaydı geçersiz.", error) }
        }
        val reviewDays = mutableSetOf<Pair<String, Int>>()
        data.tekrarlar.forEach { review ->
            if (review.soruId !in questionIds || review.gun < 0 || review.zaman < 0L ||
                !reviewDays.add(review.soruId to review.gun) ||
                review.oncekiAralik?.let { it <= 0 } == true || review.sonrakiAralik?.let { it <= 0 } == true
            ) {
                throw IOException("Yedekte tekrar ilişkisi veya günü geçersiz.")
            }
        }
    }

    private fun parseCounts(json: JSONObject): YedekSayilari = try {
        json.toCounts()
    } catch (error: Exception) {
        throw IOException("Manifest sayıları okunamadı.", error)
    }

    private fun JSONObject.toCounts(formatVersion: Int = FORMAT_VERSION) = YedekSayilari(
        dersSayisi = getInt("lessons"),
        konuSayisi = getInt("topics"),
        soruSayisi = getInt("questions"),
        gorselSayisi = getInt("images"),
        tekrarSayisi = getInt("reviews"),
        olcumSayisi = if (formatVersion >= 4) OlcumSerilestirme.tamInt(this, "measurements") else 0,
    ).also { counts ->
        if (listOf(counts.dersSayisi, counts.konuSayisi, counts.soruSayisi, counts.gorselSayisi, counts.tekrarSayisi, counts.olcumSayisi).any { it < 0 }) {
            throw IOException("Manifest içindeki sayı negatif.")
        }
    }

    private fun JSONArray.toStringSet(): Set<String> = buildSet {
        for (index in 0 until length()) {
            val value = getString(index)
            if (!add(value)) throw IOException("Manifest CSV listesinde yinelenen yol var.")
        }
    }

    private fun <T> JSONArray.toTypedList(max: Int, transform: (JSONObject) -> T): List<T> {
        if (length() > max) throw IOException("Yedekte bir tablo için satır sınırı aşıldı.")
        return List(length()) { index -> transform(getJSONObject(index)) }
    }

    private fun JSONObject.nullableString(key: String): String? =
        if (isNull(key)) null else getString(key)

    private fun JSONObject.nullableInt(key: String): Int? =
        if (isNull(key)) null else getInt(key)

    private fun uniqueIds(ids: List<String>, type: String): Set<String> {
        if (ids.any { it.isBlank() || it.length > MAX_ID_LENGTH }) throw IOException("Yedekte geçersiz $type kimliği var.")
        val result = ids.toSet()
        if (result.size != ids.size) throw IOException("Yedekte yinelenen $type kimliği var.")
        return result
    }

    private fun readEntryBytes(zip: ZipFile, entry: ZipEntry, limit: Int): ByteArray {
        val output = ByteArrayOutputStream()
        zip.getInputStream(entry).use { input ->
            val buffer = ByteArray(IO_BUFFER_SIZE)
            var total = 0
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                if (total > limit) throw IOException("ZIP girdisi üst sınırı aşıyor: ${entry.name}")
                output.write(buffer, 0, count)
            }
        }
        return output.toByteArray()
    }

    private fun copyZipEntry(zip: ZipFile, entry: ZipEntry, destination: File, maxBytes: Long): Pair<Long, String> {
        val digest = MessageDigest.getInstance("SHA-256")
        var total = 0L
        val out = FileOutputStream(destination)
        try {
            zip.getInputStream(entry).use { input ->
                val buffer = ByteArray(IO_BUFFER_SIZE)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > maxBytes || total > MAX_TOTAL_UNCOMPRESSED_BYTES) {
                        throw IOException("ZIP içeriği beyan edilen boyutu aşıyor: ${entry.name}")
                    }
                    digest.update(buffer, 0, count)
                    out.write(buffer, 0, count)
                }
            }
            out.fd.sync()
        } finally {
            out.close()
        }
        return total to digest.digest().toHex()
    }

    private fun copyBounded(input: java.io.InputStream, destination: File, limit: Long) {
        destination.parentFile?.mkdirs()
        val temp = File(destination.parentFile, "${destination.name}.pending")
        temp.delete()
        val output = FileOutputStream(temp)
        try {
            val buffer = ByteArray(IO_BUFFER_SIZE)
            var total = 0L
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                if (total > limit) throw IOException("Yedek dosyası üst boyut sınırını aşıyor.")
                val usableSpace = destination.parentFile?.usableSpace ?: 0L
                if (usableSpace > 0L && count.toLong() > usableSpace) {
                    throw IOException("Yedek kopyalanırken cihazda yeterli boş alan kalmadı.")
                }
                output.write(buffer, 0, count)
            }
            output.fd.sync()
        } catch (error: Exception) {
            temp.delete()
            throw error
        } finally {
            output.close()
        }
        java.nio.file.Files.move(temp.toPath(), destination.toPath(), java.nio.file.StandardCopyOption.ATOMIC_MOVE)
    }

    private fun estimatedArchiveBytes(uncompressedBytes: Long): Long =
        uncompressedBytes + maxOf(MIN_FREE_SPACE_RESERVE_BYTES, uncompressedBytes / 100L)

    private fun ensureFreeSpace(directory: File, requiredBytes: Long) {
        val usableBytes = directory.usableSpace
        if (usableBytes > 0L && requiredBytes > usableBytes) {
            throw IOException("İşlem için cihazda yeterli boş alan yok; gereken yaklaşık ${requiredBytes} bayt.")
        }
    }

    private fun isSafeZipPath(path: String): Boolean =
        path.isNotBlank() && !path.startsWith('/') && !path.contains('\\') &&
            !path.contains(':') && !path.contains('\u0000') &&
            path.split('/').none { it.isEmpty() || it == "." || it == ".." }

    private fun isAllowedPayloadPath(path: String): Boolean =
        path == DATA_FILE || path in REQUIRED_CSV_FILES || IMAGE_PATH.matches(path)

    private fun preparedDirectory(id: String): File {
        if (!UUID_PATTERN.matches(id)) throw IOException("Geri yükleme oturumu geçersiz.")
        return File(preparedRoot, id)
    }

    private fun writeJournal(journal: RestoreJournal) {
        val json = JSONObject()
            .put("id", journal.id)
            .put("state", journal.state)
            .put("safety_backup", journal.safetyBackupName)
            .put("prepared_id", journal.preparedId)
            .put("old_files", JSONArray(journal.oldFileNames))
            .put("new_files", JSONArray(journal.newFileNames))
            .put("old_teacher_files", JSONArray(journal.oldTeacherFileNames))
            .put("new_teacher_files", JSONArray(journal.newTeacherFileNames))
            .put("teacher_workspace", com.keremtulu.questionnote.data.prefs.OgretmenDeposu.jsonYaz(journal.teacherWorkspace))
            .put(
                "mock_exams",
                JSONArray().apply {
                    if (journal.mockExamsRaw != null) {
                        // Old app versions must fail closed instead of treating raw data as an empty list.
                        put(JSONObject().put("raw_recovery_required", true))
                    } else {
                        journal.mockExams.forEach {
                            put(com.keremtulu.questionnote.data.prefs.DenemeDeposu.jsonYaz(it))
                        }
                    }
                },
            )
            .apply { journal.mockExamsRaw?.let { put("mock_exams_raw", JSONObject(it)) } }
        writeAtomic(journalFile, json.toString().toByteArray(Charsets.UTF_8))
    }

    private fun readJournal(): RestoreJournal = try {
        val json = JSONObject(journalFile.readText(Charsets.UTF_8))
        val id = json.getString("id")
        val preparedId = json.getString("prepared_id")
        if (!UUID_PATTERN.matches(id) || preparedId != id) throw IOException("Geri yükleme günlüğü kimliği geçersiz.")
        val state = json.getString("state")
        if (state !in setOf(STATE_PREPARED, STATE_COMMITTED)) throw IOException("Geri yükleme aşaması geçersiz.")
        val safety = json.getString("safety_backup")
        if (!SAFETY_NAME.matches(safety)) throw IOException("Güvenlik yedeği yolu geçersiz.")
        val oldFiles = json.getJSONArray("old_files").toStringList()
        val newFiles = json.getJSONArray("new_files").toStringList()
        if (oldFiles.any { !PERMANENT_IMAGE_PATH.matches(it) } || newFiles.any { !PERMANENT_IMAGE_PATH.matches(it) }) {
            throw IOException("Geri yükleme günlüğü görsel yolları geçersiz.")
        }
        val oldTeacherFiles = json.optJSONArray("old_teacher_files")?.toStringList().orEmpty()
        val newTeacherFiles = json.optJSONArray("new_teacher_files")?.toStringList().orEmpty()
        if (oldTeacherFiles.any { !TEACHER_PERMANENT_IMAGE_PATH.matches(it) } ||
            newTeacherFiles.any { !TEACHER_PERMANENT_IMAGE_PATH.matches(it) }
        ) {
            throw IOException("Geri yükleme günlüğü öğretmen görsel yolları geçersiz.")
        }
        val teacherWorkspace = if (json.has("teacher_workspace") && !json.isNull("teacher_workspace")) {
            com.keremtulu.questionnote.data.prefs.OgretmenDeposu.jsonOku(json.getJSONObject("teacher_workspace"))
        } else {
            com.keremtulu.questionnote.domain.model.OgretmenVerisi()
        }
        val hamDeneme = if (json.has("mock_exams_raw") && !json.isNull("mock_exams_raw")) {
            val marker = json.getJSONArray("mock_exams")
            if (marker.length() != 1 || marker.getJSONObject(0).length() != 1 ||
                marker.getJSONObject(0).opt("raw_recovery_required") != true
            ) throw IOException("Ham deneme kurtarma günlüğü geçersiz.")
            json.getJSONObject("mock_exams_raw").toString().also(::validateRawMockExams)
        } else null
        val mockExams = if (hamDeneme != null) {
            emptyList()
        } else if (json.has("mock_exams") && !json.isNull("mock_exams")) {
            json.getJSONArray("mock_exams").toTypedList(MAX_ROWS_PER_TABLE) { item ->
                com.keremtulu.questionnote.data.prefs.DenemeDeposu.jsonOku(item, strict = true)
            }
        } else {
            emptyList()
        }
        RestoreJournal(
            id = id,
            state = state,
            safetyBackupName = safety,
            preparedId = preparedId,
            oldFileNames = oldFiles,
            newFileNames = newFiles,
            mockExams = mockExams,
            mockExamsRaw = hamDeneme,
            oldTeacherFileNames = oldTeacherFiles,
            newTeacherFileNames = newTeacherFiles,
            teacherWorkspace = teacherWorkspace,
        )
    } catch (error: IOException) {
        throw error
    } catch (error: Exception) {
        throw IOException("Geri yükleme günlüğü okunamıyor.", error)
    }

    private fun JSONArray.toStringList(): List<String> = List(length()) { getString(it) }

    private fun saveLastBackupInfo(info: SonYedekBilgisi) {
        val json = JSONObject()
            .put("file_name", info.dosyaAdi)
            .put("created_at_epoch_ms", info.olusturmaZamani)
            .put("size_bytes", info.boyutBayt)
            .put("safety_copy", info.guvenlikKopyasi)
        writeAtomic(lastBackupFile, json.toString().toByteArray(Charsets.UTF_8))
    }

    private fun writeAtomic(destination: File, bytes: ByteArray) {
        destination.parentFile?.mkdirs()
        val temp = File(destination.parentFile, "${destination.name}.pending")
        val out = FileOutputStream(temp)
        try {
            out.write(bytes)
            out.flush()
            out.fd.sync()
        } finally {
            out.close()
        }
        java.nio.file.Files.move(
            temp.toPath(),
            destination.toPath(),
            java.nio.file.StandardCopyOption.ATOMIC_MOVE,
            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
        )
    }

    private fun payloadFromBytes(name: String, bytes: ByteArray) =
        ArchivePayload(name, bytes, null, bytes.size.toLong(), sha256(bytes))

    private fun payloadFromFile(name: String, file: File, size: Long, sha: String) =
        ArchivePayload(name, null, file, size, sha)

    private fun ZipOutputStream.putBytes(name: String, bytes: ByteArray) {
        putNextEntry(ZipEntry(name))
        write(bytes)
        closeEntry()
    }

    private fun GorselEntity.toInput() = SoruGorselGirdisi(
        id = id,
        tur = runCatching { GorselTuru.valueOf(tur) }.getOrElse { throw IOException("Bilinmeyen görsel türü.") },
        sira = sira,
        dosyaAdi = dosyaAdi,
        gecici = false,
        mimeType = mimeType,
        genislik = genislik,
        yukseklik = yukseklik,
        boyut = boyut,
        sha256 = sha256,
    )

    private fun BackupImage.asInput(fileName: String) = SoruGorselGirdisi(
        id = id,
        tur = tur,
        sira = sira,
        dosyaAdi = fileName,
        gecici = false,
        mimeType = mimeType,
        genislik = width,
        yukseklik = height,
        boyut = sizeBytes,
        sha256 = sha256,
    )

    private fun BackupImage.toEntity(permanentFileName: String) = GorselEntity(
        id = id,
        soruId = questionId,
        tur = tur.name,
        sira = sira,
        dosyaAdi = permanentFileName,
        mimeType = mimeType,
        genislik = width,
        yukseklik = height,
        boyut = sizeBytes,
        sha256 = sha256,
        olusturma = createdAt,
    )

    private fun BackupImage.permanentFileName(restoreId: String): String {
        val stableId = UUID.nameUUIDFromBytes("$restoreId:$id".toByteArray(Charsets.UTF_8))
        return "img/$stableId.jpg"
    }

    private fun teacherPermanentFileName(restoreId: String, sourceDosyaAdi: String): String {
        val stableId = UUID.nameUUIDFromBytes("$restoreId:teacher:$sourceDosyaAdi".toByteArray(Charsets.UTF_8))
        return "ogretmen-img/$stableId.jpg"
    }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).toHex()

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(IO_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it.toInt() and 0xff) }

    private fun ExportSnapshot.counts() = YedekSayilari(dersler.size, konular.size, sorular.size, gorseller.size, tekrarlar.size, olcumler.size)
    private fun BackupData.counts() = YedekSayilari(dersler.size, konular.size, sorular.size, images.size, tekrarlar.size, olcumler.size)

    private data class ExportSnapshot(
        val dersler: List<DersEntity>,
        val konular: List<KonuEntity>,
        val sorular: List<SoruEntity>,
        val secenekler: List<SoruSecenegiEntity>,
        val gorseller: List<GorselEntity>,
        val tekrarlar: List<TekrarEntity>,
        val olcumler: List<TekrarOlcumEntity>,
        val denemeler: List<com.keremtulu.questionnote.domain.model.DenemeKaydi> = emptyList(),
        val denemeHam: String? = null,
        val ogretmenVerisi: com.keremtulu.questionnote.domain.model.OgretmenVerisi =
            com.keremtulu.questionnote.domain.model.OgretmenVerisi(),
    )

    private data class BackupData(
        val dersler: List<DersEntity>,
        val konular: List<KonuEntity>,
        val sorular: List<SoruEntity>,
        val secenekler: List<SoruSecenegiEntity>,
        val images: List<BackupImage>,
        val tekrarlar: List<TekrarEntity>,
        val olcumler: List<TekrarOlcumEntity>,
        val denemeler: List<com.keremtulu.questionnote.domain.model.DenemeKaydi> = emptyList(),
        val denemeHam: String? = null,
        val ogretmenVerisi: com.keremtulu.questionnote.domain.model.OgretmenVerisi =
            com.keremtulu.questionnote.domain.model.OgretmenVerisi(),
    )

    private data class BackupImage(
        val id: String,
        val questionId: String,
        val tur: GorselTuru,
        val sira: Int,
        val zipFileName: String,
        val mimeType: String,
        val width: Int,
        val height: Int,
        val sizeBytes: Long,
        val sha256: String,
        val createdAt: Long,
    )

    private data class ArchivePayload(
        val name: String,
        val bytes: ByteArray?,
        val file: File?,
        val sizeBytes: Long,
        val sha256: String,
    )

    private data class ArchiveFileInfo(val path: String, val sizeBytes: Long, val sha256: String)
    private data class VerifiedArchive(val data: BackupData, val summary: YedekOzet)
    private data class RestoreJournal(
        val id: String,
        val state: String,
        val safetyBackupName: String,
        val preparedId: String,
        val oldFileNames: List<String>,
        val newFileNames: List<String>,
        val mockExams: List<com.keremtulu.questionnote.domain.model.DenemeKaydi> = emptyList(),
        val mockExamsRaw: String? = null,
        val oldTeacherFileNames: List<String> = emptyList(),
        val newTeacherFileNames: List<String> = emptyList(),
        val teacherWorkspace: com.keremtulu.questionnote.domain.model.OgretmenVerisi =
            com.keremtulu.questionnote.domain.model.OgretmenVerisi(),
    )

    private companion object {
        const val BACKUP_DIRECTORY = "yedekler"
        const val PREPARED_DIRECTORY = "hazirlanan"
        const val EXPORT_DIRECTORY = "exports"
        const val RESTORE_JOURNAL = "geri-yukleme.json"
        const val LAST_BACKUP_INFO = "son-yedek.json"
        const val INPUT_ARCHIVE = "secili-yedek.zip"
        const val PAYLOAD_DIRECTORY = "icerik"
        const val MANIFEST_FILE = "manifest.json"
        const val DATA_FILE = "data.json"
        const val IMAGE_DIRECTORY = "images/"
        const val FORMAT_ID = "yanlis-defteri-backup"
        const val LEGACY_FORMAT_VERSION = 2
        const val LEGACY_ROOM_SCHEMA_VERSION = 2
        const val FORMAT_VERSION = 5
        // Only safety copies containing unreadable raw exams use v6; old readers reject it safely.
        const val RECOVERY_FORMAT_VERSION = 6
        const val ROOM_SCHEMA_VERSION = 5
        val SUPPORTED_FORMAT_VERSIONS = setOf(2, 3, 4, 5, 6)
        const val STATE_PREPARED = "PREPARED"
        const val STATE_COMMITTED = "COMMITTED"
        const val JPEG_MIME = "image/jpeg"
        const val MAX_ARCHIVE_BYTES = 512L * 1024L * 1024L
        const val MAX_TOTAL_UNCOMPRESSED_BYTES = 1_024L * 1024L * 1024L
        const val MAX_ENTRY_BYTES = 128L * 1024L * 1024L
        const val MAX_JSON_BYTES = 64L * 1024L * 1024L
        const val MAX_CORRUPT_EXAM_BYTES = 4 * 1024 * 1024
        const val MAX_MANIFEST_BYTES = 1 * 1024 * 1024
        const val MAX_ARCHIVE_ENTRIES = 10_009
        const val MAX_IMAGES = 10_000
        const val MAX_ROWS_PER_TABLE = 200_000
        const val MAX_TEXT_FIELD = 200_000
        const val MAX_ID_LENGTH = 128
        const val MAX_IMAGE_EDGE = 3_000
        const val MAX_COMPRESSION_RATIO = 1_000L
        const val MIN_FREE_SPACE_RESERVE_BYTES = 1L * 1024L * 1024L
        const val IO_BUFFER_SIZE = 16 * 1024
        val UUID_PATTERN = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
        val IMAGE_ID = UUID_PATTERN
        val IMAGE_PATH = Regex("images/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.jpg")
        val PERMANENT_IMAGE_PATH = Regex("img/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.jpg")
        // A teacher question may reference a shared student-archive image (`img/`) added from the archive.
        val TEACHER_PERMANENT_IMAGE_PATH = Regex(
            "(?:ogretmen-img|img)/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.jpg",
        )
        val SAFETY_NAME = Regex("safety-[0-9a-fA-F-]{36}\\.zip")
        val SHA256 = Regex("[0-9a-f]{64}")
        val LEGACY_REQUIRED_CSV_FILES = setOf(
            "csv/lessons.csv",
            "csv/topics.csv",
            "csv/questions.csv",
            "csv/images.csv",
            "csv/reviews.csv",
        )
        val V3_REQUIRED_CSV_FILES = LEGACY_REQUIRED_CSV_FILES + "csv/catalog_options.csv"
        val REQUIRED_CSV_FILES = V3_REQUIRED_CSV_FILES + "csv/measurements.csv"
        val CSV_FILES = REQUIRED_CSV_FILES.sorted()
    }
}

package com.keremtulu.questionnote.data.export

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.DersEntity
import com.keremtulu.questionnote.data.local.entity.GorselEntity
import com.keremtulu.questionnote.data.local.entity.KonuEntity
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.local.entity.TekrarEntity
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class YerelDisaAktarmaHizmeti(
    context: Context,
    private val database: YanlisDefteriDatabase,
    private val gorselDeposu: LocalSoruGorselDeposu,
) {
    private val appContext = context.applicationContext

    suspend fun zipOlustur(): File = withContext(Dispatchers.IO) {
        val snapshot = database.withTransaction {
            ExportSnapshot(
                dersler = database.dersDao().tumunuGetir(),
                konular = database.konuDao().tumunuGetir(),
                sorular = database.soruDao().tumunuGetir(),
                gorseller = database.gorselDao().tumunuGetir(),
                tekrarlar = database.tekrarDao().tumunuGetir(),
            )
        }
        val directory = File(appContext.cacheDir, "exports").apply { mkdirs() }
        val destination = File(directory, "yanlis-defteri-${UUID.randomUUID()}.zip")
        try {
            ZipOutputStream(BufferedOutputStream(destination.outputStream())).use { zip ->
                zip.putEntry("manifest.json", manifest(snapshot).toString(2))
                zip.putEntry("data.json", data(snapshot).toString())
                snapshot.gorseller.forEach { image ->
                    val input = image.toInput()
                    if (!gorselDeposu.kaliciDosyayiDogrula(input)) {
                        throw IOException("Eksik veya bozuk görsel bulundu: ${image.id}")
                    }
                    val file = gorselDeposu.dosyaGoruntuIcin(image.dosyaAdi)
                        ?: throw IOException("Görsel bulunamadı: ${image.id}")
                    val entryName = "images/${image.dosyaAdi.substringAfterLast('/')}"
                    zip.putNextEntry(ZipEntry(entryName))
                    BufferedInputStream(file.inputStream()).use { it.copyTo(zip, bufferSize = IO_BUFFER_SIZE) }
                    zip.closeEntry()
                }
            }
            destination
        } catch (error: Exception) {
            destination.delete()
            throw error
        }
    }

    suspend fun disariAktar(destination: Uri) = withContext(Dispatchers.IO) {
        val archive = zipOlustur()
        try {
            val output = appContext.contentResolver.openOutputStream(destination, "w")
                ?: throw IOException("Seçilen konuma yazılamadı.")
            output.use { out ->
                archive.inputStream().buffered().use { input -> input.copyTo(out, bufferSize = IO_BUFFER_SIZE) }
                out.flush()
            }
        } finally {
            archive.delete()
        }
    }

    private fun manifest(snapshot: ExportSnapshot) = JSONObject()
        .put("format", FORMAT_ID)
        .put("format_version", FORMAT_VERSION)
        .put("created_at_epoch_ms", System.currentTimeMillis())
        .put("room_schema_version", 1)
        .put("counts", JSONObject()
            .put("lessons", snapshot.dersler.size)
            .put("topics", snapshot.konular.size)
            .put("questions", snapshot.sorular.size)
            .put("images", snapshot.gorseller.size)
            .put("reviews", snapshot.tekrarlar.size))
        .put("data_file", "data.json")
        .put("image_directory", "images/")

    private fun data(snapshot: ExportSnapshot) = JSONObject()
        .put("lessons", JSONArray().apply { snapshot.dersler.forEach { put(it.toJson()) } })
        .put("topics", JSONArray().apply { snapshot.konular.forEach { put(it.toJson()) } })
        .put("questions", JSONArray().apply { snapshot.sorular.forEach { put(it.toJson()) } })
        .put("images", JSONArray().apply { snapshot.gorseller.forEach { put(it.toJson()) } })
        .put("reviews", JSONArray().apply { snapshot.tekrarlar.forEach { put(it.toJson()) } })

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
        .putNullable("mistake_reason", yanlisNedeni)
        .put("favorite", favori)
        .put("plan_status", planDurumu)
        .put("learning_status", ogrenmeDurumu)
        .putNullable("last_review_day", sonTekrarGun)
        .putNullable("next_review_day", sonrakiTekrarGun)
        .put("created_at_epoch_ms", olusturma)
        .put("updated_at_epoch_ms", guncelleme)

    private fun GorselEntity.toJson() = JSONObject()
        .put("id", id)
        .put("question_id", soruId)
        .put("type", tur)
        .put("order", sira)
        .put("file", "images/${dosyaAdi.substringAfterLast('/')}")
        .put("mime_type", mimeType)
        .put("width", genislik)
        .put("height", yukseklik)
        .put("size_bytes", boyut)
        .put("sha256", sha256)
        .put("created_at_epoch_ms", olusturma)

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

    private fun ZipOutputStream.putEntry(name: String, contents: String) {
        putNextEntry(ZipEntry(name))
        write(contents.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun GorselEntity.toInput() = SoruGorselGirdisi(
        id = id,
        tur = runCatching { GorselTuru.valueOf(tur) }.getOrDefault(GorselTuru.SORU),
        sira = sira,
        dosyaAdi = dosyaAdi,
        gecici = false,
        mimeType = mimeType,
        genislik = genislik,
        yukseklik = yukseklik,
        boyut = boyut,
        sha256 = sha256,
    )

    private data class ExportSnapshot(
        val dersler: List<DersEntity>,
        val konular: List<KonuEntity>,
        val sorular: List<SoruEntity>,
        val gorseller: List<GorselEntity>,
        val tekrarlar: List<TekrarEntity>,
    )

    private companion object {
        const val FORMAT_ID = "yanlis-defteri-export"
        const val FORMAT_VERSION = 1
        const val IO_BUFFER_SIZE = 16 * 1024
    }
}

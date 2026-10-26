package com.keremtulu.questionnote.data.images

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import com.keremtulu.questionnote.domain.repository.GorselDosyaYonetimi
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class KameraCekimi(
    val id: String,
    val uri: Uri,
)

/** Metadata of a stored teacher image; no hashing, so it is safe to list often. */
data class DepolananGorselDosyasi(
    val dosyaAdi: String,
    val boyut: Long,
    val sonDegisme: Long,
)

data class OgretmenDepolamaKullanimi(
    val adet: Int = 0,
    val bayt: Long = 0,
)

class LocalSoruGorselDeposu(context: Context) : GorselDosyaYonetimi {
    private val appContext = context.applicationContext
    private val kaliciDizin = File(appContext.filesDir, KALICI_DIZIN).apply { mkdirs() }
    private val ogretmenKaliciDizin = File(appContext.filesDir, OGRETMEN_KALICI_DIZIN).apply { mkdirs() }
    private val geciciDizin = File(appContext.cacheDir, GECICI_DIZIN).apply { mkdirs() }
    private val kameraDizin = File(appContext.cacheDir, KAMERA_DIZIN).apply { mkdirs() }

    suspend fun secicidenHazirla(
        uri: Uri,
        tur: GorselTuru,
        sira: Int,
    ): SoruGorselGirdisi = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val hamDosya = File(geciciDizin, "$id.source")
        val sonucDosyasi = File(geciciDizin, "$id.jpg")
        try {
            val input = appContext.contentResolver.openInputStream(uri)
                ?: throw IOException("Seçilen görsel okunamadı.")
            input.use { source ->
                BufferedInputStream(source).use { buffered ->
                    FileOutputStream(hamDosya).use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var total = 0L
                        while (true) {
                            val count = buffered.read(buffer)
                            if (count < 0) break
                            total += count
                            if (total > MAX_GIRDI_BOYUTU) throw IOException("Görsel dosyası çok büyük.")
                            output.write(buffer, 0, count)
                        }
                        output.fd.sync()
                    }
                }
            }
            optimizeEt(hamDosya, sonucDosyasi)
            sonucDosyasi.toGirdi(id, tur, sira)
        } catch (error: Exception) {
            sonucDosyasi.delete()
            throw error
        } finally {
            hamDosya.delete()
        }
    }

    suspend fun dosyadanHazirla(
        source: File,
        tur: GorselTuru,
        sira: Int,
    ): SoruGorselGirdisi = withContext(Dispatchers.IO) {
        if (!source.isFile || source.length() > MAX_GIRDI_BOYUTU) {
            throw IOException("Kaynak görsel okunamadı veya çok büyük.")
        }
        val id = UUID.randomUUID().toString()
        val staged = File(geciciDizin, "$id.jpg")
        try {
            optimizeEt(source, staged)
            staged.toGirdi(id, tur, sira)
        } catch (error: Exception) {
            staged.delete()
            throw error
        }
    }

    /**
     * Creates a cropped staging copy without ever modifying the selected or already-saved original.
     * Coordinates are normalized to the oriented JPEG that the form preview displays.
     */
    suspend fun kirp(
        gorsel: SoruGorselGirdisi,
        sol: Float,
        ust: Float,
        sag: Float,
        alt: Float,
    ): SoruGorselGirdisi = withContext(Dispatchers.IO) {
        val oranlar = listOf(sol, ust, sag, alt)
        require(oranlar.all { it.isFinite() } && sol >= 0f && ust >= 0f && sag <= 1f && alt <= 1f && sag > sol && alt > ust) {
            "Kırpma alanı görsel sınırlarının dışında."
        }
        val source = (if (gorsel.gecici) geciciDosyasi(gorsel.dosyaAdi) else tumKaliciDosyasi(gorsel.dosyaAdi))
            ?: throw IOException("Kırpılacak görsel bulunamadı.")
        if (gorsel.genislik <= 0 || gorsel.yukseklik <= 0 || !source.isFile || gorsel.mimeType != MIME_JPEG ||
            !boyutlarEslesiyor(source, gorsel.genislik, gorsel.yukseklik)
        ) {
            throw IOException("Kırpılacak görsel doğrulanamadı.")
        }
        val left = (sol * gorsel.genislik).toInt().coerceIn(0, gorsel.genislik - 1)
        val top = (ust * gorsel.yukseklik).toInt().coerceIn(0, gorsel.yukseklik - 1)
        val right = (sag * gorsel.genislik).toInt().coerceIn(left + 1, gorsel.genislik)
        val bottom = (alt * gorsel.yukseklik).toInt().coerceIn(top + 1, gorsel.yukseklik)
        if (left == 0 && top == 0 && right == gorsel.genislik && bottom == gorsel.yukseklik) return@withContext gorsel
        if (right - left < MIN_KIRPMA_KENARI || bottom - top < MIN_KIRPMA_KENARI) {
            throw IOException("Kırpma alanı çok küçük. Daha geniş bir alan seçin.")
        }

        val id = UUID.randomUUID().toString()
        val staged = File(geciciDizin, "$id.jpg")
        var decoder: BitmapRegionDecoder? = null
        var cropped: Bitmap? = null
        try {
            val cropBitmap = try {
                val regionDecoder = BitmapRegionDecoder.newInstance(source.absolutePath, false)
                    ?: throw IOException("Görsel kırpma için açılamadı.")
                decoder = regionDecoder
                try {
                    regionDecoder.decodeRegion(
                        Rect(left, top, right, bottom),
                        BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 },
                    )
                } catch (_: OutOfMemoryError) {
                    regionDecoder.decodeRegion(
                        Rect(left, top, right, bottom),
                        BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 },
                    )
                } ?: throw IOException("Seçilen kırpma alanı okunamadı.")
            } catch (_: Throwable) {
                var sample = 1
                val full = try {
                    BitmapFactory.decodeFile(
                        source.absolutePath,
                        BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 },
                    )
                } catch (_: OutOfMemoryError) {
                    sample = 2
                    BitmapFactory.decodeFile(
                        source.absolutePath,
                        BitmapFactory.Options().apply {
                            inSampleSize = sample
                            inPreferredConfig = Bitmap.Config.RGB_565
                        },
                    )
                } ?: throw IOException("Görsel kırpma için açılamadı.")
                var created: Bitmap? = null
                try {
                    val sLeft = (left / sample).coerceIn(0, (full.width - 1).coerceAtLeast(0))
                    val sTop = (top / sample).coerceIn(0, (full.height - 1).coerceAtLeast(0))
                    val sRight = (right / sample).coerceIn(sLeft + 1, full.width.coerceAtLeast(1))
                    val sBottom = (bottom / sample).coerceIn(sTop + 1, full.height.coerceAtLeast(1))
                    Bitmap.createBitmap(full, sLeft, sTop, sRight - sLeft, sBottom - sTop).also { created = it }
                } finally {
                    if (created !== full && !full.isRecycled) full.recycle()
                }
            }
            cropped = cropBitmap
            FileOutputStream(staged).use { output ->
                BufferedOutputStream(output).use { buffered ->
                    if (!cropBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, buffered)) {
                        throw IOException("Kırpılan görsel kaydedilemedi.")
                    }
                    buffered.flush()
                    output.fd.sync()
                }
            }
            if (staged.length() <= 0L) throw IOException("Kırpılan görsel boş.")
            staged.toGirdi(id, gorsel.tur, gorsel.sira)
        } catch (error: OutOfMemoryError) {
            staged.delete()
            throw IOException("Görsel bellekte güvenle kırpılamadı.", error)
        } catch (error: Exception) {
            staged.delete()
            throw error
        } catch (error: Throwable) {
            staged.delete()
            throw IOException("Görsel kırpılırken beklenmeyen hata oluştu.", error)
        } finally {
            cropped?.takeIf { !it.isRecycled }?.recycle()
            runCatching { decoder?.recycle() }
        }
    }

    fun kameraCekimiOlustur(): KameraCekimi {
        val id = UUID.randomUUID().toString()
        val file = kameraDosyasi(id)
        file.parentFile?.mkdirs()
        if (!file.createNewFile()) throw IOException("Kamera için geçici dosya oluşturulamadı.")
        val uri = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            file,
        )
        return KameraCekimi(id = id, uri = uri)
    }

    suspend fun kameradanHazirla(
        captureId: String,
        tur: GorselTuru,
        sira: Int,
    ): SoruGorselGirdisi = withContext(Dispatchers.IO) {
        val source = kameraDosyasi(captureId)
        if (!source.isFile || source.length() == 0L) throw IOException("Kamera görseli oluşturamadı.")
        val id = UUID.randomUUID().toString()
        val staged = File(geciciDizin, "$id.jpg")
        try {
            optimizeEt(source, staged)
            staged.toGirdi(id, tur, sira)
        } catch (error: Exception) {
            staged.delete()
            throw error
        } finally {
            source.delete()
        }
    }

    fun kameraCekiminiSil(captureId: String) {
        kameraDosyasi(captureId).delete()
    }

    fun dosyaGoruntuIcin(gorsel: SoruGorselGirdisi): File? {
        val file = (if (gorsel.gecici) geciciDosyasi(gorsel.dosyaAdi) else tumKaliciDosyasi(gorsel.dosyaAdi))
            ?: return null
        return file.takeIf { it.isFile }
    }

    fun dosyaGoruntuIcin(dosyaAdi: String): File? = tumKaliciDosyasi(dosyaAdi)?.takeIf { it.isFile }

    override suspend fun geciciDosyayiDogrula(gorsel: SoruGorselGirdisi): Boolean = withContext(Dispatchers.IO) {
        val file = geciciDosyasi(gorsel.dosyaAdi) ?: return@withContext false
        file.isFile && gorsel.mimeType == MIME_JPEG && file.length() == gorsel.boyut &&
            sha256(file) == gorsel.sha256 && boyutlarEslesiyor(file, gorsel.genislik, gorsel.yukseklik)
    }

    override suspend fun kaliciyaKopyala(geciciDosyaAdi: String): String? = withContext(Dispatchers.IO) {
        val source = geciciDosyasi(geciciDosyaAdi) ?: return@withContext null
        if (!source.isFile) return@withContext null
        val id = runCatching { UUID.fromString(geciciDosyaAdi.removeSuffix(".jpg")) }.getOrNull()
            ?: return@withContext null
        val name = "img/$id.jpg"
        val destination = kaliciDosyasi(name) ?: return@withContext null
        if (destination.exists()) return@withContext null
        val pending = File(kaliciDizin, "$id.pending")
        try {
            Files.copy(source.toPath(), pending.toPath())
            Files.move(pending.toPath(), destination.toPath(), ATOMIC_MOVE)
            name
        } catch (_: Exception) {
            pending.delete()
            null
        }
    }

    override suspend fun geciciDosyayiSil(geciciDosyaAdi: String) = withContext(Dispatchers.IO) {
        geciciDosyasi(geciciDosyaAdi)?.delete()
        Unit
    }

    override suspend fun kaliciDosyayiSil(dosyaAdi: String): Boolean = withContext(Dispatchers.IO) {
        val file = kaliciDosyasi(dosyaAdi) ?: return@withContext false
        !file.exists() || file.delete()
    }

    override suspend fun kaliciDosyayiDogrula(gorsel: SoruGorselGirdisi): Boolean = withContext(Dispatchers.IO) {
        val file = kaliciDosyasi(gorsel.dosyaAdi) ?: return@withContext false
        file.isFile && gorsel.mimeType == MIME_JPEG && file.length() == gorsel.boyut &&
            sha256(file) == gorsel.sha256 && boyutlarEslesiyor(file, gorsel.genislik, gorsel.yukseklik)
    }

    suspend fun yedektenKaliciyaKopyala(
        source: File,
        beklenen: SoruGorselGirdisi,
        hedefDosyaAdi: String? = null,
    ): String? = withContext(Dispatchers.IO) {
        if (
            !source.isFile ||
            source.length() != beklenen.boyut ||
            beklenen.mimeType != MIME_JPEG ||
            sha256(source) != beklenen.sha256 ||
            !boyutlarEslesiyor(source, beklenen.genislik, beklenen.yukseklik)
        ) return@withContext null
        val id = UUID.randomUUID()
        val name = hedefDosyaAdi ?: "img/$id.jpg"
        val destination = kaliciDosyasi(name) ?: return@withContext null
        if (destination.exists()) return@withContext null
        val pending = File(kaliciDizin, "${UUID.randomUUID()}.pending")
        try {
            Files.copy(source.toPath(), pending.toPath())
            Files.move(pending.toPath(), destination.toPath(), ATOMIC_MOVE)
            name
        } catch (_: Exception) {
            pending.delete()
            null
        }
    }

    suspend fun ogretmenKaliciyaKopyala(geciciDosyaAdi: String): String? = withContext(Dispatchers.IO) {
        val source = geciciDosyasi(geciciDosyaAdi) ?: return@withContext null
        if (!source.isFile) return@withContext null
        val id = runCatching { UUID.fromString(geciciDosyaAdi.removeSuffix(".jpg")) }.getOrNull()
            ?: return@withContext null
        val name = "$OGRETMEN_KALICI_DIZIN/$id.jpg"
        val destination = ogretmenKaliciDosyasi(name) ?: return@withContext null
        if (destination.exists()) return@withContext null
        ogretmenKaliciDizin.mkdirs()
        val pending = File(ogretmenKaliciDizin, "$id.pending")
        try {
            Files.copy(source.toPath(), pending.toPath())
            Files.move(pending.toPath(), destination.toPath(), ATOMIC_MOVE)
            name
        } catch (_: Exception) {
            pending.delete()
            null
        }
    }

    /**
     * Verifies any durable image by its own relative name, so a teacher question may reference a
     * shared `img/` file (added from the archive) or its own `ogretmen-img/` file.
     */
    suspend fun depolananDosyayiDogrula(gorsel: SoruGorselGirdisi): Boolean = withContext(Dispatchers.IO) {
        val file = tumKaliciDosyasi(gorsel.dosyaAdi) ?: return@withContext false
        file.isFile && gorsel.mimeType == MIME_JPEG && file.length() == gorsel.boyut &&
            sha256(file) == gorsel.sha256 && boyutlarEslesiyor(file, gorsel.genislik, gorsel.yukseklik)
    }

    suspend fun depolananDosyayiSil(dosyaAdi: String): Boolean = withContext(Dispatchers.IO) {
        val file = tumKaliciDosyasi(dosyaAdi) ?: return@withContext false
        !file.exists() || file.delete()
    }

    suspend fun ogretmenKaliciDosyayiDogrula(gorsel: SoruGorselGirdisi): Boolean = withContext(Dispatchers.IO) {
        val file = ogretmenKaliciDosyasi(gorsel.dosyaAdi) ?: return@withContext false
        file.isFile && gorsel.mimeType == MIME_JPEG && file.length() == gorsel.boyut &&
            sha256(file) == gorsel.sha256 && boyutlarEslesiyor(file, gorsel.genislik, gorsel.yukseklik)
    }

    suspend fun ogretmenKaliciDosyayiSil(dosyaAdi: String): Boolean = withContext(Dispatchers.IO) {
        val file = ogretmenKaliciDosyasi(dosyaAdi) ?: return@withContext false
        !file.exists() || file.delete()
    }

    suspend fun ogretmenYedektenKaliciyaKopyala(
        source: File,
        beklenen: SoruGorselGirdisi,
        hedefDosyaAdi: String? = null,
    ): String? = withContext(Dispatchers.IO) {
        if (
            !source.isFile ||
            source.length() != beklenen.boyut ||
            beklenen.mimeType != MIME_JPEG ||
            sha256(source) != beklenen.sha256 ||
            !boyutlarEslesiyor(source, beklenen.genislik, beklenen.yukseklik)
        ) return@withContext null
        val id = UUID.randomUUID()
        val name = hedefDosyaAdi ?: "$OGRETMEN_KALICI_DIZIN/$id.jpg"
        val destination = ogretmenKaliciDosyasi(name) ?: return@withContext null
        if (destination.exists()) return@withContext null
        ogretmenKaliciDizin.mkdirs()
        val pending = File(ogretmenKaliciDizin, "${UUID.randomUUID()}.pending")
        try {
            Files.copy(source.toPath(), pending.toPath())
            Files.move(pending.toPath(), destination.toPath(), ATOMIC_MOVE)
            name
        } catch (_: Exception) {
            pending.delete()
            null
        }
    }

    suspend fun ogretmenKaliciDosyalariListele(): List<String> = withContext(Dispatchers.IO) {
        ogretmenKaliciDizin.listFiles().orEmpty()
            .filter { it.isFile && it.name.matches(Regex("[0-9a-fA-F-]{36}\\.jpg")) }
            .map { "$OGRETMEN_KALICI_DIZIN/${it.name}" }
    }

    /** Cheap metadata listing (no hashing) used by the teacher storage card and orphan audit. */
    suspend fun ogretmenDosyaBilgileri(): List<DepolananGorselDosyasi> = withContext(Dispatchers.IO) {
        ogretmenKaliciDizin.listFiles().orEmpty()
            .filter { it.isFile && it.name.matches(Regex("[0-9a-fA-F-]{36}\\.jpg")) }
            .map { file ->
                DepolananGorselDosyasi(
                    dosyaAdi = "$OGRETMEN_KALICI_DIZIN/${file.name}",
                    boyut = file.length(),
                    sonDegisme = file.lastModified(),
                )
            }
    }

    suspend fun ogretmenToplamKullanim(): OgretmenDepolamaKullanimi = withContext(Dispatchers.IO) {
        val files = ogretmenDosyaBilgileri()
        OgretmenDepolamaKullanimi(adet = files.size, bayt = files.sumOf { it.boyut })
    }

    suspend fun kaliciDosyalariListele(): List<KaliciGorselDosyasi> = withContext(Dispatchers.IO) {
        kaliciDizin.listFiles().orEmpty()
            .filter { it.isFile && it.name.matches(Regex("[0-9a-fA-F-]{36}\\.jpg")) }
            .map { file ->
                val metadata = metaOku(file)
                KaliciGorselDosyasi(
                    dosyaAdi = "img/${file.name}",
                    boyut = metadata.boyut,
                    sonDegisme = file.lastModified(),
                    sha256 = metadata.sha256,
                    genislik = metadata.genislik,
                    yukseklik = metadata.yukseklik,
                )
            }
    }

    suspend fun eskiGecicileriTemizle(
        simdi: Long = System.currentTimeMillis(),
        enAzYas: Long = GECICI_YAS_SINIRI,
    ): Int = withContext(Dispatchers.IO) {
        val cutoff = simdi - enAzYas
        val staleStages = geciciDizin.listFiles().orEmpty().filter { it.lastModified() < cutoff }
        val staleCamera = kameraDizin.listFiles().orEmpty().filter { it.lastModified() < cutoff }
        (staleStages + staleCamera).count { it.delete() }
    }

    private fun bilinenGorselImzasiVar(file: File): Boolean {
        val header = ByteArray(16)
        val read = file.inputStream().use { input ->
            var total = 0
            while (total < header.size) {
                val count = input.read(header, total, header.size - total)
                if (count < 0) break
                total += count
            }
            total
        }
        fun startsWith(vararg signature: Int): Boolean =
            read >= signature.size && signature.indices.all { (header[it].toInt() and 0xff) == signature[it] }

        if (startsWith(0xff, 0xd8, 0xff) ||
            startsWith(0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a) ||
            startsWith(0x47, 0x49, 0x46, 0x38, 0x37, 0x61) ||
            startsWith(0x47, 0x49, 0x46, 0x38, 0x39, 0x61) ||
            startsWith(0x42, 0x4d)
        ) return true
        if (read >= 12 && String(header, 0, 4, Charsets.US_ASCII) == "RIFF" &&
            String(header, 8, 4, Charsets.US_ASCII) == "WEBP"
        ) return true
        if (read >= 12 && String(header, 4, 4, Charsets.US_ASCII) == "ftyp") {
            return String(header, 8, 4, Charsets.US_ASCII) in setOf("heic", "heix", "hevc", "hevx", "mif1", "msf1", "avif", "avis")
        }
        return false
    }

    private fun optimizeEt(kaynak: File, hedef: File) {
        if (!bilinenGorselImzasiVar(kaynak)) throw IOException("Dosya geçerli bir görsel değil.")
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(kaynak.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException("Dosya geçerli bir görsel değil.")
        var sampleSize = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sampleSize > MAX_DECODE_EDGE) sampleSize *= 2
        val decoded = try {
            BitmapFactory.decodeFile(
                kaynak.absolutePath,
                BitmapFactory.Options().apply { inSampleSize = sampleSize },
            )
        } catch (error: OutOfMemoryError) {
            throw IOException("Görsel güvenli biçimde işlenemeyecek kadar büyük.", error)
        } ?: throw IOException("Görsel çözümlenemedi.")
        var oriented: Bitmap? = null
        var scaled: Bitmap? = null
        var whiteBackground: Bitmap? = null
        try {
            val orientation = runCatching {
                ExifInterface(kaynak.absolutePath).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
            oriented = yonelt(decoded, orientation)
            val source = oriented ?: decoded
            val factor = minOf(1f, MAX_SAKLAMA_KENARI.toFloat() / maxOf(source.width, source.height))
            val resized = if (factor < 1f) {
                Bitmap.createScaledBitmap(
                    source,
                    (source.width * factor).toInt().coerceAtLeast(1),
                    (source.height * factor).toInt().coerceAtLeast(1),
                    true,
                ).also { scaled = it }
            } else {
                source
            }
            val jpegBitmap = if (resized.hasAlpha()) {
                Bitmap.createBitmap(resized.width, resized.height, Bitmap.Config.ARGB_8888).also { whiteBackground = it }
                    .also { canvasBitmap ->
                        Canvas(canvasBitmap).apply {
                            drawColor(Color.WHITE)
                            drawBitmap(resized, 0f, 0f, null)
                        }
                    }
            } else {
                resized
            }
            FileOutputStream(hedef).use { fileOutput ->
                BufferedOutputStream(fileOutput).use { buffered ->
                    if (!jpegBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, buffered)) {
                        throw IOException("Görsel sıkıştırılamadı.")
                    }
                    buffered.flush()
                    fileOutput.fd.sync()
                }
            }
            if (hedef.length() <= 0L) throw IOException("Sıkıştırılmış görsel boş.")
        } catch (error: OutOfMemoryError) {
            throw IOException("Görsel güvenli biçimde işlenemeyecek kadar büyük.", error)
        } finally {
            if (whiteBackground !== decoded && whiteBackground !== oriented && whiteBackground !== scaled) whiteBackground?.recycle()
            if (scaled !== decoded && scaled !== oriented) scaled?.recycle()
            if (oriented !== decoded) oriented?.recycle()
            decoded.recycle()
        }
    }

    private fun yonelt(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun File.toGirdi(id: String, tur: GorselTuru, sira: Int): SoruGorselGirdisi {
        val metadata = metaOku(this)
        if (metadata.genislik <= 0 || metadata.yukseklik <= 0) throw IOException("Sıkıştırılmış görsel okunamadı.")
        return SoruGorselGirdisi(
            id = id,
            tur = tur,
            sira = sira,
            dosyaAdi = name,
            gecici = true,
            mimeType = MIME_JPEG,
            genislik = metadata.genislik,
            yukseklik = metadata.yukseklik,
            boyut = metadata.boyut,
            sha256 = metadata.sha256,
        )
    }

    private fun metaOku(file: File): GorselMeta {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        return GorselMeta(
            genislik = options.outWidth,
            yukseklik = options.outHeight,
            boyut = file.length(),
            sha256 = sha256(file),
        )
    }

    private fun boyutlarEslesiyor(file: File, width: Int, height: Int): Boolean {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        return options.outWidth == width && options.outHeight == height
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }

    private fun geciciDosyasi(fileName: String): File? =
        fileName.takeIf { it.matches(Regex("[0-9a-fA-F-]{36}\\.jpg")) }
            ?.let { File(geciciDizin, it) }

    private fun kaliciDosyasi(relativeName: String): File? =
        relativeName.takeIf { it.matches(Regex("img/[0-9a-fA-F-]{36}\\.jpg")) }
            ?.let { File(kaliciDizin.parentFile, it) }

    private fun ogretmenKaliciDosyasi(relativeName: String): File? =
        relativeName.takeIf { it.matches(Regex("ogretmen-img/[0-9a-fA-F-]{36}\\.jpg")) }
            ?.let { File(ogretmenKaliciDizin.parentFile, it) }

    private fun tumKaliciDosyasi(relativeName: String): File? =
        kaliciDosyasi(relativeName) ?: ogretmenKaliciDosyasi(relativeName)

    private fun kameraDosyasi(id: String): File {
        val uuid = UUID.fromString(id)
        return File(kameraDizin, "$uuid.jpg")
    }

    private data class GorselMeta(
        val genislik: Int,
        val yukseklik: Int,
        val boyut: Long,
        val sha256: String,
    )

    companion object {
        private const val KALICI_DIZIN = "img"
        private const val OGRETMEN_KALICI_DIZIN = "ogretmen-img"
        private const val GECICI_DIZIN = "yanlis-defteri/staging"
        private const val KAMERA_DIZIN = "yanlis-defteri/camera"
        private const val MAX_GIRDI_BOYUTU = 100L * 1024L * 1024L
        private const val MAX_DECODE_EDGE = 4_096
        private const val MAX_SAKLAMA_KENARI = 3_000
        private const val MIN_KIRPMA_KENARI = 32
        private const val JPEG_QUALITY = 90
        private const val MIME_JPEG = "image/jpeg"
        private const val GECICI_YAS_SINIRI = 24L * 60L * 60L * 1000L
    }
}

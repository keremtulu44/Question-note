package com.keremtulu.questionnote.ui.ogretmen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.domain.model.OgretmenSorusu

/**
 * Local-only sharing of teacher questions.
 *
 * The whole feature is one `ACTION_SEND` / `ACTION_SEND_MULTIPLE` intent: no network call, no
 * account, no messenger-specific API, so it works with every app the teacher already has and the
 * Play listing keeps the "no data collected" claim.
 *
 * Privacy rules baked into the text:
 * - the student name ("kim sordu") is never shared,
 * - the answer is never shared,
 * - only question photos (plus solution photos on request) and lesson/topic/source labels go out.
 *
 * Photos are handed over as [FileProvider] content URIs with a read grant, never as file paths.
 */
internal object OgretmenPaylasim {
    /** Upper bound keeps a share small enough for messenger apps to accept. */
    const val MAKS_GORSEL = 9

    /** Human readable body: one block per question, grouped by the labels the teacher filled in. */
    fun metin(
        context: Context,
        sorular: List<OgretmenSorusu>,
        dersAdlari: Map<String, String> = emptyMap(),
        konuAdlari: Map<String, String> = emptyMap(),
    ): String {
        if (sorular.isEmpty()) return ""
        val bloklar = sorular.mapIndexed { index, soru ->
            val baslik = soru.baslik?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.teacher_untitled_question)
            val etiketler = listOfNotNull(
                soru.dersId?.let { dersAdlari[it] }?.takeIf { it.isNotBlank() }
                    ?.let { context.getString(R.string.teacher_share_lesson, it) },
                soru.konuId?.let { konuAdlari[it] }?.takeIf { it.isNotBlank() }
                    ?.let { context.getString(R.string.teacher_share_topic, it) },
                soru.kaynak?.takeIf { it.isNotBlank() }
                    ?.let { context.getString(R.string.teacher_share_source, it.trim()) },
            )
            val satirlar = mutableListOf<String>()
            satirlar += if (sorular.size > 1) "${index + 1}) $baslik" else baslik
            if (etiketler.isNotEmpty()) satirlar += etiketler.joinToString(" · ")
            satirlar.joinToString("\n")
        }
        return bloklar.joinToString("\n\n")
    }

    /**
     * Content URIs of the photos that will travel with the share; unreadable files are skipped.
     *
     * [uriUret] exists for tests: [FileProvider] caches its path strategy per authority for the
     * whole process, while Robolectric hands out a new files directory for every test method, so
     * the provider cannot be exercised twice in one JVM. Production always uses the default.
     */
    fun gorselUrisi(
        context: Context,
        sorular: List<OgretmenSorusu>,
        gorselDeposu: LocalSoruGorselDeposu,
        cozumDahil: Boolean = false,
        uriUret: ((java.io.File) -> Uri?)? = null,
    ): List<Uri> {
        val uret = uriUret ?: { dosya: java.io.File -> fileUri(context, dosya) }
        val uriler = ArrayList<Uri>(MAKS_GORSEL)
        for (soru in sorular) {
            val gorseller = if (cozumDahil) {
                soru.soruGorselleri() + soru.cozumGorselleri()
            } else {
                soru.soruGorselleri()
            }
            for (gorsel in gorseller) {
                if (uriler.size >= MAKS_GORSEL) return uriler
                val dosya = gorselDeposu.dosyaGoruntuIcin(gorsel.dosyaAdi) ?: continue
                val uri = runCatching { uret(dosya) }.getOrNull() ?: continue
                uriler += uri
            }
        }
        return uriler
    }

    /**
     * Share intent wrapped in a chooser, or `null` when there is nothing to share. Text-only falls
     * back to `text/plain` so the action never dies on a device without an image-capable messenger.
     */
    fun intent(
        context: Context,
        sorular: List<OgretmenSorusu>,
        gorselDeposu: LocalSoruGorselDeposu,
        dersAdlari: Map<String, String> = emptyMap(),
        konuAdlari: Map<String, String> = emptyMap(),
        cozumDahil: Boolean = false,
        uriUret: ((java.io.File) -> Uri?)? = null,
    ): Intent? {
        if (sorular.isEmpty()) return null
        val uriler = gorselUrisi(context, sorular, gorselDeposu, cozumDahil, uriUret)
        val metin = metin(context, sorular, dersAdlari, konuAdlari)
        val gonder = when {
            uriler.isEmpty() -> Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, metin)
            }
            uriler.size == 1 -> Intent(Intent.ACTION_SEND).apply {
                type = GORSEL_TURU
                putExtra(Intent.EXTRA_STREAM, uriler.first())
                putExtra(Intent.EXTRA_TEXT, metin)
            }
            else -> Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = GORSEL_TURU
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uriler))
                putExtra(Intent.EXTRA_TEXT, metin)
            }
        }
        gonder.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return Intent.createChooser(gonder, context.getString(R.string.teacher_share_chooser))
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    /** Launches the chooser; false means the device has no app that accepts it. */
    fun baslat(context: Context, intent: Intent): Boolean = runCatching {
        if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    }.getOrDefault(false)

    fun paylas(
        context: Context,
        sorular: List<OgretmenSorusu>,
        gorselDeposu: LocalSoruGorselDeposu,
        dersAdlari: Map<String, String> = emptyMap(),
        konuAdlari: Map<String, String> = emptyMap(),
        cozumDahil: Boolean = false,
    ): Boolean {
        val hazir = intent(context, sorular, gorselDeposu, dersAdlari, konuAdlari, cozumDahil)
            ?: return false
        return baslat(context, hazir)
    }

    private const val GORSEL_TURU = "image/jpeg"

    private fun fileUri(context: Context, dosya: java.io.File): Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        dosya,
    )
}

package com.keremtulu.questionnote.data.ses

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.annotation.VisibleForTesting
import com.keremtulu.questionnote.data.prefs.SesEfektiDeposu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * App-scoped short sound and haptic service. Short UI cues are preloaded into SoundPool;
 * longer ambient loops use MediaPlayer so they don't compete with the effects pool.
 * Sound playback respects the existing sound preference and silent/vibrate ringer modes.
 */
class SesYoneticisi private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = SesEfektiDeposu(appContext)
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val pool: SoundPool? = runCatching {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(attributes)
            .build()
    }.getOrNull()

    private val sampleIds = ConcurrentHashMap<SesTuruGenis, Int>()
    private val loadedSamples = ConcurrentHashMap.newKeySet<Int>()
    private var ambientPlayer: MediaPlayer? = null
    private var ambientResource: Int? = null
    private var resumeAmbientAfterPause = false

    init {
        pool?.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loadedSamples.add(sampleId)
        }
        SesTuruGenis.entries
            .filter { it.kategori != SesKategori.AMBIYANS }
            .forEach { type ->
                runCatching { pool?.load(appContext, type.kaynakId, 1) }
                    .getOrNull()
                    ?.takeIf { it > 0 }
                    ?.let { sampleIds[type] = it }
            }
    }

    /** Play one short effect. Pan is -1 (left) to +1 (right); volume is clamped to 0..1. */
    fun cal(
        tur: SesTuruGenis,
        volume: Float = 1f,
        pan: Float = 0f,
        rate: Float = 1f,
        loop: Boolean = false,
        scope: CoroutineScope? = null,
    ) {
        if (!prefs.acikMi() || !isAudioAllowed()) return
        val soundPool = pool ?: return
        val soundId = sampleIds[tur] ?: return
        if (!loadedSamples.contains(soundId)) return

        val clampedVolume = volume.coerceIn(0f, 1f)
        val clampedPan = pan.coerceIn(-1f, 1f)
        // Equal-power panning preserves perceived loudness while sounds move across the stereo field.
        val angle = (clampedPan + 1f) * (PI.toFloat() / 4f)
        val left = clampedVolume * cos(angle)
        val right = clampedVolume * sin(angle)
        runCatching {
            val streamId = soundPool.play(
                soundId,
                left,
                right,
                (4 - tur.oncelik).coerceAtLeast(1),
                if (loop || tur.looping) -1 else 0,
                rate.coerceIn(0.5f, 2f),
            )
            if (streamId > 0) soundPool.setStereoVolume(streamId, left, right)
        }
    }

    /** Play a cue with its category volume and optional haptic pattern. */
    fun calPremium(
        tur: SesTuruGenis,
        hapticType: HapticTipi? = null,
        scope: CoroutineScope? = null,
        pan: Float = 0f,
    ) {
        if (prefs.acikMi()) {
            val mediaVolume = runCatching {
                val current = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 1
                val maximum = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 1
                if (maximum <= 0) 0f else current.toFloat() / maximum
            }.getOrDefault(1f)
            val categoryVolume = when (tur.kategori) {
                SesKategori.KUTLAMA -> 0.9f
                SesKategori.BASARI -> 0.75f
                SesKategori.GERI_BILDIRIM -> 0.7f
                SesKategori.ETKILESIM -> 0.45f
                SesKategori.HATA -> 0.65f
                SesKategori.AMBIYANS -> 0.25f
            }
            if (mediaVolume > 0f) cal(tur, categoryVolume * mediaVolume, pan = pan, loop = tur.looping, scope = scope)
        }
        hapticType?.let { HapticYoneticisi.uygulama(appContext).tetkikle(it) }
    }

    /** Schedule distinct sound cues, e.g. for a badge or rank celebration. */
    fun calDizi(turler: List<Pair<SesTuruGenis, Long>>, scope: CoroutineScope) {
        scope.launch(Dispatchers.Main.immediate) {
            turler.forEach { (tur, gecikme) ->
                if (gecikme > 0L) delay(gecikme)
                calPremium(tur)
            }
        }
    }

    /** Start a low-volume ambient loop. Repeated calls with the same type are idempotent. */
    @Synchronized
    fun ambiyansBaslat(tur: SesTuruGenis = SesTuruGenis.AMBIYANS_ODAK) {
        if (tur.kategori != SesKategori.AMBIYANS || !prefs.acikMi() || !isAudioAllowed()) return
        if (ambientResource == tur.kaynakId && ambientPlayer?.isPlaying == true) return
        ambiyansDurdur()
        val player = runCatching { MediaPlayer.create(appContext, tur.kaynakId) }.getOrNull() ?: return
        runCatching {
            player.isLooping = true
            player.setVolume(0.18f, 0.18f)
            player.setOnErrorListener { failed, _, _ ->
                runCatching { failed.release() }
                synchronized(this) {
                    if (ambientPlayer === failed) {
                        ambientPlayer = null
                        ambientResource = null
                    }
                }
                true
            }
            player.start()
            ambientPlayer = player
            ambientResource = tur.kaynakId
            resumeAmbientAfterPause = true
        }.onFailure { runCatching { player.release() } }
    }

    @Synchronized
    fun ambiyansDurdur() {
        ambientPlayer?.let { player ->
            runCatching { if (player.isPlaying) player.stop() }
            runCatching { player.release() }
        }
        ambientPlayer = null
        ambientResource = null
        resumeAmbientAfterPause = false
    }

    fun hepsiniDurdur() {
        runCatching { pool?.autoPause() }
        synchronized(this) {
            ambientPlayer?.let { player ->
                resumeAmbientAfterPause = runCatching { player.isPlaying }.getOrDefault(false)
                runCatching { if (resumeAmbientAfterPause) player.pause() }
            }
        }
    }

    fun devamEt() {
        runCatching { pool?.autoResume() }
        synchronized(this) {
            if (resumeAmbientAfterPause) {
                runCatching { ambientPlayer?.start() }
                resumeAmbientAfterPause = false
            }
        }
    }

    /** Release native resources when the owning application process is shutting down. */
    @Synchronized
    fun serbestBirak() {
        ambiyansDurdur()
        runCatching { pool?.release() }
        loadedSamples.clear()
        sampleIds.clear()
    }

    fun acikMi(): Boolean = prefs.acikMi()

    fun ayarla(acik: Boolean) {
        prefs.ayarla(acik)
        if (!acik) ambiyansDurdur()
    }

    private fun isAudioAllowed(): Boolean {
        val ringerMode = runCatching { audioManager?.ringerMode }.getOrNull()
        return ringerMode == null || ringerMode == AudioManager.RINGER_MODE_NORMAL
    }

    companion object {
        @Volatile
        private var instance: SesYoneticisi? = null

        fun uygulama(context: Context): SesYoneticisi = instance ?: synchronized(this) {
            instance ?: SesYoneticisi(context.applicationContext).also { instance = it }
        }

        @VisibleForTesting
        fun sifirla() {
            synchronized(this) {
                instance?.serbestBirak()
                instance = null
            }
        }
    }
}

/** Haptic patterns use separate timings and amplitudes so intensity isn't confused with duration. */
enum class HapticTipi(
    internal val timings: LongArray,
    internal val intensity: Int = 150,
) {
    TIKLAMA(longArrayOf(0, 12), 90),
    TIKLAMA_AGIR(longArrayOf(0, 24), 170),
    BASARI(longArrayOf(0, 12, 38, 18), 150),
    BASARI_GUCLU(longArrayOf(0, 20, 36, 24, 36, 28), 220),
    HATA(longArrayOf(0, 45, 50, 45), 190),
    UYARI(longArrayOf(0, 25, 65, 25), 140),
    KUTLAMA(longArrayOf(0, 14, 32, 18, 40, 26, 55, 35), 220),
    RUTBE(longArrayOf(0, 18, 30, 20, 38, 26, 48, 32), 230),
    SWIPE(longArrayOf(0, 9), 75),
    PULL_REFRESH(longArrayOf(0, 12, 28, 12), 110),
    RELEASE_REFRESH(longArrayOf(0, 20, 32, 22), 160),
    KART_SECILDI(longArrayOf(0, 10), 95),
    SLIDER(longArrayOf(0, 7), 60),
    TOGGLE(longArrayOf(0, 11, 22, 9), 100),
}

class HapticYoneticisi private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val vibrator = appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    fun tetkikle(tipi: HapticTipi) {
        val deviceVibrator = vibrator ?: return
        if (!runCatching { deviceVibrator.hasVibrator() }.getOrDefault(false)) return
        val feedbackEnabled = runCatching {
            Settings.System.getInt(
                appContext.contentResolver,
                Settings.System.HAPTIC_FEEDBACK_ENABLED,
                1,
            ) == 1
        }.getOrDefault(true)
        if (!feedbackEnabled) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitudes = IntArray(tipi.timings.size) { index ->
                if (index % 2 == 0) 0 else tipi.intensity.coerceIn(1, 255)
            }
            runCatching {
                deviceVibrator.vibrate(VibrationEffect.createWaveform(tipi.timings, amplitudes, -1))
            }
        }
    }

    companion object {
        @Volatile
        private var instance: HapticYoneticisi? = null

        fun uygulama(context: Context): HapticYoneticisi = instance ?: synchronized(this) {
            instance ?: HapticYoneticisi(context.applicationContext).also { instance = it }
        }
    }
}

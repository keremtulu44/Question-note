package com.keremtulu.questionnote.data.ses

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.prefs.SesEfektiDeposu

enum class SesTuru(val kaynakId: Int) {
    TEKRAR(R.raw.ses_tekrar),
    HEDEF(R.raw.ses_hedef),
    KUTLAMA(R.raw.ses_rozet),
}

/**
 * Application-lifetime SoundPool for very short UI feedback sounds.
 * Plays only when the user toggle is on and the device ringer is in normal mode.
 * All playback is guarded so previews, Robolectric tests and missing resources stay silent instead of crashing.
 */
class SesHizmeti private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val depo = SesEfektiDeposu(appContext)
    private val audio = runCatching {
        appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }.getOrNull()
    private val pool: SoundPool? = runCatching {
        SoundPool.Builder().setMaxStreams(2).build()
    }.getOrNull()
    private val sesKimlikleri: Map<SesTuru, Int> = runCatching {
        val p = pool ?: return@runCatching emptyMap()
        SesTuru.entries.associateWith { p.load(appContext, it.kaynakId, 1) }
    }.getOrDefault(emptyMap())

    fun acikMi(): Boolean = depo.acikMi()

    fun ayarla(acik: Boolean) = depo.ayarla(acik)

    fun cal(tur: SesTuru) {
        if (!depo.acikMi()) return
        // Silent/vibrate mode: stay silent without touching media volume behaviour.
        if (audio?.ringerMode != AudioManager.RINGER_MODE_NORMAL) return
        val kimlik = sesKimlikleri[tur] ?: return
        runCatching { pool?.play(kimlik, 0.85f, 0.85f, 1, 0, 1f) }
    }

    companion object {
        @Volatile private var instance: SesHizmeti? = null

        fun uygulama(context: Context): SesHizmeti = instance ?: synchronized(this) {
            instance ?: SesHizmeti(context.applicationContext).also { instance = it }
        }
    }
}

package com.keremtulu.questionnote.data.prefs

import android.content.Context
import com.keremtulu.questionnote.domain.repeat.MotorAyari
import com.keremtulu.questionnote.domain.repeat.MotorYayinKapisi
import com.keremtulu.questionnote.domain.repeat.TekrarModuSaglayici
import com.keremtulu.questionnote.domain.repeat.TekrarMotorModu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/** Device-local opt-out. Active v3 cannot be enabled by editing a preference; the release gate wins. */
class TekrarPolitikasiDeposu(context: Context, private val kapi: MotorYayinKapisi = MotorYayinKapisi()) : TekrarModuSaglayici {
    private val prefs = context.applicationContext.getSharedPreferences("tekrar_politikasi", Context.MODE_PRIVATE)
    private val durum = MutableStateFlow(MotorAyari(TekrarMotorModu.entries.firstOrNull {
        it.name == prefs.getString("mod", null) } ?: TekrarMotorModu.GOLGE, kapi))
    val izle = durum.asStateFlow()
    override fun oku(): MotorAyari = durum.value
    suspend fun ayarla(mod: TekrarMotorModu): Boolean = withContext(Dispatchers.IO) {
        synchronized(this@TekrarPolitikasiDeposu) {
            if (mod == TekrarMotorModu.ETKIN && !kapi.etkinAcik) false else {
                check(prefs.edit().putString("mod", mod.name).commit())
                durum.value = MotorAyari(mod, kapi)
                true
            }
        }
    }
}

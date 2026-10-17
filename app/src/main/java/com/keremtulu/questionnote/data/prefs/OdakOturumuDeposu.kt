package com.keremtulu.questionnote.data.prefs

import android.content.Context
import com.keremtulu.questionnote.domain.calisma.OdakOturumu
import com.keremtulu.questionnote.domain.karne.OturumSonucu
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import org.json.JSONArray
import org.json.JSONObject

/** Device-local, small resumable session checkpoint. Does not change the portable backup format. */
class OdakOturumuDeposu(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("odak_oturumu", Context.MODE_PRIVATE)

    fun kaydet(oturum: OdakOturumu) {
        val json = JSONObject().put("id", oturum.id).put("baslama", oturum.baslamaZamani)
            .put("bugunden", oturum.bugunden).put("sira", oturum.sira)
            .put("sorular", JSONArray(oturum.soruIdleri)).put("cozumler", JSONArray(oturum.cozumAcilanlar.toList()))
            .put("sonuclar", JSONArray().apply {
                oturum.sonuclar.forEach { put(JSONObject().put("id", it.soruId).put("sonuc", it.sonuc.name).put("cozum", it.cozumAcildi)) }
            })
        check(prefs.edit().putString("oturum", json.toString()).commit()) { "Oturum kaydedilemedi" }
    }

    fun oku(): OdakOturumu? {
        val text = prefs.getString("oturum", null) ?: return null
        return runCatching {
            val json = JSONObject(text)
            val sorular = json.getJSONArray("sorular").strings().distinct()
            require(sorular.isNotEmpty())
            val sira = json.getInt("sira").coerceIn(0, sorular.size)
            val sonuclar = json.getJSONArray("sonuclar")
            OdakOturumu(
                id = json.getString("id"), soruIdleri = sorular, baslamaZamani = json.getLong("baslama"),
                bugunden = json.optBoolean("bugunden"), sira = sira,
                cozumAcilanlar = json.getJSONArray("cozumler").strings().toSet(),
                sonuclar = (0 until sonuclar.length()).map {
                    val s = sonuclar.getJSONObject(it)
                    OturumSonucu(s.getString("id"), TekrarSonucu.valueOf(s.getString("sonuc")), s.getBoolean("cozum"))
                }.distinctBy { it.soruId },
            )
        }.getOrNull()
    }

    fun temizle() { check(prefs.edit().remove("oturum").commit()) }

    private fun JSONArray.strings(): List<String> = (0 until length()).map { getString(it) }
}

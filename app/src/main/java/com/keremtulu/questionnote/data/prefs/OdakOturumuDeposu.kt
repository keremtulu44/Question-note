package com.keremtulu.questionnote.data.prefs

import android.content.Context
import com.keremtulu.questionnote.data.gelisim.OlcumSerilestirme
import com.keremtulu.questionnote.domain.calisma.OdakOturumu
import com.keremtulu.questionnote.domain.calisma.OlcumDevamNoktasi
import com.keremtulu.questionnote.domain.karne.OturumSonucu
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Process-owned small checkpoint. In-memory revisions prevent late IO from reviving a cleared session. */
class OdakOturumuDeposu(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("odak_oturumu", Context.MODE_PRIVATE)
    private var diskIzi = prefs.getString("oturum", null)
    private var revizyon = 0L
    private var bellek = disktenOku()?.let { s -> s.copy(olcumler = s.olcumler.mapValues { it.value.surectenKurtar() }) }

    @Synchronized fun oku(): OdakOturumu? { disDegisikligiOku(); return bellek }

    /** Runs on the UI thread without IO. Call kalicilastir with the returned revision. */
    @Synchronized fun hazirla(oturum: OdakOturumu): Long {
        disDegisikligiOku()
        val onceki = bellek?.takeIf { it.id == oturum.id }
        val birlesik = oturum.olcumler.toMutableMap()
        onceki?.olcumler?.forEach { (id, nokta) ->
            if ((birlesik[id]?.revizyon ?: -1L) < nokta.revizyon) birlesik[id] = nokta
        }
        val kalan = oturum.soruIdleri.drop(oturum.sira).toSet() - oturum.sonuclar.map { it.soruId }.toSet()
        bellek = oturum.copy(olcumler = birlesik.filterKeys { it in kalan },
            cozumAcilanlar = oturum.cozumAcilanlar + onceki?.cozumAcilanlar.orEmpty())
        return ++revizyon
    }

    @Synchronized fun olcumuHazirla(oturumId: String, nokta: OlcumDevamNoktasi): Long? {
        disDegisikligiOku()
        val oturum = bellek?.takeIf { it.id == oturumId } ?: return null
        if (nokta.soruId !in oturum.soruIdleri.drop(oturum.sira) || oturum.sonuclar.any { it.soruId == nokta.soruId }) return null
        val eski = oturum.olcumler[nokta.soruId]
        if (eski != null && (eski.denemeId != nokta.denemeId || eski.revizyon >= nokta.revizyon)) return null
        bellek = oturum.copy(olcumler = oturum.olcumler + (nokta.soruId to nokta),
            cozumAcilanlar = if (nokta.cozumAcildi) oturum.cozumAcilanlar + nokta.soruId else oturum.cozumAcilanlar)
        return ++revizyon
    }

    suspend fun kalicilastir(surum: Long?) = withContext(Dispatchers.IO) {
        synchronized(this@OdakOturumuDeposu) {
            disDegisikligiOku()
            if (surum != null && surum == revizyon) diskeYaz(bellek)
        }
    }

    /** Synchronous compatibility/test API. App UI uses hazirla + the IO method instead. */
    @Synchronized fun kaydet(oturum: OdakOturumu) { hazirla(oturum); diskeYaz(bellek) }
    @Synchronized fun sifirlamayiHazirla(): Long { bellek = null; return ++revizyon }

    /** Committed restore boundary: persistence must succeed BEFORE memory/recovery markers clear. */
    @Synchronized fun temizle() { diskeYaz(null); bellek = null; revizyon++ }

    private fun diskeYaz(oturum: OdakOturumu?) {
        val editor = prefs.edit()
        var yazilan: String? = null
        if (oturum == null) editor.remove("oturum") else {
            val json = JSONObject().put("version", 2).put("id", oturum.id).put("baslama", oturum.baslamaZamani)
                .put("bugunden", oturum.bugunden).put("sira", oturum.sira).put("olcum_destekli", oturum.olcumDestekli)
                .put("sorular", JSONArray(oturum.soruIdleri)).put("cozumler", JSONArray(oturum.cozumAcilanlar.toList()))
                .put("sonuclar", JSONArray().apply { oturum.sonuclar.forEach {
                    put(JSONObject().put("id", it.soruId).put("sonuc", it.sonuc.name).put("cozum", it.cozumAcildi))
                } })
                .put("olcumler", JSONArray().apply { oturum.olcumler.values.forEach {
                    put(JSONObject().put("soru", it.soruId).put("deneme", it.denemeId).put("olcum", OlcumSerilestirme.olcumYaz(it.olcum))
                        .put("cozum", it.cozumAcildi).put("dondu", it.donduruldu).put("secim", it.secim?.name ?: JSONObject.NULL)
                        .put("revizyon", it.revizyon))
                } })
            val metin = json.toString()
            check(metin.length <= 4_000_000) { "Oturum devam noktası çok büyük" }
            editor.putString("oturum", metin)
            yazilan = metin
        }
        check(editor.commit()) { "Oturum kaydedilemedi" }
        diskIzi = yazilan
    }

    private fun disDegisikligiOku() {
        val kayit = prefs.getString("oturum", null)
        if (kayit != diskIzi) {
            diskIzi = kayit
            bellek = disktenOku()?.let { s -> s.copy(olcumler = s.olcumler.mapValues { it.value.surectenKurtar() }) }
            revizyon++
        }
    }

    private fun disktenOku(): OdakOturumu? = runCatching {
        val text = prefs.getString("oturum", null) ?: return null
        require(text.length <= 4_000_000)
        val json = JSONObject(text)
        val surum = if (json.has("version")) OlcumSerilestirme.tamInt(json, "version") else 1
        require(surum in 1..2)
        val sorular = json.getJSONArray("sorular").strings().distinct()
        require(sorular.isNotEmpty() && sorular.size <= 10_000 && sorular.all(::kimlik))
        val id = json.getString("id"); require(kimlik(id))
        val sira = json.getInt("sira").coerceIn(0, sorular.size)
        val sonuclar = json.getJSONArray("sonuclar")
        val cozumler = json.getJSONArray("cozumler").strings().toSet().intersect(sorular.toSet())
        var destekli = surum == 2 && json.getBoolean("olcum_destekli")
        val olcumler = mutableMapOf<String, OlcumDevamNoktasi>()
        if (surum == 2) {
            val dizi = json.getJSONArray("olcumler"); require(dizi.length() <= sorular.size)
            for (i in 0 until dizi.length()) {
                val nokta = runCatching {
                    val o = dizi.getJSONObject(i)
                    val soruId = o.getString("soru"); val denemeId = o.getString("deneme")
                    require(soruId in sorular && kimlik(denemeId))
                    OlcumDevamNoktasi(soruId, denemeId, OlcumSerilestirme.olcumOku(o.getJSONObject("olcum")),
                        o.getBoolean("cozum") || soruId in cozumler, o.getBoolean("dondu"),
                        if (o.isNull("secim")) null else TekrarSonucu.valueOf(o.getString("secim")),
                        OlcumSerilestirme.tamLong(o, "revizyon").also { require(it in 0 until Long.MAX_VALUE) })
                }.getOrNull()
                if (nokta == null || nokta.soruId in olcumler) destekli = false
                else olcumler[nokta.soruId] = nokta
            }
        }
        OdakOturumu(id, sorular, json.getLong("baslama"), json.optBoolean("bugunden"), sira,
            (0 until sonuclar.length()).map {
                val s = sonuclar.getJSONObject(it)
                OturumSonucu(s.getString("id"), TekrarSonucu.valueOf(s.getString("sonuc")), s.getBoolean("cozum"))
            }.filter { it.soruId in sorular }.distinctBy { it.soruId }, cozumler, olcumler, destekli)
    }.getOrNull()

    private fun kimlik(s: String): Boolean = s.isNotBlank() && s.length <= 128
    private fun JSONArray.strings(): List<String> = (0 until length()).map { getString(it) }
}

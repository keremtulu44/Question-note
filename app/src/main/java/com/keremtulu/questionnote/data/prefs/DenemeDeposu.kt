package com.keremtulu.questionnote.data.prefs

import android.content.Context
import com.keremtulu.questionnote.data.local.entity.DersEntity
import com.keremtulu.questionnote.domain.model.DenemeDersSatiri
import com.keremtulu.questionnote.domain.model.DenemeKaydi
import com.keremtulu.questionnote.domain.model.NetKurali
import com.keremtulu.questionnote.domain.model.dersAnahtari
import com.keremtulu.questionnote.domain.model.denemeKaynakEslesir
import com.keremtulu.questionnote.domain.model.yerelEpochGun
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class DenemeDeposu(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("denemeler", Context.MODE_PRIVATE)
    private val kayitlar = MutableStateFlow(oku())
    val kayitlariIzle: StateFlow<List<DenemeKaydi>> = kayitlar.asStateFlow()

    fun getir(id: String): DenemeKaydi? = kayitlar.value.firstOrNull { it.id == id }

    @Synchronized
    fun kaydet(deneme: DenemeKaydi) {
        val onceki = kayitlar.value.firstOrNull { it.id == deneme.id }
        val adDegisti = onceki != null && !denemeKaynakEslesir(onceki.ad, deneme.ad)
        val digerleri = kayitlar.value.filter { it.id != deneme.id }.map { diger ->
            if (adDegisti && denemeKaynakEslesir(diger.ad, onceki?.ad)) diger.copy(kaynakBelirsiz = true)
            else if (denemeKaynakEslesir(diger.ad, deneme.ad)) diger.copy(kaynakBelirsiz = true)
            else diger
        }
        val yeniBelirsiz = digerleri.any { denemeKaynakEslesir(it.ad, deneme.ad) } ||
            (!adDegisti && (onceki?.kaynakBelirsiz == true || deneme.kaynakBelirsiz))
        yaz(sirala(digerleri + deneme.copy(kaynakBelirsiz = yeniBelirsiz)))
    }

    @Synchronized
    fun sil(id: String) {
        val silinen = kayitlar.value.firstOrNull { it.id == id }
        val kalan = kayitlar.value.filter { it.id != id }.map { diger ->
            if (silinen != null && denemeKaynakEslesir(diger.ad, silinen.ad)) diger.copy(kaynakBelirsiz = true)
            else diger
        }
        yaz(kalan)
    }

    @Synchronized
    fun geriYukleVeUzlastir(yeniListe: List<DenemeKaydi>, dersler: List<DersEntity>) {
        val idHaritasi = dersler.associateBy { it.id }
        val adHaritasi = dersler.associateBy { it.ad.dersAnahtari() }
        val uzlasmis = yeniListe.map { deneme ->
            val satirlar = deneme.satirlar.map { satir ->
                val mevcutDers = idHaritasi[satir.dersId]
                    ?: satir.dersAdi?.let { adHaritasi[it.dersAnahtari()] }
                if (mevcutDers != null) {
                    satir.copy(dersId = mevcutDers.id, dersAdi = mevcutDers.ad)
                } else {
                    satir
                }
            }.distinctBy { it.dersId }
            deneme.copy(satirlar = satirlar)
        }
        yaz(sirala(uzlasmis))
    }

    private fun oku(): List<DenemeKaydi> {
        val ham = prefs.getString("liste", null) ?: return emptyList()
        return runCatching {
            val dizi = JSONArray(ham)
            sirala(
                buildList {
                    for (i in 0 until dizi.length()) {
                        add(jsonOku(dizi.getJSONObject(i)))
                    }
                },
            )
        }.getOrDefault(emptyList())
    }

    private fun yaz(liste: List<DenemeKaydi>) {
        val dizi = JSONArray()
        liste.forEach { deneme -> dizi.put(jsonYaz(deneme)) }
        check(prefs.edit().putString("liste", dizi.toString()).commit())
        kayitlar.value = liste
    }

    companion object {
        fun sirala(liste: List<DenemeKaydi>): List<DenemeKaydi> =
            liste.sortedWith(compareByDescending<DenemeKaydi> { it.gun }.thenByDescending { it.olusturma })

        fun jsonYaz(deneme: DenemeKaydi): JSONObject =
            JSONObject()
                .put("id", deneme.id)
                .put("ad", deneme.ad)
                .put("kural", deneme.kural.name)
                .put("olusturma", deneme.olusturma)
                .put("gun", deneme.gun)
                .put("kaynakBelirsiz", deneme.kaynakBelirsiz)
                .put(
                    "satirlar",
                    JSONArray().apply {
                        deneme.satirlar.forEach { satir ->
                            put(
                                JSONObject()
                                    .put("dersId", satir.dersId)
                                    .put("soruSayisi", satir.soruSayisi)
                                    .put("dogru", satir.dogru)
                                    .put("yanlis", satir.yanlis)
                                    .apply {
                                        if (!satir.dersAdi.isNullOrBlank()) {
                                            put("dersAdi", satir.dersAdi)
                                        }
                                    },
                            )
                        }
                    },
                )

        fun jsonOku(o: JSONObject, strict: Boolean = false): DenemeKaydi {
            val satirlarJson = o.getJSONArray("satirlar")
            val satirlar = buildList {
                for (s in 0 until satirlarJson.length()) {
                    val satir = satirlarJson.getJSONObject(s)
                    val dersAdi = if (satir.has("dersAdi") && !satir.isNull("dersAdi")) {
                        satir.getString("dersAdi").trim().takeIf { it.isNotEmpty() }
                    } else {
                        null
                    }
                    add(
                        DenemeDersSatiri(
                            dersId = satir.getString("dersId"),
                            soruSayisi = satir.getInt("soruSayisi"),
                            dogru = satir.getInt("dogru"),
                            yanlis = satir.getInt("yanlis"),
                            dersAdi = dersAdi,
                        ),
                    )
                }
            }
            val olusturma = o.getLong("olusturma")
            val kuralAd = o.getString("kural")
            val kural = NetKurali.entries.firstOrNull { it.name == kuralAd }
                ?: if (strict) {
                    throw IllegalArgumentException("Bilinmeyen net kuralı: $kuralAd")
                } else {
                    NetKurali.DORTTE_BIR
                }
            return DenemeKaydi(
                id = o.getString("id"),
                ad = o.getString("ad"),
                kural = kural,
                olusturma = olusturma,
                gun = if (o.has("gun")) o.getInt("gun") else olusturma.yerelEpochGun(),
                satirlar = satirlar,
                kaynakBelirsiz = o.optBoolean("kaynakBelirsiz", false),
            )
        }
    }
}

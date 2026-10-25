package com.keremtulu.questionnote.data.prefs

import android.content.Context
import com.keremtulu.questionnote.domain.model.DenemeDersSatiri
import com.keremtulu.questionnote.domain.model.DenemeKaydi
import com.keremtulu.questionnote.domain.model.NetKurali
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

    fun kaydet(deneme: DenemeKaydi) {
        val guncel = kayitlar.value.filter { it.id != deneme.id } + deneme
        yaz(guncel.sortedByDescending { it.olusturma })
    }

    fun sil(id: String) {
        yaz(kayitlar.value.filter { it.id != id })
    }

    private fun oku(): List<DenemeKaydi> {
        val ham = prefs.getString("liste", null) ?: return emptyList()
        return runCatching {
            val dizi = JSONArray(ham)
            buildList {
                for (i in 0 until dizi.length()) {
                    val o = dizi.getJSONObject(i)
                    val satirlarJson = o.getJSONArray("satirlar")
                    val satirlar = buildList {
                        for (s in 0 until satirlarJson.length()) {
                            val satir = satirlarJson.getJSONObject(s)
                            add(
                                DenemeDersSatiri(
                                    dersId = satir.getString("dersId"),
                                    soruSayisi = satir.getInt("soruSayisi"),
                                    dogru = satir.getInt("dogru"),
                                    yanlis = satir.getInt("yanlis"),
                                ),
                            )
                        }
                    }
                    add(
                        DenemeKaydi(
                            id = o.getString("id"),
                            ad = o.getString("ad"),
                            kural = runCatching { NetKurali.valueOf(o.getString("kural")) }.getOrDefault(NetKurali.DORTTE_BIR),
                            olusturma = o.getLong("olusturma"),
                            satirlar = satirlar,
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun yaz(liste: List<DenemeKaydi>) {
        val dizi = JSONArray()
        liste.forEach { deneme ->
            dizi.put(
                JSONObject()
                    .put("id", deneme.id)
                    .put("ad", deneme.ad)
                    .put("kural", deneme.kural.name)
                    .put("olusturma", deneme.olusturma)
                    .put(
                        "satirlar",
                        JSONArray().apply {
                            deneme.satirlar.forEach { satir ->
                                put(
                                    JSONObject()
                                        .put("dersId", satir.dersId)
                                        .put("soruSayisi", satir.soruSayisi)
                                        .put("dogru", satir.dogru)
                                        .put("yanlis", satir.yanlis),
                                )
                            }
                        },
                    ),
            )
        }
        check(prefs.edit().putString("liste", dizi.toString()).commit())
        kayitlar.value = liste
    }
}

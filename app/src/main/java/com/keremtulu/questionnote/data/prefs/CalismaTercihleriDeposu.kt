package com.keremtulu.questionnote.data.prefs

import android.content.Context
import com.keremtulu.questionnote.domain.model.DersCalismaAyar
import com.keremtulu.questionnote.domain.model.FiltreliCalismaEkraniAyar
import com.keremtulu.questionnote.domain.model.SinavProfili
import com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi
import com.keremtulu.questionnote.domain.model.SoruPlanDurumu
import org.json.JSONObject

/** Last-used local filters, scoped to the exam profile and lesson. Not part of the data backup. */
class CalismaTercihleriDeposu(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("calisma_tercihleri", Context.MODE_PRIVATE)

    fun filtreliOku(profil: SinavProfili): FiltreliCalismaEkraniAyar = runCatching {
        val json = JSONObject(prefs.getString("filtre_${profil.name}", null) ?: return FiltreliCalismaEkraniAyar())
        val filtre = SoruCalismaFiltresi(
            dersId = json.metin("ders"),
            vadesiGelen = json.optBoolean("vade"),
            kaynakAnahtari = json.metin("kaynakAnahtari"),
            konuId = json.metin("konu"),
            konusuz = json.optBoolean("konusuz"),
            kaynak = json.metin("kaynak"),
            kaynakBelirtilmemis = json.optBoolean("kaynaksiz"),
            yanlisNedeni = json.metin("neden"),
            yanlisNedeniBelirtilmemis = json.optBoolean("nedensiz"),
        )
        FiltreliCalismaEkraniAyar(
            filtre = filtre,
            soruSayisi = json.optString("sayi", "10").take(5).filter(Char::isDigit),
            akilliSira = json.optBoolean("akilli", true),
        )
    }.getOrDefault(FiltreliCalismaEkraniAyar())

    fun filtreliKaydet(profil: SinavProfili, ayar: FiltreliCalismaEkraniAyar) {
        val f = ayar.filtre
        val json = JSONObject()
            .put("ders", f.dersId).put("vade", f.vadesiGelen)
            .put("kaynakAnahtari", f.kaynakAnahtari).put("konu", f.konuId)
            .put("konusuz", f.konusuz).put("kaynak", f.kaynak)
            .put("kaynaksiz", f.kaynakBelirtilmemis).put("neden", f.yanlisNedeni)
            .put("nedensiz", f.yanlisNedeniBelirtilmemis).put("sayi", ayar.soruSayisi)
            .put("akilli", ayar.akilliSira)
        prefs.edit().putString("filtre_${profil.name}", json.toString()).apply()
    }

    fun dersOku(profil: SinavProfili, dersId: String): DersCalismaAyar = runCatching {
        val json = JSONObject(prefs.getString(dersAnahtari(profil, dersId), null) ?: return DersCalismaAyar())
        DersCalismaAyar(
            konuId = json.metin("konu"),
            konusuz = json.optBoolean("konusuz"),
            planDurumu = SoruPlanDurumu.entries.firstOrNull { it.name == json.metin("durum") },
            sadeceFavori = json.optBoolean("favori"),
            arama = json.optString("arama", "").take(80),
        )
    }.getOrDefault(DersCalismaAyar())

    fun dersKaydet(profil: SinavProfili, dersId: String, ayar: DersCalismaAyar) {
        val json = JSONObject().put("konu", ayar.konuId).put("konusuz", ayar.konusuz)
            .put("durum", ayar.planDurumu?.name).put("favori", ayar.sadeceFavori)
            .put("arama", ayar.arama.take(80))
        prefs.edit().putString(dersAnahtari(profil, dersId), json.toString()).apply()
    }

    private fun dersAnahtari(profil: SinavProfili, dersId: String) = "ders_${profil.name}_$dersId"

    private fun JSONObject.metin(anahtar: String): String? =
        if (!has(anahtar) || isNull(anahtar)) null else optString(anahtar).takeIf { it.isNotBlank() }
}

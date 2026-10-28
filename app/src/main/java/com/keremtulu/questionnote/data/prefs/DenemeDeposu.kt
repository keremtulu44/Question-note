package com.keremtulu.questionnote.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.keremtulu.questionnote.data.local.entity.DersEntity
import com.keremtulu.questionnote.domain.model.DenemeDersSatiri
import com.keremtulu.questionnote.domain.model.DenemeKaydi
import com.keremtulu.questionnote.domain.model.NetKurali
import com.keremtulu.questionnote.domain.model.dersAnahtari
import com.keremtulu.questionnote.domain.model.yerelEpochGun
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.util.UUID

/** Unreadable records must never be treated as an empty, writable repository. */
class DenemeVerisiOkunamadiException : IOException("Deneme kayıtları okunamıyor; yedekten geri yükleyin veya kayıtları sıfırlayın.")

internal data class DenemeYedekGoruntusu(
    val kayitlar: List<DenemeKaydi>,
    // A typed, lossless wrapper around the original SharedPreferences value.
    val bozukHam: String? = null,
)

class DenemeDeposu(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("denemeler", Context.MODE_PRIVATE)
    private val baslangic = oku()
    private val kayitlar = MutableStateFlow(baslangic.getOrDefault(emptyList()))
    private val bozuk = MutableStateFlow(baslangic.isFailure)
    val kayitlariIzle: StateFlow<List<DenemeKaydi>> = kayitlar.asStateFlow()
    val bozukMuIzle: StateFlow<Boolean> = bozuk.asStateFlow()
    private val tercihDinleyicisi = SharedPreferences.OnSharedPreferenceChangeListener { _, anahtar ->
        if (anahtar == null || anahtar == LISTE_ANAHTARI) yenile()
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(tercihDinleyicisi)
        yenile() // Close the gap between the initial read and listener registration.
    }

    fun getir(id: String): DenemeKaydi? = kayitlar.value.firstOrNull { it.id == id }

    @Synchronized
    fun yazilabilirMi(): Boolean {
        yenile()
        return !bozuk.value
    }

    @Synchronized
    private fun yenile() {
        val sonuc = oku()
        if (sonuc.isFailure) {
            bozuk.value = true
            kayitlar.value = emptyList()
        } else {
            kayitlar.value = sonuc.getOrThrow()
            bozuk.value = false
        }
    }

    @Synchronized
    fun kaydet(deneme: DenemeKaydi) {
        yenile()
        yazilabilirligiDogrula()
        val guncel = kayitlar.value.filter { it.id != deneme.id } + deneme
        yaz(sirala(guncel))
    }

    @Synchronized
    fun sil(id: String) {
        yenile()
        yazilabilirligiDogrula()
        yaz(kayitlar.value.filter { it.id != id })
    }

    /** A normal export fails closed. Restore's safety copy can preserve the raw value instead. */
    @Synchronized
    internal fun yedekGoruntusu(bozukHamIle: Boolean = false): DenemeYedekGoruntusu {
        yenile()
        if (bozuk.value) {
            if (!bozukHamIle) throw DenemeVerisiOkunamadiException()
            val ham = prefs.all[LISTE_ANAHTARI] ?: throw DenemeVerisiOkunamadiException()
            return DenemeYedekGoruntusu(emptyList(), hamYedekYaz(ham))
        }
        return DenemeYedekGoruntusu(kayitlar.value.toList())
    }

    /** Explicit user action: retain the unreadable value on-device before starting a new list. */
    @Synchronized
    fun bozukKayitlariSifirla() {
        yenile()
        if (!bozuk.value) return
        val ham = prefs.all[LISTE_ANAHTARI] ?: throw DenemeVerisiOkunamadiException()
        val editor = prefs.edit()
        editor.hamDegerYaz("bozuk_liste_${UUID.randomUUID()}", ham)
        editor.putString(LISTE_ANAHTARI, "[]")
        check(editor.commit()) { "Bozuk deneme verisi korunamadı." }
        kayitlar.value = emptyList()
        bozuk.value = false
    }

    /** Only used after a verified restore has committed, or while replaying its journal. */
    @Synchronized
    internal fun hamYedegiGeriYukle(ham: String) {
        val deger = hamYedekOku(ham)
        val editor = prefs.edit()
        editor.hamDegerYaz(LISTE_ANAHTARI, deger)
        check(editor.commit()) { "Deneme verisi geri yüklenemedi." }
        val sonuc = oku()
        kayitlar.value = sonuc.getOrDefault(emptyList())
        bozuk.value = sonuc.isFailure
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

    private fun yazilabilirligiDogrula() {
        if (bozuk.value) throw DenemeVerisiOkunamadiException()
    }

    private fun oku(): Result<List<DenemeKaydi>> = runCatching {
        val ham = prefs.all[LISTE_ANAHTARI] ?: return@runCatching emptyList()
        require(ham is String) { "Deneme verisinin türü geçersiz." }
        val tokener = JSONTokener(ham)
        val dizi = tokener.nextValue() as? JSONArray ?: error("Deneme listesi geçersiz.")
        require(tokener.nextClean() == '\u0000') { "Deneme listesinden sonra beklenmeyen veri var." }
        require(dizi.length() <= MAKS_KAYIT) { "Deneme kaydı sınırı aşıldı." }
        val liste = buildList {
            for (i in 0 until dizi.length()) {
                val obje = dizi.getJSONObject(i)
                jsonAlanlariniDogrula(obje)
                add(jsonOku(obje, strict = true))
            }
        }
        require(listeGecerli(liste)) { "Deneme kayıtları geçersiz." }
        sirala(liste)
    }

    private fun yaz(liste: List<DenemeKaydi>) {
        require(listeGecerli(liste)) { "Geçersiz deneme kayıtları yazılamaz." }
        val dizi = JSONArray()
        liste.forEach { deneme -> dizi.put(jsonYaz(deneme)) }
        check(prefs.edit().putString(LISTE_ANAHTARI, dizi.toString()).commit())
        kayitlar.value = liste
        bozuk.value = false
    }

    companion object {
        private const val LISTE_ANAHTARI = "liste"
        private const val MAKS_KAYIT = 200_000
        private const val MAKS_METIN = 200_000
        private const val MAKS_KIMLIK = 128
        private val DENEME_ALANLARI = setOf("id", "ad", "kural", "olusturma", "gun", "satirlar")
        private val SATIR_ALANLARI = setOf("dersId", "soruSayisi", "dogru", "yanlis", "dersAdi")

        private fun JSONObject.yalnizca(alanlar: Set<String>) = keys().asSequence().all { it in alanlar }
        private fun JSONObject.tamSayi(anahtar: String, min: Long, max: Long): Boolean {
            val deger = opt(anahtar)
            val sayi = when (deger) {
                is Int -> deger.toLong()
                is Long -> deger
                else -> return false
            }
            return sayi in min..max
        }

        /** Refuse unknown/ambiguous fields instead of silently rewriting them on the next save. */
        private fun jsonAlanlariniDogrula(o: JSONObject) {
            require(o.yalnizca(DENEME_ALANLARI))
            require(o.opt("id") is String && o.opt("ad") is String && o.opt("kural") is String)
            require(o.tamSayi("olusturma", 0, Long.MAX_VALUE))
            if (o.has("gun")) require(o.tamSayi("gun", 0, Int.MAX_VALUE.toLong()))
            val satirlar = o.getJSONArray("satirlar")
            require(satirlar.length() <= MAKS_KAYIT)
            for (i in 0 until satirlar.length()) {
                val satir = satirlar.getJSONObject(i)
                require(satir.yalnizca(SATIR_ALANLARI) && satir.opt("dersId") is String)
                listOf("soruSayisi", "dogru", "yanlis").forEach { alan ->
                    require(satir.tamSayi(alan, 0, Int.MAX_VALUE.toLong()))
                }
                if (satir.has("dersAdi") && !satir.isNull("dersAdi")) require(satir.opt("dersAdi") is String)
            }
        }

        private fun listeGecerli(liste: List<DenemeKaydi>): Boolean =
            liste.size <= MAKS_KAYIT && liste.map { it.id }.distinct().size == liste.size && liste.all { deneme ->
                deneme.id.isNotBlank() && deneme.id.length <= MAKS_KIMLIK &&
                    deneme.ad.isNotBlank() && deneme.ad.length <= MAKS_METIN &&
                    deneme.olusturma >= 0L && deneme.gun >= 0 && deneme.satirlar.isNotEmpty() &&
                    deneme.satirlar.size <= MAKS_KAYIT &&
                    deneme.satirlar.map { it.dersId }.distinct().size == deneme.satirlar.size &&
                    deneme.satirlar.all { satir ->
                        satir.dersId.isNotBlank() && satir.dersId.length <= MAKS_KIMLIK &&
                            (satir.dersAdi == null || satir.dersAdi.length <= MAKS_METIN) && satir.gecerli()
                    }
            }

        private fun SharedPreferences.Editor.hamDegerYaz(anahtar: String, deger: Any) {
            when (deger) {
                is String -> putString(anahtar, deger)
                is Int -> putInt(anahtar, deger)
                is Long -> putLong(anahtar, deger)
                is Boolean -> putBoolean(anahtar, deger)
                is Float -> putFloat(anahtar, deger)
                is Set<*> -> {
                    require(deger.all { it is String })
                    putStringSet(anahtar, deger.map { it as String }.toSet())
                }
                else -> throw IOException("Desteklenmeyen deneme tercih türü.")
            }
        }

        private fun hamYedekYaz(deger: Any): String {
            val json = JSONObject().put("version", 1)
            when (deger) {
                is String -> json.put("type", "string").put("value", deger)
                is Int -> json.put("type", "int").put("value", deger)
                is Long -> json.put("type", "long").put("value", deger)
                is Boolean -> json.put("type", "boolean").put("value", deger)
                // Store raw bits, including signed zero and non-finite values, without JSON number conversion.
                is Float -> json.put("type", "float_bits").put("value", deger.toRawBits())
                is Set<*> -> {
                    require(deger.all { it is String })
                    json.put("type", "string_set").put("value", JSONArray(deger.map { it as String }.sorted()))
                }
                else -> throw IOException("Desteklenmeyen deneme tercih türü.")
            }
            return json.toString()
        }

        internal fun hamYedekDogrula(ham: String) { hamYedekOku(ham) }

        private fun hamYedekOku(ham: String): Any {
            try {
                val json = JSONObject(ham)
                require(json.yalnizca(setOf("version", "type", "value")))
                require(json.opt("version") == 1 && json.opt("type") is String && json.has("value"))
                val deger = json.get("value")
                return when (json.getString("type")) {
                    "string" -> deger as? String ?: error("Geçersiz ham metin.")
                    "int" -> (deger as? Int) ?: error("Geçersiz ham tamsayı.")
                    "long" -> when (deger) {
                        is Int -> deger.toLong()
                        is Long -> deger
                        else -> error("Geçersiz ham uzun tamsayı.")
                    }
                    "boolean" -> deger as? Boolean ?: error("Geçersiz ham mantıksal değer.")
                    "float_bits" -> Float.fromBits(deger as? Int ?: error("Geçersiz ham ondalık değer."))
                    "string_set" -> {
                        val dizi = deger as? JSONArray ?: error("Geçersiz ham liste.")
                        require(dizi.length() <= MAKS_KAYIT)
                        val liste = List(dizi.length()) { i -> dizi.get(i) as? String ?: error("Geçersiz ham liste elemanı.") }
                        require(liste.distinct().size == liste.size)
                        liste.toSet()
                    }
                    else -> error("Bilinmeyen ham deneme türü.")
                }
            } catch (error: Exception) {
                throw IOException("Bozuk deneme verisinin güvenlik kopyası okunamadı.", error)
            }
        }

        fun sirala(liste: List<DenemeKaydi>): List<DenemeKaydi> =
            liste.sortedWith(compareByDescending<DenemeKaydi> { it.gun }.thenByDescending { it.olusturma })

        fun jsonYaz(deneme: DenemeKaydi): JSONObject =
            JSONObject()
                .put("id", deneme.id)
                .put("ad", deneme.ad)
                .put("kural", deneme.kural.name)
                .put("olusturma", deneme.olusturma)
                .put("gun", deneme.gun)
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
            )
        }
    }
}

package com.keremtulu.questionnote.data.gelisim

import com.keremtulu.questionnote.data.local.entity.TekrarEntity
import com.keremtulu.questionnote.data.local.entity.TekrarOlcumEntity
import com.keremtulu.questionnote.domain.calisma.OlcumKalitesi
import com.keremtulu.questionnote.domain.calisma.OlcumNedeni
import com.keremtulu.questionnote.domain.calisma.SureOlcumu
import com.keremtulu.questionnote.domain.gelisim.KisiselProfil
import com.keremtulu.questionnote.domain.gelisim.ProfilPolitikasi
import com.keremtulu.questionnote.domain.gelisim.ProfilDuzeyi
import com.keremtulu.questionnote.domain.gelisim.ProfilSecimi
import com.keremtulu.questionnote.domain.gelisim.SureProfili
import com.keremtulu.questionnote.domain.gelisim.VeriDayanagi
import com.keremtulu.questionnote.domain.gelisim.kaynakAnahtari
import com.keremtulu.questionnote.domain.model.TekrarPlani
import com.keremtulu.questionnote.domain.repeat.*
import com.keremtulu.questionnote.domain.calisma.TekrarDenemesi
import com.keremtulu.questionnote.domain.gelisim.DayanikliDagilim
import com.keremtulu.questionnote.domain.gelisim.ProfilEsigi
import com.keremtulu.questionnote.domain.model.OgrenmeDurumu
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import org.json.JSONArray
import org.json.JSONObject

/** Strict, bounded v1 measurement/v2-decision codec shared by DB validation, checkpoint and ZIP v4. */
object OlcumSerilestirme {
    const val KARAR_UST_SINIR = 16_384
    fun nedenlerYaz(nedenler: Set<OlcumNedeni>): String = JSONArray(nedenler.map { it.name }.sorted()).toString()
    fun nedenlerOku(json: String): Set<OlcumNedeni> {
        require(json.length <= 512)
        val dizi = JSONArray(json)
        require(dizi.length() <= OlcumNedeni.entries.size)
        val sonuc = (0 until dizi.length()).map { OlcumNedeni.valueOf(dizi.getString(it)) }
        require(sonuc.distinct().size == sonuc.size)
        return sonuc.toSet()
    }
    fun olcum(entity: com.keremtulu.questionnote.data.local.dao.ProfilOlcumSatiri) = SureOlcumu(entity.denemeAktifMs, entity.incelemeAktifMs,
        OlcumKalitesi.valueOf(entity.kalite), nedenlerOku(entity.nedenlerJson), entity.olcumSurumu)
    fun olcum(entity: TekrarOlcumEntity) = SureOlcumu(entity.denemeAktifMs, entity.incelemeAktifMs,
        OlcumKalitesi.valueOf(entity.kalite), nedenlerOku(entity.nedenlerJson), entity.olcumSurumu)

    fun olcumYaz(olcum: SureOlcumu) = JSONObject()
        .put("attempt_active_ms", olcum.denemeAktifMs ?: JSONObject.NULL)
        .put("assisted_active_ms", olcum.incelemeAktifMs ?: JSONObject.NULL)
        .put("quality", olcum.kalite.name).put("reasons", JSONArray(nedenlerYaz(olcum.nedenler)))
        .put("measurement_version", olcum.surum)

    fun olcumOku(json: JSONObject): SureOlcumu = SureOlcumu(
        bosLong(json, "attempt_active_ms"), bosLong(json, "assisted_active_ms"),
        OlcumKalitesi.valueOf(json.getString("quality")), nedenlerOku(json.getJSONArray("reasons").toString()),
        tamInt(json, "measurement_version"),
    ).also { require(it.gecerliAlanlar()) }

    fun yaz(e: TekrarOlcumEntity): JSONObject = olcumYaz(olcum(e))
        .put("review_id", e.tekrarId).put("attempt_id", e.denemeId).put("session_id", e.oturumId ?: JSONObject.NULL)
        .put("lesson_id", e.dersId).put("lesson_label", e.dersAdi)
        .put("topic_id", e.konuId ?: JSONObject.NULL).put("topic_label", e.konuAdi ?: JSONObject.NULL)
        .put("source_key", e.kaynakAnahtari ?: JSONObject.NULL).put("source_label", e.kaynakEtiketi ?: JSONObject.NULL)
        .put("decision", JSONObject(e.kararJson))

    fun oku(json: JSONObject): TekrarOlcumEntity {
        val olcum = olcumOku(json)
        return TekrarOlcumEntity(json.getString("review_id"), json.getString("attempt_id"), bosMetin(json, "session_id"),
            olcum.denemeAktifMs, olcum.incelemeAktifMs, olcum.kalite.name, nedenlerYaz(olcum.nedenler), olcum.surum,
            json.getString("lesson_id"), json.getString("lesson_label"), bosMetin(json, "topic_id"), bosMetin(json, "topic_label"),
            bosMetin(json, "source_key"), bosMetin(json, "source_label"), json.getJSONObject("decision").toString())
    }

    fun kararYaz(plan: TekrarPlani, gun: Int, profil: KisiselProfil, saatDilimi: String): String = JSONObject()
        .put("version", 1).put("mode", "V2").put("algorithm_version", 2).put("day", gun).put("timezone", saatDilimi)
        .put("base_interval", plan.sonrakiAralik).put("final_interval", plan.sonrakiAralik)
        .put("next_day", plan.sonrakiGun).put("reasons", JSONArray(listOf("V2_KORUNDU")))
        .put("profile", JSONObject().put("policy_version", profil.politikaSurumu)
            .put("independent", secimYaz(profil.yardimsiz)).put("workload", secimYaz(profil.isYuku))).toString()

    fun kararYaz(karar: TekrarKarari, gun: Int, profil: KisiselProfil, saatDilimi: String): String {
        if (karar.mod == TekrarMotorModu.KAPALI) return kararYaz(karar.temel, gun, profil, saatDilimi)
        return JSONObject(kararYaz(karar.temel, gun, profil, saatDilimi))
            .put("version", 2).put("mode", karar.mod.name).put("requested_mode", karar.istenenMod.name)
            .put("algorithm_version", karar.algoritmaSurumu).put("candidate_algorithm_version", 3)
            .put("candidate_interval", karar.aday.sonrakiAralik).put("final_interval", karar.uygulanan.sonrakiAralik)
            .put("next_day", karar.uygulanan.sonrakiGun).put("unknown_history", karar.desteklenmeyenGecmis)
            .put("v2_late_discount", karar.v2GecikmeIndirimi)
            .put("reasons", JSONArray(karar.nedenler.map { it.name }.sorted()))
            .put("recent", JSONArray().apply { karar.yakin.gunler.forEach {
                put(JSONObject().put("day", it.gun).put("result", it.sonuc.name).put("solution_opened", it.cozumAcildi))
            } }).toString()
    }

    private fun secimYaz(secim: ProfilSecimi): JSONObject = JSONObject().put("requested", profilYaz(secim.istenen))
        .put("reference", secim.referans?.let(::profilYaz) ?: JSONObject.NULL)

    private fun profilYaz(p: SureProfili): JSONObject = JSONObject().put("scope", p.duzey.name).put("basis", p.dayanak.name)
        .put("measurements", p.olcumSayisi).put("questions", p.farkliSoru).put("days", p.farkliGun)
        .put("known_context_records", p.kapsamKaydi).put("first_day", p.ilkGun).put("last_day", p.sonGun)
        .put("minimum_measurements", p.esik.olcum).put("minimum_questions", p.esik.soru).put("minimum_days", p.esik.gun)
        .put("distribution", p.dagilim?.let { JSONObject().put("median_ms", it.medyanMs).put("q1_ms", it.altCeyrekMs)
            .put("q3_ms", it.ustCeyrekMs).put("mad_ms", it.madMs) } ?: JSONObject.NULL)

    fun dogrula(e: TekrarOlcumEntity, tekrar: TekrarEntity) {
        fun kimlik(s: String) = s.isNotBlank() && s.length <= 128
        require(e.tekrarId == tekrar.id && kimlik(e.tekrarId) && kimlik(e.denemeId) && kimlik(e.dersId))
        require(e.oturumId == null || kimlik(e.oturumId))
        require(e.dersAdi.isNotBlank() && e.dersAdi.length <= 200_000)
        require((e.konuId == null) == (e.konuAdi == null))
        require(e.konuId == null || kimlik(e.konuId) && !e.konuAdi.isNullOrBlank() && e.konuAdi.length <= 200_000)
        require((e.kaynakAnahtari == null) == (e.kaynakEtiketi == null))
        require(e.kaynakEtiketi == null || e.kaynakEtiketi.length <= 200_000)
        require(e.kaynakAnahtari == kaynakAnahtari(e.kaynakEtiketi))
        val o = olcum(e)
        require(o.gecerliAlanlar())
        require(tekrar.cozumAcildi || (o.incelemeAktifMs ?: 0) == 0L)
        require(e.kararJson.toByteArray(Charsets.UTF_8).size <= KARAR_UST_SINIR)
        val karar = JSONObject(e.kararJson)
        val surum = tamInt(karar, "version")
        require(surum in 1..2)
        require(tamInt(karar, "day") == tekrar.gun)
        java.time.ZoneId.of(karar.getString("timezone"))
        val aralik = tamInt(karar, "final_interval")
        require(aralik in 1..180 && aralik == tekrar.sonrakiAralik)
        require(tamLong(karar, "next_day") == tekrar.gun.toLong() + aralik)
        if (surum == 1) {
            require(karar.getString("mode") == "V2")
            require(tamInt(karar, "algorithm_version") == 2 && tekrar.algoritmaSurumu == 2)
            require(aralik == tamInt(karar, "base_interval"))
            val neden = karar.getJSONArray("reasons")
            require(neden.length() == 1 && neden.getString(0) == "V2_KORUNDU")
        }
        val profil = karar.getJSONObject("profile")
        require(tamInt(profil, "policy_version") == 1)
        listOf("independent", "workload").forEach { key ->
            val secim = profil.getJSONObject(key)
            profilDogrula(secim.getJSONObject("requested"), tekrar.gun)
            if (!secim.isNull("reference")) {
                val ref = secim.getJSONObject("reference")
                profilDogrula(ref, tekrar.gun)
                require(ref.getString("basis") == VeriDayanagi.YETERLI.name)
            }
        }
        if (surum == 2) v2KararDogrula(e, tekrar, karar)
    }

    private fun v2KararDogrula(e: TekrarOlcumEntity, t: TekrarEntity, json: JSONObject) {
        val mod = TekrarMotorModu.valueOf(json.getString("mode"))
        val istenen = TekrarMotorModu.valueOf(json.getString("requested_mode"))
        require(mod in setOf(TekrarMotorModu.GOLGE, TekrarMotorModu.ETKIN))
        require(istenen == mod || istenen == TekrarMotorModu.ETKIN && mod == TekrarMotorModu.GOLGE)
        require(tamInt(json, "candidate_algorithm_version") == 3)
        require(t.algoritmaSurumu == if (mod == TekrarMotorModu.ETKIN) 3 else 2)
        require(tamInt(json, "algorithm_version") == t.algoritmaSurumu)
        val temelAralik = tamInt(json, "base_interval")
        val onceki = requireNotNull(t.oncekiAralik)
        require(temelAralik in 1..180 && onceki in 1..180)
        if (t.cozumAcildi || t.sonuc == "COZEMEDIM") require(temelAralik == 1)
        val dizi = json.getJSONArray("recent")
        require(dizi.length() <= YakinGecmisHesabi.EN_COK_GUN)
        val gunler = (0 until dizi.length()).map { i ->
            val k = dizi.getJSONObject(i)
            val gun = tamInt(k, "day")
            require(gun.toLong() in (t.gun.toLong() - 89)..t.gun.toLong())
            YakinTekrar(gun, TekrarSonucu.valueOf(k.getString("result")), k.get("solution_opened") as Boolean)
        }
        require(gunler.map { it.gun } == gunler.map { it.gun }.distinct().sortedDescending())
        val temel = TekrarPlani(onceki, temelAralik, Math.addExact(t.gun, temelAralik), when {
            temelAralik <= 3 -> OgrenmeDurumu.OGRENIYOR
            temelAralik <= 21 -> OgrenmeDurumu.PEKISIYOR
            else -> OgrenmeDurumu.SAGLAM
        })
        val profil = json.getJSONObject("profile").getJSONObject("independent")
        val referans = if (profil.isNull("reference")) null else profilOku(profil.getJSONObject("reference"))
        val gecikme = t.planlananGun?.let { t.gun.toLong() - it > 2L * onceki } == true && !t.cozumAcildi && t.sonuc != "COZEMEDIM"
        require(json.get("v2_late_discount") as Boolean == gecikme)
        val hesap = UyarlanabilirTekrarKurali.karar(temel,
            TekrarDenemesi(e.denemeId, t.soruId, TekrarSonucu.valueOf(t.sonuc), t.cozumAcildi, olcum(e), e.oturumId),
            referans, t.gun, istenen, mod, YakinGecmis(gunler), json.get("unknown_history") as Boolean, gecikme)
        require(hesap.aday.sonrakiAralik == tamInt(json, "candidate_interval"))
        require(hesap.uygulanan.sonrakiAralik == t.sonrakiAralik)
        val nedenler = json.getJSONArray("reasons")
        require(nedenler.length() <= KararNedeni.entries.size)
        val adlar = (0 until nedenler.length()).map { nedenler.getString(it) }
        require(adlar.size == adlar.distinct().size && adlar.toSet() == hesap.nedenler.map { it.name }.toSet())
    }

    private fun profilOku(j: JSONObject): SureProfili {
        val d = if (j.isNull("distribution")) null else j.getJSONObject("distribution").let {
            DayanikliDagilim(it.getDouble("median_ms"), it.getDouble("q1_ms"), it.getDouble("q3_ms"), it.getDouble("mad_ms")) }
        return SureProfili(ProfilDuzeyi.valueOf(j.getString("scope")), VeriDayanagi.valueOf(j.getString("basis")),
            tamInt(j, "measurements"), tamInt(j, "questions"), tamInt(j, "days"), tamInt(j, "known_context_records"),
            ProfilEsigi(tamInt(j, "minimum_measurements"), tamInt(j, "minimum_questions"), tamInt(j, "minimum_days")),
            tamInt(j, "first_day"), tamInt(j, "last_day"), d)
    }

    private fun profilDogrula(json: JSONObject, gun: Int) {
        val duzey = ProfilDuzeyi.valueOf(json.getString("scope"))
        val dayanak = VeriDayanagi.valueOf(json.getString("basis"))
        val politika = ProfilPolitikasi()
        require(tamInt(json, "last_day") == gun && tamLong(json, "first_day") == gun.toLong() - politika.pencereGun + 1)
        val olcum = tamInt(json, "measurements"); val soru = tamInt(json, "questions"); val gunler = tamInt(json, "days")
        val kapsam = tamInt(json, "known_context_records")
        require(kapsam >= olcum && olcum >= soru && soru >= 0 && gunler in 0..politika.pencereGun && gunler <= olcum)
        val esik = if (duzey == ProfilDuzeyi.DERS) politika.ders else politika.altGrup
        val minOlcum = esik.olcum; val minSoru = esik.soru; val minGun = esik.gun
        require(tamInt(json, "minimum_measurements") == minOlcum && tamInt(json, "minimum_questions") == minSoru && tamInt(json, "minimum_days") == minGun)
        val yeterli = olcum >= minOlcum && soru >= minSoru && gunler >= minGun
        require((dayanak == VeriDayanagi.YETERLI) == yeterli)
        require((dayanak == VeriDayanagi.YETERSIZ) == (olcum == 0))
        require(json.isNull("distribution") == !yeterli)
        if (yeterli) {
            val d = json.getJSONObject("distribution")
            fun deger(key: String) = d.getDouble(key).also { require(it.isFinite() && it in 0.0..(SureOlcumu.EN_COK_AKTIF_MS * 2).toDouble()) }
            val medyan = deger("median_ms"); val alt = deger("q1_ms"); val ust = deger("q3_ms")
            deger("mad_ms"); require(alt <= medyan && medyan <= ust)
        }
    }

    fun tamLong(json: JSONObject, key: String): Long {
        val value = json.get(key)
        require(value is Int || value is Long) { "Tam sayı alanı: $key" }
        return (value as Number).toLong()
    }
    fun tamInt(json: JSONObject, key: String): Int = tamLong(json, key).also { require(it in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) }.toInt()
    fun bosLong(json: JSONObject, key: String): Long? { require(json.has(key)); return if (json.isNull(key)) null else tamLong(json, key) }
    fun bosMetin(json: JSONObject, key: String): String? { require(json.has(key)); return if (json.isNull(key)) null else (json.get(key) as String) }
}

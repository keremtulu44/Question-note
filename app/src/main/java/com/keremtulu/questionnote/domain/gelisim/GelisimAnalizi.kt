package com.keremtulu.questionnote.domain.gelisim

import com.keremtulu.questionnote.domain.calisma.OlcumKalitesi
import com.keremtulu.questionnote.domain.calisma.SureOlcumu
import com.keremtulu.questionnote.domain.model.*
import com.keremtulu.questionnote.domain.repeat.YakinGecmisHesabi
import com.keremtulu.questionnote.domain.repeat.YakinGecmis
import com.keremtulu.questionnote.domain.repeat.YakinTekrar

/** Labels are captured at review time. A null context is NOT reconstructed from today's question. */
data class TarihselBaglam(val dersId: String, val dersAdi: String, val konuId: String?, val konuAdi: String?,
    val kaynakAnahtari: String?, val kaynakEtiketi: String?)
data class AnalizTekrari(val id: String, val soruId: String, val gun: Int, val zaman: Long,
    val sonuc: TekrarSonucu, val cozumAcildi: Boolean, val baglam: TarihselBaglam?, val olcum: SureOlcumu?) {
    val bagimsiz: Boolean get() = sonuc == TekrarSonucu.RAHAT && !cozumAcildi
    val guvenilirSure: Boolean get() = olcum?.let { it.gecerliAlanlar() && it.kalite == OlcumKalitesi.TAM &&
        (it.toplamMs ?: 0) > 0 && (cozumAcildi || it.incelemeAktifMs == 0L) } == true
}

enum class AnalizTuru { KONU, KAYNAK }
data class GrupAnahtari(val tur: AnalizTuru, val dersId: String?, val deger: String?, val bilinen: Boolean = true)
data class AnalizSayaci(val soru: Int, val tekrar: Int, val bagimsiz: Int, val guvenilirSure: Int,
    val kismiSure: Int, val gecersizSure: Int, val baglamsiz: Int) {
    val kapsamYuzde: Int get() = if (tekrar == 0) 0 else (100L * guvenilirSure / tekrar).toInt()
}
data class EslesenSorular(val soru: Int, val onceBagimsiz: Int, val sonraBagimsiz: Int,
    val yeterli: Boolean, val baglamDegisen: Int)
data class DonemKarsilastirmasi(val onceki: AnalizSayaci, val son: AnalizSayaci, val eslesen: EslesenSorular,
    val oncekiIlkGun: Int, val sonIlkGun: Int, val sonGun: Int)
data class AnalizGrubu(val anahtar: GrupAnahtari, val dersAdi: String?, val etiketler: List<String>,
    val sayac: AnalizSayaci, val donem: DonemKarsilastirmasi, val filtre: SoruCalismaFiltresi?, val uygunSoru: Int,
    val yardimsizProfil: SureProfili?, val dersReferansi: SureProfili?, val yakinGuclukSoru: Int, val sonIkiBagimsizSoru: Int)
data class RitimGunu(val gun: Int, val soru: Int, val tekrar: Int, val bugunTamamlanmadi: Boolean)
data class PlanIsYuku(val gun: Int, val adet: Int, val kapsanan: Int, val bilinmeyen: Int,
    val altMs: Double, val ustMs: Double) {
    val kapsamYuzde: Int get() = if (adet == 0) 0 else (100L * kapsanan / adet).toInt()
    // Covered-question subtotals stay separate. Optional total extrapolation is allowed only at >=80% and labelled approximate.
    val toplamTahminAcik: Boolean get() = adet > 0 && kapsanan * 5L >= adet * 4L
    val toplamAltMs: Double? get() = if (toplamTahminAcik) altMs * adet / kapsanan else null
    val toplamUstMs: Double? get() = if (toplamTahminAcik) ustMs * adet / kapsanan else null
}
enum class OneriTuru { VADESI_GELEN, YAKIN_GUCLUK, KUCUK_TUR }
data class CalismaOnerisi(val tur: OneriTuru, val filtre: SoruCalismaFiltresi, val etiket: String?,
    val uygunSoru: Int, val guclukSoru: Int = 0) {
    val turBoyutu: Int get() = minOf(5, uygunSoru)
}
data class GelisimAnalizi(val gun: Int, val kapsamAdi: String, val toplam: AnalizSayaci,
    val konular: List<AnalizGrubu>, val kaynaklar: List<AnalizGrubu>, val ritim: List<RitimGunu>,
    val donem: DonemKarsilastirmasi, val plan: List<PlanIsYuku>, val oneri: CalismaOnerisi?)

/** Pure batch calculation. No database, Compose, cache, XP or plan mutation. */
object GelisimAnaliziHesabi {
    fun hesapla(sorular: List<CalismaSorusu>, dersler: List<Ders>, konular: Map<String, String>,
        tekrarlar: List<AnalizTekrari>, secim: SinavSecimi, gun: Int): GelisimAnalizi {
        val dersMap = dersler.associateBy { it.id }
        val tarihli = tekrarlar.filter { it.gun <= gun }
        val soruGecmisi = tarihli.groupBy { it.soruId }
        val gunKontrol = tekrarlar.groupBy { it.soruId }.mapValues { (_, l) -> l.mapTo(hashSetOf()) { it.gun } }
        fun uygun(q: CalismaSorusu, g: Int) = CalismaUygunlugu.uygun(q, dersMap[q.dersId], secim, g, g in gunKontrol[q.id].orEmpty())
        val bugunUygun = sorular.filter { uygun(it, gun) }
        val uygunGruplar = AnalizTuru.entries.associateWith { tur -> bugunUygun.groupingBy { anahtar(it, tur) }.eachCount() }
        val gruplar = AnalizTuru.entries.associateWith { tur -> tekrarlar.groupBy { anahtar(it.baglam, tur) } }
        val profiller = isYukuReferanslari(tarihli, gun)
        fun grupListesi(tur: AnalizTuru) = gruplar.getValue(tur).map { (key, rows) ->
            val son = rows.maxWith(compareBy<AnalizTekrari> { it.gun }.thenBy { it.zaman }.thenBy { it.id }).baglam
            val etiketler = rows.mapNotNull { if (tur == AnalizTuru.KONU) it.baglam?.konuAdi else it.baglam?.kaynakEtiketi }
                .distinct().sorted()
            val yakinlar = rows.filter { it.gun in (gun - 89)..gun }.groupBy { it.soruId }.values.map { l ->
                YakinGecmis(l.sortedWith(compareByDescending<AnalizTekrari> { it.gun }.thenByDescending { it.zaman }.thenBy { it.id })
                    .distinctBy { it.gun }.take(5).map { YakinTekrar(it.gun, it.sonuc, it.cozumAcildi) }) }
            AnalizGrubu(key, son?.dersAdi, etiketler, say(rows), donem(rows, gun), filtre(key), uygunGruplar.getValue(tur)[key] ?: 0,
                profiller.bagimsiz[key], profiller.bagimsiz[GrupAnahtari(AnalizTuru.KONU, key.dersId, null, false)]
                    ?.takeIf { it.dayanak == VeriDayanagi.YETERLI },
                yakinlar.count { it.tekrarGucluk }, yakinlar.count { it.toparlandi })
        }.sortedWith(compareBy<AnalizGrubu> { !it.anahtar.bilinen }.thenBy { it.dersAdi.orEmpty() }
            .thenBy { it.etiketler.firstOrNull().orEmpty() }.thenBy { it.anahtar.deger.orEmpty() })

        val planli = sorular.filter { q -> q.sonrakiTekrarGun?.let { it <= gun + 6 && uygun(q, maxOf(gun, it)) } == true }
            .groupBy { maxOf(gun, requireNotNull(it.sonrakiTekrarGun)) }
        val plan = (gun..gun + 6).map { g ->
            val qs = planli[g].orEmpty()
            val dagilimlar = qs.mapNotNull { q ->
                (q.konuId?.let { profiller.isYuku[GrupAnahtari(AnalizTuru.KONU, q.dersId, it)] }
                    ?: kaynakAnahtari(q.kaynak)?.let { profiller.isYuku[GrupAnahtari(AnalizTuru.KAYNAK, q.dersId, it)] }
                    ?: profiller.isYuku[GrupAnahtari(AnalizTuru.KONU, q.dersId, null, false)])?.dagilim
            }
            PlanIsYuku(g, qs.size, dagilimlar.size, qs.size - dagilimlar.size,
                dagilimlar.sumOf { it.altCeyrekMs }, dagilimlar.sumOf { it.ustCeyrekMs })
        }
        val ritim = tarihli.filter { it.gun >= gun - 6 }.groupBy { it.gun }
        return GelisimAnalizi(gun, secim.profil.ad, say(tekrarlar), grupListesi(AnalizTuru.KONU), grupListesi(AnalizTuru.KAYNAK),
            (gun - 6..gun).map { g -> val l = ritim[g].orEmpty(); RitimGunu(g, l.map { it.soruId }.distinct().size, l.size, g == gun) },
            donem(tarihli, gun), plan, oneri(bugunUygun, soruGecmisi, dersMap, konular, gun))
    }

    fun say(rows: List<AnalizTekrari>) = AnalizSayaci(rows.map { it.soruId }.distinct().size, rows.size,
        rows.count { it.bagimsiz }, rows.count { it.guvenilirSure }, rows.count { it.olcum?.kalite == OlcumKalitesi.KISMI },
        rows.count { it.olcum?.let { o -> o.kalite == OlcumKalitesi.GECERSIZ || !o.gecerliAlanlar() || !it.cozumAcildi && (o.incelemeAktifMs ?: 0) > 0 } == true },
        rows.count { it.baglam == null })

    fun donem(rows: List<AnalizTekrari>, gun: Int): DonemKarsilastirmasi {
        val once = rows.filter { it.gun in (gun - 14)..(gun - 8) }
        val son = rows.filter { it.gun in (gun - 7)..(gun - 1) }
        fun sonlar(l: List<AnalizTekrari>) = l.sortedWith(compareByDescending<AnalizTekrari> { it.gun }
            .thenByDescending { it.zaman }.thenBy { it.id }).distinctBy { it.soruId }.associateBy { it.soruId }
        val a = sonlar(once); val b = sonlar(son)
        val ortak = a.keys.intersect(b.keys)
        fun esBaglam(x: TarihselBaglam?, y: TarihselBaglam?) = x != null && y != null && x.dersId == y.dersId &&
            x.konuId == y.konuId && x.kaynakAnahtari == y.kaynakAnahtari
        val eslesen = ortak.filter { esBaglam(a.getValue(it).baglam, b.getValue(it).baglam) }
        val yeterli = eslesen.size >= 10 && eslesen.map { a.getValue(it).gun }.distinct().size >= 3 &&
            eslesen.map { b.getValue(it).gun }.distinct().size >= 3
        return DonemKarsilastirmasi(say(once), say(son), EslesenSorular(eslesen.size,
            eslesen.count { a.getValue(it).bagimsiz }, eslesen.count { b.getValue(it).bagimsiz }, yeterli, ortak.size - eslesen.size),
            gun - 14, gun - 7, gun - 1)
    }

    private data class Referanslar(val isYuku: Map<GrupAnahtari, SureProfili>, val bagimsiz: Map<GrupAnahtari, SureProfili>)
    private fun isYukuReferanslari(rows: List<AnalizTekrari>, gun: Int): Referanslar {
        val ornekler = rows.filter { it.gun >= gun - 59 }
            .sortedWith(compareByDescending<AnalizTekrari> { it.zaman }.thenBy { it.id })
            .distinctBy { it.soruId to it.gun }.mapNotNull { r ->
            val b = r.baglam ?: return@mapNotNull null
            val o = r.olcum ?: return@mapNotNull null
            SureOrnegi(r.id, r.id, r.soruId, r.gun, r.zaman, ProfilBaglami(b.dersId, b.konuId, b.kaynakAnahtari), r.sonuc, r.cozumAcildi, o)
        }
        val sonuc = mutableMapOf<GrupAnahtari, SureProfili>()
        val bagimsiz = mutableMapOf<GrupAnahtari, SureProfili>()
        // Each row belongs to only one lesson/topic/source bucket; never rescan the entire archive per group/question.
        ornekler.groupBy { it.baglam.dersId }.forEach { (id, l) ->
            val p = KisiselProfilHesabi.hesapla(l, ProfilBaglami(id), gun)
            val key = GrupAnahtari(AnalizTuru.KONU, id, null, false)
            p.isYuku.referans?.let { sonuc[key] = it }
            bagimsiz[key] = p.yardimsiz.istenen
        }
        AnalizTuru.entries.forEach { tur ->
            ornekler.groupBy { GrupAnahtari(tur, it.baglam.dersId, if (tur == AnalizTuru.KONU) it.baglam.konuId else it.baglam.kaynak) }
                .filterKeys { it.deger != null }.forEach { (key, l) ->
                    val b = ProfilBaglami(requireNotNull(key.dersId), if (tur == AnalizTuru.KONU) key.deger else null,
                        if (tur == AnalizTuru.KAYNAK) key.deger else null)
                    val p = KisiselProfilHesabi.hesapla(l, b, gun, duzey = if (tur == AnalizTuru.KONU) ProfilDuzeyi.KONU else ProfilDuzeyi.KAYNAK)
                    if (p.isYuku.istenen.dayanak == VeriDayanagi.YETERLI) sonuc[key] = p.isYuku.istenen
                    bagimsiz[key] = p.yardimsiz.istenen
                }
        }
        return Referanslar(sonuc, bagimsiz)
    }

    private fun oneri(uygun: List<CalismaSorusu>, gecmis: Map<String, List<AnalizTekrari>>, dersler: Map<String, Ders>,
        konular: Map<String, String>, gun: Int): CalismaOnerisi? {
        val vade = uygun.count { it.sonrakiTekrarGun != null && it.sonrakiTekrarGun <= gun }
        if (vade > 0) return CalismaOnerisi(OneriTuru.VADESI_GELEN, SoruCalismaFiltresi(vadesiGelen = true), null, vade)
        val gucluk = mutableMapOf<GrupAnahtari, Int>()
        val adetler = mutableMapOf<GrupAnahtari, Int>()
        uygun.forEach { q ->
            val yakin = gecmis[q.id].orEmpty().filter { it.gun in (gun - YakinGecmisHesabi.PENCERE_GUN + 1)..gun }
                .sortedWith(compareByDescending<AnalizTekrari> { it.gun }.thenByDescending { it.zaman }.thenBy { it.id })
                .distinctBy { it.gun }.take(YakinGecmisHesabi.EN_COK_GUN)
            val toparlandi = yakin.size >= 2 && yakin.take(2).all { it.bagimsiz }
            AnalizTuru.entries.forEach { tur ->
                val key = anahtar(q, tur)
                adetler[key] = (adetler[key] ?: 0) + 1
                // Never attribute an old/unknown book or topic's difficulty to a newly edited current label.
                if (key.deger != null && !toparlandi && yakin.count { !it.bagimsiz && anahtar(it.baglam, tur) == key } >= 2)
                    gucluk[key] = (gucluk[key] ?: 0) + 1
            }
        }
        val secilen = gucluk.entries.sortedWith(compareByDescending<Map.Entry<GrupAnahtari, Int>> { it.value }
            .thenBy { it.key.tur.ordinal }.thenBy { it.key.dersId }.thenBy { it.key.deger }).firstOrNull()
        if (secilen != null) {
            val k = secilen.key
            val ad = if (k.tur == AnalizTuru.KONU) konular[k.deger] else uygun.firstOrNull { anahtar(it, k.tur) == k }?.kaynak
            return CalismaOnerisi(OneriTuru.YAKIN_GUCLUK, requireNotNull(filtre(k)),
                listOfNotNull(dersler[k.dersId]?.ad, ad).joinToString(" · "), adetler.getValue(k), secilen.value)
        }
        return if (uygun.isEmpty()) null else CalismaOnerisi(OneriTuru.KUCUK_TUR, SoruCalismaFiltresi(), null, uygun.size)
    }

    private fun anahtar(q: CalismaSorusu, tur: AnalizTuru) = GrupAnahtari(tur, q.dersId,
        if (tur == AnalizTuru.KONU) q.konuId else kaynakAnahtari(q.kaynak))
    private fun anahtar(b: TarihselBaglam?, tur: AnalizTuru) = GrupAnahtari(tur, b?.dersId,
        if (tur == AnalizTuru.KONU) b?.konuId else b?.kaynakAnahtari, b != null)
    private fun filtre(k: GrupAnahtari): SoruCalismaFiltresi? = if (!k.bilinen) null else when (k.tur) {
        AnalizTuru.KONU -> SoruCalismaFiltresi(dersId = k.dersId, konuId = k.deger, konusuz = k.deger == null)
        AnalizTuru.KAYNAK -> SoruCalismaFiltresi(dersId = k.dersId, kaynakAnahtari = k.deger, kaynakBelirtilmemis = k.deger == null)
    }
}

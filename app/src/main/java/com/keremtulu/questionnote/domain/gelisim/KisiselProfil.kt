package com.keremtulu.questionnote.domain.gelisim

import com.keremtulu.questionnote.domain.calisma.OlcumKalitesi
import com.keremtulu.questionnote.domain.calisma.SureOlcumu
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import java.text.Normalizer
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor

/** Case/spacing/NFC only. Never fuzzy-match books or compare different lessons as one population. */
fun kaynakAnahtari(etiket: String?): String? = etiket?.let {
    Normalizer.normalize(it, Normalizer.Form.NFC).trim().replace(Regex("\\s+"), " ")
        .lowercase(Locale.forLanguageTag("tr-TR")).let { s -> Normalizer.normalize(s, Normalizer.Form.NFC) }
        .takeIf(String::isNotEmpty)
}

enum class ProfilDuzeyi { DERS, KONU, KAYNAK }
enum class VeriDayanagi { YETERSIZ, SINIRLI, YETERLI }
data class ProfilEsigi(val olcum: Int, val soru: Int, val gun: Int)
data class ProfilPolitikasi(
    val surum: Int = 1,
    val pencereGun: Int = 60,
    val ders: ProfilEsigi = ProfilEsigi(20, 10, 5),
    val altGrup: ProfilEsigi = ProfilEsigi(30, 15, 7),
) {
    init { require(surum == 1 && pencereGun in 1..365); listOf(ders, altGrup).forEach { require(it.olcum > 0 && it.soru > 0 && it.gun > 0) } }
}

data class ProfilBaglami(val dersId: String, val konuId: String? = null, val kaynak: String? = null)

data class SureOrnegi(
    val tekrarId: String,
    val denemeId: String,
    val soruId: String,
    val gun: Int,
    val zaman: Long,
    val baglam: ProfilBaglami,
    val sonuc: TekrarSonucu,
    val cozumAcildi: Boolean,
    val olcum: SureOlcumu,
)

data class DayanikliDagilim(val medyanMs: Double, val altCeyrekMs: Double, val ustCeyrekMs: Double, val madMs: Double) {
    val iqrMs: Double get() = (ustCeyrekMs - altCeyrekMs).coerceAtLeast(0.0)
}

data class SureProfili(
    val duzey: ProfilDuzeyi,
    val dayanak: VeriDayanagi,
    val olcumSayisi: Int,
    val farkliSoru: Int,
    val farkliGun: Int,
    val kapsamKaydi: Int,
    val esik: ProfilEsigi,
    val ilkGun: Int,
    val sonGun: Int,
    val dagilim: DayanikliDagilim?,
)

data class ProfilSecimi(val istenen: SureProfili, val referans: SureProfili?) {
    val derseDonuldu: Boolean get() = referans?.duzey == ProfilDuzeyi.DERS && istenen.duzey != ProfilDuzeyi.DERS
}

data class KisiselProfil(val politikaSurumu: Int, val yardimsiz: ProfilSecimi, val isYuku: ProfilSecimi)

data class DersSureProfili(val dersId: String, val dersAdi: String, val profil: KisiselProfil)

data class SureKapsami(
    val ilkGun: Int,
    val sonGun: Int,
    val toplamTekrar: Int,
    val tamOlcum: Int,
    val kismiOlcum: Int,
    val olcumsuz: Int,
    val gecersizOlcum: Int,
    val dersler: List<DersSureProfili>,
)

/** Profile only. It cannot assign a review date, reward speed, or label the student's ability. */
object KisiselProfilHesabi {
    fun hesapla(
        veriler: List<SureOrnegi>, baglam: ProfilBaglami, bugun: Int,
        mevcutDenemeId: String? = null, duzey: ProfilDuzeyi = ProfilDuzeyi.DERS,
        politika: ProfilPolitikasi = ProfilPolitikasi(),
    ): KisiselProfil {
        val ilk = (bugun.toLong() - politika.pencereGun + 1).coerceAtLeast(Int.MIN_VALUE.toLong()).toInt()
        val ders = veriler.asSequence()
            .filter { it.baglam.dersId == baglam.dersId && it.gun in ilk..bugun && it.denemeId != mevcutDenemeId }
            .sortedWith(compareByDescending<SureOrnegi> { it.zaman }.thenBy { it.tekrarId })
            .distinctBy { it.soruId to it.gun }.toList()
        val alt = when (duzey) {
            ProfilDuzeyi.DERS -> ders
            ProfilDuzeyi.KONU -> ders.filter { baglam.konuId != null && it.baglam.konuId == baglam.konuId }
            ProfilDuzeyi.KAYNAK -> ders.filter { baglam.kaynak != null && it.baglam.kaynak == baglam.kaynak }
        }
        fun sec(yardimsiz: Boolean): ProfilSecimi {
            val istenen = profil(alt, duzey, yardimsiz, ilk, bugun, politika)
            val referans = if (istenen.dayanak == VeriDayanagi.YETERLI) istenen
                else if (duzey != ProfilDuzeyi.DERS) profil(ders, ProfilDuzeyi.DERS, yardimsiz, ilk, bugun, politika)
                    .takeIf { it.dayanak == VeriDayanagi.YETERLI } else null
            return ProfilSecimi(istenen, referans)
        }
        return KisiselProfil(politika.surum, sec(true), sec(false))
    }

    private fun profil(veriler: List<SureOrnegi>, duzey: ProfilDuzeyi, yardimsiz: Boolean,
        ilk: Int, son: Int, politika: ProfilPolitikasi): SureProfili {
        val uygun = veriler.mapNotNull { ornek ->
            val olcum = ornek.olcum
            if (!olcum.gecerliAlanlar() || olcum.kalite != OlcumKalitesi.TAM ||
                (!ornek.cozumAcildi && olcum.incelemeAktifMs != 0L)) return@mapNotNull null
            val deger = if (yardimsiz) {
                if (ornek.sonuc != TekrarSonucu.RAHAT || ornek.cozumAcildi) return@mapNotNull null
                olcum.denemeAktifMs
            } else olcum.toplamMs
            if (deger == null || deger <= 0) null else ornek to deger.toDouble()
        }
        // Each question gets exactly one vote; repeat counts are reported but cannot dominate the median.
        val sorular = uygun.groupBy { it.first.soruId }.values.map { grup -> medyan(grup.map { it.second }) }
        val gunler = uygun.map { it.first.gun }.distinct().size
        val esik = if (duzey == ProfilDuzeyi.DERS) politika.ders else politika.altGrup
        val yeterli = uygun.size >= esik.olcum && sorular.size >= esik.soru && gunler >= esik.gun
        val dagilim = if (!yeterli) null else {
            val ortanca = medyan(sorular)
            DayanikliDagilim(ortanca, yuzdelik(sorular, 0.25), yuzdelik(sorular, 0.75), medyan(sorular.map { abs(it - ortanca) }))
        }
        return SureProfili(duzey, when { yeterli -> VeriDayanagi.YETERLI; uygun.isNotEmpty() -> VeriDayanagi.SINIRLI; else -> VeriDayanagi.YETERSIZ },
            uygun.size, sorular.size, gunler, veriler.size, esik, ilk, son, dagilim)
    }

    private fun medyan(degerler: List<Double>): Double = yuzdelik(degerler, 0.5)
    private fun yuzdelik(degerler: List<Double>, oran: Double): Double {
        require(degerler.isNotEmpty())
        val sirali = degerler.sorted()
        val konum = (sirali.size - 1) * oran
        val alt = floor(konum).toInt()
        val ust = (alt + 1).coerceAtMost(sirali.lastIndex)
        return sirali[alt] + (sirali[ust] - sirali[alt]) * (konum - alt)
    }
}

package com.keremtulu.questionnote.domain.repeat

import com.keremtulu.questionnote.domain.calisma.OlcumKalitesi
import com.keremtulu.questionnote.domain.calisma.TekrarDenemesi
import com.keremtulu.questionnote.domain.gelisim.KisiselProfil
import com.keremtulu.questionnote.domain.gelisim.SureProfili
import com.keremtulu.questionnote.domain.gelisim.VeriDayanagi
import com.keremtulu.questionnote.domain.model.OgrenmeDurumu
import com.keremtulu.questionnote.domain.model.TekrarKaydi
import com.keremtulu.questionnote.domain.model.TekrarPlani
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import java.time.LocalDate

enum class TekrarMotorModu { KAPALI, GOLGE, ETKIN }

/** Active rollout is a release gate, never a preference that can silently bypass device/shadow acceptance. */
data class MotorYayinKapisi(val cihazKabul: Boolean = false, val golgeKabul: Boolean = false) {
    val etkinAcik: Boolean get() = cihazKabul && golgeKabul
}
data class MotorAyari(val istenen: TekrarMotorModu = TekrarMotorModu.GOLGE, val kapi: MotorYayinKapisi = MotorYayinKapisi()) {
    val uygulanan: TekrarMotorModu get() = if (istenen == TekrarMotorModu.ETKIN && !kapi.etkinAcik) TekrarMotorModu.GOLGE else istenen
}
fun interface TekrarModuSaglayici {
    fun oku(): MotorAyari
}

enum class KararNedeni {
    KATMAN_KAPALI, ETKIN_KAPISI_KAPALI, DESTEKLENMEYEN_GECMIS, COZUM_VEYA_COZEMEDI,
    ZORLANDIM_KURALI_KORUNDU, OLCUM_YETERSIZ, PROFIL_YETERSIZ, YAKIN_GUCLUK_YOK,
    TOPARLANMA, SURE_TEK_BASINA_ETKEN_DEGIL, IKINCI_INDIRIM_YOK, KUCUK_ARALIK_KORUNDU,
    BIRLIKTE_SINYAL, GOLGE_TARIH_KORUNDU, V3_SINIRLI_DUZELTME,
}

data class YakinTekrar(val gun: Int, val sonuc: TekrarSonucu, val cozumAcildi: Boolean) {
    val bagimsiz: Boolean get() = sonuc == TekrarSonucu.RAHAT && !cozumAcildi
    val gucluk: Boolean get() = !bagimsiz
}
data class YakinGecmis(val gunler: List<YakinTekrar>) {
    val toparlandi: Boolean get() = gunler.size >= 2 && gunler.take(2).all { it.bagimsiz }
    val guclukGunleri: Int get() = gunler.count { it.gucluk }
    val tekrarGucluk: Boolean get() = !toparlandi && guclukGunleri >= 2
}
object YakinGecmisHesabi {
    const val PENCERE_GUN = 90
    const val EN_COK_GUN = 5
    fun hesapla(gecmis: List<TekrarKaydi>, bugun: Int): YakinGecmis = YakinGecmis(gecmis
        .filter { it.gun.toLong() in (bugun.toLong() - PENCERE_GUN + 1)..bugun.toLong() }
        .sortedWith(compareByDescending<TekrarKaydi> { it.gun }.thenByDescending { it.zaman }.thenBy { it.id })
        .distinctBy { it.gun }.take(EN_COK_GUN).map { YakinTekrar(it.gun, it.sonuc, it.cozumAcildi) })
}

data class TekrarKarari(
    val istenenMod: TekrarMotorModu,
    val mod: TekrarMotorModu,
    val temel: TekrarPlani,
    val aday: TekrarPlani,
    val uygulanan: TekrarPlani,
    val algoritmaSurumu: Int,
    val nedenler: Set<KararNedeni>,
    val yakin: YakinGecmis,
    val desteklenmeyenGecmis: Boolean,
    val v2GecikmeIndirimi: Boolean,
)

/** Bounded v3 candidate. No speed bonus, no slowness-only penalty, no stacking with v2's late discount. */
object UyarlanabilirTekrarKurali {
    const val SURUM = 3
    val DESTEKLENEN_GECMIS = setOf(1, 2, 3)

    fun hesapla(gecmis: List<TekrarKaydi>, deneme: TekrarDenemesi, profil: KisiselProfil,
        bugun: LocalDate, planlananGun: Int?, ayar: MotorAyari): TekrarKarari {
        val temel = AkilliTekrarKurali.hesapla(gecmis, deneme.sonuc, deneme.cozumAcildi, bugun, planlananGun)
        val yakin = YakinGecmisHesabi.hesapla(gecmis.filter { it.denemeId != deneme.denemeId }, bugun.toEpochDay().toInt())
        val gecikme = planlananGun?.let { bugun.toEpochDay() - it > 2L * temel.oncekiAralik } == true &&
            !deneme.cozumAcildi && deneme.sonuc != TekrarSonucu.COZEMEDIM
        return karar(temel, deneme, profil.yardimsiz.referans, bugun.toEpochDay().toInt(), ayar.istenen,
            ayar.uygulanan, yakin, gecmis.any { it.algoritmaSurumu !in DESTEKLENEN_GECMIS }, gecikme)
    }

    /** Also used to validate bounded snapshots on import; inputs are validated by the codec. */
    fun karar(temel: TekrarPlani, deneme: TekrarDenemesi, referans: SureProfili?, gun: Int,
        istenen: TekrarMotorModu, mod: TekrarMotorModu, yakin: YakinGecmis,
        bilinmeyen: Boolean, gecikmeIndirimi: Boolean): TekrarKarari {
        val nedenler = linkedSetOf<KararNedeni>()
        if (istenen == TekrarMotorModu.ETKIN && mod != TekrarMotorModu.ETKIN) nedenler += KararNedeni.ETKIN_KAPISI_KAPALI
        val o = deneme.olcum
        val sureUygun = o.gecerliAlanlar() && o.kalite == OlcumKalitesi.TAM && o.incelemeAktifMs == 0L && (o.denemeAktifMs ?: 0L) > 0L
        var aralik = temel.sonrakiAralik
        when {
            mod == TekrarMotorModu.KAPALI -> nedenler += KararNedeni.KATMAN_KAPALI
            bilinmeyen -> nedenler += KararNedeni.DESTEKLENMEYEN_GECMIS
            deneme.cozumAcildi || deneme.sonuc == TekrarSonucu.COZEMEDIM -> nedenler += KararNedeni.COZUM_VEYA_COZEMEDI
            deneme.sonuc == TekrarSonucu.ZORLANDIM -> nedenler += KararNedeni.ZORLANDIM_KURALI_KORUNDU
            yakin.toparlandi -> nedenler += KararNedeni.TOPARLANMA
            !sureUygun -> nedenler += KararNedeni.OLCUM_YETERSIZ
            !guvenilir(referans) -> nedenler += KararNedeni.PROFIL_YETERSIZ
            !yakin.tekrarGucluk -> nedenler += KararNedeni.YAKIN_GUCLUK_YOK
            !belirginUzun(o.denemeAktifMs!!, requireNotNull(referans)) -> nedenler += KararNedeni.SURE_TEK_BASINA_ETKEN_DEGIL
            gecikmeIndirimi -> nedenler += KararNedeni.IKINCI_INDIRIM_YOK
            aralik < 10 -> nedenler += KararNedeni.KUCUK_ARALIK_KORUNDU
            else -> {
                // floor: even after integer rounding the ACTUAL reduction cannot exceed ten percent.
                aralik -= aralik / 10
                nedenler += KararNedeni.BIRLIKTE_SINYAL
            }
        }
        val aday = plan(temel, aralik.coerceIn(1, 180), gun)
        val uygulanan = if (mod == TekrarMotorModu.ETKIN) aday else temel
        if (mod == TekrarMotorModu.GOLGE) nedenler += KararNedeni.GOLGE_TARIH_KORUNDU
        if (mod == TekrarMotorModu.ETKIN && aday.sonrakiAralik != temel.sonrakiAralik) nedenler += KararNedeni.V3_SINIRLI_DUZELTME
        return TekrarKarari(istenen, mod, temel, aday, uygulanan,
            if (mod == TekrarMotorModu.ETKIN) SURUM else 2, nedenler, yakin, bilinmeyen, gecikmeIndirimi)
    }

    private fun guvenilir(profil: SureProfili?): Boolean {
        if (profil == null || profil.dayanak != VeriDayanagi.YETERLI) return false
        val d = profil.dagilim ?: return false
        return profil.olcumSayisi >= profil.esik.olcum && profil.farkliSoru >= profil.esik.soru && profil.farkliGun >= profil.esik.gun &&
            listOf(d.medyanMs, d.altCeyrekMs, d.ustCeyrekMs, d.madMs).all { it.isFinite() && it >= 0 } &&
            d.altCeyrekMs > 0 && d.altCeyrekMs <= d.medyanMs && d.medyanMs <= d.ustCeyrekMs
    }
    private fun belirginUzun(ms: Long, profil: SureProfili): Boolean {
        val d = profil.dagilim ?: return false
        if (!listOf(d.medyanMs, d.madMs, d.iqrMs).all { it.isFinite() && it >= 0 } || d.medyanMs <= 0) return false
        return ms > d.medyanMs * 1.5 && ms > d.medyanMs + maxOf(3 * d.madMs, 1.5 * d.iqrMs)
    }
    private fun plan(temel: TekrarPlani, aralik: Int, gun: Int) = temel.copy(sonrakiAralik = aralik,
        sonrakiGun = Math.addExact(gun, aralik), ogrenmeDurumu = when {
            aralik <= 3 -> OgrenmeDurumu.OGRENIYOR
            aralik <= 21 -> OgrenmeDurumu.PEKISIYOR
            else -> OgrenmeDurumu.SAGLAM
        })
}

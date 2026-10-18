package com.keremtulu.questionnote.domain.model

import com.keremtulu.questionnote.domain.gelisim.kaynakAnahtari

/** Slim current question, shared by analysis and practice. Historical labels belong elsewhere. */
data class CalismaSorusu(val id: String, val dersId: String, val konuId: String?, val kaynak: String?,
    val planDurumu: SoruPlanDurumu, val sonTekrarGun: Int?, val sonrakiTekrarGun: Int?)

object CalismaUygunlugu {
    fun uygun(q: CalismaSorusu, ders: Ders?, secim: SinavSecimi, gun: Int, ayniGunKaydi: Boolean): Boolean =
        ders != null && !ders.arsivli && secim.gorunur(ders) && q.planDurumu == SoruPlanDurumu.AKTIF &&
            (q.sonTekrarGun == null || q.sonTekrarGun < gun) && !ayniGunKaydi

    fun filtreUyar(q: CalismaSorusu, f: SoruCalismaFiltresi, gun: Int): Boolean =
        (f.dersId == null || f.dersId == q.dersId) && (!f.vadesiGelen || q.sonrakiTekrarGun != null && q.sonrakiTekrarGun <= gun) &&
            (f.konuId == null || f.konuId == q.konuId) && (!f.konusuz || q.konuId == null) &&
            (f.kaynak == null || f.kaynak == q.kaynak) && (!f.kaynakBelirtilmemis || kaynakAnahtari(q.kaynak) == null) &&
            (f.kaynakAnahtari == null || f.kaynakAnahtari == kaynakAnahtari(q.kaynak))
}

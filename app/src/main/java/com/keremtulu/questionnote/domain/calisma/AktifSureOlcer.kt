package com.keremtulu.questionnote.domain.calisma

import com.keremtulu.questionnote.domain.model.TekrarSonucu

fun interface MonotonikSaat {
    fun simdiMs(): Long
}

/** Pure segment accumulator. UI ticks only read it; wall-clock changes never produce study time. */
class AktifSureOlcer(private val saat: MonotonikSaat, baslangic: OlcumDevamNoktasi) {
    private var nokta = baslangic.copy(olcum = baslangic.olcum.guvenli())
    private var basladi: Long? = null
    private var sonSaat: Long? = null
    val donduruldu: Boolean get() = nokta.donduruldu
    val cozumAcildi: Boolean get() = nokta.cozumAcildi

    fun aktifligiAyarla(aktif: Boolean) {
        biriktir()
        basladi = if (aktif && !nokta.donduruldu && nokta.olcum.kalite != OlcumKalitesi.GECERSIZ) okuSaat() else null
    }

    fun cozumuAc() {
        biriktir()
        nokta = nokta.copy(cozumAcildi = true)
    }

    fun tamEkranaGir() {
        aktifligiAyarla(false)
        kismiIsaretle(OlcumNedeni.TAM_EKRAN)
    }

    fun dondur(secim: TekrarSonucu? = nokta.secim) {
        aktifligiAyarla(false)
        nokta = nokta.copy(donduruldu = true, secim = secim)
    }

    fun calismayaDon() { nokta = nokta.copy(donduruldu = false, secim = null) }

    fun kismiIsaretle(neden: OlcumNedeni) {
        if (nokta.olcum.kalite in setOf(OlcumKalitesi.TAM, OlcumKalitesi.KISMI)) {
            nokta = nokta.copy(olcum = nokta.olcum.copy(kalite = OlcumKalitesi.KISMI, nedenler = nokta.olcum.nedenler + neden))
        }
    }

    /** Flushes the current segment, but leaves active study running from a fresh monotonic origin. */
    fun devamNoktasi(): OlcumDevamNoktasi {
        biriktir()
        nokta = nokta.copy(revizyon = nokta.revizyon + 1)
        return nokta
    }

    fun olcum(): SureOlcumu { biriktir(); return nokta.olcum }

    private fun okuSaat(): Long? {
        val simdi = saat.simdiMs()
        if (simdi < 0 || sonSaat?.let { simdi < it } == true) {
            gecersiz(OlcumNedeni.SAAT_TUTARSIZ)
            return null
        }
        sonSaat = simdi
        return simdi
    }

    private fun biriktir() {
        val ilk = basladi ?: return
        val simdi = okuSaat() ?: return
        val fark = simdi - ilk // nonnegative, checked by okuSaat; no wall time or reboot origin.
        val onceki = if (nokta.cozumAcildi) nokta.olcum.incelemeAktifMs else nokta.olcum.denemeAktifMs
        if (onceki == null || fark > SureOlcumu.EN_COK_AKTIF_MS - onceki) {
            gecersiz(OlcumNedeni.SURE_SINIRI)
            return
        }
        nokta = nokta.copy(olcum = if (nokta.cozumAcildi) nokta.olcum.copy(incelemeAktifMs = onceki + fark)
            else nokta.olcum.copy(denemeAktifMs = onceki + fark))
        basladi = simdi
    }

    private fun gecersiz(neden: OlcumNedeni) {
        basladi = null
        nokta = nokta.copy(olcum = nokta.olcum.copy(kalite = OlcumKalitesi.GECERSIZ, nedenler = nokta.olcum.nedenler + neden))
    }
}

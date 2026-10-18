package com.keremtulu.questionnote.domain.calisma

import com.keremtulu.questionnote.domain.model.TekrarPlani
import com.keremtulu.questionnote.domain.model.TekrarSonucu

/** TAM describes this screen's measurement coverage, never the student's attention or ability. */
enum class OlcumKalitesi { YOK, TAM, KISMI, GECERSIZ }
enum class OlcumNedeni { SUREC_KURTARMA, TAM_EKRAN, GORSEL_EKSIK, ESKI_OTURUM, SAAT_TUTARSIZ, SURE_SINIRI, GECERSIZ_GIRDI }

data class SureOlcumu(
    val denemeAktifMs: Long? = null,
    val incelemeAktifMs: Long? = null,
    val kalite: OlcumKalitesi = OlcumKalitesi.YOK,
    val nedenler: Set<OlcumNedeni> = emptySet(),
    val surum: Int = 1,
) {
    val toplamMs: Long? get() = if (gecerliAlanlar() && denemeAktifMs != null && incelemeAktifMs != null)
        denemeAktifMs + incelemeAktifMs else null

    fun gecerliAlanlar(): Boolean = surum == 1 &&
        (denemeAktifMs == null || denemeAktifMs in 0..EN_COK_AKTIF_MS) &&
        (incelemeAktifMs == null || incelemeAktifMs in 0..EN_COK_AKTIF_MS) &&
        ((denemeAktifMs == null) == (incelemeAktifMs == null)) &&
        when (kalite) {
            OlcumKalitesi.YOK -> denemeAktifMs == null && nedenler.isEmpty()
            OlcumKalitesi.TAM -> denemeAktifMs != null && nedenler.isEmpty()
            OlcumKalitesi.KISMI -> denemeAktifMs != null && nedenler.isNotEmpty()
            OlcumKalitesi.GECERSIZ -> nedenler.isNotEmpty()
        }

    /** Invalid/missing measurement must not prevent a valid self-assessment from being saved. */
    fun guvenli(): SureOlcumu = if (gecerliAlanlar()) this else
        SureOlcumu(kalite = OlcumKalitesi.GECERSIZ, nedenler = setOf(OlcumNedeni.GECERSIZ_GIRDI))

    companion object {
        const val EN_COK_AKTIF_MS = 24L * 60 * 60 * 1_000
    }
}

/** No elapsedRealtime origin is persisted. Only accumulated segments and immutable attempt identity. */
data class OlcumDevamNoktasi(
    val soruId: String,
    val denemeId: String,
    val olcum: SureOlcumu = SureOlcumu(0, 0, OlcumKalitesi.TAM),
    val cozumAcildi: Boolean = false,
    val donduruldu: Boolean = false,
    val secim: TekrarSonucu? = null,
    val revizyon: Long = 0,
) {
    fun surectenKurtar(): OlcumDevamNoktasi {
        val kurtarilan = if (olcum.kalite == OlcumKalitesi.TAM || olcum.kalite == OlcumKalitesi.KISMI) {
            olcum.copy(kalite = OlcumKalitesi.KISMI, nedenler = olcum.nedenler + OlcumNedeni.SUREC_KURTARMA)
        } else olcum
        return copy(olcum = kurtarilan)
    }

}

data class TekrarDenemesi(
    val denemeId: String,
    val soruId: String,
    val sonuc: TekrarSonucu,
    val cozumAcildi: Boolean,
    val olcum: SureOlcumu = SureOlcumu(),
    val oturumId: String? = null,
)

/** The v2 plan and its inputs are frozen together. Saving checks this token again in the DB transaction. */
data class TekrarOnizlemesi(
    val deneme: TekrarDenemesi,
    val gun: Int,
    val plan: TekrarPlani,
    val baglamImzasi: String,
    val kararOzeti: String,
    val saatDilimi: String,
)

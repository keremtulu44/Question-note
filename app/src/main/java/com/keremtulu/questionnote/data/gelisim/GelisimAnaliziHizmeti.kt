package com.keremtulu.questionnote.data.gelisim

import androidx.room.withTransaction
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.domain.calisma.OlcumKalitesi
import com.keremtulu.questionnote.domain.calisma.OlcumNedeni
import com.keremtulu.questionnote.domain.calisma.SureOlcumu
import com.keremtulu.questionnote.domain.gelisim.*
import com.keremtulu.questionnote.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Four batched reads, no N+1 group queries, images, notes or decision JSON; no stale materialized cache. */
class GelisimAnaliziHizmeti(private val db: YanlisDefteriDatabase) {
    suspend fun hesapla(gun: Int, secim: SinavSecimi): GelisimAnalizi = withContext(Dispatchers.Default) {
        val veri = db.withTransaction {
            val sorular = db.soruDao().analizSorulari().map { q -> CalismaSorusu(q.id, q.dersId, q.konuId, q.kaynak,
                SoruPlanDurumu.valueOf(q.planDurumu), q.sonTekrarGun, q.sonrakiTekrarGun) }
            val dersler = db.dersDao().tumunuGetir().map { Ders(it.id, it.ad, it.sira, 0, it.arsivli) }
            val konular = db.konuDao().tumunuGetir().associate { it.id to it.ad }
            val tekrarlar = db.tekrarOlcumDao().analizVerileri().mapNotNull { row ->
                val t = row.tekrar
                val sonuc = TekrarSonucu.entries.firstOrNull { it.name == t.sonuc } ?: return@mapNotNull null
                val m = row.olcum
                val o = m?.let { runCatching { OlcumSerilestirme.olcum(it).guvenli() }.getOrElse {
                    SureOlcumu(kalite = OlcumKalitesi.GECERSIZ, nedenler = setOf(OlcumNedeni.GECERSIZ_GIRDI)) } }
                AnalizTekrari(t.id, t.soruId, t.gun, t.zaman, sonuc, t.cozumAcildi, m?.let {
                    TarihselBaglam(it.dersId, it.dersAdi, it.konuId, it.konuAdi, it.kaynakAnahtari, it.kaynakEtiketi) }, o)
            }
            Girdi(sorular, dersler, konular, tekrarlar)
        }
        GelisimAnaliziHesabi.hesapla(veri.sorular, veri.dersler, veri.konular, veri.tekrarlar, secim, gun)
    }
    private data class Girdi(val sorular: List<CalismaSorusu>, val dersler: List<Ders>, val konular: Map<String, String>,
        val tekrarlar: List<AnalizTekrari>)
}

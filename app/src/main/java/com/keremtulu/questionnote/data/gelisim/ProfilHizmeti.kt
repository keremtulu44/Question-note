package com.keremtulu.questionnote.data.gelisim

import androidx.room.withTransaction
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.dao.ProfilSatiri
import com.keremtulu.questionnote.domain.calisma.OlcumKalitesi
import com.keremtulu.questionnote.domain.calisma.OlcumNedeni
import com.keremtulu.questionnote.domain.calisma.SureOlcumu
import com.keremtulu.questionnote.domain.gelisim.DersSureProfili
import com.keremtulu.questionnote.domain.gelisim.KisiselProfil
import com.keremtulu.questionnote.domain.gelisim.KisiselProfilHesabi
import com.keremtulu.questionnote.domain.gelisim.ProfilBaglami
import com.keremtulu.questionnote.domain.gelisim.ProfilDuzeyi
import com.keremtulu.questionnote.domain.gelisim.ProfilPolitikasi
import com.keremtulu.questionnote.domain.gelisim.SureKapsami
import com.keremtulu.questionnote.domain.gelisim.SureOrnegi
import com.keremtulu.questionnote.domain.model.TekrarSonucu

/** No persisted profile cache: import/delete/rename cannot leave a stale materialized result. */
class ProfilHizmeti(private val db: YanlisDefteriDatabase) {
    private val dao = db.tekrarOlcumDao()
    private val politika = ProfilPolitikasi()

    suspend fun profil(baglam: ProfilBaglami, gun: Int, denemeId: String?,
        duzey: ProfilDuzeyi = if (baglam.konuId != null) ProfilDuzeyi.KONU else if (baglam.kaynak != null) ProfilDuzeyi.KAYNAK else ProfilDuzeyi.DERS,
    ): KisiselProfil = db.withTransaction {
        KisiselProfilHesabi.hesapla(dao.dersVerileri(baglam.dersId, gun - politika.pencereGun + 1, gun).map(::ornek),
            baglam, gun, denemeId, duzey, politika)
    }

    suspend fun kapsam(gun: Int): SureKapsami = db.withTransaction {
        val ilk = gun - politika.pencereGun + 1
        val satirlar = dao.pencereVerileri(ilk, gun)
        val ornekler = satirlar.map(::ornek)
        val toplam = dao.pencereSayisi(ilk, gun)
        val gruplar = satirlar.groupBy { it.olcum.dersId }
        val dersler = ornekler.groupBy { it.baglam.dersId }.map { (id, kayitlar) ->
            DersSureProfili(id, gruplar.getValue(id).last().olcum.dersAdi,
                KisiselProfilHesabi.hesapla(kayitlar, ProfilBaglami(id), gun, politika = politika))
        }.sortedBy { it.dersAdi }
        SureKapsami(ilk, gun, toplam, ornekler.count { it.olcum.kalite == OlcumKalitesi.TAM },
            ornekler.count { it.olcum.kalite == OlcumKalitesi.KISMI },
            toplam - ornekler.size + ornekler.count { it.olcum.kalite == OlcumKalitesi.YOK },
            ornekler.count { it.olcum.kalite == OlcumKalitesi.GECERSIZ }, dersler)
    }

    private fun ornek(satir: ProfilSatiri): SureOrnegi {
        val e = satir.olcum
        val olcum = runCatching { OlcumSerilestirme.olcum(e).guvenli() }.getOrElse {
            SureOlcumu(kalite = OlcumKalitesi.GECERSIZ, nedenler = setOf(OlcumNedeni.GECERSIZ_GIRDI))
        }
        return SureOrnegi(e.tekrarId, e.denemeId, satir.soruId, satir.gun, satir.zaman,
            ProfilBaglami(e.dersId, e.konuId, e.kaynakAnahtari), TekrarSonucu.valueOf(satir.sonuc), satir.cozumAcildi, olcum)
    }
}

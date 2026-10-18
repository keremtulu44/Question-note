package com.keremtulu.questionnote.data.ilerleme

import androidx.room.withTransaction
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.prefs.GunlukHedefDeposu
import com.keremtulu.questionnote.data.prefs.RozetDeposu
import com.keremtulu.questionnote.domain.karne.GunDenge
import com.keremtulu.questionnote.domain.karne.KarneHesabi
import com.keremtulu.questionnote.domain.karne.OturumSonucu
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.domain.karne.RozetGirdisi
import com.keremtulu.questionnote.domain.karne.RozetKurallari
import com.keremtulu.questionnote.domain.seviye.XpHesabi
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withContext

/** A single source for the gallery, retroactive lifetime awards and completed-session awards. */
class RozetHizmeti(
    private val database: YanlisDefteriDatabase,
    private val depo: RozetDeposu,
    private val hedefDeposu: GunlukHedefDeposu,
    private val saat: Clock = Clock.systemDefaultZone(),
) {
    suspend fun girdi(
        sonuclar: List<OturumSonucu> = emptyList(),
        hedef: Int = hedefDeposu.hedef(),
        bugun: LocalDate = LocalDate.now(saat),
    ): RozetGirdisi = database.withTransaction {
        val gun = bugun.toEpochDay().toInt()
        val dao = database.ilerlemeDao()
        val tekrar = database.tekrarDao()
        val gunler = tekrar.gunDengeleri(gun - 4, gun).associateBy { it.gun }
        RozetGirdisi(
            oturum = KarneHesabi.hesapla(sonuclar),
            // Lifetime milestones remain attainable for existing users even after a break.
            seri = XpHesabi.enUzunSeri(tekrar.gunler()),
            toplamBagimsiz = dao.bagimsizTekrarSayisi(),
            sonBesGun = (gun - 4..gun).map {
                GunDenge(it, gunler[it]?.toplam?.toInt() ?: 0, gunler[it]?.bagimsiz?.toInt() ?: 0)
            },
            soruSayisi = dao.soruSayisi(),
            tekrarSayisi = dao.tekrarSayisi(),
            hedefGunSayisi = dao.hedefTamamlananGunSayisi(hedef),
            birGundeDersSayisi = dao.enCokGunDersSayisi(),
            toplamDersSayisi = dao.calisilanDersSayisi(),
            donusumVar = dao.donusumVar(),
            bagimsizZincir = RozetKurallari.enUzunBagimsizZincir(sonuclar),
        )
    }

    suspend fun degerlendir(sonuclar: List<OturumSonucu> = emptyList()): List<Rozet> {
        val girdi = girdi(sonuclar)
        return withContext(Dispatchers.IO) {
            depo.kaydet(RozetKurallari.kazanilanlar(girdi, depo.sahipOlanlar()))
        }
    }

    /** Collect once in the app shell, after restore recovery. No awards while a backup is partial. */
    fun degisiklikleriIzle(): Flow<List<Rozet>> = merge(
        database.ilerlemeDao().soruSayisiIzle(),
        database.ilerlemeDao().tekrarSayisiIzle(),
        hedefDeposu.izle(),
    ).conflate().map { degerlendir() }
}

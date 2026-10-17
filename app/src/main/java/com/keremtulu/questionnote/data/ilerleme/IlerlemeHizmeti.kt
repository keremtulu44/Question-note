package com.keremtulu.questionnote.data.ilerleme

import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.domain.ilerleme.GunDilimi
import com.keremtulu.questionnote.domain.ilerleme.HaftaDengesi
import com.keremtulu.questionnote.domain.ilerleme.IlerlemeOzeti
import com.keremtulu.questionnote.domain.karne.HaftaPenceresi
import com.keremtulu.questionnote.domain.karne.SeriHesabi
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Read-only progress figures derived from `tekrarlar`. No extra tables: every number is recomputed,
 * so it cannot drift out of sync with the review history.
 */
class IlerlemeHizmeti(
    private val database: YanlisDefteriDatabase,
    private val saat: Clock = Clock.systemDefaultZone(),
) {
    private val tekrarDao = database.tekrarDao()
    private val soruDao = database.soruDao()

    fun bugunSayisiIzle(gun: Int): Flow<Int> = tekrarDao.gunSayisiIzle(gun)

    fun seriIzle(bugun: Int): Flow<Int> = tekrarDao.gunlerIzle().map { SeriHesabi.seri(it, bugun) }

    suspend fun ozet(bugun: LocalDate = LocalDate.now(saat)): IlerlemeOzeti {
        val bugunGun = bugun.toEpochDay().toInt()
        val buHafta = HaftaPenceresi.buHafta(bugun)
        val gecenHafta = HaftaPenceresi.gecenHafta(bugun)
        val sonGunler = HaftaPenceresi.sonGunler(bugun, SON_GUN_SAYISI)

        val gunDengeleri = tekrarDao
            .gunDengeleri(sonGunler.first(), bugunGun)
            .associateBy { it.gun }
        val buHaftaDengeleri = tekrarDao.gunDengeleri(buHafta.ilkGun, buHafta.sonGun)
        val gecenHaftaDengeleri = tekrarDao.gunDengeleri(gecenHafta.ilkGun, gecenHafta.sonGun)

        return IlerlemeOzeti(
            seri = SeriHesabi.seri(tekrarDao.gunler(), bugunGun),
            bugunTekrar = gunDengeleri[bugunGun]?.toplam?.toInt() ?: 0,
            toplamBagimsiz = tekrarDao.toplamBagimsiz(),
            sonYediGun = sonGunler.map { gun ->
                val denge = gunDengeleri[gun]
                GunDilimi(
                    gun = gun,
                    toplam = denge?.toplam?.toInt() ?: 0,
                    bagimsiz = denge?.bagimsiz?.toInt() ?: 0,
                )
            },
            hafta = HaftaDengesi(
                buHaftaToplam = buHaftaDengeleri.sumOf { it.toplam }.toInt(),
                buHaftaBagimsiz = buHaftaDengeleri.sumOf { it.bagimsiz }.toInt(),
                gecenHaftaToplam = gecenHaftaDengeleri.sumOf { it.toplam }.toInt(),
                gecenHaftaBagimsiz = gecenHaftaDengeleri.sumOf { it.bagimsiz }.toInt(),
            ),
            yarinPlanli = soruDao.planliSoruSayisi(bugunGun + 1),
        )
    }

    private companion object {
        const val SON_GUN_SAYISI = 7
    }
}

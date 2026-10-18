package com.keremtulu.questionnote.data.seviye

import com.keremtulu.questionnote.data.gelisim.ProfilHizmeti
import com.keremtulu.questionnote.domain.gelisim.SureKapsami
import androidx.room.withTransaction
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.prefs.GunlukHedefDeposu
import com.keremtulu.questionnote.data.prefs.RozetDeposu
import com.keremtulu.questionnote.domain.seviye.SeviyeDurumu
import com.keremtulu.questionnote.domain.seviye.SeviyeHesabi
import com.keremtulu.questionnote.domain.seviye.XpKirilimi
import com.keremtulu.questionnote.domain.seviye.XpHesabi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import com.keremtulu.questionnote.data.ilerleme.RozetHizmeti
import com.keremtulu.questionnote.domain.karne.RozetGirdisi
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import com.keremtulu.questionnote.domain.calisma.YerelTakvimSaati
import java.time.Clock
import java.time.LocalDate

/**
 * Rebuilds the whole progression from data the app already keeps (`sorular`, `tekrarlar`,
 * owned badges). Nothing is stored twice, so history can never drift from the XP shown.
 */
class SeviyeHizmeti(
    private val database: YanlisDefteriDatabase,
    private val rozetDeposu: RozetDeposu,
    private val gunlukHedefDeposu: GunlukHedefDeposu,
    private val rozetHizmeti: RozetHizmeti,
    private val saat: Clock = YerelTakvimSaati,
) {
    private val ilerlemeDao = database.ilerlemeDao()
    private val tekrarDao = database.tekrarDao()
    private val profilHizmeti = ProfilHizmeti(database)

    suspend fun ozet(bugun: LocalDate = LocalDate.now(saat)): SeviyeOzeti = database.withTransaction {
        val sahip = rozetDeposu.sahipOlanlar()
        val hedef = gunlukHedefDeposu.hedef()
        val soruSayisi = ilerlemeDao.soruSayisi()
        val tekrarSayisi = ilerlemeDao.tekrarSayisi()
        val bagimsizSayisi = ilerlemeDao.bagimsizTekrarSayisi()
        val gunler = tekrarDao.gunler()
        val kirilim = XpHesabi.kirilim(
            soruSayisi = soruSayisi,
            tekrarSayisi = tekrarSayisi,
            bagimsizTekrarSayisi = bagimsizSayisi,
            hedefTamamGunSayisi = ilerlemeDao.hedefTamamlananGunSayisi(hedef),
            seriBonusu = XpHesabi.seriBonusu(gunler),
            rozetSayisi = sahip.size,
        )
        SeviyeOzeti(
            durum = SeviyeHesabi.durum(kirilim.toplam),
            kirilim = kirilim,
            soruSayisi = soruSayisi,
            tekrarSayisi = tekrarSayisi,
            bagimsizSayisi = bagimsizSayisi,
            enUzunSeri = XpHesabi.enUzunSeri(gunler),
            hedef = hedef,
            dersler = dersDagilimi(),
            tahmin = tahmin(bugun),
            rozetGirdisi = rozetHizmeti.girdi(hedef = hedef, bugun = bugun),
            sahipRozetler = sahip,
            sureKapsami = profilHizmeti.kapsam(bugun.toEpochDay().toInt()),
        )
    }

    private suspend fun dersDagilimi(): List<DersIlerlemesi> =
        ilerlemeDao.dersDagilimi()
            .groupBy { it.dersId }
            .map { (dersId, satirlar) ->
                val toplam = satirlar.sumOf { it.adet }.toInt()
                val durumlar = satirlar.associate { it.durum to it.adet.toInt() }
                DersIlerlemesi(
                    dersId = dersId,
                    dersAdi = satirlar.first().dersAd,
                    toplam = toplam,
                    saglam = durumlar["SAGLAM"] ?: 0,
                    pekisiyor = durumlar["PEKISIYOR"] ?: 0,
                    ogreniyor = durumlar["OGRENIYOR"] ?: 0,
                    yeni = durumlar["YENI"] ?: 0,
                )
            }
            .sortedByDescending { it.toplam }

    private suspend fun tahmin(bugun: LocalDate): List<GunTahmini> {
        val ilk = bugun.toEpochDay().toInt()
        val son = bugun.plusDays(6).toEpochDay().toInt()
        val yuk = ilerlemeDao.planYuku(ilk, son).associate { it.gun to it.adet.toInt() }
        return (0..6).map { fark ->
            val gun = ilk + fark
            GunTahmini(gun = gun, adet = yuk[gun] ?: 0)
        }
    }

    /**
     * Full summary that refreshes whenever a question or a review is added. Queries are cheap
     * (single-table aggregates) and only run on real changes, so the header and the progress screen
     * always agree with each other.
     */
    fun ozetIzle(): Flow<SeviyeOzeti> =
        merge(ilerlemeDao.soruSayisiIzle(), ilerlemeDao.tekrarSayisiIzle(),
            rozetDeposu.sahipIzle.map { it.size }, gunlukHedefDeposu.izle())
            .onStart { emit(0) }
            .conflate()
            .map { ozet() }
}

data class SeviyeOzeti(
    val durum: SeviyeDurumu,
    val kirilim: XpKirilimi,
    val soruSayisi: Int,
    val tekrarSayisi: Int,
    val bagimsizSayisi: Int,
    val enUzunSeri: Int,
    val hedef: Int,
    val dersler: List<DersIlerlemesi>,
    val tahmin: List<GunTahmini>,
    val rozetGirdisi: RozetGirdisi,
    val sahipRozetler: Set<Rozet> = emptySet(),
    val sureKapsami: SureKapsami? = null,
)

data class DersIlerlemesi(
    val dersId: String,
    val dersAdi: String,
    val toplam: Int,
    val saglam: Int,
    val pekisiyor: Int,
    val ogreniyor: Int,
    val yeni: Int,
) {
    /** SAGLAM counts fully, PEKISIYOR counts three quarters. */
    val pekismeOrani: Int
        get() = if (toplam == 0) 0 else ((saglam * 4 + pekisiyor * 3) * 25 / toplam).coerceIn(0, 100)
}

data class GunTahmini(val gun: Int, val adet: Int)

package com.keremtulu.questionnote

import android.content.Context
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabaseProvider
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.images.GorselTutarliligiHizmeti
import com.keremtulu.questionnote.data.export.YerelYedeklemeHizmeti
import com.keremtulu.questionnote.data.ilerleme.IlerlemeHizmeti
import com.keremtulu.questionnote.data.ilerleme.RozetHizmeti
import com.keremtulu.questionnote.data.prefs.SinavProfiliDeposu
import com.keremtulu.questionnote.data.prefs.OdakOturumuDeposu
import com.keremtulu.questionnote.data.prefs.GunlukHedefDeposu
import com.keremtulu.questionnote.data.prefs.RozetDeposu
import com.keremtulu.questionnote.data.prefs.TercihGunlukHedefDeposu
import com.keremtulu.questionnote.data.seviye.SeviyeHizmeti
import com.keremtulu.questionnote.data.repository.RoomDersRepository
import com.keremtulu.questionnote.data.repository.RoomKonuRepository
import com.keremtulu.questionnote.data.repository.RoomSoruRepository
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.KonuRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository

class AppContainer(context: Context) {
    private val database = YanlisDefteriDatabaseProvider.getInstance(context)

    val gorselDeposu = LocalSoruGorselDeposu(context)
    val profilDeposu = SinavProfiliDeposu(context)
    val oturumDeposu = OdakOturumuDeposu(context)
    val dersRepository: DersRepository = RoomDersRepository(database, profilDeposu)
    val konuRepository: KonuRepository = RoomKonuRepository(database)
    val soruRepository: SoruRepository = RoomSoruRepository(database, gorselDeposu)
    val gorselTutarliligi = GorselTutarliligiHizmeti(database, gorselDeposu)
    val disaAktarmaHizmeti = YerelYedeklemeHizmeti(context, database, gorselDeposu)
    val ilerlemeHizmeti = IlerlemeHizmeti(database)
    val rozetDeposu = RozetDeposu(context)
    val gunlukHedefDeposu: GunlukHedefDeposu = TercihGunlukHedefDeposu(context)
    val tercihHedefDeposu = gunlukHedefDeposu as TercihGunlukHedefDeposu
    val rozetHizmeti = RozetHizmeti(database, rozetDeposu, gunlukHedefDeposu)
    val seviyeHizmeti = SeviyeHizmeti(database, rozetDeposu, gunlukHedefDeposu, rozetHizmeti)
}

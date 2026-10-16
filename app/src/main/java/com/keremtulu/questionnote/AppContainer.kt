package com.keremtulu.questionnote

import android.content.Context
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabaseProvider
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.images.GorselTutarliligiHizmeti
import com.keremtulu.questionnote.data.export.YerelDisaAktarmaHizmeti
import com.keremtulu.questionnote.data.repository.RoomDersRepository
import com.keremtulu.questionnote.data.repository.RoomKonuRepository
import com.keremtulu.questionnote.data.repository.RoomSoruRepository
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.KonuRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository

class AppContainer(context: Context) {
    private val database = YanlisDefteriDatabaseProvider.getInstance(context)

    val gorselDeposu = LocalSoruGorselDeposu(context)
    val dersRepository: DersRepository = RoomDersRepository(database)
    val konuRepository: KonuRepository = RoomKonuRepository(database)
    val soruRepository: SoruRepository = RoomSoruRepository(database, gorselDeposu)
    val gorselTutarliligi = GorselTutarliligiHizmeti(database, gorselDeposu)
    val disaAktarmaHizmeti = YerelDisaAktarmaHizmeti(context, database, gorselDeposu)
}

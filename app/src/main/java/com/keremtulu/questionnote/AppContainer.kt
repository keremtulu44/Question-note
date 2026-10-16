package com.keremtulu.questionnote

import android.content.Context
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabaseProvider
import com.keremtulu.questionnote.data.repository.RoomDersRepository
import com.keremtulu.questionnote.domain.repository.DersRepository

class AppContainer(context: Context) {
    private val database = YanlisDefteriDatabaseProvider.getInstance(context)

    val dersRepository: DersRepository = RoomDersRepository(database.dersDao())
}

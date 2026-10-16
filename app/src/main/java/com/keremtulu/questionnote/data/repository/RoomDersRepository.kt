package com.keremtulu.questionnote.data.repository

import com.keremtulu.questionnote.data.local.dao.DersDao
import com.keremtulu.questionnote.data.local.dao.DersOzetEntity
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.repository.DersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDersRepository(
    private val dersDao: DersDao,
) : DersRepository {
    override fun aktifDersleriIzle(): Flow<List<Ders>> =
        dersDao.aktifDersleriIzle().map { dersler ->
            dersler.map { it.toDomain() }
        }

    private fun DersOzetEntity.toDomain() =
        Ders(
            id = ders.id,
            ad = ders.ad,
            sira = ders.sira,
            soruSayisi = soruSayisi,
        )
}

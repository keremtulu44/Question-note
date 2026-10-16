package com.keremtulu.questionnote.domain.repository

import com.keremtulu.questionnote.domain.model.Ders
import kotlinx.coroutines.flow.Flow

interface DersRepository {
    fun aktifDersleriIzle(): Flow<List<Ders>>
}

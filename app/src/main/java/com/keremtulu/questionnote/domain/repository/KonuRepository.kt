package com.keremtulu.questionnote.domain.repository

import com.keremtulu.questionnote.domain.model.Konu
import com.keremtulu.questionnote.domain.model.KonuIslemSonucu
import kotlinx.coroutines.flow.Flow

interface KonuRepository {
    fun konulariIzle(dersId: String, arsivli: Boolean): Flow<List<Konu>>

    fun tumunuIzle(): Flow<List<Konu>>

    suspend fun konuEkle(dersId: String, ad: String): KonuIslemSonucu

    suspend fun konuYenidenAdlandir(konuId: String, ad: String): KonuIslemSonucu

    suspend fun konuArsivle(konuId: String): KonuIslemSonucu

    suspend fun konuYenidenEtkinlestir(konuId: String): KonuIslemSonucu
}

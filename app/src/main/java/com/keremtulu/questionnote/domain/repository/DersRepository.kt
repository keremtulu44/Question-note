package com.keremtulu.questionnote.domain.repository

import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.DersIslemSonucu
import com.keremtulu.questionnote.domain.model.SinavProfili
import kotlinx.coroutines.flow.Flow

interface DersRepository {
    fun aktifDersleriIzle(): Flow<List<Ders>>

    fun dersleriIzle(arsivli: Boolean): Flow<List<Ders>>

    suspend fun sinavProfiliniUygula(profil: SinavProfili)

    suspend fun kataloguHazirla(adlar: List<String> = com.keremtulu.questionnote.domain.model.KatalogDersAdlari): List<Ders>

    suspend fun dersSeciminiKaydet(dersIdleri: Set<String>)

    suspend fun dersEkle(ad: String): DersIslemSonucu

    suspend fun dersYenidenAdlandir(dersId: String, ad: String): DersIslemSonucu

    suspend fun dersArsivDurumunuDegistir(dersId: String, arsivli: Boolean): DersIslemSonucu

    suspend fun dersSirasiniDegistir(dersId: String, yon: Int): DersIslemSonucu
}

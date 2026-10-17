package com.keremtulu.questionnote.domain.repository

import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi
import com.keremtulu.questionnote.domain.model.SoruSecenekTuru
import com.keremtulu.questionnote.domain.model.SoruSecenegi
import com.keremtulu.questionnote.domain.model.SoruSecenegiEklemeSonucu
import com.keremtulu.questionnote.domain.model.SoruSecenegiSilmeSonucu
import com.keremtulu.questionnote.domain.model.SoruIslemSonucu
import com.keremtulu.questionnote.domain.model.SoruKaydetmeIstegi
import com.keremtulu.questionnote.domain.model.SoruPlanDurumu
import com.keremtulu.questionnote.domain.model.TekrarKaydi
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import kotlinx.coroutines.flow.Flow

interface SoruRepository {
    fun sorulariIzle(dersId: String, konuId: String? = null, konusuz: Boolean = false): Flow<List<Soru>>

    fun filtreliCalismaSorulariniIzle(filtre: SoruCalismaFiltresi): Flow<List<Soru>>

    fun bugunTekrarlanacaklariIzle(bugun: Int): Flow<List<Soru>>

    fun tekrarGecmisiniIzle(soruId: String): Flow<List<TekrarKaydi>>

    suspend fun soruGetir(soruId: String): Soru?

    fun secenekleriIzle(tur: SoruSecenekTuru): Flow<List<SoruSecenegi>>

    suspend fun secenekEkle(tur: SoruSecenekTuru, ad: String): SoruSecenegiEklemeSonucu

    suspend fun secenekSil(secenekId: String): SoruSecenegiSilmeSonucu

    suspend fun soruKaydet(istek: SoruKaydetmeIstegi): SoruIslemSonucu

    suspend fun soruSil(soruId: String): SoruIslemSonucu

    suspend fun planDurumunuAyarla(soruId: String, durum: SoruPlanDurumu): SoruIslemSonucu

    suspend fun tekrarKaydet(
        soruId: String,
        sonuc: TekrarSonucu,
        cozumAcildi: Boolean,
    ): SoruIslemSonucu
}

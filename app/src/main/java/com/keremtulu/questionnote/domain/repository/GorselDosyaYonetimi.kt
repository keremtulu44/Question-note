package com.keremtulu.questionnote.domain.repository

import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi

interface GorselDosyaYonetimi {
    suspend fun geciciDosyayiDogrula(gorsel: SoruGorselGirdisi): Boolean

    suspend fun kaliciyaKopyala(geciciDosyaAdi: String): String?

    suspend fun geciciDosyayiSil(geciciDosyaAdi: String)

    suspend fun kaliciDosyayiSil(dosyaAdi: String): Boolean

    suspend fun kaliciDosyayiDogrula(gorsel: SoruGorselGirdisi): Boolean
}

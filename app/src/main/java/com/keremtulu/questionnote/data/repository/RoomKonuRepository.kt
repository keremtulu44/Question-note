package com.keremtulu.questionnote.data.repository

import androidx.room.withTransaction
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.KonuEntity
import com.keremtulu.questionnote.domain.model.Konu
import com.keremtulu.questionnote.domain.model.KonuIslemSonucu
import com.keremtulu.questionnote.domain.repository.KonuRepository
import java.text.Normalizer
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomKonuRepository(
    private val database: YanlisDefteriDatabase,
) : KonuRepository {
    private val konuDao = database.konuDao()

    override fun konulariIzle(dersId: String, arsivli: Boolean): Flow<List<Konu>> =
        konuDao.konulariIzle(dersId, arsivli).map { konular ->
            konular.map { it.toDomain() }
        }

    override fun tumunuIzle(): Flow<List<Konu>> =
        konuDao.tumunuIzle().map { konular -> konular.map { it.toDomain() } }

    override suspend fun konuEkle(dersId: String, ad: String): KonuIslemSonucu {
        val temizAd = ad.trim()
        if (temizAd.isEmpty()) return KonuIslemSonucu.BosAd

        return database.withTransaction {
            val konular = konuDao.dersinKonulariniGetir(dersId)
            if (konular.any { it.ad.anahtar() == temizAd.anahtar() }) {
                return@withTransaction KonuIslemSonucu.AyniAdVar
            }

            val now = System.currentTimeMillis()
            val sira = (konular.maxOfOrNull { it.sira } ?: -1) + 1
            konuDao.ekle(
                KonuEntity(
                    id = UUID.randomUUID().toString(),
                    dersId = dersId,
                    ad = temizAd,
                    sira = sira,
                    arsivli = false,
                    olusturma = now,
                    guncelleme = now,
                ),
            )
            KonuIslemSonucu.Basarili
        }
    }

    override suspend fun konuYenidenAdlandir(konuId: String, ad: String): KonuIslemSonucu {
        val temizAd = ad.trim()
        if (temizAd.isEmpty()) return KonuIslemSonucu.BosAd

        return database.withTransaction {
            val konu = konuDao.idIleGetir(konuId) ?: return@withTransaction KonuIslemSonucu.Bulunamadi
            val ayniAdKullananVar = konuDao.dersinKonulariniGetir(konu.dersId).any {
                it.id != konu.id && it.ad.anahtar() == temizAd.anahtar()
            }
            if (ayniAdKullananVar) return@withTransaction KonuIslemSonucu.AyniAdVar

            val guncelKonu = konu.copy(ad = temizAd, guncelleme = System.currentTimeMillis())
            if (konuDao.guncelle(guncelKonu) == 1) {
                KonuIslemSonucu.Basarili
            } else {
                KonuIslemSonucu.Bulunamadi
            }
        }
    }

    override suspend fun konuArsivle(konuId: String): KonuIslemSonucu =
        arsivDurumunuAyarla(konuId, arsivli = true)

    override suspend fun konuYenidenEtkinlestir(konuId: String): KonuIslemSonucu =
        arsivDurumunuAyarla(konuId, arsivli = false)

    private suspend fun arsivDurumunuAyarla(
        konuId: String,
        arsivli: Boolean,
    ): KonuIslemSonucu = database.withTransaction {
        val konu = konuDao.idIleGetir(konuId) ?: return@withTransaction KonuIslemSonucu.Bulunamadi
        if (konu.arsivli == arsivli) return@withTransaction KonuIslemSonucu.Basarili

        val guncelKonu = konu.copy(arsivli = arsivli, guncelleme = System.currentTimeMillis())
        if (konuDao.guncelle(guncelKonu) == 1) {
            KonuIslemSonucu.Basarili
        } else {
            KonuIslemSonucu.Bulunamadi
        }
    }

    private fun KonuEntity.toDomain() =
        Konu(
            id = id,
            dersId = dersId,
            ad = ad,
            sira = sira,
            arsivli = arsivli,
        )

    private fun String.anahtar(): String =
        Normalizer.normalize(trim(), Normalizer.Form.NFC)
            .lowercase(TURKISH_LOCALE)

    private companion object {
        val TURKISH_LOCALE: Locale = Locale.forLanguageTag("tr-TR")
    }
}

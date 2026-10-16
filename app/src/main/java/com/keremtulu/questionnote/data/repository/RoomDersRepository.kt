package com.keremtulu.questionnote.data.repository

import androidx.room.withTransaction
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.dao.DersDao
import com.keremtulu.questionnote.data.local.dao.DersOzetEntity
import com.keremtulu.questionnote.data.local.entity.DersEntity
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.DersIslemSonucu
import com.keremtulu.questionnote.domain.repository.DersRepository
import java.text.Normalizer
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDersRepository(
    private val database: YanlisDefteriDatabase,
) : DersRepository {
    private val dersDao: DersDao = database.dersDao()

    override fun aktifDersleriIzle(): Flow<List<Ders>> = dersleriIzle(arsivli = false)

    override fun dersleriIzle(arsivli: Boolean): Flow<List<Ders>> =
        dersDao.dersleriIzle(arsivli).map { dersler -> dersler.map { it.toDomain() } }

    override suspend fun dersEkle(ad: String): DersIslemSonucu {
        val temizAd = ad.trim()
        if (temizAd.isEmpty()) return DersIslemSonucu.BosAd

        return database.withTransaction {
            if (dersDao.tumunuGetir().any { it.ad.anahtar() == temizAd.anahtar() }) {
                return@withTransaction DersIslemSonucu.AyniAdVar
            }
            val now = System.currentTimeMillis()
            dersDao.ekle(
                DersEntity(
                    id = UUID.randomUUID().toString(),
                    ad = temizAd,
                    sira = (dersDao.tumunuGetir().maxOfOrNull { it.sira } ?: -1) + 1,
                    arsivli = false,
                    olusturma = now,
                    guncelleme = now,
                ),
            )
            DersIslemSonucu.Basarili
        }
    }

    override suspend fun dersYenidenAdlandir(dersId: String, ad: String): DersIslemSonucu {
        val temizAd = ad.trim()
        if (temizAd.isEmpty()) return DersIslemSonucu.BosAd

        return database.withTransaction {
            val ders = dersDao.idIleGetir(dersId) ?: return@withTransaction DersIslemSonucu.Bulunamadi
            if (dersDao.tumunuGetir().any { it.id != dersId && it.ad.anahtar() == temizAd.anahtar() }) {
                return@withTransaction DersIslemSonucu.AyniAdVar
            }
            if (dersDao.guncelle(ders.copy(ad = temizAd, guncelleme = System.currentTimeMillis())) == 1) {
                DersIslemSonucu.Basarili
            } else {
                DersIslemSonucu.Bulunamadi
            }
        }
    }

    override suspend fun dersArsivDurumunuDegistir(
        dersId: String,
        arsivli: Boolean,
    ): DersIslemSonucu = database.withTransaction {
        val ders = dersDao.idIleGetir(dersId) ?: return@withTransaction DersIslemSonucu.Bulunamadi
        if (ders.arsivli == arsivli) return@withTransaction DersIslemSonucu.Basarili
        if (dersDao.guncelle(ders.copy(arsivli = arsivli, guncelleme = System.currentTimeMillis())) == 1) {
            DersIslemSonucu.Basarili
        } else {
            DersIslemSonucu.Bulunamadi
        }
    }

    override suspend fun dersSirasiniDegistir(dersId: String, yon: Int): DersIslemSonucu =
        database.withTransaction {
            if (yon != -1 && yon != 1) return@withTransaction DersIslemSonucu.Bulunamadi
            val ders = dersDao.idIleGetir(dersId) ?: return@withTransaction DersIslemSonucu.Bulunamadi
            val kardesler = dersDao.tumunuGetir()
                .filter { it.arsivli == ders.arsivli }
                .sortedWith(compareBy<DersEntity> { it.sira }.thenBy { it.ad.anahtar() })
            val index = kardesler.indexOfFirst { it.id == dersId }
            val diger = kardesler.getOrNull(index + yon)
                ?: return@withTransaction DersIslemSonucu.Basarili
            val now = System.currentTimeMillis()
            dersDao.guncelle(
                listOf(
                    ders.copy(sira = diger.sira, guncelleme = now),
                    diger.copy(sira = ders.sira, guncelleme = now),
                ),
            )
            DersIslemSonucu.Basarili
        }

    private fun DersOzetEntity.toDomain() =
        Ders(
            id = ders.id,
            ad = ders.ad,
            sira = ders.sira,
            soruSayisi = soruSayisi,
        )

    private fun String.anahtar(): String =
        Normalizer.normalize(trim(), Normalizer.Form.NFC).lowercase(TURKISH_LOCALE)

    private companion object {
        val TURKISH_LOCALE: Locale = Locale.forLanguageTag("tr-TR")
    }
}

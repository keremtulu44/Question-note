package com.keremtulu.questionnote.data.repository

import androidx.room.withTransaction
import com.keremtulu.questionnote.data.prefs.SinavProfiliDeposu
import com.keremtulu.questionnote.domain.model.SinavProfili
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    private val profilDeposu: SinavProfiliDeposu? = null,
) : DersRepository {
    private val dersDao: DersDao = database.dersDao()

    override fun aktifDersleriIzle(): Flow<List<Ders>> {
        val tumu = dersleriIzle(arsivli = false)
        val depo = profilDeposu ?: return tumu
        return combine(tumu, depo.secimIzle) { dersler, secim -> dersler.filter(secim::gorunur) }
    }

    /** Reuse existing IDs, names, topics and questions. Switching profiles is non-destructive. */
    override suspend fun sinavProfiliniUygula(profil: SinavProfili) = withContext(Dispatchers.IO) {
        if (profil == SinavProfili.OZEL) {
            val mevcut = profilDeposu?.secimIzle?.value
            val seciliIdler = mevcut?.dersIdleri.orEmpty()
            val seciliAnahtarlar = mevcut?.ozelDersAdlari.orEmpty().map { it.anahtar() }.toSet()
            val (ids, adlar) = database.withTransaction {
                val etkinler = dersDao.tumunuGetir().filter { !it.arsivli }
                val eslesenler = etkinler.filter { ders ->
                    ders.id in seciliIdler || ders.ad.anahtar() in seciliAnahtarlar
                }.ifEmpty { etkinler }
                eslesenler.map { it.id }.toSet() to eslesenler.map { it.ad }.toSet()
            }
            profilDeposu?.kaydet(SinavProfili.OZEL, ids, adlar)
            return@withContext
        }
        val ids = database.withTransaction {
            val dersler = dersDao.tumunuGetir().toMutableList()
            profil.dersAdlari.map { ad ->
                val mevcut = dersler.firstOrNull { it.ad.anahtar() == ad.anahtar() }
                if (mevcut != null) {
                    mevcut.id
                } else {
                    val now = System.currentTimeMillis()
                    val yeni = DersEntity(
                        id = UUID.randomUUID().toString(), ad = ad,
                        sira = (dersler.maxOfOrNull { it.sira } ?: -1) + 1,
                        arsivli = false, olusturma = now, guncelleme = now,
                    )
                    dersDao.ekle(yeni)
                    dersler.add(yeni)
                    yeni.id
                }
            }.toSet()
        }
        profilDeposu?.kaydet(profil, ids)
        Unit
    }

    override suspend fun kataloguHazirla(adlar: List<String>): List<Ders> = withContext(Dispatchers.IO) {
        database.withTransaction {
            val dersler = dersDao.tumunuGetir().toMutableList()
            adlar.forEach { ad ->
                val mevcut = dersler.firstOrNull { it.ad.anahtar() == ad.anahtar() }
                if (mevcut == null) {
                    val now = System.currentTimeMillis()
                    val yeni = DersEntity(
                        id = UUID.randomUUID().toString(),
                        ad = ad,
                        sira = (dersler.maxOfOrNull { it.sira } ?: -1) + 1,
                        arsivli = false,
                        olusturma = now,
                        guncelleme = now,
                    )
                    dersDao.ekle(yeni)
                    dersler.add(yeni)
                }
            }
            dersler.filter { !it.arsivli }.sortedBy { it.sira }.map { entity ->
                Ders(id = entity.id, ad = entity.ad, sira = entity.sira, soruSayisi = 0, arsivli = entity.arsivli)
            }
        }
    }

    override suspend fun dersSeciminiKaydet(dersIdleri: Set<String>) = withContext(Dispatchers.IO) {
        val adlar = database.withTransaction {
            dersDao.tumunuGetir()
                .filter { it.id in dersIdleri }
                .map { it.ad }
                .toSet()
        }
        profilDeposu?.kaydet(SinavProfili.OZEL, dersIdleri, adlar)
        Unit
    }

    override fun dersleriIzle(arsivli: Boolean): Flow<List<Ders>> =
        dersDao.dersleriIzle(arsivli).map { dersler -> dersler.map { it.toDomain() } }

    override suspend fun dersEkle(ad: String): DersIslemSonucu {
        val temizAd = ad.trim()
        if (temizAd.isEmpty()) return DersIslemSonucu.BosAd

        val yeniId = UUID.randomUUID().toString()
        val sonuc = database.withTransaction {
            if (dersDao.tumunuGetir().any { it.ad.anahtar() == temizAd.anahtar() }) {
                return@withTransaction DersIslemSonucu.AyniAdVar
            }
            val now = System.currentTimeMillis()
            dersDao.ekle(
                DersEntity(
                    id = yeniId,
                    ad = temizAd,
                    sira = (dersDao.tumunuGetir().maxOfOrNull { it.sira } ?: -1) + 1,
                    arsivli = false,
                    olusturma = now,
                    guncelleme = now,
                ),
            )
            DersIslemSonucu.Basarili
        }
        if (sonuc == DersIslemSonucu.Basarili) {
            val mevcut = profilDeposu?.secimIzle?.value
            if (mevcut != null && mevcut.profil == SinavProfili.OZEL && mevcut.dersIdleri.isNotEmpty()) {
                profilDeposu.kaydet(
                    SinavProfili.OZEL,
                    mevcut.dersIdleri + yeniId,
                    mevcut.ozelDersAdlari + temizAd,
                )
            }
        }
        return sonuc
    }

    override suspend fun dersYenidenAdlandir(dersId: String, ad: String): DersIslemSonucu {
        val temizAd = ad.trim()
        if (temizAd.isEmpty()) return DersIslemSonucu.BosAd

        val sonuc = database.withTransaction {
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
        if (sonuc == DersIslemSonucu.Basarili) {
            val mevcut = profilDeposu?.secimIzle?.value
            if (mevcut != null && mevcut.profil == SinavProfili.OZEL && dersId in mevcut.dersIdleri) {
                val guncelAdlar = database.withTransaction {
                    dersDao.tumunuGetir()
                        .filter { it.id in mevcut.dersIdleri }
                        .map { it.ad }
                        .toSet()
                }
                profilDeposu.kaydet(SinavProfili.OZEL, mevcut.dersIdleri, guncelAdlar)
            }
        }
        return sonuc
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
            arsivli = ders.arsivli,
        )

    private fun String.anahtar(): String =
        Normalizer.normalize(trim(), Normalizer.Form.NFC).lowercase(TURKISH_LOCALE)

    private companion object {
        val TURKISH_LOCALE: Locale = Locale.forLanguageTag("tr-TR")
    }
}

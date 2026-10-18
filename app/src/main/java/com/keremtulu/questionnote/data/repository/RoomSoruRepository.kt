package com.keremtulu.questionnote.data.repository

import com.keremtulu.questionnote.data.gelisim.OlcumSerilestirme
import com.keremtulu.questionnote.data.gelisim.ProfilHizmeti
import com.keremtulu.questionnote.data.local.entity.TekrarOlcumEntity
import com.keremtulu.questionnote.domain.calisma.TekrarDenemesi
import com.keremtulu.questionnote.domain.calisma.TekrarOnizlemesi
import com.keremtulu.questionnote.domain.calisma.SureOlcumu
import com.keremtulu.questionnote.domain.calisma.OlcumKalitesi
import com.keremtulu.questionnote.domain.calisma.OlcumNedeni
import com.keremtulu.questionnote.domain.gelisim.ProfilBaglami
import com.keremtulu.questionnote.domain.gelisim.kaynakAnahtari
import org.json.JSONObject
import org.json.JSONArray
import java.security.MessageDigest
import androidx.room.withTransaction
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.GorselEntity
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.local.entity.SoruSecenegiEntity
import com.keremtulu.questionnote.data.local.entity.TekrarEntity
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.OgrenmeDurumu
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import com.keremtulu.questionnote.domain.model.SoruGorseli
import com.keremtulu.questionnote.domain.model.SoruIslemSonucu
import com.keremtulu.questionnote.domain.model.SoruKaydetmeIstegi
import com.keremtulu.questionnote.domain.model.SoruPlanDurumu
import com.keremtulu.questionnote.domain.model.SoruSecenekTuru
import com.keremtulu.questionnote.domain.model.SoruSecenegi
import com.keremtulu.questionnote.domain.model.SoruSecenegiEklemeSonucu
import com.keremtulu.questionnote.domain.model.SoruSecenegiSilmeSonucu
import com.keremtulu.questionnote.domain.model.SORU_SECENEGI_ADI_UST_SINIR
import com.keremtulu.questionnote.domain.model.canonicalSoruSecenegiAdi
import com.keremtulu.questionnote.domain.model.normalizeSoruSecenegiAdi
import com.keremtulu.questionnote.domain.model.TekrarKaydi
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import com.keremtulu.questionnote.domain.repeat.TekrarKurali
import com.keremtulu.questionnote.domain.repeat.AkilliTekrarKurali
import com.keremtulu.questionnote.domain.repository.GorselDosyaYonetimi
import com.keremtulu.questionnote.domain.repository.SoruRepository
import com.keremtulu.questionnote.domain.calisma.YerelTakvimSaati
import java.time.Clock
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSoruRepository(
    private val database: YanlisDefteriDatabase,
    private val gorselDosyalari: GorselDosyaYonetimi,
    private val saat: Clock = YerelTakvimSaati,
) : SoruRepository {
    private val dersDao = database.dersDao()
    private val konuDao = database.konuDao()
    private val soruDao = database.soruDao()
    private val soruSecenegiDao = database.soruSecenegiDao()
    private val gorselDao = database.gorselDao()
    private val tekrarDao = database.tekrarDao()
    private val olcumDao = database.tekrarOlcumDao()
    private val profilHizmeti = ProfilHizmeti(database)

    override fun sorulariIzle(
        dersId: String,
        konuId: String?,
        konusuz: Boolean,
    ): Flow<List<Soru>> = soruDao.sorulariIzle(dersId, konuId, konusuz)
        .map { rows -> rows.map { row -> row.soru.toDomain(row.gorseller.map { it.toDomain() }) } }

    override fun filtreliCalismaSorulariniIzle(filtre: SoruCalismaFiltresi): Flow<List<Soru>> =
        soruDao.filtreliCalismaSorulariniIzle(
            bugun = LocalDate.now(saat).toEpochDay().toInt(),
            konuId = filtre.konuId,
            konusuz = filtre.konusuz,
            kaynak = filtre.kaynak,
            kaynakBelirtilmemis = filtre.kaynakBelirtilmemis,
            yanlisNedeni = filtre.yanlisNedeni,
            yanlisNedeniBelirtilmemis = filtre.yanlisNedeniBelirtilmemis,
        ).map { rows -> rows.map { row -> row.soru.toDomain(row.gorseller.map { it.toDomain() }) } }

    override fun bugunTekrarlanacaklariIzle(bugun: Int): Flow<List<Soru>> =
        soruDao.bugunTekrarlanacaklariIzle(bugun)
            .map { rows -> rows.map { row -> row.soru.toDomain(row.gorseller.map { it.toDomain() }) } }

    override fun tekrarGecmisiniIzle(soruId: String): Flow<List<TekrarKaydi>> =
        olcumDao.gecmisiIzle(soruId).map { records -> records.map { it.tekrar.toDomain(it.olcum) } }

    override suspend fun soruGetir(soruId: String): Soru? {
        val row = soruDao.idIleGetir(soruId) ?: return null
        return row.toDomain(gorselDao.sorununGorselleriniGetir(soruId).map { it.toDomain() })
    }

    override fun secenekleriIzle(tur: SoruSecenekTuru): Flow<List<SoruSecenegi>> =
        soruSecenegiDao.secenekleriIzle(tur.name).map { rows -> rows.map { it.toDomain() } }

    override suspend fun secenekEkle(tur: SoruSecenekTuru, ad: String): SoruSecenegiEklemeSonucu {
        val cleanName = canonicalSoruSecenegiAdi(ad)
        if (cleanName.isBlank()) return SoruSecenegiEklemeSonucu.BosAd
        if (cleanName.length > SORU_SECENEGI_ADI_UST_SINIR) return SoruSecenegiEklemeSonucu.CokUzunAd
        val normalizedName = normalizeSoruSecenegiAdi(cleanName)
        return database.withTransaction {
            val existing = soruSecenegiDao.adaGoreGetir(tur.name, normalizedName)
            if (existing != null) {
                return@withTransaction SoruSecenegiEklemeSonucu.ZatenVar(existing.toDomain())
            }
            val row = SoruSecenegiEntity(
                id = UUID.randomUUID().toString(),
                tur = tur.name,
                ad = cleanName,
                normalizeAd = normalizedName,
                varsayilan = false,
                sira = 0,
                olusturma = saat.millis(),
            )
            soruSecenegiDao.ekle(row)
            SoruSecenegiEklemeSonucu.Basarili(row.toDomain())
        }
    }

    /**
     * Removes a catalog entry. Question columns store the *text*, so deletion is blocked while any
     * question still uses the value — otherwise the filter would lose the entry while the question
     * kept the value. Built-in entries can never be deleted.
     */
    override suspend fun secenekSil(secenekId: String): SoruSecenegiSilmeSonucu =
        database.withTransaction {
            val secenek = soruSecenegiDao.idIleGetir(secenekId)
                ?: return@withTransaction SoruSecenegiSilmeSonucu.Bulunamadi
            if (secenek.varsayilan) return@withTransaction SoruSecenegiSilmeSonucu.VarsayilanSilinemez
            val kullanim = when (SoruSecenekTuru.valueOf(secenek.tur)) {
                SoruSecenekTuru.KAYNAK -> soruDao.kaynakKullanimSayisi(secenek.ad)
                SoruSecenekTuru.YANLIS_NEDENI -> soruDao.nedenKullanimSayisi(secenek.ad)
            }
            if (kullanim > 0) return@withTransaction SoruSecenegiSilmeSonucu.Kullanimda(kullanim)
            if (soruSecenegiDao.sil(secenekId) == 1) {
                SoruSecenegiSilmeSonucu.Basarili
            } else {
                SoruSecenegiSilmeSonucu.Bulunamadi
            }
        }

    override suspend fun soruKaydet(istek: SoruKaydetmeIstegi): SoruIslemSonucu {
        if (!istek.gorseller.siraliVeGecerli()) return SoruIslemSonucu.GorselGecersiz
        if (istek.gorseller.none { it.tur == GorselTuru.SORU }) return SoruIslemSonucu.SoruGorseliGerekli

        val eskiSoru = istek.id?.let { soruDao.idIleGetir(it) }
        if (istek.id != null && eskiSoru == null) return SoruIslemSonucu.Bulunamadi
        val eskiGorseller = istek.id?.let { gorselDao.sorununGorselleriniGetir(it) }.orEmpty()
        val ilkDogrulama = veriIliskisiDogrula(istek, eskiSoru)
        if (ilkDogrulama != SoruIslemSonucu.Basarili) return ilkDogrulama

        val eskiGorselIdleri = eskiGorseller.associateBy { it.id }
        for (secim in istek.gorseller) {
            if (secim.gecici) {
                if (!gorselDosyalari.geciciDosyayiDogrula(secim)) return SoruIslemSonucu.GorselGecersiz
            } else {
                val eski = eskiGorselIdleri[secim.id] ?: return SoruIslemSonucu.GorselGecersiz
                if (
                    eski.dosyaAdi != secim.dosyaAdi ||
                    eski.tur != secim.tur.name ||
                    eski.sha256 != secim.sha256 ||
                    !gorselDosyalari.kaliciDosyayiDogrula(secim)
                ) return SoruIslemSonucu.GorselGecersiz
            }
        }
        if (istek.gorseller.any { secim -> secim.gecici && secim.id in eskiGorselIdleri }) {
            return SoruIslemSonucu.GorselGecersiz
        }

        val yeniKaliciDosyalar = mutableMapOf<String, String>()
        var veritabaninaKaydedildi = false
        try {
            for (gorsel in istek.gorseller.filter { it.gecici }) {
                val kaliciDosyaAdi = gorselDosyalari.kaliciyaKopyala(gorsel.dosyaAdi)
                    ?: return SoruIslemSonucu.GorselGecersiz
                yeniKaliciDosyalar[gorsel.id] = kaliciDosyaAdi
            }

            val now = saat.millis()
            val soruId = istek.id ?: UUID.randomUUID().toString()
            val bugun = LocalDate.now(saat)
            val kayitSonucu = database.withTransaction {
                val guncelEskiSoru = istek.id?.let { soruDao.idIleGetir(it) }
                if (istek.id != null && guncelEskiSoru == null) {
                    return@withTransaction SoruIslemSonucu.Bulunamadi
                }
                val iliskiSonucu = veriIliskisiDogrula(istek, guncelEskiSoru)
                if (iliskiSonucu != SoruIslemSonucu.Basarili) return@withTransaction iliskiSonucu

                val guncelSoru = SoruEntity(
                    id = soruId,
                    dersId = istek.dersId,
                    konuId = istek.konuId,
                    // The note editor was removed from the UI; preserve legacy notes on every edit.
                    soruNotu = guncelEskiSoru?.soruNotu ?: istek.soruNotu.temizMetin(),
                    cozumNotu = istek.cozumNotu.temizMetin(),
                    kaynak = istek.kaynak.temizMetin(),
                    kaynakKonumu = istek.kaynakKonumu.temizMetin(),
                    yanlisNedeni = istek.yanlisNedeni.temizMetin(),
                    favori = istek.favori,
                    planDurumu = istek.planDurumu.name,
                    ogrenmeDurumu = guncelEskiSoru?.ogrenmeDurumu ?: OgrenmeDurumu.YENI.name,
                    sonTekrarGun = guncelEskiSoru?.sonTekrarGun,
                    sonrakiTekrarGun = guncelEskiSoru?.sonrakiTekrarGun
                        ?: TekrarKurali.ilkTekrarGunu(bugun),
                    olusturma = guncelEskiSoru?.olusturma ?: now,
                    guncelleme = now,
                )
                if (guncelEskiSoru == null) {
                    soruDao.ekle(guncelSoru)
                } else if (soruDao.guncelle(guncelSoru) != 1) {
                    return@withTransaction SoruIslemSonucu.Bulunamadi
                }

                val kayitliGorseller = istek.gorseller.map { gorsel ->
                    GorselEntity(
                        id = gorsel.id,
                        soruId = soruId,
                        tur = gorsel.tur.name,
                        sira = gorsel.sira,
                        dosyaAdi = yeniKaliciDosyalar[gorsel.id] ?: gorsel.dosyaAdi,
                        mimeType = gorsel.mimeType,
                        genislik = gorsel.genislik,
                        yukseklik = gorsel.yukseklik,
                        boyut = gorsel.boyut,
                        sha256 = gorsel.sha256,
                        olusturma = now,
                    )
                }
                gorselDao.sorununGorselleriniSil(soruId)
                if (kayitliGorseller.isNotEmpty()) gorselDao.ekle(kayitliGorseller)
                SoruIslemSonucu.Basarili
            }

            if (kayitSonucu != SoruIslemSonucu.Basarili) return kayitSonucu
            veritabaninaKaydedildi = true
            val yeniDosyaAdlari = istek.gorseller.mapNotNull { gorsel ->
                yeniKaliciDosyalar[gorsel.id] ?: gorsel.takeUnless { it.gecici }?.dosyaAdi
            }.toSet()
            eskiGorseller.map { it.dosyaAdi }.filterNot { it in yeniDosyaAdlari }
                .forEach { runCatching { gorselDosyalari.kaliciDosyayiSil(it) } }
            istek.gorseller.filter { it.gecici }.forEach { runCatching { gorselDosyalari.geciciDosyayiSil(it.dosyaAdi) } }
            return SoruIslemSonucu.Basarili
        } finally {
            if (!veritabaninaKaydedildi) {
                yeniKaliciDosyalar.values.forEach { runCatching { gorselDosyalari.kaliciDosyayiSil(it) } }
            }
        }
    }

    override suspend fun soruSil(soruId: String): SoruIslemSonucu {
        val silinenGorseller = database.withTransaction {
            val soru = soruDao.idIleGetir(soruId) ?: return@withTransaction null
            val gorseller = gorselDao.sorununGorselleriniGetir(soruId)
            if (soruDao.sil(soru) == 1) gorseller else null
        } ?: return SoruIslemSonucu.Bulunamadi
        silinenGorseller.forEach { runCatching { gorselDosyalari.kaliciDosyayiSil(it.dosyaAdi) } }
        return SoruIslemSonucu.Basarili
    }

    override suspend fun planDurumunuAyarla(soruId: String, durum: SoruPlanDurumu): SoruIslemSonucu =
        database.withTransaction {
            val soru = soruDao.idIleGetir(soruId) ?: return@withTransaction SoruIslemSonucu.Bulunamadi
            if (soruDao.guncelle(soru.copy(planDurumu = durum.name, guncelleme = System.currentTimeMillis())) == 1) {
                SoruIslemSonucu.Basarili
            } else {
                SoruIslemSonucu.Bulunamadi
            }
        }

    override suspend fun tekrarOnizle(deneme: TekrarDenemesi): TekrarOnizlemesi? = database.withTransaction {
        val soru = soruDao.idIleGetir(deneme.soruId) ?: return@withTransaction null
        if (soru.planDurumu != SoruPlanDurumu.AKTIF.name) return@withTransaction null
        onizlemeUret(soru, deneme)
    }

    /** Compatibility path for unmeasured callers. Still writes a context row, never invents a duration. */
    override suspend fun tekrarKaydet(soruId: String, sonuc: TekrarSonucu, cozumAcildi: Boolean): SoruIslemSonucu =
        database.withTransaction {
            val soru = soruDao.idIleGetir(soruId) ?: return@withTransaction SoruIslemSonucu.Bulunamadi
            if (soru.planDurumu != SoruPlanDurumu.AKTIF.name) return@withTransaction SoruIslemSonucu.PlanDurumuKapali
            tekrarKaydet(onizlemeUret(soru, TekrarDenemesi(UUID.randomUUID().toString(), soruId, sonuc, cozumAcildi)))
        }

    override suspend fun tekrarKaydet(onizleme: TekrarOnizlemesi): SoruIslemSonucu = database.withTransaction {
        val deneme = onizleme.deneme
        val once = olcumDao.denemeIleGetir(deneme.denemeId)
        if (once != null) {
            val kayit = olcumDao.tekrarGetir(once.tekrarId)
            return@withTransaction if (kayit?.soruId == deneme.soruId && kayit.sonuc == deneme.sonuc.name &&
                kayit.cozumAcildi == deneme.cozumAcildi && once.oturumId == deneme.oturumId)
                SoruIslemSonucu.Basarili else SoruIslemSonucu.DenemeCakismasi
        }
        val soru = soruDao.idIleGetir(deneme.soruId) ?: return@withTransaction SoruIslemSonucu.Bulunamadi
        if (soru.planDurumu != SoruPlanDurumu.AKTIF.name) return@withTransaction SoruIslemSonucu.PlanDurumuKapali
        val simdi = onizlemeUret(soru, deneme)
        val gun = simdi.gun
        if (tekrarDao.ayniGunTekrarVar(soru.id, gun)) return@withTransaction SoruIslemSonucu.AyniGunTekrarVar
        if (simdi != onizleme) return@withTransaction SoruIslemSonucu.OnizlemeGuncellendi
        val plan = simdi.plan
        val kayitAni = saat.instant()
        if (saat.zone.id != simdi.saatDilimi || kayitAni.atZone(saat.zone).toLocalDate().toEpochDay().toInt() != simdi.gun) {
            return@withTransaction SoruIslemSonucu.OnizlemeGuncellendi
        }
        val now = kayitAni.toEpochMilli()
        val tekrar = TekrarEntity(UUID.randomUUID().toString(), soru.id, now, gun, soru.sonrakiTekrarGun,
            deneme.sonuc.name, deneme.cozumAcildi, plan.oncekiAralik, plan.sonrakiAralik,
            AkilliTekrarKurali.ALGORITMA_SURUMU, false)
        val olcum = simdi.deneme.olcum
        val kayit = TekrarOlcumEntity(tekrar.id, deneme.denemeId, deneme.oturumId,
            olcum.denemeAktifMs, olcum.incelemeAktifMs, olcum.kalite.name, OlcumSerilestirme.nedenlerYaz(olcum.nedenler), olcum.surum,
            soru.dersId, requireNotNull(dersDao.idIleGetir(soru.dersId)).ad,
            soru.konuId, soru.konuId?.let { konuDao.idIleGetir(it)?.ad },
            kaynakAnahtari(soru.kaynak), soru.kaynak?.takeIf { it.isNotBlank() }, simdi.kararOzeti)
        OlcumSerilestirme.dogrula(kayit, tekrar)
        // Exactly one transaction: answer + measurement/context + the unchanged v2 plan.
        tekrarDao.ekle(tekrar)
        olcumDao.ekle(kayit)
        check(soruDao.guncelle(soru.copy(ogrenmeDurumu = plan.ogrenmeDurumu.name,
            sonTekrarGun = gun, sonrakiTekrarGun = plan.sonrakiGun, guncelleme = now)) == 1)
        SoruIslemSonucu.Basarili
    }

    private suspend fun onizlemeUret(soru: SoruEntity, istek: TekrarDenemesi): TekrarOnizlemesi {
        require(istek.denemeId.isNotBlank() && istek.denemeId.length <= 128)
        val deneme = istek.guvenli()
        val bolge = saat.zone
        val bugun = saat.instant().atZone(bolge).toLocalDate()
        val gun = bugun.toEpochDay().toInt()
        val gecmis = tekrarDao.sorununTekrarlariniGetir(soru.id)
        val plan = AkilliTekrarKurali.hesapla(gecmis.map { it.toDomain() }, deneme.sonuc, deneme.cozumAcildi, bugun, soru.sonrakiTekrarGun)
        val profil = profilHizmeti.profil(ProfilBaglami(soru.dersId, soru.konuId, kaynakAnahtari(soru.kaynak)), gun, deneme.denemeId)
        val karar = OlcumSerilestirme.kararYaz(plan, gun, profil, bolge.id)
        val baglam = JSONObject().put("day", gun).put("question", JSONObject()
            .put("id", soru.id).put("updated", soru.guncelleme).put("lesson", soru.dersId)
            .put("lesson_label", dersDao.idIleGetir(soru.dersId)?.ad)
            .put("topic", soru.konuId ?: JSONObject.NULL).put("topic_label", soru.konuId?.let { konuDao.idIleGetir(it)?.ad } ?: JSONObject.NULL)
            .put("source", soru.kaynak ?: JSONObject.NULL).put("scheduled", soru.sonrakiTekrarGun ?: JSONObject.NULL))
            .put("history", JSONArray().apply { gecmis.sortedBy { it.id }.forEach { put(JSONObject()
                .put("id", it.id).put("day", it.gun).put("time", it.zaman).put("result", it.sonuc).put("solution", it.cozumAcildi)
                .put("interval", it.sonrakiAralik ?: JSONObject.NULL).put("algorithm", it.algoritmaSurumu)) } })
            .put("decision", JSONObject(karar)).toString()
        val imza = MessageDigest.getInstance("SHA-256").digest(baglam.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
        return TekrarOnizlemesi(deneme, gun, plan, imza, karar, bolge.id)
    }

    private suspend fun veriIliskisiDogrula(
        istek: SoruKaydetmeIstegi,
        eskiSoru: SoruEntity?,
    ): SoruIslemSonucu {
        val ders = dersDao.idIleGetir(istek.dersId) ?: return SoruIslemSonucu.GecersizDers
        if (eskiSoru == null || eskiSoru.dersId != istek.dersId) {
            if (ders.arsivli) return SoruIslemSonucu.GecersizDers
        }
        val konuId = istek.konuId ?: return SoruIslemSonucu.Basarili
        val konu = konuDao.idIleGetir(konuId) ?: return SoruIslemSonucu.GecersizKonu
        if (konu.dersId != istek.dersId) return SoruIslemSonucu.GecersizKonu
        if (eskiSoru?.konuId != konuId && konu.arsivli) return SoruIslemSonucu.GecersizKonu
        return SoruIslemSonucu.Basarili
    }

    private fun List<SoruGorselGirdisi>.siraliVeGecerli(): Boolean {
        if (map { it.id }.distinct().size != size) return false
        if (map { it.dosyaAdi }.distinct().size != size) return false
        if (any { it.dosyaAdi.isBlank() || it.genislik <= 0 || it.yukseklik <= 0 || it.boyut <= 0L || it.sha256.length != 64 }) {
            return false
        }
        return GorselTuru.entries.all { tur ->
            filter { it.tur == tur }.map { it.sira }.sorted() == filter { it.tur == tur }.indices.toList()
        }
    }

    private fun SoruEntity.toDomain(gorseller: List<SoruGorseli>) = Soru(
        id = id,
        dersId = dersId,
        konuId = konuId,
        soruNotu = soruNotu,
        cozumNotu = cozumNotu,
        kaynak = kaynak,
        kaynakKonumu = kaynakKonumu,
        yanlisNedeni = yanlisNedeni,
        favori = favori,
        planDurumu = enumOrDefault<SoruPlanDurumu>(planDurumu, SoruPlanDurumu.AKTIF),
        ogrenmeDurumu = enumOrDefault<OgrenmeDurumu>(ogrenmeDurumu, OgrenmeDurumu.YENI),
        sonTekrarGun = sonTekrarGun,
        sonrakiTekrarGun = sonrakiTekrarGun,
        olusturma = olusturma,
        guncelleme = guncelleme,
        gorseller = gorseller,
    )

    private fun SoruSecenegiEntity.toDomain() = SoruSecenegi(
        id = id,
        tur = SoruSecenekTuru.valueOf(tur),
        ad = ad,
        varsayilan = varsayilan,
        sira = sira,
    )

    private fun GorselEntity.toDomain() = SoruGorseli(
        id = id,
        soruId = soruId,
        tur = enumOrDefault(tur, GorselTuru.SORU),
        sira = sira,
        dosyaAdi = dosyaAdi,
        mimeType = mimeType,
        genislik = genislik,
        yukseklik = yukseklik,
        boyut = boyut,
        sha256 = sha256,
    )

    private fun TekrarEntity.toDomain(olcumKaydi: TekrarOlcumEntity? = null) = TekrarKaydi(
        id = id,
        gun = gun,
        planlananGun = planlananGun,
        sonuc = enumOrDefault(sonuc, TekrarSonucu.COZEMEDIM),
        cozumAcildi = cozumAcildi,
        oncekiAralik = oncekiAralik,
        sonrakiAralik = sonrakiAralik,
        elleAtandi = elleAtandi,
        algoritmaSurumu = algoritmaSurumu,
        zaman = zaman,
        denemeId = olcumKaydi?.denemeId,
        oturumId = olcumKaydi?.oturumId,
        olcum = olcumKaydi?.let(OlcumSerilestirme::olcum),
    )

    private inline fun <reified T : Enum<T>> enumOrDefault(value: String, default: T): T =
        enumValues<T>().firstOrNull { it.name == value } ?: default

    private fun String.temizMetin(): String? = trim().takeIf { it.isNotEmpty() }
}

package com.keremtulu.questionnote.data.gelisim

import androidx.room.Room
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.local.entity.TekrarEntity
import com.keremtulu.questionnote.data.repository.RoomKonuRepository
import com.keremtulu.questionnote.data.repository.RoomSoruRepository
import com.keremtulu.questionnote.domain.calisma.OlcumKalitesi
import com.keremtulu.questionnote.domain.calisma.SureOlcumu
import com.keremtulu.questionnote.domain.calisma.TekrarDenemesi
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import com.keremtulu.questionnote.domain.model.SoruIslemSonucu
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import com.keremtulu.questionnote.domain.repository.GorselDosyaYonetimi
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OlcumKaydiTest {
    private lateinit var db: YanlisDefteriDatabase
    private lateinit var repo: RoomSoruRepository
    private lateinit var dersId: String
    private val saat = DegisenSaat()
    private class DegisenSaat : Clock() {
        var an: Instant = Instant.parse("2026-10-18T10:00:00Z")
        var bolge: ZoneId = ZoneOffset.UTC
        override fun getZone(): ZoneId = bolge
        override fun withZone(zone: ZoneId): Clock = Clock.fixed(an, zone)
        var sonrakiAn: Instant? = null
        override fun instant(): Instant {
            val sonuc = an
            sonrakiAn?.let { an = it; sonrakiAn = null }
            return sonuc
        }
    }
    private val files = object : GorselDosyaYonetimi {
        override suspend fun geciciDosyayiDogrula(gorsel: SoruGorselGirdisi) = true
        override suspend fun kaliciyaKopyala(geciciDosyaAdi: String) = geciciDosyaAdi
        override suspend fun geciciDosyayiSil(geciciDosyaAdi: String) {}
        override suspend fun kaliciDosyayiSil(dosyaAdi: String) = true
        override suspend fun kaliciDosyayiDogrula(gorsel: SoruGorselGirdisi) = true
    }
    @Before fun ac() {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle()).allowMainThreadQueries().build()
        dersId = db.dersDao().tumunuGetir().first().id
        repo = RoomSoruRepository(db, files, saat)
    }
    @After fun kapat() { db.close() }
    private suspend fun soru(id: String = "q") { db.soruDao().ekle(SoruEntity(id, dersId, kaynak = " IŞIK  TEST ", sonrakiTekrarGun = 20_744, olusturma = 1, guncelleme = 1)) }
    private fun deneme(id: String = "a", q: String = "q", ms: Long = 50_000) = TekrarDenemesi(id, q, TekrarSonucu.RAHAT, false,
        SureOlcumu(ms, 0, OlcumKalitesi.TAM), "session")

    @Test fun previewAndAtomicRecordUseExactlyTheSameV2PlanAndMeasurement() = runBlocking {
        soru()
        val p = requireNotNull(repo.tekrarOnizle(deneme()))
        assertEquals(SoruIslemSonucu.Basarili, repo.tekrarKaydet(p))
        val tekrar = db.tekrarDao().tumunuGetir().single()
        val olcum = db.tekrarOlcumDao().tumunuGetir().single()
        assertEquals(p.plan.sonrakiGun, db.soruDao().idIleGetir("q")?.sonrakiTekrarGun)
        assertEquals(p.plan.sonrakiAralik, tekrar.sonrakiAralik)
        assertEquals(2, tekrar.algoritmaSurumu)
        assertEquals(50_000L, olcum.denemeAktifMs)
        assertEquals("ışık test", olcum.kaynakAnahtari)
        assertEquals(p.kararOzeti, olcum.kararJson)
        val history = repo.tekrarGecmisiniIzle("q").first().single()
        assertEquals("a", history.denemeId)
        assertEquals(deneme().olcum, history.olcum)
    }

    @Test fun retryAfterCommitIsIdempotentEvenOnAnotherDay() = runBlocking {
        soru(); val p = requireNotNull(repo.tekrarOnizle(deneme()))
        assertEquals(SoruIslemSonucu.Basarili, repo.tekrarKaydet(p))
        saat.an = saat.an.plusSeconds(86_400)
        assertEquals(SoruIslemSonucu.Basarili, repo.tekrarKaydet(p))
        assertEquals(1, db.tekrarDao().tumunuGetir().size)
        assertEquals(1, db.tekrarOlcumDao().tumunuGetir().size)
    }

    @Test fun reusedAttemptWithAnotherResultIsRejectedWithoutRewritingHistory() = runBlocking {
        soru(); val p = requireNotNull(repo.tekrarOnizle(deneme()))
        repo.tekrarKaydet(p)
        val farkli = requireNotNull(repo.tekrarOnizle(deneme().copy(sonuc = TekrarSonucu.COZEMEDIM)))
        assertEquals(SoruIslemSonucu.DenemeCakismasi, repo.tekrarKaydet(farkli))
        assertEquals("RAHAT", db.tekrarDao().tumunuGetir().single().sonuc)
    }

    @Test fun aNewAttemptOnTheSameDayCannotDuplicateXpOrMeasurements() = runBlocking {
        soru(); repo.tekrarKaydet(requireNotNull(repo.tekrarOnizle(deneme())))
        assertEquals(SoruIslemSonucu.AyniGunTekrarVar, repo.tekrarKaydet(requireNotNull(repo.tekrarOnizle(deneme("other")))))
        assertEquals(1, db.tekrarOlcumDao().tumunuGetir().size)
    }

    @Test fun midnightRequiresRefreshingThePreviewBeforeAnyWrite() = runBlocking {
        soru(); val p = requireNotNull(repo.tekrarOnizle(deneme()))
        saat.an = saat.an.plusSeconds(86_400)
        assertEquals(SoruIslemSonucu.OnizlemeGuncellendi, repo.tekrarKaydet(p))
        assertTrue(db.tekrarDao().tumunuGetir().isEmpty())
        val yeni = requireNotNull(repo.tekrarOnizle(deneme()))
        assertEquals(p.gun + 1, yeni.gun)
        assertEquals(SoruIslemSonucu.Basarili, repo.tekrarKaydet(yeni))
    }

    @Test fun changedQuestionContextOrProfileRequiresRefreshingInsteadOfSilentSave() = runBlocking {
        soru(); soru("q2")
        val p = requireNotNull(repo.tekrarOnizle(deneme()))
        repo.tekrarKaydet(requireNotNull(repo.tekrarOnizle(deneme("b", "q2"))))
        assertEquals(SoruIslemSonucu.OnizlemeGuncellendi, repo.tekrarKaydet(p))
        val yenilenen = requireNotNull(repo.tekrarOnizle(deneme()))
        val q = requireNotNull(db.soruDao().idIleGetir("q"))
        db.soruDao().guncelle(q.copy(kaynak = "Başka kitap"))
        assertEquals(SoruIslemSonucu.OnizlemeGuncellendi, repo.tekrarKaydet(yenilenen))
        assertEquals(1, db.tekrarDao().tumunuGetir().size)
    }

    @Test fun measurementInsertionFailureRollsBackReviewAndPlanThenSameAttemptCanRetry() = runBlocking {
        soru(); val onceki = db.soruDao().idIleGetir("q")
        val p = requireNotNull(repo.tekrarOnizle(deneme()))
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fail_measurement BEFORE INSERT ON tekrar_olcumleri BEGIN SELECT RAISE(ABORT, 'test'); END")
        var hata = false
        try { repo.tekrarKaydet(p) } catch (_: Exception) { hata = true }
        assertTrue(hata)
        assertTrue(db.tekrarDao().tumunuGetir().isEmpty())
        assertTrue(db.tekrarOlcumDao().tumunuGetir().isEmpty())
        assertEquals(onceki, db.soruDao().idIleGetir("q"))
        db.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_measurement")
        assertEquals(SoruIslemSonucu.Basarili, repo.tekrarKaydet(p))
    }

    @Test fun invalidDurationDoesNotBlockAValidSelfAssessment() = runBlocking {
        soru()
        val p = requireNotNull(repo.tekrarOnizle(deneme().copy(olcum = SureOlcumu(-1, Long.MAX_VALUE, OlcumKalitesi.TAM))))
        assertEquals(SoruIslemSonucu.Basarili, repo.tekrarKaydet(p))
        val o = db.tekrarOlcumDao().tumunuGetir().single()
        assertEquals("GECERSIZ", o.kalite)
        assertNull(o.denemeAktifMs)
        assertEquals("RAHAT", db.tekrarDao().tumunuGetir().single().sonuc)
    }

    @Test fun unmeasuredCompatibilityPathCreatesNoFakeHistoricalTime() = runBlocking {
        soru(); assertEquals(SoruIslemSonucu.Basarili, repo.tekrarKaydet("q", TekrarSonucu.RAHAT, false))
        val o = db.tekrarOlcumDao().tumunuGetir().single()
        assertEquals("YOK", o.kalite)
        assertNull(o.denemeAktifMs)
        assertNull(o.incelemeAktifMs)
    }

    @Test fun sourceAndTopicRenamesDoNotRelabelTheSavedContext() = runBlocking {
        soru()
        RoomKonuRepository(db).konuEkle(dersId, "Eski konu")
        val topic = db.konuDao().dersinKonulariniGetir(dersId).single()
        db.soruDao().guncelle(requireNotNull(db.soruDao().idIleGetir("q")).copy(konuId = topic.id))
        repo.tekrarKaydet(requireNotNull(repo.tekrarOnizle(deneme())))
        db.soruDao().guncelle(requireNotNull(db.soruDao().idIleGetir("q")).copy(konuId = null, kaynak = "Yeni kitap"))
        db.openHelper.writableDatabase.execSQL("DELETE FROM konular WHERE id = ?", arrayOf(topic.id))
        val o = db.tekrarOlcumDao().tumunuGetir().single()
        assertEquals("Eski konu", o.konuAdi)
        assertEquals(topic.id, o.konuId)
        assertEquals(" IŞIK  TEST ", o.kaynakEtiketi)
        OlcumSerilestirme.dogrula(o, db.tekrarDao().tumunuGetir().single())
        assertEquals(1, ProfilHizmeti(db).kapsam(LocalDate.now(saat).toEpochDay().toInt()).tamOlcum)
    }

    @Test fun deletingAQuestionCascadesMeasurementsAndRebuildsProfilesWithoutACache() = runBlocking {
        soru(); repo.tekrarKaydet(requireNotNull(repo.tekrarOnizle(deneme())))
        assertEquals(SoruIslemSonucu.Basarili, repo.soruSil("q"))
        assertTrue(db.tekrarOlcumDao().tumunuGetir().isEmpty())
        val kapsam = ProfilHizmeti(db).kapsam(LocalDate.now(saat).toEpochDay().toInt())
        assertEquals(0, kapsam.toplamTekrar)
        assertTrue(kapsam.dersler.isEmpty())
    }

    @Test fun oldRowsRemainAbsentMeasurementsNotZeroAndDoNotCreateHistoricalGroups() = runBlocking {
        soru()
        val gun = LocalDate.now(saat).toEpochDay().toInt()
        db.tekrarDao().ekle(TekrarEntity("legacy", "q", saat.millis(), gun, sonuc = "RAHAT"))
        assertNull(repo.tekrarGecmisiniIzle("q").first().single().olcum)
        val kapsam = ProfilHizmeti(db).kapsam(gun)
        assertEquals(1, kapsam.olcumsuz)
        assertTrue(kapsam.dersler.isEmpty())
    }

    @Test fun durationNeverChangesTheV2PlanOrIndependentDefinition() = runBlocking {
        soru()
        val kisa = requireNotNull(repo.tekrarOnizle(deneme(ms = 1_000)))
        val uzun = requireNotNull(repo.tekrarOnizle(deneme(ms = 80_000_000)))
        assertEquals(kisa.plan, uzun.plan)
        val destekli = requireNotNull(repo.tekrarOnizle(deneme().copy(cozumAcildi = true,
            olcum = SureOlcumu(5_000, 10_000, OlcumKalitesi.TAM))))
        assertEquals(1, destekli.plan.sonrakiAralik)
    }

    @Test fun savedProfileSnapshotExcludesTheCurrentAttemptWhileFutureProfilesCanIncludeIt() = runBlocking {
        val ilk = Instant.parse("2026-10-14T10:00:00Z")
        repeat(20) { i ->
            saat.an = ilk.plusSeconds((i / 4) * 86_400L)
            val q = "q${i % 10}"
            if (db.soruDao().idIleGetir(q) == null) soru(q)
            val p = requireNotNull(repo.tekrarOnizle(deneme("a$i", q)))
            if (i == 19) {
                val secim = org.json.JSONObject(p.kararOzeti).getJSONObject("profile").getJSONObject("independent")
                assertEquals(19, secim.getJSONObject("requested").getInt("measurements"))
                assertTrue(secim.isNull("reference"))
            }
            assertEquals(SoruIslemSonucu.Basarili, repo.tekrarKaydet(p))
        }
        val profil = ProfilHizmeti(db).kapsam(LocalDate.now(saat).toEpochDay().toInt()).dersler.single().profil
        assertEquals(20, profil.yardimsiz.istenen.olcumSayisi)
        assertNotNull(profil.yardimsiz.referans)
    }


    @Test fun timeZoneChangeRequiresARefreshEvenWhenTheCalendarDayStaysTheSame() = runBlocking {
        soru(); val p = requireNotNull(repo.tekrarOnizle(deneme()))
        saat.bolge = ZoneId.of("Europe/Istanbul")
        val yeni = requireNotNull(repo.tekrarOnizle(deneme()))
        assertEquals(p.gun, yeni.gun)
        assertNotEquals(p.saatDilimi, yeni.saatDilimi)
        assertEquals(SoruIslemSonucu.OnizlemeGuncellendi, repo.tekrarKaydet(p))
        assertTrue(db.tekrarDao().tumunuGetir().isEmpty())
        assertEquals(SoruIslemSonucu.Basarili, repo.tekrarKaydet(yeni))
    }


    @Test fun aClockJumpDuringSaveRequestsRefreshInsteadOfMixingTwoCalendarDays() = runBlocking {
        soru()
        val p = requireNotNull(repo.tekrarOnizle(deneme()))
        saat.sonrakiAn = saat.an
        saat.an = saat.an.minusSeconds(86_400)
        assertEquals(SoruIslemSonucu.OnizlemeGuncellendi, repo.tekrarKaydet(p))
        assertTrue(db.tekrarDao().tumunuGetir().isEmpty())
        val yeni = requireNotNull(repo.tekrarOnizle(deneme()))
        assertEquals(SoruIslemSonucu.Basarili, repo.tekrarKaydet(yeni))
        assertEquals(yeni.gun, db.tekrarDao().tumunuGetir().single().gun)
    }

}

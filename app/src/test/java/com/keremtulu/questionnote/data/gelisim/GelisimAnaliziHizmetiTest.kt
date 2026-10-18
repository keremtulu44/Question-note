package com.keremtulu.questionnote.data.gelisim

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.keremtulu.questionnote.data.local.*
import com.keremtulu.questionnote.data.local.entity.*
import com.keremtulu.questionnote.data.prefs.SinavProfiliDeposu
import com.keremtulu.questionnote.data.repository.RoomSoruRepository
import com.keremtulu.questionnote.domain.calisma.*
import com.keremtulu.questionnote.domain.gelisim.*
import com.keremtulu.questionnote.domain.model.*
import com.keremtulu.questionnote.domain.repository.GorselDosyaYonetimi
import java.time.*
import java.util.Collections
import java.util.concurrent.Executor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
class GelisimAnaliziHizmetiTest {
    private lateinit var db: YanlisDefteriDatabase
    private lateinit var repo: RoomSoruRepository
    private lateinit var profil: SinavProfiliDeposu
    private lateinit var math: String
    private lateinit var physics: String
    private val gun = LocalDate.of(2026, 10, 18).toEpochDay().toInt()
    private val saat = Clock.fixed(Instant.parse("2026-10-18T10:00:00Z"), ZoneOffset.UTC)
    private val queries = Collections.synchronizedList(mutableListOf<String>())
    private val files = object : GorselDosyaYonetimi {
        override suspend fun geciciDosyayiDogrula(gorsel: SoruGorselGirdisi) = true
        override suspend fun kaliciyaKopyala(geciciDosyaAdi: String) = geciciDosyaAdi
        override suspend fun geciciDosyayiSil(geciciDosyaAdi: String) {}
        override suspend fun kaliciDosyayiSil(dosyaAdi: String) = true
        override suspend fun kaliciDosyayiDogrula(gorsel: SoruGorselGirdisi) = true
    }
    @Before fun ac() {
        val c: Context = RuntimeEnvironment.getApplication()
        c.getSharedPreferences("sinav_profili", 0).edit().clear().commit()
        profil = SinavProfiliDeposu(c)
        db = Room.inMemoryDatabaseBuilder(c, YanlisDefteriDatabase::class.java).allowMainThreadQueries()
            .addCallback(VarsayilanDersleriEkle()).setQueryCallback(object : RoomDatabase.QueryCallback {
                override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) { queries.add(sqlQuery) }
            }, Executor { it.run() }).build()
        val ds = db.dersDao().tumunuGetir()
        math = ds.first { it.ad == "Matematik" }.id; physics = ds.first { it.ad == "Fizik" }.id
        repo = RoomSoruRepository(db, files, saat, sinav = profil)
    }
    @After fun kapat() { db.close() }
    private suspend fun soru(id: String, ders: String = math, source: String? = " IŞIK  TEST ", vade: Int? = gun) {
        db.soruDao().ekle(SoruEntity(id, ders, kaynak = source, sonrakiTekrarGun = vade, olusturma = 1, guncelleme = 1))
    }
    private suspend fun analiz() = GelisimAnaliziHizmeti(db).hesapla(gun, profil.secimIzle.value)
    private suspend fun kaydet(id: String) {
        val p = requireNotNull(repo.tekrarOnizle(TekrarDenemesi("attempt-$id", id, TekrarSonucu.RAHAT, false,
            SureOlcumu(60_000, 0, OlcumKalitesi.TAM))))
        assertEquals(SoruIslemSonucu.Basarili, repo.tekrarKaydet(p))
    }

    @Test fun databaseAnalyticsDistinguishLegacyAndRecordedMissingContextAndNeverLoadDecisionBlobs() = runBlocking<Unit> {
        soru("legacy"); soru("measured", source = null)
        db.tekrarDao().ekle(TekrarEntity("legacy-r", "legacy", 1, gun - 1, sonuc = "RAHAT"))
        kaydet("measured")
        queries.clear()
        val a = analiz()
        assertEquals(2, a.toplam.tekrar); assertEquals(2, a.toplam.bagimsiz); assertEquals(1, a.toplam.baglamsiz)
        assertEquals(2, a.kaynaklar.size)
        val sql = synchronized(queries) { queries.filter { it.trimStart().startsWith("SELECT", true) } }
        assertEquals(4, sql.size)
        assertTrue(sql.none { it.contains("karar_json", true) || it.contains("soru_notu", true) || it.contains("gorseller", true) })
    }
    @Test fun actualSameDayRowsOverrideAStaleQuestionSummaryInAllPracticeRoutes() = runBlocking<Unit> {
        soru("q")
        db.tekrarDao().ekle(TekrarEntity("real-today", "q", 1, gun, sonuc = "RAHAT"))
        assertNull(db.soruDao().idIleGetir("q")?.sonTekrarGun)
        assertTrue(repo.filtreliCalismaSorulariniIzle(SoruCalismaFiltresi()).first().isEmpty())
        assertTrue(repo.bugunTekrarlanacaklariIzle(gun).first().isEmpty())
        assertNull(analiz().oneri)
    }
    @Test fun normalizedSourceAndLessonDueScopeRoundTripToExactlyEligibleQuestions() = runBlocking<Unit> {
        soru("a"); soru("b", source = "ışık test"); soru("other", physics); soru("later", vade = gun + 1)
        soru("suspended"); db.soruDao().guncelle(requireNotNull(db.soruDao().idIleGetir("suspended")).copy(planDurumu = "ASKIDA"))
        val f = SoruCalismaFiltresi(dersId = math, kaynakAnahtari = "ışık test", vadesiGelen = true)
        val ids = repo.filtreliCalismaSorulariniIzle(f).first().map { it.id }.toSet()
        assertEquals(setOf("a", "b"), ids)
    }
    @Test fun unicodeBlankSourceMatchesTheKnownMissingGroupRatherThanBeingLostInSqlTrim() = runBlocking<Unit> {
        soru("unicode", source = "\u00a0"); soru("null", source = null); soru("book")
        assertEquals(setOf("unicode", "null"), repo.filtreliCalismaSorulariniIzle(
            SoruCalismaFiltresi(kaynakBelirtilmemis = true)).first().map { it.id }.toSet())
    }
    @Test fun profileChangeInvalidatesEligibilityWithoutHidingHistoricalAnalyticsOrMutatingPlans() = runBlocking<Unit> {
        soru("math"); soru("physics", physics)
        db.tekrarDao().ekle(TekrarEntity("past", "physics", 1, gun - 1, sonuc = "COZEMEDIM"))
        val before = db.soruDao().tumunuGetir()
        profil.kaydet(SinavProfili.LGS, setOf(math))
        assertEquals(listOf("math"), repo.filtreliCalismaSorulariniIzle(SoruCalismaFiltresi()).first().map { it.id })
        val a = analiz()
        assertEquals(1, a.plan.sumOf { it.adet }); assertEquals(1, a.toplam.tekrar)
        assertEquals(before, db.soruDao().tumunuGetir())
        val p = requireNotNull(repo.tekrarOnizle(TekrarDenemesi("excluded", "physics", TekrarSonucu.RAHAT, false)))
        assertEquals(SoruIslemSonucu.PlanDurumuKapali, repo.tekrarKaydet(p))
    }
    @Test fun livePracticeFlowReactsToProfileChangeWithoutAQuestionCountChange() = runBlocking<Unit> {
        soru("math"); soru("physics", physics)
        val initial = CompletableDeferred<Unit>()
        val next = async(Dispatchers.Default) {
            withTimeout(10_000) {
                repo.filtreliCalismaSorulariniIzle(SoruCalismaFiltresi()).onEach { if (it.size == 2) initial.complete(Unit) }
                    .first { it.size == 1 }
            }
        }
        withTimeout(10_000) { initial.await() }
        profil.kaydet(SinavProfili.LGS, setOf(math))
        assertEquals(listOf("math"), next.await().map { it.id })
    }
    @Test fun sourceRenameAndQuestionDeletionRebuildAnalyticsWithoutStaleCache() = runBlocking<Unit> {
        soru("q"); kaydet("q")
        val old = analiz().kaynaklar.single()
        db.soruDao().guncelle(requireNotNull(db.soruDao().idIleGetir("q")).copy(kaynak = "new book"))
        val renamed = analiz().kaynaklar.single()
        assertEquals(old.etiketler, renamed.etiketler); assertEquals(0, renamed.uygunSoru)
        assertEquals(SoruIslemSonucu.Basarili, repo.soruSil("q"))
        assertEquals(0, analiz().toplam.tekrar); assertTrue(analiz().kaynaklar.isEmpty())
    }
}

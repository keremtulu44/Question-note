package com.keremtulu.questionnote.data.repository

import androidx.room.Room
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.KonuIslemSonucu
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import com.keremtulu.questionnote.domain.model.SoruIslemSonucu
import com.keremtulu.questionnote.domain.model.SoruKaydetmeIstegi
import com.keremtulu.questionnote.domain.model.SoruPlanDurumu
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import com.keremtulu.questionnote.domain.repository.GorselDosyaYonetimi
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomSoruRepositoryTest {
    private lateinit var database: YanlisDefteriDatabase
    private lateinit var imageFiles: FakeGorselDosyaYonetimi
    private lateinit var repository: RoomSoruRepository
    private lateinit var dersId: String

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            YanlisDefteriDatabase::class.java,
        )
            .addCallback(VarsayilanDersleriEkle())
            .allowMainThreadQueries()
            .build()
        imageFiles = FakeGorselDosyaYonetimi()
        repository = RoomSoruRepository(
            database = database,
            gorselDosyalari = imageFiles,
            saat = Clock.fixed(Instant.parse("2026-10-16T12:00:00Z"), ZoneId.of("Europe/Istanbul")),
        )
        dersId = database.dersDao().tumunuGetir().first().id
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createEditAndFilterQuestionsKeepsLessonAndTopicRelationships() = runBlocking {
        val konuRepo = RoomKonuRepository(database)
        assertEquals(
            com.keremtulu.questionnote.domain.model.KonuIslemSonucu.Basarili,
            konuRepo.konuEkle(dersId, "Fonksiyonlar"),
        )
        val konuId = konuRepo.konulariIzle(dersId, arsivli = false).first().single().id
        val saved = SoruKaydetmeIstegi(
            dersId = dersId,
            konuId = konuId,
            soruNotu = "  Polinom sorusu  ",
            gorseller = listOf(imageFiles.yeniGorsel(GorselTuru.SORU, 0)),
        )
        assertEquals(SoruIslemSonucu.Basarili, repository.soruKaydet(saved))
        val created = repository.sorulariIzle(dersId, konuId = konuId).first().single()
        assertEquals("Polinom sorusu", created.soruNotu)
        assertEquals(konuId, created.konuId)
        assertEquals(1, created.gorseller.size)
        assertTrue(created.sonrakiTekrarGun != null)

        val detail = repository.soruGetir(created.id)
        assertNotNull(detail)
        val existingImage = detail!!.gorseller.single()
        val edit = saved.copy(
            id = created.id,
            soruNotu = "Güncellendi",
            cozumNotu = "Önce çarpanlara ayır.",
            gorseller = listOf(existingImage.asGirdi()),
        )
        assertEquals(SoruIslemSonucu.Basarili, repository.soruKaydet(edit))
        assertEquals("Güncellendi", repository.soruGetir(created.id)?.soruNotu)
        assertTrue(repository.sorulariIzle(dersId, konusuz = true).first().isEmpty())
        assertEquals("Önce çarpanlara ayır.", repository.soruGetir(created.id)?.cozumNotu)
    }

    @Test
    fun questionRequiresAtLeastOneQuestionImageAndArchivedLessonsRejectNewQuestions() = runBlocking {
        assertEquals(
            SoruIslemSonucu.SoruGorseliGerekli,
            repository.soruKaydet(SoruKaydetmeIstegi(dersId = dersId, gorseller = emptyList())),
        )
        assertEquals(
            com.keremtulu.questionnote.domain.model.DersIslemSonucu.Basarili,
            RoomDersRepository(database).dersArsivDurumunuDegistir(dersId, true),
        )
        assertEquals(
            SoruIslemSonucu.GecersizDers,
            repository.soruKaydet(
                SoruKaydetmeIstegi(
                    dersId = dersId,
                    gorseller = listOf(imageFiles.yeniGorsel(GorselTuru.SORU, 0)),
                ),
            ),
        )
    }

    @Test
    fun archiveRetainsTopicLinkAndDeletingQuestionCascadesOnlyItsRowsAndFiles() = runBlocking {
        val konuRepo = RoomKonuRepository(database)
        konuRepo.konuEkle(dersId, "Fonksiyonlar")
        val konuId = konuRepo.konulariIzle(dersId, false).first().single().id
        val image = imageFiles.yeniGorsel(GorselTuru.SORU, 0)
        assertEquals(
            SoruIslemSonucu.Basarili,
            repository.soruKaydet(SoruKaydetmeIstegi(dersId = dersId, konuId = konuId, gorseller = listOf(image))),
        )
        val soru = repository.sorulariIzle(dersId).first().single()
        assertEquals(KonuIslemSonucu.Basarili, konuRepo.konuArsivle(konuId))
        val details = repository.soruGetir(soru.id)!!
        assertEquals(konuId, details.konuId)
        assertEquals(
            SoruIslemSonucu.Basarili,
            repository.soruKaydet(
                SoruKaydetmeIstegi(
                    id = soru.id,
                    dersId = dersId,
                    konuId = konuId,
                    soruNotu = "Konu arşivlendi",
                    gorseller = details.gorseller.map { it.asGirdi() },
                ),
            ),
        )
        assertEquals(SoruIslemSonucu.Basarili, repository.tekrarKaydet(soru.id, TekrarSonucu.COZEMEDIM, false))
        assertEquals(SoruIslemSonucu.Basarili, repository.soruSil(soru.id))
        assertNull(repository.soruGetir(soru.id))
        assertTrue(imageFiles.permanent.isEmpty())
        assertEquals(0, database.tekrarDao().sorununTekrarlariniGetir(soru.id).size)
        assertEquals(0, database.gorselDao().sorununGorselleriniGetir(soru.id).size)
    }

    @Test
    fun sameDayReviewIsRejectedAndPlanStatusIsHonoured() = runBlocking {
        val image = imageFiles.yeniGorsel(GorselTuru.SORU, 0)
        assertEquals(
            SoruIslemSonucu.Basarili,
            repository.soruKaydet(SoruKaydetmeIstegi(dersId = dersId, gorseller = listOf(image))),
        )
        val soru = repository.sorulariIzle(dersId).first().single()
        assertEquals(SoruIslemSonucu.Basarili, repository.tekrarKaydet(soru.id, TekrarSonucu.RAHAT, true))
        assertEquals(
            SoruIslemSonucu.AyniGunTekrarVar,
            repository.tekrarKaydet(soru.id, TekrarSonucu.RAHAT, false),
        )
        assertEquals(1, database.tekrarDao().sorununTekrarlariniGetir(soru.id).size)
        assertEquals(
            LocalDate.of(2026, 10, 17).toEpochDay().toInt(),
            repository.soruGetir(soru.id)?.sonrakiTekrarGun,
        )

        assertEquals(
            SoruIslemSonucu.Basarili,
            repository.planDurumunuAyarla(soru.id, SoruPlanDurumu.ASKIDA),
        )
        assertEquals(
            SoruIslemSonucu.PlanDurumuKapali,
            repository.tekrarKaydet(soru.id, TekrarSonucu.RAHAT, false),
        )
    }

    private fun com.keremtulu.questionnote.domain.model.SoruGorseli.asGirdi() = SoruGorselGirdisi(
        id = id,
        tur = tur,
        sira = sira,
        dosyaAdi = dosyaAdi,
        gecici = false,
        mimeType = mimeType,
        genislik = genislik,
        yukseklik = yukseklik,
        boyut = boyut,
        sha256 = sha256,
    )

    private class FakeGorselDosyaYonetimi : GorselDosyaYonetimi {
        val staged = mutableMapOf<String, SoruGorselGirdisi>()
        val permanent = mutableMapOf<String, SoruGorselGirdisi>()

        fun yeniGorsel(tur: GorselTuru, sira: Int): SoruGorselGirdisi {
            val id = UUID.randomUUID().toString()
            val fileName = "$id.jpg"
            val image = SoruGorselGirdisi(
                id = id,
                tur = tur,
                sira = sira,
                dosyaAdi = fileName,
                gecici = true,
                mimeType = "image/jpeg",
                genislik = 900,
                yukseklik = 1200,
                boyut = 12_000,
                sha256 = "a".repeat(64),
            )
            staged[fileName] = image
            return image
        }

        override suspend fun geciciDosyayiDogrula(gorsel: SoruGorselGirdisi): Boolean =
            staged[gorsel.dosyaAdi] == gorsel

        override suspend fun kaliciyaKopyala(geciciDosyaAdi: String): String? {
            val source = staged[geciciDosyaAdi] ?: return null
            val name = "img/${source.id}.jpg"
            permanent[name] = source.copy(dosyaAdi = name, gecici = false)
            return name
        }

        override suspend fun geciciDosyayiSil(geciciDosyaAdi: String) {
            staged.remove(geciciDosyaAdi)
        }

        override suspend fun kaliciDosyayiSil(dosyaAdi: String): Boolean {
            permanent.remove(dosyaAdi)
            return true
        }

        override suspend fun kaliciDosyayiDogrula(gorsel: SoruGorselGirdisi): Boolean =
            permanent[gorsel.dosyaAdi]?.sha256 == gorsel.sha256
    }
}

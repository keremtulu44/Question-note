package com.keremtulu.questionnote.data.repository

import androidx.room.Room
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.TekrarEntity
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.KonuIslemSonucu
import com.keremtulu.questionnote.domain.model.OgrenmeDurumu
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi
import com.keremtulu.questionnote.domain.model.SoruIslemSonucu
import com.keremtulu.questionnote.domain.model.SoruKaydetmeIstegi
import com.keremtulu.questionnote.domain.model.SoruPlanDurumu
import com.keremtulu.questionnote.domain.model.SoruSecenekTuru
import com.keremtulu.questionnote.domain.model.SoruSecenegiEklemeSonucu
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import com.keremtulu.questionnote.domain.calisma.TekrarDenemesi
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
    fun createEditAndFilterQuestionsKeepsRelationshipsAndPreservesLegacyNote() = runBlocking {
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
        assertEquals("Polinom sorusu", repository.soruGetir(created.id)?.soruNotu)
        assertTrue(repository.sorulariIzle(dersId, konusuz = true).first().isEmpty())
        assertEquals("Önce çarpanlara ayır.", repository.soruGetir(created.id)?.cozumNotu)
    }

    @Test
    fun filteredStudyFeedCombinesTopicSourceAndReasonAndExcludesPausedQuestions() = runBlocking {
        val topicRepository = RoomKonuRepository(database)
        assertEquals(KonuIslemSonucu.Basarili, topicRepository.konuEkle(dersId, "Fonksiyonlar"))
        assertEquals(KonuIslemSonucu.Basarili, topicRepository.konuEkle(dersId, "Üslü sayılar"))
        val topics = topicRepository.konulariIzle(dersId, arsivli = false).first().associateBy { it.ad }
        val topicId = requireNotNull(topics["Fonksiyonlar"]).id
        val anotherTopicId = requireNotNull(topics["Üslü sayılar"]).id

        val matching = newQuestionFor(dersId, topicId, "Kitap A", "İşlem hatası")
        newQuestionFor(dersId, topicId, "Kitap B", "İşlem hatası")
        newQuestionFor(dersId, topicId, "Kitap A", "Kavram yanılgısı")
        newQuestionFor(dersId, anotherTopicId, "Kitap A", "İşlem hatası")
        val paused = newQuestionFor(dersId, topicId, "Kitap A", "İşlem hatası")
        assertEquals(SoruIslemSonucu.Basarili, repository.planDurumunuAyarla(paused.id, SoruPlanDurumu.ASKIDA))

        val result = repository.filtreliCalismaSorulariniIzle(
            SoruCalismaFiltresi(konuId = topicId, kaynak = "Kitap A", yanlisNedeni = "İşlem hatası"),
        ).first()

        assertEquals(listOf(matching.id), result.map { it.id })

        val archivedLessonId = database.dersDao().tumunuGetir().first { it.id != dersId }.id
        assertEquals(KonuIslemSonucu.Basarili, topicRepository.konuEkle(archivedLessonId, "Arşiv konusu"))
        val archivedTopicId = topicRepository.konulariIzle(archivedLessonId, arsivli = false).first().single().id
        newQuestionFor(archivedLessonId, archivedTopicId, "Kitap A", "İşlem hatası")
        assertEquals(
            com.keremtulu.questionnote.domain.model.DersIslemSonucu.Basarili,
            RoomDersRepository(database).dersArsivDurumunuDegistir(archivedLessonId, true),
        )
        assertTrue(
            repository.filtreliCalismaSorulariniIzle(
                SoruCalismaFiltresi(konuId = archivedTopicId, kaynak = "Kitap A", yanlisNedeni = "İşlem hatası"),
            ).first().isEmpty(),
        )
    }

    @Test
    fun filteredStudyFeedCanSelectUnassignedTopicAndMissingSourceAndReason() = runBlocking {
        val missingValues = newQuestionFor(dersId, topicId = null, kaynak = "", yanlisNedeni = "")
        newQuestionFor(dersId, topicId = null, kaynak = "Kitap A", yanlisNedeni = "")
        newQuestionFor(dersId, topicId = null, kaynak = "", yanlisNedeni = "İşlem hatası")

        val result = repository.filtreliCalismaSorulariniIzle(
            SoruCalismaFiltresi(konusuz = true, kaynakBelirtilmemis = true, yanlisNedeniBelirtilmemis = true),
        ).first()

        assertEquals(listOf(missingValues.id), result.map { it.id })
    }

    @Test
    fun filteredStudyFeedDoesNotOfferQuestionsAlreadyReviewedToday() = runBlocking {
        val reviewedToday = newQuestionFor(dersId)
        val notReviewedToday = newQuestionFor(dersId)
        assertEquals(
            SoruIslemSonucu.Basarili,
            repository.tekrarKaydet(reviewedToday.id, TekrarSonucu.RAHAT, cozumAcildi = false),
        )

        val result = repository.filtreliCalismaSorulariniIzle(SoruCalismaFiltresi()).first()

        assertEquals(listOf(notReviewedToday.id), result.map { it.id })
    }

    @Test
    fun sharedCatalogOptionsAreReusableAndQuestionStoresItsOwnSourceLocation() = runBlocking {
        val source = repository.secenekEkle(SoruSecenekTuru.KAYNAK, " Test Kitabı A ")
        assertTrue(source is SoruSecenegiEklemeSonucu.Basarili)
        val sourceOption = (source as SoruSecenegiEklemeSonucu.Basarili).secenek
        val duplicateSource = repository.secenekEkle(SoruSecenekTuru.KAYNAK, "test kitabı a")
        assertTrue(duplicateSource is SoruSecenegiEklemeSonucu.ZatenVar)
        assertEquals(sourceOption.id, (duplicateSource as SoruSecenegiEklemeSonucu.ZatenVar).secenek.id)

        val reason = repository.secenekEkle(SoruSecenekTuru.YANLIS_NEDENI, "Grafiği yanlış yorumladım")
        assertTrue(reason is SoruSecenegiEklemeSonucu.Basarili)
        val reasonOption = (reason as SoruSecenegiEklemeSonucu.Basarili).secenek
        val image = imageFiles.yeniGorsel(GorselTuru.SORU, 0)
        assertEquals(
            SoruIslemSonucu.Basarili,
            repository.soruKaydet(
                SoruKaydetmeIstegi(
                    dersId = dersId,
                    soruNotu = "Eski not verisi",
                    kaynak = sourceOption.ad,
                    kaynakKonumu = "Sayfa 42, test 3, soru 8",
                    yanlisNedeni = reasonOption.ad,
                    gorseller = listOf(image),
                ),
            ),
        )
        val question = repository.sorulariIzle(dersId).first().single()
        assertEquals(sourceOption.ad, question.kaynak)
        assertEquals("Sayfa 42, test 3, soru 8", question.kaynakKonumu)
        assertEquals(reasonOption.ad, question.yanlisNedeni)
        assertEquals("Eski not verisi", question.soruNotu)
        assertTrue(repository.secenekleriIzle(SoruSecenekTuru.KAYNAK).first().any { it.id == sourceOption.id })
        assertTrue(repository.secenekleriIzle(SoruSecenekTuru.YANLIS_NEDENI).first().any { it.id == reasonOption.id })
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
    fun todayReviewFeedIncludesOverdueAndDueActiveQuestionsOnly() = runBlocking {
        val today = LocalDate.of(2026, 10, 16).toEpochDay().toInt()
        val overdue = newQuestionFor(dersId)
        val dueToday = newQuestionFor(dersId)
        val future = newQuestionFor(dersId)
        val paused = newQuestionFor(dersId)
        val archivedLessonId = database.dersDao().tumunuGetir().last().id
        val archivedLessonQuestion = newQuestionFor(archivedLessonId)

        setNextReview(overdue.id, today - 2)
        setNextReview(dueToday.id, today)
        setNextReview(future.id, today + 1)
        setNextReview(paused.id, today - 1, SoruPlanDurumu.ASKIDA)
        setNextReview(archivedLessonQuestion.id, today - 3)
        assertEquals(
            com.keremtulu.questionnote.domain.model.DersIslemSonucu.Basarili,
            RoomDersRepository(database).dersArsivDurumunuDegistir(archivedLessonId, true),
        )

        val actual = repository.bugunTekrarlanacaklariIzle(today).first()
        assertEquals(listOf(overdue.id, dueToday.id), actual.map { it.id })
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

    @Test
    fun reviewHistoryIsNewestFirstAndPreservesPlannedActualAndSolutionDetails() = runBlocking {
        val question = newQuestionFor(dersId)
        val plannedDay = LocalDate.of(2026, 10, 15).toEpochDay().toInt()
        val earlierActualDay = LocalDate.of(2026, 10, 16).toEpochDay().toInt()
        val laterActualDay = LocalDate.of(2026, 10, 17).toEpochDay().toInt()
        assertTrue(repository.tekrarGecmisiniIzle(question.id).first().isEmpty())
        database.tekrarDao().ekle(
            listOf(
                TekrarEntity(
                    id = "older-review",
                    soruId = question.id,
                    zaman = 100L,
                    gun = earlierActualDay,
                    planlananGun = plannedDay,
                    sonuc = TekrarSonucu.COZEMEDIM.name,
                    cozumAcildi = false,
                    oncekiAralik = 1,
                    sonrakiAralik = 1,
                ),
                TekrarEntity(
                    id = "newer-review",
                    soruId = question.id,
                    zaman = 200L,
                    gun = laterActualDay,
                    planlananGun = earlierActualDay,
                    sonuc = TekrarSonucu.RAHAT.name,
                    cozumAcildi = true,
                    oncekiAralik = 7,
                    sonrakiAralik = 1,
                    elleAtandi = true,
                ),
            ),
        )

        val history = repository.tekrarGecmisiniIzle(question.id).first()

        assertEquals(listOf("newer-review", "older-review"), history.map { it.id })
        assertEquals(laterActualDay, history.first().gun)
        assertEquals(earlierActualDay, history.first().planlananGun)
        assertEquals(TekrarSonucu.RAHAT, history.first().sonuc)
        assertTrue(history.first().cozumAcildi)
        assertEquals(7, history.first().oncekiAralik)
        assertEquals(1, history.first().sonrakiAralik)
        assertTrue(history.first().elleAtandi)
        assertEquals(TekrarSonucu.COZEMEDIM, history.last().sonuc)
        assertTrue(!history.last().cozumAcildi)
    }

    @Test
    fun adaptivePreviewAndStoredV2PlanMatchWhileHistoryIsPreserved() = runBlocking {
        val soru = newQuestionFor(dersId)
        val once = database.soruDao().idIleGetir(soru.id)
        val bugun = LocalDate.of(2026, 10, 16)
        val preview = com.keremtulu.questionnote.domain.repeat.AkilliTekrarKurali.hesapla(
            emptyList(), TekrarSonucu.ZORLANDIM, false, bugun, soru.sonrakiTekrarGun,
        )
        assertEquals(SoruIslemSonucu.Basarili, repository.tekrarKaydet(soru.id, TekrarSonucu.ZORLANDIM, false))
        val tekrar = database.tekrarDao().sorununTekrarlariniGetir(soru.id).single()
        assertEquals(2, tekrar.algoritmaSurumu)
        assertEquals(preview.sonrakiAralik, tekrar.sonrakiAralik)
        assertEquals(2, tekrar.sonrakiAralik)
        assertEquals(preview.sonrakiGun, database.soruDao().idIleGetir(soru.id)?.sonrakiTekrarGun)
        assertEquals(once?.dersId, repository.soruGetir(soru.id)?.dersId)
        assertEquals(2, repository.tekrarGecmisiniIzle(soru.id).first().single().algoritmaSurumu)
    }

    @Test
    fun catalogueDeletionProtectsDefaultsAndUsedCustomChoices() = runBlocking {
        val default = repository.secenekleriIzle(SoruSecenekTuru.YANLIS_NEDENI).first().first { it.varsayilan }
        assertEquals(com.keremtulu.questionnote.domain.model.SoruSecenegiSilmeSonucu.VarsayilanSilinemez, repository.secenekSil(default.id))
        val yeni = repository.secenekEkle(SoruSecenekTuru.KAYNAK, "Özel deneme") as SoruSecenegiEklemeSonucu.Basarili
        newQuestionFor(dersId, kaynak = yeni.secenek.ad)
        assertEquals(com.keremtulu.questionnote.domain.model.SoruSecenegiSilmeSonucu.Kullanimda(1), repository.secenekSil(yeni.secenek.id))
        val bos = repository.secenekEkle(SoruSecenekTuru.YANLIS_NEDENI, "Kullanılmayan neden") as SoruSecenegiEklemeSonucu.Basarili
        assertEquals(com.keremtulu.questionnote.domain.model.SoruSecenegiSilmeSonucu.Basarili, repository.secenekSil(bos.secenek.id))
    }

    private suspend fun newQuestionFor(
        lessonId: String,
        topicId: String? = null,
        kaynak: String = "",
        yanlisNedeni: String = "",
    ): com.keremtulu.questionnote.domain.model.Soru {
        val existingIds = repository.sorulariIzle(lessonId).first().map { it.id }.toSet()
        val image = imageFiles.yeniGorsel(GorselTuru.SORU, 0)
        assertEquals(
            SoruIslemSonucu.Basarili,
            repository.soruKaydet(
                SoruKaydetmeIstegi(
                    dersId = lessonId,
                    konuId = topicId,
                    kaynak = kaynak,
                    yanlisNedeni = yanlisNedeni,
                    gorseller = listOf(image),
                ),
            ),
        )
        val saved = repository.sorulariIzle(lessonId).first().first { it.id !in existingIds }
        return requireNotNull(repository.soruGetir(saved.id))
    }

    private suspend fun setNextReview(
        soruId: String,
        day: Int,
        status: SoruPlanDurumu = SoruPlanDurumu.AKTIF,
    ) {
        val soru = requireNotNull(database.soruDao().idIleGetir(soruId))
        assertEquals(
            1,
            database.soruDao().guncelle(soru.copy(sonrakiTekrarGun = day, planDurumu = status.name)),
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

    @Test
    fun geriAlSonKaydiSilipPlaniEskiHalineDondurur() = runBlocking {
        val konuRepo = RoomKonuRepository(database)
        assertEquals(KonuIslemSonucu.Basarili, konuRepo.konuEkle(dersId, "Üslü Sayılar"))
        val konuId = konuRepo.konulariIzle(dersId, arsivli = false).first().single().id
        val saved = SoruKaydetmeIstegi(
            dersId = dersId,
            konuId = konuId,
            gorseller = listOf(imageFiles.yeniGorsel(GorselTuru.SORU, 0)),
        )
        assertEquals(SoruIslemSonucu.Basarili, repository.soruKaydet(saved))
        val soru = repository.sorulariIzle(dersId, konuId = konuId).first().single()
        val baslangicPlani = soru.sonrakiTekrarGun
        assertNotNull(baslangicPlani)

        val denemeId = UUID.randomUUID().toString()
        val onizleme = repository.tekrarOnizle(TekrarDenemesi(denemeId, soru.id, TekrarSonucu.RAHAT, false))
        assertNotNull(onizleme)
        assertEquals(SoruIslemSonucu.Basarili, repository.tekrarKaydet(onizleme!!))
        assertEquals(1, database.tekrarDao().sorununTekrarlariniGetir(soru.id).size)
        assertNotNull(database.tekrarOlcumDao().denemeIleGetir(denemeId))
        assertNotNull(repository.soruGetir(soru.id)!!.sonTekrarGun)

        assertEquals(SoruIslemSonucu.Basarili, repository.tekrarGeriAl(soru.id))
        assertEquals(0, database.tekrarDao().sorununTekrarlariniGetir(soru.id).size)
        assertNull(database.tekrarOlcumDao().denemeIleGetir(denemeId))
        val geriAlinan = repository.soruGetir(soru.id)!!
        assertEquals(baslangicPlani, geriAlinan.sonrakiTekrarGun)
        assertNull(geriAlinan.sonTekrarGun)
        assertEquals(OgrenmeDurumu.YENI, geriAlinan.ogrenmeDurumu)

        // Geri alınca idempotluk satırı da gider: aynı deneme kimliğiyle yeniden kayıt serbesttir.
        val yeniden = repository.tekrarOnizle(TekrarDenemesi(denemeId, soru.id, TekrarSonucu.RAHAT, false))
        assertNotNull(yeniden)
        assertEquals(SoruIslemSonucu.Basarili, repository.tekrarKaydet(yeniden!!))
    }

    @Test
    fun geriAlOnDakikadanEskiKaydiSilmemez() = runBlocking {
        val konuRepo = RoomKonuRepository(database)
        assertEquals(KonuIslemSonucu.Basarili, konuRepo.konuEkle(dersId, "Logaritma"))
        val konuId = konuRepo.konulariIzle(dersId, arsivli = false).first().single().id
        assertEquals(
            SoruIslemSonucu.Basarili,
            repository.soruKaydet(
                SoruKaydetmeIstegi(
                    dersId = dersId,
                    konuId = konuId,
                    gorseller = listOf(imageFiles.yeniGorsel(GorselTuru.SORU, 0)),
                ),
            ),
        )
        val soru = repository.sorulariIzle(dersId, konuId = konuId).first().single()
        val onizleme = repository.tekrarOnizle(
            TekrarDenemesi(UUID.randomUUID().toString(), soru.id, TekrarSonucu.RAHAT, false),
        )
        assertNotNull(onizleme)
        assertEquals(SoruIslemSonucu.Basarili, repository.tekrarKaydet(onizleme!!))

        // Kayıttan 11 dakika sonra: pencere kapalı, kayıt silinemez.
        val gecSaatli = RoomSoruRepository(
            database = database,
            gorselDosyalari = imageFiles,
            saat = Clock.fixed(Instant.parse("2026-10-16T12:11:00Z"), ZoneId.of("Europe/Istanbul")),
        )
        assertEquals(SoruIslemSonucu.Bulunamadi, gecSaatli.tekrarGeriAl(soru.id))
        assertEquals(1, database.tekrarDao().sorununTekrarlariniGetir(soru.id).size)
        assertNotNull(database.tekrarDao().sorununTekrarlariniGetir(soru.id).single().id)
    }

    @Test
    fun geriAlCokluGecmisteYalnizcaSonKaydiSilipKalanlarKorur() = runBlocking {
        val konuRepo = RoomKonuRepository(database)
        assertEquals(KonuIslemSonucu.Basarili, konuRepo.konuEkle(dersId, "Türev"))
        val konuId = konuRepo.konulariIzle(dersId, arsivli = false).first().single().id
        assertEquals(
            SoruIslemSonucu.Basarili,
            repository.soruKaydet(
                SoruKaydetmeIstegi(
                    dersId = dersId,
                    konuId = konuId,
                    gorseller = listOf(imageFiles.yeniGorsel(GorselTuru.SORU, 0)),
                ),
            ),
        )
        val soru = repository.sorulariIzle(dersId, konuId = konuId).first().single()
        val ilkOnizleme = repository.tekrarOnizle(
            TekrarDenemesi(UUID.randomUUID().toString(), soru.id, TekrarSonucu.ZORLANDIM, false),
        )
        assertNotNull(ilkOnizleme)
        assertEquals(SoruIslemSonucu.Basarili, repository.tekrarKaydet(ilkOnizleme!!))
        assertEquals(1, database.tekrarDao().sorununTekrarlariniGetir(soru.id).size)

        val ikiGunSonra = RoomSoruRepository(
            database = database,
            gorselDosyalari = imageFiles,
            saat = Clock.fixed(Instant.parse("2026-10-18T12:00:00Z"), ZoneId.of("Europe/Istanbul")),
        )
        val ikinciOncesi = ikiGunSonra.soruGetir(soru.id)!!
        val ikinciOnizleme = ikiGunSonra.tekrarOnizle(
            TekrarDenemesi(UUID.randomUUID().toString(), soru.id, TekrarSonucu.RAHAT, false),
        )
        assertNotNull(ikinciOnizleme)
        assertEquals(SoruIslemSonucu.Basarili, ikiGunSonra.tekrarKaydet(ikinciOnizleme!!))
        assertEquals(2, database.tekrarDao().sorununTekrarlariniGetir(soru.id).size)

        assertEquals(SoruIslemSonucu.Basarili, ikiGunSonra.tekrarGeriAl(soru.id))
        val kalan = database.tekrarDao().sorununTekrarlariniGetir(soru.id)
        assertEquals(1, kalan.size)
        val geri = ikiGunSonra.soruGetir(soru.id)!!
        assertEquals(ikinciOncesi.sonrakiTekrarGun, geri.sonrakiTekrarGun)
        assertNotNull(geri.sonTekrarGun)
        assertTrue(
            geri.sonTekrarGun!! < LocalDate.of(2026, 10, 18).toEpochDay().toInt(),
        )
    }

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

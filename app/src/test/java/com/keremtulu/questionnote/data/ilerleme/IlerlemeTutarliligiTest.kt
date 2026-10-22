package com.keremtulu.questionnote.data.ilerleme

import android.content.Context
import androidx.room.Room
import com.keremtulu.questionnote.data.local.VarsayilanDersleriEkle
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.SoruEntity
import com.keremtulu.questionnote.data.local.entity.TekrarEntity
import com.keremtulu.questionnote.data.prefs.RozetDeposu
import com.keremtulu.questionnote.data.prefs.TercihGunlukHedefDeposu
import com.keremtulu.questionnote.data.seviye.SeviyeHizmeti
import com.keremtulu.questionnote.data.seviye.SeviyeOzeti
import com.keremtulu.questionnote.domain.karne.Rozet
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
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
class IlerlemeTutarliligiTest {
    private lateinit var db: YanlisDefteriDatabase
    private lateinit var rozetler: RozetDeposu
    private lateinit var hedef: TercihGunlukHedefDeposu
    private lateinit var odul: RozetHizmeti
    private lateinit var seviye: SeviyeHizmeti

    @Before fun hazirla() {
        val context: Context = RuntimeEnvironment.getApplication()
        listOf("karne_rozetleri", "ilerleme_ayarlari").forEach {
            context.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().commit()
        }
        db = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java)
            .addCallback(VarsayilanDersleriEkle()).allowMainThreadQueries().build()
        rozetler = RozetDeposu(context)
        hedef = TercihGunlukHedefDeposu(context)
        odul = RozetHizmeti(db, rozetler, hedef)
        seviye = SeviyeHizmeti(db, rozetler, hedef, odul)
    }
    @After fun kapat() { db.close() }

    private suspend fun besTekrar() {
        val ders = db.dersDao().tumunuGetir().first().id
        repeat(5) { i ->
            val id = "q$i"
            db.soruDao().ekle(SoruEntity(id, ders, olusturma = 1, guncelleme = 1))
            db.tekrarDao().ekle(TekrarEntity("r$i", id, 1, 1, sonuc = if (i == 0) "ZORLANDIM" else "RAHAT"))
        }
    }

    @Test fun summaryUsesOneOwnershipSnapshotForGalleryAndBadgeXp() = runBlocking {
        besTekrar()
        rozetler.kaydet(listOf(Rozet.ILK_SORU, Rozet.ILK_KARNE))
        val ozet = seviye.ozet()
        assertEquals(rozetler.sahipOlanlar(), ozet.sahipRozetler)
        assertEquals(ozet.sahipRozetler.size * 50, ozet.kirilim.rozet)
        assertEquals(4, ozet.bagimsizSayisi)
        assertEquals(20, ozet.kirilim.bagimsiz)
        assertEquals(25, ozet.kirilim.tekrar)
        assertEquals(ozet.kirilim.toplam, ozet.durum.toplamXp)
    }

    @Test fun goalChangesRefreshProgressWithoutAnotherReviewOrNavigation() = runBlocking {
        besTekrar()
        val cevaplar = Channel<SeviyeOzeti>(Channel.UNLIMITED)
        val islem = launch { seviye.ozetIzle().collect { cevaplar.send(it) } }
        try {
            val ilk = withTimeout(5_000) { cevaplar.receive() }
            assertEquals(10, ilk.hedef)
            assertEquals(0, ilk.kirilim.hedef)
            hedef.hedefiAyarla(5)
            val yeni = withTimeout(5_000) {
                var sonraki = cevaplar.receive()
                while (sonraki.hedef != 5) sonraki = cevaplar.receive()
                sonraki
            }
            assertEquals(25, yeni.kirilim.hedef)
        } finally { islem.cancelAndJoin(); cevaplar.close() }
    }

    @Test fun goalChangesReevaluateOwnershipWithoutChangingTheArchiveOrCelebratingRetroactiveWork() = runBlocking {
        besTekrar()
        val cevaplar = Channel<List<Rozet>>(Channel.UNLIMITED)
        val islem = launch { odul.degisiklikleriIzle().collect { cevaplar.send(it) } }
        try {
            withTimeout(5_000) { cevaplar.receive() }
            assertFalse(Rozet.HEDEF_1 in rozetler.sahipOlanlar())
            hedef.hedefiAyarla(5)
            withTimeout(5_000) {
                while (Rozet.HEDEF_1 !in rozetler.sahipOlanlar()) cevaplar.receive()
            }
            assertTrue(Rozet.HEDEF_1 in rozetler.sahipOlanlar())
            assertEquals(5, db.tekrarDao().tumunuGetir().size)
            // P5 contract: goal recalculation grants ownership/XP, not a retroactive celebration storm.
            assertEquals(0, rozetler.bekleyenIzle.value.count { it == Rozet.HEDEF_1 })
            assertTrue(Rozet.HEDEF_1 in RozetDeposu(RuntimeEnvironment.getApplication()).sahipOlanlar())
        } finally { islem.cancelAndJoin(); cevaplar.close() }
    }
}

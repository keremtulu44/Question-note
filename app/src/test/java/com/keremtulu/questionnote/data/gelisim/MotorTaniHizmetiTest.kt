package com.keremtulu.questionnote.data.gelisim

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.*
import com.keremtulu.questionnote.domain.calisma.TekrarDenemesi
import com.keremtulu.questionnote.domain.gelisim.KisiselProfilHesabi
import com.keremtulu.questionnote.domain.gelisim.ProfilBaglami
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import com.keremtulu.questionnote.domain.repeat.*
import java.time.LocalDate
import java.util.Collections
import java.util.concurrent.Executor
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
class MotorTaniHizmetiTest {
    private lateinit var db: YanlisDefteriDatabase
    private val queries = Collections.synchronizedList(mutableListOf<String>())
    private val gun = LocalDate.of(2026, 10, 18).toEpochDay().toInt()
    @Before fun ac() {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), YanlisDefteriDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryCallback(object : RoomDatabase.QueryCallback {
                override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) { queries += sqlQuery }
            }, Executor { it.run() }).build()
    }
    @After fun kapat() { db.close() }

    @Test fun emptyArchiveHasNoInventedDecisionOrValidationSuccess() = runBlocking<Unit> {
        assertEquals(MotorTanisi(0, 0, 0, 0, 0, 0, 0), MotorTaniHizmeti(db).oku())
    }

    @Test fun onlyNewestHundredAreValidatedAndTotalIsNotMistakenForTheSample() = runBlocking<Unit> {
        doldur()
        queries.clear()
        val tani = MotorTaniHizmeti(db).oku()
        assertEquals(MotorTanisi(121, 100, 0, 99, 0, 0, 1), tani)
        val reads = synchronized(queries) { queries.filter { it.trimStart().startsWith("SELECT", true) &&
            !it.contains("room_table_modification_log", true) }.toList() }
        // Room may replay the parent cursor after collecting relation keys. That is still bounded;
        // require one COUNT, 1–2 limited parent executions and exactly one batched child read (not N+1).
        val parents = reads.filter { it.contains("SELECT t.* FROM tekrarlar") }
        assertTrue(reads.joinToString("\n"), parents.size in 1..2)
        assertTrue(parents.all { it.contains("LIMIT", true) })
        assertEquals(1, reads.count { it.contains("SELECT COUNT(*)") })
        assertEquals(1, reads.count { it.contains("WHERE `tekrar_id` IN") && it.count { c -> c == '?' } == 100 })
        assertEquals(reads.joinToString("\n"), parents.size + 2, reads.size)
        val rows = db.tekrarOlcumDao().sonKararlar(100)
        assertEquals("r120", rows.first().tekrar.id)
        assertEquals("r21", rows.last().tekrar.id)
        assertEquals(100, rows.size)
    }

    @Test fun deletingNewestInvalidRecordRebuildsTheBoundedWindowWithoutAStaleCache() = runBlocking<Unit> {
        doldur()
        val hizmet = MotorTaniHizmeti(db)
        assertEquals(1, hizmet.oku().gecersiz)
        db.soruDao().sil(requireNotNull(db.soruDao().idIleGetir("q120")))
        // Cascade removes the bad record; the next-oldest V2 record enters the 100-row window.
        assertEquals(MotorTanisi(120, 100, 1, 99, 0, 0, 0), hizmet.oku())
    }

    private suspend fun doldur() {
        val reference = KisiselProfilHesabi.hesapla(emptyList(), ProfilBaglami("m"), gun)
        val questions = (0..120).map { SoruEntity("q$it", "m", olusturma = 1, guncelleme = 1) }
        val repeats = mutableListOf<TekrarEntity>()
        val measures = mutableListOf<TekrarOlcumEntity>()
        questions.forEachIndexed { i, q ->
            val attempt = TekrarDenemesi("a$i", q.id, TekrarSonucu.RAHAT, false)
            val mod = if (i < 21) TekrarMotorModu.KAPALI else TekrarMotorModu.GOLGE
            val karar = UyarlanabilirTekrarKurali.hesapla(emptyList(), attempt, reference,
                LocalDate.ofEpochDay(gun.toLong()), gun, MotorAyari(mod))
            val t = TekrarEntity("r$i", q.id, gun * 86_400_000L + i, gun, gun, "RAHAT", false,
                karar.uygulanan.oncekiAralik, karar.uygulanan.sonrakiAralik, 2)
            val m = TekrarOlcumEntity(t.id, attempt.denemeId, null, null, null, "YOK", "[]", 1,
                "m", "Matematik", null, null, null, null, OlcumSerilestirme.kararYaz(karar, gun, reference, "UTC"))
            OlcumSerilestirme.dogrula(m, t)
            repeats += t
            measures += if (i == 120) m.copy(kararJson = "{broken") else m
        }
        db.withTransaction {
            db.dersDao().ekle(DersEntity("m", "Matematik", 0, olusturma = 1, guncelleme = 1))
            db.soruDao().ekle(questions); db.tekrarDao().ekle(repeats); db.tekrarOlcumDao().ekle(measures)
        }
    }
}

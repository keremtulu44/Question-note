package com.keremtulu.questionnote.data.gelisim

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import androidx.sqlite.db.SimpleSQLiteQuery
import com.keremtulu.questionnote.BuildConfig
import com.keremtulu.questionnote.data.ilerleme.RozetHizmeti
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.entity.*
import com.keremtulu.questionnote.data.prefs.RozetDeposu
import com.keremtulu.questionnote.data.prefs.TercihGunlukHedefDeposu
import com.keremtulu.questionnote.data.seviye.SeviyeHizmeti
import com.keremtulu.questionnote.domain.calisma.*
import com.keremtulu.questionnote.domain.gelisim.*
import com.keremtulu.questionnote.domain.model.*
import com.keremtulu.questionnote.domain.repeat.*
import java.io.File
import java.time.LocalDate
import java.util.Collections
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.math.ceil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode

/** Synthetic and test-only. Host/JVM query/aggregation timings are NOT Android frame or S24 FE measurements. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@SQLiteMode(SQLiteMode.Mode.NATIVE)
class AnalizPerformansTest {
    private data class Sorgu(val sql: String, val args: List<Any?>)
    @Test(timeout = 180_000) fun thousandQuestionsTwentyThousandReviewsHaveBatchedQueriesAndMeasuredReport() = runBlocking<Unit> {
        val context: Context = RuntimeEnvironment.getApplication()
        listOf("karne_rozetleri", "ilerleme_ayarlari").forEach { context.getSharedPreferences(it, 0).edit().clear().commit() }
        val queries = Collections.synchronizedList(mutableListOf<Sorgu>())
        val db = Room.inMemoryDatabaseBuilder(context, YanlisDefteriDatabase::class.java).allowMainThreadQueries()
            .setQueryCallback(object : RoomDatabase.QueryCallback {
                override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) { queries.add(Sorgu(sqlQuery, bindArgs.toList())) }
            }, Executor { it.run() }).build()
        val gun = LocalDate.of(2026, 10, 18).toEpochDay().toInt()
        val profile = SinavSecimi(SinavProfili.SERBEST)
        val hizmet = GelisimAnaliziHizmeti(db)
        val times = linkedMapOf<String, List<Double>>()
        fun ms(start: Long) = (System.nanoTime() - start) / 1_000_000.0
        try {
            val seedStart = System.nanoTime()
            val dersler = (0..3).map { DersEntity("lesson$it", "Test dersi $it", it, olusturma = 1, guncelleme = 1) }
            val konular = (0..39).map { KonuEntity("topic$it", "lesson${it % 4}", "Test konusu $it", it, olusturma = 1, guncelleme = 1) }
            val sorular = (0..999).map { SoruEntity("q$it", "lesson${it % 4}", konuId = "topic${it % 40}",
                kaynak = "Kitap ${it % 25}", sonTekrarGun = gun - 1, sonrakiTekrarGun = gun + it % 7, olusturma = 1, guncelleme = 1) }
            val repeats = ArrayList<TekrarEntity>(20_000)
            val measures = ArrayList<TekrarOlcumEntity>(20_000)
            sorular.forEachIndexed { i, q ->
                val history = mutableListOf<TekrarKaydi>()
                repeat(20) { n ->
                    val day = gun - 20 + n
                    val result = TekrarSonucu.entries[(i + n) % 3]
                    val opened = (i + n) % 5 == 0
                    val quality = when ((i + n) % 10) { 0 -> OlcumKalitesi.YOK; 1 -> OlcumKalitesi.KISMI; 2 -> OlcumKalitesi.GECERSIZ; else -> OlcumKalitesi.TAM }
                    val o = when (quality) {
                        OlcumKalitesi.YOK -> SureOlcumu()
                        OlcumKalitesi.GECERSIZ -> SureOlcumu(kalite = quality, nedenler = setOf(OlcumNedeni.GECERSIZ_GIRDI))
                        else -> SureOlcumu(30_000L + i % 17 * 1_000, if (opened) 45_000 else 0, quality,
                            if (quality == OlcumKalitesi.KISMI) setOf(OlcumNedeni.TAM_EKRAN) else emptySet())
                    }
                    val attempt = TekrarDenemesi("a-$i-$n", q.id, result, opened, o)
                    val reference = KisiselProfilHesabi.hesapla(emptyList(), ProfilBaglami(q.dersId), day)
                    val k = UyarlanabilirTekrarKurali.hesapla(history, attempt, reference, LocalDate.ofEpochDay(day.toLong()), day, MotorAyari())
                    val t = TekrarEntity("r-$i-$n", q.id, day * 86_400_000L + i, day, day, result.name, opened,
                        k.uygulanan.oncekiAralik, k.uygulanan.sonrakiAralik, 2)
                    val m = TekrarOlcumEntity(t.id, attempt.denemeId, null, o.denemeAktifMs, o.incelemeAktifMs,
                        o.kalite.name, OlcumSerilestirme.nedenlerYaz(o.nedenler), 1, q.dersId, "Test dersi ${i % 4}",
                        q.konuId, "Test konusu ${i % 40}", kaynakAnahtari(q.kaynak), q.kaynak,
                        OlcumSerilestirme.kararYaz(k, day, reference, "UTC"))
                    if (i == 0 || i == 999) OlcumSerilestirme.dogrula(m, t)
                    repeats += t; measures += m
                    history += TekrarKaydi(t.id, day, day, result, opened, t.oncekiAralik, t.sonrakiAralik, false, 2, t.zaman)
                }
            }
            db.withTransaction {
                db.dersDao().ekle(dersler); db.konuDao().ekle(konular); db.soruDao().ekle(sorular)
                db.tekrarDao().ekle(repeats); db.tekrarOlcumDao().ekle(measures)
            }
            times["Sentetik veri üretimi + transaction yükleme"] = listOf(ms(seedStart))
            assertEquals(1_000, db.soruDao().analizSorulari().size)
            assertEquals(20_000, db.tekrarOlcumDao().toplamKararSayisi())
            queries.clear()
            val firstStart = System.nanoTime()
            val first = hizmet.hesapla(gun, profile)
            times["İlk analiz çağrısı (DB kurulumu hariç)"] = listOf(ms(firstStart))
            assertEquals(20_000, first.toplam.tekrar); assertEquals(1_000, first.toplam.soru)
            assertEquals(1_000, first.plan.sumOf { it.adet })
            val appQueries = synchronized(queries) { queries.filter { it.sql.trimStart().startsWith("SELECT", true) &&
                !it.sql.contains("room_table_modification_log", true) }.toList() }
            assertEquals(appQueries.joinToString("\n"), 4, appQueries.size)
            assertTrue(appQueries.none { it.sql.contains("karar_json", true) || it.sql.contains("gorseller", true) })
            val warm = mutableListOf<Double>()
            repeat(5) {
                val start = System.nanoTime()
                assertEquals(first, hizmet.hesapla(gun, profile))
                warm += ms(start)
            }
            times["Isınmış toplu analiz (5 tekrar)"] = warm
            val profilStart = System.nanoTime()
            ProfilHizmeti(db).profil(ProfilBaglami("lesson0"), gun, null)
            times["Tek ders profili (ince projeksiyon)"] = listOf(ms(profilStart))
            val kapsamStart = System.nanoTime()
            assertEquals(20_000, ProfilHizmeti(db).kapsam(gun).toplamTekrar)
            times["Tüm ders süre kapsamı"] = listOf(ms(kapsamStart))
            val rozet = RozetDeposu(context)
            val hedef = TercihGunlukHedefDeposu(context)
            val xpStart = System.nanoTime()
            val summary = SeviyeHizmeti(db, rozet, hedef, RozetHizmeti(db, rozet, hedef)).ozet(LocalDate.ofEpochDay(gun.toLong()))
            assertEquals(20_000, summary.tekrarSayisi)
            times["Tam İlerleme özeti (XP + profil + analiz)"] = listOf(ms(xpStart))
            db.soruDao().filtreliCalismaSorulariniIzle(gun, null, false, null, null, false, "lesson0", true).first()
            val adayQueries = synchronized(queries) { queries.toList() }
            val secilen = listOf(
                "Toplu tarihsel analiz" to appQueries.single { it.sql.contains("LEFT JOIN tekrar_olcumleri") },
                "Ders/pencere profili" to adayQueries.first { it.sql.contains("m.ders_id =") },
                "Uygun çalışma + gerçek aynı-gün dışlama" to adayQueries.first { it.sql.contains("NOT EXISTS (SELECT 1 FROM tekrarlar") },
            )
            val plans = secilen.map { (ad, q) ->
                val lines = db.openHelper.readableDatabase.query(SimpleSQLiteQuery("EXPLAIN QUERY PLAN ${q.sql}", q.args.toTypedArray())).use { c ->
                    buildList { while (c.moveToNext()) add(c.getString(3)) }
                }
                ad to lines
            }
            assertTrue(plans.last().second.any { it.contains("index_tekrarlar_soru_id_gun") })
            val out = File("build/outputs/p6-benchmark/${BuildConfig.BUILD_TYPE}.md").apply { parentFile.mkdirs() }
            val payload = measures.sumOf { it.kararJson.toByteArray(Charsets.UTF_8).size.toLong() }
            fun fmt(n: Double) = String.format(Locale.ROOT, "%.2f", n)
            out.writeText(buildString {
                appendLine("# P6 sentetik analitik ölçümü · ${BuildConfig.BUILD_TYPE}")
                appendLine("\nKaynak: `${System.getenv("GITHUB_SHA") ?: "local"}`")
                appendLine("\nRobolectric 4.15.1 / SDK 35 / yerel SQLite / ${System.getProperty("java.vm.name")} ${System.getProperty("java.version")} / ${System.getProperty("os.name")} ${System.getProperty("os.arch")}")
                appendLine("\n1.000 soru, 20.000 tekrar ve bağlam/ölçüm; 4 ders, 40 konu, 100 ders/kaynak grubu, 20 gün; tüm sonuç/kalite türleri. Veri yalnız testte üretildi, Git'e yazılmadı.")
                appendLine("\nAnaliz başına **${appQueries.size} uygulama SELECT**; karar JSON'u taşınmadı (veritabanındaki toplam karar yükü: $payload bayt). Room iç invalidation sorguları bu sayıya dahil değildir.")
                appendLine("\n| İşlem | Örnek | medyan ms | p95 ms* | ham ms |\n|---|---:|---:|---:|---|")
                times.forEach { (ad, values) ->
                    val sorted = values.sorted()
                    appendLine("| $ad | ${values.size} | ${fmt(sorted[sorted.size / 2])} | ${fmt(sorted[(ceil(sorted.size * .95).toInt() - 1).coerceAtLeast(0)])} | ${values.joinToString(", ") { fmt(it) }} |")
                }
                appendLine("\n*Az sayıda host örneğinin nearest-rank yüzdeliği; kare/frame p95 değildir. İlk çağrı gerçek telefon soğuk açılışı değildir. GC/CI paylaşımı sonuçları etkileyebilir; bu sayılar cihaz performansı veya S24 FE kabulü diye yorumlanamaz.")
                plans.forEach { (ad, lines) -> appendLine("\n## $ad\n\n```text\n${lines.joinToString("\n")}\n```") }
                appendLine("\nPlan okuması: tüm-arşiv analizinde tarama beklenir; PK ile bağlam eşleşir. Ders/pencere ve aynı-gün kontrolünün mevcut indeksleri EXPLAIN çıktısında gösterilir. Yeni bir şema/indeks gereksinimi cihaz ölçümüyle yeniden değerlendirilecek.")
                appendLine("\nBekleyen fiziksel kapılar: S24 FE ve düşük güçlü cihazda frame-p95/jank, TalkBack, gerçek süreç ölümü, SAF/Drive ve düşük depolama. **Etkin v3 kapalı.**")
            })
            assertTrue(out.length() in 1..100_000)
        } finally { db.close() }
    }
}

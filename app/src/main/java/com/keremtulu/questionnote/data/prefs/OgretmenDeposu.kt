package com.keremtulu.questionnote.data.prefs

import android.content.Context
import androidx.room.withTransaction
import com.keremtulu.questionnote.data.images.DepolananGorselDosyasi
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabaseProvider
import com.keremtulu.questionnote.data.local.entity.OgretmenGrupEntity
import com.keremtulu.questionnote.data.local.entity.OgretmenSoruEntity
import com.keremtulu.questionnote.data.local.entity.OgretmenSoruGorselEntity
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.OgretmenDurum
import com.keremtulu.questionnote.domain.model.OgretmenGorseli
import com.keremtulu.questionnote.domain.model.OgretmenGrubu
import com.keremtulu.questionnote.domain.model.OgretmenSorusu
import com.keremtulu.questionnote.domain.model.OgretmenVerisi
import com.keremtulu.questionnote.domain.model.OgretmenZorluk
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import com.keremtulu.questionnote.domain.model.dersAnahtari
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Result of the one-time import of a pre-5 JSON workspace. */
sealed interface OgretmenAlanDurumu {
    data object Yukleniyor : OgretmenAlanDurumu

    data object Hazir : OgretmenAlanDurumu

    /** A pre-v5 JSON file was imported; the old file is kept as `*.ice-aktarildi.json`. */
    data class IceAktarildi(val atlananKayit: Int) : OgretmenAlanDurumu

    /** A pre-v5 JSON file could not be imported. Room is still usable, the file is quarantined. */
    data class Aktarilamadi(val karantinaAdi: String?, val atlananKayit: Int) : OgretmenAlanDurumu
}

data class OgretmenDepolamaOzeti(
    val dosyaBayt: Long = 0,
    val disktedekiDosya: Int = 0,
    val disktekiBayt: Long = 0,
    val kullanilmayanDosyalar: List<String> = emptyList(),
    val kullanilmayanBayt: Long = 0,
    val sonYedekZamani: Long? = null,
)

/**
 * Teacher workspace store (Room schema v5).
 *
 * - Lessons/topics are the shared student catalog (`dersler` / `konular`); only ids are stored here.
 * - A v1/v2 JSON file from 1.5.x is imported once, tolerantly, and then kept as an archive copy.
 * - All rows of one user action are written in a single transaction; the in-memory snapshot is
 *   refreshed from the database right after, so reads never depend on a stale cache.
 */
class OgretmenDeposu(
    context: Context,
    private val gorselDeposu: LocalSoruGorselDeposu,
    database: YanlisDefteriDatabase? = null,
) {
    private val appContext = context.applicationContext
    private val db: YanlisDefteriDatabase = database ?: YanlisDefteriDatabaseProvider.getInstance(appContext)
    private val dao get() = db.ogretmenDao()

    private val eskiJsonDosyasi = File(appContext.filesDir, ESKI_JSON_ADI)
    private val mutex = Mutex()
    private val baslatmaMutex = Mutex()

    private val state = MutableStateFlow(OgretmenVerisi())
    val veriIzle: StateFlow<OgretmenVerisi> = state.asStateFlow()

    private val durumState = MutableStateFlow<OgretmenAlanDurumu>(OgretmenAlanDurumu.Yukleniyor)
    val durumIzle: StateFlow<OgretmenAlanDurumu> = durumState.asStateFlow()

    @Volatile
    private var hazir = false

    fun anlikVeri(): OgretmenVerisi = state.value

    fun anlikDurum(): OgretmenAlanDurumu = durumState.value

    /** Loads the workspace once (and imports a legacy JSON file if present). Safe to call repeatedly. */
    suspend fun baslat() {
        if (hazir) return
        baslatmaMutex.withLock {
            if (hazir) return
            withContext(Dispatchers.IO) {
                if (dao.grupSayisi() == 0 && dao.soruSayisi() == 0) iceriAktarEskiJson()
                durumState.value = OgretmenAlanDurumu.Hazir
                yenile()
            }
            hazir = true
        }
    }

    // ---------------------------------------------------------------- groups

    suspend fun grupEkle(
        ad: String,
        aciklama: String? = null,
        dersId: String? = null,
    ): OgretmenGrubu? {
        baslat()
        return mutex.withLock {
            val temizAd = ad.trim()
            val temizAciklama = aciklama?.trim()?.takeIf { it.isNotEmpty() }
            if (temizAd.isEmpty() || temizAd.length > MAX_GRUP_ADI ||
                (temizAciklama != null && temizAciklama.length > MAX_ACIKLAMA)
            ) return@withLock null
            val anahtar = temizAd.dersAnahtari()
            val mevcut = state.value
            if (mevcut.gruplar.any { it.ad.dersAnahtari() == anahtar }) return@withLock null
            val yeni = OgretmenGrubu(
                id = UUID.randomUUID().toString(),
                ad = temizAd,
                aciklama = temizAciklama,
                dersId = dersId?.takeIf { it.isNotBlank() },
                sira = (mevcut.gruplar.maxOfOrNull { it.sira } ?: -1) + 1,
                olusturma = System.currentTimeMillis(),
            )
            val yazildi = runCatching { dao.grupEkle(yeni.toEntity()) }.isSuccess
            if (!yazildi) return@withLock null
            yenile()
            yeni
        }
    }

    suspend fun grupGuncelle(
        grupId: String,
        yeniAd: String,
        yeniAciklama: String? = null,
        yeniDersId: String? = null,
    ): Boolean {
        baslat()
        return mutex.withLock {
            val temizAd = yeniAd.trim()
            val temizAciklama = yeniAciklama?.trim()?.takeIf { it.isNotEmpty() }
            if (temizAd.isEmpty() || temizAd.length > MAX_GRUP_ADI ||
                (temizAciklama != null && temizAciklama.length > MAX_ACIKLAMA)
            ) return@withLock false
            val mevcut = state.value
            val hedef = mevcut.gruplar.firstOrNull { it.id == grupId } ?: return@withLock false
            val anahtar = temizAd.dersAnahtari()
            if (mevcut.gruplar.any { it.id != grupId && it.ad.dersAnahtari() == anahtar }) return@withLock false
            val guncel = hedef.copy(
                ad = temizAd,
                aciklama = temizAciklama,
                dersId = yeniDersId?.takeIf { it.isNotBlank() },
            )
            val yazildi = runCatching { dao.grupGuncelle(guncel.toEntity()) }.getOrDefault(0) > 0
            if (!yazildi) return@withLock false
            yenile()
            true
        }
    }

    suspend fun grupArsivle(grupId: String, arsivli: Boolean): Boolean {
        baslat()
        return mutex.withLock {
            val hedef = state.value.gruplar.firstOrNull { it.id == grupId } ?: return@withLock false
            val yazildi = runCatching { dao.grupGuncelle(hedef.copy(arsivli = arsivli).toEntity()) }
                .getOrDefault(0) > 0
            if (!yazildi) return@withLock false
            yenile()
            true
        }
    }

    /** Questions and images follow the foreign key (`ON DELETE CASCADE`). */
    suspend fun grupSil(grupId: String): Boolean {
        baslat()
        return mutex.withLock {
            val mevcut = state.value
            if (mevcut.gruplar.none { it.id == grupId }) return@withLock false
            val silinenDosyalar = mevcut.sorular
                .filter { it.grupId == grupId }
                .flatMap { soru -> soru.gorseller.map { it.dosyaAdi } }
            val yazildi = db.withTransaction {
                val silinen = dao.grupSil(grupId) > 0
                if (silinen) {
                    dao.gruplariGetir().forEachIndexed { idx, entity ->
                        if (entity.sira != idx) dao.grupGuncelle(entity.copy(sira = idx))
                    }
                }
                silinen
            }
            if (!yazildi) return@withLock false
            yenile()
            temizleKullanilmayanDosyalar(silinenDosyalar)
            true
        }
    }

    // ------------------------------------------------------------- questions

    suspend fun soruKaydet(
        id: String? = null,
        grupId: String,
        baslik: String?,
        zorluk: OgretmenZorluk?,
        cevap: String?,
        gorseller: List<SoruGorselGirdisi>,
        dersId: String? = null,
        konuId: String? = null,
        kaynak: String? = null,
        kimSordu: String? = null,
        planGun: Long? = null,
        cozumGorselleri: List<SoruGorselGirdisi> = emptyList(),
    ): OgretmenSorusu? {
        baslat()
        return mutex.withLock {
            val soruGirdileri = gorseller.map { it.copy(tur = GorselTuru.SORU) }
            val cozumGirdileri = cozumGorselleri.map { it.copy(tur = GorselTuru.COZUM) }
            val tumGirdiler = soruGirdileri + cozumGirdileri
            if (soruGirdileri.isEmpty() || tumGirdiler.size > MAX_GORSEL_ADEDI ||
                tumGirdiler.distinctBy { it.id }.size != tumGirdiler.size
            ) return@withLock null
            val temizBaslik = baslik?.trim()?.takeIf { it.isNotEmpty() }
            val temizCevap = cevap?.trim()?.takeIf { it.isNotEmpty() }
            val temizDers = dersId?.trim()?.takeIf { it.isNotEmpty() }
            val temizKonu = konuId?.trim()?.takeIf { it.isNotEmpty() }
            val temizKaynak = kaynak?.trim()?.takeIf { it.isNotEmpty() }
            val temizKimSordu = kimSordu?.trim()?.takeIf { it.isNotEmpty() }
            if (listOf(temizBaslik, temizCevap, temizKaynak, temizKimSordu).any { (it?.length ?: 0) > MAX_METIN }) {
                return@withLock null
            }
            if ((temizDers?.length ?: 0) > MAX_KIMLIK || (temizKonu?.length ?: 0) > MAX_KIMLIK) {
                return@withLock null
            }

            val mevcut = state.value
            if (mevcut.gruplar.none { it.id == grupId }) return@withLock null
            val eskiSoru = id?.let { aranan -> mevcut.sorular.firstOrNull { it.id == aranan } }
            if (id != null && eskiSoru == null) return@withLock null
            val eskiGorselMap = eskiSoru?.gorseller.orEmpty().associateBy { it.id }

            for (secim in tumGirdiler) {
                if (secim.gecici) {
                    if (!gorselDeposu.geciciDosyayiDogrula(secim)) return@withLock null
                } else {
                    val eski = eskiGorselMap[secim.id] ?: return@withLock null
                    if (eski.dosyaAdi != secim.dosyaAdi || eski.sha256 != secim.sha256 ||
                        !gorselDeposu.depolananDosyayiDogrula(secim)
                    ) return@withLock null
                }
            }

            val yeniKaliciDosyalar = mutableMapOf<String, String>()
            var yazildi = false
            try {
                for (gorsel in tumGirdiler.filter { it.gecici }) {
                    val kaliciAd = gorselDeposu.ogretmenKaliciyaKopyala(gorsel.dosyaAdi)
                        ?: return@withLock null
                    yeniKaliciDosyalar[gorsel.id] = kaliciAd
                }
                val now = System.currentTimeMillis()
                val kaliciGorseller = tumGirdiler.mapIndexed { idx, g ->
                    OgretmenGorseli(
                        id = g.id,
                        sira = idx,
                        dosyaAdi = yeniKaliciDosyalar[g.id] ?: g.dosyaAdi,
                        tur = g.tur,
                        mimeType = g.mimeType,
                        genislik = g.genislik,
                        yukseklik = g.yukseklik,
                        boyut = g.boyut,
                        sha256 = g.sha256,
                    )
                }
                val kaydedilen = OgretmenSorusu(
                    id = eskiSoru?.id ?: UUID.randomUUID().toString(),
                    grupId = grupId,
                    baslik = temizBaslik,
                    dersId = temizDers,
                    konuId = temizKonu,
                    kaynak = temizKaynak,
                    kimSordu = temizKimSordu,
                    planGun = planGun,
                    zorluk = zorluk,
                    cevap = temizCevap,
                    durum = eskiSoru?.durum ?: OgretmenDurum.BEKLIYOR,
                    cozulme = eskiSoru?.cozulme,
                    gorseller = kaliciGorseller,
                    olusturma = eskiSoru?.olusturma ?: now,
                    guncelleme = now,
                )
                yazildi = runCatching {
                    db.withTransaction {
                        val entity = kaydedilen.toEntity()
                        if (eskiSoru == null) dao.soruEkle(entity) else dao.soruGuncelle(entity)
                        dao.soruGorselleriniSil(kaydedilen.id)
                        if (kaliciGorseller.isNotEmpty()) {
                            dao.gorselEkle(kaliciGorseller.map { it.toEntity(kaydedilen.id) })
                        }
                    }
                    true
                }.getOrDefault(false)
                if (!yazildi) return@withLock null
                yenile()
                temizleKullanilmayanDosyalar(eskiSoru?.gorseller.orEmpty().map { it.dosyaAdi })
                tumGirdiler.filter { it.gecici }
                    .forEach { runCatching { gorselDeposu.geciciDosyayiSil(it.dosyaAdi) } }
                kaydedilen
            } finally {
                if (!yazildi) {
                    yeniKaliciDosyalar.values.forEach {
                        runCatching { gorselDeposu.ogretmenKaliciDosyayiSil(it) }
                    }
                }
            }
        }
    }

    suspend fun durumDegistir(soruId: String, yeniDurum: OgretmenDurum): Boolean {
        baslat()
        return topluDurumDegistir(setOf(soruId), yeniDurum) > 0
    }

    /** Backwards-compatible wrapper used by existing screens and tests. */
    suspend fun cozulduDurumuDegistir(soruId: String, cozuldu: Boolean): Boolean =
        durumDegistir(
            soruId,
            if (cozuldu) OgretmenDurum.COZULDU else OgretmenDurum.BEKLIYOR,
        )

    /** One statement for a whole multi-select action; returns how many rows actually changed. */
    suspend fun topluDurumDegistir(soruIdleri: Set<String>, yeniDurum: OgretmenDurum): Int {
        baslat()
        if (soruIdleri.isEmpty()) return 0
        return mutex.withLock {
            val mevcut = state.value
            val hedefler = mevcut.sorular
                .filter { it.id in soruIdleri && it.durum != yeniDurum }
                .map { it.id }
            if (hedefler.isEmpty()) return@withLock 0
            val now = System.currentTimeMillis()
            val cozulme = if (yeniDurum == OgretmenDurum.BEKLIYOR) null else now
            val degisen = runCatching {
                dao.topluDurumGuncelle(hedefler, yeniDurum.name, cozulme, now)
            }.getOrDefault(0)
            if (degisen == 0) return@withLock 0
            yenile()
            degisen
        }
    }

    suspend fun cozulmusleriArsivle(grupId: String? = null): Int {
        baslat()
        val hedefler = state.value.sorular
            .filter { it.durum == OgretmenDurum.COZULDU && (grupId == null || it.grupId == grupId) }
            .mapTo(mutableSetOf()) { it.id }
        return topluDurumDegistir(hedefler, OgretmenDurum.ARSIV)
    }

    suspend fun baskaGruplaraKopyala(soruId: String, hedefGrupIdleri: Set<String>): Int {
        baslat()
        if (hedefGrupIdleri.isEmpty()) return 0
        return mutex.withLock {
            val mevcut = state.value
            val kaynak = mevcut.sorular.firstOrNull { it.id == soruId } ?: return@withLock 0
            val gecerliGruplar = mevcut.gruplar.map { it.id }.toSet()
            val hedefler = hedefGrupIdleri.filter { it in gecerliGruplar && it != kaynak.grupId }
            if (hedefler.isEmpty()) return@withLock 0
            val now = System.currentTimeMillis()
            val kopyalar = hedefler.map { hedefGrupId ->
                OgretmenSorusu(
                    id = UUID.randomUUID().toString(),
                    grupId = hedefGrupId,
                    baslik = kaynak.baslik,
                    dersId = kaynak.dersId,
                    konuId = kaynak.konuId,
                    kaynak = kaynak.kaynak,
                    kimSordu = kaynak.kimSordu,
                    planGun = kaynak.planGun,
                    zorluk = kaynak.zorluk,
                    cevap = kaynak.cevap,
                    durum = OgretmenDurum.BEKLIYOR,
                    gorseller = kaynak.gorseller.mapIndexed { idx, g ->
                        g.copy(id = UUID.randomUUID().toString(), sira = idx)
                    },
                    olusturma = now,
                    guncelleme = now,
                )
            }
            val yazildi = runCatching {
                db.withTransaction {
                    kopyalar.forEach { kopya ->
                        dao.soruEkle(kopya.toEntity())
                        if (kopya.gorseller.isNotEmpty()) {
                            dao.gorselEkle(kopya.gorseller.map { it.toEntity(kopya.id) })
                        }
                    }
                }
                true
            }.getOrDefault(false)
            if (!yazildi) return@withLock 0
            yenile()
            kopyalar.size
        }
    }

    /** Move keeps creation time, solved state, images and tags; the source row is removed. */
    suspend fun soruTasi(soruId: String, hedefGrupId: String): Boolean {
        baslat()
        return mutex.withLock {
            val mevcut = state.value
            val kaynak = mevcut.sorular.firstOrNull { it.id == soruId } ?: return@withLock false
            if (kaynak.grupId == hedefGrupId) return@withLock false
            if (mevcut.gruplar.none { it.id == hedefGrupId }) return@withLock false
            val degisen = runCatching {
                dao.soruTasi(soruId, hedefGrupId, System.currentTimeMillis())
            }.getOrDefault(0)
            if (degisen == 0) return@withLock false
            yenile()
            true
        }
    }

    suspend fun soruSil(soruId: String): Boolean = topluSil(setOf(soruId)) > 0

    /**
     * Deletes rows (images cascade). Files are kept when [dosyalariKoru] is true so the caller can
     * offer a real undo for a few seconds; kept files are only ever removed through the explicit
     * storage-maintenance screen, which also requires a 7 day age and a user confirmation.
     */
    suspend fun topluSil(soruIdleri: Set<String>, dosyalariKoru: Boolean = false): Int {
        baslat()
        if (soruIdleri.isEmpty()) return 0
        return mutex.withLock {
            val mevcut = state.value
            val silinecek = mevcut.sorular.filter { it.id in soruIdleri }
            if (silinecek.isEmpty()) return@withLock 0
            val silinen = runCatching { dao.sorulariSil(soruIdleri.toList()) }.getOrDefault(0)
            if (silinen == 0) return@withLock 0
            yenile()
            if (!dosyalariKoru) {
                temizleKullanilmayanDosyalar(
                    silinecek.flatMap { soru -> soru.gorseller.map { it.dosyaAdi } },
                )
            }
            silinen
        }
    }

    /**
     * Undo for [topluSil]. Re-inserts the exact rows, so ids, creation time and image order survive;
     * only questions whose group still exists and whose files are still on disk come back.
     */
    suspend fun silmeyiGeriAl(sorular: List<OgretmenSorusu>): Int {
        baslat()
        if (sorular.isEmpty()) return 0
        return mutex.withLock {
            val mevcut = state.value
            val grupIdleri = mevcut.gruplar.mapTo(mutableSetOf()) { it.id }
            val varOlanIdler = mevcut.sorular.mapTo(mutableSetOf()) { it.id }
            val geriGelecek = sorular.filter { soru ->
                soru.grupId in grupIdleri && soru.id !in varOlanIdler && soru.soruGorselleri().isNotEmpty()
            }
            if (geriGelecek.isEmpty()) return@withLock 0
            val yazildi = runCatching {
                db.withTransaction {
                    geriGelecek.forEach { soru ->
                        dao.soruEkle(soru.toEntity())
                        if (soru.gorseller.isNotEmpty()) {
                            dao.gorselEkle(soru.gorseller.map { it.toEntity(soru.id) })
                        }
                    }
                }
                true
            }.getOrDefault(false)
            if (!yazildi) return@withLock 0
            yenile()
            geriGelecek.size
        }
    }

    /** Explicit recovery path (restore): replaces every teacher row atomically. */
    suspend fun geriYukle(yeniVeri: OgretmenVerisi) {
        baslat()
        mutex.withLock {
            val yazildi = runCatching {
                db.withTransaction {
                    dao.tumGorselleriSil()
                    dao.tumSorulariSil()
                    dao.tumGruplariSil()
                    yeniVeri.gruplar.forEach { dao.grupEkle(it.toEntity()) }
                    yeniVeri.sorular.forEach { soru ->
                        dao.soruEkle(soru.toEntity())
                        if (soru.gorseller.isNotEmpty()) {
                            dao.gorselEkle(soru.gorseller.map { it.toEntity(soru.id) })
                        }
                    }
                }
                true
            }.getOrDefault(false)
            if (yazildi) {
                durumState.value = OgretmenAlanDurumu.Hazir
                yenile()
            }
        }
    }

    // ------------------------------------------------------- maintenance

    suspend fun depolamaOzeti(sonYedekZamani: Long? = null): OgretmenDepolamaOzeti {
        baslat()
        val kullanilan = state.value.kaliciDosyaAdlari()
        val dosyalar = gorselDeposu.ogretmenDosyaBilgileri()
        val kullanilmayan = dosyalar.filter { it.dosyaAdi !in kullanilan }
        return OgretmenDepolamaOzeti(
            dosyaBayt = eskiJsonDosyasi.takeIf { it.isFile }?.length() ?: 0L,
            disktedekiDosya = dosyalar.size,
            disktekiBayt = dosyalar.sumOf { it.boyut },
            kullanilmayanDosyalar = kullanilmayan.take(MAX_BAKIM_LISTESI).map { it.dosyaAdi },
            kullanilmayanBayt = kullanilmayan.sumOf { it.boyut },
            sonYedekZamani = sonYedekZamani,
        )
    }

    /**
     * Deletes only names the caller explicitly approved, after re-checking live references and file
     * age. Never touches `img/`, so the student archive stays out of scope.
     */
    suspend fun onaylananYetimleriSil(dosyaAdlari: Set<String>, simdi: Long = System.currentTimeMillis()): Int {
        if (dosyaAdlari.isEmpty()) return 0
        val dosyalar: Map<String, DepolananGorselDosyasi> =
            gorselDeposu.ogretmenDosyaBilgileri().associateBy { it.dosyaAdi }
        var silinen = 0
        for (ad in dosyaAdlari) {
            val dosya = dosyalar[ad] ?: continue
            if (simdi - dosya.sonDegisme < YETIM_MIN_YASI_MS) continue
            if (ad in state.value.kaliciDosyaAdlari()) continue
            if (gorselDeposu.depolananDosyayiSil(ad)) silinen++
        }
        return silinen
    }

    private suspend fun temizleKullanilmayanDosyalar(adayAdlar: List<String>) {
        val kullanilan = state.value.kaliciDosyaAdlari()
        adayAdlar.distinct()
            // Never touch `img/`: a file shared with the student archive is only removed by the
            // student-side maintenance path, which knows every reference.
            .filter { it.startsWith(OGRETMEN_DIZIN_ONEKI) }
            .filterNot { it in kullanilan }
            .forEach { runCatching { gorselDeposu.depolananDosyayiSil(it) } }
    }

    // ---------------------------------------------------- room <-> model

    private suspend fun yenile() {
        val gruplar = dao.gruplariGetir()
        val sorular = dao.sorulariGetir()
        val gorseller = dao.gorselleriGetir().groupBy { it.soruId }
        state.value = OgretmenVerisi(
            gruplar = gruplar.map { it.toModel() },
            sorular = sorular.map { entity ->
                entity.toModel(gorseller[entity.id].orEmpty().map { it.toModel() })
            },
        )
    }

    private suspend fun iceriAktarEskiJson() {
        if (!eskiJsonDosyasi.isFile) return
        val okuma = runCatching {
            val json = JSONObject(eskiJsonDosyasi.readText(Charsets.UTF_8))
            jsonOkuToleransli(json)
        }
        if (okuma.isFailure) {
            val karantina = runCatching {
                val ad = "ogretmen_calisma_alani.bozuk-${System.currentTimeMillis()}.json"
                Files.move(eskiJsonDosyasi.toPath(), File(appContext.filesDir, ad).toPath(), REPLACE_EXISTING)
                ad
            }.getOrNull()
            durumState.value = OgretmenAlanDurumu.Aktarilamadi(karantina, 0)
            return
        }
        val veri = okuma.getOrThrow()
        var atlanan = veri.atlanan
        // Lessons/topics must exist in the shared catalog, otherwise the FK would reject the row.
        val dersIdleri = runCatching { db.dersDao().tumunuGetir().map { it.id }.toSet() }.getOrDefault(emptySet())
        val konuIdleri = runCatching { db.konuDao().tumunuGetir().map { it.id }.toSet() }.getOrDefault(emptySet())
        val grupIdleri = veri.veri.gruplar.map { it.id }.toSet()
        val aktarilabilir = OgretmenVerisi(
            gruplar = veri.veri.gruplar.map {
                it.copy(dersId = it.dersId?.takeIf { ders -> ders in dersIdleri })
            },
            sorular = veri.veri.sorular
                .filter { soru ->
                    val uygun = soru.grupId in grupIdleri && soru.soruGorselleri().isNotEmpty()
                    if (!uygun) atlanan++
                    uygun
                }
                .map {
                    it.copy(
                        dersId = it.dersId?.takeIf { ders -> ders in dersIdleri },
                        konuId = it.konuId?.takeIf { konu -> konu in konuIdleri },
                    )
                },
        )
        val yazildi = runCatching {
            db.withTransaction {
                dao.tumGorselleriSil()
                dao.tumSorulariSil()
                dao.tumGruplariSil()
                aktarilabilir.gruplar.forEach { dao.grupEkle(it.toEntity()) }
                aktarilabilir.sorular.forEach { soru ->
                    dao.soruEkle(soru.toEntity())
                    if (soru.gorseller.isNotEmpty()) {
                        dao.gorselEkle(soru.gorseller.map { it.toEntity(soru.id) })
                    }
                }
            }
            true
        }.getOrDefault(false)
        if (!yazildi) {
            durumState.value = OgretmenAlanDurumu.Aktarilamadi(null, atlanan)
            return
        }
        val arsiv = File(appContext.filesDir, ESKI_JSON_ADI.removeSuffix(".json") + ".ice-aktarildi.json")
        runCatching { Files.move(eskiJsonDosyasi.toPath(), arsiv.toPath(), REPLACE_EXISTING) }
        durumState.value = OgretmenAlanDurumu.IceAktarildi(atlanan)
    }

    companion object {
        /** Serialization version of the backup/legacy JSON payload (Room schema is separate). */
        const val SURUM = 2

        private const val ESKI_JSON_ADI = "ogretmen_calisma_alani.json"
        private const val MAX_GRUP_ADI = 120
        private const val MAX_ACIKLAMA = 240
        private const val MAX_METIN = 50_000
        private const val MAX_KIMLIK = 120
        private const val MAX_GORSEL_ADEDI = 20
        private const val OGRETMEN_DIZIN_ONEKI = "ogretmen-img/"
        private const val MAX_BAKIM_LISTESI = 200
        private const val YETIM_MIN_YASI_MS = 7L * 24L * 60L * 60L * 1000L

        fun jsonYaz(veri: OgretmenVerisi): JSONObject = JSONObject()
            .put("schema_version", SURUM)
            .put(
                "groups",
                JSONArray().apply {
                    veri.gruplar.forEach { g ->
                        put(
                            JSONObject()
                                .put("id", g.id)
                                .put("name", g.ad)
                                .put("description", g.aciklama ?: JSONObject.NULL)
                                .put("lesson_id", g.dersId ?: JSONObject.NULL)
                                .put("archived", g.arsivli)
                                .put("order", g.sira)
                                .put("created_at", g.olusturma),
                        )
                    }
                },
            )
            .put(
                "questions",
                JSONArray().apply {
                    veri.sorular.forEach { s ->
                        put(
                            JSONObject()
                                .put("id", s.id)
                                .put("group_id", s.grupId)
                                .put("title", s.baslik ?: JSONObject.NULL)
                                .put("lesson_id", s.dersId ?: JSONObject.NULL)
                                .put("topic_id", s.konuId ?: JSONObject.NULL)
                                .put("source", s.kaynak ?: JSONObject.NULL)
                                .put("asked_by", s.kimSordu ?: JSONObject.NULL)
                                .put("due_day", s.planGun ?: JSONObject.NULL)
                                .put("difficulty", s.zorluk?.name ?: JSONObject.NULL)
                                .put("answer", s.cevap ?: JSONObject.NULL)
                                .put("status", s.durum.name)
                                // v1 readers still understand `solved`; keep it in sync for compatibility.
                                .put("solved", s.cozuldu)
                                .put("solved_at", s.cozulme ?: JSONObject.NULL)
                                .put("created_at", s.olusturma)
                                .put("updated_at", s.guncelleme)
                                .put(
                                    "images",
                                    JSONArray().apply {
                                        s.gorseller.forEach { img ->
                                            put(
                                                JSONObject()
                                                    .put("id", img.id)
                                                    .put("order", img.sira)
                                                    .put("kind", img.tur.name)
                                                    .put("file_name", img.dosyaAdi)
                                                    .put("zip_file_name", zipAdi(img.dosyaAdi))
                                                    .put("mime_type", img.mimeType)
                                                    .put("width", img.genislik)
                                                    .put("height", img.yukseklik)
                                                    .put("size_bytes", img.boyut)
                                                    .put("sha256", img.sha256),
                                            )
                                        }
                                    },
                                ),
                        )
                    }
                },
            )

        /**
         * Strict on the file shape (a missing group id or image list means a corrupt file), tolerant per
         * record: one broken question is skipped and counted instead of destroying the workspace.
         */
        fun jsonOkuToleransli(json: JSONObject): EskiJsonOkuma {
            val surum = json.optInt("schema_version", 1)
            if (surum > SURUM) throw IOException("Bilinmeyen öğretmen çalışma alanı sürümü: $surum")
            val groupsJson = json.optJSONArray("groups") ?: JSONArray()
            val gruplar = ArrayList<OgretmenGrubu>(groupsJson.length())
            var atlanan = 0
            for (i in 0 until groupsJson.length()) {
                val kayit = runCatching {
                    val obj = groupsJson.getJSONObject(i)
                    OgretmenGrubu(
                        id = obj.getString("id"),
                        ad = obj.getString("name"),
                        aciklama = obj.optNullMetin("description"),
                        dersId = obj.optNullMetin("lesson_id"),
                        arsivli = obj.optBoolean("archived", false),
                        sira = obj.optInt("order", i),
                        olusturma = obj.optLong("created_at", 0L),
                    )
                }
                if (kayit.isSuccess) gruplar += kayit.getOrThrow() else atlanan++
            }
            val questionsJson = json.optJSONArray("questions") ?: JSONArray()
            val sorular = ArrayList<OgretmenSorusu>(questionsJson.length())
            for (i in 0 until questionsJson.length()) {
                val kayit = runCatching { questionsJson.getJSONObject(i).toSoru() }
                if (kayit.isSuccess) sorular += kayit.getOrThrow() else atlanan++
            }
            return EskiJsonOkuma(OgretmenVerisi(gruplar = gruplar, sorular = sorular), atlanan)
        }

        /** Strict read used by ZIP restore: any unreadable record fails the whole backup. */
        fun jsonOku(json: JSONObject): OgretmenVerisi {
            val okuma = jsonOkuToleransli(json)
            if (okuma.atlanan > 0) {
                throw IOException("Öğretmen çalışma alanında okunamayan kayıt var: ${okuma.atlanan}")
            }
            return okuma.veri
        }

        /** Teacher images live in `ogretmen-img/` (or a shared `img/`) and map into the ZIP `images/` folder. */
        fun zipAdi(kaliciDosyaAdi: String): String =
            "images/${kaliciDosyaAdi.substringAfterLast('/')}"

        private fun JSONObject.toSoru(): OgretmenSorusu {
            val imagesJson = getJSONArray("images")
            val gorseller = ArrayList<OgretmenGorseli>(imagesJson.length())
            for (j in 0 until imagesJson.length()) {
                val img = imagesJson.getJSONObject(j)
                gorseller += OgretmenGorseli(
                    id = img.getString("id"),
                    sira = img.optInt("order", j),
                    dosyaAdi = img.getString("file_name"),
                    tur = GorselTuru.entries.firstOrNull { it.name == img.optString("kind", "SORU") }
                        ?: GorselTuru.SORU,
                    mimeType = img.optString("mime_type", "image/jpeg"),
                    genislik = img.getInt("width"),
                    yukseklik = img.getInt("height"),
                    boyut = img.getLong("size_bytes"),
                    sha256 = img.getString("sha256").lowercase(),
                )
            }
            val zorlukMetni = optNullMetin("difficulty")
            val zorluk = zorlukMetni?.let { d ->
                OgretmenZorluk.entries.firstOrNull { it.name == d }
                    ?: throw IOException("Bilinmeyen öğretmen zorluk düzeyi: $d")
            }
            val durum = optNullMetin("status")?.let { OgretmenDurum.anahtarla(it) }
                ?: if (optBoolean("solved", false)) OgretmenDurum.COZULDU else OgretmenDurum.BEKLIYOR
            return OgretmenSorusu(
                id = getString("id"),
                grupId = getString("group_id"),
                baslik = optNullMetin("title"),
                dersId = optNullMetin("lesson_id"),
                konuId = optNullMetin("topic_id"),
                kaynak = optNullMetin("source"),
                kimSordu = optNullMetin("asked_by"),
                planGun = if (has("due_day") && !isNull("due_day")) getLong("due_day") else null,
                zorluk = zorluk,
                cevap = optNullMetin("answer"),
                durum = durum,
                cozulme = if (has("solved_at") && !isNull("solved_at")) getLong("solved_at") else null,
                gorseller = gorseller,
                olusturma = optLong("created_at", 0L),
                guncelleme = optLong("updated_at", 0L),
            )
        }

        private fun JSONObject.optNullMetin(key: String): String? =
            if (!has(key) || isNull(key)) null else getString(key).trim().takeIf { it.isNotEmpty() }
    }
}

/** Outcome of a tolerant legacy JSON read. */
data class EskiJsonOkuma(
    val veri: OgretmenVerisi,
    val atlanan: Int,
)

private fun OgretmenGrubu.toEntity() = OgretmenGrupEntity(
    id = id,
    ad = ad,
    aciklama = aciklama,
    dersId = dersId,
    arsivli = arsivli,
    sira = sira,
    olusturma = olusturma,
)

private fun OgretmenGrupEntity.toModel() = OgretmenGrubu(
    id = id,
    ad = ad,
    aciklama = aciklama,
    dersId = dersId,
    arsivli = arsivli,
    sira = sira,
    olusturma = olusturma,
)

private fun OgretmenSorusu.toEntity() = OgretmenSoruEntity(
    id = id,
    grupId = grupId,
    baslik = baslik,
    dersId = dersId,
    konuId = konuId,
    kaynak = kaynak,
    kimSordu = kimSordu,
    planGun = planGun,
    zorluk = zorluk?.name,
    cevap = cevap,
    durum = durum.name,
    cozulme = cozulme,
    olusturma = olusturma,
    guncelleme = guncelleme,
)

private fun OgretmenSoruEntity.toModel(gorseller: List<OgretmenGorseli>) = OgretmenSorusu(
    id = id,
    grupId = grupId,
    baslik = baslik,
    dersId = dersId,
    konuId = konuId,
    kaynak = kaynak,
    kimSordu = kimSordu,
    planGun = planGun,
    zorluk = zorluk?.let { deger -> OgretmenZorluk.entries.firstOrNull { it.name == deger } },
    cevap = cevap,
    durum = OgretmenDurum.anahtarla(durum),
    cozulme = cozulme,
    gorseller = gorseller,
    olusturma = olusturma,
    guncelleme = guncelleme,
)

private fun OgretmenGorseli.toEntity(soruId: String) = OgretmenSoruGorselEntity(
    id = id,
    soruId = soruId,
    sira = sira,
    tur = tur.name,
    dosyaAdi = dosyaAdi,
    mimeType = mimeType,
    genislik = genislik,
    yukseklik = yukseklik,
    boyut = boyut,
    sha256 = sha256,
)

private fun OgretmenSoruGorselEntity.toModel() = OgretmenGorseli(
    id = id,
    sira = sira,
    dosyaAdi = dosyaAdi,
    tur = GorselTuru.entries.firstOrNull { it.name == tur } ?: GorselTuru.SORU,
    mimeType = mimeType,
    genislik = genislik,
    yukseklik = yukseklik,
    boyut = boyut,
    sha256 = sha256,
)

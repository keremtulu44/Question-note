package com.keremtulu.questionnote.data.prefs

import android.content.Context
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.domain.model.OgretmenGorseli
import com.keremtulu.questionnote.domain.model.OgretmenGrubu
import com.keremtulu.questionnote.domain.model.OgretmenSorusu
import com.keremtulu.questionnote.domain.model.OgretmenVerisi
import com.keremtulu.questionnote.domain.model.OgretmenZorluk
import com.keremtulu.questionnote.domain.model.SoruGorselGirdisi
import com.keremtulu.questionnote.domain.model.dersAnahtari
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
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

class OgretmenDeposu(
    context: Context,
    private val gorselDeposu: LocalSoruGorselDeposu,
) {
    private val appContext = context.applicationContext
    private val storageFile = File(appContext.filesDir, DOSYA_ADI)
    private val mutex = Mutex()
    private val state = MutableStateFlow(baslangicOku())
    val veriIzle: StateFlow<OgretmenVerisi> = state.asStateFlow()

    fun anlikVeri(): OgretmenVerisi = state.value

    private fun baslangicOku(): OgretmenVerisi = runCatching {
        if (!storageFile.isFile) OgretmenVerisi()
        else jsonOku(JSONObject(storageFile.readText(Charsets.UTF_8)))
    }.getOrDefault(OgretmenVerisi())

    suspend fun grupEkle(ad: String, aciklama: String? = null): OgretmenGrubu? = mutex.withLock {
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
            sira = (mevcut.gruplar.maxOfOrNull { it.sira } ?: -1) + 1,
            olusturma = System.currentTimeMillis(),
        )
        val guncel = mevcut.copy(gruplar = mevcut.gruplar + yeni)
        atomikYaz(guncel)
        state.value = guncel
        yeni
    }

    suspend fun grupGuncelle(grupId: String, yeniAd: String, yeniAciklama: String? = null): Boolean = mutex.withLock {
        val temizAd = yeniAd.trim()
        val temizAciklama = yeniAciklama?.trim()?.takeIf { it.isNotEmpty() }
        if (temizAd.isEmpty() || temizAd.length > MAX_GRUP_ADI ||
            (temizAciklama != null && temizAciklama.length > MAX_ACIKLAMA)
        ) return@withLock false
        val mevcut = state.value
        val hedef = mevcut.gruplar.firstOrNull { it.id == grupId } ?: return@withLock false
        val anahtar = temizAd.dersAnahtari()
        if (mevcut.gruplar.any { it.id != grupId && it.ad.dersAnahtari() == anahtar }) return@withLock false
        val guncelGruplar = mevcut.gruplar.map {
            if (it.id == hedef.id) it.copy(ad = temizAd, aciklama = temizAciklama) else it
        }
        val guncel = mevcut.copy(gruplar = guncelGruplar)
        atomikYaz(guncel)
        state.value = guncel
        true
    }

    suspend fun grupSil(grupId: String): Boolean = mutex.withLock {
        val mevcut = state.value
        if (mevcut.gruplar.none { it.id == grupId }) return@withLock false
        val silinecekSorular = mevcut.sorular.filter { it.grupId == grupId }
        val kalanSorular = mevcut.sorular.filterNot { it.grupId == grupId }
        val guncel = OgretmenVerisi(
            gruplar = mevcut.gruplar.filterNot { it.id == grupId }
                .mapIndexed { idx, g -> g.copy(sira = idx) },
            sorular = kalanSorular,
        )
        atomikYaz(guncel)
        state.value = guncel
        val halaKullanilan = guncel.kaliciDosyaAdlari()
        silinecekSorular.flatMap { it.gorseller.map { g -> g.dosyaAdi } }
            .distinct()
            .filterNot { it in halaKullanilan }
            .forEach { runCatching { gorselDeposu.ogretmenKaliciDosyayiSil(it) } }
        true
    }

    suspend fun soruKaydet(
        id: String? = null,
        grupId: String,
        baslik: String?,
        zorluk: OgretmenZorluk?,
        cevap: String?,
        gorseller: List<SoruGorselGirdisi>,
    ): OgretmenSorusu? = mutex.withLock {
        if (gorseller.isEmpty() || gorseller.size > MAX_GORSEL_ADEDI ||
            gorseller.distinctBy { it.id }.size != gorseller.size
        ) return@withLock null
        val temizBaslik = baslik?.trim()?.takeIf { it.isNotEmpty() }
        val temizCevap = cevap?.trim()?.takeIf { it.isNotEmpty() }
        if ((temizBaslik != null && temizBaslik.length > MAX_METIN) ||
            (temizCevap != null && temizCevap.length > MAX_METIN)
        ) return@withLock null

        val mevcut = state.value
        if (mevcut.gruplar.none { it.id == grupId }) return@withLock null
        val eskiSoru = id?.let { aranan -> mevcut.sorular.firstOrNull { it.id == aranan } }
        if (id != null && eskiSoru == null) return@withLock null
        val eskiGorselMap = eskiSoru?.gorseller.orEmpty().associateBy { it.id }

        for (secim in gorseller) {
            if (secim.gecici) {
                if (!gorselDeposu.geciciDosyayiDogrula(secim)) return@withLock null
            } else {
                val eski = eskiGorselMap[secim.id] ?: return@withLock null
                if (eski.dosyaAdi != secim.dosyaAdi || eski.sha256 != secim.sha256 ||
                    !gorselDeposu.ogretmenKaliciDosyayiDogrula(secim)
                ) return@withLock null
            }
        }

        val yeniKaliciDosyalar = mutableMapOf<String, String>()
        var diskaYazildi = false
        try {
            for (gorsel in gorseller.filter { it.gecici }) {
                val kaliciAd = gorselDeposu.ogretmenKaliciyaKopyala(gorsel.dosyaAdi)
                    ?: return@withLock null
                yeniKaliciDosyalar[gorsel.id] = kaliciAd
            }
            val now = System.currentTimeMillis()
            val kaliciGorseller = gorseller.mapIndexed { idx, g ->
                OgretmenGorseli(
                    id = g.id,
                    sira = idx,
                    dosyaAdi = yeniKaliciDosyalar[g.id] ?: g.dosyaAdi,
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
                zorluk = zorluk,
                cevap = temizCevap,
                cozuldu = eskiSoru?.cozuldu ?: false,
                gorseller = kaliciGorseller,
                olusturma = eskiSoru?.olusturma ?: now,
                guncelleme = now,
            )
            val guncelSorular = if (eskiSoru == null) {
                listOf(kaydedilen) + mevcut.sorular
            } else {
                mevcut.sorular.map { if (it.id == kaydedilen.id) kaydedilen else it }
            }
            val guncel = mevcut.copy(sorular = guncelSorular)
            atomikYaz(guncel)
            diskaYazildi = true
            state.value = guncel

            val halaKullanilan = guncel.kaliciDosyaAdlari()
            eskiSoru?.gorseller.orEmpty()
                .map { it.dosyaAdi }
                .distinct()
                .filterNot { it in halaKullanilan }
                .forEach { runCatching { gorselDeposu.ogretmenKaliciDosyayiSil(it) } }
            gorseller.filter { it.gecici }
                .forEach { runCatching { gorselDeposu.geciciDosyayiSil(it.dosyaAdi) } }
            kaydedilen
        } finally {
            if (!diskaYazildi) {
                yeniKaliciDosyalar.values.forEach {
                    runCatching { gorselDeposu.ogretmenKaliciDosyayiSil(it) }
                }
            }
        }
    }

    suspend fun cozulduDurumuDegistir(soruId: String, cozuldu: Boolean): Boolean = mutex.withLock {
        val mevcut = state.value
        val hedef = mevcut.sorular.firstOrNull { it.id == soruId } ?: return@withLock false
        val guncel = mevcut.copy(
            sorular = mevcut.sorular.map {
                if (it.id == hedef.id) it.copy(cozuldu = cozuldu, guncelleme = System.currentTimeMillis()) else it
            },
        )
        atomikYaz(guncel)
        state.value = guncel
        true
    }

    suspend fun baskaGruplaraKopyala(soruId: String, hedefGrupIdleri: Set<String>): Int = mutex.withLock {
        if (hedefGrupIdleri.isEmpty()) return@withLock 0
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
                zorluk = kaynak.zorluk,
                cevap = kaynak.cevap,
                cozuldu = false,
                gorseller = kaynak.gorseller.mapIndexed { idx, g ->
                    g.copy(id = UUID.randomUUID().toString(), sira = idx)
                },
                olusturma = now,
                guncelleme = now,
            )
        }
        val guncel = mevcut.copy(sorular = kopyalar + mevcut.sorular)
        atomikYaz(guncel)
        state.value = guncel
        kopyalar.size
    }

    suspend fun soruSil(soruId: String): Boolean = mutex.withLock {
        val mevcut = state.value
        val silinecek = mevcut.sorular.firstOrNull { it.id == soruId } ?: return@withLock false
        val guncel = mevcut.copy(sorular = mevcut.sorular.filterNot { it.id == soruId })
        atomikYaz(guncel)
        state.value = guncel
        val halaKullanilan = guncel.kaliciDosyaAdlari()
        silinecek.gorseller.map { it.dosyaAdi }
            .distinct()
            .filterNot { it in halaKullanilan }
            .forEach { runCatching { gorselDeposu.ogretmenKaliciDosyayiSil(it) } }
        true
    }

    suspend fun geriYukle(yeniVeri: OgretmenVerisi) = mutex.withLock {
        atomikYaz(yeniVeri)
        state.value = yeniVeri
    }

    private suspend fun atomikYaz(veri: OgretmenVerisi) = withContext(Dispatchers.IO) {
        storageFile.parentFile?.mkdirs()
        val pending = File(storageFile.parentFile, "$DOSYA_ADI.pending")
        val bytes = jsonYaz(veri).toString().toByteArray(Charsets.UTF_8)
        FileOutputStream(pending).use { out ->
            out.write(bytes)
            out.fd.sync()
        }
        Files.move(pending.toPath(), storageFile.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
    }

    companion object {
        private const val DOSYA_ADI = "ogretmen_calisma_alani.json"
        private const val MAX_GRUP_ADI = 120
        private const val MAX_ACIKLAMA = 240
        private const val MAX_METIN = 50_000
        private const val MAX_GORSEL_ADEDI = 20

        fun jsonYaz(veri: OgretmenVerisi): JSONObject = JSONObject()
            .put(
                "groups",
                JSONArray().apply {
                    veri.gruplar.forEach { g ->
                        put(
                            JSONObject()
                                .put("id", g.id)
                                .put("name", g.ad)
                                .put("description", g.aciklama ?: JSONObject.NULL)
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
                                .put("difficulty", s.zorluk?.name ?: JSONObject.NULL)
                                .put("answer", s.cevap ?: JSONObject.NULL)
                                .put("solved", s.cozuldu)
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

        fun jsonOku(json: JSONObject): OgretmenVerisi {
            val groupsJson = json.optJSONArray("groups") ?: JSONArray()
            val gruplar = ArrayList<OgretmenGrubu>(groupsJson.length())
            for (i in 0 until groupsJson.length()) {
                val obj = groupsJson.getJSONObject(i)
                gruplar += OgretmenGrubu(
                    id = obj.getString("id"),
                    ad = obj.getString("name"),
                    aciklama = obj.optNullMetin("description"),
                    sira = obj.optInt("order", i),
                    olusturma = obj.optLong("created_at", 0L),
                )
            }
            val questionsJson = json.optJSONArray("questions") ?: JSONArray()
            val sorular = ArrayList<OgretmenSorusu>(questionsJson.length())
            for (i in 0 until questionsJson.length()) {
                val obj = questionsJson.getJSONObject(i)
                val imagesJson = obj.getJSONArray("images")
                val gorseller = ArrayList<OgretmenGorseli>(imagesJson.length())
                for (j in 0 until imagesJson.length()) {
                    val img = imagesJson.getJSONObject(j)
                    gorseller += OgretmenGorseli(
                        id = img.getString("id"),
                        sira = img.optInt("order", j),
                        dosyaAdi = img.getString("file_name"),
                        mimeType = img.optString("mime_type", "image/jpeg"),
                        genislik = img.getInt("width"),
                        yukseklik = img.getInt("height"),
                        boyut = img.getLong("size_bytes"),
                        sha256 = img.getString("sha256").lowercase(),
                    )
                }
                val zorlukMetni = obj.optNullMetin("difficulty")
                val zorluk = zorlukMetni?.let { d ->
                    OgretmenZorluk.entries.firstOrNull { it.name == d }
                        ?: throw IOException("Bilinmeyen öğretmen zorluk düzeyi: $d")
                }
                sorular += OgretmenSorusu(
                    id = obj.getString("id"),
                    grupId = obj.getString("group_id"),
                    baslik = obj.optNullMetin("title"),
                    zorluk = zorluk,
                    cevap = obj.optNullMetin("answer"),
                    cozuldu = obj.optBoolean("solved", false),
                    gorseller = gorseller,
                    olusturma = obj.optLong("created_at", 0L),
                    guncelleme = obj.optLong("updated_at", 0L),
                )
            }
            return OgretmenVerisi(gruplar = gruplar, sorular = sorular)
        }

        fun zipAdi(kaliciDosyaAdi: String): String =
            "images/${kaliciDosyaAdi.substringAfterLast('/')}"

        private fun JSONObject.optNullMetin(key: String): String? =
            if (!has(key) || isNull(key)) null else getString(key).trim().takeIf { it.isNotEmpty() }
    }
}

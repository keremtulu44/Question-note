package com.keremtulu.questionnote.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * T3 "görünürlük": search, day filters, "who asked" grouping and the duplicate-photo guard.
 * Pure JVM test — no Android, no database, so it also documents the intended semantics.
 */
class OgretmenAramaVeFiltreTest {
    private val bugun = 20_000L

    private fun gorsel(sha: String = "sha0") = OgretmenGorseli(
        id = "g-$sha",
        sira = 0,
        dosyaAdi = "ogretmen-img/${sha}.jpg",
        genislik = 100,
        yukseklik = 80,
        boyut = 1_024,
        sha256 = sha,
    )

    private fun soru(
        id: String,
        grupId: String = "g1",
        baslik: String? = null,
        kimSordu: String? = null,
        kaynak: String? = null,
        dersId: String? = null,
        konuId: String? = null,
        planGun: Long? = null,
        durum: OgretmenDurum = OgretmenDurum.BEKLIYOR,
        sha256: String = "sha-$id",
        cevap: String? = null,
    ) = OgretmenSorusu(
        id = id,
        grupId = grupId,
        baslik = baslik,
        dersId = dersId,
        konuId = konuId,
        kaynak = kaynak,
        kimSordu = kimSordu,
        planGun = planGun,
        cevap = cevap,
        durum = durum,
        gorseller = listOf(gorsel(sha256)),
        olusturma = 1_000L,
        guncelleme = 1_000L,
    )

    private fun veri(sorular: List<OgretmenSorusu>) = OgretmenVerisi(
        gruplar = listOf(OgretmenGrubu(id = "g1", ad = "8A"), OgretmenGrubu(id = "g2", ad = "7B")),
        sorular = sorular,
    )

    @Test
    fun aramaBaslikSoranKisiKaynakVeDersAdindaEslesir() {
        val v = veri(
            listOf(
                soru("1", baslik = "Oran Orantı", kimSordu = "Elif Y."),
                soru("2", baslik = "Basınç", kaynak = "Miray 3", dersId = "m"),
                soru("3", baslik = "Üçgenler", konuId = "k"),
            ),
        )
        val dersler = mapOf("m" to "Matematik")
        val konular = mapOf("k" to "Geometri")

        fun bul(metin: String) = v.filtrele(arama = metin, dersAdlari = dersler, konuAdlari = konular)
            .map { it.id }

        assertEquals(listOf("1"), bul("oran"))
        assertEquals(listOf("1"), bul("ELİF"))
        assertEquals(listOf("1"), bul("elif y."))
        assertEquals(listOf("2"), bul("miray 3"))
        assertEquals(listOf("2"), bul("matematik"))
        assertEquals(listOf("3"), bul("geometri"))
        // Two words must both match the same question.
        assertEquals(listOf("2"), bul("basınç miray"))
        assertTrue(bul("basınç geometri").isEmpty())
    }

    @Test
    fun aramaCevapMetniniAramaz() {
        val v = veri(listOf(soru("1", baslik = "Oran", cevap = "gizli çözüm 42")))
        assertTrue(v.filtrele(arama = "gizli").isEmpty())
        assertTrue(v.filtrele(arama = "42").isEmpty())
    }

    @Test
    fun filtrelerBirlikteUygulanirVeBosGirdiListeyiDegistirmez() {
        val liste = listOf(
            soru("1", dersId = "m", kimSordu = "Elif"),
            soru("2", dersId = "m", kimSordu = "Ahmet"),
            soru("3", dersId = "f", kimSordu = "Elif"),
        )
        val v = veri(liste)
        assertSame(liste, v.filtrele(liste = liste))
        assertEquals(listOf("1", "3"), v.filtrele(liste = liste, kimSordu = "  elif  ").map { it.id })
        assertEquals(listOf("1"), v.filtrele(liste = liste, dersId = "m", kimSordu = "elif").map { it.id })
        assertEquals(listOf("3"), v.filtrele(liste = liste, dersId = "f", konuId = null, arama = "elif").map { it.id })
    }

    @Test
    fun kuyrukGunFiltreleriBugunGecikenVePlanlanmamis() {
        val v = veri(
            listOf(
                soru("bugun", planGun = bugun),
                soru("geciken", planGun = bugun - 3),
                soru("yarin", planGun = bugun + 1),
                soru("plansiz"),
                soru("cozuldu", planGun = bugun - 1, durum = OgretmenDurum.COZULDU),
            ),
        )

        fun idler(filtre: KuyrukGunFiltresi) = v.kuyruk(gunFiltresi = filtre, bugun = bugun).map { it.id }

        assertEquals(listOf("geciken", "bugun", "yarin", "plansiz"), idler(KuyrukGunFiltresi.HEPSI))
        assertEquals(listOf("bugun"), idler(KuyrukGunFiltresi.BUGUN))
        assertEquals(listOf("geciken"), idler(KuyrukGunFiltresi.GECIKEN))
        assertEquals(listOf("plansiz"), idler(KuyrukGunFiltresi.PLANLANMAMIS))
        assertEquals(KuyrukGunFiltresi.GECIKEN, KuyrukGunFiltresi.anahtarla("GECIKEN"))
        assertEquals(KuyrukGunFiltresi.HEPSI, KuyrukGunFiltresi.anahtarla("bilinmeyen"))
    }

    @Test
    fun kimSorduIsimleriNormalizeEdilirVeSiklikSiraliDoner() {
        val v = veri(
            listOf(
                soru("1", kimSordu = "Elif Y."),
                soru("2", kimSordu = " elif y. "),
                soru("3", kimSordu = "Ahmet"),
                soru("4", kimSordu = "Ahmet"),
                soru("5", kimSordu = "   "),
                soru("6"),
            ),
        )
        // 2 + 2 eşit: ilk yazım korunur, ardından ikinci isim gelir.
        assertEquals(listOf("Elif Y.", "Ahmet"), v.kimSorduIsimleri())
        assertTrue(veri(listOf(soru("1"))).kimSorduIsimleri().isEmpty())
    }

    @Test
    fun ayniGorselBaskaSorudaUyarir() {
        val v = veri(
            listOf(
                soru("1", sha256 = "abc"),
                soru("2", sha256 = "def"),
            ),
        )
        assertNull(v.ayniGorselliSoru(emptySet()))
        assertNull(v.ayniGorselliSoru(setOf("zzz")))
        assertEquals("1", v.ayniGorselliSoru(setOf("abc"))?.id)
        // Düzenleme sırasında sorunun kendi fotoğrafı çakışma sayılmaz.
        assertNull(v.ayniGorselliSoru(setOf("abc"), haricSoruId = "1"))
        assertEquals("2", v.ayniGorselliSoru(setOf("abc", "def"), haricSoruId = "1")?.id)
    }

    @Test
    fun binSorudaAramaKabulKapisiAltindaKalir() {
        val v = veri(
            (1..1_000).map { i ->
                soru(
                    id = "s$i",
                    baslik = "Soru $i oran orantı",
                    kimSordu = "Öğrenci ${i % 120}",
                    kaynak = "Miray ${i % 40}",
                    dersId = "m",
                )
            },
        )
        val dersler = mapOf("m" to "Matematik")
        // Isınma turu: JIT/ilk ayırma maliyeti ölçüme girmesin.
        v.filtrele(arama = "miray 7", dersAdlari = dersler)
        var enIyi = Long.MAX_VALUE
        repeat(3) {
            val basla = System.nanoTime()
            val sonuc = v.filtrele(arama = "miray 7", dersAdlari = dersler)
            enIyi = minOf(enIyi, (System.nanoTime() - basla) / 1_000_000)
            assertTrue(sonuc.isNotEmpty())
            // İki kelime ayrı ayrı aranır: "miray 17" de eşleşir, bu yüzden kaynak tam eşleşmez.
            assertTrue(sonuc.all { it.id.removePrefix("s").toInt() % 40 == 7 })
        }
        assertTrue("1000 soruda arama ${enIyi}ms sürdü", enIyi < 200)
    }

    @Test
    fun tutarliMiKuraliGrupVeGorselIstiyor() {
        val v = veri(listOf(soru("1")))
        assertTrue(v.tutarliMi())
        assertFalse(OgretmenVerisi(sorular = listOf(soru("1"))).tutarliMi())
    }
}

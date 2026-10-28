package com.keremtulu.questionnote.data.prefs

import android.content.Context
import com.keremtulu.questionnote.domain.model.DenemeDersSatiri
import com.keremtulu.questionnote.domain.model.DenemeKaydi
import com.keremtulu.questionnote.domain.model.NetKurali
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DenemeDeposuGuvenlikTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val prefs get() = context.getSharedPreferences("denemeler", Context.MODE_PRIVATE)
    private val deneme = DenemeKaydi(
        "deneme-1", "Bir deneme", NetKurali.DORTTE_BIR, 100L, 20_000,
        listOf(DenemeDersSatiri("mat", 40, 30, 4, "Matematik")),
    )

    @Before
    fun temizle() { prefs.edit().clear().commit() }

    @After
    fun bitir() { prefs.edit().clear().commit() }

    @Test
    fun bozukVeyaBelirsizIcerikBosListeGibiYazilamaz() {
        val duzgun = DenemeDeposu.jsonYaz(deneme)
        val bilinmeyenKural = JSONObject(duzgun.toString()).put("kural", "GELECEK_KURAL")
        val yeniAlan = JSONObject(duzgun.toString()).put("gelecekteki_alan", "korunmalı")
        val hataliSkor = JSONObject(duzgun.toString()).apply {
            getJSONArray("satirlar").getJSONObject(0).put("dogru", 39).put("yanlis", 4)
        }
        val kesirliSkor = JSONObject(duzgun.toString()).apply {
            getJSONArray("satirlar").getJSONObject(0).put("dogru", 12.5)
        }
        val ornekler = listOf(
            "[bozuk", "[] trailing", JSONArray().put(bilinmeyenKural).toString(),
            JSONArray().put(yeniAlan).toString(), JSONArray().put(hataliSkor).toString(),
            JSONArray().put(kesirliSkor).toString(), JSONArray().put(duzgun).put(duzgun).toString(),
        )
        for (ham in ornekler) {
            prefs.edit().clear().putString("liste", ham).commit()
            val depo = DenemeDeposu(context)
            assertTrue("Hata algılanmadı: $ham", depo.bozukMuIzle.value)
            assertThrows(DenemeVerisiOkunamadiException::class.java) { depo.kaydet(deneme) }
            assertThrows(DenemeVerisiOkunamadiException::class.java) { depo.sil(deneme.id) }
            assertThrows(DenemeVerisiOkunamadiException::class.java) { depo.yedekGoruntusu() }
            assertEquals(ham, prefs.getString("liste", null))
        }
    }

    @Test
    fun baskaBirYerTercihiBozarsaEskiDepoDaHamVerininUstuneYazmaz() {
        val depo = DenemeDeposu(context)
        depo.kaydet(deneme)
        val ham = "[başka yerden bozuldu"
        prefs.edit().putString("liste", ham).commit()
        assertThrows(DenemeVerisiOkunamadiException::class.java) { depo.kaydet(deneme) }
        assertTrue(depo.bozukMuIzle.value)
        assertEquals(ham, prefs.getString("liste", null))
    }

    @Test
    fun eskiGunAlaniOlmayanKayitOkunurVeNormalSekildeYazilir() {
        val eski = DenemeDeposu.jsonYaz(deneme).apply { remove("gun") }
        prefs.edit().putString("liste", JSONArray().put(eski).toString()).commit()
        val depo = DenemeDeposu(context)
        assertFalse(depo.bozukMuIzle.value)
        assertEquals(1, depo.kayitlariIzle.value.size)
        depo.kaydet(depo.kayitlariIzle.value.single().copy(ad = "Düzenlendi"))
        assertEquals("Düzenlendi", DenemeDeposu(context).kayitlariIzle.value.single().ad)
    }

    @Test
    fun sifirlamaHamVeriyiAyniTercihteKorurVeYazmayiYenidenAcar() {
        val ham = "[bozuk ama korunacak"
        prefs.edit().putString("liste", ham).commit()
        val depo = DenemeDeposu(context)
        depo.bozukKayitlariSifirla()
        assertFalse(depo.bozukMuIzle.value)
        assertEquals("[]", prefs.getString("liste", null))
        assertEquals(ham, prefs.all.filterKeys { it.startsWith("bozuk_liste_") }.values.single())
        depo.kaydet(deneme)
        assertEquals(deneme, DenemeDeposu(context).kayitlariIzle.value.single())
    }

    @Test
    fun yanlisTercihTuruHamYedekteTipiyleBirlikteKorunur() {
        val values: List<Any> = listOf("[broken", 42, 42L, true, -0.0f, setOf("a", "b"))
        values.forEach { expected ->
            val editor = prefs.edit().clear()
            when (expected) {
                is String -> editor.putString("liste", expected)
                is Int -> editor.putInt("liste", expected)
                is Long -> editor.putLong("liste", expected)
                is Boolean -> editor.putBoolean("liste", expected)
                is Float -> editor.putFloat("liste", expected)
                is Set<*> -> editor.putStringSet("liste", expected.map { it as String }.toSet())
            }
            editor.commit()
            val depo = DenemeDeposu(context)
            assertTrue(depo.bozukMuIzle.value)
            val ham = depo.yedekGoruntusu(bozukHamIle = true).bozukHam!!
            prefs.edit().putString("liste", "[]").commit()
            depo.hamYedegiGeriYukle(ham)
            assertEquals(expected, prefs.all["liste"])
            assertTrue(depo.bozukMuIzle.value)
        }
    }

    @Test
    fun hamYedekDogrulamaBilinmeyenVeyaYanlisTipiReddeder() {
        assertThrows(java.io.IOException::class.java) {
            DenemeDeposu.hamYedekDogrula("""{"version":1,"type":"unknown","value":"x"}""")
        }
        assertThrows(java.io.IOException::class.java) {
            DenemeDeposu.hamYedekDogrula("""{"version":1,"type":"int","value":1.5}""")
        }
    }
}

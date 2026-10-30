package com.keremtulu.questionnote.data.prefs

import android.content.Context
import com.keremtulu.questionnote.domain.model.DenemeDersSatiri
import com.keremtulu.questionnote.domain.model.DenemeKaydi
import com.keremtulu.questionnote.domain.model.NetKurali
import com.keremtulu.questionnote.domain.model.kaynakla
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DenemeKaynakGuvenligiTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()
    private fun deneme(id: String, ad: String) = DenemeKaydi(
        id, ad, NetKurali.DORTTE_BIR, 1L, 100, listOf(DenemeDersSatiri("m", 10, 7, 2)),
    )

    @Before fun temizle() {
        context.getSharedPreferences("denemeler", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun sharedNameStaysAmbiguousAfterRenameAndJsonRoundTrip() {
        val depo = DenemeDeposu(context)
        depo.kaydet(deneme("a", "Deneme 1"))
        depo.kaydet(deneme("b", " deneme  1 "))
        assertNull(depo.kayitlariIzle.value.kaynakla("Deneme 1"))
        depo.kaydet(requireNotNull(depo.getir("a")).copy(ad = "Deneme 2"))
        val kalan = requireNotNull(DenemeDeposu(context).getir("b"))
        assertTrue(kalan.kaynakBelirsiz)
        assertFalse(requireNotNull(depo.getir("a")).kaynakBelirsiz)
        assertNull(depo.kayitlariIzle.value.kaynakla("Deneme 1"))
        assertTrue(DenemeDeposu.jsonOku(DenemeDeposu.jsonYaz(kalan), strict = true).kaynakBelirsiz)
        val eskiJson = DenemeDeposu.jsonYaz(kalan).apply { remove("kaynakBelirsiz") }
        assertFalse(DenemeDeposu.jsonOku(eskiJson, strict = true).kaynakBelirsiz)
    }

    @Test fun deletingOneOfTheDuplicatesDoesNotMakeTheOtherAppearCertain() {
        val depo = DenemeDeposu(context)
        depo.kaydet(deneme("a", "Ortak"))
        depo.kaydet(deneme("b", "ORTAK"))
        depo.sil("a")
        assertTrue(requireNotNull(depo.getir("b")).kaynakBelirsiz)
        assertNull(DenemeDeposu(context).kayitlariIzle.value.kaynakla("ortak"))
    }
}

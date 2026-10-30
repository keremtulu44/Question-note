package com.keremtulu.questionnote.data.prefs

import android.content.Context
import com.keremtulu.questionnote.domain.model.DersCalismaAyar
import com.keremtulu.questionnote.domain.model.FiltreliCalismaEkraniAyar
import com.keremtulu.questionnote.domain.model.SinavProfili
import com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi
import com.keremtulu.questionnote.domain.model.SoruPlanDurumu
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CalismaTercihleriDeposuTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Before fun temizle() {
        context.getSharedPreferences("calisma_tercihleri", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun allPracticeFiltersSurviveStoreRecreationAndAreScopedByProfile() {
        val filtre = SoruCalismaFiltresi(
            dersId = "mat", vadesiGelen = true, kaynakAnahtari = "deneme 1", konuId = "konu",
            konusuz = false, kaynak = "Kaynak", kaynakBelirtilmemis = false,
            yanlisNedeni = "Dikkat", yanlisNedeniBelirtilmemis = false,
        )
        val ayar = FiltreliCalismaEkraniAyar(filtre, "20", akilliSira = false)
        CalismaTercihleriDeposu(context).filtreliKaydet(SinavProfili.LGS, ayar)
        val yeniden = CalismaTercihleriDeposu(context)
        assertEquals(ayar, yeniden.filtreliOku(SinavProfili.LGS))
        assertEquals(FiltreliCalismaEkraniAyar(), yeniden.filtreliOku(SinavProfili.YKS))
        val eksik = FiltreliCalismaEkraniAyar(
            filtre = SoruCalismaFiltresi(konusuz = true, kaynakBelirtilmemis = true, yanlisNedeniBelirtilmemis = true),
            soruSayisi = "",
        )
        yeniden.filtreliKaydet(SinavProfili.YKS, eksik)
        assertEquals(eksik, CalismaTercihleriDeposu(context).filtreliOku(SinavProfili.YKS))
        assertEquals(ayar, yeniden.filtreliOku(SinavProfili.LGS))
    }

    @Test fun lessonFiltersRemainSeparateAcrossLessonsAndProfiles() {
        val mat = DersCalismaAyar("cebir", false, SoruPlanDurumu.ASKIDA, true, "problem")
        val fen = DersCalismaAyar(konusuz = true, arama = "ısı")
        val depo = CalismaTercihleriDeposu(context)
        depo.dersKaydet(SinavProfili.LGS, "mat", mat)
        depo.dersKaydet(SinavProfili.LGS, "fen", fen)
        val yeniden = CalismaTercihleriDeposu(context)
        assertEquals(mat, yeniden.dersOku(SinavProfili.LGS, "mat"))
        assertEquals(fen, yeniden.dersOku(SinavProfili.LGS, "fen"))
        assertEquals(DersCalismaAyar(), yeniden.dersOku(SinavProfili.YKS, "mat"))
        yeniden.dersKaydet(SinavProfili.LGS, "mat", DersCalismaAyar())
        assertEquals(DersCalismaAyar(), CalismaTercihleriDeposu(context).dersOku(SinavProfili.LGS, "mat"))
        assertEquals(fen, yeniden.dersOku(SinavProfili.LGS, "fen"))
    }

    @Test fun corruptAndUnknownPreferencesFallBackSafely() {
        context.getSharedPreferences("calisma_tercihleri", Context.MODE_PRIVATE).edit()
            .putString("filtre_LGS", "{broken")
            .putString("ders_LGS_mat", "{\"durum\":\"OLD_STATUS\",\"arama\":\"test\"}")
            .commit()
        val depo = CalismaTercihleriDeposu(context)
        assertEquals(FiltreliCalismaEkraniAyar(), depo.filtreliOku(SinavProfili.LGS))
        assertEquals(DersCalismaAyar(arama = "test"), depo.dersOku(SinavProfili.LGS, "mat"))
    }
}

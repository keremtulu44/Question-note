package com.keremtulu.questionnote.ui.seviye

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.domain.calisma.*
import com.keremtulu.questionnote.domain.gelisim.*
import com.keremtulu.questionnote.domain.model.*
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema
import java.time.LocalDate

/** Synthetic data never enters main/release. This is a component preview, not a phone screenshot or user history. */
fun p6AnalizOrnegi(bos: Boolean = false): GelisimAnalizi {
    val gun = LocalDate.of(2026, 10, 18).toEpochDay().toInt()
    val dersler = listOf(Ders("m", "Matematik", 0, 30), Ders("f", "Fen Bilimleri", 1, 10))
    val sorular = if (bos) emptyList() else (0..39).map { i -> CalismaSorusu("q$i", if (i % 4 == 3) "f" else "m",
        if (i % 4 == 3) "elektrik" else "kesirler", "Çalışma kitabım", SoruPlanDurumu.AKTIF, gun - 1, gun + i % 7) }
    val tekrarlar = if (bos) emptyList() else (0..199).map { i ->
        val q = sorular[i % 40]
        val acik = i % 7 == 0
        AnalizTekrari("r$i", q.id, gun - 1 - i / 20, i.toLong(), TekrarSonucu.entries[(i + i / 40) % 3], acik,
            TarihselBaglam(q.dersId, dersler.first { it.id == q.dersId }.ad, q.konuId,
                if (q.dersId == "m") "Kesirler" else "Elektrik", kaynakAnahtari(q.kaynak), q.kaynak),
            if (i % 10 == 0) SureOlcumu() else SureOlcumu(45_000 + i % 20 * 2_000L, if (acik) 30_000 else 0, OlcumKalitesi.TAM))
    }
    return GelisimAnaliziHesabi.hesapla(sorular, dersler, mapOf("kesirler" to "Kesirler", "elektrik" to "Elektrik"),
        tekrarlar, SinavSecimi(SinavProfili.LGS), gun)
}

@Composable
fun P6BilesenOnizlemesi(koyu: Boolean = false, analiz: Boolean = false, bos: Boolean = false) {
    val a = remember(bos) { p6AnalizOrnegi(bos) }
    YanlisDefteriTema(koyuTema = koyu, hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).testTag("p6-preview")) {
            Text("YEREL COMPOSE · SENTETİK TEST VERİSİ", Modifier.padding(12.dp), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (analiz) GelisimAnaliziKarti(a) { _, _ -> } else {
                    SonrakiAdimKarti(a) { _, _ -> }
                    Text("Soru/tekrar ayrımı, tarihsel bağlam ve kapsama ayrıntıları İlerleme ekranında açılır.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}

@Preview(name = "P6 · gerçek bileşen / örnek veri", widthDp = 393, heightDp = 852)
@Composable
private fun Eylem() = P6BilesenOnizlemesi()
@Preview(name = "P6 · koyu / büyük yazı", widthDp = 393, heightDp = 852, fontScale = 2f)
@Composable
private fun Buyuk() = P6BilesenOnizlemesi(koyu = true)
@Preview(name = "P6 · analitik", widthDp = 393, heightDp = 852)
@Composable
private fun Analiz() = P6BilesenOnizlemesi(analiz = true)
@Preview(name = "P6 · verisiz", widthDp = 393, heightDp = 852)
@Composable
private fun Bos() = P6BilesenOnizlemesi(bos = true)

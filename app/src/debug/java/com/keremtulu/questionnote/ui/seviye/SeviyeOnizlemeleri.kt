package com.keremtulu.questionnote.ui.seviye

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.data.seviye.DersIlerlemesi
import com.keremtulu.questionnote.data.seviye.GunTahmini
import com.keremtulu.questionnote.data.seviye.SeviyeOzeti
import com.keremtulu.questionnote.domain.karne.KarneSayilari
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.domain.karne.RozetGirdisi
import com.keremtulu.questionnote.domain.seviye.SeviyeHesabi
import com.keremtulu.questionnote.domain.seviye.XpHesabi
import com.keremtulu.questionnote.ui.AltMenu
import com.keremtulu.questionnote.ui.AltSekme
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema
import java.time.LocalDate

/** Synthetic fixture, DEBUG ONLY. No sample observations are shipped in the release application. */
fun ornekIlerlemeOzeti(bos: Boolean = false): SeviyeOzeti {
    val sahip = if (bos) emptySet() else setOf(Rozet.ILK_SORU, Rozet.ARSIVCI, Rozet.ILK_KARNE,
        Rozet.KUSURSUZ_TUR, Rozet.ON_BAGIMSIZ, Rozet.ELLI_TEKRAR, Rozet.SERI_7, Rozet.HEDEF_7)
    val soru = if (bos) 0 else 72
    val tekrar = if (bos) 0 else 144
    val bagimsiz = if (bos) 0 else 98
    val xp = XpHesabi.kirilim(soru, tekrar, bagimsiz, if (bos) 0 else 9, if (bos) 0 else 100, sahip.size)
    val gun = LocalDate.of(2026, 10, 18).toEpochDay().toInt()
    return SeviyeOzeti(
        durum = SeviyeHesabi.durum(xp.toplam), kirilim = xp,
        soruSayisi = soru, tekrarSayisi = tekrar, bagimsizSayisi = bagimsiz,
        enUzunSeri = if (bos) 0 else 12, hedef = 10,
        dersler = if (bos) emptyList() else listOf(
            DersIlerlemesi("math", "Matematik", 40, 12, 14, 8, 6),
            DersIlerlemesi("science", "Fen Bilimleri", 32, 8, 10, 8, 6),
        ),
        tahmin = (0..6).map { GunTahmini(gun + it, if (bos) 0 else listOf(8, 4, 10, 3, 7, 0, 5)[it]) },
        rozetGirdisi = RozetGirdisi(KarneSayilari(0, 0, 0, 0, 0), if (bos) 0 else 12, bagimsiz,
            emptyList(), soruSayisi = soru, tekrarSayisi = tekrar, hedefGunSayisi = if (bos) 0 else 9),
        sahipRozetler = sahip,
    )
}

@Composable
fun P1TasarimOnizlemesi(durum: SeviyeDurumuUi, koyu: Boolean = false) {
    YanlisDefteriTema(koyuTema = koyu, hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).testTag("preview-root")) {
            Text("TASARIM ÖNİZLEMESİ · ÖRNEK VERİ", Modifier.fillMaxWidth().padding(12.dp),
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            Box(Modifier.weight(1f)) {
                Scaffold(bottomBar = { AltMenu(AltSekme.SEVIYE, {}) }) { padding ->
                    SeviyeSayfasi(durum, insets = padding)
                }
            }
        }
    }
}

@Preview(name = "01 · İlk gün", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun IlkGun() = P1TasarimOnizlemesi(SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti(bos = true)))

@Preview(name = "02 · Yoğun arşiv", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun YogunArsiv() = P1TasarimOnizlemesi(SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()))

@Preview(name = "03 · Koyu / büyük yazı", widthDp = 393, heightDp = 852, fontScale = 2f, showBackground = true)
@Composable
private fun KoyuBuyukYazi() = P1TasarimOnizlemesi(SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()), koyu = true)

@Preview(name = "04 · Yükleniyor", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun Yukleniyor() = P1TasarimOnizlemesi(SeviyeDurumuUi.Yukleniyor)

@Preview(name = "05 · Hata", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun Hata() = P1TasarimOnizlemesi(SeviyeDurumuUi.Hata)

@Preview(name = "06 · Hareket kapalı / koyu", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun Koyu() = P1TasarimOnizlemesi(SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()), koyu = true)

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
import com.keremtulu.questionnote.domain.karne.GunDenge
import com.keremtulu.questionnote.domain.karne.KarneSayilari
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.domain.karne.RozetGirdisi
import com.keremtulu.questionnote.domain.seviye.SeviyeHesabi
import com.keremtulu.questionnote.domain.seviye.XpHesabi
import com.keremtulu.questionnote.ui.AltMenu
import com.keremtulu.questionnote.ui.AltSekme
import com.keremtulu.questionnote.ui.tema.HareketPolitikasi
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema

/** Synthetic fixture, DEBUG ONLY. No sample observations are shipped in the release application. */
fun ornekIlerlemeOzeti(bos: Boolean = false): SeviyeOzeti {
    val sahip = if (bos) emptySet() else setOf(Rozet.ILK_SORU, Rozet.ARSIVCI, Rozet.ILK_KARNE,
        Rozet.KUSURSUZ_TUR, Rozet.ON_BAGIMSIZ, Rozet.ELLI_TEKRAR, Rozet.SERI_7, Rozet.HEDEF_7)
    val analiz = p6AnalizOrnegi(bos)
    val soru = if (bos) 0 else 40
    val tekrar = analiz.toplam.tekrar
    val bagimsiz = analiz.toplam.bagimsiz
    val xp = XpHesabi.kirilim(soru, tekrar, bagimsiz, if (bos) 0 else 10, if (bos) 0 else 100, sahip.size)
    return SeviyeOzeti(
        durum = SeviyeHesabi.durum(xp.toplam), kirilim = xp,
        soruSayisi = soru, tekrarSayisi = tekrar, bagimsizSayisi = bagimsiz,
        enUzunSeri = if (bos) 0 else 10, hedef = 10,
        dersler = if (bos) emptyList() else listOf(
            DersIlerlemesi("m", "Matematik", 30, 8, 10, 7, 5),
            DersIlerlemesi("f", "Fen Bilimleri", 10, 2, 4, 3, 1),
        ),
        tahmin = analiz.plan.map { GunTahmini(it.gun, it.adet) },
        rozetGirdisi = RozetGirdisi(KarneSayilari(0, 0, 0, 0, 0), if (bos) 0 else 10, bagimsiz,
            analiz.ritim.takeLast(5).map { GunDenge(it.gun, it.tekrar, 0) }, soruSayisi = soru, tekrarSayisi = tekrar, hedefGunSayisi = if (bos) 0 else 10),
        sahipRozetler = sahip, analiz = analiz,
    )
}

@Composable
fun IlerlemeTasarimOnizlemesi(durum: SeviyeDurumuUi, koyu: Boolean = false) {
    YanlisDefteriTema(koyuTema = koyu, hareketPolitikasi = HareketPolitikasi.Azaltilmis) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).testTag("preview-root")) {
            Text("YEREL COMPOSE · SENTETİK VERİ", Modifier.fillMaxWidth().padding(12.dp),
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
private fun IlkGun() = IlerlemeTasarimOnizlemesi(SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti(bos = true)))

@Preview(name = "02 · Yoğun arşiv", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun YogunArsiv() = IlerlemeTasarimOnizlemesi(SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()))

@Preview(name = "03 · Koyu / büyük yazı", widthDp = 393, heightDp = 852, fontScale = 2f, showBackground = true)
@Composable
private fun KoyuBuyukYazi() = IlerlemeTasarimOnizlemesi(SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()), koyu = true)

@Preview(name = "04 · Yükleniyor", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun Yukleniyor() = IlerlemeTasarimOnizlemesi(SeviyeDurumuUi.Yukleniyor)

@Preview(name = "05 · Hata", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun Hata() = IlerlemeTasarimOnizlemesi(SeviyeDurumuUi.Hata)

@Preview(name = "06 · Hareket kapalı / koyu", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun Koyu() = IlerlemeTasarimOnizlemesi(SeviyeDurumuUi.ozetten(ornekIlerlemeOzeti()), koyu = true)

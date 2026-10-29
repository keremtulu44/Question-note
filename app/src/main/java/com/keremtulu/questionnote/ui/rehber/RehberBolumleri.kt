package com.keremtulu.questionnote.ui.rehber

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.NetKurali
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import com.keremtulu.questionnote.ui.bilesenler.HedefHalkasi
import com.keremtulu.questionnote.ui.bilesenler.InceOlcek
import com.keremtulu.questionnote.ui.bilesenler.KucukOnay
import com.keremtulu.questionnote.ui.bilesenler.SadeKart
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.calisma.TAHTA_MAKS_KALINLIK_DP
import com.keremtulu.questionnote.ui.calisma.TAHTA_MIN_KALINLIK_DP
import com.keremtulu.questionnote.ui.calisma.tahtaKalinligiSinirla
import com.keremtulu.questionnote.ui.deneme.netYazi
import com.keremtulu.questionnote.ui.premium.PremiumButton
import com.keremtulu.questionnote.ui.premium.PremiumGradyanButon
import com.keremtulu.questionnote.ui.premium.PremiumGradyanlar
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val RehberLocale: Locale = Locale.forLanguageTag("tr-TR")
private val RehberTarihBicimi: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM", RehberLocale)

/** Bölümler arasında yaşayan etkileşim durumu; sayfa değişse de seçim korunur. */
internal class RehberDurumu {
    var tekrarSonucu by mutableStateOf(TekrarSonucu.ZORLANDIM)
    var dersSkoru by mutableStateOf(DemoDersSkoru(dersAdi = "Matematik"))
    var kural by mutableStateOf(NetKurali.DORTTE_BIR)

    /** Rehber yeniden açıldığında etkileşimleri başlangıç değerine döndürür. */
    fun sifirla() {
        tekrarSonucu = TekrarSonucu.ZORLANDIM
        dersSkoru = DemoDersSkoru(dersAdi = "Matematik")
        kural = NetKurali.DORTTE_BIR
    }
}

@Composable
internal fun RehberBolumuIcerigi(
    bolum: RehberBolumu,
    durum: RehberDurumu,
    onSoruEkle: () -> Unit,
    onDenemeler: () -> Unit,
    onYedek: () -> Unit,
    onBugun: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rehber-bolum-${bolum.anahtar}"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        when (bolum) {
            RehberBolumu.TANITIM -> TanitimBolumu()
            RehberBolumu.KAYIT -> KayitBolumu(durum)
            RehberBolumu.BUGUN -> BugunBolumu(onBugun)
            RehberBolumu.DENEME -> DenemeBolumu(durum, onDenemeler)
            RehberBolumu.COZUM -> CozumBolumu()
            RehberBolumu.YEDEK -> YedekBolumu(onYedek)
            RehberBolumu.KAPANIS -> KapanisBolumu(onSoruEkle, onBugun)
        }
    }
}

// ---------------------------------------------------------------- 1. Tanıtım

@Composable
private fun TanitimBolumu() {
    GradyanKart(
        modifier = Modifier.fillMaxWidth(),
        gradyan = PremiumGradyanlar.KAHRAMAN_GRADYAN,
        shape = RoundedCornerShape(26.dp),
        borderAnimation = false,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RehberDefterCizimi(
                    modifier = Modifier.size(width = 64.dp, height = 64.dp).clearAndSetSemantics { },
                    kagit = PremiumMarkaRenkler.Kagit,
                    murekkep = PremiumMarkaRenkler.Murekkep,
                    bakir = PremiumMarkaRenkler.BakirIsik,
                    vurgu = PremiumMarkaRenkler.Mercan,
                )
                Text(
                    text = stringResource(R.string.rehber_1_kicker),
                    color = PremiumMarkaRenkler.BakirIsik,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp,
                )
            }
            Text(
                text = stringResource(R.string.rehber_1_title),
                color = PremiumMarkaRenkler.Kagit,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.rehber_1_body),
                color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.88f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
    SadeKart {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            IlkeSatiri(
                baslik = stringResource(R.string.rehber_1_item1_title),
                aciklama = stringResource(R.string.rehber_1_item1_body),
                vurgu = PremiumMarkaRenkler.Bakir,
            )
            IlkeSatiri(
                baslik = stringResource(R.string.rehber_1_item2_title),
                aciklama = stringResource(R.string.rehber_1_item2_body),
                vurgu = PremiumMarkaRenkler.Petrol,
            )
            IlkeSatiri(
                baslik = stringResource(R.string.rehber_1_item3_title),
                aciklama = stringResource(R.string.rehber_1_item3_body),
                vurgu = PremiumMarkaRenkler.Yesil,
            )
        }
    }
}

@Composable
private fun IlkeSatiri(baslik: String, aciklama: String, vurgu: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Numarasız işaret: anlatım sırası önemli değil, üç ilke eşit ağırlıkta.
        Box(
            modifier = Modifier.padding(top = 5.dp).size(8.dp).background(vurgu, CircleShape),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = baslik,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = aciklama,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ---------------------------------------------------------------- 2. Kayıt

@Composable
private fun KayitBolumu(durum: RehberDurumu) {
    BolumBasligi(
        kicker = stringResource(R.string.rehber_2_kicker),
        baslik = stringResource(R.string.rehber_2_title),
        govde = stringResource(R.string.rehber_2_body),
    )
    SadeKart {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AdimSatiri(1, stringResource(R.string.rehber_2_step1))
            AdimSatiri(2, stringResource(R.string.rehber_2_step2))
            AdimSatiri(3, stringResource(R.string.rehber_2_step3))
        }
    }

    val bugun = remember { LocalDate.now() }
    val plan = remember(durum.tekrarSonucu, bugun) {
        tekrarOnizlemesi(durum.tekrarSonucu, bugun)
    }
    val tarih = remember(plan.sonrakiGun) {
        LocalDate.ofEpochDay(plan.sonrakiGun.toLong()).format(RehberTarihBicimi)
    }
    val vurgu = remember(plan.sonrakiAralik) {
        when {
            plan.sonrakiAralik >= 21 -> 3
            plan.sonrakiAralik >= 7 -> 2
            plan.sonrakiAralik >= 3 -> 1
            else -> 0
        }
    }

    SadeKart {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.rehber_2_prompt),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                DemoEtiketi()
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SecenekCipi(
                    metin = stringResource(R.string.rehber_2_sonuc_cozemedim),
                    secili = durum.tekrarSonucu == TekrarSonucu.COZEMEDIM,
                    modifier = Modifier.weight(1f).testTag("rehber-sonuc-cozemedim"),
                ) { durum.tekrarSonucu = TekrarSonucu.COZEMEDIM }
                SecenekCipi(
                    metin = stringResource(R.string.rehber_2_sonuc_zorlandim),
                    secili = durum.tekrarSonucu == TekrarSonucu.ZORLANDIM,
                    modifier = Modifier.weight(1f).testTag("rehber-sonuc-zorlandim"),
                ) { durum.tekrarSonucu = TekrarSonucu.ZORLANDIM }
                SecenekCipi(
                    metin = stringResource(R.string.rehber_2_sonuc_rahat),
                    secili = durum.tekrarSonucu == TekrarSonucu.RAHAT,
                    modifier = Modifier.weight(1f).testTag("rehber-sonuc-rahat"),
                ) { durum.tekrarSonucu = TekrarSonucu.RAHAT }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.rehber_2_next_prefix),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.rehber_2_next_date, tarih, plan.sonrakiAralik),
                        modifier = Modifier.testTag("rehber-sonraki-tekrar"),
                        style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    RehberAralikYolu(
                        modifier = Modifier.fillMaxWidth().height(18.dp),
                        cizgi = MaterialTheme.colorScheme.primary,
                        dolgu = MaterialTheme.colorScheme.primary,
                        vurgulanan = vurgu,
                    )
                    Text(
                        text = stringResource(R.string.rehber_2_next_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
    DemoNotu()
}

@Composable
private fun SecenekCipi(
    metin: String,
    secili: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .heightIn(min = 46.dp)
            .background(if (secili) colors.primary else colors.surface, shape)
            .border(
                width = 1.dp,
                color = if (secili) colors.primary else colors.outlineVariant.copy(alpha = 0.8f),
                shape = shape,
            )
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics {
                this.selected = secili
                role = Role.RadioButton
            }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = metin,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (secili) FontWeight.Bold else FontWeight.Medium,
            color = if (secili) colors.onPrimary else colors.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ---------------------------------------------------------------- 3. Bugün

@Composable
private fun BugunBolumu(onBugun: () -> Unit) {
    BolumBasligi(
        kicker = stringResource(R.string.rehber_3_kicker),
        baslik = stringResource(R.string.rehber_3_title),
        govde = stringResource(R.string.rehber_3_body),
    )
    val kuyruk = remember { demoKuyrugu() }
    SadeKart {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = stringResource(R.string.rehber_3_round, kuyruk.turBoyu),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(
                            R.string.rehber_3_remaining,
                            kuyruk.siradaKalan,
                            kuyruk.geciken,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DemoEtiketi()
                Spacer(Modifier.width(8.dp))
                HedefHalkasi(
                    oran = kuyruk.turBoyu / 20f,
                    modifier = Modifier.size(34.dp).clearAndSetSemantics { },
                    izRenk = MaterialTheme.colorScheme.outlineVariant,
                    dolguRenk = MaterialTheme.colorScheme.primary,
                    kalinlik = 2.4.dp,
                    isaretSayisi = 12,
                )
            }
            InceOlcek(
                oran = kuyruk.turBoyu / 20f,
                modifier = Modifier.fillMaxWidth().height(8.dp).clearAndSetSemantics { },
                izRenk = MaterialTheme.colorScheme.surfaceVariant,
                dolguRenk = MaterialTheme.colorScheme.primary,
                bolme = 10,
            )
            kuyruk.onizleme.forEach { soru -> KuyrukSatiri(soru) }
            Text(
                text = stringResource(R.string.rehber_3_queue_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    DemoNotu()
    PremiumOutlinedButton(
        onClick = onBugun,
        modifier = Modifier.fillMaxWidth().testTag("rehber-cta-bugun"),
    ) {
        Text(stringResource(R.string.rehber_3_cta))
    }
}

@Composable
private fun KuyrukSatiri(soru: DemoSoru) {
    val colors = MaterialTheme.colorScheme
    val vurgu = when (soru.durum) {
        DemoSoruDurumu.GECIKTI -> colors.error
        DemoSoruDurumu.BUGUN -> colors.primary
        DemoSoruDurumu.YAKINDA -> colors.onSurfaceVariant
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(7.dp).background(vurgu, CircleShape))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = soru.konu,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = soru.ders,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
        Text(
            text = when (soru.durum) {
                DemoSoruDurumu.GECIKTI -> stringResource(R.string.rehber_3_overdue, soru.gecikmeGun)
                DemoSoruDurumu.BUGUN -> stringResource(R.string.rehber_3_due_today)
                DemoSoruDurumu.YAKINDA -> stringResource(R.string.rehber_3_upcoming)
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (soru.durum == DemoSoruDurumu.YAKINDA) colors.onSurfaceVariant else vurgu,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ---------------------------------------------------------------- 4. Deneme

@Composable
private fun DenemeBolumu(durum: RehberDurumu, onDenemeler: () -> Unit) {
    BolumBasligi(
        kicker = stringResource(R.string.rehber_4_kicker),
        baslik = stringResource(R.string.rehber_4_title),
        govde = stringResource(R.string.rehber_4_body),
    )
    val skor = durum.dersSkoru
    val kural = durum.kural
    val net = skor.net(kural)
    val aday = skor.yanlis + skor.bos

    SadeKart {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.rehber_4_subject, skor.soruSayisi),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                DemoEtiketi()
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SayacAlani(
                    etiket = stringResource(R.string.rehber_4_correct),
                    deger = skor.dogru,
                    azaltEtiket = stringResource(R.string.rehber_4_azalt, stringResource(R.string.rehber_4_correct)),
                    artirEtiket = stringResource(R.string.rehber_4_artir, stringResource(R.string.rehber_4_correct)),
                    modifier = Modifier.weight(1f).testTag("rehber-dogru"),
                    onAzalt = { durum.dersSkoru = skor.dogruAzalt() },
                    onArtir = { durum.dersSkoru = skor.dogruArtir() },
                )
                SayacAlani(
                    etiket = stringResource(R.string.rehber_4_wrong),
                    deger = skor.yanlis,
                    azaltEtiket = stringResource(R.string.rehber_4_azalt, stringResource(R.string.rehber_4_wrong)),
                    artirEtiket = stringResource(R.string.rehber_4_artir, stringResource(R.string.rehber_4_wrong)),
                    modifier = Modifier.weight(1f).testTag("rehber-yanlis"),
                    onAzalt = { durum.dersSkoru = skor.yanlisAzalt() },
                    onArtir = { durum.dersSkoru = skor.yanlisArtir() },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
                ) {
                    Text(
                        text = stringResource(R.string.rehber_4_net, netYazi(net)),
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("rehber-net"),
                        style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = stringResource(R.string.rehber_4_blank, skor.bos),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = stringResource(R.string.rehber_4_rule),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NetKurali.entries.forEach { secenek ->
                    FilterChip(
                        selected = kural == secenek,
                        onClick = { durum.kural = secenek },
                        label = {
                            Text(
                                when (secenek) {
                                    NetKurali.DORTTE_BIR -> stringResource(R.string.deneme_rule_4)
                                    NetKurali.UCTE_BIR -> stringResource(R.string.deneme_rule_3)
                                    NetKurali.YOK -> stringResource(R.string.deneme_rule_none)
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            }
            Text(
                text = stringResource(R.string.rehber_4_upload, aday),
                modifier = Modifier.testTag("rehber-aday"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    val ozet = remember { demoDenemeOzeti() }
    val ortalama = ozet.ortalamaNet
    val enIyi = ozet.enIyiNet
    SadeKart {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (ortalama != null && enIyi != null) {
                Text(
                    text = stringResource(
                        R.string.rehber_4_summary,
                        ozet.kayitSayisi,
                        netYazi(ortalama),
                        netYazi(enIyi),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
            RehberAralikYolu(
                modifier = Modifier.fillMaxWidth().height(18.dp),
                cizgi = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                dolgu = MaterialTheme.colorScheme.primary,
                vurgulanan = 2,
            )
            Text(
                text = stringResource(R.string.rehber_4_link),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            DemoNotu()
        }
    }
    PremiumOutlinedButton(
        onClick = onDenemeler,
        modifier = Modifier.fillMaxWidth().testTag("rehber-cta-deneme"),
    ) {
        Text(stringResource(R.string.rehber_4_cta))
    }
}

/** –/+ ile artırılan sayaç: kaydırıcı jesti olmadan da kullanılabilir (TalkBack, büyük yazı). */
@Composable
private fun SayacAlani(
    etiket: String,
    deger: Int,
    azaltEtiket: String,
    artirEtiket: String,
    modifier: Modifier = Modifier,
    onAzalt: () -> Unit,
    onArtir: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.75f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = etiket,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SayacDugmesi(metin = "−", aciklama = azaltEtiket, onClick = onAzalt)
                Text(
                    text = deger.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Bold,
                )
                SayacDugmesi(metin = "+", aciklama = artirEtiket, onClick = onArtir)
            }
        }
    }
}

@Composable
private fun SayacDugmesi(metin: String, aciklama: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(colors.surfaceVariant.copy(alpha = 0.7f), CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = aciklama },
        contentAlignment = Alignment.Center,
    ) {
        Text(metin, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

// ---------------------------------------------------------------- 5. Çözüm

@Composable
private fun CozumBolumu() {
    BolumBasligi(
        kicker = stringResource(R.string.rehber_5_kicker),
        baslik = stringResource(R.string.rehber_5_title),
        govde = stringResource(R.string.rehber_5_body),
    )
    RehberMiniTahta()
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
    ) {
        Text(
            text = stringResource(R.string.rehber_5_honest),
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

/**
 * Rehberin mini tahtası: gerçek tahtanın sözleşmesini (1–10 dp kalınlık, kesintisiz çizgi,
 * tek dokunuşta nokta) gösteren küçük bir yüzey. Hiçbir yere kaydedilmez.
 */
@Composable
private fun RehberMiniTahta() {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val vuruslar = remember { mutableStateListOf<List<Offset>>() }
    val canli = remember { mutableStateListOf<Offset>() }
    val surum = remember { mutableIntStateOf(0) }
    var kalinlik by remember { mutableStateOf(4f) }
    val kalinlikEtiketi = stringResource(R.string.whiteboard_width_value, kalinlik.roundToInt())
    val bosMetin = stringResource(R.string.rehber_5_empty)
    val cizimEtiketi = if (vuruslar.isEmpty()) bosMetin else stringResource(R.string.rehber_5_mark_count, vuruslar.size)

    SadeKart {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.rehber_5_draw_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .background(Color(0xFFFFFEFB), RoundedCornerShape(16.dp))
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("rehber-mini-tahta")
                        .semantics {
                            contentDescription = context.getString(R.string.rehber_5_draw_hint)
                            stateDescription = cizimEtiketi
                        }
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val ilk = awaitFirstDown(requireUnconsumed = false)
                                val genislik = size.width.toFloat().coerceAtLeast(1f)
                                val yukseklik = size.height.toFloat().coerceAtLeast(1f)
                                fun nokta(offset: Offset) = Offset(
                                    (offset.x / genislik).coerceIn(0f, 1f),
                                    (offset.y / yukseklik).coerceIn(0f, 1f),
                                )
                                canli.clear()
                                canli.add(nokta(ilk.position))
                                surum.intValue++
                                ilk.consume()
                                var devam = true
                                while (devam) {
                                    val olay = awaitPointerEvent()
                                    val degisim = olay.changes.firstOrNull { it.id == ilk.id }
                                        ?: olay.changes.firstOrNull()
                                        ?: break
                                    degisim.historical.forEach { canli.add(nokta(it.position)) }
                                    canli.add(nokta(degisim.position))
                                    surum.intValue++
                                    degisim.consume()
                                    if (!degisim.pressed || olay.changes.none { it.pressed }) devam = false
                                }
                                if (canli.isNotEmpty()) vuruslar.add(canli.toList())
                                canli.clear()
                                surum.intValue++
                            }
                        },
                ) {
                    // Durum okuması çizim fazını bağlar; sayfa kaydırma jestiyle karışmaz.
                    val guncel = surum.intValue
                    val cizgi = PremiumMarkaRenkler.Murekkep
                    vuruslar.forEach { vurus ->
                        drawRehberVurusu(vurus, cizgi, kalinlik)
                    }
                    if (guncel >= 0 && canli.isNotEmpty()) drawRehberVurusu(canli.toList(), cizgi, kalinlik)
                }
                if (vuruslar.isEmpty() && canli.isEmpty()) {
                    Text(
                        text = bosMetin,
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant.copy(alpha = 0.8f),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.rehber_5_width),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                )
                Slider(
                    value = kalinlik,
                    onValueChange = { kalinlik = tahtaKalinligiSinirla(it) },
                    valueRange = TAHTA_MIN_KALINLIK_DP..TAHTA_MAKS_KALINLIK_DP,
                    steps = (TAHTA_MAKS_KALINLIK_DP - TAHTA_MIN_KALINLIK_DP).toInt() - 1,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 34.dp)
                        .testTag("rehber-kalinlik")
                        .semantics { stateDescription = kalinlikEtiketi },
                )
                Text(
                    text = kalinlikEtiketi,
                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                )
                Box(
                    modifier = Modifier
                        .heightIn(min = 38.dp)
                        .clickable(
                            role = Role.Button,
                            enabled = vuruslar.isNotEmpty(),
                            onClick = { vuruslar.clear() },
                        )
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.rehber_5_clear),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (vuruslar.isEmpty()) colors.onSurfaceVariant.copy(alpha = 0.5f) else colors.primary,
                    )
                }
            }
        }
    }
}

/** Normalize noktaları yumuşak bir yola çevirir (gerçek tahtadaki yöntemin küçük hâli). */
private fun DrawScope.drawRehberVurusu(noktalar: List<Offset>, renk: Color, kalinlikDp: Float) {
    if (noktalar.isEmpty()) return
    val noktalarPx = noktalar.map { Offset(it.x * size.width, it.y * size.height) }
    val yolGenisligi = kalinlikDp.dp.toPx()
    if (noktalarPx.size == 1) {
        drawCircle(renk, radius = yolGenisligi / 2f, center = noktalarPx.first())
        return
    }
    val yol = Path().apply {
        moveTo(noktalarPx.first().x, noktalarPx.first().y)
        if (noktalarPx.size == 2) {
            lineTo(noktalarPx[1].x, noktalarPx[1].y)
        } else {
            for (i in 1 until noktalarPx.size - 1) {
                val kontrol = noktalarPx[i]
                val sonraki = noktalarPx[i + 1]
                quadraticTo(
                    kontrol.x,
                    kontrol.y,
                    (kontrol.x + sonraki.x) / 2f,
                    (kontrol.y + sonraki.y) / 2f,
                )
            }
            lineTo(noktalarPx.last().x, noktalarPx.last().y)
        }
    }
    drawPath(
        path = yol,
        color = renk,
        style = Stroke(width = yolGenisligi, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}

// ---------------------------------------------------------------- 6. Yedek

@Composable
private fun YedekBolumu(onYedek: () -> Unit) {
    BolumBasligi(
        kicker = stringResource(R.string.rehber_6_kicker),
        baslik = stringResource(R.string.rehber_6_title),
        govde = stringResource(R.string.rehber_6_body),
    )
    SadeKart {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RehberYedekKutusu(
                modifier = Modifier.fillMaxWidth().height(96.dp).clearAndSetSemantics { },
                cizgi = MaterialTheme.colorScheme.primary,
                vurgu = PremiumMarkaRenkler.Bakir,
                dolgu = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            )
            AdimSatiri(1, stringResource(R.string.rehber_6_step1))
            AdimSatiri(2, stringResource(R.string.rehber_6_step2))
            AdimSatiri(3, stringResource(R.string.rehber_6_step3))
        }
    }
    SadeKart {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.rehber_6_list_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            yedekIcerigi().forEach { oge ->
                YedekSatiri(oge)
            }
            Text(
                text = stringResource(R.string.rehber_6_note),
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    PremiumButton(
        onClick = onYedek,
        modifier = Modifier.fillMaxWidth().testTag("rehber-cta-yedek"),
    ) {
        Text(stringResource(R.string.rehber_6_cta), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun YedekSatiri(oge: YedekOgesi) {
    val colors = MaterialTheme.colorScheme
    val metin = stringResource(
        when (oge.adAnahtari) {
            "rehber_backup_item_questions" -> R.string.rehber_backup_item_questions
            "rehber_backup_item_images" -> R.string.rehber_backup_item_images
            "rehber_backup_item_solutions" -> R.string.rehber_backup_item_solutions
            "rehber_backup_item_reviews" -> R.string.rehber_backup_item_reviews
            "rehber_backup_item_denemeler" -> R.string.rehber_backup_item_denemeler
            "rehber_backup_item_catalog" -> R.string.rehber_backup_item_catalog
            "rehber_backup_item_badges" -> R.string.rehber_backup_item_badges
            else -> R.string.rehber_backup_item_guide
        },
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (oge.dahil) {
            KucukOnay(
                modifier = Modifier.size(15.dp).clearAndSetSemantics { },
                renk = PremiumMarkaRenkler.Yesil,
            )
        } else {
            Canvas(Modifier.size(15.dp).clearAndSetSemantics { }) {
                drawLine(
                    color = colors.outline,
                    start = Offset(size.width * 0.2f, size.height * 0.55f),
                    end = Offset(size.width * 0.8f, size.height * 0.55f),
                    strokeWidth = 1.6.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
        Text(
            text = metin,
            style = MaterialTheme.typography.bodySmall,
            color = if (oge.dahil) colors.onSurface else colors.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = if (oge.dahil) "✓" else "–",
            style = MaterialTheme.typography.labelSmall,
            color = if (oge.dahil) PremiumMarkaRenkler.Yesil else colors.onSurfaceVariant,
            fontFamily = com.keremtulu.questionnote.ui.premium.PremiumSembolAilesi,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}

// ---------------------------------------------------------------- 7. Kapanış

@Composable
private fun KapanisBolumu(onSoruEkle: () -> Unit, onBugun: () -> Unit) {
    BolumBasligi(
        kicker = stringResource(R.string.rehber_7_kicker),
        baslik = stringResource(R.string.rehber_7_title),
        govde = stringResource(R.string.rehber_7_body),
    )
    SadeKart {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            IlkeSatiri(
                baslik = stringResource(R.string.rehber_7_title),
                aciklama = stringResource(R.string.rehber_7_item1),
                vurgu = PremiumMarkaRenkler.Petrol,
            )
            IlkeSatiri(
                baslik = stringResource(R.string.rehber_1_item3_title),
                aciklama = stringResource(R.string.rehber_7_item2),
                vurgu = PremiumMarkaRenkler.Bakir,
            )
            IlkeSatiri(
                baslik = stringResource(R.string.rehber_4_kicker),
                aciklama = stringResource(R.string.rehber_7_item3),
                vurgu = PremiumMarkaRenkler.Mor,
            )
        }
    }
    SadeKart {
        Text(
            text = stringResource(R.string.rehber_reopen_hint),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    PremiumGradyanButon(
        onClick = onSoruEkle,
        modifier = Modifier.fillMaxWidth().testTag("rehber-cta-soru-ekle"),
    ) {
        Text(stringResource(R.string.rehber_7_cta_add), fontWeight = FontWeight.Bold)
    }
    PremiumOutlinedButton(
        onClick = onBugun,
        modifier = Modifier.fillMaxWidth().testTag("rehber-cta-bugun-kapanis"),
    ) {
        Text(stringResource(R.string.rehber_7_cta_today))
    }
}

// ---------------------------------------------------------------- ortak parçalar

@Composable
private fun BolumBasligi(kicker: String, baslik: String, govde: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = kicker,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
        )
        Text(
            text = baslik,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = govde,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AdimSatiri(no: Int, metin: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier.size(24.dp).background(colors.primary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = no.toString(),
                style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Bold,
                color = colors.primary,
            )
        }
        Text(
            text = metin,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun DemoEtiketi() {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f),
    ) {
        Text(
            text = stringResource(R.string.rehber_demo_badge),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun DemoNotu() {
    Text(
        text = stringResource(R.string.rehber_demo_note),
        modifier = Modifier.padding(horizontal = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

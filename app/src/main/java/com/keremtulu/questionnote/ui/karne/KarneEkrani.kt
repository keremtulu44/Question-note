package com.keremtulu.questionnote.ui.karne

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import com.keremtulu.questionnote.ui.tema.LocalHareketPolitikasi
import com.keremtulu.questionnote.ui.tema.HareketTuru
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.ilerleme.IlerlemeHizmeti
import com.keremtulu.questionnote.data.ilerleme.RozetHizmeti
import com.keremtulu.questionnote.data.prefs.GunlukHedefDeposu
import com.keremtulu.questionnote.data.prefs.RozetDeposu
import com.keremtulu.questionnote.domain.ilerleme.IlerlemeOzeti
import com.keremtulu.questionnote.domain.karne.KarneHesabi
import com.keremtulu.questionnote.domain.karne.KarneKovasi
import com.keremtulu.questionnote.domain.karne.KarneSayilari
import com.keremtulu.questionnote.domain.karne.OturumSonucu
import com.keremtulu.questionnote.domain.karne.Rozet
import com.keremtulu.questionnote.domain.karne.RozetGirdisi
import com.keremtulu.questionnote.domain.karne.RozetKurallari
import com.keremtulu.questionnote.domain.karne.TebrikSeviyesi
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.Konu
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.repository.KonuRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch

data class DagilimSatiri(val etiket: String, val sayi: Int)

data class KonuDagilimi(
    val konuId: String?,
    val dersAdi: String,
    val konuAdi: String,
    val sayi: Int,
)

private data class KarneDagilimi(
    val nedenler: List<DagilimSatiri>,
    val konular: List<KonuDagilimi>,
)

private data class KarneVerisi(
    val sayilar: KarneSayilari,
    val ozet: IlerlemeOzeti?,
    val yeniRozetler: List<Rozet>,
    val hedef: Int,
)

@Composable
fun KarneEkrani(
    sorular: List<Soru>,
    dersler: Map<String, Ders>,
    konuRepository: KonuRepository,
    sonuclar: List<OturumSonucu>,
    ilerlemeHizmeti: IlerlemeHizmeti,
    rozetDeposu: RozetDeposu,
    rozetHizmeti: RozetHizmeti,
    gunlukHedefDeposu: GunlukHedefDeposu,
    onKonudanCalis: (String) -> Unit,
    onFiltrelereDon: () -> Unit,
    onBugun: () -> Unit,
) {
    val context = LocalContext.current
    val sayilar = remember(sonuclar) { KarneHesabi.hesapla(sonuclar) }
    val konuAkimI = remember(konuRepository) { konuRepository.tumunuIzle() }
    val konular = produceState<Map<String, Konu>>(emptyMap(), konuAkimI) {
        konuAkimI.catch { emit(emptyList()) }.collect { liste ->
            value = liste.associateBy { it.id }
        }
    }.value
    val dagilim = remember(sorular, sonuclar, dersler, konular) {
        dagilimHesapla(sorular, sonuclar, dersler, konular)
    }
    val animasyonAcik = LocalHareketPolitikasi.current.hareketAcik
    var yenileme by remember { mutableIntStateOf(0) }
    var ekBilgiHatasi by remember { mutableStateOf(false) }

    val durum = produceState<KarneVerisi?>(null, sorular, sonuclar, yenileme) {
        ekBilgiHatasi = false
        value = try {
            val ozet = ilerlemeHizmeti.ozet()
            val yeni = rozetHizmeti.degerlendir(sonuclar)
            KarneVerisi(
                sayilar = sayilar,
                ozet = ozet,
                yeniRozetler = yeni,
                hedef = gunlukHedefDeposu.hedef(),
            )
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            ekBilgiHatasi = true
            null
        }
    }.value

    val ozet = durum?.ozet
    val konusuzEtiket = stringResource(R.string.karne_no_topic)
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.karne_kicker),
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.primary,
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.4.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(10.dp))
                Baslik(sayilar, animasyonAcik)
                Spacer(Modifier.height(18.dp))
                OranHalkasi(
                    sayilar = sayilar,
                    animasyonAcik = animasyonAcik,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.karne_partial_explainer),
                    modifier = Modifier.padding(top = 4.dp),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                KovaBolumu(sayilar, animasyonAcik)
                if (dagilim.nedenler.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    BolumKarti(baslik = stringResource(R.string.karne_reasons_title)) {
                        dagilim.nedenler.forEach { satir ->
                            DagilimCubugu(satir)
                        }
                    }
                }
                if (dagilim.konular.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    BolumKarti(baslik = stringResource(R.string.karne_topics_title)) {
                        dagilim.konular.forEach { konu ->
                            KonuSatiri(
                                konu = konu,
                                konusuzEtiket = konusuzEtiket,
                                onKonudanCalis = onKonudanCalis,
                            )
                        }
                    }
                }
                if (ozet != null) {
                    Spacer(Modifier.height(16.dp))
                    HaftaBolumu(ozet)
                    Spacer(Modifier.height(16.dp))
                    IlerlemeBolumu(ozet, durum?.hedef ?: 0)
                    Spacer(Modifier.height(16.dp))
                    SonYediGunBolumu(ozet)
                }
                if (durum != null && durum.yeniRozetler.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    KarneRozetBolumu(durum.yeniRozetler, rozetDeposu)
                }
                if (ekBilgiHatasi) {
                    Text(stringResource(R.string.karne_progress_error), Modifier.padding(top = 12.dp))
                    TextButton(onClick = { yenileme++ }) { Text(stringResource(R.string.level_retry)) }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onFiltrelereDon,
                ) {
                    Text(stringResource(R.string.karne_to_filters))
                }
                TextButton(onClick = onBugun) {
                    Text(stringResource(R.string.menu_today))
                }
                Spacer(Modifier.height(12.dp))
            }
            if (animasyonAcik && sayilar.oran >= KONFETI_ESIGI) {
                Konfeti(
                    renkler = listOf(colors.primary, colors.secondary, colors.tertiary, colors.primaryContainer),
                )
            }
        }
    }
}

private const val KONFETI_ESIGI = 70

@Composable
private fun Baslik(sayilar: KarneSayilari, animasyonAcik: Boolean) {
    val toplam = sayanDeger(sayilar.toplam, animasyonAcik, 0)
    val bagimsiz = sayanDeger(sayilar.bagimsiz, animasyonAcik, 150)
    val colors = MaterialTheme.colorScheme
    if (sayilar.kisaOturum) {
        Text(
            text = stringResource(R.string.karne_short_title),
            modifier = Modifier.fillMaxWidth(),
            color = colors.onBackground,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.karne_short_description, toplam, bagimsiz),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    } else {
        Text(
            text = stringResource(R.string.karne_headline, toplam, bagimsiz),
            modifier = Modifier.fillMaxWidth(),
            color = colors.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
    }
    Text(
        text = stringResource(tebrikMetni(KarneHesabi.tebrikSeviyesi(sayilar))),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        color = colors.onSurface,
        style = MaterialTheme.typography.bodyLarge,
    )
}

private fun tebrikMetni(seviye: TebrikSeviyesi): Int = when (seviye) {
    TebrikSeviyesi.KISA_OTURUM -> R.string.karne_greeting_short
    TebrikSeviyesi.HARIKA -> R.string.karne_greeting_high
    TebrikSeviyesi.IYI -> R.string.karne_greeting_good
    TebrikSeviyesi.ORTA -> R.string.karne_greeting_mid
    TebrikSeviyesi.DUSUK -> R.string.karne_greeting_low
}

@Composable
private fun sayanDeger(hedef: Int, animasyonAcik: Boolean, gecikmeMs: Int): Int {
    if (!animasyonAcik) return hedef
    var basla by remember(hedef, animasyonAcik) { mutableStateOf(!animasyonAcik) }
    LaunchedEffect(hedef, animasyonAcik) {
        if (animasyonAcik) {
            delay(gecikmeMs.toLong())
            basla = true
        }
    }
    val deger by animateIntAsState(
        targetValue = if (basla) hedef else 0,
        animationSpec = tween(durationMillis = LocalHareketPolitikasi.current.sure(HareketTuru.DEGER), easing = FastOutSlowInEasing),
        label = "karne-sayac",
    )
    return deger
}

@Composable
private fun OranHalkasi(sayilar: KarneSayilari, animasyonAcik: Boolean) {
    val colors = MaterialTheme.colorScheme
    val hedefOran = sayilar.oran / 100f
    val sure = LocalHareketPolitikasi.current.sure(HareketTuru.GRAFIK)
    val ilerleme = remember { Animatable(if (animasyonAcik) 0f else hedefOran) }
    LaunchedEffect(hedefOran, animasyonAcik) {
        if (animasyonAcik) {
            delay(120)
            ilerleme.animateTo(hedefOran, tween(durationMillis = sure, easing = FastOutSlowInEasing))
        } else {
            ilerleme.snapTo(hedefOran)
        }
    }
    val oran = if (animasyonAcik) ilerleme.value else hedefOran
    Box(Modifier.size(174.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val kalinlik = 14.dp.toPx()
            val inset = kalinlik / 2f
            val boyut = androidx.compose.ui.geometry.Size(size.width - kalinlik, size.height - kalinlik)
            drawArc(
                color = colors.surfaceVariant,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = boyut,
                style = Stroke(width = kalinlik, cap = StrokeCap.Round),
            )
            drawArc(
                color = colors.primary,
                startAngle = -90f,
                sweepAngle = 360f * oran,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = boyut,
                style = Stroke(width = kalinlik, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.karne_rate, sayilar.oran),
                color = colors.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.karne_rate_caption),
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun KovaBolumu(sayilar: KarneSayilari, animasyonAcik: Boolean) {
    val colors = MaterialTheme.colorScheme
    val kovalar = if (sayilar.kisaOturum) {
        listOf(
            stringResource(R.string.karne_symbol_independent) to
                stringResource(R.string.karne_bucket_independent, sayilar.bagimsiz),
            stringResource(R.string.karne_symbol_failed) to
                stringResource(R.string.karne_bucket_failed, sayilar.cozemedi),
        )
    } else {
        listOf(
            stringResource(R.string.karne_symbol_independent) to
                stringResource(R.string.karne_bucket_independent, sayilar.bagimsiz),
            stringResource(R.string.karne_symbol_partial) to
                stringResource(R.string.karne_bucket_partial, sayilar.yarim),
            stringResource(R.string.karne_symbol_failed) to
                stringResource(R.string.karne_bucket_failed, sayilar.cozemedi),
            stringResource(R.string.karne_symbol_solution) to
                stringResource(R.string.karne_bucket_solution, sayilar.cozumle),
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        kovalar.chunked(2).forEach { satir ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                satir.forEach { (isaret, etiket) ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surface,
                        tonalElevation = 0.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(isaret, color = colors.primary, style = MaterialTheme.typography.titleMedium)
                            Text(
                                etiket,
                                color = colors.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
                if (satir.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BolumKarti(baslik: String, icerik: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                baslik,
                color = colors.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            icerik()
        }
    }
}

@Composable
private fun DagilimCubugu(satir: DagilimSatiri) {
    val colors = MaterialTheme.colorScheme
    val oran = (satir.sayi / 6f).coerceIn(0.12f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(satir.etiket, color = colors.onSurface, style = MaterialTheme.typography.bodyMedium)
            Text("${satir.sayi}", color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Box(
            Modifier
                .fillMaxWidth(oran)
                .height(8.dp)
                .background(colors.primary.copy(alpha = 0.35f), RoundedCornerShape(4.dp)),
        )
    }
}

@Composable
private fun KonuSatiri(
    konu: KonuDagilimi,
    konusuzEtiket: String,
    onKonudanCalis: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val etiket = listOf(konu.dersAdi, konu.konuAdi.ifBlank { konusuzEtiket })
        .filter { it.isNotBlank() }
        .joinToString(" · ")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(etiket, color = colors.onSurface, style = MaterialTheme.typography.bodyMedium)
            Text(
                stringResource(R.string.karne_topic_count, konu.sayi),
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (konu.konuId != null) {
            TextButton(onClick = { onKonudanCalis(konu.konuId) }) {
                Text(stringResource(R.string.karne_topic_study))
            }
        }
    }
}

@Composable
private fun HaftaBolumu(ozet: IlerlemeOzeti) {
    val colors = MaterialTheme.colorScheme
    val hafta = ozet.hafta
    val buOran = hafta.buHaftaOran
    val gecenOran = hafta.gecenHaftaOran
    val fark = hafta.fark
    BolumKarti(baslik = stringResource(R.string.karne_week_title)) {
        when {
            !hafta.gecenHaftaYeterli -> Text(
                stringResource(R.string.karne_week_not_enough),
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            buOran == null || gecenOran == null -> Text(
                stringResource(R.string.karne_week_no_data),
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            fark != null -> {
                val isaret = when {
                    fark > 0 -> stringResource(R.string.karne_week_diff_plus, fark)
                    fark < 0 -> stringResource(R.string.karne_week_diff_minus, -fark)
                    else -> stringResource(R.string.karne_week_diff_same)
                }
                Text(
                    text = stringResource(R.string.karne_week_compare, buOran, gecenOran, isaret),
                    color = colors.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            else -> Text(
                stringResource(R.string.karne_week_not_enough),
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun IlerlemeBolumu(ozet: IlerlemeOzeti, hedef: Int) {
    val colors = MaterialTheme.colorScheme
    BolumKarti(baslik = stringResource(R.string.karne_progress_title)) {
        Text(
            text = stringResource(R.string.karne_streak, ozet.seri),
            color = colors.onSurface,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.karne_daily_goal, ozet.bugunTekrar, hedef),
            color = colors.onSurface,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = if (ozet.yarinPlanli > 0) {
                stringResource(R.string.karne_tomorrow, ozet.yarinPlanli)
            } else {
                stringResource(R.string.karne_tomorrow_none)
            },
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SonYediGunBolumu(ozet: IlerlemeOzeti) {
    val colors = MaterialTheme.colorScheme
    val gunler = ozet.sonYediGun
    if (gunler.all { it.toplam == 0 }) return
    val enYuksek = gunler.maxOf { it.toplam }.coerceAtLeast(1)
    val formatter = remember { DateTimeFormatter.ofPattern("EEEEE", Locale.forLanguageTag("tr-TR")) }
    BolumKarti(baslik = stringResource(R.string.karne_last_seven_title)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            gunler.forEach { dilim ->
                val oran = dilim.toplam.toFloat() / enYuksek
                val gun = LocalDate.ofEpochDay(dilim.gun.toLong())
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = if (dilim.toplam == 0) "" else "${dilim.toplam}",
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height((6 + 66 * oran).dp)
                            .background(
                                if (dilim.toplam == 0) colors.surfaceVariant
                                else colors.primary.copy(alpha = 0.75f),
                                RoundedCornerShape(6.dp),
                            ),
                    )
                    Text(
                        formatter.format(gun),
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

/** Ownership stays live even if the global observer earns another badge after this report loads. */
@Composable
internal fun KarneRozetBolumu(rozetler: List<Rozet>, depo: RozetDeposu) {
    val sahip by depo.sahipIzle.collectAsState()
    RozetBolumu(rozetler, sahip.size, LocalHareketPolitikasi.current.hareketAcik)
}

@Composable
private fun RozetBolumu(rozetler: List<Rozet>, toplamRozet: Int, animasyonAcik: Boolean) {
    val colors = MaterialTheme.colorScheme
    var buyudu by remember(animasyonAcik) { mutableStateOf(!animasyonAcik) }
    LaunchedEffect(animasyonAcik) {
        if (animasyonAcik) {
            delay(250)
            buyudu = true
        }
    }
    val olcek by animateFloatAsState(
        targetValue = if (buyudu) 1f else 0.6f,
        animationSpec = tween(LocalHareketPolitikasi.current.sure(HareketTuru.GRAFIK), easing = FastOutSlowInEasing),
        label = "rozet-olcek",
    )
    BolumKarti(baslik = stringResource(R.string.karne_badges_title)) {
        rozetler.take(2).forEach { rozet ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RozetGorseli(
                    rozet = rozet,
                    kazanildi = true,
                    modifier = Modifier.size((52 * if (animasyonAcik) olcek else 1f).dp),
                )
                Text(
                    text = stringResource(rozetMetni(rozet)),
                    color = colors.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        if (rozetler.size > 2) {
            Text(
                stringResource(R.string.karne_badge_more, rozetler.size - 2),
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            stringResource(R.string.karne_badge_total, toplamRozet, Rozet.entries.size),
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private fun rozetMetni(rozet: Rozet): Int = rozetAdi(rozet)

private data class KonfetiParcasi(
    val x: Float,
    val gecikme: Float,
    val boyut: Float,
    val hiz: Float,
    val salinim: Float,
    val renkIndex: Int,
)

@Composable
private fun Konfeti(renkler: List<Color>) {
    var goster by remember { mutableStateOf(true) }
    val ilerleme = remember { Animatable(0f) }
    val sure = LocalHareketPolitikasi.current.sure(HareketTuru.KUTLAMA)
    LaunchedEffect(sure) {
        ilerleme.animateTo(1f, tween(durationMillis = sure, easing = LinearEasing))
        goster = false
    }
    if (!goster) return
    val parcaciklar = remember {
        List(42) { i ->
            val x = ((i * 37) % 100) / 100f
            KonfetiParcasi(
                x = x,
                gecikme = ((i * 13) % 100) / 100f * 0.2f,
                boyut = 5f + ((i * 7) % 6),
                hiz = 0.7f + ((i * 11) % 40) / 100f,
                salinim = ((i * 17) % 60) / 100f,
                renkIndex = i % renkler.size,
            )
        }
    }
    Canvas(Modifier.fillMaxWidth().height(240.dp)) {
        val ilerlemeDegeri = ilerleme.value
        parcaciklar.forEach { parcacik ->
            val yerel = ((ilerlemeDegeri - parcacik.gecikme) / (1f - parcacik.gecikme)).coerceIn(0f, 1f)
            if (yerel <= 0f) return@forEach
            val x = size.width * (parcacik.x + parcacik.salinim * 0.06f * yerel)
            val y = size.height * (yerel * (0.9f + parcacik.hiz) - 0.05f)
            val alfa = (1f - yerel).coerceIn(0f, 1f)
            drawCircle(
                color = renkler[parcacik.renkIndex].copy(alpha = 0.85f * alfa),
                radius = parcacik.boyut,
                center = Offset(x, y),
            )
        }
    }
}

private fun dagilimHesapla(
    sorular: List<Soru>,
    sonuclar: List<OturumSonucu>,
    dersler: Map<String, Ders>,
    konular: Map<String, Konu>,
): KarneDagilimi {
    val sonucById = sonuclar.associateBy { it.soruId }
    val zorlananlar = sorular.filter { soru ->
        val sonuc = sonucById[soru.id] ?: return@filter false
        KarneHesabi.kova(sonuc) != KarneKovasi.BAGIMSIZ
    }
    val nedenler = zorlananlar
        .mapNotNull { it.yanlisNedeni?.trim()?.takeIf(String::isNotEmpty) }
        .groupingBy { it }
        .eachCount()
        .filterValues { it >= 2 }
        .entries
        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
        .take(3)
        .map { DagilimSatiri(it.key, it.value) }
    val konularDagilimi = zorlananlar
        .groupBy { it.konuId }
        .map { (konuId, grup) ->
            val ilk = grup.first()
            KonuDagilimi(
                konuId = konuId,
                dersAdi = dersler[ilk.dersId]?.ad.orEmpty(),
                konuAdi = konuId?.let { konular[it]?.ad }.orEmpty(),
                sayi = grup.size,
            )
        }
        .filter { it.sayi >= 2 }
        .sortedWith(compareByDescending<KonuDagilimi> { it.sayi }.thenBy { it.dersAdi }.thenBy { it.konuAdi })
        .take(3)
    return KarneDagilimi(nedenler = nedenler, konular = konularDagilimi)
}

/** Kept for callers that only have epoch millis (not used by the card, but avoids duplicating logic). */
internal fun gunBasi(zamanMs: Long, zoneId: ZoneId): LocalDate =
    Instant.ofEpochMilli(zamanMs).atZone(zoneId).toLocalDate()

package com.keremtulu.questionnote.ui.deneme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.prefs.DenemeDeposu
import com.keremtulu.questionnote.domain.model.DenemeDersSatiri
import com.keremtulu.questionnote.domain.model.DenemeKaydi
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.NetKurali
import com.keremtulu.questionnote.domain.model.dersBarlari
import com.keremtulu.questionnote.domain.model.enZayif
import com.keremtulu.questionnote.domain.model.grafikNoktalari
import com.keremtulu.questionnote.domain.model.karisikKural
import com.keremtulu.questionnote.domain.model.oncekiAyniKural
import com.keremtulu.questionnote.domain.model.yerelTarih
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.premium.PremiumButton
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private val DenemeTarihBicimi: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("tr-TR"))

fun denemeTarihYazi(gun: Int): String = gun.yerelTarih().format(DenemeTarihBicimi)

@Composable
fun DenemeListesiEkrani(
    denemeDeposu: DenemeDeposu,
    dersRepository: DersRepository,
    onGeri: () -> Unit,
    onYeni: () -> Unit,
    onAc: (String) -> Unit,
) {
    val liste by denemeDeposu.kayitlariIzle.collectAsState()
    val dersler by produceState(emptyList<Ders>()) {
        dersRepository.aktifDersleriIzle().collect { value = it }
    }
    val dersAd = remember(dersler) { dersler.associate { it.id to it.ad } }
    Scaffold { insets ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(insets),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                GradyanKart(modifier = Modifier.fillMaxWidth(), borderAnimation = false) {
                    Column {
                        PremiumTextButton(onClick = onGeri) { Text(stringResource(R.string.common_back)) }
                        Text(
                            stringResource(R.string.deneme_kicker),
                            color = PremiumMarkaRenkler.BakirIsik,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            stringResource(R.string.deneme_title),
                            color = PremiumMarkaRenkler.Kagit,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            stringResource(R.string.deneme_list_subtitle),
                            modifier = Modifier.padding(top = 4.dp),
                            color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.82f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            item {
                PremiumButton(onClick = onYeni, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.deneme_new))
                }
            }
            if (liste.isEmpty()) {
                item {
                    Text(stringResource(R.string.deneme_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                item { NetCizgisi(liste) }
                liste.maxWithOrNull(compareBy<DenemeKaydi> { it.gun }.thenBy { it.olusturma })?.let { son ->
                    item { DersNetBarlari(liste, son, dersAd) }
                }
            }
            items(liste, key = { it.id }) { deneme ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onAc(deneme.id) }
                        .padding(vertical = 8.dp),
                ) {
                    Text(deneme.ad, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        denemeTarihYazi(deneme.gun) + " · " +
                            stringResource(R.string.deneme_net_line, "%.2f".format(deneme.toplamNet())),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun DenemeDetayEkrani(
    denemeId: String?,
    denemeDeposu: DenemeDeposu,
    dersRepository: DersRepository,
    soruRepository: SoruRepository,
    onGeri: () -> Unit,
    onSoruEkle: (Ders, String) -> Unit,
) {
    val dersler by produceState(emptyList<Ders>()) {
        dersRepository.aktifDersleriIzle().collect { value = it }
    }
    val mevcut = denemeId?.let { denemeDeposu.getir(it) }
    var ad by remember { mutableStateOf(mevcut?.ad.orEmpty()) }
    var kural by remember { mutableStateOf(mevcut?.kural ?: NetKurali.DORTTE_BIR) }
    var gun by remember { mutableIntStateOf(mevcut?.gun ?: LocalDate.now().toEpochDay().toInt()) }
    val dogru = remember { mutableStateMapOf<String, String>() }
    val yanlis = remember { mutableStateMapOf<String, String>() }
    val soruSayisi = remember { mutableStateMapOf<String, String>() }
    var kayitId by remember { mutableStateOf(mevcut?.id) }
    var hata by remember { mutableStateOf<Int?>(null) }
    var ozet by remember { mutableStateOf<String?>(null) }
    if (mevcut != null && dogru.isEmpty()) {
        mevcut.satirlar.forEach { satir ->
            dogru[satir.dersId] = satir.dogru.toString()
            yanlis[satir.dersId] = satir.yanlis.toString()
            soruSayisi[satir.dersId] = satir.soruSayisi.toString()
        }
    }
    val kayitli = kayitId?.let { denemeDeposu.getir(it) }
    val liste by denemeDeposu.kayitlariIzle.collectAsState()
    val yuklenen = produceState(emptyMap<String, Int>(), kayitli?.ad, dersler) {
        val adKaynak = kayitli?.ad ?: return@produceState
        if (dersler.isEmpty()) return@produceState
        combine(dersler.map { ders ->
            soruRepository.sorulariIzle(ders.id).map { sorular ->
                ders.id to sorular.count { it.kaynak == adKaynak }
            }
        }) { pairs -> pairs.toMap() }.collect { value = it }
    }.value
    Scaffold { insets ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(insets),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                GradyanKart(modifier = Modifier.fillMaxWidth(), borderAnimation = false) {
                    Column {
                        PremiumTextButton(onClick = onGeri) { Text(stringResource(R.string.common_back)) }
                        Text(
                            stringResource(R.string.deneme_form_title),
                            color = PremiumMarkaRenkler.Kagit,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = ad,
                    onValueChange = { ad = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.deneme_name)) },
                    shape = RoundedCornerShape(16.dp),
                )
            }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    PremiumTextButton(onClick = { gun -= 1 }) { Text("‹") }
                    Text(denemeTarihYazi(gun), fontWeight = FontWeight.SemiBold)
                    PremiumTextButton(onClick = { gun += 1 }) { Text("›") }
                }
                if (gun != LocalDate.now().toEpochDay().toInt()) {
                    PremiumTextButton(onClick = { gun = LocalDate.now().toEpochDay().toInt() }) {
                        Text(stringResource(R.string.deneme_today))
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = kural == NetKurali.DORTTE_BIR, onClick = { kural = NetKurali.DORTTE_BIR }, label = { Text(stringResource(R.string.deneme_rule_4)) })
                    FilterChip(selected = kural == NetKurali.UCTE_BIR, onClick = { kural = NetKurali.UCTE_BIR }, label = { Text(stringResource(R.string.deneme_rule_3)) })
                    FilterChip(selected = kural == NetKurali.YOK, onClick = { kural = NetKurali.YOK }, label = { Text(stringResource(R.string.deneme_rule_none)) })
                }
            }
            items(dersler, key = { it.id }) { ders ->
                val s = soruSayisi[ders.id].orEmpty()
                val d = dogru[ders.id].orEmpty()
                val y = yanlis[ders.id].orEmpty()
                val sInt = s.toIntOrNull() ?: 0
                val dInt = d.toIntOrNull() ?: 0
                val yInt = y.toIntOrNull() ?: 0
                val bos = (sInt - dInt - yInt).coerceAtLeast(0)
                val net = kural.net(dInt, yInt)
                val kalan = ((yInt + bos) - (yuklenen[ders.id] ?: 0)).coerceAtLeast(0)
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(ders.ad, fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SayiAlani(stringResource(R.string.deneme_total), s, Modifier.weight(1f)) { soruSayisi[ders.id] = it }
                        SayiAlani(stringResource(R.string.deneme_correct), d, Modifier.weight(1f)) { dogru[ders.id] = it }
                        SayiAlani(stringResource(R.string.deneme_wrong), y, Modifier.weight(1f)) { yanlis[ders.id] = it }
                    }
                    Text(
                        stringResource(R.string.deneme_blank_net, bos, "%.2f".format(net)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (kayitli != null && kalan > 0) {
                        PremiumOutlinedButton(
                            onClick = { onSoruEkle(ders, kayitli.ad) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.deneme_add_photo, kalan))
                        }
                    }
                }
            }
            hata?.let { item { Text(stringResource(it), color = MaterialTheme.colorScheme.error) } }
            ozet?.let { item { Text(it, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) } }
            item {
                PremiumButton(
                    onClick = {
                        val temizAd = ad.trim()
                        if (temizAd.isEmpty()) {
                            hata = R.string.deneme_error_name
                            return@PremiumButton
                        }
                        val satirlar = dersler.map { ders ->
                            DenemeDersSatiri(
                                dersId = ders.id,
                                soruSayisi = soruSayisi[ders.id].orEmpty().toIntOrNull() ?: 0,
                                dogru = dogru[ders.id].orEmpty().toIntOrNull() ?: 0,
                                yanlis = yanlis[ders.id].orEmpty().toIntOrNull() ?: 0,
                            )
                        }.filter { it.soruSayisi > 0 || it.dogru > 0 || it.yanlis > 0 }
                        if (satirlar.isEmpty() || satirlar.any { !it.gecerli() }) {
                            hata = R.string.deneme_error_scores
                            return@PremiumButton
                        }
                        hata = null
                        val id = kayitId ?: UUID.randomUUID().toString()
                        val kayit = DenemeKaydi(
                            id = id,
                            ad = temizAd,
                            kural = kural,
                            olusturma = kayitli?.olusturma ?: System.currentTimeMillis(),
                            gun = gun,
                            satirlar = satirlar,
                        )
                        denemeDeposu.kaydet(kayit)
                        kayitId = id
                        val zayif = kayit.enZayif()
                        val zayifAd = dersler.firstOrNull { it.id == zayif?.dersId }?.ad
                        val onceki = liste.oncekiAyniKural(kayit)
                        val delta = onceki?.let { kayit.toplamNet() - it.toplamNet() }
                        ozet = buildString {
                            append("%.2f".format(kayit.toplamNet()))
                            append(" net.")
                            if (zayifAd != null) append(" En zayıf: ").append(zayifAd).append(".")
                            if (delta != null) {
                                append(" Önceki denemeye göre ")
                                append(if (delta >= 0) "+" else "")
                                append("%.2f".format(delta))
                                append(".")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.deneme_save)) }
            }
            if (kayitId != null) {
                item {
                    PremiumTextButton(onClick = {
                        kayitId?.let { denemeDeposu.sil(it) }
                        onGeri()
                    }) { Text(stringResource(R.string.deneme_delete)) }
                }
            }
        }
    }
}

@Composable
private fun NetCizgisi(liste: List<DenemeKaydi>) {
    val noktalar = liste.grafikNoktalari()
    val cizgi = MaterialTheme.colorScheme.primary
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(R.string.deneme_chart_net), fontWeight = FontWeight.SemiBold)
        if (liste.karisikKural()) {
            Text(stringResource(R.string.deneme_chart_mixed), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (noktalar.size < 2) {
            Text(stringResource(R.string.deneme_chart_need_more), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Canvas(Modifier.fillMaxWidth().height(120.dp)) {
                val nets = noktalar.map { it.toplamNet().toFloat() }
                val min = minOf(0f, nets.min())
                val max = maxOf(nets.max(), min + 1f)
                val dx = if (nets.size == 1) 0f else size.width / (nets.size - 1)
                fun y(v: Float) = size.height - ((v - min) / (max - min)) * size.height
                val path = Path()
                nets.forEachIndexed { i, v ->
                    val x = i * dx
                    if (i == 0) path.moveTo(x, y(v)) else path.lineTo(x, y(v))
                }
                drawPath(path, cizgi, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
                nets.forEachIndexed { i, v ->
                    drawCircle(cizgi, 5.dp.toPx(), Offset(i * dx, y(v)))
                }
            }
        }
    }
}

@Composable
private fun DersNetBarlari(liste: List<DenemeKaydi>, son: DenemeKaydi, dersAd: Map<String, String>) {
    val barlar = liste.dersBarlari(son)
    if (barlar.isEmpty()) return
    val tavan = (barlar.flatMap { listOfNotNull(it.sonNet, it.oncekiOrtalama) }.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    val sonRenk = MaterialTheme.colorScheme.primary
    val onceRenk = MaterialTheme.colorScheme.outline
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.deneme_chart_lessons), fontWeight = FontWeight.SemiBold)
        Row(Modifier.fillMaxWidth().height(110.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            barlar.forEach { bar ->
                Column(Modifier.width(56.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                        Row(Modifier.fillMaxHeight(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                            Box(
                                Modifier.width(14.dp).fillMaxHeight((bar.sonNet / tavan).toFloat().coerceIn(0.04f, 1f)),
                            ) {
                                Canvas(Modifier.fillMaxSize()) { drawRect(sonRenk) }
                            }
                            bar.oncekiOrtalama?.let { once ->
                                Box(
                                    Modifier.width(14.dp).fillMaxHeight((once / tavan).toFloat().coerceIn(0.04f, 1f)),
                                ) {
                                    Canvas(Modifier.fillMaxSize()) { drawRect(onceRenk) }
                                }
                            }
                        }
                    }
                    Text(
                        dersAd[bar.dersId] ?: "—",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Text(stringResource(R.string.deneme_chart_legend), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SayiAlani(etiket: String, deger: String, modifier: Modifier = Modifier, onDegis: (String) -> Unit) {
    OutlinedTextField(
        value = deger,
        onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 3) onDegis(it) },
        modifier = modifier,
        singleLine = true,
        label = { Text(etiket) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
    )
}

package com.keremtulu.questionnote.ui.deneme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.prefs.DenemeDeposu
import com.keremtulu.questionnote.domain.model.DenemeDersSatiri
import com.keremtulu.questionnote.domain.model.DenemeKaydi
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.NetKurali
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.premium.PremiumButton
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import java.util.UUID
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@Composable
fun DenemeListesiEkrani(
    denemeDeposu: DenemeDeposu,
    onGeri: () -> Unit,
    onYeni: () -> Unit,
    onAc: (String) -> Unit,
) {
    val liste by denemeDeposu.kayitlariIzle.collectAsState()
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
    val dogru = remember { mutableStateMapOf<String, String>() }
    val yanlis = remember { mutableStateMapOf<String, String>() }
    val soruSayisi = remember { mutableStateMapOf<String, String>() }
    var kayitId by remember { mutableStateOf(mevcut?.id) }
    var hata by remember { mutableStateOf<Int?>(null) }
    if (mevcut != null && dogru.isEmpty()) {
        mevcut.satirlar.forEach { satir ->
            dogru[satir.dersId] = satir.dogru.toString()
            yanlis[satir.dersId] = satir.yanlis.toString()
            soruSayisi[satir.dersId] = satir.soruSayisi.toString()
        }
    }
    val kayitli = kayitId?.let { denemeDeposu.getir(it) }
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
                        denemeDeposu.kaydet(
                            DenemeKaydi(
                                id = id,
                                ad = temizAd,
                                kural = kural,
                                olusturma = kayitli?.olusturma ?: System.currentTimeMillis(),
                                satirlar = satirlar,
                            ),
                        )
                        kayitId = id
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

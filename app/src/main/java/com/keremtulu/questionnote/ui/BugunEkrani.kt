package com.keremtulu.questionnote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.ilerleme.IlerlemeHizmeti
import com.keremtulu.questionnote.data.prefs.GunlukHedefDeposu
import com.keremtulu.questionnote.domain.model.DenemeKaydi
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.repeat.GunlukCalismaPlani
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository
import com.keremtulu.questionnote.ui.animasyon.SureToken
import com.keremtulu.questionnote.ui.animasyon.animateFloatPremium
import com.keremtulu.questionnote.ui.animasyon.tweenPremium
import com.keremtulu.questionnote.ui.bilesenler.InceOk
import com.keremtulu.questionnote.ui.bilesenler.InceOlcek
import com.keremtulu.questionnote.ui.bilesenler.KucukArti
import com.keremtulu.questionnote.ui.bilesenler.KucukOnay
import com.keremtulu.questionnote.ui.bilesenler.OynatSimgesi
import com.keremtulu.questionnote.ui.bilesenler.SadeKart
import com.keremtulu.questionnote.ui.bilesenler.SoruIsareti
import com.keremtulu.questionnote.ui.bilesenler.hatirlaYerelGun
import com.keremtulu.questionnote.ui.bilesenler.premium.BosDurumSoruYok
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.bilesenler.premium.KutlamaParcaciklari
import com.keremtulu.questionnote.ui.deneme.netYazi
import com.keremtulu.questionnote.ui.panel.ekranPencereInsetsi
import com.keremtulu.questionnote.ui.premium.EtkilesimTuru
import com.keremtulu.questionnote.ui.premium.PremiumButton
import com.keremtulu.questionnote.ui.premium.PremiumGradyanlar
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

private data class BugunSoruSatiri(
    val soru: Soru,
    val ders: Ders,
)

private sealed interface BugunDurumu {
    data object Yukleniyor : BugunDurumu
    data class Hazir(val sorular: List<BugunSoruSatiri>) : BugunDurumu
    data object Hata : BugunDurumu
}

/**
 * Bugün ekranı: tek bir karşılama başlığı, günün odağını taşıyan tek koyu kart ve
 * ardından sıra/kısayol kartları. Durum çubuğu ve günlük hedef üst paneldedir; burada
 * aynı bilgi ikinci kez büyük başlık olarak tekrarlanmaz.
 */
@Composable
fun BugunEkrani(
    soruRepository: SoruRepository,
    dersRepository: DersRepository,
    ilerlemeHizmeti: IlerlemeHizmeti,
    gunlukHedefDeposu: GunlukHedefDeposu,
    onSoruEkle: () -> Unit,
    onOturumBaslat: (List<Soru>, Map<String, Ders>) -> Unit,
    onDevam: (() -> Unit)? = null,
    onDeneme: () -> Unit = {},
    onSorulacaklar: () -> Unit = {},
    denemeler: List<DenemeKaydi> = emptyList(),
) {
    val today = hatirlaYerelGun()
    val bugunGun = today.toEpochDay().toInt()
    var yenileme by remember { mutableIntStateOf(0) }
    val questionsFlow = remember(soruRepository, bugunGun, yenileme) {
        soruRepository.bugunTekrarlanacaklariIzle(bugunGun)
    }
    val lessonsFlow = remember(dersRepository) { dersRepository.aktifDersleriIzle() }
    val sorulacaklarFlow = remember(soruRepository, lessonsFlow, yenileme) {
        combine(soruRepository.ogretmeneSorulacaklariIzle(), lessonsFlow) { sorular, dersler ->
            val dersIdleri = dersler.associateBy { it.id }
            sorular.groupBy { it.dersId }
                .mapNotNull { (dersId, liste) -> dersIdleri[dersId]?.let { it to liste.size } }
                .sortedWith(compareBy<Pair<Ders, Int>> { it.first.sira }.thenBy { it.first.ad })
        }
    }
    val sorulacakGruplar by produceState<List<Pair<Ders, Int>>>(emptyList(), sorulacaklarFlow) {
        sorulacaklarFlow.catch { emit(emptyList()) }.collect { value = it }
    }
    val homeFlow = remember(questionsFlow, lessonsFlow) {
        combine(questionsFlow, lessonsFlow) { sorular, dersler ->
            val dersIdleri = dersler.associateBy { it.id }
            sorular.mapNotNull { soru ->
                dersIdleri[soru.dersId]?.let { ders -> BugunSoruSatiri(soru, ders) }
            }
        }
    }
    val durum by produceState<BugunDurumu>(BugunDurumu.Yukleniyor, homeFlow) {
        homeFlow.catch { value = BugunDurumu.Hata }
            .collect { value = BugunDurumu.Hazir(it) }
    }
    val bugunAkisi = remember(ilerlemeHizmeti, bugunGun) { ilerlemeHizmeti.bugunSayisiIzle(bugunGun) }
    val gunluk by produceState(0, bugunAkisi) {
        bugunAkisi.catch { emit(0) }.collect { value = it }
    }
    // Üst panel hedefi canlı izler; Bugün de aynı akışı kullanır ki iki yüzey çelişmesin.
    val hedefAkisi = remember(gunlukHedefDeposu) { gunlukHedefDeposu.izle() }
    val hedef by produceState(gunlukHedefDeposu.hedef().coerceAtLeast(1), hedefAkisi) {
        hedefAkisi.catch { }.collect { value = it.coerceAtLeast(1) }
    }
    val hazir = durum as? BugunDurumu.Hazir
    val plan = remember(hazir?.sorular, today, hedef, gunluk) {
        hazir?.let { gun ->
            GunlukCalismaPlani.olustur(gun.sorular.map { it.soru }, bugunGun, hedef, gunluk)
        }
    }
    val dersler = remember(hazir) { hazir?.sorular?.associate { it.ders.id to it.ders }.orEmpty() }
    val satirlar = remember(hazir) { hazir?.sorular?.associateBy { it.soru.id }.orEmpty() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = ekranPencereInsetsi(),
    ) { insets ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets),
            contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "bugun-baslik") {
                // Boş havuzda tek ekleme daveti kartta durur; başlıktaki + ikinci kez çağırmaz.
                BugunBasligi(
                    durum = durum,
                    geciken = plan?.geciken,
                    onSoruEkle = onSoruEkle,
                    ekleGoster = !(hazir != null && hazir.sorular.isEmpty()),
                )
            }
            if (durum is BugunDurumu.Hata) {
                // Hata durumunda odak kartı yerine tek, eyleme dönük kart: aynı metin iki kez yazılmaz.
                item(key = "bugun-hata") {
                    DurumKarti(
                        metin = stringResource(R.string.today_load_error),
                        onRetry = { yenileme++ },
                    )
                }
            } else if (hazir != null && hazir.sorular.isEmpty()) {
                // Boş havuzda sıfırlı bir kahraman kart göstermek yerine tek, dürüst bir davet.
                item(key = "bugun-bos") { BosDurumSoruYok(onSoruEkle) }
            } else {
                item(key = "bugun-odak") {
                    BugunOdakKarti(
                        hedef = hedef,
                        yapilan = gunluk,
                        durum = durum,
                        turSorulari = plan?.tur.orEmpty(),
                        dersler = dersler,
                        onOturumBaslat = onOturumBaslat,
                    )
                }
                if (plan != null && hazir != null) {
                    item(key = "bugun-kuyruk") {
                        BugunKuyrukKarti(
                            plan = plan,
                            satirlar = satirlar,
                            today = today,
                            onSoru = { satir ->
                                onOturumBaslat(listOf(satir.soru), mapOf(satir.ders.id to satir.ders))
                            },
                        )
                    }
                }
            }
            onDevam?.let { devam ->
                item(key = "bugun-devam") { DevamSatiri(devam) }
            }
            item(key = "bugun-kisayollar") {
                BugunKisayollari(
                    sorulacakGruplar = sorulacakGruplar,
                    denemeler = denemeler,
                    onSorulacaklar = onSorulacaklar,
                    onDeneme = onDeneme,
                )
            }
        }
    }
}

@Composable
private fun BugunBasligi(
    durum: BugunDurumu,
    geciken: Int?,
    onSoruEkle: () -> Unit,
    ekleGoster: Boolean,
) {
    // Sayı odak kartında bir kez büyük yazılır. Başlıkta yalnız başka yerde olmayan
    // bilgi kalır: yükleniyor ya da kaç sorunun geciktiği. Boş/hata metni kartında durur.
    val altSatir = when (durum) {
        BugunDurumu.Yukleniyor -> stringResource(R.string.today_loading_count)
        BugunDurumu.Hata -> null
        is BugunDurumu.Hazir -> geciken?.takeIf { it > 0 }?.let {
            pluralStringResource(R.plurals.today_overdue_count, it, it)
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.today_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics { heading() },
            )
            if (altSatir != null) {
                Text(
                    text = altSatir,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (ekleGoster) EkleDugmesi(onClick = onSoruEkle)
    }
}

/** Tek soru ekleme eylemi: başlığın yanında, akışın üstünde; içeriği örten FAB yok. */
@Composable
private fun EkleDugmesi(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val etiket = stringResource(R.string.today_add_question)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(38.dp).background(colors.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            KucukArti(
                modifier = Modifier.size(16.dp).semantics { contentDescription = etiket },
                renk = colors.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun BugunOdakKarti(
    hedef: Int,
    yapilan: Int,
    durum: BugunDurumu,
    turSorulari: List<Soru>,
    dersler: Map<String, Ders>,
    onOturumBaslat: (List<Soru>, Map<String, Ders>) -> Unit,
) {
    val kagit = PremiumMarkaRenkler.Kagit
    val bakir = PremiumMarkaRenkler.BakirIsik
    val guvenliHedef = hedef.coerceAtLeast(1)
    val tamamlandi = yapilan >= guvenliHedef
    // Hedefin yalnız yeni aşıldığı anda tek bir kutlama; ekrana dönüşte tekrar oynamaz.
    var oncekiTamam by remember { mutableStateOf(tamamlandi) }
    var kutlama by remember { mutableStateOf(false) }
    LaunchedEffect(tamamlandi) {
        if (tamamlandi && !oncekiTamam) {
            kutlama = true
            delay(2_200)
            kutlama = false
        }
        oncekiTamam = tamamlandi
    }
    var olcekGoster by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { olcekGoster = true }
    val oran = (yapilan.toFloat() / guvenliHedef).coerceIn(0f, 1f)
    val olcek = animateFloatPremium(
        target = if (olcekGoster) oran else 0f,
        animationSpec = tweenPremium(SureToken.VERY_SLOW),
        label = "bugun-hedef-olcegi",
    )
    val hazir = durum as? BugunDurumu.Hazir
    val adet = hazir?.sorular?.size
    val mesaj = when {
        durum is BugunDurumu.Yukleniyor -> stringResource(R.string.today_loading_count)
        durum is BugunDurumu.Hata -> stringResource(R.string.today_load_error)
        adet == 0 -> stringResource(R.string.today_focus_empty)
        else -> stringResource(R.string.today_focus_message)
    }
    Box(Modifier.fillMaxWidth()) {
        GradyanKart(
            modifier = Modifier.fillMaxWidth(),
            gradyan = PremiumGradyanlar.KAHRAMAN_GRADYAN,
            shape = RoundedCornerShape(26.dp),
            borderAnimation = false,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.today_focus_label),
                        color = bakir,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp,
                        modifier = Modifier.weight(1f),
                    )
                    if (tamamlandi) {
                        KucukOnay(Modifier.size(18.dp).clearAndSetSemantics { }, bakir)
                    }
                }
                if (adet != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = adet.toString(),
                            color = kagit,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.today_question_unit),
                            color = kagit.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                Text(
                    text = mesaj,
                    color = kagit.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                // Yüklenirken hedef/CTA iskeleti gösterilmez: yanlış "0 soru" düğmesi doğmaz.
                if (adet != null) {
                    OdakHedefiVeEylemi(
                        yapilan = yapilan,
                        hedef = guvenliHedef,
                        oran = olcek.value,
                        turSorulari = turSorulari,
                        dersler = dersler,
                        onOturumBaslat = onOturumBaslat,
                    )
                }
            }
        }
        KutlamaParcaciklari(visible = kutlama, modifier = Modifier.matchParentSize())
    }
}

/** Kahraman kartın alt yarısı: günlük hedefin ince ölçeği ve tek birincil eylem. */
@Composable
private fun OdakHedefiVeEylemi(
    yapilan: Int,
    hedef: Int,
    oran: Float,
    turSorulari: List<Soru>,
    dersler: Map<String, Ders>,
    onOturumBaslat: (List<Soru>, Map<String, Ders>) -> Unit,
) {
    val kagit = PremiumMarkaRenkler.Kagit
    val bakir = PremiumMarkaRenkler.BakirIsik
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.today_goal_title),
                color = bakir,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
            )
            // Sayı üst panelde zaten var; burada yeni bilgi kalsın: kaç soru kaldığı.
            Text(
                text = if (yapilan >= hedef) {
                    stringResource(R.string.today_goal_met)
                } else {
                    stringResource(R.string.today_goal_left, hedef - yapilan)
                },
                color = kagit.copy(alpha = 0.82f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        InceOlcek(
            oran = oran,
            modifier = Modifier.fillMaxWidth().height(8.dp).clearAndSetSemantics { },
            izRenk = Color.White.copy(alpha = 0.16f),
            dolguRenk = bakir,
            bolme = 10,
            yukseklik = 8.dp,
        )
    }
    PremiumButton(
        onClick = { onOturumBaslat(turSorulari, dersler) },
        enabled = turSorulari.isNotEmpty(),
        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bakir,
            contentColor = PremiumMarkaRenkler.Gece,
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        interactionTur = EtkilesimTuru.ODAK_BASLADI,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = stringResource(R.string.focus_start_today),
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.focus_start_count, turSorulari.size),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            InceOk(Modifier.size(18.dp).clearAndSetSemantics { }, PremiumMarkaRenkler.Gece)
        }
    }
}

@Composable
private fun DevamSatiri(onDevam: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onDevam,
        shape = RoundedCornerShape(18.dp),
        color = colors.secondaryContainer.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OynatSimgesi(Modifier.size(18.dp), colors.onSecondaryContainer)
            Text(
                text = stringResource(R.string.focus_continue_session),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSecondaryContainer,
            )
            InceOk(Modifier.size(16.dp), colors.onSecondaryContainer)
        }
    }
}

@Composable
private fun BugunKuyrukKarti(
    plan: GunlukCalismaPlani,
    satirlar: Map<String, BugunSoruSatiri>,
    today: LocalDate,
    onSoru: (BugunSoruSatiri) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    SadeKart(shape = RoundedCornerShape(22.dp)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.today_section_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = plan.sirali.size.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                )
            }
            plan.onizleme.forEachIndexed { index, soru ->
                val satir = satirlar[soru.id] ?: return@forEachIndexed
                KuyrukSatiri(satir = satir, today = today) { onSoru(satir) }
                if (index != plan.onizleme.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = colors.outlineVariant.copy(alpha = 0.45f),
                    )
                }
            }
            if (plan.kalan > 0) {
                Text(
                    text = stringResource(R.string.today_queue_turn, plan.tur.size, plan.kalan),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            } else {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun KuyrukSatiri(
    satir: BugunSoruSatiri,
    today: LocalDate,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val dueDay = satir.soru.sonrakiTekrarGun ?: today.toEpochDay().toInt()
    val gecikme = (today.toEpochDay().toInt() - dueDay).coerceAtLeast(0)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = satir.ders.ad,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (gecikme > 0) {
                    Text(
                        text = pluralStringResource(R.plurals.today_overdue_days, gecikme, gecikme),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.error,
                        maxLines = 1,
                    )
                }
            }
            Text(
                text = satir.soru.soruNotu?.trim()?.takeIf { it.isNotEmpty() }
                    ?: satir.soru.kaynak?.trim()?.takeIf { it.isNotEmpty() }
                    ?: stringResource(R.string.question_photo_only),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        InceOk(Modifier.size(16.dp), colors.onSurfaceVariant)
    }
}

@Composable
private fun BugunKisayollari(
    sorulacakGruplar: List<Pair<Ders, Int>>,
    denemeler: List<DenemeKaydi>,
    onSorulacaklar: () -> Unit,
    onDeneme: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    SadeKart(shape = RoundedCornerShape(22.dp)) {
        Column {
            if (sorulacakGruplar.isNotEmpty()) {
                SorulacaklarSatiri(gruplar = sorulacakGruplar, onClick = onSorulacaklar)
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = colors.outlineVariant.copy(alpha = 0.45f),
                )
            }
            DenemeSatiri(denemeler = denemeler, onClick = onDeneme)
        }
    }
}

@Composable
private fun SorulacaklarSatiri(
    gruplar: List<Pair<Ders, Int>>,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val dersOzeti = remember(gruplar) {
        gruplar.joinToString(" · ") { (ders, adet) -> "${ders.ad} ($adet)" }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bugun_sorulacaklar_karti")
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(38.dp).background(colors.secondaryContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            SoruIsareti(Modifier.size(18.dp), colors.onSecondaryContainer)
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(R.string.today_ask_teacher_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = dersOzeti,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        InceOk(Modifier.size(16.dp), colors.onSurfaceVariant)
    }
}

@Composable
private fun DenemeSatiri(
    denemeler: List<DenemeKaydi>,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val sonDeneme = denemeler.firstOrNull()
    val altMetin = if (sonDeneme == null) {
        stringResource(R.string.today_deneme_subtitle_empty)
    } else {
        stringResource(
            R.string.today_deneme_subtitle_summary,
            denemeler.size,
            sonDeneme.ad,
            netYazi(sonDeneme.toplamNet()),
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today-deneme-card")
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(38.dp).background(colors.primaryContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            com.keremtulu.questionnote.ui.bilesenler.HedefHalkasi(
                oran = 0.72f,
                modifier = Modifier.size(18.dp),
                izRenk = colors.primary.copy(alpha = 0.25f),
                dolguRenk = colors.primary,
                kalinlik = 2.dp,
                isaretSayisi = 12,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(R.string.deneme_open),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = altMetin,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        InceOk(Modifier.size(16.dp), colors.onSurfaceVariant)
    }
}

@Composable
private fun DurumKarti(
    metin: String,
    onRetry: () -> Unit,
) {
    SadeKart(shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(metin, color = MaterialTheme.colorScheme.onSurface)
            PremiumTextButton(onClick = onRetry) {
                Text(stringResource(R.string.topic_retry))
            }
        }
    }
}

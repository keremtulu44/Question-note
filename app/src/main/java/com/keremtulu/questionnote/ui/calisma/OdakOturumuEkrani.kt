package com.keremtulu.questionnote.ui.calisma

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import com.keremtulu.questionnote.ui.premium.EtkilesimTuru
import com.keremtulu.questionnote.ui.premium.PremiumButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumSwitch
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.prefs.OdakOturumuDeposu
import com.keremtulu.questionnote.data.ses.SesTuruGenis
import com.keremtulu.questionnote.data.ses.SesYoneticisi
import com.keremtulu.questionnote.domain.calisma.MonotonikSaat
import com.keremtulu.questionnote.domain.calisma.OlcumKalitesi
import com.keremtulu.questionnote.domain.calisma.OlcumNedeni
import com.keremtulu.questionnote.domain.calisma.TekrarOnizlemesi
import com.keremtulu.questionnote.ui.bilesenler.GorselHazirligi
import android.os.SystemClock
import com.keremtulu.questionnote.ui.tema.LocalHareketPolitikasi
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.domain.karne.KarneHesabi
import com.keremtulu.questionnote.domain.karne.OturumSonucu
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.GorselTuru
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.SoruGorseli
import com.keremtulu.questionnote.domain.model.SoruIslemSonucu
import com.keremtulu.questionnote.domain.model.TekrarKaydi
import com.keremtulu.questionnote.domain.model.TekrarSonucu
import com.keremtulu.questionnote.domain.repeat.AkilliTekrarKurali
import com.keremtulu.questionnote.domain.repository.SoruRepository
import com.keremtulu.questionnote.ui.bilesenler.SoruGorselResmi
import com.keremtulu.questionnote.ui.bilesenler.SoruGorselTamEkran
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** A focused review surface; no archive/delete controls, no forced solution reveal, no speed score. */
@Composable
fun OdakOturumuEkrani(
    soru: Soru,
    ders: Ders,
    repository: SoruRepository,
    gorselDeposu: LocalSoruGorselDeposu,
    oturumId: String,
    oturumDeposu: OdakOturumuDeposu,
    disDuraklatildi: Boolean = false,
    sira: Int,
    toplam: Int,
    sonuclar: List<OturumSonucu>,
    cozumOnceAcildi: Boolean,
    onCozumAcildi: () -> Unit,
    onGeri: () -> Unit,
    onSonuc: (TekrarSonucu, Boolean) -> Unit,
    onKaydedildi: (soruId: String, sonrakiGun: Int?) -> Unit = { _, _ -> },
    onSonaBirak: () -> Unit,
    onAtla: () -> Unit,
    monotonikSaat: MonotonikSaat = MonotonikSaat { SystemClock.elapsedRealtime() },
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var ambiyansAcik by remember(oturumId) { mutableStateOf(false) }
    val liste = rememberLazyListState()
    val olcum = rememberOdakOlcum(oturumId, soru.id, cozumOnceAcildi, oturumDeposu, monotonikSaat)
    val cozumAcildi = olcum.nokta.cozumAcildi
    val degerlendirme = olcum.nokta.donduruldu
    val secim = olcum.nokta.secim
    var gorselDurumlari by remember(soru.id) { mutableStateOf<Map<String, GorselHazirligi>>(emptyMap()) }
    var soruGoruldu by remember(soru.id) { mutableStateOf(!soru.soruNotu.isNullOrBlank()) }
    var planHatasi by remember(soru.id) { mutableStateOf(false) }
    var planYenilendi by remember(soru.id) { mutableStateOf(false) }
    var mola by rememberSaveable(soru.id) { mutableStateOf(false) }
    var cozumGorunur by rememberSaveable(soru.id) { mutableStateOf(false) }
    var kaydediliyor by remember(soru.id) { mutableStateOf(false) }
    var cozumAciliyor by remember(soru.id) { mutableStateOf(false) }
    var cozumKayitHatasi by remember(soru.id) { mutableStateOf(false) }
    val mesgul = kaydediliyor || cozumAciliyor
    var kayitHatasi by remember(soru.id) { mutableStateOf(false) }
    var tekrarli by remember(soru.id) { mutableStateOf(false) }
    var kullanilamaz by remember(soru.id) { mutableStateOf(false) }
    var gorsel by remember(soru.id) { mutableStateOf<SoruGorseli?>(null) }
    var yenileme by remember(soru.id) { mutableIntStateOf(0) }
    var yuklemeHatasi by remember(soru.id) { mutableStateOf(false) }
    val gecmis by produceState<List<TekrarKaydi>?>(null, soru.id, yenileme) {
        yuklemeHatasi = false
        value = try {
            repository.tekrarGecmisiniIzle(soru.id).first()
        } catch (iptal: CancellationException) {
            throw iptal
        } catch (_: Exception) {
            yuklemeHatasi = true
            null
        }
    }
    val oncekiKayitlar = gecmis
    val yakinGecmis = remember(oncekiKayitlar, olcum.yenidenOnPlanda, degerlendirme) {
        com.keremtulu.questionnote.domain.repeat.YakinGecmisHesabi.hesapla(oncekiKayitlar.orEmpty(),
            java.time.LocalDate.now().toEpochDay().toInt())
    }
    val sayilar = remember(sonuclar) { KarneHesabi.hesapla(sonuclar) }
    val cozumVar = !soru.cozumNotu.isNullOrBlank() || soru.gorseller.any { it.tur == GorselTuru.COZUM }
    val colors = MaterialTheme.colorScheme

    DisposableEffect(oturumId, ambiyansAcik, mola, disDuraklatildi) {
        val sesYoneticisi = SesYoneticisi.uygulama(context)
        val oynat = ambiyansAcik && !mola && !disDuraklatildi
        if (oynat) sesYoneticisi.ambiyansBaslat(SesTuruGenis.AMBIYANS_ODAK)
        else sesYoneticisi.ambiyansDurdur()
        onDispose { if (oynat) sesYoneticisi.ambiyansDurdur() }
    }

    val gorselHazir = soruGoruldu && gorselDurumlari.values.none { it != GorselHazirligi.HAZIR }
    OdakOlcumIzleyicisi(olcum, oncekiKayitlar != null && gorselHazir && !mola && gorsel == null &&
        !mesgul && !degerlendirme && !disDuraklatildi && !tekrarli && !kullanilamaz)
    val onizlemeler by produceState<Map<TekrarSonucu, TekrarOnizlemesi>?>(null, soru.id, degerlendirme,
        cozumAcildi, olcum.nokta.olcum, oncekiKayitlar, yenileme, olcum.yenidenOnPlanda) {
        value = null
        planHatasi = false
        if (degerlendirme && oncekiKayitlar != null) {
            value = try {
                TekrarSonucu.entries.associateWith { sonuc -> requireNotNull(repository.tekrarOnizle(olcum.deneme(sonuc))) }
            } catch (iptal: CancellationException) { throw iptal } catch (_: Exception) { planHatasi = true; null }
        }
    }

    val hareket = LocalHareketPolitikasi.current
    LaunchedEffect(degerlendirme, hareket) {
        if (degerlendirme) {
            delay(100) // wait for newly composed assessment items; only the scroll motion is optional
            val hedef = (liste.layoutInfo.totalItemsCount - if (sira < toplam) 2 else 1).coerceAtLeast(0)
            if (hareket.azaltildi) liste.scrollToItem(hedef) else liste.animateScrollToItem(hedef)
        }
    }

    fun kaydet() {
        val sonuc = olcum.nokta.secim ?: return
        val onizleme = onizlemeler?.get(sonuc) ?: return
        if (kaydediliyor || cozumAciliyor || !olcum.nokta.donduruldu || oncekiKayitlar == null) return
        if (onizleme.deneme != olcum.deneme(sonuc).guvenli()) {
            planYenilendi = true
            yenileme++
            return
        }
        olcum.kaydet()
        kaydediliyor = true
        kayitHatasi = false
        scope.launch {
            try {
                when (repository.tekrarKaydet(onizleme)) {
                    SoruIslemSonucu.Basarili -> {
                        onKaydedildi(soru.id, onizleme.plan.sonrakiGun)
                        onSonuc(sonuc, cozumAcildi || cozumOnceAcildi)
                    }
                    SoruIslemSonucu.AyniGunTekrarVar -> tekrarli = true
                    SoruIslemSonucu.OnizlemeGuncellendi -> { planYenilendi = true; yenileme++ }
                    SoruIslemSonucu.Bulunamadi, SoruIslemSonucu.PlanDurumuKapali -> kullanilamaz = true
                    else -> kayitHatasi = true
                }
            } catch (iptal: CancellationException) {
                throw iptal
            } catch (_: Exception) {
                kayitHatasi = true
            } finally {
                kaydediliyor = false
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            Surface(color = colors.surface, tonalElevation = 3.dp) {
                Column(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)).padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (olcum.kayitHatasi) Text(stringResource(R.string.focus_checkpoint_error), color = colors.error, style = MaterialTheme.typography.bodySmall)
                    if (planYenilendi) Text(stringResource(R.string.measure_plan_changed), color = colors.primary, style = MaterialTheme.typography.bodySmall)
                    if (planHatasi) {
                        Text(stringResource(R.string.measure_preview_error), color = colors.error, style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { yenileme++ }) { Text(stringResource(R.string.level_retry)) }
                    }
                    if (kayitHatasi) Text(stringResource(R.string.focus_save_error), color = colors.error, style = MaterialTheme.typography.bodySmall)
                    if (tekrarli || kullanilamaz) {
                        Text(stringResource(if (kullanilamaz) R.string.filtered_practice_question_unavailable else R.string.focus_duplicate), style = MaterialTheme.typography.bodySmall)
                        PremiumButton(onClick = { olcum.aktiflik(false); onAtla() }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.focus_skip)) }
                    } else if (!degerlendirme) {
                        PremiumButton(onClick = {
                            if (!gorselHazir) olcum.kismi(OlcumNedeni.GORSEL_EKSIK)
                            olcum.dondur()
                        }, enabled = oncekiKayitlar != null && !mesgul,
                            modifier = Modifier.fillMaxWidth(),
                            interactionTur = EtkilesimTuru.DEGERLENDIRME) { Text(stringResource(R.string.focus_assess)) }
                    } else {
                        Text(stringResource(R.string.focus_plan_note), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                        PremiumButton(onClick = ::kaydet, enabled = secim != null && !mesgul && onizlemeler != null,
                            modifier = Modifier.fillMaxWidth(), interactionTur = EtkilesimTuru.TIKLAMA) {
                            if (kaydediliyor) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text(stringResource(when {
                                kaydediliyor -> R.string.focus_saving
                                sira == toplam -> R.string.focus_save_last
                                else -> R.string.focus_save
                            }), modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }
        },
    ) { insets ->
        LazyColumn(
            Modifier.fillMaxSize().padding(insets),
            state = liste,
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { if (!kaydediliyor && !cozumAciliyor) { olcum.aktiflik(false); onGeri() } }, enabled = !mesgul) { Text(stringResource(R.string.common_back)) }
                    Text(stringResource(R.string.filtered_practice_session_progress, sira, toplam),
                        style = MaterialTheme.typography.labelLarge, color = colors.primary)
                    TextButton(onClick = { if (!kaydediliyor && !cozumAciliyor) { olcum.aktiflik(false); mola = true } }, enabled = !mesgul) { Text(stringResource(R.string.focus_pause)) }
                }
            }
            item {
                Column(
                    Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF173A49), Color(0xFF2C5D69))), RoundedCornerShape(24.dp)).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(stringResource(R.string.focus_kicker), color = Color(0xFFEDD09A), letterSpacing = 2.sp,
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text(ders.ad, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.measure_phase_times,
                        sureMetni(olcum.nokta.olcum.denemeAktifMs), sureMetni(olcum.nokta.olcum.incelemeAktifMs)),
                        color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                    if (olcum.nokta.olcum.kalite != OlcumKalitesi.TAM) Text(stringResource(
                        if (olcum.nokta.olcum.kalite == OlcumKalitesi.KISMI) R.string.measure_partial else R.string.measure_invalid),
                        color = Color(0xFFF0D18E), style = MaterialTheme.typography.labelSmall)
                    Box(Modifier.fillMaxWidth().height(5.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))) {
                        Box(Modifier.fillMaxWidth(((sira - 1).toFloat() / toplam.coerceAtLeast(1)).coerceIn(0f, 1f)).height(5.dp)
                            .background(Color(0xFFF0D18E), RoundedCornerShape(4.dp)))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.focus_done, sonuclar.size), color = Color.White, style = MaterialTheme.typography.labelSmall)
                        Text(stringResource(R.string.focus_effort_xp, sonuclar.size * 5 + sayilar.bagimsiz * 5),
                            color = Color(0xFFF0D18E), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant.copy(alpha = 0.62f)),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(Modifier.weight(1f).padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(stringResource(R.string.focus_ambient_title), fontWeight = FontWeight.SemiBold)
                            Text(stringResource(R.string.focus_ambient_note), style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant)
                        }
                        PremiumSwitch(
                            checked = ambiyansAcik,
                            onCheckedChange = { ambiyansAcik = it },
                            enabled = !mesgul,
                        )
                    }
                }
            }
            if (yakinGecmis.tekrarGucluk) item {
                Card(colors = CardDefaults.cardColors(containerColor = colors.secondaryContainer), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(R.string.focus_difficult), fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.recent_difficulty_hint), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (!soru.soruNotu.isNullOrBlank()) item {
                Text(soru.soruNotu.orEmpty(), style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
            }
            val kaynak = listOfNotNull(soru.kaynak, soru.kaynakKonumu).filter { it.isNotBlank() }.joinToString(" · ")
            if (kaynak.isNotBlank()) item { Text(kaynak, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) }
            items(soru.gorseller.filter { it.tur == GorselTuru.SORU }, key = { it.id }) { image ->
                OdakGorseli(image, gorselDeposu, stringResource(R.string.question_image_description),
                    onDurum = { durum ->
                        gorselDurumlari = if (durum == null) gorselDurumlari - image.id else gorselDurumlari + (image.id to durum)
                        if (durum == GorselHazirligi.HAZIR) soruGoruldu = true
                        if (durum == GorselHazirligi.HATA) olcum.kismi(OlcumNedeni.GORSEL_EKSIK)
                    }) { olcum.tamEkran(); gorsel = image }
            }
            item { Text(stringResource(R.string.focus_image_hint), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant) }
            item {
                PremiumOutlinedButton(onClick = {
                    if (kaydediliyor || cozumAciliyor) return@PremiumOutlinedButton
                    if (cozumGorunur) cozumGorunur = false else {
                        olcum.aktiflik(false)
                        olcum.cozumuAc()
                        cozumAciliyor = true
                        cozumKayitHatasi = false
                        scope.launch {
                            try {
                                if (olcum.cozumIzni()) {
                                    cozumGorunur = true
                                    onCozumAcildi()
                                } else cozumKayitHatasi = true
                            } finally { cozumAciliyor = false }
                        }
                    }
                }, enabled = cozumVar && !mesgul, modifier = Modifier.fillMaxWidth(),
                    interactionTur = if (cozumGorunur) EtkilesimTuru.KART_KAPANIS else EtkilesimTuru.COZUM_ACILDI) {
                    Text(stringResource(if (cozumAciliyor) R.string.measure_solution_preparing
                        else if (cozumGorunur) R.string.question_hide_solution else R.string.focus_reveal))
                }
                if (cozumKayitHatasi) Text(stringResource(R.string.measure_solution_checkpoint_error), color = colors.error,
                    style = MaterialTheme.typography.bodySmall)
                if (!cozumVar) Text(stringResource(R.string.focus_no_solution), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                if (cozumAcildi || cozumOnceAcildi) Text(stringResource(R.string.focus_solution_note), style = MaterialTheme.typography.bodySmall,
                    color = colors.primary, modifier = Modifier.padding(top = 8.dp))
            }
            if (cozumGorunur) {
                if (!soru.cozumNotu.isNullOrBlank()) item {
                    Card(shape = RoundedCornerShape(20.dp)) {
                        Text(soru.cozumNotu.orEmpty(), Modifier.padding(18.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                items(soru.gorseller.filter { it.tur == GorselTuru.COZUM }, key = { it.id }) { image ->
                    OdakGorseli(image, gorselDeposu, stringResource(R.string.solution_image_description),
                        onDurum = { durum ->
                            gorselDurumlari = if (durum == null) gorselDurumlari - image.id else gorselDurumlari + (image.id to durum)
                            if (durum == GorselHazirligi.HATA) olcum.kismi(OlcumNedeni.GORSEL_EKSIK)
                        }) { olcum.tamEkran(); gorsel = image }
                }
            }
            if (yuklemeHatasi) item {
                Text(stringResource(R.string.focus_load_error), color = colors.error)
                TextButton(onClick = { yenileme++ }) { Text(stringResource(R.string.topic_retry)) }
            }
            if (degerlendirme && oncekiKayitlar != null && !tekrarli && !kullanilamaz) item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.focus_assess_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.focus_assess_note), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    Text(stringResource(R.string.measure_frozen_note), style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { olcum.calismayaDon(); planYenilendi = false }, enabled = !mesgul) {
                        Text(stringResource(R.string.measure_return_to_study))
                    }
                    if (onizlemeler == null && !planHatasi) Text(stringResource(R.string.measure_preparing))
                    onizlemeler?.get(secim ?: TekrarSonucu.RAHAT)?.karar?.let { KararAciklamasi(it) }
                    TekrarSonucu.entries.forEach { sonuc ->
                        val plan = onizlemeler?.get(sonuc)?.plan
                        val secili = secim == sonuc
                        Card(onClick = { olcum.dondur(sonuc) }, enabled = !mesgul && plan != null,
                            modifier = Modifier.fillMaxWidth().semantics { selected = secili; role = Role.RadioButton },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = if (secili) colors.primaryContainer else colors.surface)) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(when (sonuc) {
                                    TekrarSonucu.COZEMEDIM -> R.string.review_result_not_solved
                                    TekrarSonucu.ZORLANDIM -> R.string.review_result_struggled
                                    TekrarSonucu.RAHAT -> R.string.review_result_easy
                                }), fontWeight = FontWeight.SemiBold)
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(if (plan == null) "—" else if (plan.sonrakiAralik == 1) stringResource(R.string.focus_tomorrow)
                                    else stringResource(R.string.focus_next_days, plan.sonrakiAralik), style = MaterialTheme.typography.labelSmall)
                                    if (secili) Text("✓", color = colors.primary)
                                }
                            }
                        }
                    }
                }
            }
            if (sira < toplam) item {
                TextButton(onClick = { if (!kaydediliyor && !cozumAciliyor) { olcum.aktiflik(false); onSonaBirak() } }, enabled = !mesgul, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.focus_defer)) }
                Text(stringResource(R.string.focus_defer_note), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
        }
    }
    if (mola) AlertDialog(onDismissRequest = { mola = false },
        title = { Text(stringResource(R.string.focus_paused_title)) },
        text = { Text(stringResource(R.string.focus_paused_note)) },
        confirmButton = { TextButton(onClick = { mola = false }) { Text(stringResource(R.string.focus_resume)) } })
    gorsel?.let { image ->
        val file = gorselDeposu.dosyaGoruntuIcin(image.dosyaAdi)
        if (file != null) SoruGorselTamEkran(file = file,
            contentDescription = stringResource(if (image.tur == GorselTuru.SORU) R.string.question_image_description else R.string.solution_image_description),
            onDismiss = { gorsel = null })
    }
}

@Composable
private fun OdakGorseli(image: SoruGorseli, depo: LocalSoruGorselDeposu, aciklama: String,
    onDurum: (GorselHazirligi?) -> Unit, onClick: () -> Unit) {
    val ratio = (image.genislik.toFloat() / image.yukseklik.coerceAtLeast(1)).coerceIn(0.55f, 2f)
    SoruGorselResmi(depo.dosyaGoruntuIcin(image.dosyaAdi), aciklama,
        modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp, max = 560.dp).aspectRatio(ratio),
        contentScale = ContentScale.Fit, maxDecodeEdge = 1800, onClick = onClick, onDurum = onDurum)
}

private fun sureMetni(ms: Long?): String = if (ms == null) "—" else
    String.format(Locale.ROOT, "%02d:%02d", ms / 60_000, ms / 1_000 % 60)

@Composable
private fun KararAciklamasi(karar: com.keremtulu.questionnote.domain.repeat.TekrarKarari) {
    val mod = karar.mod
    val aciklama = when (mod) {
        com.keremtulu.questionnote.domain.repeat.TekrarMotorModu.KAPALI -> R.string.motor_focus_off
        com.keremtulu.questionnote.domain.repeat.TekrarMotorModu.GOLGE -> R.string.motor_focus_shadow
        com.keremtulu.questionnote.domain.repeat.TekrarMotorModu.ETKIN -> R.string.motor_focus_active
    }
    Text(stringResource(aciklama, karar.temel.sonrakiAralik, karar.aday.sonrakiAralik),
        style = MaterialTheme.typography.bodySmall)
    Text(stringResource(when {
        com.keremtulu.questionnote.domain.repeat.KararNedeni.BIRLIKTE_SINYAL in karar.nedenler -> R.string.motor_reason_combined
        com.keremtulu.questionnote.domain.repeat.KararNedeni.TOPARLANMA in karar.nedenler -> R.string.motor_reason_recovery
        com.keremtulu.questionnote.domain.repeat.KararNedeni.IKINCI_INDIRIM_YOK in karar.nedenler -> R.string.motor_reason_no_stack
        else -> R.string.motor_reason_baseline
    }), style = MaterialTheme.typography.bodySmall)
}

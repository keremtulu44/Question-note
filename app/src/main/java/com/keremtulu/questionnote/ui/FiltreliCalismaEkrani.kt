package com.keremtulu.questionnote.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.Konu
import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.SoruCalismaFiltresi
import com.keremtulu.questionnote.domain.model.SoruSecenegi
import com.keremtulu.questionnote.domain.model.SoruSecenekTuru
import com.keremtulu.questionnote.domain.repeat.CalismaSorulariniSirala
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.KonuRepository
import com.keremtulu.questionnote.domain.repository.SoruRepository
import com.keremtulu.questionnote.ui.animasyon.animateItemInsertion
import com.keremtulu.questionnote.ui.animasyon.rememberShimmer
import com.keremtulu.questionnote.ui.bilesenler.premium.CamKarti
import com.keremtulu.questionnote.ui.bilesenler.premium.CamYukseklik
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.bilesenler.premium.IskeletKart
import com.keremtulu.questionnote.ui.bilesenler.premium.IskeletMetin
import com.keremtulu.questionnote.ui.bilesenler.premium.PremiumBosDurum
import com.keremtulu.questionnote.ui.bilesenler.premium.VarsayilanIlustrasyon
import com.keremtulu.questionnote.ui.premium.EtkilesimTuru
import com.keremtulu.questionnote.ui.premium.PremiumGradyanButon
import com.keremtulu.questionnote.ui.premium.PremiumCard
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import java.time.ZoneId
import java.time.LocalDate
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine

/** State kept by the app shell so opening a question does not discard the chosen filters. */
data class FiltreliCalismaEkraniAyar(
    val filtre: SoruCalismaFiltresi = SoruCalismaFiltresi(),
    val soruSayisi: String = "10",
    val akilliSira: Boolean = true,
)

private sealed interface EkranYukleme<out T> {
    data object Yukleniyor : EkranYukleme<Nothing>
    data class Hazir<T>(val deger: T) : EkranYukleme<T>
    data object Hata : EkranYukleme<Nothing>
}

private data class CalismaFiltreSecenekleri(
    val dersler: List<Ders>,
    val konular: List<Konu>,
    val kaynaklar: List<SoruSecenegi>,
    val yanlisNedenleri: List<SoruSecenegi>,
)

private sealed interface FiltreSecimi {
    data object Hepsi : FiltreSecimi
    data object Belirtilmemis : FiltreSecimi
    data class Deger(val metin: String) : FiltreSecimi
}

private data class FiltreSecenegi(
    val secim: FiltreSecimi,
    val etiket: String,
)

@Composable
fun FiltreliCalismaEkrani(
    dersRepository: DersRepository,
    konuRepository: KonuRepository,
    soruRepository: SoruRepository,
    ayar: FiltreliCalismaEkraniAyar,
    onAyarDegisti: (FiltreliCalismaEkraniAyar) -> Unit,
    onGeri: () -> Unit,
    onOturumBaslat: (List<Soru>, Map<String, Ders>) -> Unit,
    onDevam: (() -> Unit)? = null,
) {
    var yenileme by remember { mutableIntStateOf(0) }
    val bugun = com.keremtulu.questionnote.ui.bilesenler.hatirlaYerelGun()

    val dersAkisi = remember(dersRepository, yenileme) { dersRepository.aktifDersleriIzle() }
    val konuAkisi = remember(konuRepository, yenileme) { konuRepository.tumunuIzle() }
    val kaynakAkisi = remember(soruRepository, yenileme) {
        soruRepository.secenekleriIzle(SoruSecenekTuru.KAYNAK)
    }
    val nedenAkisi = remember(soruRepository, yenileme) {
        soruRepository.secenekleriIzle(SoruSecenekTuru.YANLIS_NEDENI)
    }
    val seceneklerAkisi = remember(dersAkisi, konuAkisi, kaynakAkisi, nedenAkisi) {
        combine(dersAkisi, konuAkisi, kaynakAkisi, nedenAkisi) { dersler, konular, kaynaklar, nedenler ->
            val etkinDersIdleri = dersler.mapTo(mutableSetOf()) { it.id }
            CalismaFiltreSecenekleri(
                dersler = dersler,
                konular = konular.filter { it.dersId in etkinDersIdleri },
                kaynaklar = kaynaklar,
                yanlisNedenleri = nedenler,
            )
        }
    }
    val secenekDurumu = produceState<EkranYukleme<CalismaFiltreSecenekleri>>(
        EkranYukleme.Yukleniyor,
        seceneklerAkisi,
    ) {
        value = EkranYukleme.Yukleniyor
        seceneklerAkisi.catch { value = EkranYukleme.Hata }
            .collect { value = EkranYukleme.Hazir(it) }
    }.value

    val soruAkisi = remember(soruRepository, ayar.filtre, yenileme, bugun) {
        soruRepository.filtreliCalismaSorulariniIzle(ayar.filtre)
    }
    val soruDurumu = produceState<EkranYukleme<List<Soru>>>(
        EkranYukleme.Yukleniyor,
        soruAkisi,
    ) {
        value = EkranYukleme.Yukleniyor
        soruAkisi.catch { value = EkranYukleme.Hata }
            .collect { value = EkranYukleme.Hazir(it) }
    }.value

    val secenekler: CalismaFiltreSecenekleri? = when (val state = secenekDurumu) {
        is EkranYukleme.Hazir -> state.deger
        else -> null
    }
    val dersHaritasi = remember(secenekler?.dersler) {
        secenekler?.dersler.orEmpty().associateBy { it.id }
    }
    val konuHaritasi = remember(secenekler?.konular) {
        secenekler?.konular.orEmpty().associateBy { it.id }
    }
    val tumuSecimi = FiltreSecimi.Hepsi
    val bosSecimi = FiltreSecimi.Belirtilmemis
    val topicSecimi = when {
        ayar.filtre.konusuz -> FiltreSecimi.Belirtilmemis
        ayar.filtre.konuId != null -> FiltreSecimi.Deger(ayar.filtre.konuId)
        else -> tumuSecimi
    }
    val sourceSecimi = when {
        ayar.filtre.kaynakBelirtilmemis -> bosSecimi
        ayar.filtre.kaynak != null -> FiltreSecimi.Deger(ayar.filtre.kaynak)
        else -> tumuSecimi
    }
    val reasonSecimi = when {
        ayar.filtre.yanlisNedeniBelirtilmemis -> bosSecimi
        ayar.filtre.yanlisNedeni != null -> FiltreSecimi.Deger(ayar.filtre.yanlisNedeni)
        else -> tumuSecimi
    }

    val topicOptions = buildList {
        add(FiltreSecenegi(tumuSecimi, ""))
        add(FiltreSecenegi(bosSecimi, ""))
        secenekler?.konular.orEmpty().filter { ayar.filtre.dersId == null || it.dersId == ayar.filtre.dersId }.forEach { topic ->
            val lessonName = dersHaritasi[topic.dersId]?.ad ?: return@forEach
            val topicName = if (topic.arsivli) {
                "${topic.ad} (arşivli)"
            } else {
                topic.ad
            }
            add(FiltreSecenegi(FiltreSecimi.Deger(topic.id), "$lessonName · $topicName"))
        }
    }
    val sourceOptions = buildList {
        add(FiltreSecenegi(tumuSecimi, ""))
        add(FiltreSecenegi(bosSecimi, ""))
        secenekler?.kaynaklar.orEmpty().forEach { add(FiltreSecenegi(FiltreSecimi.Deger(it.ad), it.ad)) }
    }
    val reasonOptions = buildList {
        add(FiltreSecenegi(tumuSecimi, ""))
        add(FiltreSecenegi(bosSecimi, ""))
        secenekler?.yanlisNedenleri.orEmpty().forEach { add(FiltreSecenegi(FiltreSecimi.Deger(it.ad), it.ad)) }
    }

    val allLabel = androidx.compose.ui.res.stringResource(R.string.filtered_practice_all)
    val noTopicLabel = androidx.compose.ui.res.stringResource(R.string.question_filter_unassigned)
    val missingLabel = androidx.compose.ui.res.stringResource(R.string.filtered_practice_missing)
    val topicDropdownOptions = topicOptions.map {
        it.copy(
            etiket = when (it.secim) {
                FiltreSecimi.Hepsi -> allLabel
                FiltreSecimi.Belirtilmemis -> noTopicLabel
                is FiltreSecimi.Deger -> it.etiket
            },
        )
    }
    val textDropdownOptions = { values: List<FiltreSecenegi> ->
        values.map {
            it.copy(
                etiket = when (it.secim) {
                    FiltreSecimi.Hepsi -> allLabel
                    FiltreSecimi.Belirtilmemis -> missingLabel
                    is FiltreSecimi.Deger -> it.etiket
                },
            )
        }
    }

    val sorular: List<Soru> = when (val state = soruDurumu) {
        is EkranYukleme.Hazir -> state.deger.filter { it.dersId in dersHaritasi }
        else -> emptyList()
    }
    val siraliSorular = remember(sorular, ayar.akilliSira, bugun) {
        if (ayar.akilliSira) CalismaSorulariniSirala.akilliOncelik(sorular, bugun.toEpochDay().toInt())
        else CalismaSorulariniSirala.enUzunSureCalisilmayanlar(sorular, ZoneId.systemDefault())
    }
    // Kullanıcının yazdığı sayı korunur; istenen sayı eşleşenden büyükse altındaki bilgi metni
    // ve "Başlat" düğmesi tüm eşleşenleri çalıştırır. Alan geri yazılarak kullanıcı düzeltilmez.
    // Boş alan "tümü" anlamına gelir; 0 gibi geçersiz girişte düğme kapalı kalır.
    val girilenSoruSayisi = ayar.soruSayisi.toIntOrNull()
    val istenenSoruSayisi = when {
        ayar.soruSayisi.isBlank() -> siraliSorular.size
        else -> girilenSoruSayisi?.takeIf { it >= 1 }
    }
    val secilecekSoruSayisi = istenenSoruSayisi
        ?.coerceAtMost(siraliSorular.size)
        ?.takeIf { siraliSorular.isNotEmpty() }
    val sayiAsimi = istenenSoruSayisi != null &&
        siraliSorular.isNotEmpty() &&
        istenenSoruSayisi > siraliSorular.size

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { insets ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(insets).testTag("practice-filter-list"),
            contentPadding = PaddingValues(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PremiumTextButton(
                        onClick = onGeri,
                        interactionTur = EtkilesimTuru.GERI,
                    ) { Text("‹ ${androidx.compose.ui.res.stringResource(R.string.menu_today)}") }
                    GradyanKart(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        borderAnimation = false,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.filtered_practice_kicker),
                                color = PremiumMarkaRenkler.BakirIsik,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.filtered_practice_title),
                                color = PremiumMarkaRenkler.Kagit,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.filtered_practice_subtitle),
                                color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.86f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            when (secenekDurumu) {
                EkranYukleme.Yukleniyor -> item {
                    val loadingLabel = androidx.compose.ui.res.stringResource(R.string.filtered_practice_loading)
                    val shimmer = rememberShimmer()
                    CamKarti(
                        modifier = Modifier.fillMaxWidth().semantics {
                            contentDescription = loadingLabel
                            liveRegion = LiveRegionMode.Polite
                        },
                        elevation = CamYukseklik.DUSUK,
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            IskeletMetin(lines = 1, lineHeight = 20, widthFraction = 0.48f, animation = shimmer)
                            repeat(4) {
                                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                    IskeletMetin(lines = 1, lineHeight = 14, widthFraction = 0.34f, animation = shimmer)
                                    IskeletMetin(lines = 1, lineHeight = 48, widthFraction = 1f, animation = shimmer)
                                }
                            }
                        }
                    }
                }
                EkranYukleme.Hata -> item {
                    PremiumBosDurum(
                        illustration = { VarsayilanIlustrasyon() },
                        baslik = androidx.compose.ui.res.stringResource(R.string.filtered_practice_options_error_title),
                        aciklama = androidx.compose.ui.res.stringResource(R.string.filtered_practice_options_error),
                        butonMetni = androidx.compose.ui.res.stringResource(R.string.topic_retry),
                        onButonTikla = { yenileme++ },
                        modifier = Modifier.fillMaxWidth().semantics {
                            liveRegion = LiveRegionMode.Assertive
                        },
                    )
                }
                is EkranYukleme.Hazir -> item {
                    CamKarti(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CamYukseklik.DUSUK,
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.filtered_practice_filters_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            FiltreDropdown(
                                baslik = androidx.compose.ui.res.stringResource(R.string.analysis_filter_lesson),
                                secim = ayar.filtre.dersId?.let { FiltreSecimi.Deger(it) } ?: tumuSecimi,
                                secenekler = listOf(FiltreSecenegi(tumuSecimi, allLabel)) + secenekler?.dersler.orEmpty().map {
                                    FiltreSecenegi(FiltreSecimi.Deger(it.id), it.ad)
                                },
                                onSecim = { choice ->
                                    onAyarDegisti(
                                        ayar.copy(
                                            filtre = ayar.filtre.copy(
                                                dersId = (choice as? FiltreSecimi.Deger)?.metin,
                                                konuId = null,
                                                konusuz = false,
                                            ),
                                        ),
                                    )
                                },
                            )
                            if (ayar.filtre.vadesiGelen) {
                                Text(androidx.compose.ui.res.stringResource(R.string.analysis_filter_due))
                                PremiumTextButton(
                                    onClick = {
                                        onAyarDegisti(ayar.copy(filtre = ayar.filtre.copy(vadesiGelen = false)))
                                    },
                                    modifier = Modifier.heightIn(min = 48.dp),
                                ) { Text(androidx.compose.ui.res.stringResource(R.string.analysis_filter_remove_due)) }
                            }
                            ayar.filtre.kaynakAnahtari?.let { anahtar ->
                                Text(androidx.compose.ui.res.stringResource(R.string.analysis_filter_normalized, anahtar))
                                PremiumTextButton(
                                    onClick = {
                                        onAyarDegisti(ayar.copy(filtre = ayar.filtre.copy(kaynakAnahtari = null)))
                                    },
                                    modifier = Modifier.heightIn(min = 48.dp),
                                ) { Text(androidx.compose.ui.res.stringResource(R.string.analysis_filter_remove_source)) }
                            }
                            FiltreDropdown(
                                baslik = androidx.compose.ui.res.stringResource(R.string.filtered_practice_filter_topic),
                                secim = topicSecimi,
                                secenekler = topicDropdownOptions,
                                onSecim = { choice ->
                                    val topic = choice as? FiltreSecimi.Deger
                                    onAyarDegisti(
                                        ayar.copy(
                                            filtre = ayar.filtre.copy(
                                                konuId = topic?.metin,
                                                konusuz = choice == FiltreSecimi.Belirtilmemis,
                                            ),
                                        ),
                                    )
                                },
                            )
                            FiltreDropdown(
                                baslik = androidx.compose.ui.res.stringResource(R.string.filtered_practice_filter_source),
                                secim = sourceSecimi,
                                secenekler = textDropdownOptions(sourceOptions),
                                onSecim = { choice ->
                                    val source = choice as? FiltreSecimi.Deger
                                    onAyarDegisti(
                                        ayar.copy(
                                            filtre = ayar.filtre.copy(
                                                kaynak = source?.metin,
                                                kaynakAnahtari = null,
                                                kaynakBelirtilmemis = choice == FiltreSecimi.Belirtilmemis,
                                            ),
                                        ),
                                    )
                                },
                            )
                            FiltreDropdown(
                                baslik = androidx.compose.ui.res.stringResource(R.string.filtered_practice_filter_reason),
                                secim = reasonSecimi,
                                secenekler = textDropdownOptions(reasonOptions),
                                onSecim = { choice ->
                                    val reason = choice as? FiltreSecimi.Deger
                                    onAyarDegisti(
                                        ayar.copy(
                                            filtre = ayar.filtre.copy(
                                                yanlisNedeni = reason?.metin,
                                                yanlisNedeniBelirtilmemis = choice == FiltreSecimi.Belirtilmemis,
                                            ),
                                        ),
                                    )
                                },
                            )
                            PremiumTextButton(
                                onClick = { onAyarDegisti(ayar.copy(filtre = SoruCalismaFiltresi())) },
                            ) {
                                Text(androidx.compose.ui.res.stringResource(R.string.filtered_practice_clear_filters))
                            }
                        }
                    }
                }
            }

            if (onDevam != null) item {
                PremiumOutlinedButton(onClick = onDevam, modifier = Modifier.fillMaxWidth()) {
                    Text(androidx.compose.ui.res.stringResource(R.string.focus_continue_session))
                }
            }
            if (secenekDurumu is EkranYukleme.Hazir) item {
                CamKarti(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CamYukseklik.DUSUK,
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.focus_order_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(
                                ayar.akilliSira,
                                { onAyarDegisti(ayar.copy(akilliSira = true)) },
                                label = { Text(androidx.compose.ui.res.stringResource(R.string.focus_smart)) },
                            )
                            FilterChip(
                                !ayar.akilliSira,
                                { onAyarDegisti(ayar.copy(akilliSira = false)) },
                                label = { Text(androidx.compose.ui.res.stringResource(R.string.focus_oldest)) },
                            )
                        }
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.filtered_practice_count_label),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf("5", "10", "20").forEach { adet ->

                                FilterChip(
                                    selected = ayar.soruSayisi == adet,
                                    onClick = { onAyarDegisti(ayar.copy(soruSayisi = adet)) },
                                    label = { Text(adet, maxLines = 1) },
                                )
                            }
                            FilterChip(
                                selected = ayar.soruSayisi.isBlank(),
                                onClick = { onAyarDegisti(ayar.copy(soruSayisi = "")) },
                                label = {
                                    Text(
                                        androidx.compose.ui.res.stringResource(R.string.filtered_practice_all_count),
                                        maxLines = 1,
                                    )
                                },
                            )
                        }
                    }
                }
            }
            if (secenekDurumu is EkranYukleme.Hazir) when (soruDurumu) {
                EkranYukleme.Yukleniyor -> item {
                    val loadingLabel = androidx.compose.ui.res.stringResource(R.string.filtered_practice_loading)
                    Column(
                        modifier = Modifier.fillMaxWidth().semantics {
                            contentDescription = loadingLabel
                            liveRegion = LiveRegionMode.Polite
                        },
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        repeat(3) { IskeletKart(height = 92, showImage = false, showSubtitle = true) }
                    }
                }
                EkranYukleme.Hata -> item {
                    PremiumBosDurum(
                        illustration = { VarsayilanIlustrasyon() },
                        baslik = androidx.compose.ui.res.stringResource(R.string.filtered_practice_error_title),
                        aciklama = androidx.compose.ui.res.stringResource(R.string.filtered_practice_error),
                        butonMetni = androidx.compose.ui.res.stringResource(R.string.topic_retry),
                        onButonTikla = { yenileme++ },
                        modifier = Modifier.fillMaxWidth().semantics {
                            liveRegion = LiveRegionMode.Assertive
                        },
                    )
                }
                is EkranYukleme.Hazir -> {
                    if (siraliSorular.isEmpty()) {
                        item {
                            val hasActiveFilters = ayar.filtre != SoruCalismaFiltresi()
                            PremiumBosDurum(
                                illustration = { VarsayilanIlustrasyon() },
                                baslik = androidx.compose.ui.res.stringResource(R.string.filtered_practice_empty_title),
                                aciklama = androidx.compose.ui.res.stringResource(R.string.filtered_practice_no_matches),
                                ikincilButonMetni = if (hasActiveFilters) {
                                    androidx.compose.ui.res.stringResource(R.string.filtered_practice_clear_filters)
                                } else {
                                    null
                                },
                                onIkincilButon = if (hasActiveFilters) {
                                    { onAyarDegisti(ayar.copy(filtre = SoruCalismaFiltresi())) }
                                } else {
                                    null
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    } else {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    androidx.compose.ui.res.stringResource(R.string.filtered_practice_matched_count, siraliSorular.size),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Text(
                                    androidx.compose.ui.res.stringResource(R.string.filtered_practice_order_note),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (sayiAsimi) {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(
                                            R.string.filtered_practice_count_capped,
                                            siraliSorular.size,
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                                PremiumGradyanButon(
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                                    enabled = secenekler != null && secilecekSoruSayisi != null,
                                    contentPadding = PaddingValues(vertical = 16.dp),
                                    onClick = {
                                        val count = secilecekSoruSayisi ?: return@PremiumGradyanButon
                                        val lessonMap = secenekler?.dersler.orEmpty().associateBy { it.id }
                                        val selected = siraliSorular.take(count)
                                        if (selected.isNotEmpty() && selected.all { it.dersId in lessonMap }) {
                                            onOturumBaslat(selected, lessonMap)
                                        }
                                    },
                                ) {
                                    Text(
                                        androidx.compose.ui.res.stringResource(R.string.filtered_practice_start),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                        itemsIndexed(siraliSorular, key = { _, question -> question.id }) { index, question ->
                            val lesson = dersHaritasi[question.dersId]
                            if (lesson != null) {
                                val topic = question.konuId?.let(konuHaritasi::get)
                                val enter = animateItemInsertion(index)
                                CalismaSoruKarti(
                                    soru = question,
                                    ders = lesson,
                                    konu = topic,
                                    onClick = { onOturumBaslat(listOf(question), mapOf(lesson.id to lesson)) },
                                    modifier = Modifier.graphicsLayer {
                                        alpha = enter.value
                                        translationY = (1f - enter.value) * 12.dp.toPx()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltreDropdown(
    baslik: String,
    secim: FiltreSecimi,
    secenekler: List<FiltreSecenegi>,
    onSecim: (FiltreSecimi) -> Unit,
) {
    var acik by remember(baslik) { mutableStateOf(false) }
    val selectedLabel = secenekler.firstOrNull { it.secim == secim }?.etiket
        ?: androidx.compose.ui.res.stringResource(R.string.analysis_filter_not_visible)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            baslik,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Box {
            PremiumOutlinedButton(
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                onClick = { acik = true },
                interactionTur = EtkilesimTuru.TIKLAMA,
            ) {
                Text(
                    selectedLabel,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text("▾", modifier = Modifier.padding(start = 8.dp))
            }
            DropdownMenu(
                expanded = acik,
                onDismissRequest = { acik = false },
                modifier = Modifier.heightIn(max = 360.dp),
            ) {
                secenekler.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.etiket) },
                        onClick = {
                            acik = false
                            onSecim(option.secim)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CalismaSoruKarti(
    soru: Soru,
    ders: Ders,
    konu: Konu?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PremiumCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        interactionTur = EtkilesimTuru.KART_ACILIS,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        R.string.filtered_practice_course_topic,
                        ders.ad,
                        konu?.let { if (it.arsivli) "${it.ad} (arşivli)" else it.ad }
                            ?: androidx.compose.ui.res.stringResource(R.string.question_filter_unassigned),
                    ),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (soru.favori) {
                    Text("★", color = PremiumMarkaRenkler.Bakir, style = MaterialTheme.typography.titleMedium)
                }
            }
            soru.soruNotu?.takeIf { it.isNotBlank() }?.let { note ->
                Text(
                    note,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            soru.kaynak?.takeIf { it.isNotBlank() }?.let { source ->
                Text(
                    androidx.compose.ui.res.stringResource(R.string.filtered_practice_source_value, source),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            soru.yanlisNedeni?.takeIf { it.isNotBlank() }?.let { reason ->
                Text(
                    androidx.compose.ui.res.stringResource(R.string.filtered_practice_reason_value, reason),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

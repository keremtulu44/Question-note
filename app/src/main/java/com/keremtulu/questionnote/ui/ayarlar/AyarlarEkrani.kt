package com.keremtulu.questionnote.ui.ayarlar

import android.text.format.Formatter
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.BuildConfig
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.export.SonYedekBilgisi
import com.keremtulu.questionnote.data.prefs.SinavProfiliDeposu
import com.keremtulu.questionnote.data.prefs.TercihGunlukHedefDeposu
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.ui.LocalUygulamaSesleri
import com.keremtulu.questionnote.ui.bilesenler.premium.CamKarti
import com.keremtulu.questionnote.ui.bilesenler.premium.CamYukseklik
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.premium.EtkilesimTuru
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumSwitch
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun AyarlarEkrani(
    gunlukHedefDeposu: TercihGunlukHedefDeposu,
    profilDeposu: SinavProfiliDeposu,
    tekrarPolitikasi: com.keremtulu.questionnote.data.prefs.TekrarPolitikasiDeposu,
    motorTanisi: com.keremtulu.questionnote.data.gelisim.MotorTaniHizmeti,
    dersRepository: DersRepository,
    sonYedek: SonYedekBilgisi?,
    yedeklemeIslemde: Boolean,
    onYedekOlustur: () -> Unit,
    onYedekGeriYukle: () -> Unit,
    onGorselBakimi: () -> Unit,
    onKatalog: () -> Unit,
    onKonular: () -> Unit,
    onPremiumDemo: () -> Unit = {},
    onOgretmenModunaGec: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sesEfektleri = LocalUygulamaSesleri.current
    var sesAcik by remember { mutableStateOf(sesEfektleri?.acikMi() ?: false) }
    val secim by profilDeposu.secimIzle.collectAsState()
    LaunchedEffect(Unit) { runCatching { dersRepository.kataloguHazirla() } }
    var profilKaydediliyor by remember { mutableStateOf(false) }
    var profilHatasi by remember { mutableStateOf(false) }
    var hedef by remember { mutableIntStateOf(gunlukHedefDeposu.hedef()) }
    val sonYedekMetni = sonYedek?.let { backup ->
        val tarih = SimpleDateFormat("d MMM yyyy HH:mm", Locale.forLanguageTag("tr-TR"))
            .format(Date(backup.olusturmaZamani))
        val boyut = Formatter.formatShortFileSize(context, backup.boyutBayt)
        context.getString(
            R.string.backup_last_info,
            backup.dosyaAdi,
            tarih,
            boyut,
            context.getString(if (backup.guvenlikKopyasi) R.string.backup_safety_copy else R.string.backup_external_copy),
        )
    } ?: stringResource(R.string.backup_last_none)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { insets ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 20.dp,
                end = 20.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                GradyanKart(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    borderAnimation = false,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.settings_kicker),
                            color = PremiumMarkaRenkler.BakirIsik,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                        Text(
                            stringResource(R.string.settings_title),
                            color = PremiumMarkaRenkler.Kagit,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            stringResource(R.string.settings_header_subtitle),
                            color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.86f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            if (com.keremtulu.questionnote.domain.model.UrunKapsami.OGRETMEN_MODU_GORUNUR) item {
                Bolum(stringResource(R.string.settings_role_section_title)) {
                    Text(
                        stringResource(R.string.settings_role_current_student),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(R.string.settings_role_switch_note),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    PremiumOutlinedButton(
                        onClick = onOgretmenModunaGec,
                        enabled = !yedeklemeIslemde,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .testTag("ogretmen_moduna_gec_buton"),
                    ) {
                        Text(stringResource(R.string.settings_switch_to_teacher))
                    }
                }
            }
            item {
                val tumDersler by produceState(emptyList<Ders>()) {
                    dersRepository.dersleriIzle(false).collect { value = it }
                }
                Bolum(stringResource(R.string.settings_profile_title)) {
                    Text(stringResource(R.string.settings_profile_note), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        tumDersler.forEach { ders ->
                            val acik = secim.gorunur(ders)
                            FilterChip(
                                selected = acik,
                                onClick = {
                                    if (profilKaydediliyor || yedeklemeIslemde) return@FilterChip
                                    profilKaydediliyor = true
                                    profilHatasi = false
                                    val simdiki = tumDersler.filter(secim::gorunur).map { it.id }.toSet()
                                    val yeni = if (ders.id in simdiki) simdiki - ders.id else simdiki + ders.id
                                    scope.launch {
                                        try {
                                            if (yeni.isNotEmpty()) dersRepository.dersSeciminiKaydet(yeni)
                                        } catch (iptal: CancellationException) {
                                            throw iptal
                                        } catch (_: Exception) {
                                            profilHatasi = true
                                        } finally {
                                            profilKaydediliyor = false
                                        }
                                    }
                                },
                                enabled = !profilKaydediliyor && !yedeklemeIslemde,
                                label = { Text(ders.ad) },
                            )
                        }
                    }
                    if (profilKaydediliyor) {
                        Text(
                            stringResource(R.string.settings_profile_saving),
                            modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
                        )
                    }
                    if (profilHatasi) {
                        Text(
                            stringResource(R.string.settings_profile_error),
                            modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Assertive },
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            item {
                Bolum(stringResource(R.string.settings_category_goal)) {
                    Text(
                        stringResource(R.string.settings_goal_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(R.string.settings_goal_note),
                        modifier = Modifier.padding(top = 2.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TercihGunlukHedefDeposu.SECENEKLER.forEach { secenek ->
                            FilterChip(
                                selected = hedef == secenek,
                                onClick = {
                                    hedef = secenek
                                    gunlukHedefDeposu.hedefiAyarla(secenek)
                                },
                                label = { Text(stringResource(R.string.settings_goal_option, secenek)) },
                            )
                        }
                    }
                }
            }
            if (sesEfektleri != null) item {
                Bolum(stringResource(R.string.settings_category_feedback)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                stringResource(R.string.settings_sound_title),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                stringResource(R.string.settings_sound_note),
                                modifier = Modifier.padding(top = 2.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        PremiumSwitch(
                            checked = sesAcik,
                            onCheckedChange = { acik ->
                                sesAcik = acik
                                sesEfektleri.ayarla(acik)
                            },
                        )
                    }
                }
            }
            item {
                Bolum(stringResource(R.string.settings_category_data)) {
                    Text(
                        stringResource(R.string.settings_backup_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(R.string.settings_backup_note),
                        modifier = Modifier.padding(top = 2.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        sonYedekMetni,
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (yedeklemeIslemde) {
                        Text(
                            stringResource(R.string.settings_data_operation_running),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                .semantics { liveRegion = LiveRegionMode.Polite },
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        maxItemsInEachRow = if (LocalDensity.current.fontScale >= 1.5f) 1 else 2,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PremiumOutlinedButton(
                            modifier = Modifier.weight(1f),
                            enabled = !yedeklemeIslemde,
                            interactionTur = EtkilesimTuru.TIKLAMA,
                            onClick = onYedekOlustur,
                        ) { Text(stringResource(R.string.menu_create_backup)) }
                        PremiumOutlinedButton(
                            modifier = Modifier.weight(1f),
                            enabled = !yedeklemeIslemde,
                            interactionTur = EtkilesimTuru.TIKLAMA,
                            onClick = onYedekGeriYukle,
                        ) { Text(stringResource(R.string.menu_restore_backup)) }
                    }
                }
            }
            item {
                Bolum(stringResource(R.string.settings_category_other)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            stringResource(R.string.menu_image_maintenance),
                            modifier = Modifier.weight(1f).padding(end = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        PremiumOutlinedButton(onClick = onGorselBakimi, enabled = !yedeklemeIslemde) {
                            Text(stringResource(R.string.settings_open))
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            stringResource(R.string.menu_catalog),
                            modifier = Modifier.weight(1f).padding(end = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        PremiumOutlinedButton(onClick = onKatalog) {
                            Text(stringResource(R.string.settings_open))
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.settings_topics), style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.settings_topics_note), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        PremiumOutlinedButton(onClick = onKonular, enabled = !yedeklemeIslemde) {
                            Text(stringResource(R.string.settings_open))
                        }
                    }
                }
            }
            item {
                Bolum(stringResource(R.string.settings_about_title)) {
                    Text(
                        stringResource(R.string.settings_about_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        stringResource(R.string.settings_about_data),
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun Bolum(baslik: String, icerik: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            baslik,
            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.sp,
        )
        CamKarti(
            modifier = Modifier.fillMaxWidth(),
            elevation = CamYukseklik.DUSUK,
            shape = RoundedCornerShape(22.dp),
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) { icerik() }
        }
    }
}

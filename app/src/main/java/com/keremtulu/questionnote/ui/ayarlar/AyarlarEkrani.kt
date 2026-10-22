package com.keremtulu.questionnote.ui.ayarlar

import android.text.format.Formatter
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.TextButton
import com.keremtulu.questionnote.data.prefs.SinavProfiliDeposu
import com.keremtulu.questionnote.domain.model.SinavProfili
import com.keremtulu.questionnote.ui.LocalUygulamaSesleri
import com.keremtulu.questionnote.domain.repository.DersRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.BuildConfig
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.export.SonYedekBilgisi
import com.keremtulu.questionnote.data.prefs.TercihGunlukHedefDeposu
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sesEfektleri = LocalUygulamaSesleri.current
    var sesAcik by remember { mutableStateOf(sesEfektleri?.acikMi() ?: false) }
    val secim by profilDeposu.secimIzle.collectAsState()
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

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { insets ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = insets.calculateTopPadding() + 12.dp,
                end = 20.dp,
                bottom = insets.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.settings_kicker),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.4.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(R.string.settings_title),
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.displaySmall,
                )
            }
            item { MotorAyarlariKarti(tekrarPolitikasi, motorTanisi) }
            item {
                Bolum(stringResource(R.string.settings_profile_title)) {
                    Text(stringResource(R.string.settings_profile_note), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SinavProfili.entries.forEach { profil ->
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !profilKaydediliyor && !yedeklemeIslemde,
                            onClick = {
                                if (secim.profil != profil) {
                                    profilKaydediliyor = true
                                    profilHatasi = false
                                    scope.launch {
                                        try {
                                            dersRepository.sinavProfiliniUygula(profil)
                                        } catch (iptal: CancellationException) {
                                            throw iptal
                                        } catch (_: Exception) {
                                            profilHatasi = true
                                        } finally {
                                            profilKaydediliyor = false
                                        }
                                    }
                                }
                            },
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(profil.ad, fontWeight = FontWeight.SemiBold)
                                Text(profil.aciklama, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Text(if (secim.profil == profil) "✓" else "›", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    if (secim.profil.dersAdlari.isNotEmpty()) {
                        Text(secim.profil.dersAdlari.joinToString(" · "), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (profilKaydediliyor) Text(stringResource(R.string.settings_profile_saving))
                    if (profilHatasi) Text(stringResource(R.string.settings_profile_error), color = MaterialTheme.colorScheme.error)
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
                        Switch(
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
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            enabled = !yedeklemeIslemde,
                            onClick = onYedekOlustur,
                        ) { Text(stringResource(R.string.menu_create_backup)) }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            enabled = !yedeklemeIslemde,
                            onClick = onYedekGeriYukle,
                        ) { Text(stringResource(R.string.menu_restore_backup)) }
                    }
                }
            }
            item {
                Bolum(stringResource(R.string.settings_category_other)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            stringResource(R.string.menu_image_maintenance),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        OutlinedButton(onClick = onGorselBakimi, enabled = !yedeklemeIslemde) {
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
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        OutlinedButton(onClick = onKatalog) {
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
                        OutlinedButton(onClick = onKonular, enabled = !yedeklemeIslemde) {
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(Modifier.padding(16.dp)) { icerik() }
        }
    }
}

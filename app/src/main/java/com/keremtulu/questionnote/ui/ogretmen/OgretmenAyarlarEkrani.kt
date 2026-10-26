package com.keremtulu.questionnote.ui.ogretmen

import android.text.format.Formatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.BuildConfig
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.export.SonYedekBilgisi
import com.keremtulu.questionnote.data.prefs.OgretmenDepolamaOzeti
import com.keremtulu.questionnote.data.prefs.OgretmenDeposu
import com.keremtulu.questionnote.domain.model.OgretmenVerisi
import com.keremtulu.questionnote.ui.bilesenler.premium.CamKarti
import com.keremtulu.questionnote.ui.bilesenler.premium.CamYukseklik
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.premium.PremiumGradyanButon
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumSwitch
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import com.keremtulu.questionnote.ui.LocalUygulamaSesleri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
internal fun OgretmenAyarlarEkrani(
    veri: OgretmenVerisi,
    ogretmenDeposu: OgretmenDeposu,
    sonYedek: SonYedekBilgisi?,
    yedeklemeIslemde: Boolean,
    onOgrenciModunaGec: () -> Unit,
    onYedekOlustur: () -> Unit,
    onYedekGeriYukle: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sesEfektleri = LocalUygulamaSesleri.current
    var sesAcik by remember { mutableStateOf(sesEfektleri?.acikMi() ?: false) }
    var ozet by remember { mutableStateOf<OgretmenDepolamaOzeti?>(null) }
    var temizlikOnayi by remember { mutableStateOf(false) }

    val yedekZamani = sonYedek?.olusturmaZamani
    LaunchedEffect(veri, yedekZamani) {
        ozet = runCatching { ogretmenDeposu.depolamaOzeti(yedekZamani) }.getOrNull()
    }

    fun boyut(bayt: Long): String = Formatter.formatShortFileSize(context, bayt)

    val sonYedekMetni = sonYedek?.let { backup ->
        val tarih = SimpleDateFormat("d MMM yyyy HH:mm", Locale.forLanguageTag("tr-TR"))
            .format(Date(backup.olusturmaZamani))
        context.getString(
            R.string.backup_last_info,
            backup.dosyaAdi,
            tarih,
            boyut(backup.boyutBayt),
            context.getString(if (backup.guvenlikKopyasi) R.string.backup_safety_copy else R.string.backup_external_copy),
        )
    } ?: stringResource(R.string.backup_last_none)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(WindowInsets.safeDrawing),
        contentPadding = PaddingValues(start = 18.dp, top = 12.dp, end = 18.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            GradyanKart(modifier = Modifier.fillMaxWidth(), borderAnimation = false) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.settings_kicker),
                        color = PremiumMarkaRenkler.BakirIsik,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.settings_title),
                        color = PremiumMarkaRenkler.Kagit,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.settings_role_current_teacher),
                        color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.86f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        item {
            CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.settings_role_section_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.settings_role_switch_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    PremiumOutlinedButton(
                        onClick = onOgrenciModunaGec,
                        enabled = !yedeklemeIslemde,
                        modifier = Modifier.fillMaxWidth().testTag("ogrenci_moduna_gec_buton"),
                    ) {
                        Text(stringResource(R.string.settings_switch_to_student))
                    }
                }
            }
        }
        item {
            CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.settings_backup_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(
                            R.string.teacher_settings_workspace_summary,
                            veri.gruplar.size,
                            veri.sorular.size,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = sonYedekMetni,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    PremiumGradyanButon(
                        onClick = onYedekOlustur,
                        enabled = !yedeklemeIslemde,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.menu_create_backup), fontWeight = FontWeight.Bold)
                    }
                    PremiumOutlinedButton(
                        onClick = onYedekGeriYukle,
                        enabled = !yedeklemeIslemde,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.menu_restore_backup))
                    }
                }
            }
        }
        item {
            CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.teacher_storage_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    val mevcut = ozet
                    if (mevcut == null) {
                        Text(
                            text = stringResource(R.string.teacher_storage_refresh),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = stringResource(
                                R.string.teacher_storage_summary,
                                mevcut.disktekiDosya,
                                boyut(mevcut.disktedekiBayt),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (mevcut.kullanilmayanDosyalar.isEmpty()) {
                                stringResource(R.string.teacher_storage_clean)
                            } else {
                                stringResource(
                                    R.string.teacher_storage_orphans,
                                    mevcut.kullanilmayanDosyalar.size,
                                    boyut(mevcut.kullanilmayanBayt),
                                )
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            PremiumTextButton(
                                onClick = {
                                    scope.launch {
                                        ozet = runCatching {
                                            ogretmenDeposu.depolamaOzeti(yedekZamani)
                                        }.getOrNull()
                                    }
                                },
                                modifier = Modifier.heightIn(min = 44.dp),
                            ) {
                                Text(stringResource(R.string.teacher_storage_refresh))
                            }
                            if (mevcut.kullanilmayanDosyalar.isNotEmpty()) {
                                PremiumTextButton(
                                    onClick = { temizlikOnayi = true },
                                    modifier = Modifier.heightIn(min = 44.dp),
                                ) {
                                    Text(stringResource(R.string.teacher_storage_cleanup))
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.settings_sound_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    PremiumSwitch(
                        checked = sesAcik,
                        onCheckedChange = { yeni ->
                            sesAcik = yeni
                            sesEfektleri?.ayarla(yeni)
                        },
                    )
                }
            }
        }
        item {
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    val onaylanan = ozet?.kullanilmayanDosyalar.orEmpty()
    if (temizlikOnayi && onaylanan.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { temizlikOnayi = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text(stringResource(R.string.teacher_storage_cleanup)) },
            text = {
                Text(
                    stringResource(
                        R.string.teacher_storage_cleanup_confirm,
                        onaylanan.size,
                        boyut(ozet?.kullanilmayanBayt ?: 0L),
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        temizlikOnayi = false
                        scope.launch {
                            val silinen = runCatching {
                                ogretmenDeposu.onaylananYetimleriSil(onaylanan.toSet())
                            }.getOrDefault(0)
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.teacher_storage_cleaned, silinen),
                            )
                            ozet = runCatching { ogretmenDeposu.depolamaOzeti(yedekZamani) }.getOrNull()
                        }
                    },
                ) {
                    Text(stringResource(R.string.teacher_storage_cleanup))
                }
            },
            dismissButton = {
                TextButton(onClick = { temizlikOnayi = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

/** Shared card wrapper so every teacher panel uses the same surface + spacing. */
@Composable
internal fun OgretmenPanelKarti(
    baslik: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    CamKarti(modifier = modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            baslik?.let {
                Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

@Composable
internal fun BolumBasligi(metin: String, modifier: Modifier = Modifier) {
    Text(
        text = metin,
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun InceAyrac() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 0.5.dp,
    )
}

/** Neutral bordered surface used by dialogs and inline rows. */
@Composable
internal fun NotKutusu(metin: String, vurgu: androidx.compose.ui.graphics.Color) {
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = vurgu.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, vurgu.copy(alpha = 0.30f)),
    ) {
        Text(
            text = metin,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

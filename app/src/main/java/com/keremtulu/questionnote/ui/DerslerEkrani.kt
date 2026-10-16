package com.keremtulu.questionnote.ui

import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.export.SonYedekBilgisi
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.DersIslemSonucu
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.ui.bilesenler.DersKartVerisi
import com.keremtulu.questionnote.ui.bilesenler.DersKarti
import com.keremtulu.questionnote.ui.tema.DersRenkleri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private sealed interface DerslerEkraniDurumu {
    data object Yukleniyor : DerslerEkraniDurumu
    data class Hazir(val dersler: List<Ders>) : DerslerEkraniDurumu
    data object Hata : DerslerEkraniDurumu
}

private data class DersKartStili(
    val isaret: Int,
    val acikRenk: androidx.compose.ui.graphics.Color,
    val koyuRenk: androidx.compose.ui.graphics.Color,
)

private data class DersFormu(
    val duzenlenenDers: Ders? = null,
    val ad: String,
    val hata: String? = null,
    val kaydediliyor: Boolean = false,
)

private val dersKartStilleri = mapOf(
    "edebiyat" to DersKartStili(R.string.symbol_edebiyat, DersRenkleri.EdebiyatAcik, DersRenkleri.EdebiyatKoyu),
    "matematik" to DersKartStili(R.string.symbol_matematik, DersRenkleri.MatematikAcik, DersRenkleri.MatematikKoyu),
    "biyoloji" to DersKartStili(R.string.symbol_biyoloji, DersRenkleri.BiyolojiAcik, DersRenkleri.BiyolojiKoyu),
    "kimya" to DersKartStili(R.string.symbol_kimya, DersRenkleri.KimyaAcik, DersRenkleri.KimyaKoyu),
    "fizik" to DersKartStili(R.string.symbol_fizik, DersRenkleri.FizikAcik, DersRenkleri.FizikKoyu),
    "coğrafya" to DersKartStili(R.string.symbol_cografya, DersRenkleri.CografyaAcik, DersRenkleri.CografyaKoyu),
    "tarih" to DersKartStili(R.string.symbol_tarih, DersRenkleri.TarihAcik, DersRenkleri.TarihKoyu),
)

private val varsayilanDersKartStili = DersKartStili(
    R.string.symbol_diger,
    DersRenkleri.GenelAcik,
    DersRenkleri.GenelKoyu,
)

@Composable
fun DerslerEkrani(
    repository: DersRepository,
    onDersSecildi: (Ders, Boolean) -> Unit,
    onDisariAktar: () -> Unit,
    onIceriAktar: () -> Unit,
    sonYedek: SonYedekBilgisi?,
    yedeklemeIslemde: Boolean,
    onBakim: () -> Unit,
) {
    var arsivlilerGosteriliyor by remember { mutableStateOf(false) }
    var yenileme by remember { mutableIntStateOf(0) }
    var form by remember { mutableStateOf<DersFormu?>(null) }
    var yonetimAcik by remember { mutableStateOf(false) }
    var arsivOnayi by remember { mutableStateOf<Ders?>(null) }
    val akis = remember(repository, arsivlilerGosteriliyor, yenileme) {
        repository.dersleriIzle(arsivli = arsivlilerGosteriliyor)
    }
    val durum = produceState<DerslerEkraniDurumu>(DerslerEkraniDurumu.Yukleniyor, akis) {
        akis.catch { value = DerslerEkraniDurumu.Hata }
            .collect { value = DerslerEkraniDurumu.Hazir(it) }
    }.value
    val koyuTema = isSystemInDarkTheme()
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { icBosluk ->
        Column(
            modifier = Modifier.fillMaxSize().padding(icBosluk),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, top = 18.dp, end = 22.dp, bottom = 10.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_kicker),
                    color = colors.primary,
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.8.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.home_title),
                    modifier = Modifier.padding(top = 8.dp),
                    color = colors.onBackground,
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    text = stringResource(R.string.home_subtitle),
                    modifier = Modifier.padding(top = 4.dp),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Surface(
                    modifier = Modifier.padding(top = 12.dp),
                    shape = CircleShape,
                    color = colors.primaryContainer,
                ) {
                    Text(
                        text = dersSayisiMetni(durum),
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                        color = colors.onPrimaryContainer,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = !arsivlilerGosteriliyor,
                    onClick = { arsivlilerGosteriliyor = false },
                    label = { Text(stringResource(R.string.lesson_filter_active)) },
                )
                FilterChip(
                    selected = arsivlilerGosteriliyor,
                    onClick = { arsivlilerGosteriliyor = true },
                    label = { Text(stringResource(R.string.lesson_filter_archived)) },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = { form = DersFormu(ad = "") }) {
                    Text(stringResource(R.string.lesson_add))
                }
                TextButton(onClick = { yonetimAcik = true }) {
                    Text(stringResource(R.string.lesson_manage))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(onClick = onDisariAktar, enabled = !yedeklemeIslemde) {
                    Text(stringResource(R.string.export_action))
                }
                TextButton(onClick = onIceriAktar, enabled = !yedeklemeIslemde) {
                    Text(stringResource(R.string.backup_restore_action))
                }
            }
            Text(
                text = sonYedek?.let { info ->
                    val date = SimpleDateFormat("d MMM yyyy HH:mm", Locale("tr", "TR")).format(Date(info.olusturmaZamani))
                    val size = android.text.format.Formatter.formatShortFileSize(context, info.boyutBayt)
                    context.getString(
                        R.string.backup_last_info,
                        info.dosyaAdi,
                        date,
                        size,
                        context.getString(if (info.guvenlikKopyasi) R.string.backup_safety_copy else R.string.backup_external_copy),
                    )
                } ?: stringResource(R.string.backup_last_none),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 2.dp),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            TextButton(
                onClick = onBakim,
                modifier = Modifier.padding(start = 14.dp),
            ) {
                Text(stringResource(R.string.image_maintenance_action))
            }

            when (val current = durum) {
                DerslerEkraniDurumu.Yukleniyor -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                DerslerEkraniDurumu.Hata -> DerslerDurumMesaji(
                    metin = stringResource(R.string.home_error_dersler),
                    modifier = Modifier.weight(1f),
                    onRetry = { yenileme++ },
                )

                is DerslerEkraniDurumu.Hazir -> {
                    if (current.dersler.isEmpty()) {
                        DerslerDurumMesaji(
                            metin = stringResource(
                                if (arsivlilerGosteriliyor) R.string.lesson_empty_archived else R.string.home_empty_dersler,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 156.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentPadding = PaddingValues(start = 18.dp, top = 4.dp, end = 18.dp, bottom = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(current.dersler, key = { it.id }) { ders ->
                                DersKarti(
                                    veri = ders.toKartVerisi(),
                                    koyuTema = koyuTema,
                                    onClick = { onDersSecildi(ders, arsivlilerGosteriliyor) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val currentDersler = (durum as? DerslerEkraniDurumu.Hazir)?.dersler.orEmpty()
    if (yonetimAcik) {
        AlertDialog(
            onDismissRequest = { yonetimAcik = false },
            title = { Text(stringResource(R.string.lesson_manage)) },
            text = {
                if (currentDersler.isEmpty()) {
                    Text(stringResource(R.string.lesson_empty_archived))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(currentDersler, key = { it.id }) { ders ->
                            Card {
                                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                    Text(ders.ad, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        pluralStringResource(R.plurals.question_count, ders.soruSayisi, ders.soruSayisi),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        TextButton(onClick = {
                                            yonetimAcik = false
                                            form = DersFormu(duzenlenenDers = ders, ad = ders.ad)
                                        }) { Text(stringResource(R.string.topic_edit)) }
                                        TextButton(onClick = {
                                            if (arsivlilerGosteriliyor) {
                                                scope.launch {
                                                    val result = repository.dersArsivDurumunuDegistir(ders.id, false)
                                                    snackbar.showSnackbar(context.getString(
                                                        if (result == DersIslemSonucu.Basarili) R.string.lesson_restored else R.string.lesson_error,
                                                    ))
                                                }
                                            } else {
                                                arsivOnayi = ders
                                            }
                                        }) {
                                            Text(stringResource(if (arsivlilerGosteriliyor) R.string.lesson_restore else R.string.lesson_archive))
                                        }
                                        TextButton(onClick = {
                                            scope.launch { repository.dersSirasiniDegistir(ders.id, -1) }
                                        }) { Text("↑") }
                                        TextButton(onClick = {
                                            scope.launch { repository.dersSirasiniDegistir(ders.id, 1) }
                                        }) { Text("↓") }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { yonetimAcik = false }) { Text(stringResource(R.string.topic_cancel)) } },
        )
    }

    arsivOnayi?.let { ders ->
        AlertDialog(
            onDismissRequest = { arsivOnayi = null },
            title = { Text(stringResource(R.string.lesson_archive_title)) },
            text = { Text(context.getString(R.string.lesson_archive_confirm, ders.ad, ders.soruSayisi)) },
            confirmButton = {
                TextButton(onClick = {
                    arsivOnayi = null
                    scope.launch {
                        val result = repository.dersArsivDurumunuDegistir(ders.id, true)
                        snackbar.showSnackbar(context.getString(
                            if (result == DersIslemSonucu.Basarili) R.string.lesson_archived else R.string.lesson_error,
                        ))
                    }
                }) { Text(stringResource(R.string.lesson_archive)) }
            },
            dismissButton = { TextButton(onClick = { arsivOnayi = null }) { Text(stringResource(R.string.topic_cancel)) } },
        )
    }

    form?.let { currentForm ->
        AlertDialog(
            onDismissRequest = { if (!currentForm.kaydediliyor) form = null },
            title = {
                Text(stringResource(if (currentForm.duzenlenenDers == null) R.string.lesson_add else R.string.lesson_rename))
            },
            text = {
                Column {
                    androidx.compose.material3.OutlinedTextField(
                        value = currentForm.ad,
                        onValueChange = { form = currentForm.copy(ad = it, hata = null) },
                        enabled = !currentForm.kaydediliyor,
                        singleLine = true,
                        label = { Text(stringResource(R.string.lesson_name_label)) },
                        isError = currentForm.hata != null,
                    )
                    currentForm.hata?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = !currentForm.kaydediliyor, onClick = {
                    if (form?.kaydediliyor == true) return@TextButton
                    form = currentForm.copy(kaydediliyor = true, hata = null)
                    scope.launch {
                        val result = try {
                            currentForm.duzenlenenDers?.let {
                                repository.dersYenidenAdlandir(it.id, currentForm.ad)
                            } ?: repository.dersEkle(currentForm.ad)
                        } catch (cancel: CancellationException) {
                            throw cancel
                        } catch (_: Exception) {
                            null
                        }
                        if (result == DersIslemSonucu.Basarili) {
                            form = null
                            snackbar.showSnackbar(context.getString(
                                if (currentForm.duzenlenenDers == null) R.string.lesson_added else R.string.lesson_updated,
                            ))
                        } else {
                            val error = when (result) {
                                DersIslemSonucu.BosAd -> R.string.lesson_error_empty
                                DersIslemSonucu.AyniAdVar -> R.string.lesson_error_duplicate
                                DersIslemSonucu.Bulunamadi -> R.string.lesson_error_not_found
                                else -> R.string.lesson_error
                            }
                            form = form?.copy(kaydediliyor = false, hata = context.getString(error))
                        }
                    }
                }) { Text(stringResource(if (currentForm.kaydediliyor) R.string.topic_saving else R.string.topic_save)) }
            },
            dismissButton = {
                TextButton(enabled = !currentForm.kaydediliyor, onClick = { form = null }) {
                    Text(stringResource(R.string.topic_cancel))
                }
            },
        )
    }
}

@Composable
private fun dersSayisiMetni(durum: DerslerEkraniDurumu): String = when (durum) {
    DerslerEkraniDurumu.Yukleniyor -> stringResource(R.string.home_loading_dersler)
    DerslerEkraniDurumu.Hata -> stringResource(R.string.home_error_dersler)
    is DerslerEkraniDurumu.Hazir -> pluralStringResource(
        R.plurals.lesson_count,
        durum.dersler.size,
        durum.dersler.size,
    )
}

@Composable
private fun DerslerDurumMesaji(
    metin: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = metin,
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
        onRetry?.let { retry -> TextButton(onClick = retry) { Text(stringResource(R.string.topic_retry)) } }
    }
}

private fun Ders.toKartVerisi(): DersKartVerisi {
    val style = dersKartStilleri[ad.trim().lowercase(Locale.ROOT)] ?: varsayilanDersKartStili
    return DersKartVerisi(
        ad = ad,
        isaret = style.isaret,
        acikRenk = style.acikRenk,
        koyuRenk = style.koyuRenk,
        soruSayisi = soruSayisi,
    )
}

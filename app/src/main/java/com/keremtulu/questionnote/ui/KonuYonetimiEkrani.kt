package com.keremtulu.questionnote.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.model.Konu
import com.keremtulu.questionnote.domain.model.KonuIslemSonucu
import com.keremtulu.questionnote.domain.model.KonuSilmeSonucu
import com.keremtulu.questionnote.domain.repository.KonuRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private sealed interface KonuEkraniDurumu {
    data object Yukleniyor : KonuEkraniDurumu
    data class Hazir(val konular: List<Konu>) : KonuEkraniDurumu
    data object Hata : KonuEkraniDurumu
}

private data class KonuFormu(
    val duzenlenenKonu: Konu? = null,
    val ad: String,
    val hata: String? = null,
    val kaydediliyor: Boolean = false,
)

@Composable
fun KonuYonetimiEkrani(
    ders: Ders,
    repository: KonuRepository,
    onGeri: () -> Unit,
) {
    var arsivlilerGosteriliyor by remember { mutableStateOf(false) }
    var akisYenileme by remember { mutableIntStateOf(0) }
    var konuFormu by remember { mutableStateOf<KonuFormu?>(null) }
    var silinecekKonu by remember { mutableStateOf<Konu?>(null) }
    var kullanimdakiKonu by remember { mutableStateOf<Pair<Konu, Int>?>(null) }
    val konularAkisi = remember(repository, ders.id, arsivlilerGosteriliyor, akisYenileme) {
        repository.konulariIzle(ders.id, arsivli = arsivlilerGosteriliyor)
    }
    val durum = produceState<KonuEkraniDurumu>(
        initialValue = KonuEkraniDurumu.Yukleniyor,
        key1 = konularAkisi,
    ) {
        konularAkisi
            .catch { value = KonuEkraniDurumu.Hata }
            .collect { konular -> value = KonuEkraniDurumu.Hazir(konular) }
    }.value
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, top = 8.dp, end = 22.dp, bottom = 12.dp),
            ) {
                TextButton(onClick = onGeri) {
                    Text(text = stringResource(R.string.topic_back_to_lessons))
                }
                Text(
                    text = stringResource(R.string.topic_screen_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = ders.ad,
                    modifier = Modifier.padding(top = 3.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FilterChip(
                    selected = !arsivlilerGosteriliyor,
                    onClick = { arsivlilerGosteriliyor = false },
                    label = { Text(stringResource(R.string.topic_filter_active)) },
                )
                FilterChip(
                    selected = arsivlilerGosteriliyor,
                    onClick = { arsivlilerGosteriliyor = true },
                    label = { Text(stringResource(R.string.topic_filter_archived)) },
                )
            }

            if (!arsivlilerGosteriliyor) {
                Button(
                    onClick = { konuFormu = KonuFormu(ad = "") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .padding(top = 6.dp),
                ) {
                    Text(text = stringResource(R.string.topic_add))
                }
            }

            when (val current = durum) {
                KonuEkraniDurumu.Yukleniyor -> KonuDurumMesaji(
                    metin = stringResource(R.string.topic_loading),
                    modifier = Modifier.weight(1f),
                    yukleniyor = true,
                )

                KonuEkraniDurumu.Hata -> Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.topic_load_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    TextButton(onClick = { akisYenileme++ }) {
                        Text(text = stringResource(R.string.topic_retry))
                    }
                }

                is KonuEkraniDurumu.Hazir -> {
                    if (current.konular.isEmpty()) {
                        KonuDurumMesaji(
                            metin = stringResource(
                                if (arsivlilerGosteriliyor) {
                                    R.string.topic_empty_archived
                                } else {
                                    R.string.topic_empty_active
                                },
                            ),
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(current.konular, key = { it.id }) { konu ->
                                KonuListeSatiri(
                                    konu = konu,
                                    onSil = { silinecekKonu = konu },
                                    onDuzenle = {
                                        konuFormu = KonuFormu(duzenlenenKonu = konu, ad = konu.ad)
                                    },
                                    onArsivDurumunuDegistir = {
                                        scope.launch {
                                            val sonuc = try {
                                                if (konu.arsivli) {
                                                    repository.konuYenidenEtkinlestir(konu.id)
                                                } else {
                                                    repository.konuArsivle(konu.id)
                                                }
                                            } catch (iptal: CancellationException) {
                                                throw iptal
                                            } catch (_: Exception) {
                                                null
                                            }
                                            val mesaj = when (sonuc) {
                                                KonuIslemSonucu.Basarili -> context.getString(
                                                    if (konu.arsivli) R.string.topic_restored else R.string.topic_archived,
                                                )
                                                KonuIslemSonucu.Bulunamadi -> context.getString(R.string.topic_not_found)
                                                else -> context.getString(R.string.topic_change_error)
                                            }
                                            snackbarHostState.showSnackbar(mesaj)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    silinecekKonu?.let { konu ->
        AlertDialog(
            onDismissRequest = { silinecekKonu = null },
            title = { Text(stringResource(R.string.topic_delete_title)) },
            text = { Text(stringResource(R.string.topic_delete_message, konu.ad)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        silinecekKonu = null
                        scope.launch {
                            val sonuc = try {
                                repository.konuSil(konu.id)
                            } catch (iptal: CancellationException) {
                                throw iptal
                            } catch (_: Exception) {
                                null
                            }
                            when (sonuc) {
                                KonuSilmeSonucu.Basarili -> snackbarHostState.showSnackbar(
                                    context.getString(R.string.topic_deleted),
                                )
                                is KonuSilmeSonucu.Kullanimda -> kullanimdakiKonu = konu to sonuc.soruSayisi
                                KonuSilmeSonucu.Bulunamadi -> snackbarHostState.showSnackbar(
                                    context.getString(R.string.topic_not_found),
                                )
                                null -> snackbarHostState.showSnackbar(
                                    context.getString(R.string.topic_delete_error),
                                )
                            }
                        }
                    },
                ) { Text(stringResource(R.string.topic_delete_confirm), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { silinecekKonu = null }) {
                    Text(stringResource(R.string.topic_cancel))
                }
            },
        )
    }

    kullanimdakiKonu?.let { (konu, adet) ->
        AlertDialog(
            onDismissRequest = { kullanimdakiKonu = null },
            title = { Text(stringResource(R.string.topic_delete_in_use_title)) },
            text = { Text(stringResource(R.string.topic_delete_in_use_message, adet)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        kullanimdakiKonu = null
                        scope.launch {
                            val sonuc = try {
                                repository.konuArsivle(konu.id)
                            } catch (iptal: CancellationException) {
                                throw iptal
                            } catch (_: Exception) {
                                null
                            }
                            snackbarHostState.showSnackbar(
                                context.getString(
                                    if (sonuc == KonuIslemSonucu.Basarili) {
                                        R.string.topic_archived
                                    } else {
                                        R.string.topic_change_error
                                    },
                                ),
                            )
                        }
                    },
                ) { Text(stringResource(R.string.topic_delete_archive)) }
            },
            dismissButton = {
                TextButton(onClick = { kullanimdakiKonu = null }) {
                    Text(stringResource(R.string.topic_cancel))
                }
            },
        )
    }

    val form = konuFormu
    if (form != null) {
        AlertDialog(
            onDismissRequest = {
                if (!form.kaydediliyor) konuFormu = null
            },
            title = {
                Text(
                    text = stringResource(
                        if (form.duzenlenenKonu == null) R.string.topic_dialog_add_title else R.string.topic_dialog_edit_title,
                    ),
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = form.ad,
                        onValueChange = { yeniAd -> konuFormu = form.copy(ad = yeniAd, hata = null) },
                        enabled = !form.kaydediliyor,
                        singleLine = true,
                        label = { Text(stringResource(R.string.topic_name_label)) },
                        isError = form.hata != null,
                    )
                    form.hata?.let { hata ->
                        Text(
                            text = hata,
                            modifier = Modifier.padding(top = 6.dp),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !form.kaydediliyor,
                    onClick = {
                        if (konuFormu?.kaydediliyor == true) return@TextButton
                        konuFormu = form.copy(kaydediliyor = true, hata = null)
                        scope.launch {
                            val sonuc = try {
                                if (form.duzenlenenKonu == null) {
                                    repository.konuEkle(ders.id, form.ad)
                                } else {
                                    repository.konuYenidenAdlandir(form.duzenlenenKonu.id, form.ad)
                                }
                            } catch (iptal: CancellationException) {
                                throw iptal
                            } catch (_: Exception) {
                                null
                            }

                            if (sonuc == KonuIslemSonucu.Basarili) {
                                konuFormu = null
                                snackbarHostState.showSnackbar(
                                    context.getString(
                                        if (form.duzenlenenKonu == null) R.string.topic_added else R.string.topic_updated,
                                    ),
                                )
                            } else {
                                val hataMetni = sonuc?.let { context.getString(it.hataMetniKaynak()) }
                                    ?: context.getString(R.string.topic_save_error)
                                konuFormu = konuFormu?.copy(kaydediliyor = false, hata = hataMetni)
                            }
                        }
                    },
                ) {
                    Text(
                        text = stringResource(
                            if (form.kaydediliyor) R.string.topic_saving else R.string.topic_save,
                        ),
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !form.kaydediliyor,
                    onClick = { konuFormu = null },
                ) {
                    Text(text = stringResource(R.string.topic_cancel))
                }
            },
        )
    }
}

@Composable
private fun KonuListeSatiri(
    konu: Konu,
    onSil: () -> Unit,
    onDuzenle: () -> Unit,
    onArsivDurumunuDegistir: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = konu.ad,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            if (konu.arsivli) {
                Text(
                    text = stringResource(R.string.topic_archived_label),
                    modifier = Modifier.padding(top = 2.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDuzenle) {
                    Text(text = stringResource(R.string.topic_edit))
                }
                TextButton(onClick = onArsivDurumunuDegistir) {
                    Text(
                        text = stringResource(
                            if (konu.arsivli) R.string.topic_restore else R.string.topic_archive,
                        ),
                    )
                }
                TextButton(onClick = onSil) {
                    Text(
                        text = stringResource(R.string.topic_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun KonuDurumMesaji(
    metin: String,
    modifier: Modifier = Modifier,
    yukleniyor: Boolean = false,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        if (yukleniyor) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        } else {
            Text(
                text = metin,
                modifier = Modifier.padding(horizontal = 24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

private fun KonuIslemSonucu.hataMetniKaynak(): Int =
    when (this) {
        KonuIslemSonucu.Basarili -> R.string.topic_save
        KonuIslemSonucu.BosAd -> R.string.topic_error_empty_name
        KonuIslemSonucu.AyniAdVar -> R.string.topic_error_duplicate_name
        KonuIslemSonucu.ArsivliAyniAdVar -> R.string.topic_error_archived_duplicate_name
        KonuIslemSonucu.Bulunamadi -> R.string.topic_not_found
    }

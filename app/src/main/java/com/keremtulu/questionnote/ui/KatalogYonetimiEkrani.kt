package com.keremtulu.questionnote.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.SoruSecenekTuru
import com.keremtulu.questionnote.domain.model.SoruSecenegi
import com.keremtulu.questionnote.domain.model.SoruSecenegiSilmeSonucu
import com.keremtulu.questionnote.domain.repository.SoruRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler

private sealed interface KatalogDurumu {
    data object Yukleniyor : KatalogDurumu
    data class Hazir(val kaynaklar: List<SoruSecenegi>, val nedenler: List<SoruSecenegi>) : KatalogDurumu
    data object Hata : KatalogDurumu
}

/** Manage the shared source / wrong-reason catalog, including deleting user-added entries. */
@Composable
fun KatalogYonetimiEkrani(
    soruRepository: SoruRepository,
    onGeri: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var silinecek by remember { mutableStateOf<SoruSecenegi?>(null) }
    var uyariMetni by remember { mutableStateOf<String?>(null) }

    val akis = remember(soruRepository) {
        combine(
            soruRepository.secenekleriIzle(SoruSecenekTuru.KAYNAK),
            soruRepository.secenekleriIzle(SoruSecenekTuru.YANLIS_NEDENI),
        ) { kaynaklar, nedenler -> KatalogDurumu.Hazir(kaynaklar, nedenler) }
    }
    val durum = produceState<KatalogDurumu>(KatalogDurumu.Yukleniyor, akis) {
        akis.catch { value = KatalogDurumu.Hata }.collect { value = it }
    }.value

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { insets ->
        Column(Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 4.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PremiumTextButton(onClick = onGeri) {
                    Text(stringResource(R.string.common_back))
                }
            }
            @Composable
            fun KatalogUstKart(modifier: Modifier = Modifier) {
                com.keremtulu.questionnote.ui.bilesenler.DuzBaslik(
                    baslik = stringResource(R.string.catalog_manage_title),
                    alt = stringResource(R.string.catalog_manage_subtitle),
                )
            }
            when (val current = durum) {
                KatalogDurumu.Yukleniyor -> {
                    KatalogUstKart(Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                }
                KatalogDurumu.Hata -> {
                    KatalogUstKart(Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            stringResource(R.string.filtered_practice_options_error),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                is KatalogDurumu.Hazir -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, top = 6.dp, end = 20.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    item(key = "katalog-hero") {
                        KatalogUstKart(Modifier.padding(bottom = 4.dp))
                    }
                    item { Baslik(stringResource(R.string.catalog_source_heading)) }
                    if (current.kaynaklar.isEmpty()) {
                        item { BosMesaj() }
                    } else {
                        items(current.kaynaklar, key = { "k-${it.id}" }) { secenek ->
                            KatalogSatiri(secenek, onSil = { silinecek = secenek })
                        }
                    }
                    item { Baslik(stringResource(R.string.catalog_reason_heading)) }
                    if (current.nedenler.isEmpty()) {
                        item { BosMesaj() }
                    } else {
                        items(current.nedenler, key = { "n-${it.id}" }) { secenek ->
                            KatalogSatiri(secenek, onSil = { silinecek = secenek })
                        }
                    }
                }
            }
        }
    }

    silinecek?.let { secenek ->
        AlertDialog(
            onDismissRequest = { silinecek = null },
            title = { Text(stringResource(R.string.catalog_delete_title)) },
            text = { Text(stringResource(R.string.catalog_delete_message, secenek.ad)) },
            confirmButton = {
                PremiumTextButton(
                    onClick = {
                        silinecek = null
                        scope.launch {
                            val sonuc = try {
                                soruRepository.secenekSil(secenek.id)
                            } catch (iptal: CancellationException) {
                                throw iptal
                            } catch (_: Exception) {
                                null
                            }
                            when (sonuc) {
                                SoruSecenegiSilmeSonucu.Basarili -> snackbarHostState.showSnackbar(
                                    context.getString(R.string.catalog_deleted),
                                )
                                is SoruSecenegiSilmeSonucu.Kullanimda -> uyariMetni = context.getString(
                                    R.string.catalog_delete_in_use_message,
                                    secenek.ad,
                                    sonuc.soruSayisi,
                                )
                                SoruSecenegiSilmeSonucu.VarsayilanSilinemez -> uyariMetni =
                                    context.getString(R.string.catalog_delete_default_message)
                                SoruSecenegiSilmeSonucu.Bulunamadi -> snackbarHostState.showSnackbar(
                                    context.getString(R.string.catalog_delete_error),
                                )
                                null -> snackbarHostState.showSnackbar(
                                    context.getString(R.string.catalog_delete_error),
                                )
                            }
                        }
                    },
                ) { Text(stringResource(R.string.catalog_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                PremiumTextButton(onClick = { silinecek = null }) {
                    Text(stringResource(R.string.topic_cancel))
                }
            },
        )
    }

    uyariMetni?.let { metin ->
        AlertDialog(
            onDismissRequest = { uyariMetni = null },
            title = { Text(stringResource(R.string.catalog_delete_in_use_title)) },
            text = { Text(metin) },
            confirmButton = {
                PremiumTextButton(onClick = { uyariMetni = null }) {
                    Text(stringResource(R.string.topic_cancel))
                }
            },
        )
    }
}

@Composable
private fun Baslik(metin: String) {
    Text(
        metin,
        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp, start = 2.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
private fun BosMesaj() {
    Text(
        stringResource(R.string.catalog_empty),
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun KatalogSatiri(secenek: SoruSecenegi, onSil: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    secenek.ad,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (secenek.varsayilan) {
                    Text(
                        stringResource(R.string.catalog_default_label),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            if (!secenek.varsayilan) {
                PremiumTextButton(onClick = onSil) {
                    Text(
                        stringResource(R.string.catalog_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

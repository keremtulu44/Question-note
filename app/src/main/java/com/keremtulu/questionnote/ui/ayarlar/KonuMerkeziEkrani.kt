package com.keremtulu.questionnote.ui.ayarlar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.KonuRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine

private data class KonuMerkeziSatiri(val ders: Ders, val arsivli: Boolean, val konuSayisi: Int)

/** All lessons are reachable here even if a selected exam profile currently hides them. */
@Composable
fun KonuMerkeziEkrani(
    dersRepository: DersRepository,
    konuRepository: KonuRepository,
    onDers: (Ders) -> Unit,
    onGeri: () -> Unit,
) {
    var arama by rememberSaveable { mutableStateOf("") }
    var yenile by remember { mutableIntStateOf(0) }
    var hata by remember { mutableStateOf(false) }
    val akis = remember(dersRepository, konuRepository, yenile) {
        combine(dersRepository.dersleriIzle(false), dersRepository.dersleriIzle(true), konuRepository.tumunuIzle()) { aktif, arsiv, konular ->
            (aktif.map { it to false } + arsiv.map { it to true }).map { (ders, arsivli) ->
                KonuMerkeziSatiri(ders, arsivli, konular.count { it.dersId == ders.id })
            }
        }
    }
    val dersler by produceState<List<KonuMerkeziSatiri>?>(null, akis) {
        hata = false
        akis.catch { hata = true }.collect { value = it }
    }
    Scaffold { insets ->
        LazyColumn(
            Modifier.fillMaxSize().padding(insets),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                TextButton(onClick = onGeri) { Text(stringResource(R.string.common_back)) }
                Text(stringResource(R.string.settings_topics), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.topics_center_note), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                OutlinedTextField(arama, { arama = it }, Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.topics_search)) }, singleLine = true)
            }
            if (hata) {
                item { TextButton(onClick = { yenile++ }) { Text(stringResource(R.string.topic_retry)) } }
            } else if (dersler == null) {
                item { CircularProgressIndicator() }
            } else {
                val bulunanlar = dersler.orEmpty().filter { it.ders.ad.contains(arama, ignoreCase = true) }
                if (bulunanlar.isEmpty()) item { Text(stringResource(R.string.topics_search_empty)) }
                items(bulunanlar, key = { it.ders.id }) { satir ->
                    Card(onClick = { onDers(satir.ders) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(satir.ders.ad, style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.topics_center_count, satir.konuSayisi, satir.ders.soruSayisi),
                                style = MaterialTheme.typography.bodySmall)
                            if (satir.arsivli) Text(stringResource(R.string.topic_filter_archived), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

package com.keremtulu.questionnote.ui.ogretmen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.prefs.OgretmenDeposu
import com.keremtulu.questionnote.domain.model.OgretmenDurum
import com.keremtulu.questionnote.domain.model.OgretmenSorusu
import com.keremtulu.questionnote.domain.model.OgretmenVerisi
import com.keremtulu.questionnote.ui.bilesenler.premium.CamKarti
import com.keremtulu.questionnote.ui.bilesenler.premium.CamYukseklik
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import kotlinx.coroutines.launch

/**
 * Cross-class queue: one list for "what do I solve today", with the oldest waiting question first.
 * Group filtering is a chip row instead of a second navigation level.
 */
@Composable
internal fun OgretmenKuyrukEkrani(
    veri: OgretmenVerisi,
    ogretmenDeposu: OgretmenDeposu,
    gorselDeposu: LocalSoruGorselDeposu,
    dersAdlari: Map<String, String>,
    konuAdlari: Map<String, String>,
    bugun: Long,
    snackbarHostState: SnackbarHostState,
    onGrupAc: (String) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var seciliGrupId by remember { mutableStateOf<String?>(null) }
    var yalnizGeciken by remember { mutableStateOf(false) }
    var sunumIndex by remember { mutableStateOf<Int?>(null) }
    var kopyalanacak by remember { mutableStateOf<OgretmenSorusu?>(null) }
    var secimModu by remember { mutableStateOf(false) }
    var seciliIdler by remember { mutableStateOf<Set<String>>(emptySet()) }

    val kuyruk = remember(veri, seciliGrupId, yalnizGeciken, bugun) {
        veri.kuyruk(grupId = seciliGrupId, yalnizGeciken = yalnizGeciken, bugun = bugun)
    }
    val grupAdlari = remember(veri.gruplar) { veri.gruplar.associate { it.id to it.ad } }
    val enEski = kuyruk.firstOrNull()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, top = 10.dp, end = 18.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                OgretmenHeroKarti(
                    kicker = stringResource(R.string.teacher_queue_kicker),
                    baslik = stringResource(R.string.teacher_queue_title),
                    altBaslik = stringResource(R.string.teacher_queue_subtitle),
                    bekleyen = kuyruk.size,
                    cozulen = veri.sorular.size - kuyruk.size,
                    grupSayisi = veri.gruplar.count { !it.arsivli },
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FiltreCipi(
                        metin = stringResource(R.string.teacher_queue_all_groups),
                        secili = seciliGrupId == null && !yalnizGeciken,
                        onClick = {
                            seciliGrupId = null
                            yalnizGeciken = false
                        },
                    )
                    FiltreCipi(
                        metin = stringResource(R.string.teacher_queue_filter_overdue),
                        secili = yalnizGeciken,
                        onClick = { yalnizGeciken = !yalnizGeciken },
                    )
                    Box(Modifier.weight(1f))
                    if (kuyruk.isNotEmpty()) {
                        PremiumTextButton(
                            onClick = {
                                if (secimModu) {
                                    secimModu = false
                                    seciliIdler = emptySet()
                                } else {
                                    secimModu = true
                                }
                            },
                            modifier = Modifier.testTag("ogretmen_kuyruk_secim"),
                        ) {
                            Text(
                                stringResource(
                                    if (secimModu) R.string.teacher_bulk_cancel
                                    else R.string.teacher_bulk_select,
                                ),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(veri.gruplar.filter { !it.arsivli }.sortedBy { it.sira }, key = { it.id }) { grup ->
                        FiltreCipi(
                            metin = grup.ad,
                            secili = seciliGrupId == grup.id,
                            onClick = {
                                seciliGrupId = if (seciliGrupId == grup.id) null else grup.id
                            },
                        )
                    }
                }
            }
            if (kuyruk.isEmpty()) {
                item {
                    CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                        Column(
                            Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.teacher_queue_empty_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = stringResource(R.string.teacher_queue_empty_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            items(kuyruk, key = { it.id }) { soru ->
                OgretmenSoruKarti(
                    soru = soru,
                    grupAdi = grupAdlari[soru.grupId],
                    dersAdi = soru.dersId?.let { dersAdlari[it] },
                    konuAdi = soru.konuId?.let { konuAdlari[it] },
                    koyuTema = false,
                    gorselDeposu = gorselDeposu,
                    onTamEkran = { sunumIndex = kuyruk.indexOfFirst { it.id == soru.id } },
                    onCozulduDegistir = { cozuldu ->
                        scope.launch { ogretmenDeposu.cozulduDurumuDegistir(soru.id, cozuldu) }
                    },
                    onBaskaGrubaEkle = { kopyalanacak = soru },
                    onDuzenle = { onGrupAc(soru.grupId) },
                    onSil = { scope.launch { ogretmenDeposu.soruSil(soru.id) } },
                    secimModu = secimModu,
                    secili = soru.id in seciliIdler,
                    onSecimDegistir = { sec ->
                        seciliIdler = if (sec) seciliIdler + soru.id else seciliIdler - soru.id
                    },
                )
            }
            if (enEski != null && kuyruk.size > 1) {
                item {
                    Text(
                        text = stringResource(R.string.teacher_queue_sort),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                    )
                }
            }
        }

        if (secimModu) {
            SecimCubugu(
                adet = seciliIdler.size,
                modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 14.dp, vertical = 18.dp),
                onCoz = {
                    scope.launch {
                        ogretmenDeposu.topluDurumDegistir(seciliIdler, OgretmenDurum.COZULDU)
                        seciliIdler = emptySet()
                        secimModu = false
                    }
                },
                onCozme = {
                    scope.launch {
                        ogretmenDeposu.topluDurumDegistir(seciliIdler, OgretmenDurum.BEKLIYOR)
                        seciliIdler = emptySet()
                        secimModu = false
                    }
                },
                onArsivle = {
                    scope.launch {
                        ogretmenDeposu.topluDurumDegistir(seciliIdler, OgretmenDurum.ARSIV)
                        seciliIdler = emptySet()
                        secimModu = false
                    }
                },
                onSil = {
                    scope.launch {
                        ogretmenDeposu.topluSil(seciliIdler)
                        seciliIdler = emptySet()
                        secimModu = false
                    }
                },
            )
        }
    }

    sunumIndex?.let { index ->
        OgretmenSunumDialog(
            sorular = kuyruk,
            baslangicIndex = index,
            grupAdlari = grupAdlari,
            gorselDeposu = gorselDeposu,
            onKapat = { sunumIndex = null },
            onCozulduDegistir = { soru, cozuldu ->
                scope.launch { ogretmenDeposu.cozulduDurumuDegistir(soru.id, cozuldu) }
            },
            onBaskaGrubaEkle = { soru ->
                sunumIndex = null
                kopyalanacak = soru
            },
        )
    }

    kopyalanacak?.let { soru ->
        BaskaGrubaEkleDialog(
            digerGruplar = veri.gruplar.filter { it.id != soru.grupId && !it.arsivli }.sortedBy { it.sira },
            kaynak = soru,
            onDismiss = { kopyalanacak = null },
            onOnayla = { hedefler ->
                scope.launch {
                    val sayi = ogretmenDeposu.baskaGruplaraKopyala(soru.id, hedefler)
                    kopyalanacak = null
                    if (sayi > 0) {
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.teacher_copy_done, sayi),
                        )
                    }
                }
            },
        )
    }
}

@Composable
internal fun FiltreCipi(
    metin: String,
    secili: Boolean,
    onClick: () -> Unit,
) {
    val vurgu = MaterialTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        modifier = Modifier.heightIn(min = 38.dp),
        shape = RoundedCornerShape(999.dp),
        color = if (secili) vurgu.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (secili) vurgu.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Text(
            text = metin,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (secili) FontWeight.Bold else FontWeight.Medium,
            color = if (secili) vurgu else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

/** Explains what the archive action does before it runs; nothing is deleted. */
@Composable
internal fun ArsivOnayDialogu(
    adet: Int,
    onDismiss: () -> Unit,
    onOnayla: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text(stringResource(R.string.teacher_archive_title), fontWeight = FontWeight.Bold) },
        text = { Text(stringResource(R.string.teacher_archive_message, adet)) },
        confirmButton = {
            TextButton(onClick = onOnayla, modifier = Modifier.testTag("teacher_archive_confirm")) {
                Text(stringResource(R.string.teacher_archive_action), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
    )
}

/** Colour tokens used by queue chips; keeps the queue visually distinct from the class list. */
internal object KuyrukRenkleri {
    val vurgu = PremiumMarkaRenkler.Deniz
    val uyari = PremiumMarkaRenkler.Mercan
}

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.export.SonYedekBilgisi
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.data.prefs.OgretmenDeposu
import com.keremtulu.questionnote.domain.model.OgretmenDurum
import com.keremtulu.questionnote.domain.model.OgretmenGrubu
import com.keremtulu.questionnote.domain.model.OgretmenSorusu
import com.keremtulu.questionnote.domain.model.OgretmenVerisi
import com.keremtulu.questionnote.ui.bilesenler.premium.CamKarti
import com.keremtulu.questionnote.ui.bilesenler.premium.CamYukseklik
import com.keremtulu.questionnote.ui.premium.PremiumButton
import com.keremtulu.questionnote.ui.premium.PremiumGradyanButon
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

private enum class GrupSekmesi { COZMEDIKLERIM, COZDUKLERIM, ARSIV }

@Composable
internal fun OgretmenGrupDetayEkrani(
    grup: OgretmenGrubu,
    veri: OgretmenVerisi,
    ogretmenDeposu: OgretmenDeposu,
    gorselDeposu: LocalSoruGorselDeposu,
    dersAdlari: Map<String, String>,
    konuAdlari: Map<String, String>,
    bugun: Long,
    sonYedek: SonYedekBilgisi?,
    snackbarHostState: SnackbarHostState,
    onGeri: () -> Unit,
    onSoruEkle: () -> Unit,
    onSoruDuzenle: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sorulari = remember(veri.sorular, grup.id) { veri.grupSorulari(grup.id) }

    var sekme by remember(grup.id) { mutableStateOf(GrupSekmesi.COZMEDIKLERIM) }
    var secimModu by remember(grup.id) { mutableStateOf(false) }
    var seciliIdler by remember(grup.id) { mutableStateOf<Set<String>>(emptySet()) }
    var sunumId by remember(grup.id) { mutableStateOf<String?>(null) }
    var kopyalanan by remember(grup.id) { mutableStateOf<OgretmenSorusu?>(null) }
    var tasinan by remember(grup.id) { mutableStateOf<OgretmenSorusu?>(null) }
    var silinecekIdler by remember(grup.id) { mutableStateOf<Set<String>?>(null) }
    var grupFormu by remember(grup.id) { mutableStateOf(false) }
    var grupSilOnayi by remember(grup.id) { mutableStateOf(false) }
    var menuAcik by remember(grup.id) { mutableStateOf(false) }

    val bekleyen = remember(sorulari) {
        sorulari.filter { it.durum == OgretmenDurum.BEKLIYOR }
            .sortedWith(compareBy<OgretmenSorusu> { it.planGun ?: Long.MAX_VALUE }.thenBy { it.olusturma })
    }
    val cozulen = remember(sorulari) {
        sorulari.filter { it.durum == OgretmenDurum.COZULDU }.sortedByDescending { it.cozulme ?: it.guncelleme }
    }
    val arsiv = remember(sorulari) {
        sorulari.filter { it.durum == OgretmenDurum.ARSIV }.sortedByDescending { it.guncelleme }
    }
    val liste = when (sekme) {
        GrupSekmesi.COZMEDIKLERIM -> bekleyen
        GrupSekmesi.COZDUKLERIM -> cozulen
        GrupSekmesi.ARSIV -> arsiv
    }

    fun secimiTemizle() {
        secimModu = false
        seciliIdler = emptySet()
    }

    fun sil(idler: Set<String>) {
        if (idler.isEmpty()) return
        val hedefler = sorulari.filter { it.id in idler }
        scope.launch {
            val sayi = ogretmenDeposu.topluSil(idler, dosyalariKoru = true)
            if (sayi <= 0) return@launch
            secimiTemizle()
            val sonuc = snackbarHostState.showSnackbar(
                message = context.getString(R.string.teacher_deleted_message, sayi),
                actionLabel = context.getString(R.string.teacher_undo),
                duration = SnackbarDuration.Long,
            )
            if (sonuc == SnackbarResult.ActionPerformed) {
                val geri = ogretmenDeposu.silmeyiGeriAl(hedefler)
                snackbarHostState.showSnackbar(
                    context.getString(
                        if (geri > 0) R.string.teacher_undo_restored
                        else R.string.teacher_undo_failed,
                    ),
                )
            }
        }
    }

    fun durumUygula(idler: Set<String>, durum: OgretmenDurum) {
        if (idler.isEmpty()) return
        scope.launch {
            val degisen = ogretmenDeposu.topluDurumDegistir(idler, durum)
            if (degisen > 0) secimiTemizle()
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PremiumTextButton(onClick = onGeri) {
                        Text("← " + stringResource(R.string.teacher_nav_groups))
                    }
                    Box(Modifier.weight(1f))
                    if (liste.isNotEmpty()) {
                        PremiumTextButton(
                            onClick = {
                                if (secimModu) secimiTemizle() else secimModu = true
                            },
                            modifier = Modifier.testTag("ogretmen_toplu_sec"),
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
                    PremiumTextButton(onClick = { menuAcik = true }) {
                        Text("⋮", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            item {
                // The count is stated here, not in the delete dialog only, so the cost of a delete
                // is visible before the teacher even opens the menu.
                OgretmenHeroKarti(
                    kicker = grup.ad,
                    baslik = stringResource(R.string.teacher_group_detail_title),
                    altBaslik = grup.aciklama
                        ?: stringResource(R.string.teacher_group_detail_subtitle),
                    bekleyen = bekleyen.size,
                    cozulen = cozulen.size,
                    grupSayisi = sorulari.size,
                    birinciEtiket = stringResource(R.string.teacher_stat_total),
                    ikinciEtiket = stringResource(R.string.teacher_detail_tab_pending),
                    ucuncuEtiket = stringResource(R.string.teacher_detail_tab_solved),
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    SekmeSecici(
                        metin = stringResource(R.string.teacher_detail_tab_pending, bekleyen.size),
                        secili = sekme == GrupSekmesi.COZMEDIKLERIM,
                        modifier = Modifier.weight(1f).testTag("sekme_cozmediklerim"),
                        onClick = {
                            sekme = GrupSekmesi.COZMEDIKLERIM
                            secimiTemizle()
                        },
                    )
                    SekmeSecici(
                        metin = stringResource(R.string.teacher_detail_tab_solved, cozulen.size),
                        secili = sekme == GrupSekmesi.COZDUKLERIM,
                        modifier = Modifier.weight(1f).testTag("sekme_cozduklerim"),
                        onClick = {
                            sekme = GrupSekmesi.COZDUKLERIM
                            secimiTemizle()
                        },
                    )
                    if (arsiv.isNotEmpty()) {
                        SekmeSecici(
                            metin = stringResource(R.string.teacher_tab_archived, arsiv.size),
                            secili = sekme == GrupSekmesi.ARSIV,
                            modifier = Modifier.weight(1f).testTag("sekme_arsiv"),
                            onClick = {
                                sekme = GrupSekmesi.ARSIV
                                secimiTemizle()
                            },
                        )
                    }
                }
            }
            if (liste.isEmpty()) {
                item {
                    CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(
                                    when (sekme) {
                                        GrupSekmesi.COZMEDIKLERIM -> R.string.teacher_detail_empty_pending
                                        GrupSekmesi.COZDUKLERIM -> R.string.teacher_detail_empty_solved
                                        GrupSekmesi.ARSIV -> R.string.teacher_empty_archive_title
                                    },
                                ),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = stringResource(R.string.teacher_detail_empty_pending_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            items(liste, key = { it.id }) { soru ->
                val secili = soru.id in seciliIdler
                OgretmenSoruKarti(
                    soru = soru,
                    grupAdi = grup.ad,
                    dersAdi = soru.dersId?.let { dersAdlari[it] },
                    konuAdi = soru.konuId?.let { konuAdlari[it] },
                    koyuTema = false,
                    gorselDeposu = gorselDeposu,
                    onTamEkran = { sunumId = soru.id },
                    onCozulduDegistir = { cozuldu ->
                        scope.launch {
                            ogretmenDeposu.cozulduDurumuDegistir(soru.id, cozuldu)
                        }
                    },
                    onBaskaGrubaEkle = { kopyalanan = soru },
                    onDuzenle = { onSoruDuzenle(soru.id) },
                    onSil = { silinecekIdler = setOf(soru.id) },
                    secimModu = secimModu,
                    secili = secili,
                    onSecimDegistir = { secildi ->
                        seciliIdler = if (secildi) seciliIdler + soru.id else seciliIdler - soru.id
                    },
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    PremiumTextButton(
                        onClick = { tasinan = liste.firstOrNull() },
                        enabled = liste.isNotEmpty(),
                    ) {
                        Text(stringResource(R.string.teacher_move_title))
                    }
                    if (cozulen.isNotEmpty()) {
                        PremiumTextButton(
                            onClick = {
                                scope.launch {
                                    val sayi = ogretmenDeposu.cozulmusleriArsivle(grup.id)
                                    if (sayi > 0) {
                                        snackbarHostState.showSnackbar(
                                            context.getString(R.string.teacher_archive_done, sayi),
                                        )
                                    }
                                }
                            },
                        ) {
                            Text(stringResource(R.string.teacher_archive_title))
                        }
                    }
                }
            }
        }

        if (secimModu) {
            SecimCubugu(
                adet = seciliIdler.size,
                modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 14.dp, vertical = 90.dp),
                onCoz = { durumUygula(seciliIdler, OgretmenDurum.COZULDU) },
                onCozme = { durumUygula(seciliIdler, OgretmenDurum.BEKLIYOR) },
                onArsivle = { durumUygula(seciliIdler, OgretmenDurum.ARSIV) },
                onSil = { silinecekIdler = seciliIdler },
            )
        }

        if (!secimModu) {
            PremiumGradyanButon(
                onClick = onSoruEkle,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .heightIn(min = 54.dp)
                    .testTag("ogretmen_soru_ekle_fab"),
            ) {
                Text("+ " + stringResource(R.string.teacher_add_question), fontWeight = FontWeight.Bold)
            }
        }
    }

    sunumId?.let { id ->
        val aktifListe = when (sekme) {
            GrupSekmesi.COZMEDIKLERIM -> bekleyen
            GrupSekmesi.COZDUKLERIM -> cozulen
            GrupSekmesi.ARSIV -> arsiv
        }
        OgretmenSunumDialog(
            sorular = aktifListe,
            baslangicIndex = aktifListe.indexOfFirst { it.id == id }.coerceAtLeast(0),
            grupAdlari = mapOf(grup.id to grup.ad),
            gorselDeposu = gorselDeposu,
            onKapat = { sunumId = null },
            onCozulduDegistir = { soru, cozuldu ->
                scope.launch { ogretmenDeposu.cozulduDurumuDegistir(soru.id, cozuldu) }
            },
            onBaskaGrubaEkle = { soru ->
                sunumId = null
                kopyalanan = soru
            },
        )
    }

    kopyalanan?.let { soru ->
        BaskaGrubaEkleDialog(
            digerGruplar = veri.gruplar.filter { it.id != soru.grupId && !it.arsivli }.sortedBy { it.sira },
            kaynak = soru,
            onDismiss = { kopyalanan = null },
            onOnayla = { hedefler ->
                scope.launch {
                    val sayi = ogretmenDeposu.baskaGruplaraKopyala(soru.id, hedefler)
                    kopyalanan = null
                    if (sayi > 0) {
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.teacher_copy_done, sayi),
                        )
                    }
                }
            },
        )
    }

    tasinan?.let { soru ->
        SoruTasiDialog(
            gruplar = veri.gruplar.filter { it.id != soru.grupId && !it.arsivli }.sortedBy { it.sira },
            onDismiss = { tasinan = null },
            onTasi = { hedefId ->
                scope.launch {
                    val oldu = ogretmenDeposu.soruTasi(soru.id, hedefId)
                    tasinan = null
                    if (oldu) {
                        snackbarHostState.showSnackbar(context.getString(R.string.teacher_move_done))
                    }
                }
            },
        )
    }

    silinecekIdler?.let { hedefler ->
        AlertDialog(
            onDismissRequest = { silinecekIdler = null },
            shape = RoundedCornerShape(24.dp),
            title = { Text(stringResource(R.string.teacher_bulk_delete), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.teacher_bulk_delete_confirm, hedefler.size)) },
            confirmButton = {
                TextButton(onClick = {
                    silinecekIdler = null
                    sil(hedefler)
                }) {
                    Text(stringResource(R.string.question_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { silinecekIdler = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }

    if (grupFormu) {
        GrupFormDialog(
            mevcut = grup,
            dersAdlari = dersAdlari,
            onDismiss = { grupFormu = false },
            onKaydet = { ad, aciklama, dersId ->
                scope.launch {
                    if (ogretmenDeposu.grupGuncelle(grup.id, ad, aciklama, dersId)) grupFormu = false
                }
            },
        )
    }

    if (grupSilOnayi) {
        val toplam = sorulari.size
        val yedekMetni = sonYedek?.let { yedek ->
            SimpleDateFormat("d MMM yyyy HH:mm", Locale.forLanguageTag("tr-TR"))
                .format(Date(yedek.olusturmaZamani))
        } ?: stringResource(R.string.teacher_delete_backup_never)
        AlertDialog(
            onDismissRequest = { grupSilOnayi = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text(stringResource(R.string.teacher_group_delete), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.teacher_delete_confirm_counts, grup.ad, toplam, yedekMetni)) },
            confirmButton = {
                TextButton(onClick = {
                    grupSilOnayi = false
                    scope.launch {
                        // Group delete is permanent (files included), so the backup age is in the text.
                        if (ogretmenDeposu.grupSil(grup.id)) onGeri()
                    }
                }) {
                    Text(stringResource(R.string.question_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { grupSilOnayi = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }

    if (menuAcik) {
        AlertDialog(
            onDismissRequest = { menuAcik = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text(grup.ad, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PremiumTextButton(
                        onClick = {
                            menuAcik = false
                            grupFormu = true
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp),
                    ) {
                        Text(stringResource(R.string.teacher_group_edit))
                    }
                    PremiumTextButton(
                        onClick = {
                            menuAcik = false
                            scope.launch {
                                if (ogretmenDeposu.grupArsivle(grup.id, true)) onGeri()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp),
                    ) {
                        Text(stringResource(R.string.teacher_group_archive))
                    }
                    PremiumTextButton(
                        onClick = {
                            menuAcik = false
                            grupSilOnayi = true
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp),
                    ) {
                        Text(
                            stringResource(R.string.teacher_group_delete),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { menuAcik = false }) {
                    Text(stringResource(R.string.common_close))
                }
            },
        )
    }
}

@Composable
private fun SekmeSecici(
    metin: String,
    secili: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val vurgu = MaterialTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 42.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (secili) vurgu.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (secili) vurgu.copy(alpha = 0.46f) else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = metin,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (secili) FontWeight.Bold else FontWeight.Medium,
                color = if (secili) vurgu else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun SecimCubugu(
    adet: Int,
    modifier: Modifier = Modifier,
    onCoz: () -> Unit,
    onCozme: () -> Unit,
    onArsivle: () -> Unit,
    onSil: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth().testTag("ogretmen_secim_cubugu"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.teacher_bulk_selected, adet),
                modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                PremiumTextButton(onClick = onCoz, enabled = adet > 0) {
                    Text(stringResource(R.string.teacher_bulk_solve), style = MaterialTheme.typography.labelMedium)
                }
                PremiumTextButton(onClick = onCozme, enabled = adet > 0) {
                    Text(stringResource(R.string.teacher_bulk_unsolve), style = MaterialTheme.typography.labelMedium)
                }
                PremiumTextButton(onClick = onArsivle, enabled = adet > 0) {
                    Text(stringResource(R.string.teacher_bulk_archive), style = MaterialTheme.typography.labelMedium)
                }
                PremiumTextButton(onClick = onSil, enabled = adet > 0) {
                    Text(
                        stringResource(R.string.teacher_bulk_delete),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

/**
 * Copy keeps history: the source question is untouched and each target starts as unsolved.
 * The warning exists because a silent duplicate is the most common teacher complaint.
 */
@Composable
internal fun BaskaGrubaEkleDialog(
    digerGruplar: List<OgretmenGrubu>,
    kaynak: OgretmenSorusu,
    onDismiss: () -> Unit,
    onOnayla: (Set<String>) -> Unit,
) {
    var secilenler by remember { mutableStateOf<Set<String>>(emptySet()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text(stringResource(R.string.teacher_copy_to_group), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NotKutusu(
                    metin = stringResource(R.string.teacher_copy_warning),
                    vurgu = PremiumMarkaRenkler.Bakir,
                )
                if (digerGruplar.isEmpty()) {
                    Text(
                        text = stringResource(R.string.teacher_copy_no_target),
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    digerGruplar.forEach { hedef ->
                        val secili = hedef.id in secilenler
                        Surface(
                            onClick = {
                                secilenler = if (secili) secilenler - hedef.id else secilenler + hedef.id
                            },
                            modifier = Modifier.fillMaxWidth().testTag("hedef_grup_${hedef.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (secili) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        ) {
                            Text(
                                text = hedef.ad,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (secili) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onOnayla(secilenler) },
                enabled = secilenler.isNotEmpty(),
                modifier = Modifier.testTag("hedef_gruplara_kopyala_onay"),
            ) {
                Text(stringResource(R.string.teacher_copy_action), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
    )
}

@Composable
internal fun SoruTasiDialog(
    gruplar: List<OgretmenGrubu>,
    onDismiss: () -> Unit,
    onTasi: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text(stringResource(R.string.teacher_move_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.teacher_move_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (gruplar.isEmpty()) {
                    Text(
                        text = stringResource(R.string.teacher_copy_no_target),
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    gruplar.forEach { hedef ->
                        Surface(
                            onClick = { onTasi(hedef.id) },
                            modifier = Modifier.fillMaxWidth().testTag("tasi_grup_${hedef.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        ) {
                            Text(
                                text = hedef.ad,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
    )
}

/** Keeps the FAB out of the selection bar's way. */
@Composable
internal fun SoruEkleButonu(onClick: () -> Unit, modifier: Modifier = Modifier) {
    PremiumButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Text(stringResource(R.string.teacher_add_question), fontWeight = FontWeight.Bold)
    }
}

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.keremtulu.questionnote.domain.model.KuyrukGunFiltresi
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
    // rememberSaveable: a rotation must not throw away the search the teacher just typed.
    var seciliGrupId by rememberSaveable { mutableStateOf<String?>(null) }
    var gunFiltresiAdi by rememberSaveable { mutableStateOf(KuyrukGunFiltresi.HEPSI.name) }
    var seciliDersId by rememberSaveable { mutableStateOf<String?>(null) }
    var seciliSoran by rememberSaveable { mutableStateOf<String?>(null) }
    var arama by rememberSaveable { mutableStateOf("") }
    var sunumIndex by remember { mutableStateOf<Int?>(null) }
    var kopyalanacak by remember { mutableStateOf<OgretmenSorusu?>(null) }
    var secimModu by remember { mutableStateOf(false) }
    var seciliIdler by remember { mutableStateOf<Set<String>>(emptySet()) }
    var silmeOnayi by remember { mutableStateOf<List<OgretmenSorusu>?>(null) }
    var arsivOnayi by remember { mutableStateOf<Int?>(null) }
    val gunFiltresi = KuyrukGunFiltresi.anahtarla(gunFiltresiAdi)

    val kuyrukTumu = remember(veri, seciliGrupId, seciliDersId, gunFiltresi, bugun) {
        veri.kuyruk(
            grupId = seciliGrupId,
            dersId = seciliDersId,
            gunFiltresi = gunFiltresi,
            bugun = bugun,
        )
    }
    val kuyruk = remember(kuyrukTumu, arama, seciliSoran, dersAdlari, konuAdlari) {
        veri.filtrele(
            liste = kuyrukTumu,
            arama = arama,
            kimSordu = seciliSoran,
            dersAdlari = dersAdlari,
            konuAdlari = konuAdlari,
        )
    }
    // Hidden questions must not remain targets after a search, tab or day-filter change.
    val gorunurIdler = remember(kuyruk) { kuyruk.mapTo(mutableSetOf()) { it.id } }
    val etkinSecim = seciliIdler.intersect(gorunurIdler)
    LaunchedEffect(gorunurIdler) { seciliIdler = seciliIdler.intersect(gorunurIdler) }

    val grupAdlari = remember(veri.gruplar) { veri.gruplar.associate { it.id to it.ad } }
    val kullanilanDersler = remember(veri.sorular, dersAdlari) {
        veri.sorular.mapNotNull { it.dersId }.distinct()
            .mapNotNull { id -> dersAdlari[id]?.let { ad -> id to ad } }
            .sortedBy { it.second }
    }
    val soranlar = remember(kuyrukTumu) { veri.kimSorduIsimleri(kuyrukTumu) }
    val filtreAktif = arama.isNotBlank() || gunFiltresi != KuyrukGunFiltresi.HEPSI ||
        seciliDersId != null || seciliSoran != null
    // The "all" chip is only highlighted when nothing at all narrows the list, class included.
    val tumFiltrelerKapali = !filtreAktif && seciliGrupId == null
    val enEski = kuyruk.firstOrNull()

    fun paylas(hedefler: List<OgretmenSorusu>) {
        if (hedefler.isEmpty()) return
        val acildi = OgretmenPaylasim.paylas(context, hedefler, gorselDeposu, dersAdlari, konuAdlari)
        if (!acildi) {
            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.teacher_share_failed)) }
        }
    }

    fun filtreleriTemizle() {
        arama = ""
        gunFiltresiAdi = KuyrukGunFiltresi.HEPSI.name
        seciliDersId = null
        seciliSoran = null
        seciliGrupId = null
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("teacher-queue-list"),
            contentPadding = PaddingValues(start = 18.dp, top = 20.dp, end = 18.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                OgretmenHeroKarti(
                    kicker = stringResource(R.string.teacher_queue_kicker),
                    baslik = stringResource(R.string.teacher_queue_title),
                    altBaslik = stringResource(R.string.teacher_queue_subtitle),
                    // Counts of the whole workspace, not of the filtered list: the hero must not
                    // look like the teacher lost questions while a filter is active.
                    bekleyen = veri.sorular.count { it.kuyrukta },
                    cozulen = veri.sorular.count { it.cozuldu },
                    grupSayisi = veri.gruplar.count { !it.arsivli },
                )
            }
            item {
                OgretmenAramaAlani(
                    deger = arama,
                    onDeger = { arama = it },
                    sonucSayisi = if (filtreAktif) kuyruk.size else null,
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item {
                            FiltreCipi(
                                metin = stringResource(R.string.teacher_plan_all),
                                secili = tumFiltrelerKapali,
                                onClick = { filtreleriTemizle() },
                            )
                        }
                        item {
                            FiltreCipi(
                                metin = stringResource(R.string.teacher_plan_today),
                                secili = gunFiltresi == KuyrukGunFiltresi.BUGUN,
                                onClick = {
                                    gunFiltresiAdi = if (gunFiltresi == KuyrukGunFiltresi.BUGUN) {
                                        KuyrukGunFiltresi.HEPSI.name
                                    } else {
                                        KuyrukGunFiltresi.BUGUN.name
                                    }
                                },
                            )
                        }
                        item {
                            FiltreCipi(
                                metin = stringResource(R.string.teacher_plan_overdue),
                                secili = gunFiltresi == KuyrukGunFiltresi.GECIKEN,
                                onClick = {
                                    gunFiltresiAdi = if (gunFiltresi == KuyrukGunFiltresi.GECIKEN) {
                                        KuyrukGunFiltresi.HEPSI.name
                                    } else {
                                        KuyrukGunFiltresi.GECIKEN.name
                                    }
                                },
                            )
                        }
                        item {
                            FiltreCipi(
                                metin = stringResource(R.string.teacher_plan_unscheduled),
                                secili = gunFiltresi == KuyrukGunFiltresi.PLANLANMAMIS,
                                onClick = {
                                    gunFiltresiAdi = if (gunFiltresi == KuyrukGunFiltresi.PLANLANMAMIS) {
                                        KuyrukGunFiltresi.HEPSI.name
                                    } else {
                                        KuyrukGunFiltresi.PLANLANMAMIS.name
                                    }
                                },
                            )
                        }
                    }
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
            if (kullanilanDersler.size > 1) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(kullanilanDersler, key = { it.first }) { (dersId, ad) ->
                            FiltreCipi(
                                metin = ad,
                                secili = seciliDersId == dersId,
                                onClick = {
                                    seciliDersId = if (seciliDersId == dersId) null else dersId
                                },
                            )
                        }
                    }
                }
            }
            if (soranlar.size > 1) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(soranlar, key = { it }) { ad ->
                            FiltreCipi(
                                metin = stringResource(R.string.teacher_asked_by_filter) + ": " + ad,
                                secili = seciliSoran == ad,
                                onClick = { seciliSoran = if (seciliSoran == ad) null else ad },
                            )
                        }
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
                                text = stringResource(
                                    if (filtreAktif) R.string.teacher_search_empty
                                    else R.string.teacher_queue_empty_title,
                                ),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            if (!filtreAktif) {
                                Text(
                                    text = stringResource(R.string.teacher_queue_empty_subtitle),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            items(kuyruk, key = { it.id }) { soru ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OgretmenSoruKarti(
                        soru = soru,
                        grupAdi = grupAdlari[soru.grupId],
                        dersAdi = soru.dersId?.let { dersAdlari[it] },
                        konuAdi = soru.konuId?.let { konuAdlari[it] },
                        koyuTema = androidx.compose.foundation.isSystemInDarkTheme(),
                        gorselDeposu = gorselDeposu,
                        onTamEkran = { sunumIndex = kuyruk.indexOfFirst { it.id == soru.id } },
                        onCozulduDegistir = { cozuldu ->
                            scope.launch { ogretmenDeposu.cozulduDurumuDegistir(soru.id, cozuldu) }
                        },
                        onBaskaGrubaEkle = { kopyalanacak = soru },
                        onDuzenle = { onGrupAc(soru.grupId) },
                        onSil = { silmeOnayi = listOf(soru) },
                        onPaylas = { paylas(listOf(soru)) },
                        secimModu = secimModu,
                        secili = soru.id in seciliIdler,
                        onSecimDegistir = { sec ->
                            seciliIdler = if (sec) seciliIdler + soru.id else seciliIdler - soru.id
                        },
                    )
                    Text(
                        text = soru.planGun?.let { gun ->
                            stringResource(R.string.teacher_queue_plan_date,
                                java.time.LocalDate.ofEpochDay(gun).format(
                                    java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale.forLanguageTag("tr-TR")),
                                ),
                            )
                        } ?: stringResource(R.string.teacher_queue_no_date),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!secimModu) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                PremiumTextButton(onClick = {
                                    scope.launch {
                                        val ok = ogretmenDeposu.planDegistir(soru.id, bugun)
                                        snackbarHostState.showSnackbar(context.getString(
                                            if (ok) R.string.teacher_plan_updated else R.string.teacher_plan_failed,
                                        ))
                                    }
                                }, modifier = Modifier.testTag("teacher-plan-today-${soru.id}")) {
                                    Text(stringResource(R.string.teacher_plan_move_today))
                                }
                            }
                            item {
                                PremiumTextButton(onClick = {
                                    scope.launch {
                                        val ok = ogretmenDeposu.planDegistir(soru.id, bugun + 1)
                                        snackbarHostState.showSnackbar(context.getString(
                                            if (ok) R.string.teacher_plan_updated else R.string.teacher_plan_failed,
                                        ))
                                    }
                                }, modifier = Modifier.testTag("teacher-plan-tomorrow-${soru.id}")) {
                                    Text(stringResource(R.string.teacher_plan_move_tomorrow))
                                }
                            }
                        }
                    }
                }

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
                adet = etkinSecim.size,
                modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 14.dp, vertical = 18.dp),
                onCoz = {
                    scope.launch {
                        ogretmenDeposu.topluDurumDegistir(etkinSecim, OgretmenDurum.COZULDU)
                        seciliIdler = emptySet()
                        secimModu = false
                    }
                },
                onCozme = {
                    scope.launch {
                        ogretmenDeposu.topluDurumDegistir(etkinSecim, OgretmenDurum.BEKLIYOR)
                        seciliIdler = emptySet()
                        secimModu = false
                    }
                },
                onArsivle = {
                    // Arşivleme veriyi silmez ama listeden düşürür: önce ne olacağını söyle.
                    if (etkinSecim.isEmpty()) Unit else arsivOnayi = etkinSecim.size
                },
                onSil = {
                    silmeOnayi = kuyruk.filter { it.id in etkinSecim }.takeIf { it.isNotEmpty() }
                },
                onPaylas = {
                    val hedefler = kuyruk.filter { it.id in etkinSecim }
                    seciliIdler = emptySet()
                    secimModu = false
                    paylas(hedefler)
                },
            )
        }
    }

    sunumIndex?.let { index ->
        OgretmenSunumDialog(
            sorular = kuyruk,
            baslangicIndex = index,
            grupAdlari = grupAdlari,
            dersAdlari = dersAdlari,
            konuAdlari = konuAdlari,
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

    silmeOnayi?.let { hedefler ->
        OgretmenDiyalogu(onDismiss = { silmeOnayi = null }, testEtiketi = "teacher_queue_delete_dialog") {
            Text(stringResource(R.string.teacher_bulk_delete), style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.teacher_bulk_delete_confirm, hedefler.size))
            DiyalogDugmeleri {
                TextButton(onClick = { silmeOnayi = null }) { Text(stringResource(R.string.common_cancel)) }
                TextButton(onClick = {
                    silmeOnayi = null
                    scope.launch {
                        val adet = ogretmenDeposu.topluSil(hedefler.mapTo(mutableSetOf()) { it.id }, dosyalariKoru = true)
                        if (adet <= 0) return@launch
                        seciliIdler = emptySet()
                        secimModu = false
                        val sonuc = snackbarHostState.showSnackbar(
                            context.getString(R.string.teacher_deleted_message, adet),
                            actionLabel = context.getString(R.string.teacher_undo),
                            duration = SnackbarDuration.Long,
                        )
                        if (sonuc == SnackbarResult.ActionPerformed) {
                            val geri = ogretmenDeposu.silmeyiGeriAl(hedefler)
                            snackbarHostState.showSnackbar(context.getString(
                                if (geri > 0) R.string.teacher_undo_restored else R.string.teacher_undo_failed,
                            ))
                        }
                    }
                }, modifier = Modifier.testTag("teacher_queue_delete_confirm")) {
                    Text(stringResource(R.string.question_delete), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    arsivOnayi?.let { adet ->
        ArsivOnayDialogu(
            adet = adet,
            onDismiss = { arsivOnayi = null },
            onOnayla = {
                val hedef = etkinSecim
                arsivOnayi = null
                scope.launch {
                    ogretmenDeposu.topluDurumDegistir(hedef, OgretmenDurum.ARSIV)
                    seciliIdler = emptySet()
                    secimModu = false
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.teacher_archive_done, hedef.size),
                    )
                }
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
        modifier = Modifier.heightIn(min = 48.dp),
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
    OgretmenDiyalogu(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.teacher_archive_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(stringResource(R.string.teacher_archive_message, adet))
        DiyalogDugmeleri {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
            TextButton(onClick = onOnayla, modifier = Modifier.testTag("teacher_archive_confirm")) {
                Text(stringResource(R.string.teacher_archive_action), fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Colour tokens used by queue chips; keeps the queue visually distinct from the class list. */
internal object KuyrukRenkleri {
    val vurgu = PremiumMarkaRenkler.Deniz
    val uyari = PremiumMarkaRenkler.Mercan
}

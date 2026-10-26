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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.keremtulu.questionnote.domain.model.OgretmenGrubu
import com.keremtulu.questionnote.domain.model.OgretmenVerisi
import com.keremtulu.questionnote.ui.bilesenler.premium.CamKarti
import com.keremtulu.questionnote.ui.bilesenler.premium.CamYukseklik
import com.keremtulu.questionnote.ui.premium.PremiumButton
import com.keremtulu.questionnote.ui.premium.PremiumCard
import com.keremtulu.questionnote.ui.premium.PremiumGradyanButon
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.launch

private val GRUP_ONERILERI = listOf("8A", "7B", "6C", "Özel Ders")

/**
 * Landing tab. The existing user already knows the class list, so it stays first; the queue is
 * promoted with a pending-count card instead of being hidden behind a menu.
 */
@Composable
internal fun OgretmenGruplarEkrani(
    veri: OgretmenVerisi,
    ogretmenDeposu: OgretmenDeposu,
    gorselDeposu: LocalSoruGorselDeposu,
    dersAdlari: Map<String, String>,
    bugun: Long,
    onGrupAc: (String) -> Unit,
    onKuyrukAc: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var grupFormu by remember { mutableStateOf(false) }
    var duzenlenen by remember { mutableStateOf<OgretmenGrubu?>(null) }
    var arsivAcik by remember { mutableStateOf(false) }

    val aktifGruplar = remember(veri.gruplar) {
        veri.gruplar.filterNot { it.arsivli }.sortedBy { it.sira }
    }
    val arsivliGruplar = remember(veri.gruplar) {
        veri.gruplar.filter { it.arsivli }.sortedBy { it.sira }
    }
    val bekleyen = remember(veri.sorular) { veri.sorular.count { it.kuyrukta } }
    val cozulen = remember(veri.sorular) { veri.sorular.count { it.cozuldu } }
    val enEskiBekleyen = remember(veri.sorular) {
        veri.sorular.filter { it.kuyrukta }.minByOrNull { it.olusturma }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, top = 10.dp, end = 18.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OgretmenHeroKarti(
                    kicker = stringResource(R.string.teacher_workspace_kicker),
                    baslik = stringResource(R.string.teacher_workspace_title),
                    altBaslik = stringResource(R.string.teacher_workspace_subtitle),
                    bekleyen = bekleyen,
                    cozulen = cozulen,
                    grupSayisi = aktifGruplar.size,
                )
            }
            if (bekleyen > 0) {
                item {
                    KuyrukOnizlemeKarti(
                        bekleyen = bekleyen,
                        enEski = enEskiBekleyen,
                        onAc = onKuyrukAc,
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BolumBasligi(stringResource(R.string.teacher_nav_groups))
                    if (aktifGruplar.isNotEmpty()) {
                        PremiumTextButton(onClick = { grupFormu = true }) {
                            Text(stringResource(R.string.teacher_group_add), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            if (aktifGruplar.isEmpty()) {
                item {
                    CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.teacher_groups_empty_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = stringResource(R.string.teacher_groups_empty_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            PremiumGradyanButon(
                                onClick = { grupFormu = true },
                                modifier = Modifier.fillMaxWidth().testTag("ogretmen_bos_grup_ekle"),
                            ) {
                                Text(stringResource(R.string.teacher_group_add), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            items(aktifGruplar, key = { it.id }) { grup ->
                val sorulari = remember(veri.sorular, grup.id) { veri.grupSorulari(grup.id) }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OgretmenGrupKarti(
                        grup = grup,
                        cozulmemis = sorulari.count { it.kuyrukta },
                        cozulmus = sorulari.count { it.cozuldu },
                        koyuTema = false,
                        onClick = { onGrupAc(grup.id) },
                    )
                    grup.dersId?.let { dersId ->
                        val ad = dersAdlari[dersId]
                        Text(
                            text = stringResource(
                                R.string.teacher_group_lesson_chip,
                                ad ?: stringResource(R.string.teacher_label_unknown_lesson),
                            ),
                            modifier = Modifier.padding(start = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (arsivliGruplar.isNotEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                        PremiumTextButton(onClick = { arsivAcik = !arsivAcik }) {
                            Text(
                                text = stringResource(R.string.teacher_archived_badge) +
                                    " · " + arsivliGruplar.size,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                if (arsivAcik) {
                    items(arsivliGruplar, key = { "arsiv-" + it.id }) { grup ->
                        CamKarti(modifier = Modifier.fillMaxWidth(), elevation = CamYukseklik.DUSUK) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = grup.ad,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = stringResource(R.string.teacher_archived_badge),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    PremiumTextButton(onClick = { duzenlenen = grup }) {
                                        Text(stringResource(R.string.question_edit))
                                    }
                                    PremiumTextButton(
                                        onClick = {
                                            scope.launch { ogretmenDeposu.grupArsivle(grup.id, false) }
                                        },
                                    ) {
                                        Text(stringResource(R.string.teacher_group_unarchive))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        PremiumButton(
            onClick = { grupFormu = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .heightIn(min = 56.dp)
                .testTag("ogretmen_grup_ekle_fab"),
            shape = RoundedCornerShape(20.dp),
        ) {
            Text(
                text = "+ " + stringResource(R.string.teacher_group_add),
                fontWeight = FontWeight.Bold,
            )
        }
    }

    if (grupFormu || duzenlenen != null) {
        GrupFormDialog(
            mevcut = duzenlenen,
            dersAdlari = dersAdlari,
            onDismiss = {
                grupFormu = false
                duzenlenen = null
            },
            onKaydet = { ad, aciklama, dersId ->
                val hedef = duzenlenen
                scope.launch {
                    if (hedef == null) {
                        val yeni = ogretmenDeposu.grupEkle(ad, aciklama, dersId)
                        if (yeni != null) grupFormu = false
                    } else {
                        val oldu = ogretmenDeposu.grupGuncelle(hedef.id, ad, aciklama, dersId)
                        if (oldu) duzenlenen = null
                    }
                }
            },
        )
    }
}

@Composable
private fun KuyrukOnizlemeKarti(
    bekleyen: Int,
    enEski: com.keremtulu.questionnote.domain.model.OgretmenSorusu?,
    onAc: () -> Unit,
) {
    val beklemeMetni = enEski?.let { eski ->
        val gun = ChronoUnit.DAYS.between(
            Instant.ofEpochMilli(eski.olusturma).atZone(ZoneId.systemDefault()).toLocalDate(),
            java.time.LocalDate.now(),
        ).coerceAtLeast(0)
        when {
            gun <= 0L -> stringResource(R.string.teacher_age_today)
            gun == 1L -> stringResource(R.string.teacher_age_one_day)
            gun < 7L -> stringResource(R.string.teacher_age_days, gun.toInt())
            else -> stringResource(R.string.teacher_age_week_plus)
        }
    }
    PremiumCard(
        onClick = onAc,
        modifier = Modifier.fillMaxWidth().testTag("ogretmen_kuyruk_onizleme"),
        shape = RoundedCornerShape(24.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = PremiumMarkaRenkler.Bakir.copy(alpha = 0.10f),
        ),
        border = BorderStroke(1.dp, PremiumMarkaRenkler.Bakir.copy(alpha = 0.34f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = stringResource(R.string.teacher_queue_pending_card, bekleyen),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (beklemeMetni != null) {
                    Text(
                        text = stringResource(R.string.teacher_queue_pending_hint, beklemeMetni),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            PremiumTextButton(onClick = onAc) {
                Text(stringResource(R.string.teacher_queue_open), fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Shared create/edit sheet for a class group. Keeps the teacher's free-form naming. */
@Composable
internal fun GrupFormDialog(
    mevcut: OgretmenGrubu?,
    dersAdlari: Map<String, String>,
    onDismiss: () -> Unit,
    onKaydet: (ad: String, aciklama: String?, dersId: String?) -> Unit,
) {
    var ad by remember(mevcut?.id) { mutableStateOf(mevcut?.ad.orEmpty()) }
    var aciklama by remember(mevcut?.id) { mutableStateOf(mevcut?.aciklama.orEmpty()) }
    var dersId by remember(mevcut?.id) { mutableStateOf(mevcut?.dersId) }
    var dersMenusu by remember { mutableStateOf(false) }
    val gecerli = ad.trim().isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = stringResource(
                    if (mevcut == null) R.string.teacher_group_add else R.string.teacher_group_edit,
                ),
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = ad,
                    onValueChange = { ad = it },
                    modifier = Modifier.fillMaxWidth().testTag("ogretmen_grup_ad_girdi"),
                    singleLine = true,
                    label = { Text(stringResource(R.string.teacher_group_name_label)) },
                    shape = RoundedCornerShape(14.dp),
                )
                OutlinedTextField(
                    value = aciklama,
                    onValueChange = { aciklama = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.teacher_group_note_label)) },
                    shape = RoundedCornerShape(14.dp),
                )
                if (mevcut == null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GRUP_ONERILERI.forEach { oneri ->
                            PremiumTextButton(
                                onClick = { ad = oneri },
                                modifier = Modifier.heightIn(min = 40.dp),
                            ) {
                                Text(oneri, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
                if (dersAdlari.isNotEmpty()) {
                    val dersEtiketi = dersId?.let { dersAdlari[it] }
                        ?: stringResource(R.string.teacher_form_lesson_none)
                    androidx.compose.material3.OutlinedButton(
                        onClick = { dersMenusu = true },
                        modifier = Modifier.fillMaxWidth().testTag("ogretmen_grup_ders_sec"),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.teacher_form_lesson_label) + ": " + dersEtiketi,
                            maxLines = 1,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onKaydet(ad.trim(), aciklama.trim().takeIf { it.isNotEmpty() }, dersId) },
                enabled = gecerli,
                modifier = Modifier.testTag("ogretmen_grup_kaydet_buton"),
            ) {
                Text(stringResource(R.string.topic_save), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.question_form_cancel))
            }
        },
    )

    if (dersMenusu) {
        SecimDialogu(
            baslik = stringResource(R.string.teacher_form_lesson_label),
            secenekler = dersAdlari.entries.map { it.key to it.value },
            seciliId = dersId,
            bosEtiket = stringResource(R.string.teacher_form_lesson_none),
            onSec = {
                dersId = it
                dersMenusu = false
            },
            onDismiss = { dersMenusu = false },
            testEtiketi = "ogretmen_ders_secim",
        )
    }
}

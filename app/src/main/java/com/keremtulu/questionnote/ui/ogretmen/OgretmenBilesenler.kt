package com.keremtulu.questionnote.ui.ogretmen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.domain.model.OgretmenGrubu
import com.keremtulu.questionnote.domain.model.OgretmenSorusu
import com.keremtulu.questionnote.domain.model.OgretmenZorluk
import com.keremtulu.questionnote.ui.bilesenler.SoruGorselResmi
import com.keremtulu.questionnote.ui.premium.EtkilesimTuru
import com.keremtulu.questionnote.ui.premium.PremiumButton
import com.keremtulu.questionnote.ui.premium.PremiumCard
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumSembolAilesi
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Top-level teacher destinations. The queue is the "what should I solve today" answer. */
internal sealed interface OgretmenEkranDurumu {
    data object Kuyruk : OgretmenEkranDurumu
    data object Gruplar : OgretmenEkranDurumu
    data object Ayarlar : OgretmenEkranDurumu
    data class GrupDetay(val grupId: String) : OgretmenEkranDurumu
    data class SoruFormu(val grupId: String, val soruId: String?) : OgretmenEkranDurumu
}

internal enum class OgretmenAltSekme { KUYRUK, GRUPLAR, AYARLAR }

internal fun bugunEpochGun(): Long = LocalDate.now().toEpochDay()

internal fun epochGununTarihi(gun: Long): LocalDate = LocalDate.ofEpochDay(gun)

internal fun zamanMetni(zaman: Long): String {
    val tarih = Instant.ofEpochMilli(zaman).atZone(ZoneId.systemDefault()).toLocalDate()
    return tarih.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("tr-TR")))
}

/** "bugün", "3 gün", "1 hafta+" — the age label that makes the oldest waiting question visible. */
@Composable
internal fun beklemeEtiketi(soru: OgretmenSorusu): String {
    val gun = ChronoUnit.DAYS.between(
        Instant.ofEpochMilli(soru.olusturma).atZone(ZoneId.systemDefault()).toLocalDate(),
        LocalDate.now(),
    ).coerceAtLeast(0)
    return when {
        gun <= 0L -> stringResource(R.string.teacher_age_today)
        gun == 1L -> stringResource(R.string.teacher_age_one_day)
        gun < 7L -> stringResource(R.string.teacher_age_days, gun.toInt())
        else -> stringResource(R.string.teacher_age_week_plus)
    }
}

internal fun gecikenMi(soru: OgretmenSorusu, bugun: Long): Boolean =
    soru.planGun?.let { it < bugun } ?: false

internal fun grupMonogram(ad: String): String {
    val temiz = ad.trim()
    if (temiz.isEmpty()) return "GR"
    val parcalar = temiz.split(Regex("[\\s()\\-/]+")).filter { it.isNotEmpty() }
    if (parcalar.size == 1 && temiz.length <= 3) {
        return temiz.uppercase(Locale.forLanguageTag("tr-TR"))
    }
    if (parcalar.size >= 2) {
        val ilk = parcalar[0]
        if (ilk.length <= 2 && ilk.any { it.isDigit() }) {
            return ilk.uppercase(Locale.forLanguageTag("tr-TR"))
        }
        return (parcalar[0].take(1) + parcalar[1].take(1)).uppercase(Locale.forLanguageTag("tr-TR"))
    }
    return temiz.take(2).uppercase(Locale.forLanguageTag("tr-TR"))
}

internal fun grupVurguRengi(ad: String, koyuTema: Boolean): Color {
    val acikPalet = listOf(
        PremiumMarkaRenkler.Petrol,
        PremiumMarkaRenkler.Bakir,
        PremiumMarkaRenkler.Deniz,
        PremiumMarkaRenkler.Yesil,
        Color(0xFF5C6BC0),
        Color(0xFF7B5EA7),
    )
    val koyuPalet = listOf(
        PremiumMarkaRenkler.DenizAcik,
        PremiumMarkaRenkler.BakirIsik,
        PremiumMarkaRenkler.PetrolParlak,
        PremiumMarkaRenkler.YesilAcik,
        Color(0xFF9FA8DA),
        PremiumMarkaRenkler.MorAcik,
    )
    val palet = if (koyuTema) koyuPalet else acikPalet
    val indeks = abs(ad.trim().lowercase(Locale.forLanguageTag("tr-TR")).hashCode()) % palet.size
    return palet[indeks]
}

internal fun zorlukRengi(zorluk: OgretmenZorluk, koyuTema: Boolean): Color = when (zorluk) {
    OgretmenZorluk.KOLAY -> if (koyuTema) PremiumMarkaRenkler.YesilAcik else PremiumMarkaRenkler.Yesil
    OgretmenZorluk.ORTA -> if (koyuTema) PremiumMarkaRenkler.BakirIsik else PremiumMarkaRenkler.Bakir
    OgretmenZorluk.ZOR -> if (koyuTema) PremiumMarkaRenkler.MercanAcik else Color(0xFFC62828)
}

@androidx.annotation.StringRes
internal fun zorlukEtiketi(zorluk: OgretmenZorluk): Int = when (zorluk) {
    OgretmenZorluk.KOLAY -> R.string.teacher_difficulty_easy
    OgretmenZorluk.ORTA -> R.string.teacher_difficulty_medium
    OgretmenZorluk.ZOR -> R.string.teacher_difficulty_hard
}

@Composable
internal fun DurumRozeti(metin: String, renk: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = renk.copy(alpha = 0.13f),
    ) {
        Text(
            text = metin,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            color = renk,
            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
internal fun ZorlukRozeti(zorluk: OgretmenZorluk, koyuTema: Boolean = false) {
    val renk = zorlukRengi(zorluk, koyuTema)
    Surface(
        shape = CircleShape,
        color = renk.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, renk.copy(alpha = 0.36f)),
    ) {
        Text(
            text = stringResource(zorlukEtiketi(zorluk)),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = renk,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Small tag chip (lesson / topic / source), reused by the queue, detail and form screens. */
@Composable
internal fun EtiketCipi(metin: String, vurgu: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = vurgu.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, vurgu.copy(alpha = 0.28f)),
    ) {
        Text(
            text = metin,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            color = vurgu,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}


/**
 * Dialog shell for the teacher screens. A raw `Dialog` with an unconstrained width is used on
 * purpose: the platform-default AlertDialog measure pass never settles under Robolectric UI tests,
 * so every teacher dialog is built from this single window instead.
 */
@Composable
internal fun OgretmenDiyalogu(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    testEtiketi: String? = null,
    icerik: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .then(if (testEtiketi != null) Modifier.testTag(testEtiketi) else Modifier),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = icerik,
            )
        }
    }
}

/** Right-aligned action row used at the bottom of [OgretmenDiyalogu]. */
@Composable
internal fun DiyalogDugmeleri(icerik: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        content = icerik,
    )
}

/**
 * Version-proof single-choice dialog. `ExposedDropdownMenu` changed its anchor API between
 * Material3 releases, and this screen must compile on the project's BOM without guesswork.
 */
@Composable
internal fun SecimDialogu(
    baslik: String,
    secenekler: List<Pair<String, String>>,
    seciliId: String?,
    onSec: (String?) -> Unit,
    onDismiss: () -> Unit,
    bosEtiket: String? = null,
    testEtiketi: String? = null,
) {
    OgretmenDiyalogu(onDismiss = onDismiss, testEtiketi = testEtiketi) {
        Text(baslik, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (bosEtiket != null) {
                item {
                    SecimSatiri(
                        metin = bosEtiket,
                        secili = seciliId == null,
                        onClick = { onSec(null) },
                        testEtiketi = testEtiketi?.let { it + "_bos" },
                    )
                }
            }
            items(secenekler, key = { it.first }) { (id, etiket) ->
                SecimSatiri(
                    metin = etiket,
                    secili = id == seciliId,
                    onClick = { onSec(id) },
                    testEtiketi = testEtiketi?.let { it + "_" + id },
                )
            }
        }
        DiyalogDugmeleri {
            PremiumTextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    }
}

@Composable
private fun SecimSatiri(
    metin: String,
    secili: Boolean,
    onClick: () -> Unit,
    testEtiketi: String? = null,
) {
    val vurgu = MaterialTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (testEtiketi != null) Modifier.testTag(testEtiketi) else Modifier),
        shape = RoundedCornerShape(12.dp),
        color = if (secili) vurgu.copy(alpha = 0.14f) else Color.Transparent,
        border = if (secili) BorderStroke(1.dp, vurgu.copy(alpha = 0.45f)) else null,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (secili) vurgu else MaterialTheme.colorScheme.surfaceVariant),
            )
            Text(
                text = metin,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (secili) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun OgretmenGrupKarti(
    grup: OgretmenGrubu,
    cozulmemis: Int,
    cozulmus: Int,
    koyuTema: Boolean,
    onClick: () -> Unit,
) {
    val vurgu = grupVurguRengi(grup.ad, koyuTema)
    val cozulmusRenk = if (koyuTema) PremiumMarkaRenkler.YesilAcik else PremiumMarkaRenkler.Yesil
    val bekleyenRenk = if (koyuTema) PremiumMarkaRenkler.BakirIsik else PremiumMarkaRenkler.Bakir
    val toplam = cozulmemis + cozulmus

    PremiumCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag("ogretmen_grup_${grup.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (cozulmemis > 0) vurgu.copy(alpha = 0.42f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        interactionTur = EtkilesimTuru.KART_ACILIS,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(vurgu.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = grupMonogram(grup.ad),
                        color = vurgu,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = grup.ad,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    grup.aciklama?.takeIf { it.isNotBlank() }?.let { not ->
                        Text(
                            text = not,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (toplam == 0) {
                            Text(
                                text = stringResource(R.string.teacher_group_empty_badge),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            DurumRozeti(
                                metin = stringResource(R.string.teacher_group_pending_badge, cozulmemis),
                                renk = if (cozulmemis > 0) bekleyenRenk else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            DurumRozeti(
                                metin = stringResource(R.string.teacher_group_solved_badge, cozulmus),
                                renk = cozulmusRenk,
                            )
                        }
                    }
                }
                Surface(shape = CircleShape, color = vurgu.copy(alpha = 0.12f)) {
                    Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "→",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = PremiumSembolAilesi,
                            color = vurgu,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            if (toplam > 0) {
                val oran = cozulmus.toFloat() / toplam.toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(oran.coerceIn(0f, 1f))
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(cozulmusRenk),
                    )
                }
            }
        }
    }
}

/**
 * Question card shared by queue, group detail and archive. `secimModu` swaps the footer for a
 * checkbox so a whole class list can be updated in one action.
 */
@Composable
internal fun OgretmenSoruKarti(
    soru: OgretmenSorusu,
    grupAdi: String?,
    dersAdi: String?,
    konuAdi: String?,
    koyuTema: Boolean,
    gorselDeposu: LocalSoruGorselDeposu,
    onTamEkran: () -> Unit,
    onCozulduDegistir: (Boolean) -> Unit,
    onBaskaGrubaEkle: () -> Unit,
    onDuzenle: () -> Unit,
    onSil: () -> Unit,
    secimModu: Boolean = false,
    secili: Boolean = false,
    onSecimDegistir: (Boolean) -> Unit = {},
) {
    val siraliGorseller = remember(soru.gorseller) { soru.soruGorselleri() }
    val cozumSayisi = remember(soru.gorseller) { soru.cozumGorselleri().size }
    val ilkDosya = siraliGorseller.firstOrNull()?.let { gorselDeposu.dosyaGoruntuIcin(it.dosyaAdi) }

    PremiumCard(
        onClick = { if (secimModu) onSecimDegistir(!secili) else onTamEkran() },
        modifier = Modifier.fillMaxWidth().testTag("ogretmen_soru_kart_${soru.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (secili) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        interactionTur = EtkilesimTuru.KART_ACILIS,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = soru.baslik ?: stringResource(R.string.teacher_untitled_question),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (secimModu) {
                    Checkbox(
                        checked = secili,
                        onCheckedChange = { onSecimDegistir(it) },
                        modifier = Modifier.testTag("ogretmen_soru_sec_${soru.id}"),
                    )
                } else {
                    soru.zorluk?.let { z -> ZorlukRozeti(z, koyuTema = koyuTema) }
                }
            }
            // Compact thumbnail: 196dp cards made a 100-question class list unscannable.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)),
            ) {
                SoruGorselResmi(
                    file = ilkDosya,
                    contentDescription = stringResource(R.string.question_image_description),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    maxDecodeEdge = 720,
                    cerceve = false,
                    onClick = { if (!secimModu) onTamEkran() },
                )
                if (siraliGorseller.size > 1) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                        shape = CircleShape,
                        color = PremiumMarkaRenkler.MurekkepKoyu.copy(alpha = 0.78f),
                    ) {
                        Text(
                            text = stringResource(R.string.teacher_photo_count_badge, siraliGorseller.size),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            color = PremiumMarkaRenkler.Kagit,
                            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                beklemeEtiketi(soru).let { yas ->
                    EtiketCipi(metin = yas, vurgu = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                grupAdi?.takeIf { it.isNotBlank() }?.let { ad ->
                    EtiketCipi(metin = ad, vurgu = MaterialTheme.colorScheme.primary)
                }
                dersAdi?.takeIf { it.isNotBlank() }?.let { ad ->
                    EtiketCipi(metin = ad, vurgu = PremiumMarkaRenkler.Deniz)
                }
                if (!soru.cevap.isNullOrBlank()) {
                    EtiketCipi(
                        metin = stringResource(R.string.teacher_answer_hidden_badge),
                        vurgu = MaterialTheme.colorScheme.secondary,
                    )
                }
                if (cozumSayisi > 0) {
                    EtiketCipi(
                        metin = stringResource(R.string.teacher_solution_badge, cozumSayisi),
                        vurgu = PremiumMarkaRenkler.Yesil,
                    )
                }
            }
            konuAdi?.takeIf { it.isNotBlank() }?.let { ad ->
                Text(
                    text = ad,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!secimModu) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PremiumOutlinedButton(
                        onClick = onTamEkran,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.ask_teacher_open_fullscreen))
                    }
                    PremiumButton(
                        onClick = { onCozulduDegistir(!soru.cozuldu) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("ogretmen_cozuldu_degistir_${soru.id}"),
                        shape = RoundedCornerShape(14.dp),
                        interactionTur = if (soru.cozuldu) EtkilesimTuru.TIKLAMA else EtkilesimTuru.BASARI,
                    ) {
                        Text(
                            stringResource(
                                if (soru.cozuldu) R.string.teacher_mark_unsolved
                                else R.string.teacher_mark_solved,
                            ),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PremiumTextButton(
                        onClick = onBaskaGrubaEkle,
                        modifier = Modifier.testTag("ogretmen_baska_gruba_ekle_${soru.id}"),
                    ) {
                        Text(
                            text = stringResource(R.string.teacher_copy_to_group),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        PremiumTextButton(onClick = onDuzenle) {
                            Text(stringResource(R.string.question_edit))
                        }
                        PremiumTextButton(onClick = onSil) {
                            Text(
                                stringResource(R.string.question_delete),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Simple drawn icon set so no font glyph can silently fail on the teacher navigation. */
@Composable
internal fun OgretmenMenuSimgesi(sekme: OgretmenAltSekme, renk: Color) {
    Canvas(Modifier.size(22.dp)) {
        val kalinlik = 1.9.dp.toPx()
        val firca = Stroke(width = kalinlik, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = size.width
        val h = size.height
        when (sekme) {
            OgretmenAltSekme.KUYRUK -> {
                drawLine(renk, Offset(w * 0.22f, h * 0.26f), Offset(w * 0.78f, h * 0.26f), kalinlik, StrokeCap.Round)
                drawLine(renk, Offset(w * 0.22f, h * 0.52f), Offset(w * 0.66f, h * 0.52f), kalinlik, StrokeCap.Round)
                drawLine(
                    color = renk.copy(alpha = 0.55f),
                    start = Offset(w * 0.22f, h * 0.78f),
                    end = Offset(w * 0.52f, h * 0.78f),
                    strokeWidth = kalinlik,
                    cap = StrokeCap.Round,
                )
                drawCircle(renk, radius = kalinlik * 0.85f, center = Offset(w * 0.84f, h * 0.78f))
            }
            OgretmenAltSekme.GRUPLAR -> {
                drawRoundRect(
                    color = renk.copy(alpha = 0.45f),
                    topLeft = Offset(w * 0.24f, h * 0.14f),
                    size = Size(w * 0.62f, h * 0.50f),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                    style = firca,
                )
                drawRoundRect(
                    color = renk,
                    topLeft = Offset(w * 0.12f, h * 0.30f),
                    size = Size(w * 0.66f, h * 0.56f),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                    style = firca,
                )
            }
            OgretmenAltSekme.AYARLAR -> {
                val m = Offset(w / 2f, h / 2f)
                drawCircle(renk, radius = w * 0.25f, center = m, style = firca)
                drawCircle(renk, radius = 1.8.dp.toPx(), center = m)
                for (i in 0 until 6) {
                    val aci = Math.toRadians((i * 60.0) - 90.0)
                    val ic = Offset(
                        (m.x + cos(aci) * w * 0.28f).toFloat(),
                        (m.y + sin(aci) * h * 0.28f).toFloat(),
                    )
                    val dis = Offset(
                        (m.x + cos(aci) * w * 0.41f).toFloat(),
                        (m.y + sin(aci) * h * 0.41f).toFloat(),
                    )
                    drawLine(renk, ic, dis, strokeWidth = kalinlik, cap = StrokeCap.Round)
                }
            }
        }
    }
}


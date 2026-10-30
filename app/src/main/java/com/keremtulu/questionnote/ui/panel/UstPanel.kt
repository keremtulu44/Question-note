package com.keremtulu.questionnote.ui.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.ilerleme.IlerlemeHizmeti
import com.keremtulu.questionnote.data.prefs.GunlukHedefDeposu
import com.keremtulu.questionnote.ui.animasyon.SureToken
import com.keremtulu.questionnote.ui.animasyon.animateFloatPremium
import com.keremtulu.questionnote.ui.animasyon.tweenPremium
import com.keremtulu.questionnote.ui.bilesenler.Damga
import com.keremtulu.questionnote.ui.bilesenler.HedefHalkasi
import com.keremtulu.questionnote.ui.bilesenler.SeriAlevi
import com.keremtulu.questionnote.ui.bilesenler.hatirlaYerelGun
import kotlinx.coroutines.flow.catch
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Üst panel görünürken alt ekranlar kendi durum çubuğu boşluğunu eklemez; aksi hâlde
 * çentik yüksekliği iki kez sayılır. Varsayılan `false` doğrudan (önizleme/test) kullanımda
 * ekranların bugünkü davranışını birebir korur.
 */
val LocalUstPanelVar = staticCompositionLocalOf { false }

/**
 * Ekranın kendi Scaffold'u için pencere kenarları.
 * Panel ve alt menü görünürken üst/alt boşlukları host'taki çubuklar taşır; ekran yalnız
 * yatay güvenli alanı ekler. Böylece alt menünün üstünde ikinci bir gezinme çubuğu boşluğu kalmaz.
 */
@Composable
fun ekranPencereInsetsi(): WindowInsets =
    if (LocalUstPanelVar.current) {
        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
    } else {
        WindowInsets.safeDrawing
    }

/**
 * Alt menünün üstteki karşılığı: gün ve seri kimliği solda, günlük hedef halkası sağda.
 * Oyunlaştırılmış para/puan yok; yalnız bugünün gerçek durumu ve tek anlamlı geçiş (İlerleme).
 */
@Composable
fun UstPanel(
    ilerlemeHizmeti: IlerlemeHizmeti,
    gunlukHedefDeposu: GunlukHedefDeposu,
    onIlerleme: () -> Unit,
    systemInsets: WindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
) {
    val colors = MaterialTheme.colorScheme
    val bugun = hatirlaYerelGun()
    val buyukYazi = LocalDensity.current.fontScale >= 1.5f
    val ilerlemeEtiketi = stringResource(R.string.ust_panel_open_progress)
    val gun = bugun.toEpochDay().toInt()
    val hedef by produceState(gunlukHedefDeposu.hedef().coerceAtLeast(1), gunlukHedefDeposu) {
        gunlukHedefDeposu.izle().catch { }.collect { value = it.coerceAtLeast(1) }
    }
    val bugunAkisi = remember(ilerlemeHizmeti, gun) { ilerlemeHizmeti.bugunSayisiIzle(gun) }
    val seriAkisi = remember(ilerlemeHizmeti, gun) { ilerlemeHizmeti.seriIzle(gun) }
    // Yüklenene kadar sayı uydurmayız: null, arayüzde tire olarak görünür.
    val yapilan by produceState<Int?>(null, bugunAkisi) {
        value = null
        bugunAkisi.catch { }.collect { value = it }
    }
    val seri by produceState<Int?>(null, seriAkisi) {
        value = null
        seriAkisi.catch { }.collect { value = it }
    }
    val seriSayisi = seri
    // %200 yazıda hafta günü yerine kısa tarih: tarih tek satırda okunur kalır.
    val tarih = remember(bugun, buyukYazi) {
        val desen = if (buyukYazi) "d MMMM" else "d MMMM EEEE"
        bugun.format(DateTimeFormatter.ofPattern(desen, Locale.forLanguageTag("tr-TR")))
    }
    val oran = ((yapilan ?: 0).toFloat() / hedef).coerceIn(0f, 1f)
    val halka = animateFloatPremium(
        target = oran,
        animationSpec = tweenPremium(SureToken.SLOW),
        label = "ust-panel-hedef",
    )

    Surface(color = colors.surface, tonalElevation = 2.dp, shadowElevation = 6.dp) {
        Column(Modifier.windowInsetsPadding(systemInsets)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ust-panel")
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .widthIn(max = 840.dp)
                    .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Damga(
                    modifier = Modifier.size(30.dp),
                    zemin = colors.primaryContainer.copy(alpha = 0.7f),
                    cizgi = colors.primary,
                    vurgu = colors.secondary,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = tarih,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        SeriAlevi(
                            modifier = Modifier.size(12.dp).clearAndSetSemantics { },
                            renk = if ((seriSayisi ?: 0) > 0) colors.secondary else colors.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                        Text(
                            text = when {
                                seriSayisi == null -> stringResource(R.string.ust_panel_loading)
                                seriSayisi > 0 -> stringResource(R.string.today_streak, seriSayisi)
                                else -> stringResource(R.string.ust_panel_streak_start)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant.copy(alpha = 0.92f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                HedefDugmesi(
                    yapilan = yapilan,
                    hedef = hedef,
                    oran = halka.value,
                    onIlerleme = onIlerleme,
                    icerikAciklamasi = ilerlemeEtiketi,
                    etiketGoster = !buyukYazi,
                )
            }
            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun HedefDugmesi(
    yapilan: Int?,
    hedef: Int,
    oran: Float,
    onIlerleme: () -> Unit,
    icerikAciklamasi: String,
    etiketGoster: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    val hedefMetni = yapilan?.let { stringResource(R.string.ust_panel_progress, it, hedef) }
        ?: stringResource(R.string.ust_panel_loading)
    Row(
        modifier = Modifier
            .testTag("ust-panel-hedef")
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.primaryContainer.copy(alpha = 0.55f))
            .clickable(
                role = Role.Button,
                onClickLabel = icerikAciklamasi,
                onClick = onIlerleme,
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HedefHalkasi(
            oran = oran,
            modifier = Modifier.size(24.dp).clearAndSetSemantics { },
            izRenk = colors.primary.copy(alpha = 0.22f),
            dolguRenk = colors.primary,
            kalinlik = 2.5.dp,
            isaretSayisi = 16,
        )
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            if (etiketGoster) {
                Text(
                    text = stringResource(R.string.ust_panel_goal_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    maxLines = 1,
                )
            }
            Text(
                text = hedefMetni,
                style = MaterialTheme.typography.labelLarge,
                color = colors.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

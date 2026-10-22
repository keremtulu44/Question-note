package com.keremtulu.questionnote.ui.premium

import androidx.compose.animation.core.Spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.data.ses.HapticTipi
import com.keremtulu.questionnote.data.ses.HapticYoneticisi
import com.keremtulu.questionnote.data.ses.SesTuruGenis
import com.keremtulu.questionnote.data.ses.SesYoneticisi
import com.keremtulu.questionnote.ui.animasyon.animateFloatPremium
import com.keremtulu.questionnote.ui.animasyon.springPremium
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Sound and haptic profile for a commonly used interaction. */
enum class EtkilesimTuru {
    TIKLAMA, TIKLAMA_AGIR, GERI, SAYFA_GECIS,
    KART_ACILIS, KART_KAPANIS, TOGGLE_AC, TOGGLE_KAPA,
    SLIDER, SWIPE, PULL_REFRESH, RELEASE_REFRESH,
    BASARI, BASARI_ORTA, BASARI_BUYUK, ROZET, RUTBE, SERI,
    ODAK_BASLADI, ODAK_TAMAMLANDI, COZUM_ACILDI, DEGERLENDIRME,
    HATA, UYARI, ILK_SORU, MILESTONE, YUKLEME_TAMAM,
}

private data class GeriBildirimProfili(val ses: SesTuruGenis, val haptik: HapticTipi)

private fun EtkilesimTuru.profil(): GeriBildirimProfili = when (this) {
    EtkilesimTuru.TIKLAMA -> GeriBildirimProfili(SesTuruGenis.TIKLAMA, HapticTipi.TIKLAMA)
    EtkilesimTuru.TIKLAMA_AGIR -> GeriBildirimProfili(SesTuruGenis.TIKLAMA, HapticTipi.TIKLAMA_AGIR)
    EtkilesimTuru.GERI -> GeriBildirimProfili(SesTuruGenis.GERI, HapticTipi.TIKLAMA)
    EtkilesimTuru.SAYFA_GECIS -> GeriBildirimProfili(SesTuruGenis.SAYFA_GECIS, HapticTipi.SWIPE)
    EtkilesimTuru.KART_ACILIS -> GeriBildirimProfili(SesTuruGenis.KART_ACILIS, HapticTipi.KART_SECILDI)
    EtkilesimTuru.KART_KAPANIS -> GeriBildirimProfili(SesTuruGenis.KART_KAPANIS, HapticTipi.TIKLAMA)
    EtkilesimTuru.TOGGLE_AC -> GeriBildirimProfili(SesTuruGenis.TOGGLE_AC, HapticTipi.TOGGLE)
    EtkilesimTuru.TOGGLE_KAPA -> GeriBildirimProfili(SesTuruGenis.TOGGLE_KAPA, HapticTipi.TOGGLE)
    EtkilesimTuru.SLIDER -> GeriBildirimProfili(SesTuruGenis.SLIDER_DEGISIKLIK, HapticTipi.SLIDER)
    EtkilesimTuru.SWIPE -> GeriBildirimProfili(SesTuruGenis.SWIPE, HapticTipi.SWIPE)
    EtkilesimTuru.PULL_REFRESH -> GeriBildirimProfili(SesTuruGenis.PULL_REFRESH, HapticTipi.PULL_REFRESH)
    EtkilesimTuru.RELEASE_REFRESH -> GeriBildirimProfili(SesTuruGenis.RELEASE_REFRESH, HapticTipi.RELEASE_REFRESH)
    EtkilesimTuru.BASARI -> GeriBildirimProfili(SesTuruGenis.BASARI_KUCUK, HapticTipi.BASARI)
    EtkilesimTuru.BASARI_ORTA -> GeriBildirimProfili(SesTuruGenis.BASARI_ORTA, HapticTipi.BASARI_GUCLU)
    EtkilesimTuru.BASARI_BUYUK -> GeriBildirimProfili(SesTuruGenis.BASARI_BUYUK, HapticTipi.BASARI_GUCLU)
    EtkilesimTuru.ROZET -> GeriBildirimProfili(SesTuruGenis.ROZET_KAZANDI, HapticTipi.KUTLAMA)
    EtkilesimTuru.RUTBE -> GeriBildirimProfili(SesTuruGenis.RUTBE_ATLADI, HapticTipi.RUTBE)
    EtkilesimTuru.SERI -> GeriBildirimProfili(SesTuruGenis.SERI_ARTTI, HapticTipi.BASARI)
    EtkilesimTuru.ODAK_BASLADI -> GeriBildirimProfili(SesTuruGenis.ODAK_BASLADI, HapticTipi.BASARI)
    EtkilesimTuru.ODAK_TAMAMLANDI -> GeriBildirimProfili(SesTuruGenis.ODAK_TAMAMLANDI, HapticTipi.KUTLAMA)
    EtkilesimTuru.COZUM_ACILDI -> GeriBildirimProfili(SesTuruGenis.COZUM_ACILDI, HapticTipi.TIKLAMA)
    EtkilesimTuru.DEGERLENDIRME -> GeriBildirimProfili(SesTuruGenis.DEGERLENDIRME_ACILDI, HapticTipi.TIKLAMA_AGIR)
    EtkilesimTuru.HATA -> GeriBildirimProfili(SesTuruGenis.HATA, HapticTipi.HATA)
    EtkilesimTuru.UYARI -> GeriBildirimProfili(SesTuruGenis.UYARI, HapticTipi.UYARI)
    EtkilesimTuru.ILK_SORU -> GeriBildirimProfili(SesTuruGenis.ILK_SORU, HapticTipi.KUTLAMA)
    EtkilesimTuru.MILESTONE -> GeriBildirimProfili(SesTuruGenis.MILESTONE, HapticTipi.KUTLAMA)
    EtkilesimTuru.YUKLEME_TAMAM -> GeriBildirimProfili(SesTuruGenis.YUKLEME_TAMAM, HapticTipi.BASARI)
}

private fun premiumGeriBildirim(
    context: android.content.Context,
    tur: EtkilesimTuru,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    val profil = tur.profil()
    SesYoneticisi.uygulama(context).calPremium(profil.ses, profil.haptik, scope)
}

/** Initializes the app-scoped audio and haptic managers once when the app's Compose tree starts. */
@Composable
fun PremiumSistemleriBaslat() {
    val context = LocalContext.current.applicationContext
    remember(context) {
        SesYoneticisi.uygulama(context)
        HapticYoneticisi.uygulama(context)
        true
    }
}

@Composable
fun PremiumButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.large,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    interactionTur: EtkilesimTuru = EtkilesimTuru.TIKLAMA,
    content: @Composable RowScope.() -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Button(
        onClick = { premiumGeriBildirim(context, interactionTur, scope); onClick() },
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        content = content,
    )
}

@Composable
fun PremiumOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.large,
    interactionTur: EtkilesimTuru = EtkilesimTuru.TIKLAMA,
    content: @Composable RowScope.() -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    OutlinedButton(
        onClick = { premiumGeriBildirim(context, interactionTur, scope); onClick() },
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        content = content,
    )
}

@Composable
fun PremiumTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionTur: EtkilesimTuru = EtkilesimTuru.TIKLAMA,
    content: @Composable RowScope.() -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    TextButton(
        onClick = { premiumGeriBildirim(context, interactionTur, scope); onClick() },
        modifier = modifier,
        enabled = enabled,
        content = content,
    )
}

@Composable
fun PremiumFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.large,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    FloatingActionButton(
        onClick = { premiumGeriBildirim(context, EtkilesimTuru.KART_ACILIS, scope); onClick() },
        modifier = modifier,
        shape = shape,
        content = content,
    )
}

@Composable
fun PremiumExtendedFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.large,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    content: @Composable RowScope.() -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    ExtendedFloatingActionButton(
        onClick = { premiumGeriBildirim(context, EtkilesimTuru.KART_ACILIS, scope); onClick() },
        modifier = modifier,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        content = content,
    )
}

@Composable
fun PremiumCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.extraLarge,
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 5.dp),
    colors: CardColors = CardDefaults.cardColors(),
    border: BorderStroke? = null,
    interactionTur: EtkilesimTuru = EtkilesimTuru.KART_ACILIS,
    content: @Composable ColumnScope.() -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale = animateFloatPremium(
        target = if (pressed && enabled) 0.985f else 1f,
        animationSpec = springPremium(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label = "premium-card-press",
    )
    val clickModifier = if (onClick == null) Modifier else Modifier.clickable(
        enabled = enabled,
        interactionSource = interactionSource,
        indication = null,
        role = Role.Button,
        onClick = { premiumGeriBildirim(context, interactionTur, scope); onClick?.invoke() },
    )
    Card(
        modifier = modifier
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .then(clickModifier),
        shape = shape,
        elevation = elevation,
        colors = colors,
        border = border,
        content = content,
    )
}

@Composable
fun PremiumSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Switch(
        checked = checked,
        onCheckedChange = { value ->
            premiumGeriBildirim(context, if (value) EtkilesimTuru.TOGGLE_AC else EtkilesimTuru.TOGGLE_KAPA, scope)
            onCheckedChange(value)
        },
        modifier = modifier,
        enabled = enabled,
    )
}

@Composable
fun PremiumSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = {
            premiumGeriBildirim(context, EtkilesimTuru.SLIDER, scope)
            onValueChangeFinished?.invoke()
        },
    )
}

@Composable
fun PremiumListItem(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    interactionTur: EtkilesimTuru = EtkilesimTuru.KART_ACILIS,
) {
    PremiumCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        interactionTur = interactionTur,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            leading?.invoke()
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                subtitle?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            trailing?.invoke()
        }
    }
}

enum class KutlamaTuru { ROZET, RUTBE, SERI, HEDEF, ODAK_TAMAM, ILK_SORU, MILESTONE }

@Composable
fun KutlamaDizisiOynat(trigger: Boolean, tur: KutlamaTuru = KutlamaTuru.ROZET) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(trigger, tur) {
        if (!trigger) return@LaunchedEffect
        scope.launch {
            val ses = SesYoneticisi.uygulama(context)
            val (sequence, haptic) = when (tur) {
                KutlamaTuru.ROZET -> listOf(
                    SesTuruGenis.ROZET_KAZANDI,
                    SesTuruGenis.BASARI_ORTA,
                    SesTuruGenis.BASARI_KUCUK,
                ) to HapticTipi.KUTLAMA
                KutlamaTuru.RUTBE -> listOf(
                    SesTuruGenis.RUTBE_ATLADI,
                    SesTuruGenis.BASARI_BUYUK,
                    SesTuruGenis.BASARI_ORTA,
                ) to HapticTipi.RUTBE
                KutlamaTuru.SERI -> listOf(SesTuruGenis.SERI_ARTTI, SesTuruGenis.BASARI_ORTA) to HapticTipi.BASARI_GUCLU
                KutlamaTuru.HEDEF -> listOf(SesTuruGenis.HEDEF, SesTuruGenis.BASARI_ORTA) to HapticTipi.BASARI_GUCLU
                KutlamaTuru.ODAK_TAMAM -> listOf(SesTuruGenis.ODAK_TAMAMLANDI, SesTuruGenis.BASARI_BUYUK) to HapticTipi.KUTLAMA
                KutlamaTuru.ILK_SORU -> listOf(SesTuruGenis.ILK_SORU) to HapticTipi.KUTLAMA
                KutlamaTuru.MILESTONE -> listOf(
                    SesTuruGenis.MILESTONE,
                    SesTuruGenis.BASARI_BUYUK,
                    SesTuruGenis.ROZET_KAZANDI,
                ) to HapticTipi.RUTBE
            }
            sequence.forEachIndexed { index, cue ->
                if (index > 0) delay(270L)
                ses.calPremium(cue, if (index == 0) haptic else null)
            }
        }
    }
}

@Composable
fun AmbiansKontrol(aktif: Boolean, tur: SesTuruGenis = SesTuruGenis.AMBIYANS_ODAK) {
    val context = LocalContext.current
    LaunchedEffect(aktif, tur) {
        val ses = SesYoneticisi.uygulama(context)
        if (aktif) ses.ambiyansBaslat(tur) else ses.ambiyansDurdur()
    }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { SesYoneticisi.uygulama(context).ambiyansDurdur() }
    }
}

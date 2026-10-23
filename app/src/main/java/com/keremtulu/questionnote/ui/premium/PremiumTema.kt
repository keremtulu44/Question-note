package com.keremtulu.questionnote.ui.premium

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.R

/**
 * Display family for titles, kickers and labels. Static faces cover Latin Extended-A so Turkish
 * glyphs (ğşıİçöü) render from the bundle, not from fallback; body copy stays on the system font.
 */
val PremiumYaziAilesi = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semi_bold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
)

/** Refined visual system used by the premium app theme and reusable UI components. */
val PremiumAcikRenkSemasi = lightColorScheme(
    primary = Color(0xFF15596B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7EDF2),
    onPrimaryContainer = Color(0xFF0A3543),
    secondary = Color(0xFF8A5C31),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE5C4),
    onSecondaryContainer = Color(0xFF34200B),
    tertiary = Color(0xFF55775E),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD9EEDB),
    onTertiaryContainer = Color(0xFF173B21),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF5F7F5),
    onBackground = Color(0xFF17252A),
    surface = Color(0xFFFFFEFB),
    onSurface = Color(0xFF17252A),
    surfaceVariant = Color(0xFFE6ECEB),
    onSurfaceVariant = Color(0xFF4D5B5E),
    outline = Color(0xFF718083),
    outlineVariant = Color(0xFFD2DCDA),
    inverseSurface = Color(0xFF26373C),
    inverseOnSurface = Color(0xFFECF3F3),
    inversePrimary = Color(0xFF92D8D2),
    surfaceTint = Color(0xFF15596B),
)

val PremiumKoyuRenkSemasi = darkColorScheme(
    primary = Color(0xFF92D8D2),
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF174D55),
    onPrimaryContainer = Color(0xFFBDEDE6),
    secondary = Color(0xFFEAC294),
    onSecondary = Color(0xFF432B10),
    secondaryContainer = Color(0xFF5D4122),
    onSecondaryContainer = Color(0xFFFFDEB8),
    tertiary = Color(0xFFB4D1B5),
    onTertiary = Color(0xFF203A25),
    tertiaryContainer = Color(0xFF38533B),
    onTertiaryContainer = Color(0xFFD0ECD0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF10191D),
    onBackground = Color(0xFFE2EBEA),
    surface = Color(0xFF172328),
    onSurface = Color(0xFFE2EBEA),
    surfaceVariant = Color(0xFF354246),
    onSurfaceVariant = Color(0xFFBDCAC9),
    outline = Color(0xFF879695),
    outlineVariant = Color(0xFF465457),
    inverseSurface = Color(0xFFE2EBEA),
    inverseOnSurface = Color(0xFF253438),
    inversePrimary = Color(0xFF15596B),
    surfaceTint = Color(0xFF92D8D2),
)

object PremiumMarkaRenkler {
    val Murekkep = Color(0xFF153544)
    val MurekkepAcik = Color(0xFF1E4A5E)
    val MurekkepKoyu = Color(0xFF0C202C)
    val Petrol = Color(0xFF15596B)
    val PetrolParlak = Color(0xFF28859A)
    val Bakir = Color(0xFFB87333)
    val BakirIsik = Color(0xFFEFC18A)
    val BakirKoyu = Color(0xFF8B5A1A)
    val Altin = Color(0xFFF5D38E)
    val AltinKoyu = Color(0xFFD4A84E)
    val Deniz = Color(0xFF008080)
    val DenizParlak = Color(0xFF20AFA6)
    val DenizAcik = Color(0xFF92D8D2)
    val Mercan = Color(0xFFE87A7A)
    val MercanAcik = Color(0xFFFFB4B4)
    val Mor = Color(0xFF8B5CF6)
    val MorAcik = Color(0xFFA78BFA)
    val Yesil = Color(0xFF27845B)
    val YesilAcik = Color(0xFF86CFA2)
    val Kagit = Color(0xFFFFF7E8)
    val Gece = Color(0xFF0C202C)
    val Grafit = Color(0xFF1E293B)
}

// Manrope'ta check/ok gibi sembol glifleri yok; tek basina kullanilanlar sistem ailesinden
// ayinsel olarak aileye uygun gorunur (harf karisimini onler).
val PremiumSembolAilesi = FontFamily.Default

val PremiumTipografi: Typography = Typography().let { base ->
    fun androidx.compose.ui.text.TextStyle.yaziAilesi(
        ozellikler: String? = null,
    ): androidx.compose.ui.text.TextStyle = copy(
        fontFamily = PremiumYaziAilesi,
        fontFeatureSettings = ozellikler,
    )
    base.copy(
        displaySmall = base.displaySmall.copy(
            fontSize = 34.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.5).sp,
            fontWeight = FontWeight.Bold,
        ).yaziAilesi("tnum"),
        headlineLarge = base.headlineLarge.copy(
            fontSize = 30.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.4).sp,
            fontWeight = FontWeight.Bold,
        ).yaziAilesi("tnum"),
        headlineMedium = base.headlineMedium.copy().yaziAilesi(),
        headlineSmall = base.headlineSmall.copy(
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.SemiBold,
        ).yaziAilesi(),
        titleLarge = base.titleLarge.copy().yaziAilesi(),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold).yaziAilesi("tnum"),
        titleSmall = base.titleSmall.copy().yaziAilesi(),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold).yaziAilesi("tnum"),
        labelMedium = base.labelMedium.copy().yaziAilesi("tnum"),
        labelSmall = base.labelSmall.copy().yaziAilesi("tnum"),
    )
}

val PremiumSekiller = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
)

object PremiumBosluk {
    val XXS = 2.dp
    val XS = 4.dp
    val SM = 8.dp
    val MD = 16.dp
    val LG = 24.dp
    val XL = 32.dp
    val XXL = 48.dp
    val XXXL = 64.dp
}

object PremiumGolge {
    val YOK = 0.dp
    val HAFIF = 2.dp
    val DUSUK = 4.dp
    val ORTA = 8.dp
    val YUKSEK = 16.dp
    val COK_YUKSEK = 24.dp
}

object PremiumHareket {
    const val ANINDA = 50
    const val HIZLI = 100
    const val NORMAL = 160
    const val ORTA = 240
    const val YAVAS = 320
    const val KUTLAMA = 800
    const val BUYUK_KUTLAMA = 1200
}

object PremiumGradyanlar {
    val KAHRAMAN_KARANLIK = Brush.verticalGradient(
        listOf(PremiumMarkaRenkler.Murekkep, PremiumMarkaRenkler.Gece),
    )
    val KAHRAMAN_AYDINLIK = Brush.verticalGradient(
        listOf(Color(0xFFE4F1F3), Color(0xFFF5F8F7)),
    )
    val KAHRAMAN_GRADYAN = Brush.horizontalGradient(
        listOf(PremiumMarkaRenkler.Murekkep, PremiumMarkaRenkler.Gece),
    )
    val BAKIR_GRADYAN = Brush.horizontalGradient(
        listOf(PremiumMarkaRenkler.BakirIsik, PremiumMarkaRenkler.Altin),
    )
    val DENIZ_GRADYAN = Brush.horizontalGradient(
        listOf(PremiumMarkaRenkler.DenizParlak, PremiumMarkaRenkler.DenizAcik),
    )
    val MERCAN_GRADYAN = Brush.horizontalGradient(
        listOf(PremiumMarkaRenkler.Mercan, PremiumMarkaRenkler.MercanAcik),
    )
    val MOR_GRADYAN = Brush.horizontalGradient(
        listOf(PremiumMarkaRenkler.Mor, PremiumMarkaRenkler.MorAcik),
    )
    val YESIL_GRADYAN = Brush.horizontalGradient(
        listOf(PremiumMarkaRenkler.Yesil, PremiumMarkaRenkler.YesilAcik),
    )
    val PARLAMA_MERKEZ = Brush.radialGradient(
        listOf(PremiumMarkaRenkler.BakirIsik.copy(alpha = 0.26f), Color.Transparent),
    )
    val PARLAMA_DENIZ = Brush.radialGradient(
        listOf(PremiumMarkaRenkler.DenizAcik.copy(alpha = 0.28f), Color.Transparent),
    )
    val BUTON_BIRINCI = Brush.horizontalGradient(
        listOf(PremiumMarkaRenkler.Petrol, PremiumMarkaRenkler.PetrolParlak),
    )
    val BUTON_IKINCI = Brush.horizontalGradient(
        listOf(PremiumMarkaRenkler.Bakir, PremiumMarkaRenkler.BakirIsik),
    )
    val BUTON_KUTLAMA = Brush.horizontalGradient(
        listOf(PremiumMarkaRenkler.Mor, PremiumMarkaRenkler.MorAcik),
    )
}

object PremiumAnimasyonOzelikleri {
    val HIZLI_CIKIS: AnimationSpec<Float> = tween(
        PremiumHareket.HIZLI,
        easing = CubicBezierEasing(0.4f, 0f, 1f, 1f),
    )
    val NORMAL: AnimationSpec<Float> = tween(
        PremiumHareket.NORMAL,
        easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f),
    )
    val VURGULU: AnimationSpec<Float> = tween(
        PremiumHareket.ORTA,
        easing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
    )
    val ZIPLAMA: AnimationSpec<Float> = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)
    val KUTLAMA: AnimationSpec<Float> = tween(
        PremiumHareket.KUTLAMA,
        easing = CubicBezierEasing(0.34f, 1.2f, 0.64f, 1f),
    )
}

@Composable
fun PremiumTema(koyuTema: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (koyuTema) PremiumKoyuRenkSemasi else PremiumAcikRenkSemasi,
        typography = PremiumTipografi,
        shapes = PremiumSekiller,
        content = content,
    )
}

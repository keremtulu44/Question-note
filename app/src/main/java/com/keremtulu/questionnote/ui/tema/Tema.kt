package com.keremtulu.questionnote.ui.tema

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.keremtulu.questionnote.ui.premium.PremiumAcikRenkSemasi
import com.keremtulu.questionnote.ui.premium.PremiumKoyuRenkSemasi
import com.keremtulu.questionnote.ui.premium.PremiumSekiller
import com.keremtulu.questionnote.ui.premium.PremiumTipografi

/** Existing theme retained for previews and backwards-compatible callers. */
@Composable
fun YanlisDefteriTema(
    koyuTema: Boolean = isSystemInDarkTheme(),
    hareketPolitikasi: HareketPolitikasi? = null,
    content: @Composable () -> Unit,
) {
    val hareket = hareketPolitikasi ?: sistemHareketPolitikasi()
    CompositionLocalProvider(LocalHareketPolitikasi provides hareket) {
        MaterialTheme(
            colorScheme = if (koyuTema) KoyuRenkSemasi else AcikRenkSemasi,
            typography = YanlisDefteriTipografi,
            content = content,
        )
    }
}

/** Premium production theme; Motion policy is shared with the original app and always respected. */
@Composable
fun PremiumYanlisDefteriTema(
    koyuTema: Boolean = isSystemInDarkTheme(),
    hareketPolitikasi: HareketPolitikasi? = null,
    content: @Composable () -> Unit,
) {
    val hareket = hareketPolitikasi ?: sistemHareketPolitikasi()
    CompositionLocalProvider(LocalHareketPolitikasi provides hareket) {
        MaterialTheme(
            colorScheme = if (koyuTema) PremiumKoyuRenkSemasi else PremiumAcikRenkSemasi,
            typography = PremiumTipografi,
            shapes = PremiumSekiller,
            content = content,
        )
    }
}

@Composable
fun PremiumTemaOnizlemeAydinlik(content: @Composable () -> Unit) =
    PremiumYanlisDefteriTema(koyuTema = false, content = content)

@Composable
fun PremiumTemaOnizlemeKaranlik(content: @Composable () -> Unit) =
    PremiumYanlisDefteriTema(koyuTema = true, content = content)

@Composable
fun KlasikTemaOnizlemeAydinlik(content: @Composable () -> Unit) =
    YanlisDefteriTema(koyuTema = false, content = content)

@Composable
fun KlasikTemaOnizlemeKaranlik(content: @Composable () -> Unit) =
    YanlisDefteriTema(koyuTema = true, content = content)

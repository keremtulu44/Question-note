package com.keremtulu.questionnote.ui.tema

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

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

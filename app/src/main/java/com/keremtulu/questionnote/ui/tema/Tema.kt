package com.keremtulu.questionnote.ui.tema

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun YanlisDefteriTema(
    koyuTema: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (koyuTema) KoyuRenkSemasi else AcikRenkSemasi,
        typography = YanlisDefteriTipografi,
        content = content,
    )
}

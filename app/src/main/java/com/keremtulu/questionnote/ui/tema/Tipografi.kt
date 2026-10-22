package com.keremtulu.questionnote.ui.tema

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val temelTipografi = Typography()

internal val YanlisDefteriTipografi = temelTipografi.copy(
    headlineLarge = temelTipografi.headlineLarge.copy(
        fontSize = 30.sp,
        lineHeight = 37.sp,
        letterSpacing = (-0.6).sp,
        fontWeight = FontWeight.Bold,
    ),
    headlineSmall = temelTipografi.headlineSmall.copy(
        fontSize = 25.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.3).sp,
        fontWeight = FontWeight.SemiBold,
    ),
    displaySmall = temelTipografi.displaySmall.copy(
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
        fontWeight = FontWeight.Bold,
    ),
    titleMedium = temelTipografi.titleMedium.copy(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    labelMedium = temelTipografi.labelMedium.copy(
        fontWeight = FontWeight.Medium,
    ),
)

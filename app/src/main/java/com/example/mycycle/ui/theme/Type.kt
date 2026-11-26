package com.example.mycycle.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.example.mycycle.R

@OptIn(ExperimentalTextApi::class)
private fun nunito(w: Int) = Font(
    R.font.nunito,
    weight = FontWeight(w),
    variationSettings = FontVariation.Settings(FontVariation.weight(w)),
)

val Nunito = FontFamily(nunito(400), nunito(600), nunito(700), nunito(800))

private val base = Typography()

private fun TextStyle.n() = copy(fontFamily = Nunito)

val PeonyTypography = Typography(
    displayLarge = base.displayLarge.n(),
    displayMedium = base.displayMedium.n(),
    displaySmall = base.displaySmall.n(),
    headlineLarge = base.headlineLarge.n().copy(fontWeight = FontWeight.ExtraBold),
    headlineMedium = base.headlineMedium.n().copy(fontWeight = FontWeight.ExtraBold),
    headlineSmall = base.headlineSmall.n().copy(fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.n().copy(fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.n().copy(fontWeight = FontWeight.Bold),
    titleSmall = base.titleSmall.n().copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.n(),
    bodyMedium = base.bodyMedium.n(),
    bodySmall = base.bodySmall.n(),
    labelLarge = base.labelLarge.n().copy(fontWeight = FontWeight.Bold),
    labelMedium = base.labelMedium.n().copy(fontWeight = FontWeight.SemiBold),
    labelSmall = base.labelSmall.n().copy(fontWeight = FontWeight.SemiBold),
)

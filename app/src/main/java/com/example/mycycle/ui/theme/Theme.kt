package com.example.mycycle.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object PeonyShapes {
    val Card = RoundedCornerShape(24.dp)
    val Button = RoundedCornerShape(18.dp)
    val CalendarCell = RoundedCornerShape(
        topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 6.dp,
    )
}

private val PeonyColorScheme = lightColorScheme(
    primary = PeonyColors.Accent,
    onPrimary = Color.White,
    primaryContainer = PeonyColors.Period,
    onPrimaryContainer = PeonyColors.Text,
    secondary = PeonyColors.TextSecondary,
    onSecondary = Color.White,
    secondaryContainer = PeonyColors.Forecast,
    onSecondaryContainer = PeonyColors.Text,
    tertiary = PeonyColors.TextSecondary,
    tertiaryContainer = PeonyColors.Ovulation,
    onTertiaryContainer = PeonyColors.Text,
    background = PeonyColors.Background,
    onBackground = PeonyColors.Text,
    surface = PeonyColors.Card,
    onSurface = PeonyColors.Text,
    surfaceVariant = PeonyColors.Forecast,
    onSurfaceVariant = PeonyColors.TextSecondary,
    outline = PeonyColors.TextSecondary,
)

@Composable
fun PeonyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PeonyColorScheme,
        typography = PeonyTypography,
        shapes = Shapes(medium = PeonyShapes.Button, large = PeonyShapes.Card),
        content = content,
    )
}

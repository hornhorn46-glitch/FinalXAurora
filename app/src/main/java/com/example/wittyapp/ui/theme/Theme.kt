package com.example.wittyapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CosmosDarkColors = darkColorScheme(
    primary = Color(0xFF00E5FF),
    secondary = Color(0xFF7C4DFF),
    tertiary = Color(0xFF00FFB3),

    background = Color(0xFF0B0F1A),
    surface = Color(0xFF121826),
    surfaceVariant = Color(0xFF1B2335),

    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun CosmosTheme(
    auroraScore: Int,
    content: @Composable () -> Unit
) {

    val dynamicPrimary = when {
        auroraScore > 85 -> Color(0xFF00FFB3)
        auroraScore > 65 -> Color(0xFF00C3FF)
        auroraScore > 45 -> Color(0xFF7C4DFF)
        else -> Color(0xFFFFC107)
    }

    val scheme = CosmosDarkColors.copy(primary = dynamicPrimary)

    MaterialTheme(
        colorScheme = scheme,
        typography = Typography(),
        content = content
    )
}
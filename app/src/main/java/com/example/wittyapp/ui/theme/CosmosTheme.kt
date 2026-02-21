package com.example.wittyapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.wittyapp.AppMode

@Composable
fun CosmosTheme(
    mode: AppMode,
    auroraScore: Int,
    content: @Composable () -> Unit
) {
    val earth = darkColorScheme(
        primary = Color(0xFF00FFB3),
        secondary = Color(0xFF00C3FF),
        tertiary = Color(0xFF7C4DFF),
        background = Color(0xFF070B14),
        surface = Color(0xFF0B1324),
        onSurface = Color.White
    )

    val sun = darkColorScheme(
        primary = Color(0xFFFFC107),
        secondary = Color(0xFFFF6D00),
        tertiary = Color(0xFFFF5252),
        background = Color(0xFF0B0711),
        surface = Color(0xFF120A18),
        onSurface = Color.White
    )

    MaterialTheme(
        colorScheme = if (mode == AppMode.EARTH) earth else sun,
        typography = Typography,
        content = content
    )
}

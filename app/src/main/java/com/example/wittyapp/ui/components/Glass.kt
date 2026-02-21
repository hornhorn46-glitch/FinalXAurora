package com.example.wittyapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(14.dp),
    corner: Dp = 22.dp,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(corner)
    val base = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f)
    val border = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = 0.35f),
            Color.White.copy(alpha = 0.08f),
            Color.White.copy(alpha = 0.20f)
        ),
        start = Offset.Zero,
        end = Offset(500f, 500f)
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = base,
        border = BorderStroke(1.dp, border),
        tonalElevation = 0.dp,
        shadowElevation = 10.dp
    ) {
        Box(
            Modifier
                .background(Color.Transparent)
                .drawBehind {
                    // мягкий "блик" сверху слева
                    val highlight = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.15f),
                        radius = size.minDimension * 0.9f
                    )
                    drawRect(highlight)
                }
                .padding(padding)
        ) {
            content()
        }
    }
}

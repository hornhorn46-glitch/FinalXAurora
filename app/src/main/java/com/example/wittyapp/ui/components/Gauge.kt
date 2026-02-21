package com.example.wittyapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Lightweight speedometer-like gauge.
 *
 * Fix: value text is shifted LOWER (bottom-center) to avoid overlapping the title/arc.
 */
@Composable
fun SpeedometerGauge(
    title: String,
    value: Float,
    unit: String,
    minValue: Float,
    maxValue: Float,
    warnThreshold: Float? = null,
    dangerThreshold: Float? = null,
    modifier: Modifier = Modifier
) {
    val v = value.coerceIn(minValue, maxValue)
    val t = ((v - minValue) / (maxValue - minValue).coerceAtLeast(1e-6f)).coerceIn(0f, 1f)
    val sweep = 240f
    val startAngle = 150f
    val endAngle = startAngle + sweep

    val color = when {
        dangerThreshold != null && (v >= dangerThreshold || v <= dangerThreshold && minValue < 0f && v <= dangerThreshold) ->
            Color(0xFFFF5252)
        warnThreshold != null && (v >= warnThreshold || v <= warnThreshold && minValue < 0f && v <= warnThreshold) ->
            Color(0xFFFFC107)
        else -> MaterialTheme.colorScheme.primary
    }

    Box(modifier = modifier.size(170.dp)) {

        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = min(size.width, size.height) * 0.38f
            val stroke = r * 0.26f

            // track
            drawArc(
                color = Color.White.copy(alpha = 0.18f),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(c.x - r, c.y - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // progress
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweep * t,
                useCenter = false,
                topLeft = Offset(c.x - r, c.y - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // needle
            val a = Math.toRadians((startAngle + sweep * t).toDouble())
            val tip = Offset(
                x = c.x + cos(a).toFloat() * (r * 0.95f),
                y = c.y + sin(a).toFloat() * (r * 0.95f)
            )
            drawLine(
                color = Color.Black.copy(alpha = 0.28f),
                start = c + Offset(2f, 2f),
                end = tip + Offset(2f, 2f),
                strokeWidth = stroke * 0.22f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.92f),
                start = c,
                end = tip,
                strokeWidth = stroke * 0.22f,
                cap = StrokeCap.Round
            )
            drawCircle(Color.White.copy(alpha = 0.92f), radius = stroke * 0.22f, center = c)
        }

        // title: slightly above center
        Text(
            text = title,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 26.dp),
            color = Color.White.copy(alpha = 0.92f),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )

        // value: moved LOWER (bottom-center)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (unit.isBlank()) "${'$'}{v.toInt()}" else String.format("%.1f", v),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            if (unit.isNotBlank()) {
                Text(
                    text = unit,
                    color = Color.White.copy(alpha = 0.80f),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

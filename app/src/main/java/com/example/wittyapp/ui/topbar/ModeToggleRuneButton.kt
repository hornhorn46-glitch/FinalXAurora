package com.example.wittyapp.ui.topbar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.wittyapp.AppMode
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ModeToggleRuneButton(
    mode: AppMode,
    onToggle: () -> Unit
) {
    val bg = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
    val stroke = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)

    Surface(
        shape = CircleShape,
        color = bg,
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .pointerInput(Unit) { detectTapGestures(onTap = { onToggle() }) }
    ) {
        Canvas(Modifier.background(Color.Transparent)) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = size.minDimension / 2f * 0.72f

            val main = stroke
            val shadow = Color.Black.copy(alpha = 0.18f)

            fun line(a: Offset, b: Offset, w: Float) {
                drawLine(shadow, a + Offset(1f, 1f), b + Offset(1f, 1f), strokeWidth = w, cap = StrokeCap.Round)
                drawLine(main, a, b, strokeWidth = w, cap = StrokeCap.Round)
            }

            fun circle(rr: Float, w: Float) {
                drawCircle(shadow, radius = rr, center = c + Offset(1f, 1f), style = Stroke(w, cap = StrokeCap.Round))
                drawCircle(main, radius = rr, center = c, style = Stroke(w, cap = StrokeCap.Round))
            }

            circle(r * 1.08f, w = 3.2f)

            if (mode == AppMode.EARTH) {
                circle(r * 0.78f, w = 3.2f)
                line(Offset(c.x, c.y - r * 0.78f), Offset(c.x, c.y + r * 0.78f), w = 3.0f)
                val y = c.y + r * 0.15f
                line(Offset(c.x - r * 0.62f, y), Offset(c.x + r * 0.62f, y), w = 3.0f)
            } else {
                circle(r * 0.62f, w = 3.2f)
                val ray = r * 0.95f
                val inner = r * 0.72f
                val w = 3.0f
                for (i in 0 until 8) {
                    val a = Math.toRadians((i * 45.0))
                    val p0 = Offset(c.x + cos(a).toFloat() * inner, c.y + sin(a).toFloat() * inner)
                    val p1 = Offset(c.x + cos(a).toFloat() * ray, c.y + sin(a).toFloat() * ray)
                    line(p0, p1, w)
                }
            }
        }
    }
}

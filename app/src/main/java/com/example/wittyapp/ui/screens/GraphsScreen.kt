package com.example.wittyapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.wittyapp.domain.GraphPoint
import com.example.wittyapp.ui.strings.AppStrings
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

data class GraphSeries(
    val title: String,
    val unit: String,
    val points: List<GraphPoint>,
    val minY: Double,
    val maxY: Double,
    val gridStepY: Double,
    val dangerBelow: Double? = null,
    val dangerAbove: Double? = null
)

enum class GraphsMode { EARTH, SUN }

@Composable
fun GraphsScreen(
    title: String,
    series: List<GraphSeries>,
    mode: GraphsMode,
    strings: AppStrings,
    onClose: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onClose) { Text(strings.close) }
        }

        series.forEach { s ->
            Card {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("${s.title} (${s.unit})", style = MaterialTheme.typography.titleMedium)
                    GraphCanvas(series = s)
                }
            }
        }

        Spacer(Modifier.height(60.dp))
    }
}

@Composable
private fun GraphCanvas(series: GraphSeries) {
    val gridColor = Color.White.copy(alpha = 0.10f)
    val axisColor = Color.White.copy(alpha = 0.25f)
    val lineColor = MaterialTheme.colorScheme.primary
    val dangerColor = Color.Red.copy(alpha = 0.20f)

    val timeFmt = rememberTimeFormatter()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val leftPad = 54f
        val rightPad = 18f
        val topPad = 10f
        val bottomPad = 28f

        val w = size.width
        val h = size.height

        val plotW = (w - leftPad - rightPad).coerceAtLeast(1f)
        val plotH = (h - topPad - bottomPad).coerceAtLeast(1f)

        fun x(i: Int): Float {
            val n = (series.points.size - 1).coerceAtLeast(1)
            return leftPad + (i.toFloat() / n.toFloat()) * plotW
        }

        fun y(v: Double): Float {
            val den = (series.maxY - series.minY)
            val t = if (den == 0.0) 0.5f else ((v - series.minY) / den).toFloat()
            return topPad + (1f - t.coerceIn(0f, 1f)) * plotH
        }

        // Danger zones
        series.dangerBelow?.let { thr ->
            val yy = y(thr)
            drawRect(
                dangerColor,
                topLeft = Offset(leftPad, yy),
                size = androidx.compose.ui.geometry.Size(plotW, h - bottomPad - yy)
            )
        }
        series.dangerAbove?.let { thr ->
            val yy = y(thr)
            drawRect(
                dangerColor,
                topLeft = Offset(leftPad, topPad),
                size = androidx.compose.ui.geometry.Size(plotW, yy - topPad)
            )
        }

        // Horizontal grid + Y labels positions
        var gy = series.minY
        while (gy <= series.maxY + 1e-9) {
            val yy = y(gy)
            drawLine(gridColor, Offset(leftPad, yy), Offset(w - rightPad, yy), strokeWidth = 1f)
            gy += series.gridStepY
        }

        // Vertical grid (6 lines)
        val vLines = 6
        for (i in 0..vLines) {
            val xx = leftPad + (i.toFloat() / vLines.toFloat()) * plotW
            drawLine(gridColor, Offset(xx, topPad), Offset(xx, h - bottomPad), strokeWidth = 1f)
        }

        // Axis
        drawLine(axisColor, Offset(leftPad, topPad), Offset(leftPad, h - bottomPad), strokeWidth = 2f)
        drawLine(axisColor, Offset(leftPad, h - bottomPad), Offset(w - rightPad, h - bottomPad), strokeWidth = 2f)

        // Line
        if (series.points.size >= 2) {
            for (i in 0 until series.points.size - 1) {
                val p0 = series.points[i]
                val p1 = series.points[i + 1]
                drawLine(
                    lineColor,
                    Offset(x(i), y(p0.value)),
                    Offset(x(i + 1), y(p1.value)),
                    strokeWidth = 3f
                )
            }
        }

        // Labels (native canvas)
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.WHITE
                textSize = 24f
            }

            // Y labels (min/mid/max)
            fun drawYLabel(v: Double) {
                val yy = y(v)
                val txt = v.roundToInt().toString()
                canvas.nativeCanvas.drawText(txt, 6f, yy + 8f, paint)
            }

            drawYLabel(series.minY)
            drawYLabel((series.minY + series.maxY) / 2.0)
            drawYLabel(series.maxY)

            // X labels: first / mid / last
            if (series.points.isNotEmpty()) {
                paint.textSize = 22f
                val first = timeFmt(series.points.first())
                val mid = timeFmt(series.points[series.points.size / 2])
                val last = timeFmt(series.points.last())

                canvas.nativeCanvas.drawText(first, leftPad, h - 6f, paint)
                canvas.nativeCanvas.drawText(mid, leftPad + plotW * 0.45f, h - 6f, paint)
                canvas.nativeCanvas.drawText(last, leftPad + plotW * 0.82f, h - 6f, paint)
            }
        }
    }
}

@Composable
private fun rememberTimeFormatter(): (GraphPoint) -> String {
    val fmt = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val zone = remember { ZoneId.systemDefault() }
    return remember {
        { p: GraphPoint -> p.t.atZone(zone).toLocalTime().format(fmt) }
    }
}
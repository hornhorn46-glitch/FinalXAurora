package com.example.wittyapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.wittyapp.ui.GraphSeries
import com.example.wittyapp.ui.strings.AppStrings
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

enum class GraphsMode { EARTH }

@Composable
fun GraphsScreen(
    title: String,
    series: GraphSeries,
    mode: GraphsMode,
    strings: AppStrings,
    contentPadding: PaddingValues,
    onClose: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onClose) { Text(strings.close) }
        }

        GraphCard(
            header = "Kp",
            unit = "",
            points = series.kp.map { it.t to it.v.toFloat() },
            yStep = 1f,
            dangerBands = listOf(DangerBand(from = 7f, to = 9f, color = Color(0x33FF5252)))
        )

        GraphCard(
            header = "Bz (нТл)",
            unit = "нТл",
            points = series.bz.map { it.t to it.v.toFloat() },
            yStep = 2f,
            dangerBands = listOf(DangerBand(from = -20f, to = -6f, color = Color(0x33FF5252)))
        )

        GraphCard(
            header = "Speed (км/с)",
            unit = "км/с",
            points = series.speed.map { it.t to it.v.toFloat() },
            yStep = 100f,
            dangerBands = listOf(DangerBand(from = 650f, to = 1200f, color = Color(0x22FFB300)))
        )

        Spacer(Modifier.height(80.dp))
    }
}

private data class DangerBand(val from: Float, val to: Float, val color: Color)

@Composable
private fun GraphCard(
    header: String,
    unit: String,
    points: List<Pair<java.time.Instant, Float>>,
    yStep: Float,
    dangerBands: List<DangerBand>
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f))) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(header, style = MaterialTheme.typography.titleLarge)

            if (points.size < 2) {
                Text("Нет данных", style = MaterialTheme.typography.bodyMedium)
                return@Column
            }

            val times = points.map { it.first }
            val ys = points.map { it.second }
            val minY = (ys.minOrNull() ?: 0f)
            val maxY = (ys.maxOrNull() ?: 1f)
            val pad = (maxY - minY).coerceAtLeast(yStep)
            val y0 = (minY - pad * 0.15f)
            val y1 = (maxY + pad * 0.15f)

            val leftLabel = 44.dp
            val bottomLabel = 22.dp

            Box(Modifier.fillMaxWidth().height(220.dp)) {
                Canvas(
                    Modifier
                        .fillMaxSize()
                        .padding(start = leftLabel, bottom = bottomLabel)
                ) {
                    val w = size.width
                    val h = size.height

                    fun yToPx(v: Float): Float {
                        val t = ((v - y0) / (y1 - y0)).coerceIn(0f, 1f)
                        return h - t * h
                    }

                    // опасные зоны
                    dangerBands.forEach { b ->
                        val top = yToPx(b.to)
                        val bot = yToPx(b.from)
                        val yTop = minOf(top, bot)
                        val yBot = maxOf(top, bot)
                        drawRect(b.color, topLeft = Offset(0f, yTop), size = Size(w, yBot - yTop))
                    }

                    // сетка горизонтальная
                    val firstTick = (kotlin.math.floor(y0 / yStep) * yStep).toFloat()
                    var tick = firstTick
                    while (tick <= y1 + 0.0001f) {
                        val yy = yToPx(tick)
                        drawLine(Color.White.copy(alpha = 0.10f), Offset(0f, yy), Offset(w, yy), strokeWidth = 1f)
                        tick += yStep
                    }

                    // сетка вертикальная (6 полос)
                    val vLines = 6
                    for (i in 0..vLines) {
                        val x = w * (i / vLines.toFloat())
                        drawLine(Color.White.copy(alpha = 0.08f), Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                    }

                    // линия
                    val minT = times.first()
                    val maxT = times.last()
                    val span = (maxT.toEpochMilli() - minT.toEpochMilli()).coerceAtLeast(1)

                    fun xToPx(t: java.time.Instant): Float {
                        val dt = (t.toEpochMilli() - minT.toEpochMilli()).toFloat()
                        return (dt / span) * w
                    }

                    val path = Path()
                    points.forEachIndexed { i, (t, v) ->
                        val x = xToPx(t)
                        val y = yToPx(v)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = MaterialTheme.colorScheme.primary,
                        style = Stroke(width = 5f, cap = StrokeCap.Round)
                    )
                }

                // подписи Y слева
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(leftLabel),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    val top = y1
                    val mid = (y0 + y1) / 2f
                    val bot = y0
                    Text(formatTick(top), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                    Text(formatTick(mid), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                    Text(formatTick(bot), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                }

                // подписи X снизу
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = leftLabel)
                        .fillMaxWidth()
                        .height(bottomLabel),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val fmt = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
                    val t0 = times.first()
                    val t1 = times[times.size / 2]
                    val t2 = times.last()
                    Text(fmt.format(t0), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                    Text(fmt.format(t1), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                    Text(fmt.format(t2), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                }
            }
        }
    }
}

private fun formatTick(v: Float): String {
    val iv = v.roundToInt()
    return iv.toString()
}

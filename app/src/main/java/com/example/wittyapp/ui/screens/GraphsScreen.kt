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
import com.example.wittyapp.ui.strings.AppStrings
import kotlin.math.roundToInt

data class GraphPoint(
    val xLabel: String,
    val value: Double
)

data class GraphSeries(
    val title: String,
    val unit: String,
    val points: List<GraphPoint>,
    val minY: Double,
    val maxY: Double,
    val gridStepY: Double
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

    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = Color.White.copy(alpha = 0.10f)

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

                    Text("${s.title} (${s.unit})")

                    Canvas(
                        Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {

                        val left = 50f
                        val bottom = size.height - 30f
                        val right = size.width - 20f
                        val top = 20f

                        val width = right - left
                        val height = bottom - top

                        fun x(i: Int): Float {
                            val count = (s.points.size - 1).coerceAtLeast(1)
                            return left + (i.toFloat() / count) * width
                        }

                        fun y(v: Double): Float {
                            val ratio = ((v - s.minY) / (s.maxY - s.minY))
                                .toFloat()
                                .coerceIn(0f, 1f)
                            return bottom - ratio * height
                        }

                        // Grid
                        var gy = s.minY
                        while (gy <= s.maxY) {
                            val yy = y(gy)
                            drawLine(gridColor, Offset(left, yy), Offset(right, yy))
                            gy += s.gridStepY
                        }

                        // Line
                        for (i in 0 until s.points.size - 1) {
                            drawLine(
                                primaryColor,
                                Offset(x(i), y(s.points[i].value)),
                                Offset(x(i + 1), y(s.points[i + 1].value)),
                                strokeWidth = 3f
                            )
                        }

                        // Labels
                        drawIntoCanvas { canvas ->
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 24f
                            }

                            canvas.nativeCanvas.drawText(
                                s.minY.roundToInt().toString(),
                                5f,
                                bottom,
                                paint
                            )
                            canvas.nativeCanvas.drawText(
                                s.maxY.roundToInt().toString(),
                                5f,
                                top,
                                paint
                            )
                        }
                    }
                }
            }
        }
    }
}
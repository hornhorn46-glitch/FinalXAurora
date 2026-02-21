package com.example.wittyapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wittyapp.AppMode
import com.example.wittyapp.R
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.components.GlassCard
import com.example.wittyapp.ui.components.SpeedometerGauge
import com.example.wittyapp.ui.strings.AppStrings
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.*
import kotlin.random.Random

@Composable
fun NowScreen(
    vm: SpaceWeatherViewModel,
    mode: AppMode,
    strings: AppStrings,
    contentPadding: PaddingValues,
    onOpenGraphs: () -> Unit,
    onOpenEvents: () -> Unit
) {
    val state = vm.state
    LaunchedEffect(Unit) {
        vm.refresh()
        vm.startAutoRefresh(10 * 60 * 1000L)
    }

    var help by remember { mutableStateOf<HelpTopic?>(null) }

    val bgRes = if (mode == AppMode.EARTH) R.drawable.earth_bg else R.drawable.sun_bg

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(bgRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 1.0f
        )
        Canvas(Modifier.fillMaxSize()) { drawRect(Color(0x70000000)) }

        SnowLayerWithWindReduced()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TopRow(
                title = strings.now,
                loading = state.loading,
                updatedAt = state.updatedAt?.let(::formatUpdatedAt) ?: "—",
                onRefresh = { vm.refresh() },
                onGraphs = onOpenGraphs,
                onEvents = onOpenEvents
            )

            if (state.error != null) {
                GlassCard {
                    Text(state.error, color = Color.White)
                }
            }

            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Прогноз сияний (3 часа)", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Text("${state.auroraScore}/100 — ${state.auroraTitle}", color = Color.White.copy(alpha = 0.90f))
                    LinearProgressIndicator(
                        progress = { (state.auroraScore.coerceIn(0, 100) / 100f) },
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.White.copy(alpha = 0.20f)
                    )
                    if (state.auroraDetails.isNotBlank()) {
                        Text(state.auroraDetails, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                    }
                }
            }

            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Параметры (сейчас)", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        IconButton(onClick = { help = HelpTopic.OVERVIEW }) {
                            Icon(Icons.Outlined.Info, contentDescription = null, tint = Color.White)
                        }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            SpeedometerGauge(
                                title = "Kp",
                                value = (state.kpNow ?: 0.0).toFloat(),
                                unit = "",
                                minValue = 0f,
                                maxValue = 9f,
                                warnThreshold = 5f,
                                dangerThreshold = 7f
                            )
                            HelpDot { help = HelpTopic.KP }
                        }
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            SpeedometerGauge(
                                title = "Speed",
                                value = (state.speedNow ?: 0.0).toFloat(),
                                unit = "км/с",
                                minValue = 250f,
                                maxValue = 1200f,
                                warnThreshold = 600f,
                                dangerThreshold = 750f
                            )
                            HelpDot { help = HelpTopic.SPEED }
                        }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            SpeedometerGauge(
                                title = "Bz",
                                value = (state.bzNow ?: 0.0).toFloat(),
                                unit = "нТл",
                                minValue = -20f,
                                maxValue = 20f,
                                warnThreshold = 2f,
                                dangerThreshold = 6f
                            )
                            HelpDot { help = HelpTopic.BZ }
                        }
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            SpeedometerGauge(
                                title = "ρ",
                                value = (state.densityNow ?: 0.0).toFloat(),
                                unit = "",
                                minValue = 0f,
                                maxValue = 50f,
                                warnThreshold = 15f,
                                dangerThreshold = 25f
                            )
                            HelpDot { help = HelpTopic.DENSITY }
                        }
                    }
                }
            }

            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Компас B-field (Bx/Bz)", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        IconButton(onClick = { help = HelpTopic.BFIELD }) {
                            Icon(Icons.Outlined.Info, contentDescription = null, tint = Color.White)
                        }
                    }
                    BFieldCompass(
                        bx = (state.bxNow ?: 0.0).toFloat(),
                        bz = (state.bzNow ?: 0.0).toFloat()
                    )
                }
            }

            GlassCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("🐸 тут была лягушка", color = Color.White.copy(alpha = 0.9f))
                    Text("тут был Женя", color = Color.White.copy(alpha = 0.65f))
                }
            }

            Spacer(Modifier.height(80.dp))
        }

        LoadingToastSheet(visible = state.loading, text = strings.loadingData)

        help?.let { t ->
            HelpDialog(topic = t, onClose = { help = null })
        }
    }
}

@Composable
private fun HelpDot(onClick: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
        IconButton(onClick = onClick) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = Color.White.copy(alpha = 0.9f))
        }
    }
}

@Composable
private fun TopRow(
    title: String,
    loading: Boolean,
    updatedAt: String,
    onRefresh: () -> Unit,
    onGraphs: () -> Unit,
    onEvents: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Column {
            Text(title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(
                "обновлено: $updatedAt",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.80f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(onClick = onGraphs) { Icon(Icons.Default.ShowChart, null, tint = Color.White) }
            IconButton(onClick = onEvents) { Icon(Icons.Default.Notifications, null, tint = Color.White) }
            IconButton(onClick = onRefresh, enabled = !loading) { Icon(Icons.Default.Refresh, null, tint = Color.White) }
        }
    }
}

@Composable
private fun LoadingToastSheet(visible: Boolean, text: String) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            GlassCard(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                    Text(text, color = Color.White)
                }
            }
        }
    }
}

// снег: 66.7% от прошлого (делаем 94 частиц)
@Composable
private fun SnowLayerWithWindReduced() {
    val particles = remember {
        List(94) {
            SnowParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                r = 1.0f + Random.nextFloat() * 2.6f,
                speedY = 0.08f + Random.nextFloat() * 0.45f,
                drift = (Random.nextFloat() - 0.5f) * 0.35f
            )
        }
    }

    val t by rememberInfiniteTransition(label = "snow")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(16000, easing = LinearEasing), RepeatMode.Restart),
            label = "snowT"
        )

    // периодический "ветерок"
    val wind by rememberInfiniteTransition(label = "wind")
        .animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(6200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "windX"
        )

    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        particles.forEach { p ->
            val pxBase = p.x * w
            val py = ((p.y + t * p.speedY) % 1f) * h
            val windX = wind * 18f
            val px = (pxBase + windX + sin((py / h) * 6.28f) * 6f * p.drift).mod(w)
            drawCircle(Color.White.copy(alpha = 0.30f), radius = p.r, center = Offset(px, py))
        }
    }
}

private data class SnowParticle(
    val x: Float, val y: Float, val r: Float, val speedY: Float, val drift: Float
)

private fun formatUpdatedAt(i: Instant): String {
    val z = ZoneId.systemDefault()
    val dt = i.atZone(z).toLocalDateTime()
    val f = DateTimeFormatter.ofPattern("dd.MM HH:mm")
    return dt.format(f)
}

private enum class HelpTopic { OVERVIEW, KP, SPEED, BZ, DENSITY, BFIELD }

@Composable
private fun HelpDialog(topic: HelpTopic, onClose: () -> Unit) {
    val (title, text) = when (topic) {
        HelpTopic.OVERVIEW -> "Как читать показатели" to
            "Смотри на три вещи: Kp (итоговая активность), Speed (скорость ветра) и Bz.
" +
            "Если Bz отрицательный (вниз) и скорость высокая — шанс активности растёт."
        HelpTopic.KP -> "Kp" to
            "Kp — индекс геомагнитной активности (0–9).
" +
            "5+ — заметные возмущения, 7+ — сильные."
        HelpTopic.SPEED -> "Speed" to
            "Скорость солнечного ветра.
" +
            "600+ км/с — часто усиливает эффект на Земле."
        HelpTopic.BZ -> "Bz" to
            "Компонента магнитного поля межпланетного поля.
" +
            "Отрицательный Bz (вниз) — лучше для сияний/бурь."
        HelpTopic.DENSITY -> "Плотность" to
            "Плотность плазмы.
" +
            "Резкие всплески могут усиливать воздействие."
        HelpTopic.BFIELD -> "Компас Bx/Bz" to
            "Стрелка показывает направление (Bx,Bz).
" +
            "Сектора вокруг «вниз» показывают насколько направление благоприятно."
    }

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = { TextButton(onClick = onClose) { Text("Ок") } },
        title = { Text(title) },
        text = { Text(text) }
    )
}

@Composable
private fun BFieldCompass(bx: Float, bz: Float) {
    // угол по двум осям: вверх при Bz>0, вниз при Bz<0, вправо при Bx>0
    val angle = Math.toDegrees(atan2(bz.toDouble(), bx.toDouble())).toFloat() // -180..180, 0 = вправо
    // преобразуем так, чтобы 90 = вверх, -90 = вниз
    val displayAngle = angle

    Canvas(Modifier.fillMaxWidth().height(220.dp)) {
        val w = size.width
        val h = size.height
        val c = Offset(w / 2f, h / 2f)
        val r = min(w, h) * 0.38f

        // сектора вокруг "вниз" (270°). В координатах Canvas 0° вправо, 90° вниз, 180° влево, 270° вверх
        // Нам проще рисовать математически: сделаем целевую точку "вниз" = 90° в canvas.
        val downCanvasDeg = 90f

        fun sector(deg: Float, color: Color) {
            drawArc(
                color = color,
                startAngle = downCanvasDeg - deg,
                sweepAngle = deg * 2,
                useCenter = true,
                topLeft = Offset(c.x - r, c.y - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                alpha = 0.14f
            )
        }

        sector(65f, Color(0xFF66FF66))
        sector(40f, Color(0xFFFFFF66))
        sector(20f, Color(0xFFFFB74D))
        sector(5f, Color(0xFFFF5252))

        // окружность
        drawCircle(Color.White.copy(alpha = 0.20f), radius = r, center = c, style = Stroke(6f))
        drawCircle(Color.White.copy(alpha = 0.08f), radius = r * 0.82f, center = c, style = Stroke(2f))

        // стрелка: преобразуем math angle (atan2(bz,bx) где 0 вправо, 90 вверх) к canvas (0 вправо, 90 вниз)
        val mathDeg = displayAngle
        val canvasDeg = 90f - mathDeg
        val a = Math.toRadians(canvasDeg.toDouble())
        val p = Offset(c.x + cos(a).toFloat() * (r * 0.92f), c.y + sin(a).toFloat() * (r * 0.92f))
        val col = when {
            // близко к "вниз": canvasDeg близко 90
            angleDiff(canvasDeg, downCanvasDeg) <= 5f -> Color(0xFFFF5252)
            angleDiff(canvasDeg, downCanvasDeg) <= 20f -> Color(0xFFFFB74D)
            angleDiff(canvasDeg, downCanvasDeg) <= 40f -> Color(0xFFFFFF66)
            angleDiff(canvasDeg, downCanvasDeg) <= 65f -> Color(0xFF66FF66)
            else -> MaterialTheme.colorScheme.primary
        }

        drawLine(Color.Black.copy(alpha = 0.28f), c + Offset(2f, 2f), p + Offset(2f, 2f), strokeWidth = 10f, cap = StrokeCap.Round)
        drawLine(col, c, p, strokeWidth = 10f, cap = StrokeCap.Round)
        drawCircle(Color.White.copy(alpha = 0.90f), radius = 10f, center = c)
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Bx=${"%.1f".format(bx)}", color = Color.White.copy(alpha = 0.85f))
        Text("Bz=${"%.1f".format(bz)}", color = Color.White.copy(alpha = 0.85f))
    }
}

private fun angleDiff(a: Float, b: Float): Float {
    var d = (a - b) % 360f
    if (d < -180f) d += 360f
    if (d > 180f) d -= 360f
    return abs(d)
}

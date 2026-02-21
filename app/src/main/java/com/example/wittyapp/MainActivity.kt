package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.SpaceWeatherViewModelFactory
import com.example.wittyapp.ui.screens.EventsScreen
import com.example.wittyapp.ui.screens.FullscreenWebImageScreen
import com.example.wittyapp.ui.screens.GraphPoint as UiGraphPoint
import com.example.wittyapp.ui.screens.GraphSeries as UiGraphSeries
import com.example.wittyapp.ui.screens.GraphsMode
import com.example.wittyapp.ui.screens.GraphsScreen
import com.example.wittyapp.ui.screens.NowScreen
import com.example.wittyapp.ui.screens.SunScreen
import com.example.wittyapp.ui.screens.TutorialScreen
import com.example.wittyapp.ui.screens.SettingsScreen
import com.example.wittyapp.ui.strings.AppLanguage
import com.example.wittyapp.ui.strings.rememberAppStrings
import com.example.wittyapp.ui.theme.CosmosTheme
import com.example.wittyapp.ui.topbar.ModeToggleRuneButton
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private sealed interface Screen {
    data object Now : Screen
    data object Graphs : Screen
    data object Events : Screen
    data object Sun : Screen
    data object Settings : Screen
    data object Tutorial : Screen
    data class FullImage(val url: String, val title: String) : Screen
}

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val api = remember { SpaceWeatherApi() }
            val vm: SpaceWeatherViewModel = viewModel(factory = SpaceWeatherViewModelFactory(api))

            var mode by remember { mutableStateOf(AppMode.EARTH) }
            var language by remember { mutableStateOf(AppLanguage.RU) }
            val strings = rememberAppStrings(language)

            // простой стек экранов без navigation-compose
            val stack = remember { mutableStateListOf<Screen>(Screen.Now) }
            val current = stack.last()

            fun push(s: Screen) = stack.add(s)
            fun pop(): Boolean {
                return if (stack.size > 1) {
                    stack.removeAt(stack.lastIndex)
                    true
                } else false
            }

            CosmosTheme(mode = mode, auroraScore = vm.state.auroraScore) {
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {

                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = when (current) {
                                            Screen.Now -> strings.now
                                            Screen.Graphs -> "Графики 24ч"
                                            Screen.Events -> strings.events
                                            Screen.Sun -> "Солнце"
                                            Screen.Settings -> strings.settingsTitle
                                            Screen.Tutorial -> strings.tutorialTitle
                                            is Screen.FullImage -> current.title
                                        }
                                    )
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.10f)
                                ),
                                actions = {
                                    // Кнопка обучения
                                    IconButton(onClick = { push(Screen.Tutorial) }) {
                                        Icon(Icons.Default.MenuBook, contentDescription = "Обучение")
                                    }

                                    // Переключатель Земля/Солнце (руна-кнопка)
                                    ModeToggleRuneButton(
                                        mode = mode,
                                        onToggle = {
                                            mode = if (mode == AppMode.EARTH) AppMode.SUN else AppMode.EARTH
                                            // при переключении режима сразу показываем соответствующий главный экран
                                            if (mode == AppMode.SUN) {
                                                // если уже на Sun — оставим
                                                if (stack.last() !is Screen.Sun) push(Screen.Sun)
                                            } else {
                                                // Земля -> Now
                                                if (stack.last() !is Screen.Now) push(Screen.Now)
                                            }
                                        }
                                    )

                                    // Настройки
                                    IconButton(onClick = { push(Screen.Settings) }) {
                                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                                    }
                                }
                            )
                        }
                    ) { contentPadding ->

                        when (val scr = current) {

                            Screen.Now -> NowScreen(
                                vm = vm,
                                mode = mode,
                                strings = strings,
                                contentPadding = contentPadding,
                                onOpenGraphs = { push(Screen.Graphs) },
                                onOpenEvents = { push(Screen.Events) }
                            )

                            Screen.Graphs -> {
                                val series = buildGraphsSeriesForUi(vm)
                                GraphsScreen(
                                    title = "Графики 24ч",
                                    series = series,
                                    mode = if (mode == AppMode.SUN) GraphsMode.SUN else GraphsMode.EARTH,
                                    strings = strings,
                                    onClose = { pop() }
                                )
                            }

                            Screen.Events -> EventsScreen(
                                vm = vm,
                                strings = strings,
                                contentPadding = contentPadding,
                                onClose = { pop() }
                            )

                            Screen.Sun -> SunScreen(
                                strings = strings,
                                contentPadding = contentPadding,
                                onOpenFull = { url, title -> push(Screen.FullImage(url, title)) }
                            )

                            Screen.Settings -> SettingsScreen(
                                strings = strings,
                                contentPadding = contentPadding,
                                currentLanguage = language,
                                onSetLanguage = { language = it },
                                onClose = { pop() }
                            )

                            Screen.Tutorial -> TutorialScreen(
                                strings = strings,
                                contentPadding = contentPadding,
                                onClose = { pop() }
                            )

                            is Screen.FullImage -> FullscreenWebImageScreen(
                                title = scr.title,
                                url = scr.url,
                                contentPadding = contentPadding,
                                onClose = { pop() }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildGraphsSeriesForUi(vm: SpaceWeatherViewModel): List<UiGraphSeries> {
    val f = DateTimeFormatter.ofPattern("HH:mm")
    val zone = ZoneId.systemDefault()

    fun label(t: java.time.Instant): String = t.atZone(zone).toLocalTime().format(f)

    val kp = vm.state.kpSeries24h.map { UiGraphPoint(xLabel = label(it.t), value = it.v) }
    val sp = vm.state.speedSeries24h.map { UiGraphPoint(xLabel = label(it.t), value = it.v) }
    val bz = vm.state.bzSeries24h.map { UiGraphPoint(xLabel = label(it.t), value = it.v) }

    fun bounds(values: List<Double>, pad: Double): Pair<Double, Double> {
        if (values.isEmpty()) return 0.0 to 1.0
        val min = values.minOrNull() ?: 0.0
        val max = values.maxOrNull() ?: 1.0
        if (min == max) return (min - 1.0) to (max + 1.0)
        return (min - pad) to (max + pad)
    }

    val (kpMin, kpMax) = bounds(kp.map { it.value }, 0.5)
    val (spMin, spMax) = bounds(sp.map { it.value }, 30.0)
    val (bzMin, bzMax) = bounds(bz.map { it.value }, 1.0)

    return listOf(
        UiGraphSeries(
            title = "Kp",
            unit = "",
            points = kp,
            minY = kpMin.coerceAtMost(0.0),
            maxY = kpMax.coerceAtLeast(9.0),
            gridStepY = 1.0,
            dangerBelow = null,
            dangerAbove = 7.0
        ),
        UiGraphSeries(
            title = "Speed",
            unit = "км/с",
            points = sp,
            minY = spMin,
            maxY = spMax,
            gridStepY = 100.0,
            dangerBelow = null,
            dangerAbove = 750.0
        ),
        UiGraphSeries(
            title = "Bz",
            unit = "нТл",
            points = bz,
            minY = bzMin,
            maxY = bzMax,
            gridStepY = 2.0,
            dangerBelow = -6.0,
            dangerAbove = null
        )
    )
}
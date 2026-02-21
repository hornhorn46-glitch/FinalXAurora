package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.SpaceWeatherViewModelFactory
import com.example.wittyapp.ui.settings.SettingsStore
import com.example.wittyapp.ui.strings.rememberAppStrings
import com.example.wittyapp.ui.topbar.ModeToggleRuneButton
import com.example.wittyapp.ui.screens.GraphSeries
import com.example.wittyapp.ui.screens.GraphsMode
import com.example.wittyapp.ui.screens.GraphsScreen
import com.example.wittyapp.ui.screens.NowScreen
import com.example.wittyapp.ui.screens.SunScreen
import com.example.wittyapp.ui.theme.CosmosTheme
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val api = remember { SpaceWeatherApi() }
            val vm: SpaceWeatherViewModel = viewModel(factory = SpaceWeatherViewModelFactory(api))

            val settings = remember { SettingsStore(applicationContext) }
            var mode by remember { mutableStateOf(AppMode.EARTH) }
            var screen by remember { mutableStateOf<Screen>(Screen.Now) }

            val language = remember { settings.getLanguage() }
            val strings = rememberAppStrings(language)

            CosmosTheme(mode = mode, auroraScore = vm.state.auroraScore) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(if (mode == AppMode.EARTH) strings.earth else strings.sun)
                            },
                            navigationIcon = {
                                if (screen != Screen.Now) {
                                    IconButton(onClick = { screen = Screen.Now }) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                                    }
                                }
                            },
                            actions = {
                                ModeToggleRuneButton(
                                    mode = mode,
                                    onToggle = {
                                        mode = if (mode == AppMode.EARTH) AppMode.SUN else AppMode.EARTH
                                        screen = Screen.Now
                                    }
                                )
                            }
                        )
                    }
                ) { padding ->

                    when (mode) {
                        AppMode.EARTH -> {
                            when (screen) {
                                Screen.Now -> NowScreen(
                                    vm = vm,
                                    mode = mode,
                                    strings = strings,
                                    contentPadding = padding,
                                    onOpenGraphs = { screen = Screen.Graphs },
                                    onOpenEvents = { /* пока не используем */ }
                                )

                                Screen.Graphs -> GraphsScreen(
                                    title = strings.graphs,
                                    series = buildGraphSeries(vm),
                                    mode = GraphsMode.EARTH,
                                    strings = strings,
                                    onClose = { screen = Screen.Now }
                                )
                            }
                        }

                        AppMode.SUN -> {
                            // солнце отдельно — пока без графиков/ивентов
                            SunScreen(contentPadding = padding)
                        }
                    }
                }
            }
        }
    }
}

private enum class Screen { Now, Graphs }

/** Earth graphs (24h) built from ViewModel state. */
private fun buildGraphSeries(vm: SpaceWeatherViewModel): List<GraphSeries> {
    val s = vm.state

    fun <T> minMax(values: List<Double>, fallbackMin: Double, fallbackMax: Double): Pair<Double, Double> {
        if (values.isEmpty()) return fallbackMin to fallbackMax
        val mn = values.minOrNull() ?: fallbackMin
        val mx = values.maxOrNull() ?: fallbackMax
        // чуть расширяем, чтобы линия не упиралась в рамки
        val pad = (mx - mn).coerceAtLeast(1.0) * 0.08
        return (mn - pad) to (mx + pad)
    }

    val kpVals = s.kpSeries24h.map { it.value }
    val spVals = s.speedSeries24h.map { it.value }
    val bzVals = s.bzSeries24h.map { it.value }

    val (kpMin, kpMax) = minMax(kpVals, 0.0, 9.0)
    val (spMin, spMax) = minMax(spVals, 250.0, 1200.0)
    val (bzMin, bzMax) = minMax(bzVals, -20.0, 20.0)

    return listOf(
        GraphSeries(
            title = "Kp",
            unit = "",
            points = s.kpSeries24h,
            minY = max(0.0, kpMin),
            maxY = min(9.0, kpMax),
            gridStepY = 1.0,
            dangerAbove = 5.0
        ),
        GraphSeries(
            title = "Speed",
            unit = "км/с",
            points = s.speedSeries24h,
            minY = spMin,
            maxY = spMax,
            gridStepY = 100.0,
            dangerAbove = 600.0
        ),
        GraphSeries(
            title = "Bz",
            unit = "нТл",
            points = s.bzSeries24h,
            minY = bzMin,
            maxY = bzMax,
            gridStepY = 2.0,
            dangerBelow = -2.0
        )
    )
}

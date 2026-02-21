package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.SpaceWeatherViewModelFactory
import com.example.wittyapp.ui.screens.*
import com.example.wittyapp.ui.settings.SettingsStore
import com.example.wittyapp.ui.strings.Language
import com.example.wittyapp.ui.strings.rememberAppStrings
import com.example.wittyapp.ui.theme.CosmosTheme
import com.example.wittyapp.ui.topbar.ModeToggleRuneButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed class Screen {
    data object Now : Screen()
    data object Graphs : Screen()
    data object Events : Screen()
    data object Sun : Screen()
    data object Settings : Screen()
    data object Tutorial : Screen()
    data class FullImage(val url: String, val title: String) : Screen()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val api = remember { SpaceWeatherApi() }
            val vm: SpaceWeatherViewModel = viewModel(factory = SpaceWeatherViewModelFactory(api))

            val settings = remember { SettingsStore(this) }

            var mode by remember { mutableStateOf(settings.loadMode()) }
            var language by remember { mutableStateOf(settings.loadLanguage()) }

            val strings = rememberAppStrings(language)

            // simple stack navigation
            val stack = remember { mutableStateListOf<Screen>(Screen.Now) }
            val current = stack.last()

            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            var lastBackPressMs by remember { mutableLongStateOf(0L) }

            fun push(s: Screen) { stack.add(s) }
            fun pop(): Boolean {
                return if (stack.size > 1) {
                    stack.removeAt(stack.lastIndex); true
                } else false
            }

            BackHandler {
                // close full screens / dialogs via pop; exit app by double back on root
                if (!pop()) {
                    val now = System.currentTimeMillis()
                    if (now - lastBackPressMs < 1200L) {
                        finish()
                    } else {
                        lastBackPressMs = now
                        scope.launch { snackbarHostState.showSnackbar(strings.pressBackAgainToExit) }
                    }
                }
            }

            CosmosTheme(mode = mode, auroraScore = vm.state.auroraScore) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = { Text(strings.appTitle) },
                            actions = {
                                IconButton(onClick = { push(Screen.Tutorial) }) {
                                    Icon(Icons.Outlined.MenuBook, contentDescription = strings.tutorialTitle)
                                }

                                ModeToggleRuneButton(
                                    mode = mode,
                                    onToggle = {
                                        mode = if (mode == AppMode.EARTH) AppMode.SUN else AppMode.EARTH
                                        settings.saveMode(mode)
                                        // при переключении режима можно автоперекинуть на нужный главный экран
                                        if (mode == AppMode.SUN) {
                                            // если сейчас земной экран — покажем Sun
                                            if (current == Screen.Now || current == Screen.Graphs || current == Screen.Events) {
                                                stack.clear(); stack.add(Screen.Sun)
                                            }
                                        } else {
                                            // обратно на Now
                                            if (current == Screen.Sun) {
                                                stack.clear(); stack.add(Screen.Now)
                                            }
                                        }
                                    }
                                )

                                IconButton(onClick = { push(Screen.Settings) }) {
                                    Icon(Icons.Outlined.Settings, contentDescription = strings.settingsTitle)
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            // Земные вкладки
                            NavigationBarItem(
                                selected = current == Screen.Now,
                                onClick = {
                                    stack.clear()
                                    stack.add(if (mode == AppMode.SUN) Screen.Sun else Screen.Now)
                                },
                                icon = {
                                    Icon(
                                        if (mode == AppMode.SUN) Icons.Outlined.WbSunny else Icons.Outlined.Public,
                                        contentDescription = null
                                    )
                                },
                                label = { Text(if (mode == AppMode.SUN) strings.sun else strings.earth) }
                            )
                            if (mode == AppMode.EARTH) {
                                NavigationBarItem(
                                    selected = current == Screen.Graphs,
                                    onClick = { stack.clear(); stack.add(Screen.Graphs) },
                                    icon = { Icon(Icons.Outlined.Public, contentDescription = null) },
                                    label = { Text(strings.graphs) }
                                )
                                NavigationBarItem(
                                    selected = current == Screen.Events,
                                    onClick = { stack.clear(); stack.add(Screen.Events) },
                                    icon = { Icon(Icons.Outlined.Public, contentDescription = null) },
                                    label = { Text(strings.events) }
                                )
                            }
                            NavigationBarItem(
                                selected = current == Screen.Settings,
                                onClick = { push(Screen.Settings) },
                                icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                                label = { Text(strings.settingsTitle) }
                            )
                        }
                    }
                ) { padding ->

                    // content routing
                    when (current) {
                        Screen.Now -> NowScreen(
                            vm = vm,
                            mode = mode,
                            strings = strings,
                            contentPadding = padding,
                            onOpenGraphs = { push(Screen.Graphs) },
                            onOpenEvents = { push(Screen.Events) }
                        )

                        Screen.Graphs -> {
                            val uiSeries = build24hUiSeries(vm)
                            GraphsScreen(
                                title = strings.graphs24hTitle,
                                series = uiSeries,
                                mode = GraphsMode.EARTH,
                                strings = strings,
                                onClose = { pop() }
                            )
                        }

                        Screen.Events -> EventsScreen(
                            vm = vm,
                            strings = strings,
                            contentPadding = padding,
                            onClose = { pop() }
                        )

                        Screen.Sun -> SunScreen(
                            strings = strings,
                            contentPadding = padding,
                            onOpenFull = { url, title -> push(Screen.FullImage(url, title)) },
                            onClose = { /* root screen: back handled */ }
                        )

                        Screen.Settings -> SettingsScreen(
                            strings = strings,
                            currentLanguage = language,
                            onSetLanguage = {
                                language = it
                                settings.saveLanguage(it)
                            },
                            onClose = { pop() }
                        )

                        Screen.Tutorial -> TutorialScreen(
                            strings = strings,
                            onClose = { pop() }
                        )

                        is Screen.FullImage -> FullscreenWebImageScreen(
                            title = current.title,
                            url = current.url,
                            onClose = { pop() }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Собираем 3 графика на 24ч, используя ТОЛЬКО domain.GraphPoint из vm.state.
 * Это убирает конфликт типов (domain.GraphPoint vs ui.screens.GraphPoint).
 */
private fun build24hUiSeries(vm: SpaceWeatherViewModel): List<com.example.wittyapp.ui.screens.GraphSeries> {
    val s = vm.state

    fun minMax(values: List<Double>, fallbackMin: Double, fallbackMax: Double): Pair<Double, Double> {
        if (values.isEmpty()) return fallbackMin to fallbackMax
        val min = values.minOrNull() ?: fallbackMin
        val max = values.maxOrNull() ?: fallbackMax
        // запас
        val pad = (max - min).let { if (it <= 0.0) 1.0 else it * 0.15 }
        return (min - pad) to (max + pad)
    }

    val kpVals = s.kpSeries24h.map { it.value }
    val spVals = s.speedSeries24h.map { it.value }
    val bzVals = s.bzSeries24h.map { it.value }

    val (kpMin, kpMax) = minMax(kpVals, 0.0, 9.0)
    val (spMin, spMax) = minMax(spVals, 250.0, 1200.0)
    val (bzMin, bzMax) = minMax(bzVals, -20.0, 20.0)

    return listOf(
        com.example.wittyapp.ui.screens.GraphSeries(
            title = "Kp",
            unit = "",
            points = s.kpSeries24h,
            minY = kpMin,
            maxY = kpMax,
            gridStepY = 1.0,
            dangerBelow = null,
            dangerAbove = 7.0
        ),
        com.example.wittyapp.ui.screens.GraphSeries(
            title = "Speed",
            unit = "км/с",
            points = s.speedSeries24h,
            minY = spMin,
            maxY = spMax,
            gridStepY = 100.0,
            dangerBelow = null,
            dangerAbove = 750.0
        ),
        com.example.wittyapp.ui.screens.GraphSeries(
            title = "Bz",
            unit = "нТл",
            points = s.bzSeries24h,
            minY = bzMin,
            maxY = bzMax,
            gridStepY = 2.0,
            dangerBelow = -6.0,
            dangerAbove = null
        )
    )
}
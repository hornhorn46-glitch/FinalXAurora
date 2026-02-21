package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.screens.*
import com.example.wittyapp.ui.strings.AppLanguage
import com.example.wittyapp.ui.strings.rememberAppStrings
import com.example.wittyapp.ui.theme.CosmosTheme
import java.time.Instant

private sealed class Screen {
    data object Now : Screen()
    data object Graphs : Screen()
    data object Events : Screen()
    data object Sun : Screen()
    data object Settings : Screen()
    data object Tutorial : Screen()
    data class FullImage(val title: String, val url: String) : Screen()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val api = remember { SpaceWeatherApi() }
            val vm: SpaceWeatherViewModel = viewModel(factory = SpaceWeatherViewModelFactory(api))

            // язык (позже можно хранить в DataStore, но сейчас просто держим в state)
            var language by remember { mutableStateOf(AppLanguage.RU) }
            val strings = rememberAppStrings(language)

            // режим Земля/Солнце
            var mode by remember { mutableStateOf(AppMode.EARTH) }

            // простая навигация без navigation-compose
            val backStack = remember { mutableStateListOf<Screen>(Screen.Now) }
            val current = backStack.last()

            // двойной back для выхода
            var lastBackAt by remember { mutableStateOf<Instant?>(null) }
            var showExitSnack by remember { mutableStateOf(false) }

            fun push(s: Screen) { backStack.add(s) }
            fun pop(): Boolean {
                return if (backStack.size > 1) {
                    backStack.removeAt(backStack.lastIndex)
                    true
                } else false
            }

            BackHandler {
                val popped = pop()
                if (!popped) {
                    val now = Instant.now()
                    val last = lastBackAt
                    if (last != null && now.epochSecond - last.epochSecond <= 2) {
                        finish()
                    } else {
                        lastBackAt = now
                        showExitSnack = true
                    }
                }
            }

            CosmosTheme(mode = mode, auroraScore = vm.state.auroraScore) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

                    val snackbarHostState = remember { SnackbarHostState() }
                    LaunchedEffect(showExitSnack) {
                        if (showExitSnack) {
                            snackbarHostState.showSnackbar(strings.pressBackAgainToExit)
                            showExitSnack = false
                        }
                    }

                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            TopAppBar(
                                title = { Text(strings.appTitle) },
                                actions = {
                                    // Tutorial
                                    IconButton(onClick = { push(Screen.Tutorial) }) {
                                        Icon(Icons.Outlined.MenuBook, contentDescription = "Tutorial")
                                    }

                                    // Toggle Earth/Sun
                                    ModeToggleRuneButton(
                                        mode = mode,
                                        onToggle = { mode = if (mode == AppMode.EARTH) AppMode.SUN else AppMode.EARTH }
                                    )

                                    // Settings
                                    IconButton(onClick = { push(Screen.Settings) }) {
                                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = current is Screen.Now,
                                    onClick = { backStack.clear(); backStack.add(Screen.Now) },
                                    icon = { Icon(Icons.Outlined.Home, null) },
                                    label = { Text(strings.now) }
                                )
                                NavigationBarItem(
                                    selected = current is Screen.Graphs,
                                    onClick = { backStack.clear(); backStack.add(Screen.Graphs) },
                                    icon = { Icon(Icons.Outlined.ShowChart, null) },
                                    label = { Text(strings.graphs) }
                                )
                                NavigationBarItem(
                                    selected = current is Screen.Events,
                                    onClick = { backStack.clear(); backStack.add(Screen.Events) },
                                    icon = { Icon(Icons.Outlined.Notifications, null) },
                                    label = { Text(strings.events) }
                                )
                                NavigationBarItem(
                                    selected = current is Screen.Sun,
                                    onClick = { backStack.clear(); backStack.add(Screen.Sun) },
                                    icon = { Icon(Icons.Outlined.WbSunny, null) },
                                    label = { Text(strings.sun) }
                                )
                            }
                        }
                    ) { padding ->
                        val contentPadding = PaddingValues(
                            start = 0.dp,
                            end = 0.dp,
                            top = padding.calculateTopPadding(),
                            bottom = padding.calculateBottomPadding()
                        )

                        when (val s = current) {
                            is Screen.Now -> NowScreen(
                                vm = vm,
                                mode = mode,
                                strings = strings,
                                contentPadding = contentPadding,
                                onOpenGraphs = { push(Screen.Graphs) },
                                onOpenEvents = { push(Screen.Events) }
                            )

                            is Screen.Graphs -> {
                                val series = build24hSeries(vm, strings)
                                GraphsScreen(
                                    title = strings.graphs24hTitle,
                                    series = series,
                                    strings = strings,
                                    onClose = { pop() }
                                )
                            }

                            is Screen.Events -> EventsScreen(
                                strings = strings,
                                contentPadding = contentPadding
                            )

                            is Screen.Sun -> SunScreen(
                                strings = strings,
                                contentPadding = contentPadding,
                                onOpenFull = { title, url -> push(Screen.FullImage(title, url)) }
                            )

                            is Screen.Settings -> SettingsScreen(
                                strings = strings,
                                contentPadding = contentPadding,
                                currentLanguage = language,
                                onSetLanguage = { language = it },
                                onClose = { pop() }
                            )

                            is Screen.Tutorial -> TutorialScreen(
                                strings = strings,
                                contentPadding = contentPadding,
                                onClose = { pop() }
                            )

                            is Screen.FullImage -> FullImageScreen(
                                title = s.title,
                                url = s.url,
                                strings = strings,
                                onClose = { pop() }
                            )
                        }
                    }
                }
            }
        }
    }
}
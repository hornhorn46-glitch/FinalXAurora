package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.SpaceWeatherViewModelFactory
import com.example.wittyapp.ui.screens.*
import com.example.wittyapp.ui.settings.SettingsStore
import com.example.wittyapp.ui.strings.Language
import com.example.wittyapp.ui.strings.stringsFor
import com.example.wittyapp.ui.theme.CosmosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppRoot() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRoot() {
    val context = LocalContext.current
    val settings = remember { SettingsStore(context) }
    val api = remember { SpaceWeatherApi() }

    val vm: SpaceWeatherViewModel = viewModel(factory = SpaceWeatherViewModelFactory(api))

    var mode by remember { mutableStateOf(settings.loadMode()) }
    var language by remember { mutableStateOf(settings.loadLanguage()) }

    val strings = remember(language) { stringsFor(language) }

    // простая навигация без navigation-compose (как вы и хотели)
    var screen by remember { mutableStateOf<Screen>(Screen.Now) }
    var fullImage by remember { mutableStateOf<FullImageRequest?>(null) }

    CosmosTheme(mode = mode, auroraScore = vm.state.auroraScore) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    // если у тебя уже есть своя TopBar — можешь вернуть её,
                    // главное чтобы она принимала (mode, onToggleMode, onOpenTutorial, onOpenSettings)
                    AppTopBar(
                        mode = mode,
                        onToggleMode = {
                            mode = if (mode == AppMode.EARTH) AppMode.SUN else AppMode.EARTH
                            settings.saveMode(mode)
                        },
                        onOpenTutorial = { screen = Screen.Tutorial },
                        onOpenSettings = { screen = Screen.Settings }
                    )
                },
                bottomBar = {
                    AppBottomBar(
                        current = screen,
                        onGoNow = { screen = Screen.Now },
                        onGoGraphs = { screen = Screen.Graphs },
                        onGoEvents = { screen = Screen.Events }
                    )
                }
            ) { padding ->
                when {
                    fullImage != null -> {
                        FullImageScreen(
                            title = fullImage!!.title,
                            url = fullImage!!.url,
                            strings = strings,
                            onClose = { fullImage = null }
                        )
                    }

                    mode == AppMode.SUN -> {
                        // экран Солнца (если у тебя SunScreen уже есть — оставь его сигнатуру и подстрой вызов)
                        SunScreen(
                            strings = strings,
                            contentPadding = padding,
                            onOpenFull = { title, url -> fullImage = FullImageRequest(title, url) }
                        )
                    }

                    else -> {
                        when (screen) {
                            Screen.Now -> NowScreen(
                                vm = vm,
                                mode = mode,
                                strings = strings,
                                contentPadding = padding,
                                onOpenGraphs = { screen = Screen.Graphs },
                                onOpenEvents = { screen = Screen.Events }
                            )

                            Screen.Graphs -> GraphsHostScreen(
                                vm = vm,
                                strings = strings,
                                contentPadding = padding,
                                onClose = { screen = Screen.Now }
                            )

                            Screen.Events -> EventsScreen(
                                vm = vm,
                                strings = strings,
                                contentPadding = padding
                            )

                            Screen.Settings -> SettingsScreen(
                                strings = strings,
                                contentPadding = padding,
                                currentLanguage = language,
                                onSetLanguage = {
                                    language = it
                                    settings.saveLanguage(it)
                                }
                            )

                            Screen.Tutorial -> TutorialScreen(
                                strings = strings,
                                contentPadding = padding,
                                onClose = { screen = Screen.Now }
                            )
                        }
                    }
                }
            }
        }
    }
}

private sealed interface Screen {
    data object Now : Screen
    data object Graphs : Screen
    data object Events : Screen
    data object Settings : Screen
    data object Tutorial : Screen
}

private data class FullImageRequest(val title: String, val url: String)
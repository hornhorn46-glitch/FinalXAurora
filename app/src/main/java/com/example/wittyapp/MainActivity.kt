package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.screens.EventsScreen
import com.example.wittyapp.ui.screens.FullscreenWebImageScreen
import com.example.wittyapp.ui.screens.GraphsMode
import com.example.wittyapp.ui.screens.GraphsScreen
import com.example.wittyapp.ui.screens.NowScreen
import com.example.wittyapp.ui.screens.SettingsScreen
import com.example.wittyapp.ui.screens.SunScreen
import com.example.wittyapp.ui.screens.TutorialScreen
import com.example.wittyapp.ui.settings.SettingsStore
import com.example.wittyapp.ui.strings.AppLanguage
import com.example.wittyapp.ui.strings.AppStrings
import com.example.wittyapp.ui.strings.rememberAppStrings
import com.example.wittyapp.ui.theme.CosmosTheme
import com.example.wittyapp.ui.topbar.ModeToggleRuneButton
import kotlinx.coroutines.launch

enum class AppMode { EARTH, SUN }

sealed class Screen {
    data object Now : Screen()
    data object Graphs : Screen()
    data object Events : Screen()
    data object Sun : Screen()
    data object Settings : Screen()
    data object Tutorial : Screen()
    data class Fullscreen(val title: String, val url: String) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val ctx = LocalContext.current
            val store = remember { SettingsStore(ctx) }

            var mode by remember { mutableStateOf(store.getMode()) }
            var lang by remember { mutableStateOf(store.getLanguage()) }
            val strings = rememberAppStrings(lang)

            val vm: SpaceWeatherViewModel = viewModel(
                factory = SimpleFactory { SpaceWeatherViewModel(SpaceWeatherApi()) }
            )

            CosmosTheme(mode = mode, auroraScore = vm.state.auroraScore) {
                AppScaffold(
                    vm = vm,
                    mode = mode,
                    onToggleMode = {
                        mode = if (mode == AppMode.EARTH) AppMode.SUN else AppMode.EARTH
                        store.setMode(mode)
                    },
                    strings = strings,
                    currentLanguage = lang,
                    onSetLanguage = {
                        lang = it
                        store.setLanguage(it)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(
    vm: SpaceWeatherViewModel,
    mode: AppMode,
    onToggleMode: () -> Unit,
    strings: AppStrings,
    currentLanguage: AppLanguage,
    onSetLanguage: (AppLanguage) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    val backStack = remember { mutableStateListOf<Screen>(Screen.Now) }
    fun push(s: Screen) { backStack.add(s) }
    fun pop(): Boolean = if (backStack.size > 1) { backStack.removeLast(); true } else false
    val current = backStack.last()

    // double-back-to-exit
    var backArmed by remember { mutableStateOf(false) }
    LaunchedEffect(current) { backArmed = false }

    BackHandler {
        if (pop()) return@BackHandler
        if (!backArmed) {
            backArmed = true
            scope.launch { snack.showSnackbar(strings.exitHint) }
        } else {
            (LocalContext.current as? ComponentActivity)?.finish()
        }
    }

    val showTopBar = current !is Screen.Fullscreen

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = { push(Screen.Tutorial) }) {
                            Icon(Icons.Default.MenuBook, contentDescription = strings.tutorial)
                        }
                        ModeToggleRuneButton(mode = mode, onToggle = onToggleMode)
                        IconButton(onClick = { push(Screen.Settings) }) {
                            Icon(Icons.Default.Settings, contentDescription = strings.settings)
                        }
                    }
                )
            }
        }
    ) { padding ->
        when (current) {
            Screen.Now -> NowScreen(
                vm = vm,
                mode = mode,
                strings = strings,
                contentPadding = padding,
                onOpenGraphs = { push(Screen.Graphs) },
                onOpenEvents = { push(Screen.Events) }
            )

            Screen.Graphs -> GraphsScreen(
                title = strings.graphsTitle24h,
                series = vm.buildGraphSeries(),
                mode = if (mode == AppMode.EARTH) GraphsMode.EARTH else GraphsMode.SUN,
                strings = strings,
                onClose = { pop() }
            )

            Screen.Events -> EventsScreen(vm = vm, strings = strings, contentPadding = padding, onClose = { pop() })

            Screen.Sun -> SunScreen(
                strings = strings,
                contentPadding = padding,
                onOpenFull = { title, url -> push(Screen.Fullscreen(title, url)) }
            )

            Screen.Settings -> SettingsScreen(
                strings = strings,
                currentLanguage = currentLanguage,
                onSetLanguage = onSetLanguage,
                contentPadding = padding,
                onClose = { pop() }
            )

            Screen.Tutorial -> TutorialScreen(strings = strings, contentPadding = padding, onClose = { pop() })

            is Screen.Fullscreen -> {
                val fs = current as Screen.Fullscreen
                FullscreenWebImageScreen(title = fs.title, url = fs.url, onClose = { pop() })
            }
        }
    }
}

/** Tiny factory for `viewModel(...)` without adding extra DI. */
private class SimpleFactory<T : ViewModel>(val create: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
}

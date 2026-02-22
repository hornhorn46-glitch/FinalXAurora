package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.SpaceWeatherViewModelFactory
import com.example.wittyapp.ui.screens.*
import com.example.wittyapp.ui.settings.SettingsStore
import com.example.wittyapp.ui.strings.AppLanguage
import com.example.wittyapp.ui.strings.AppStrings
import com.example.wittyapp.ui.strings.rememberAppStrings
import com.example.wittyapp.ui.theme.CosmosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppRoot() }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRoot() {
    val context = LocalContext.current
    val settings = remember { SettingsStore(context) }

    val api = remember { SpaceWeatherApi() }
    val vm: SpaceWeatherViewModel = viewModel(factory = SpaceWeatherViewModelFactory(api))

    var mode by rememberSaveable { mutableStateOf(settings.loadMode()) }
    var language by rememberSaveable { mutableStateOf(settings.loadLanguage()) }

    // ВАЖНО: используем то, что уже есть в твоём проекте (а не stringsFor)
    val strings: AppStrings = rememberAppStrings(language)

    var screen by rememberSaveable { mutableStateOf<Screen>(Screen.Now) }
    var fullImage by rememberSaveable { mutableStateOf<FullImageRequest?>(null) }

    // Back: если открыт full image → закрываем; иначе по стеку; выход — двойной Back (можно потом вернуть)
    var backArmed by remember { mutableStateOf(false) }
    LaunchedEffect(backArmed) {
        if (backArmed) {
            kotlinx.coroutines.delay(1400)
            backArmed = false
        }
    }

    BackHandler(enabled = true) {
        when {
            fullImage != null -> fullImage = null
            screen != Screen.Now -> screen = Screen.Now
            else -> {
                if (backArmed) (context as? ComponentActivity)?.finish()
                else backArmed = true
            }
        }
    }

    CosmosTheme(mode = mode, auroraScore = vm.state.auroraScore) {
        Scaffold(
            topBar = {
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
                // В режиме SUN можно оставить нижнюю навигацию (или скрыть — на твой вкус)
                AppBottomBar(
                    mode = mode,
                    current = screen,
                    strings = strings,
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
                        contentPadding = padding,
                        onClose = { fullImage = null }
                    )
                }

                mode == AppMode.SUN -> {
                    // SUN экран: сигнатура именно такая была у тебя в логах: нужен strings + contentPadding + onOpenFull
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
                            },
                            onClose = { screen = Screen.Now }
                        )

                        Screen.Tutorial -> TutorialScreen(
                            strings = strings,
                            contentPadding = padding,
                            onClose = { screen = Screen.Now }
                        )
                    }
                }
            }

            // маленький snackbar на “нажми ещё раз”
            if (backArmed && screen == Screen.Now && fullImage == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    Surface(
                        tonalElevation = 4.dp,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.padding(bottom = padding.calculateBottomPadding() + 16.dp)
                    ) {
                        Text(
                            text = "Нажми назад ещё раз, чтобы выйти",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    mode: AppMode,
    onToggleMode: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenSettings: () -> Unit
) {
    TopAppBar(
        title = { Text("FinalXAurora") },
        actions = {
            IconButton(onClick = onOpenTutorial) {
                Icon(Icons.Default.MenuBook, contentDescription = "Tutorial")
            }
            // круглая кнопка режима (пока без “руны”; руны можно дорисовать позже)
            FilledTonalIconButton(onClick = onToggleMode) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Mode")
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    )
}

@Composable
private fun AppBottomBar(
    mode: AppMode,
    current: Screen,
    strings: AppStrings,
    onGoNow: () -> Unit,
    onGoGraphs: () -> Unit,
    onGoEvents: () -> Unit
) {
    // В SUN режиме можно показывать только одну “Солнце” вкладку, но пока оставим как есть.
    NavigationBar {
        NavigationBarItem(
            selected = current == Screen.Now,
            onClick = onGoNow,
            icon = {},
            label = { Text(strings.now) }
        )
        NavigationBarItem(
            selected = current == Screen.Graphs,
            onClick = onGoGraphs,
            icon = {},
            label = { Text(strings.graphs) }
        )
        NavigationBarItem(
            selected = current == Screen.Events,
            onClick = onGoEvents,
            icon = {},
            label = { Text(strings.events) }
        )
    }
}

/**
 * Хост-обёртка для графиков: делает UiGraphSeries и вызывает твой GraphsScreen.
 * Никаких GraphSeriesHelpers/GraphSeries здесь не нужно — убираем лишние зависимости.
 */
@Composable
private fun GraphsHostScreen(
    vm: SpaceWeatherViewModel,
    strings: AppStrings,
    contentPadding: PaddingValues,
    onClose: () -> Unit
) {
    val s = vm.state

    val series = remember(s.kpSeries24h, s.speedSeries24h, s.bzSeries24h) {
        listOf(
            UiGraphSeries(
                title = "Kp",
                unit = "",
                points = s.kpSeries24h.map { UiGraphPoint(it.xLabel, it.value) },
                minY = 0.0,
                maxY = 9.0,
                gridStepY = 1.0,
                dangerBelow = null,
                dangerAbove = 7.0
            ),
            UiGraphSeries(
                title = "Speed",
                unit = "км/с",
                points = s.speedSeries24h.map { UiGraphPoint(it.xLabel, it.value) },
                minY = 200.0,
                maxY = 1200.0,
                gridStepY = 100.0,
                dangerBelow = null,
                dangerAbove = 750.0
            ),
            UiGraphSeries(
                title = "Bz",
                unit = "нТл",
                points = s.bzSeries24h.map { UiGraphPoint(it.xLabel, it.value) },
                minY = -20.0,
                maxY = 20.0,
                gridStepY = 2.0,
                dangerBelow = -6.0,
                dangerAbove = null
            )
        )
    }

    Box(Modifier.fillMaxSize().padding(contentPadding)) {
        GraphsScreen(
            title = strings.graphs,
            series = series,
            strings = strings,
            onClose = onClose
        )
    }
}

/**
 * Full image. Если у тебя уже есть экран, можешь удалить этот блок и использовать свой.
 * Здесь это просто заглушка: показываем URL текстом, чтобы не тащить Coil.
 */
@Composable
private fun FullImageScreen(
    title: String,
    url: String,
    strings: AppStrings,
    contentPadding: PaddingValues,
    onClose: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = strings.close,
                modifier = Modifier
                    .clickable { onClose() }
                    .padding(8.dp)
            )
        }

        Surface(tonalElevation = 2.dp, shape = MaterialTheme.shapes.large) {
            Column(Modifier.fillMaxWidth().padding(14.dp)) {
                Text("Full screen preview:")
                Spacer(Modifier.height(8.dp))
                Text(url, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Text("Позже подключим нормальную загрузку картинки/анимации (Coil/WebView).")
            }
        }
    }
}
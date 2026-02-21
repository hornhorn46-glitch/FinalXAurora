package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.screens.GraphsMode
import com.example.wittyapp.ui.screens.GraphsScreen
import com.example.wittyapp.ui.screens.buildUiSeries
import com.example.wittyapp.ui.screens.NowScreen
import com.example.wittyapp.ui.strings.AppStrings
import com.example.wittyapp.ui.theme.CosmosTheme

private enum class Screen { NOW, GRAPHS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppRoot()
        }
    }
}

@Composable
private fun AppRoot() {
    val api = remember { SpaceWeatherApi() }

    val vm: SpaceWeatherViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SpaceWeatherViewModel(api) as T
        }
    })

    var mode by remember { mutableStateOf(AppMode.EARTH) }
    var screen by remember { mutableStateOf(Screen.NOW) }

    val strings = remember { AppStrings.ru() }

    CosmosTheme(
        mode = mode,
        auroraScore = vm.state.auroraScore
    ) {
        Scaffold { padding ->
            when (screen) {
                Screen.NOW -> NowScreen(
                    vm = vm,
                    mode = mode,
                    strings = strings,
                    contentPadding = padding,
                    onOpenGraphs = { screen = Screen.GRAPHS },
                    onOpenEvents = { /* пока пусто */ }
                )

                Screen.GRAPHS -> {
                    val series = remember(vm.state.kpSeries24h, vm.state.speedSeries24h, vm.state.bzSeries24h) {
                        listOf(
                            buildUiSeries(
                                title = "Kp",
                                unit = "",
                                points = vm.state.kpSeries24h,
                                minY = 0.0,
                                maxY = 9.0,
                                gridStep = 1.0
                            ),
                            buildUiSeries(
                                title = "Speed",
                                unit = "км/с",
                                points = vm.state.speedSeries24h,
                                minY = 200.0,
                                maxY = 1200.0,
                                gridStep = 100.0
                            ),
                            buildUiSeries(
                                title = "Bz",
                                unit = "нТл",
                                points = vm.state.bzSeries24h,
                                minY = -20.0,
                                maxY = 20.0,
                                gridStep = 5.0
                            )
                        )
                    }

                    GraphsScreen(
                        title = "Графики 24ч",
                        series = series,
                        mode = if (mode == AppMode.EARTH) GraphsMode.EARTH else GraphsMode.SUN,
                        strings = strings,
                        onClose = { screen = Screen.NOW }
                    )
                }
            }
        }
    }
}
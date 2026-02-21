package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.screens.NowScreen
import com.example.wittyapp.ui.strings.AppLanguage
import com.example.wittyapp.ui.strings.rememberAppStrings
import com.example.wittyapp.ui.theme.CosmosTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Если SettingsStore сейчас ломает сборку — временно фиксируем язык/режим так:
            val mode = AppMode.EARTH
            val strings = rememberAppStrings(AppLanguage.RU)

            val api = remember { SpaceWeatherApi() }
            val vm: SpaceWeatherViewModel = viewModel(
                factory = SimpleFactory { SpaceWeatherViewModel(api) }
            )

            CosmosTheme(mode = mode, auroraScore = vm.state.auroraScore) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    NowScreen(
                        vm = vm,
                        mode = mode,
                        strings = strings,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(),
                        onOpenGraphs = { /* TODO */ },
                        onOpenEvents = { /* TODO */ }
                    )
                }
            }
        }
    }
}

private class SimpleFactory<T : ViewModel>(
    private val make: () -> T
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = make() as T
}
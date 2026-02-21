package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

            var language by remember { mutableStateOf(AppLanguage.RU) }
            val strings = rememberAppStrings(language)

            val vm: SpaceWeatherViewModel = viewModel(
                factory = SpaceWeatherViewModelFactory(
                    SpaceWeatherApi()
                )
            )

            CosmosTheme(auroraScore = vm.state.auroraScore) {

                Scaffold { padding ->

                    NowScreen(
                        vm = vm,
                        mode = AppMode.EARTH,
                        strings = strings,
                        contentPadding = padding,
                        onOpenGraphs = {},
                        onOpenEvents = {}
                    )
                }
            }
        }
    }
}
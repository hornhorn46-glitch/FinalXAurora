package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.screens.NowScreen
import com.example.wittyapp.ui.screens.SunScreen
import com.example.wittyapp.ui.strings.rememberAppStrings
import com.example.wittyapp.ui.theme.CosmosTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val api = remember { SpaceWeatherApi() }

            val vm: SpaceWeatherViewModel = viewModel(
                factory = SpaceWeatherViewModelFactory(api)
            )

            var mode by remember { mutableStateOf(AppMode.EARTH) }

            val strings = rememberAppStrings()

            CosmosTheme(
                mode = mode,
                auroraScore = vm.state.auroraScore
            ) {

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    if (mode == AppMode.EARTH)
                                        strings.earth
                                    else
                                        strings.sun
                                )
                            },
                            actions = {
                                TextButton(
                                    onClick = {
                                        mode = if (mode == AppMode.EARTH)
                                            AppMode.SUN
                                        else
                                            AppMode.EARTH
                                    }
                                ) {
                                    Text(
                                        if (mode == AppMode.EARTH)
                                            strings.sun
                                        else
                                            strings.earth
                                    )
                                }
                            }
                        )
                    }
                ) { padding ->

                    when (mode) {

                        AppMode.EARTH -> {
                            NowScreen(
                                vm = vm,
                                mode = mode,
                                strings = strings,
                                contentPadding = padding,
                                onOpenGraphs = { },
                                onOpenEvents = { }
                            )
                        }

                        AppMode.SUN -> {
                            SunScreen(
                                strings = strings,
                                contentPadding = padding,
                                onOpenFull = { }
                            )
                        }
                    }
                }
            }
        }
    }
}
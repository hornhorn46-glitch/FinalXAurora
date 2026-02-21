package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.screens.NowScreen
import com.example.wittyapp.ui.theme.CosmosTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val api = remember { SpaceWeatherApi() }

            val vm: SpaceWeatherViewModel = viewModel(
                factory = SpaceWeatherViewModelFactory(api)
            )

            CosmosTheme(auroraScore = vm.state.auroraScore) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    NowScreen(
                        state = vm.state,
                        onRefresh = { vm.refresh() }
                    )
                }
            }
        }
    }
}
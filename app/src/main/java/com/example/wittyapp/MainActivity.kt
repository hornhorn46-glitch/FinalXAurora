package com.example.wittyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.strings.AppLanguage
import com.example.wittyapp.ui.strings.rememberAppStrings
import com.example.wittyapp.ui.theme.CosmosTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var crashText by remember { mutableStateOf<String?>(null) }

            if (crashText != null) {
                MaterialTheme {
                    Surface(Modifier.fillMaxSize()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("CRASH", style = MaterialTheme.typography.headlineMedium)
                            Spacer(Modifier.height(12.dp))
                            Text(crashText!!, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                return@setContent
            }

            try {
                var language by remember { mutableStateOf(AppLanguage.RU) }
                val strings = rememberAppStrings(language)

                val vm: SpaceWeatherViewModel = viewModel(
                    factory = SpaceWeatherViewModelFactory(SpaceWeatherApi())
                )

                CosmosTheme(auroraScore = vm.state.auroraScore) {
                    Scaffold { padding ->
                        // Если падение из-за ресурсов/экранов — оно проявится тут, но мы его поймаем
                        com.example.wittyapp.ui.screens.NowScreen(
                            vm = vm,
                            mode = AppMode.EARTH,
                            strings = strings,
                            contentPadding = padding,
                            onOpenGraphs = {},
                            onOpenEvents = {}
                        )
                    }
                }
            } catch (t: Throwable) {
                crashText = (t::class.qualifiedName ?: "Throwable") + "\n\n" +
                    (t.message ?: "") + "\n\n" +
                    t.stackTraceToString()
            }
        }
    }
}
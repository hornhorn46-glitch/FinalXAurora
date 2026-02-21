package com.example.wittyapp

import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wittyapp.net.SpaceWeatherApi
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.screens.NowScreen
import com.example.wittyapp.ui.strings.AppLanguage
import com.example.wittyapp.ui.strings.rememberAppStrings
import com.example.wittyapp.ui.theme.CosmosTheme

class MainActivity : ComponentActivity() {

    private val crashPrefs by lazy {
        getSharedPreferences("witty_crash", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // ставим хендлер максимально рано
        installCrashHandler()
        super.onCreate(savedInstanceState)

        val lastCrash = crashPrefs.getString(KEY_CRASH, null)

        setContent {
            if (lastCrash != null) {
                CrashScreen(
                    crashText = lastCrash,
                    onClear = {
                        crashPrefs.edit().remove(KEY_CRASH).commit()
                        recreate()
                    }
                )
            } else {
                AppRoot()
            }
        }
    }

    private fun installCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val text = buildString {
                    appendLine("Thread: ${t.name}")
                    appendLine("Exception: ${e::class.qualifiedName}")
                    appendLine("Message: ${e.message}")
                    appendLine()
                    appendLine(e.stackTraceToString())
                }
                // ВАЖНО: commit() синхронный — успевает записать перед убийством процесса
                crashPrefs.edit().putString(KEY_CRASH, text).commit()
            } catch (_: Throwable) {
                // ничего
            } finally {
                // гарантированно завершаем процесс уже после commit()
                Process.killProcess(Process.myPid())
            }
        }
    }

    @Composable
    private fun AppRoot() {
        var language by remember { mutableStateOf(AppLanguage.RU) }
        val strings = rememberAppStrings(language)

        val vm: SpaceWeatherViewModel = viewModel(
            factory = SpaceWeatherViewModelFactory(SpaceWeatherApi())
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

    companion object {
        private const val KEY_CRASH = "last_crash"
    }
}

@Composable
private fun CrashScreen(
    crashText: String,
    onClear: () -> Unit
) {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text("CRASH REPORT", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(12.dp))

                Button(onClick = onClear) { Text("Очистить и запустить") }

                Spacer(Modifier.height(12.dp))

                Text(
                    crashText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}
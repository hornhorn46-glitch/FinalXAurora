package com.example.wittyapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.components.GlassCard
import com.example.wittyapp.ui.strings.AppStrings

@Composable
fun EventsScreen(
    vm: SpaceWeatherViewModel,
    strings: AppStrings,
    contentPadding: PaddingValues,
    onClose: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(strings.events, style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onClose) { Text(strings.close) }
        }

        GlassCard {
            Text(
                "В этой версии события — простая справка.\n\n" +
                    "Дальше подключим полноценные источники по вспышкам/КВМ/бурям и сделаем таймлайн.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

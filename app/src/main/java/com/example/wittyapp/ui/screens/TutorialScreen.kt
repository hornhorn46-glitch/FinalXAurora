package com.example.wittyapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wittyapp.ui.components.GlassCard
import com.example.wittyapp.ui.strings.AppStrings

@Composable
fun TutorialScreen(
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
            Text(strings.tutorialTitle, style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onClose) { Text(strings.close) }
        }

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("1) Быстрый старт", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Открой «Земля». Смотри «Прогноз сияний (3 часа)».\n" +
                        "Если балл высокий — стоит проверить графики.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text("2) Главное сочетание", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Лучше всего для активности:\n" +
                        "• Bz отрицательный (вниз)\n" +
                        "• скорость ветра высокая\n" +
                        "• Kp растёт",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text("3) Раздел «Солнце»", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Там — визуальные источники: LASCO (CME), пятна, овал.\n" +
                        "Нажимай карточку — откроется на весь экран.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

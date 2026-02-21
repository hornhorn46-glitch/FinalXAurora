package com.example.wittyapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wittyapp.ui.components.GlassCard
import com.example.wittyapp.ui.strings.AppLanguage
import com.example.wittyapp.ui.strings.AppStrings

@Composable
fun SettingsScreen(
    strings: AppStrings,
    contentPadding: PaddingValues,
    currentLanguage: AppLanguage,
    onSetLanguage: (AppLanguage) -> Unit,
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
            Text(strings.settingsTitle, style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onClose) { Text(strings.close) }
        }

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(strings.language, style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = currentLanguage == AppLanguage.RU,
                        onClick = { onSetLanguage(AppLanguage.RU) },
                        label = { Text("Русский") }
                    )
                    FilterChip(
                        selected = currentLanguage == AppLanguage.EN,
                        onClick = { onSetLanguage(AppLanguage.EN) },
                        label = { Text("English") }
                    )
                }
            }
        }

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.about, style = MaterialTheme.typography.titleMedium)
                Text(strings.aboutText, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

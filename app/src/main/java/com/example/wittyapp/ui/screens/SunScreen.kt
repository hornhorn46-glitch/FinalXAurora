package com.example.wittyapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wittyapp.ui.components.GlassCard
import com.example.wittyapp.ui.strings.AppStrings

private enum class SunTab { CME, SUNSPOTS, OVAL }

@Composable
fun SunScreen(
    strings: AppStrings,
    contentPadding: PaddingValues,
    onOpenFull: (url: String, title: String) -> Unit
) {
    var tab by remember { mutableStateOf(SunTab.CME) }

    val lascoC3 = "https://soho.nascom.nasa.gov/data/LATEST/tinyc3.gif"
    val auroraNorth = "https://services.swpc.noaa.gov/images/animations/ovation/north/latest.jpg"
    val auroraSouth = "https://services.swpc.noaa.gov/images/animations/ovation/south/latest.jpg"
    val sunspotsPlaceholder = "https://soho.nascom.nasa.gov/data/realtime/hmi_igr/1024/latest.jpg"

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = tab == SunTab.CME, onClick = { tab = SunTab.CME }, label = { Text(strings.sunTabCme) })
            FilterChip(selected = tab == SunTab.SUNSPOTS, onClick = { tab = SunTab.SUNSPOTS }, label = { Text(strings.sunTabSunspots) })
            FilterChip(selected = tab == SunTab.OVAL, onClick = { tab = SunTab.OVAL }, label = { Text(strings.sunTabAuroraOval) })
        }

        when (tab) {
            SunTab.CME -> SunCard(
                title = "LASCO C3 (realtime)",
                subtitle = "Коронограф: выбросы и «гало»-структуры.",
                hint = strings.tapToFull,
                onOpen = { onOpenFull(lascoC3, "LASCO C3") }
            )
            SunTab.SUNSPOTS -> SunCard(
                title = "Солнечные пятна",
                subtitle = "Быстрый просмотр карты (в этой версии — официальный realtime).",
                hint = strings.tapToFull,
                onOpen = { onOpenFull(sunspotsPlaceholder, "Sunspots") }
            )
            SunTab.OVAL -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SunCard(
                    title = "Aurora Oval — Север",
                    subtitle = "OVATION, ближайшие 30–90 минут.",
                    hint = strings.tapToFull,
                    onOpen = { onOpenFull(auroraNorth, "Aurora Oval (North)") }
                )
                SunCard(
                    title = "Aurora Oval — Юг",
                    subtitle = "OVATION, ближайшие 30–90 минут.",
                    hint = strings.tapToFull,
                    onOpen = { onOpenFull(auroraSouth, "Aurora Oval (South)") }
                )
            }
        }

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Подсказка", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Если видишь крупный CME/гало на LASCO — через 1–3 дня могут быть эффекты на Земле.\n" +
                        "Для «сейчас» важнее всего вкладка «Земля»: Bz вниз + высокая скорость.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun SunCard(
    title: String,
    subtitle: String,
    hint: String,
    onOpen: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

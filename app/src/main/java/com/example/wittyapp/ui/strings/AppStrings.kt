package com.example.wittyapp.ui.strings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

enum class AppLanguage { RU, EN }

data class AppStrings(
    val now: String,
    val close: String,
    val loadingData: String,

    // screens
    val events: String,
    val settingsTitle: String,
    val tutorialTitle: String,

    // settings
    val language: String,
    val about: String,
    val aboutText: String,

    // sun tabs
    val sunTabCme: String,
    val sunTabSunspots: String,
    val sunTabAuroraOval: String,

    // misc
    val tapToFull: String
)

@Composable
fun rememberAppStrings(lang: AppLanguage): AppStrings {
    return remember(lang) {
        when (lang) {
            AppLanguage.RU -> AppStrings(
                now = "Сейчас",
                close = "Закрыть",
                loadingData = "Загрузка данных...",

                events = "События",
                settingsTitle = "Настройки",
                tutorialTitle = "Обучение",

                language = "Язык",
                about = "О приложении",
                aboutText = "Приложение для отслеживания космической погоды и оценки вероятности сияний. Данные берутся из открытых источников и обновляются автоматически.",

                sunTabCme = "CME (корона)",
                sunTabSunspots = "Пятна",
                sunTabAuroraOval = "Овал сияний",

                tapToFull = "Нажми, чтобы открыть на весь экран"
            )

            AppLanguage.EN -> AppStrings(
                now = "Now",
                close = "Close",
                loadingData = "Loading data...",

                events = "Events",
                settingsTitle = "Settings",
                tutorialTitle = "Tutorial",

                language = "Language",
                about = "About",
                aboutText = "Space weather dashboard with aurora probability hints. Data comes from public sources and refreshes automatically.",

                sunTabCme = "CME (corona)",
                sunTabSunspots = "Sunspots",
                sunTabAuroraOval = "Aurora oval",

                tapToFull = "Tap to open fullscreen"
            )
        }
    }
}
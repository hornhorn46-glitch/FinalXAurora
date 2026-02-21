package com.example.wittyapp.ui.strings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

data class AppStrings(
    val appTitle: String,

    val now: String,
    val graphs: String,
    val events: String,
    val sun: String,
    val earth: String,

    val close: String,
    val loadingData: String,
    val pressBackAgainToExit: String,

    val graphs24hTitle: String,

    val tutorialTitle: String,

    val settingsTitle: String,
    val language: String,
    val about: String,
    val aboutText: String,

    val sunTabCme: String,
    val sunTabSunspots: String,
    val sunTabAuroraOval: String,
    val tapToFull: String
)

@Composable
fun rememberAppStrings(language: AppLanguage): AppStrings {
    return remember(language) {
        when (language) {
            AppLanguage.RU -> AppStrings(
                appTitle = "WittyApp",

                now = "Сейчас",
                graphs = "Графики",
                events = "События",
                sun = "Солнце",
                earth = "Земля",

                close = "Закрыть",
                loadingData = "Загружаю данные…",
                pressBackAgainToExit = "Нажми Back ещё раз для выхода",

                graphs24hTitle = "Графики за 24 часа",

                tutorialTitle = "Обучение",

                settingsTitle = "Настройки",
                language = "Язык",
                about = "О приложении",
                aboutText = "Приложение помогает оценить геомагнитную активность и вероятность сияний по данным космической погоды.",

                sunTabCme = "CME / Halo",
                sunTabSunspots = "Пятна",
                sunTabAuroraOval = "Овал",
                tapToFull = "Нажми для полного экрана"
            )

            AppLanguage.EN -> AppStrings(
                appTitle = "WittyApp",

                now = "Now",
                graphs = "Graphs",
                events = "Events",
                sun = "Sun",
                earth = "Earth",

                close = "Close",
                loadingData = "Loading data…",
                pressBackAgainToExit = "Press Back again to exit",

                graphs24hTitle = "Graphs (24h)",

                tutorialTitle = "Tutorial",

                settingsTitle = "Settings",
                language = "Language",
                about = "About",
                aboutText = "Helps estimate geomagnetic activity and aurora probability using space weather data.",

                sunTabCme = "CME / Halo",
                sunTabSunspots = "Sunspots",
                sunTabAuroraOval = "Aurora oval",
                tapToFull = "Tap to open full"
            )
        }
    }
}
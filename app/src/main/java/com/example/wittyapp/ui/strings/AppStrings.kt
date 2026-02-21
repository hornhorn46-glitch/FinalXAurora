package com.example.wittyapp.ui.strings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

enum class AppLanguage(val code: String) {
    RU("ru"),
    EN("en");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.firstOrNull { it.code == code } ?: RU
    }
}

class AppStrings(private val lang: AppLanguage) {
    val titleEarth = tr("Земля", "Earth")
    val titleSun = tr("Солнце", "Sun")
    val settings = tr("Настройки", "Settings")
    val tutorial = tr("Обучение", "Tutorial")
    val exitHint = tr("Нажмите НАЗАД ещё раз для выхода", "Press BACK again to exit")
    val now = tr("Сейчас", "Now")
    val graphs = tr("Графики", "Graphs")
    val events = tr("События", "Events")
    val graphsTitle24h = tr("Графики (24 часа)", "Graphs (24h)")
    val loadingData = tr("Данные обновляются…", "Loading data…")
    val settingsTitle = tr("Настройки", "Settings")
    val language = tr("Язык", "Language")
    val about = tr("О приложении", "About")
    val aboutText = tr(
        "Wittyapp — приложение для понятного и красивого мониторинга космической погоды: от Солнца до Земли.\n\n" +
            "Цель — помочь даже новичку понять, когда возможны магнитные бури и сияния, и какие параметры за это отвечают.",
        "Wittyapp is a user-friendly space weather app from Sun to Earth."
    )
    val close = tr("Закрыть", "Close")
    val tutorialTitle = tr("Короткое обучение", "Quick tutorial")
    val sunTabCme = tr("CME (LASCO)", "CME (LASCO)")
    val sunTabSunspots = tr("Пятна", "Sunspots")
    val sunTabAuroraOval = tr("Овал", "Aurora oval")
    val tapToFull = tr("Нажмите для полного экрана", "Tap for fullscreen")

    private fun tr(ru: String, en: String) = if (lang == AppLanguage.RU) ru else en
}

@Composable
fun rememberAppStrings(lang: AppLanguage): AppStrings = remember(lang) { AppStrings(lang) }

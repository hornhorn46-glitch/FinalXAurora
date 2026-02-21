package com.example.wittyapp.ui.strings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

enum class AppLanguage {
    RU,
    EN
}

data class AppStrings(
    val now: String,
    val close: String,
    val loadingData: String
)

@Composable
fun rememberAppStrings(lang: AppLanguage): AppStrings {
    return remember(lang) {
        when (lang) {
            AppLanguage.RU -> AppStrings(
                now = "Сейчас",
                close = "Закрыть",
                loadingData = "Загрузка данных..."
            )
            AppLanguage.EN -> AppStrings(
                now = "Now",
                close = "Close",
                loadingData = "Loading data..."
            )
        }
    }
}
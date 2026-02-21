package com.example.wittyapp.ui.settings

import android.content.Context
import com.example.wittyapp.ui.strings.AppLanguage

class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences("witty_settings", Context.MODE_PRIVATE)

    fun getLanguage(): AppLanguage {
        val raw = prefs.getString("lang", AppLanguage.RU.name) ?: AppLanguage.RU.name
        return runCatching { AppLanguage.valueOf(raw) }.getOrDefault(AppLanguage.RU)
    }

    fun setLanguage(lang: AppLanguage) {
        prefs.edit().putString("lang", lang.name).apply()
    }
}
package com.example.wittyapp.ui.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.wittyapp.AppMode
import com.example.wittyapp.ui.strings.Language

class SettingsStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("witty_settings", Context.MODE_PRIVATE)

    fun loadMode(): AppMode {
        val raw = prefs.getString(KEY_MODE, AppMode.EARTH.name) ?: AppMode.EARTH.name
        return runCatching { AppMode.valueOf(raw) }.getOrElse { AppMode.EARTH }
    }

    fun saveMode(mode: AppMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
    }

    fun loadLanguage(): Language {
        val raw = prefs.getString(KEY_LANG, Language.RU.code) ?: Language.RU.code
        return Language.fromCode(raw)
    }

    fun saveLanguage(language: Language) {
        prefs.edit().putString(KEY_LANG, language.code).apply()
    }

    companion object {
        private const val KEY_MODE = "mode"
        private const val KEY_LANG = "lang"
    }
}
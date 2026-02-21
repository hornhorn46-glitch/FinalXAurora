package com.example.wittyapp.ui.settings

import android.content.Context
import com.example.wittyapp.AppMode
import com.example.wittyapp.ui.strings.AppLanguage

class SettingsStore(ctx: Context) {
    private val sp = ctx.getSharedPreferences("witty_settings", Context.MODE_PRIVATE)

    fun getLanguage(): AppLanguage {
        val v = sp.getString("lang", AppLanguage.RU.code) ?: AppLanguage.RU.code
        return AppLanguage.fromCode(v)
    }

    fun setLanguage(lang: AppLanguage) {
        sp.edit().putString("lang", lang.code).apply()
    }

    fun getMode(): AppMode {
        val v = sp.getString("mode", AppMode.EARTH.name) ?: AppMode.EARTH.name
        return runCatching { AppMode.valueOf(v) }.getOrDefault(AppMode.EARTH)
    }

    fun setMode(mode: AppMode) {
        sp.edit().putString("mode", mode.name).apply()
    }
}

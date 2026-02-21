package com.example.wittyapp.ui.strings

enum class Language(val code: String) {
    RU("ru"),
    EN("en");

    companion object {
        fun fromCode(code: String): Language =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: RU
    }
}
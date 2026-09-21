package com.autocare.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("autocare_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_AUTO_LOGIN = "auto_login"
    }

    var isDarkTheme: Boolean
        get() = sharedPreferences.getBoolean(KEY_DARK_THEME, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_DARK_THEME, value) }

    var isAutoLoginEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_AUTO_LOGIN, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_AUTO_LOGIN, value) }
}

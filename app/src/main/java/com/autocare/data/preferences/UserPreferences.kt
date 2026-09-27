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
        private const val KEY_IS_PRO_USER = "is_pro_user"
    }

    var isProUser: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_PRO_USER, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_IS_PRO_USER, value) }

    var isDarkTheme: Boolean
        get() = sharedPreferences.getBoolean(KEY_DARK_THEME, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_DARK_THEME, value) }

    var isAutoLoginEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_AUTO_LOGIN, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_AUTO_LOGIN, value) }
}

package com.roadrater.preferences

import android.os.Build
import com.roadrater.preferences.preference.PreferenceStore
import com.roadrater.preferences.preference.getEnum
import com.roadrater.ui.theme.AppTheme
import com.roadrater.ui.theme.DarkMode
import com.roadrater.ui.theme.ThemeMode
import com.roadrater.utils.DeviceUtil
import com.roadrater.utils.isDynamicColorAvailable

class AppearancePreferences(preferenceStore: PreferenceStore) {
    val themeMode = preferenceStore.getEnum("pref_theme_mode_key", ThemeMode.SYSTEM)

    val appTheme = preferenceStore.getEnum(
        "pref_app_theme",
        if (DeviceUtil.isDynamicColorAvailable) {
            AppTheme.MONET
        } else {
            AppTheme.DEFAULT
        },
    )

    val themeDarkAmoled = preferenceStore.getBoolean("pref_theme_dark_amoled_key", false)
}

package com.roadrater.preferences

import com.roadrater.preferences.preference.PreferenceStore

class GeneralPreferences(preferenceStore: PreferenceStore) {
    val tempPreference = preferenceStore.getString("temp-pref", "000")
    val loggedIn = preferenceStore.getBoolean("logged-in", false)
    val onboardingComplete = preferenceStore.getBoolean("logged-in", false)
}

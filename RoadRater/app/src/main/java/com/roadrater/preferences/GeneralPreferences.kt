package com.roadrater.preferences

import com.roadrater.database.entities.User
import com.roadrater.preferences.preference.PreferenceStore
import kotlinx.serialization.json.Json

class GeneralPreferences(preferenceStore: PreferenceStore) {
    val tempPreference = preferenceStore.getString("temp-pref", "000")
    val loggedIn = preferenceStore.getBoolean("logged-in", false)
    val onboardingComplete = preferenceStore.getBoolean("logged-in", false)
    val user = preferenceStore.getNullableObject<User>(
        key = "user_data",
        defaultValue = null,
        serializer = { Json.encodeToString(it) },
        deserializer = { Json.decodeFromString(it) },
    )
}

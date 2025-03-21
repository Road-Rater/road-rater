package com.roadrater

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.roadrater.preferences.AppearancePreferences
import com.roadrater.preferences.preference.collectAsState
import com.roadrater.ui.home.HomeScreen
import com.roadrater.ui.theme.DarkMode
import com.roadrater.ui.theme.RoadRaterTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val appearancePreferences by inject<AppearancePreferences>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val dark by appearancePreferences.darkMode.collectAsState()
            val isSystemInDarkTheme = isSystemInDarkTheme()
            enableEdgeToEdge(
                SystemBarStyle.auto(
                    lightScrim = Color.White.toArgb(),
                    darkScrim = Color.White.toArgb(),
                ) { dark == DarkMode.Dark || (dark == DarkMode.System && isSystemInDarkTheme) },
            )

            RoadRaterTheme {
                Navigator(screen = HomeScreen) {
                    SlideTransition(navigator = it)
                }
            }
        }
    }
}

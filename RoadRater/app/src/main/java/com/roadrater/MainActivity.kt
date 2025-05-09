package com.roadrater

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.transitions.SlideTransition
import com.roadrater.auth.Auth
import com.roadrater.auth.WelcomeScreen
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.preferences.preference.collectAsState
import com.roadrater.presentation.components.preferences.TachiyomiTheme
import com.roadrater.ui.home.HomeScreen
import com.roadrater.utils.FirebaseConfig
import org.koin.android.ext.android.inject
import org.koin.java.KoinJavaComponent.inject

class MainActivity : ComponentActivity() {
    private val generalPreferences by inject<GeneralPreferences>()

    private val auth by lazy {
        Auth()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseConfig.init(applicationContext)
        FirebaseConfig.setAnalyticsEnabled(true)
        FirebaseConfig.setCrashlyticsEnabled(true)

        setContent {
            val isSystemInDarkTheme = isSystemInDarkTheme()
            val onboardingComplete = generalPreferences.onboardingComplete.get()
            val signedInUser = generalPreferences.user.get()
            val statusBarBackgroundColor = MaterialTheme.colorScheme.surface

            LaunchedEffect(isSystemInDarkTheme, statusBarBackgroundColor) {
                // Draw edge-to-edge and set system bars color to transparent
                val lightStyle = SystemBarStyle.light(Color.Transparent.toArgb(), Color.Black.toArgb())
                val darkStyle = SystemBarStyle.dark(Color.Transparent.toArgb())
                enableEdgeToEdge(
                    statusBarStyle = if (isSystemInDarkTheme) darkStyle else lightStyle,
                    navigationBarStyle = if (isSystemInDarkTheme) darkStyle else lightStyle,
                )
            }

            val initialScreen = if (onboardingComplete && signedInUser != null) {
                HomeScreen
            } else {
                WelcomeScreen()
            }

            TachiyomiTheme {
                Navigator(
                    screen = initialScreen,
                    disposeBehavior = NavigatorDisposeBehavior(disposeNestedNavigators = false, disposeSteps = true),
                ) { SlideTransition(navigator = it) }
            }
        }
    }
}

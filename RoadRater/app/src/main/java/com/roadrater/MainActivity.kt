package com.roadrater

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.transitions.SlideTransition
import com.roadrater.auth.Auth
import com.roadrater.auth.WelcomeScreen
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.presentation.components.preferences.TachiyomiTheme
import com.roadrater.ui.home.HomeScreen
import com.roadrater.utils.FirebaseConfig
import com.roadrater.ui.NotificationHelper
import org.koin.android.ext.android.inject

import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class MainActivity : ComponentActivity() {
    private val generalPreferences by inject<GeneralPreferences>()
    private val auth by lazy { Auth() }

    // ✅ 1. Modern permission request launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // You can show a confirmation Toast here if you want
            } else {
                // Handle permission denial (e.g., disable notifications or show info dialog)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 2. Create notification channel (safe to call anytime)
        NotificationHelper.createNotificationChannel(this)

        // ✅ 3. Ask for notification permission (Android 13+ only)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Init Firebase
        FirebaseConfig.init(applicationContext)
        FirebaseConfig.setAnalyticsEnabled(true)
        FirebaseConfig.setCrashlyticsEnabled(true)

        setContent {
            val isSystemInDarkTheme = isSystemInDarkTheme()
            val onboardingComplete = generalPreferences.onboardingComplete.get()
            val signedInUser = generalPreferences.user.get()
            val statusBarBackgroundColor = MaterialTheme.colorScheme.surface

            LaunchedEffect(isSystemInDarkTheme, statusBarBackgroundColor) {
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
                    disposeBehavior = NavigatorDisposeBehavior(
                        disposeNestedNavigators = false,
                        disposeSteps = true
                    ),
                ) { SlideTransition(navigator = it) }
            }
        }
    }
}

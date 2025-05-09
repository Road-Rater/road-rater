package com.roadrater.auth

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.preferences.preference.collectAsState
import com.roadrater.presentation.Screen
import com.roadrater.ui.home.HomeScreen
import org.koin.compose.koinInject

class WelcomeScreen() : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow

        val generalPreferences = koinInject<GeneralPreferences>()
        val onboardingComplete by generalPreferences.onboardingComplete.collectAsState()

        val finishOnboarding: () -> Unit = {
            generalPreferences.onboardingComplete.set(true)
            Log.i("ONBOARDING FINISHED", "yay")
            navigator.push(HomeScreen)
        }

        BackHandler(
            enabled = !onboardingComplete,
            onBack = {
                // Prevent exiting if onboarding hasn't been completed
            },
        )

        OnboardingScreen(
            onComplete = finishOnboarding,

//            onRestoreBackup = {
//                finishOnboarding()
//                navigator.push(HomeScreen)
//            },
        )
    }
}

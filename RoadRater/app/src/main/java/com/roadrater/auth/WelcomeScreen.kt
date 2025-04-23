package com.roadrater.auth

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.preferences.preference.collectAsState
import com.roadrater.presentation.Screen
import org.koin.compose.koinInject

class WelcomeScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val generalPreferences = koinInject<GeneralPreferences>()
        val shownOnboardingFlow by generalPreferences.loggedIn.collectAsState()

        val finishOnboarding: () -> Unit = {
            generalPreferences.loggedIn.set(true)
            navigator.pop()
        }

        BackHandler(
            enabled = !shownOnboardingFlow,
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

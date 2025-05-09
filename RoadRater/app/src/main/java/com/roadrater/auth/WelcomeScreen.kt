package com.roadrater.auth

import android.util.Log
import android.app.Activity.RESULT_OK
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.android.gms.auth.api.identity.Identity
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.preferences.preference.collectAsState
import com.roadrater.presentation.Screen
import com.roadrater.ui.home.HomeScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

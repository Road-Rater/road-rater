package com.roadrater.auth

import android.app.Activity.RESULT_OK
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.android.gms.auth.api.identity.Identity
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.preferences.preference.collectAsState
import com.roadrater.presentation.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class WelcomeScreen() : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = viewModel<SignInViewModel>()
        val googleAuthUiClient = GoogleAuthUiClient(context, Identity.getSignInClient(context))

        val generalPreferences = koinInject<GeneralPreferences>()
        val shownOnboardingFlow by generalPreferences.onboardingComplete.collectAsState()

        val finishOnboarding: () -> Unit = {
            generalPreferences.onboardingComplete.set(true)
            navigator.pop()
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
            onResult = { result ->
                if (result.resultCode == RESULT_OK) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val signInResult = googleAuthUiClient.signInWithIntent(
                            intent = result.data ?: return@launch,
                        )
                        viewModel.onSignInResult(signInResult)
                    }
                }
            },
        )

        BackHandler(
            enabled = !shownOnboardingFlow,
            onBack = {
                // Prevent exiting if onboarding hasn't been completed
            },
        )

        OnboardingScreen(
            viewModel = viewModel,
            onSignInClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val signInIntentSender = googleAuthUiClient.signIn()
                    launcher.launch(
                        IntentSenderRequest.Builder(
                            signInIntentSender ?: return@launch,
                        ).build(),
                    )
                }
            },
            onComplete = finishOnboarding,
//            onRestoreBackup = {
//                finishOnboarding()
//                navigator.push(HomeScreen)
//            },
        )
    }
}

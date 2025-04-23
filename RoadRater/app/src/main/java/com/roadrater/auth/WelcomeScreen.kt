package com.roadrater.auth

import android.app.Activity.RESULT_OK
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.preferences.preference.collectAsState
import com.roadrater.presentation.Screen
import org.koin.compose.koinInject
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

class WelcomeScreen(
    val state: SignInState,
    val onSignInClick: () -> Unit,
) : Screen() {

//    private val googleAuthUiClient by lazy {
//        GoogleAuthUiClient(
//            context = LocalContext.current,
//            oneTapClient = Identity.getSignInClient(applicationContext)
//        )
//    }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val generalPreferences = koinInject<GeneralPreferences>()
        val shownOnboardingFlow by generalPreferences.loggedIn.collectAsState()


//        val screenModel = rememberScreenModel { SignInScreenModel() }
//        val state by screenModel.state.collectAsStateWithLifecycle()
//
//        val launcher = rememberLauncherForActivityResult(
//            contract = ActivityResultContracts.StartIntentSenderForResult(),
//            onResult = { result ->
//                if(result.resultCode == RESULT_OK) {
//                    lifecycleScope.launch {
//                        val signInResult = googleAuthUiClient.signInWithIntent(
//                            intent = result.data ?: return@launch
//                        )
//                        screenModel.onSignInResult(signInResult)
//                    }
//                }
//            }
//        )

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
            state = state,
            onSignInClick = onSignInClick,
            onComplete = finishOnboarding,
//            onRestoreBackup = {
//                finishOnboarding()
//                navigator.push(HomeScreen)
//            },
        )
    }
}

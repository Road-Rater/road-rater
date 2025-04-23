package com.roadrater

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.transitions.SlideTransition
import com.google.android.gms.auth.api.identity.Identity
import com.roadrater.auth.GoogleAuthUiClient
import com.roadrater.auth.SignInViewModel
import com.roadrater.auth.WelcomeScreen
import com.roadrater.preferences.AppearancePreferences
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.preferences.preference.collectAsState
import com.roadrater.ui.home.HomeScreen
import com.roadrater.ui.theme.DarkMode
import com.roadrater.ui.theme.RoadRaterTheme
import com.roadrater.utils.FirebaseConfig
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val generalPreferences by inject<GeneralPreferences>()
    private val appearancePreferences by inject<AppearancePreferences>()

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext),
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseConfig.init(applicationContext)
        FirebaseConfig.setAnalyticsEnabled(true)
        FirebaseConfig.setCrashlyticsEnabled(true)

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
                Navigator(
                    screen = HomeScreen,
                    disposeBehavior = NavigatorDisposeBehavior(disposeNestedNavigators = false, disposeSteps = true),
                ) {
                    SlideTransition(navigator = it)
                    ShowOnboarding()
                }
            }
        }
    }

    @Composable
    private fun ShowOnboarding() {
        val navigator = LocalNavigator.currentOrThrow

        val viewModel = viewModel<SignInViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
            onResult = { result ->
                if (result.resultCode == RESULT_OK) {
                    lifecycleScope.launch {
                        val signInResult = googleAuthUiClient.signInWithIntent(
                            intent = result.data ?: return@launch,
                        )
                        viewModel.onSignInResult(signInResult)
                    }
                }
            },
        )

        LaunchedEffect(Unit) {
            if (!generalPreferences.loggedIn.get() && navigator.lastItem !is WelcomeScreen) {
                navigator.push(
                    WelcomeScreen(
                        state = state,
                        onSignInClick = {
                            lifecycleScope.launch {
                                val signInIntentSender = googleAuthUiClient.signIn()
                                launcher.launch(
                                    IntentSenderRequest.Builder(
                                        signInIntentSender ?: return@launch,
                                    ).build(),
                                )
                            }
                        },
                    ),
                )
            }
        }
    }
}

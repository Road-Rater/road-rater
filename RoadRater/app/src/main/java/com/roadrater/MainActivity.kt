package com.roadrater

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.preferences.preference.collectAsState
import com.roadrater.presentation.components.preferences.TachiyomiTheme
import com.roadrater.ui.home.HomeScreen
import com.roadrater.utils.FirebaseConfig
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {
    private val generalPreferences by inject<GeneralPreferences>()

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
            val dark by appearancePreferences.themeMode.collectAsState()
            val isSystemInDarkTheme = isSystemInDarkTheme()
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

            TachiyomiTheme {
                Navigator(
                    screen = HomeScreen,
                    disposeBehavior = NavigatorDisposeBehavior(disposeNestedNavigators = false, disposeSteps = true),
                ) {
                    SlideTransition(navigator = it)
                    // ShowOnboarding()
                }
            }
        }
    }

    @Composable
    private fun ShowOnboarding() {
        val navigator = LocalNavigator.currentOrThrow

        val viewModel = viewModel<SignInViewModel>()

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
            if (!generalPreferences.onboardingComplete.get() && navigator.lastItem !is WelcomeScreen) {
                navigator.push(
                    WelcomeScreen(
                        viewModel = viewModel,
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

package com.roadrater.ui.myCars

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.roadrater.preferences.GeneralPreferences
import io.github.jan.supabase.SupabaseClient
import org.koin.compose.koinInject

object MyCarsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val supabaseClient: SupabaseClient = koinInject()
        val screenModel = rememberScreenModel { MyCarsScreenModel(supabaseClient) }

        //USERID INFO
        val generalPreferences = koinInject<GeneralPreferences>()
        val currentUser = generalPreferences.user.get()
        val userId = currentUser?.uid

        //TOP BAR
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("My Cars") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            },
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //NUMPLATE INPUT FIELD
                OutlinedTextField(
                    value = screenModel.inputText.value,
                    onValueChange = {
                        if (it.text.length <= 6) screenModel.inputText.value = it
                    },
                    label = { Text("Vehicle to register") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )

                Button(
                    onClick = {
                        screenModel.submitCar(userId) { _, _ -> }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit")
                }

                //FEEDBACK MESSAGE
                screenModel.feedbackMessage.value?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp)
                    )
                }

                //TODO: ADD OWNED CARS CARDS BELOW
            }
        }
    }
}

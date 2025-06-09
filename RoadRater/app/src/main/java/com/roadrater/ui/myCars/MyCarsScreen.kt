package com.roadrater.ui.myCars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.roadrater.R
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.presentation.components.CarWatchingCard
import io.github.jan.supabase.SupabaseClient
import org.koin.compose.koinInject

object MyCarsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        // SUPABASE CLIENT
        val supabaseClient: SupabaseClient = koinInject()
        val screenModel = rememberScreenModel { MyCarsScreenModel(supabaseClient) }

        // USERID INFO
        val generalPreferences = koinInject<GeneralPreferences>()
        val currentUser = generalPreferences.user.get()
        val userId = currentUser?.uid

        // TRACK SELECTED PLATE FOR CONFIRMATION
        var plateToUnregister by remember { mutableStateOf<String?>(null) }

        // LOAD CARS ON INIT
        LaunchedEffect(userId) {
            userId?.let { screenModel.loadOwnedCars(it) }
        }

        // TOP BAR
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.my_cars)) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back)) // new
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
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // NUMPLATE INPUT FIELD
                OutlinedTextField(
                    value = screenModel.inputText.value,
                    onValueChange = {
                        if (it.text.length <= 6) screenModel.inputText.value = it
                    },
                    label = { Text(stringResource(R.string.vehicle_to_register)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                )

                Button(
                    onClick = {
                        screenModel.submitCar(userId) { _, _ -> }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.submit)) // updated
                }

                // FEEDBACK MESSAGE
                screenModel.feedbackMessage.value?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp),
                    )
                }

                screenModel.ownedCars.forEach { car ->
                    CarWatchingCard(
                        car = car,
                        onClick = {
                            plateToUnregister = car.number_plate
                        },
                    )
                }
            }
        }

        // UNREGISTER CONFIRMATION DIALOG
        if (plateToUnregister != null) {
            AlertDialog(
                onDismissRequest = { plateToUnregister = null },
                confirmButton = {
                    TextButton(onClick = {
                        screenModel.unregisterCar(userId, plateToUnregister!!)
                        plateToUnregister = null
                    }) {
                        Text(stringResource(R.string.unregister)) // new
                    }
                },
                dismissButton = {
                    TextButton(onClick = { plateToUnregister = null }) {
                        Text(stringResource(R.string.cancel)) // already exists
                    }
                },
                title = { Text(stringResource(R.string.confirm_unregister)) }, // new
                text = { Text(stringResource(R.string.unregister_confirm_message, plateToUnregister!!)) }, // new
            )
        }
    }
}

package com.roadrater.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.android.gms.auth.api.identity.Identity
import com.roadrater.R
import com.roadrater.auth.GoogleAuthUiClient
import com.roadrater.presentation.Screen
import com.roadrater.presentation.components.CarWatchingCard
import io.github.jan.supabase.SupabaseClient
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import org.koin.compose.koinInject

object WatchedCarsScreen : Screen() {
    private fun readResolve(): Any = WatchedCarsScreen

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val supabaseClient = koinInject<SupabaseClient>()
        val currentUser = GoogleAuthUiClient(context, Identity.getSignInClient(context)).getSignedInUser()

        val screenModel = rememberScreenModel { WatchedCarsScreenModel(supabaseClient, currentUser!!.userId) }
        var showDialog by remember { mutableStateOf(false) }

        val watchedCars by screenModel.watchedCars.collectAsState()

        var numberPlate by remember { mutableStateOf("") }
        var selectedNumberPlate by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.watched_cars)) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                        }
                    },
                )
            },
        ) { paddingValues ->
            ProvidePreferenceLocals {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                        .padding(vertical = 2.dp, horizontal = 8.dp),
                ) {
                    OutlinedTextField(
                        value = numberPlate,
                        onValueChange = { numberPlate = it },
                        label = { Text(stringResource(R.string.number_plate)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    Button(
                        onClick = {
                            screenModel.watchCar(numberPlate)
                            numberPlate = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.add_car_watchlist))
                    }

                    if (watchedCars.isNotEmpty()) {
                        watchedCars.forEach { car ->
                            CarWatchingCard(
                                car = car,
                                onClick = {
                                    selectedNumberPlate = car.number_plate
                                    showDialog = true
                                },
                            )
                        }
                    }
                }
            }
        }

        if (showDialog) {
            RemoveCarDialog(
                onDismissRequest = { showDialog = false },
                onConfirm = {
                    screenModel.unwatchCar(selectedNumberPlate)
                },
                numberPlate = selectedNumberPlate,
            )
        }
    }

    @Composable
    fun RemoveCarDialog(
        onDismissRequest: () -> Unit,
        onConfirm: () -> Unit,
        numberPlate: String,
    ) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(text = stringResource(R.string.remove_car_dialog_title))
            },
            text = {
                Text(text = stringResource(R.string.remove_car_dialog_body, numberPlate))
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm()
                    onDismissRequest()
                }) {
                    Text(text = stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }
}

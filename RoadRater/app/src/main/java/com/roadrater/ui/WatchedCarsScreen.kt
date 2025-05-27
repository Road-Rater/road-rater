package com.roadrater.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.roadrater.R
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.presentation.Screen
import com.roadrater.presentation.components.CarWatchingCard
import com.roadrater.presentation.components.RemoveCarDialog
import com.roadrater.utils.ValidationUtils
import com.roadrater.utils.toast
import io.github.jan.supabase.SupabaseClient
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import org.koin.compose.getKoin
import org.koin.compose.koinInject

object WatchedCarsScreen : Screen() {
    private fun readResolve(): Any = WatchedCarsScreen

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val supabaseClient = koinInject<SupabaseClient>()
        val generalPreferences = getKoin().get<GeneralPreferences>()
        val currentUser = generalPreferences.user.get()
        val context = LocalContext.current

        val screenModel = rememberScreenModel { WatchedCarsScreenModel(supabaseClient, currentUser!!.uid) }
        var showDialog by remember { mutableStateOf(false) }
        var showError by remember { mutableStateOf(false) }

        val watchedCars by screenModel.watchedCars.collectAsState()
        val isLoading by screenModel.isLoading.collectAsState()
        val error by screenModel.errorMessage.collectAsState()
        val persistentError by screenModel.persistentErrorMessage.collectAsState()

        var numberPlate by remember { mutableStateOf("") }
        var selectedNumberPlate by remember { mutableStateOf("") }

        // Show transient error messages as toasts
        error?.let { errorMsg ->
            LaunchedEffect(errorMsg) {
                context.toast(errorMsg)
            }
        }

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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 2.dp, horizontal = 8.dp),
                    ) {
                        OutlinedTextField(
                            value = numberPlate,
                            onValueChange = { 
                                numberPlate = ValidationUtils.formatNumberPlate(it)
                                showError = !ValidationUtils.isValidNumberPlate(numberPlate) && numberPlate.isNotEmpty()
                                screenModel.clearPersistentError()
                            },
                            label = { Text(stringResource(R.string.number_plate)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = showError || persistentError != null,
                            supportingText = {
                                if (showError) {
                                    Text("Plate must be 1-6 alphanumeric characters")
                                } else if (persistentError != null) {
                                     Text(persistentError!!, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        Button(
                            onClick = {
                                if (ValidationUtils.isValidNumberPlate(numberPlate)) {
                                    screenModel.watchCar(numberPlate)
                                    numberPlate = ""
                                    showError = false
                                } else {
                                    showError = true
                                }
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
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No cars in your watchlist",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Text(
                                text = "Add cars to your watchlist to track their reviews",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
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
                    showDialog = false
                },
                numberPlate = selectedNumberPlate,
            )
        }
    }
}

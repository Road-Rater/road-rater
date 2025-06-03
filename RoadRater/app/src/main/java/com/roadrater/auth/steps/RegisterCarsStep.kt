package com.roadrater.auth.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.roadrater.R
import com.roadrater.auth.OnboardingStep
import com.roadrater.database.entities.Car
import com.roadrater.database.entities.WatchedCar
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.presentation.components.CarWatchingCard
import com.roadrater.ui.theme.spacing
import com.roadrater.utils.GetCarInfo
import com.roadrater.utils.ValidationUtils
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.collections.plus

internal class RegisterCarsStep : OnboardingStep {

    private var _isComplete by mutableStateOf(false)

    override val isComplete: Boolean
        get() = _isComplete

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val handler = LocalUriHandler.current

        var car by remember { mutableStateOf("") }
        var cars by remember { mutableStateOf(listOf<Car>()) }
        var showError by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }
        val supabaseClient = koinInject<SupabaseClient>()
        val generalPreferences = koinInject<GeneralPreferences>()
        val currentUser = generalPreferences.user.get()

        Column(
            modifier = Modifier.Companion.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            Text(stringResource(R.string.watchlist_cars_title))

            OutlinedTextField(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = car,
                onValueChange = {
                    car = ValidationUtils.formatNumberPlate(it)
                    showError = false
                },
                label = {
                    Text(stringResource(R.string.number_plate))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Text),
                singleLine = true,
                isError = showError,
                supportingText = {
                    if (showError) {
                        Text(stringResource(R.string.plate_format))
                    }
                },
            )

            Button(
                modifier = Modifier.Companion.fillMaxWidth(),
                onClick = {
                    if (ValidationUtils.isValidNumberPlate(car)) {
                        if (currentUser?.uid == null) return@Button
                        CoroutineScope(Dispatchers.IO).launch {
                            val watchedCar = watchCar(currentUser.uid, car, supabaseClient)
                            if (watchedCar != null) {
                                cars = cars + watchedCar
                                car = ""
                                _isComplete = true
                                showError = false
                            }
                        }
                    } else {
                        showError = true
                    }
                },
            ) {
                Text(stringResource(R.string.add_car))
            }

            if (cars.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.watchlist_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.Companion.padding(top = 16.dp),
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cars.forEach { addedCar ->
                        CarWatchingCard(addedCar, {})
                    }
                }
            }
        }
    }

    // Simple watchCar function
    private fun watchCar(uid: String, numberPlate: String, supabaseClient: SupabaseClient): Car {
        val car = GetCarInfo.getCarInfo(numberPlate)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabaseClient.from("cars").upsert(car)
                supabaseClient.from("watched_cars").upsert(
                    WatchedCar(
                        number_plate = numberPlate,
                        uid = uid,
                    ),
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return car
    }
}

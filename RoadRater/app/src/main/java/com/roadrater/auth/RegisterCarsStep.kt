package com.roadrater.auth

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.identity.Identity
import com.roadrater.database.entities.Car
import com.roadrater.database.entities.WatchedCar
import com.roadrater.ui.theme.spacing
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

internal class RegisterCarsStep : OnboardingStep {

    private var _isComplete by mutableStateOf(false)

    override val isComplete: Boolean
        get() = _isComplete

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val handler = LocalUriHandler.current

        var car by remember { mutableStateOf("") }
        var cars by remember { mutableStateOf(listOf<String>()) }
        val focusRequester = remember { FocusRequester() }
        val supabaseClient = koinInject<SupabaseClient>()
        val currentUser = GoogleAuthUiClient(context, Identity.getSignInClient(context)).getSignedInUser()


        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            Text("Add any cars which you want to be notified about")

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = car,
                onValueChange = { car = it },
                label = {
                    Text("Number Plate")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (car.isNotBlank()) {
                        if (currentUser?.userId == null) return@Button
                        watchCar(currentUser.userId, car, supabaseClient)
                        cars = cars + car // add the new car to the list
                        car = ""          // clear the text field
                        _isComplete = true
                    }
                },
            ) {
                Text("Add Car")
            }

            // List of added cars
            if (cars.isNotEmpty()) {
                Text(
                    text = "Cars you're watching:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )

                for (addedCar in cars) {
                    Text(
                        text = addedCar,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }

    // Simple watchCar function
    private fun watchCar(uid: String, numberPlate: String, supabaseClient: SupabaseClient) {
        println("Watching car: $numberPlate")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabaseClient.from("cars").upsert(Car(
                    number_plate = numberPlate,
                    make = null,
                    model = null,
                    year = null,
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                supabaseClient.from("watched_cars").upsert(WatchedCar(
                    number_plate = numberPlate,
                    uid = uid,
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

}

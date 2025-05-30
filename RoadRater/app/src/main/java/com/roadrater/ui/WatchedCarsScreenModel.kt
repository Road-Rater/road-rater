package com.roadrater.ui

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.roadrater.database.entities.Car
import com.roadrater.database.entities.WatchedCar
import com.roadrater.utils.GetCarInfo
import com.roadrater.utils.ValidationUtils
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class WatchedCarsScreenModel(
    private val supabaseClient: SupabaseClient,
    private val uid: String,
) : ScreenModel {

    var watchedCars = MutableStateFlow<List<Car>>(emptyList())
    var errorMessage = MutableStateFlow<String?>(null)
    var persistentErrorMessage = MutableStateFlow<String?>(null)
    var isLoading = MutableStateFlow(false)

    init {
        getWatchedCars()
    }

    fun getWatchedCars() {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                isLoading.value = true
                errorMessage.value = null
                persistentErrorMessage.value = null
                val watched = supabaseClient.from("watched_cars")
                    .select { filter { eq("uid", uid) } }
                    .decodeList<WatchedCar>()
                val carList = mutableListOf<Car>()
                watched.forEach { watchedCar ->
                    val car = supabaseClient.from("cars")
                        .select { filter { eq("number_plate", watchedCar.number_plate) } }
                        .decodeList<Car>()
                    carList.addAll(car)
                }
                watchedCars.value = carList
            } catch (e: Exception) {
                errorMessage.value = "Failed to load watched cars: ${e.message ?: "Unknown error"}"
                persistentErrorMessage.value = null // Clear persistent error on load failure
            } finally {
                isLoading.value = false
            }
        }
    }

    fun watchCar(numberPlate: String) {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                errorMessage.value = null
                persistentErrorMessage.value = null // Clear persistent errors on new attempt

                // Validate number plate format before proceeding using ValidationUtils
                if (!ValidationUtils.isValidNumberPlate(numberPlate)) {
                    persistentErrorMessage.value = "Invalid number plate format. Please check and try again."
                    return@launch
                }

                // Check if already watching
                val isWatching = supabaseClient.from("watched_cars")
                    .select {
                        filter {
                            eq("uid", uid)
                            eq("number_plate", numberPlate)
                        }
                    }
                    .countOrNull() ?: 0 > 0

                if (isWatching) {
                    persistentErrorMessage.value = "You are already watching this car"
                    return@launch
                }

                // Attempt to get car info
                val car = GetCarInfo.getCarInfo(numberPlate)

                // Check if car info was successfully retrieved (e.g., plate is not blank)
                if (car.number_plate.isBlank()) {
                    persistentErrorMessage.value = "Could not find information for this number plate. Please check and try again."
                    return@launch
                }

                // Upsert car and watched car if info is valid
                supabaseClient.from("cars").upsert(car)
                supabaseClient.from("watched_cars").upsert(
                    WatchedCar(
                        number_plate = numberPlate,
                        uid = uid,
                    ),
                )
                getWatchedCars()
            } catch (e: Exception) {
                // Handle potential exceptions (e.g., network, GetCarInfo failure, or database errors)
                errorMessage.value = when {
                    e.message?.contains("network", ignoreCase = true) == true -> "Network error: Please check your internet connection"
                    e.message?.contains("timeout", ignoreCase = true) == true -> "Request timed out: Please try again"
                    // Check for database unique constraint violation message (as a fallback)
                    e.message?.contains("duplicate key value violates unique constraint", ignoreCase = true) == true -> "You are already watching this car"
                    // Fallback for any other unexpected errors
                    else -> "An unexpected error occurred: ${e.message ?: "Unknown error"}"
                }
                persistentErrorMessage.value = null // Clear persistent error on other exceptions
            }
        }
    }

    fun unwatchCar(numberPlate: String) {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                errorMessage.value = null
                persistentErrorMessage.value = null // Clear persistent errors on unwatch attempt
                supabaseClient.from("watched_cars").delete {
                    filter {
                        eq("number_plate", numberPlate)
                        eq("uid", uid)
                    }
                }
                getWatchedCars()
            } catch (e: Exception) {
                // Handle potential exceptions (e.g., network, or database errors)
                errorMessage.value = when {
                    e.message?.contains("network", ignoreCase = true) == true -> "Network error: Please check your internet connection"
                    e.message?.contains("timeout", ignoreCase = true) == true -> "Request timed out: Please try again"
                    // Fallback for any other unexpected errors
                    else -> "Failed to unwatch car: ${e.message ?: "Unknown error"}"
                }
                persistentErrorMessage.value = null // Clear persistent error on unwatch failure
            }
        }
    }

    // Function to clear the persistent error message when input changes
    fun clearPersistentError() {
        persistentErrorMessage.value = null
    }
}

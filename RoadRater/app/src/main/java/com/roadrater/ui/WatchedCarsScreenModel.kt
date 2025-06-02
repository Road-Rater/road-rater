package com.roadrater.ui

import android.content.Context
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.roadrater.R
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
    private val context: Context,

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
                errorMessage.value = context.getString(R.string.load_watch_failed)
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
                    persistentErrorMessage.value = context.getString(R.string.invalid_plate)
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
                    persistentErrorMessage.value = context.getString(R.string.watching)
                    return@launch
                }

                // Attempt to get car info
                val car = GetCarInfo.getCarInfo(numberPlate)

                // Check if car info was successfully retrieved (e.g., plate is not blank)
                if (car.number_plate.isBlank()) {
                    persistentErrorMessage.value = context.getString(R.string.info_retrieval_error)
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
                    e.message?.contains("network", ignoreCase = true) == true -> context.getString(R.string.network_error)
                    e.message?.contains("timeout", ignoreCase = true) == true -> context.getString(R.string.request_time_out)
                    // Check for database unique constraint violation message (as a fallback)
                    e.message?.contains(context.getString(R.string.duplicate_key), ignoreCase = true) == true -> context.getString(R.string.watching)
                    // Fallback for any other unexpected errors
                    else -> context.getString(R.string.unexpected_error, e.message ?: context.getString(R.string.unknown_error))
                }
                persistentErrorMessage.value = null // Clear persistent error on other exceptions
            }
        }
    }

    private fun isValidNumberPlate(numberPlate: String): Boolean {
        // Basic NZ number plate format validation (should match ValidationUtils)
        // The pattern in ValidationUtils is 1..6 alphanumeric, so let's match that.
        val pattern = "^[A-Z0-9]{1,6}$".toRegex()
        return pattern.matches(numberPlate)
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
                    e.message?.contains("network", ignoreCase = true) == true -> context.getString(R.string.network_error)
                    e.message?.contains("timeout", ignoreCase = true) == true -> context.getString(R.string.request_time_out)
                    // Fallback for any other unexpected errors
                    else -> context.getString(R.string.unwatch_failed, e.message ?: context.getString(R.string.unknown_error))
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

package com.roadrater.ui.myCars

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import cafe.adriel.voyager.core.model.ScreenModel
import com.roadrater.database.entities.Car
import com.roadrater.database.entities.CarOwnership
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.roadrater.utils.ValidationUtils
import com.roadrater.R

class MyCarsScreenModel(
    private val supabaseClient: SupabaseClient
) : ScreenModel {

    var inputText = mutableStateOf(TextFieldValue(""))

    //MOST RECENT ERROR MESSAGE
    var feedbackMessage = mutableStateOf<String?>(null)

    //LIST OF USER-OWNED CARS
    val ownedCars = mutableStateListOf<Car>()

    //SUBMIT CAR FUNCTION
    fun submitCar(userId: String?, onResult: (Boolean, String) -> Unit) {
        val rawInput = inputText.value.text
        val plate = rawInput.trim().uppercase()

        Log.d("MyCars", "Raw input: '$rawInput', Normalized: '$plate'")

        //BASIC CHECKS BEFOREHAND
        if (userId.isNullOrEmpty()) {
            feedbackMessage.value = "You must be signed in"
            onResult(false, "You must be signed in")
            return
        }

        //VALIDATING LICENSE PLATE INPUT FORMAT
        if (!ValidationUtils.isValidNumberPlate(plate)) {
            feedbackMessage.value = "Invalid number plate format. Please check and try again."
            onResult(false, "Invalid number plate format. Please check and try again.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                //CHECKING IF PLATE EXISTS WITHIN "car_ownership" TABLE
                Log.d("MyCars", "Checking if plate already exists...")

                val plateQuery = supabaseClient
                    .from("car_ownership")
                    .select {
                        filter {
                            eq("number_plate", plate)
                        }
                    }
                    .decodeList<CarOwnership>()

                //HANDLES PRE-EXISTING PLATES
                if (plateQuery.isNotEmpty()) {
                    val record = plateQuery.first()
                    val existingOwnerId = record.user_id

                    when {
                        //PLATE ALREADY OWNED BY CURRENT USER
                        existingOwnerId == userId -> {
                            feedbackMessage.value = "You already own this car"
                            onResult(false, "You already own this car")
                        }

                        //PLATE ALREADY OWNED BY SOMEONE ELSE
                        !existingOwnerId.isNullOrBlank() -> {
                            feedbackMessage.value = "Car has already been claimed"
                            onResult(false, "Car has already been claimed")
                        }

                        //PLATE EXISTS BUT UNCLAIMED
                        else -> {
                            Log.d("MyCars", "Plate exists but has no owner, assigning now...")
                            assignPlateToUser(plate, userId, onResult)
                        }
                    }
                    return@launch
                }

                //CHECKING USER CAR COUNT
                Log.d("MyCars", "Checking user car count...")

                val userCarsQuery = supabaseClient
                    .from("car_ownership")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<CarOwnership>()

                val carCount = userCarsQuery.size
                Log.d("MyCars", "User owns $carCount cars")

                //MAX 2 CARS PER USER
                if (carCount >= 2) {
                    feedbackMessage.value = "You already registered the max of 2 cars"
                    onResult(false, "You already registered the max of 2 cars")
                    return@launch
                }

                //INSERTING OWNERSHIP
                Log.d("MyCars", "Inserting new car record")

                val insertResult = supabaseClient
                    .from("car_ownership")
                    .insert(CarOwnership(number_plate = plate, user_id = userId))

                Log.d("MyCars", "Car registered successfully")
                feedbackMessage.value = "Car successfully registered"
                onResult(true, "Car successfully registered")
                loadOwnedCars(userId)

            } catch (e: Exception) {
                //ERROR HANDLING
                Log.e("MyCars", "Exception: ${e.message}", e)
                feedbackMessage.value = "Something went wrong. Try again later"
                onResult(false, "Something went wrong. Try again later")
            }
        }
    }

    //ASSIGNING OWNERSHIP OF UNCLAIMED PLATE
    private suspend fun assignPlateToUser(
        plate: String,
        userId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        try {
            supabaseClient
                .from("car_ownership")
                .update(
                    mapOf("user_id" to userId)
                ) {
                    filter {
                        eq("number_plate", plate)
                    }
                }

            Log.d("MyCars", "Plate ownership updated")
            feedbackMessage.value = "Car successfully registered"
            onResult(true, "Car successfully registered")
            loadOwnedCars(userId)

        } catch (e: Exception) {
            Log.e("MyCars", "Error updating car ownership: ${e.message}", e)
            feedbackMessage.value = "Something went wrong. Try again later"
            onResult(false, "Something went wrong. Try again later")
        }
    }

    //LOADING ALL CARS THE USER OWNS
    fun loadOwnedCars(userId: String?) {
        if (userId.isNullOrEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ownerships = supabaseClient
                    .from("car_ownership")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<CarOwnership>()

                val cars = ownerships.mapNotNull { ownership ->
                    supabaseClient
                        .from("cars")
                        .select {
                            filter {
                                eq("number_plate", ownership.number_plate)
                            }
                        }
                        .decodeSingleOrNull<Car>()
                }

                ownedCars.clear()
                ownedCars.addAll(cars)

            } catch (e: Exception) {
                Log.e("MyCars", "Failed to load owned cars: ${e.message}", e)
            }
        }
    }

    //UNREGISTER A CAR
    fun unregisterCar(userId: String?, plate: String) {
        if (userId.isNullOrEmpty()) {
            feedbackMessage.value = "You must be signed in"
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabaseClient
                    .from("car_ownership")
                    .update(
                        mapOf("user_id" to null)
                    ) {
                        filter {
                            eq("number_plate", plate)
                            eq("user_id", userId)
                        }
                    }

                Log.d("MyCars", "Car unregistered: $plate")
                feedbackMessage.value = "Car successfully unregistered"
                loadOwnedCars(userId)

            } catch (e: Exception) {
                Log.e("MyCars", "Failed to unregister car: ${e.message}", e)
                feedbackMessage.value = "Failed to unregister car"
            }
        }
    }
}

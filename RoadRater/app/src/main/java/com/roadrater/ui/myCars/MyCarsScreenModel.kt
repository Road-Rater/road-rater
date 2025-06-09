package com.roadrater.ui.myCars

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import cafe.adriel.voyager.core.model.ScreenModel
import com.roadrater.database.entities.CarOwnership
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyCarsScreenModel(
    private val supabaseClient: SupabaseClient
) : ScreenModel {

    var inputText = mutableStateOf(TextFieldValue(""))

    //MOST RECENT ERROR MESSAGE
    var feedbackMessage = mutableStateOf<String?>(null)

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
        if (!plate.matches(Regex("^[A-Za-z0-9]{1,6}$"))) {
            feedbackMessage.value = "Invalid plate format"
            onResult(false, "Invalid plate format")
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

                //PLATE DOESNT EXIST

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

        } catch (e: Exception) {
            Log.e("MyCars", "Error updating car ownership: ${e.message}", e)
            feedbackMessage.value = "Something went wrong. Try again later"
            onResult(false, "Something went wrong. Try again later")
        }
    }
}

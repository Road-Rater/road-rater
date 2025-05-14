package com.roadrater.ui

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.roadrater.database.entities.Car
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.WatchedCar
import com.roadrater.utils.GetCarInfo
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CarDetailsScreenModel(
    private val supabaseClient: SupabaseClient,
    private val numberPlate: String,
    private val uid: String,
) : ScreenModel {

    var isWatching = MutableStateFlow<Boolean>(false)
    var car = MutableStateFlow<Car?>(null)
    var reviews = MutableStateFlow<List<Review>>(emptyList())

    init {
        isWatching()
        fetchCar()
        fetchReviews()
    }

    fun fetchCar() {
        screenModelScope.launch(Dispatchers.IO) {
            car.value = supabaseClient.from("cars")
                .select {
                    filter {
                        ilike("number_plate", numberPlate)
                    }
                }
                .decodeSingleOrNull<Car>()
        }
    }

    fun fetchReviews() {
        screenModelScope.launch(Dispatchers.IO) {
            reviews.value = supabaseClient.from("reviews")
                .select {
                    filter {
                        ilike("number_plate", numberPlate)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Review>()
        }
    }

    fun isWatching() {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val watched = supabaseClient.from("watched_cars").select {
                    filter {
                        eq("uid", uid)
                        eq("number_plate", numberPlate)
                    }
                }.decodeList<WatchedCar>()
                isWatching.value = watched.isNotEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun watchCar() {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val car = GetCarInfo.getCarInfo(numberPlate)
                supabaseClient.from("cars").upsert(car)
                supabaseClient.from("watched_cars").upsert(
                    WatchedCar(
                        number_plate = numberPlate,
                        uid = uid,
                    ),
                )
                isWatching.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unwatchCar(numberPlate: String) {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                supabaseClient.from("watched_cars").delete {
                    filter {
                        eq("number_plate", numberPlate)
                        eq("uid", uid)
                    }
                }
                isWatching.value = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

package com.roadrater.ui

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.roadrater.database.entities.Car
import com.roadrater.database.entities.WatchedCar
import com.roadrater.utils.GetCarInfo
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

    init {
        getWatchedCars()
    }

    fun getWatchedCars() {
        screenModelScope.launch(Dispatchers.IO) {
            val watched = supabaseClient.from("watched_cars").select { filter { eq("uid", uid) } }.decodeList<WatchedCar>()
            val carList = mutableListOf<Car>()
            watched.forEach { watchedCar ->
                val car = supabaseClient.from("cars").select { filter { eq("number_plate", watchedCar.number_plate) } }.decodeList<Car>()
                carList.addAll(car)
            }
            watchedCars.value = carList
        }
    }

    fun watchCar(numberPlate: String) {
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
                getWatchedCars()
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
                getWatchedCars()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

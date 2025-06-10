package com.roadrater.database.repository

import android.util.Log
import com.roadrater.database.entities.Car
import com.roadrater.database.entities.WatchedCar
import com.roadrater.utils.GetCarInfo
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class CarRepository(val supabase: SupabaseClient) {
    suspend fun getCarByPlate(numberPlate: String): Car? {
        return try {
            supabase.from("cars")
                .select {
                    filter {
                        eq("number_plate", numberPlate)
                    }
                }.decodeSingleOrNull<Car>()
        } catch (e: Exception) {
            Log.e("CarRepository", "Error in getCarByPlate", e)
            null
        }
    }

    suspend fun getWatchedCars(uid: String): List<Car> {
        return try {
            val watchedCarsResult = supabase.from("watched_cars")
                .select { filter { eq("uid", uid) } }
                .decodeList<WatchedCar>()

            val plates = watchedCarsResult.map {
                it.number_plate
            }

            supabase.from("cars")
                .select { filter { isIn("number_plate", plates) } }
                .decodeList<Car>()
        } catch (e: Exception) {
            Log.e("CarRepository", "Error in getWatchedCars", e)
            emptyList()
        }
    }

    suspend fun watchCar(uid: String, numberPlate: String) {
        try {
            val count = supabase
                .from("cars")
                .select {
                    filter {
                        eq("number_plate", numberPlate)
                    }
                    limit(1)
                }.countOrNull()

            if (count == null || count >= 0) {
                supabase.from("cars").upsert(GetCarInfo.getCarInfo(numberPlate))
            }
            supabase.from("watched_cars").upsert(
                WatchedCar(
                    number_plate = numberPlate,
                    uid = uid,
                ),
            )
        } catch (e: Exception) {
            Log.e("CarRepository", "Error in watchCar", e)
        }
    }
}

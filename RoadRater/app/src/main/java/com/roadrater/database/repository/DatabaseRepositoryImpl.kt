package com.roadrater.database.repository

import com.roadrater.database.entities.Car
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.TableUser
import com.roadrater.database.entities.WatchedCar
import com.roadrater.database.entities.Notification
import com.roadrater.domain.DatabaseRepository
import com.roadrater.utils.GetCarInfo
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class DatabaseRepositoryImpl(
    private val supabaseClient: SupabaseClient,
) : DatabaseRepository {

    override suspend fun getCarByPlate(numberPlate: String): Car? {
        val result = supabaseClient.from("cars").select {
            filter { eq("number_plate", numberPlate) }
        }
        val jsonString = Json.encodeToString(result.data ?: emptyList<Map<String, Any>>())
        val cars: List<Car> = Json.decodeFromString(jsonString)
        return cars.firstOrNull()
    }

    override suspend fun insertCar(numberPlate: String) {
        supabaseClient.from("cars").insert(GetCarInfo.getCarInfo(numberPlate))
    }

    override suspend fun upsertCar(car: Car) {
        supabaseClient.from("cars").upsert(car)
    }

    override suspend fun watchCar(uid: String, numberPlate: String) {
        val result = supabaseClient.from("cars").select {
            filter { eq("number_plate", numberPlate) }
        }
        val jsonString = Json.encodeToString(result.data ?: emptyList<Map<String, Any>>())
        val cars: List<Car> = Json.decodeFromString(jsonString)
        val exists = cars.isNotEmpty()
        if (!exists) {
            supabaseClient.from("cars").upsert(GetCarInfo.getCarInfo(numberPlate))
        }
        supabaseClient.from("watched_cars").upsert(
            WatchedCar(
                number_plate = numberPlate,
                uid = uid,
            )
        )
    }

    override suspend fun unwatchCar(uid: String, numberPlate: String) {
        supabaseClient.from("watched_cars").delete {
            filter {
                eq("number_plate", numberPlate)
                eq("uid", uid)
            }
        }
    }

    override suspend fun getWatchedCars(uid: String): List<Car> {
        val watchedResult = supabaseClient.from("watched_cars").select {
            filter { eq("uid", uid) }
        }
        val watchedJson = Json.encodeToString(watchedResult.data ?: emptyList<Map<String, Any>>())
        val watched: List<WatchedCar> = Json.decodeFromString(watchedJson)
        return watched.mapNotNull { getCarByPlate(it.number_plate) }
    }

    override suspend fun isWatchingCar(uid: String, numberPlate: String): Boolean {
        val result = supabaseClient.from("watched_cars").select {
            filter {
                eq("uid", uid)
                eq("number_plate", numberPlate)
            }
        }
        val jsonString = Json.encodeToString(result.data ?: emptyList<Map<String, Any>>())
        val watched: List<WatchedCar> = Json.decodeFromString(jsonString)
        return watched.isNotEmpty()
    }

    override suspend fun insertReview(review: Review) {
        supabaseClient.from("reviews").insert(review)
    }

    override suspend fun getReviewsByPlate(numberPlate: String): List<Review> {
        val result = supabaseClient.from("reviews").select {
            filter { eq("number_plate", numberPlate) }
        }
        val jsonString = Json.encodeToString(result.data ?: emptyList<Map<String, Any>>())
        return Json.decodeFromString(jsonString)
    }

    override suspend fun getReviewsByUser(uid: String): List<Review> {
        val result = supabaseClient.from("reviews").select {
            filter { eq("created_by", uid) }
        }
        val jsonString = Json.encodeToString(result.data ?: emptyList<Map<String, Any>>())
        return Json.decodeFromString(jsonString)
    }

    override suspend fun getUser(uid: String): TableUser? {
        val result = supabaseClient.from("users").select {
            filter { eq("uid", uid) }
        }
        val jsonString = Json.encodeToString(result.data ?: emptyList<Map<String, Any>>())
        val users: List<TableUser> = Json.decodeFromString(jsonString)
        return users.firstOrNull()
    }

    override suspend fun insertUser(user: TableUser) {
        supabaseClient.from("users").insert(user)
    }

    override suspend fun updateNickname(uid: String, nickname: String) {
        supabaseClient.from("users").update({
            set("nickname", nickname)
        }) {
            filter { eq("uid", uid) }
        }
    }

    override suspend fun nicknameAvailable(nickname: String): Boolean {
        return try {
            val result = supabaseClient.from("users").select {
                filter { eq("nickname", nickname) }
            }
            val jsonString = Json.encodeToString(result.data ?: emptyList<Map<String, Any>>())
            val response: List<TableUser> = Json.decodeFromString(jsonString)
            response.isEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun getNotifications(uid: String): List<Notification> {
        val result = supabaseClient.from("notifications").select {
            filter { eq("uid", uid) }
            order("created_at", Order.DESCENDING)
        }
        val jsonString = Json.encodeToString(result.data ?: emptyList<Map<String, Any>>())
        return Json.decodeFromString(jsonString)
    }
}

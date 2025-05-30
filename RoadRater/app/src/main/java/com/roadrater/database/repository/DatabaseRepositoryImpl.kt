package com.roadrater.database.repository

import com.roadrater.database.entities.Car
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.TableUser
import com.roadrater.database.entities.WatchedCar
import com.roadrater.domain.DatabaseRepository
import com.roadrater.utils.GetCarInfo
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class DatabaseRepositoryImpl(
    private val supabaseClient: SupabaseClient,
) : DatabaseRepository {
    override suspend fun getCarByPlate(numberPlate: String): Car? {
        return supabaseClient.from("cars").select { filter { eq("number_plate", numberPlate) } }.decodeSingleOrNull<Car>()
    }

    override suspend fun insertCar(numberPlate: String) {
        supabaseClient.from("cars").insert(GetCarInfo.getCarInfo(numberPlate))
    }

    override suspend fun upsertCar(car: Car) {
        supabaseClient.from("car").insert(car)
    }

    override suspend fun watchCar(uid: String, numberPlate: String) {
        // Redundant checks for invalid format and already watching are removed as they are handled in ViewModel

        // Ensure car exists in database (or upsert if not)
        val count = supabaseClient
            .from("cars")
            .select {
                filter {
                    eq("number_plate", numberPlate)
                }
                limit(1)
            }.countOrNull()

        if (count == null || count >= 0) {
            supabaseClient.from("cars").upsert(GetCarInfo.getCarInfo(numberPlate))
        }

        // Add to watched cars
        supabaseClient.from("watched_cars").upsert(
            WatchedCar(
                number_plate = numberPlate,
                uid = uid,
            ),
        )
    }

    override suspend fun unwatchCar(uid: String, numberPlate: String) {
        // Redundant check for not watching is removed as it should be handled in ViewModel
        supabaseClient.from("watched_cars").delete {
            filter {
                eq("number_plate", numberPlate)
                eq("uid", uid)
            }
        }
    }

    override suspend fun insertReview(review: Review) {
        supabaseClient.from("reviews").insert(review)
    }

    override suspend fun getReviewsByPlate(numberPlate: String): List<Review?> {
        return supabaseClient.from("watched_cars").select {
            filter {
                eq("number_plate", numberPlate)
            }
        }.decodeList<Review>()
    }

    override suspend fun getReviewsByUser(uid: String): List<Review?> {
        return supabaseClient.from("watched_cars").select {
            filter {
                eq("created_by", uid)
            }
        }.decodeList<Review>()
    }

    override suspend fun getUser(uid: String): TableUser? {
        return supabaseClient.from("users").select {
            filter {
                eq("uid", uid)
            }
        }.decodeSingleOrNull<TableUser>()
    }

    override suspend fun insertUser(user: TableUser) {
        supabaseClient.from("users").insert(user)
    }

    override suspend fun updateNickname(uid: String, nickname: String) {
        supabaseClient.from("users").update(
            {
                set("nickname", nickname)
            },
        ) {
            filter {
                eq("uid", uid)
            }
        }
    }

    override suspend fun nicknameAvailable(nickname: String): Boolean {
        return try {
            val response = supabaseClient.from("users").select { filter { eq("nickname", nickname) } }.decodeList<TableUser>()
            response.isEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

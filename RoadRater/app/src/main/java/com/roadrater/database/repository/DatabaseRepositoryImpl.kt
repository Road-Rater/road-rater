package com.roadrater.database.repository

import com.roadrater.database.entities.Car
import com.roadrater.database.entities.Reviews
import com.roadrater.database.entities.TableUser
import com.roadrater.domain.DatabaseRepository
import io.github.jan.supabase.SupabaseClient

class DatabaseRepositoryImpl(
    private val supabaseClient: SupabaseClient,
) : DatabaseRepository {
    override suspend fun getCarByPlate(plate: String): Car? {
        TODO("Not yet implemented")
    }

    override suspend fun insertCar(plate: String) {
        TODO("Not yet implemented")
    }

    override suspend fun upsertCar(car: Car) {
        TODO("Not yet implemented")
    }

    override suspend fun watchCar(uid: String, numberPlate: String) {
        TODO("Not yet implemented")
    }

    override suspend fun unwatchCar(uid: String, numberPlate: String) {
        TODO("Not yet implemented")
    }

    override suspend fun insertReview(review: Reviews) {
        TODO("Not yet implemented")
    }

    override suspend fun getReviewsByPlate(plate: String): Reviews? {
        TODO("Not yet implemented")
    }

    override suspend fun getReviewsByUser(uid: String): Reviews? {
        TODO("Not yet implemented")
    }

    override suspend fun getUser(uid: String): TableUser? {
        TODO("Not yet implemented")
    }

    override suspend fun insertUser(user: TableUser) {
        TODO("Not yet implemented")
    }

    override suspend fun updateNickname(uid: String, nickname: String) {
        TODO("Not yet implemented")
    }

    override suspend fun nicknameAvailable(nickname: String): Boolean {
        TODO("Not yet implemented")
    }
}

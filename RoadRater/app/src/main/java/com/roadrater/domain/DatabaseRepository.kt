package com.roadrater.domain

import com.roadrater.database.entities.Car
import com.roadrater.database.entities.Reviews
import com.roadrater.database.entities.TableUser

interface DatabaseRepository {

    suspend fun getCarByPlate(numberPlate: String): Car?
    suspend fun insertCar(numberPlate: String)
    suspend fun upsertCar(car: Car)
    suspend fun watchCar(uid: String, numberPlate: String)
    suspend fun unwatchCar(uid: String, numberPlate: String)

    suspend fun insertReview(review: Reviews)
    suspend fun getReviewsByPlate(numberPlate: String): List<Reviews?>
    suspend fun getReviewsByUser(uid: String): List<Reviews?>

    suspend fun getUser(uid: String): TableUser?
    suspend fun insertUser(user: TableUser)
    suspend fun updateNickname(uid: String, nickname: String)
    suspend fun nicknameAvailable(nickname: String): Boolean
}

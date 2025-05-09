package com.roadrater.database.repository

import com.roadrater.database.dao.RatingDao
import com.roadrater.database.entities.Rating

class RatingRepository(private val ratingDao: RatingDao) {
    suspend fun insertRating(rating: Rating) {
        ratingDao.insertRating(rating)
    }

    suspend fun getRatingsForCar(numberPlate: String): List<Rating> {
        return ratingDao.getRatingsForCar(numberPlate)
    }
}

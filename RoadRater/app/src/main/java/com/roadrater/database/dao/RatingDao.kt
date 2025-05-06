package com.roadrater.database.dao

import androidx.room.*
import com.roadrater.database.entities.Rating

@Dao
interface RatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: Rating)

    @Query("SELECT * FROM rating WHERE numberPlate = :plate")
    suspend fun getRatingsForCar(plate: String): List<Rating>
}

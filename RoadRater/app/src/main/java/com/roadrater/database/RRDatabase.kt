package com.roadrater.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.roadrater.database.dao.CarDao
import com.roadrater.database.dao.RatingDao
import com.roadrater.database.entities.CarEntity
import com.roadrater.database.entities.Rating

@Database(entities = [CarEntity::class, Rating::class], version = 2)
abstract class RRDatabase : RoomDatabase() {
    abstract fun carDao(): CarDao
    abstract fun ratingDao(): RatingDao
}

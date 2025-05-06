package com.roadrater.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

//Using composite key (userId + numberPlate + createdAt)
@Serializable
@Entity(tableName = "rating", primaryKeys = ["userId", "numberPlate", "createdAt"])
data class Rating(
    val userId: String,
    val numberPlate: String,
    val review: Int,
    val comment: String,
    val createdAt: Int
)

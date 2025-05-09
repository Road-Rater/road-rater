package com.roadrater.database.entities

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "rating", primaryKeys = ["userId", "numberPlate", "createdAt"])
data class Rating(
    val userId: String,
    val numberPlate: String,
    val review: Int,
    val comment: String,
    val createdAt: Int,
)

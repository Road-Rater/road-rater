package com.roadrater.database.entities

import com.roadrater.database.entities.User
import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: Int,
    val created_at: String,
    val created_by: String,
    val rating: Int,
    val title: String?,
    val description: String?,
    val labels: List<String>?,
    val number_plate: String,
)
package com.roadrater.database.entities

import kotlinx.serialization.Serializable

@Serializable
data class Rating(
    val id: Int,
    val userId: String,
    val numberPlate: String,
    val rating: Int,
    val comment: String,
    val createdAt: Int,
)

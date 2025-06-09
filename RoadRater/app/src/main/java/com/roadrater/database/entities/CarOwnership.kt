package com.roadrater.database.entities

import kotlinx.serialization.Serializable

@Serializable
data class CarOwnership(
    val number_plate: String,
    val user_id: String? = null
)

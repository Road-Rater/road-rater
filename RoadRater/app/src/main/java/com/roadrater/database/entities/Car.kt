package com.roadrater.database.entities

import kotlinx.serialization.Serializable

@Serializable
data class Car(
    val id: Int,
    val numberPlate: String,
    val make: String,
    val model: String,
    val year: String,
    val lastChecked: String,
)

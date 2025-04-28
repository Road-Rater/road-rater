package com.roadrater.database.entities

import kotlinx.serialization.Serializable

@Serializable
data class Car(
    val number_plate: String,
    val make: String?,
    val model: String?,
    val year: String?,
    // val lastChecked: String?,
)

@Serializable
data class WatchedCar(
    val number_plate: String,
    val uid: String,
)

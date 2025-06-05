package com.roadrater.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class Notification(
    //val id: Int,
    val title: String,
    val message: String,
    @SerialName("number_plate") val numberPlate: String,
    val read: Boolean = false,
    @SerialName("review_id") val reviewId: Int,
    val uid: String,
    @SerialName("created_at") val createdAt: String,
)
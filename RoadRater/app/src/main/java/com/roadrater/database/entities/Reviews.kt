package com.roadrater.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Reviews(
    val id: Int,
    @SerialName("created_at") val createdAt: String,
    @SerialName("created_by") val createdBy: String,
    @SerialName("rating") val rating: Double? = null,
    val description: String? = null,
    val title: String? = null,
    val labels: List<String>? = null,
    @SerialName("number_plate") val numberPlate: String,
)

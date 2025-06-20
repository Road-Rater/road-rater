package com.roadrater.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Review(
    @SerialName("id") val id: Int? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("created_by") val createdBy: String,
    @SerialName("rating") val rating: Int,
    val description: String = "No description",
    val title: String = "No Title",
    val labels: List<String> = emptyList(),
    @SerialName("number_plate") val numberPlate: String,
    @SerialName("is_flagged") val isFlagged: Boolean = false,
)

package com.roadrater.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: Long,
    @SerialName("review_id") val reviewId: Long,
    @SerialName("user_id") val userId: String,
    val content: String,
    @SerialName("parent_id") val parentId: Long? = null,
    @SerialName("created_at") val createdAt: String
)

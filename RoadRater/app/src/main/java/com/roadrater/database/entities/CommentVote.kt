package com.roadrater.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CommentVote(
    val id: Long,

    @SerialName("comment_id")
    val commentId: Long,

    @SerialName("user_id")
    val userId: String,

    val vote: Int,

    @SerialName("created_at")
    val createdAt: String
)
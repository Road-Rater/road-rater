package com.roadrater.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TableUser(
    // val id: Int,
    val uid: String,
    val name: String?,
    val nickname: String?,
    val email: String?,
    val profilePictureUrl: String? = null,
)

@Serializable
data class NicknamelessUser(
    val uid: String,
    val name: String?,
    val email: String?,
    @SerialName("profile_pic_url") val profilePictureUrl: String? = null,
)

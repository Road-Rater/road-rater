package com.roadrater.database.entities

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String,
    val name: String?,
    val nickname: String?,
    val email: String?,
    val profile_pic_url: String?,
)
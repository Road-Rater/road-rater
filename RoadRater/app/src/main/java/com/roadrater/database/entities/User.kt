package com.roadrater.database.entities

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val uid: String,
    val name: String,
    val nickname: String,
    val email: String,
    val timestamp: Int,
)

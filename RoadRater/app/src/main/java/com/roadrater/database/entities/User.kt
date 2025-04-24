package com.roadrater.database.entities

import kotlinx.serialization.Serializable

@Serializable
data class TableUser(
    //val id: Int,
    val uid: String,
    val name: String?,
    val nickname: String?,
    val email: String?,
)

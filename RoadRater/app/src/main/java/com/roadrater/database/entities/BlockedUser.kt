package com.roadrater.database.entities

import kotlinx.serialization.Serializable

@Serializable
data class BlockedUser(
    val uid: String, // UNIQUE ID
    val blocked_user: String?, // PERSON BEING BLOCKED (NULLABLE)
    val user_blocking: String, // PERSON DOING THE BLOCKING

)

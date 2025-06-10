package com.roadrater.database.entities

import kotlinx.serialization.Serializable

@Serializable
data class BlockedUser(
    val uid: String,               // PERSON BLOCKING
    val blocked_user: String,     // PERSON BEING BLOCKED
    val created_at: String? = null        // TIMESTAMPZ
)

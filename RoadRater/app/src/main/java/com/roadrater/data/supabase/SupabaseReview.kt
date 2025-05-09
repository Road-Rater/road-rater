package com.roadrater.data.supabase

import kotlinx.serialization.Serializable

@Serializable
data class SupabaseReview(
    val created_by: String,
    val number_plate: String,
    val rating: Int,
    val title: String,
    val description: String,
    val created_at: String
)

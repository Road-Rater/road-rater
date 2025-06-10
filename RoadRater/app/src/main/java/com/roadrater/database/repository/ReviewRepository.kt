package com.roadrater.database.repository

import android.util.Log
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class ReviewRepository(private val supabase: SupabaseClient) {
    suspend fun insertReview(review: Review) {
        try {
            supabase.from("reviews").insert(review)
        } catch (e: Exception) {
            Log.e("ReviewRepository", "insertReview error", e)
        }
    }

    suspend fun getReviewsByPlate(plate: String): List<Review> {
        return try {
            supabase.from("reviews").select {
                filter {
                    eq("number_plate", plate)
                }
            }.decodeList<Review>()
        } catch (e: Exception) {
            Log.e("ReviewRepository", "getReviewsByPlate error", e)
            emptyList()
        }
    }

    suspend fun getReviewsByPlates(plates: List<String>): List<Review> {
        return try {
            supabase.from("reviews").select {
                filter {
                    isIn("number_plate", plates)
                }
            }.decodeList<Review>()
        } catch (e: Exception) {
            Log.e("ReviewRepository", "getReviewsByPlates error", e)
            emptyList()
        }
    }

    suspend fun getReviewsByUser(uid: String): List<Review> {
        return try {
            supabase.from("reviews").select {
                filter {
                    eq("created_by", uid)
                }
            }.decodeList<Review>()
        } catch (e: Exception) {
            Log.e("ReviewRepository", "getReviewsByUser error", e)
            emptyList()
        }
    }

    suspend fun mapReviewsToUsers(reviews: List<Review>): Map<Review, User> {
        return try {
            val reviewerIds = reviews.map { it.createdBy }.distinct()

            val reviewers = supabase
                .from("users")
                .select {
                    filter {
                        isIn("uid", reviewerIds)
                    }
                }
                .decodeList<User>()

            val userMap = reviewers.associateBy { it.uid }

            reviews.mapNotNull { review ->
                userMap[review.createdBy]?.let { review to it }
            }.toMap()
        } catch (e: Exception) {
            Log.e("ReviewRepository", "mapReviewsToUsers error", e)
            emptyMap()
        }
    }
}

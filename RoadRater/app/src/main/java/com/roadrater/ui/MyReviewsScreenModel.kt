package com.roadrater.ui

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.roadrater.database.entities.Review
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MyReviewsScreenModel(
    private val supabaseClient: SupabaseClient,
    private val uid: String,
) : ScreenModel {

    val reviews = MutableStateFlow<List<Review>>(emptyList())

    init {
        getMyReviews()
    }

    fun getMyReviews() {
        screenModelScope.launch(Dispatchers.IO) {
            val reviewsResult = supabaseClient.from("reviews")
                .select {
                    filter {
                        eq("created_by", uid)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Review>()
            reviews.value = reviewsResult
        }
    }
}

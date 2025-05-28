package com.roadrater.ui.home.tabs

import android.util.Log
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.WatchedCar
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.MissingFieldException

class HomeTabScreenModel(
    private val supabaseClient: SupabaseClient,
    private val uid: String,
) : ScreenModel {

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    init {
        screenModelScope.launch(Dispatchers.IO) {
            getRelevantReviews()
        }
    }

    suspend fun getRelevantReviews() {
        val relevantReviews = mutableListOf<Review>()

        val watchedCars: List<WatchedCar> = try {
            supabaseClient
                .from("watched_cars")
                .select { filter { eq("uid", uid) } }
                .decodeList<WatchedCar>()
        } catch (e: MissingFieldException) {
            Log.e("Missing Field Error", e.message.orEmpty())
            emptyList()
        }

        val plates = watchedCars.map { it.number_plate }

        val watchedReviews = try {
            supabaseClient.from("reviews")
                .select(
                    columns = Columns.list(
                        listOf(
                            "id",
                            "created_at",
                            "created_by",
                            "rating",
                            "title",
                            "description",
                            "labels",
                            "number_plate",
                        ),
                    ),
                ) {
                    filter {
                        isIn("number_plate", watchedCars)
                    }
                }
                .decodeList<Review>()
        } catch (e: MissingFieldException) {
            Log.e("Missing Field Error", e.message.orEmpty())
            emptyList()
        }

        val ratingsAgainst = supabaseClient
            .from("reviews")
            .select { filter { eq("created_by", uid) } }
            .decodeList<Review>()

        relevantReviews.addAll(watchedReviews + ratingsAgainst)
        _reviews.value = relevantReviews
    }

    fun openAddReviewDialog() {

    }

    suspend fun addReview() {

    }
}

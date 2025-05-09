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

// Screen model for the Home tab, responsible for loading relevant reviews based on watched cars and user activity.
class HomeTabScreenModel(
    private val supabaseClient: SupabaseClient,
    private val uid: String,
) : ScreenModel {

    // Holds the list of relevant reviews to be displayed.
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())

    // Publicly exposed read-only StateFlow of reviews.
    val reviews: StateFlow<List<Review>> = _reviews

    init {
        // Automatically fetch relevant reviews when this screen model is initialized.
        screenModelScope.launch(Dispatchers.IO) {
            getRelevantReviews()
        }
    }

    // Fetches reviews related to the user's watched cars and their own submitted reviews.
    suspend fun getRelevantReviews() {
        val relevantReviews = mutableListOf<Review>()

        // Fetch all cars the user is watching.
        val watchedCars: List<WatchedCar> = try {
            supabaseClient
                .from("watched_cars")
                .select { filter { eq("uid", uid) } }
                .decodeList<WatchedCar>()
        } catch (e: MissingFieldException) {
            Log.e("Missing Field Error", e.message.orEmpty())
            emptyList()
        }

        // Extract number plates from the watched cars (currently unused).
        val plates = watchedCars.map { it.number_plate }

        // Fetch reviews written about the watched cars.
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
                        isIn("number_plate", plates)
                    }
                }
                .decodeList<Review>()
        } catch (e: MissingFieldException) {
            Log.e("Missing Field Error", e.message.orEmpty())
            emptyList()
        }

        // Fetch reviews written by the user.
        val ratingsAgainst = supabaseClient
            .from("reviews")
            .select { filter { eq("created_by", uid) } }
            .decodeList<Review>()

        // Combine watched car reviews and user's own reviews into the relevant list.
        relevantReviews.addAll(watchedReviews + ratingsAgainst)
        _reviews.value = relevantReviews
    }
}

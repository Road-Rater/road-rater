package com.roadrater.ui.home.tabs

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.WatchedCar
import com.roadrater.preferences.GeneralPreferences
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.java.KoinJavaComponent.inject

class ProfileTabScreenModel(
    private val supabaseClient: SupabaseClient,
    private val generalPreferences: GeneralPreferences
) : ScreenModel {
    private val _reviewsGiven = MutableStateFlow<List<Review>>(emptyList())
    val reviewsGiven: StateFlow<List<Review>> = _reviewsGiven

    private val _reviewsReceived = MutableStateFlow<List<Review>>(emptyList())
    val reviewsReceived: StateFlow<List<Review>> = _reviewsReceived

    val currentUser = generalPreferences.user.get()

    init {
        screenModelScope.launch(Dispatchers.IO) {
            getReviews()
        }
    }

    suspend fun getReviews() {
        try {
            // Get all reviews written by this user
            val reviewsGivenResult = supabaseClient.from("reviews")
                .select { filter { eq("created_by", currentUser!!.uid) } }
                .decodeList<Review>()
            _reviewsGiven.value = reviewsGivenResult

        } catch (e: Exception) {
            println("Error fetching profile data: ${e.message}")
        }
    }
}

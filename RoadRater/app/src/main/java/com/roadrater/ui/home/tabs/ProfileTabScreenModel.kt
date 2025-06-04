package com.roadrater.ui.home.tabs

import android.util.Log
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.roadrater.database.entities.Car
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.WatchedCar
import com.roadrater.preferences.GeneralPreferences
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileTabScreenModel(
    private val supabaseClient: SupabaseClient,
    private val generalPreferences: GeneralPreferences
) : ScreenModel {
    private val _reviewsGiven = MutableStateFlow<List<Review>>(emptyList())
    val reviewsGiven: StateFlow<List<Review>> = _reviewsGiven

    private val _reviewsReceived = MutableStateFlow<List<Review>>(emptyList())
    val reviewsReceived: StateFlow<List<Review>> = _reviewsReceived

    private val _selectedPrimaryTab = MutableStateFlow<Int>(0)
    val selectedPrimaryTab: StateFlow<Int> = _selectedPrimaryTab

    private val _selectedSecondaryTab = MutableStateFlow<Int>(0)
    val selectedSecondaryTab: StateFlow<Int> = _selectedSecondaryTab

    fun setSelectedSecondaryTab(tab: Int) {
        _selectedSecondaryTab.value = tab
    }

    fun setSelectedPrimaryTab(tab: Int) {
        _selectedPrimaryTab.value = tab
    }

    val currentUser = generalPreferences.user.get()

    private val _watchedCars = MutableStateFlow<List<Car>>(emptyList())
    val watchedCars: StateFlow<List<Car>> = _watchedCars

    init {
        screenModelScope.launch(Dispatchers.IO) {
            getReviewsGiven()
            getWatchedCars()
            getReviewsReceived()
        }
    }

    suspend fun getReviewsReceived() {
        try {
            val plates = watchedCars.value.map {
                it.number_plate
            }
            // Get all reviews written against cars this user watches
            val reviewsReceivedResult = supabaseClient.from("reviews")
                .select { filter { isIn("number_plate", plates) } }
                .decodeList<Review>()
            _reviewsReceived.value = reviewsReceivedResult

        } catch (e: Exception) {
            println("Error fetching given reviews: ${e.message}")
        }
    }

    suspend fun getReviewsGiven() {
        try {
            // Get all reviews written by this user
            val reviewsGivenResult = supabaseClient.from("reviews")
                .select { filter { eq("created_by", currentUser!!.uid) } }
                .decodeList<Review>()
            _reviewsGiven.value = reviewsGivenResult

        } catch (e: Exception) {
            println("Error fetching received reviews: ${e.message}")
        }
    }

    suspend fun getWatchedCars() {
        try {
            // Get cars this user is watching
            val watchedCarsPlates = supabaseClient.from("watched_cars")
                .select { filter { eq("uid", currentUser!!.uid) } }
                .decodeList<WatchedCar>()

            Log.d("Watched Car Plates", watchedCarsPlates.toString())

            val plates = watchedCarsPlates.map {
                it.number_plate
            }

            val watchedCars = supabaseClient.from("cars")
                .select { filter { isIn("number_plate", plates) } }
                .decodeList<Car>()

            Log.d("Watched Cars", watchedCars.toString())

            _watchedCars.value = watchedCars

        } catch (e: Exception) {
            println("Error fetching watched cars: ${e.message}")
        }
    }
}

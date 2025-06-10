package com.roadrater.ui

import android.util.Log
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.roadrater.database.entities.Car
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.User
import com.roadrater.database.entities.WatchedCar
import com.roadrater.utils.GetCarInfo
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CarDetailsScreenModel(
    private val supabaseClient: SupabaseClient,
    private val numberPlate: String,
    private val uid: String,
) : ScreenModel {

    var isWatching = MutableStateFlow<Boolean>(false)

    private val _car = MutableStateFlow<Car?>(null)
    val car: StateFlow<Car?> = _car

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    private val _reviewsAndReviewers = MutableStateFlow<Map<Review, User>>(emptyMap())
    val reviewsAndReviewers: StateFlow<Map<Review, User>> = _reviewsAndReviewers

    private val _isCarLoading = MutableStateFlow(false)
    val isCarLoading: StateFlow<Boolean> = _isCarLoading

    private val _isReviewsLoading = MutableStateFlow(false)
    val isReviewsLoading: StateFlow<Boolean> = _isReviewsLoading

    init {
        isWatching()
        fetchCar()
        fetchReviews()
    }

    fun fetchCar() {
        screenModelScope.launch(Dispatchers.IO) {
            _isCarLoading.value = true
            try {
                val carResult = supabaseClient.from("cars")
                    .select {
                        filter {
                            eq("number_plate", numberPlate)
                        }
                    }
                    .decodeSingleOrNull<Car>()

                if (carResult != null) {
                    _car.value = carResult
                } else {
                    val carInfoResult = GetCarInfo.getCarInfo(numberPlate)
                    _car.value = carInfoResult
                    supabaseClient.from("cars").upsert(carInfoResult)
                }
                Log.d("CarDetailsScreenModel - FetchCar", car.value.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isCarLoading.value = false
            }
        }
    }

    fun fetchReviews() {
        screenModelScope.launch(Dispatchers.IO) {
            _isReviewsLoading.value = true
            try {
                val reviewsResult = supabaseClient.from("reviews")
                    .select {
                        filter {
                            eq("number_plate", numberPlate)
                        }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<Review>()

                _reviews.value = reviewsResult
                mapReviewsToUsers(reviewsResult)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isReviewsLoading.value = false
            }
        }
    }

    fun mapReviewsToUsers(reviews: List<Review>) {
        screenModelScope.launch(Dispatchers.IO) {
            val reviewerIds = reviews.map { it.createdBy }.distinct()


            val reviewers = supabaseClient
                .from("users")
                .select {
                    filter {
                        isIn("uid", reviewerIds)
                    }
                }
                .decodeList<User>()
            Log.d("CarDetailsScreenModel - Reviewers", reviewers.toString())
            val userMap = reviewers.associateBy { it.uid }

            _reviewsAndReviewers.value = reviews.mapNotNull { review ->
                userMap[review.createdBy]?.let { review to it }
            }.toMap()
        }
    }

    fun isWatching() {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val watched = supabaseClient.from("watched_cars").select {
                    filter {
                        eq("uid", uid)
                        eq("number_plate", numberPlate)
                    }
                }.decodeList<WatchedCar>()
                isWatching.value = watched.isNotEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun watchCar() {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val car = GetCarInfo.getCarInfo(numberPlate)
                supabaseClient.from("cars").upsert(car)
                supabaseClient.from("watched_cars").upsert(
                    WatchedCar(
                        number_plate = numberPlate,
                        uid = uid,
                    ),
                )
                isWatching.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unwatchCar(numberPlate: String) {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                supabaseClient.from("watched_cars").delete {
                    filter {
                        eq("number_plate", numberPlate)
                        eq("uid", uid)
                    }
                }
                isWatching.value = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

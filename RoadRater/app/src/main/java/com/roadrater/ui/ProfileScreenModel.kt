package com.roadrater.ui

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.roadrater.database.entities.Car
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.User
import com.roadrater.database.repository.CarRepository
import com.roadrater.database.repository.ReviewRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileScreenModel(
    private val user: User,
    private val reviewRepository: ReviewRepository,
    private val carRepository: CarRepository,
) : ScreenModel {
    private val _reviewsGiven = MutableStateFlow<List<Review>>(emptyList())
    val reviewsGiven: StateFlow<List<Review>> = _reviewsGiven

    private val _reviewsReceived = MutableStateFlow<List<Review>>(emptyList())
    val reviewsReceived: StateFlow<List<Review>> = _reviewsReceived

    private val _selectedPrimaryTab = MutableStateFlow<Int>(0)
    val selectedPrimaryTab: StateFlow<Int> = _selectedPrimaryTab

    private val _selectedSecondaryTab = MutableStateFlow<Int>(0)
    val selectedSecondaryTab: StateFlow<Int> = _selectedSecondaryTab

    private val _reviewsReceivedAndReviewers = MutableStateFlow<Map<Review, User>>(emptyMap())
    val reviewsReceivedAndReviewers: StateFlow<Map<Review, User>> = _reviewsReceivedAndReviewers

    private val _reviewsGivenAndReviewers = MutableStateFlow<Map<Review, User>>(emptyMap())
    val reviewsGivenAndReviewers: StateFlow<Map<Review, User>> = _reviewsGivenAndReviewers

    private val _watchedCars = MutableStateFlow<List<Car>>(emptyList())
    val watchedCars: StateFlow<List<Car>> = _watchedCars

    fun setSelectedSecondaryTab(tab: Int) {
        _selectedSecondaryTab.value = tab
    }

    fun setSelectedPrimaryTab(tab: Int) {
        _selectedPrimaryTab.value = tab
    }

    init {
        screenModelScope.launch(Dispatchers.IO) {
            _reviewsGiven.value = reviewRepository.getReviewsByUser(user.uid)
            _watchedCars.value = carRepository.getWatchedCars(user.uid)
            _reviewsReceived.value = reviewRepository.getReviewsByPlates(
                watchedCars.value.map { it.number_plate },
            )
            _reviewsReceivedAndReviewers.value = reviewRepository
                .mapReviewsToUsers(
                    reviewsGiven.value + reviewsReceived.value,
                )

            _reviewsGivenAndReviewers.value = reviewsGiven.value.associate { review ->
                Pair(review, user)
            }
        }
    }
}

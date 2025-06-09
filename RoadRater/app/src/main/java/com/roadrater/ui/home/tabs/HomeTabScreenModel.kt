package com.roadrater.ui.home.tabs

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.User
import com.roadrater.database.repository.CarRepository
import com.roadrater.database.repository.ReviewRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HomeTabScreenModel(
    private val currentUser: User,
    private val reviewRepository: ReviewRepository,
    private val carRepository: CarRepository,
) : ScreenModel {
    val reviewsAndReviewers = MutableStateFlow<Map<Review, User>>(emptyMap())

    init {
        screenModelScope.launch(Dispatchers.IO) {
            val reviews = reviewRepository.getReviewsByUser(currentUser.uid)
            reviewsAndReviewers.value = reviewRepository.mapReviewsToUsers(reviews)
        }
    }
}

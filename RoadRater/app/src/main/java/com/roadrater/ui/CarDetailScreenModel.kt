package com.roadrater.ui

import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.roadrater.database.entities.Car
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.User
import com.roadrater.database.repository.CarRepository
import com.roadrater.database.repository.ReviewRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CarDetailScreenModel(
    private val plate: String,
    private val reviewRepository: ReviewRepository,
    private val carRepository: CarRepository,
) : ScreenModel {
    val car = mutableStateOf<Car?>(null)
    val reviewsAndReviewers = mutableStateOf<Map<Review, User>>(emptyMap())

    init {
        screenModelScope.launch(Dispatchers.IO) {
            car.value = carRepository.getCarByPlate(plate)
            val reviews = reviewRepository.getReviewsByPlate(plate)
            reviewsAndReviewers.value = reviewRepository.mapReviewsToUsers(reviews)
        }
    }
}

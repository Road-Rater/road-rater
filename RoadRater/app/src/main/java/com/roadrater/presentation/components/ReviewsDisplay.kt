package com.roadrater.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.User
import com.roadrater.ui.CarDetailsScreen

@Composable
fun ReviewsDisplay(
    modifier: Modifier,
    reviewsAndReviewers: Map<Review, User>,
    showReviewSummary: Boolean = false,
) {
    val navigator = LocalNavigator.currentOrThrow
    LazyColumn(modifier = modifier) {
        if (showReviewSummary) {
            item {
                ReviewSummary(reviewsAndReviewers.keys.toList())
            }
        }
        items(reviewsAndReviewers.entries.toList()) { (review, reviewer) ->
            ReviewCard(review,
                reviewer,
                onPlateClick = {
                    navigator.push(CarDetailsScreen(review.numberPlate))
                }
            )
        }
    }
}

@Composable
fun ReviewSummary(reviewsForTab: List<Review>) {
    var average = reviewsForTab.map { it.rating.toDouble() }.average()
    var averageFormatted = "%.1f".format(average)
    Column(
        modifier = Modifier.Companion.padding(horizontal = 15.dp),
    ) {
        Row(
            modifier = Modifier.Companion.fillMaxWidth(),
            verticalAlignment = Alignment.Companion.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            AnimatedContent(
                targetState = average.toInt(),
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(
                        animationSpec = tween(
                            300,
                        ),
                    )
                },
                label = "StarRatingFade",
            ) { average ->
                StarRating(average, 40.dp)
            }
            Spacer(modifier = Modifier.Companion.width(10.dp))
            AnimatedContent(
                targetState = averageFormatted,
                transitionSpec = {
                    // Compare the incoming number with the previous number.
                    if (targetState > initialState) {
                        // If the target number is larger, it slides up and fades in
                        // while the initial (smaller) number slides up and fades out.
                        slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                    } else {
                        // If the target number is smaller, it slides down and fades in
                        // while the initial number slides down and fades out.
                        slideInVertically { height -> -height } + fadeIn() togetherWith
                            slideOutVertically { height -> height } + fadeOut()
                    }.using(
                        // Disable clipping since the faded slide-in/out should
                        // be displayed out of bounds.
                        SizeTransform(clip = false),
                    )
                },
                label = "animated content",
            ) { averageFormatted ->
                Text(
                    text = if (reviewsForTab.isNotEmpty()) averageFormatted else "0.0",
                    style = MaterialTheme.typography.displayLarge,
                )
            }
        }
        RatingBarBreakdown(reviewsForTab)
    }
}

@Composable
fun RatingBarBreakdown(reviews: List<Review>) {
    fun getRatingCounts(reviews: List<Review>): List<Int> {
        val rawCounts = reviews
            .map { it.rating }
            .groupingBy { it }
            .eachCount()

        return (1..5).map { rawCounts[it] ?: 0 }
    }

    val ratingCounts = getRatingCounts(reviews)
    val ratingDistribution = ratingCounts.map {
        if (it > 0) it.toFloat() / reviews.size else 0.0f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        ratingDistribution.forEachIndexed { index, progress ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "${5 - index}", modifier = Modifier.width(24.dp))
                // Animate the progress value
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(durationMillis = 600),
                    label = "Rating Bar Animation",
                )
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF4CAF50),
                    trackColor = Color.LightGray,
                    strokeCap = StrokeCap.Round,
                    gapSize = (-5).dp,
                    drawStopIndicator = {},
                )
            }
        }
    }
}

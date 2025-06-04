package com.roadrater.ui.home.tabs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Output
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.tab.TabOptions
import coil3.compose.rememberAsyncImagePainter
import com.roadrater.R
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.User
import com.roadrater.presentation.components.CarWatchingCard
import com.roadrater.presentation.components.ReviewCard
import com.roadrater.presentation.components.StarRating
import com.roadrater.presentation.util.Tab
import com.roadrater.ui.theme.spacing
import org.koin.java.KoinJavaComponent.getKoin

object ProfileTab : Tab {
    private fun readResolve(): Any = ProfileTab

    override val options: TabOptions
        @Composable
        get() {
            val image = rememberVectorPainter(Icons.Outlined.Person)
            return TabOptions(
                index = 2u,
                title = stringResource(R.string.profile_tab),
                icon = image,
            )
        }
    @Composable
    override fun Content() {

        val screenModel = rememberScreenModel {
            getKoin().get<ProfileTabScreenModel>()
        }

        val currentUser = screenModel.currentUser
        var selectedPrimaryTab = screenModel.selectedPrimaryTab.collectAsState()

        val primaryTabTitles = listOf<String>("Reviews", "Vehicles", "More")

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(30.dp)
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                UserDetails(currentUser)
            }

            PrimaryTabRow(selectedTabIndex = selectedPrimaryTab.value) {
                primaryTabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedPrimaryTab.value == index,
                        onClick = { screenModel.setSelectedPrimaryTab(index) },
                        text = { Text(title, modifier = Modifier.padding(10.dp)) }
                    )
                }
            }
            AnimatedContent(
                targetState = selectedPrimaryTab.value,
                transitionSpec = {
                    // Example: slide left when increasing tab index, right when decreasing
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }.using(SizeTransform(clip = false))
                },
                label = "Sliding Tab Transition"
            ) { target ->
                when (target) {
                    0 -> ReviewsTabContent(screenModel)
                    1 -> VehiclesTabContent(screenModel)
                    2 -> MoreTabContent()
                }
            }
        }
    }

    @Composable
    fun MoreTabContent() {
        Text("More stuff")
    }

    @Composable
    fun VehiclesTabContent(screenModel: ProfileTabScreenModel) {
        val watchedCars by screenModel.watchedCars.collectAsState()
        if (watchedCars.isEmpty()) {
            Text(
                text = "No cars being watched.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
            )
        } else {
            LazyColumn {
                items(watchedCars) {
                    CarWatchingCard(car = it, onClick = {})
                }
            }
        }
    }

    @Composable
    fun ReviewsTabContent(screenModel: ProfileTabScreenModel) {
        val reviewsGiven by screenModel.reviewsGiven.collectAsState()
        val reviewsReceived by screenModel.reviewsReceived.collectAsState()
        var selectedSecondaryTab = screenModel.selectedSecondaryTab.collectAsState()

        val secondaryTabTitles = listOf<String>("Given", "Received")
        val reviewsForTab = if (selectedSecondaryTab.value == 0) reviewsGiven else reviewsReceived

        Column {
            SecondaryTabRow(selectedTabIndex = selectedSecondaryTab.value) {
                secondaryTabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedSecondaryTab.value == index,
                        onClick = { screenModel.setSelectedSecondaryTab(index) },
                        text = {
                            BadgedBox(badge = {
                                if ((index == 0 && reviewsGiven.isNotEmpty()) || index == 1 && reviewsReceived.isNotEmpty()) Badge {
                                    Text(
                                        if (index == 0) reviewsGiven.size.toString() else reviewsReceived.size.toString()
                                    )
                                }
                            }) {
                                Text(title, modifier = Modifier.padding(10.dp))
                            }
                        }
                    )
                }
            }
            if (reviewsForTab.isEmpty()) {
                Text(
                    text = "No reviews available.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn {
                    item {
                        ReviewSummary(reviewsForTab)
                    }
                    items(reviewsForTab) { review ->
                        ReviewCard(review = review)
                    }
                }
            }
        }
    }

    @Composable
    fun ReviewSummary(reviewsForTab: List<Review>) {
        var average = reviewsForTab.map { it.rating.toDouble() }.average()
        var averageFormatted = "%.1f".format(average)
        Column (
            modifier = Modifier.padding(horizontal = 15.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                AnimatedContent(
                    targetState = average.toInt(),
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "StarRatingFade"
                ) { average ->
                    StarRating(average, 40.dp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                AnimatedContent(targetState = averageFormatted,
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
                            SizeTransform(clip = false)
                        )
                    }, label = "animated content") { averageFormatted ->
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
    fun UserDetails(currentUser: User?) {
        Row {
            // Show profile picture if available
            currentUser?.profile_pic_url?.let { imageUrl ->
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                )
            }
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Text(
                    text = currentUser!!.name ?: currentUser.nickname ?: "Guest User",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = currentUser.email ?: ""
                )
            }
        }
    }

    @Composable
    fun UserStatistics(
        reviewsGiven: List<Review>,
        reviewsReceived: List<Review>
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatItem(
                icon = Icons.Outlined.RateReview,
                value = reviewsGiven.size.toString(),
                label = "Reviews",
            )
            StatItem(
                icon = Icons.Outlined.Star,
                value = if (reviewsReceived.isNotEmpty()) {
                    reviewsReceived.map { it.rating.toDouble() }.average().toString()
                } else {
                    "0.0"
                },
                label = "Rating",
            )
        }

    }

}


// Shows a single stat (like number of reviews)
@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
            .fillMaxWidth()
    ) {
        ratingDistribution.forEachIndexed { index, progress ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${5 - index}", modifier = Modifier.width(24.dp))
                // Animate the progress value
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(durationMillis = 600),
                    label = "Rating Bar Animation"
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
                    drawStopIndicator = {}
                )
            }
        }
    }
}

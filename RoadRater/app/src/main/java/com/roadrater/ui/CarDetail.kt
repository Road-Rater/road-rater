package com.roadrater.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DirectionsCarFilled
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.roadrater.R
import com.roadrater.database.entities.Car
import com.roadrater.database.entities.Review
import com.roadrater.ui.ReviewCard
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import com.roadrater.ui.newReviewScreen.NewReviewScreen
import io.github.jan.supabase.auth.auth


data class CarDetail(val plate: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val supabaseClient = koinInject<SupabaseClient>()
        val car = remember { mutableStateOf<Car?>(null) }
        val reviews = remember { mutableStateOf<List<Review>>(emptyList()) }
        var searchHistory by remember { mutableStateOf(listOf<String>()) }
        val userId = "testId"

        LaunchedEffect(plate) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Fetch car details (case-insensitive)
                    val carResult = supabaseClient.from("cars")
                        .select {
                            filter {
                                ilike("number_plate", plate)
                            }
                        }
                        .decodeSingleOrNull<Car>()
                    car.value = carResult

                    if (carResult != null) {
                        // Fetch reviews from 'reviews' table (case-insensitive)
                        val reviewsResult = supabaseClient.from("reviews")
                            .select {
                                filter {
                                    ilike("number_plate", plate)
                                }
                                order("created_at", Order.DESCENDING)
                            }
                            .decodeList<Review>()
                        val reviewList = reviewsResult.map { review ->
                            val dateTime = try {
                                val odt = OffsetDateTime.parse(review.createdAt)
                                odt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                            } catch (e: Exception) {
                                ""
                            }
                            Review(
                                title = review.title,
                                createdAt = dateTime,
                                labels = review.labels ?: emptyList(),
                                description = review.description,
                                rating = review.rating,
                                createdBy = review.createdBy,
                                numberPlate = review.numberPlate,
                            )
                        }
                        reviews.value = reviewList
                    } else {
                        reviews.value = emptyList()
                    }
                } catch (e: Exception) {
                    println("Error fetching car details: ${e.message}")
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.car_details)) },
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        val userId = "testId"
                        if (userId != null) {
                            navigator.push(NewReviewScreen(numberPlate = plate))
                        }
                    },
                ) {
                    Icon(Icons.Filled.Add, "Add review")
                }
            },
        ) { innerPadding ->
            if (car.value == null) {
                Text("Car not found.", modifier = Modifier.padding(16.dp))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DirectionsCarFilled,
                        contentDescription = "Car Icon",
                        modifier = Modifier
                            .size(64.dp)
                            .padding(bottom = 16.dp),
                    )

                    Text(
                        text = plate.uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                    )

                    Text(
                        text = "${car.value?.make ?: ""} ${car.value?.model ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                    )

                    Text(
                        text = car.value?.year ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 20.dp),
                    )

                    Text(
                        text = "Reviews",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 8.dp),
                    )

                    if (reviews.value.isEmpty()) {
                        Text("No reviews yet.", modifier = Modifier.padding(16.dp))
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        ) {
                            items(reviews.value) { review ->
                                ReviewCard(review)
                            }
                        }
                    }
                }
            }
        }
    }
}

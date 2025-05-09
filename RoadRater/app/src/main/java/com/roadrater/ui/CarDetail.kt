package com.roadrater.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DirectionsCarFilled
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.roadrater.ui.newReviewScreen.NewReviewScreen
import com.roadrater.ui.ReviewCard
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

data class CarDetail(val plate: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val supabaseClient = koinInject<SupabaseClient>()
        val car = remember { mutableStateOf<Car?>(null) }
        val reviews = remember { mutableStateOf<List<Review>>(emptyList()) }
        var showDialog by remember { mutableStateOf(false) }
        val userId = "testId" // Replace with actual userId if needed

        LaunchedEffect(plate) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val carResult = supabaseClient.from("cars")
                        .select {
                            filter {
                                ilike("number_plate", plate)
                            }
                        }
                        .decodeSingleOrNull<Car>()
                    car.value = carResult

                    if (carResult != null) {
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
                        navigator.push(NewReviewScreen(numberPlate = plate))
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

                    // REMOVE CAR BUTTON
                    Button(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Remove, contentDescription = "Remove")
                        Text("Remove from Watchlist", modifier = Modifier.padding(start = 8.dp))
                    }

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

        // REMOVE CAR DIALOG
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text("Remove Car from Watchlist?")
                },
                text = {
                    Text("Are you sure you want to remove $plate from your watchlist?")
                },
                confirmButton = {
                    TextButton(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            supabaseClient.from("watched_cars")
                                .delete {
                                    filter {
                                        eq("number_plate", plate)
                                        // Optionally: eq("user_id", userId)
                                    }
                                }
                        }
                        showDialog = false
                        navigator.pop()
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
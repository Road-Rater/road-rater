package com.roadrater.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DirectionsCarFilled
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.roadrater.R
import com.roadrater.database.entities.Car
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.TableUser
import com.roadrater.presentation.components.ReviewsDisplay
import com.roadrater.ui.newReviewScreen.NewReviewScreen
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent

class CarDetailScreen(val plate: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val supabaseClient = koinInject<SupabaseClient>()

        val screenModel = rememberScreenModel {
            KoinJavaComponent.getKoin().get<CarDetailScreenModel>(parameters = { parametersOf(plate) })
        }

        val car = screenModel.car.value
        val reviewsAndReviewers = screenModel.reviewsAndReviewers.value

        var sortAsc by remember { mutableStateOf(true) } // true = Oldest First, false = Newest First
        var showDialog by remember { mutableStateOf(false) }

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
            if (car == null) {
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
                        text = "${car.make ?: ""} ${car.model ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                    )

                    Text(
                        text = car.year ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 20.dp),
                    )

                    // REMOVE CAR BUTTON
                    Button(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
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

                    // Only show sort UI if there are reviews
                    if (reviewsAndReviewers.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Sort by: Date", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(16.dp))
                            // Toggle sort order with a clear label
                            OutlinedButton(onClick = { sortAsc = !sortAsc }) {
                                Text(if (sortAsc) "Oldest First" else "Newest First")
                            }
                        }
                    }

                    // Apply sorting to reviews (by date only)
                    val sortedReviewsAndReviewers = if (sortAsc) {
                        reviewsAndReviewers.entries
                            .sortedBy { it.key.createdAt }
                            .associate { it.key to it.value }
                    } else {
                        reviewsAndReviewers.entries
                            .sortedByDescending { it.key.createdAt }
                            .associate { it.key to it.value }
                    }

                    if (sortedReviewsAndReviewers.isEmpty()) {
                        Text("No reviews yet.", modifier = Modifier.padding(16.dp))
                    } else {
                        ReviewsDisplay(modifier = Modifier, sortedReviewsAndReviewers)
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
                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
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
                },
            )
        }
    }
}

package com.roadrater.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DirectionsCarFilled
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.roadrater.R
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.presentation.components.RemoveCarDialog
import com.roadrater.presentation.components.ReviewsDisplay
import com.roadrater.ui.newReviewScreen.AddReviewScreen
import io.github.jan.supabase.SupabaseClient
import org.koin.compose.koinInject

class CarDetailsScreen(val plate: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val supabaseClient = koinInject<SupabaseClient>()
        val generalPreferences = koinInject<GeneralPreferences>()
        val currentUser = generalPreferences.user.get()
        val screenModel = rememberScreenModel { CarDetailsScreenModel(supabaseClient, plate, currentUser!!.uid) }
        val car by screenModel.car.collectAsState()
        val isWatching by screenModel.isWatching.collectAsState()
        val reviewsAndReviewers by screenModel.reviewsAndReviewers.collectAsState()
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
                        navigator.push(AddReviewScreen(numberPlate = plate))
                    },
                ) {
                    Icon(Icons.Filled.Add, "Add review")
                }
            },
        ) { innerPadding ->
            if (car == null) {
                Text(stringResource(R.string.car_not_found), modifier = Modifier.padding(16.dp))
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
                        text = "${car?.make ?: ""} ${car?.model ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                    )

                    Text(
                        text = car?.year ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 20.dp),
                    )

                    // REMOVE CAR BUTTON
                    Button(
                        onClick = {
                            if (isWatching) {
                                showDialog = true
                            } else {
                                screenModel.watchCar()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                    ) {
                        Icon(imageVector = if (isWatching) Icons.Outlined.Remove else Icons.Outlined.Add, contentDescription = "Toggle Watched State")
                        Text(if (isWatching) stringResource(R.string.remove_watchlist) else stringResource(R.string.add_watchlist), modifier = Modifier.padding(start = 8.dp))
                    }

                    Text(
                        text = stringResource(R.string.reviews),
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
                            Text(stringResource(R.string.sort_by_date), style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(16.dp))
                            // Toggle sort order with a clear label
                            OutlinedButton(onClick = { sortAsc = !sortAsc }) {
                                Text(if (sortAsc) stringResource(R.string.oldest_first) else stringResource(R.string.newest_first))
                            }
                        }
                    }

                    // Apply sorting to reviews (by date only)
                    val sortedReviews = if (sortAsc) {
                        reviewsAndReviewers.entries
                            .sortedBy { it.key.createdAt }
                            .associate { it.toPair() }
                    } else {
                        reviewsAndReviewers.entries
                            .sortedByDescending { it.key.createdAt }
                            .associate { it.toPair() }
                    }

                    if (sortedReviews.isEmpty()) {
                        Text(stringResource(R.string.no_reviews), modifier = Modifier.padding(16.dp))
                    } else {
                        ReviewsDisplay(Modifier, sortedReviews)
                    }
                }
            }
        }

        // REMOVE CAR DIALOG
        if (showDialog) {
            RemoveCarDialog(
                onDismissRequest = { showDialog = false },
                onConfirm = {
                    screenModel.unwatchCar(plate)
                },
                numberPlate = plate,
            )
        }
    }
}

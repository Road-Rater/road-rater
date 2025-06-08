package com.roadrater.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.roadrater.R
import com.roadrater.database.entities.Review
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.presentation.Screen
import com.roadrater.presentation.components.ReviewCard
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

object FlaggedMessagesScreen : Screen() {
    private fun readResolve(): Any = FlaggedMessagesScreen

    @Composable
    override fun Content() {
        val supabaseClient = koinInject<SupabaseClient>()
        val generalPreferences = koinInject<GeneralPreferences>()
        val reviews = remember { mutableStateListOf<Review>() }
        val navigator = LocalNavigator.currentOrThrow

        fun loadFlaggedReviews() {
            CoroutineScope(Dispatchers.IO).launch {
                val reviewsResult = supabaseClient.from("reviews")
                    .select {
                        filter {
                            eq("is_flagged", true)
                        }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<Review>()
                reviews.clear() // Clear the existing list
                reviews.addAll(reviewsResult) // Add new reviews
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.flagged_messages)) },
                )
            },
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                // Load flagged reviews from the database
                LaunchedEffect(Unit) {
                    loadFlaggedReviews()
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f),
                ) {
                    items(reviews) { review ->
                        ReviewCard(
                            review = review,
                            onNumberPlateClick = {
                                navigator.push(CarDetailsScreen(review.numberPlate))
                            },
                            supabaseClient = supabaseClient,
                            onModChange = {
                                reviews.remove(review) // Remove the review from the list
                            },
                        )
                    }
                }
            }
        }
    }
}

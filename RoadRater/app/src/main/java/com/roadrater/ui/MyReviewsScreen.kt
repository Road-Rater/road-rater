package com.roadrater.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.User
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.presentation.Screen
import com.roadrater.presentation.components.ReviewCard
import com.roadrater.ui.CarDetailsScreen
import com.roadrater.presentation.components.ReviewsDisplay
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.collections.plus

object MyReviewsScreen : Screen() {
    private fun readResolve(): Any = MyReviewsScreen

    @Composable
    override fun Content() {
        val supabaseClient = koinInject<SupabaseClient>()
        val generalPreferences = koinInject<GeneralPreferences>()
        val currentUser = generalPreferences.user.get()
        val reviews = remember { mutableStateOf<List<Review>>(emptyList()) }
        val reviewsAndReviewers = remember { mutableStateOf<Map<Review, User>>(emptyMap()) }
        // List of labels for filtering reviews
        val labels = listOf("All", "Speeding", "Safe", "Reckless")
        var selectedLabel by remember { mutableStateOf("All") }
        var sortOption by remember { mutableStateOf("Date") } // "Date" or "Title"
        var sortAsc by remember { mutableStateOf(true) }
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("My Reviews") },
                )
            },
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                // Load reviews for the current user from the database
                LaunchedEffect(currentUser?.uid) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val reviewsResult = supabaseClient.from("reviews")
                            .select {
                                filter {
                                    eq("created_by", currentUser!!.uid)
                                }
                                order("created_at", Order.DESCENDING)
                            }
                            .decodeList<Review>()
                        reviews.value = reviewsResult

                        val reviewerIds = reviewsResult.map {
                            it.createdBy
                        }

                        val reviewersResult = supabaseClient.from("users")
                            .select {
                                filter {
                                    isIn("uid", reviewerIds)
                                }
                            }
                            .decodeList<User>()

                        reviewsResult.forEach { review ->
                            val reviewer = reviewersResult.find {
                                it.uid == review.createdBy
                            }
                            if (reviewer != null) {
                                reviewsAndReviewers.value += Pair(review, reviewer)
                            }
                        }
                    }
                }

                // Filter and sort reviews based on user selection
                val filteredReviews = reviewsAndReviewers.value.entries
                    .filter { (review, _) ->
                        selectedLabel == "All" || review.labels.contains(selectedLabel)
                    }
                    .let { entries ->
                        when (sortOption) {
                            "Title" -> if (sortAsc) {
                                entries.sortedBy { it.key.title }
                            } else {
                                entries.sortedByDescending { it.key.title }
                            }
                            else -> if (sortAsc) {
                                entries.sortedBy { it.key.createdAt }
                            } else {
                                entries.sortedByDescending { it.key.createdAt }
                            }
                        }
                    }
                    .associate { it.toPair() } // Converts the sorted list of Map.Entry back into a Map

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    var sortExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = sortExpanded,
                        onExpandedChange = { sortExpanded = !sortExpanded },
                    ) {
                        OutlinedTextField(
                            value = "Sort by: $sortOption",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor()
                                .weight(1f),
                            label = { Text("Sort") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded)
                            },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        )

                        ExposedDropdownMenu(
                            expanded = sortExpanded,
                            onDismissRequest = { sortExpanded = false },
                        ) {
                            listOf("Date", "Title").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        sortOption = option
                                        sortExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    OutlinedButton(onClick = { sortAsc = !sortAsc }) {
                        Icon(
                            imageVector = if (sortAsc) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = "Toggle sort order",
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                ReviewsDisplay(Modifier, filteredReviews)
            }
        }
    }
}

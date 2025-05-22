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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
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
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.roadrater.ui.CarDetailsScreen

object MyReviewsScreen : Screen() {
    private fun readResolve(): Any = MyReviewsScreen

    @Composable
    override fun Content() {
        val supabaseClient = koinInject<SupabaseClient>()
        val generalPreferences = koinInject<GeneralPreferences>()
        val currentUser = generalPreferences.user.get()
        val reviews = remember { mutableStateOf<List<Review>>(emptyList()) }
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
                    }
                }

                // Filter and sort reviews based on user selection
                val filteredReviews = reviews.value.let {
                    when (sortOption) {
                        "Title" -> if (sortAsc) {
                            it.sortedBy { review -> review.title }
                        } else {
                            it.sortedByDescending { review -> review.title }
                        }
                        else -> if (sortAsc) {
                            it.sortedBy { review -> review.createdAt }
                        } else {
                            it.sortedByDescending { review -> review.createdAt }
                        }
                    }
                }

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

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    items(filteredReviews) { review ->
                        ReviewCard(
                            review = review,
                            onNumberPlateClick = { plate ->
                                navigator.push(CarDetailsScreen(plate))
                            }
                        )
                    }
                }
            }
        }
    }
}
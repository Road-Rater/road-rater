package com.roadrater.ui.home.tabs

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DirectionsCarFilled
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabOptions
import coil3.compose.AsyncImage
import com.roadrater.R
import com.roadrater.database.entities.TableUser
import com.roadrater.database.entities.WatchedCar
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.presentation.components.ReviewsDisplay
import com.roadrater.presentation.util.Tab
import com.roadrater.ui.CarDetailsScreen
import com.roadrater.ui.ReviewDetailsScreen
import com.roadrater.utils.GetCarInfo
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent

object HomeTab : Tab {
    private fun readResolve(): Any = HomeTab

    override val options: TabOptions
        @Composable
        get() {
            // Tab icon and label for the bottom navigation
            val image = rememberVectorPainter(Icons.Outlined.DirectionsCarFilled)
            return TabOptions(
                index = 0u,
                title = stringResource(R.string.home_tab),
                icon = image,
            )
        }

    @Composable
    fun FilterButton(
        label: String,
        isSelected: Boolean,
        onSelect: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val backgroundColor = if (isSelected) {
            Color(0xd97757) // Fixed color format
        } else {
            Color(0xFF2A2A2A) // Dark gray for unselected
        }

        val textColor = Color.White

        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .clickable { onSelect() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            )
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    override fun Content() {
        val supabaseClient = koinInject<SupabaseClient>()
        val generalPreferences = koinInject<GeneralPreferences>()
        val currentUser = generalPreferences.user.get()
        var selectedLabel by remember { mutableStateOf("All") }
        var sortOption by remember { mutableStateOf("Date") } // "Date" or "Title"
        var sortAsc by remember { mutableStateOf(true) }
        val navigator = LocalNavigator.currentOrThrow

        // ViewModel for managing reviews and state
        val screenModel = rememberScreenModel {
            KoinJavaComponent.getKoin().get<HomeTabScreenModel>(parameters = { parametersOf(currentUser) })
        }

        var searchHistory by rememberSaveable { mutableStateOf(listOf<String>()) }
        var searchResults by remember { mutableStateOf(listOf<String>()) }
        var text by remember { mutableStateOf("") }
        var active by remember { mutableStateOf(false) }
        var noResults by remember { mutableStateOf(false) }
        var userResults by remember { mutableStateOf<Map<String, List<TableUser>>>(emptyMap()) }
        var pendingNavigationPlate by remember { mutableStateOf<String?>(null) }

        // List of reviews for the home feed
        val reviews = screenModel.reviewsAndReviewers.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Home") },
                    actions = {
                        // Show user's profile picture in the top bar
                        AsyncImage(
                            model = currentUser?.profile_pic_url,
                            contentDescription = "Profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { },
                        )
                    },
                )
            },
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                // Search bar for searching cars by plate
                SearchBar(
                    modifier = Modifier.fillMaxWidth(),
                    query = text,
                    onQueryChange = { newText ->
                        text = newText
                        noResults = false
                        if (newText.isNotBlank()) {
                            // Search for cars in the database
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val results = supabaseClient.from("cars")
                                        .select {
                                            filter {
                                                ilike("number_plate", "%$newText%")
                                            }
                                            limit(10)
                                        }
                                        .decodeList<Map<String, String>>()
                                    searchResults = results.map { it["number_plate"]?.uppercase() ?: "" }
                                } catch (e: Exception) {
                                    println("Error searching cars: ${e.message}")
                                }
                            }
                        } else {
                            searchResults = emptyList()
                        }
                    },
                    onSearch = {
                        if (text.isNotBlank()) {
                            val upperText = text.uppercase()
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    // Check if car exists in the database
                                    val carExists = supabaseClient.from("cars")
                                        .select {
                                            filter {
                                                ilike("number_plate", upperText)
                                            }
                                            limit(1)
                                        }
                                        .decodeList<Map<String, String>>()
                                        .isNotEmpty()
                                    if (carExists) {
                                        // Fetch users linked to this car
                                        val watchedUsers = supabaseClient.from("watched_cars")
                                            .select { filter { eq("number_plate", upperText) } }
                                            .decodeList<WatchedCar>()
                                        val userIds = watchedUsers.map { it.uid }
                                        val users = if (userIds.isNotEmpty()) {
                                            supabaseClient.from("users")
                                                .select()
                                                .decodeList<TableUser>()
                                                .filter { it.uid in userIds }
                                        } else {
                                            emptyList()
                                        }
                                        userResults = userResults + (upperText to users)
                                        searchHistory = listOf(upperText) + searchHistory.filter { it != upperText }
                                        pendingNavigationPlate = upperText
                                        active = false
                                        text = ""
                                        noResults = false
                                    } else {
                                        // Try to scrape car info if not found
                                        val scrapedCar = try {
                                            GetCarInfo.getCarInfo(upperText)
                                        } catch (e: Exception) {
                                            null
                                        }
                                        if (scrapedCar != null && scrapedCar.number_plate.isNotBlank()) {
                                            // Insert scraped car into Supabase
                                            supabaseClient.from("cars").insert(scrapedCar)
                                            searchHistory = listOf(upperText) + searchHistory.filter { it != upperText }
                                            pendingNavigationPlate = upperText
                                            active = false
                                            text = ""
                                            noResults = false
                                        } else {
                                            noResults = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    println("Error searching for car: ${e.message}")
                                }
                            }
                        }
                    },
                    active = active,
                    onActiveChange = {
                        active = it
                    },
                    placeholder = {
                        Text(stringResource(R.string.search_tab))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                        )
                    },
                    trailingIcon = {
                        if (active) {
                            Icon(
                                modifier = Modifier.clickable {
                                    if (text.isNotEmpty()) {
                                        text = ""
                                    } else {
                                        active = false
                                    }
                                },
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Icon",
                            )
                        }
                    },
                ) {
                    if (text.isBlank()) {
                        if (searchHistory.isEmpty()) {
                            Text(stringResource(R.string.search_no_history), modifier = Modifier.padding(14.dp))
                        } else {
                            // Show previous search history
                            searchHistory.forEach { plate ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchHistory = listOf(plate) + searchHistory.filter { it != plate }
                                            navigator.push(CarDetailsScreen(plate))
                                            text = ""
                                            active = false
                                        }
                                        .padding(all = 14.dp),
                                ) {
                                    Icon(
                                        modifier = Modifier.padding(end = 10.dp),
                                        imageVector = Icons.Default.History,
                                        contentDescription = "History Icon",
                                    )
                                    Text(text = plate)
                                }
                            }
                        }
                    } else {
                        if (noResults || searchResults.isEmpty()) {
                            Text(
                                text = stringResource(R.string.no_results),
                                modifier = Modifier.padding(14.dp),
                            )
                        } else {
                            // Show search results for car plates
                            searchResults.forEach { plate ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                searchHistory = listOf(plate) + searchHistory.filter { it != plate }
                                                navigator.push(CarDetailsScreen(plate))
                                                text = ""
                                                active = false
                                            },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.DirectionsCarFilled,
                                            contentDescription = "Car Icon",
                                            modifier = Modifier.padding(end = 10.dp),
                                        )
                                        Text(text = plate)
                                    }
                                }
                            }
                        }
                    }
                }

                // Calculate available labels from reviews
                val availableLabels = remember(reviews) {
                    val allLabels = mutableSetOf<String>()
                    reviews.forEach { review ->
                        review.labels.forEach { label ->
                            if (label.isNotEmpty()) {
                                allLabels.add(label)
                            }
                        }
                    }
                    listOf("All") + allLabels.sorted()
                }

                // Filter buttons using LazyRow
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    items(availableLabels) { label ->
                        FilterButton(
                            label = label,
                            isSelected = selectedLabel == label,
                            onSelect = { selectedLabel = label },
                        )
                    }
                }

                // Sort Controls
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
                        Text(if (sortAsc) "" else "")
                    }
                }

                // Filter and sort reviews based on user selection
                val filteredReviews = reviews.value.entries
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
                    
                // Show filtered count
                Text(
                    text = "Showing ${filteredReviews.size} of ${reviews.size} reviews",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                
                // Show a list of reviews on the home screen
                ReviewsDisplay(Modifier.padding(paddingValues), reviews.value)
                // Navigate to car detail screen if needed
                LaunchedEffect(pendingNavigationPlate) {
                    pendingNavigationPlate?.let { plate ->
                        navigator.push(CarDetailsScreen(plate))
                        pendingNavigationPlate = null
                    }
                }
            }
        }
    }
}

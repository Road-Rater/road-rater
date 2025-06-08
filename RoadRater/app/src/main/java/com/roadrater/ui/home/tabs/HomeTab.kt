package com.roadrater.ui.home.tabs

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DirectionsCarFilled
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabOptions
import coil3.compose.AsyncImage
import com.roadrater.R
import com.roadrater.database.entities.TableUser
import com.roadrater.database.entities.WatchedCar
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.presentation.components.ReviewCard
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

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val supabaseClient = koinInject<SupabaseClient>()
        val generalPreferences = koinInject<GeneralPreferences>()
        val currentUser = generalPreferences.user.get()
        // ViewModel for managing reviews and state
        val screenModel = rememberScreenModel {
            HomeTabScreenModel(
                supabaseClient = supabaseClient,
                currentUser!!.uid,
            )
        }
        var searchHistory by rememberSaveable { mutableStateOf(listOf<String>()) }
        var searchResults by remember { mutableStateOf(listOf<String>()) }
        var text by remember { mutableStateOf("") }
        var active by remember { mutableStateOf(false) }
        var noResults by remember { mutableStateOf(false) }
        var userResults by remember { mutableStateOf<Map<String, List<TableUser>>>(emptyMap()) }
        var pendingNavigationPlate by remember { mutableStateOf<String?>(null) }

        // List of reviews for the home feed
        val reviews by screenModel.reviews.collectAsState()

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
            floatingActionButton = {},
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                Scaffold {
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
                    // Show a list of reviews on the home screen
                    LazyColumn(modifier = Modifier.padding(paddingValues)) {
                        items(reviews) { review ->
                            ReviewCard(
                                review = review,
                                onClick = {
                                    review.id?.let { id ->
                                        navigator.push(ReviewDetailsScreen(id.toString()))
                                    }
                                },
                            )
                        }
                    }
                }
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

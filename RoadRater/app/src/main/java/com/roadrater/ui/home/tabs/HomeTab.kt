package com.roadrater.ui.home.tabs

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabOptions
import coil3.compose.AsyncImage
import com.google.android.gms.auth.api.identity.Identity
import com.roadrater.R
import com.roadrater.auth.GoogleAuthUiClient
import com.roadrater.presentation.util.Tab
import com.roadrater.ui.CarDetail
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
        val currentUser =
            GoogleAuthUiClient(context, Identity.getSignInClient(context)).getSignedInUser()

        var recentSearches by remember { mutableStateOf(listOf<String>()) }
        var searchResults by remember { mutableStateOf(listOf<String>()) }
        var text by remember { mutableStateOf("") }
        var active by remember { mutableStateOf(false) }

        // Fetch recent searches when component mounts
        LaunchedEffect(Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val cars = supabaseClient.from("cars")
                        .select {
                            limit(5)
                        }
                        .decodeList<Map<String, String>>()
                    recentSearches = cars.map { it["number_plate"] ?: "" }
                } catch (e: Exception) {
                    println("Error fetching recent searches: ${e.message}")
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Home") },
                    actions = {
                        AsyncImage(
                            model = currentUser?.profilePictureUrl,
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
                    SearchBar(
                        modifier = Modifier.fillMaxWidth(),
                        query = text,
                        onQueryChange = { newText ->
                            text = newText
                            if (newText.isNotBlank()) {
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
                                        searchResults = results.map { it["number_plate"] ?: "" }
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
                                navigator.push(CarDetail(text))
                                active = false
                                text = ""
                            }
                        },
                        active = active,
                        onActiveChange = {
                            active = it
                        },
                        placeholder = {
                            Text(text = "Search")
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
                            recentSearches.forEach { plate ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { navigator.push(CarDetail(plate)) }
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
                        } else {
                            if (searchResults.isEmpty()) {
                                Text(
                                    text = "No results found",
                                    modifier = Modifier.padding(14.dp),
                                )
                            } else {
                                searchResults.forEach { plate ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { navigator.push(CarDetail(plate)) }
                                            .padding(14.dp),
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
            }
        }
    }
}

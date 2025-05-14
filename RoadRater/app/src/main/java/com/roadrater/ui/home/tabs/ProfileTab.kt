package com.roadrater.ui.home.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.TabOptions
import coil3.compose.rememberAsyncImagePainter
import com.roadrater.R
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.TableUser
import com.roadrater.database.entities.WatchedCar
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.presentation.util.Tab
import com.roadrater.ui.theme.spacing
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

object ProfileTab : Tab {
    private fun readResolve(): Any = ProfileTab

    override val options: TabOptions
        @Composable
        get() {
            val image = rememberVectorPainter(Icons.Outlined.Person)
            return TabOptions(
                index = 2u,
                title = stringResource(R.string.profile_tab),
                icon = image,
            )
        }

    @Composable
    override fun Content() {
        // Get user and database client from dependency injection
        val supabaseClient = koinInject<SupabaseClient>()
        val generalPreferences = koinInject<GeneralPreferences>()
        val currentUser = generalPreferences.user.get()
        val user = remember { mutableStateOf<TableUser?>(null) }
        val reviews = remember { mutableStateOf<List<Review>>(emptyList()) }
        val watchedCars = remember { mutableStateOf<List<WatchedCar>>(emptyList()) }

        // Load user info, reviews, and watched cars from the database
        LaunchedEffect(currentUser?.uid) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Get user details from Supabase
                    val userResult = supabaseClient.from("users")
                        .select { filter { eq("uid", currentUser!!.uid) } }
                        .decodeSingleOrNull<TableUser>()
                    user.value = userResult

                    // Get all reviews written by this user
                    val reviewsResult = supabaseClient.from("reviews")
                        .select { filter { eq("created_by", currentUser!!.uid) } }
                        .decodeList<Review>()
                    reviews.value = reviewsResult

                    // Get all cars this user is watching
                    val watchedCarsResult = supabaseClient.from("watched_cars")
                        .select { filter { eq("uid", currentUser!!.uid) } }
                        .decodeList<WatchedCar>()
                    watchedCars.value = watchedCarsResult
                } catch (e: Exception) {
                    println("Error fetching profile data: ${e.message}")
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            // Show user profile info
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Show profile picture if available
                    currentUser?.profile_pic_url?.let { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Show user's name or nickname
                    Text(
                        text = user.value?.name ?: currentUser?.nickname ?: stringResource(R.string.guest_user),
                        style = MaterialTheme.typography.headlineSmall,
                    )

                    // Show nickname if available
                    if (user.value?.nickname != null) {
                        Text(
                            text = user.value?.nickname ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Show email or 'Guest Account' if not signed in
                    Text(
                        text = currentUser?.email ?: stringResource(R.string.guest_account),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Show some user stats
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.statistics),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // Number of reviews
                        StatItem(
                            icon = Icons.Outlined.RateReview,
                            value = reviews.value.size.toString(),
                            label = stringResource(R.string.reviews),
                        )
                        // Number of watched cars
                        StatItem(
                            icon = Icons.Outlined.DirectionsCar,
                            value = watchedCars.value.size.toString(),
                            label = stringResource(R.string.watched_cars),
                        )
                        // Average rating given by user
                        StatItem(
                            icon = Icons.Outlined.Star,
                            value = if (reviews.value.isNotEmpty()) {
                                String.format("%.1f", reviews.value.map { it.rating.toDouble() }.average())
                            } else {
                                "0.0"
                            },
                            label = stringResource(R.string.avg_rating),
                        )
                    }
                }
            }
        }
    }
}

// Shows a single stat (like number of reviews)
@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(32.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

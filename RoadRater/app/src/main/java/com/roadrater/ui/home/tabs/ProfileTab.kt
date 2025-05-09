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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
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
        val context = LocalContext.current
        val supabaseClient = koinInject<SupabaseClient>()
        val generalPreferences = koinInject<GeneralPreferences>()
        val currentUser = generalPreferences.user.get()
        val user = remember { mutableStateOf<TableUser?>(null) }
        val reviews = remember { mutableStateOf<List<Review>>(emptyList()) }
        val watchedCars = remember { mutableStateOf<List<WatchedCar>>(emptyList()) }

        LaunchedEffect(currentUser?.uid) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Fetch user details
                    val userResult = supabaseClient.from("users")
                        .select { filter { eq("uid", currentUser!!.uid) } }
                        .decodeSingleOrNull<TableUser>()
                    user.value = userResult

                    // Fetch user's reviews
                    val reviewsResult = supabaseClient.from("reviews")
                        .select { filter { eq("created_by", currentUser!!.uid) } }
                        .decodeList<Review>()
                    reviews.value = reviewsResult

                    // Fetch watched cars
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
            // Profile Section
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
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

                    Text(
                        text = user.value?.name ?: currentUser?.nickname ?: "Guest User",
                        style = MaterialTheme.typography.headlineSmall,
                    )

                    if (user.value?.nickname != null) {
                        Text(
                            text = user.value?.nickname ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Text(
                        text = currentUser?.email ?: "Guest Account",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Statistics Section
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "Statistics",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        StatItem(
                            icon = Icons.Outlined.RateReview,
                            value = reviews.value.size.toString(),
                            label = "Reviews",
                        )
                        StatItem(
                            icon = Icons.Outlined.DirectionsCar,
                            value = watchedCars.value.size.toString(),
                            label = "Watched Cars",
                        )
                        StatItem(
                            icon = Icons.Outlined.Star,
                            value = if (reviews.value.isNotEmpty()) {
                                String.format("%.1f", reviews.value.map { it.rating.toDouble() }.average())
                            } else {
                                "0.0"
                            },
                            label = "Avg Rating",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

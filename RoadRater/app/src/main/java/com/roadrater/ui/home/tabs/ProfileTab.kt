package com.roadrater.ui.home.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.tab.TabOptions
import coil3.compose.rememberAsyncImagePainter
import com.roadrater.R
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.TableUser
import com.roadrater.database.entities.User
import com.roadrater.database.entities.WatchedCar
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.presentation.components.ReviewCard
import com.roadrater.presentation.util.Tab
import com.roadrater.ui.theme.spacing
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.java.KoinJavaComponent.getKoin

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

        val screenModel = rememberScreenModel {
            getKoin().get<ProfileTabScreenModel>()
        }

        val reviewsGiven by screenModel.reviewsGiven.collectAsState()
        val reviewsReceived by screenModel.reviewsReceived.collectAsState()
        val currentUser = screenModel.currentUser

        var selectedTab by remember { mutableIntStateOf(0) }
        val tabTitles = listOf<String>("Given", "Received")
        val reviewsForTab = if (selectedTab == 0) reviewsGiven else reviewsReceived

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(30.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                UserDetails(currentUser = currentUser)
            }

            Text(
                text = "Reviews",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(text = title, maxLines = 2) }
                    )
                }
            }

            if (reviewsForTab.isEmpty()) {
                Text(
                    text = "No reviews available.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                )
            } else {
                LazyColumn {
                    items(reviewsForTab) {
                        ReviewCard(review = it)
                    }
                }
            }
        }
    }

    @Composable
    fun UserDetails(currentUser: User?) {
        Column(
            modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.height(8.dp))

            // Show user's nickname
            Text(
                text = currentUser!!.name ?: currentUser.nickname ?: "Guest User",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            // Show email or 'Guest Account' if not signed in
            Text(
                text = currentUser.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    fun UserStatistics(
        reviewsGiven: List<Review>,
        reviewsReceived: List<Review>
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatItem(
                icon = Icons.Outlined.RateReview,
                value = reviewsGiven.size.toString(),
                label = "Reviews",
            )
            StatItem(
                icon = Icons.Outlined.Star,
                value = if (reviewsReceived.isNotEmpty()) {
                    reviewsReceived.map { it.rating.toDouble() }.average().toString()
                } else {
                    "0.0"
                },
                label = "Rating",
            )
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

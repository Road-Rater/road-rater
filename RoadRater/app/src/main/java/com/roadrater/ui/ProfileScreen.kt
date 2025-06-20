package com.roadrater.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import coil3.compose.rememberAsyncImagePainter
import com.roadrater.database.entities.BlockedUser
import com.roadrater.database.entities.User
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.presentation.Screen
import com.roadrater.presentation.components.CarWatchingCard
import com.roadrater.presentation.components.ReviewsDisplay
import com.roadrater.ui.ProfileScreenModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent

class ProfileScreen(val user: User) : Screen() {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel {
            KoinJavaComponent.getKoin().get<ProfileScreenModel>(parameters = { parametersOf(user) })
        }

        val generalPreferences = koinInject<GeneralPreferences>()
        val currentUserId = generalPreferences.user.get()?.uid
        val supabaseClient = koinInject<SupabaseClient>()
        val context = LocalContext.current

        var selectedPrimaryTab = screenModel.selectedPrimaryTab.collectAsState()
        val isBlocked = remember { mutableStateOf(false) }

        LaunchedEffect(currentUserId, user.uid) {
            if (currentUserId != null && currentUserId != user.uid) {
                try {
                    val result = supabaseClient
                        .from("blocked_users")
                        .select {
                            filter {
                                eq("user_blocking", currentUserId)
                                eq("blocked_user", user.uid)
                            }
                        }
                        .decodeList<BlockedUser>()

                    isBlocked.value = result.isNotEmpty()
                } catch (e: Exception) {
                }
            }
        }

        val primaryTabTitles = listOf<String>("Reviews", "Vehicles")

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Companion.Start,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(30.dp),
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                UserDetails(user)

                if (currentUserId != null && currentUserId != user.uid) {
                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    if (isBlocked.value) {
                                        // UNBLOCK
                                        supabaseClient.from("blocked_users").delete {
                                            filter {
                                                eq("user_blocking", currentUserId)
                                                eq("blocked_user", user.uid)
                                            }
                                        }
                                        isBlocked.value = false
                                        CoroutineScope(Dispatchers.Main).launch {
                                            Toast.makeText(context, "User unblocked", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        // BLOCK
                                        val newUid = java.util.UUID.randomUUID().toString()
                                        supabaseClient.from("blocked_users").insert(
                                            BlockedUser(
                                                uid = newUid,
                                                blocked_user = user.uid,
                                                user_blocking = currentUserId!!,
                                            ),
                                        )
                                        isBlocked.value = true
                                        CoroutineScope(Dispatchers.Main).launch {
                                            Toast.makeText(context, "User blocked", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.Block, contentDescription = "Block Icon", modifier = Modifier.padding(end = 8.dp))
                        Text(if (isBlocked.value) "Unblock User" else "Block User")
                    }
                }
            }

            PrimaryTabRow(selectedTabIndex = selectedPrimaryTab.value) {
                primaryTabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedPrimaryTab.value == index,
                        onClick = { screenModel.setSelectedPrimaryTab(index) },
                        text = { Text(title, modifier = Modifier.padding(10.dp)) },
                    )
                }
            }
            AnimatedContent(
                targetState = selectedPrimaryTab.value,
                transitionSpec = {
                    // Example: slide left when increasing tab index, right when decreasing
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                    }.using(SizeTransform(clip = false))
                },
                label = "Sliding Tab Transition",
            ) { target ->
                when (target) {
                    0 -> ReviewsTabContent(screenModel)
                    1 -> VehiclesTabContent(screenModel)
                }
            }
        }
    }

    @Composable
    fun VehiclesTabContent(screenModel: ProfileScreenModel) {
        val watchedCars = screenModel.watchedCars.collectAsState()
        if (watchedCars.value.isEmpty()) {
            Text(
                text = "No cars being watched.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.Companion
                    .padding(20.dp)
                    .fillMaxWidth(),
            )
        } else {
            LazyColumn {
                items(watchedCars.value) {
                    CarWatchingCard(car = it, onClick = {})
                }
            }
        }
    }

    @Composable
    fun ReviewsTabContent(screenModel: ProfileScreenModel) {
        val reviewsGivenAndReviewers = screenModel.reviewsGivenAndReviewers.collectAsState()
        val reviewsReceivedAndReviewers = screenModel.reviewsReceivedAndReviewers.collectAsState()
        var selectedSecondaryTab = screenModel.selectedSecondaryTab.collectAsState()

        val secondaryTabTitles = listOf<String>("Given", "Received")
        val reviewsForTab = if (selectedSecondaryTab.value == 0) reviewsGivenAndReviewers else reviewsReceivedAndReviewers

        Column {
            SecondaryTabRow(selectedTabIndex = selectedSecondaryTab.value) {
                secondaryTabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedSecondaryTab.value == index,
                        onClick = { screenModel.setSelectedSecondaryTab(index) },
                        text = {
                            BadgedBox(badge = {
                                if (
                                    (index == 0 && reviewsGivenAndReviewers.value.isNotEmpty()) ||
                                    (index == 1 && reviewsReceivedAndReviewers.value.isNotEmpty())
                                ) {
                                    Badge {
                                        Text(
                                            if (index == 0) {
                                                reviewsGivenAndReviewers.value.size.toString()
                                            } else {
                                                reviewsReceivedAndReviewers.value.size.toString()
                                            },
                                        )
                                    }
                                }
                            }) {
                                Text(title, modifier = Modifier.Companion.padding(10.dp))
                            }
                        },
                    )
                }
            }
            if (reviewsForTab.value.isEmpty()) {
                Text(
                    text = "No reviews available.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.Companion
                        .padding(20.dp)
                        .fillMaxWidth(),
                )
            } else {
                Spacer(modifier = Modifier.Companion.height(10.dp))
                ReviewsDisplay(Modifier, reviewsForTab.value, true)
            }
        }
    }

    @Composable
    fun UserDetails(currentUser: User?) {
        Row {
            // Show profile picture if available
            currentUser?.profile_pic_url?.let { imageUrl ->
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "Profile picture",
                    modifier = Modifier.Companion
                        .size(80.dp)
                        .clip(CircleShape),
                )
            }
            Column(
                horizontalAlignment = Alignment.Companion.Start,
                modifier = Modifier.Companion.padding(horizontal = 12.dp),
            ) {
                Text(
                    text = currentUser!!.name ?: currentUser.nickname ?: "Guest User",
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(
                    text = currentUser.email ?: "",
                )
            }
        }
    }

    @Composable
    fun UserStatistics(screenModel: ProfileScreenModel) {
        Row(
            modifier = Modifier.Companion.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
        }
    }
}

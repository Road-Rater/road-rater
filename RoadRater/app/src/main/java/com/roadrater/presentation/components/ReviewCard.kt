package com.roadrater.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.twotone.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.rememberAsyncImagePainter
import com.roadrater.R
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.User
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.ui.ProfileScreen
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ReviewCard(
    review: Review,
    createdBy: User,
    onClick: () -> Unit = {},
    onModChange: () -> Unit = {},
) {
    val navigator = LocalNavigator.currentOrThrow
    val generalPreferences = koinInject<GeneralPreferences>()
    val supabaseClient = koinInject<SupabaseClient>()
    val isModerator = generalPreferences.user.get()?.is_moderator
    var showReportDialog by remember { mutableStateOf(false) }
    var showModDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dateTime = try {
        val odt = OffsetDateTime.parse(review.createdAt)
        odt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
    } catch (e: Exception) {
        ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    if (isModerator == true) {
                        showModDialog = true
                    } else {
                        showReportDialog = true
                    }
                },
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                StarRating(review.rating.toInt(), 25.dp)
                IconButton(
                    onClick = { navigator.push(ProfileScreen(createdBy)) },
                    modifier = Modifier.size(30.dp),
                ) {
                    if (createdBy.profile_pic_url != null) {
                        Image(
                            painter = rememberAsyncImagePainter(createdBy.profile_pic_url),
                            contentDescription = "Profile picture",
                            modifier = Modifier.clip(CircleShape),
                        )
                    } else {
                        Icon(Icons.TwoTone.AccountCircle, "Blank Profile Picture")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Plate link
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onClick),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DirectionsCar,
                        contentDescription = "Car",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = review.numberPlate.uppercase(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = review.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = dateTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

// Replace the commented out section with this:
            if (review.labels.isNotEmpty() && review.labels.first().isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState()),
                ) {
                    review.labels.forEach { label ->
                        if (label.isNotEmpty()) {
                            Text(
                                text = label,
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(10.dp),
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = review.description.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(stringResource(R.string.report_message)) },
            text = { Text(stringResource(R.string.report_dialog_body, review.description)) },
            confirmButton = {
                TextButton(onClick = {
                    showReportDialog = false
                    scope.launch {
                        scope.launch {
                            supabaseClient
                                .from("reviews")
                                .update(mapOf("is_flagged" to true)) {
                                    filter {
                                        eq("id", review.id!!)
                                    }
                                }
                        }
                    }
                }) {
                    Text(stringResource(R.string.report))
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showModDialog) {
        AlertDialog(
            onDismissRequest = { showModDialog = false },
            title = { Text(stringResource(R.string.manage_message)) },
            text = {
                Column {
                    Text("\"${review.description}\"")

                    Spacer(modifier = Modifier.height(16.dp))

                    // Option 1: Delete
                    TextButton(
                        onClick = {
                            showModDialog = false
                            scope.launch {
                                supabaseClient
                                    .from("reviews")
                                    .delete {
                                        filter {
                                            eq("id", review.id!!)
                                        }
                                    }
                            }
                            onModChange()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.delete))
                    }

                    // Option 2: Unflag
                    TextButton(
                        onClick = {
                            showModDialog = false
                            if (review.isFlagged) {
                                scope.launch {
                                    supabaseClient
                                        .from("reviews")
                                        .update(mapOf("is_flagged" to false)) {
                                            filter {
                                                eq("id", review.id!!)
                                            }
                                        }
                                }
                            }
                            onModChange()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.unflag))
                    }

                    TextButton(
                        onClick = {
                            showModDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {},
        )
    }
}

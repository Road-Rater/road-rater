package com.roadrater.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.roadrater.presentation.components.ReviewCard
import com.roadrater.database.entities.Comment
import com.roadrater.database.entities.Review
import com.roadrater.preferences.GeneralPreferences
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.compose.foundation.layout.Column

class ReviewDetailsScreen(private val reviewId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val supabaseClient = koinInject<SupabaseClient>()
        val generalPreferences = koinInject<GeneralPreferences>()
        val currentUser = generalPreferences.user.get()

        currentUser?.let { user ->
            val screenModel = rememberScreenModel {
                ReviewDetailsScreenModel(supabaseClient, reviewId, user.uid)
            }

            val review by screenModel.review.collectAsState()
            val comments by screenModel.comments.collectAsState()
            val voteMap by screenModel.voteMap.collectAsState()
            val commentTree by screenModel.commentTree.collectAsState()
            var replyTo by remember { mutableStateOf<String?>(null) }

            Scaffold(topBar = {
                TopAppBar(title = { Text("Review Details") })
            }) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    item {
                        review?.let { ReviewCard(it) }
                    }
                    // Render threaded comments
                    item {
                        ThreadedComments(
                            parentId = null,
                            commentTree = commentTree,
                            voteMap = voteMap,
                            onUpvote = { screenModel.voteOnComment(it, 1) },
                            onDownvote = { screenModel.voteOnComment(it, -1) },
                            onReply = { replyTo = it }
                        )
                    }
                    item {
                        replyTo?.let {
                            Text("Replying to comment ID: $it", style = MaterialTheme.typography.labelSmall)
                            Column {
                                OutlinedTextField(
                                    value = screenModel.replyContent.value,
                                    onValueChange = { screenModel.replyContent.value = it },
                                    label = { Text("Reply") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(
                                    onClick = {
                                        screenModel.postComment(
                                            content = screenModel.replyContent.value,
                                            parentId = replyTo
                                        )
                                        screenModel.replyContent.value = ""
                                        replyTo = null
                                    },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Post Reply")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentCard(
    comment: Comment,
    voteCount: Int,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit,
    onReply: () -> Unit
) {
    val generalPreferences = koinInject<GeneralPreferences>()
    val currentUser = generalPreferences.user.get()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Placeholder avatar
//                Icon(Icons.Outlined.ArrowUpward, contentDescription = "User Avatar")
//                Spacer(modifier = Modifier.width(8.dp))
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
//                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatRelativeTime(comment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            //Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onUpvote,modifier=Modifier.size(24.dp)) {
                    Icon(Icons.Outlined.ArrowUpward, contentDescription = "Upvote", modifier = Modifier.size(16.dp))
                }
                Text("$voteCount",style=MaterialTheme.typography.labelMedium)
                IconButton(onClick = onDownvote, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Outlined.ArrowDownward, contentDescription = "Downvote", modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onReply) {
                    Text("Reply",style=MaterialTheme.typography.labelSmall)
                }
            }
        Divider(modifier = Modifier.padding(top=8.dp))
        }
    }

@Composable
fun formatRelativeTime(timestamp: String?): String {
    if (timestamp == null) return "Unknown time"
    return try {
        val time = Instant.parse(timestamp)
        val now = Instant.now()
        val minutes = ChronoUnit.MINUTES.between(time, now)
        val hours = ChronoUnit.HOURS.between(time, now)
        val days = ChronoUnit.DAYS.between(time, now)

        when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes minute${if (minutes != 1L) "s" else ""} ago"
            hours < 24 -> "$hours hour${if (hours != 1L) "s" else ""} ago"
            else -> "$days day${if (days != 1L) "s" else ""} ago"
        }
    } catch (e: Exception) {
        "Unknown time"
    }
}

@Composable
fun ThreadedComments(
    parentId: String? = null,
    commentTree: Map<String?, List<Comment>>,
    voteMap: Map<String, Int>,
    onUpvote: (String) -> Unit,
    onDownvote: (String) -> Unit,
    onReply: (String) -> Unit,
    depth: Int = 0
) {
    commentTree[parentId]?.forEach { comment ->
        Column(
            modifier = Modifier
                .padding(start = (depth * 16).dp, bottom = 8.dp)
        ) {
            CommentCard(
                comment = comment,
                voteCount = voteMap[comment.id.toString()] ?: 0,
                onUpvote = { onUpvote(comment.id.toString()) },
                onDownvote = { onDownvote(comment.id.toString()) },
                onReply = { onReply(comment.id.toString()) }
            )
            // Recursive call to display child comments
            ThreadedComments(
                parentId = comment.id.toString(),
                commentTree = commentTree,
                voteMap = voteMap,
                onUpvote = onUpvote,
                onDownvote = onDownvote,
                onReply = onReply,
                depth = depth + 1
            )
        }
    }
}

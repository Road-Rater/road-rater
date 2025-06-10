package com.roadrater.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.roadrater.R
import com.roadrater.database.entities.Comment
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.presentation.components.ReviewCard
import io.github.jan.supabase.SupabaseClient
import org.koin.compose.koinInject
import java.time.Instant
import java.time.temporal.ChronoUnit

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
            val reviewer by screenModel.reviewer.collectAsState()
            val comments by screenModel.comments.collectAsState()
            val commentTree by screenModel.commentTree.collectAsState()
            var replyTo by remember { mutableStateOf<String?>(null) }

            Scaffold(topBar = {
                TopAppBar(title = { Text(stringResource(R.string.review_details)) })
            }) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp),
                ) {
                    item {
                        val currentReview = review
                        val currentReviewer = reviewer
                        if (currentReview != null && currentReviewer != null) {
                            ReviewCard(
                                review = currentReview,
                                createdBy = currentReviewer,
                            )
                            TextButton(
                                onClick = { replyTo = "REVIEW" },
                                modifier = Modifier.padding(top = 8.dp),
                            ) {
                                Text(stringResource(R.string.review_reply))
                            }
                            // Inline reply input under the review
                            if (replyTo == "REVIEW") {
                                val focusRequester = remember { FocusRequester() }
                                val keyboardController = LocalSoftwareKeyboardController.current
                                LaunchedEffect(replyTo) {
                                    focusRequester.requestFocus()
                                    keyboardController?.show()
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .imePadding(),
                                ) {
                                    Column {
                                        OutlinedTextField(
                                            value = screenModel.replyContent.value,
                                            onValueChange = { screenModel.replyContent.value = it },
                                            label = { Text(stringResource(R.string.reply)) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(focusRequester),
                                        )
                                        Button(
                                            onClick = {
                                                screenModel.postComment(
                                                    content = screenModel.replyContent.value,
                                                    parentId = null,
                                                )
                                                screenModel.replyContent.value = ""
                                                replyTo = null
                                            },
                                            modifier = Modifier.padding(top = 8.dp),
                                        ) {
                                            Text(stringResource(R.string.post_reply))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        ThreadedComments(
                            parentId = null,
                            commentTree = commentTree,

                            replyTo = replyTo,
                            setReplyTo = { replyTo = it },
                            replyContent = screenModel.replyContent.value,
                            onReplyContentChange = { screenModel.replyContent.value = it },
                            onPostReply = { parentId ->
                                screenModel.postComment(
                                    content = screenModel.replyContent.value,
                                    parentId = parentId,
                                )
                                screenModel.replyContent.value = ""
                                replyTo = null
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommentCard(
    comment: Comment,
    onReply: () -> Unit,
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
            modifier = Modifier.fillMaxWidth(),
        ) {
            AsyncImage(
                model = currentUser?.profile_pic_url,
                contentDescription = stringResource(R.string.pfp),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { },
            )
            Text(
                text = formatRelativeTime(comment.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = comment.content,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 4.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onReply) {
                Text("Reply", style = MaterialTheme.typography.labelSmall)
            }
        }
        Divider(modifier = Modifier.padding(top = 8.dp))
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
    replyTo: String?,
    setReplyTo: (String?) -> Unit,
    replyContent: String,
    onReplyContentChange: (String) -> Unit,
    onPostReply: (String?) -> Unit,
    depth: Int = 0,
) {
    commentTree[parentId]?.forEach { comment ->
        Column(
            modifier = Modifier
                .padding(start = (depth * 16).dp, bottom = 8.dp),
        ) {
            CommentCard(
                comment = comment,
                onReply = { setReplyTo(comment.id.toString()) },
            )
            // Inline reply input under the comment being replied to
            if (replyTo == comment.id.toString()) {
                val focusRequester = remember { FocusRequester() }
                val keyboardController = LocalSoftwareKeyboardController.current
                LaunchedEffect(replyTo) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding(),
                ) {
                    Column {
                        OutlinedTextField(
                            value = replyContent,
                            onValueChange = onReplyContentChange,
                            label = { Text(stringResource(R.string.reply)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                        )
                        Button(
                            onClick = {
                                onPostReply(comment.id.toString())
                            },
                            modifier = Modifier.padding(top = 8.dp),
                        ) {
                            Text(stringResource(R.string.post_reply))
                        }
                    }
                }
            }
            // Recursive call to display child comments
            ThreadedComments(
                parentId = comment.id.toString(),
                commentTree = commentTree,
                replyTo = replyTo,
                setReplyTo = setReplyTo,
                replyContent = replyContent,
                onReplyContentChange = onReplyContentChange,
                onPostReply = onPostReply,
                depth = depth + 1,
            )
        }
    }
}

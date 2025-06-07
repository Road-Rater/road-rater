package com.roadrater.ui

import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.roadrater.database.entities.Comment
import com.roadrater.database.entities.Review
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import android.util.Log



class ReviewDetailsScreenModel(
    private val supabaseClient: SupabaseClient,
    private val reviewId: String,
    private val uid: String,
) : ScreenModel {

    val review = MutableStateFlow<Review?>(null)
    val comments = MutableStateFlow<List<Comment>>(emptyList())
    val voteMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    val replyTo = MutableStateFlow<String?>(null)
    val replyContent = mutableStateOf("")

    init {
        fetchReview()
        fetchCommentsAndVotes()
    }

    private fun fetchReview() {
        screenModelScope.launch(Dispatchers.IO) {
            review.value = supabaseClient.from("reviews")
                .select {
                    filter {
                        eq("id", reviewId)
                    }
                }
                .decodeSingleOrNull()
        }
    }

    private fun fetchCommentsAndVotes() {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val commentList = supabaseClient.from("comments")
                    .select {
                        filter {
                            eq("review_id", reviewId)
                        }
                    }
                    .decodeList<Comment>()

                comments.value = commentList

                val commentIds = commentList.map { it.id }
                if (commentIds.isNotEmpty()) {
                    val voteList = supabaseClient.from("comment_votes")
                        .select()
                        .decodeList<Map<String, Any>>()

                    val voteCounts = voteList
                        .filter { it["comment_id"] in commentIds }
                        .groupBy { it["comment_id"].toString() }
                        .mapValues { entry ->
                            entry.value.sumOf {
                                (it["vote"] as? Number)?.toInt() ?: 0
                            }
                        }

                    voteMap.value = voteCounts
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun voteOnComment(commentId: Long, vote: Int) {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val existingVotes = supabaseClient.from("comment_votes")
                    .select {
                        filter {
                            eq("comment_id", commentId)
                            eq("user_id", uid)
                        }
                    }
                    .decodeList<Map<String, Any>>()

                if (existingVotes.isNotEmpty()) {
                    val existing = existingVotes.first()
                    supabaseClient.from("comment_votes")
                        .update(
                            {
                                set("vote", vote)
                            }
                        ) {
                            filter {
                                eq("id", existing["id"]!!)
                            }
                        }
                } else {
                    supabaseClient.from("comment_votes")
                        .insert(
                            mapOf(
                                "comment_id" to commentId,
                                "user_id" to uid,
                                "vote" to vote
                            )
                        )
                }
                fetchCommentsAndVotes()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun postComment(content: String,parentId:String?) {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                //val parentId = replyTo.value
                val insertData = buildMap {
                    put("review_id",reviewId)
                    put("user_id",uid)
                    put("content",content)
                    if (!parentId.isNullOrBlank()) put("parent_id",parentId)
                }

                val result = supabaseClient.from("comments").insert(insertData)

//                        mapOf(
//                            "review_id" to reviewId,
//                            "user_id" to uid,
//                            "content" to content,
//                            "parent_id" to replyTo.value.takeIf { !it.isNullOrBlank() }
//                        )

                fetchCommentsAndVotes()
            } catch (e: Exception) {
                Log.e("PostComment","Error Submitting comment",e)

            }
        }
    }



    fun setReplyTo(commentId: String?) {
        replyTo.value = commentId
    }
}

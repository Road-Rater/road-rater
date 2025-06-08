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
import android.util.Log
import kotlinx.coroutines.flow.StateFlow

class ReviewDetailsScreenModel(
    private val supabaseClient: SupabaseClient,
    private val reviewId: String,
    private val uid: String,
) : ScreenModel {

    val review = MutableStateFlow<Review?>(null)
    val comments = MutableStateFlow<List<Comment>>(emptyList())
    val replyTo = MutableStateFlow<String?>(null)
    val replyContent = mutableStateOf("")
    private val _commentTree = MutableStateFlow<Map<String?, List<Comment>>>(emptyMap())
    val commentTree: StateFlow<Map<String?, List<Comment>>> = _commentTree

    init {
        fetchReview()
        fetchComments()
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

    private fun fetchComments() {
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

                // Group by parentId as String?
                val grouped = commentList.groupBy { it.parentId?.toString() }
                _commentTree.value = grouped
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun postComment(content: String, parentId: String?) {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val insertData = buildMap {
                    put("review_id", reviewId)
                    put("user_id", uid)
                    put("content", content)
                    if (!parentId.isNullOrBlank()) put("parent_id", parentId)
                }
                supabaseClient.from("comments").insert(insertData)
                fetchComments()
            } catch (e: Exception) {
                Log.e("PostComment", "Error Submitting comment", e)
            }
        }
    }

    fun setReplyTo(commentId: String?) {
        replyTo.value = commentId
    }
}

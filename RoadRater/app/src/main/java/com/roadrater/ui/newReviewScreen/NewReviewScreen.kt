package com.roadrater.ui.newReviewScreen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.roadrater.database.entities.Review
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.utils.toast
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.Instant

class NewReviewScreen(private val numberPlate: String) : Screen {
    @Composable
    override fun Content() {
        val context = LocalContext.current
        // UI State
        var rating by remember { mutableIntStateOf(0) }
        var commentText by remember { mutableStateOf(TextFieldValue("")) }
        var reviewTitle by remember { mutableStateOf(TextFieldValue("")) }
        var numberPlateInput by remember { mutableStateOf(numberPlate) }
        val isPlateEditable = numberPlate.isEmpty()
        val generalPreferences = koinInject<GeneralPreferences>()
        val user = generalPreferences.user.get()

        // Dependencies
        val supabaseClient: SupabaseClient = koinInject()
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("New Review") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // LICENSE PLATE
                OutlinedTextField(
                    value = numberPlateInput,
                    onValueChange = {
                        if (isPlateEditable && it.length <= 6) numberPlateInput = it
                    },
                    label = { Text("License Plate to review (Max 6 chars):") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isPlateEditable,
                )

                Row {
                    repeat(5) { index ->
                        IconButton(
                            onClick = {
                                rating = index + 1
                            },
                        ) {
                            Icon(
                                imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Star",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                // REVIEW TITLE
                OutlinedTextField(
                    value = reviewTitle,
                    onValueChange = {
                        if (it.text.length <= 60) reviewTitle = it
                    },
                    label = { Text("Review Title (Max 60 characters):") },
                    modifier = Modifier.fillMaxWidth(),
                )

                // REVIEW TEXT
                OutlinedTextField(
                    value = commentText,
                    onValueChange = {
                        if (it.text.length <= 500) commentText = it
                    },
                    label = { Text("Your review (Max 500 characters):") },
                    modifier = Modifier.fillMaxWidth(),
                )

                // SUBMIT BUTTON
                Button(onClick = {
                    Log.d("NewReviewScreen", "Signed-in user = ${user?.uid}")

                    val currentUserId = user?.uid
                    if (currentUserId == null) {
                        context.toast("You're not signed in.")
                        return@Button
                    }

                    val newReview = Review(
                        createdBy = currentUserId.toString(),
                        numberPlate = numberPlateInput,
                        rating = rating,
                        title = reviewTitle.text,
                        description = commentText.text,
                        createdAt = Instant.now().toString(),
                        labels = listOf(""),
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            Log.d("NewReviewScreen", "Submitting review: $newReview")
                            val response = supabaseClient.from("reviews").insert(newReview)
                            Log.d("NewReviewScreen", "Supabase insert response: $response")
                            context.toast("Review submitted!")
                        } catch (e: Exception) {
                            Log.e("NewReviewScreen", "Error submitting review", e)
                            context.toast("Failed to submit review: ${e.message}")
                        }
                    }
                }) {
                    Text("Submit review")
                }
            }
        }
    }
}

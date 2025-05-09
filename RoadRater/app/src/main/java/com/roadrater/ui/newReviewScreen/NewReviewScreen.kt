package com.roadrater.ui.NewReviewScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.roadrater.database.entities.Rating
import com.roadrater.database.repository.RatingRepository
import io.github.jan.supabase.SupabaseClient
import org.koin.compose.koinInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import io.github.jan.supabase.postgrest.from


//LEAVE NEW REVIEW SCREEN
class NewReviewScreen(private val numberPlate: String, private val userId: String) : Screen {
    @Composable
    override fun Content() {
        // Variables
        var reviewScore by remember { mutableStateOf("") }
        var commentText by remember { mutableStateOf(TextFieldValue("")) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var successMessage by remember { mutableStateOf<String?>(null) }
        var numberPlateInput by remember { mutableStateOf("") }

        val ratingRepository: RatingRepository = koinInject()
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
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // LICENSE PLATE
                OutlinedTextField(
                    value = numberPlateInput,
                    onValueChange = {
                        if (it.length <= 6) numberPlateInput = it
                    },
                    label = { Text("License Plate to review (Max 6 chars):") },
                    modifier = Modifier.fillMaxWidth()
                )

                // REVIEW SCORE
                OutlinedTextField(
                    value = reviewScore,
                    onValueChange = { reviewScore = it },
                    label = { Text("Driver Rating (1-10)") },
                    isError = errorMessage != null && (reviewScore.toIntOrNull() == null || reviewScore.toInt() !in 1..10)
                )

                // REVIEW TEXT
                OutlinedTextField(
                    value = commentText,
                    onValueChange = {
                        if (it.text.length <= 500) commentText = it
                    },
                    label = { Text("Your review (Max 500 characters):") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(onClick = {
                    val score = reviewScore.toIntOrNull()
                    if (score == null || score !in 1..10) {
                        errorMessage = "Rating must be a number from 1 to 10."
                        return@Button
                    }

                    val rating = Rating(
                        userId = userId,
                        numberPlate = numberPlateInput,
                        review = score,
                        comment = commentText.text,
                        createdAt = (System.currentTimeMillis() / 1000).toInt()
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            supabaseClient.from("rating").insert(rating)
                            successMessage = "Review submitted!"
                            errorMessage = null
                        } catch (e: Exception) {
                            errorMessage = "Failed to submit review."
                            successMessage = null
                        }
                    }
                }) {
                    Text("Submit review")
                }

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                successMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

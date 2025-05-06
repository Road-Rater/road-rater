package com.roadrater.ui.NewReviewScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.roadrater.database.entities.Rating
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


//LEAVE NEW REVIEW SCREEN
class NewReviewScreen(private val numberPlate: String, private val userId: String) : Screen {
    @Composable
    override fun Content() {
        //Variables
        var reviewScore by remember { mutableStateOf("") }
        var commentText by remember { mutableStateOf(TextFieldValue("")) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var successMessage by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //Title
            Text("Writing review for \"$numberPlate\"", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = reviewScore,
                onValueChange = { reviewScore = it },
                label = { Text("Rating (1-10)") },
                isError = errorMessage != null && (reviewScore.toIntOrNull() == null || reviewScore.toInt() !in 1..10) //Making sure review score is between 1-10
            )

            //Review Text
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                label = { Text("Your review:") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = {
                val score = reviewScore.toIntOrNull()
                if (score == null || score !in 1..10) {
                    errorMessage = "Rating must be a number from 1 to 10." //Error message
                    return@Button
                }

                val rating = Rating(
                    userId = userId,
                    numberPlate = numberPlate,
                    review = score,
                    comment = commentText.text,
                    createdAt = (System.currentTimeMillis() / 1000).toInt() //Timestamp
                )

                CoroutineScope(Dispatchers.IO).launch {
                    // TODO: submit rating to db here
                    successMessage = "Review submitted!" //Great success
                    errorMessage = null //Error submitting review
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

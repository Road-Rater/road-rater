package com.roadrater.ui.home.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.roadrater.presentation.util.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.roadrater.R
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.rememberCoroutineScope

object AddReviewTab : Tab {
    private fun readResolve(): Any = AddReviewTab
    override val options: TabOptions
        @Composable
        get() {
            return TabOptions(
                index = 1u, // Change index as needed
                title = "Add Review",
                icon = rememberVectorPainter(Icons.Filled.Add)
            )
        }

    @Composable
    override fun Content() {
        val supabaseClient = koinInject<SupabaseClient>()
        val generalPreferences = koinInject<com.roadrater.preferences.GeneralPreferences>()
        val userId = generalPreferences.user.get()?.uid ?: ""
        var numberPlate by remember { mutableStateOf("") }
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var rating by remember { mutableStateOf(0) }
        var isSubmitting by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf<String?>(null) }
        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Create a Review", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = numberPlate,
                onValueChange = { numberPlate = it.uppercase() },
                label = { Text("Number Plate") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { index ->
                    IconButton(
                        onClick = { rating = index + 1 }
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
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Review Title (Max 60 characters):") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Your review (Max 500 characters):") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 10
            )
            Button(
                onClick = {
                    isSubmitting = true
                    message = null
                    coroutineScope.launch {
                        try {
                            supabaseClient.from("reviews").insert(
                                mapOf(
                                    "number_plate" to numberPlate,
                                    "title" to title,
                                    "description" to description,
                                    "rating" to rating,
                                    "created_by" to userId
                                )
                            )
                            message = "Review submitted!"
                            numberPlate = ""
                            title = ""
                            description = ""
                            rating = 0
                        } catch (e: Exception) {
                            message = "Error: ${e.message}"
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting && numberPlate.isNotBlank() && title.isNotBlank() && description.isNotBlank()
            ) {
                Text("Submit Review")
            }
            message?.let {
                Text(it, color = if (it.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }
        }
    }
}
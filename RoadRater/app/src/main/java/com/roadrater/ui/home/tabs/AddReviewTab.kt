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
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

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

    // Predefined labels for categorizing reviews
    private val availableLabels = listOf(
        "Speeding",
        "Courteous",
        "Aggressive",
        "Road Rage",
        "Tailgating",
        "Bad Driving",
        "Good Driving",
        "Reckless",
        "Patient",
        "Distracted",
        "Lane Cutting",
        "Parking Issues"
    )

    @Composable
    private fun LabelChip(
        label: String,
        isSelected: Boolean,
        onToggle: () -> Unit
    ) {
        val backgroundColor = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }

        val textColor = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        }

        val borderColor = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onToggle() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
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
        var selectedLabels by remember { mutableStateOf(setOf<String>()) }


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

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Labels (Select all that apply)",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableLabels.forEach { label ->
                        LabelChip(
                            label = label,
                            isSelected = selectedLabels.contains(label),
                            onToggle = {
                                selectedLabels = if (selectedLabels.contains(label)) {
                                    selectedLabels - label
                                } else {
                                    selectedLabels + label
                                }
                            }
                        )
                    }
                }

                if (selectedLabels.isNotEmpty()) {
                    Text(
                        text = "Selected: ${selectedLabels.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

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
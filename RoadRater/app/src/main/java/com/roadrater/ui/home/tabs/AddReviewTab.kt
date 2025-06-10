package com.roadrater.ui.home.tabs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.roadrater.database.entities.Review
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.presentation.util.Tab
import com.roadrater.utils.ValidationUtils
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.Instant
import com.roadrater.database.entities.CarOwnership
import com.roadrater.database.entities.BlockedUser


object AddReviewTab : Tab {
    private fun readResolve(): Any = AddReviewTab
    override val options: TabOptions
        @Composable
        get() {
            return TabOptions(
                index = 1u, // Change index as needed
                title = "Add Review",
                icon = rememberVectorPainter(Icons.Filled.Add),
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
        "Parking Issues",
    )

    @Composable
    private fun LabelChip(
        label: String,
        isSelected: Boolean,
        onToggle: () -> Unit,
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
                    shape = RoundedCornerShape(16.dp),
                )
                .clickable { onToggle() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            )
        }
    }

    @Composable
    override fun Content() {
        val supabaseClient = koinInject<SupabaseClient>()
        val generalPreferences = koinInject<GeneralPreferences>()
        val context = LocalContext.current
        val userId = generalPreferences.user.get()?.uid ?: ""

        var numberPlate by remember { mutableStateOf("") }
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var rating by remember { mutableStateOf(0) }
        var isSubmitting by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf<String?>(null) }
        val coroutineScope = rememberCoroutineScope()
        var selectedLabels by remember { mutableStateOf(setOf<String>()) }

        // Error states for validation
        var numberPlateError by remember { mutableStateOf(false) }
        var titleError by remember { mutableStateOf(false) }
        var descriptionError by remember { mutableStateOf(false) }
        var ratingError by remember { mutableStateOf(false) }

        // Character limits
        val maxTitleLength = 60
        val maxDescriptionLength = 500

        // Show success/error messages as toasts
        message?.let { msg ->
            LaunchedEffect(msg) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }

        // Add scrollable modifier to the main Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Create a Review", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = numberPlate,
                onValueChange = {
                    numberPlate = ValidationUtils.formatNumberPlate(it)
                    numberPlateError = !ValidationUtils.isValidNumberPlate(numberPlate) && numberPlate.isNotEmpty()
                    message = null // Clear any previous messages
                },
                label = { Text("Number Plate") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = numberPlateError,
                supportingText = {
                    if (numberPlateError) {
                        Text(
                            text = "Please enter a valid number plate format",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Text("", style = MaterialTheme.typography.bodyMedium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    repeat(5) { index ->
                        IconButton(
                            onClick = {
                                rating = index + 1
                                ratingError = false
                                message = null
                            },
                        ) {
                            Icon(
                                imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Star ${index + 1}",
                                modifier = Modifier.size(24.dp),
                                tint = if (ratingError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                if (ratingError) {
                    Text(
                        text = "Please select a rating",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = {
                    if (it.length <= maxTitleLength) {
                        title = it
                        titleError = title.isBlank()
                        message = null
                    }
                },
                label = { Text("Review Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = titleError,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        if (titleError) {
                            Text(
                                text = "Title is required",
                                color = MaterialTheme.colorScheme.error,
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                        Text(
                            text = "${title.length}/$maxTitleLength",
                            color = if (title.length > maxTitleLength * 0.9) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
            )

            OutlinedTextField(
                value = description,
                onValueChange = {
                    if (it.length <= maxDescriptionLength) {
                        description = it
                        descriptionError = description.isBlank()
                        message = null
                    }
                },
                label = { Text("Your review") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 10,
                isError = descriptionError,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        if (descriptionError) {
                            Text(
                                text = "Review description is required",
                                color = MaterialTheme.colorScheme.error,
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                        Text(
                            text = "${description.length}/$maxDescriptionLength",
                            color = if (description.length > maxDescriptionLength * 0.9) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
            )

            Text(
                text = "Labels (Select all that apply)",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
            )

            // Use LazyVerticalGrid instead of FlowRow for better layout stability
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Create rows of chips manually
                val chunkedLabels = availableLabels.chunked(3)
                chunkedLabels.forEach { rowLabels ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowLabels.forEach { label ->
                            LabelChip(
                                label = label,
                                isSelected = selectedLabels.contains(label),
                                onToggle = {
                                    selectedLabels = if (selectedLabels.contains(label)) {
                                        selectedLabels - label
                                    } else {
                                        selectedLabels + label
                                    }
                                },
                            )
                        }
                        // Fill remaining space if row is not full
                        repeat(3 - rowLabels.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                if (selectedLabels.isNotEmpty()) {
                    Text(
                        text = "Selected: ${selectedLabels.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Submit Button - Moved here to ensure visibility
            Button(
                onClick = {
                    // Validate all fields before submission
                    numberPlateError = !ValidationUtils.isValidNumberPlate(numberPlate)
                    titleError = title.isBlank()
                    descriptionError = description.isBlank()
                    ratingError = rating == 0

                    if (!numberPlateError && !titleError && !descriptionError && !ratingError) {
                        isSubmitting = true
                        message = null

                        coroutineScope.launch {
                            try {
                                // GETTING OWNER OF PLATE
                                val ownerships = supabaseClient
                                    .from("car_ownership")
                                    .select {
                                        filter { eq("number_plate", numberPlate.uppercase()) }
                                    }
                                    .decodeList<CarOwnership>()

                                val carOwnerId = ownerships.firstOrNull()?.user_id

                                // IF CURRENT USER IS NOT OWNER, CHECK IF BLOCKED
                                if (!carOwnerId.isNullOrBlank() && carOwnerId != userId) {
                                    val blocked = supabaseClient
                                        .from("blocked_users")
                                        .select {
                                            filter {
                                                eq("user_blocking", carOwnerId)
                                                eq("blocked_user", userId)
                                            }
                                        }
                                        .decodeList<BlockedUser>()


                                    if (blocked.isNotEmpty()) {
                                        message = "You are blocked by the owner of this plate"
                                        return@launch
                                    }
                                }

                                // SUBMIT REVIEW
                                val review = Review(
                                    numberPlate = numberPlate,
                                    title = title.trim(),
                                    description = description.trim(),
                                    rating = rating,
                                    labels = selectedLabels.toList(),
                                    createdBy = userId,
                                    createdAt = Instant.now().toString(),
                                )

                                supabaseClient.from("reviews").insert(review)

                                message = "Review submitted successfully!"

                                numberPlate = ""
                                title = ""
                                description = ""
                                rating = 0
                                selectedLabels = setOf()
                            } catch (e: Exception) {
                                message = "Failed to submit review: ${e.message}"
                            } finally {
                                isSubmitting = false
                            }
                        }

                    } else {
                        message = "Please fix the errors above"
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isSubmitting) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Text("Submitting...")
                    }
                } else {
                    Text("Submit Review")
                }
            }

            // Add some bottom padding to ensure button is fully visible
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

package com.roadrater.ui.newReviewScreen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.roadrater.R
import com.roadrater.database.entities.Review
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.utils.ValidationUtils
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.Instant

class AddReviewScreen(private val numberPlate: String) : Screen {

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

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content() {
        val context = LocalContext.current
        // UI State
        var rating by remember { mutableIntStateOf(0) }
        var commentText by remember { mutableStateOf(TextFieldValue("")) }
        var reviewTitle by remember { mutableStateOf(TextFieldValue("")) }
        var editableNumberPlate by remember { mutableStateOf(TextFieldValue(numberPlate)) }
        var selectedLabels by remember { mutableStateOf(setOf<String>()) }
        val isPlateEditable = numberPlate.isEmpty()
        val generalPreferences = koinInject<GeneralPreferences>()
        val user = generalPreferences.user.get()

        // Dependencies
        val supabaseClient: SupabaseClient = koinInject()
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.add_review)) },
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
                    value = editableNumberPlate,
                    onValueChange = {
                        editableNumberPlate = it
                    },
                    label = { Text(stringResource(R.string.license_plate_input_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isPlateEditable,
                    isError = !ValidationUtils.isValidNumberPlate(editableNumberPlate.text) && editableNumberPlate.text.isNotEmpty(),
                    supportingText = {
                        if (!ValidationUtils.isValidNumberPlate(editableNumberPlate.text) && editableNumberPlate.text.isNotEmpty()) {
                            Text(stringResource(R.string.plate_format))
                        }
                    },
                )

                // RATING STARS
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
                    label = { Text(stringResource(R.string.review_title_input_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )

                // REVIEW TEXT
                OutlinedTextField(
                    value = commentText,
                    onValueChange = {
                        if (it.text.length <= 500) commentText = it
                    },
                    label = { Text(stringResource(R.string.review_body_input_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )

                // LABELS SECTION
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Labels (Select all that apply)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
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
                                },
                            )
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

                // SUBMIT BUTTON
                Button(onClick = {
                    Log.d("NewReviewScreen", "Signed-in user = ${user?.uid}")

                    val currentUserId = user?.uid
                    if (currentUserId == null) {
                        Toast.makeText(context, context.getString(R.string.login_error), Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (!ValidationUtils.isValidNumberPlate(editableNumberPlate.text)) {
                        Toast.makeText(context, context.getString(R.string.invalid_plate), Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Convert selected labels to list, or use empty list if none selected
                    val labelsList = selectedLabels.toList().ifEmpty { emptyList() }

                    val newReview = Review(
                        createdBy = currentUserId.toString(),
                        numberPlate = editableNumberPlate.text.uppercase(),
                        rating = rating,
                        title = reviewTitle.text,
                        description = commentText.text,
                        createdAt = Instant.now().toString(),
                        labels = labelsList,
                    )

                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            Log.d("NewReviewScreen", "Submitting review: $newReview")
                            val response = supabaseClient.from("reviews").insert(newReview)
                            Log.d("NewReviewScreen", "Supabase insert response: $response")

                            // Switch to Main thread for UI updates
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, context.getString(R.string.review_submitted), Toast.LENGTH_SHORT).show()
                                navigator.pop() // Navigate back after successful submission
                            }
                        } catch (e: Exception) {
                            Log.e("NewReviewScreen", "Error submitting review", e)

                            // Switch to Main thread for UI updates
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, context.getString(R.string.review_insert_failed, e.message ?: "Unknown error"), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) {
                    Text(stringResource(R.string.submit_review))
                }
            }
        }
    }

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
}

package com.roadrater.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Preview
@Composable
fun AddReviewDialog(
//    onDismissRequest: () -> Unit,
//    onConfirmation: () -> Unit
) {
    var numberPlate by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    fun validTitle(title: String): Boolean {
        return true
    }

    fun validDescription(description: String): Boolean {
        return true
    }

    fun validNumberPlate(numberPlate: String): Boolean {
        return true
    }

    Dialog(onDismissRequest = {}) {
        Card(
            Modifier.padding(12.dp),
        ) {
            NumberPlateInput()
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
                            modifier = Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            OutlinedTextField(
                value = title,
                onValueChange = {
                    if (validTitle(it)) title = it
                },
                label = { Text("Review Title (Max 60 characters):") },
                modifier = Modifier.fillMaxWidth(),
            )

            Row {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cancel",
                    )
                }

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Confirm",
                    )
                }
            }
        }
    }
}

@Composable
fun NumberPlateInput() {
    var plate = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = List(6) { FocusRequester() }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        plate.forEachIndexed { index, char ->
            OutlinedTextField(

                value = char,
                onValueChange = {
                    plate[index] = it

                    if (index < 5) {
                        focusRequesters[index + 1].requestFocus()
                    }
                },
                modifier = Modifier
                    .width(48.dp)
                    .focusRequester(focusRequesters[index])
                    .onKeyEvent {
                        if (it.key == Key.Backspace && index > 0) {
                            plate[index - 1] = ""
                            focusRequesters[index - 1].requestFocus()
                        }
                        false
                    },
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Ascii,
                ),
            )
        }
    }
}

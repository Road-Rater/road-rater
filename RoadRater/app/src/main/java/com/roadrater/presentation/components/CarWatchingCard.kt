package com.roadrater.presentation.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.roadrater.database.entities.Car

@Composable
fun CarWatchingCard(
    car: Car,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
            ),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(text = "Plate: ${car.number_plate}", style = MaterialTheme.typography.bodyLarge)
            if (!car.make.isNullOrBlank()) {
                Text(text = "Make: ${car.make}", style = MaterialTheme.typography.bodyMedium)
            }
            if (!car.model.isNullOrBlank()) {
                Text(text = "Model: ${car.model}", style = MaterialTheme.typography.bodyMedium)
            }
            if (car.year != null) {
                Text(text = "Year: ${car.year}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

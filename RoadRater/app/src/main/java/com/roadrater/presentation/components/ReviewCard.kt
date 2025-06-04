package com.roadrater.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.roadrater.database.entities.Review
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ReviewCard(review: Review) {
    val dateTime = try {
        val odt = OffsetDateTime.parse(review.createdAt)
        odt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
    } catch (e: Exception) {
        ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            StarRating(review.rating.toInt(), 24.dp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = review.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = dateTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

//            Row(
//                modifier = Modifier
//                    .horizontalScroll(rememberScrollState()),
//            ) {
//                review.labels.forEach { label ->
//                    Text(
//                        text = label,
//                        color = MaterialTheme.colorScheme.onPrimary,
//                        style = MaterialTheme.typography.labelSmall,
//                        modifier = Modifier
//                            .padding(end = 8.dp)
//                            .background(
//                                color = MaterialTheme.colorScheme.primary,
//                                shape = RoundedCornerShape(10.dp),
//                            )
//                            .padding(horizontal = 8.dp, vertical = 4.dp),
//                    )
//                }
//            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = review.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

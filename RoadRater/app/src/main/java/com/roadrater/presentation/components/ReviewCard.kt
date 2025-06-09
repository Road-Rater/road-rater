package com.roadrater.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.twotone.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.rememberAsyncImagePainter
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.User
import com.roadrater.ui.ProfileScreen
import com.roadrater.R
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ReviewCard(review: Review, createdBy: User) {
    val dateTime = try {
        val odt = OffsetDateTime.parse(review.createdAt)
        odt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
    } catch (e: Exception) {
        ""
    }
    val navigator = LocalNavigator.currentOrThrow

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
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                StarRating(review.rating.toInt(), 25.dp)
                IconButton(
                    onClick = { navigator.push(ProfileScreen(createdBy)) },
                    modifier = Modifier.size(30.dp)
                ) {
                    if (createdBy.profile_pic_url != null) {
                        Image(
                            painter = rememberAsyncImagePainter(createdBy.profile_pic_url),
                            contentDescription = "Profile picture",
                            modifier = Modifier.clip(CircleShape)
                        )
                    } else {
                        Icon(Icons.TwoTone.AccountCircle, "Blank Profile Picture")
                    }
                }
            }

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

            Text(
                text = review.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

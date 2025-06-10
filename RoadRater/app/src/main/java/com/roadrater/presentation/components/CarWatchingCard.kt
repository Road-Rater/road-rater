package com.roadrater.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roadrater.database.entities.Car
import com.roadrater.ui.theme.Licenz

@Composable
fun CarWatchingCard(
    car: Car,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .combinedClickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onClick),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    LogoByName(car.make.toString(), Modifier.width(50.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .border(1.dp, Color.Black, shape = RoundedCornerShape(5.dp))
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        car.number_plate,
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = Licenz,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text("${car.year} ${car.make} ${car.model?.substringBefore(" ")}")
        }
    }
}

@Composable
fun LogoByName(make: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val cleanedMake = make.lowercase().replace(" ", "_")

    // Get the resource ID from the drawable name
    val resId = remember(cleanedMake) {
        context.resources.getIdentifier(cleanedMake, "drawable", context.packageName)
    }

    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = cleanedMake,
            modifier = modifier,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
        )
    } else {
        // Optional fallback if the image is missing
        Text("Image not found: $cleanedMake", color = Color.Red)
    }
}

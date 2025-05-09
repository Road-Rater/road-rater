package com.roadrater.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DirectionsCarFilled
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.roadrater.R
import com.roadrater.presentation.util.Tab
import com.roadrater.presentation.components.ReviewCard
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.User

object MyReviews : Tab {
    private fun readResolve(): Any = MyReviews

    override val options: TabOptions
        @Composable
        get() {
            val image = rememberVectorPainter(Icons.Outlined.DirectionsCarFilled)
            return TabOptions(
                index = 0u,
                title = stringResource(R.string.home_tab),
                icon = image,
            )
        }

    @Composable
    override fun Content() {
        val labels = listOf("All", "Speeding", "Safe", "Reckless")
        var selectedLabel by remember { mutableStateOf("All") }
        var sortOption by remember { mutableStateOf("Date") } // "Date" or "Title"
        var sortAsc by remember { mutableStateOf(true) }
    }
}
package com.roadrater.ui.home.tabs

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.roadrater.presentation.components.LogoHeader
import com.roadrater.presentation.components.TextPreferenceWidget
import com.roadrater.presentation.util.ScrollbarLazyColumn
import com.roadrater.presentation.util.Tab
import com.roadrater.ui.MyReviews
import com.roadrater.ui.WatchedCarsScreen

object MoreTab : Tab {
    private fun readResolve(): Any = HomeTab

    override val options: TabOptions
        @Composable
        get() {
            val image = rememberVectorPainter(Icons.Outlined.MoreHoriz)
            return TabOptions(
                index = 0u,
                title = "More",
                icon = image,
            )
        }

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
//        val screenModel = rememberScreenModel { HomeTabScreenModel() }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {},
                )
            },
            floatingActionButton = {},
        ) { paddingValues ->

            ScrollbarLazyColumn(
                modifier = Modifier.padding(paddingValues),
            ) {
                item {
                    LogoHeader()
                }

                item { HorizontalDivider() }

                item {
                    TextPreferenceWidget(
                        title = "Watched Cars",
                        icon = Icons.AutoMirrored.Outlined.Label,
                        onPreferenceClick = { navigator.push(WatchedCarsScreen) },
                    )
                }
                item {
                    TextPreferenceWidget(
                        title = "Stats",
                        icon = Icons.Outlined.QueryStats,
                        onPreferenceClick = { },
                    )
                }
                item {
                    TextPreferenceWidget(
                        title = "My Reviews",
                        icon = Icons.Outlined.Storage,
                        onPreferenceClick = { navigator.push(MyReviews) },
                    )
                }

                item { HorizontalDivider() }

                item {
                    TextPreferenceWidget(
                        title = "Settings",
                        icon = Icons.Outlined.Settings,
                        onPreferenceClick = { },
                    )
                }
                item {
                    TextPreferenceWidget(
                        title = "About",
                        icon = Icons.Outlined.Info,
                        onPreferenceClick = { },
                    )
                }
                item {
                    TextPreferenceWidget(
                        title = "Help",
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        onPreferenceClick = { },
                    )
                }
            }
        }
    }
}

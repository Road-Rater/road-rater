package com.roadrater.ui.home.tabs

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.DirectionsCarFilled
import androidx.compose.material.icons.outlined.Flag
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
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.roadrater.R
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.presentation.components.LogoHeader
import com.roadrater.presentation.components.preferences.TextPreferenceWidget
import com.roadrater.presentation.util.ScrollbarLazyColumn
import com.roadrater.presentation.util.Tab
import com.roadrater.ui.FlaggedMessagesScreen
import com.roadrater.ui.MyReviewsScreen
import com.roadrater.ui.WatchedCarsScreen
import com.roadrater.ui.preferences.PreferencesScreen
import com.roadrater.ui.preferences.options.AboutPreferencesScreen
import org.koin.compose.koinInject
import com.roadrater.ui.myCars.MyCarsScreen

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
        val screenModel = rememberScreenModel { MoreTabScreenModel() }
        val generalPreferences = koinInject<GeneralPreferences>()
        val currentUser = generalPreferences.user.get()

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

                //MY CARS
                item {
                    TextPreferenceWidget(
                        title = stringResource(R.string.my_cars),
                        icon = Icons.Outlined.DirectionsCarFilled,
                        onPreferenceClick = { navigator.push(MyCarsScreen) },
                    )
                }

                item { HorizontalDivider() }

                //WATCHED CARS
                item {
                    TextPreferenceWidget(
                        title = stringResource(R.string.watched_cars),
                        icon = Icons.AutoMirrored.Outlined.Label,
                        onPreferenceClick = { navigator.push(WatchedCarsScreen) },
                    )
                }

                //STATS
                item {
                    TextPreferenceWidget(
                        title = stringResource(R.string.stats),
                        icon = Icons.Outlined.QueryStats,
                        onPreferenceClick = { },
                    )
                }

                //MY REVIEWS
                item {
                    TextPreferenceWidget(
                        title = stringResource(R.string.my_reviews),
                        icon = Icons.Outlined.Storage,
                        onPreferenceClick = { navigator.push(MyReviewsScreen) },
                    )
                }

                item { HorizontalDivider() }

                //SETTINGS
                item {
                    TextPreferenceWidget(
                        title = stringResource(R.string.settings),
                        icon = Icons.Outlined.Settings,
                        onPreferenceClick = { navigator.push(PreferencesScreen) },
                    )
                }

                //ABOUT
                item {
                    TextPreferenceWidget(
                        title = stringResource(R.string.about),
                        icon = Icons.Outlined.Info,
                        onPreferenceClick = { navigator.push(AboutPreferencesScreen) },
                    )
                }
                if (currentUser?.is_moderator == true) {
                    item {
                        TextPreferenceWidget(
                            title = stringResource(R.string.flagged_messages),
                            icon = Icons.Outlined.Flag,
                            onPreferenceClick = { navigator.push(FlaggedMessagesScreen) },
                        )
                    }
                }
            }
        }
    }
}

package com.roadrater.ui.home.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabOptions
import coil3.compose.AsyncImage
import com.roadrater.R
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.presentation.util.Tab
import org.koin.compose.koinInject

object ProfileTab : Tab {
    private fun readResolve(): Any = ProfileTab

    override val options: TabOptions
        @Composable
        get() {
            val image = rememberVectorPainter(Icons.Outlined.Person)
            return TabOptions(
                index = 0u,
                title = stringResource(R.string.profile_tab),
                icon = image,
            )
        }

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { ProfileTabScreenModel() }
        val generalPreferences = koinInject<GeneralPreferences>()
        val currentUser = generalPreferences.user.get()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profile") },
                    actions = {
                        AsyncImage(
                            model = currentUser?.profile_pic_url,
                            contentDescription = "Profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { },
                        )
                    },
                )
            },
            floatingActionButton = {},
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
//                Text("Profile Tab")
            }
        }
    }
}

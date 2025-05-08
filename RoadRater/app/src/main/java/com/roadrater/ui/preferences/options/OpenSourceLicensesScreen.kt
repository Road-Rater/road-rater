package com.roadrater.ui.preferences.options

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.util.htmlReadyLicenseContent
import com.roadrater.R
import com.roadrater.presentation.Screen

class OpenSourceLicensesScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.licenses)) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                        }
                    },
                )
            },
        ) { contentPadding ->

            LibrariesContainer(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = contentPadding,
                onLibraryClick = {
                    navigator.push(
                        OpenSourceLibraryLicenseScreen(
                            name = it.name,
                            website = it.website,
                            license = it.licenses.firstOrNull()?.htmlReadyLicenseContent.orEmpty(),
                        ),
                    )
                },
            )
        }
    }
}

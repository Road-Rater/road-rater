package com.roadrater.ui.preferences.options

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.roadrater.BuildConfig
import com.roadrater.R
import com.roadrater.presentation.Screen
import com.roadrater.presentation.components.LogoHeader
import com.roadrater.presentation.components.icons.CustomIcons
import com.roadrater.presentation.components.icons.Github
import com.roadrater.presentation.components.preferences.TextPreferenceWidget
import com.roadrater.utils.CrashLogUtil
import com.roadrater.utils.copyToClipboard
import com.roadrater.utils.openInBrowser

object AboutPreferencesScreen : Screen() {
    private fun readResolve(): Any = AboutPreferencesScreen

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.pref_about_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                        }
                    },
                )
            },
        ) { contentPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding),
            ) {
                LogoHeader()

                TextPreferenceWidget(
                    title = stringResource(R.string.version),
                    subtitle = getVersionName(true),
                    onPreferenceClick = {
                        val deviceInfo = CrashLogUtil(context).getDebugInfo()
                        context.copyToClipboard("Debug information", deviceInfo)
                    },
                )

                TextPreferenceWidget(
                    title = stringResource(R.string.check_for_updates),
                    widget = {},
                    onPreferenceClick = {
                        context.openInBrowser("https://github.com/Road-Rater/road-rater/releases/latest")
                    },
                )

                TextPreferenceWidget(
                    title = stringResource(R.string.licenses),
                    onPreferenceClick = { navigator.push(OpenSourceLicensesScreen()) },
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    LinkIcon(
                        label = "GitHub",
                        icon = CustomIcons.Github,
                        url = "https://github.com/Road-Rater/road-rater",
                    )
                }
            }
        }
    }

    fun getVersionName(withBuildDate: Boolean): String {
        return when {
            BuildConfig.DEBUG -> {
                "Road Rater Debug r${BuildConfig.COMMIT_COUNT}".let {
                    if (withBuildDate) {
                        "$it (${BuildConfig.BUILD_TIME})"
                    } else {
                        it
                    }
                }
            }
            else -> {
                "Road Rater Stable v${BuildConfig.VERSION_NAME}".let {
                    if (withBuildDate) {
                        "$it (${BuildConfig.BUILD_TIME})"
                    } else {
                        it
                    }
                }
            }
        }
    }

    @Composable
    fun LinkIcon(
        label: String,
        icon: ImageVector,
        url: String,
        modifier: Modifier = Modifier,
    ) {
        val uriHandler = LocalUriHandler.current
        IconButton(
            modifier = modifier.padding(4.dp),
            onClick = { uriHandler.openUri(url) },
        ) {
            Icon(
                imageVector = icon,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = label,
            )
        }
    }
}

package com.roadrater.presentation.components.preferences

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.roadrater.preferences.AppearancePreferences
import com.roadrater.preferences.preference.collectAsState
import com.roadrater.presentation.themes.BaseColorScheme
import com.roadrater.presentation.themes.GreenAppleColorScheme
import com.roadrater.presentation.themes.LavenderColorScheme
import com.roadrater.presentation.themes.MidnightDuskColorScheme
import com.roadrater.presentation.themes.MonetColorScheme
import com.roadrater.presentation.themes.NordColorScheme
import com.roadrater.presentation.themes.StrawberryColorScheme
import com.roadrater.presentation.themes.TachiyomiColorScheme
import com.roadrater.presentation.themes.TakoColorScheme
import com.roadrater.presentation.themes.TealTurqoiseColorScheme
import com.roadrater.presentation.themes.TidalWaveColorScheme
import com.roadrater.presentation.themes.YinYangColorScheme
import com.roadrater.presentation.themes.YotsubaColorScheme
import com.roadrater.ui.theme.AppTheme
import com.roadrater.ui.theme.ThemeMode
import org.koin.compose.koinInject

@Composable
fun TachiyomiTheme(
    appTheme: AppTheme? = null,
    amoled: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val uiPreferences = koinInject<AppearancePreferences>()

    BaseTachiyomiTheme(
        appTheme = (appTheme ?: uiPreferences.appTheme.get()),
        isAmoled = (amoled ?: uiPreferences.themeDarkAmoled.get()),
        content = content,
    )
}

@Composable
fun TachiyomiPreviewTheme(
    appTheme: AppTheme = AppTheme.DEFAULT,
    isAmoled: Boolean = false,
    content: @Composable () -> Unit,
) = BaseTachiyomiTheme(appTheme, isAmoled, content)

@Composable
private fun BaseTachiyomiTheme(
    appTheme: AppTheme,
    isAmoled: Boolean,
    content: @Composable () -> Unit,
) {
    val preferences = koinInject<AppearancePreferences>()
    val themeMode by preferences.themeMode.collectAsState()

    MaterialTheme(
        colorScheme = getThemeColorScheme(appTheme, themeMode, isAmoled),
        content = content,
    )
}

@Composable
@ReadOnlyComposable
private fun getThemeColorScheme(
    appTheme: AppTheme,
    themeMode: ThemeMode,
    isAmoled: Boolean,
): ColorScheme {
    val colorScheme = if (appTheme == AppTheme.MONET) {
        MonetColorScheme(LocalContext.current)
    } else {
        colorSchemes.getOrDefault(appTheme, TachiyomiColorScheme)
    }

    var dark: Boolean = false

    if (themeMode == ThemeMode.DARK) {
        dark = true
    } else if (themeMode == ThemeMode.LIGHT) {
        dark = false
    } else if (themeMode == ThemeMode.SYSTEM) {
        dark = isSystemInDarkTheme()
    }

    return colorScheme.getColorScheme(
        dark,
        isAmoled,
    )
}

private val colorSchemes: Map<AppTheme, BaseColorScheme> = mapOf(
    AppTheme.DEFAULT to TachiyomiColorScheme,
    AppTheme.GREEN_APPLE to GreenAppleColorScheme,
    AppTheme.LAVENDER to LavenderColorScheme,
    AppTheme.MIDNIGHT_DUSK to MidnightDuskColorScheme,
    AppTheme.NORD to NordColorScheme,
    AppTheme.STRAWBERRY_DAIQUIRI to StrawberryColorScheme,
    AppTheme.TAKO to TakoColorScheme,
    AppTheme.TEALTURQUOISE to TealTurqoiseColorScheme,
    AppTheme.TIDAL_WAVE to TidalWaveColorScheme,
    AppTheme.YINYANG to YinYangColorScheme,
    AppTheme.YOTSUBA to YotsubaColorScheme,
)

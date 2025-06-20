package com.roadrater

import android.app.Application
import androidx.compose.runtime.getValue
import com.roadrater.di.DatabaseModule
import com.roadrater.di.PreferencesModule
import com.roadrater.di.RepositoryModule
import com.roadrater.di.ScreenModelModule
import com.roadrater.di.SupabaseModule
import com.roadrater.preferences.AppearancePreferences
import com.roadrater.presentation.crash.CrashActivity
import com.roadrater.presentation.crash.GlobalExceptionHandler
import com.roadrater.ui.theme.setAppCompatDelegateThemeMode
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.inject
import org.koin.core.context.startKoin

class App : Application() {
    private val appearancePreferences by inject<AppearancePreferences>()

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(
            GlobalExceptionHandler(applicationContext, CrashActivity::class.java),
        )

        startKoin {
            androidContext(this@App)

            modules(
                PreferencesModule,
                RepositoryModule,
                DatabaseModule,
                SupabaseModule,
                ScreenModelModule,
            )
        }

        setAppCompatDelegateThemeMode(appearancePreferences.themeMode.get())
    }
}

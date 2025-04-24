package com.roadrater

import android.app.Application
import com.roadrater.di.DatabaseModule
import com.roadrater.di.PreferencesModule
import com.roadrater.di.RepositoryModule
import com.roadrater.di.SupabaseModule
import com.roadrater.presentation.crash.CrashActivity
import com.roadrater.presentation.crash.GlobalExceptionHandler
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
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
            )
        }
    }
}

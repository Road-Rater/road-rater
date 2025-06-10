package com.roadrater.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.roadrater.database.Migrations
import com.roadrater.database.RRDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val DatabaseModule = module {
    single<RRDatabase> {
        Room
            .databaseBuilder(androidContext(), RRDatabase::class.java, "RR.db")
            .addMigrations(migrations = Migrations)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                }
            })
            .build()
    }
}

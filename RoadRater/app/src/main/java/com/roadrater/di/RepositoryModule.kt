package com.roadrater.di

import com.roadrater.database.repository.CarRepository
import com.roadrater.database.repository.ReviewRepository
import org.koin.dsl.module

val RepositoryModule = module {
    single { ReviewRepository(get()) }
    single { CarRepository(get()) }
}
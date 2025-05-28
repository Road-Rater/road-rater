package com.roadrater.di

import com.roadrater.ui.home.tabs.ProfileTabScreenModel
import org.koin.dsl.module

val ScreenModelModule = module {
    factory {
        ProfileTabScreenModel(get(), get())
    }
}
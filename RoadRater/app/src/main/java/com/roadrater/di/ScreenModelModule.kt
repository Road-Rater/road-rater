package com.roadrater.di

import com.roadrater.database.entities.User
import com.roadrater.ui.CarDetailScreenModel
import com.roadrater.ui.ProfileScreenModel
import com.roadrater.ui.home.tabs.HomeTabScreenModel
import org.koin.dsl.module

val ScreenModelModule = module {
    factory { (user: User) ->
        ProfileScreenModel(user, get(), get())
    }

    factory { (plate: String) ->
        CarDetailScreenModel(plate, get(), get())
    }

    factory { (currentUser: User) ->
        HomeTabScreenModel(currentUser, get(), get())
    }
}

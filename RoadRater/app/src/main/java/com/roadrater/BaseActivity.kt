package com.roadrater

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.roadrater.preferences.AppearancePreferences
import com.roadrater.ui.theme.ThemingDelegate
import com.roadrater.ui.theme.ThemingDelegateImpl
import org.koin.android.ext.android.inject
import kotlin.getValue

open class BaseActivity :
    AppCompatActivity(),
    ThemingDelegate by ThemingDelegateImpl() {
    val appearancePreferences by inject<AppearancePreferences>()

    override fun onCreate(savedInstanceState: Bundle?) {
        applyAppTheme(this, appearancePreferences)
        super.onCreate(savedInstanceState)
    }
}

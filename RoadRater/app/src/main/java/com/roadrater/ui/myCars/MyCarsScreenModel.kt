package com.roadrater.ui.myCars

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import cafe.adriel.voyager.core.model.ScreenModel

class MyCarsScreenModel : ScreenModel {
    var inputText = mutableStateOf(TextFieldValue(""))
}
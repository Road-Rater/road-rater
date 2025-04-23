package com.roadrater.auth

import android.content.Context
import com.google.android.gms.signin.internal.SignInClientImpl

class AuthUIClient(
    private val context: Context,
    private val oneTapClient: SignInClientImpl,
)

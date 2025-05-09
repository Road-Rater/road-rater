package com.roadrater.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.roadrater.R
import com.roadrater.database.entities.TableUser
import com.roadrater.database.entities.User
import com.roadrater.preferences.GeneralPreferences
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.java.KoinJavaComponent.getKoin

// Handles authentication logic, including Google Sign-In and user state management.
class Auth() {

    // Singleton object to maintain and update the signed-in user state and handle sign-in flow.
    companion object {
        // Holds the current signed-in user as a StateFlow.
        private val _signedInUser = MutableStateFlow<User?>(null)
        val signedInUser: StateFlow<User?> = _signedInUser.asStateFlow()

        // Access to general preferences for storing user data.
        val generalPreferences = getKoin().get<GeneralPreferences>()

        // Updates the signed-in user and persists it in shared preferences.
        fun updateUser(user: User?) {
            _signedInUser.value = user
            generalPreferences.user.set(user)
        }

        // Attempts to sign in using Google ID token, authenticates with Firebase, and syncs user to Supabase.
        fun attemptGoogleSignIn(
            context: Context,
            scope: CoroutineScope,
            supabaseClient: SupabaseClient,
        ) {
            // Prepare the Google Sign-In credential request.
            val credentialManager = CredentialManager.create(context)
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(getCredentialOptions(context))
                .build()

            scope.launch {
                // Launch coroutine to handle async sign-in flow.
                try {
                    val result = credentialManager.getCredential(context, request)

                    // Check the type of credential returned.
                    when (result.credential) {
                        is CustomCredential -> {
                            if (result.credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                                val googleTokenId = googleIdTokenCredential.idToken
                                val authCredential = GoogleAuthProvider.getCredential(googleTokenId, null)
                                var firebaseUser = Firebase.auth.signInWithCredential(authCredential).await().user

                                // If user is not anonymous, map Firebase user to our User model.
                                firebaseUser?.let {
                                    if (it.isAnonymous.not()) {
                                        val user = User(
                                            uid = firebaseUser.uid,
                                            name = firebaseUser.displayName,
                                            nickname = firebaseUser.displayName,
                                            email = firebaseUser.email,
                                            profile_pic_url = firebaseUser.photoUrl.toString(),
                                        )

                                        // Insert user into Supabase if not already existing.
                                        try {
                                            val existingUsers = supabaseClient.postgrest["users"].select { filter { eq("uid", user.uid) } }
                                            if (existingUsers.data.isEmpty() || existingUsers.data == "[]") {
                                                val response = supabaseClient.postgrest["users"].insert(TableUser(user.uid, user.nickname, null, user.email))
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        updateUser(user)
                                    }
                                }
                            }
                        } else -> {
                        }
                    }
                } catch (e: GetCredentialException) {
                    e.printStackTrace()
                }
            }
        }

        // Returns the Google ID credential options used for sign-in.
        private fun getCredentialOptions(context: Context): CredentialOption {
            return GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.web_client_id))
                .build()
        }

        // Retrieves the current signed-in user.
        fun getSignedInUser(): User? = _signedInUser.value
    }
}

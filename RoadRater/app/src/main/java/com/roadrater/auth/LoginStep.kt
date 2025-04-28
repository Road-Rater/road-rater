package com.roadrater.auth

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.google.android.gms.auth.api.identity.Identity
import com.roadrater.database.entities.TableUser
import com.roadrater.di.SupabaseModule
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.ui.theme.spacing
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

internal class LoginStep(
    val viewModel: SignInViewModel,
    val onSignInClick: () -> Unit,
) : OnboardingStep {

    private var _isComplete by mutableStateOf(false)

    override val isComplete: Boolean
        get() = _isComplete

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val handler = LocalUriHandler.current
        val supabaseClient = koinInject<SupabaseClient>()
        val state by viewModel.state.collectAsState()
        var user by remember { mutableStateOf<UserData?>(null) }

        LaunchedEffect(Unit) {
            GoogleAuthUiClient(context, Identity.getSignInClient(context)).signOut()
        }

        LaunchedEffect(state.isSignInSuccessful) {
            if (state.isSignInSuccessful) {
                user = GoogleAuthUiClient(context, Identity.getSignInClient(context)).getSignedInUser()
                if (user != null) {
                    val id = user!!.userId
                    val name = user!!.username.toString()
                    val email = user!!.email.toString()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val existingUsers = supabaseClient.postgrest["users"].select { filter { eq("uid", id) } }
                            if (existingUsers.data.isEmpty() || existingUsers.data == "[]") {
                                val response = supabaseClient.postgrest["users"].insert(TableUser(id, name, null, email))
                            }

                        } catch (e: Exception) {
                        }
                    }
                }
                _isComplete = true
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            Text("Login or create an account using Google")

            if (user != null) {
                UserCard(user)
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onSignInClick()
                    },
                ) {
                    Text("Login with Google")
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    _isComplete = true
                },
            ) {
                Text("Continue as Guest")
            }
        }
    }

    @Composable
    fun UserCard(
        user: UserData?,
        modifier: Modifier = Modifier
    ) {
        if (user == null) return

        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                user.profilePictureUrl?.let { imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "User profile image",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = user.username ?: "No name",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "ID: ${user.email}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

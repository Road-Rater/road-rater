package com.roadrater.auth.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.roadrater.R
import com.roadrater.auth.OnboardingStep
import com.roadrater.database.entities.TableUser
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.ui.theme.spacing
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import org.koin.compose.getKoin
import org.koin.compose.koinInject

internal class NicknameStep : OnboardingStep {

    private var _isComplete by mutableStateOf(false)

    override val isComplete: Boolean
        get() = _isComplete

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val handler = LocalUriHandler.current
        val scope = rememberCoroutineScope()

        var nicknameAvailable by remember { mutableStateOf(true) }
        val generalPreferences = getKoin().get<GeneralPreferences>()
        val currentUser = generalPreferences.user.get()
        val defaultNickname = currentUser?.nickname ?: ""
        var nickname by remember { mutableStateOf(defaultNickname) }
        val focusRequester = remember { FocusRequester() }
        val supabaseClient = koinInject<SupabaseClient>()

        Column(
            modifier = Modifier.Companion.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            Text(stringResource(R.string.select_nickname_title))

            OutlinedTextField(
                modifier = Modifier.Companion
                    .focusRequester(focusRequester),
                value = nickname,
                onValueChange = { newNickname ->
                    nickname = newNickname
                    scope.launch {
                        nicknameAvailable = nicknameAvailable(newNickname, supabaseClient)
                    }
                },
                label = {
                    Text(stringResource(R.string.nickname))
                },
                supportingText = {
                    val msgRes = if (nickname.isNotEmpty() && !nicknameAvailable) {
                        R.string.onboarding_nickname_taken
                    } else {
                        R.string.information_required_plain
                    }
                    Text(text = stringResource(msgRes))
                },
                isError = nickname.isNotEmpty() && !nicknameAvailable,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Text),
                singleLine = true,
            )

            Button(
                onClick = {
                    if (currentUser == null) return@Button
                    scope.launch {
                        if (nicknameAvailable) {
                            scope.launch {
                                try {
                                    supabaseClient
                                        .from("users")
                                        .update(
                                            {
                                                set("nickname", nickname)
                                            },
                                        ) {
                                            filter {
                                                eq("uid", currentUser.uid)
                                            }
                                        }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            _isComplete = true
                        }
                    }
                },
                enabled = nicknameAvailable,
            ) {
                Text(stringResource(R.string.select))
            }
        }
    }

    suspend fun nicknameAvailable(nickname: String, supabaseClient: SupabaseClient): Boolean {
        return try {
            val response = supabaseClient.from("users").select { filter { eq("nickname", nickname) } }.decodeList<TableUser>()
            response.isEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

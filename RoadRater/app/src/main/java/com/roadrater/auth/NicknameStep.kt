package com.roadrater.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.roadrater.ui.theme.spacing

internal class NicknameStep : OnboardingStep {

    private var _isComplete by mutableStateOf(false)

    override val isComplete: Boolean
        get() = _isComplete

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val handler = LocalUriHandler.current

        var nicknameAvailable: Boolean = true
        var nickname by remember { mutableStateOf("") }
        val nameAlreadyExists = remember(nickname) { nicknameAvailable(nickname) }
        val focusRequester = remember { FocusRequester() }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            Text("Select a nickname, this is the name others will know you by")

            OutlinedTextField(
                modifier = Modifier
                    .focusRequester(focusRequester),
                value = nickname,
                onValueChange = { nickname = it },
                label = {
                    Text("Nickname")
                },
                supportingText = {
                    val msgRes = if (nickname.isNotEmpty() && nameAlreadyExists) {
                        R.string.onboarding_nickname_taken
                    } else {
                        R.string.information_required_plain
                    }
                    Text(text = stringResource(msgRes))
                },
                isError = nickname.isNotEmpty() && nameAlreadyExists,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    nicknameAvailable = nicknameAvailable(nickname)
                    _isComplete = true
                },
            ) {
                Text("Select")
            }

            if (!nicknameAvailable) {
                Text("This nickname is already in use.")
            }
        }
    }

    private fun nicknameAvailable(
        nickname: String,
    ): Boolean {
        return false
    }
}

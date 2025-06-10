package com.roadrater.ui.preferences.options

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.roadrater.preferences.GeneralPreferences

object OptOutScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val supabaseClient = koinInject<SupabaseClient>()
        val generalPreferences = koinInject<GeneralPreferences>()
        val currentUser = generalPreferences.user.get()

        var isLoading by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Opt Out") },
                    navigationIcon = {
                        IconButton(onClick = { navigator?.pop() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "What happens when you opt out?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = """
                            • Your reviews and comments will be hidden from everyone.
                            • Your name will appear as "Anonymous" in all mentions.
                            • Your profile will no longer be viewable.
                            • You can opt back in at any time.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Button(
                    onClick = {
                        if (currentUser != null) {
                            isLoading = true
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    supabaseClient.from("users")
                                        .update(mapOf("is_visible" to false)) {
                                            filter {
                                                eq("id", currentUser.uid)
                                            }
                                        }
                                } catch (e: Exception) {
                                    println("Error opting out: ${e.message}")
                                } finally {
                                    isLoading = false
                                    navigator?.pop()
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Confirm Opt Out")
                }
            }
        }
    }
}
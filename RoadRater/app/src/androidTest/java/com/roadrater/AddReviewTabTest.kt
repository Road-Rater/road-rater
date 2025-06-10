package com.roadrater

import android.content.Context
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.roadrater.preferences.GeneralPreferences
import com.roadrater.preferences.preference.AndroidPreferenceStore
import com.roadrater.ui.home.tabs.AddReviewTab
import io.github.jan.supabase.SupabaseClient
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class AddReviewTabTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    companion object {
        private val mockSupabaseClient = mock<SupabaseClient>()
        private lateinit var generalPreferences: GeneralPreferences

        @JvmStatic
        @BeforeClass
        fun setupKoin() {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val preferenceStore = AndroidPreferenceStore(context)
            generalPreferences = GeneralPreferences(preferenceStore)

            org.koin.core.context.stopKoin() // just in case
            org.koin.core.context.startKoin {
                modules(
                    module {
                        single<SupabaseClient> { mockSupabaseClient }
                        single<GeneralPreferences> { generalPreferences }
                    },
                )
            }
        }

        @JvmStatic
        @AfterClass
        fun tearDownKoin() {
            org.koin.core.context.stopKoin()
        }
    }

    @Test
    fun showsValidationMessages_onInvalidSubmission() {
        composeTestRule.setContent {
            AddReviewTab.Content()
        }

        composeTestRule.onNodeWithText("Submit Review").performClick()

        composeTestRule.onNodeWithText("Please enter a valid number plate format").assertExists()
        composeTestRule.onNodeWithText("Title is required").assertExists()
        composeTestRule.onNodeWithText("Review description is required").assertExists()
        composeTestRule.onNodeWithText("Please select a rating").assertExists()
    }

    @Test
    fun submitButton_isEnabled_whenAllFieldsValid() {
        composeTestRule.setContent {
            AddReviewTab.Content()
        }

        composeTestRule.onNodeWithText("Number Plate").performTextInput("ABC123")
        composeTestRule.onNodeWithText("Review Title").performTextInput("Respectful driver")
        composeTestRule.onNodeWithText("Your review").performTextInput("Signaled properly and stayed calm in traffic.")
        composeTestRule.onAllNodesWithContentDescription("Star g")[0].performClick()

        composeTestRule.onNodeWithText("Submit Review").assertIsEnabled()
    }
}

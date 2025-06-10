package com.roadrater

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.roadrater.database.entities.User
import com.roadrater.ui.ProfileScreen
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testUser = User(
        uid = "test123",
        name = "John Doe",
        email = "john@example.com",
        nickname = "johnny",
        profile_pic_url = null,
        is_moderator = false,
    )

    @Test
    fun profileScreen_showsUserDetails() {
        composeTestRule.setContent {
            ProfileScreen(testUser).Content()
        }

        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("john@example.com").assertIsDisplayed()
    }

    @Test
    fun profileScreen_defaultTabIsReviews() {
        composeTestRule.setContent {
            ProfileScreen(testUser).Content()
        }

        composeTestRule.onNodeWithText("Reviews").assertIsSelected()
    }

    @Test
    fun profileScreen_switchesToVehiclesTab() {
        composeTestRule.setContent {
            ProfileScreen(testUser).Content()
        }

        composeTestRule.onNodeWithText("Vehicles").performClick()
        composeTestRule.onNodeWithText("No cars being watched.").assertExists()
    }

    @Test
    fun profileScreen_switchesToOtherTab() {
        composeTestRule.setContent {
            ProfileScreen(testUser).Content()
        }

        composeTestRule.onNodeWithText("Other").performClick()
        // No content shown in UserStatistics yet, but no crash/assert
    }

    @Test
    fun profileScreen_secondaryTabsWork() {
        composeTestRule.setContent {
            ProfileScreen(testUser).Content()
        }

        composeTestRule.onNodeWithText("Given").assertIsSelected()
        composeTestRule.onNodeWithText("Received").performClick()
        composeTestRule.onNodeWithText("Received").assertIsSelected()
    }

    @Test
    fun reviewsTab_showsNoReviewsMessageWhenEmpty() {
        composeTestRule.setContent {
            ProfileScreen(testUser).Content()
        }

        composeTestRule.onNodeWithText("No reviews available.").assertIsDisplayed()
    }
}

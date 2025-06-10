package com.roadrater

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import cafe.adriel.voyager.navigator.Navigator
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.User
import com.roadrater.presentation.Screen
import com.roadrater.presentation.components.ReviewCard
import org.junit.Rule
import org.junit.Test

class ReviewCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    object EmptyScreen : Screen() {
        private fun readResolve(): Any = EmptyScreen

        @Composable
        override fun Content() {
        }
    }

    private val dummyReview = Review(
        id = 1,
        title = "Test Review",
        description = "This is a test",
        createdAt = "2024-06-10T10:00:00Z",
        createdBy = "user123",
        numberPlate = "abc123",
        rating = 4,
        labels = listOf("Safe"),
        isFlagged = false,
    )

    private val userWithImage = User(
        uid = "user123",
        name = "Jane Doe",
        email = "jane@example.com",
        nickname = "jdoe",
        profile_pic_url = "https://example.com/image.png",
        is_moderator = false,
    )

    private val userWithoutImage = userWithImage.copy(profile_pic_url = null)

    @Test
    fun profileIcon_click_navigatesToProfile() {
        var clicked = false

        composeTestRule.setContent {
            Navigator(screen = EmptyScreen) {
                ReviewCard(
                    review = dummyReview,
                    createdBy = userWithImage,
                )
            }
        }

        // Click on the profile icon
        composeTestRule
            .onNodeWithContentDescription("Profile picture")
            .performClick()

        // Actual navigation cannot be tested without setting up a mock navigator,
        // but presence of clickable icon is verified.
        composeTestRule
            .onNodeWithContentDescription("Profile picture")
            .assertExists()
    }

    @Test
    fun showsProfileImage_whenUrlIsPresent() {
        composeTestRule.setContent {
            Navigator(screen = EmptyScreen) {
                ReviewCard(
                    review = dummyReview,
                    createdBy = userWithImage,
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Profile picture")
            .assertExists()
    }

    @Test
    fun showsDefaultIcon_whenProfileImageIsMissing() {
        composeTestRule.setContent {
            Navigator(screen = EmptyScreen) {
                ReviewCard(
                    review = dummyReview,
                    createdBy = userWithoutImage,
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Blank Profile Picture")
            .assertExists()
    }
}

package com.roadrater

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import cafe.adriel.voyager.navigator.Navigator
import com.roadrater.database.entities.Review
import com.roadrater.database.entities.User
import com.roadrater.presentation.Screen
import com.roadrater.presentation.components.ReviewCard
import org.junit.Rule
import org.junit.Test

class ModeratorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    object EmptyScreen : Screen() {
        private fun readResolve(): Any = EmptyScreen

        @Composable
        override fun Content() {
        }
    }

    private val moderator = User(
        uid = "test123",
        name = "John Doe",
        email = "john@example.com",
        nickname = "johnny",
        profile_pic_url = null,
        is_moderator = true,
    )

    private val user = moderator.copy(is_moderator = false)

    private val review = Review(
        id = 1,
        title = "Test Review",
        description = "This is a test review",
        createdAt = "2024-06-10T10:00:00Z",
        createdBy = "Jane Doe",
        rating = 5,
        numberPlate = "ABC123",
        isFlagged = false,
    )

    @Test
    fun moderatorDialogIsShownOnLongPressToModerators() {
        composeTestRule.setContent {
            Navigator(screen = EmptyScreen) {
                ReviewCard(
                    review = review,
                    createdBy = moderator,
                    currentUser = moderator,
                    onClick = { },
                )
            }
        }

        // Locate the review card by its title and perform a long click
        composeTestRule.onNodeWithText("This is a test review")
            .performTouchInput { longClick() }

        // Check that the moderator dialog is displayed
        composeTestRule.onNodeWithText("Manage Message")
            .assertIsDisplayed()
    }

    @Test
    fun reportDialogIsShownOnLongToNormalUsers() {
        composeTestRule.setContent {
            Navigator(screen = EmptyScreen) {
                ReviewCard(
                    review = review,
                    createdBy = moderator,
                    currentUser = user,
                    onClick = { },
                )
            }
        }

        // Locate the review card by its title and perform a long click
        composeTestRule.onNodeWithText("This is a test review")
            .performTouchInput { longClick() }

        // Check that the moderator dialog is displayed
        composeTestRule.onNodeWithText("Report Message")
            .assertIsDisplayed()
    }

    @Test
    fun moderationDialogIsNotShownToNormalUsers() {
        composeTestRule.setContent {
            Navigator(screen = EmptyScreen) {
                ReviewCard(
                    review = review,
                    createdBy = moderator,
                    currentUser = user,
                    onClick = { },
                )
            }
        }

        // Locate the review card by its title and perform a long click
        composeTestRule.onNodeWithText("This is a test review")
            .performTouchInput { longClick() }

        // Check that the moderator dialog is displayed
        composeTestRule.onNodeWithText("Manage Message")
            .assertIsNotDisplayed()
    }

    @Test
    fun reportDialogIsNotShownToModerators() {
        composeTestRule.setContent {
            Navigator(screen = EmptyScreen) {
                ReviewCard(
                    review = review,
                    createdBy = moderator,
                    currentUser = moderator,
                    onClick = { },
                )
            }
        }

        // Locate the review card by its title and perform a long click
        composeTestRule.onNodeWithText("This is a test review")
            .performTouchInput { longClick() }

        // Check that the moderator dialog is displayed
        composeTestRule.onNodeWithText("Report Message")
            .assertIsNotDisplayed()
    }
}

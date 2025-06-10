package com.roadrater

import com.roadrater.database.entities.BlockedUser
import org.junit.Assert.*
import org.junit.Test

class BlockedUserCheckTest {


    @Test
    fun `User is blocked when matching user_blocking and blocked_user`() {
        val userBlocking = "owner123"
        val currentUser = "reviewer456"

        val blockedList = listOf(
            BlockedUser(uid = "abc", blocked_user = currentUser, user_blocking = userBlocking)
        )

        val isBlocked = blockedList.any {
            it.user_blocking == userBlocking && it.blocked_user == currentUser
        }

        assertTrue(isBlocked)
    }

    @Test
    fun `User is not blocked if blocked_user does not match`() {

        val blockedList = listOf(
            BlockedUser(uid = "abc", blocked_user = "someone_else", user_blocking = "owner123")
        )

        val isBlocked = blockedList.any {
            it.user_blocking == "owner123" && it.blocked_user == "reviewer456"
        }

        assertFalse(isBlocked)

    }

    @Test
    fun `User is not blocked when list is empty`() {

        val blockedList = emptyList<BlockedUser>()

        val isBlocked = blockedList.any {
            it.user_blocking == "owner123" && it.blocked_user == "reviewer456"
        }

        assertFalse(isBlocked)
    }
}

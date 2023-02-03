package com.esop.validators

import com.esop.schema.User
import com.esop.service.UserService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class UsernameValidatorTest {
    private val username = UserService()

    @Test
    fun `it should return true for non-existing username`() {
        //Assert
        val user = "sankar"
        assertTrue(username.check_username(user))
    }

    @Test
    fun `it should return false for existing username`() {
        //Assert
        val user = User("sankaranarayanan","M","+917550276216","sankar06@gmail.com","sankar");
        UserService.userList["sankar"] = user
        assertFalse(username.check_username("sankar"))
    }

}

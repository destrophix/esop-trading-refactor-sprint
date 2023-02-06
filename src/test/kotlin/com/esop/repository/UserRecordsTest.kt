package com.esop.repository

import com.esop.schema.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UserRecordsTest {
    @Test
    fun `should get user using userName`() {
        val userName = "sankar"
        val expectedUser = User("sankaranarayanan", "M", "+917550276216", "sankar06@gmail.com", "sankar")
        val userRecords = UserRecords()
        userRecords.addUser(expectedUser)

        val user = userRecords.getUser(userName)

        assertEquals(expectedUser, user)
    }

    @Test
    fun `should add user`() {
        val userName = "sankar"
        val expectedUser = User("sankaranarayanan", "M", "+917550276216", "sankar06@gmail.com", "sankar")
        val userRecords = UserRecords()

        userRecords.addUser(expectedUser)

        val user = userRecords.getUser(userName)
        assertEquals(expectedUser, user)
    }

    @Test
    fun `check if user is exists`() {
        val userName = "sankar"
        val expectedUser = User("sankaranarayanan", "M", "+917550276216", "sankar06@gmail.com", "sankar")
        val userRecords = UserRecords()
        userRecords.addUser(expectedUser)

        val response = userRecords.checkIfUserExists(userName)

        assertTrue(response)
    }

    @Test
    fun `check if user does not exists`() {
        val userName = "sankar"
        val userRecords = UserRecords()

        val response = userRecords.checkIfUserExists(userName)

        assertFalse(response)
    }
}
package com.esop.repository

import com.esop.schema.User
import jakarta.inject.Singleton

@Singleton
class UserRecords {
    private val users = mutableMapOf<String, User>()

    fun addUser(user: User) {
        users[user.username] = user
    }

    fun getUser(userName: String): User? {
        return users[userName]
    }

    fun checkIfUserExists(userName: String): Boolean{
        return users.containsKey(userName)
    }
}
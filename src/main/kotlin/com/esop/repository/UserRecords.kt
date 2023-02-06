package com.esop.repository

import com.esop.schema.User

class UserRecords {
    private val users = mutableMapOf<String, User>()

    fun addUser(user: User) {
        users[user.username] = user
    }

    fun getUser(userName: String): User? {
        return users[userName]

    }
}
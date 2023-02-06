package com.esop.repository

import com.esop.schema.User
import jakarta.inject.Singleton

@Singleton
class UserRecords {
    private val users = mutableMapOf<String, User>()
    private val emails = mutableListOf<String>()
    private val phoneNumbers = mutableListOf<String>()

    fun addUser(user: User) {
        users[user.username] = user
    }

    fun getUser(userName: String): User? {
        return users[userName]
    }

    fun checkIfUserExists(userName: String): Boolean {
        return users.containsKey(userName)
    }

    fun addEmail(emailId: String) {
        emails.add(emailId)
    }

    fun checkIfEmailExists(emailId: String): Boolean {
        return emails.contains(emailId)
    }

    fun addPhoneNumber(phoneNumber: String) {
        phoneNumbers.add(phoneNumber)
    }

    fun checkIfPhoneNumberExists(phoneNumber: String): Boolean {
        return phoneNumbers.contains(phoneNumber)
    }
}
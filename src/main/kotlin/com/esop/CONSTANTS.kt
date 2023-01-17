package com.esop.constant


val errors = mapOf(
    "PHONENUMBER_EXISTS" to "User with given phone number already exists.",
    "USERNAME_EXISTS" to "User with given username already exists",
    "EMAIL_EXISTS" to "User with given email already exists",
    "INVALID_EMAIL" to "Email id is not valid",
    "INVALID_PHONENUMBER" to "Phone number is not valids",
    "USER_DOES_NOT_EXISTS" to "User not found"
)

val success_response = mapOf(
    "USER_CREATED" to "User has been registered successfully."
)
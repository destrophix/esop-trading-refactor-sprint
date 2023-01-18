package com.esop.constant


val errors = mapOf(
    "PHONENUMBER_EXISTS" to "User with given phone number already exists.",
    "USERNAME_EXISTS" to "User with given username already exists",
    "EMAIL_EXISTS" to "User with given email already exists",
    "INVALID_EMAIL" to "Email id is not valid",
    "INVALID_PHONENUMBER" to "Phone number is not valid",
    "USER_DOES_NOT_EXISTS" to "User not found",
    "POSITIVE_QUANTITY" to "Quantity must be positive.",
    "POSITIVE_PRICE" to "Price must be positive.",
    "INVALID_TYPE" to "Given type doesn't exist.",
    "NO_ORDERS" to "User does not have any orders"
)

val success_response = mapOf(
    "USER_CREATED" to "User has been registered successfully."
)
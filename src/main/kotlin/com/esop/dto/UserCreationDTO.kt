package com.esop.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size


const val PHONE_NUMBER_REGEX = "^(\\+91[\\-\\s]?)?[0]?(91)?[789]\\d{9}\$"

const val USERNAME_REGEX = "^[a-zA-Z]+([a-zA-Z]|_|[0-9])*"

const val ALPHABET_SEQUENCE_REGEX = "^[a-zA-Z]+"


@Introspected
class UserCreationDTO @JsonCreator constructor(
    @JsonProperty("firstName")
    @field:NotNull(message = "First Name is required")
    @field:NotBlank(message = "First Name should not be empty")
    @field:Size(max=30, message = "First Name should not exceed 30 characters")
    @field:Pattern(regexp = "($ALPHABET_SEQUENCE_REGEX| *)", message = "First Name can only contain alphabets")
    var firstName: String? = null,

    @JsonProperty("lastName")
    @field:NotNull(message = "Last Name is required")
    @field:NotBlank(message = "Last Name should not be empty")
    @field:Size(max=30, message = "Last Name should not exceed 30 characters")
    @field:Pattern(regexp = "($ALPHABET_SEQUENCE_REGEX| *)", message = "Last Name can only contain alphabets")
    var lastName: String? = null,

    @JsonProperty("phoneNumber")
    @field:NotNull(message = "Phone Number is required")
    @field:NotBlank(message = "Phone Number should not be empty")
    @field:Pattern(regexp = "($PHONE_NUMBER_REGEX| *)", message = "Invalid Phone Number")
    var phoneNumber: String? = null,

    @JsonProperty("email")
    @field:NotNull(message = "Email is required")
    @field:NotBlank(message = "Email should not be empty")
    @field:Size(max=30, message = "Email should not exceed 30 characters")
    @field:Email
    var email: String,

    @JsonProperty("username")
    @field:NotNull(message = "User Name is required")
    @field:NotBlank(message = "User Name should not be empty")
    @field:Size(max=20, message = "User Name should not exceed 20 characters")
    @field:Pattern(regexp = "($USERNAME_REGEX| *)", message =
    "User Name should only consist alphabets, numbers or underscore(s) and it must start with an alphabet.")
    var username: String,
)

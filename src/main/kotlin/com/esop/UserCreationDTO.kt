package com.esop

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size


const val PHONE_NUMBER_REGEX = "^(\\+91[\\-\\s]?)?[0]?(91)?[789]\\d{9}\$"

const val EMAIL_REGEX = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"

const val USERNAME_REGEX = "^[a-zA-Z]+([a-zA-Z]|-|[0-9])*"

const val ALPHABET_SEQUENCE_REGEX = "^[a-zA-Z]+"

@Introspected
class UserCreationDTO @JsonCreator constructor(
    @JsonProperty("firstName")
    @field:NotNull
    @field:NotBlank(message = "First Name should not be empty")
    @field:Size(max=30, message = "First Name should not exceed 30 characters")
    @field:Pattern(regexp = "(${ALPHABET_SEQUENCE_REGEX}| *)", message = "First Name can only contain alphabets")
    var firstName: String,

    @JsonProperty("lastName")
    @field:NotNull
    @field:NotBlank(message = "Last Name should not be empty")
    @field:Size(max=30, message = "Last Name should not exceed 30 characters")
    @field:Pattern(regexp = "(${ALPHABET_SEQUENCE_REGEX}| *)", message = "Last Name can only contain alphabets")
    var lastName: String,

    @JsonProperty("phoneNumber")
    @field:NotNull
    @field:NotBlank(message = "Phone Number should not be empty")
    @field:Pattern(regexp = "(${PHONE_NUMBER_REGEX}| *)", message = "Invalid Phone Number")
    var phoneNumber: String,

    @JsonProperty("email")
    @field:NotNull
    @field:NotBlank(message = "Email should not be empty")
    @field:Size(max=30, message = "Email should not exceed 30 characters")
    @field:Pattern(regexp = "(${EMAIL_REGEX}| *)", message = "Invalid Email format")
    var email: String,

    @JsonProperty("username")
    @field:NotNull
    @field:NotBlank(message = "User Name should not be empty")
    @field:Size(max=20, message = "User Name should not exceed 20 characters")
    @field:Pattern(regexp = "(${USERNAME_REGEX}| *)", message =
    "User Name should only consist alphabets, numbers or hyphen(s) and should start with an alphabet.")
    var username: String,
)

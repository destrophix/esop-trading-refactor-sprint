package com.esop.dto

import com.esop.EmailValidatorConstraintAnnotation
import com.esop.PhoneNumberConstraintValidator
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size


const val PHONE_NUMBER_REGEX = "^(\\+91[\\-\\s]?)?[0]?(91)?[789]\\d{9}\$"

const val USERNAME_REGEX = "^[a-zA-Z]+([a-zA-Z]|_|[0-9])*"

const val EMAIL_REGEX = "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"

const val ALPHABET_SEQUENCE_REGEX = "^\\s*[a-zA-Z]+[a-zA-Z\\s]*"

@Introspected
class UserCreationDTO @JsonCreator constructor(
    @JsonProperty("firstName")
    @field:NotBlank(message = "First Name can not be missing or empty.")
    @field:Size(max=30, message = "First Name should not exceed 30 characters.")
    @field:Pattern(regexp = "($ALPHABET_SEQUENCE_REGEX| *)", message = "First Name can only contain alphabets.")
    var firstName: String? = null,

    @JsonProperty("lastName")
    @field:NotBlank(message = "Last Name can not be missing or empty.")
    @field:Size(max=30, message = "Last Name should not exceed 30 characters")
    @field:Pattern(regexp = "($ALPHABET_SEQUENCE_REGEX| *)", message = "Last Name can only contain alphabets")
    var lastName: String? = null,

    @JsonProperty("phoneNumber")
    @field:NotBlank(message = "Phone Number can not be missing or empty.")
    @field:PhoneNumberConstraintValidator()
    var phoneNumber: String? = null,

    @JsonProperty("email")
    @field:NotBlank(message = "Email can not be missing or empty.")
    @field:EmailValidatorConstraintAnnotation()
    var email: String? = null,

    @JsonProperty("username")
    @field:NotBlank(message = "User Name can not be missing or empty.")
    @field:Size(max=20, message = "User Name should not exceed 20 characters")
    @field:Pattern(regexp = "($USERNAME_REGEX| *)", message =
    "User Name should only consist alphabets, numbers or underscore(s) and it must start with an alphabet.")
    var username: String? = null
)

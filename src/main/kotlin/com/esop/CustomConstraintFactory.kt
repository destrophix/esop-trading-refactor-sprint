package com.esop

import com.esop.repository.UserRecords
import com.esop.service.UserService
import com.esop.validators.*
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import io.micronaut.context.annotation.Factory
import io.micronaut.validation.validator.constraints.ConstraintValidator
import jakarta.inject.Singleton

@Factory
class CustomConstraintFactory(private val userRecords: UserRecords) {
    val username = UserService(userRecords)

    @Singleton
    fun phoneNumberValidator(): ConstraintValidator<PhoneNumberValidator, String> {
        val phoneUtil = PhoneNumberUtil.getInstance()

        return ConstraintValidator { value, _, context ->
            value == null || try {
                phoneUtil.isValidNumber(phoneUtil.parse(value, null))
            } catch (e: NumberParseException) {
                context.messageTemplate(e.message)
                false
            }
        }
    }

    @Singleton
    fun userNameValidator(): ConstraintValidator<UsernameValidator, String> {
        return ConstraintValidator { value, _, _ ->
            value == null || !userRecords.checkIfUserExists(value)
        }
    }

    @Singleton
    fun emailValidator(): ConstraintValidator<EmailValidator, String> {
        return ConstraintValidator { value, _, _ ->
            value == null || validate(value)
        }
    }

    @Singleton
    fun emailAlreadyExists(): ConstraintValidator<EmailAlreadyExistsValidator, String> {
        return ConstraintValidator { value, _, _ ->
            value == null || userRecords.checkIfEmailExists(value)
        }
    }

    @Singleton
    fun phoneNumberAlreadyExists(): ConstraintValidator<PhoneNumberAlreadyExists, String> {
        return ConstraintValidator { value, _, _ ->
            value == null || !userRecords.checkIfPhoneNumberExists(value)
        }
    }

    fun validate(email: String): Boolean {
        val p = Regex("^[a-z0-9-_]+[\"]")
        if (p.containsMatchIn(email)) {
            return false
        }
        val email_regex =
            "^(?=[\"\'a-zA-Z0-9][\"\\s\'a-zA-Z0-9@._%+-]{5,253}\$)[\\s\"\'a-zA-Z0-9._%+-]{1,64}@(?:(?=[a-zA-Z0-9-]{1,63}\\.)[a-zA-Z0-9]+(?:-[a-zA-Z0-9]+)*\\.){1,8}[a-z0-9A-Z]{2,63}\$"
        val pattern = Regex(email_regex)
        return pattern.containsMatchIn(email)
    }
}
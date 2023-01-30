package com.esop

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import io.micronaut.context.annotation.Factory
import io.micronaut.validation.validator.constraints.ConstraintValidator
import jakarta.inject.Singleton


@Factory
class CustomConstraintFactory {
    @Singleton
    fun phoneNumberValidator() : ConstraintValidator<PhoneNumber, String> {
        val phoneUtil = PhoneNumberUtil.getInstance()

        return ConstraintValidator { value, annotation, context ->
            value == null || try {
                 phoneUtil.isValidNumber(phoneUtil.parse(value, null))
            } catch (e: NumberParseException) {
                context.messageTemplate(e.message)
                false
            }
        }
    }
}
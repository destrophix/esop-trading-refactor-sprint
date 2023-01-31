package com.esop
import org.apache.commons.validator.routines.EmailValidator
class Validator {

    fun checkIfEmailIsValid(email :String): Boolean {
        return EmailValidator.getInstance().isValid(email)
    }
}
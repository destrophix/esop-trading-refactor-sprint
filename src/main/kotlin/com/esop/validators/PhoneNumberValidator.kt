package com.esop.validators

import javax.validation.Constraint

@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
annotation class PhoneNumberValidator(
    val message: String = "Invalid Phone Number"
)
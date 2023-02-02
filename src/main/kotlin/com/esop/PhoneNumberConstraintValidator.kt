package com.esop

import javax.validation.Constraint
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
annotation class PhoneNumberConstraintValidator(
    val message: String = "Invalid Phone Number ({validatedValue})"
)
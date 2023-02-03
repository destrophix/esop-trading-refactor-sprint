package com.esop.validators

import javax.validation.Constraint

@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
annotation class EmailValidator(
    val message: String = "Invalid email address"
)



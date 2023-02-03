package com.esop.validators

import javax.validation.Constraint

@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
annotation class UsernameValidator(
    val message: String = "Username already exists"
)
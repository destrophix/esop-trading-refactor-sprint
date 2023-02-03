package com.esop.validators

import javax.validation.Constraint

@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
annotation class EmailAlreadyExistsValidator(
    val message: String = "Email address already exists"
)



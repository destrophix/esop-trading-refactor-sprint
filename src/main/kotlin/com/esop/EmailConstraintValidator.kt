package com.esop

import javax.validation.Constraint
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
annotation class EmailConstraintValidator(
    val message:String = "Invalid email Address"
)



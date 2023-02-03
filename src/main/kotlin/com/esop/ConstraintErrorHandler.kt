package com.esop

import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor
import io.micronaut.validation.exceptions.ConstraintExceptionHandler
import jakarta.inject.Singleton
import javax.validation.ConstraintViolationException


@Produces
@Singleton
@Replaces(ConstraintExceptionHandler::class)
@Requirements(
    Requires(classes = [ConstraintViolationException::class, ExceptionHandler::class])
)
class ValidationExceptionHandler(private val errorResponseProcessor: ErrorResponseProcessor<Any>) :
    ConstraintExceptionHandler(errorResponseProcessor) {

    override fun handle(request: HttpRequest<*>, exception: ConstraintViolationException): HttpResponse<*> {
        return HttpResponse.badRequest(mapOf("errors" to exception.constraintViolations.map { it.message }))
    }
}
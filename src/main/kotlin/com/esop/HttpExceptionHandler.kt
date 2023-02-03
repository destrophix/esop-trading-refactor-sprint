package com.esop

import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler

import jakarta.inject.Singleton


@Produces
@Singleton
@Requirements(
    Requires(classes = [HttpException::class, ExceptionHandler::class])
)
class HttpExceptionHandler :
    ExceptionHandler<HttpException, HttpResponse<*>> {

    override fun handle(request: HttpRequest<*>, exception: HttpException): HttpResponse<*> {
        return HttpResponse.status<Map<String, ArrayList<String>>>(exception.status)
            .body(mapOf("errors" to arrayListOf(exception.message)))
    }
}
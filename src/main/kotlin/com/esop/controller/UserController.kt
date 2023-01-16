package com.esop.controller

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Post

@Controller("/user")
class UserController {

    @Post(uri="/register", consumes = [MediaType.APPLICATION_JSON],produces=[MediaType.APPLICATION_JSON])
    fun register(): String {
        return "Example Response"
    }

    @Post(uri="/{userName}/order", consumes = [MediaType.APPLICATION_JSON],produces=[MediaType.APPLICATION_JSON])
    fun order(userName: String): String {
        return "Example Response"
    }
}
package com.esop.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Post
import org.json.JSONObject

@Controller("/user")
class UserController {

    @Post(uri="/register", consumes = [MediaType.APPLICATION_JSON],produces=[MediaType.APPLICATION_JSON])
    fun register(@Body body: String): HttpResponse<*> {
        val obj = JSONObject(body)

        val firstName = obj.optString("firstName").trim()
        val lastName = obj.optString("lastName").trim()
        val phoneNumber = obj.optString("phoneNumber").trim()
        val email = obj.optString("email").trim()
        val username = obj.optString("username").trim()

        val response = "{\"firstName\": $firstName, \"lastName\": $lastName, \"phoneNumber\": $phoneNumber, \"email\": $email, \"username\": $username}"

        return HttpResponse.ok<Any>().body(response)
    }

    @Post(uri="/{userName}/order", consumes = [MediaType.APPLICATION_JSON],produces=[MediaType.APPLICATION_JSON])
    fun order(userName: String): String {
        return "Example Response"
    }
}
package com.esop.controller

import com.esop.schema.User
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.json.tree.JsonObject



@Controller("/user")
class UserController {
    @Post(uri="/register", consumes = [MediaType.APPLICATION_JSON],produces=[MediaType.APPLICATION_JSON])

    //open fun register(@Body response: JsonObject): HttpResponse<*> {
    //    val  text:String = response.get("content").stringValue

//    }   var userr: User(firstName:String, lastName:String, email, phoneNumber);




}







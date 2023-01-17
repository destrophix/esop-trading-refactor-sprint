package com.esop.controller

import com.esop.schema.Order
import com.esop.schema.User
import com.esop.service.*
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Post
import io.micronaut.json.tree.JsonObject
import jakarta.inject.Inject
import org.json.JSONObject
import java.lang.Error


@Controller("/user")
class UserController {

    @Inject
    lateinit var userService: UserService


    @Post(uri="/register", consumes = [MediaType.APPLICATION_JSON],produces=[MediaType.APPLICATION_JSON])
    open fun register(@Body response: JsonObject): String {
          return this.userService.registerUser(response)
    }

    @Post(uri="/{userName}/order", consumes = [MediaType.APPLICATION_JSON],produces=[MediaType.APPLICATION_JSON])
    fun order(userName: String, @Body body: JsonObject){
        var quantity: Long = body.get("quantity").longValue
        var type: String = body.get("type").stringValue
        var price: Long = body.get("price").longValue
        println(type)
        println(price)
        println(quantity)
        println(userName)
        println(body)
    }

    @Get(uri = "/{userName}/accountInformation", produces = [MediaType.APPLICATION_JSON])
    fun getAccountInformation(userName: String): String {
        return "Some response"
    }

    @Post(uri = "{userName}/inventory", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun addInventory(userName: String): String {
        return "Some response"
    }

    @Post(uri = "{userName}/wallet", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun addWallet(userName: String): String {
        return "Some response"
    }

    @Get(uri = "/{userName}/order", produces = [MediaType.APPLICATION_JSON])
    fun getOrder(userName: String): String {
        return "Some response"
    }
}
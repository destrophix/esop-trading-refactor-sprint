package com.esop.controller

import com.esop.service.*
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Post
import io.micronaut.json.tree.JsonObject
import jakarta.inject.Inject


@Controller("/user")
class UserController {

    @Inject
    lateinit var orderService: OrderService

    @Inject
    lateinit var userService: UserService

    @Post(uri="/register", consumes = [MediaType.APPLICATION_JSON],produces=[MediaType.APPLICATION_JSON])
    open fun register(@Body response: JsonObject): HttpResponse<*> {
        val newUser = this.userService.registerUser(response)
        return HttpResponse.ok(newUser)
    }

    @Post(uri="/{userName}/order", consumes = [MediaType.APPLICATION_JSON],produces=[MediaType.APPLICATION_JSON])

    fun order(userName: String, @Body body: JsonObject): Any? {
        var quantity: Long = body.get("quantity").longValue
        var type: String = body.get("type").stringValue
        var price: Long = body.get("price").longValue
        var userErrors = this.userService.orderCheckBeforePlace(userName, quantity, type, price)
        if(userErrors["errors"]?.isEmpty()!!){
            println("PLACING ORDER")
            var userOrderOrErrors = this.orderService.placeOrder(userName, quantity, type, price)
            println(userOrderOrErrors)
            if (userOrderOrErrors["orderId"] != null) {
                return mapOf(
                    "orderId" to userOrderOrErrors["orderId"],
                    "quantity" to quantity,
                    "type" to type,
                    "price" to price
                )
            }else{
                return userOrderOrErrors
            }

        }

        return userErrors
    }

    @Get(uri = "/{userName}/accountInformation", produces = [MediaType.APPLICATION_JSON])
    fun getAccountInformation(userName: String): HttpResponse<*> {
        val userData = this.userService.accountInformation(userName)
        return HttpResponse.ok(userData)
    }

    @Post(uri = "{userName}/inventory", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun addInventory(userName: String, @Body body: JsonObject): HttpResponse<*>{
        val newInventory = this.userService.adding_inventory(body,userName)
        return HttpResponse.ok(newInventory)
    }

    @Post(uri = "{userName}/wallet", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun addWallet(userName: String, @Body body: JsonObject) :HttpResponse<*> {
        val addedMoney=this.userService.adding_Money(body,userName)
        return HttpResponse.ok(addedMoney)

    }

    @Get(uri = "/{userName}/order", produces = [MediaType.APPLICATION_JSON])
    fun orderHistory(userName: String): HttpResponse<*> {
        val order_history = this.orderService.orderHistory(userName)
        println(order_history)
        return HttpResponse.ok(order_history)
    }
}
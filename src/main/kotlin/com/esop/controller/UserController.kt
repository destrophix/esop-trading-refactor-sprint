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
        if(newUser["error"] != null) {
            return HttpResponse.badRequest(newUser)
        }
        return HttpResponse.ok(newUser)
    }

    @Post(uri="/{userName}/order", consumes = [MediaType.APPLICATION_JSON],produces=[MediaType.APPLICATION_JSON])

    fun order(userName: String, @Body body: JsonObject): Any? {
        var quantity: Long = body.get("quantity").longValue
        var type: String = body.get("type").stringValue
        var price: Long = body.get("price").longValue
        var userErrors = this.userService.orderCheckBeforePlace(userName, quantity, type, price)
        if(userErrors["error"]?.isEmpty()!!){
            var userOrderOrErrors = this.orderService.placeOrder(userName, quantity, type, price)
            if (userOrderOrErrors["orderId"] != null) {
                return HttpResponse.ok(mapOf(
                    "orderId" to userOrderOrErrors["orderId"],
                    "quantity" to quantity,
                    "type" to type,
                    "price" to price
                ))
            }else{
                return HttpResponse.badRequest(userOrderOrErrors)
            }

        }

        return HttpResponse.badRequest(userErrors)
    }

    @Get(uri = "/{userName}/accountInformation", produces = [MediaType.APPLICATION_JSON])
    fun getAccountInformation(userName: String): HttpResponse<*> {
        val userData = this.userService.accountInformation(userName)

        if(userData["error"] != null) {
            return HttpResponse.badRequest(userData)
        }

        return HttpResponse.ok(userData)
    }

    @Post(uri = "{userName}/inventory", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun addInventory(userName: String, @Body body: JsonObject): HttpResponse<*>{
        val newInventory = this.userService.adding_inventory(body,userName)

        if(newInventory["error"] != null) {
            return HttpResponse.badRequest(newInventory)
        }
        return HttpResponse.ok(newInventory)
    }

    @Post(uri = "{userName}/wallet", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun addWallet(userName: String, @Body body: JsonObject) :HttpResponse<*> {
        val addedMoney=this.userService.adding_Money(body,userName)
        if(addedMoney["error"] != null) {
            return HttpResponse.badRequest(addedMoney)
        }
        return HttpResponse.ok(addedMoney)

    }

    @Get(uri = "/{userName}/order", produces = [MediaType.APPLICATION_JSON])
    fun orderHistory(userName: String): HttpResponse<*> {
        val order_history = this.orderService.orderHistory(userName)
        if(order_history is Map<*, *>){
            return HttpResponse.badRequest(order_history)
        }
        return HttpResponse.ok(order_history)
    }
}
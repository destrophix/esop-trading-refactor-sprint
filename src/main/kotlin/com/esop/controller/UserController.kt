package com.esop.controller

import com.esop.dto.AddInventoryDTO
import com.esop.dto.AddWalletDTO
import com.esop.dto.CreateOrderDTO
import com.esop.dto.UserCreationDTO
import com.esop.service.*
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Post
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import javax.validation.Valid
import javax.validation.Validator


@Validated
@Controller("/user")
class UserController {

    @Inject
    lateinit var validator: Validator

    @Inject
    lateinit var orderService: OrderService

    @Inject
    lateinit var userService: UserService

    fun <T> checkValidationError(input: T): List<String> {
        return validator.validate(input).map { it.message }
    }

    @Post(uri="/register", consumes = [MediaType.APPLICATION_JSON],produces=[MediaType.APPLICATION_JSON])
     fun register(@Body @Valid userData: UserCreationDTO): HttpResponse<*> {
        val newUser = this.userService.registerUser(userData)
        if(newUser["error"] != null) {
            return HttpResponse.badRequest(newUser)
        }
        return HttpResponse.ok(newUser)
    }

    @Post(uri="/{userName}/order", consumes = [MediaType.APPLICATION_JSON],produces=[MediaType.APPLICATION_JSON])
    fun order(userName: String, @Body @Valid body: CreateOrderDTO): Any? {
        val quantity: Long = body.quantity!!.toLong()
        val type: String = body.type.toString().lowercase()
        val price: Long = body.price!!.toLong()
        var inventoryType = ""

        if(type == "sell"){
            inventoryType = body.inventoryType.toString().lowercase()
            print(inventoryType)
            if(inventoryType != "performance" && inventoryType != "normal"){
                return HttpResponse.ok("Invalid inventory type")
            }
        }

        val userErrors = this.userService.orderCheckBeforePlace(userName, quantity, type, price, inventoryType)
        if(userErrors["error"]?.isEmpty()!!){
            val userOrderOrErrors = this.orderService.placeOrder(userName, quantity, type, price, inventoryType)
            
            if (userOrderOrErrors["orderId"] != null) {
                return HttpResponse.ok(mapOf(
                    "orderId" to userOrderOrErrors["orderId"],
                    "quantity" to body.quantity,
                    "type" to body.type,
                    "price" to body.price
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
    fun addInventory(userName: String, @Body @Valid body: AddInventoryDTO): HttpResponse<*>{
        val validationErrors = checkValidationError(body)

        if (validationErrors.isNotEmpty()) {
            return HttpResponse.badRequest(mapOf("errors" to validationErrors))
        }


        val newInventory = this.userService.addingInventory(body,userName)

        if(newInventory["error"] != null) {
            return HttpResponse.badRequest(newInventory)
        }
        return HttpResponse.ok(newInventory)
    }


    @Post(uri = "{userName}/wallet", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun addWallet(userName: String, @Body @Valid body: AddWalletDTO) :HttpResponse<*> {
        val validationErrors = checkValidationError(body)

        if (validationErrors.isNotEmpty()) {
            return HttpResponse.badRequest(mapOf("errors" to validationErrors))
        }


        val addedMoney=this.userService.addingMoney(body,userName)

        if(addedMoney["error"] != null) {
            return HttpResponse.badRequest(addedMoney)
        }
        return HttpResponse.ok(addedMoney)

    }

    @Get(uri = "/{userName}/order", produces = [MediaType.APPLICATION_JSON])
    fun orderHistory(userName: String): HttpResponse<*> {
        val orderHistoryData = this.orderService.orderHistory(userName)
        if(orderHistoryData is Map<*, *>){
            return HttpResponse.badRequest(orderHistoryData)
        }
        return HttpResponse.ok(orderHistoryData)
    }
}
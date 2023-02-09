package com.esop.controller

import com.esop.dto.CreateOrderDTO
import com.esop.exceptions.InsufficientFreeESOPsInInventoryException
import com.esop.exceptions.UserNotFoundException
import com.esop.service.OrderService
import com.esop.service.UserService
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import javax.validation.Valid

@Validated
@Controller("/user")
class OrderController {

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var orderService: OrderService

    @Error(exception = InsufficientFreeESOPsInInventoryException::class)
    fun onInsufficientESOPsInInventoryException(exception: InsufficientFreeESOPsInInventoryException): HttpResponse<*> {
        return HttpResponse.badRequest(mapOf("errors" to arrayListOf(exception.message)))
    }

    @Post(uri = "/{userName}/order", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun order(userName: String, @Body @Valid newOrderDetails: CreateOrderDTO): Any? {
        val user = userService.getUserOrNull(userName) ?: throw UserNotFoundException("User not found")

        val order = orderService.placeOrder(newOrderDetails, user)

        return HttpResponse.ok(
            mapOf(
                "orderId" to order.getOrderID(),
                "quantity" to order.getQuantity(),
                "type" to order.getType(),
                "price" to order.getPrice()
            )
        )
    }

    @Get(uri = "/{userName}/orderHistory", produces = [MediaType.APPLICATION_JSON])
    fun orderHistory(userName: String): HttpResponse<*> {
        val orderHistoryData = orderService.orderHistory(userName)
        if (orderHistoryData is Map<*, *>) {
            return HttpResponse.badRequest(orderHistoryData)
        }
        return HttpResponse.ok(orderHistoryData)
    }
}
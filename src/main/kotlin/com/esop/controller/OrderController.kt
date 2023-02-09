package com.esop.controller

import com.esop.dto.CreateOrderDTO
import com.esop.exceptions.UserNotFoundException
import com.esop.schema.Order
import com.esop.service.OrderService
import com.esop.service.UserService
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
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


    @Post(uri = "/{userName}/order", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun order(userName: String, @Body @Valid newOrderDetails: CreateOrderDTO): Any? {
        val user = userService.getUserOrNull(userName) ?: throw UserNotFoundException("User not found")

        val order = Order.from(
            orderDetails = newOrderDetails,
            orderPlacer = user
        )

        val errorList = userService.orderCheckBeforePlace(order)
        if (errorList.size > 0) {
            return HttpResponse.badRequest(mapOf("errors" to errorList))
        }

        orderService.placeOrder(order)

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
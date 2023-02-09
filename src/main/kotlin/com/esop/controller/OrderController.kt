package com.esop.controller

import com.esop.dto.CreateOrderDTO
import com.esop.dto.CreateOrderRequestBody
import com.esop.exceptions.InsufficientFreeESOPsInInventoryException
import com.esop.exceptions.UserNotFoundException
import com.esop.schema.CreateOrderResponse
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
    fun placeOrderRouteHandler(
        userName: String, @Body @Valid requestBody: CreateOrderRequestBody
    ): HttpResponse<CreateOrderResponse> {
        val user = userService.getUserOrNull(userName) ?: throw UserNotFoundException("User not found")

        val newOrderDetails = CreateOrderDTO.from(requestBody, orderPlacer = user)

        val order = orderService.placeOrder(newOrderDetails)

        return HttpResponse.ok(
            CreateOrderResponse(
                orderId = order.getOrderID(),
                quantity = order.getQuantity(),
                type = order.getType(),
                price = order.getPrice()
            )
        )
    }

    @Get(uri = "/{userName}/orderHistory", produces = [MediaType.APPLICATION_JSON])
    fun fetchOrderHistoryRouteHandler(userName: String): HttpResponse<*> {
        val orderHistoryData = orderService.orderHistory(userName)
        if (orderHistoryData is Map<*, *>) {
            return HttpResponse.badRequest(orderHistoryData)
        }
        return HttpResponse.ok(orderHistoryData)
    }
}
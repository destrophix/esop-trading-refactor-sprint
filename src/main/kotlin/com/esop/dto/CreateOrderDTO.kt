package com.esop.dto

import com.esop.schema.User

data class CreateOrderDTO (
    val price: Long,
    val quantity: Long,
    val type: String,
    val orderPlacer: User,
    val esopType: String = "NON_PERFORMANCE",
) {

    companion object {
        fun from(orderDetails: CreateOrderRequestBody, orderPlacer: User): CreateOrderDTO {
            return CreateOrderDTO(
                price = orderDetails.price!!,
                quantity = orderDetails.quantity!!,
                type = orderDetails.type!!,
                esopType = orderDetails.esopType!!,
                orderPlacer = orderPlacer
            )
        }
    }

    fun getTotalAmount() = price * quantity
}
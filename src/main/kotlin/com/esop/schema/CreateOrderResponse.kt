package com.esop.schema

data class CreateOrderResponse(val orderId: Long, val quantity: Long, val price: Long, val type: String)

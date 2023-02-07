package com.esop.schema

class Order(
    private var quantity: Long,
    private var type: String,
    private var price: Long,
    private var userName: String
) {
    var timeStamp = System.currentTimeMillis()
    var remainingQuantity: Long = 0
    var orderStatus: String = "PENDING" // COMPLETED, PARTIAL, PENDING
    var orderFilledLogs: MutableList<OrderFilledLog> = mutableListOf()
    var orderID: Long = -1
    var esopType = "NON_PERFORMANCE"
    var inventoryPriority = 2

    fun getQuantity(): Long {
        return quantity
    }

    fun getPrice(): Long {
        return price
    }

    fun getType(): String {
        return type
    }

    fun getUserName(): String {
        return userName
    }
}
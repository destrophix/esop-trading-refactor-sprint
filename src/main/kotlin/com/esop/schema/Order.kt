package com.esop.schema

class Order(
    var quantity: Long,
    var type: String,
    var price: Long,
    var userName: String
) {
    var timeStamp = System.currentTimeMillis()
    var remainingQuantity: Long = 0
    var orderStatus: String = "PENDING" // COMPLETED, PARTIAL, PENDING
    var orderFilledLogs: MutableList<OrderFilledLog> = mutableListOf()
    var orderID: Long = -1
    var esopType = "NON_PERFORMANCE"
    var inventoryPriority = 2

}
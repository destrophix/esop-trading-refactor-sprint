package com.esop.schema

class Order(
    var quantity: Long,
    var type: String,
    var price: Long,
    var userName: String
)
{
    var timeStamp = System.currentTimeMillis()
    var currentQuantity: Long = 0
    var remainingQuantity: Long = 0
    var orderStatus: String = "PENDING" // COMPLETED, PARTIAL, PENDING
    var orderFilledLogs: MutableList<OrderFilledLog> = mutableListOf()
    var orderID: Long = -1
    var inventoryType = "NON_PERFORMANCE"
    var inventoryPriority = 2

    fun orderAvailable():Boolean{
        return orderStatus != "COMPLETED"
    }

}
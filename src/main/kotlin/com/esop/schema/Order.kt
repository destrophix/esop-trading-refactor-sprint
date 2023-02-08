package com.esop.schema

import com.esop.schema.InventoryPriority.*

enum class InventoryPriority(val priority: Int) {
    NONE(0),
    PERFORMANCE(1),
    NON_PERFORMANCE(2)
}

class Order(
    private var quantity: Long,
    private var type: String,
    private var price: Long,
    private var userName: String
) {
    var timeStamp = System.currentTimeMillis()
    var orderStatus: String = "PENDING" // COMPLETED, PARTIAL, PENDING
    var orderFilledLogs: MutableList<OrderFilledLog> = mutableListOf()
    var orderID: Long = -1
    var esopType = "NON_PERFORMANCE"
    var inventoryPriority = NONE
    var remainingQuantity = quantity

    init {
        if (isTypeSellAndEsopTypePerformance()) {
            inventoryPriority = PERFORMANCE
        } else if (isTypeSellAndEsopTypeNonPerformance()) {
            inventoryPriority = NON_PERFORMANCE
        }
    }

    private fun isTypeSellAndEsopTypePerformance() = type == "SELL" && esopType == "PERFORMANCE"

    private fun isTypeSellAndEsopTypeNonPerformance() = type == "SELL" && esopType == "NON_PERFORMANCE"
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


    fun subtractFromRemainingQuantity(quantityToBeUpdated: Long) {
        remainingQuantity -= quantityToBeUpdated
    }

    fun updateStatus() {
        if (remainingQuantity == 0L) {
            orderStatus = "COMPLETED"
        } else if (remainingQuantity != quantity) {
            orderStatus = "PARTIAL"
        }
    }

    fun addOrderFilledLogs(orderFilledLog: OrderFilledLog) {
        orderFilledLogs.add(orderFilledLog)
    }
}
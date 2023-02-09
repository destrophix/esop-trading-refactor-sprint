package com.esop.schema

import com.esop.dto.CreateOrderDTO
import com.esop.schema.InventoryPriority.*
import com.esop.utils.Counter

enum class InventoryPriority(val priority: Int) {
    NONE(0),
    PERFORMANCE(1),
    NON_PERFORMANCE(2)
}

class Order(
    private var quantity: Long,
    private var type: String,
    private var price: Long,
    private val orderPlacer: User,
    private var esopType: String = "NON_PERFORMANCE",
) : Comparable<Order> {
    private val timeStamp = System.currentTimeMillis()
    private var orderFilledLogs: MutableList<OrderFilledLog> = mutableListOf()
    private var orderID: Long = -1
    private var inventoryPriority = NONE
    private var remainingQuantity = quantity

    companion object {
        fun  from(orderDetails: CreateOrderDTO, orderPlacer: User): Order {
            return Order(
                quantity = orderDetails.quantity!!,
                type = orderDetails.type!!,
                price = orderDetails.price!!,
                orderPlacer = orderPlacer,
                esopType = orderDetails.esopType!!
            )
        }
    }

    init {
        orderID = Counter.next()

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


    fun getOrderID(): Long {
        return orderID
    }

    fun getOrderPlacer(): User {
        return orderPlacer
    }

    fun subtractFromRemainingQuantity(quantityToBeUpdated: Long) {
        remainingQuantity -= quantityToBeUpdated
    }

    fun addOrderFilledLogs(orderFilledLog: OrderFilledLog) {
        orderFilledLogs.add(orderFilledLog)
    }

    override fun compareTo(other: Order): Int {
        if (type == "BUY") {
            return compareByDescending<Order> { it.getPrice() }.thenBy { it.timeStamp }.compare(this, other)
        }

        if (inventoryPriority != other.inventoryPriority)
            return inventoryPriority.priority - other.inventoryPriority.priority

        if (inventoryPriority.priority == 1) {
            if (timeStamp < other.timeStamp)
                return -1
            return 1
        }

        if (getPrice() == other.getPrice()) {
            if (timeStamp < other.timeStamp)
                return -1
            return 1
        }

        if (getPrice() < other.getPrice())
            return -1
        return 1
    }

    fun isCompleted(): Boolean {
        return getOrderStatus() == "COMPLETED"
    }

    fun getRemainingQuantity(): Long {
        return remainingQuantity
    }

    fun getESOPType(): String {
        return esopType
    }

    fun getOrderFilledLogs(): MutableList<OrderFilledLog> {
        return orderFilledLogs
    }

    fun getOrderStatus(): String {
        return when (remainingQuantity) {
            0L -> "COMPLETED"
            quantity -> "PENDING"
            else -> "PARTIAL"
        }
    }
}
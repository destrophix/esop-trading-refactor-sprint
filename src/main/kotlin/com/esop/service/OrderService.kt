package com.esop.service


import com.esop.InventoryLimitExceededException
import com.esop.WalletLimitExceededException
import com.esop.constant.MAX_INVENTORY_CAPACITY
import com.esop.constant.MAX_WALLET_CAPACITY
import com.esop.constant.errors
import com.esop.dto.CreateOrderDTO
import com.esop.exceptions.InsufficientFreeAmountInWalletException
import com.esop.exceptions.InsufficientFreeESOPsInInventoryException
import com.esop.repository.UserRecords
import com.esop.schema.History
import com.esop.schema.Order
import com.esop.schema.User
import jakarta.inject.Singleton


@Singleton
class OrderService(private val userRecords: UserRecords, private val orderExecutionPool: OrderExecutionPool) {


    fun placeOrder(orderDetails: CreateOrderDTO): Order {
        val order = createOrder(orderDetails)
        orderExecutionPool.add(order)

        return order
    }

    private fun createOrder(orderDetails: CreateOrderDTO): Order {
        val orderPlacer = orderDetails.orderPlacer

        checkOrderPlacementPossible(orderDetails)
        lockResources(orderDetails)

        val order = Order.from(orderDetails)
        orderPlacer.addOrder(order)

        return order
    }

    private fun lockResources(orderDetails: CreateOrderDTO) {
        val orderPlacer = orderDetails.orderPlacer

        when (orderDetails.type) {
            "BUY" -> orderPlacer.lockAmount(orderDetails.getTotalAmount())
            "SELL" -> orderPlacer.lockESOPs(orderDetails.esopType, orderDetails.quantity)
        }
    }

    private fun checkOrderPlacementPossible(orderDetails: CreateOrderDTO) {
        when (orderDetails.type) {
            "BUY" -> checkBuyOrderPlacementPossible(orderDetails)
            "SELL" -> checkSellOrderPlacementPossible(orderDetails)
        }
    }

    private fun checkSellOrderPlacementPossible(orderDetails: CreateOrderDTO) {
        checkEnoughFreeESOPsInInventory(orderDetails)
        checkWalletWillNotExceedMaxLimitOnOrderCompletion(
            orderDetails.getTotalAmount(),
            orderPlacer = orderDetails.orderPlacer
        )
    }

    private fun checkEnoughFreeESOPsInInventory(orderDetails: CreateOrderDTO) {
        val orderPlacer = orderDetails.orderPlacer
        if (orderPlacer.getFreeESOPsInInventory(orderDetails.esopType) < orderDetails.quantity)
            throw InsufficientFreeESOPsInInventoryException(
                "Insufficient ${if (orderDetails.esopType == "PERFORMANCE") "PERFORMANCE" else ""} ESOPs in Inventory"
            )
    }

    private fun checkBuyOrderPlacementPossible(orderDetails: CreateOrderDTO) {
        checkEnoughFreeAmountInWallet(orderPlacer = orderDetails.orderPlacer, amount = orderDetails.getTotalAmount())
        checkInventoryWillNotExceedMaxLimitOnOrderCompletion(orderDetails.quantity, orderDetails.orderPlacer)
    }

    private fun checkEnoughFreeAmountInWallet(amount: Long, orderPlacer: User) {
        if (orderPlacer.getFreeAmountInWallet() < amount)
            throw InsufficientFreeAmountInWalletException("Insufficient funds")
    }

    private fun checkInventoryWillNotExceedMaxLimitOnOrderCompletion(quantity: Long, orderPlacer: User) {
        if (orderPlacer.getTotalESOP() + quantity > MAX_INVENTORY_CAPACITY)
            throw InventoryLimitExceededException()
    }

    private fun checkWalletWillNotExceedMaxLimitOnOrderCompletion(amount: Long, orderPlacer: User) {
        if (orderPlacer.getTotalAmount() + amount > MAX_WALLET_CAPACITY)
            throw WalletLimitExceededException()
    }


    fun orderHistory(userName: String): Any {
        val userErrors = ArrayList<String>()
        if (!userRecords.checkIfUserExists(userName)) {
            errors["USER_DOES_NOT_EXISTS"]?.let { userErrors.add(it) }
            return mapOf("error" to userErrors)
        }
        val orderDetails = userRecords.getUser(userName)!!.getAllOrders()
        val orderHistory = ArrayList<History>()

        for (orders in orderDetails) {
            orderHistory.add(
                History(
                    orders.getOrderID(),
                    orders.getQuantity(),
                    orders.getType(),
                    orders.getPrice(),
                    orders.getESOPType(),
                    orders.getOrderFilledLogs()
                )
            )
        }
        return orderHistory
    }


}
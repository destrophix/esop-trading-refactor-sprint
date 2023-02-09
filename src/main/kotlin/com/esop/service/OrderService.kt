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
import jakarta.inject.Inject
import jakarta.inject.Singleton


@Singleton
class OrderService(private val userRecords: UserRecords, private val orderExecutionPool: OrderExecutionPool) {


    fun placeOrder(orderDetails: CreateOrderDTO, orderPlacer: User): Order {
        val order = createOrder(orderDetails, orderPlacer)
        orderExecutionPool.add(order)

        return order
    }

    private fun createOrder(orderDetails: CreateOrderDTO, orderPlacer: User): Order {
        checkOrderPlacementPossible(orderDetails, orderPlacer)

        lockResources(orderDetails, orderPlacer)

        val order = Order.from(orderDetails, orderPlacer)
        orderPlacer.addOrder(order)

        return order
    }

    private fun lockResources(orderDetails: CreateOrderDTO, orderPlacer: User) {
        when (orderDetails.type) {
            "BUY" -> orderPlacer.lockAmount(orderDetails.quantity!! * orderDetails.price!!)
            "SELL" -> orderPlacer.lockESOPs(orderDetails.esopType!!, orderDetails.quantity!!)
        }
    }

    private fun checkOrderPlacementPossible(orderDetails: CreateOrderDTO, orderPlacer: User) {
        when (orderDetails.type) {
            "BUY" -> checkBuyOrderPlacementPossible(orderDetails, orderPlacer)
            "SELL" -> checkSellOrderPlacementPossible(orderDetails, orderPlacer)
        }
    }

    private fun checkSellOrderPlacementPossible(orderDetails: CreateOrderDTO, orderPlacer: User) {
        checkEnoughFreeESOPsInInventory(orderDetails, orderPlacer)
        checkWalletWillNotExceedMaxLimitOnOrderCompletion(
            orderDetails.quantity!! * orderDetails.price!!,
            orderPlacer
        )
    }

    private fun checkEnoughFreeESOPsInInventory(orderDetails: CreateOrderDTO, orderPlacer: User) {
        if (orderPlacer.getFreeESOPsInInventory(orderDetails.esopType!!) < orderDetails.quantity!!)
            throw InsufficientFreeESOPsInInventoryException(
                "Insufficient ${if (orderDetails.esopType == "PERFORMANCE") "PERFORMANCE" else ""} ESOPs in Inventory"
            )
    }

    private fun checkBuyOrderPlacementPossible(orderDetails: CreateOrderDTO, orderPlacer: User) {
        checkEnoughFreeAmountInWallet(orderDetails.quantity!! * orderDetails.price!!, orderPlacer)
        checkInventoryWillNotExceedMaxLimitOnOrderCompletion(orderDetails.quantity!!, orderPlacer)
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
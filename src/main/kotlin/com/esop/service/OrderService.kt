package com.esop.service


import com.esop.constant.errors
import com.esop.repository.UserRecords
import com.esop.schema.*
import com.esop.schema.PlatformFee.Companion.addPlatformFee
import jakarta.inject.Singleton
import kotlin.math.min
import kotlin.math.round

private const val TWO_PERCENT = 0.02

@Singleton
class OrderService(private val userRecords: UserRecords) {
    companion object {
        private var orderId = 1L

        var buyOrders = mutableListOf<Order>()
        var sellOrders = mutableListOf<Order>()
    }

    @Synchronized
    fun generateOrderId(): Long {
        return orderId++
    }

    private fun updateOrderDetails(
        currentTradeQuantity: Long,
        sellerOrder: Order,
        buyerOrder: Order
    ) {
        // Deduct money of quantity taken from buyer
        val sellAmount = sellerOrder.getPrice() * (currentTradeQuantity)
        val buyer = userRecords.getUser(buyerOrder.getUserName())!!
        val seller = userRecords.getUser(sellerOrder.getUserName())!!
        var platformFee = 0L


        if (sellerOrder.esopType == "NON_PERFORMANCE")
            platformFee = round(sellAmount * TWO_PERCENT).toLong()

        updateWalletBalances(sellAmount, platformFee, buyer, seller)


        seller.transferLockedESOPsTo(buyer, EsopTransferRequest(sellerOrder.esopType, currentTradeQuantity))

        val amountToBeReleased = (buyerOrder.getPrice() - sellerOrder.getPrice()) * (currentTradeQuantity)
        buyer.userWallet.moveMoneyFromLockedToFree(amountToBeReleased)

    }

    private fun updateWalletBalances(
        sellAmount: Long,
        platformFee: Long,
        buyer: User,
        seller: User
    ) {
        val adjustedSellAmount = sellAmount - platformFee
        addPlatformFee(platformFee)

        buyer.userWallet.removeMoneyFromLockedState(sellAmount)
        seller.userWallet.addMoneyToWallet(adjustedSellAmount)
    }


    private fun sortAscending(): List<Order> {
        return sellOrders.sortedWith(object : Comparator<Order> {
            override fun compare(o1: Order, o2: Order): Int {

                if (o1.inventoryPriority != o2.inventoryPriority)
                    return o1.inventoryPriority.priority - o2.inventoryPriority.priority

                if (o1.inventoryPriority.priority == 1) {
                    if (o1.timeStamp < o2.timeStamp)
                        return -1
                    return 1
                }

                if (o1.getPrice() == o2.getPrice()) {
                    if (o1.timeStamp < o2.timeStamp)
                        return -1
                    return 1
                }
                if (o1.getPrice() < o2.getPrice())
                    return -1
                return 1
            }
        })
    }

    fun placeOrder(order: Order): Map<String, Any> {
        order.orderID = generateOrderId()

        if (order.getType() == "BUY") {
            executeBuyOrder(order)
        } else {
            executeSellOrder(order)
        }
        userRecords.getUser(order.getUserName())?.orderList?.add(order)
        return mapOf("orderId" to order.orderID)
    }

    private fun executeBuyOrder(buyOrder: Order) {
        buyOrders.add(buyOrder)
        val sortedSellOrders = sortAscending()

        for (sellOrder in sortedSellOrders) {
            if ((buyOrder.getPrice() >= sellOrder.getPrice()) && (sellOrder.remainingQuantity > 0)) {
                performOrderMatching(sellOrder, buyOrder)
            }
        }
    }

    private fun executeSellOrder(sellOrder: Order) {
        sellOrders.add(sellOrder)
        val sortedBuyOrders =
            buyOrders.sortedWith(compareByDescending<Order> { it.getPrice() }.thenBy { it.timeStamp })

        for (buyOrder in sortedBuyOrders) {
            if ((sellOrder.getPrice() <= buyOrder.getPrice()) && (buyOrder.remainingQuantity > 0)) {
                performOrderMatching(sellOrder, buyOrder)
            }
        }
    }

    private fun performOrderMatching(sellOrder: Order, buyOrder: Order) {
        val orderExecutionPrice = sellOrder.getPrice()
        val orderExecutionQuantity = min(sellOrder.remainingQuantity, buyOrder.remainingQuantity)

        buyOrder.subtractFromRemainingQuantity(orderExecutionQuantity)
        sellOrder.subtractFromRemainingQuantity(orderExecutionQuantity)

        buyOrder.updateStatus()
        sellOrder.updateStatus()

        createOrderFilledLogs(orderExecutionQuantity, orderExecutionPrice, sellOrder, buyOrder)

        updateOrderDetails(
            orderExecutionQuantity,
            sellOrder,
            buyOrder
        )

        if (buyOrder.orderStatus == "COMPLETED") {
            buyOrders.remove(buyOrder)
        }
        if (sellOrder.orderStatus == "COMPLETED") {
            sellOrders.remove(sellOrder)
        }
    }

    private fun createOrderFilledLogs(
        orderExecutionQuantity: Long,
        orderExecutionPrice: Long,
        sellOrder: Order,
        buyOrder: Order
    ) {
        val buyOrderLog = OrderFilledLog(
            orderExecutionQuantity,
            orderExecutionPrice,
            null,
            sellOrder.getUserName(),
            null
        )
        val sellOrderLog = OrderFilledLog(
            orderExecutionQuantity,
            orderExecutionPrice,
            sellOrder.esopType,
            null,
            buyOrder.getUserName()
        )

        buyOrder.addOrderFilledLogs(buyOrderLog)
        sellOrder.addOrderFilledLogs(sellOrderLog)
    }

    fun orderHistory(userName: String): Any {
        val userErrors = ArrayList<String>()
        if (!userRecords.checkIfUserExists(userName)) {
            errors["USER_DOES_NOT_EXISTS"]?.let { userErrors.add(it) }
            return mapOf("error" to userErrors)
        }
        val orderDetails = userRecords.getUser(userName)!!.orderList
        val orderHistory = ArrayList<History>()

        for (orders in orderDetails) {
            orderHistory.add(
                History(
                    orders.orderID,
                    orders.getQuantity(),
                    orders.getType(),
                    orders.getPrice(),
                    orders.orderStatus,
                    orders.orderFilledLogs
                )
            )
        }
        return orderHistory
    }
}
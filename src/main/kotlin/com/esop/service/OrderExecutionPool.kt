package com.esop.service

import com.esop.schema.*
import jakarta.inject.Singleton
import kotlin.math.min
import kotlin.math.round

@Singleton
class OrderExecutionPool {
    private val buyOrders = mutableListOf<Order>()
    private val sellOrders = mutableListOf<Order>()

    fun add(order: Order) {
        addToOrdersPool(order)
        executeTransactionIfMatchFound(order)
    }

    private fun addToOrdersPool(order: Order) {
        when (order.getType()) {
            "BUY" -> addBuyOrder(order)
            "SELL" -> addSellOrder(order)
        }
    }

    private fun executeTransactionIfMatchFound(order: Order) {
        findOrderMatch(order)?.let {
            executeTransaction(transaction = it)
        }
    }

    private fun findOrderMatch(order: Order): Transaction? {
        return when (order.getType()) {
            "SELL" -> findSellOrderMatch(sellOrder = order)
            else -> findBuyOrderMatch(buyOrder = order)
        }
    }

    private fun addBuyOrder(order: Order) {
        buyOrders.add(order)
    }

    private fun addSellOrder(order: Order) {
        sellOrders.add(order)
    }

    private fun findBuyOrderMatch(buyOrder: Order): Transaction? =
        sellOrders.filter { sellOrder -> isOrderMatch(sellOrder, buyOrder) }.minOrNull()
            ?.let { sellOrder -> Transaction(sellOrder = sellOrder, buyOrder = buyOrder) }


    private fun findSellOrderMatch(sellOrder: Order): Transaction? =
        buyOrders.filter { buyOrder -> isOrderMatch(sellOrder, buyOrder) }.minOrNull()
            ?.let { buyOrder -> Transaction(sellOrder = sellOrder, buyOrder = buyOrder) }


    private fun isOrderMatch(sellOrder: Order, buyOrder: Order): Boolean =
        sellOrder.getPrice() <= buyOrder.getPrice() && sellOrder.getRemainingQuantity() > 0
                && buyOrder.getRemainingQuantity() > 0


    private fun executeTransaction(transaction: Transaction) {
        val sellOrder = transaction.sellOrder
        val buyOrder = transaction.buyOrder

        val orderExecutionPrice = sellOrder.getPrice()
        val orderExecutionQuantity = min(sellOrder.getRemainingQuantity(), buyOrder.getRemainingQuantity())

        buyOrder.subtractFromRemainingQuantity(orderExecutionQuantity)
        sellOrder.subtractFromRemainingQuantity(orderExecutionQuantity)

        appendTransactionLogs(orderExecutionQuantity, orderExecutionPrice, sellOrder, buyOrder)

        updateOrderDetails(
            orderExecutionQuantity,
            sellOrder,
            buyOrder
        )

        removeFromPoolIfOrderCompleted(buyOrder)

        removeFromPoolIfOrderCompleted(sellOrder)

        if (!buyOrder.isCompleted()) {
            executeTransactionIfMatchFound(buyOrder)
        }

        if (!sellOrder.isCompleted()) {
            executeTransactionIfMatchFound(sellOrder)
        }
    }

    private fun removeFromPoolIfOrderCompleted(order: Order) {
        if (!order.isCompleted()) return
        when (order.getType()) {
            "BUY" -> buyOrders.remove(order)
            "SELL" -> sellOrders.remove(order)
        }
    }

    private fun updateWalletBalances(
        sellAmount: Long,
        platformFee: Long,
        buyer: User,
        seller: User
    ) {
        val adjustedSellAmount = sellAmount - platformFee
        PlatformFee.addPlatformFee(platformFee)

        buyer.removeMoneyFromLockedState(sellAmount)
        seller.addMoneyToWallet(adjustedSellAmount)
    }

    private fun appendTransactionLogs(
        orderExecutionQuantity: Long,
        orderExecutionPrice: Long,
        sellOrder: Order,
        buyOrder: Order
    ) {
        val buyOrderLog = OrderFilledLog(
            orderExecutionQuantity,
            orderExecutionPrice,
            null,
            sellOrder.getOrderPlacer().username,
            null
        )
        val sellOrderLog = OrderFilledLog(
            orderExecutionQuantity,
            orderExecutionPrice,
            sellOrder.getESOPType(),
            null,
            buyOrder.getOrderPlacer().username
        )

        buyOrder.addOrderFilledLogs(buyOrderLog)
        sellOrder.addOrderFilledLogs(sellOrderLog)
    }

    private fun updateOrderDetails(
        currentTradeQuantity: Long,
        sellerOrder: Order,
        buyerOrder: Order
    ) {
        val sellAmount = sellerOrder.getPrice() * (currentTradeQuantity)
        val buyer = buyerOrder.getOrderPlacer()
        val seller = sellerOrder.getOrderPlacer()
        var platformFee = 0L

        if (sellerOrder.getESOPType() == "NON_PERFORMANCE")
            platformFee = round(sellAmount * 0.02).toLong()

        updateWalletBalances(sellAmount, platformFee, buyer, seller)

        seller.transferLockedESOPsTo(buyer, EsopTransferRequest(sellerOrder.getESOPType(), currentTradeQuantity))

        val amountToBeReleased = (buyerOrder.getPrice() - sellerOrder.getPrice()) * (currentTradeQuantity)
        buyer.moveMoneyFromLockedToFree(amountToBeReleased)
    }
}
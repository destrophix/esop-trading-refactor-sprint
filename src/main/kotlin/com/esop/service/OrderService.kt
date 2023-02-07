package com.esop.service


import com.esop.constant.errors
import com.esop.repository.UserRecords
import com.esop.schema.History
import com.esop.schema.Order
import com.esop.schema.OrderFilledLog
import com.esop.schema.PlatformFee.Companion.addPlatformFee
import jakarta.inject.Singleton
import kotlin.math.round

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

    private fun updateOrderDetailsForBuy(
        userName: String,
        prevQuantity: Long,
        remainingQuantity: Long,
        sellerOrder: Order,
        buyerOrder: Order
    ) {
        // Deduct money of quantity taken from buyer
        val amountToBeDeductedFromLockedState = sellerOrder.getPrice() * (prevQuantity - remainingQuantity)
        userRecords.getUser(userName)!!.userWallet.removeMoneyFromLockedState(amountToBeDeductedFromLockedState)

        // Add money of quantity taken from seller
        var amountToBeAddedToSellersAccount = amountToBeDeductedFromLockedState
        if (sellerOrder.esopType == "NON_PERFORMANCE") {
            amountToBeAddedToSellersAccount -= round(amountToBeDeductedFromLockedState * 0.02).toLong()
            addPlatformFee(round(amountToBeDeductedFromLockedState * 0.02).toLong())
        }
        userRecords.getUser(sellerOrder.getUserName())!!.userWallet.addMoneyToWallet(amountToBeAddedToSellersAccount)

        // Deduct inventory of stock from sellers inventory based on its type
        if (sellerOrder.esopType == "PERFORMANCE") {
            userRecords.getUser(sellerOrder.getUserName())!!.userPerformanceInventory.removeESOPsFromLockedState(
                prevQuantity - remainingQuantity
            )
        }

        if (sellerOrder.esopType == "NON_PERFORMANCE") {
            userRecords.getUser(sellerOrder.getUserName())!!.userNonPerfInventory.removeESOPsFromLockedState(
                prevQuantity - remainingQuantity
            )
        }

        // Add purchased inventory to buyer
        userRecords.getUser(userName)!!.userNonPerfInventory.addESOPsToInventory(prevQuantity - remainingQuantity)

        // Add buyers money back to free from locked
        userRecords.getUser(userName)!!.userWallet.addMoneyToWallet((buyerOrder.getPrice() - sellerOrder.getPrice()) * (prevQuantity - remainingQuantity))
        userRecords.getUser(userName)!!.userWallet.removeMoneyFromLockedState((buyerOrder.getPrice() - sellerOrder.getPrice()) * (prevQuantity - remainingQuantity))
    }

    private fun updateOrderDetailsForSell(
        userName: String,
        prevQuantity: Long,
        remainingQuantity: Long,
        buyerOrder: Order,
        sellerOrder: Order
    ) {

        // Deduct inventory of stock from sellers inventory based on its type
        if (sellerOrder.esopType == "PERFORMANCE") {
            userRecords.getUser(userName)!!.userPerformanceInventory.removeESOPsFromLockedState(prevQuantity - remainingQuantity)
        }

        if (sellerOrder.esopType == "NON_PERFORMANCE") {
            userRecords.getUser(userName)!!.userNonPerfInventory.removeESOPsFromLockedState(prevQuantity - remainingQuantity)
        }

        // Add inventory to buyers stock
        userRecords.getUser(buyerOrder.getUserName())!!.userNonPerfInventory.addESOPsToInventory(prevQuantity - remainingQuantity)

        // Deduct money from buyers wallet
        userRecords.getUser(buyerOrder.getUserName())!!.userWallet.removeMoneyFromLockedState((sellerOrder.getPrice() * (prevQuantity - remainingQuantity)))

        // Add money to sellers wallet
        var totOrderPrice = sellerOrder.getPrice() * (prevQuantity - remainingQuantity)
        if (sellerOrder.esopType == "NON_PERFORMANCE") {
            totOrderPrice -= round(totOrderPrice * 0.02).toLong()
            addPlatformFee(round(totOrderPrice * 0.02).toLong())
        }
        userRecords.getUser(userName)!!.userWallet.addMoneyToWallet(totOrderPrice)

        // Add buyers luck back to free from locked
        userRecords.getUser(buyerOrder.getUserName())!!.userWallet.addMoneyToWallet((buyerOrder.getPrice() - sellerOrder.getPrice()) * (prevQuantity - remainingQuantity))
        userRecords.getUser(buyerOrder.getUserName())!!.userWallet.removeMoneyFromLockedState((buyerOrder.getPrice() - sellerOrder.getPrice()) * (prevQuantity - remainingQuantity))
    }

    private fun sortAscending(): List<Order> {
        return sellOrders.sortedWith(object : Comparator<Order> {
            override fun compare(o1: Order, o2: Order): Int {

                if (o1.inventoryPriority != o2.inventoryPriority)
                    return o1.inventoryPriority - o2.inventoryPriority

                if (o1.inventoryPriority == 1) {
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
        var inventoryPriority = 2
        if (order.esopType == "PERFORMANCE") {
            inventoryPriority -= 1
        }
        order.orderID = generateOrderId()
        order.inventoryPriority = inventoryPriority
        order.remainingQuantity = order.getQuantity()

        if (order.getType() == "BUY") {
            buyOrders.add(order)
            val sortedSellOrders = sortAscending()


            for (bestSellOrder in sortedSellOrders) {
                if (order.remainingQuantity == 0L) {
                    break
                }
                if ((order.getPrice() >= bestSellOrder.getPrice()) && (bestSellOrder.remainingQuantity > 0)) {
                    val prevQuantity = order.remainingQuantity
                    if (order.remainingQuantity < bestSellOrder.remainingQuantity) {

                        val buyOrderLog = OrderFilledLog(
                            order.remainingQuantity,
                            bestSellOrder.getPrice(),
                            null,
                            bestSellOrder.getUserName(),
                            null
                        )
                        val sellOrderLog = OrderFilledLog(
                            order.remainingQuantity,
                            bestSellOrder.getPrice(),
                            bestSellOrder.esopType,
                            null,
                            order.getUserName()
                        )

                        bestSellOrder.remainingQuantity = bestSellOrder.remainingQuantity - order.remainingQuantity
                        bestSellOrder.orderStatus = "PARTIAL"
                        bestSellOrder.orderFilledLogs.add(sellOrderLog)

                        order.remainingQuantity = 0
                        order.orderStatus = "COMPLETED"
                        order.orderFilledLogs.add(buyOrderLog)
                        buyOrders.remove(order)

                        updateOrderDetailsForBuy(
                            order.getUserName(),
                            prevQuantity,
                            order.remainingQuantity,
                            bestSellOrder,
                            order
                        )

                    } else if (order.remainingQuantity > bestSellOrder.remainingQuantity) {

                        val buyOrderLog = OrderFilledLog(
                            bestSellOrder.remainingQuantity,
                            order.getPrice(),
                            null,
                            bestSellOrder.getUserName(),
                            null
                        )
                        val sellOrderLog = OrderFilledLog(
                            bestSellOrder.remainingQuantity,
                            order.getPrice(),
                            bestSellOrder.esopType,
                            null
                        )

                        order.remainingQuantity = order.remainingQuantity - bestSellOrder.remainingQuantity
                        order.orderStatus = "PARTIAL"
                        order.orderFilledLogs.add(sellOrderLog)

                        bestSellOrder.remainingQuantity = 0
                        bestSellOrder.orderStatus = "COMPLETED"
                        bestSellOrder.orderFilledLogs.add(buyOrderLog)
                        sellOrders.remove(bestSellOrder)

                        updateOrderDetailsForBuy(
                            order.getUserName(),
                            prevQuantity,
                            order.remainingQuantity,
                            bestSellOrder,
                            order
                        )
                    } else {
                        val buyOrderLog = OrderFilledLog(
                            bestSellOrder.remainingQuantity,
                            bestSellOrder.getPrice(),
                            null,
                            bestSellOrder.getUserName()
                        )
                        val sellOrderLog = OrderFilledLog(
                            order.remainingQuantity,
                            bestSellOrder.getPrice(),
                            bestSellOrder.esopType,
                            order.getUserName(),
                            null
                        )

                        bestSellOrder.remainingQuantity = 0
                        bestSellOrder.orderStatus = "COMPLETED"
                        bestSellOrder.orderFilledLogs.add(buyOrderLog)
                        sellOrders.remove(bestSellOrder)

                        order.remainingQuantity = 0
                        order.orderStatus = "COMPLETED"
                        order.orderFilledLogs.add(sellOrderLog)
                        buyOrders.remove(order)

                        updateOrderDetailsForBuy(
                            order.getUserName(),
                            prevQuantity,
                            order.remainingQuantity,
                            bestSellOrder,
                            order
                        )

                    }

                }
            }
        } else {
            sellOrders.add(order)
            val sortedBuyOrders =
                buyOrders.sortedWith(compareByDescending<Order> { it.getPrice() }.thenBy { it.timeStamp })

            for (bestBuyOrder in sortedBuyOrders) {
                if (order.remainingQuantity == 0L) {
                    break
                }
                if ((order.getPrice() <= bestBuyOrder.getPrice()) && (bestBuyOrder.remainingQuantity > 0)) {
                    val prevQuantity = order.remainingQuantity
                    if (order.remainingQuantity < bestBuyOrder.remainingQuantity) {

                        val buyOrderLog =
                            OrderFilledLog(
                                order.remainingQuantity,
                                bestBuyOrder.getPrice(),
                                null,
                                order.getUserName(),
                                null
                            )
                        val sellOrderLog = OrderFilledLog(
                            order.remainingQuantity,
                            bestBuyOrder.getPrice(),
                            order.esopType,
                            null,
                            bestBuyOrder.getUserName()
                        )

                        bestBuyOrder.remainingQuantity = bestBuyOrder.remainingQuantity - order.remainingQuantity
                        bestBuyOrder.orderStatus = "PARTIAL"
                        bestBuyOrder.orderFilledLogs.add(buyOrderLog)

                        order.remainingQuantity = 0
                        order.orderStatus = "COMPLETED"
                        order.orderFilledLogs.add(sellOrderLog)
                        sellOrders.remove(order)

                        updateOrderDetailsForSell(
                            order.getUserName(),
                            prevQuantity,
                            order.remainingQuantity,
                            bestBuyOrder,
                            order
                        )

                    } else if (order.remainingQuantity > bestBuyOrder.remainingQuantity) {

                        val buyOrderLog =
                            OrderFilledLog(
                                bestBuyOrder.remainingQuantity,
                                order.getPrice(),
                                null,
                                order.getUserName(),
                                null
                            )
                        val sellOrderLog = OrderFilledLog(
                            bestBuyOrder.remainingQuantity,
                            order.getPrice(),
                            order.esopType,
                            null,
                            bestBuyOrder.getUserName()
                        )


                        order.remainingQuantity = order.remainingQuantity - bestBuyOrder.remainingQuantity
                        order.orderStatus = "PARTIAL"
                        order.orderFilledLogs.add(sellOrderLog)

                        bestBuyOrder.remainingQuantity = 0
                        bestBuyOrder.orderStatus = "COMPLETED"
                        bestBuyOrder.orderFilledLogs.add(buyOrderLog)
                        buyOrders.remove(bestBuyOrder)

                        updateOrderDetailsForSell(
                            order.getUserName(),
                            prevQuantity,
                            order.remainingQuantity,
                            bestBuyOrder,
                            order
                        )
                    } else {
                        val buyOrderLog =
                            OrderFilledLog(
                                bestBuyOrder.remainingQuantity,
                                order.getPrice(),
                                null,
                                order.getUserName(),
                                null
                            )
                        val sellOrderLog = OrderFilledLog(
                            order.remainingQuantity,
                            order.getPrice(),
                            order.esopType,
                            null,
                            bestBuyOrder.getUserName()
                        )

                        bestBuyOrder.remainingQuantity = 0
                        bestBuyOrder.orderStatus = "COMPLETED"
                        bestBuyOrder.orderFilledLogs.add(buyOrderLog)
                        buyOrders.remove(bestBuyOrder)

                        order.remainingQuantity = 0
                        order.orderStatus = "COMPLETED"
                        order.orderFilledLogs.add(sellOrderLog)
                        sellOrders.remove(order)

                        updateOrderDetailsForSell(
                            order.getUserName(),
                            prevQuantity,
                            order.remainingQuantity,
                            bestBuyOrder,
                            order
                        )

                    }
                }
            }
        }
        userRecords.getUser(order.getUserName())?.orderList?.add(order)
        return mapOf("orderId" to order.orderID)
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


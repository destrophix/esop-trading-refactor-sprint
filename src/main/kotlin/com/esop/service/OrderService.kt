package com.esop.service


import com.esop.constant.errors
import com.esop.schema.History
import com.esop.schema.Order
import com.esop.schema.OrderFilledLog
import com.esop.schema.PlatformFee.Companion.addPlatformFee
import jakarta.inject.Singleton
import kotlin.math.round

@Singleton
class OrderService {
    companion object {
        private var orderId = 1L

        var buyOrders = mutableListOf<Order>()
        var sellOrders = mutableListOf<Order>()

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
            val amountToBeDeductedFromLockedState = sellerOrder.price * (prevQuantity - remainingQuantity)
            UserService.userList[userName]!!.userWallet.removeMoneyFromLockedState(amountToBeDeductedFromLockedState)

            // Add money of quantity taken from seller
            var amountToBeAddedToSellersAccount = amountToBeDeductedFromLockedState
            if (sellerOrder.esopType == "NON_PERFORMANCE") {
                amountToBeAddedToSellersAccount -= round(amountToBeDeductedFromLockedState * 0.02).toLong()
                addPlatformFee(round(amountToBeDeductedFromLockedState * 0.02).toLong())

            }
            UserService.userList[sellerOrder.userName]!!.userWallet.addMoneyToWallet(amountToBeAddedToSellersAccount)

            // Deduct inventory of stock from sellers inventory based on its type
            if (sellerOrder.esopType == "PERFORMANCE") {
                UserService.userList[sellerOrder.userName]!!.userPerformanceInventory.removeESOPsFromLockedState(
                    prevQuantity - remainingQuantity
                )
            }

            if (sellerOrder.esopType == "NON_PERFORMANCE") {
                UserService.userList[sellerOrder.userName]!!.userNonPerfInventory.removeESOPsFromLockedState(
                    prevQuantity - remainingQuantity
                )
            }

            // Add purchased inventory to buyer
            UserService.userList[userName]!!.userNonPerfInventory.addESOPsToInventory(prevQuantity - remainingQuantity)

            // Add buyers money back to free from locked
            UserService.userList[userName]!!.userWallet.addMoneyToWallet((buyerOrder.price - sellerOrder.price) * (prevQuantity - remainingQuantity))
            UserService.userList[userName]!!.userWallet.removeMoneyFromLockedState((buyerOrder.price - sellerOrder.price) * (prevQuantity - remainingQuantity))
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
                UserService.userList[userName]!!.userPerformanceInventory.removeESOPsFromLockedState(prevQuantity - remainingQuantity)
            }

            if (sellerOrder.esopType == "NON_PERFORMANCE") {
                UserService.userList[userName]!!.userNonPerfInventory.removeESOPsFromLockedState(prevQuantity - remainingQuantity)
            }

            // Add inventory to buyers stock
            UserService.userList[buyerOrder.userName]!!.userNonPerfInventory.addESOPsToInventory(prevQuantity - remainingQuantity)

            // Deduct money from buyers wallet
            UserService.userList[buyerOrder.userName]!!.userWallet.removeMoneyFromLockedState((sellerOrder.price * (prevQuantity - remainingQuantity)))

            // Add money to sellers wallet
            var totOrderPrice = sellerOrder.price * (prevQuantity - remainingQuantity)
            if (sellerOrder.esopType == "NON_PERFORMANCE") {
                totOrderPrice -= round(totOrderPrice * 0.02).toLong()
                addPlatformFee(round(totOrderPrice * 0.02).toLong())
            }
            UserService.userList[userName]!!.userWallet.addMoneyToWallet(totOrderPrice)

            // Add buyers luck back to free from locked
            UserService.userList[buyerOrder.userName]!!.userWallet.addMoneyToWallet((buyerOrder.price - sellerOrder.price) * (prevQuantity - remainingQuantity))
            UserService.userList[buyerOrder.userName]!!.userWallet.removeMoneyFromLockedState((buyerOrder.price - sellerOrder.price) * (prevQuantity - remainingQuantity))

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

                    if (o1.price == o2.price) {
                        if (o1.timeStamp < o2.timeStamp)
                            return -1
                        return 1
                    }
                    if (o1.price < o2.price)
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
            order.remainingQuantity = order.quantity

            if (order.type == "BUY") {
                buyOrders.add(order)
                val sortedSellOrders = sortAscending()


                for (bestSellOrder in sortedSellOrders) {
                    if (order.remainingQuantity == 0L) {
                        break
                    }
                    if ((order.price >= bestSellOrder.price) && (bestSellOrder.remainingQuantity > 0)) {
                        val prevQuantity = order.remainingQuantity
                        if (order.remainingQuantity < bestSellOrder.remainingQuantity) {

                            val buyOrderLog = OrderFilledLog(
                                order.remainingQuantity,
                                bestSellOrder.price,
                                null,
                                bestSellOrder.userName,
                                null
                            )
                            val sellOrderLog = OrderFilledLog(
                                order.remainingQuantity,
                                bestSellOrder.price,
                                bestSellOrder.esopType,
                                null,
                                order.userName
                            )

                            bestSellOrder.remainingQuantity = bestSellOrder.remainingQuantity - order.remainingQuantity
                            bestSellOrder.orderStatus = "PARTIAL"
                            bestSellOrder.orderFilledLogs.add(sellOrderLog)

                            order.remainingQuantity = 0
                            order.orderStatus = "COMPLETED"
                            order.orderFilledLogs.add(buyOrderLog)
                            buyOrders.remove(order)

                            updateOrderDetailsForBuy(
                                order.userName,
                                prevQuantity,
                                order.remainingQuantity,
                                bestSellOrder,
                                order
                            )

                        } else if (order.remainingQuantity > bestSellOrder.remainingQuantity) {

                            val buyOrderLog = OrderFilledLog(
                                bestSellOrder.remainingQuantity,
                                order.price,
                                null,
                                bestSellOrder.userName,
                                null
                            )
                            val sellOrderLog = OrderFilledLog(
                                bestSellOrder.remainingQuantity,
                                order.price,
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
                                order.userName,
                                prevQuantity,
                                order.remainingQuantity,
                                bestSellOrder,
                                order
                            )
                        } else {
                            val buyOrderLog = OrderFilledLog(
                                bestSellOrder.remainingQuantity,
                                bestSellOrder.price,
                                null,
                                bestSellOrder.userName
                            )
                            val sellOrderLog = OrderFilledLog(
                                order.remainingQuantity,
                                bestSellOrder.price,
                                bestSellOrder.esopType,
                                order.userName,
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
                                order.userName,
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
                    buyOrders.sortedWith(compareByDescending<Order> { it.price }.thenBy { it.timeStamp })

                for (bestBuyOrder in sortedBuyOrders) {
                    if (order.remainingQuantity == 0L) {
                        break
                    }
                    if ((order.price <= bestBuyOrder.price) && (bestBuyOrder.remainingQuantity > 0)) {
                        val prevQuantity = order.remainingQuantity
                        if (order.remainingQuantity < bestBuyOrder.remainingQuantity) {

                            val buyOrderLog =
                                OrderFilledLog(order.remainingQuantity, bestBuyOrder.price, null, order.userName, null)
                            val sellOrderLog = OrderFilledLog(
                                order.remainingQuantity,
                                bestBuyOrder.price,
                                order.esopType,
                                null,
                                bestBuyOrder.userName
                            )

                            bestBuyOrder.remainingQuantity = bestBuyOrder.remainingQuantity - order.remainingQuantity
                            bestBuyOrder.orderStatus = "PARTIAL"
                            bestBuyOrder.orderFilledLogs.add(buyOrderLog)

                            order.remainingQuantity = 0
                            order.orderStatus = "COMPLETED"
                            order.orderFilledLogs.add(sellOrderLog)
                            sellOrders.remove(order)

                            updateOrderDetailsForSell(
                                order.userName,
                                prevQuantity,
                                order.remainingQuantity,
                                bestBuyOrder,
                                order
                            )

                        } else if (order.remainingQuantity > bestBuyOrder.remainingQuantity) {

                            val buyOrderLog =
                                OrderFilledLog(bestBuyOrder.remainingQuantity, order.price, null, order.userName, null)
                            val sellOrderLog = OrderFilledLog(
                                bestBuyOrder.remainingQuantity,
                                order.price,
                                order.esopType,
                                null,
                                bestBuyOrder.userName
                            )


                            order.remainingQuantity = order.remainingQuantity - bestBuyOrder.remainingQuantity
                            order.orderStatus = "PARTIAL"
                            order.orderFilledLogs.add(sellOrderLog)

                            bestBuyOrder.remainingQuantity = 0
                            bestBuyOrder.orderStatus = "COMPLETED"
                            bestBuyOrder.orderFilledLogs.add(buyOrderLog)
                            buyOrders.remove(bestBuyOrder)

                            updateOrderDetailsForSell(
                                order.userName,
                                prevQuantity,
                                order.remainingQuantity,
                                bestBuyOrder,
                                order
                            )
                        } else {
                            val buyOrderLog =
                                OrderFilledLog(bestBuyOrder.remainingQuantity, order.price, null, order.userName, null)
                            val sellOrderLog = OrderFilledLog(
                                order.remainingQuantity,
                                order.price,
                                order.esopType,
                                null,
                                bestBuyOrder.userName
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
                                order.userName,
                                prevQuantity,
                                order.remainingQuantity,
                                bestBuyOrder,
                                order
                            )

                        }
                    }
                }
            }

            UserService.userList[order.userName]?.orderList?.add(order)

            return mapOf("orderId" to order.orderID)
        }

        fun orderHistory(userName: String): Any {
            val userErrors = ArrayList<String>()
            if (!UserService.userList.contains(userName)) {
                errors["USER_DOES_NOT_EXISTS"]?.let { userErrors.add(it) }
                return mapOf("error" to userErrors)
            }
            val orderDetails = UserService.userList[userName]!!.orderList
            val orderHistory = ArrayList<History>()

            for (orders in orderDetails) {
                orderHistory.add(
                    History(
                        orders.orderID,
                        orders.quantity,
                        orders.type,
                        orders.price,
                        orders.orderStatus,
                        orders.orderFilledLogs
                    )
                )
            }

            return orderHistory
        }
    }
}


package com.esop.service


import com.esop.constant.errors
import com.esop.schema.Order
import com.esop.schema.OrderFilledLog
import com.esop.schema.User
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlin.math.round
import com.esop.service.UserService

@Singleton
class OrderService{
    companion object {
        var orderId = 1L;

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
            var amountToBeDeductedFromLockedState = sellerOrder.price * (prevQuantity - remainingQuantity)
            UserService.userList.get(userName)!!.userWallet.removeMoneyFromLockedState(amountToBeDeductedFromLockedState)

            // Add money of quantity taken from seller
            var amountToBeAddedToSellersAccount =
                amountToBeDeductedFromLockedState - round(amountToBeDeductedFromLockedState * 0.02).toLong()
            UserService.userList.get(sellerOrder.userName)!!.userWallet.addMoneyToWallet(amountToBeAddedToSellersAccount)

            // Deduct inventory of stock from sellers inventory based on its type
            if (sellerOrder.inventoryType == "PERFORMANCE") {
                UserService.userList.get(sellerOrder.userName)!!.userPerformanceInventory.moveESOPsFromFreeToLockedState(
                    prevQuantity - remainingQuantity
                )
            }

            if (sellerOrder.inventoryType == "NORMAL") {
                UserService.userList.get(sellerOrder.userName)!!.userNormalInventory.moveESOPsFromFreeToLockedState(
                    prevQuantity - remainingQuantity
                )
            }

            // Add purchased inventory to buyer
            UserService.userList.get(userName)!!.userNormalInventory.addESOPsToInventory(prevQuantity - remainingQuantity)

            // Add buyers money back to free from locked
            UserService.userList.get(userName)!!.userWallet.addMoneyToWallet((buyerOrder.price - sellerOrder.price) * (prevQuantity - remainingQuantity))
            UserService.userList.get(userName)!!.userWallet.removeMoneyFromLockedState((buyerOrder.price - sellerOrder.price) * (prevQuantity - remainingQuantity))
        }

        private fun updateOrderDetailsForSell(
            userName: String,
            prevQuantity: Long,
            remainingQuantity: Long,
            buyerOrder: Order,
            sellerOrder: Order
        ) {

            // Deduct inventory of stock from sellers inventory based on its type
            if (sellerOrder.inventoryType == "PERFORMANCE") {
                UserService.userList.get(userName)!!.userPerformanceInventory.removeESOPsFromLockedState(prevQuantity - remainingQuantity)
            }

            if (sellerOrder.inventoryType == "NORMAL") {
                UserService.userList.get(userName)!!.userNormalInventory.removeESOPsFromLockedState(prevQuantity - remainingQuantity)
            }

            // Add inventory to buyers stock
            UserService.userList.get(buyerOrder.userName)!!.userNormalInventory.addESOPsToInventory(prevQuantity - remainingQuantity)

            // Deduct money from buyers wallet
            UserService.userList.get(buyerOrder.userName)!!.userWallet.removeMoneyFromLockedState((sellerOrder.price * (prevQuantity - remainingQuantity)))

            // Add money to sellers wallet
            val totOrderPrice = sellerOrder.price * (prevQuantity - remainingQuantity)
            UserService.userList.get(userName)!!.userWallet.addMoneyToWallet(totOrderPrice - round(totOrderPrice * 0.02).toLong())

            // Add buyers luck back to free from locked
            UserService.userList.get(buyerOrder.userName)!!.userWallet.addMoneyToWallet((buyerOrder.price - sellerOrder.price) * (prevQuantity - remainingQuantity))
            UserService.userList.get(buyerOrder.userName)!!.userWallet.removeMoneyFromLockedState((buyerOrder.price - sellerOrder.price) * (prevQuantity - remainingQuantity))

        }

        fun placeOrder(order: Order): Map<String, Any> {
            var inventoryPriority = 2
            if (order.inventoryType == "PERFORMANCE") {
                inventoryPriority -= 1
            }
            order.orderID = generateOrderId()
            order.inventoryPriority = inventoryPriority
            UserService.userList.get(order.userName)?.orderList?.add(order)
            if (order.type == "BUY") {
                buyOrders.add(order)
                val sortedSellOrders =
                    sellOrders.sortedWith(compareBy({ it.inventoryPriority }, { it.price }, { it.timeStamp }))
                var remainingQuantity = order.quantity
                for (anOrder in sortedSellOrders) {
                    if ((order.price >= anOrder.price) && (anOrder.orderAvailable())) {
                        val prevQuantity = remainingQuantity
                        remainingQuantity = anOrder.addOrderFilledLogs(remainingQuantity, anOrder.price)
                        if (!anOrder.orderAvailable()) {
                            sellOrders.remove(anOrder)
                        }
                        if (remainingQuantity == 0L) {
                            buyOrders.remove(order)
                            order.addOrderFilledLogs(prevQuantity - remainingQuantity, anOrder.price)
                        } else {
                            order.addOrderFilledLogs(prevQuantity - remainingQuantity, anOrder.price)
                        }
                        updateOrderDetailsForBuy(order.userName, prevQuantity, remainingQuantity, anOrder, order)
                        if (remainingQuantity == 0L) {
                            break
                        }
                    }

                }
            } else {
                sellOrders.add(order)
                val sortedBuyOrders =
                    buyOrders.sortedWith(compareByDescending<Order> { it.price }.thenBy { it.timeStamp })
                var remainingQuantity = order.quantity
                for (anOrder in sortedBuyOrders) {
                    if ((order.price <= anOrder.price) && (anOrder.orderAvailable())) {
                        val prevQuantity = remainingQuantity
                        remainingQuantity = anOrder.addOrderFilledLogs(remainingQuantity, order.price)
                        if (!anOrder.orderAvailable()) {
                            buyOrders.remove(anOrder)
                        }
                        if (remainingQuantity == 0L) {
                            // Order is complete
                            sellOrders.remove(order)
                            order.addOrderFilledLogs(prevQuantity - remainingQuantity, order.price)
                        } else {
                            order.addOrderFilledLogs(prevQuantity - remainingQuantity, order.price)
                        }
                        updateOrderDetailsForSell(order.userName, prevQuantity, remainingQuantity, anOrder, order)
                        if (remainingQuantity == 0L) {
                            break
                        }
                    }
                }
            }
            return mapOf("orderId" to order.orderID)
        }

        fun orderHistory(userName: String): Any {
            val userErrors = ArrayList<String>()
            if (!UserService.userList.contains(userName)) {
                errors["USER_DOES_NOT_EXISTS"]?.let { userErrors.add(it) }
                return mapOf("error" to userErrors)
            }
            val order_history = UserService.userList.get(userName)!!.orderList

            if (order_history.size > 0) {
                return order_history
            }

            errors["NO_ORDERS"]?.let { userErrors.add(it) }
            return mapOf("error" to userErrors)
        }
    }
}


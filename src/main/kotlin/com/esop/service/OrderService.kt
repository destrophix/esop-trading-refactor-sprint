package com.esop.service


import com.esop.constant.errors
import com.esop.schema.*
import jakarta.inject.Singleton
import kotlin.math.round

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
            var amountToBeAddedToSellersAccount = amountToBeDeductedFromLockedState
            if (sellerOrder.inventoryType == "NON_PERFORMANCE") {
                amountToBeAddedToSellersAccount -= round(amountToBeDeductedFromLockedState * 0.02).toLong()
            }
            UserService.userList.get(sellerOrder.userName)!!.userWallet.addMoneyToWallet(amountToBeAddedToSellersAccount)

            // Deduct inventory of stock from sellers inventory based on its type
            if (sellerOrder.inventoryType == "PERFORMANCE") {
                UserService.userList.get(sellerOrder.userName)!!.userPerformanceInventory.removeESOPsFromLockedState(
                    prevQuantity - remainingQuantity
                )
            }

            if (sellerOrder.inventoryType == "NON_PERFORMANCE") {
                UserService.userList.get(sellerOrder.userName)!!.userNonPerfInventory.removeESOPsFromLockedState(
                    prevQuantity - remainingQuantity
                )
            }

            // Add purchased inventory to buyer
            UserService.userList.get(userName)!!.userNonPerfInventory.addESOPsToInventory(prevQuantity - remainingQuantity)

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

            if (sellerOrder.inventoryType == "NON_PERFORMANCE") {
                UserService.userList.get(userName)!!.userNonPerfInventory.removeESOPsFromLockedState(prevQuantity - remainingQuantity)
            }

            // Add inventory to buyers stock
            UserService.userList.get(buyerOrder.userName)!!.userNonPerfInventory.addESOPsToInventory(prevQuantity - remainingQuantity)

            // Deduct money from buyers wallet
            UserService.userList.get(buyerOrder.userName)!!.userWallet.removeMoneyFromLockedState((sellerOrder.price * (prevQuantity - remainingQuantity)))

            // Add money to sellers wallet
            var totOrderPrice = sellerOrder.price * (prevQuantity - remainingQuantity)
            if (sellerOrder.inventoryType == "NON_PERFORMANCE") {
                totOrderPrice -= kotlin.math.round(totOrderPrice * 0.02).toLong()
            }
            UserService.userList.get(userName)!!.userWallet.addMoneyToWallet(totOrderPrice)

            // Add buyers luck back to free from locked
            UserService.userList.get(buyerOrder.userName)!!.userWallet.addMoneyToWallet((buyerOrder.price - sellerOrder.price) * (prevQuantity - remainingQuantity))
            UserService.userList.get(buyerOrder.userName)!!.userWallet.removeMoneyFromLockedState((buyerOrder.price - sellerOrder.price) * (prevQuantity - remainingQuantity))

        }

        private fun sortAscending():List<Order>{
            return sellOrders.sortedWith<Order>(object : Comparator<Order> {
                override fun compare(o1: Order, o2: Order): Int {

                    if(o1.inventoryPriority != o2.inventoryPriority)
                        return o1.inventoryPriority - o2.inventoryPriority

                    if (o1.inventoryPriority == 1) {
                        if(o1.timeStamp < o2.timeStamp)
                            return -1
                        return 1
                    }

                    if(o1.price == o2.price)
                    {
                        if(o1.timeStamp < o2.timeStamp)
                            return -1
                        return 1
                    }
                    if(o1.price < o2.price)
                        return -1
                    return 1
                }
            })
        }

        fun placeOrder(order: Order): Map<String, Any> {
            var inventoryPriority = 2
            if (order.inventoryType == "PERFORMANCE") {
                inventoryPriority -= 1
            }
            order.orderID = generateOrderId()
            order.inventoryPriority = inventoryPriority
            order.remainingQuantity = order.quantity

            if (order.type == "BUY") {
                buyOrders.add(order)
                val sortedSellOrders = sortAscending()

                for (bestSellOrder in sortedSellOrders) {
                    if(order.remainingQuantity == 0L){
                        break
                    }
                    if ((order.price >= bestSellOrder.price) && (bestSellOrder.remainingQuantity > 0)) {
                        if(order.remainingQuantity < bestSellOrder.remainingQuantity){
                            updateOrderDetailsForBuy(order.userName, bestSellOrder.remainingQuantity, order.remainingQuantity, bestSellOrder, order)

                            val buyOrderLog = OrderFilledLog(bestSellOrder.remainingQuantity - order.remainingQuantity,bestSellOrder.price,bestSellOrder.inventoryType)
                            val sellOrderLog = OrderFilledLog(order.remainingQuantity,bestSellOrder.price,bestSellOrder.inventoryType)

                            bestSellOrder.remainingQuantity = bestSellOrder.remainingQuantity - order.remainingQuantity
                            bestSellOrder.orderStatus = "PARTIAL"
                            bestSellOrder.orderFilledLogs.add(sellOrderLog)

                            order.remainingQuantity = 0
                            order.orderStatus = "COMPLETED"
                            order.orderFilledLogs.add(buyOrderLog)
                            buyOrders.remove(order)



                        }else if (order.remainingQuantity > bestSellOrder.remainingQuantity){
                            updateOrderDetailsForBuy(order.userName, order.remainingQuantity ,bestSellOrder.remainingQuantity, bestSellOrder, order)

                            val buyOrderLog = OrderFilledLog(bestSellOrder.remainingQuantity, bestSellOrder.price,bestSellOrder.inventoryType)
                            val sellOrderLog = OrderFilledLog(order.remainingQuantity - bestSellOrder.remainingQuantity, bestSellOrder.price,bestSellOrder.inventoryType)



                            order.remainingQuantity = order.remainingQuantity - bestSellOrder.remainingQuantity
                            order.orderStatus = "PARTIAL"
                            order.orderFilledLogs.add(sellOrderLog)

                            bestSellOrder.remainingQuantity = 0
                            bestSellOrder.orderStatus = "COMPLETED"
                            bestSellOrder.orderFilledLogs.add(buyOrderLog)
                            sellOrders.remove(bestSellOrder)

                        }else{

                            updateOrderDetailsForBuy(order.userName, order.remainingQuantity ,bestSellOrder.remainingQuantity, bestSellOrder, order)

                            val buyOrderLog = OrderFilledLog(bestSellOrder.remainingQuantity, bestSellOrder.price,bestSellOrder.inventoryType)
                            val sellOrderLog = OrderFilledLog(order.remainingQuantity, bestSellOrder.price,bestSellOrder.inventoryType)

                            bestSellOrder.remainingQuantity = 0
                            bestSellOrder.orderStatus = "COMPLETED"
                            bestSellOrder.orderFilledLogs.add(buyOrderLog)
                            sellOrders.remove(bestSellOrder)

                            order.remainingQuantity = 0
                            order.orderStatus = "COMPLETED"
                            order.orderFilledLogs.add(sellOrderLog)
                            buyOrders.remove(order)
                        }
                    }
                }
            } else {
                sellOrders.add(order)
                val sortedBuyOrders = buyOrders.sortedWith(compareByDescending<Order> { it.price }.thenBy { it.timeStamp })

                for (bestBuyOrder in sortedBuyOrders) {
                    if(order.remainingQuantity == 0L){
                        break
                    }
                    if ((order.price <= bestBuyOrder.price) && (bestBuyOrder.remainingQuantity > 0)) {
                        if(order.remainingQuantity < bestBuyOrder.remainingQuantity){
                            updateOrderDetailsForSell(order.userName, bestBuyOrder.remainingQuantity, order.remainingQuantity, bestBuyOrder, order)

                            val buyOrderLog = OrderFilledLog(bestBuyOrder.remainingQuantity - order.remainingQuantity,order.price,order.inventoryType)
                            val sellOrderLog = OrderFilledLog(order.remainingQuantity,order.price,order.inventoryType)

                            bestBuyOrder.remainingQuantity = bestBuyOrder.remainingQuantity - order.remainingQuantity
                            bestBuyOrder.orderStatus = "PARTIAL"
                            bestBuyOrder.orderFilledLogs.add(sellOrderLog)

                            order.remainingQuantity = 0
                            order.orderStatus = "COMPLETED"
                            order.orderFilledLogs.add(buyOrderLog)
                            sellOrders.remove(order)


                        }else if (order.remainingQuantity > bestBuyOrder.remainingQuantity){
                            updateOrderDetailsForSell(order.userName, order.remainingQuantity ,bestBuyOrder.remainingQuantity, bestBuyOrder, order)

                            val buyOrderLog = OrderFilledLog(bestBuyOrder.remainingQuantity, order.price,order.inventoryType)
                            val sellOrderLog = OrderFilledLog(order.remainingQuantity - bestBuyOrder.remainingQuantity, order.price,order.inventoryType)


                            order.remainingQuantity = order.remainingQuantity - bestBuyOrder.remainingQuantity
                            order.orderStatus = "PARTIAL"
                            order.orderFilledLogs.add(sellOrderLog)

                            bestBuyOrder.remainingQuantity = 0
                            bestBuyOrder.orderStatus = "COMPLETED"
                            bestBuyOrder.orderFilledLogs.add(buyOrderLog)
                            buyOrders.remove(bestBuyOrder)
                        }else{
                            updateOrderDetailsForSell(order.userName, order.remainingQuantity ,bestBuyOrder.remainingQuantity, bestBuyOrder, order)

                            val buyOrderLog = OrderFilledLog(bestBuyOrder.remainingQuantity, order.price,order.inventoryType)
                            val sellOrderLog = OrderFilledLog(order.remainingQuantity, order.price,order.inventoryType)

                            bestBuyOrder.remainingQuantity = 0
                            bestBuyOrder.orderStatus = "COMPLETED"
                            bestBuyOrder.orderFilledLogs.add(buyOrderLog)
                            buyOrders.remove(bestBuyOrder)

                            order.remainingQuantity = 0
                            order.orderStatus = "COMPLETED"
                            order.orderFilledLogs.add(sellOrderLog)
                            sellOrders.remove(order)
                        }

                    }
                }
            }

            UserService.userList.get(order.userName)?.orderList?.add(order)

            return mapOf("orderId" to order.orderID)
        }

        fun orderHistory(userName: String): Any {
            val userErrors = ArrayList<String>()
            if (!UserService.userList.contains(userName)) {
                errors["USER_DOES_NOT_EXISTS"]?.let { userErrors.add(it) }
                return mapOf("error" to userErrors)
            }
            val orderDetails = UserService.userList.get(userName)!!.orderList
            var orderHistory = ArrayList<History>()

            if (orderDetails.size > 0) {
                for (orders in orderDetails){
                    orderHistory.add(History(orders.orderID,orders.quantity,orders.type,orders.price,orders.orderStatus,orders.orderFilledLogs))
                }
                return orderHistory
            }

            errors["NO_ORDERS"]?.let { userErrors.add(it) }
            return mapOf("error" to userErrors)
        }
    }
}


package com.esop.service


import com.esop.constant.errors
import com.esop.schema.Order
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlin.math.round

@Singleton
class OrderService{

    @Inject
    lateinit var userService: UserService
    private var all_orders = HashMap<String, ArrayList<Order>>()

    private var orderCount = 1

    private fun checkOrderParameters(quantity: Long, price: Long, type:String): MutableList<String>{
        val userErrors = mutableListOf<String>()
        if(quantity <= 0){
            errors["POSITIVE_QUANTITY"]?.let { userErrors.add(it) }
        }
        if(price <= 0){
            errors["POSITIVE_PRICE"]?.let { userErrors.add(it) }
        }
        if(type != "sell" && type != "buy"){
            errors["INVALID_TYPE"]?.let { userErrors.add(it) }
        }
        return userErrors
    }


    var buyOrders = mutableListOf<Order>()
    var sellOrders = mutableListOf<Order>()

    private fun updateOrderDetailsForBuy(userName: String, prevQuantity: Long, remainingQuantity: Long, sellerOrder: Order, buyerOrder: Order){
        // Deduct money of quantity taken from buyer
        this.userService.allUsers[userName]?.wallet?.locked = this.userService.allUsers[userName]?.wallet?.locked?.minus(
            sellerOrder.price * (prevQuantity - remainingQuantity)
        )!!

        // Add money of quantity taken from seller
        val totOrderPrice = sellerOrder.price * (prevQuantity - remainingQuantity)
        this.userService.allUsers[sellerOrder.userName]?.wallet?.free  = this.userService.allUsers[sellerOrder.userName]?.wallet?.free?.plus(
            totOrderPrice- round(totOrderPrice*0.02).toLong()
        )!!
        // Deduct inventory of stock from sellers inventory based on its type
        if(sellerOrder.inventoryType == "performance"){
            this.userService.allUsers[sellerOrder.userName]?.inventory?.performanceInventory!!.locked = this.userService.allUsers[sellerOrder.userName]?.inventory?.performanceInventory!!.locked?.minus(
                (prevQuantity - remainingQuantity)
            )!!
        }

        if(sellerOrder.inventoryType == "normal"){
            this.userService.allUsers[sellerOrder.userName]?.inventory?.normalInventory!!.locked = this.userService.allUsers[sellerOrder.userName]?.inventory?.normalInventory!!.locked?.minus(
                (prevQuantity - remainingQuantity)
            )!!
        }

        // Add purchased inventory to buyer
        this.userService.allUsers[userName]?.inventory?.normalInventory!!.free = this.userService.allUsers[userName]?.inventory?.normalInventory!!.free.plus(
            (prevQuantity - remainingQuantity)
        )!!
        // Add buyers luck back to free from locked
        this.userService.allUsers[userName]?.wallet?.free = this.userService.allUsers[userName]?.wallet?.free?.plus(
            (buyerOrder.price - sellerOrder.price) * (prevQuantity - remainingQuantity)
        )!!
        this.userService.allUsers[userName]?.wallet?.locked = this.userService.allUsers[userName]?.wallet?.locked?.minus(
            (buyerOrder.price - sellerOrder.price) * (prevQuantity - remainingQuantity)
        )!!
    }

    private fun updateOrderDetailsForSell(userName: String, prevQuantity: Long, remainingQuantity: Long, buyerOrder: Order, sellerOrder: Order){

        // Deduct inventory of stock from sellers inventory based on its type
        if(sellerOrder.inventoryType == "performance"){
            this.userService.allUsers[userName]?.inventory?.performanceInventory!!.locked = this.userService.allUsers[sellerOrder.userName]?.inventory?.performanceInventory!!.locked?.minus(
                (prevQuantity - remainingQuantity)
            )!!
        }

        if(sellerOrder.inventoryType == "normal"){
            this.userService.allUsers[userName]?.inventory?.normalInventory!!.locked = this.userService.allUsers[sellerOrder.userName]?.inventory?.normalInventory!!.locked?.minus(
                (prevQuantity - remainingQuantity)
            )!!
        }

        // Add inventory to buyers stock
        this.userService.allUsers[buyerOrder.userName]?.inventory?.normalInventory!!.free  = this.userService.allUsers[buyerOrder.userName]?.inventory?.normalInventory!!.free?.plus(
            (prevQuantity - remainingQuantity)
        )!!
        // Deduct money from buyers wallet
        this.userService.allUsers[buyerOrder.userName]?.wallet?.locked  = this.userService.allUsers[buyerOrder.userName]?.wallet?.locked?.minus(
            sellerOrder.price * (prevQuantity - remainingQuantity)
        )!!

        // Add money to sellers wallet
        val totOrderPrice = sellerOrder.price * (prevQuantity - remainingQuantity)
        this.userService.allUsers[userName]?.wallet?.free = this.userService.allUsers[userName]?.wallet?.free?.plus(
            totOrderPrice- round(totOrderPrice*0.02).toLong()
        )!!
        // Add buyers luck back to free from locked
        this.userService.allUsers[buyerOrder.userName]?.wallet?.free = this.userService.allUsers[buyerOrder.userName]?.wallet?.free?.plus(
            (buyerOrder.price - sellerOrder.price) * (prevQuantity - remainingQuantity)
        )!!
        this.userService.allUsers[buyerOrder.userName]?.wallet?.locked = this.userService.allUsers[buyerOrder.userName]?.wallet?.locked?.minus(
            (buyerOrder.price - sellerOrder.price) * (prevQuantity - remainingQuantity)
        )!!
    }

    fun placeOrder(userName: String, quantity: Long, type: String, price: Long, inventoryType : String): Map<String, Any> {

        val userErrors = checkOrderParameters(quantity, price, type)
        if (userErrors.isNotEmpty()) {
            // add to list of errors
            return mapOf("error" to userErrors)
        } else {
            var inventoryPriority = 2
            if (inventoryType == "performance") {
                inventoryPriority -= 1
            }
            val userOrder = Order(quantity, type, price, orderCount, userName, inventoryType, inventoryPriority)
            orderCount += 1
            if (!all_orders.containsKey(userName)) {
                all_orders[userName] = ArrayList()
            }
            all_orders[userName]?.add(userOrder)
            if (type == "buy") {
                //altering buy order queue
                buyOrders.add(userOrder)
                val sortedSellOrders =
                    sellOrders.sortedWith(compareBy({ it.inventoryPriority }, { it.price }, { it.timeStamp }))
                var remainingQuantity = userOrder.quantity


                for (anOrder in sortedSellOrders) {
                    if ((userOrder.price >= anOrder.price) && (anOrder.orderAvailable())) {
                        val prevQuantity = remainingQuantity
                        remainingQuantity = anOrder.updateOrderQuantity(remainingQuantity, anOrder.price)
                        if (!anOrder.orderAvailable()) {
                            sellOrders.remove(anOrder)
                        }
                        if (remainingQuantity == 0L) {
                            // Order is complete
                            buyOrders.remove(userOrder)
                            userOrder.updateOrderQuantity(prevQuantity - remainingQuantity, anOrder.price)
                        } else {
                            userOrder.updateOrderQuantity(prevQuantity - remainingQuantity, anOrder.price)
                        }
                        updateOrderDetailsForBuy(userName, prevQuantity, remainingQuantity, anOrder, userOrder)
                        if (remainingQuantity == 0L) {
                            break
                        }
                    }

                }
            } else {
                sellOrders.add(userOrder)
                val sortedBuyOrders =
                    buyOrders.sortedWith(compareByDescending<Order> { it.price }.thenBy { it.timeStamp })
                var remainingQuantity = userOrder.quantity
                for (anOrder in sortedBuyOrders) {
                    if ((userOrder.price <= anOrder.price) && (anOrder.orderAvailable())) {
                        val prevQuantity = remainingQuantity
                        remainingQuantity = anOrder.updateOrderQuantity(remainingQuantity, userOrder.price)
                        if (!anOrder.orderAvailable()) {
                            buyOrders.remove(anOrder)
                        }
                        if (remainingQuantity == 0L) {
                            // Order is complete
                            sellOrders.remove(userOrder)
                            userOrder.updateOrderQuantity(prevQuantity - remainingQuantity, userOrder.price)
                        } else {
                            userOrder.updateOrderQuantity(prevQuantity - remainingQuantity, userOrder.price)
                        }
                        updateOrderDetailsForSell(userName, prevQuantity, remainingQuantity, anOrder, userOrder)
                        if (remainingQuantity == 0L) {
                            break
                        }
                    }
                }
            }
            return mapOf("orderId" to userOrder.orderId)
        }
    }


        fun orderHistory(userName: String): Any {
            val userErrors = ArrayList<String>()
            if (!this.userService.allUsers.contains(userName)) {
                errors["USER_DOES_NOT_EXISTS"]?.let { userErrors.add(it) }
                return mapOf("error" to userErrors)
            }
            val order_history = all_orders[userName]?.toList()

            if (order_history.isNullOrEmpty()) {
                errors["NO_ORDERS"]?.let { userErrors.add(it) }
                return mapOf("error" to userErrors)
            }

            return order_history
        }



}


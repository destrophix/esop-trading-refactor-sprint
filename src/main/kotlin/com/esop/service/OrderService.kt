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

    var buyOrders = mutableListOf<Order>()
    var sellOrders = mutableListOf<Order>()

    private fun updateOrderDetailsForBuy(userName: String, prevQuantity: Long, remainingQuantity: Long, sellerOrder: Order, buyerOrder: Order){
        // Deduct money of quantity taken from buyer
        this.userService.allUsers[userName]?.wallet?.locked = this.userService.allUsers[userName]?.wallet?.locked?.minus(
            sellerOrder.price * (prevQuantity - remainingQuantity)
        )!!

        // Add money of quantity taken from seller
        var totOrderPrice = sellerOrder.price * (prevQuantity - remainingQuantity)
        this.userService.allUsers[sellerOrder.userName]?.wallet?.free  = this.userService.allUsers[sellerOrder.userName]?.wallet?.free?.plus(
            totOrderPrice- round(totOrderPrice*0.02).toLong()
        )!!

        // Deduct inventory of stock from sellers
        this.userService.allUsers[sellerOrder.userName]?.inventory?.locked = this.userService.allUsers[sellerOrder.userName]?.inventory?.locked?.minus(
            (prevQuantity - remainingQuantity)
        )!!
        // Add purchased inventory to buyer
        this.userService.allUsers[userName]?.inventory?.free = this.userService.allUsers[userName]?.inventory?.free?.plus(
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
        // Deduct inventory from sellers stock
        this.userService.allUsers[userName]?.inventory?.locked = this.userService.allUsers[userName]?.inventory?.locked?.minus(
            (prevQuantity - remainingQuantity)
        )!!
        // Add inventory to buyers stock
        this.userService.allUsers[buyerOrder.userName]?.inventory?.free  = this.userService.allUsers[buyerOrder.userName]?.inventory?.free?.plus(
            (prevQuantity - remainingQuantity)
        )!!
        // Deduct money from buyers wallet
        this.userService.allUsers[buyerOrder.userName]?.wallet?.locked  = this.userService.allUsers[buyerOrder.userName]?.wallet?.locked?.minus(
            sellerOrder.price * (prevQuantity - remainingQuantity)
        )!!

        // Add money to sellers wallet
        var totOrderPrice = sellerOrder.price * (prevQuantity - remainingQuantity)
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
    fun placeOrder(userName: String, quantity: Long, type: String, price: Long): Map<String, Any> {
        var userOrder = Order(quantity, type, price, orderCount, userName)
        orderCount += 1
        if(!all_orders.containsKey(userName)){
            all_orders[userName] = ArrayList()
        }
        all_orders[userName]?.add(userOrder)
        if(type == "BUY"){
            buyOrders.add(userOrder)
            var sortedSellOrders = sellOrders.sortedWith(compareBy({it.price}, {it.timeStamp}))
            var remainingQuantity = userOrder.quantity
            for(anOrder in sortedSellOrders){
               if((userOrder.price >= anOrder.price) && (anOrder.orderAvailable())){
                   var prevQuantity = remainingQuantity
                   remainingQuantity = anOrder.updateOrderQuantity(remainingQuantity, anOrder.price)
                   if(!anOrder.orderAvailable()){
                       sellOrders.remove(anOrder)
                   }
                   if(remainingQuantity == 0L){
                       // Order is complete
                       buyOrders.remove(userOrder)
                       userOrder.updateOrderQuantity(prevQuantity - remainingQuantity, anOrder.price)
                   }
                   else{
                       userOrder.updateOrderQuantity(prevQuantity - remainingQuantity, anOrder.price)
                   }
                   updateOrderDetailsForBuy(userName, prevQuantity, remainingQuantity, anOrder, userOrder)
                   if(remainingQuantity == 0L){
                       break
                   }
               }
            }
        }
        else{
            sellOrders.add(userOrder)
            var sortedBuyOrders = buyOrders.sortedWith(compareByDescending<Order> {it.price}.thenBy{it.timeStamp})
            var remainingQuantity = userOrder.quantity
            for(anOrder in sortedBuyOrders){
                if((userOrder.price <= anOrder.price) && (anOrder.orderAvailable())){
                    var prevQuantity = remainingQuantity
                    remainingQuantity = anOrder.updateOrderQuantity(remainingQuantity, userOrder.price)
                    if(!anOrder.orderAvailable()){
                        buyOrders.remove(anOrder)
                    }
                    if(remainingQuantity == 0L){
                        // Order is complete
                        sellOrders.remove(userOrder)
                        userOrder.updateOrderQuantity(prevQuantity - remainingQuantity, userOrder.price)
                    } else{
                        userOrder.updateOrderQuantity(prevQuantity - remainingQuantity, userOrder.price)
                    }
                    updateOrderDetailsForSell(userName, prevQuantity, remainingQuantity, anOrder, userOrder)
                    if(remainingQuantity == 0L){
                        break
                    }
                }
            }
        }
        return mapOf("orderId" to userOrder.orderId)
    }
    fun orderHistory(userName: String): Any {
        var userErrors = ArrayList<String>()
        if(!this.userService.allUsers.contains(userName))
        {
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


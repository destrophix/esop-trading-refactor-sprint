package com.esop.service


import com.esop.constant.errors
import com.esop.schema.Order
import io.micronaut.json.tree.JsonObject
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class OrderService{

    @Inject
    lateinit var userService: UserService
    var all_orders = HashMap<String, ArrayList<Order>>()

    var orderCount = 1

    fun checkOrderParameters(quantity: Long, price: Long, type:String): MutableList<String>{
        var userErrors = mutableListOf<String>()
        if(quantity <= 0){
            userErrors.add("Quantity must be positive.")
        }
        if(price <= 0){
            userErrors.add("Price must be positive.")
        }
        if(type != "SELL" && type != "BUY"){
            userErrors.add("Invalid type.")
        }
        return userErrors
    }

    var buyOrders = mutableListOf<Order>()
    var sellOrders = mutableListOf<Order>()

    fun updateOrderDetailsForBuy(userName: String, prevQuantity: Long, remainingQuantity: Long, anOrder: Order, userOrder: Order){
        // Deduct money of quantity taken from buyer
        this.userService.all_users[userName]?.wallet?.locked = this.userService.all_users[userName]?.wallet?.locked?.minus(
            anOrder.price * (prevQuantity - remainingQuantity)
        )!!
        // Add money of quantity taken from seller
        this.userService.all_users[anOrder.userName]?.wallet?.free  = this.userService.all_users[anOrder.userName]?.wallet?.free?.plus(
            anOrder.price * (prevQuantity - remainingQuantity)
        )!!
        // Deduct inventory of stock from sellers
        this.userService.all_users[anOrder.userName]?.inventory?.locked = this.userService.all_users[anOrder.userName]?.inventory?.locked?.minus(
            (prevQuantity - remainingQuantity)
        )!!
        // Add purchased inventory to buyer
        this.userService.all_users[userName]?.inventory?.free = this.userService.all_users[userName]?.inventory?.free?.plus(
            (prevQuantity - remainingQuantity)
        )!!
        // Add buyers luck back to free from locked
        this.userService.all_users[userName]?.wallet?.free = this.userService.all_users[userName]?.wallet?.free?.plus(
            (userOrder.price - anOrder.price) * (prevQuantity - remainingQuantity)
        )!!
        this.userService.all_users[userName]?.wallet?.locked = this.userService.all_users[userName]?.wallet?.locked?.minus(
            (userOrder.price - anOrder.price) * (prevQuantity - remainingQuantity)
        )!!
    }

    fun updateOrderDetailsForSell(userName: String, prevQuantity: Long, remainingQuantity: Long, anOrder: Order, userOrder: Order){
        // Deduct inventory from sellers stock
        this.userService.all_users[userName]?.inventory?.locked = this.userService.all_users[userName]?.inventory?.locked?.minus(
            (prevQuantity - remainingQuantity)
        )!!
        // Add inventory to buyers stock
        this.userService.all_users[anOrder.userName]?.inventory?.free  = this.userService.all_users[anOrder.userName]?.inventory?.free?.plus(
            (prevQuantity - remainingQuantity)
        )!!
        // Deduct money from buyers wallet
        this.userService.all_users[anOrder.userName]?.wallet?.locked  = this.userService.all_users[anOrder.userName]?.wallet?.locked?.minus(
            userOrder.price * (prevQuantity - remainingQuantity)
        )!!
        // Add money to sellers wallet
        this.userService.all_users[userName]?.wallet?.free = this.userService.all_users[userName]?.wallet?.free?.plus(
            userOrder.price * (prevQuantity - remainingQuantity)
        )!!
        // Add buyers luck back to free from locked
        this.userService.all_users[anOrder.userName]?.wallet?.free = this.userService.all_users[anOrder.userName]?.wallet?.free?.plus(
            (anOrder.price - userOrder.price) * (prevQuantity - remainingQuantity)
        )!!
        this.userService.all_users[anOrder.userName]?.wallet?.locked = this.userService.all_users[anOrder.userName]?.wallet?.locked?.minus(
            (anOrder.price - userOrder.price) * (prevQuantity - remainingQuantity)
        )!!
    }
    fun placeOrder(userName: String, quantity: Long, type: String, price: Long): Map<String, Any> {

        var userErrors = checkOrderParameters(quantity, price, type)
        if(userErrors.isNotEmpty()){
            // add to list of errors
            return mapOf("error" to userErrors)
        }
        else{
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
            println(userOrder)
            println(userOrder.orderId)
            return mapOf("orderId" to userOrder.orderId)
        }
    }
    fun orderHistory(userName: String): Any {
        if(!this.userService.all_users.contains(userName))
        {
            return mapOf("message" to "User does not exist")
        }
        val order_history = all_orders[userName]?.toList()

        if (order_history.isNullOrEmpty()) {
            return mapOf("message" to "User does not have any orders")
        }

        return order_history
    }


}


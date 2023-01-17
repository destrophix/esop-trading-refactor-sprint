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

    fun checkOrderParameters(quantity: Long, price: Long, type:String): Boolean{
        if(quantity > 0 && price > 0 && (type == "BUY" || type =="SELL")){
            return true
        }
        return false
    }

    var buyOrders = mutableListOf<Order>()
    var sellOrders = mutableListOf<Order>()


    fun placeOrder(userName: String, quantity: Long, type: String, price: Long){


        if(!checkOrderParameters(quantity, price, type)){
            // add to list of errors
        }
        else{
            var userOrder = Order(quantity, type, price, orderCount, userName)
            orderCount += 1
            all_orders[userName]?.add(userOrder)
            if(type == "BUY"){
                buyOrders.add(userOrder)
                var sortedSellOrders = sellOrders.sortedWith(compareBy({it.price}, {it.timeStamp}))
                var remainingQuantity = userOrder.quantity
                for(anOrder in sortedSellOrders){
                   if((userOrder.price >= anOrder.price) && (anOrder.orderAvailable())){
                       var prevQuantity = remainingQuantity
                       remainingQuantity = anOrder.updateOrderQuantity(remainingQuantity, userOrder.price)
                       if(!anOrder.orderAvailable()){
                           sellOrders.remove(anOrder)
                       }
                       if(remainingQuantity == 0L){
                           buyOrders.remove(userOrder)
                           break
                       }
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
                            sellOrders.remove(userOrder)
                            break
                        }
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
                        this.userService.all_users[userName]?.wallet?.free = this.userService.all_users[userName]?.wallet?.free?.minus(
                            userOrder.price * (prevQuantity - remainingQuantity)
                        )!!

                    }
                }
            }

        }
    }

    fun orderHistory(userName: String): Any {
        val order_history = all_orders[userName]?.toList()


        if (order_history.isNullOrEmpty()) {
            return mapOf("message" to "User does not have any orders")
        }

        return order_history
    }


}


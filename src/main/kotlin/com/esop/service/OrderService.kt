package com.esop.service


import com.esop.schema.Order
import com.esop.schema.User
import io.micronaut.json.tree.JsonNode
import io.micronaut.json.tree.JsonObject
import jakarta.inject.Singleton
import java.util.regex.Pattern

@Singleton
class OrderService{
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
            var userOrder = Order(quantity, type, price, orderCount)
            orderCount += 1
            all_orders[userName]?.add(userOrder)
            if(type == "BUY"){
                buyOrders.add(userOrder)
                var sortedSellOrders = sellOrders.sortedWith(compareBy({it.price}, {it.timeStamp}))
                for(anOrder in sortedSellOrders){
                   if((userOrder.price >= anOrder.price) && (anOrder.orderAvailable())){
                       anOrder.updateOrderQuantity(userOrder.quantity, userOrder.price)
                   }
                }
            }
            else{
                sellOrders.add(userOrder)
                buyOrders.sortedWith(compareBy({it.price}, {it.timeStamp}))
            }


        }
    }

}


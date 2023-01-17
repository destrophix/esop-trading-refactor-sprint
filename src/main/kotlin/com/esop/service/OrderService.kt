package com.esop.service


import com.esop.schema.Order
import io.micronaut.json.tree.JsonObject
import jakarta.inject.Singleton

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

    fun placeOrder(body: JsonObject, userName: String){

        var quantity: Long = body.get("quantity").longValue
        var type: String = body.get("type").stringValue
        var price: Long = body.get("price").longValue

        if(!checkOrderParameters(quantity, price, type)){
            // add to list of errors
        }
        else{
            var userOrder = Order(quantity, type, price, orderCount)
            orderCount += 1
            if(type == "BUY"){
                buyOrders.add(userOrder)
            }
            else{
                sellOrders.add(userOrder)
            }
            all_orders[userName]?.add(userOrder)

        }
    }

}


package com.esop.service

import com.esop.schema.Order
import com.esop.schema.User
import io.micronaut.json.tree.JsonNode
import java.util.regex.Pattern


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
package com.esop.service

import com.esop.constant.success_response
import com.esop.schema.Order
import io.micronaut.json.tree.JsonObject

var all_orders = HashMap<String, List<Order>>()
var orderID_counter=0;
fun place_order(orderdata: JsonObject):String
{
    //getting all parameters of a placed order
    var quant = orderdata.get("quantity").toString().toInt();
    var type= orderdata.get("type").toString();
    var price = orderdata.get("price").toString().toInt();

    // initialising a object of class Order
    val order= Order(quant,type,price, orderID_counter);
    orderID_counter+=1;   // order ID counter increment

    return success_response["Order placed successfully"].toString()

}
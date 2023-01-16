package com.esop.schema

class OrderFiller{
    var quantity: Int = 0
    var amount: Int = 0
    constructor(quantity: Int, amount: Int){
        this.quantity = quantity
        this.amount = amount
    }
}

class Order{
    var quantity: Int = 0
    var type: String = "NULL" // BUY or SELL
    var price: Int = 0
    var orderId: Int = 0
    var timeStamp = System.currentTimeMillis()

    var orderStatus: String = "PENDING" // COMPLETED, PARTIAL, PENDING
    var currentQuantity: Int = 0
    var filled: MutableList<OrderFiller> = mutableListOf()
    constructor(quantity: Int, type: String, price: Int, orderId: Int){
        this.quantity = quantity
        this.type = type
        this.price = price
        this.orderId = orderId

    }
    fun updateOrderQuantity(quantity: Int, amount: Int){
        // This function will execute when the user's order was partially or fully
        // filled/sold
        if((currentQuantity - quantity) >= 0){
            currentQuantity -= quantity
            if(currentQuantity == 0){
                orderStatus = "COMPLETED"
            }
            else{
                orderStatus = "PARTIAL"
            }
            val newOrder = OrderFiller(quantity, amount)
            filled.add(newOrder)
        }
    }
}
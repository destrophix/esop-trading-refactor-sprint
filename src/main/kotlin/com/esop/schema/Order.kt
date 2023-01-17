package com.esop.schema

class OrderFiller{
    var quantity: Long = 0
    var amount: Long = 0
    constructor(quantity: Long, amount: Long){
        this.quantity = quantity
        this.amount = amount
    }
}

class Order{
    var quantity: Long = 0
    var type: String = "NULL" // BUY or SELL
    var price: Long = 0
    var orderId: Int = 0
    var timeStamp = System.currentTimeMillis()

    var orderStatus: String = "PENDING" // COMPLETED, PARTIAL, PENDING
    var currentQuantity: Long = 0
    var filled: MutableList<OrderFiller> = mutableListOf()
    constructor(quantity: Long, type: String, price: Long, orderId: Int){
        this.quantity = quantity
        this.type = type
        this.price = price
        this.orderId = orderId

    }
    fun orderAvailable():Boolean{
        return orderStatus != "Completed"
    }
    fun updateOrderQuantity(quantity: Long, amount: Long){
        // This function will execute when the user's order was partially or fully
        // filled/sold
        if((currentQuantity - quantity) >= 0){
            currentQuantity -= quantity
            if(currentQuantity == 0L){
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
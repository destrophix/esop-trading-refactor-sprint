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

    var userName: String = ""
    constructor(quantity: Long, type: String, price: Long, orderId: Int, userName: String){
        this.quantity = quantity

        this.type = type
        this.price = price.toInt()
        this.orderId = orderId

        this.userName = userName
    }
    fun orderAvailable():Boolean{
        return orderStatus != "Completed"
    }
    fun updateOrderQuantity(quantity: Long, amount: Long): Long{
        // This function will execute when the user's order was partially or fully
        // filled/sold
        var remainingQuantity = quantity
        val prevQuantity = currentQuantity
        if(currentQuantity>0){
            if(quantity > currentQuantity){
                remainingQuantity -= currentQuantity
                currentQuantity = 0
            }
            else{
                currentQuantity -= quantity
                remainingQuantity = 0L
            }
            if(currentQuantity == 0L){

                orderStatus = "COMPLETED"
            }
            else
                orderStatus = "PARTIAL"
            }

            val newOrder = OrderFiller(prevQuantity - currentQuantity, amount)

            filled.add(newOrder)
        }
        return remainingQuantity
    }
}
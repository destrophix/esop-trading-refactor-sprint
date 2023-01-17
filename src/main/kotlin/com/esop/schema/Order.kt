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

    var userName: String = ""
    constructor(quantity: Long, type: String, price: Long, orderId: Int, userName: String){
        this.currentQuantity = quantity
        this.quantity = quantity

        this.type = type
        this.price = price
        this.orderId = orderId

        this.userName = userName
    }
    fun orderAvailable():Boolean{
        return orderStatus != "Completed"
    }
    fun updateOrderQuantity(quantity: Long, amount: Long): Long{
        // This function will execute when the user's order was partially or fully
        // filled/sold
        println("UPDATE ORDER QUANTITY")
        var remainingQuantity: Long = quantity
        val prevQuantity = currentQuantity
        if(currentQuantity>0){
            println(5)
            if(quantity > currentQuantity){
                println(1)
                remainingQuantity -= currentQuantity
                currentQuantity = 0L
            }
            else{
                println(2)
                currentQuantity -= quantity
                remainingQuantity = 0L
            }
            if(currentQuantity == 0L){
                println(3)
                orderStatus = "COMPLETED"
            }
            else{
                println(4)
                orderStatus = "PARTIAL"
            }

            val newOrder = OrderFiller(prevQuantity - currentQuantity, amount)

            filled.add(newOrder)
        }
        return remainingQuantity
    }
}
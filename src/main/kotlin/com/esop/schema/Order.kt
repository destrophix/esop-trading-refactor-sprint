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
    var inventoryType : String = "normal"
    var inventoryPriority : Int = 2
    var userName: String = ""
    constructor(quantity: Long, type: String, price: Long, orderId: Int, userName: String,inventoryType : String,inventoryPriority : Int){
        this.currentQuantity = quantity
        this.quantity = quantity

        this.type = type
        this.price = price
        this.orderId = orderId
        this.inventoryType = inventoryType
        this.userName = userName
        this.inventoryPriority = inventoryPriority
    }
    fun orderAvailable():Boolean{
        return orderStatus != "Completed"
    }
    fun updateOrderQuantity(givenQuantity: Long, amount: Long): Long{
        // This function will execute when the user's order was partially or fully
        // filled/sold
        var remainingQuantity: Long = givenQuantity
        val prevQuantity = currentQuantity
        if(currentQuantity>0){
            if(givenQuantity > currentQuantity){
                remainingQuantity -= currentQuantity
                currentQuantity = 0L
            }
            else{
                currentQuantity -= givenQuantity
                remainingQuantity = 0L
            }
            if(currentQuantity == 0L){
                orderStatus = "COMPLETED"
            }
            else if(this.quantity != currentQuantity){
                orderStatus = "PARTIAL"
            }
            if(prevQuantity - currentQuantity != 0L )  
            {
                val newOrder = OrderFiller(prevQuantity - currentQuantity, amount)
                filled.add(newOrder)
            }
        }
        return remainingQuantity
    }
}
package com.esop.schema

class Order(
    var quantity: Long,
    var type: String,
    var price: Long,
    var userName: String
)
{
    var timeStamp = System.currentTimeMillis()
    var currentQuantity: Long = quantity
    var orderStatus: String = "PENDING" // COMPLETED, PARTIAL, PENDING
    var orderFilledLogs: MutableList<OrderFilledLog> = mutableListOf()
    var orderID: Long = -1
    var inventoryType = "NON_PERFORMANCE"
    var inventoryPriority = 2

    fun orderAvailable():Boolean{
        return orderStatus != "COMPLETED"
    }
    fun addOrderFilledLogs(givenQuantity:Long,amount: Long): Long {
        var remainingQuantity: Long = givenQuantity
        var prevQuantity = currentQuantity
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
                val newOrder = OrderFilledLog(prevQuantity - currentQuantity, amount)
                orderFilledLogs.add(newOrder)
            }
        }
        return remainingQuantity
    }
}
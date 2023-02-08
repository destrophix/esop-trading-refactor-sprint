package com.esop.schema

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class OrderTest{
    @Test
    fun `it should update remaining quantity`(){
        val buy = Order(10,"BUY",10,"sankar")
        val expectedRemainingQuantity = 5L

        buy.subtractFromRemainingQuantity(5L)

        assertEquals(expectedRemainingQuantity,buy.getRemainingQuantity())
    }

    @Test
    fun `it should add order log`(){
        val buyOrder = Order(10,"BUY",10,"Sankar")
        val buyOrderLog = OrderFilledLog(
            10,
            10,
            null,
            "Sankar",
            null
        )

        buyOrder.addOrderFilledLogs(buyOrderLog)

        assertEquals(1,buyOrder.getOrderFilledLogs().size)
    }
}
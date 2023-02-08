package com.esop.schema

import com.esop.repository.UserRecords
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OrderTest{
    lateinit var userRecords: UserRecords

    @BeforeEach
    fun setUp() {
        userRecords = UserRecords()
    }

    @Test
    fun `it should update remaining quantity`(){
        val user = User("Sankaranarayanan", "M", "7550276216", "sankaranarayananm@sahaj.ai", "sankar")
        userRecords.addUser(user)

        val buy = Order(10,"BUY",10,user)
        val expectedRemainingQuantity = 5L

        buy.subtractFromRemainingQuantity(5L)

        assertEquals(expectedRemainingQuantity,buy.getRemainingQuantity())
    }

    @Test
    fun `it should add order log`(){
        val user = User("Sankaranarayanan", "M", "7550276216", "sankaranarayananm@sahaj.ai", "sankar")
        userRecords.addUser(user)

        val buyOrder = Order(10,"BUY",10,user)
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
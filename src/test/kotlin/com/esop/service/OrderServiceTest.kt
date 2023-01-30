package com.esop.service

import com.esop.schema.Order
import com.esop.schema.User
import com.esop.service.OrderService.Companion.buyOrders
import com.esop.service.OrderService.Companion.placeOrder
import com.esop.service.OrderService.Companion.sellOrders
import com.esop.service.UserService.Companion.userList
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OrderServiceTest {
    @BeforeEach
    fun `It should create user`() {
        val buyer1 = User("Sankaranarayanan", "M", "7550276216", "sankaranarayananm@sahaj.ai", "sankar")
        val buyer2 = User("Aditya", "Tiwari", "", "aditya@sahaj.ai", "aditya")
        val seller1 = User("Kajal", "Pawar", "", "kajal@sahaj.ai", "kajal")
        val seller2 = User("Arun", "Murugan", "", "arun@sahaj.ai", "arun")

        userList["sankar"] = buyer1
        userList["aditya"] = buyer2
        userList["kajal"] = seller1
        userList["arun"] = seller2
    }

    @AfterEach
    fun `It should clear the in memory data`() {
        buyOrders.clear()
        sellOrders.clear()
        userList.clear()
    }

    @Test
    fun `It should place BUY order`() {
        //Arrange
        val buyOrder = Order(10, "BUY", 10, "sankar")

        //Act
        placeOrder(buyOrder)

        //Assert
        assertTrue(buyOrders.contains(buyOrder))
    }

    @Test
    fun `It should place SELL order`() {
        val sellOrder = Order(10, "SELL", 10, "kajal")


        placeOrder(sellOrder)


        assertTrue(sellOrders.contains(sellOrder))
    }

    @Test
    fun `It should match BUY order for existing SELL order`() {
        userList["kajal"]!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrder = Order(10, "SELL", 10, "kajal")
        userList["kajal"]!!.userNonPerfInventory.moveESOPsFromFreeToLockedState(10)
        placeOrder(sellOrder)

        userList["sankar"]!!.userWallet.addMoneyToWallet(100)
        val buyOrder = Order(10, "BUY", 10, "sankar")
        userList["sankar"]!!.userWallet.moveMoneyFromFreeToLockedState(100)


        placeOrder(buyOrder)


        assertEquals(40, userList["kajal"]!!.userNonPerfInventory.getFreeInventory())
        assertEquals(10, userList["sankar"]!!.userNonPerfInventory.getFreeInventory())
        assertEquals(98, userList["kajal"]!!.userWallet.getFreeMoney())
        assertEquals(0, userList["sankar"]!!.userWallet.getFreeMoney())
    }

    @Test
    fun `It should place 2 SELL orders followed by a BUY order where the BUY order is partial`() {
        userList["kajal"]!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByKajal = Order(10, "SELL", 10, "kajal")
        userList["kajal"]!!.userNonPerfInventory.moveESOPsFromFreeToLockedState(10)
        placeOrder(sellOrderByKajal)

        userList["arun"]!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByArun = Order(10, "SELL", 10, "arun")
        userList["arun"]!!.userNonPerfInventory.moveESOPsFromFreeToLockedState(10)
        placeOrder(sellOrderByArun)

        userList["sankar"]!!.userWallet.addMoneyToWallet(250)
        val buyOrderBySankar = Order(25, "BUY", 10, "sankar")
        userList["sankar"]!!.userWallet.moveMoneyFromFreeToLockedState(250)


        placeOrder(buyOrderBySankar)


        assertEquals(40, userList["kajal"]!!.userNonPerfInventory.getFreeInventory())
        assertEquals(40, userList["arun"]!!.userNonPerfInventory.getFreeInventory())
        assertEquals(20, userList["sankar"]!!.userNonPerfInventory.getFreeInventory())
        assertEquals(98, userList["kajal"]!!.userWallet.getFreeMoney())
        assertEquals(98, userList["arun"]!!.userWallet.getFreeMoney())
        assertEquals(50, userList["sankar"]!!.userWallet.getLockedMoney())
        assertEquals("PARTIAL", buyOrders[buyOrders.indexOf(buyOrderBySankar)]!!.orderStatus)
        assertEquals(
            "COMPLETED",
            userList["kajal"]!!.orderList[userList["kajal"]!!.orderList.indexOf(sellOrderByKajal)].orderStatus
        )
        assertEquals(
            "COMPLETED",
            userList["arun"]!!.orderList[userList["arun"]!!.orderList.indexOf(sellOrderByArun)].orderStatus
        )
    }

    @Test
    fun `It should place 2 SELL orders followed by a BUY order where the BUY order is complete`() {
        userList["kajal"]!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByKajal = Order(10, "SELL", 10, "kajal")
        userList["kajal"]!!.userNonPerfInventory.moveESOPsFromFreeToLockedState(10)
        placeOrder(sellOrderByKajal)

        userList["arun"]!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByArun = Order(10, "SELL", 10, "arun")
        userList["arun"]!!.userNonPerfInventory.moveESOPsFromFreeToLockedState(10)
        placeOrder(sellOrderByArun)

        userList["sankar"]!!.userWallet.addMoneyToWallet(250)
        val buyOrderBySankar = Order(20, "BUY", 10, "sankar")
        userList["sankar"]!!.userWallet.moveMoneyFromFreeToLockedState(200)


        placeOrder(buyOrderBySankar)


        assertEquals(40, userList["kajal"]!!.userNonPerfInventory.getFreeInventory())
        assertEquals(40, userList["arun"]!!.userNonPerfInventory.getFreeInventory())
        assertEquals(20, userList["sankar"]!!.userNonPerfInventory.getFreeInventory())
        assertEquals(98, userList["kajal"]!!.userWallet.getFreeMoney())
        assertEquals(98, userList["arun"]!!.userWallet.getFreeMoney())
        assertEquals(0, userList["sankar"]!!.userWallet.getLockedMoney())
        assertEquals(
            "COMPLETED",
            userList["sankar"]!!.orderList[userList["sankar"]!!.orderList.indexOf(buyOrderBySankar)].orderStatus
        )
        assertEquals(
            "COMPLETED",
            userList["kajal"]!!.orderList[userList["kajal"]!!.orderList.indexOf(sellOrderByKajal)].orderStatus
        )
        assertEquals(
            "COMPLETED",
            userList["arun"]!!.orderList[userList["arun"]!!.orderList.indexOf(sellOrderByArun)].orderStatus
        )
    }
}

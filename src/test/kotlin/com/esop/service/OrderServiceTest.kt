package com.esop.service

import com.esop.repository.UserRecords
import com.esop.schema.Order
import com.esop.schema.User
import com.esop.service.OrderService.Companion.buyOrders
import com.esop.service.OrderService.Companion.sellOrders
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OrderServiceTest {

    private lateinit var userRecords:UserRecords
    private lateinit var orderService:OrderService

    @BeforeEach
    fun `It should create user`() {
        userRecords = UserRecords()
        orderService = OrderService(userRecords)

        val buyer1 = User("Sankaranarayanan", "M", "7550276216", "sankaranarayananm@sahaj.ai", "sankar")
        val buyer2 = User("Aditya", "Tiwari", "", "aditya@sahaj.ai", "aditya")
        val seller1 = User("Kajal", "Pawar", "", "kajal@sahaj.ai", "kajal")
        val seller2 = User("Arun", "Murugan", "", "arun@sahaj.ai", "arun")

        userRecords.addUser(buyer1)
        userRecords.addUser(buyer2)
        userRecords.addUser(seller1)
        userRecords.addUser(seller2)
    }

    @AfterEach
    fun `It should clear the in memory data`() {
        buyOrders.clear()
        sellOrders.clear()
    }

    @Test
    fun `It should place BUY order`() {
        //Arrange
        val buyOrder = Order(10, "BUY", 10, "sankar")

        //Act
        orderService.placeOrder(buyOrder)

        //Assert
        assertTrue(buyOrders.contains(buyOrder))
    }

    @Test
    fun `It should place SELL order`() {
        //Arrange
        val sellOrder = Order(10, "SELL", 10, "kajal")

        //Act
        orderService.placeOrder(sellOrder)

        //Assert
        assertTrue(sellOrders.contains(sellOrder))
    }

    @Test
    fun `It should match BUY order for existing SELL order`() {
        //Arrange
        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrder = Order(10, "SELL", 10, "kajal")
        userRecords.getUser("kajal")!!.userNonPerfInventory.moveESOPsFromFreeToLockedState(10)
        orderService.placeOrder(sellOrder)

        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(100)
        val buyOrder = Order(10, "BUY", 10, "sankar")
        userRecords.getUser("sankar")!!.userWallet.moveMoneyFromFreeToLockedState(100)

        //Act
        orderService.placeOrder(buyOrder)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(10, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(98, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getFreeMoney())
    }

    @Test
    fun `It should place 2 SELL orders followed by a BUY order where the BUY order is partial`() {
        //Arrange
        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByKajal = Order(10, "SELL", 10, "kajal")
        userRecords.getUser("kajal")!!.userNonPerfInventory.moveESOPsFromFreeToLockedState(10)
        orderService.placeOrder(sellOrderByKajal)

        userRecords.getUser("arun")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByArun = Order(10, "SELL", 10, "arun")
        userRecords.getUser("arun")!!.userNonPerfInventory.moveESOPsFromFreeToLockedState(10)
        orderService.placeOrder(sellOrderByArun)

        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(250)
        val buyOrderBySankar = Order(25, "BUY", 10, "sankar")
        userRecords.getUser("sankar")!!.userWallet.moveMoneyFromFreeToLockedState(250)

        //Act
        orderService.placeOrder(buyOrderBySankar)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(40, userRecords.getUser("arun")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(20, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(98, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(98, userRecords.getUser("arun")!!.userWallet.getFreeMoney())
        assertEquals(50, userRecords.getUser("sankar")!!.userWallet.getLockedMoney())
        assertEquals("PARTIAL", buyOrders[buyOrders.indexOf(buyOrderBySankar)].orderStatus)
        assertEquals(
            "COMPLETED",
            userRecords.getUser("kajal")!!.orderList[userRecords.getUser("kajal")!!.orderList.indexOf(sellOrderByKajal)].orderStatus
        )
        assertEquals(
            "COMPLETED",
            userRecords.getUser("arun")!!.orderList[userRecords.getUser("arun")!!.orderList.indexOf(sellOrderByArun)].orderStatus
        )
    }

    @Test
    fun `It should place 2 SELL orders followed by a BUY order where the BUY order is complete`() {
        //Arrange
        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByKajal = Order(10, "SELL", 10, "kajal")
        userRecords.getUser("kajal")!!.userNonPerfInventory.moveESOPsFromFreeToLockedState(10)
        orderService.placeOrder(sellOrderByKajal)

        userRecords.getUser("arun")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByArun = Order(10, "SELL", 10, "arun")
        userRecords.getUser("arun")!!.userNonPerfInventory.moveESOPsFromFreeToLockedState(10)
        orderService.placeOrder(sellOrderByArun)

        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(250)
        val buyOrderBySankar = Order(20, "BUY", 10, "sankar")
        userRecords.getUser("sankar")!!.userWallet.moveMoneyFromFreeToLockedState(200)

        //Act
        orderService.placeOrder(buyOrderBySankar)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(40, userRecords.getUser("arun")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(20, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(98, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(98, userRecords.getUser("arun")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getLockedMoney())
        assertEquals(
            "COMPLETED",
            userRecords.getUser("sankar")!!.orderList[userRecords.getUser("sankar")!!.orderList.indexOf(buyOrderBySankar)].orderStatus
        )
        assertEquals(
            "COMPLETED",
            userRecords.getUser("kajal")!!.orderList[userRecords.getUser("kajal")!!.orderList.indexOf(sellOrderByKajal)].orderStatus
        )
        assertEquals(
            "COMPLETED",
            userRecords.getUser("arun")!!.orderList[userRecords.getUser("arun")!!.orderList.indexOf(sellOrderByArun)].orderStatus
        )
    }

    @Test
    fun `It should place 1 SELL orders followed by a BUY order where the BUY order is complete`() {
        //Arrange
        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByKajal = Order(10, "SELL", 10, "kajal")
        userRecords.getUser("kajal")!!.userNonPerfInventory.moveESOPsFromFreeToLockedState(10)
        orderService.placeOrder(sellOrderByKajal)


        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(250)
        val buyOrderBySankar = Order(5, "BUY", 10, "sankar")
        userRecords.getUser("sankar")!!.userWallet.moveMoneyFromFreeToLockedState(50)

        //Act
        orderService.placeOrder(buyOrderBySankar)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(5, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(49, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getLockedMoney())
        assertEquals(
            "COMPLETED",
            userRecords.getUser("sankar")!!.orderList[userRecords.getUser("sankar")!!.orderList.indexOf(buyOrderBySankar)].orderStatus
        )
        assertEquals(
            "PARTIAL",
            userRecords.getUser("kajal")!!.orderList[userRecords.getUser("kajal")!!.orderList.indexOf(sellOrderByKajal)].orderStatus
        )
    }

    @Test
    fun `It should place 1 SELL orders followed by a BUY order where the BUY order is partial`() {
        //Arrange
        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByKajal = Order(10, "SELL", 10, "kajal")
        userRecords.getUser("kajal")!!.userNonPerfInventory.moveESOPsFromFreeToLockedState(10)
        orderService.placeOrder(sellOrderByKajal)


        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(250)
        val buyOrderBySankar = Order(15, "BUY", 10, "sankar")
        userRecords.getUser("sankar")!!.userWallet.moveMoneyFromFreeToLockedState(150)

        //Act
        orderService.placeOrder(buyOrderBySankar)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(10, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(98, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(50, userRecords.getUser("sankar")!!.userWallet.getLockedMoney())
        assertEquals(
            "PARTIAL",
            userRecords.getUser("sankar")!!.orderList[userRecords.getUser("sankar")!!.orderList.indexOf(buyOrderBySankar)].orderStatus
        )
        assertEquals(
            "COMPLETED",
            userRecords.getUser("kajal")!!.orderList[userRecords.getUser("kajal")!!.orderList.indexOf(sellOrderByKajal)].orderStatus
        )
    }

    @Test
    fun `It should place 2 BUY orders followed by a SELL order where the SELL order is partial`() {
        //Arrange
        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(100)
        val buyOrderBySankar = Order(10, "BUY", 10, "sankar")
        userRecords.getUser("sankar")!!.userWallet.moveMoneyFromFreeToLockedState(100)
        orderService.placeOrder(buyOrderBySankar)


        userRecords.getUser("aditya")!!.userWallet.addMoneyToWallet(100)
        val buyOrderByAditya = Order(10, "BUY", 10, "aditya")
        userRecords.getUser("sankar")!!.userWallet.moveMoneyFromFreeToLockedState(100)
        orderService.placeOrder(buyOrderByAditya)

        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByKajal = Order(25, "SELL", 10, "kajal")
        userRecords.getUser("kajal")!!.userNonPerfInventory.moveESOPsFromFreeToLockedState(25)

        //Act
        orderService.placeOrder(sellOrderByKajal)

        //Assert
        assertEquals(25, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(10, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(10, userRecords.getUser("aditya")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(196, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getFreeMoney())
        assertEquals("PARTIAL", sellOrders[sellOrders.indexOf(sellOrderByKajal)].orderStatus)
        assertEquals(
            "COMPLETED",
            userRecords.getUser("sankar")!!.orderList[userRecords.getUser("sankar")!!.orderList.indexOf(buyOrderBySankar)].orderStatus
        )
        assertEquals(
            "COMPLETED",
            userRecords.getUser("aditya")!!.orderList[userRecords.getUser("aditya")!!.orderList.indexOf(buyOrderByAditya)].orderStatus
        )
    }

    @Test
    fun `It should place 2 BUY orders followed by a SELL order where the SELL order is complete`() {
        //Arrange
        userRecords.getUser("kajal")!!.userWallet.addMoneyToWallet(100)
        val buyOrderByKajal = Order(10, "BUY", 10, "kajal")
        userRecords.getUser("kajal")!!.userWallet.moveMoneyFromFreeToLockedState(10 * 10)
        orderService.placeOrder(buyOrderByKajal)

        userRecords.getUser("arun")!!.userWallet.addMoneyToWallet(100)
        val buyOrderByArun = Order(10, "BUY", 10, "arun")
        userRecords.getUser("arun")!!.userWallet.moveMoneyFromFreeToLockedState(10 * 10)
        orderService.placeOrder(buyOrderByArun)

        userRecords.getUser("sankar")!!.userNonPerfInventory.addESOPsToInventory(30)
        val sellOrderBySankar = Order(20, "SELL", 10, "sankar")
        userRecords.getUser("sankar")!!.userNonPerfInventory.moveESOPsFromFreeToLockedState(20)

        //Act
        orderService.placeOrder(sellOrderBySankar)

        //Assert
        assertEquals(10, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(0, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())

        assertEquals(10, userRecords.getUser("arun")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(0, userRecords.getUser("arun")!!.userWallet.getFreeMoney())

        assertEquals(10, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(98 + 98, userRecords.getUser("sankar")!!.userWallet.getFreeMoney())

        assertEquals(
            "COMPLETED",
            userRecords.getUser("sankar")!!.orderList[userRecords.getUser("sankar")!!.orderList.indexOf(sellOrderBySankar)].orderStatus
        )
        assertEquals(
            "COMPLETED",
            userRecords.getUser("kajal")!!.orderList[userRecords.getUser("kajal")!!.orderList.indexOf(buyOrderByKajal)].orderStatus
        )
        assertEquals(
            "COMPLETED",
            userRecords.getUser("arun")!!.orderList[userRecords.getUser("arun")!!.orderList.indexOf(buyOrderByArun)].orderStatus
        )
    }

}
